/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import glassbox.track.api.*;
import junit.framework.TestCase;


public class DatabaseCallDescriptionTest extends TestCase {

    private CallDescription descriptionA;
    private CallDescription descriptionAprime;
    private CallDescription descriptionB;
    private DefaultCallDescription descriptionX;
    private CallDescription descriptionY;
    private static final String db1 = "jdbc://fooo";

    public void setUp() {
        String db2 = "ds://bar";
        
        descriptionA = new DefaultCallDescription(db1, "select * from big", CallDescription.DATABASE_STATEMENT);
        descriptionAprime = new DefaultCallDescription(db1, "select * from big", CallDescription.DATABASE_STATEMENT);
        descriptionB = new DefaultCallDescription(db1, "delete from big", CallDescription.DATABASE_STATEMENT);
        descriptionX = new DefaultCallDescription(db1, "select * from big", CallDescription.DATABASE_STATEMENT);
        descriptionX.setThreadState(new ThreadState());
        descriptionY = new DefaultCallDescription(db2, "select * from big", CallDescription.DATABASE_STATEMENT);
    }
    
    public void testEquals() {
        assertEquals("select * from big", descriptionA.getCallKey());
        assertTrue(descriptionA.equals(descriptionA));
        assertTrue(descriptionA.equals(descriptionAprime));
        assertTrue(descriptionAprime.equals(descriptionA));
        assertEquals(descriptionA.hashCode(), descriptionAprime.hashCode());
        assertFalse(descriptionA.equals(null));
        assertFalse(descriptionA.equals(descriptionB));
        assertFalse(descriptionB.equals(descriptionA));
        assertFalse(descriptionX.equals(descriptionA));
        assertFalse(descriptionY.equals(descriptionA));
        
        CallDescription connectionCall = new DefaultCallDescription(db1, null, CallDescription.DATABASE_CONNECTION);
        assertFalse(descriptionA.equals(connectionCall));
        assertFalse(connectionCall.equals(descriptionA));
    }
    
    public void testAllowNullCtor() {
        CallDescription dc1 = new DefaultCallDescription(null, null, CallDescription.DATABASE_CONNECTION);
        CallDescription dc2 = new DefaultCallDescription(null, null, CallDescription.DATABASE_CONNECTION);
        assertEquals(dc1, dc2);
        assertEquals(dc1.hashCode(), dc2.hashCode());
        assertNotNull(dc1.toString());
    }

    public void testToString() {
        String a = descriptionA.toString();
        String b = descriptionAprime.toString();
        assertTrue(!a.equals(""));
        assertTrue(a.equals(b));
    }    
    
}
