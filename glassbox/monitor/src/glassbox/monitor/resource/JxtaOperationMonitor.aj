package glassbox.monitor.resource;

import glassbox.monitor.ui.TemplateOperationMonitor;
import glassbox.response.Response;

public aspect JxtaOperationMonitor extends TemplateOperationMonitor {

    // simple form of JXTA operations: one operation per type of message listener
    // some listeners will dispatch on message content, which would require message-specific logic
    // a possible common one would be based on advertisement
    protected pointcut classControllerExecTarget() : 
        within(net.jxta.pipe.PipeMsgListener+) && execution(void pipeMsgEvent(net.jxta.pipe.PipeMsgEvent+));
        
    public String getLayer() {
        return Response.SERVICE_PROCESSOR;
    }
}
