/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.ui;

import glassbox.monitor.MonitorResponseTestCase;
import glassbox.response.Response;
import glassbox.test.MockServlet;
import glassbox.track.api.OperationDescription;
import glassbox.track.api.Request;

import java.io.*;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.HttpJspPage;

import org.springframework.mock.web.*;

// the test case is Serializable so we can have a reference to it in our inner servlets that get serialized to record responses
public class ServletRequestUnitTest extends MonitorResponseTestCase {
    private ServletRequestMonitor monitor = ServletRequestMonitor.aspectOf();
    
    protected void setUp() throws Exception {
        super.setUp();
        assertEquals(ServletRequestMonitor.RECORD_NON_SSL_REQUESTS, monitor.getRecordDataPolicy());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        monitor.setRecordDataPolicy(ServletRequestMonitor.RECORD_NON_SSL_REQUESTS);
    }
    
    public void testNullConfig() {
        MockServlet servlet = new MockServlet();
        servlet.forceDoGet();
        assertServletOperations(servlet, 2);
        assertEquals("", servlet.REQUEST.getContextPath());
        assertEquals("/", responseFactory.getApplication());
    }

    public void testNullContext() {
        MockServlet servlet = new MockServlet();
        servlet.init(new MockServletConfig(null));
        MockHttpServletRequest request = makeServletRequest();
        request.setContextPath("/testApp");
        servlet.service(request, new MockHttpServletResponse());
        assertServletOperations(servlet, 2);
        assertEquals(request.getContextPath().substring(1), responseFactory.getApplication());
    }
    
    public void testNullContextName() {
        String path = "glassbox";
        MockServlet servlet = makeMockServlet(path, null);
        
        servlet.forceDoGet();
        assertServletOperations(servlet, 2);        
        assertEquals(path, responseFactory.getApplication());
    }

    public void testNullContextNameRoot() {
        MockServlet servlet = new MockServlet();
        MockServletContext mockServletContext = new MockServletContext("") {
            public String getServletContextName() { return null; }
            public String getRealPath(String path) {
                return new File(path).getAbsolutePath();
            }
        };            
        servlet.init(new MockServletConfig(mockServletContext));

        servlet.forceDoGet();
        assertServletOperations(servlet, 2);        
        assertEquals("/", responseFactory.getApplication());
    }
    
    public void testNullContextNameNullRealPath() {
        MockServlet servlet = new MockServlet();
        MockServletContext mockServletContext = new MockServletContext("") {
            public String getServletContextName() { return null; }
            public String getRealPath(String path) {
                return null;
            }
        };            
        servlet.init(new MockServletConfig(mockServletContext));

        servlet.forceDoGet();
        assertServletOperations(servlet, 2);
        assertEquals("", servlet.REQUEST.getContextPath());
        assertEquals("/", responseFactory.getApplication());
    }
    
    public void testShortContextName() {
        String name = "shortName";
        MockServlet servlet = makeMockServlet("glassbox", name);

        MockHttpServletRequest request = makeServletRequest();
        servlet.service(request, new MockHttpServletResponse());
        assertServletOperations(servlet, 2);
        assertEquals(name, responseFactory.getApplication());
    }

    public void testLongContextName() {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<=ServletRequestMonitor.MAX_CONTEXT_LENGTH; i++) {
            buf.append((char)('a'+(i%26)));
        }
        String name = buf.toString();
        MockServlet servlet = makeMockServlet("glassbox", name);

