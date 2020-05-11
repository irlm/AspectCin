/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import glassbox.track.api.ThreadState;
import junit.framework.TestCase;

public class ThreadStateTest extends TestCase {
    private ThreadState state;
    public void setUp() {
        state = new ThreadState();
    }
    
    public void testSetBadLockName() {
        try {
            state.setLockName("X");
            fail("illegal lock not rejected");
        } catch (IllegalArgumentException e) {
            // success
        }
    }
    
    public void testSetNullLockName() {
        state.setLockName(null);
        assertEquals(null, state.getLockName());
    }
    
    public void testSetObjectLockName() {
        state.setLockName("java.lang.Object@12347");
        assertEquals("java.lang.Object", state.getLockName());
    }
    
    public void testSetClassLockName() {
        String lockName = "java.lang.Class@12347"; 
        state.setLockName(lockName);
        assertEquals(lockName, state.getLockName());
    }
    
    private static final long serialVersionUID = 1;
}
