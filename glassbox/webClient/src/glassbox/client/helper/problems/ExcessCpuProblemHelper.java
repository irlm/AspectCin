/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;

import glassbox.analysis.api.ExcessCpuProblem;
import glassbox.client.helper.MessageHelper;
import glassbox.util.timing.api.TimeConversion;

import java.text.MessageFormat;
import java.text.NumberFormat;

public class ExcessCpuProblemHelper extends BaseProblemHelper {

   protected String problemKey = "excess.cpu";
    
    public String getProblemKey() {       
        return problemKey;
    }
 
    public String getProblemSummary() {
        ExcessCpuProblem xcprob = (ExcessCpuProblem)problem;

        String time = TimeConversion.formatMeanNanos(xcprob.getCpuTime(),xcprob.getCount()); 
        Object[] args = {operationData.getOperationShortName(), time};
        MessageFormat form = new MessageFormat(MessageHelper.getString("summary.info_1." + getProblemKey()));
        return form.format(args);
    }
    
    public String getProblemDescription() {
        ExcessCpuProblem xcprob = (ExcessCpuProblem)problem;
        
        int count = analysisData.getScenarioSummary().getCount();
        NumberFormat fmt = NumberFormat.getPercentInstance();
        fmt.setMinimumFractionDigits(0);
        fmt.setMaximumFractionDigits(1);
        String fraction = fmt.format(xcprob.getCausedSlowFrequency());
        Object[] args = {new Integer(xcprob.getCount()), TimeConversion.formatTime(xcprob.getCpuTime()), TimeConversion.formatTime(xcprob.getTotalTime()), fraction};
        return formatDescription(args);         
    }
    
    public String getProblemDetailPanel() {
        return MessageHelper.getString(problemKey+".problem.panel");
    }
}
