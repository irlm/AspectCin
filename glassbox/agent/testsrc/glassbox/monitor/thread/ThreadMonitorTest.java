/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.thread;

import glassbox.simulator.MultithreadedRunner;
import glassbox.simulator.ui.LockingRunnable;
import glassbox.test.DelayingRunnable;
import glassbox.test.TimingTestHelper;
import glassbox.track.api.PerfStats;
import glassbox.track.api.PerfStatsImpl;
import glassbox.util.jmx.JmxManagement;
import glassbox.util.timing.ClockManager;
import glassbox.util.timing.api.TimeConversion;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

public class ThreadMonitorTest extends TestCase {
    ThreadMonitor15Impl monitor;
    Thread runThread;
    
    public void setUp() {
        monitor = new ThreadMonitor15Impl(TimingTestHelper.TICK_TIME);
        LockingRunnable runnable = new LockingRunnable(TimingTestHelper.TICK_TIME); 
        runThread = new Thread(runnable);
        JmxManagement.aspectOf().setEnabled(false);
    }

    public void testMonitorOne() {
        monitor.startMonitoring(runThread, null);
        assertEquals(0, monitor.getCurrentData(runThread).getSnapshots().size());
        
        synchronized (LockingRunnable.class) {
            runThread.start();
            DelayingRunnable.sleep(TimingTestHelper.TICK_TIME);
            monitor.run();
        }
        long time = ClockManager.getTime();
        runThread.join();
        
        OperationSample sample = monitor.getCurrentData(runThread); 
        List l = sample.getSnapshots();
        assertEquals(monitor.getInterval(), sample.getInterval());
        assertEquals(1, l.size());
        ThreadSnapshot snapshot = (ThreadSnapshot)l.get(0);
        assertTrue(snapshot.getUsageInfo().getEventTime()+" should be close to "+time,
                snapshot.getUsageInfo().getEventTime()<=time+sample.getInterval());
        assertTrue(TimeConversion.convertNanosToMillis(snapshot.getUsageInfo().getEventTime())+" should be less than tick before "+TimeConversion.convertNanosToMillis(time), snapshot.getUsageInfo().getEventTime()+2*TimingTestHelper.TICK_TIME>=time+sample.getInterval());
        StackTraceElement[] trace = snapshot.getStackTrace();
        int matchCount = 0;
        for (int i=0; i<trace.length; i++) {
            if (LockingRunnable.class.getName().equals(trace[i].getClassName())) {
                if ("run".equals(trace[i].getMethodName())) {
                    matchCount++;
                }
            }
            assertFalse(ThreadMonitorTest.class.getName().equals(trace[i].getClassName()));
        }
        assertEquals("no match in "+traceToString(trace), 1, matchCount);
        // we can't assert there's no monitor; some VM's (e.g., JRockIt use a monitor for sleep)
    }
    
