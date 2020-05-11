/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;

import glassbox.client.helper.MessageHelper;

public class SlowRemoteCallHelper extends BaseProblemHelper {

    protected String problemKey = "slow.remote.call";
    
    public String getProblemKey() {       
        return problemKey;
    }
    
    //summary.info_1.slow.remote.call=When the {0} operation ran slowly, it took an average of {1}, including significant time in remote call {2} waiting for a response from an external Web Service.
    //summary.info_n.slow.remote.call=When the {0} operation ran slowly, it took an average of {1}, including {2} distinct occurences of waiting excessively for a response from an external Web Service.
    public String getProblemSummary() {
        Object description;
        int sz = problemsOfType.size();
        if (sz > 1) {
            description = new Integer(sz);
        } else {
            description = getCall().getSummary();
        }

        Object test = getAffectedRemoteServices();
        Object[] args = { operationData.getOperationShortName(),
                analysisData.getSlowOperationsAverageTime(), description };

        return formatSummary(args);
    }
    
    public String getProblemDetailPanel() {
        //only one for the problem type
        if (isVisible()) { 
            return MessageHelper.getString("slow.remote.call.problem.panel");
        } else {
            return MessageHelper.getString("empty.panel");
        }
        
    }

    public boolean isVisible() {
        return problemsOfType.get(0) == problem;
    }
}
