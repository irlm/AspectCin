/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.track.api.CallDescription;
import glassbox.util.timing.api.TimeConversion;

import java.util.List;

public class SlowCallProblem extends DefaultSingleCallProblem implements SlowProblem {
    private long accumulatedTime;
    private int count;
    private int opCount;

    public SlowCallProblem(CallDescription aCall, List events, long accumulatedTime, int count, int opCount) {
        super(aCall, events);
        this.accumulatedTime = accumulatedTime;
        this.count = count;
        this.opCount = opCount;
    }

    /* (non-Javadoc)
     * @see glassbox.analysis.api.ProblemAnalysis#getMeanTime()
     */
    public double getMeanTime() {
        return TimeConversion.meanNanosInSeconds(accumulatedTime, opCount);
    }
    
    public long getAccumulatedTime() {
        return accumulatedTime;
    }
    
    public int getCount() {
        return count;
    }
    
    public int getOperationCount() {
        return opCount;
    }

    public double getMeanCount() {
        if (opCount==0) {
            return 0.;
        }
        return ((double)count)/((double)opCount);
    }
}