        MockHttpServletRequest request = makeServletRequest();
        servlet.service(request, new MockHttpServletResponse());
        assertServletOperations(servlet, 2);
        assertEquals(name.substring(0, ServletRequestMonitor.MAX_CONTEXT_LENGTH-3)+"...", responseFactory.getApplication());
    }
    
    public void testTwoDispatch() {
        MockServlet servlet = makeMockServlet("glassbox", "foo");
        servlet.forceDoPost();
        assertServletOperations(servlet, 4);
    }

    public void testTwoDispatchErr() {
        MockServlet servlet = new MockServlet();
        servlet.setRunnable(new Runnable() { public void run() { throw new IllegalArgumentException("boom"); } });
        try {
            servlet.forceDoPost();
            fail("should error");
        } catch (IllegalArgumentException _) { ; } // ok
        assertServletOperations(servlet, 4);
        assertFailure(IllegalArgumentException.class, 2);
        assertFailure(IllegalArgumentException.class, 3);
    }

    public void testServiceOverride() {
        MockServlet servlet = new MockServlet() {
            public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                    IOException {
                run();
            }

        };
        servlet.forceDoGet(); // just calls service...
        assertServletOperations(servlet, 2);
    }

    public void testGetServletParams() {
        MockServlet servlet = makeMockServlet("glassbox", "foo");
        MockHttpServletRequest request = makeServletRequest();
        String queryStr = "a=3&b=one&b=two"; 
        request.setQueryString(queryStr);
        servlet.service(request, new MockHttpServletResponse());

        assertServletOperations(servlet, 2);        
        Response response = (Response) (listener.responses.get(1));
        Request requestDesc = (Request)response.get(Response.REQUEST);
        assertEquals(request.getRequestURL().toString()+"?"+request.getQueryString(), requestDesc.getRequestString());
        assertEquals("", requestDesc.getParameterString());
    }
    
    public void testSslServletParams() {
        MockServlet servlet = makeMockServlet("glassbox", "foo");
        MockHttpServletRequest request = makeServletRequest();
        String queryStr = "a=3&b=one&b=two"; 
        request.setQueryString(queryStr);
        request.setSecure(true);
        servlet.service(request, new MockHttpServletResponse());

        assertServletOperations(servlet, 2);        
        Response response = (Response) (listener.responses.get(1));
        assertNull(response.get(Response.REQUEST));
    }
    
    public void testAlwaysRecordServletParams() {
        monitor.setRecordDataPolicy(ServletRequestMonitor.ALWAYS_RECORD_REQUESTS);
        MockServlet servlet = makeMockServlet("glassbox", "foo");
        MockHttpServletRequest request = makeServletRequest();
        String queryStr = "a=3"; 
        request.setQueryString(queryStr);
        request.setSecure(true);
        servlet.service(request, new MockHttpServletResponse());

        assertServletOperations(servlet, 2);        
        Response response = (Response) (listener.responses.get(1));
        Request requestDesc = (Request)response.get(Response.REQUEST);
        assertEquals(request.getRequestURL().toString()+"?"+request.getQueryString(), requestDesc.getRequestString());
    }
    
    public void testDontRecordParams() {
        monitor.setRecordDataPolicy(ServletRequestMonitor.NEVER_RECORD_REQUESTS);
        MockServlet servlet = makeMockServlet("glassbox", "foo");
        MockHttpServletRequest request = makeServletRequest();
        String queryStr = "b=one&b=two"; 
        request.setQueryString(queryStr);
        servlet.service(request, new MockHttpServletResponse());

        assertServletOperations(servlet, 2);        
        Response response = (Response) (listener.responses.get(1));
        assertNull(response.get(Response.REQUEST));
    }
    
    public void testPostServletParams() {
        MockServlet servlet = makeMockServlet("glassbox", "foo");
        MockHttpServletRequest request = makeServletRequest();
        request.setMethod("POST");
        String simple = "simple";
        String[] compound = new String[] { "11", "15" };
        request.addParameter("one", simple); 
        request.addParameter("two", compound);
        servlet.service(request, new MockHttpServletResponse());
        
        assertServletOperations(servlet, 4);        
        Response response = (Response) (listener.responses.get(3));
        Request requestDesc = (Request)response.get(Response.REQUEST);
        assertEquals("one=simple, two=11, 15", requestDesc.getParameterString());
    }
    
    public void testInit() {
        MockServlet servlet = new MockServlet() {
            public void init() {
                super.init();
            }
        };
        initMockServlet(servlet, "test", "contextName");
        assertEquals(2, listener.responses.size());
        assertEquals("contextName", responseFactory.getApplication());
 
    }

    public void testInitCfg() {
        MockServlet servlet = new MockServlet() {
            public void init(ServletConfig config) {
                super.init(config);
            }
        };
        initMockServlet(servlet, "testB", "contextNameB");
        assertEquals(2, listener.responses.size());
        assertEquals("contextNameB", responseFactory.getApplication());
    }

    public void testInitFailure() {
        HttpServlet servlet = new MockServlet() {
            public void init() throws ServletException {
                throw new ServletException("Can't init");
            }
        };

        try {
            servlet.init(null);
            fail("should have thrown");
        } catch (ServletException e) {
            ;
        } // ok
        assertServletFailure(servlet);
        assertNull(responseFactory.getApplication());
    }

    public void testInitFailureConfig() {
        HttpServlet servlet = new MockServlet() {
            public void init(ServletConfig config) throws ServletException {
                throw new ServletException("Can't init");
            }
        };
        try {
            servlet.init(null);
            fail("should have thrown");
        } catch (ServletException e) {
            ;
        } // ok
        assertServletFailure(servlet);
    }

    private static class MockJspPage extends MockServlet implements HttpJspPage {
        public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            _jspService(request, response);
        }

        public void _jspService(HttpServletRequest request, HttpServletResponse response) throws ServletException {
            run();
        }

        public void jspDestroy() {
        }

        public void jspInit() {
        }
    }
    
    public void testJspServiceExec() {
        MockJspPage jsp = new MockJspPage(); 
        MockHttpServletRequest request = makeServletRequest();
        String JSP = "/test.jsp";
        request.setServletPath(JSP);
        jsp.service(request, new MockHttpServletResponse());
        assertJspOperations(JSP, 2);
    }

    public void testNullRequestUrlJsp() {
        // this can happen when there's an internal request dispatch contained within another
        MockJspPage jsp = new MockJspPage(); 
        MockHttpServletRequest request = new MockHttpServletRequest() {
            public StringBuffer getRequestURL() {
                return null;
            }
        };
        request.setRequestURI(null);
        request.setServletPath(null);
        jsp.service(request, new MockHttpServletResponse());
        assertJspOperations(jsp.getClass().getName(), 2);
    }
        
    public void testEmptyRequestUrlJsp() {
        // this can happen when there's an internal request dispatch contained within another
        MockJspPage jsp = new MockJspPage(); 
        MockHttpServletRequest request = new MockHttpServletRequest();
        jsp.service(request, new MockHttpServletResponse());
        assertJspOperations("/", 2);
    }
    
    public void testJspServiceExecFail() {
        MockJspPage jsp = new MockJspPage() { 
            public void _jspService(HttpServletRequest request, HttpServletResponse response) throws ServletException {
                throw new ServletException("Boom");
            }   
        };
        MockHttpServletRequest request = makeServletRequest();
        String JSP = "/bill knight_cars.jsp";
        request.setServletPath(JSP);
        try {
            jsp.service(request, new MockHttpServletResponse());
            fail("should error");
        } catch (ServletException se) {
            ;
        } // ok
        assertJspOperations(JSP, 2);
        assertFailure(ServletException.class, 1);
    }

    public void testFilter() {
        MockServletContext mockContext = new MockServletContext("glassbox");
        SerializableMockHttpServletRequest request = new SerializableMockHttpServletRequest(mockContext);
        request.setContextPath("/"); // inconsistent... shows we don't get the servletContext without the session        
        testFilter("/glassbox/servlet.do", request);
        assertEquals("/", responseFactory.getApplication());
    }
    
    public void testNullPathFilter() {
        testFilter(null, makeServletRequest());
        assertEquals("/", responseFactory.getApplication());
    }
    
    public void testSessionFilter() {
        MockServletContext mockContext = new MockServletContext();
        MockHttpServletRequest request = makeServletRequest();
        MockHttpSession session = new MockHttpSession(mockContext);
        request.setSession(session);
        testFilter(null, request);
        assertEquals(mockContext.getServletContextName(), responseFactory.getApplication());
    }    

    protected void testFilter(String path, MockHttpServletRequest request) {
        
        Filter filter = new Filter() {

            public void destroy() {
            }

            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
            }

            public void init(FilterConfig config) throws ServletException {
            }

        };
        filter.init(null);
        request.setServletPath(path);
        filter.doFilter(request, new MockHttpServletResponse(), null);
        
        assertEquals(2, listener.responses.size());
        
        Response response = (Response) (listener.responses.get(1));
        OperationDescription operation = (OperationDescription) response.getKey();
        assertNotNull(operation);
        assertEquals("HTTPRequest", operation.getOperationType());
        Object expected = (path!=null ? path : request.getRemoteHost()); 
        assertEquals(expected, operation.getOperationName());
        assertEquals(ServletRequestMonitor.LOW_PRIORITY, response.get(Response.OPERATION_PRIORITY));
    }


    public void testRequestListener() {
        String path = "/ctx/servlet.do";
        final MockHttpServletRequest request = makeServletRequest();
        request.setServletPath(path);
        ServletRequestEvent sre = new ServletRequestEvent(new MockServletContext(), request);
        
        ServletRequestListener requestListener = new ServletRequestListener() {
            public void requestDestroyed(ServletRequestEvent sre) {
            }

            public void requestInitialized(ServletRequestEvent sre) {
                throw new RuntimeException("failed");                
            }            
        };
        try {
            requestListener.requestInitialized(sre);
            fail("shouldn't succeed");
        } catch (RuntimeException e) { ; } //ok
        
        assertEquals(2, listener.responses.size());
        assertFailure(RuntimeException.class, 1);
        
        Response response = (Response) (listener.responses.get(1));
        OperationDescription operation = (OperationDescription) response.getKey();
        assertNotNull(operation);
        assertEquals("HTTPRequest", operation.getOperationType());
        assertEquals(path, operation.getOperationName());
        assertEquals(ServletRequestMonitor.LOW_PRIORITY, response.get(Response.OPERATION_PRIORITY));
    }

    public void testContextListener() {
        ServletContextEvent contextEvent = new ServletContextEvent(new MockServletContext());
        
        ServletContextListener contextListener = new ServletContextListener () {
            public void contextDestroyed(ServletContextEvent sce) {}

            public void contextInitialized(ServletContextEvent sce) {
                throw new IllegalStateException("test err");
            }            
        };
        
        try {
            contextListener.contextInitialized(contextEvent);
            fail("shouldn't succeed");
        } catch (IllegalStateException e) { ; } //ok
        
        assertEquals(2, listener.responses.size());
        assertFailure(IllegalStateException.class, 1);
        
        Response response = (Response) (listener.responses.get(1));
        OperationDescription operation = (OperationDescription) response.getKey();
        assertNotNull(operation);
        assertEquals(ServletContextListener.class.getName(), operation.getOperationType());
        assertEquals("initialization", operation.getOperationName());
        assertEquals(ServletRequestMonitor.LOW_PRIORITY, response.get(Response.OPERATION_PRIORITY));
    }

    private void assertJspOperations(String path, int n) {
        assertEquals(n, listener.responses.size());
        for (int i = 0; i < n; i++) {
            Response response = (Response) (listener.responses.get(i));
            OperationDescription operation = (OperationDescription) response.getKey();
            assertNotNull(operation);
            assertEquals(HttpJspPage.class.getName(), operation.getOperationType());
            assertEquals(path, operation.getOperationName());
            assertEquals(ServletRequestMonitor.JSP_PRIORITY, response.get(Response.OPERATION_PRIORITY));
        }
    }

    private void assertServletOperations(Servlet servlet, int n) {
        assertEquals(n, listener.responses.size());
        for (int i = 0; i < n; i++) {
            Response response = (Response) (listener.responses.get(i));
            OperationDescription operation = (OperationDescription) response.getKey();
            assertNotNull(operation);
            assertEquals(Servlet.class.getName(), operation.getOperationType());
            assertEquals(servlet.getClass().getName(), operation.getOperationName());
            assertEquals(ServletRequestMonitor.SERVLET_PRIORITY, response.get(Response.OPERATION_PRIORITY));
        }
    }

    private void assertServletFailure(Servlet servlet) {
        assertServletOperations(servlet, 2);
        assertFailure(ServletException.class, 1);

        Response end = (Response) (listener.responses.get(1));
        Request userRequest = (Request) end.get(Response.REQUEST);
        assertTrue("Unexpected request: " + userRequest, userRequest.getDescription().indexOf("initialization") != -1);
    }

    public static class SerializableMockHttpServletRequest extends MockHttpServletRequest implements Serializable {
        public SerializableMockHttpServletRequest(MockServletContext context) {
            super(context);
            setMethod("GET");
        }
        
        public SerializableMockHttpServletRequest() {
            setMethod("GET");
        }
    }
    
    public SerializableMockHttpServletRequest makeServletRequest() {
        return new SerializableMockHttpServletRequest();
    }
    
    private MockServletContext makeServletContext() {
        return new MockServletContext() {};
    }

    private MockServlet makeMockServlet(String path, final String contextName) {
        MockServlet servlet = new MockServlet();
        return initMockServlet(servlet, path, contextName);
    }
    
    private MockServlet initMockServlet(MockServlet servlet, String path, final String contextName) {
        MockServletConfig config = createMockServletConfig(servlet, path, contextName);
        servlet.init(config);
        return servlet;
    }
    
    private MockServletConfig createMockServletConfig(MockServlet servlet, String path, final String contextName) {
        MockServletContext mockServletContext = new MockServletContext(path) {
            public String getServletContextName() { return contextName; }
        };            
        return new MockServletConfig(mockServletContext);
    }
    
}
