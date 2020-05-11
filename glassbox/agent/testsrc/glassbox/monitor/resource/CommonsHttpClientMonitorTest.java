/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.resource;
import glassbox.analysis.api.OperationSummary;
import glassbox.simulator.resource.http.MockCommonsHttpClient;
import glassbox.simulator.resource.http.MockCommonsHttpMethod;
import glassbox.test.TestMonitor;
import glassbox.test.TimingTestHelper;
import glassbox.test.ajmock.VirtualMockObjectTestCase;
import glassbox.track.OperationTrackerImpl;
import glassbox.track.ThreadStats;
import glassbox.track.api.PerfStats;
import glassbox.track.api.StatisticsTypeImpl;
import glassbox.util.jmx.JmxManagement;

import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

public class CommonsHttpClientMonitorTest extends VirtualMockObjectTestCase {
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
    
        Iterator preit = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertFalse(preit.hasNext());
        
	    MockCommonsHttpMethod http1 = new MockCommonsHttpMethod();
        http1.execute();
   
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        stats.summarizeOperation(null);
        assertEquals(1, stats.getCount());
        assertEquals(0, stats.getSlowCount());
        assertEquals(0, stats.getSlowSingleOperationCount());
        assertFalse(it.hasNext());        
      
//      TODO: Add the PerfStats/Problem tests and assertions.
        
//        Set operations = tracker.getOperations();
//        assertEquals(1, operations.size());
//        assertEquals(0, tracker.getSlowOperations().size());
        
//        OperationSummary desc = findSummary(operations, http1);      
//        assertNotNull(desc); // in the list
//        assertEquals(1, desc.summaryStats().getCount());
//        assertEquals("org.apache.commons.httpclient.HttpMethod", desc.operationDescription().getOperationType());
    }
    
    public void testMultipleFastMethodMonitor() throws Exception {
        MockCommonsHttpMethod http1 = new MockCommonsHttpMethod();
	    MockCommonsHttpMethod http2 = new MockCommonsHttpMethod();
        
        http1.execute();
        http2.execute();
        http1.execute();
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
                      
//        Set operations = tracker.getOperations();
//        assertEquals(2, operations.size());
//        assertEquals(0, tracker.getSlowOperations().size());
//        OperationSummary desc = findSummary(operations, http1);      
//        assertNotNull(desc); // in the list
//        assertEquals(2, desc.summaryStats().getCount());
//
//        desc = findSummary(operations, http2);       
//        assertNotNull(desc); // in the list
//        assertEquals(1, desc.summaryStats().getCount());
    }
    
    public void testMultipleInstanceMethodMonitor() throws Exception {
        MockCommonsHttpMethod http1 = new MockCommonsHttpMethod();
        http1.execute();
        http1.execute();
        MockCommonsHttpMethod http1b = new MockCommonsHttpMethod();
        http1b.execute();
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        
        //Set operations = tracker.getOperations();
        //assertEquals(1, operations.size());
    }

    private MockCommonsHttpMethod doOneSlowMethod() throws Exception {
        MockCommonsHttpMethod http1 = new MockCommonsHttpMethod();
        http1.setDelay(TimingTestHelper.GUARANTEED_SLOW);
        http1.execute();
        return http1;
    }
    
    public void testOneSlowMethodMonitor() throws Exception {
        MockCommonsHttpMethod http1 = doOneSlowMethod();
        http1.setDelay(TimingTestHelper.SLOW_THRESHOLD/2);
        http1.execute();
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        stats.summarizeOperation(null);
        assertEquals(2, stats.getCount());
        assertEquals(1, stats.getSlowCount());
        assertEquals(1, stats.getSlowSingleOperationCount());
        
    }
    
    public void testWarmUpSlowFastMethodMonitor() throws Exception {
        glassbox.track.api.OperationPerfStatsImpl.setInitialSkipCount(1);
        MockCommonsHttpMethod http1 = doOneSlowMethod();
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        //Set operations = tracker.getOperations();
        //OperationSummary desc = findSummary(operations, http1);      
        //assertNotNull(desc); // in the list
       // assertEquals(0, desc.summaryStats().getCount());
        
        new MockCommonsHttpMethod().execute();
        //operations = tracker.getOperations();
        //desc = findSummary(operations, http1);      
        //assertNotNull(desc); // in the list
        //assertEquals(1, desc.summaryStats().getCount());
        //assertEquals(0, tracker.getSlowOperations().size());        
    }        
    
    public void testOccasionallySlowMethodMonitor() throws Exception {
        MockCommonsHttpMethod http1 = new MockCommonsHttpMethod();
        for (int i=0; i<8; i++) {
            http1.execute();
        }
        http1.setDelay(TimingTestHelper.GUARANTEED_SLOW);
        http1.execute();
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        
        //Set operations = tracker.getSlowOperations();
        //assertEquals(1, operations.size());        
    }
    
    public void testRarelySlowMethodMonitor() throws Exception {
        MockCommonsHttpMethod http1 = new MockCommonsHttpMethod();
        for (int i=0; i<9; i++) {
            http1.execute();
        }
        http1.setDelay(TimingTestHelper.GUARANTEED_SLOW);
        http1.execute();
     
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        //Set operations = tracker.getSlowOperations();
        //assertEquals(0, operations.size());        
    }
    
    
    public void testEmptyClient() {
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertFalse(it.hasNext());
    }
    
    public void testSingleFastClientMonitor() throws Exception {
    
        MockCommonsHttpClient http1 = new MockCommonsHttpClient();
        http1.executeMethod();
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        
        //Set operations = tracker.getOperations();
        //assertEquals(1, operations.size());
        //assertEquals(0, tracker.getSlowOperations().size());
        
        //OperationSummary desc = findSummary(operations, http1);      
       // assertNotNull(desc); // in the list
        //assertEquals(1, desc.summaryStats().getCount());
        //assertEquals("org.apache.commons.httpclient.HttpMethod", desc.operationDescription().getOperationType());
    }
    
    public void testMultipleFastClientMonitor() throws Exception {
        MockCommonsHttpClient http1 = new MockCommonsHttpClient();
        MockCommonsHttpClient http2 = new MockCommonsHttpClient();
        
        http1.executeMethod();
        http2.executeMethod();
        http1.executeMethod();
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        
        //Set operations = tracker.getOperations();
        //assertEquals(2, operations.size());
        //assertEquals(0, tracker.getSlowOperations().size());
        //OperationSummary desc = findSummary(operations, http1);      
        //assertNotNull(desc); // in the list
        //assertEquals(2, desc.summaryStats().getCount());

       // desc = findSummary(operations, http2);       
        //assertNotNull(desc); // in the list
        //assertEquals(1, desc.summaryStats().getCount());
    }
    
    public void testMultipleInstanceClientMonitor() throws Exception {
        MockCommonsHttpClient http1 = new MockCommonsHttpClient();
        http1.executeMethod();
        http1.executeMethod();
        MockCommonsHttpClient http1b = new MockCommonsHttpClient();
        http1b.executeMethod();
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        
        //Set operations = tracker.getOperations();
        //assertEquals(1, operations.size());
    }

    private MockCommonsHttpClient doOneSlowClient() throws Exception {
        MockCommonsHttpClient http1 = new MockCommonsHttpClient();
        http1.setDelay(TimingTestHelper.GUARANTEED_SLOW);
        http1.executeMethod();
        return http1;
    }
    
    public void testOneSlowClientMonitor() throws Exception {
        MockCommonsHttpClient http1 = doOneSlowClient();
        http1.executeMethod();
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        
        //Set operations = tracker.getSlowOperations();
        //assertEquals(1, operations.size());
    }
    
    public void testWarmUpSlowFastClientMonitor() throws Exception {
        glassbox.track.api.OperationPerfStatsImpl.setInitialSkipCount(1);
        MockCommonsHttpClient http1 = doOneSlowClient();
        //Set operations = tracker.getOperations();
        //OperationSummary desc = findSummary(operations, http1);      
        //assertNotNull(desc); // in the list
        //assertEquals(0, desc.summaryStats().getCount());
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        
        new MockCommonsHttpClient().executeMethod();
        //operations = tracker.getOperations();
        //desc = findSummary(operations, http1);      
        //assertNotNull(desc); // in the list
        //assertEquals(1, desc.summaryStats().getCount());
        //assertEquals(0, tracker.getSlowOperations().size());        
    }        
    
    public void testOccasionallySlowClientMonitor() throws Exception {
        MockCommonsHttpClient http1 = new MockCommonsHttpClient();
        for (int i=0; i<8; i++) {
            http1.executeMethod();
        }
        http1.setDelay(TimingTestHelper.GUARANTEED_SLOW);
        http1.executeMethod();
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        
        //Set operations = tracker.getSlowOperations();
        //assertEquals(1, operations.size());        
    }
    
    public void testRarelySlowClientMonitor() throws Exception {
        MockCommonsHttpClient http1 = new MockCommonsHttpClient();
        for (int i=0; i<9; i++) {
            http1.executeMethod();
        }
        http1.setDelay(TimingTestHelper.GUARANTEED_SLOW);
        http1.executeMethod();
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.RemoteCall);
        assertTrue(it.hasNext());
        Entry entry = (Entry)it.next();
        PerfStats stats = (PerfStats)entry.getValue();
        //Set operations = tracker.getSlowOperations();
        //assertEquals(0, operations.size());        
    }
    
    OperationSummary findSummary(Set operations, Object requestObject) {
        if (operations == null) {
            return null;
        }
        for (Iterator it = operations.iterator(); it.hasNext();) {
            OperationSummary desc = (OperationSummary)it.next();
            if (desc.getOperation().getOperationName().equals(requestObject.getClass().getName())) {
                return desc;
            }
        }
        return null;
    }
}
