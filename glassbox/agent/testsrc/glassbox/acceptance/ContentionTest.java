/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.acceptance;

import glassbox.agent.control.GlassboxServiceImpl;
import glassbox.analysis.api.*;
import glassbox.config.GlassboxInitializer;
import glassbox.monitor.thread.ThreadMonitor;
import glassbox.monitor.thread.ThreadMonitorIntegration;
import glassbox.simulator.MultithreadedRunner;
import glassbox.simulator.ui.LockingRunnable;
import glassbox.simulator.ui.MockServletThreadContention;
import glassbox.test.GlassboxIntegrationTest;
import glassbox.test.TimingTestHelper;
import glassbox.track.OperationTrackerImpl;
import glassbox.track.api.OperationDescription;
import glassbox.track.api.ThreadState;
import glassbox.util.DetectionUtils;
import glassbox.util.timing.api.TimeConversion;

import java.util.Iterator;
import java.util.Set;

import javax.servlet.Servlet;


public class ContentionTest extends GlassboxIntegrationTest {
    private GlassboxServiceImpl service;
    private OperationTrackerImpl tracker;

    public void setGlassboxService(GlassboxServiceImpl service) {
        this.service = service;
    }
    
    public void testSlowContendingOperations() throws Exception {
        // this test will work only on 1.5 for now...
        
        if (DetectionUtils.getJavaRuntimeVersion() < 1.5) {
            return; // not supported
        }

        glassbox.track.api.OperationPerfStatsImpl.setInitialSkipCount(0);
        ThreadMonitor monitor = ThreadMonitorIntegration.aspectOf().getThreadMonitor(); 
        assertTrue(monitor.getInterval() > 0);
        assertEquals(0, service.listProblemOperations().size());
        
        MultithreadedRunner runner = new MultithreadedRunner(); 
        Thread[] threads = runner.createContending(10, monitor.getInterval()*5);
        runner.run(threads);
        
        Set slowOperations=service.listProblemOperations();
        assertEquals(1, slowOperations.size());      
        OperationSummary summary = (OperationSummary)slowOperations.iterator().next();
        OperationDescription operation = summary.getOperation();
        assertEquals(Servlet.class.getName(), operation.getOperationType());
        assertEquals(MockServletThreadContention.class.getName(), operation.getOperationName());
        OperationAnalysis analysis = service.analyze(operation);
        
        System.err.println("interval = "+TimeConversion.convertNanosToMillis(monitor.getInterval()));
        System.err.println(analysis.getScenarioSummary());

        assertTrue("too few problems "+analysis.problems(), analysis.problems().size()>=2);
        assertTrue("too many problems "+analysis.problems(), analysis.problems().size()<4);
        
        ContentionProblem cProblem = null;
        boolean hasSlowMethod = false;
        boolean hasExcessCpu = false;
        for (Iterator it=analysis.problems().iterator(); it.hasNext();) {
            Object problem = it.next();
            if (problem instanceof ContentionProblem) {
                if (cProblem == null) {
                    cProblem = (ContentionProblem)problem;
                    continue;
                }
            } else if (problem instanceof SlowMethodProblem) {
                if (!hasSlowMethod) {                    
                    hasSlowMethod=true;
                    continue;
                }
            } if (problem instanceof ExcessCpuProblem) {
                if (!hasExcessCpu) {                
                    hasExcessCpu=true;
                    continue;
                }
            }
            fail("unexpected problem "+problem+" in "+analysis.problems());
        }
        assertTrue("No contention problem "+analysis.problems(), cProblem!=null);
        assertTrue("No slow method problem "+analysis.problems(), hasSlowMethod);
        ThreadState threadState = cProblem.getDescriptor().getThreadState();
        // JRockIt has a monitor on Objects when sleeping...
        assertTrue("unexpected lock "+threadState.getLockName()+" at "+dump(threadState.getStackTrace()), 
                threadState.getLockName().startsWith(Class.class.getName()) || threadState.getLockName().startsWith(Object.class.getName()));
        boolean matched = false;
        for (int i = 0; i < threadState.getStackTrace().length; i++) {
            StackTraceElement elt = threadState.getStackTrace()[i];
            if (elt.getMethodName().contains("run") && elt.getClassName().equals(LockingRunnable.class.getName())) {
                matched = true;
                break;
            }
        }
        assertTrue(dump(threadState.getStackTrace())+" doesn't have run method", matched);        
    }
    
    private String dump(Object[] trace) {
        StringBuffer buf = new StringBuffer("trace = ");
        for (int i = 0; i < trace.length; i++) {
            if (i>0) {
                buf.append(", ");
            }
            buf.append(trace[i]);
        }
        buf.append(")");
        return buf.toString();
    }

    public void setUp() {
        super.setUp();
        tracker.setOperationAnalyzer(TimingTestHelper.getTestAnalyzer());
        GlassboxInitializer.start(false);
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
