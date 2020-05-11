/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import glassbox.monitor.AbstractMonitor;
import glassbox.monitor.OperationFactory;
import glassbox.response.*;
import glassbox.simulator.MultithreadedRunner;
import glassbox.simulator.MultithreadedRunner.RunnableFactory;
import glassbox.track.api.*;
import glassbox.util.timing.api.NoCpuUsageTrackingInfoImpl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

// does not test the default behavior for aggregation: subclass tests test this...
public class PerfStatsImplTest extends MockObjectTestCase {
    protected PerfStats stats;

    public void setUp() {
        stats = createPerfStats();
        ResponseFactory defaultResponseFactory = new DefaultResponseFactory();
        OperationFactory operationFactory = new OperationFactory();
        operationFactory.setResponseFactory(defaultResponseFactory);
        AbstractMonitor.setResponseFactory(defaultResponseFactory);
        AbstractMonitor.setOperationFactory(operationFactory);
    }

    public void testNoOp() {
        assertEquals(0, stats.getCount());
    }
    
    protected void runQuickOp(long timeSnap, long thresh) {
        setSlowThreshold(thresh);

        recordUsage(stats, timeSnap, timeSnap);
        
        recordUsage(stats, timeSnap, timeSnap+thresh-1L);               
    }
    
    public void testQuickOp() {
        long timeSnap = 100L;
        long thresh = 1000L;
        runQuickOp(timeSnap, thresh);

        assertEquals(2, stats.getCount());
        assertEquals(0, stats.getSlowCount());
        assertEquals(thresh-1L, stats.getAccumulatedTime());
        assertEquals(timeSnap, stats.getFirstEventTime());
        assertEquals(thresh, stats.getSlowThreshold());
    }

    public void testSlowOp() {
        long slow = 1000L;
        long timeLag = slow*2;
        setSlowThreshold(slow);

        recordUsage(stats, 0L, timeLag);

        assertEquals(1, stats.getCount());
        assertEquals(1, stats.getSlowCount());
        assertEquals(timeLag, stats.getAccumulatedTime());
        assertEquals(0L, stats.getFirstEventTime());        
        assertEquals(timeLag, stats.getLastEventTime());        
    }
    
    private void runMany(int count, long tm) {
        for (int i=0; i<count; i++) {
            long e = tm + (long)i*10L;
            recordUsage(stats, tm, e);

            tm = e+ 50L;
        }        
    }
    
    public void testMany() {
        long tm = 0L;
        int count = 30;
        runMany(count, tm);

        assertEquals(count, stats.getCount());
        assertEquals(count*(count-1)/2*10L, stats.getAccumulatedTime());
        assertEquals(0L, stats.getFirstEventTime());        
        assertEquals(50L*(count-1)+count*(count-1)/2*10L, stats.getLastEventTime());        
    }
    
    public void testThresholdChange() {
        setSlowThreshold(100L);
        long timeLag = 150L;
        setSlowThreshold(200L);
        recordUsage(stats, 0L, timeLag);

        assertEquals(0, stats.getSlowCount());        
    }
    
    public void testToString() {
        PerfStats statsb = createPerfStats();
        String a = stats.toString();
        String b = statsb.toString();
        assertTrue(!a.equals(""));
        assertTrue(!b.equals(""));
        assertTrue(!b.equals(a));
    }
    
    public void testCopy() throws Exception {
        copyAndAssert(stats);
    }

    private void copyAndAssert(PerfStats stats) throws Exception {
        PerfStats copy = new PerfStatsImpl(stats);
        
        Method[] methods = PerfStats.class.getMethods();
        List notIncluded = Arrays.asList(new String[] { "getStartTime", "getEntries" });
        for (int i=0; i<methods.length; i++) {
            if (methods[i].getName().startsWith("get") && methods[i].getParameterTypes().length==0 && !notIncluded.contains(methods[i].getName())) {
                assertEquals(methods[i].getName()+" differs", methods[i].invoke(stats, null), methods[i].invoke(copy, null));
            }
        }
    }
    
    public void testFailure() throws Exception {
        setSlowThreshold(200L);

        recordFailure(stats, 0L, 100L, new Exception("mockFailure"));
        summarize(stats);

        assertEquals(1, stats.getFailureCount());
        assertEquals(1, stats.getFailingOperationCount());
        assertEquals(0, stats.getSlowCount());
    }
    
    public void testSlowFailure() throws Exception {
        setSlowThreshold(200L);
        UsageTrackingInfo startUsageInfo = new NoCpuUsageTrackingInfoImpl(0L);
        long end = 1000L;

        recordFailure(stats, 0L, end, new Exception("mockFailure"));
        summarize(stats);

        assertEquals(1, stats.getFailureCount());
        assertEquals(1, stats.getFailingOperationCount());
        assertEquals(1, stats.getSlowCount());
        assertEquals(1, stats.getSlowSingleOperationCount());
    }
    
