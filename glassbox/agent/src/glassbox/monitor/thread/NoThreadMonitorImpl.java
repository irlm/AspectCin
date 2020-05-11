/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.thread;

import glassbox.response.Response;
import glassbox.track.api.PerfStats;

/**
 * Thread monitor implementation for platforms where no thread monitor is available...
 * 
 * @author Ron Bodkin
 *
 */
public class NoThreadMonitorImpl implements ThreadMonitor {

    public void startMonitoring(Thread thread, Response response) {
    }

    public void startMonitoring(Response response) {
    }

    public OperationSample stopMonitoring(Thread thread, PerfStats stats) {
        return null;
    }

    public OperationSample stopMonitoring(PerfStats stats) {
        return null;
    }

    public OperationSample getCurrentData(Thread thread) {
        return null;
    }

    public void resumeMonitoring(PerfStats stats) {
    }

    public void suspendMonitoring(PerfStats stats) {
    }

    public OperationSample getCurrentData() {
        return null;
    }

    public long getInterval() {
        return 0;
    }

    public void setInterval(long interval) {
    }

    public void setIntervalMillis(long intervalInMillis) {        
    }
    
    public void run() {
    }

    private static final long serialVersionUID = 1L;
}
