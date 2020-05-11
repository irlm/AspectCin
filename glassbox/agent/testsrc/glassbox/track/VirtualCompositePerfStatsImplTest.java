/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import glassbox.track.api.*;
import glassbox.util.timing.api.NoCpuUsageTrackingInfoImpl;
import junit.framework.AssertionFailedError;

public class VirtualCompositePerfStatsImplTest extends CompositePerfStatsImplTest {
    
    private class TimeStruct {
        private long start;
        private long end;
        private long duration;
        public TimeStruct(long start, long end) {
            this.start=start;
            this.end=end;
            this.duration = end - start;
        }
        public long getDuration() {
            return duration;
        }
        public long getEnd() {
            return end;
        }
        public long getStart() {
            return start;
        }
    }
    private final TimeStruct t1 = new TimeStruct(250L, 350L);
    private final TimeStruct t2 = new TimeStruct(500L, 800L);
    private final TimeStruct t3 = new TimeStruct(900L, 940L);
    private final long thres = 101L;
    
    public void setUp() {
        super.setUp();
        stats.setSlowThreshold(thres);
    }
       
    protected PerfStats makePerfStats() {
        return new CompositePerfStatsImpl(true);
    }
    
    public void testAggregatedAfter() {
        testAggregated(t1, t2);
    }
    
    public void testAggregatedBefore() {
        testAggregated(t2, t1);
    }
    
    public void testChildOnly() {
        PerfStats dStats = stats.getPerfStats(StatisticsTypeImpl.DatabaseStatement, "select count(*) from nowhere");
        
        dStats.recordUsage(t2.getStart(), t2.getEnd(), 0L);
        summarize(stats);

        assertEquals(1, stats.getCount());
        assertEquals(t2.getDuration(), stats.getAccumulatedTime());
        assertEquals(t2.getEnd(), stats.getLastEventTime());
        assertEquals(1, stats.getSlowCount());
        assertEquals(t2.getStart(), stats.getFirstEventTime());
    }

    public void testTwoChildren() {
        CompositePerfStats cStats = (CompositePerfStats)stats.getPerfStats(StatisticsTypeImpl.DatabaseConnection, null);

        UsageTrackingInfo cStartUsageInfo = new NoCpuUsageTrackingInfoImpl(t1.getStart());
        UsageTrackingInfo cEndUsageInfo = new NoCpuUsageTrackingInfoImpl(t1.getEnd());
        
        cStats.recordUsage(t1.getStart(), t1.getEnd(), 0L);
        
        stats.recordUsage(t2.getStart(), t2.getEnd(), 0L);
        
        PerfStats dStats = stats.getPerfStats(StatisticsTypeImpl.DatabaseStatement, "select count(*) from nowhere");
        dStats.recordUsage(t3.getStart(), t3.getEnd(), 0L);
        summarize(stats);
        
        assertEquals(3, stats.getCount());
        assertEquals(t1.getDuration()+t2.getDuration()+t3.getDuration(), stats.getAccumulatedTime());
        assertEquals(t3.getEnd(), stats.getLastEventTime());
        assertEquals(1, stats.getSlowCount());
        assertEquals(t1.getStart(), stats.getFirstEventTime());
    }
    
    private void testAggregated(TimeStruct a, TimeStruct b) {
        stats.recordUsage(a.getStart(), a.getEnd(), 0L);
        PerfStats dStats = stats.getPerfStats(StatisticsTypeImpl.DatabaseStatement, "select count(*) from nowhere");
        dStats.recordUsage(b.getStart(), b.getEnd(), 0L);
        summarize(stats);

        assertEquals(2, stats.getCount());
        assertEquals(a.getDuration()+b.getDuration(), stats.getAccumulatedTime());
        assertEquals(Math.max(a.getEnd(), b.getEnd()), stats.getLastEventTime());
        assertEquals(1, stats.getSlowCount());
        assertEquals(Math.min(a.getStart(), b.getStart()), stats.getFirstEventTime());
    }
    
    public void testNotAggregating() {
        try {
            super.testNotAggregating();
            fail("should be aggregating");
        } catch (AssertionFailedError afe) {
            // ok: the assertion should fail...
        }
    }
}