    public static String traceToString(StackTraceElement[] trace) {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<trace.length; i++) {
            buf.append(trace[i].toString());
        }
        return buf.toString();
    }
    
    public void testNoMonitor() {
        runThread.start();
        monitor.run();
        runThread.join();
        
        assertNull(monitor.getCurrentData(runThread));
    }
    
    public void testStopMonitor() {
        monitor.startMonitoring(runThread, null);        
        monitor.stopMonitoring(runThread, null);        
        runThread.start();
        monitor.run();
        runThread.join();
        
        assertNull(monitor.getCurrentData(runThread));
    }
    
    public void testDetectContention() {
        int nThreads = 4;
        ThreadMonitor15Impl monitor = new ThreadMonitor15Impl(TimingTestHelper.TICK_TIME);
        MultithreadedRunner runner = new MultithreadedRunner();
        
        // use raw runnables to avoid triggering other monitors...
        Thread[] threads = new Thread[nThreads];
        for (int i=0; i<nThreads; i++) {
            Runnable r = new LockingRunnable(TimingTestHelper.TICK_TIME*2);
            threads[i] = new Thread(r, "instance "+i);
            monitor.startMonitoring(threads[i], null);
        }
        
        synchronized(LockingRunnable.class) {
            runner.start(threads);
            DelayingRunnable.sleep(TimingTestHelper.TICK_TIME);
            monitor.run();
        }
        runner.join(threads);
        
        String lockName = null;
        int lCount = 0;
        for (int i = 0; i < nThreads; i++) {
            List l = monitor.getCurrentData(threads[i]).getSnapshots();
            for (Iterator iter = l.iterator(); iter.hasNext();) {
                ThreadSnapshot snapshot = (ThreadSnapshot) iter.next();
                if (snapshot.getLockName() != null) {
                    // there may be other points where the thread is waiting on another monitor
                    if (snapshot.getLockName().startsWith(Class.class.getName())) {
                        lCount++;
                        break;
                    }
                }
            }
        }
        assertEquals(lCount, nThreads);
    }

    public void testNoContention() {
        int nThreads = 4;
        MultithreadedRunner runner = new MultithreadedRunner();
        
        // use raw runnables to avoid triggering other monitors...
        Thread[] threads = new Thread[nThreads];
        for (int i=0; i<nThreads; i++) {
            Runnable r = new DelayingRunnable(TimingTestHelper.TICK_TIME+1);
            threads[i] = new Thread(r, "instance "+i);
            monitor.startMonitoring(threads[i], null);
        }
        
        runner.start(threads);
        DelayingRunnable.sleep(TimingTestHelper.TICK_TIME);
        monitor.run();
        runner.join(threads);
        
        String lockName = null;
        for (int i = 0; i < nThreads; i++) {
            List l = monitor.getCurrentData(threads[i]).getSnapshots();
            for (Iterator iter = l.iterator(); iter.hasNext();) {
                ThreadSnapshot snapshot = (ThreadSnapshot) iter.next();
                assertTrue(null==snapshot.getLockName() || !snapshot.getLockName().startsWith(Class.class.getName()));
            }
        }
    }

    public void testDeadThread() {
        Thread t = new Thread(new DelayingRunnable(0));
        monitor.startMonitoring(t, null);
        t.start();
        t.join();
        monitor.run();
        assertEquals(0, monitor.getCurrentData(t).getSnapshots().size());
    }
    
    // does the monitor work for threads that hide their id's when finished
    public void testBadDeadThreadId() {
        Thread t = new Thread() {
            public long getId() {
                return -1;
            }
        };
        monitor.startMonitoring(t, null);
        t.start();
        t.join();
        monitor.run();
        assertEquals(0, monitor.getCurrentData(t).getSnapshots().size());
    }
    
    // does the monitor work for threads that hide their id's for other reasons
    public void testBadLiveThreadId() {
        Thread t = new Thread(new DelayingRunnable(TimingTestHelper.TICK_TIME)) {
            public long getId() {
                return -1;
            }
        };
        monitor.startMonitoring(t, null);
        t.start();
        monitor.run();
        t.join();
        assertTrue(monitor.getCurrentData(t).getSnapshots().size()>0);
    }

    static Throwable failException = null;
    
    //test thread safety (daemon writer, other readers)
    public void testConcurrent() throws Throwable {
        int nThreads = 50;
        final int count = 30;        
        MultithreadedRunner runner = new MultithreadedRunner();
        Thread[] threads = runner.createThreads(nThreads, new Runnable() {
            public void run() {
                try {
                	PerfStats stats = new PerfStatsImpl();
                    for (int i=0; i<count*25; i++) {
                        monitor.startMonitoring(null);
                        monitor.stopMonitoring(stats);
                    }
                } catch (Throwable t){
                    failException = t;
                }
            }
        });
        runner.start(threads);
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
 
        long startUserTime = mxBean.getThreadCpuTime(Thread.currentThread().getId());
        for (int i=0; i<count; i++) {
            monitor.run();
        }
        long endUserTime = mxBean.getThreadCpuTime(Thread.currentThread().getId());
        long monTime = ClockManager.getTime();
        runner.join(threads);
        long runTime = ClockManager.getTime();
        if (failException != null) {
            throw failException;
        }
        long avgTimeInUs = (endUserTime-startUserTime)/count/1000;
        
        System.out.println("monitor cpu time is " + avgTimeInUs + " us per execution");
        System.out.println("run time is "+TimeConversion.convertNanosToMillis(runTime-monTime)+" ms");
        
        //TODO: Figure out what is wrong with Linux here....
        if(!System.getProperty("os.name").equals("Linux")) { 
            assertTrue("Too much time for thread monitor "+avgTimeInUs, avgTimeInUs<=50);        
        }
    }
    
}
