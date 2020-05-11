/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.track.api;

import glassbox.util.timing.api.TimeConversion;

/**
 * This is really a sampled state for a request. 
 * 
 * @author Ron Bodkin
 *
 */ 
public class SlowRequestDescriptor {
    private ThreadState threadState;

    private long elapsedTime;
    private int slowCount;
    private int totalCount;

    public SlowRequestDescriptor() {
    }
    
    public SlowRequestDescriptor(long elapsedTime, ThreadState threadState, int totalCount, int slowCount) {
        this.elapsedTime = elapsedTime;
        this.slowCount = slowCount;
        this.totalCount = totalCount; 
        setThreadState(threadState);
    }        
        
    public void setTraceElement(int depth, StackTraceElement slowestTraceElement, long elapsedTime) {
        if (threadState == null) {
            threadState = new ThreadState();
            threadState.setStackTrace(new StackTraceElement[depth]);
            this.setElapsedTime(elapsedTime);   
        }
        StackTraceElement[] stackTrace = threadState.getStackTrace();
        stackTrace[stackTrace.length - depth] = slowestTraceElement;
    }
    
    public void setThreadState(ThreadState threadState) {
        this.threadState = threadState;
    }

    public ThreadState getThreadState() {
        return threadState;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }
    
    public double getMeanTime() {
        return TimeConversion.meanNanosInSeconds(elapsedTime, totalCount);
    }

    public void setSlowCount(int slowCount) {
        this.slowCount = slowCount;
    }

    public int getSlowCount() {
        return slowCount;
    }
    
    public int getOperationCount() {
        return totalCount;
    }
    
    public double getMeanCount() {
        if (getOperationCount()==0) {
            return 0.;
        }
        return ((double)getSlowCount())/((double)getOperationCount());
    }
    
    private static final long serialVersionUID = 1L;
}