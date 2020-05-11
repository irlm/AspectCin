/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;


import glassbox.agent.api.ApiType;
import glassbox.track.api.OperationDescription;

import java.util.List;


/**
 * 
 * Description of data about an operation.
 *  
 * We don't use JavaBeans-style getters for these operations, so that they are portable
 * to JMX's that require a matching setter...
 * 
 * @author Ron Bodkin
 *
 */
public interface OperationSummary {
    public static final int StatusOK = 0; 
    public static final int StatusSlow = 200; 
    public static final int StatusFailing = 400;
    public static final String SLOW_DATABASE = "slow.database";
    public static final String SLOW_REMOTE_OVERALL = "slow.remote.overall";
    public static final String SLOW_REMOTE_CALL = "slow.remote.call";
    public static final String FAIL_DATABASE_CONNECTION = "fail.database.connection";
    public static final String FAIL_DATABASE_STATEMENT = "fail.database.statement";
    public static final String FAIL_REMOTE_CALL = "fail.remote.call";
    public static final String SINGLE_THREAD_QUEUE = "single.threaded.queue";
    public static final String EXCESS_WORK = "excess.work";
    public static final String FAIL_PROCESSING = "fail.processing";
    public static final String EXCESS_CPU = "excess.cpu";
    public static final String SLOW_METHOD = "slow.method";
    public static final String FAILING_DISPATCH = "fail.dispatch";
    public static final String SLOW_DISPATCH = "slow.dispatch";
    public static final String OK = "ok.performance"; // use an "okay" diagnosis instead of testing for empty list of diagnoses all over the place
    
	OperationDescription getOperation();
	
    /**
     * @return summary of status - for now OK or SLOW - see constants above
     */
    int statusCode();

    boolean isFailing();
    
    boolean isProblem();
    
    boolean isSlow();
    
    int getCount();
    
    /** average time in seconds */
    double getAvgExecutionTime();
    
    /**
	 * @return list of string descriptions of analysis findings. Probably will evolve to be an object
	 * summary format... The list will never be null, and will never be empty when it is sent to the client-- in the okay case
     * it will still have a finding of OK.
	 */
	List/*<String>*/ analysisFindings();
}
