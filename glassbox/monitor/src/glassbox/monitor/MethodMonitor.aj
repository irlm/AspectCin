package glassbox.monitor;

import glassbox.monitor.AbstractMonitorControl.RuntimeControl;
import glassbox.monitor.ui.MvcFrameworkMonitor;
import glassbox.monitor.ui.ServletRequestMonitor;
import glassbox.response.Response;
import glassbox.track.api.OperationDescription;
import glassbox.track.api.*;
import glassbox.util.timing.api.TimeConversion;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

public abstract aspect MethodMonitor extends MvcFrameworkMonitor {
    //TODO: make this configuratble
    public long methodThreshold = 1; // 1ms is the lowest resolution we have right now...
    
    private ThreadLocal timeStackHolder = new ThreadLocal() {
        public Object initialValue() {
            return new TimeStack();
        }
    };
    
    public MethodMonitor() {
        controllerType = getClass().getName();
    }
    
    public MethodMonitor(String name, boolean enabled) {
        super(name);
        RuntimeControl.aspectOf(this).setEnabled(enabled);
    }
    
    protected pointcut monitoredPublicMethods();
    protected pointcut monitoredMethods();
    protected pointcut monitoredPoint();
    private final pointcut monitoredAll() : 
        monitoredPoint() || 
        (execution(* *(..)) || execution(new(..))) && monitoredMethods() || 
        (execution(public * *(..)) || execution(public new(..))) && monitoredPublicMethods();
    
    private pointcut monitorInstance(Object o): monitoredAll() && target(o);
    private pointcut monitorStatic(): monitoredAll() && !target(*);
    
    before() : monitoredAll() {
        enter();
    }
    
    after(Object object) returning: monitorInstance(object) {
        long end = responseFactory.getClock().getTimeQuickly();        
        TimeStack ts = (TimeStack)timeStackHolder.get();
        long start = ts.pop();
        long delta = end-start;
        
        if (delta >= methodThreshold) {
            Response response = monitor(thisJoinPoint, object.getClass());
            complete(response, start, end);
        }
    }
    
    after(Object object) throwing (Throwable t): monitorInstance(object) {
        long end = responseFactory.getClock().getTimeQuickly();        
        TimeStack ts = (TimeStack)timeStackHolder.get();
        long start = ts.pop();
        long delta = end-start;
        
        FailureDescription fd =  failureDetectionStrategy.getFailureDescription(t);
        Response response;
        if (fd != null && fd.getSeverity() >= FailureDetectionStrategy.FAILURE) {
            response = monitor(thisJoinPoint, object.getClass());
            response.set(Response.FAILURE_DATA, fd);            
        } else if (delta >=  methodThreshold) {
            response = monitor(thisJoinPoint, object.getClass());
            if (fd != null && fd.getSeverity() >= FailureDetectionStrategy.FAILURE) {
                response.set(Response.EXCEPTION_WARNING, fd);
            }
        } else {
            return;
        }
        complete(response, start, end);
    }        
    
    after() returning: monitorStatic() {
        long end = responseFactory.getClock().getTimeQuickly();        
        TimeStack ts = (TimeStack)timeStackHolder.get();
        long start = ts.pop();
        long delta = end-start;
        
        if (delta >= methodThreshold) {
            Response response = monitorStatic(thisJoinPoint);
            complete(response, start, end);
        }
    }
    
    after() throwing (Throwable t): monitorStatic() {
        long end = responseFactory.getClock().getTimeQuickly();        
        TimeStack ts = (TimeStack)timeStackHolder.get();
        long start = ts.pop();
        long delta = end-start;
        
        FailureDescription fd =  failureDetectionStrategy.getFailureDescription(t);
        Response response;
        if (fd != null && fd.getSeverity() >= FailureDetectionStrategy.FAILURE) {
            response = monitorStatic(thisJoinPoint);
            response.set(Response.FAILURE_DATA, fd);            
        } else if (delta >=  methodThreshold) {
            response = monitorStatic(thisJoinPoint);
            if (fd != null && fd.getSeverity() >= FailureDetectionStrategy.FAILURE) {
                response.set(Response.EXCEPTION_WARNING, fd);
            }
        } else {
            return;
        }
        complete(response, start, end);
    }        
    
    protected void complete(Response response, long start, long end) {    
        // TODO: manage starting parent requests if not already started, and define details on existing requests if already present 
//        TimeStack ts = (TimeStack)timeStackHolder.get();        
//        ResponseStack rs = (ResponseStack)responseStackHolder.get();
//        if (ts.isEmpty()) {
//        } else {
//            for (int i=0; i<Time
//            response.setStartTime(start);
//            response.setEndTime(end);
//            rs.push(response);
//        }
        response.start(start);
        response.complete(end - start);            
    }
    
    private void enter() {
        TimeStack ts = (TimeStack)timeStackHolder.get();
        ts.push(responseFactory.getClock().getTimeQuickly());
    }       

    protected Response monitorStatic(JoinPoint joinPoint) {
        return monitor(joinPoint, ((MethodSignature)(joinPoint.getSignature())).getMethod().getDeclaringClass());
    }
    
    protected Response monitor(JoinPoint joinPoint, Class clazz) {        
        //lots of room to cache these...
        String name = clazz.getName();
        Signature sig = joinPoint.getStaticPart().getSignature();
        String methodName = sig.getName();
        if (sig instanceof ConstructorSignature) {
            int tailpos = name.lastIndexOf('.');
            if (tailpos == -1) {
                tailpos = 0;
            }
            methodName = name.substring(tailpos);
        }
        OperationDescription operation = operationFactory.makeRemoteOperation(controllerType, name, methodName);
        
        Response response = createResponse(operation, ServletRequestMonitor.JSP_PRIORITY);
        response.set(Response.PARAMETERS, joinPoint.getArgs());

        return response;
    }
        
    public String getLayer() {
        return Response.RESOURCE_SERVICE;
    }

    private static class TimeStack {
        long[] stack = new long[10];
        int pos = 0;
        
        public void push(long time) {
            try {
                stack[pos++] = time;
            } catch (ArrayIndexOutOfBoundsException oob) {
                long[] newstack = new long[stack.length*2];
                System.arraycopy(stack, 0, newstack, 0, pos-1);
                newstack[pos] = time;
                stack = newstack;
            }
        }
        
        public long pop() {
            return stack[--pos];
        }        
    }    
}
