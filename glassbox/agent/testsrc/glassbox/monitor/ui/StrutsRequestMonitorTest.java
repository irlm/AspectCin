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
import glassbox.monitor.AbstractMonitor;
import glassbox.monitor.OperationFactory;
import glassbox.monitor.ui.StrutsRequestUnitTest.DummyStrutsAction;
import glassbox.test.TestMonitor;
import glassbox.test.ajmock.VirtualMockObjectTestCase;
import glassbox.track.OperationTrackerImpl;
import glassbox.track.ThreadStats;
import glassbox.track.api.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionServlet;
import org.springframework.mock.web.MockHttpServletRequest;


/**
 * Integration tests for struts request monitor.
 * 
 * @author Ron Bodkin
 */
public class StrutsRequestMonitorTest extends VirtualMockObjectTestCase {
    private ThreadStats threadStats;
    private OperationTrackerImpl tracker;

    public void setUp() {
        threadStats = TestMonitor.setUpStatsForUofW(StrutsRequestMonitor.aspectOf());
        tracker = new OperationTrackerImpl();
        tracker.setRegistry(threadStats.getRegistry());
        tracker.setOperationAnalyzer(new OperationAnalyzer()); // this could be a mock...
    }
    
    public void testStrutsRequestMonitor() throws Exception {
        //if our AjMock TestCase extended the CGLIB one and set it up so that the mocks are woven at load-time
        //then we'd be able to use jMock . If we did that, we would probably then want to add the ability to
        //specify the package etc.
        //heck, in that case the highly dynamic virtual mocks would be reasonable, since they'd only affect
        //this test!
        
//        Mock strutsAction1 = mock(Action.class);
//        strutsAction1.expects(once()).method("execute").will(returnValue(null));
//        ((Action)strutsAction1.proxy()).execute(null,null,null,null);
        new DummyStrutsAction().execute(null,null,null,null);
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.UiRequest);
        assertTrue(it.hasNext());
        Entry entry = (Entry)it.next();
        OperationDescription desc = (OperationDescription)entry.getKey();
        PerfStats stats = (PerfStats)entry.getValue();
        
		assertEquals(1, stats.getCount());
		assertEquals(Action.class.getName(), desc.getOperationType());
        assertEquals(DummyStrutsAction.class.getName(), desc.getOperationName());
        assertFalse(it.hasNext());        
    }
    
    public void testNotActionServletMonitor() throws Exception {
        new DummyActionServlet().service(new MockHttpServletRequest("POST", null), null);
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.UiRequest);
        assertTrue(it.hasNext());
    }

    public void testWithinActionServletMonitor() throws Exception {
        new DummyActionServlet().service(new MockHttpServletRequest("GET", null), null);
        
        Set operations = tracker.getOperations();
        assertEquals(1, operations.size());
        OperationSummary summary = (OperationSummary)operations.iterator().next();
        OperationDescription desc = summary.getOperation();
        assertEquals(Action.class.getName(), desc.getOperationType());
        assertEquals(DummyStrutsAction.class.getName(), desc.getOperationName());
        assertEquals(1, summary.getCount());
        OperationFactory operationFactory = AbstractMonitor.getOperationFactory();
        OperationDescription expected;
        try {
            operationFactory.getResponseFactory().setApplication("/");
            expected = operationFactory.makeOperation(Servlet.class, DummyActionServlet.class.getName());       
        } finally {
            operationFactory.getResponseFactory().setApplication(null);
        }
        assertEquals(expected, desc.getParent());
    }

    public void testNestedActionsMonitor() throws Exception {
        new DummyActionServlet().service(new MockHttpServletRequest("GET", null), null);
        
        Set operations = tracker.getOperations();
        assertEquals(1, operations.size());
        OperationSummary summary = (OperationSummary)operations.iterator().next();
        OperationDescription desc = summary.getOperation();
        assertEquals(Action.class.getName(), desc.getOperationType());
        assertEquals(DummyStrutsAction.class.getName(), desc.getOperationName());
        assertEquals(1, summary.getCount());
    }
    
    static class DummyActionServlet extends ActionServlet {
        
        public void service(HttpServletRequest request, HttpServletResponse response) {
            super.service(request, response);
        }
        
        public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        }
        
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            try {
                new DummyStrutsAction().execute(null,null,null,null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static final long serialVersionUID = 1;
    }
	
}
