/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.thread;

import glassbox.summary.thread.ThreadSummarization;
import glassbox.test.DelayingRunnable;
import glassbox.test.TimingTestHelper;
import glassbox.test.ajmock.InvokeAtLeastCountMatcher;
import glassbox.test.ajmock.VirtualMockObjectTestCase;
import glassbox.track.api.CompositePerfStats;
import glassbox.track.api.CompositePerfStatsImpl;

import java.util.Iterator;

import org.jmock.Mock;

/**
 * 
 * @author Ron Bodkin
 * @see TestThreadSummarization for more integration tests of thread monitor integration 
 */
public class ThreadMonitorIntegrationTest extends VirtualMockObjectTestCase {

    private ThreadMonitorIntegration monitorIntegration = ThreadMonitorIntegration.aspectOf();
    private ThreadMonitor monitor;
    private Mock monitorMock;
    private static final int INTERVALS = 4;

    public void setUp() {
        monitorMock = mock(ThreadMonitor.class);
        monitor = (ThreadMonitor)monitorMock.proxy();
    }
    
    public void testMonitorIsScheduled() {
        monitorMock.expects(new InvokeAtLeastCountMatcher(INTERVALS-2)).method("run");
        monitorMock.expects(atLeastOnce()).method("getInterval").will(returnValue(TimingTestHelper.TICK_TIME));

        doMonitor();
    }
    
    public void testNoMonitorIfUnscheduled() {
        monitorMock.expects(atLeastOnce()).method("getInterval").will(returnValue(0L));
        
        doMonitor();
    }
    
    public void testWithNoMonitor() {
        monitorIntegration.setThreadMonitor(null);
        DelayingRunnable.sleep(TimingTestHelper.TICK_TIME*2);
    }
    
    // we really need to advise the mock's execution here (that would require LTW if we used jMock)
    //LTW would let us use our virtual mocks too
    // instead, we create a good old hand written mock object that can be woven statically...
    
    private interface DummyThreadMonitor extends ThreadMonitor {
        int getInvocationCount();        
    }
    
    private class DummyThreadMonitorImpl extends ThreadMonitor15Impl implements DummyThreadMonitor {
        int count = 0;

        public DummyThreadMonitorImpl(long interval) {
            super(interval);
        }
        
        public void run() {
            count++;
        }
        
        public int getInvocationCount() {
            return count;
        }
    }
   
    private DummyThreadMonitor dummyMonitor;
    
    private void setUpTestChange() throws Throwable {
        dummyMonitor = new DummyThreadMonitorImpl(0L);
        monitorIntegration.setThreadMonitor(dummyMonitor);
    }
    
    public void testChangeInterval() {
        try {
            setUpTestChange();
            assertEquals(0, dummyMonitor.getInvocationCount());       
            
            dummyMonitor.setInterval(TimingTestHelper.TICK_TIME);
            DelayingRunnable.sleep(TimingTestHelper.TICK_TIME*INTERVALS);
            monitorIntegration.setThreadMonitor(null);
            assertTrue(dummyMonitor.getInvocationCount() + " is bad", dummyMonitor.getInvocationCount() >= INTERVALS-1);
        } catch (Throwable t) {/*oldVM*/}
    }
    
    public void testManyChanges() {
        try {
            setUpTestChange();
            monitorIntegration.setThreadMonitor(null);
            dummyMonitor.setInterval(TimingTestHelper.TICK_TIME);
            assertEquals(0, dummyMonitor.getInvocationCount());
            
            monitorIntegration.setThreadMonitor(dummyMonitor);
            DelayingRunnable.sleep(TimingTestHelper.TICK_TIME*2);
            int count = dummyMonitor.getInvocationCount();
            assertTrue("count "+count+" should be at least 1", 1 <= count);
            assertTrue("count "+count+" should be at most 3", 3 >= count);
            
            dummyMonitor.setInterval(TimingTestHelper.TICK_TIME*5);
            DelayingRunnable.sleep(TimingTestHelper.TICK_TIME*2);
            monitorIntegration.setThreadMonitor(null);            
            int nCount = dummyMonitor.getInvocationCount();
            assertTrue(1 <= nCount - count);
                   
            DelayingRunnable.sleep(TimingTestHelper.TICK_TIME*2);
            monitorIntegration.setThreadMonitor(null);
            assertEquals(nCount, dummyMonitor.getInvocationCount());
        } catch (Throwable t) {/*oldVM*/}
    }
    
