/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import glassbox.response.Response;

import java.util.List;

public interface OperationPerfStats extends CompositePerfStats {

    //TODO: make a typesafe enum in ugly old Java fashion
    public static final int NORMAL_SCENARIO = 0;
    public static final int SLOW_SCENARIO = 1;
    public static final int FAILURE_SCENARIO = 2;
    public static final int NUMBER_OF_SCENARIOS = 3;
    
    /**
     * Record end of top-level statistics for a unit of work. This method lets the first identified operation record time and errors
     * in dispatch.
     * 
     * @param response
     * @param entryStats
     */
    public void summarizeOperation(Response response, PerfStats entryStats);
    
    /**
     * Get the statistics for a certain performance scenario, e.g., failure, slow, both,
     * some resource was long.
     * 
     * @param scenarioKey - initially just one of the constants above
     * @return perf stats for the scenario
     */
    public PerfStats getScenarioStats(final int scenarioKey);

    /**
     * Get the total statistics for a certain type of resource, e.g., database, remote calls, etc.
     * @param statsTypeIndex is the index as defined in @see StatisticsType
     */
    public PerfStats getResourceStats(int statsTypeIndex);
    
    /**
     * 
     * @return the time and other statistics not accessing real resources like database or remote calls.
     * Includes contention and slow method times.
     */
    public PerfStats getNonResourceStats();
    
    /**
     * @return How many remaining times will we not recorded this statistic. Typically used to discard initial data points while "warming up."
     */
    public int getSkipCount();
    
    /**
     * @return Record another time that we did not record this statistic. Typically used to discard initial data points while "warming up."
     */
    public int decrementSkipCount();
    
    public void resetSkipCount();

    public List getProblemRequests();
    
    public int getOperationCount();
    
    public long getOperationTime();
    
    public void recordAsOperation();
    
    public boolean isSlowDispatch();

    public boolean isFailingDispatch();

    public long getDispatchTime();

    public void recordDispatch(Response response);
    
    /** stats for time when an operation */
    public PerfStats getOperationStats();
    
    public void setOperationKey(boolean isOperationKey);
}
