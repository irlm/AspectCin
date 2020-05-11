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

public interface ThreadMonitor extends Runnable {

    /**
     * Start monitoring this thread. Can run in any thread. 
     * It is not thread safe to call this method simultaneously for the same value of thread, nor to call this
     * method on a thread while that thread is calling <code>startMonitoring()</code>. 
     */
    void startMonitoring(Thread thread, Response response);

    void startMonitoring(Response response);

    /**
     * Stop monitoring this thread. Can run in any thread. 
     * Also provides a natural hook for any summarizer to advise.
     * TODO: revisit the stats parameter
     * 
     * @param sample 
     * @param stats The statistics from finishing work on this thread (if any).
     */
    OperationSample stopMonitoring(Thread thread, PerfStats stats);

    /**
     * stop monitoring for the current thread
     * 
     * @see #stopMonitoring(thread, stats)
     */
    OperationSample stopMonitoring(PerfStats stats);

    void suspendMonitoring(PerfStats stats);
    
    void resumeMonitoring(PerfStats stats);

    /**
     * Get the current data for the given thread. Can run in any thread. Useful for single-threaded tests, but not thread
     * safe if monitoring the given thread (i.e., the OperationSample might be modified)
     * 
     * @param thread
     * @return null if not monitoring the thread
     */
    OperationSample getCurrentData(Thread thread);

    /**
     * Get the current data for the current thread. Not thread safe if monitoring the current thread
     * 
     * @return null if not monitoring the thread
     * @see #getCurrentData(Thread)
     */
    OperationSample getCurrentData();

    /**
     * Get sampling interval in nanoseconds
     */
    long getInterval();

    /**
     * Change sampling interval.
     * 
     * @param interval in nanoseconds
     */
    void setInterval(long interval);
    
    /**
     * Change sampling interval.
     * 
     * @param interval in milliseconds
     */
    void setIntervalMillis(long intervalInMillis);
    
    /**
     * Run a single monitor view. Runs in daemon thread only.
     */
    void run();

}
