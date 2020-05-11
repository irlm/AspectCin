/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;

import glassbox.client.helper.MessageHelper;
import glassbox.track.api.CallDescription;

public class SlowDatabaseCallHelper extends BaseProblemHelper {

    protected String problemKey = "slow.database";

    public String getProblemKey() {
        return problemKey;
    }

    public String getProblemDetailPanel() {
        //only one for the problem type
        if (isVisible()) { 
            return MessageHelper.getString("database.panel");
        } else {
            return MessageHelper.getString("empty.panel");
        }
        
    }

    public String getProblemSummary() {
        Object description;
        int sz = problemsOfType.size();
        if (sz > 1) {
            description = new Integer(sz);
        } else {
            CallDescription slowcall = getCall();
            if (slowcall.callType() == CallDescription.DATABASE_CONNECTION) {
                description = "establishing a connection to";
            } else {
                description = "executing the statement '" + slowcall.getSummary() + "' on";
            }
        }

        Object[] args = { operationData.getOperationShortName(),
                analysisData.getSlowOperationsAverageTime(), getAffectedDatabases(), description };
        return formatSummary(args);
    }
    
    public boolean isVisible() {
        return problemsOfType.get(0) == problem;
    }
}
