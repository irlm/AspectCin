/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.lang.reflect.Field;
import java.util.Comparator;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DefaultRequestTest extends MockObjectTestCase {

    private Mock callMock1;
    private CallDescription call1;
    private DefaultRequest req1;
    
    public void setUp() {
        callMock1 = mock(CallDescription.class);
        call1 = (CallDescription)callMock1.proxy();
        
        req1 = new DefaultRequest(call1, "one", "paramStr");
        req1.setElapsedTime(10L);
        req1.setLastTime(200L);
    }
    
    public void testSimpleOrdering() {        
        DefaultRequest req2 = new DefaultRequest(call1, "two", "paramStr");
        req2.setElapsedTime(100L);
        req2.setLastTime(20L);
        
        // should be descending order: so the smaller time is considered bigger 
        assertEquals(1, req1.compareTo(req2));

        assertEquals(-1, Request.END_TIME_COMPARATOR.compare(req1, req2));
    }

    public void testEquivalent() {
        callMock1.stubs().method("getSummary").will(returnValue("call"));
        
        DefaultRequest req1prime = new DefaultRequest(call1, "one", "paramStr");
        req1prime.setElapsedTime(10L);
        req1prime.setLastTime(200L);
        
        assertEquals(0, Request.END_TIME_COMPARATOR.compare(req1, req1prime));
        assertEquals(0, req1.compareTo(req1));
    }
    
    public void testCopy() {
        callMock1.stubs().method("getSummary").will(returnValue("call"));
        
        DefaultRequest req1prime = (DefaultRequest)req1.copy();
        
        assertEquals(0, Request.END_TIME_COMPARATOR.compare(req1, req1prime));
        assertEquals(0, req1.compareTo(req1));
        
        for (Class clazz=req1.getClass(); clazz!=null; clazz=clazz.getSuperclass()) {
            Field fields[] = clazz.getDeclaredFields();
            for (int i=0; i<fields.length; i++) {
                fields[i].setAccessible(true);
                assertEquals(fields[i].get(req1), fields[i].get(req1prime));
            }
        }
        
    }
    
    public void testTieBreakNaming() {
        callMock1.stubs().method("getSummary").will(returnValue("call"));
        
        DefaultRequest req2 = new DefaultRequest(call1, "two", "paramStr");
        req2.setElapsedTime(10L);
        req2.setLastTime(200L);
        
        assertFalse(0 == req1.compareTo(req2));
        assertFalse(0 == Request.END_TIME_COMPARATOR.compare(req1, req2));
    }        

    public static aspect AssertTotalOrderingComparatorInvariants {
        pointcut inTest() : within(DefaultRequestTest) && !within(AssertTotalOrderingComparatorInvariants);
        
        after(Comparable objA, Comparable objB) returning (int val) : 
                call(int compareTo(*)) && target(objA) && args(objB) && inTest() {
            assertEquals(-val, objB.compareTo(objA));
            assertEquals((val==0), objA.equals(objB));
            assertEquals((val==0), objB.equals(objA));
        }
        
        after(Comparator comparator, Comparable objA, Comparable objB) returning (int val) : 
                call(int compare(*, *)) && target(comparator) && args(objA, objB) && inTest() {
            assertEquals(-val, comparator.compare(objB, objA));
            assertEquals((val==0), objA.equals(objB));
            assertEquals((val==0), objB.equals(objA));
        }
    }
}
