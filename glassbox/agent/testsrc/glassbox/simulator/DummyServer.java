/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Apr 7, 2005
 */
package glassbox.simulator;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import com.google.gwt.user.client.rpc.RemoteService;

import glassbox.config.GlassboxInitializer;
import glassbox.simulator.ui.*;
import glassbox.test.MockServlet;
import glassbox.util.timing.Clock;
import glassbox.util.timing.api.TimeConversion;

/**
 * Simulated web application which gets instrumented by agent/monitor, just like any other real web app.
 * Client can then view wide variety of simulated app problems.
 * 
 * @author Ron Bodkin
 */
public class DummyServer {
    private Random random = new Random();
    
    public static void main(String arg[]) {
        
        final double maxDelay = arg.length<1 ? 2. : Double.parseDouble(arg[0]);
        int nThreads = arg.length<2 ? 4 : Integer.parseInt(arg[1]);
        System.out.println("Dummy server running with delay of up to "+maxDelay+" per request using "+nThreads+" threads " );
        
        GlassboxInitializer.start(true);
        
        int maxIterations = Integer.MAX_VALUE;
        if (arg.length>=3) {
            maxIterations = Integer.parseInt(arg[2]);
            System.out.println("Running "+maxIterations+" iterations.");
        }
        final int finalMaxIts = maxIterations;
        
        Thread threads[] = new Thread[nThreads];
        for (int i=0; i<nThreads; i++) {
            threads[i] =
                new Thread() {
                    public void run() {
                        new DummyServer().run(maxDelay, finalMaxIts);
                    }
                };
            threads[i].start();
        }
        
        for (int i=0; i<nThreads; i++) {
            threads[i].join();
        }
        System.out.println("Finished iterations: waiting");
        for(;;) {
            Thread.sleep(1000000);
        }
    }
    
