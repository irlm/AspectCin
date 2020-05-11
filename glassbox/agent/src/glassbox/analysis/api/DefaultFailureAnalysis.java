/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.track.api.FailureDescription;
import glassbox.track.api.FailureStats;

import java.util.List;


public class DefaultFailureAnalysis extends DefaultSingleCallProblem implements FailureAnalysis {

    private int count;
    private FailureDescription failure;
    private List failingRequests;
    private List relatedURLs;
    
    public DefaultFailureAnalysis(FailureStats stats) {
        super(stats.getFailure().getCall(), stats.getRecentRequests());
        this.count = stats.getCount();
        this.failure = stats.getFailure();
    }
    
    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }
    
    /**
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }
    
    /**
     * @return the failure
     */
    public FailureDescription getFailure() {
        return failure;
    }
    
    /**
     * @param failure the failure to set
     */
    public void setFailure(FailureDescription failure) {
        this.failure = failure;
    }
    
    /**
     * @return the relatedURLs
     */
    public List getRelatedURLs() {
        return relatedURLs;
    }
    
    /**
     * @param relatedURLs the relatedURLs to set
     */
    public void setRelatedURLs(List relatedURLs) {
        this.relatedURLs = relatedURLs;
    }
    
    /* (non-Javadoc)
     * @see glassbox.analysis.api.FailureAnalysis#getFailingRequests()
     */
    public List getFailingRequests() {
        return failingRequests;
    }

}
