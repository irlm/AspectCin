/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.monitor.thread;

import glassbox.response.Response;
import glassbox.summary.StatsSummarizer;
import glassbox.track.api.PerfStats;
import glassbox.util.timing.api.TimeConversion;

import java.util.Timer;
import java.util.TimerTask;

public aspect ThreadMonitorIntegration {
    private ThreadMonitor monitor;
    private Timer timer;

    public ThreadMonitor getThreadMonitor() {
        return monitor;
    }

    /**
     * Set the system monitor. Has the side effect of scheduling the monitor to run periodically.
     */
    public void setThreadMonitor(ThreadMonitor monitor) {
        this.monitor = monitor;

        reschedule();
    }

    public void destroy() {
        if (monitor != null) {
            monitor = null;
        }
        
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    
    private void reschedule() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            
        }
        if (monitor != null && monitor.getInterval() > 0L) {
            TimerTask monitorTask = new TimerTask() {
                public void run() {
                    monitor.run();
                }
            };
            timer = new Timer("Thread Monitor", true);
            timer.schedule(monitorTask, 0, TimeConversion.convertNanosToMillis(monitor.getInterval()));
        }
    }

    // it might be better to add samples to the responses?
    // i'd much rather have the thread monitor publish raw data and let the stats summarizer summarize than pull state from the summarizer!    
    after(Response response) returning: StatsSummarizer.startTopLevelStats(*, response, *) {        
        if (monitor != null) {
            monitor.startMonitoring(response);
        }
    }

    after(Response response, StatsSummarizer summarizer) returning: StatsSummarizer.startNestedLaterOperation(*, response, summarizer) {
        if (monitor != null) { 
            monitor.suspendMonitoring(summarizer.getPrimaryOperationStats());
            monitor.startMonitoring(response);
        }
    }
            
    after (PerfStats stats, Response response, StatsSummarizer summarizer) returning: StatsSummarizer.endNestedLaterOperation(stats, response, summarizer) {
        if (monitor != null) {
            monitor.stopMonitoring(stats);
            monitor.resumeMonitoring(summarizer.getPrimaryOperationStats());
        }
    }    
    
    /**
     * @Name("endMonitoring")
     */
    before(PerfStats stats, StatsSummarizer summarizer) : StatsSummarizer.endTopLevelStats(stats, *, summarizer) {
        // this needs to go before ending the unit of work, so we can include contention data in our summary...
        if (monitor != null) {
            monitor.stopMonitoring(summarizer.getPrimaryOperationStats());
        }
    }

    pointcut changeTimerInterval(ThreadMonitor monitor, long interval) : 
        execution(void ThreadMonitor.setInterval(*)) && this(monitor) && args(interval);

    after(ThreadMonitor monitor) returning : changeTimerInterval(monitor, *) {
        if (monitor == this.monitor) {
            reschedule();
        }
    }
}
