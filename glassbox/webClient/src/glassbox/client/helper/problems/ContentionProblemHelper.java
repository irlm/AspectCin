/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;

import glassbox.analysis.api.ContentionProblem;
import glassbox.client.helper.MessageHelper;
import glassbox.track.api.SlowRequestDescriptor;
import glassbox.track.api.ThreadState;
import glassbox.util.timing.api.TimeConversion;

import java.text.MessageFormat;

public class ContentionProblemHelper extends BaseProblemHelper {
    
    protected String problemKey = "single.threaded.queue";
    
    public String getProblemKey() {       
        return problemKey;
    }

    public String getProblemSummary() {
        ContentionProblem contentionProblem = ((ContentionProblem)problem);
        SlowRequestDescriptor requestDescriptor = contentionProblem.getDescriptor();
        ThreadState tstate = requestDescriptor.getThreadState();
        StackTraceElement ste = contentionProblem.getDescriptor().getThreadState().getStackTrace()[0];
        String methodName = ste.getClassName() + "." + ste.getMethodName() + "()";
        String time = TimeConversion.formatMeanNanos(requestDescriptor.getElapsedTime(), requestDescriptor.getOperationCount());
        Object[] args = {operationData.getOperationShortName(), time, methodName};
        MessageFormat form = new MessageFormat(MessageHelper.getString("summary.info_1." + getProblemKey()));
        return form.format(args);
    }
    
    public String getProblemTypeInfo() {
        SlowRequestDescriptor requestDescriptor = ((ContentionProblem)problem).getDescriptor();
        String time = TimeConversion.formatMeanNanos(requestDescriptor.getElapsedTime(), requestDescriptor.getOperationCount());
        Object[] args = {time};
        MessageFormat form = new MessageFormat(MessageHelper.getString("problem.describe.slow.single.threaded.queue"));
        return form.format(args);
    }
    
    public StackTraceElement[] getStackTrace() {        
        return ((ContentionProblem)problem).getDescriptor().getThreadState().getStackTrace();
    }
    
    public String getProblemDetailPanel() {
        return MessageHelper.getString("contention.problem.panel");
    }
    
}
