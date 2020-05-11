/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import glassbox.track.api.*;

import java.sql.SQLException;

public class CompositePerfStatsImplTest extends PerfStatsImplTest {
    protected CompositePerfStats stats;

    public void setUp() {
        super.setUp();
        this.stats = (CompositePerfStats)super.stats;
        registryTest.setUp();
    }
    
    protected StatisticsRegistryTest registryTest = new StatisticsRegistryTest() {
        protected glassbox.track.api.StatisticsRegistry createRegistry() {
            return (CompositePerfStats)stats;
        }
    };

    public void testNotAggregating() {
        long start = 250L;
        long duration = 200L;
        long end = start + duration;
        stats.setSlowThreshold(duration);
        PerfStats dStats = stats.getPerfStats(StatisticsTypeImpl.Database, "jdbc://dreambase");
        recordUsage(dStats, start+1L, end-2L);
        recordUsage(stats, start, end);
        stats.summarizeOperation(null);
        
        assertEquals(1, stats.getCount());
        assertEquals(duration, stats.getAccumulatedTime());
        assertEquals(end, stats.getLastEventTime());
        assertEquals(1, stats.getSlowCount());
        assertEquals(1, stats.getSlowSingleOperationCount());
        assertEquals(start, stats.getFirstEventTime());
    }
    
    public void testChildFailure() {
        long start = 250L;
        long duration = 200L;
        long end = start + duration;
        
        PerfStats cStats = stats.getPerfStats(StatisticsTypeImpl.DatabaseConnection, null);
        recordFailure(cStats, start, end, new SQLException("test error"));
        recordUsage(stats, 0L, start+2*duration);

        assertEquals(1, cStats.getFailureCount());
        assertEquals(1, stats.getFailureCount());
    }
    
    public void testMultipleChildFailures() throws Exception {
        long start = 250L;
        long duration = 200L;
        long end = start + duration;
        
        PerfStats cStats = stats.getPerfStats(StatisticsTypeImpl.DatabaseConnection, null);
        recordFailure("failure1", cStats, start, end, new SQLException("test error"), false);
        
        recordFailure("failure2", cStats, end, end+duration, new SQLException("test error"), true);
        recordUsage(stats, 0L, start+3*duration);
        stats.summarizeOperation(null);

        assertEquals(2, cStats.getFailureCount());
        assertEquals(1, cStats.getFailingOperationCount());
        assertEquals(2, stats.getFailureCount());
        assertEquals(1, stats.getFailingOperationCount());
    }
    
    public void testClear() {
        registryTest.testClear();
    }

    public void testEmpty() {
        registryTest.testEmpty();
    }

    public void testInsertMixed() {
        registryTest.testInsertMixed();
    }

    public void testInsertOne() {
        registryTest.testInsertOne();
    }

    public void testThreshold() {
        registryTest.testThreshold();
    }
    //TODO: test changing slow thresholds and effect on child counts and other invariants
    //as in the scenario perf stats test

    protected PerfStats makePerfStats() {
        return new CompositePerfStatsImpl();
    }
    
}
