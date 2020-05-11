/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis;

import glassbox.analysis.api.*;
import glassbox.analysis.resource.jdbc.DefaultDatabaseEventFactory;
import glassbox.monitor.OperationFactory;
import glassbox.response.Response;
import glassbox.test.ajmock.*;
import glassbox.track.MockCompositePerfStatsImpl;
import glassbox.track.api.*;
import glassbox.util.jmx.JmxManagement;
import glassbox.util.timing.api.TimeConversion;

import java.sql.SQLException;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.builder.ArgumentsMatchBuilder;
import org.jmock.builder.MatchBuilder;

public class OperationAnalysisTest extends VirtualMockObjectTestCase {
    public final OperationAnalyzer analyzer = new OperationAnalyzer();
    public final long FAST_TIME = analyzer.getSlowThresholdNanos()/3;
    public final long SLOW_TIME = analyzer.getSlowThresholdNanos()+1;
    
    private OperationDescriptionImpl operation1;
    private OperationDescription operation2;
    private MockCompositePerfStatsImpl dbStats;
    private MockCompositePerfStatsImpl connStats;
    
    public void setUp() {
        analyzer.setProblemFactory(new ProblemFactory());
        JmxManagement.aspectOf().setEnabled(false);
        operation1 = makeOperation("javax.servlet.http.HttpServlet", "operation1");
        operation2 = makeOperation("org.apache.struts.action.Action", "operation2");
    }
    
    protected MockCompositePerfStatsImpl setUpPerfStats(int count, int slowCount, long totalTime) {
        return setUpPerfStats(count, slowCount, 0, null, totalTime, 0L, 0L, totalTime*2);
    }
    
    protected MockCompositePerfStatsImpl setUpPerfStats(int count, int slowCount, int failureCount, Throwable failure, long totalTime) {
        return setUpPerfStats(count, slowCount, failureCount, failure, totalTime, 0L, 0L, totalTime*2);
    }
    
    protected MockCompositePerfStatsImpl setUpPerfStats(int count, int slowCount, int failureCount, Throwable failure, long totalTime, long slowThreshold, long startTime, long endTime) {

        MockCompositePerfStatsImpl stats = new MockCompositePerfStatsImpl(count, slowCount, failureCount, totalTime, slowThreshold, startTime, endTime);
        
        if (failure != null) {
            recordFailure(stats, failure, failureCount);            
        }
        if (slowCount>failureCount) {
            stats.setScenarioStats(OperationPerfStats.SLOW_SCENARIO, setUpPerfStats(slowCount-failureCount, 0, 0, null, totalTime*(slowCount-failureCount)/slowCount));
        }
        if (failureCount>0) {
            stats.setScenarioStats(OperationPerfStats.FAILURE_SCENARIO, setUpPerfStats(failureCount, 0, 0, failure, slowCount>0 ? totalTime*failureCount/slowCount : totalTime*failureCount/count));
        }
        return stats;
    }
    
    protected void recordFailure(PerfStats stats, Throwable throwable, int count) {
        FailureDescription failureDescription = new DefaultDatabaseEventFactory().getFailureDescription(throwable);
        
        Mock mockResponse = mock(Response.class);
        mockResponse.stubs().method("getStart").will(returnValue(0L));
        mockResponse.stubs().method("getEnd").will(returnValue(0L));
        mockResponse.stubs().method("getDuration").will(returnValue(0L));
        mockResponse.stubs().method("get").with(eq(Response.FAILURE_DATA)).will(returnValue(failureDescription));
        mockResponse.stubs().method("get").with(eq(Response.PARAMETERS)).will(returnValue(null));
        mockResponse.stubs().method("get").with(eq(Response.REQUEST)).will(returnValue(null));
        mockResponse.stubs().method("get").with(eq(PerfStatsImpl.RECORDED)).will(returnValue(null));
        mockResponse.stubs().method("set").with(eq(PerfStatsImpl.RECORDED), ANYTHING).isVoid();
        mockResponse.stubs().method("set").with(eq(Response.REQUEST), ANYTHING).isVoid();
        mockResponse.stubs().method("getParent").will(returnValue(null));
        
        Response response = (Response)mockResponse.proxy();
        
        for (int i=0; i<count; i++) {
            stats.recordEnd(response);
        }
    }

