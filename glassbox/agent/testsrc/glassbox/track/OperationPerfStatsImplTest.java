/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import java.sql.SQLException;

import glassbox.response.Response;
import glassbox.track.api.*;
import glassbox.util.timing.Clock;

public class OperationPerfStatsImplTest extends CompositePerfStatsImplTest {
    private OperationPerfStats operationStats; 

    protected PerfStats makePerfStats() {
        operationStats = new OperationPerfStatsImpl();
        return operationStats;
    }

    protected void setSlowThreshold(long thresh) {
        super.setSlowThreshold(thresh);        
        for (int i=0; i<OperationPerfStats.NUMBER_OF_SCENARIOS; i++) {
            PerfStats nested = operationStats.getScenarioStats(i);
            nested.setSlowThreshold(thresh);
        }
    }
    
    public void testQuickOp() {
        super.testQuickOp();
        assertEqualValues(operationStats, operationStats.getScenarioStats(OperationPerfStats.NORMAL_SCENARIO));
    }
    
    public void testSlowOp() {
        super.testSlowOp();
        assertEqualValues(operationStats, operationStats.getScenarioStats(OperationPerfStats.SLOW_SCENARIO));
    }
    
    public void testFailure() throws Exception {
        super.testFailure();
        assertEqualValues(operationStats, operationStats.getScenarioStats(OperationPerfStats.FAILURE_SCENARIO));
    }

    public void testChildKeyDispatchFailure() {
        long start = 250L;
        long duration = 200L;
        long end = start + duration;
        
        operationStats.recordStart(start);
        OperationPerfStats cStats = (OperationPerfStats)operationStats.getPerfStats(StatisticsTypeImpl.UiRequest, new OperationDescriptionImpl("a", "b", "c", null, false));
        cStats.recordAsOperation();        
        cStats.recordUsage(start, end, 0L);
        Response response = recordFailure(operationStats, 0L, start+2*duration, new Exception("bad operation"));
        cStats.recordDispatch(response);
        cStats.summarizeOperation(response, operationStats);

        assertTrue(cStats.isFailingDispatch());
        assertEquals(1, cStats.getOperationStats().getWorstFailures().size());
        assertEquals(1, cStats.getOperationCount());
        assertEquals(1, cStats.getOperationStats().getFailingOperationCount());
        assertEquals(0, cStats.getFailureCount());
        assertEquals(1, cStats.getOperationStats().getFailureCount());
        assertEquals(duration, cStats.getAccumulatedTime());
        assertEquals(start+2*duration, cStats.getOperationStats().getAccumulatedTime());
        assertEquals(1, operationStats.getFailureCount());
        assertEquals(1, operationStats.getCount());
        assertEquals(start+2*duration, operationStats.getAccumulatedTime());
        assertEquals(0, operationStats.getOperationCount());
        assertEquals(0L, operationStats.getOperationTime());
        assertEquals(0, operationStats.getOperationStats().getFailingOperationCount());
        PerfStats topOpStats = operationStats.getOperationStats();
        assertEquals(0, topOpStats.getFailureCount());
        assertEquals(0, topOpStats.getCount());
        assertTrue(cStats.isOperationKey());
        assertTrue(operationStats.isOperationKey());
        
        response = recordFailure(operationStats, start, end, new Exception("bad operation"));
        operationStats.summarizeOperation(response, operationStats);
        
        assertEquals(1, cStats.getOperationStats().getFailureCount());
        assertTrue(cStats.isFailingDispatch());
        assertEquals(1, cStats.getOperationStats().getWorstFailures().size());
        assertEquals(1, cStats.getOperationStats().getFailingOperationCount());
        assertEquals(1, cStats.getOperationCount());
        assertEquals(0, cStats.getFailingOperationCount());
        assertEquals(2, operationStats.getFailureCount());
        assertEquals(1, operationStats.getOperationStats().getFailureCount());
        assertEquals(2, operationStats.getCount());
        assertEquals(duration, operationStats.getOperationTime());
        assertEquals(start+3*duration, operationStats.getAccumulatedTime());
        assertEquals(1, operationStats.getOperationCount());
        assertEquals(1, operationStats.getOperationStats().getFailingOperationCount());
        assertEquals(2, operationStats.getFailingOperationCount());
        assertFalse(operationStats.isFailingDispatch());
    }
    

    public void testIntermittentFailure() throws Exception {
        super.testFailure();
        runQuickOp(100L, 1000L);
        
        assertEquals(1, operationStats.getFailingOperationCount());
        assertEquals(1, operationStats.getFailureCount());
        assertEquals(1, operationStats.getScenarioStats(OperationPerfStats.FAILURE_SCENARIO).getCount());
        assertEquals(2, operationStats.getScenarioStats(OperationPerfStats.NORMAL_SCENARIO).getCount());
    }
    
    public void testSlowFailure() throws Exception {
        super.testSlowFailure();
        assertEqualValues(operationStats, operationStats.getScenarioStats(OperationPerfStats.FAILURE_SCENARIO));
    }
    
    //TODO: test nested rollups
    //TODO: test resource rollups
    public void testSummarizeDispatch() throws Exception {
        
    }

    protected void assertEqualValues(PerfStats a, PerfStats b) {
        assertEquals(a.getCount(), b.getCount());        
        assertEquals(a.getAccumulatedTime(), b.getAccumulatedTime());        
        // we do NOT assert equal failure counts, since the scenarios don't pick up failure counts: they are virtual composite data...
    }
    
    private static aspect CheckInvariants {
        after(OperationPerfStatsImplTest test) returning: execution(public * test*(..)) && this(test) {
            test.assertInvariants();
        }
    }
    
    private void assertInvariants() {
        assertPigeonHole();
        assertNoFailuresInNonFailing();
        assertNoSlowInNonSlow();
    }
    
    private void assertPigeonHole() {
        int count = 0;
        long elapsedTime = 0L;
        for (int i=0; i<OperationPerfStats.NUMBER_OF_SCENARIOS; i++) {
            PerfStats nested = operationStats.getScenarioStats(i);
            count += nested.getCount();
            elapsedTime += nested.getAccumulatedTime();
            assertTrue(nested.getLastEventTime()==Clock.UNDEFINED_TIME || nested.getLastEventTime() <= operationStats.getLastEventTime());
            assertTrue(nested.getFirstEventTime()==Clock.UNDEFINED_TIME || nested.getFirstEventTime() >= operationStats.getFirstEventTime());
        }
        assertEquals(operationStats.getOperationCount(), count);
        assertEquals(operationStats.getOperationTime(), elapsedTime);
    }
    
    private void assertNoFailuresInNonFailing() {
        assertEquals(0, operationStats.getScenarioStats(OperationPerfStats.NORMAL_SCENARIO).getFailureCount());
        assertEquals(0, operationStats.getScenarioStats(OperationPerfStats.SLOW_SCENARIO).getFailureCount());        
    }
    
    private void assertNoSlowInNonSlow() {
        assertEquals(0, operationStats.getScenarioStats(OperationPerfStats.NORMAL_SCENARIO).getSlowCount());
    }
    
    protected void summarize(PerfStats stats) {
        if (stats instanceof OperationPerfStats) {
            ((OperationPerfStats)stats).summarizeOperation(null, stats);
        } else {
            super.summarize(stats);
        }
    }        
}
