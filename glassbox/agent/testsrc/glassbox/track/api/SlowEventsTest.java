/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import glassbox.util.timing.api.CpuUsageTrackingInfoImpl;
import glassbox.util.timing.api.NoCpuUsageTrackingInfoImpl;
import junit.framework.TestCase;

public class SlowEventsTest extends TestCase {

    private static final UsageTrackingInfo usageInOrder[] = {
        new CpuUsageTrackingInfoImpl(0L, 0L, 0L),
        new CpuUsageTrackingInfoImpl(100L, 11L, 11L),
        new CpuUsageTrackingInfoImpl(101L, 0L, 0L),
        new CpuUsageTrackingInfoImpl(0x100000000L, 0x100000000L, 0x100000000L)
    };
    private Request slowInOrder[]; 
    
    public void setUp() {
        slowInOrder = new DefaultRequest[usageInOrder.length];
        for (int i=0; i<slowInOrder.length; i++) {
            slowInOrder[i] = makeRequest(""+i, usageInOrder[i]);
        }
    }
    
    private Request makeRequest(String req, UsageTrackingInfo usage) {
        return new DefaultRequest(new DefaultCallDescription("local:", req, CallDescription.OPERATION_PROCESSING), req, "slow: "+req, null, usage.getEventTime());
    }

    //should be in DESCENDING order...
    public void testCompareUnequal() {
        for (int i=1; i<slowInOrder.length; i++) {
            int delta = slowInOrder[i].compareTo(slowInOrder[i-1]);
            assertTrue("wrong order at "+i, delta<0);
            
            delta = slowInOrder[i-1].compareTo(slowInOrder[i]);
            assertTrue("wrong order at "+i, delta>0);
        }
    }
    
    public void testCompareEquiv() {
        assertEquals("equals not comparing same?", 0, usageInOrder[2].compareTo(new CpuUsageTrackingInfoImpl(101L, 0L, 0L)));
        assertEquals("equiv not comparing same?", 0, usageInOrder[1].compareTo(new NoCpuUsageTrackingInfoImpl(100L)));
    }
    
    public void testNoTrim() {
        assertEquals("3", slowInOrder[3].getRequestString());
        assertTrue(slowInOrder[3].getParameterString().startsWith("slow: 3"));
     
        String longStr = makeTestString(200);
        Request ev = makeRequest(longStr, usageInOrder[3]);
        assertTrue(ev.getRequestString().startsWith(longStr));
        assertTrue(ev.getParameterString().indexOf(longStr)>=0);    
    }
    
    private String makeTestString(int len) {
        StringBuffer longStr = new StringBuffer();
        for (int pos=0; pos<len; pos++) {
            longStr.append('a'+pos%26);
        }
        return longStr.toString();
    }
    
    public void testTrim() {
        String longStr = makeTestString(1000);
        Request ev = makeRequest(longStr, usageInOrder[3]);
        assertTrue(longStr.length() > ev.getDescription().length());
        assertTrue(longStr.length() > ev.getParameterString().length());
    }

}
