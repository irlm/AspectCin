/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis;

import org.aspectj.lang.SoftException;

import glassbox.agent.api.ServiceException;

public aspect ServiceErrorHandling {

    pointcut scope() : 
        (within(glassbox.analysis.*) || within(glassbox.agent.control.*)) && execution(* *(..));
    
    declare soft: Exception+: scope();
    
    // log any errors that happen on our service interface
    // then convert them & pass back to the caller
    after() throwing (SoftException se) : scope() && execution(public * glassbox.control.api.*.*(..)) {
        logError("Failure in analysis", se.getCause());
        throw new ServiceException("Failure invoking service", se.getCause());
    }
    
    private static final long serialVersionUID = 1L;
}
