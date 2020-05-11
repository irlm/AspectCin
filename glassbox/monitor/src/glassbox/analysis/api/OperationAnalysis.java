/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.track.api.*;
import glassbox.util.timing.api.TimeConversion;

import java.util.List;


public interface OperationAnalysis {
    /**
     * The summary also exposes the summary of the analyzed operation.
     * 
     */
    OperationSummary summary();
    
    /**
     * @return List of problems. If no problems this will be an empty list, not null.
     */
    List/*<ProblemAnalysis>*/ problems();
    
    /**
     * 
     * @param clazz the most specific subtype of operation analysis sought
     * @return a list of zero or more matching problems
     */
    List/*<ProblemAnalysis>*/ getProblemsOfType(Class/*<? extends ProblemAnalysis>*/ problemSubclass);
    
    /**
     * 
     * @return a break down of times and counts for ok, slow, and failing scenarios
     */
    ScenarioSummary getScenarioSummary();

    /**
     * 
     * @see TimeDecomposition for relevant constants (for areas)
     * @return a break down of overall time by top-level area: database, remote call, dispatch, CPU use, I/O wait, etc.  
     */
    TimeDecomposition getComponentDecomposition();

    /**
     * 
     * @see TimeDecomposition for relevant constants (for areas)
     * @return a break down of overall time by type of thread use: runnable, blocked, waiting, etc.  
     */
    TimeDecomposition getResourceDecomposition();

    /** time in nanos */
    long getMonitoringStartTime();
    
    /** how many milliseconds for an operation is considered slow by default? */
    long getSlowThresholdMillis();
    
    /** what fraction of operations must exceed the slow threshold for it to be considered slow */
    double getMinimumSlowFrac();
    
    /** @return mean seconds of CPU time used per execution of this operation */
    double getMeanCpuTime();
    
    /** @return an integer which is the percent of time spent in that component versus the overall time in the operation*/
    int getComponentTimePercent(int compID);
    
    /** @return an integer which is the percent of time spent in that resource versus the overall time in the operation*/
    int getResourceTimePercent(int compID); 
    
    /** @return an string which is the formatted time spent in that component versus the overall time in the operation*/
    String getComponentTimeMS(int compID);
    
    /** @return an string which is the formatted time spent in that resource versus the overall time in the operation*/
    String getResourceTimeMS(int compID);
    
    /** @return an integer which is the percent of time spent in CPU versus the overall time in the operation*/
    int getCPUTimePercent(); 
   
    /** @return an formatted string which is the percent of time spent in CPU versus the overall time in the operation*/
    String getFormatCPUTime();
    
    
    
    List/*<Request>*/ getProblemRequests();

    boolean isFailing();
    
    boolean isSlow();
}
