/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 29, 2005
 */
package glassbox.track.api;

import glassbox.agent.api.NotSerializable;
import glassbox.response.Response;
import glassbox.util.timing.Clock;
import glassbox.util.timing.api.TimeConversion;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;


/**
 * This implementation of the performance stats interface is used for leaf-level performance statistics and those with
 * children where the statistics for each child are stored separately, and stores total metrics separately. This is used
 * for measurements (e.g., ui requests) that are contained within a single bounding operation/unit of work.
 * 
 * @author Ron Bodkin
 * @see glassbox.track.api.CompositePerfStatsImpl
 */
public class PerfStatsImpl implements PerfStats {
    protected StatisticsRegistryImpl owner;

    protected Serializable key;
    
    protected StatisticsType type;

    protected long accumulatedTime;

    protected int count;

    /** Total number of failures seen: can be >1 per request */
    protected int failureCount;

    protected long maxTime = Clock.UNDEFINED_TIME;
    
    protected long firstEventTime = Clock.UNDEFINED_TIME;

    protected long lastEventTime = Clock.UNDEFINED_TIME;
    
    private long slowThreshold;

    /** Total number of times a single request made the <em>operation</em> slow as a whole */
    protected int slowSingleOperationCount;
    
    /** Total number of times this statistic made the <em>operation</em> slow as a whole */
    protected int slowCount;

    /** 
     * Total number of times this statistic made the <em>operation</em> fail as a whole. 
     * Each operation request can increase this by zero or one.
     */
    protected int failingOperationCount;
    
    protected static final int MIN_FAILURES = 5;
    protected static final int LIM_FAILURES = MIN_FAILURES*2;
    protected static final int LIM_SLOW_EVENTS = 5;
    
    // we should unify failing requests & worst failures...
    /** Holds the worst failures, up to MAX_FAILURES count */ 
    protected FailureCollection worstFailures = new FailureCollection(LIM_FAILURES);

    protected SortedSet/*<Request>*/ failingRequests = new TreeSet(Request.END_TIME_COMPARATOR);
    protected SortedSet/*<Request>*/ slowestRequests = new TreeSet();
    
    // this is a bit messy: we need to avoid recording problems for the same response in different stats
    // and we need to report problems for a given statistic (e.g., the primary operation) only once per request...
    public static final String RECORDED = "stats.recorded";
    public static final Integer RECORDED_VALUES[]= { new Integer(0), new Integer(1), new Integer(2), new Integer(3) };
    public static final int RECORDED_SLOW = 1;
    public static final int RECORDED_FAILURE = 2;
    
    protected transient PerOperationData perOperationData;
    private boolean virtuallyContained;
    
    protected static class PerRequestThreadLocal extends ThreadLocal {
        private PerOperationData operationData;
        
        public PerRequestThreadLocal(PerOperationData operationData) {
            this.operationData = operationData;
        }
        
        public Object initialValue() {
            return operationData.makeOperationStruct();
        }        
    }
    
    protected class PerOperationData implements NotSerializable {   
        
        protected ThreadLocal perRequestData = new PerRequestThreadLocal(this);

        protected PerOperationStruct makeOperationStruct() {
            return new PerOperationStruct(PerfStatsImpl.this);
        }
        
        protected PerOperationStruct get() {
            return (PerOperationStruct)perRequestData.get();
        }
        
        public void reset() {
            get().reset();
        }
        public void recordStart(long start) {
            get().recordStart(start);
        }
        public long recordEnd(long end) {
            return get().recordEnd(end);
        }
        public long recordFailure(long end) {
            return get().recordFailure(end);            
        }
        public long recordUsage(long start, long end, long measurementError) {
            return get().recordUsage(start, end);
        }
        public long recordEnd(glassbox.response.Response response) {
            return get().recordEnd(response);
        }
        /**
         * 
         * @param elapsedTime usage time in nanos
         */
        public long recordUsage(long elapsedTime) {
            return get().recordUsage(elapsedTime);
        }
        
        public long peekStartTime() {
            return get().lastRequestTime;
        }
        public String toString() {
            return get().toString();
        }
    };

    protected class VirtualPerOperationData extends PerOperationData {   
        
        protected PerOperationStruct makeOperationStruct() {
            return new VirtualPerOperationStruct(PerfStatsImpl.this);
        }
    }

    protected PerOperationData makePerOperationData() {
        if (virtuallyContained) {
            return new VirtualPerOperationData();
        } else {
            return new PerOperationData();
        }
    }

