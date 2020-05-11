/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 29, 2005
 */
package glassbox.monitor.ui;

import glassbox.test.TestMonitor;
import glassbox.test.ajmock.VirtualMockObjectTestCase;
import glassbox.track.OperationTrackerImpl;
import glassbox.track.ThreadStats;
import glassbox.track.api.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.Aspects;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * Integration tests for servlet request monitor.
 * 
 * @author Ron Bodkin
 * @author Joseph Shoop
 */
public class SpringMvcRequestMonitorTest extends VirtualMockObjectTestCase {
    private OperationTrackerImpl tracker;
    private ThreadStats threadStats;

    public void setUp() {
        threadStats = TestMonitor.setUpStatsForUofW(SpringMvcRequestMonitor.aspectOf());                        
    }
    
    public void testSpringMvcRequestMonitor() throws Exception {
        
        new DummySpringMvcController().handleRequest(null,null);
        
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.UiRequest);
        assertTrue(it.hasNext());
        Entry entry = (Entry)it.next();
        OperationDescription desc = (OperationDescription)entry.getKey();
        PerfStats stats = (PerfStats)entry.getValue();
        
		assertEquals(1, stats.getCount());
		assertEquals(Controller.class.getName(), desc.getOperationType());
        assertEquals(DummySpringMvcController.class.getName(), desc.getOperationName());
        assertFalse(it.hasNext());        
    }
    
    public void testNotControllerMonitor() throws Exception {
        new DummyExcludeControllerServlet().doService(null, null);
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.UiRequest);
        assertFalse(it.hasNext());
    }

    public void testWithinControllerMonitor() throws Exception {
        new DummyControllerServlet().doService(null, null);
        Iterator it = threadStats.getRegistry().getEntriesForType(StatisticsTypeImpl.UiRequest);
        assertTrue(it.hasNext());
        Entry entry = (Entry)it.next();
        OperationDescription desc = (OperationDescription)entry.getKey();
        PerfStats stats = (PerfStats)entry.getValue();
        
        assertEquals(1, stats.getCount());
        assertEquals(Controller.class.getName(), desc.getOperationType());
        assertEquals(DummySpringMvcController.class.getName(), desc.getOperationName());
        assertFalse(it.hasNext());        
    }

    private static class DummySpringMvcController implements Controller {
        
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            return null;
        }
    }

    private static class DummyControllerServlet extends org.springframework.web.servlet.DispatcherServlet {
        
        public void doService(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            post(request, response);
        }
        protected void post(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            try {
                new DummySpringMvcController().handleRequest(request, response);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        private static final long serialVersionUID = 1L;
    }
       
    
    private static class DummyExcludeControllerServlet extends org.springframework.web.servlet.DispatcherServlet {
        
        public void doService(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            post(request, response);
        }
        
        protected void post(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        }
        
        private static final long serialVersionUID = 1L;
    }
	
}