    protected MockCompositePerfStatsImpl createPerfStats(int count, int slowCount, long totalTime, int dbCount, long dbTime) {
        return createPerfStats(count, slowCount, 0, null, totalTime, dbCount, dbTime);
    }
    
    protected MockCompositePerfStatsImpl createPerfStats(int count, int slowCount, int failureCount, Throwable failure, long totalTime, int dbCount, long dbTime) {
        
        MockCompositePerfStatsImpl stats = setUpPerfStats(count, slowCount, failureCount, null, totalTime);
        dbStats = setUpPerfStats(dbCount, 0, 0, null, dbTime);
        connStats = setUpPerfStats(dbCount, (dbTime>0L ? Math.min(dbCount, slowCount) : 0), Math.min(dbCount, failureCount), failure, dbTime/2);
        stats.setResourceStats(StatisticsTypeImpl.Database, connStats);
        stats.setPerfStats(StatisticsTypeImpl.Database, "jdbc://foo", dbStats);
        dbStats.setPerfStats(StatisticsTypeImpl.DatabaseConnection, null, connStats);
        
        return stats;
    }
    
    public void testSummarizeNull() {
        OperationPerfStats stats = createPerfStats(0, 0, 0L, 0, 0L);
        
        OperationSummary summary = analyzer.summarize(operation1, stats);
        assertEquals(operation1, summary.getOperation());
        assertEquals(1, summary.analysisFindings().size());
        assertTrue(summary.analysisFindings().contains(OperationSummary.OK));
        assertEquals(OperationSummary.StatusOK, summary.statusCode());
        assertSameTopLevelStats(stats, summary);
    }
    
    public void testSummarizeFast() {       
        OperationPerfStats stats = createPerfStats(3, 0, FAST_TIME*2, 5, FAST_TIME);
        
        OperationSummary summary = analyzer.summarize(operation1, stats);
        assertEquals(operation1, summary.getOperation());
        assertEquals(1, summary.analysisFindings().size());
        assertTrue(summary.analysisFindings().contains(OperationSummary.OK));
        assertEquals(OperationSummary.StatusOK, summary.statusCode());
        assertSameTopLevelStats(stats, summary);
    }
    
    public void testAnalyzeFast() {
        OperationPerfStats stats = createPerfStats(3, 0, FAST_TIME*2, 5, FAST_TIME);
        
        OperationAnalysis analysis = analyzer.analyze(operation1, stats, 0L);
        assertEquals(0, analysis.problems().size());
        assertSameTopLevelStats(stats, analysis.getScenarioSummary());
    }   

    public void testSummarizeOccasionallySlow() {       
        OperationPerfStats stats = createPerfStats(3, 1, FAST_TIME*2, 5, FAST_TIME);
        
        OperationSummary summary = analyzer.summarize(operation1, stats);
        assertEquals(OperationSummary.StatusSlow, summary.statusCode());
        assertEquals(1, summary.analysisFindings().size());
        assertSameTopLevelStats(stats, summary);
    }

    public void testAnalyzeOccasionallySlow() {
        OperationPerfStats stats = createPerfStats(3, 1, FAST_TIME*2, 5, FAST_TIME);
        
        OperationAnalysis analysis = analyzer.analyze(operation1, stats, 0L);
        assertEquals(1, analysis.problems().size());
        assertSameTopLevelStats(stats, analysis.getScenarioSummary());
    }

    public void testSummarizeSlowDb() {       
        OperationPerfStats stats = createPerfStats(1, 1, SLOW_TIME*2, 1, SLOW_TIME);
        
        OperationSummary summary = analyzer.summarize(operation1, stats);
        assertEquals(OperationSummary.StatusSlow, summary.statusCode());
        assertEquals(1, summary.analysisFindings().size());
        String finding = (String)summary.analysisFindings().get(0);
        assertEquals(OperationSummary.SLOW_DATABASE, finding);
        assertSameTopLevelStats(stats, summary);
    }
    
