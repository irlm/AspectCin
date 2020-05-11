/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.thread;

import glassbox.response.Response;
import glassbox.track.api.PerfStats;
import glassbox.track.api.ThreadState;
import glassbox.util.concurrent.ConcurrentIdentityHashMap;
import glassbox.util.timing.api.TimeConversion;

import java.lang.management.*;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * This monitor periodically grabs thread dumps for all threads that are processing user requests.
 * The monitor is run in a daemon thread that schedules collection as a background activity.
 * TBD: who schedules this.
 * 
 * This version has Java 1.5 runtime dependencies.
 * 
 * @author Ron Bodkin
 *
 */

public class ThreadMonitor15Impl implements ThreadMonitor {
    
    /**
     * Map of operations data per thread. This is written to by different user threads.
     * However, we never make structural modifications to this map. We keep a null entry for inactive threads
     * and make a copy with another thread entry when we encounter a thread that isn't in the map. 
     */
    private Map/*<Thread, OperationSample>*/ monitored = new ConcurrentIdentityHashMap();
    private ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

    /** how frequently do we sample threads, in nanoseconds */
    private long interval;

    private static final long serialVersionUID = 2L;
    
    public ThreadMonitor15Impl(long interval) {
        this.interval = interval;
    }
    
    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadMonitor#startMonitoring(java.lang.Thread)
     */
    public void startMonitoring(Thread thread, Response response) {
        monitored.put(thread, new OperationSample(interval, getCpuTime(thread), response));        
    }
    
    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadMonitor#startMonitoring()
     */
    public void startMonitoring(Response response) {
        startMonitoring(Thread.currentThread(), response);
    }

    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadMonitor#stopMonitoring(java.lang.Thread)
     */
    public OperationSample stopMonitoring(Thread thread, PerfStats stats) {
        OperationSample sample = getCurrentData(thread);
        monitored.remove(thread);
        if (sample != null) {
            sample.end(getCpuTime(thread));
        }
        return sample;
    }
    
    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadMonitor#stopMonitoring()
     */
    public OperationSample stopMonitoring(PerfStats stats) {
        return stopMonitoring(Thread.currentThread(), stats);
    }
    
    ThreadLocal suspended = new ThreadLocal();
    
    public void suspendMonitoring(PerfStats stats) {
        suspended.set(monitored.get(Thread.currentThread()));
    }
    
    public void resumeMonitoring(PerfStats stats) {
        OperationSample sample = (OperationSample)suspended.get();
        if (suspended == null) {
            logError("Attempting to resume with nothing suspended");
        } else {
            suspended.set(null); // don't leak
            monitored.put(Thread.currentThread(), sample);
        }
    }

    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadMonitor#getCurrentData(java.lang.Thread)
     */
    public OperationSample getCurrentData(Thread thread) {
        return (OperationSample)monitored.get(thread);
    }
    
    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadMonitor#getCurrentData()
     */
    public OperationSample getCurrentData() {
        return getCurrentData(Thread.currentThread());
    }
    
    public void run() {
        monitorThreads();
    }

    void monitorThreads() {
        // this would probably be more efficient if we requested them all at once, although we might want to request
        // different stack depths...
        
//        long time = ClockManager.getTime();
        for (Iterator iter = monitored.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry) iter.next();
            OperationSample value = (OperationSample)entry.getValue();
            // null value means the thread isn't active..
            if (value != null) {            	
                Thread thread = (Thread)entry.getKey();
                long id = getThreadId(thread);
                if (id<=0) {
                    continue;
                }
                // future peformance: test whether getting them all at once be more efficient?
                ThreadInfo threadInfo = mxBean.getThreadInfo(id, Integer.MAX_VALUE);
                if (threadInfo != null) {
                    // thread is alive
                    ThreadSnapshot snapshot = new ThreadSnapshotImpl(glassbox.util.timing.ClockManager.getUsage());
                    snapshot.setStackTrace(threadInfo.getStackTrace());
                    snapshot.setLockName(threadInfo.getLockName());
                    if (threadInfo.isInNative()) {
                        snapshot.setExecutionState(ThreadState.RUNNABLE_NATIVE);
                    } else {
                        snapshot.setExecutionState(getStateCategory(threadInfo.getThreadState()));
                    }
                   	value.record(snapshot);
                }
            }
        }
    }
    
    private static boolean once = false;
    
    private long getThreadId(Thread thread) {
        long id = thread.getId();
        // we run into this on oc4j... perhaps they don't want applications to find/depend on thread id's??
        if (id <= 0 && thread.isAlive()) {
            try {
                // hopefully we don't need to do this
                Field field = Thread.class.getDeclaredField("tid");
                field.setAccessible(true);
                id = ((Long)field.get(thread)).longValue();
            } catch (SecurityException e) {
                // ignore
            } catch (IllegalArgumentException e) {
                logError("Can't read thread", e);
            } catch (IllegalAccessException e) {
                // ignore
            } catch (NoSuchFieldException e) {
                // ignore - could happen on an alternative runtime or on a future rev of Java
            }
            if (id <= 0 && !once) {
                once = true;
                logWarn("Can't sample data for threads sometimes because of invalid thread id's: "+id);
            }
        }
        return id;
    }        
    
    private int getStateCategory(Thread.State state) {
        // this is a bit confusing: in Java 5, the values in the case statement
        // are implicitly scoped to be Thread.State constants
        // whereas the values being returned are scoped to TreeTimeStats
        switch (state) {
            case BLOCKED:
                return ThreadState.BLOCKED;
            case TIMED_WAITING:
            case WAITING:
                return ThreadState.WAITING;
            default:
                return ThreadState.RUNNABLE_JAVA;
        }
    }        
    
    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadMonitor#getInterval()
     */
    public long getInterval() {
        return interval;
    }
    
    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadMonitor#setInterval(long)
     */
    public void setInterval(long interval) {
        if (interval<0L) {
            throw new IllegalArgumentException("Invalid setting for monitor interval: "+interval+", retaining setting: "+this.interval);
        }
        this.interval = interval;
    }
    
    public void setIntervalMillis(long intervalInMillis) {
        setInterval(TimeConversion.convertMillisToNanos(intervalInMillis));
    }

//  public static boolean isSystem(String className) {
//  return className.startsWith("java") || className.startsWith("sun") || className.startsWith("com.sun");  
//}

    public long getCpuTime(Thread thread) {
        long id=getThreadId(thread);
        if (id<=0) {
            return 0L;
        }
        return mxBean.getThreadCpuTime(id);        
    }    
}
