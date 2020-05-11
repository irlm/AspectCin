/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.track.api.CallDescription;
import glassbox.util.timing.api.TimeConversion;

import java.io.Serializable;
import java.util.*;

public class SlowAggregateResourceProblem extends AbstractProblemAnalysis implements SlowProblem {
    
    public static int INDIVIDUAL_RESOURCE = 1;
    public static int AGGREGATE_OF_RESOURCE_TYPE = 2;
    public static int ALL_RESOURCES = 3;
   
    private List/*<SlowCallProblem>*/ distinctCalls;
    private List resourceKeys;
    private long accumulatedTime;
    private int aggCount;
    private int opCount;

    /**
     * 
     * @param distinctCalls list of SlowCallProblem
     * @param resourceKeys list of Serializable
     * @param accumulatedTime
     * @param aggCount
     * @param opCount
     */
    public SlowAggregateResourceProblem(List distinctCalls, List resourceKeys, long accumulatedTime, int aggCount, int opCount) {
        if (resourceKeys==null) throw new IllegalArgumentException("Invalid null resource key");
        
        this.distinctCalls = distinctCalls;
        this.resourceKeys = resourceKeys;
        this.accumulatedTime = accumulatedTime;
        this.aggCount = aggCount;
        this.opCount = opCount;
        
        // sort the calls in descending order of accumulated time.
        Collections.sort(distinctCalls, new SlowCallComparator());
    }    
    
    /**
     * 
     * @return list of SlowCallProblem
     */
    public List getDistinctCalls() {
        return distinctCalls;
    }
    
    // List <Serializable> which are actually String
    public List getResourceKeys() {
        return resourceKeys;
    }

    public List getRelatedURLs() {
        return null;
    }
    
    /**
     * We currently don't aggregate any events...
     */
    public List getEvents() {
        return null;
    }
    
    /* (non-Javadoc)
     * @see glassbox.analysis.api.ProblemAnalysis#getMeanTime()
     */
    public double getMeanTime() {
        return TimeConversion.meanNanosInSeconds(accumulatedTime, opCount);
    }
    
    /* (non-Javadoc)
     * @see glassbox.analysis.api.SlowProblem#getAccumulatedTime()
     */
    public long getAccumulatedTime() {
        return accumulatedTime;
    }

    /* (non-Javadoc)
     * @see glassbox.analysis.api.SlowProblem#getCount()
     */
    public int getCount() {
        return aggCount;
    }

    /* (non-Javadoc)
     * @see glassbox.analysis.api.SlowProblem#getOperationCount()
     */
    public int getOperationCount() {
        return opCount;
    }
    
    public double getMeanCount() {
        if (getOperationCount()==0) {
            return 0.;
        }
        return ((double)getCount())/((double)getOperationCount());
    }
    
    private static final long serialVersionUID = 1;
}