    // slow but not because of queries
    public void testAnalyzeSlowDb() {       
        OperationPerfStats stats = createPerfStats(1, 1, SLOW_TIME*2, 1, SLOW_TIME);
        
        OperationAnalysis analysis = analyzer.analyze(operation1, stats, 0L);
        assertEquals(1, analysis.problems().size());
        ProblemAnalysis problem = (ProblemAnalysis)analysis.problems().get(0);
        SlowAggregateResourceProblem excessDatabaseProblem = (SlowAggregateResourceProblem)problem;
     //   assertEquals(operation1, excessDatabaseProblem.operationDescription()); // FIXME
        
        assertSameTopLevelStats(stats, analysis.getScenarioSummary());
    }

    public void testAnalyzeSlowDbOneQuery() {
    	
        MockCompositePerfStatsImpl stats = createPerfStats(5, 2, 0, null, SLOW_TIME*8, 1, SLOW_TIME*15/2);
        MockCompositePerfStatsImpl mockQueryStats = setUpPerfStats(5, 2, SLOW_TIME*6);
        String query = "select * from big_table where id<>0";
        dbStats.setPerfStats(StatisticsTypeImpl.DatabaseStatement, query, mockQueryStats);        
        
        OperationAnalysis analysis = analyzer.analyze(operation1, stats, 0L);
        assertEquals(1, analysis.problems().size());
        ProblemAnalysis problem = (ProblemAnalysis)analysis.problems().get(0);
        SlowAggregateResourceProblem slowAggregateResourceProblem = (SlowAggregateResourceProblem)problem;
        
        List problems = slowAggregateResourceProblem.getDistinctCalls();
        assertEquals("Wrong number of calls "+problems, 2, problems.size());
        
        CallDescription d1 = ((SlowCallProblem)problems.get(0)).getCall();
        CallDescription d2 = ((SlowCallProblem)problems.get(1)).getCall();
        assertTrue("No matching query: "+d1+", "+d2, d1.getCallKey().equals(query) || d2.getCallKey().equals(query));
        
        assertEquals(stats.getCount(), analysis.getScenarioSummary().getCount()); 
        assertEquals(stats.getAccumulatedTime(), analysis.getScenarioSummary().getAccumulatedTime()); 
        //XXX not yet done: call track for query
        //assertTrue(problem instanceof SlowDatabaseCallProblem);
    }
    
    public void testAnalyzeSlowDbManyFastQueries() {
        //XXX
    }

    // to write thorough tests, we should drive the simulator to force various conditions
    // or else drive the request API 
    // either way, we need a harness to drive various conditions more systematically
    public void testAnalyzeFailingSlowly() {
//        Mock mockStats = mock(OperationPerfStats.class);
//        mockStats.stubs().method("getFailingOperationCount").will(returnValue(1));
//        OperationPerfStats stats = mockStats.proxy();
        
//        createPerfStats(5, 3, 3, TEST_THROWABLE, SLOW_TIME*8, 1, SLOW_TIME*15/2);
//        
//        OperationAnalysis analysis = analyzer.analyze(operation1, stats, 0L);
//        assertEquals(1, analysis.problems().size());
//        ProblemAnalysis problem = (ProblemAnalysis)analysis.problems().get(0);
//        DefaultFailureProblem failureProblem = (DefaultFailureProblem)problem;
//        assertEquals(3, failureProblem.getNumFailures());
//        
//        assertSameTopLevelStats(stats, analysis.getScenarioSummary());

//        DefaultResponseFactory factory = new DefaultResponseFactory();
//        factory.addListener(new StatsSummarizer());
        
    }
    
    public void testSummarizeFailingAndSlow() {
    }
    public void testAnalyzeFailingAndSlow() {
    }
        
