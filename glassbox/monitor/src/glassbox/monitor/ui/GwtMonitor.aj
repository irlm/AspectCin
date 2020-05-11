package glassbox.monitor.ui;

import glassbox.response.Response;

import java.io.Serializable;

public aspect GwtMonitor extends MvcFrameworkMonitor {
    private static final String REQUEST_TYPE = "AjaxRequest"; 
    private pointcut inRemoteService() : 
        within(com.google.gwt.user.client.rpc.RemoteService+);
    
    // this is only matched in emulation mode 
//    private pointcut inClient() :
//        within(com.google.gwt.user.client.rpc.ServiceDefTarget+);
    
    public pointcut gwtService() : 
        inRemoteService() && execution(public * *(..)); 

    protected pointcut monitorEnd() : gwtService();
    
    public GwtMonitor() {
        super(REQUEST_TYPE);
    }
    
    // this could be reused as "reflection method"
    before(Object service) : gwtService() && this(service) {
        Serializable key = getMethodDescriptor(service.getClass().getName(), thisJoinPointStaticPart.getSignature().getName());
        Response response = createResponse(key, getLayer());
        response.set(Response.PARAMETERS, thisJoinPoint.getArgs());
        response.start();
    }    

}
