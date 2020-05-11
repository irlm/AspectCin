/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;

import glassbox.analysis.api.SlowMethodProblem;
import glassbox.client.helper.MessageHelper;
import glassbox.track.api.SlowRequestDescriptor;
import glassbox.util.timing.api.TimeConversion;

public class SlowMethodProblemHelper extends BaseProblemHelper {

    protected String problemKey = "slow.method";
    
    public SlowMethodProblemHelper() {
    }
    
    public void setSlowProblemCount(int slowProblemCount) {
        symptom = (slowProblemCount>1);
    }

    public String getProblemKey() {       
        return problemKey;
    }
    
    public StackTraceElement[] getStackTrace() {
        return ((SlowMethodProblem)problem).getDescriptor().getThreadState().getStackTrace();
    }
     
    public String getProblemSummary() {
        
        
        SlowMethodProblem methodProblem = ((SlowMethodProblem)problem);
        SlowRequestDescriptor requestDescriptor = methodProblem.getDescriptor();
        StackTraceElement ste = requestDescriptor.getThreadState().getStackTrace()[0];
        int count = analysisData.getScenarioSummary().getCount();
        String time =  TimeConversion.formatMeanNanos(((SlowMethodProblem)problem).getDescriptor().getElapsedTime(), count);
        String methodName = ste.getClassName() + "." + ste.getMethodName() + "()";
        Object[] args = {operationData.getOperationShortName(), analysisData.getSlowOperationsAverageTime(), methodName, time };
        return formatSummary(args);
    }
    
    public String getProblemTypeInfo() {
        if(!isSymptom()) {
            return MessageHelper.getString("problem.type.info." + getProblemKey() + ".1");
        }
        
        return MessageHelper.getString("problem.type.info." + getProblemKey() + ".2");        
    }
    
    public String getProblemDescription() {
        
        int count = analysisData.getScenarioSummary().getCount();
        String time =  TimeConversion.formatMeanNanos(((SlowMethodProblem)problem).getDescriptor().getElapsedTime(), count);        
        Object[] args = {time};
        return formatDescription(args);         
    }
    
    public String getProblemDetailPanel() {
        return MessageHelper.getString(problemKey+".problem.panel");
    }
    
}