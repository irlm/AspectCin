/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.agent;

import glassbox.agent.api.NotSerializable;
import glassbox.util.logging.api.LogManagement;

import org.aspectj.lang.SoftException;

import junit.framework.TestCase;

public class ErrorContainmentTest extends TestCase {
	static int logCount;
    static int recursiveThrows;
	public void setUp() {
		logCount = 0;
        recursiveThrows = 0;
	}
	public void testBefore() {
		hook1();
		assertEquals(1, logCount);
	}
	public void testAround() {
		hook2();
		hook2();
		assertEquals(1, logCount);
	}
	public void testMany() {
		for (int i=0; i<1000; i++) {
			hook4();
		}
		assertEquals(1, logCount);
		hook4();
		assertEquals(2, logCount);
	}
	public void testAroundInt() {
		try {
			int y = hook3();
			fail("swallowed exception");
		} catch (RuntimeException e) {
			//success
			assertEquals(0, logCount);
		}
	}
    
    public void testLoggingError() {
        hook5();
        assertEquals(1, recursiveThrows);
    }
    
	private void hook1() {}
	private void hook2() {}
	private int hook3() { return 0; }
	private void hook4() {}
    private void hook5() {}
	
	static aspect ErrorMockAspect implements NotSerializable {
		pointcut scope() : call(* ErrorContainmentTest.*(..)) && within(ErrorContainmentTest);
		
		before() : scope() && call(* hook1()) {
			throw new RuntimeException("rte"); 
		}
        before() : execution(* ErrorContainmentTest.hook5()) {
            throw new RuntimeException("rte"); 
        }
		// around/pass through
		void around()  : scope() && call(* hook2()) {
			throw new Error("foo"); 
		}
		after() : scope() && call(* hook4()) {
			throw new SoftException(null); 
		}
        static aspect Around {
            int around()  : scope() && call(int hook3()) {
                throw new RuntimeException("not caught");
            }
        }
        pointcut logError(): call(* logError(..)) && within(ErrorContainment) ;
        pointcut logErrorInTest() : logError() && cflow(execution(* test*(..)) && within(ErrorContainmentTest));
		before() : logErrorInTest() {
			logCount++;
		}
        before() : adviceexecution() && within(LogManagement) && cflow(logError()) && cflow(execution(* testLoggingError(..)) && within(ErrorContainmentTest)) {
            recursiveThrows++;
            if (recursiveThrows<2) {
                throw new RuntimeException("recursive logging failure");
            }
        }
	}
}
