package glassbox.monitor.ui;

import javax.portlet.*;
import glassbox.response.Response;

public aspect PortletMonitor extends MvcFrameworkMonitor {
    public static final Integer PORTLET_VIEW_PRIORITY = new Integer(Response.DEFAULT_PRIORITY.intValue()+10);
    public PortletMonitor() {
        super("javax.portlet.Portlet");
    }

    protected pointcut processAction(Object portlet) :
        within(Portlet+) && execution(void processAction(ActionRequest, ActionResponse)) && this(portlet);

    protected pointcut render(Object portlet) :
        (within(Portlet+) && execution(void render(RenderRequest, RenderResponse)) ||
        within(com.epicentric.portalbeans.PortalBean+) && execution(* com.epicentric.portalbeans.PortalBean.getView(..))) &&
        this(portlet);
    
    protected pointcut renderView(Object portletView) :
        within(com.epicentric.portalbeans.beans.jspbean.JSPView+) && this(portletView) &&
        execution(public * com.epicentric.portalbeans.beans.jspbean.JSPView.*(..)); /*serviceHTML*/
    
    before(Object portlet) : processAction(portlet) {
        begin(getMethodDescriptor(portlet.getClass().getName(), "process"));
    }
    
    before(Object portlet) : render(portlet) {
        begin(getMethodDescriptor(portlet.getClass().getName(), "render"));
    }
    
    before(Object portletView) : renderView(portletView) {
        begin(getMethodDescriptor(portletView.getClass().getName(), thisJoinPointStaticPart.getSignature().getName()), PORTLET_VIEW_PRIORITY);
    }

    protected pointcut monitorEnd() : processAction(*) || render(*) || renderView(*);     
}
