/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;

import glassbox.analysis.api.DbConnectionFailureProblem;
import glassbox.client.helper.MessageHelper;
import glassbox.track.api.CallDescription;

import java.text.MessageFormat;

public class DatabaseConnectionFailureHelper extends BaseProblemHelper {

    protected String problemKey = "fail.database.connection";
    
    public String getProblemKey() {       
        return problemKey;
    }
    
    public String getProblemSummary() {
        getAffectedDatabases();
        Object[] args = {operationData.getOperationShortName(), getAffectedDatabases()};
        return formatSummary(args);
    }
    
    public String getProblemTypeInfo() {
        return MessageHelper.getString("problem.type.info." + getProblemKey());
    }
    
    public String getProblemDescription() {
        CallDescription requestDescriptor = getConnProblem().getCall();
        String url = (String)requestDescriptor.getResourceKey();        
        Object[] args = {url, new Integer(getConnProblem().getNumFailures())};
        MessageFormat form = new MessageFormat(MessageHelper.getString("problem.describe." + getProblemKey()));
        return form.format(args);
    }
    public String getProblemDetailPanel() {
        return MessageHelper.getString("fail.database.connection.problem.panel");
    }
    private DbConnectionFailureProblem getConnProblem() {
        return ((DbConnectionFailureProblem)problem);
    }
}
