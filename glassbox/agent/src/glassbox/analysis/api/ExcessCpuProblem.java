/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.util.timing.api.TimeConversion;

import java.util.List;

public class ExcessCpuProblem extends AbstractProblemAnalysis {

	private long totalTime;
	private long cpuTime;
	private int count;
    private double causedSlowFrequency;

    private static final long serialVersionUID = 3;

	public long getCpuTime() {
		return cpuTime;
	}

	public void setCpuTime(long cpuTime) {
		this.cpuTime = cpuTime;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

    /* (non-Javadoc)
     * @see glassbox.analysis.api.ProblemAnalysis#getMeanTime()
     */
    public double getMeanTime() {
        return TimeConversion.meanNanosInSeconds(totalTime, count);
    }

    public void setCausedSlowFrequency(double causedSlowFrequency) {
        this.causedSlowFrequency = causedSlowFrequency;
    }
    
    public double getCausedSlowFrequency() {
        return causedSlowFrequency;
    }
    
    public List getEvents() {
        return null;
    }
    
}
