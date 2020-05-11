/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the n * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.config.extension.api.PluginTracking.PluginHolder;

import java.util.List;

/* Base interface for any problem analysis.
 * Specific problems are returned by implementations like <code>SlowAggregateResourceProblem</code>.
 */
public interface ProblemAnalysis extends PluginHolder {

//    /** 
//     * Return my best guesses of where to look for more information about the problem.
//     * May be null or zero length if I don't know of any. 
//     */
//    public List getRelatedURLs();
    
    /** 
     * Return list of specific events relevant to the problem, e.g., slow or failing calls or sampled example traces.
     */
    public List getEvents();
    
    // we could simplify the problem class hierarchy and just use a tag like this ... but
    // we're betting on wanting MORE specific context information over time
    
//    public static final int DB_CONNECTION_FAILURE = 0; 
//    public static final int DB_STATEMENT_FAILURE = 1;
//    public static final int FAIL_PROCESSING = 2;
//    public static final int DB_SLOW_CALL = 3;
//    public static final int DB_SLOW_OVERALL = 4;
//    public static final int REMOTE_SLOW_CALL = 5; 
//    public static final int REMOTE_SLOW_OVERALL = 6;
//    public static final int REMOTE_CALL_FAILURE = 7;
//    public static final int THREAD_CONTENTION = 8; 
//    public static final int SLOW_METHOD = 9;
//    public static final int EXCESS_WORK = 10;
//    
//    public int getProblemType();
}
