/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.thread;

import glassbox.monitor.thread.ThreadSnapshot;
import glassbox.track.api.ThreadState;
import glassbox.track.api.UsageTrackingInfo;

import java.util.List;


public class ThreadSnapshotImpl implements ThreadSnapshot {
    private ThreadState state;
    private UsageTrackingInfo usageInfo;

    public ThreadSnapshotImpl(UsageTrackingInfo usageInfo) {
        this.usageInfo = usageInfo;
        state = new ThreadState();
    }
    
    public ThreadSnapshotImpl(UsageTrackingInfo usageInfo, ThreadState state) {
        this.usageInfo = usageInfo;
        this.state = state;
    }
    
    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadSnapshot#getLockName()
     */
    public String getLockName() {
        return state.getLockName();
    }
    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadSnapshot#setLockName(java.lang.String)
     */
    public void setLockName(String lockName) {
        state.setLockName(lockName);
    }
    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadSnapshot#getStackTrace()
     */
    public StackTraceElement[] getStackTrace() {
        return state.getStackTrace();
    }
    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadSnapshot#setStackTrace(java.lang.StackTraceElement[])
     */
    public void setStackTrace(StackTraceElement[] stackTrace) {
        state.setStackTrace(stackTrace);
    }
    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadSnapshot#getState()
     */
    public ThreadState getState() {
        return state;
    }
    
    /* (non-Javadoc)
     * @see glassbox.monitor.thread.ThreadSnapshot#getUsageInfo()
     */
    public UsageTrackingInfo getUsageInfo() {
        return usageInfo;
    }
    
    public void setExecutionState(int executionState) {
        state.setExecutionState(executionState);
    }
    
    public int getExecutionState() {
        return state.getExecutionState();
    }

    private static final long serialVersionUID = 1;
    
}
