/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.debug;

import glassbox.monitor.thread.OperationSample;
import glassbox.summary.thread.ThreadSummarizer;
import glassbox.track.api.*;
import glassbox.util.timing.api.TimeConversion;

import java.util.*;

public privileged aspect CheckSummarization {
    ThreadLocal summaryHolder = new ThreadLocal() {
        public Object initialValue() {
            return new LinkedList();
        }
    };
    
    ThreadLocal recordedHolder = new ThreadLocal() {
        public Object initialValue() {
            return new HashSet();
        }
    };
    List getSummary() {
        return (List)(summaryHolder.get());
    }
    void addSummary(Object o) {
        getSummary().add(o.toString());
    }
    Set seenSet() {
        return (Set)(recordedHolder.get());
    }
    
    pointcut updatingCompositeSummaries(PerfStatsImpl compositeStats) :
        call(* updateSummaries(*)) && target(compositeStats);
    
    pointcut updatingSummaries(PerfStatsImpl stats) :
        call(* updateSummaries(*)) && target(stats);
    
    pointcut resetting(PerfStatsImpl stats) :
        call(* resetOperationData()) && target(stats);

    public static boolean tracing = Boolean.getBoolean("trace.summarization");
    before(PerfStats stats, OperationSample sample) : 
        execution(* ThreadSummarizer.summarize(..)) && args(sample, stats) && if(tracing) {        
        if (sample!=null && sample.getTotalTime()>200) {
            System.err.println("Sampled "+TimeConversion.convertNanosToMillis(sample.getTotalTime())+" for "+stats);
        }
    }
    
    // can't import this type: it's visible only because of aspect privilege
    before(OperationPerfStats stats, glassbox.track.api.PerOperationStruct totals) : 
        execution(* updateSummaries(..)) && this(stats) && args(totals) && if(tracing) {        
        if (totals.accumulatedRequestTime>TimeConversion.convertMillisToNanos(200)) {
            System.err.println("Rolled up "+TimeConversion.convertNanosToMillis(totals.accumulatedRequestTime)+" for "+stats);
        }
    }

//    before(OperationPerfStatsImpl stats) : execution(* recordStart(..)) && TraceRepositories.recordStats(*, *) && target(stats) {
//        getSummary().clear();
//        seenSet().clear();
//        addSummary("start uofw "+stats+": "+stats.timeStackHolder.getStack().size());
//    }
//    after(OperationPerfStatsImpl stats) returning: execution(* summarizeOperation(..)) && !cflowbelow(execution(* summarizeOperation(..))) && target(stats) {
//        addSummary("end uofw "+stats+": "+stats.timeStackHolder.getStack().size());
//        for (Iterator it=seenSet().iterator(); it.hasNext();) {
//            PerfStatsImpl childStats = (PerfStatsImpl)it.next();
//            if (childStats.perOperationData.get().accumulatedRequestTime.getEventTime() > 0) {
//                System.err.println("data not cleared for "+childStats.getKey());
//                dumpError(stats);                
//            }
//        }
//    }
//    
//    before(PerfStatsImpl stats) : updatingSummaries(stats) {
//        addSummary("before "+stats+": totals "+stats.perOperationData.get()+" from "+thisEnclosingJoinPointStaticPart.toLongString());
//    }
//    after(PerfStatsImpl stats) returning : updatingSummaries(stats) {
//        addSummary("after "+stats+": totals "+stats.perOperationData.get());
//    }
//    
//    after(PerfStatsImpl stats) returning : resetting(stats) {
//        addSummary("reset "+stats.getKey());
//    }
//
//        
//    pointcut putInMap(Map map, PerfStatsImpl stats, Object type) : 
//        call(* Map.put(..)) && withincode(PerfStats AbstractCompositePerfStatsImpl.getPerfStats(..)) && args(stats, type) && target(map);
//    
//    before(Map map, PerfStatsImpl stats, Object type) : putInMap(map, stats, type) {
//        Object res = map.get(stats);
//        if (res == null) {
//            StringBuffer buf = new StringBuffer("put new: "+stats+", map size = "+map.size());
//            PerfStatsImpl impl = (PerfStatsImpl)stats;
//            if (impl.perOperationData.get().accumulatedRequestTime.getEventTime() > 0) {
//                System.err.println("data not cleared!");
//            }
//            for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
//                Entry element = (Entry) iter.next();
//                if (stats.equals(element.getKey())) {
//                    System.err.println("dup entry");
//                }
//                buf.append(", "+element.toString());
//            }
//            addSummary(buf);
//        }
//    }
//    after(Map map, PerfStatsImpl stats, Object type) returning : putInMap(map, stats, type) && if(false) {
//        addSummary("After put: map size is "+map.size());
//    }
//    
//    after(PerfStats stats, Object arg) returning: TraceRepositories.recordStats(stats, arg) {
//        PerfStatsImpl impl = (PerfStatsImpl)stats;
//        addSummary("recorded "+thisJoinPointStaticPart.getSignature()+" "+arg+" for "+stats.getKey()+": "+impl.perOperationData);
//        seenSet().add(stats);
//    }
//    
//    after(AbstractCompositePerfStatsImpl compositeStats) returning: updatingCompositeSummaries(compositeStats) {
//        for (Iterator iter = compositeStats.getUsedStats().iterator(); iter.hasNext();) {
//            PerfStatsImpl stats = (PerfStatsImpl) iter.next();
//            if (stats.getAccumulatedTime() > compositeStats.getAccumulatedTime()) {
//                System.err.println("improper summarization! ");
//                dumpError(compositeStats);
//                break;
//            }
//        }
//    }    
    
    public void dumpError(CompositePerfStatsImpl compositeStats) {   
        for (Iterator iter = getSummary().iterator(); iter.hasNext();) {
            String element = (String) iter.next();
            System.err.println(element);
        }
        //System.err.println("parent "+compositeStats+": totals "+compositeStats.perOperationData.get());
        for (Iterator children = compositeStats.getUsedStats().iterator(); children.hasNext();) {
            PerfStatsImpl stats = (PerfStatsImpl) children.next();
            System.err.println("child "+stats+": totals "+stats.perOperationData.get());
        }
        System.err.println("done");
    }    

}
