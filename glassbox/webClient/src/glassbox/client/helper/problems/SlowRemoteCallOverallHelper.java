/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;

import glassbox.analysis.api.SlowRemoteOverallProblem;
import glassbox.client.helper.MessageHelper;

public class SlowRemoteCallOverallHelper extends BaseProblemHelper {

    protected String problemKey = "slow.remote.overall";
    
    public String getProblemKey() {       
        return problemKey;
    }
    
    public String getProblemDetailPanel() {
        return MessageHelper.getString("slow.remote.overall.problem.panel");
    }
    
    public String getProblemSummary() {
        SlowRemoteOverallProblem slowRemoteOverallProblem = (SlowRemoteOverallProblem)problem;
   
        Object[] args = {operationData.getOperationShortName(), analysisData.getSlowOperationsAverageTime(), new Integer(slowRemoteOverallProblem.getDistinctCalls().size())};

        return formatSummary(args);
    }
}
