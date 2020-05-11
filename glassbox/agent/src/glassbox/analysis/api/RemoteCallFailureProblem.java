/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import java.util.List;

import glassbox.track.api.*;

public class RemoteCallFailureProblem extends DefaultFailureProblem {    
    
    public RemoteCallFailureProblem(CallDescription aCall, int numFailures, List worstEvents) {
        super(aCall, numFailures, worstEvents);
    }

    private static final long serialVersionUID = 1;
}
