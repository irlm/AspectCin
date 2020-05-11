/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper.problems;

import glassbox.client.helper.MessageHelper;
import glassbox.track.api.CallDescription;

public class RemoteCallFailureHelper extends BaseProblemHelper {

    protected String problemKey = "fail.remote.call";
    
    public String getProblemKey() {       
        return problemKey;
    }
 
    public String getProblemSummary() {        
        Object[] args = {operationData.getOperationShortName(), getAffectedRemoteServices(), new Integer(problemsOfType.size())};
        return formatSummary(args);
    }
    
    public String getProblemDescription() {        
        CallDescription requestDescriptor = getCall();
        Object[] args = {requestDescriptor.getSummary(), new Integer(getFailure().getNumFailures())};
        return formatDescription(args);
    }

    public String getProblemDetailPanel() {
        return MessageHelper.getString("fail.remote.call.problem.panel");
    }
}
