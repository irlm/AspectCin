/**
 * 
 */
package glassbox.track.api;

import java.lang.ref.WeakReference;
import java.util.Map;

import edu.emory.mathcs.util.WeakIdentityHashMap;
import glassbox.agent.api.NotSerializable;

class PerOperationStruct implements NotSerializable {
    public long accumulatedRequestTime = 0L;
    public long lastRequestTime;
    public long longestTime = 0L;
    public int accumulatedRequestCount;
    public int accumulatedFailureCount;
    private static final int SLOW_SINGLE = 1;
    private static final int RECORDED_FAILING = 2;
    private static final int RECORDED_OPERATION = 4; // used by scenario stats
    private static final int DEFER_SUMMARIZATION = 8;
    private static final int RECORDED_FAILED_REQUEST = 16;
    private static final int RECORDED_SLOW_REQUEST = 32;
    public int flags;
    private WeakReference/*<PerfStatsImpl>*/ statsRef;
    public Map/*<PerfStats, StatisticsType>*/ usedStats = new WeakIdentityHashMap();
    
    public PerOperationStruct(PerfStats stats) {
        statsRef = new WeakReference(stats);
    }
    
    public void reset() {
        accumulatedRequestTime = 0;
        accumulatedRequestCount = 0;
        accumulatedFailureCount = 0;
        usedStats.clear();
        flags = 0;
    }
    
    public void recordStart(long startTime) {//UsageTrackingInfo startUsageInfo) {
        lastRequestTime = startTime;//timeStackHolder.getStack().push(startUsageInfo);
    }
    
    public long recordEnd(glassbox.response.Response response) {
        long elapsedTime = recordUsage(response.getStart(), response.getEnd());
        
        FailureDescription fd = (FailureDescription)response.get(glassbox.response.Response.FAILURE_DATA);
        
        PerfStatsImpl stats = ((PerfStatsImpl)(statsRef.get()));
        if (fd != null) {
            // update on the fly: don't wait for end of transaction        
            fd.setCall(stats.getCallDescription());
            accumulatedFailureCount++;
            flags |= RECORDED_FAILING;
            stats.summarizeFailure(response);
        } else if (stats.isSlow(elapsedTime)) {
            flags |= SLOW_SINGLE;
            stats.recordSlow(response);
        }
        
        return elapsedTime;
    }
    
    public long recordEnd(long end) {
        return recordUsage(lastRequestTime, end);
    }
    
    public long recordFailure(long end) {
        accumulatedFailureCount++;
        flags |= RECORDED_FAILING;
        return recordEnd(end);
    }

    long recordUsage(long elapsedTime) {
        accumulatedRequestTime += elapsedTime;
        accumulatedRequestCount++;
        
        // ugly case of "slow" counting at leaf node
        //XXX these used to use accumulatedRequestTime instead?! 
        longestTime = Math.max(longestTime, elapsedTime);
        return elapsedTime;
    }
    
    long recordUsage(long start, long end) {
        lastRequestTime = end;
        long elapsedTime = recordUsage(end - start);

        PerfStatsImpl stats = ((PerfStatsImpl)(statsRef.get()));
        if (stats.isSlow(elapsedTime)) {
            flags |= SLOW_SINGLE;
        }
        return elapsedTime;
    }            
    
    public void add(PerOperationStruct otherOperation) {
        accumulatedFailureCount += otherOperation.accumulatedFailureCount;
        // we don't add flags... they tend to be specific to this level
        
        // we don't aggregate up the request count nor the time in this case: only for
        // virtual statistics where these won't be nested physically inside the parent's requests 
    }
    
    public void subtract(PerOperationStruct otherOperation) {
        accumulatedFailureCount -= otherOperation.accumulatedFailureCount;
    }
    
    public boolean isSlowSingle() {
        return (flags&SLOW_SINGLE) != 0;
    }
    
    public boolean isFailed() {
        return (flags&RECORDED_FAILING) != 0;
    }
    
    public void recordOperation() {
        flags |= RECORDED_OPERATION;
    }

    public void clearOperation() {
         flags &= ~RECORDED_OPERATION;
    }
    
    public boolean isOperation() {
        return (flags&RECORDED_OPERATION) != 0;
    }
    
    public void deferSummarization() {
        flags |= DEFER_SUMMARIZATION;
    }
    
    public boolean shouldSummarizeOperation() {
        return (flags & (RECORDED_OPERATION|DEFER_SUMMARIZATION)) == RECORDED_OPERATION;
    }
    
    public boolean hasRecordedFailedRequest() {
        return (flags&RECORDED_FAILED_REQUEST) != 0;
    }

    public boolean hasRecordedSlowRequest() {
        return (flags&RECORDED_SLOW_REQUEST) != 0;
    }

    public void recordFailedRequest() {
        flags |= RECORDED_FAILED_REQUEST;
    }
    
    public void recordSlowRequest() {
        flags |= RECORDED_SLOW_REQUEST;
    }

    public String toString() {
        String str = accumulatedRequestTime + ", count = " +accumulatedRequestCount;
        if (accumulatedFailureCount>0) {
            str = str + ", failures = "+accumulatedFailureCount;
        }
        if (isSlowSingle()) {
            str = str + ", slowSingle";
        }
        return str;
    }                
}