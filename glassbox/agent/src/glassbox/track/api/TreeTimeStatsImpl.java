/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import glassbox.monitor.thread.OperationSample;
import glassbox.monitor.thread.ThreadSnapshot;
import glassbox.monitor.thread.ThreadSnapshotImpl;
import glassbox.response.Response;
import glassbox.util.logging.api.*;
import glassbox.util.org.sl4j.Logger;
import glassbox.util.timing.api.TimeConversion;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TreeTimeStatsImpl extends CompositePerfStatsImpl implements TreeTimeStats {
    //XXX misleading - this is really minimum ratio of elapsed time, not frequency
    protected static final long MINIMUM_FREQUENCY = 1000L; // only elapsedTime things that happen at least 1 in 1000 times 
    protected long totalElapsedTime;    
    protected PerfStatsImpl statsForState[] = makeStatsForState();
    protected PerfStatsImpl totalCpuStats = makeSimplePerfStats();
    
    //    protected ThreadSnapshot commonBase;
    // this has to be transient to avoid serializing a big data structure
    protected transient TreeStats treeStats = new TreeStats();
    private int logCount = 0;
    private int nextLogCount = 1;

    public synchronized boolean hasSlowestSignficant(int requestCount, double slowFraction) {
        // we could optimize this
        return getSlowestSignficant(requestCount, slowFraction) != null;
    }
    
    public synchronized SlowRequestDescriptor getSlowestSignficant(int requestCount, double slowFraction) {
        SlowRequestDescriptor requestDescriptor = new SlowRequestDescriptor();
        int minCount = Math.max(1, (int)(requestCount * slowFraction + 0.5));        
        int slowCount = treeStats.getSlowest(requestDescriptor, minCount, 1);
        if (slowCount < minCount) {
            return null;
        }
        requestDescriptor.setSlowCount(slowCount);
        return requestDescriptor;
    }

    /**
     * Error to call this with sample==null
     */
    public synchronized void recordSample(Object theSample) {
        OperationSample sample = (OperationSample)theSample;
        List snapshots = sample.getSnapshots();
        int sz = snapshots==null ? 0 : snapshots.size();
        count++;
        if (sz>0) {
            //findCommonBase();
            treeStats.shouldClear = true;
            for (int i=0; i<sz; i++) {
                ThreadSnapshot snapshot = (ThreadSnapshot)snapshots.get(i);
                totalElapsedTime += sample.getInterval();
                PerfStats stateStats = statsForState[snapshot.getState().getExecutionState()];
                stateStats.recordUsage(sample.getInterval()); // probably should be a switch...
                treeStats.recordSample(snapshot.getStackTrace(), snapshot.getStackTrace().length-1, sample.getInterval());
            }
            treeStats.countSlow(getMinSlowCount(sample.getInterval()));
        }
        totalCpuStats.recordUsage(sample.getTotalCpuTime());
        //userCpuTime.recordUsage(sample.getUserCpuTime());
        summarizeOperation(sample.getResponse());
        afterRecording();
    }
    
    private void afterRecording() {
        if (++logCount >= nextLogCount) {
            if (DIAGNOSTICS_LOGGER.isDebugEnabled()) {
                DIAGNOSTICS_LOGGER.debug("time samples for "+logCount+": "+treeStats.toString());
            }
            nextLogCount *= 10;
        }
    }        
    
    protected int getMinSlowCount(long interval) { 
        return (int)((getSlowThreshold() + interval/2) / interval); 
    }

    public PerfStats getStatsForState(int state) {
        return statsForState[state];
    }

    protected PerfStatsImpl[] makeStatsForState() {
        PerfStatsImpl stats[] = new PerfStatsImpl[ThreadState.NUMBER_OF_STATES];
        for (int i=0; i<ThreadState.NUMBER_OF_STATES; i++) {
            stats[i] = makeSimplePerfStats();
        }
        return stats;
    }
    
    protected void updateSimpleSummaries(PerOperationStruct totals, Response response) {
        for (int i=0; i<simpleStatsHeld.length; i++) {
            simpleStatsHeld[i].updateSummaries(response);
        }            
    }

    public PerfStats getCpuStats() {
        return totalCpuStats;
    }
//    protected void findCommonBase() {
//        for (int i=0; i<sz; i++) {
//            if (commonBase == null) {
//                commonBase = snapshots.get(0);
//            } else {
//                int depth = commonBase.highestDifference(snapshots.get(0));
//                if (depth != -1) {
//                    // different: need to walk up the stack & split our stats
//                }
//            }
//        }
//    }
    protected class TreeStats {
        /** Total estimated time in ns from sampling. */ 
        private long elapsedTime = 0;
        
        /** Count of number of requests caused to be slow by this statistic alone. */ 
        private int slowCount = 0;
        
        private transient int thisRequestCount = 0;
        private transient boolean shouldClear = false;
        protected Map/*<StackTraceElement, TreeStats>*/ children = null; /* we could also zero out line number and just report at the method level */

        // should rewrite as a loop to avoid any risk of stack overflows
        public void recordSample(StackTraceElement[] trace, int offset, long interval) {
            elapsedTime += interval;
            
            if (shouldClear) {
                if (children!=null) {
                    for (Iterator it=children.entrySet().iterator(); it.hasNext();) {
                        Entry entry = (Entry)it.next();
                        TreeStats stats = (TreeStats)entry.getValue();
                        if (stats.elapsedTime*(MINIMUM_FREQUENCY*3L) < totalElapsedTime) {
                            // prune
                            it.remove();
                        } else {
                            stats.shouldClear = true;
                        }
                    }
                }
                thisRequestCount = 1;
                shouldClear = false;
            } else {
                thisRequestCount++;
            }
            
            if (totalElapsedTime <= MINIMUM_FREQUENCY*elapsedTime) {                
                // expand detail
                if (children == null) {
                    children = new HashMap();
                }
                if (offset>=0) {
                    TreeStats stats = (TreeStats)children.get(trace[offset]);
                    if (stats == null) {
                        stats = new TreeStats();
                        children.put(trace[offset], stats);
                    }
                    stats.recordSample(trace, offset-1, interval);                
                }
            }
        }
        
        public void countSlow(int thresh) {
            if (!shouldClear && thisRequestCount>=thresh && children != null) {
                slowCount++;
                if (slowCount > count) {
                    logWarn("Problem: excess slow count for "+this); 
                    slowCount = count;
                }                
                for (Iterator it=children.entrySet().iterator(); it.hasNext();) {
                    Entry entry = (Entry)it.next();
                    TreeStats stats = (TreeStats)entry.getValue();
                    stats.countSlow(thresh);
                    if (stats.slowCount > slowCount) {
                        logWarn("Problem: excess slow count for "+this+" versus "+stats); 
                        stats.slowCount = slowCount;
                    }
                }
            } 
        }
        
        public int getSlowest(SlowRequestDescriptor requestDescriptor, int minCount, int depth) {
            int result = slowCount;
            if (children != null) {
                TreeStats slowest = null;
                StackTraceElement slowestTraceElement = null;
                for (Iterator it=children.entrySet().iterator(); it.hasNext();) {
                    Entry entry = (Entry)it.next();
                    TreeStats stats = (TreeStats)entry.getValue();
                    if (slowest == null || stats.isWorseThan(slowest, minCount)) {
                        slowest = stats;
                        slowestTraceElement = (StackTraceElement)entry.getKey();
                    }
                }
                
                if (slowest != null) {
                    if (childAccountsForSlow(slowest.slowCount, slowest.elapsedTime, minCount)) {
                        result = slowest.getSlowest(requestDescriptor, minCount, depth+1);                    
                        requestDescriptor.setTraceElement(depth, slowestTraceElement, elapsedTime);
                    } else {
                        // try to find a single method that is the slowest: we just look for the one with the biggest count
                        // this might lose occasionally if they are all counts of one
                        int slowestCount = 0;
                        long elapsedTime = 0L;
                        for (Iterator it=children.entrySet().iterator(); it.hasNext();) {
                            Entry entry = (Entry)it.next();
                            StackTraceElement elt = (StackTraceElement)entry.getKey();
                            if (eqNull(slowestTraceElement.getClassName(), elt.getClassName())
                                    && eqNull(slowestTraceElement.getFileName(), elt.getFileName())
                                    && eqNull(slowestTraceElement.getMethodName(), elt.getMethodName())) {
                                TreeStats stats = (TreeStats) entry.getValue();
                                slowestCount += stats.slowCount;
                                elapsedTime += stats.elapsedTime;
                            }
                        }
                        if (childAccountsForSlow(slowestCount, elapsedTime, minCount)) {
                            slowestTraceElement = new StackTraceElement(slowestTraceElement.getClassName(), slowestTraceElement.getMethodName(), slowestTraceElement.getFileName(), -1);
                            requestDescriptor.setTraceElement(depth, slowestTraceElement, elapsedTime);
                        }                        
                    }
                }
            }
            return result;
        }

        private boolean childAccountsForSlow(int childSlowCount, long childElapsedTime, int minCount) {
            return childSlowCount>=minCount && childSlowCount*2>=slowCount && childElapsedTime*2L>=elapsedTime;
        }

        private boolean isWorseThan(TreeStats other, int minCount) {
            if (slowCount>=minCount && other.slowCount<minCount) {
                return true;
            }
            if (slowCount<minCount && other.slowCount>=minCount) {
                return false;
            }
            // c*t/S <= MAXLONG => S >= (c*t)/MAXLONG
            long scale = ((long)Math.max(count, other.slowCount)) / (Long.MAX_VALUE/Math.max(elapsedTime+1, other.elapsedTime));
            if (scale < 1L) {
                scale = 1L;
            }
            long myscore = ((long)slowCount)*(elapsedTime/scale);
            long otherscore = ((long)other.slowCount)*(other.elapsedTime/scale);
            
            return myscore > otherscore;
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            appendDesc(buf, 0);
            return buf.toString();
        }
        
        public void appendDesc(StringBuffer buffer, int depth) {
            buffer.append("Stats (tm="+TimeConversion.formatTime(elapsedTime)+", slow="+slowCount);
            if (false) { //debug
                buffer.append(", req count "+thisRequestCount);
                buffer.append(", shouldClear "+shouldClear);
            }
            if (children != null) {
                buffer.append("): containing ");
                for (Iterator it=children.entrySet().iterator(); it.hasNext();) {
                    Entry entry = (Entry)it.next();
                    StackTraceElement elt = (StackTraceElement)entry.getKey();
                    TreeStats stats = (TreeStats) entry.getValue();
                    buffer.append("\n");
                    for (int i=-2; i<2*depth; i++) {
                        buffer.append(' ');
                    }
                    buffer.append(elt);
                    stats.appendDesc(buffer, depth+1);
                }
            } else {
                buffer.append(")");
            }
        }
        
        private static final long serialVersionUID = 1L;                
    }
    
    public static boolean eqNull(Object o1, Object o2) {
        if (o1==o2) {
            return true;
        }
        if (o1==null || o2==null) {
            return false;
        }
        return o1.equals(o2);
    }
    
    public String toString() {
        return treeStats.toString();
    }
    
    private static final long serialVersionUID = 1L;
}
