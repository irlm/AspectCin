/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;

import glassbox.client.helper.MessageHelper;

import java.text.MessageFormat;

public class ExcessWorkProblemHelper extends BaseProblemHelper {

    protected String problemKey = "excess.work";
    
    public String getProblemKey() {       
        return problemKey;
    }
    
    public String getProblemSummary() {                        
        Object[] args = {operationData.getOperationShortName()};
        MessageFormat form = new MessageFormat(MessageHelper.getString("summary.info_1." + getProblemKey()));
        return form.format(args);
    }
    
    public String getProblemDescription() {
        Object[] args = {operationData.getOperationShortName()};
        MessageFormat form = new MessageFormat(MessageHelper.getString("summary.info_1." + getProblemKey()));
        return form.format(args);
    }
    
    public String getProblemDetailPanel() {
        //only one for the problem type
            return MessageHelper.getString("excess.work.problem.panel");
    }
    
}
