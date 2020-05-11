/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import glassbox.track.api.PerfStats;
import glassbox.track.api.ThreadState;

import glassbox.track.api.PerfStatsImpl;
import glassbox.util.timing.api.NoCpuUsageTrackingInfoImpl;

public class MockPerfStatsImpl extends PerfStatsImpl implements PerfStats {
    protected int failureCount;
    
    public MockPerfStatsImpl(int count, int slowCount, int failureCount, long totalTime, long slowThreshold, long startTime, long endTime) {
        this.count = count;
        this.slowCount = slowCount;
        this.failureCount = failureCount;
        this.accumulatedTime = totalTime;
        this.setSlowThreshold(slowThreshold);
        this.firstEventTime = startTime;
        this.lastEventTime = endTime;
    }
    
    public int getFailureCount() {
        return failureCount;
    }
    
    static final private long serialVersionUID = 1;    
}
