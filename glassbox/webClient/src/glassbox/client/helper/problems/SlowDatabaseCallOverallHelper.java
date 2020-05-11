/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;

import glassbox.analysis.api.SlowDatabaseOverallProblem;
import glassbox.client.helper.MessageHelper;

public class SlowDatabaseCallOverallHelper extends BaseProblemHelper {

    protected String problemKey = "slow.database.overall";
    
    public String getProblemKey() {       
        return problemKey;
    }
    
    public String getProblemDetailPanel() {
        return MessageHelper.getString("slow.database.overall.problem.panel");
    }
    
    public String getProblemSummary() {
        SlowDatabaseOverallProblem slowDbProblem = (SlowDatabaseOverallProblem)problem;
        
        Object[] args = { operationData.getOperationShortName(), new Integer(slowDbProblem.getDistinctCalls().size()),
                getAffectedResources("database", slowDbProblem.getResourceKeys()) };
        return formatSummary(args);
    }
    
}
