/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import glassbox.monitor.OperationFactory;
import glassbox.track.api.*;

import java.io.Serializable;
import java.util.Map;

public class MockCompositePerfStatsImpl extends OperationPerfStatsImpl {
    
    public MockCompositePerfStatsImpl(int count, int slowCount, int failureCount, long totalTime, long slowThreshold, long startTime, long endTime, StatisticsType type) {
        this.count = count;
        this.slowCount = slowCount;
        this.failureCount = failureCount;
        this.accumulatedTime = totalTime;
        this.setSlowThreshold(slowThreshold);
        this.firstEventTime = startTime;
        this.lastEventTime = endTime;
        this.type = type;
        registry = new MockStatisticsRegistryImpl();
        whenOperationStats = new MockPerfStatsImpl(count, slowCount, failureCount, totalTime, slowThreshold, startTime, endTime);
        key = new OperationDescriptionImpl("mock", "dummyOperationKey", "dummyOperationKey", "undefined", false);
    }
    
    public MockCompositePerfStatsImpl(int count, int slowCount, int failureCount, long totalTime, long slowThreshold, long startTime, long endTime) {
        this(count, slowCount, failureCount, totalTime, slowThreshold, startTime, endTime, StatisticsTypeImpl.UiRequest);
    }
    
    public void setPerfStats(StatisticsType type, Serializable key, PerfStats stats) {
        if (stats == this) {
            throw new IllegalArgumentException("can't add statistics to self");
        }
        stats.setType(type);
        stats.setKey(key);
        ((PerfStatsImpl)stats).setOwner((MockStatisticsRegistryImpl)registry);
        ((MockStatisticsRegistryImpl)registry).setPerfStats(type, key, stats);
    }
    
    public void setResourceStats(StatisticsType type, PerfStats stats) {
        if (stats == this) {
            throw new IllegalArgumentException("can't add statistics to self");
        }
        resourceTotalStats[type.getIndex()] = (PerfStatsImpl)stats;
    }

    public void setScenarioStats(int scenario, PerfStats stats) {
        if (stats == this) {
            throw new IllegalArgumentException("can't add statistics to self");
        }
        scenarioStats[scenario] = (PerfStatsImpl)stats;
    }
    
    private class MockStatisticsRegistryImpl extends StatisticsRegistryImpl {
        public MockStatisticsRegistryImpl() {
            super(MockCompositePerfStatsImpl.this);
        }
        
        public void setPerfStats(StatisticsType type, Object key, PerfStats stats) {
            Map typeReg = data[type.getIndex()];
            if (key==null) {
                key = NULL_KEY;
            }
            typeReg.put(key, stats);
        }
        
        static final private long serialVersionUID = 1;        
    };    
    
    static final private long serialVersionUID = 1;

}