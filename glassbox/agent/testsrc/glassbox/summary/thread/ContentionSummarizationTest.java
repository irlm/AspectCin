/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.summary.thread;

import glassbox.monitor.AbstractMonitor;
import glassbox.monitor.OperationFactory;
import glassbox.monitor.thread.*;
import glassbox.response.DefaultResponseFactory;
import glassbox.response.ResponseFactory;
import glassbox.simulator.ui.MockServletOk;
import glassbox.summary.StatsSummarizer;
import glassbox.test.MockServlet;
import glassbox.test.TimingTestHelper;
import glassbox.track.ThreadStats;
import glassbox.track.api.*;
import glassbox.util.timing.api.NoCpuUsageTrackingInfoImpl;

import java.util.*;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.stub.VoidStub;

public class ContentionSummarizationTest extends MockObjectTestCase {

    private StatisticsRegistry registry;
    private OperationFactory operationFactory;

    private MockServlet sa;
    private Mock statsMock;
    private Mock timeStatsMock;

    private ThreadState state;

    private ThreadSummarizer summarizer = new ThreadSummarizerImpl();

    // this test tests to make sure we summarize contention at the end of a UofW
    // this one is a little integration test because of problems using jMock to
    // drive the uow monitor
    // XXX fixme
    // these tests actually do a lot of testing of ThreadMonitorIntegration
    public void testSummarizationLifecycle() {
        // setUpIntegration();
        // XXX the summarizer isn't called; is it because a null stats is passed?
        // Mock mockSummarizer = mock(ThreadSummarizer.class);
        // mockSummarizer.expects(once()).method("summarize");
        // ThreadSummarizer summarizer = ContentionSummarization.aspectOf().getSummarizer();
        // try {
        // ContentionSummarization.aspectOf().setSummarizer((ThreadSummarizer)mockSummarizer.proxy());

        // sa.forceDoGet();
        // } finally {
        // ContentionSummarization.aspectOf().setSummarizer(summarizer);
        // }
    }

    protected void setUpIntegration() {
        sa = new MockServletOk(); // this goes first to force initialization: it's a little suboptimal that we always
                                    // initialize the world
        Mock threadMon = mock(ThreadMonitor.class);
        OperationSample sampleResult = makeOperationSample(100L);
        ThreadSnapshot snapshot = createSampleLock1(new NoCpuUsageTrackingInfoImpl(0L));
        state = snapshot.getState();
        sampleResult.record(snapshot);
        threadMon.stubs().method("getInterval").will(returnValue(0L));
        threadMon.expects(once()).method("startMonitoring").id("start monitoring");
        threadMon.expects(once()).method("stopMonitoring").after("start monitoring").will(returnValue(sampleResult));
        ThreadMonitorIntegration.aspectOf().setThreadMonitor((ThreadMonitor) threadMon.proxy());
        ThreadStats threadStats = new ThreadStats();
        registry = new StatisticsRegistryImpl();
        threadStats.setRegistry(registry);
        StatsSummarizer summarizer = new StatsSummarizer(); 
        summarizer.setThreadStats(threadStats);
        List listeners = new ArrayList();
        listeners.add(summarizer);
        ResponseFactory defaultResponseFactory = new DefaultResponseFactory(); 
        defaultResponseFactory.setListeners(listeners);
        operationFactory = new OperationFactory();
        operationFactory.setResponseFactory(defaultResponseFactory);
        AbstractMonitor.setResponseFactory(defaultResponseFactory);
        AbstractMonitor.setOperationFactory(operationFactory);
    }

    protected void tearDown() {
        ThreadMonitorIntegration.aspectOf().setThreadMonitor(null);
    }

    protected void setUpStats(OperationSample results) {
        statsMock = mock(CompositePerfStats.class, "holder stats mock");
        timeStatsMock = mock(TreeTimeStats.class, "held time stats mock");
        statsMock.stubs().method("getPerfStats").with(eq(StatisticsType.SlowMethodIdx), ANYTHING).will(returnValue(timeStatsMock.proxy()));        
        timeStatsMock.stubs().method("recordSample").with(eq(results)).will(VoidStub.INSTANCE);
        timeStatsMock.stubs().method("getType").will(returnValue(null));
    }

