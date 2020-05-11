/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.policy;

import glassbox.agent.api.ApiType;
import glassbox.agent.api.NotSerializable;
import junit.framework.TestCase;

/** 
 * 
 * This enforces the rule that API elements implement glassbox.api.ApiType.
 */   

public aspect ApiPolicy {
    
    declare parents: glassbox..api..* && !ApiType && !NotSerializable+ && !TestCase+ && !glassbox.util.logging.api..* implements ApiType;

    pointcut testCode(): 
        within(TestCase+) || within(glassbox.simulator..*) || within(glassbox.test..*) || within(glassbox.acceptance..*) || within(glassbox.debug..*);
    
    pointcut printExemption(): 
        within(glassbox.bootstrap.log.BootstrapLog) || within(glassbox.util.logging.api.LogManagement) || within(glassbox.version.*) || within(glassbox.agent.ErrorContainment);
    
    declare warning: (get(* System.err) || get(* System.out) || call(* printStackTrace())) && !testCode() && !printExemption(): 
        "don't print, use a logger";
    
    private static final long serialVersionUID = 1L;
}
