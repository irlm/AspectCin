/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import glassbox.response.Response;
import glassbox.util.logging.api.LogManagement;
import glassbox.util.org.sl4j.Logger;
import glassbox.util.timing.api.TimeConversion;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public class OperationPerfStatsImpl extends CompositePerfStatsImpl implements OperationPerfStats {
    protected PerfStatsImpl scenarioStats[] = new PerfStatsImpl[OperationPerfStats.NUMBER_OF_SCENARIOS];
    protected PerfStatsImpl resourceTotalStats[] = new PerfStatsImpl[StatisticsTypeImpl.getAllTypes().size()];
    protected PerfStatsImpl otherComponentStats;
    protected int skipCount = initialSkipCount;
    protected boolean operationKey = true;
    /** includes time spent and errors encountered in dispatch as well as time in the response associated with these stats */
    protected PerfStatsImpl whenOperationStats;
    private static int initialSkipCount = 0; // how many to skip
    private static final Logger ITERATION_LOGGER = LogManagement.getLogger(PerfStats.ITERATION_LOGGER_NAME);
    private static final long serialVersionUID = 2;

    public OperationPerfStatsImpl() {
        this(false);
    }
    
    public OperationPerfStatsImpl(boolean virtualComposition) {
        super(virtualComposition);
        
        //nonstandard, doesn't support parent navigation, makes it hard for us to aggregate database stmt/conn within overall database use... 
        for(int i=0; i<scenarioStats.length; i++) {
            scenarioStats[i] = makeSimplePerfStats();            
        }
        for(int i=0; i<resourceTotalStats.length; i++) {
            resourceTotalStats[i] = makeSimplePerfStats();
        }
        otherComponentStats = makeSimplePerfStats();
        whenOperationStats = makeEndToEndStats();
    }

    public OperationPerfStatsImpl(OperationPerfStatsImpl toCopy) {
        super(toCopy);
    }
    
    public void setKey(Serializable key) {
        super.setKey(key);
        whenOperationStats.setKey(key);
    }

    protected PerfStatsImpl makeEndToEndStats() {        
        PerfStatsImpl childStats = (PerfStatsImpl)EndToEndStatisticsType.instance.makePerfStats(null);
        childStats.setSlowThreshold(getSlowThreshold());
        
        addChildStats(childStats);
        
        return childStats;
    }
    
    public PerfStats getScenarioStats(int scenarioKey) {
        return scenarioStats[scenarioKey];
    }
    
    public PerfStats getResourceStats(int statsTypeIndex) {
        return resourceTotalStats[statsTypeIndex];
    }
    
    public PerfStats getNonResourceStats() {
        return otherComponentStats;
    }

    protected void updateSimpleSummaries(PerOperationStruct totals, Response response) {
        // do this only when updating as an operation
        if (perOperationData.get().shouldSummarizeOperation()) {        
            // since this wasn't the first operation, then we need to add just its "own time" to the end to end stats...
            PerOperationStruct endToEndTotals = whenOperationStats.perOperationData.get();            
            endToEndTotals.add(perOperationData.get());            
            
            doUpdateSimpleSummaries(totals, response);            
        }
    }
    
    protected void doUpdateSimpleSummaries(PerOperationStruct totals, Response response) {
        updateScenarios(totals, response);

        updateResourceTotals(totals, response);
        
        super.updateSimpleSummaries(totals, response);
    }

    protected void updateScenarios(PerOperationStruct totals, Response response) {
        long elapsedTime = totals.accumulatedRequestTime;
        boolean slow = (elapsedTime >= getSlowThreshold()); 
        int type = (totals.accumulatedFailureCount > 0) ? 
                FAILURE_SCENARIO :
                (slow ? SLOW_SCENARIO : NORMAL_SCENARIO);
        scenarioStats[type].perOperationData.get().add(totals);

        if ((type != NORMAL_SCENARIO) && isOperationThisRequest() && (response != null)) {
            if (type==FAILURE_SCENARIO) {
                summarizeFailure(response);
            }

            // slow is needless, it will always be caught
            if (slow) {
                recordSlow(response);
            }
            problemRequest(type==FAILURE_SCENARIO, response);
        }
    }    
    
    protected void problemRequest(boolean failing, Response response) {        
        if (ITERATION_LOGGER.isDebugEnabled()) {
            Request request = getRequest(response);
            String requestDescription;
            String time;
            if (request != null) {
                requestDescription = request.getDescription();
                time = TimeConversion.formatTime(request.getElapsedTime());
            } else {
                // shouldn't happen!
                requestDescription = "not available";
                time = "not available";
            }
    
            String description = "+++ Problem in " + getCallDescription().getSummary() + ", Type: " + (failing ? "Error" : "Slow") +
                ", Description:" + requestDescription+", Time: "+ time + ", " + new Date().toString() +
                ", Iteration: " + (getCount()+1);
            ITERATION_LOGGER.debug(description);
        }
    }        

    protected void updateResourceTotals(PerOperationStruct totals, Response response) {
        for (Iterator iter = getUsedStatsMap().entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry)iter.next();
            PerfStatsImpl stats = (PerfStatsImpl) entry.getKey();
            if (stats instanceof OperationPerfStatsImpl) {
                OperationPerfStatsImpl childComp = (OperationPerfStatsImpl)stats;
                for (int i=0; i<resourceTotalStats.length; i++) {
                    PerOperationStruct resTotals = resourceTotalStats[i].perOperationData.get();
                    PerOperationStruct childTotals = childComp.resourceTotalStats[i].perOperationData.get();
                    resTotals.add(childTotals);
                }
            }
            StatisticsType type = (StatisticsType) entry.getValue();
            PerOperationStruct childTotals = stats.perOperationData.get();
            
            PerOperationStruct resTotals = resourceTotalStats[type.getIndex()].perOperationData.get();
            resTotals.add(childTotals);
        }

        PerOperationStruct otherTotals = otherComponentStats.perOperationData.get();
        otherTotals.add(totals);
        otherTotals.subtract(resourceTotalStats[StatisticsType.DatabaseIdx].perOperationData.get());
        otherTotals.subtract(resourceTotalStats[StatisticsType.RemoteCallIdx].perOperationData.get());
        // slow single isn't accurate in otherTotals... we don't "subtract it out"...
    }
    
    public int getSkipCount() {
        return skipCount;
    }

    public void resetSkipCount() {
        this.skipCount = initialSkipCount;
    }
    
    public void clear() {
        super.clear();
    }

    public List getProblemRequests() {
        if (whenOperationStats.failingOperationCount>0) {
            return whenOperationStats.getLastFailingRequests();
        } else {        
            return whenOperationStats.getSlowestSingleEvents();
        }
    }
    
    public void summarizeOperation(Response response) {
        if (skipCount<=0 || hasUofWFailure()) {
            super.summarizeOperation(response);
        } else {
            skipOperation();
        }
    }

    public void summarizeOperation(Response response, PerfStats entryStats) {
        //XXX we need a better way to flag initialization operations to not be counted unless they fail...   
        if ((skipCount<=0 && (response==null || !("true".equals(response.get("background"))))) || entryStats.hasUofWFailure()) { 
            PerfStatsImpl entryStatsImpl = ((PerfStatsImpl)entryStats);
            PerOperationStruct operationTotals = perOperationData.get();
            operationTotals.deferSummarization();
            
            // summarizes the top-level
            entryStatsImpl.updateSummaries(response);
    
            PerOperationStruct endToEndTotals = whenOperationStats.perOperationData.get();
            
            endToEndTotals.add(entryStatsImpl.perOperationData.get());
                        
            if (this == entryStats) {
                // ok: no dispatch
            } else if (getParent() == null) {
                logWarn("Unexpected null parent when summarizing operation "+getKey());
            } else {
                for (Iterator iter = ((CompositePerfStatsImpl)getParent()).getUsedStatsMap().entrySet().iterator(); iter.hasNext();) {
                    Entry entry = (Entry)iter.next();
                    PerfStatsImpl stats = (PerfStatsImpl) entry.getKey();
                    if (stats!=this && stats.isOperationThisRequest()) {
                        endToEndTotals.subtract(stats.perOperationData.get());
                    }
                }
            }
            
            // we need to track resource use & scenarios for the whenOperationStats, not this individual 
            doUpdateSimpleSummaries(endToEndTotals, response);
            
            if (isSlow(endToEndTotals.accumulatedRequestTime) && !isSlow(operationTotals.accumulatedRequestTime)) {
                whenOperationStats.doRecordSlow(response);
            }
            
            entryStatsImpl.resetOperationData();
        } else {
            skipOperation();
        }
    }
    
    public void setOperationKey(boolean operationKey) {
        this.operationKey = operationKey;
    }
    
    public boolean isOperationKey() {
        return operationKey;
    }
    
    public int decrementSkipCount() {
        return --skipCount;
    }
    
    public static void setInitialSkipCount(int anInitialSkipCount) {
        initialSkipCount = anInitialSkipCount;
    }
    
    public static int getInitialSkipCount() {
        return initialSkipCount;
    }
    
    public int getOperationCount() {
        return whenOperationStats.getCount();
    }
    
    public long getOperationTime() {
        return whenOperationStats.getAccumulatedTime();
    }
    
    public void recordAsOperation() {
        perOperationData.get().recordOperation();
    }
    
    public boolean isOperationThisRequest() {
        return perOperationData.get().isOperation();
    }

    public PerfStats getOperationStats() {
        return whenOperationStats;
    }

    public boolean isFailingDispatch() {
        return whenOperationStats.failingOperationCount > failingOperationCount;
    }

    public boolean isSlowDispatch() {
        return whenOperationStats.slowCount > slowCount;
    }

    public long getDispatchTime() {
        return whenOperationStats.accumulatedTime - accumulatedTime;
    }
    
    public void skipOperation() {
        super.skipOperation();
        decrementSkipCount();
    }
    
    public void recordDispatch(Response response) {
        if (response.get(Response.FAILURE_DATA)!=null && isOperationThisRequest()) {
            // this has to be synchronized to avoid risk of deadlock: we always acquire the operation stats lock
            // before acquiring a lock on whenOperationStats 
            synchronized(this) {
                whenOperationStats.recordFailure(response);
            }
        }
    }
    
    protected synchronized void addFailingRequest(Request request) {
        super.addFailingRequest(request);
        if (isOperationThisRequest()) {
            whenOperationStats.addFailingRequest(request);
        }
    }
    
    protected synchronized void addSlowRequest(Request request) {
        super.addSlowRequest(request);
        if (isOperationThisRequest()) {
            whenOperationStats.addSlowRequest(request);
        }
    }

    public PerfStats klone() {
        OperationPerfStatsImpl copy = new OperationPerfStatsImpl(this);
        return cloneInto(copy);
    }
    
    protected PerfStats cloneInto(CompositePerfStatsImpl copy) {
        super.cloneInto(copy);
        
        OperationPerfStatsImpl ocopy = (OperationPerfStatsImpl)copy;
        
        ocopy.operationKey = operationKey;
        for (int i=0; i<scenarioStats.length; i++) {
            ocopy.scenarioStats[i] = (PerfStatsImpl)scenarioStats[i].klone();
        }
        for (int i=0; i<resourceTotalStats.length; i++) {
            ocopy.resourceTotalStats[i] = (PerfStatsImpl)resourceTotalStats[i].klone();
        }
        ocopy.otherComponentStats = (PerfStatsImpl)otherComponentStats.klone();
        
        return copy;
    }
}