    public PerfStatsImpl() {
        this(false);
    }
    
    public PerfStatsImpl(boolean virtuallyContained) {
        this.virtuallyContained = virtuallyContained;
        perOperationData = makePerOperationData();
    }
    
    /**
     * Copy constructor copies only the top-level stats, not composite stats, if
     * any.
     * 
     * @param stats
     */
    public PerfStatsImpl(PerfStats stats) {
        accumulatedTime = stats.getAccumulatedTime();
        maxTime = stats.getMaxTime();
        count = stats.getCount();
        slowCount = stats.getSlowCount();
        slowSingleOperationCount = stats.getSlowSingleOperationCount();
        failureCount = stats.getFailureCount();
        failingOperationCount = stats.getFailingOperationCount();
        slowThreshold = stats.getSlowThreshold();        
        firstEventTime = stats.getFirstEventTime();
        lastEventTime = stats.getLastEventTime();
        type = stats.getType();
        virtuallyContained = stats.isVirtuallyContained();
        
        if (stats instanceof PerfStatsImpl) {
            PerfStatsImpl copied = ((PerfStatsImpl)stats);
            worstFailures = new FailureCollection(copied.worstFailures);
            failingRequests.addAll(copied.getLastFailingRequests());
            slowestRequests = new TreeSet(copied.getSlowestSingleEvents());                    
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see glassbox.track.PerfStats#getCount()
     */
    public int getCount() {
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see glassbox.track.PerfStats#getSlowCount()
     */
    public int getSlowCount() {
        return slowCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see glassbox.track.PerfStats#getSlowSingleOperationCount()
     */
    public int getSlowSingleOperationCount() {
        return slowSingleOperationCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see glassbox.track.PerfStats#getFailureCount()
     */
    public int getFailureCount() {
        return failureCount;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see glassbox.track.PerfStats#getFailureOperationCount()
     */
    public int getFailingOperationCount() {
        return failingOperationCount;
    }

    public long getMaxTime() {
        return maxTime;
    }
    
    public double getMeanTime() {
        if (getAccumulatedTime() == 0L) {
            // count shouldn't be 0 if time>0!
            return 0.;
        }
        return (double)getAccumulatedTime() / (double)count;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see glassbox.track.PerfStats#getAccumulatedTime()
     */
    public long getAccumulatedTime() {
        return accumulatedTime;
    }

    public boolean isVirtuallyContained() {
        return virtuallyContained;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see glassbox.track.PerfStats#recordStart()
     */
    public void recordStart(long start) {
        perOperationData.recordStart(start);
    }

    protected long peekStartTime() {
        return perOperationData.peekStartTime();
    }
    
    protected void recordEventCompleted(glassbox.response.Response response) {
        recordEventCompleted(response.getStart(), response.getEnd());
    }
    
    protected synchronized void recordEventCompleted(long start, long end) {
        if (firstEventTime == Clock.UNDEFINED_TIME || firstEventTime > start) {
            firstEventTime = start;
        }

        if (lastEventTime < end) {
            lastEventTime = end;
        }
    }
    
    public long recordEnd(glassbox.response.Response responseEvent) {
        recordEventCompleted(responseEvent);
        return perOperationData.recordEnd(responseEvent);
    }

    public long recordUsage(long start, long end, long measurementError) {
        recordEventCompleted(start, end);
        return perOperationData.recordUsage(start, end, measurementError);
    }
    
    public long recordUsage(long elapsedTime) {
        return perOperationData.recordUsage(elapsedTime);
    }
    
    public void summarizeOperation(Response response) {
        updateSummaries(response);
        resetOperationData();
    }
    
    public void skipOperation() {
        resetOperationData();
    }
    
    protected void resetOperationData() {
        perOperationData.reset();
    }
    
    public CallDescription getCallDescription() {
        return type.getCall(this);
    }

    public Request getRequest(Response response) {
        Request request = (Request)response.get(Response.REQUEST);
        if (request == null) {
            if (isOperationKey()) {
                // use *external* request key
                Response sought = response;
                while (sought != null) {
                    request = (Request)sought.get(Response.REQUEST);
                    if (request != null) {
                        break;
                    }
                    sought = sought.getParent();
                }
            }
    
            if (request == null) {
                CallDescription aCall = getCallDescription();
                
                // no request data, e.g., contention
                if (aCall == null) {
                    return null;
                }
                
                request = new DefaultRequest(aCall, aCall.getResourceKey()+":"+aCall.getCallKey(), 
                        response.get(glassbox.response.Response.PARAMETERS));            
                request.cloneParameters();
            }
            response.set(Response.REQUEST, request);
        }
        request.setElapsedTime(response.getDuration());
        request.setLastTime(response.getEnd());
        
        return request;
    }
    
    protected synchronized PerOperationStruct updateThisNodeSummaries(PerOperationStruct totals, Response response) {
        accumulatedTime += totals.accumulatedRequestTime;
        count += totals.accumulatedRequestCount;
        maxTime = Math.max(maxTime, totals.longestTime); 
        if (isSlow(totals.accumulatedRequestTime)) {
            slowCount++;
        }
        if (totals.isSlowSingle()) {
            slowSingleOperationCount++;
        }
        if (totals.accumulatedFailureCount > 0) {
            failureCount += totals.accumulatedFailureCount;
            failingOperationCount++;
        }
        return totals;
    }
    
    protected synchronized PerOperationStruct updateSummaries(PerOperationStruct totals, Response response) {
        return updateThisNodeSummaries(totals, response);
    }
    
    /** only summarizes if first */
    protected FailureStats summarizeFailure(Response response) {
        int recorded = getRecorded(response);
        if ((recorded & RECORDED_FAILURE) == 0) {
            response.set(RECORDED, RECORDED_VALUES[recorded|RECORDED_FAILURE]);             
            return recordFailure(response);
        }
        return null;
    }

    private static aspect RecordRequestsOnce implements NotSerializable {
        private pointcut failureRecording() : 
            within(PerfStatsImpl+) && (execution(* recordFailure(..)) || execution(* addFailingRequest(..)));
        
        private pointcut topLevelFailureRecording(PerfStatsImpl stats) :
            this(stats) && failureRecording() && !cflowbelow(failureRecording());
        
        Object around(PerfStatsImpl stats) : topLevelFailureRecording(stats) {
            if (!stats.perOperationData.get().hasRecordedFailedRequest()) {
                return proceed(stats);
            } else {
                return null;
            }
        }
        // a bit ugly: we need after advice to run after returning from proceed...
        after(PerfStatsImpl stats) returning : topLevelFailureRecording(stats) {
            stats.perOperationData.get().recordFailedRequest();
        }
        
        private pointcut slowRecording() : 
            within(PerfStatsImpl+) && (execution(* addSlowRequest(..)) || execution(* doRecordSlow(..)));

        private pointcut topLevelSlowRecording(PerfStatsImpl stats) :
            this(stats) && slowRecording() && !cflowbelow(slowRecording());
        
        Object around(PerfStatsImpl stats) : topLevelSlowRecording(stats) {
            if (!stats.perOperationData.get().hasRecordedSlowRequest()) {
                return proceed(stats);
            } else {
                return null;
            }
        }
        // a bit ugly: we need after advice to run after returning from proceed...
        after(PerfStatsImpl stats) returning : topLevelSlowRecording(stats) {
            stats.perOperationData.get().recordSlowRequest();
        }
        
    }        
        
    protected FailureStats recordFailure(Response response) {
        Request request = getRequest(response);
        if (request != null) {
            addFailingRequest(request);
            
            if (EXCEPTIONS_LOGGER.isDebugEnabled()) {
                EXCEPTIONS_LOGGER.debug(request.getDescription());
            }            
        }
        
        FailureStats stats = null;
        FailureDescription failureDescription = (FailureDescription)response.get(Response.FAILURE_DATA);
        if (failureDescription != null) {
            // Only record failure stats for this level if we saw one directly: otherwise just note there was a failure, 
            // but it will be recorded below us. To report on it, we need to look in child stats.
            // The analyzer has done this for remote call and database resource access for a while, and (before 2.0 beta)
            // still needs to do so for nested operations
            stats = worstFailures.add(failureDescription, request);
        }
        return stats;
    }        

    protected int getRecorded(Response response) {
        Integer recorded = (Integer)response.get(RECORDED);
        if (recorded == null) return 0;
        return recorded.intValue();
    }
    
    public void recordSlow(Response response) {
        int recorded = getRecorded(response);
        if ((recorded & RECORDED_SLOW) == 0) {
            response.set(RECORDED, new Integer(recorded|RECORDED_SLOW)); 
            doRecordSlow(response);
        }
    }
    
    protected synchronized void doRecordSlow(Response response) {
        if (!perOperationData.get().hasRecordedSlowRequest()) {
            glassbox.track.api.Request request = getRequest(response);
            if (request != null) {
                addSlowRequest(request);
            }
        }
    }
    
    protected void limit(SortedSet sortedSet, int limSize) {
        while (sortedSet.size() > limSize) {
            int sz=sortedSet.size() ;
            sortedSet.remove(sortedSet.last());
            if (sortedSet.size() == sz) {
                String err = "";
                for (Iterator it=sortedSet.iterator(); it.hasNext();) {
                    err += (err.length()>0 ? ", " : "Bad request ordering ");
                    err += it.next().toString();
                }
                logError(err);
                forceLimit(sortedSet, limSize);
            }
        }        
    }
    
    protected void forceLimit(SortedSet sortedSet, int limSize) {
        int i=0;
        for (Iterator it=sortedSet.iterator(); it.hasNext(); i++) {
            it.next();
            if (i>=limSize) {
                it.remove();
            }
        }
    }

    protected synchronized void addFailingRequest(Request request) {
        failingRequests.add(request.copy());
        limit(failingRequests, MIN_FAILURES);
    }
    
    protected synchronized void addSlowRequest(Request request) {
        if (isAmongSlowest(request.getElapsedTime())) {
            slowestRequests.add(request.copy());
            limit(slowestRequests, LIM_SLOW_EVENTS);
        }
        
        if (EXCEPTIONS_LOGGER.isDebugEnabled()) {
            EXCEPTIONS_LOGGER.debug(request.getDescription());
        }
    }        

    public synchronized List getSlowestSingleEvents() {
        return new ArrayList(slowestRequests);
    }
    
    public boolean isAmongSlowest(long slowElapsedTime) {
        if (slowestRequests.size() < LIM_SLOW_EVENTS) {
            return true;
        }
        Request request = (Request)slowestRequests.last();
        return slowElapsedTime >= request.getElapsedTime();
    }
    
    protected final PerOperationStruct updateSummaries(Response response) {
        return updateSummaries(perOperationData.get(), response);
    }
    
    /**
     * returns true if the defined duration is considered "slow"
     */
    public boolean isSlow(long nanos) {
        return nanos >= slowThreshold;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see glassbox.track.PerfStats#getSlowThreshold()
     */
    public long getSlowThreshold() {
        return slowThreshold;
    }

    public void setSlowThreshold(long slowThreshold) {
        this.slowThreshold = slowThreshold;
    }

    public long getFirstEventTime() {
        return firstEventTime;
    }

    public long getLastEventTime() {
        return lastEventTime;
    }

    public StatisticsRegistryImpl getOwner() {
        return owner;
    }

    public void setOwner(StatisticsRegistryImpl owner) {
        this.owner = owner;
        //System.err.println("Created "+getKey()+" owned by "+(getParent()!=null ? getParent().getKey() : null));
    }
    
    public CompositePerfStats getParent() {
        if (owner != null) {
            StatisticsRegistry root = owner.getContainer();
            if (root instanceof CompositePerfStats) {
                return (CompositePerfStats)root;
            }
        }
        return null;
    }

    public Serializable getKey() {
        return key;
    }

    public void setKey(Serializable key) {
        this.key = key;
    }

    public StatisticsType getType() {
        return type;
    }

    public void setType(StatisticsType type) {
        this.type = type;
    }

    public String getLayer() {
        return type.getLayer();
    }
    
    public List getWorstFailures() {
        return worstFailures.getList();
    }
    
    public synchronized List getLastFailingRequests() {
        return new ArrayList(failingRequests);
    }
      
    public String toString() {
        return super.toString() + " " + getKey()+ "(# = " + getCount() + ", tm =" + TimeConversion.formatTime(accumulatedTime) + ",  #slow = " + getSlowCount() + ", # fail = "
                + getFailureCount() + ")";
    }

    public StringBuffer dump(StringBuffer buffer, int depth) {
        buffer.append(toString());
        return buffer;
    }    

    public boolean isOperationKey() {
        return false;
    }
    
    public boolean isOperationThisRequest() {
        return false;
    }
    
    public Collection getChildren() {
        return Collections.EMPTY_LIST;        
    }
    
    public boolean hasUofWFailure() {
        return (perOperationData.get().accumulatedFailureCount > 0);
    }

    public PerfStats klone() {
        return new PerfStatsImpl(this);
    }
    
    private static final long serialVersionUID = 6;

}
