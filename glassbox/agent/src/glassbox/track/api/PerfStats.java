/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import glassbox.response.Response;
import glassbox.util.logging.api.LogManagement;
import glassbox.util.org.sl4j.Logger;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;


/**
 * Abstraction for performance statistics about anything. The slowThreshold defaults to zero, so it must be set to a non-zero value, or else all timings will be considered slow.  
 * 
 * @author Ron Bodkin
 *
 */
public interface PerfStats {

    // it'd be better to hide this in the agent, and just raise events...
    static final String DIAGNOSTICS_LOGGER_NAME = "glassbox.track.diagnostics";
    static final Logger DIAGNOSTICS_LOGGER = LogManagement.getLogger(DIAGNOSTICS_LOGGER_NAME);

    static final String EXCEPTIONS_LOGGER_NAME = "glassbox.track.exceptions";
    static final Logger EXCEPTIONS_LOGGER = LogManagement.getLogger(EXCEPTIONS_LOGGER_NAME);
    
    static final String ITERATION_LOGGER_NAME = "glassbox.track.diagnostics.iterations";
    
    /**
     * @return Returns the count.
     */
    int getCount();

    /**
     * @return Returns the slowCount, i.e., the number of executions whose time was greater than the
     * global slow parameter.
     */
    int getSlowCount();

    /**
     * @return Returns the failureCount, which is the total number of failures, which can be more than one per unit of work ("operation")
     */    
    int getFailureCount();

    /**
     * @return the count of times the operation was slow due to a single request to this
     */    
    int getSlowSingleOperationCount();
    
    /**
     * @return Returns the number of operation requests which experienced at least one failure here, which can be only zero or one per unit of work ("operation")
     */    
    int getFailingOperationCount();
    
    /**
     * 
     * @return Total time spent in nanoseconds.
     */
    long getAccumulatedTime();

    public long getMaxTime();
    
    public double getMeanTime();
    
    /**
     * 
     * @return Timestamp (in nanoseconds) of first event recorded.
     * Returns Clock.UNDEFINED_TIME if no event has been recorded.
     */
    long getFirstEventTime();
    
    /**
     * 
     * @return Timestamp (in nanoseconds) of last event recorded.
     * Returns Clock.UNDEFINED_TIME if no event has been recorded.
     */
    long getLastEventTime();

    long getSlowThreshold();
    
    /**
     * This sets the slow threshold but just for this one performance statistic.
     * If this one composes child statistics, they are not automatically affected.
     * 
     * @param thresholdTime in nanoseconds
     */
    void setSlowThreshold(long thresholdTime);

    long recordEnd(glassbox.response.Response requestEvent);
    
    /**
     * Record the start of a timed event.
     * 
     * @param startTime in nanoseconds
     */
    void recordStart(long startTime);
    
    /**
     * Record the total elapsed time for an event (e.g., when the actual start and stop were not observed).
     * @param startInfo usage info with start time in nanoseconds
     * @param endInfo usage info with end time in nanoseconds
     * @param measurementError - measurement error for start and end times in nanoseconds
     * @return total accumulated usage thus far for this statistic in this operation (on this thread)
     */
    long recordUsage(long start, long end, long measurementError);

    /**
     * Record the total elapsed time for an event.
     * @param elapsedTime in nanoseconds
     * @return total accumulated usage thus far for this statistic in this operation (on this thread)
     */
    long recordUsage(long elapsedTime);
    
    void summarizeOperation(Response response);
    
    void skipOperation();

    /** did the current operation fail for the current thread/request/UofW */
    boolean hasUofWFailure();   
    
    CompositePerfStats getParent();
    
    /**
     * @return The key value for this entry in its owning registry.
     */
    Serializable getKey();
    
    /**
     * 
     * @param key The key value for this entry in its owning registry.
     */
    void setKey(Serializable key);
    
    StatisticsType getType();
    
    String getLayer();
    
    void setType(StatisticsType type);
    
    CallDescription getCallDescription();
    
    /** 
     * what were the top few worst (most frequent failures) seen, if any
     * 
     * @return list of FailureDescription
     */
    List/*<FailureDescription>*/ getWorstFailures();
    
    List/*<Request>*/ getLastFailingRequests();
    
    /** 
     * what were the top few longest elapsed time request seen, if any exceeded the performance threshold
     * 
     * @return list of SlowRequest
     */
    List/*<Response>*/ getSlowestSingleEvents();

    boolean isSlow(long time);
    
    /**
     * Can this statistics represent part of a user-visible unit of work based on static structure?
     */    
    boolean isOperationKey();
    
    /**
     * Is this statistic definitely a key for the current thread's UofW. Returns false if not known to be true.
     * @see isOperationKey(), which returns false if it can't be.
     */
    boolean isOperationThisRequest();
    
    /**
     * Does this statistics virtually contain children. 
     * Virtual containment occurs when the child request isn't executed in the control flow of the original statistics request, for example
     * executing a database statement is virtually contained beneath database statistics because the database conneciton and stats don't
     * wrap the actual statement execution. 
     */
    boolean isVirtuallyContained();
    
    Collection getChildren();
    
    /** debug method */
    StringBuffer dump(StringBuffer buffer, int i);
    
    PerfStats klone();
}