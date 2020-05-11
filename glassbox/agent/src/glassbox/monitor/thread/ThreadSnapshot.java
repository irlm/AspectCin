/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.thread;

import glassbox.track.api.ThreadState;
import glassbox.track.api.UsageTrackingInfo;

public interface ThreadSnapshot {

    /**
     * 
     * @return null if no lock
     */
    String getLockName();

    void setLockName(String lockName);

    StackTraceElement[] getStackTrace();

    void setStackTrace(StackTraceElement[] stackTrace);

    ThreadState getState();

    // why is this here?!
    UsageTrackingInfo getUsageInfo();

    void setExecutionState(int executionState);
    int getExecutionState();
}