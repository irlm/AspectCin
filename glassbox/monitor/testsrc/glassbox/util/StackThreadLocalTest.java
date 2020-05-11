/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util;
import glassbox.test.ReaderThread;

import java.util.EmptyStackException;

import junit.framework.TestCase;


public class StackThreadLocalTest extends TestCase {
    private StackThreadLocal stackThreadLocal;

    protected void setUp() {
        stackThreadLocal = new StackThreadLocal();
    }
    
    public void testStack() {
        assertTrue(stackThreadLocal.isEmpty());
        stackThreadLocal.push(new Integer(1));
        stackThreadLocal.push("two");
        assertFalse(stackThreadLocal.isEmpty());
        assertEquals("two", stackThreadLocal.peek());
        assertEquals("two", stackThreadLocal.pop());
        assertEquals(new Integer(1), stackThreadLocal.pop());
        assertEquals(null, stackThreadLocal.peek());
    }
    
    public void testRemove() {
        stackThreadLocal.push("z2");
        assertTrue(stackThreadLocal.remove("z"+"2"));
        assertNull(stackThreadLocal.peek());
    }
    
    public void testRemoveMissing() {
        assertFalse(stackThreadLocal.remove("not existing"));
    }

    public void testPopEmpty() {
        try {
            stackThreadLocal.pop();
            fail("pop empty should throw");
        } catch (EmptyStackException e) {
            //ok
        }
    }

    public void testThreadLocality() {
        stackThreadLocal.push(new Object());
        assertNotNull(stackThreadLocal.peek());
        assertNull(new ReaderThread() { public Object read() { return stackThreadLocal.peek(); } }.getValue() );
    }
}