    protected void verifyOne() {
        CompositePerfStats theStats = (CompositePerfStats)registry.getPerfStats(StatisticsTypeImpl.UiRequest,
                operationFactory.makeOperation(HttpServlet.class, MockServletOk.class.getName()));
        Iterator it = theStats.getEntriesForType(StatisticsTypeImpl.Contention);
        assertTrue(it.hasNext());
        Entry entry = (Entry) it.next();
        assertEquals(state, entry.getKey());
        PerfStats value = (PerfStats) entry.getValue();
        assertEquals(1, value.getCount());
        assertFalse(it.hasNext());
    }

    public void testNoData() {
        OperationSample results = makeOperationSample(TimingTestHelper.TICK_TIME);
        setUpStats(results);

        summarizer.summarize(results, (CompositePerfStats) statsMock.proxy());
    }

    private OperationSample setUpMany(ThreadState state, int count, UsageTrackingInfo startUsage, long interval) {
        OperationSample results = makeOperationSample(interval);
        long time = startUsage.getEventTime();
        for (int i = 0; i < count; i++) {
            ThreadSnapshot snapshot = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(time), state);
            results.record(snapshot);
            time += interval;
        }
        return results;
    }

    public void testOneLock() {
        UsageTrackingInfo START_USAGE = new NoCpuUsageTrackingInfoImpl(100L);
        UsageTrackingInfo END_USAGE = new NoCpuUsageTrackingInfoImpl(START_USAGE.getEventTime()
                + TimingTestHelper.TICK_TIME);
        ThreadState state = createSampleLock1(new NoCpuUsageTrackingInfoImpl(0L)).getState();
        OperationSample results = setUpMany(state, 2, START_USAGE, TimingTestHelper.TICK_TIME);
        setUpStats(results);
        Mock contentionMock = mock(PerfStats.class, "mock contention stats");
        statsMock.expects(atLeastOnce()).method("getPerfStats").with(eq(StatisticsTypeImpl.Contention), eq(state))
                .will(returnValue(contentionMock.proxy()));
        contentionMock.expects(once()).method("recordUsage").with(sametime(START_USAGE), sametime(END_USAGE),
                eq(TimingTestHelper.TICK_TIME)).will(returnValue(0L));
        contentionMock.stubs().method("getType").will(returnValue(null));
        statsMock.expects(atLeastOnce()).method("getDirectCountForType").with(eq(StatisticsTypeImpl.Contention)).
            will(returnValue(1));

        summarizer.summarize(results, (CompositePerfStats) statsMock.proxy());
    }

    public void testOneLockMany() {
        UsageTrackingInfo START_USAGE = new NoCpuUsageTrackingInfoImpl(100L);
        UsageTrackingInfo END_USAGE = new NoCpuUsageTrackingInfoImpl(START_USAGE.getEventTime() + 4
                * TimingTestHelper.TICK_TIME);
        ThreadState state = createSampleLock1(new NoCpuUsageTrackingInfoImpl(0L)).getState();
        OperationSample results = setUpMany(state, 5, START_USAGE, TimingTestHelper.TICK_TIME);
        setUpStats(results);
        Mock contentionMock = mock(PerfStats.class);
        statsMock.expects(atLeastOnce()).method("getPerfStats").with(eq(StatisticsTypeImpl.Contention), eq(state))
                .will(returnValue(contentionMock.proxy()));
        contentionMock.expects(once()).method("recordUsage").with(sametime(START_USAGE), sametime(END_USAGE),
                eq(TimingTestHelper.TICK_TIME)).will(returnValue(0L));        
        contentionMock.stubs().method("getType").will(returnValue(null));
        statsMock.expects(atLeastOnce()).method("getDirectCountForType").with(eq(StatisticsTypeImpl.Contention)).
            will(returnValue(1));

        summarizer.summarize(results, (CompositePerfStats) statsMock.proxy());
    }

    public void testTwoLocks() {
        OperationSample results = makeOperationSample(2 * TimingTestHelper.TICK_TIME);
        UsageTrackingInfo START_USAGE = new NoCpuUsageTrackingInfoImpl(5 * TimingTestHelper.TICK_TIME);
        UsageTrackingInfo END_USAGE1 = new NoCpuUsageTrackingInfoImpl(START_USAGE.getEventTime() + 2
                * TimingTestHelper.TICK_TIME);
        UsageTrackingInfo END_USAGE2 = new NoCpuUsageTrackingInfoImpl(START_USAGE.getEventTime() + 4
                * TimingTestHelper.TICK_TIME);
        ThreadSnapshot snapshot = createSampleLock1(START_USAGE);
        ThreadState state = snapshot.getState();
        results.record(snapshot);

        snapshot = createSampleLock1(END_USAGE1);
        results.record(snapshot);

        snapshot = createSampleLock2(END_USAGE2);
        results.record(snapshot);

        setUpStats(results);
        Mock contentionMock1 = mock(PerfStats.class);
        Mock contentionMock2 = mock(PerfStats.class);
        statsMock.expects(atLeastOnce()).method("getPerfStats").with(eq(StatisticsTypeImpl.Contention), eq(state))
                .will(returnValue(contentionMock1.proxy()));
        statsMock.expects(atLeastOnce()).method("getPerfStats").with(eq(StatisticsTypeImpl.Contention),
                eq(snapshot.getState())).will(returnValue(contentionMock2.proxy()));
        contentionMock1.expects(once()).method("recordUsage").with(sametime(START_USAGE), sametime(END_USAGE1),
                eq(2 * TimingTestHelper.TICK_TIME)).will(returnValue(0L));
        contentionMock1.stubs().method("getType").will(returnValue(null));
        
        contentionMock2.expects(once()).method("recordUsage").with(sametime(END_USAGE2), sametime(END_USAGE2),
                eq(2 * TimingTestHelper.TICK_TIME)).will(returnValue(0L));
        contentionMock2.stubs().method("getType").will(returnValue(null));
        statsMock.expects(atLeastOnce()).method("getDirectCountForType").with(eq(StatisticsTypeImpl.Contention)).
            will(onConsecutiveCalls(returnValue(1), returnValue(2)));

        summarizer.summarize(results, (CompositePerfStats) statsMock.proxy());
    }

    public void testManyLocks() {
        // ensure there's a limit
        UsageTrackingInfo START_USAGE = new NoCpuUsageTrackingInfoImpl(100L);
        UsageTrackingInfo END_USAGE = new NoCpuUsageTrackingInfoImpl(START_USAGE.getEventTime() + 4
                * TimingTestHelper.TICK_TIME);
        ThreadState state = createSampleLock1(new NoCpuUsageTrackingInfoImpl(0L)).getState();
        OperationSample results = setUpMany(state, 5, START_USAGE, TimingTestHelper.TICK_TIME);
        setUpStats(results);
        Mock contentionMock = mock(PerfStats.class);
        statsMock.expects(atLeastOnce()).method("getPerfStats").with(eq(StatisticsTypeImpl.Contention), eq(state))
                .will(returnValue(contentionMock.proxy()));

        contentionMock.expects(once()).method("recordUsage").with(sametime(START_USAGE), sametime(END_USAGE),
                eq(TimingTestHelper.TICK_TIME)).will(returnValue(0L));
        statsMock.expects(atLeastOnce()).method("getDirectCountForType").with(eq(StatisticsTypeImpl.Contention)).will(
                returnValue(ThreadSummarizerImpl.MAX_CONTENTION_TRACES + 1));

        Mock singleMarginal = mock(PerfStats.class, "single marginal");
        singleMarginal.stubs().method("getSlowCount").will(returnValue(1));
        singleMarginal.stubs().method("getAccumulatedTime").will(returnValue(1000L));
        singleMarginal.stubs().method("getKey").will(returnValue("single marginal"));
        Mock singleVerySlow = mock(PerfStats.class, "single very slow");
        singleVerySlow.stubs().method("getSlowCount").will(returnValue(1));
        singleVerySlow.stubs().method("getAccumulatedTime").will(returnValue(10000L));
        singleVerySlow.stubs().method("getKey").will(returnValue("single very slow"));
        Mock twoMarginal = mock(PerfStats.class, "two marginal");
        twoMarginal.stubs().method("getSlowCount").will(returnValue(2));
        twoMarginal.stubs().method("getAccumulatedTime").will(returnValue(500L));
        twoMarginal.stubs().method("getKey").will(returnValue("two marginal"));
        Mock newEntry = mock(PerfStats.class, "new entry");
        newEntry.stubs().method("getSlowCount").will(returnValue(1));
        newEntry.stubs().method("getAccumulatedTime").will(returnValue(500L));
        newEntry.stubs().method("getKey").will(returnValue(state));

        Object[] existing = new Object[] { twoMarginal.proxy(), twoMarginal.proxy(), singleVerySlow.proxy(),
                singleMarginal.proxy(), newEntry.proxy() };

        Map map = new HashMap();
        for (int i=0; i<existing.length; i++) {
            PerfStats stats = (PerfStats)existing[i];
            map.put(stats.getKey(), stats);
        }
        
        statsMock.expects(atLeastOnce()).method("getDirectEntriesForType").with(eq(StatisticsTypeImpl.Contention)).will(
                returnValue(map.entrySet().iterator()));

        statsMock.expects(once()).method("removePerfStats").with(eq(StatisticsTypeImpl.Contention), not(eq(state))).will(
                returnValue(singleMarginal.proxy()));
        contentionMock.stubs().method("getType").will(returnValue(null));

        summarizer.summarize(results, (CompositePerfStats) statsMock.proxy());
    }        

    public void testLockAfterNone() {
        OperationSample results = makeOperationSample(TimingTestHelper.TICK_TIME);
        UsageTrackingInfo START_USAGE = new NoCpuUsageTrackingInfoImpl(100L);
        UsageTrackingInfo END_USAGE = new NoCpuUsageTrackingInfoImpl(START_USAGE.getEventTime()
                + TimingTestHelper.TICK_TIME);
        ThreadSnapshot snapshot = createSampleNoLock(START_USAGE);
        results.record(snapshot);

        snapshot = createSampleLock1(END_USAGE);
        results.record(snapshot);

        setUpStats(results);
        Mock contentionMock = mock(PerfStats.class);
        statsMock.expects(atLeastOnce()).method("getPerfStats").with(eq(StatisticsTypeImpl.Contention),
                eq(snapshot.getState())).will(returnValue(contentionMock.proxy()));
        contentionMock.expects(once()).method("recordUsage").with(sametime(END_USAGE), sametime(END_USAGE),
                eq(TimingTestHelper.TICK_TIME)).will(returnValue(0L));
        contentionMock.stubs().method("getType").will(returnValue(null));
        statsMock.expects(atLeastOnce()).method("getDirectCountForType").with(eq(StatisticsTypeImpl.Contention)).
            will(returnValue(1));

        summarizer.summarize(results, (CompositePerfStats) statsMock.proxy());
    }

    public void testSlowNoLock() {
        UsageTrackingInfo START_USAGE = new NoCpuUsageTrackingInfoImpl(100L);
        UsageTrackingInfo END_USAGE = new NoCpuUsageTrackingInfoImpl(START_USAGE.getEventTime() + 4
                * TimingTestHelper.TICK_TIME);
        ThreadSnapshot snapshot = createSampleNoLock(new NoCpuUsageTrackingInfoImpl(0L));
        OperationSample results = setUpMany(snapshot.getState(), 5, START_USAGE, TimingTestHelper.TICK_TIME);

        setUpStats(results);

        summarizer.summarize(results, (CompositePerfStats) statsMock.proxy());
    }

    private ThreadSnapshot createSampleNoLock(UsageTrackingInfo timeInfo) {
        StackTraceElement traceElt = new StackTraceElement(ContentionSummarizationTest.class.getName(),
                "testSummarization", null, 15);
        StackTraceElement[] trace = { traceElt };
        ThreadSnapshot snapshot = new ThreadSnapshotImpl(timeInfo);
        snapshot.setLockName(null);
        snapshot.setStackTrace(trace);
        return snapshot;
    }

    private ThreadSnapshot createSampleLock1(UsageTrackingInfo timeInfo) {
        String lockName = "com.whatever.Lock@34234";
        StackTraceElement traceElt = new StackTraceElement(ContentionSummarizationTest.class.getName(),
                "testSummarization", null, 14);
        StackTraceElement[] trace = { traceElt };
        ThreadSnapshot snapshot = new ThreadSnapshotImpl(timeInfo);
        snapshot.setLockName(lockName);
        snapshot.setStackTrace(trace);
        return snapshot;
    }

    private ThreadSnapshot createSampleLock2(UsageTrackingInfo timeInfo) {
        String lockName = "Dom.whatever.Lock@34234";
        StackTraceElement traceElt1 = new StackTraceElement(ContentionSummarizationTest.class.getName(),
                "VtestSummarization", null, 11);
        StackTraceElement traceElt2 = new StackTraceElement("java.lang.Object", "equals", null, 0);
        StackTraceElement[] trace = { traceElt1, traceElt2 };
        ThreadSnapshot snapshot = new ThreadSnapshotImpl(timeInfo);
        snapshot.setLockName(lockName);
        snapshot.setStackTrace(trace);
        return snapshot;
    }

    private ThreadSnapshot createSampleLockN(UsageTrackingInfo timeInfo, int n) {
        String lockName = "com.yetanother.Lock@2112";
        StackTraceElement traceElt = new StackTraceElement(ContentionSummarizationTest.class.getName(),
                "testSummarization", null, n);
        StackTraceElement[] trace = { traceElt };
        ThreadSnapshot snapshot = new ThreadSnapshotImpl(timeInfo);
        snapshot.setLockName(lockName);
        snapshot.setStackTrace(trace);
        return snapshot;
    }

    public void createSlowLineOne(UsageTrackingInfo timeInfo) {
        StackTraceElement traceElt1 = new StackTraceElement(ContentionSummarizationTest.class.getName(),
                "VtestSummarization", null, 15);
        StackTraceElement traceElt2 = new StackTraceElement("org.foo.Bar", "doIt", null, 0);
        StackTraceElement[] trace = { traceElt1, traceElt2 };
        ThreadSnapshot snapshot = new ThreadSnapshotImpl(timeInfo);

    }

    public void createSlowLineTwo(UsageTrackingInfo timeInfo) {
        StackTraceElement traceElt1 = new StackTraceElement(ContentionSummarizationTest.class.getName(),
                "VtestSummarization", null, 18);
        StackTraceElement traceElt2 = new StackTraceElement("org.foo.Bar", "doIt", null, 0);
        StackTraceElement[] trace = { traceElt1, traceElt2 };
        ThreadSnapshot snapshot = new ThreadSnapshotImpl(timeInfo);
    }

    public void createSlowNoLine(UsageTrackingInfo timeInfo) {
        StackTraceElement traceElt1 = new StackTraceElement(ContentionSummarizationTest.class.getName(),
                "VtestSummarization", null, 0);
        StackTraceElement traceElt2 = new StackTraceElement("org.foo.Bar", "doIt", null, 0);
        StackTraceElement[] trace = { traceElt1, traceElt2 };
        ThreadSnapshot snapshot = new ThreadSnapshotImpl(timeInfo);
    }

    public void createSlowMethodTwo(UsageTrackingInfo timeInfo) {
        StackTraceElement traceElt1 = new StackTraceElement("java.io.SomeReader", "readByte", null, 22);
        StackTraceElement traceElt2 = new StackTraceElement(ContentionSummarizationTest.class.getName(),
                "VtestSummarization", null, 18);
        StackTraceElement traceElt3 = new StackTraceElement("org.foo.Bar", "doIt", null, 0);
        StackTraceElement[] trace = { traceElt1, traceElt2, traceElt3 };
        ThreadSnapshot snapshot = new ThreadSnapshotImpl(timeInfo);

    }

    public Constraint sametime(final UsageTrackingInfo info) {
        return eq(info.getEventTime());
    }

    private OperationSample makeOperationSample(long tm) {
        return new OperationSample(tm, 0L, null);
    }
}
