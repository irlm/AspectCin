/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;


import glassbox.client.helper.MessageHelper;

public class DatabaseStatementFailureHelper extends BaseProblemHelper {

    protected String problemKey = "fail.database.statement";
    
    public String getProblemKey() {       
        return problemKey;
    }
    
    public String getProblemSummary() {        
        int sz = problemsOfType.size();
        Object description = (sz>1 ? (Object)new Integer(sz) : (Object)getCall().getCallKey().toString());
        Object[] args = {operationData.getOperationShortName(), getAffectedDatabases(), description};
        return formatSummary(args);
    }
    
    public String getProblemDescription() {        
        Object[] args = {getCall().getCallKey().toString(), new Integer(getFailure().getNumFailures())};
        return formatDescription(args);
    }
  
    public String getProblemDetailPanel() {
        return MessageHelper.getString("fail.database.statement.problem.panel");
    }
    
    
}
