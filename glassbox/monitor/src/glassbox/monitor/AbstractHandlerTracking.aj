package glassbox.monitor;

import glassbox.track.api.DefaultFailureDetectionStrategy;

// you need to extend this aspect to track handlers based on some scope
// this will probably grow into a "CodeProfilerMonitor" - extend it to provide a pointcut and it times methods, watches handlers, etc.
// this will ultimately be driven by component definitions
public abstract aspect AbstractHandlerTracking extends AbstractMonitor /*just to get a failureDetectionStrategy...*/ {
    protected abstract pointcut scope();
    public pointcut inRequest() : if(responseFactory.getLastResponse()!=null);
    public pointcut monitoredHandler(Throwable t) :
        scope() && handler(*) && args(t) && inRequest();
        
    before(Throwable t) : monitoredHandler(t) {
        recordException(responseFactory.getLastResponse(), t);        
    }
    
    public AbstractHandlerTracking() {
        // this is a dumb stub - we want to have a better way to configure what exceptions are errors! 
        
        failureDetectionStrategy = new DefaultFailureDetectionStrategy() {

            public int getSeverity(Throwable t) {
                return WARNING;
            }
            
        };
    }
}
