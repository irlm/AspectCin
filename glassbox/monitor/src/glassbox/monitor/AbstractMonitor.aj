/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.monitor;

import glassbox.response.Response;
import glassbox.response.ResponseFactory;
import glassbox.track.api.*;
import glassbox.util.logging.api.LogManagement;

import java.io.Serializable;

import org.aspectj.lang.JoinPoint.StaticPart;

public abstract aspect AbstractMonitor extends AbstractMonitorControl {
    
    protected pointcut monitorBegin(Object identifier);
    
    protected pointcut monitorPoint(Object identifier);
    
    protected pointcut monitorEnd();
    
    /** 
     * This monitor often needs to dominate its children because they want to set parameters etc. after a response is established
     * Children that want to participate in this must create a nested inner aspect that extends the marker interface NestedAdvice.
     */
    protected interface LowerPrecedence {}
    declare precedence: AbstractMonitorControlAspect, AbstractMonitor+, LowerPrecedence+;
    
    before(Object identifier) : monitorBegin(identifier) {
        begin(getKey(identifier));
    }
    
    before(Object identifier) : monitorPoint(identifier) {
        begin(getKey(identifier));
    }
    
    private pointcut monitorEndAllCases() : monitorEnd() || monitorPoint(*);

    // after throwing needs to be before after returning so we don't handle errors from completing the response (NPE? test errors...)
    after() throwing (Throwable t): monitorEndAllCases() {
        endException(t, thisJoinPointStaticPart);
    }
    
    after() returning: monitorEndAllCases() {
        endNormally(thisJoinPointStaticPart);
    }
    
    declare error: within(AbstractMonitorClass+) && call(* ResponseFactory.getResponse(..)) && !withincode(* AbstractMonitorClass.createResponse(..)):
        "call create response to get responses from the factory to ensure consistent matching";    
}