    public void testStopMonitoringConcurrency() {
        CompositePerfStats stats = new CompositePerfStatsImpl();
    	
    	ThreadMonitor monitor = new ThreadMonitor15Impl(100);

    	Thread thread = Thread.currentThread();
    	
    	monitor.startMonitoring(thread, null);
    	
    	monitor.run();
    	
    	OperationSample sample = monitor.getCurrentData();
    	
    	Thread monitorThread = new Thread(monitor);
    	
    	MultiThreadTestController.enabled = true;
    	//MultiThreadTestController.register(monitorThread);
    	
    	
    	monitorThread.start();
    	
		ThreadSummarization.aspectOf().getSummarizer().summarize(sample, stats);
		
		monitorThread.join();
    }
    
    
    static aspect MultiThreadTestController {
    	//boolean daemonRuns = true;
    	int currentStep = 0;
    	static boolean enabled = false;
    	
    	//pointcut inDaemon() : if(Thread.currentThread() == monitorThread); 
    		   	
    	pointcut readingMonitored() : 
    		call(* getValue(..)) && withincode(* ThreadMonitor15Impl.monitorThreads(..));
    	
    	after(): readingMonitored() {
    		setTo(10);
    	}    	
    	
    	// request thread stopping summarizing
    	pointcut snapshotInSummary() : call(* iterator()) && within(ThreadSummarization); //execution(* OperationSample.getSnapshots(..));
    	
    	before(): snapshotInSummary() {
    		waitFor(10);
    	}    	
    	after(): snapshotInSummary() {
    		setTo(20);
    	}    	
    	
//    	before(): execution(* ThreadMonitor.stopMonitoring(..)) {
//    		executeAt(1);    		
//    	}    	

    	// daemon thread records
    	pointcut daemonRecord() : execution(* OperationSample.record(..));
    	
    	before() : daemonRecord() {
    		waitFor(20);
    	}
    	after() : daemonRecord() {
    		setTo(30);
    	}
    	
    	before():  call(* Iterator.next()) && within(ThreadSummarization) {
    		waitFor(30);
    	}    	
    	    	
    	private void waitFor(int n) {
    		
    		if(enabled) { 
	    		//System.out.println("Thread: " + Thread.currentThread().getId() + " waitFor(" + n + ") curstep is:" + currentStep );
    			int loopCount = 0;
    			if (/*!anotherThreadWaitingForMe()*/ currentStep < n && loopCount < 1000) {
	    			//System.out.println("Thread: " + Thread.currentThread().getId() + " waitFor(" + n + ") curstep is:" + currentStep);
	    			Thread.sleep(0, 100);
	    		}
    		}
    	}
    	
    	private void setTo(int n) {
    		if(enabled) {
    			//System.out.println("Thread: " + Thread.currentThread().getId() + " setTo(" + n + ") curstep was:" + currentStep);

    			currentStep = n;
    		}
    	}
    	
//    	void around() : adviceexecution() && within(MultiThreadTestController) {
//    		if(enabled) {
//    			proceed();
//    		}
//    	}
    }
    
    private void doMonitor() {
        monitorIntegration.setThreadMonitor(monitor);
        DelayingRunnable.sleep(TimingTestHelper.TICK_TIME*INTERVALS);
        monitorIntegration.setThreadMonitor(null);                
    }
}
