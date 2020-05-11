package glassbox.simulator;

public aspect MockComponentMonitorCode extends glassbox.monitor.MethodMonitor {
    public pointcut monitoredMethods() : within(glassbox.simulator.MockComponent+);   
}
