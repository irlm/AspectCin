/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.track.api.SlowRequestDescriptor;

import java.util.ArrayList;
import java.util.List;

public class SlowMethodProblem extends AbstractProblemAnalysis implements SlowProblem {

    private SlowRequestDescriptor descriptor;

    public SlowMethodProblem(SlowRequestDescriptor descriptor) {
        this.descriptor = descriptor;
    }
    
    public SlowRequestDescriptor getDescriptor() {
        return descriptor;
    }
    
    public List getRelatedURLs() {
        return null;
    }
    
    public List getEvents() {
        List list = new ArrayList(1);
        list.add(descriptor);
        return list;
    }
    
    /* (non-Javadoc)
     * @see glassbox.analysis.api.ProblemAnalysis#getMeanTime()
     */
    public double getMeanTime() {
        return descriptor.getMeanTime();
    }

    /* (non-Javadoc)
     * @see glassbox.analysis.api.SlowProblem#getAccumulatedTime()
     */
    public long getAccumulatedTime() {
        return descriptor.getElapsedTime();
    }

    /* (non-Javadoc)
     * @see glassbox.analysis.api.SlowProblem#getCount()
     */
    public int getCount() {
        return descriptor.getSlowCount();
    }

    /* (non-Javadoc)
     * @see glassbox.analysis.api.SlowProblem#getOperationCount()
     */
    public int getOperationCount() {
        return descriptor.getOperationCount();
    }

    /* (non-Javadoc)
     * @see glassbox.analysis.api.SlowProblem#getMeanCount()
     */
    public double getMeanCount() {
        return descriptor.getMeanCount();
    }
    
    private static final long serialVersionUID = 1;
}