    private static final StackTraceElement[] TEST_STACK_TRACE = { new StackTraceElement(Object.class.getName(), "wait", null, 0) };
    private static final Throwable TEST_THROWABLE = makeTestThreadState();
    static Throwable makeTestThreadState() {
        return new Throwable("test") {
            public StackTraceElement[] getStackTrace() {
                return TEST_STACK_TRACE;
            }
        };
//        ThreadState snapshot = new ThreadState();
//        snapshot.setStackTrace(TEST_STACK_TRACE);
//        return snapshot;
    }
    
    public void testSummarizeSlowFailingDb() {       
        OperationPerfStats stats = createPerfStats(2, 1, 1, TEST_THROWABLE, SLOW_TIME*2, 1, SLOW_TIME);
        
        OperationSummary summary = analyzer.summarize(operation1, stats);
        assertEquals(OperationSummary.StatusFailing, summary.statusCode());
        assertEquals(1, summary.analysisFindings().size());
        assertTrue(summary.analysisFindings().contains(OperationSummary.FAIL_DATABASE_CONNECTION));
        assertSameTopLevelStats(stats, summary);
    }
    
    public void testAnalyzeSlowWhileFailingDb() {      
        analyzer.setProblemFactory(new ProblemFactory());
        SQLException exception = new SQLException("Reason", "bad state", 1234);
        
        OperationPerfStats stats = createPerfStats(1, 1, 1, exception, SLOW_TIME*2, 1, SLOW_TIME);
        
        OperationAnalysis analysis = analyzer.analyze(operation1, stats, 0L);
        assertEquals(1, analysis.problems().size());
        DefaultFailureProblem defaultFailureProblem = (DefaultFailureProblem)analysis.problems().get(0);
        assertEquals("connection to database jdbc://foo", defaultFailureProblem.getCall().getSummary());
        ThreadState expectedState = new ThreadState();
        expectedState.setStackTrace(exception.getStackTrace());
        FailureAnalysis event = (FailureAnalysis)defaultFailureProblem.getEvents().get(0);        
        assertEquals(expectedState, event.getFailure().getThreadState());
        assertSameTopLevelStats(stats, analysis.getScenarioSummary());
    }
    
    public void testAnalyzeSlowAndFailingDb() {      
        analyzer.setProblemFactory(new ProblemFactory());
        SQLException exception = new SQLException("Reason", "bad state", 1234);
        
        OperationPerfStats stats = createPerfStats(2, 2, 1, exception, SLOW_TIME*3, 2, SLOW_TIME*2);
        
        OperationAnalysis analysis = analyzer.analyze(operation1, stats, 0L);
        assertEquals(2, analysis.problems().size());
        int slowCrsr = 0;
        SlowAggregateResourceProblem excessDatabaseProblem;
        DefaultFailureProblem defaultFailureProblem;
        if (analysis.problems().get(0) instanceof SlowAggregateResourceProblem) {
            excessDatabaseProblem = (SlowAggregateResourceProblem)analysis.problems().get(0);
            defaultFailureProblem = (DefaultFailureProblem)analysis.problems().get(1);
        } else {
            excessDatabaseProblem = (SlowAggregateResourceProblem)analysis.problems().get(1);
            defaultFailureProblem = (DefaultFailureProblem)analysis.problems().get(0);
        }
        ThreadState expectedState = new ThreadState();
        expectedState.setStackTrace(exception.getStackTrace());
        FailureAnalysis event = (FailureAnalysis)defaultFailureProblem.getEvents().get(0);        
        assertEquals(expectedState, event.getFailure().getThreadState());
        assertSameTopLevelStats(stats, analysis.getScenarioSummary());
    }

    private final String TEST_LOCK_NAME = "dummyLock@1234";
    private OperationPerfStats setUpContention() {
        MockCompositePerfStatsImpl stats = createPerfStats(1, 1, 0, null, SLOW_TIME*2, 0, 0);
        MockCompositePerfStatsImpl mockContentionStats = setUpPerfStats(1, 1, SLOW_TIME);
        ThreadState state = new ThreadState();
        state.setLockName(TEST_LOCK_NAME);
        state.setStackTrace(TEST_STACK_TRACE);
        stats.setPerfStats(StatisticsTypeImpl.Contention, state, mockContentionStats);
        stats.setResourceStats(StatisticsTypeImpl.Contention, mockContentionStats);
        return stats;
    }

