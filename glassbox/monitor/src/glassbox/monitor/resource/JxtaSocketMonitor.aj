package glassbox.monitor.resource;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import edu.emory.mathcs.util.WeakIdentityHashMap;
import glassbox.monitor.AbstractMonitor;
import glassbox.response.Response;

public aspect JxtaSocketMonitor extends AbstractMonitor {
   
    private Map map = Collections.synchronizedMap(new WeakIdentityHashMap()); 
    private static final String PREFIX = "jxta://"; 
    
    protected pointcut socketIoMethod(Object socket) :
        within(net.jxta.socket.*) && this(socket) &&
                (execution(* net.jxta.socket.Jxta*Socket.connect*(..)) || 
                execution(* net.jxta.socket.Jxta*Socket.read*(..)) || execution(* net.jxta.socket.Jxta*Socket.write*(..)) || 
                execution(* net.jxta.socket.Jxta*Socket.close*(..)) || execution(* net.jxta.socket.Jxta*Socket.shutdown*(..)));
    
    protected pointcut monitorPoint(Object socket) :
        socketIoMethod(socket);
        
    before(Object socket, Object pipeAd) : socketIoMethod(socket) && args(*, pipeAd, ..) && execution(* *(*, net.jxta.document.Advertisement+, ..)) {
        recordSocketKey(socket, pipeAd);
    }

    before(Object socket, Object pipeAd) : socketIoMethod(socket) && args(*, *, pipeAd, ..) && execution(* *(*, *, net.jxta.document.Advertisement+, ..)) {
        recordSocketKey(socket, pipeAd);
    }
    
    private void recordSocketKey(Object socket, Object pipeAd) {
        map.put(socket, PREFIX+pipeAd);
    }
    
    protected Serializable getKey(Object socket) {
        Object key = map.get(socket);
        if (key != null) {
            return (Serializable)key;
        }
        return PREFIX+"socket_without_ad";
    }

    public String getLayer() {
        return Response.RESOURCE_SERVICE;
    }
}
