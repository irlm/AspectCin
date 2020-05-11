/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

public class FrequencySummaryStats extends SummaryStats {
    private int problemCount;

    public FrequencySummaryStats(int overallCount, int problemCount, long accumulatedTime) {
        super(overallCount, accumulatedTime);
        this.problemCount = problemCount;
    }

    public int getProblemCount() {
        return problemCount;
    }
        
    /**
     * 
     * @return how frequently did this component cause the overall operation to have its worst problem
     * (i.e., caused failures, or if not then caused overall slowness by exceeding the SLA)
     */
    public double getFrequency() {
        return ((double)problemCount)/((double)getCount());
    }
}
