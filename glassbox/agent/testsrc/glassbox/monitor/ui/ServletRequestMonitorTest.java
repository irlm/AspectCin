/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 29, 2005
 */
package glassbox.monitor.ui;

import glassbox.analysis.OperationAnalyzer;
import glassbox.analysis.api.OperationSummary;
import glassbox.config.GlassboxInitializer;
import glassbox.simulator.ui.MockServletOk;
import glassbox.simulator.ui.MockServletSlowDatabaseCall;
import glassbox.test.GlassboxIntegrationTest;
import glassbox.test.TimingTestHelper;
import glassbox.track.OperationTrackerImpl;
import glassbox.util.jmx.JmxManagement;

import java.util.Iterator;
import java.util.Set;


/**
 * Integration tests for servlet request monitor.
 * 
 * @author Ron Bodkin
 */
public class ServletRequestMonitorTest extends GlassboxIntegrationTest {
    private OperationTrackerImpl tracker;

    public void testEmptyMonitor() {
        Set operations = tracker.getOperations();
        assertTrue(operations==null || operations.size()==0);
    }

    public void testSingleFastRequestMonitor() throws Exception {
        MockServletOk servlet1 = new MockServletOk();
        MockServletSlowDatabaseCall servlet2 = new MockServletSlowDatabaseCall();
        servlet1.forceDoGet();
        
        Set operations = tracker.getOperations();
        assertEquals(1, operations.size());
        assertEquals(0, tracker.getProblemOperations().size());
		
		OperationSummary desc = findSummary(operations, servlet1);		
		assertNotNull(desc); // in the list
		assertEquals(1, desc.getCount());
		assertEquals("javax.servlet.Servlet", desc.getOperation().getOperationType());
    }
    
	public void testMultipleFastRequestMonitor() throws Exception {
        MockServletOk servlet1 = new MockServletOk();
        MockServletSlowDatabaseCall servlet2 = new MockServletSlowDatabaseCall();
        servlet2.setDelay(0);
        
        servlet1.forceDoGet();
        
        assertNull(findSummary(tracker.getOperations(), servlet2)); 
        
        servlet2.forceDoPost();
        servlet1.forceDoGet();
        
        Set operations = tracker.getOperations();
        assertEquals(2, operations.size());
        java.util.Set slow = tracker.getProblemOperations();
        assertEquals((slow.isEmpty() ? "" : "Unexpected slow operation: "+slow.iterator().next()), 0, slow.size());
		OperationSummary desc = findSummary(operations, servlet1);		
		assertNotNull(desc); // in the list
		assertEquals(2, desc.getCount());

		desc = findSummary(operations, servlet2);		
		assertNotNull(desc); // in the list
		assertEquals(1, desc.getCount());
    }
    
    public void testMultipleInstanceRequestMonitor() throws Exception {
        MockServletOk servlet1 = new MockServletOk();
        servlet1.forceDoGet();
        servlet1.forceDoGet();
        MockServletOk servlet1b = new MockServletOk();
        servlet1b.forceDoGet();
        
        Set operations = tracker.getOperations();
        assertEquals(1, operations.size());
    }

    private MockServletOk doOneSlowRequest() throws Exception {
        MockServletOk servlet1 = new MockServletOk();
        servlet1.setDelay(TimingTestHelper.GUARANTEED_SLOW);
        servlet1.forceDoPost();
        return servlet1;
    }
    
    public void testOneSlowRequestMonitor() throws Exception {
        MockServletOk servlet1 = doOneSlowRequest();
        MockServletSlowDatabaseCall servlet2 = new MockServletSlowDatabaseCall();
        servlet2.forceDoGet();
        
        Set operations = tracker.getProblemOperations();
        assertEquals(1, operations.size());
    }
    
    //broken because we really have to collapse Servlet requests into a single node whether matching 1, 2, or 3 methods in the call tree...
    //commented out: this is a known limitation for 2.0
    public void testWarmUpSlowFastRequestMonitorGetVsPost() throws Exception {
        glassbox.track.api.OperationPerfStatsImpl.setInitialSkipCount(1);
        MockServletOk servlet1 = doOneSlowRequest();
        Set operations = tracker.getOperations();
        assertEquals(0, operations.size());
        
        new MockServletOk().forceDoGet();
        operations = tracker.getOperations();
        OperationSummary desc = findSummary(operations, servlet1);      
        assertNotNull(desc); // in the list
        assertEquals(1, desc.getCount());
        assertEquals(0, tracker.getProblemOperations().size());        
    }        
    
    public void testWarmUpSlowFastRequestMonitor() throws Exception {
        glassbox.track.api.OperationPerfStatsImpl.setInitialSkipCount(1);
        MockServletOk servlet1 = doOneSlowRequest();
        Set operations = tracker.getOperations();
        assertEquals(0, operations.size());
        
        new MockServletOk().forceDoPost();
        operations = tracker.getOperations();
        OperationSummary desc = findSummary(operations, servlet1);      
        assertNotNull(desc); // in the list
        assertEquals(1, desc.getCount());
        assertEquals(0, tracker.getProblemOperations().size());        
    }        

    int getMinSlowFrequency() {
        return (int)(1./tracker.getOperationAnalyzer().getMinimumSlowFrac()+1e-9);
    }
    
    public void testOccasionallySlowRequestMonitor() throws Exception {
        int limit = getMinSlowFrequency() - 1;
        MockServletOk servlet1 = new MockServletOk();
        for (int i=0; i<limit; i++) {
            servlet1.forceDoGet();
        }
        servlet1.setDelay(TimingTestHelper.GUARANTEED_SLOW);
        servlet1.forceDoGet();
        Set operations = tracker.getProblemOperations();
        assertEquals(1, operations.size());        
    }
    
    public void testRarelySlowRequestMonitor() throws Exception {
        int limit = getMinSlowFrequency();
        
        MockServletOk servlet1 = new MockServletOk();
        for (int i=0; i<limit; i++) {
            servlet1.forceDoGet();
        }
        servlet1.setDelay(TimingTestHelper.GUARANTEED_SLOW);
        servlet1.forceDoGet();
        Set operations = tracker.getProblemOperations();
        assertEquals(0, operations.size());        
    }
    
//    public void testSlowDatabaseRequestMonitor() {
//        MockServlet servlet = new MockServletOk();
               
//        Mock mockConnection = TestJdbcMonitor.setUpMockConnection(this);
        
        //Mock getCall = virtualMock("call doGet(..)");
        //Stub callDatabase = new CustomStub() { /* calls a dummy JDBC object */ };
        //getCall.expects(once()).will(callDatabase());
//    }
    
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

    public void setUp() {
		super.setUp();
        GlassboxInitializer.start(false);
        // this is easier than replacing the object tree in the config file
        // is there REALLY no way just to override config.xml settings in another file?
        OperationAnalyzer analyzer = TimingTestHelper.getTestAnalyzer();
		tracker.setOperationAnalyzer(analyzer);
        glassbox.track.api.OperationPerfStatsImpl.setInitialSkipCount(0);
    }

    public void tearDown() {
		super.tearDown();
        // somewhat tricky: clear the set of operations between test cases...
        tracker.clear();
    }
	
    public void setTracker(OperationTrackerImpl trackerImpl) {
		tracker = trackerImpl;
	}
}
