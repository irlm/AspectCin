/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 30, 2005
 */
package glassbox.monitor.ui;

import glassbox.monitor.AbstractMonitor;
import glassbox.monitor.MonitoredType;
import glassbox.response.Response;
import glassbox.track.api.*;

import java.io.File;
import java.lang.reflect.Method;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.jsp.HttpJspPage;

// note: when AspectJ moves to allowing weaving javax..* types we can rewrite this class to use declare parents on the Servlet API types to implement our own interfaces
// and then use a direct invocation syntax on these
/**
 * 
 * @author Ron Bodkin
 */
public aspect ServletRequestMonitor extends AbstractMonitor {
    
    public static final int NEVER_RECORD_REQUESTS = 0;
    public static final int RECORD_NON_SSL_REQUESTS = 1;
    public static final int ALWAYS_RECORD_REQUESTS = 2;    
    private int recordDataPolicy = RECORD_NON_SSL_REQUESTS;
    
    public static final int MAX_CONTEXT_LENGTH = 40;
    
    public static final Integer LOW_PRIORITY = new Integer(2);
    public static final Integer JSP_PRIORITY = new Integer(10);
    public static final Integer SERVLET_PRIORITY = new Integer(30);    
    
    /** requestExec matches execution of servlet service and the various do* methods 
     * on http servlet*/
    // we can't just weave into HttpServlet.service: AspectJ 1.5.2 and earlier (at least) don't allow weaving into javax..*
    public pointcut servletRequestExec() :
        within(HttpServlet+) && !within(HttpJspPage+) && (execution(* HttpServlet.do*(..)) || execution(* HttpServlet.service(..))) &&
        !within(javax..*);

    /** jspServiceExec matches jspService on an HttpJspPage. */        
    public pointcut jspServiceExec() : 
        within(HttpJspPage+) && execution(public void _jspService(..));

    public pointcut servletFilterExec() :
        within(Filter+) && execution(* doFilter(..));
    
    public pointcut servletInit() :
        within(GenericServlet+) && execution(* GenericServlet.init());
    
    public pointcut servletInitCfg(Object config) :
        within(Servlet+) && execution(* Servlet.init(ServletConfig+)) && args(config);

    public pointcut servletContextInit() :
        within(ServletContextListener+) && execution(* contextInitialized(..));
    
    public pointcut servletRequestInit() :
        within(javax.servlet.ServletRequestListener+) && execution(* requestInitialized(..));
    
    // Web services dispatchers and JSP dispatchers are considered low priority - name the operation based on what they invoke!
    public static interface DispatcherServlet extends MonitoredType /*don't manage logging for this*/ {}
        
    declare parents: HttpServlet+ && (*..JAXRPCServlet || *..WSServlet || *..AxisServlet || *..JspServlet || *..JSPServlet) implements DispatcherServlet;

    protected pointcut monitorEnd() : servletRequestExec() || jspServiceExec() || servletFilterExec() || servletInit() || servletInitCfg(*);
    
    protected pointcut topLevelPoint() : monitorEnd();

    before(Object servlet, Object request) : servletRequestExec() && this(servlet) && args(request, *) {
        Integer priority = SERVLET_PRIORITY;
        if (servlet instanceof DispatcherServlet) {
            priority = LOW_PRIORITY;
        }
        setApplication(servlet, request);
        beginRequest(getServletKey(servlet), priority, request);
    }
    
    // this is a pretty narrowly focused case of tracking exception handling directly in a servlet...
    // see also HandlerTracking...
    // at least exceptions handled at top-level are often "interesting"
    // another option would be to have a generic failure detection strategy whenever an exception is handled in processing ...
    public pointcut servletHandler() : 
        handler(*) /*&& args(t)*/ && within(HttpServlet+) && !within(HttpJspPage+) && 
        (withincode(* HttpServlet.do*(..)) || withincode(* HttpServlet.service(..)) || withincode(* GenericServlet.init(..)) || withincode(* Servlet.init(..)));

    // work around AspectJ bug - args(t) won't match..
    before() : servletHandler() {
        Throwable t = (Throwable)(thisJoinPoint.getArgs()[0]); 
//        System.err.println("handler matched at "+thisJoinPoint.getSourceLocation().getWithinType().getName()+": "+thisJoinPoint.getSourceLocation().getLine()+", from args it's "+t);
        recordException(responseFactory.getLastResponse(), t);        
    }

    // shows AspectJ bug - args(t) doesn't match.. in SOME cases
//    before(Throwable t) :  servletHandler() && args(t) {
//        System.err.println("handler matched exception "+t+" at "+thisJoinPoint.getSourceLocation().getWithinType().getName()+": "+thisJoinPoint.getSourceLocation().getLine());
//    }

    before(Object jsp, Object request) : jspServiceExec() && this(jsp) && args(request, *) {
        setApplication(jsp, request);
        beginRequest(getJspKey(jsp, request), JSP_PRIORITY, request);        
    }

    // this should really be a non-operation that "wraps" the whole request for timing...
    // we could track the FilterConfig values...
    before(Object request) : servletFilterExec() && args(request, ..) {
        setApplicationFromRequest(request);        
        beginRequest(getServletRequestKey(request), LOW_PRIORITY, request);
    }
    
    // track servlet initialization... but it's not really an operation at all
    // instead, if it breaks then the application itself is probably poisoned...
    // current issue: we flag it as slow when multi-second initialization is fairly normal
    
    before(Object servlet) : servletInit() && this(servlet) {
        setApplication(servlet, null);
        Response response = begin(getServletKey(servlet), SERVLET_PRIORITY);
        Request userRequest = new DefaultRequest(new DefaultCallDescription(servlet.getClass().getName(),
                "initialization", CallDescription.DISPATCH), "initialization", null);
        response.set(Response.REQUEST, userRequest);
        response.set("background", "true");
    }

    before(Object servlet, Object config) : servletInitCfg(config) && this(servlet) {
        setApplicationFromConfig(config);
        Response response = begin(getServletKey(servlet), SERVLET_PRIORITY);
        Request userRequest = new DefaultRequest(new DefaultCallDescription(servlet.getClass().getName(),
                "initialization", CallDescription.DISPATCH), "initialization", null);
        response.set(Response.REQUEST, userRequest);
        response.set("background", "true");
    }
    
    // track errors in servlet listeners... these should also use the same background processing flag as above...
    after(Object sre) throwing (Throwable t) : servletRequestInit() && args(sre) {       
        setApplicationFromContext(invoke(sre, "getServletContext"));
        Object req  = invoke(sre, "getServletRequest");
        Response response = beginRequest(getServletRequestKey(req), LOW_PRIORITY, req);
        endException(response, t);
    }

    after(Object listener, Object contextEvent) throwing (Throwable t) : servletContextInit() && args(contextEvent) && this(listener) {
        Object servletContext = invoke(contextEvent, "getServletContext");
        setApplicationFromContext(servletContext);
        Response response = begin(operationFactory.makeOperation("javax.servlet.ServletContextListener",
                "initialization"), LOW_PRIORITY);
        Request userRequest = new DefaultRequest(new DefaultCallDescription(getContextName(servletContext),
                "initialization", CallDescription.DISPATCH), "initialization", null);
        response.set(Response.REQUEST, userRequest);
        endException(response, t);
    }
   
    public void setRecordDataPolicy(int policy) {
        recordDataPolicy = policy;
    }
    
    public int getRecordDataPolicy() {
        return recordDataPolicy;
    }
    
    private Response beginRequest(OperationDescription operation, Integer priority, Object request) {
        if (shouldRecordData(request)) {
            return begin(operation, priority, makeRequest(operation, request));
        } else {
            return begin(operation, priority);
        }
    }
    
    
    protected OperationDescription getServletKey(Object servlet) {
        return operationFactory.makeOperation("javax.servlet.Servlet", servlet.getClass().getName()); 
    }
    
    protected OperationDescription getJspKey(Object jsp, Object request) {
        String servletPath = (String)invoke(request, "getServletPath");
        if (servletPath != null) {
            if (servletPath.length()==0 || servletPath.charAt(0) != '/') {
                // the full name is the context path to the jsp, e.g., /index.jsp or /foo/index.jsp, the short name is index.jsp 
                servletPath = "/"+servletPath;
            }
            return operationFactory.makeOperation("javax.servlet.jsp.HttpJspPage", servletPath);
        }
        return operationFactory.makeOperation("javax.servlet.jsp.HttpJspPage", jsp.getClass().getName()); 
    }
    
    // there's no reliable way to find what servlet would respond to a servlet request except by executing it...
    // we could try deferring setting the key til it executes to see what servlet actually responds    
    protected OperationDescription getServletRequestKey(Object request) {
        String path;
        Object sPath = invoke(request, "getServletPath");
        if (sPath != null) {
            path = sPath.toString();
        } else {
            path = (String)invoke(request, "getRemoteHost");
        }
        return operationFactory.makeOperation("HTTPRequest", path);
    }    

    private boolean shouldRecordData(Object request) {
        if (recordDataPolicy == ALWAYS_RECORD_REQUESTS) {
            return true;
        } else if (recordDataPolicy == NEVER_RECORD_REQUESTS) {
            return false;
        }
        return Boolean.FALSE.equals(invoke(request, "isSecure"));
    }

    //we copy parameters for problem operations lazily if there's a problem, rather than exposing them eagerly...
    //is this the best approach?
    protected Request makeRequest(CallDescription aCall, Object request) {
        StringBuffer url = (StringBuffer)invoke(request, "getRequestURL");
        if (url == null) {
            // this can happen when there's an internal request dispatch contained within another
            return new DefaultRequest(aCall, "", null);
        }
        String queryStr = (String)invoke(request, "getQueryString");
        Object param;
        if (queryStr != null) {
            url.append('?');
            url.append(queryStr);
            param = null;
        } else {
            param = invoke(request, "getParameterMap");
        }
        return new DefaultRequest(aCall, url.toString(), param);
    }

//    static aspect Trace {
//        before(HttpServlet servlet) : adviceexecution() && within(ServletRequestMonitor) && !within(Trace) && cflow(HttpServletPointcuts.requestExec(servlet, *, *)) {
//            System.err.println("At "+thisJoinPoint.toLongString()+" at "+thisJoinPoint.getSourceLocation().getLine()+" for "+servlet.getClass()+" for "+Thread.currentThread().getId());
//        }
//    }

    public static String setApplication(Object servlet, Object request) {
        Object config = invoke(servlet, "getServletConfig");
        if (config != null) {
            String application = setApplicationFromConfig(config);
            if (application != null) {
                return application;
            }
        }
        if (request != null) {
            return setApplicationFromRequest(request);
        }
        return null;
    }
    
    public static String setApplicationFromConfig(Object servletConfig) {
        return setApplicationFromContext(invoke(servletConfig, "getServletContext"));
    }        
    
    public static String setApplicationFromContext(Object servletContext) {
        String contextName = getContextName(servletContext);
        if (contextName != null) {
            responseFactory.setApplication(contextName);
        }
        return contextName;
    }
    
    protected static String setApplicationFromRequest(Object request) {
        Object session = invoke(request, "getSession", new Class[] { Boolean.TYPE }, new Object[] { Boolean.FALSE });

        if (session != null) {
            Object servletContext = invoke(session, "getServletContext");
            String contextName = setApplicationFromContext(servletContext);
            if (contextName != null) {
                return contextName;
            }
        }
        String contextName = getSuffixTail((String)invoke(request, "getContextPath"), "/");
        // improvement: cache mappings of application context paths -> contexts so we can use the same one...
        responseFactory.setApplication(contextName);
        return contextName;
    }
    
    public static String getContextName(Object servletContext) {
        if (servletContext == null) {
            return null;
        }
        String contextName = (String)invoke(servletContext, "getServletContextName");
        if (contextName == null || contextName.equals("")) {
            contextName = getSuffixTail((String)invoke(servletContext, "getRealPath", "/"), File.separator);
        }

        if (contextName == null) {
            return null;
        }
        
        if (contextName.length() > MAX_CONTEXT_LENGTH) {
            contextName = contextName.substring(0, MAX_CONTEXT_LENGTH-3)+"...";
        }
        return contextName;
    }

    public static String getSuffixTail(String path, String separator) {
        if (path == null) {
            return null;
        }
        
        if (path.endsWith(separator)) {
            path = path.substring(0, path.length()-1);
        }                
        int lastPos = path.lastIndexOf(separator);
        if (lastPos >= 0) {
            return path.substring(lastPos+1);                    
        }
        return "/"; // bizarre case: handle a root path, e.g., a webapp deployed exploded at c:\ or /
    }
    
    public String getLayer() {
        return Response.UI_CONTROLLER;
    }

    /**
     * Reflective call to a public method on object. Returns null if not present, not accessible, etc.
     */
    private static Object invoke(Object called, String method) {
        return invoke(called, method, null, null);
    }
    
    /**
     * Reflective call to a public method on object. Returns null if not present, not accessible, etc.
     */
    private static Object invoke(Object called, String method, Object arg) {
        return invoke(called, method, new Class[] { arg.getClass() }, new Object[] { arg });
    }
    
    /**
     * Reflective call to a public method on object. Returns null if not present, not accessible, etc.
     */
    private static Object invoke(Object called, String methodName, Class argTypes[], Object argz[]) {
        try {
            // could cache...
            Method method = called.getClass().getMethod(methodName, argTypes);
            return method.invoke(called, argz);        
        } catch (Throwable t) {
            return null;
        }
    }
}
