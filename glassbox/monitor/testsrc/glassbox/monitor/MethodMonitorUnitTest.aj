package glassbox.monitor;

import glassbox.response.Response;
import glassbox.util.timing.Clock;

public class MethodMonitorUnitTest extends MonitorResponseTestCase {

    public void setUp() {
        super.setUp();
        assertFalse(ExecutionMonitor.aspectOf().isEnabled());        
        assertTrue(CallMonitor.aspectOf().isEnabled());        
        ExecutionMonitor.aspectOf().methodThreshold = 0;
        CallMonitor.aspectOf().methodThreshold = 0;
    }
    
    public void tearDown() {
        super.tearDown();
        ExecutionMonitor.aspectOf().setEnabled(false);
        CallMonitor.aspectOf().setEnabled(true);
        ExecutionMonitor.aspectOf().methodThreshold = 1;
        CallMonitor.aspectOf().methodThreshold = 1;
    }
    
    public void testNoMatch() {
        new Driver().helper();
        assertEquals(0, listener.responses.size());
    }
    
    public void testCall() {
        DriverBase db = new Driver();
        db.run();
        assertEquals(4, listener.responses.size());
        //XXX fix when we nest this properly
        Response nested = getResponse(1);
        Response end = getResponse(3);
        assertEquals(time[0], end.getStart());
        assertEquals(time[3], end.getEnd());
        assertProperties(operationFactory.makeRemoteOperation("call monitor", DriverBase.class.getName(), "exec"), Response.RESOURCE_SERVICE, nested);        
        assertProperties(operationFactory.makeRemoteOperation("call monitor", Driver.class.getName(), "run"), Response.RESOURCE_SERVICE, end);        
    }
    
    public void testDisabled() {
        CallMonitor.aspectOf().setEnabled(false);
        new Driver().run();
        assertEquals(0, listener.responses.size());        
    }
    
    public void testBothEnabled() {
        ExecutionMonitor.aspectOf().setEnabled(true);
        new Driver().run();
        assertEquals(8, listener.responses.size());
        Response end = getResponse(7);
        assertEquals(time[0], end.getStart());
        assertEquals(time[7], end.getEnd());
        assertProperties(operationFactory.makeRemoteOperation("call monitor", Driver.class.getName(), "run"), Response.RESOURCE_SERVICE, end);        
    }
    
    public void testExecution() {
        ExecutionMonitor.aspectOf().setEnabled(true);
        CallMonitor.aspectOf().setEnabled(false);
        new Driver().run();
        assertEquals(4, listener.responses.size());
        Response end = getResponse(3);
        assertEquals(time[0], end.getStart());
        assertEquals(time[3], end.getEnd());
        assertProperties(operationFactory.makeRemoteOperation("execution monitor", Driver.class.getName(), "run"), Response.RESOURCE_SERVICE, end);        
    }
    
    //TODO
    
    // testing analysis would be an integration test that ensures the analyzer reports on these as operations...

    static aspect CallMonitor extends MethodMonitor {
        public CallMonitor() {
            super("call monitor", true);
        }
        
        protected pointcut monitoredPoint() :
            within(MethodMonitorUnitTest) && call(public * DriverBase+.*(..));
    }
    
    static aspect ExecutionMonitor extends MethodMonitor {
        public ExecutionMonitor() {
            super("execution monitor", false);
        }
        
        protected pointcut monitoredPoint() :
            within(DriverBase+) && execution(public * *(..));
    }
        
    static abstract class DriverBase {
        public static void exec() {
        }
        
        public abstract void run();
        
        void helper() {
        }
    }
    
    static class Driver extends DriverBase {        
        public void run() {
            helper();
            exec();
        }        
    }

}
