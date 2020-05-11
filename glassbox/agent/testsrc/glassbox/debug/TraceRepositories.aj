/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.debug;

import edu.emory.mathcs.util.WeakIdentityHashMap;
import glassbox.track.api.PerfStatsImpl;
import glassbox.track.api.StatisticsRegistry;

// will need rewriting to track StatsSummarizer
public privileged aspect TraceRepositories {

//    pointcut scope();// : within(*);
//    pointcut repoScope();
//    
//    after(StatisticsRegistry rep) returning: execution(StatisticsRegistry+.new(..)) && this(rep) && repoScope() {
//        System.err.println("Created new registry: "+rep);
//    }
//    
//    after(StatisticsRegistry registry, Object type, Serializable key) returning (PerfStats stats): 
//            execution(* StatisticsRegistry.getPerfStats(..)) && target(registry) && args(type, key) && scope() {
//        System.err.println("get perf stats for registry: "+registry+" for "+type+", "+key+" returned "+stats);
//    }
//
//    public pointcut recordStatsExec(PerfStats stats, Object arg): 
//        execution(* PerfStats.record*(..)) && target(stats) && args(arg, ..);
//    // work around aspectj bug ... if you use cflowbelow there's an NPE
//    public pointcut recordStats(PerfStats stats, Object arg):
//        recordStatsExec(stats, arg);// && !cflowbelow(recordStatsExec(*, *));
//    
//    after(PerfStats stats, Object arg) returning: recordStats(stats, arg) && scope() {
//        System.err.println(thisJoinPoint.getSignature().getName()+" for "+stats.getKey()+" "+stats+" with arg1 = "+arg);
//    }
//    after(PerfStats stats, PerfStatsImpl.PerOperationStruct totals) returning: 
//            call(* PerfStats+.updateSummaries(*)) && target(stats) && scope() && args(totals, ..) {
//        System.err.println(thisJoinPoint.getSignature().getName()+" adding "+TimeConversion.formatTime(totals.accumulatedRequestTime)+" for "+stats.getKey()+" "+stats);
//    }
    
}
