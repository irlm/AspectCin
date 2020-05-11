/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test;

import glassbox.analysis.OperationAnalyzer;
import glassbox.analysis.ProblemFactory;
import glassbox.monitor.thread.ThreadMonitor15Impl;
import glassbox.track.api.PerfStats;
import glassbox.track.api.PerfStatsImpl;
import glassbox.util.timing.api.TimeConversion;

/**
 * This helper class lets us run integration tests for "slow" using a 200 ms delay instead of a 2.1s delay, which would
 * be required if we didn't override the normal 2s threshold... 
 * 
 * @author Ron Bodkin
 *
 */
public class TimingTestHelper {

    public static final long TICK_TIME = 50L * TimeConversion.NANOSECONDS_PER_MILLISECOND;
    public static final long SLOW_THRESHOLD = TICK_TIME*6;
    public static final long GUARANTEED_SLOW = 480L * TimeConversion.NANOSECONDS_PER_MILLISECOND;

    
    public static OperationAnalyzer getTestAnalyzer() {
        OperationAnalyzer opan = new OperationAnalyzer();
        opan.setSlowThresholdMillis(SLOW_THRESHOLD / TimeConversion.NANOSECONDS_PER_MILLISECOND);
        opan.setProblemFactory(new ProblemFactory());
        return opan;
    }
    
    public static void setSlowThreshold(PerfStats stats) {
        ((PerfStatsImpl)stats).setSlowThreshold(SLOW_THRESHOLD);
    }
    static aspect TraceTiming {
        static final boolean traceTimes = Boolean.getBoolean("glassbox.test.TraceTiming");
//        after() returning (long tm): call(* Clock.getTime()) {
//            System.out.println("time = "+((double)tm)/1000000.);
//        }
//        before(PerfStatsImpl stats) : call(* *()) && target(stats) && scope() && !within(TraceTiming) {
//            System.out.println(thisJoinPoint+" on "+stats+" accum is "+stats.getAccumulatedTime()/Clock.NANOSECONDS_PER_SECOND);
//        }
//        before(DelayingRunnable delayer) : execution(* run(..)) && target(delayer) && scope() {
//            System.out.println("delaying for "+delayer.getDelay());
//        }
//        before(long tm) : call(* sleep(long)) && args(tm) {
//            System.out.println("sleep for "+tm+" at "+thisJoinPointStaticPart);
//        }
        after(PerfStats stats, long time) returning: 
          if(traceTimes) && execution(* PerfStats.record*(..)) && this(stats) && args(time, ..) {
            System.out.println("Start: "+time+", "+this+"/"+System.identityHashCode(this));
        }
    }
//    static abstract aspect MonitorSlow {
//        abstract pointcut slowStart();
//        pointcut slowEnd() : slowStart();
        
//        before() : slowStart() {
//            final Thread testThread = Thread.currentThread();
//            Thread monitorThread = new Thread() {
//                public void run() {
//                    ThreadMonitorImpl.dumpRepeatedly(testThread, 12000000, 5);
//                }
//            };
//            monitorThread.start();
//        }
        
//        after() returning: slowEnd() {
//            monitorThread.join();
//        }
//    }
}
