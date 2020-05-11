/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.track.api.OperationDescription;
import glassbox.util.timing.api.TimeConversion;

import java.util.List;

// this along with UI impl classes should be moved out of the monitor and instead we should create a plugin implementor
// utility jar that's NOT in the common classpath
public class OperationSummaryImpl implements OperationSummary {

	private List analysisFindings;
	private double avgExecTime;
    private int count;
    private int statusCode;
	private OperationDescription operation;
    private boolean wasSlow;

	public OperationSummaryImpl(OperationDescription description, int count, long totalTime, 
            int statusCode, List analysisFindings, boolean wasSlow) {
		this.operation = description;
		this.count = count;
        avgExecTime = TimeConversion.meanNanosInSeconds(totalTime, count);
        
        this.statusCode = statusCode;
        this.analysisFindings = analysisFindings;
        this.wasSlow = wasSlow;
	}

	public List analysisFindings() {
		return analysisFindings;
	}
   
    public int statusCode() {
        return statusCode;
    }
    
    /* (non-Javadoc)
     * @see glassbox.analysis.api.OperationSummary#getAvgExecutionTime()
     */
    public double getAvgExecutionTime() {
        return avgExecTime;
    }

    /* (non-Javadoc)
     * @see glassbox.analysis.api.OperationSummary#getCount()
     */
    public int getCount() {
        return count;
    }

    /** debug output string */
    public String toString() {
        return operation+": avgTime = "+avgExecTime+", count ="+count+", status = "+statusCode+", # findings = "+analysisFindings.size(); 
    }

    public OperationDescription getOperation() {
        return operation;
    }

    /* (non-Javadoc)
     * @see glassbox.analysis.api.OperationSummary#isFailing()
     */
    public boolean isFailing() {
        return statusCode >= StatusFailing;
    }

    public boolean isProblem() {
        return statusCode != StatusOK;
    }

    /* (non-Javadoc)
     * @see glassbox.analysis.api.OperationSummary#isSlow()
     */
    public boolean isSlow() {
        return wasSlow;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((analysisFindings == null) ? 0 : analysisFindings.hashCode());
        long temp;
        temp = Double.doubleToLongBits(avgExecTime);
        result = PRIME * result + (int) (temp ^ (temp >>> 32));
        result = PRIME * result + count;
        result = PRIME * result + ((operation == null) ? 0 : operation.hashCode());
        result = PRIME * result + statusCode;
        result = PRIME * result + (wasSlow ? 1231 : 1237);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OperationSummaryImpl other = (OperationSummaryImpl) obj;
        if (analysisFindings == null) {
            if (other.analysisFindings != null)
                return false;
        } else if (!analysisFindings.equals(other.analysisFindings))
            return false;
        if (Double.doubleToLongBits(avgExecTime) != Double.doubleToLongBits(other.avgExecTime))
            return false;
        if (count != other.count)
            return false;
        if (operation == null) {
            if (other.operation != null)
                return false;
        } else if (!operation.equals(other.operation))
            return false;
        if (statusCode != other.statusCode)
            return false;
        if (wasSlow != other.wasSlow)
            return false;
        return true;
    }
    
    public static class Util {
        public static boolean isFailureProblem(String problemCode) {
            return problemCode.indexOf("fail.")==0;
        }
    }
    static final private long serialVersionUID = 2L;
}