    public void run(double maxDelay, int maxIterations) {
        MockServletOk servletOk = new MockServletOk();
        MockServletOk servletOk2 = new MockServletOk() {
        	private static final long serialVersionUID = 1L;
        }; // must be diff class to have own row
        MockServletOk servletOk3 = new MockServletOk() {
            private static final long serialVersionUID = 1L;

        };
        MockServletSlowDatabaseCall servletSlowDbCall = new MockServletSlowDatabaseCall();
        MockServletSlowDatabaseOverall servletSlowDbOverall = new MockServletSlowDatabaseOverall();
        MockServletSlowConnection servletSlowConn = new MockServletSlowConnection();
        MockServletStatementFailure servletStmtFail = new MockServletStatementFailure();
        MockServletCpuHog servletCpuHog = new MockServletCpuHog();  // The CPU one
        MockServletMultipleIssues servletMultiple = new MockServletMultipleIssues(maxDelay);
        MockServletThreadContention[] lockers = new MockServletThreadContention[3];
        MockServletConnectionFailure[] lockers2 = new MockServletConnectionFailure[3];
        MockServletSlowRemoteCall servletSlowRemoteCall = new MockServletSlowRemoteCall();
        MockServletSlowRemoteCallOverall servletSlowRemoteCallOverall = new MockServletSlowRemoteCallOverall();
        MockServletFailingRemoteCall servletFailingRemoteCall = new MockServletFailingRemoteCall();
        MockServletExcessWork servletExcessWork = new MockServletExcessWork();
        MockDelayingServlet servletOccasionalSpikes = new MockDelayingServlet();
        final MockDispatchAction dispatchAction = new MockDispatchAction();
        MockServlet dispatchServlet = new MockServlet();
        dispatchServlet.setRunnable(new Runnable() { public void run() { dispatchAction.doBatch(null, null, null, null); } });
        MockServlet failedDispatchServlet = new MockServlet();
        failedDispatchServlet.setRunnable(new Runnable() { public void run() { dispatchAction.doBatch(null, null, null, null); throw new RuntimeException("test fail dispatch"); } });
        MockSlowRemoteService slowMethodGwtService = new MockSlowRemoteService();

        final MockMultiActionDelegate multiActionDelegate = new MockMultiActionDelegate();
        final MultiActionController multiActionController = new MultiActionController();
        multiActionController.setDelegate(multiActionDelegate);

        MockServlet doQuery = makeMultiAction("doDynamicQuery", multiActionController, 3);
        MockServlet doNestedFailure = makeMultiAction("doNestedFailure", multiActionController);
        MockServlet doHandledFailure = makeMultiAction("doHandledFailure", multiActionController);

        Thread[] threads = new Thread[lockers.length];
        for (int i=0; i<lockers.length; i++) {            
            lockers[i] = new MockServletThreadContention(0L);
        }
        Thread[] threads2 = new Thread[lockers2.length];
        for (int i=0; i<lockers2.length; i++) {            
            lockers2[i] = new MockServletConnectionFailure();
        }
        
        MultithreadedRunner runner = new MultithreadedRunner();
        MultithreadedRunner runner2 = new MultithreadedRunner();
        
        // it'd be useful to CONFIGURE the simulator instead of coding it to run everything every time...
        for (int count=0;count<maxIterations;count++) {            
            slowMethodGwtService.setDelay(genDelay(maxDelay));
            slowMethodGwtService.run();
//            servletSlowDbCall.setDelay(genDelay(maxDelay));
//            servletStmtFail.forceDoPost();
//            servletOk.forceDoGet();
//            Thread.sleep(100);
        }
        Thread.sleep(100000);
        
        final MockServletHangForever servletHangForever = new MockServletHangForever(); // this runs forever, see agent handles
        new Thread() {
            public void run() {
                servletHangForever.forceDoPost();
            }
        };//.start();

        boolean runMt = false;
        for (int count=0;count<maxIterations;count++) {            
            multiActionDelegate.setDelay(genDelay(maxDelay));
            doQuery.forceDoGet();
            
            servletOk.forceDoGet(); //no delay
            servletSlowDbCall.setDelay(genDelay(maxDelay));
            servletSlowDbCall.forceDoPost();
            runMt = (count % 7 == 0);
            if (runMt) {
                for (int i=0; i<lockers.length; i++) {
                    final MockServletThreadContention locker = lockers[i];
                    final MockServletConnectionFailure locker2 = lockers2[i];
                    locker.setDelay(genDelay(maxDelay/lockers.length));
                    locker2.setDelay(genDelay(maxDelay/lockers.length));
                    threads[i] = new Thread(new Runnable() {
                        public void run() {
                            locker.forceDoGet();
                        }
                     });
                    threads2[i] = new Thread(new Runnable() {
                        public void run() {
                            locker2.forceDoPost();
                        }
                    });
                }
                runner2.run(threads2);
            }
            
            servletOk2.forceDoGet(); //no delay
            servletOk3.forceDoGet(); //no delay            
            servletSlowDbOverall.setDelay(genDelay(maxDelay));
            servletSlowDbOverall.forceDoPost();
            servletMultiple.setDelay(genDelay(maxDelay));
            servletMultiple.forceDoPost();
            servletSlowConn.setDelay(genDelay(maxDelay));
            servletSlowConn.forceDoPost();
            servletStmtFail.setDelay(genDelay(maxDelay));
           	servletStmtFail.forceDoGet();
            if (count % 10 == 0) {
                // minimize annoying cpu hogging
                servletCpuHog.setDelay(genDelay(maxDelay));
                servletCpuHog.forceDoPost();
            }
            servletSlowRemoteCall.setDelay(genDelay(maxDelay));
            servletSlowRemoteCall.forceDoPost();
            servletSlowRemoteCallOverall.setDelay(genDelay(maxDelay));
            servletSlowRemoteCallOverall.forceDoPost();
            servletFailingRemoteCall.setDelay(genDelay(maxDelay));
            servletFailingRemoteCall.forceDoGet();
            servletExcessWork.setDelay(genDelay(maxDelay));
            servletExcessWork.forceDoPost();
            if (random.nextDouble() < 0.1) {
                servletOccasionalSpikes.setDelay((long)(maxDelay * TimeConversion.NANOSECONDS_PER_SECOND));
            } else {
                servletOccasionalSpikes.setDelay(0);
            }
            servletOccasionalSpikes.forceDoGet();
            dispatchAction.setDelay(genDelay(maxDelay));
            dispatchServlet.forceDoGet();
            try { failedDispatchServlet.forceDoPost(); } catch (Throwable t) {}
            try { doNestedFailure.forceDoPost(); } catch (Throwable t) {}
            doHandledFailure.forceDoPost();
            slowMethodGwtService.setDelay(genDelay(maxDelay));
            slowMethodGwtService.run();
            if (runMt) {
                runner.run(threads);
            }
        }               
    }

    private long genDelay(double maxDelay) {
        return (long)(random.nextDouble()*maxDelay * TimeConversion.NANOSECONDS_PER_SECOND);
    }
    
    private static aspect SoftenExceptions {
        declare soft: Exception: execution(* *(..)) && within(DummyServer);
    }
    
    private MockServlet makeMultiAction(String methodName, final MultiActionController controller) {
        return makeMultiAction(methodName, controller, 1);
    }
    
    private MockServlet makeMultiAction(String methodName, final MultiActionController controller, final int count) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/"+methodName);
        request.setMethod("GET");
        return new MockServlet(
                new Runnable() { 
                    public void run() {
                        for (int i=0; i<count; i++) {
                            controller.handleRequest(request, null); 
                        }
                    }
                });
    }
    
    private class MockSlowRemoteService implements RemoteService {
        MockComponent mc = new MockComponent();
        
        public void run() { mc.doSlow(random.nextInt(500), "test String"); }
        
        public void setDelay(long delay) {
            mc.setDelay(delay);
        }
    }
}
