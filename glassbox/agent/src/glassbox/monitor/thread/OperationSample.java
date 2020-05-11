/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.thread;

import glassbox.response.Response;

import java.util.LinkedList;
import java.util.List;

/**
 * Record of sampled data thread dump data over time for a single request.
 *  
 * @author Ron Bodkin
 *
 */
public class OperationSample {
    private static final int MAXIMUM_LENGTH = 1000;
    private List snapshots = new LinkedList();
    private long interval;
    private long totalCpu;
    private Response response;

    public OperationSample(long interval, long startCpu, Response response) {
        this.interval = interval;
        this.totalCpu -= startCpu;
        this.response = response;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setSnapshots(List snapshots) {
        this.snapshots = snapshots;
    }

    public List getSnapshots() {
        return snapshots;
    }

    public long getTotalTime() {
        return getInterval() * getSnapshots().size();
    }
    
    public long getTotalCpuTime() {
        return totalCpu>0 ? totalCpu : 0L;
    }
    
    // TODO: summarize as we go!
    public synchronized void record(ThreadSnapshot snapshot) {
        if (snapshots.size() < MAXIMUM_LENGTH) {
            snapshots.add(snapshot);
        }
    }
    
    public void end(long endCpu) {
        totalCpu += endCpu;
    }
    
    public Response getResponse() {
        return response;
    }
}