    public void testSummarizeSlowContending() {       
        OperationPerfStats stats = setUpContention();
        
        OperationSummary summary = analyzer.summarize(operation1, stats);
        assertEquals(OperationSummary.StatusSlow, summary.statusCode());
        assertEquals(1, summary.analysisFindings().size());
        assertTrue("Unexpected finding "+summary.analysisFindings().get(0), summary.analysisFindings().contains(OperationSummary.SINGLE_THREAD_QUEUE)); 
//        assertSameTopLevelStats(stats, summary);
    }
    
    private static aspect MockAnalysis extends VirtualMockAspect {
        public pointcut mockPoint() : 
            cflow(execution(* testSlowComesLast()) && within(OperationAnalysisTest)) &&
            execution(* OperationAnalyzer.analyze*(*, *, java.util.Collection+));
    }

    public void testSlowComesLast() {
        MockCompositePerfStatsImpl stats = createPerfStats(1, 1, 0, null, SLOW_TIME*2, 0, 0);
        
        Mock mockAnalysis = virtualMock( MockAnalysis.aspectOf() );
        MatchBuilder slowMethod = mockAnalysis.expects(once()).method("analyzeSlowMethod");
        String[] analysisMethods = { "analyzeDatabase", "analyzeRemoteCalls", "analyzeFailures", "analyzeContention", "analyzeCpuUsage", "analyzeSlowDispatch" };
        for (int i=0; i<analysisMethods.length; i++) {
            mockAnalysis.expects(once()).method(analysisMethods[i]).isVoid();
            slowMethod = slowMethod.after(analysisMethods[i]);
        }
        slowMethod.isVoid();
        
        analyzer.analyze(operation1, stats, 0L);
    }
    
    // something is wrong with the dummy stats here...
//    public void testAnalyzeSlowContending() {      
//        OperationPerfStats stats = setUpContention();
//               
//        OperationAnalysis analysis = analyzer.analyze(operation1, stats, 0L);
//        assertEquals(1, analysis.problems().size()); 
//        ContentionProblem contentionProblem = (ContentionProblem)analysis.problems().get(0);
//        assertEquals("dummyLock", contentionProblem.threadState().getLockName());
//        TestHelper.assertArrayEquals(TEST_STACK_TRACE, contentionProblem.threadState().getStackTrace());
//        assertEquals(stats, analysis.detailedStatistics());
//    }

    private void assertSameTopLevelStats(PerfStats stats, OperationSummary summary) {
        assertEquals(TimeConversion.meanNanosInSeconds(stats.getAccumulatedTime(), stats.getCount()), 
                summary.getAvgExecutionTime(), 1e-9);
        assertEquals(stats.getCount(), summary.getCount());
//        assertEquals(stats.slowCount(), summaryStats.getSlowCount());
        assertEquals(stats.getFailureCount()>0, summary.isFailing());
//        assertEquals(stats.getFirstEventTime(), summaryStats.getFirstEventTime());
//        assertEquals(stats.getLastEventTime(), summaryStats.getLastEventTime());
    }
    
    private void assertSameTopLevelStats(OperationPerfStats stats, ScenarioSummary summary) {
        assertSameSummaryStats(stats, summary);
        for (int i=0; i<OperationPerfStats.NUMBER_OF_SCENARIOS; i++) {
            assertSameSummaryStats(stats.getScenarioStats(i), summary.getScenario(i));
        }
    }
    
    private void assertSameSummaryStats(PerfStats stats, SummaryStats summary) {
        assertEquals(stats.getAccumulatedTime(), summary.getAccumulatedTime());
        assertEquals(stats.getCount(), summary.getCount());
    }        
    
    private OperationDescriptionImpl makeOperation(String type, String operation) {
        return new OperationDescriptionImpl(type, operation, operation, null, false);
    }   
    
}
