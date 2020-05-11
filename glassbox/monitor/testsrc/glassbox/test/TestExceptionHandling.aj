/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.test;

import glassbox.agent.ErrorContainment;
import glassbox.agent.ErrorContainmentTest;
import junit.framework.TestCase;

/**
 * Aspect that handles exception handling within test code. Softens checked exceptions and
 * overrides error containment so tests fail when unexpected error occur.
 */
public aspect TestExceptionHandling {
    
    public pointcut inErrorContainmentTest():
        (within(ErrorContainmentTest) || within(glassbox.agent.control.InitializationTest)) && execution(* *(..));
    
    // we want to include normal error handling for testing error containment 
    
    public pointcut inGlassboxTestMethod():
        within(glassbox..* && TestCase+) && (execution(public * test*(..)) || execution(* TestCase.*(..)));
    
    /**
     * softens exceptions that might be thrown out of test methods. Does so at the method execution level, so we can catch checked
     * exceptions around a call site, if need be.
     */
    declare soft: Exception+: inGlassboxTestMethod();
    
    public pointcut errorContainInTest():
        execution(* ErrorContainment.handle(*)) && cflow(inGlassboxTestMethod()) && !cflow(inErrorContainmentTest());
    /**
     * make test cases fail if they have an error that was contained - rethrow the error
     */
    before(RuntimeException rte): args(rte) && errorContainInTest() {
        throw rte;
    }

    before(Error err): args(err) && errorContainInTest() {
        throw err;
    }

    // code to find out why something unexpected was softened
//    static aspect TraceExceptionConversion {
//        after() throwing (Throwable t) : within(glassbox..*) && (execution(* *(..)) || adviceexecution()) {
//            System.err.println("Exiting "+thisJoinPointStaticPart+" with "+t);
//        }
//        declare precedence: *, TraceExceptionConversion;
//    }
}
