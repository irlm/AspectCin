/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.track.api.CallDescription;
import glassbox.util.timing.api.TimeConversion;

import java.util.LinkedList;
import java.util.List;

public class DefaultSingleCallProblem extends AbstractProblemAnalysis implements SingleCallProblem {

    private CallDescription aCall;
    private List events;

//    public DefaultSingleCallProblem(CallDescription aCall) {
//        this(aCall, new LinkedList());
//    }
    
    public DefaultSingleCallProblem(CallDescription aCall, List events) {
        this.aCall = aCall;
        this.events = events;
    }
    
    /* (non-Javadoc)
     * @see glassbox.analysis.api.SingleCallProblem#getCall()
     */
    public CallDescription getCall() {
        return aCall;
    }

    /* (non-Javadoc)
     * @see glassbox.analysis.api.SingleCallProblem#getEvents()
     */
    public List getEvents() {
        return events;
    }

}
