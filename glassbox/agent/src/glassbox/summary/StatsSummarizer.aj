/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.summary;

import glassbox.response.*;
import glassbox.track.ThreadStats;
import glassbox.track.api.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StatsSummarizer implements ResponseListener {

    public static final String PERF_STATS ="perf.stats";
    private static final Integer ZERO = new Integer(0);
    
    private ThreadStats threadStats;
    private static final Map layerMap = makeLayerMap();

    public pointcut startStats(PerfStats stats, Response response, StatsSummarizer summarizer) : 
        within(StatsSummarizer) && execution(* startedStats(..)) && args(stats, *, response, ..) && this(summarizer);
    
    public pointcut endStats(PerfStats stats, Response response, StatsSummarizer summarizer) : 
        within(StatsSummarizer) && execution(* endedStats(..)) && args(stats, *, response, ..) && this(summarizer);

    public pointcut startTopLevelStats(PerfStats stats, Response response, StatsSummarizer summarizer) :
        startStats(stats, response, summarizer) && !args(*, PerfStats, ..);

    public pointcut endTopLevelStats(PerfStats stats, Response response, StatsSummarizer summarizer) :
        endStats(stats, response, summarizer) && !args(*, PerfStats, ..);
    
    public pointcut startNestedStats(PerfStats stats, Response response, StatsSummarizer summarizer) :
        startStats(stats, response, summarizer) && args(*, PerfStats, ..);
    
    public pointcut endNestedStats(PerfStats stats, Response response, StatsSummarizer summarizer) :
        endStats(stats, response, summarizer) && args(*, PerfStats, ..);
    
    /** asymmetrical with end: here we only know there will be a later operation somewhere down from this node */
    public pointcut startNestedLaterOperation(PerfStats stats, Response response, StatsSummarizer summarizer) :
        startNestedStats(stats, response, summarizer) && if(stats.isOperationKey() && summarizer.getPrimaryOperationStats()!=null);
    
    /** here we know that THESE stats count as an operation for this request */
    public pointcut endNestedLaterOperation(PerfStats stats, Response response, StatsSummarizer summarizer) :
        endNestedStats(stats, response, summarizer) && if(stats.isOperationThisRequest() && summarizer.getPrimaryOperationStats()!=stats);
    
    public void clearStats() {
        threadStats.peek().clear();
    }
    
    public void startedResponse(Response response) {
        if (response == null) {
            logError("Null response", new Exception());
            return;
        }
        
        StatisticsRegistry registry = threadStats.peek();
        StatisticsType statsType = getStats(response.getLayer());

        Response parent = response.getParent();
        Serializable key = response.getKey();
        Serializable resKey = (Serializable)response.get(Response.RESOURCE_KEY);
        boolean nestedOperation = false;
        
        if (resKey != null) {
            CompositePerfStats resStats = (CompositePerfStats)registry.getPerfStats(statsType.getParentStatsType(), resKey);
            threadStats.push(resStats);
            registry = resStats;
        } else if ((key instanceof OperationDescription) && (parent != null)) {
            Serializable parentKey = parent.getKey();
            if (parentKey instanceof OperationDescription) {
                if (parentKey.equals(key)) {
                    // forward the parent key...
                    ((OperationDescription)key).setParent(((OperationDescription)parentKey).getParent());
                    
                    // ... but don't record anything for duplicate                    
                    return;
                }
                ((OperationDescription)key).setParent((OperationDescription)parentKey);
                nestedOperation = true;
            }
        }

        PerfStats stats = registry.getPerfStats(statsType, key);
        if (stats instanceof OperationPerfStats) { // occasionally tree stats can appear here first and they have  
            if (isOperationKey(response)) {
                threadStats.setLastOperationKey((OperationPerfStats)stats);
            } else if (nestedOperation) {
                OperationPerfStats operationStats = (OperationPerfStats)stats; 
                operationStats.setOperationKey(false);
            }
        }
        
        threadStats.push(stats);
        stats.recordStart(response.getStart());

        // we need to set up any data that might be written during processing before we start summarizing, since from then on we can read data on another thread
        response.set(PerfStatsImpl.RECORDED, ZERO);        
        ensure(response, Response.FAILURE_DATA);
        ensure(response, Response.REQUEST);
        
        startedStats(stats, registry, response);
    }
    
    protected void ensure(Response response, String key) {
        if (response.get(key) == null) {
            response.set(key, null);
        }
    }
    
    private int indent = 0;

    public void finishedResponse(Response response) {
        if (response == null) {
            logError("Null response", new Exception());
            return;
        }
        
        Response parent = response.getParent();
        Serializable key = response.getKey();
        if (parent!=null) {
            Serializable parentKey = parent.getKey();
            if (key==parentKey || (key!=null && key.equals(parentKey))) {
                return; // skip duplicate
            }
        }            
        
        OperationPerfStats lastStats = threadStats.getLastOperationKey();
        
        PerfStats stats = (PerfStats) threadStats.pop();
        if (stats == null) {
            throw new IllegalStateException("finishing response that wasn't started");
        }
        
        if (isOperationKey(response)) {
            OperationPerfStats firstOperation = threadStats.getFirstOperationKey();
            if (lastStats==stats) {                
                lastStats.recordAsOperation();
                if (firstOperation == null) {
                    threadStats.setFirstOperationKey(lastStats);
                }
            } else {
                // not yet handled: containing responses that aren't operations, e.g., servlet filters
                if (firstOperation != null) { // should always be true
                    firstOperation.recordDispatch(response);
                }
            }
//        } else if (response.getKey() instanceof OperationDescription) {
//            if (firstOperation != null) { // should always be true
//                firstOperation.recordDispatch(response);
//            }
        }            
     
        stats.recordEnd(response);
        
        if (response.get(Response.RESOURCE_KEY) != null) {
            stats = (PerfStats) threadStats.pop();
        }
        
        endedStats(stats, threadStats.peek(), response);
    }

    protected void checkMismatched(Response response) {
        if (response.getParent() == null) {
            ResponseFactory factory = response.getFactory();
            if (factory.getLastResponse() != null) {
                logError("Monitoring problem: mismatched monitor calls for  "+response.getKey());
                do {
                    response = factory.getLastResponse();
                    logInfo("Recording completed response for "+response.getKey()); 
                    response.complete();
                } while (factory.getLastResponse() != null);                    
            }            
        }
    }
    
    protected boolean isOperationKey(Response response) {
        if (!(response.getKey() instanceof OperationDescription)) {
            return false;
        }
        
        Response parent = response.getParent();
        while (parent != null) {
            Serializable parentKey = parent.getKey();
            if (parentKey instanceof OperationDescription) {
                if (priority(response) <= priority(parent)) {
                    return false;
                }
            }
            parent = parent.getParent();
        }
        return true;
    }
    
    protected int priority(Response response) {
        Object priority = response.get(Response.OPERATION_PRIORITY);
        if (priority instanceof Integer) {
            return ((Integer)priority).intValue();
        }
        return Response.DEFAULT_PRIORITY.intValue();
    }
    
    protected void help(PerfStats stats, Object parent) {
    }
    
    protected void startedStats(PerfStats stats, Object parent, Response response) {
    }
    
    protected void endedStats(PerfStats stats, Object parent, Response response) {
        checkMismatched(response);        
    }
    
    /**
     * @return the threadStats
     */
    public ThreadStats getThreadStats() {
        return threadStats;
    }

    /**
     * @param threadStats the threadStats to set
     */
    public void setThreadStats(ThreadStats threadStats) {
        this.threadStats = threadStats;
    }

    private static Map makeLayerMap() {
        Map map = new HashMap();
        map.put(Response.UI_CONTROLLER, StatisticsTypeImpl.UiRequest);
        map.put(Response.UI_RENDERER, StatisticsTypeImpl.SlowMethod);
        map.put(Response.UI_MODEL, StatisticsTypeImpl.SlowMethod);
        map.put(Response.SERVICE_PROCESSOR, StatisticsTypeImpl.UiRequest);
        map.put(Response.PROCESSING, StatisticsTypeImpl.SlowMethod);
        map.put(Response.RESOURCE_DATABASE, StatisticsTypeImpl.Database);
        map.put(Response.RESOURCE_DATABASE_STATEMENT, StatisticsTypeImpl.DatabaseStatement);
        map.put(Response.RESOURCE_DATABASE_CONNECTION, StatisticsTypeImpl.DatabaseConnection);
        map.put(Response.RESOURCE_SERVICE, StatisticsTypeImpl.RemoteCall);
        map.put(Response.RESOURCE_FILE, StatisticsTypeImpl.RemoteCall);
        return map;
    }
    
    private StatisticsType getStats(Serializable layer) {
        try {
            StatisticsType statsType = (StatisticsType)layerMap.get(layer);
            if (statsType == null) {
                //default
                return StatisticsTypeImpl.SlowMethod;
            }
            return statsType;
        } catch (NullPointerException npe) {
            // this shouldn't happen: gather data to fix it...
            String sz = (layerMap == null ? "null" : ("of size "+layerMap.size()));
            logError("Can't get statistics type: NPE for "+layer+", map is "+sz);
            return StatisticsTypeImpl.SlowMethod;
        }
    }
    
    public CompositePerfStats getPrimaryOperationStats() { 
        return threadStats.getFirstOperationKey();
    }
    
    public static aspect SummarizeTopLevelStats {
        after(PerfStats stats, Response response, StatsSummarizer summarizer) returning: 
          StatsSummarizer.endTopLevelStats(stats, response, summarizer) {
            try {
                // update request data...
                Request request = (Request)response.get(Response.REQUEST);
                if (request != null) {
                    request.setElapsedTime(response.getDuration());
                    request.setLastTime(response.getEnd());
                }

                OperationPerfStats uofwStats = summarizer.threadStats.getFirstOperationKey();
                if (uofwStats == null) {
                    if (stats instanceof OperationPerfStats) {
                        uofwStats = (OperationPerfStats)stats;
                        uofwStats.recordAsOperation();
                    }
                }
                if (uofwStats != null) {
                    uofwStats.summarizeOperation(response, stats);
                } else {
                    // no top-level unit of work... unusual case
                    stats.summarizeOperation(response);
                }                
            } finally {            
                summarizer.threadStats.reset();
                response.getFactory().setApplication(null);
            }
        }        
    }    
}


