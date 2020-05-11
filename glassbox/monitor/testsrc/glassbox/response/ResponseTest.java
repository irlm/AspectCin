/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.response;

import glassbox.test.ReaderThread;
import glassbox.util.timing.Clock;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class ResponseTest extends MockObjectTestCase {
    // better name?
    private ResponseFactory factory;
    private Mock mockClock;
    private Mock mockListener;
    
    public void setUp() {
        factory = new DefaultResponseFactory();
        mockClock = mock(Clock.class);
        factory.setClock((Clock)mockClock.proxy());
        mockListener = mock(ResponseListener.class);
        factory.addListener((ResponseListener)mockListener.proxy());
    }
    
//    public void tearDown() {        
//    }
//    
    public void testApplicationIsThreadLocal() {
        String applicationName = "testAppOne";
    
        factory.setApplication(applicationName);
        assertEquals(applicationName, factory.getApplication());
        final ResponseFactory myFactory = factory;
        assertNull((new ReaderThread() { public Object read() { return myFactory.getApplication(); } }).getValue() );       
    }
    
    public void testCreateRequest() {
        String key = "DispatcherServlet";
        final Response servlet = factory.getResponse(key);
        servlet.setLayer(Response.UI_CONTROLLER);
        servlet.set("type", "servlet");
        assertEquals(Response.UI_CONTROLLER, servlet.getLayer());
        assertNull(new ReaderThread() { public Object read() { return servlet.get(Response.UI_CONTROLLER); } }.getValue() );
        assertEquals("servlet", servlet.get("type"));
        assertEquals(key, servlet.getKey());
    }
    
    public void testTimeRequest() {
        long START_TIME = 200L;
        long END_TIME = 500L;
        // expectations are LIFO..
        mockClock.expects(once()).method("getTime").will(returnValue(END_TIME));
        mockClock.expects(once()).method("getTime").will(returnValue(START_TIME));
        
        final Response response = factory.getResponse("MondoController");
        assertEquals(Response.NOT_STARTED, response.getStatus());
        mockListener.expects(once()).method("startedResponse").with(eq(response));
        mockListener.expects(once()).method("finishedResponse").with(eq(response));
        assertEquals(null, factory.getLastResponse());
        response.start();
        assertEquals(Response.IN_PROGRESS, response.getStatus());
        assertEquals(Clock.UNDEFINED_TIME, response.getDuration());
        assertEquals(Clock.UNDEFINED_TIME, response.getEnd());
        factory.getLastResponse().complete();
        assertEquals(null, factory.getLastResponse());
        assertEquals(Response.COMPLETED, response.getStatus());
        assertEquals(START_TIME, response.getStart());
        assertEquals(END_TIME, response.getEnd());
        assertEquals(END_TIME-START_TIME, response.getDuration());
    }

    public void testStartedCompleteRequest() {
        long START_TIME = 200L;
        long END_TIME = 500L;
        // expectations are LIFO..
        mockClock.expects(once()).method("getTime").will(returnValue(START_TIME));
        
        final Response response = factory.getResponse("MondoController");
        mockListener.expects(once()).method("startedResponse").with(eq(response));
        mockListener.expects(once()).method("finishedResponse").with(eq(response));
        response.start();
        assertEquals(Response.IN_PROGRESS, response.getStatus());
        assertEquals(Clock.UNDEFINED_TIME, response.getDuration());
        assertEquals(Clock.UNDEFINED_TIME, response.getEnd());
        factory.getLastResponse().complete(END_TIME-START_TIME);
        assertEquals(null, factory.getLastResponse());
        assertEquals(Response.COMPLETED, response.getStatus());
        assertEquals(START_TIME, response.getStart());
        assertEquals(END_TIME, response.getEnd());
        assertEquals(END_TIME-START_TIME, response.getDuration());
    }

    public void testOnlyCompletedRequest() {
        long START_TIME = 200L;
        long END_TIME = 500L;
        // expectations are LIFO..
        mockClock.expects(once()).method("getTime").will(returnValue(END_TIME));
        
        final Response response = factory.getResponse("MondoController");
        mockListener.expects(once()).method("finishedResponse").with(eq(response));
        Response lastResponse = factory.getLastResponse();
        response.complete(END_TIME-START_TIME);
        assertEquals(null, lastResponse);
        assertEquals(Response.COMPLETED, response.getStatus());
        assertEquals(START_TIME, response.getStart());
        assertEquals(END_TIME, response.getEnd());
        assertEquals(END_TIME-START_TIME, response.getDuration());
    }

    public void testFailedRequest() {
        long START_TIME = 200L;
        long END_TIME = 200L;
        mockClock.expects(once()).method("getTime").will(returnValue(END_TIME));
        mockClock.expects(once()).method("getTime").will(returnValue(START_TIME));
        
        mockListener.expects(once()).method("startedResponse").with(ANYTHING);
        final Response response = factory.startResponse("FailingActiveRecord");
        mockListener.expects(once()).method("finishedResponse").with(eq(response));
        factory.getLastResponse().fail();
        assertEquals(null, factory.getLastResponse());
        assertEquals(Response.FAILED, response.getStatus());
        assertEquals(START_TIME, response.getStart());
        assertEquals(END_TIME, response.getEnd());
    }
    
    public void testInvalidEndRequest() {
        final Response response = factory.getResponse("MondoController");
        try {
            response.complete();
            fail("not legal: complete without start");
        } catch (IllegalStateException e) {
            //ok
        }        
    }
    public void testSummaryRequest() {
        long ELAPSED_TIME = 250L;
        final Response response = factory.getResponse("a");
        mockClock.expects(once()).method("getTime").will(returnValue(1000L));
        mockListener.expects(once()).method("finishedResponse").with(eq(response));
        response.complete(ELAPSED_TIME);
        assertEquals(ELAPSED_TIME, response.getDuration());
        assertEquals(1000L, response.getEnd());
        assertEquals(750L, response.getStart());
        assertEquals(Response.COMPLETED, response.getStatus());
    }
    public void testIncompleteRequest() {
        long T1 = 0L;
        long T2 = 10000L;
        long T3 = 20000L;
        final Response response = factory.getResponse("x");
        mockListener.expects(once()).method("startedResponse").with(eq(response));
        
        mockClock.expects(atLeastOnce()).method("getTime").will(
                onConsecutiveCalls(returnValue(T1), returnValue(T2), returnValue(T3)));
        response.start();
        response.update();
        assertEquals(T2-T1, response.getDuration());
        assertEquals(Clock.UNDEFINED_TIME, response.getEnd());
        assertEquals(Response.IN_PROGRESS, response.getStatus());
    }
    public void testOverlappingRequests() {
        final long TIMES[] = { 20L, 50L, 120L, 300L };
        for (int i=0; i<TIMES.length; i++) {
            mockClock.expects(once()).method("getTime").will(returnValue(TIMES[TIMES.length-i-1]));
        }
        final Response first = factory.getResponse("req");
        mockListener.expects(once()).method("startedResponse").with(eq(first));
        mockListener.expects(once()).method("finishedResponse").with(eq(first));
        first.start();
        mockListener.expects(once()).method("startedResponse").with(ANYTHING);
        final Response second = factory.startResponse("req"); // same key
        mockListener.expects(once()).method("finishedResponse").with(eq(second));
        first.complete();
        assertEquals(second,factory.getLastResponse());
        assertEquals(Clock.UNDEFINED_TIME, second.getDuration());
        second.complete();
        assertNull(factory.getLastResponse());
        assertEquals(100L, first.getDuration());
        assertEquals(250L, second.getDuration());
    }
    public void testNestedRequests() {
        final long TIMES[] = { 20L, 50L, 120L, 300L };
        for (int i=0; i<TIMES.length; i++) {
            mockClock.expects(once()).method("getTime").will(returnValue(TIMES[TIMES.length-i-1]));
        }
        mockListener.expects(once()).method("startedResponse").with(ANYTHING);
        mockListener.expects(once()).method("startedResponse").with(ANYTHING);
        final Response first = factory.startResponse("req");
        final Response second = factory.startResponse("req"); // same key
        mockListener.expects(once()).method("finishedResponse").with(eq(first));
        mockListener.expects(once()).method("finishedResponse").with(eq(second));
        second.complete();
        assertEquals(first,factory.getLastResponse());
        assertEquals(Clock.UNDEFINED_TIME, first.getDuration());
        assertNull(first.getParent());
        assertEquals(first,second.getParent());
        first.complete();
        assertNull(factory.getLastResponse());
        assertEquals(280L, first.getDuration());
        assertEquals(70L, second.getDuration());
    }
    
//  public void testDistributedRequest() {
//  byte[] testBytes = new byte[] { ... };
//  Response ejb = factory.getResponse("accountBean");
//  Correlator correlator = factory.makeCorrelator(testBytes);
//  ejb.start(correlator);
//  ejb.complete();
//}
//
//public void testWizard() {
//  page1.start();
//  page1.complete();
//  page2.start();
//  page2.complete();
//  page3.start();
//  page3.complete();
//  operation_completed();
//}
//
//public void testBpmFlow() {
//  process_complete();
//}
    
//    public void testSuccessfulWebRequestWithKpi() {
//        //per VM globals
//        Metadata systemData = factory.getSystemData();
//        systemData.set("cluster", "cluster1");
//        systemData.set("pid", 0x234a);
//        systemData.set("server.vendor", "Apache Tomcat");
//        systemData.set("server.version", "5.5.862");
//        systemData.set("java.vendor", "Harmony");
//        systemData.set("java.version", "1.5.0_06");
//        
//        applicationName = servlet.getContext().getApplicationName();
//        
//        Response servlet = factory.getResponse("DispatcherServlet");
//        servlet.start();
//        servlet.set("type", "servlet");//setCategory("servlet");
//        servlet.set("application", applicationName);
//        servlet.set("layer", "ui.controller");
//        //version?
//
//        Response controller = factory.startResponse("MultiActionController.acceptLead");
//        controller.set("layer", "ui.controller");
//        // in this thread...
//        assertEquals(servlet, controller.getParent());
//
//        Response connection = factory.startResponse("connection");
//        connection.set("layer", "resource.database");
//        connection.set("url", "jdbc:hsqldb:localhost:2344//petstore");
//        
//        factory.recordSnapshot(stackTrace, lock);
//        connection.complete();
//        
//        // correlation ID?
//        Response prepare = factory.startResponse("prepare");
//        prepare.set("statement", "SELECT * from items where id=?");
//        prepare.complete();
//
//        Response execute = factory.startResponse("execute");
//        execute.set("statement", "SELECT * from items where id=?"); // correlation from request to request is NOT done here
//        execute.set("values", new Object[] { new Integer(0x1234)} );
//        execute.complete();
//
//        factory.getCounter("conversions").increment();
//        
//        controller.complete();
//        Response jsp = factory.startResponse("render_jsp");
//        jsp.set("layer", "ui.view");
//        jsp.complete();
//        
//        servlet.complete();
//        //ends uofw
//    }
//    
//    public void testFailedDatabaseWebRequest() {
//        Response execute = factory.startResponse("execute");
//        execute.set("statement", "SELECT * from items where id=?"); // correlation from request to request is NOT done here
//        execute.set("values", new Object[] { new Integer(0x1234)} );
//        execute.set("exception", translatedThrowable);
//        execute.fail();        
//    }
//    
//    
//    public void testSummaryRecording() {
//        Response foo = factory.getResponse("foo");
//        foo.set("batch.size", 25);
//        foo.record(200000);
//        foo.fail(250);
//    }
//    
//    public void testThreading() {
//        
//    }
//    
//    public void testEventCounter() {
//        //factory.
//    }

}