    public void testCopyFailure() throws Exception {
        long end = 100L;
        recordFailure(stats, 0L, end, new Exception("mockFailure"));
        summarize(stats);

        copyAndAssert(stats);
    }
    
    protected Response recordFailure(PerfStats stats, final long start, final long end, Throwable throwable) {
        return recordFailure("failure response", stats, start, end, throwable, false);
    }
    
    /**
     * 
     * @param role
     * @param stats
     * @param start
     * @param end
     * @param throwable
     * @param subsequent if there was already a failure: we have advice that limits us to recording a failling request ONCE
     * @return
     */
    protected Response recordFailure(String role, PerfStats stats, final long start, final long end, Throwable throwable, boolean subsequent) {
        FailureDescription failureDescription = new DefaultFailureDetectionStrategy().getFailureDescription(throwable);
        
        Mock mockResponse = mock(Response.class, role);
        mockResponse.stubs().method("getStart").will(returnValue(start));
        mockResponse.stubs().method("getEnd").will(returnValue(end));
        mockResponse.stubs().method("getDuration").will(returnValue(end-start));
        mockResponse.stubs().method("get").with(eq(Response.FAILURE_DATA)).will(returnValue(failureDescription));
        mockResponse.stubs().method("get").with(eq(Response.REQUEST)).will(returnValue(null));
        mockResponse.stubs().method("get").with(eq(Response.PARAMETERS)).will(returnValue(null));
        mockResponse.expects(atLeastOnce()).method("set").with(eq(PerfStatsImpl.RECORDED), ANYTHING).isVoid();
        if (!subsequent) {
            mockResponse.expects(atLeastOnce()).method("set").with(eq(Response.REQUEST), ANYTHING).will(returnValue(null));
        }
        mockResponse.stubs().method("get").with(eq(PerfStatsImpl.RECORDED)).will(returnValue(new Integer(0)));
        mockResponse.stubs().method("getParent").withNoArguments().will(returnValue(null));
        mockResponse.stubs().method("get").with(eq("background")).will(returnValue(null));

        Response response = (Response)mockResponse.proxy();
        
        stats.recordEnd(response);
        return response;
    }
    
    public void testMultiThreaded() {
        MultithreadedRunner runner = new MultithreadedRunner();
        final int count = 10;
        final int nThreads = 3;
        Thread[] threads = runner.createThreads(nThreads, new RunnableFactory() { 
            public Runnable create(int nThreads) { 
                return new Runnable() {
                    public void run() {
                        long tm = 0L;
                        runMany(count, tm);
                    }
                };
            }
        });

        runner.run(threads);
        
        assertEquals(nThreads*count, stats.getCount());
        assertEquals(nThreads*count*(count-1)/2*10L, stats.getAccumulatedTime());
        assertEquals(0L, stats.getFirstEventTime());        
        assertEquals(50L*(count-1)+count*(count-1)/2*10L, stats.getLastEventTime());        
    }
    
    public void testElapsedCall() {
        long start = 10L;
        long end = 110L;

        stats.recordUsage(start, end, 0L);
        stats.recordUsage(25L, 175L, 20L);
        summarize(stats);

        assertEquals(10L, stats.getFirstEventTime());
        assertEquals(175L, stats.getLastEventTime());
        assertEquals(2, stats.getCount());
        assertEquals(250L, stats.getAccumulatedTime());
    }

    public void testEarlierTime() {
        stats.recordUsage(10L, 100L, 0L);
        stats.recordUsage(5L, 80L, 20L);
        summarize(stats);

        assertEquals(5L, stats.getFirstEventTime());
        assertEquals(100L, stats.getLastEventTime());
    }
    
    protected void setSlowThreshold(long thresh) {
        stats.setSlowThreshold(thresh);        
    }
    
    protected final PerfStats createPerfStats() {
        PerfStats stats = makePerfStats();
        stats.setSlowThreshold(1000L); // default: can't use zero
        Mock mockType = mock(StatisticsType.class);
        Mock mockCall = mock(CallDescription.class);
        mockCall.stubs().method("getCallKey").will(returnValue("mockCallKeyValue"));
        mockCall.stubs().method("getResourceKey").will(returnValue("mockResourceKeyValue"));
        mockCall.stubs().method("getSummary").will(returnValue("mockResourceDescription"));
        mockType.stubs().method("getCall").will(returnValue(mockCall.proxy()));
        mockType.stubs().method("getLayer").will(returnValue("layer"));
        stats.setType((StatisticsType)mockType.proxy());
        return stats;
    }    
    
    protected PerfStats makePerfStats() {
        return new PerfStatsImpl();
    }
    
    protected void recordUsage(PerfStats stats, long start, long end) {
        stats.recordUsage(start, end, 0L);
        summarize(stats);
    }
    
    protected void summarize(PerfStats stats) {
        stats.summarizeOperation(null);
    }        
}
