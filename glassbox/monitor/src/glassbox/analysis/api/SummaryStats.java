/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.util.timing.api.TimeConversion;

public class SummaryStats {

    private int count;
    private long accumulatedTime;
    private static final long serialVersionUID = 2L;
    
    public SummaryStats(int count, long accumulatedTime) {
        this.count = count;
        this.accumulatedTime = accumulatedTime;
    }
    
    /**
     * @return the accumulatedTime in nanoseconds
     */
    public long getAccumulatedTime() {
        return accumulatedTime;
    }
    
    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }
    
    public double getMeanSeconds() {
        return TimeConversion.meanNanosInSeconds(accumulatedTime, count);
    }
}
