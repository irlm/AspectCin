/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.resource;

import java.util.Iterator;
import java.util.Map.Entry;

import glassbox.simulator.resource.mail.MockJavaMailTransport;
import glassbox.test.ajmock.VirtualMockObjectTestCase;
import glassbox.track.OperationTrackerImpl;
import glassbox.track.ThreadStats;
import glassbox.track.api.PerfStats;
import glassbox.track.api.StatisticsTypeImpl;

import glassbox.test.TestMonitor;

public class MailMonitorTest extends VirtualMockObjectTestCase {

    private OperationTrackerImpl tracker;
    private ThreadStats threadStats;

    public void setUp() {
        threadStats = TestMonitor.setUpStatsForUofW(null);        
    }
    
    public void testEmptyMonitor() {
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertFalse(it.hasNext());
    }
    
    public void testSingleFastMethodMonitor() throws Exception {
        
        MockJavaMailTransport mail1 = new MockJavaMailTransport();
        mail1.sendMessage(null, null);
   
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        assertFalse(it.hasNext());        
        stats.summarizeOperation(null);
        
        assertEquals(1, stats.getCount());
        assertEquals(0, stats.getSlowCount());
        assertEquals(0, stats.getSlowSingleOperationCount());
        
     //TODO: Add the PerfStats/Problem testsabd   
        
        
        
    }
    
}
