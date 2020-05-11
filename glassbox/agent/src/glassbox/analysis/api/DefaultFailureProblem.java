/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.track.api.*;

import java.util.List;

/**
 * Events is a list of FailureDescription 
 * 
 * @author Ron Bodkin
 *
 */
public class DefaultFailureProblem extends DefaultSingleCallProblem implements ProblemAnalysis {

    private int numFailures;

    public DefaultFailureProblem(CallDescription aCall, int numFailures, List worstEvents) {
        super(aCall, worstEvents);
        this.numFailures = numFailures;
    }
    
    public int getNumFailures() {
        return numFailures;
    }
    
    private static final long serialVersionUID = 1;
}
