/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.thread;

import glassbox.monitor.thread.ThreadSnapshot;
import glassbox.util.timing.api.NoCpuUsageTrackingInfoImpl;

import junit.framework.TestCase;

public class ThreadSnapshotTest extends TestCase {
    public void testSameEmpty() {
        ThreadSnapshot one = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(100L));
        ThreadSnapshot two = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(200L));
        one.setStackTrace(new StackTraceElement[0]);
        two.setStackTrace(new StackTraceElement[0]);
        assertEquals(one.getState(), two.getState());
        assertEquals(two.getState(), one.getState());
        assertEquals(one.getState().hashCode(), two.getState().hashCode());
    }
    
    public void testDifferentSizedTraces() {
        ThreadSnapshot one = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(100L));
        ThreadSnapshot two = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(200L));
        one.setStackTrace(new StackTraceElement[0]);
        two.setStackTrace(new StackTraceElement[1]);
        assertFalse(one.getState().equals(two.getState()));
        assertFalse(one.getState().equals(null));
        assertFalse(two.getState().equals(one.getState()));
    }
    
    public void testDifferentMonitors() {
        ThreadSnapshot one = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(100L));
        ThreadSnapshot two = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(200L));
        one.setStackTrace(new StackTraceElement[0]);
        two.setStackTrace(new StackTraceElement[0]);
        one.setLockName("lock1@12");
        two.setLockName("lockx@12");
        assertFalse(one.getState().equals(two.getState()));
        assertFalse(two.getState().equals(one.getState()));        
    }

    public void testDifferentMonitorsWithNull() {
        ThreadSnapshot one = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(100L));
        ThreadSnapshot two = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(200L));
        one.setStackTrace(new StackTraceElement[0]);
        two.setStackTrace(new StackTraceElement[0]);
        one.setLockName("lock1@1");
        assertFalse(one.getState().equals(two.getState()));
        assertFalse(two.getState().equals(one.getState()));        
    }

    public void testSameMonitors() {
        ThreadSnapshot one = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(100L));
        ThreadSnapshot two = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(200L));
        one.setStackTrace(new StackTraceElement[0]);
        two.setStackTrace(new StackTraceElement[0]);
        one.setLockName("lock1@123");
        two.setLockName("lock1@456");
        assertEquals(one.getState(), two.getState());
        assertEquals(two.getState(), one.getState());        
        assertEquals(one.getState().hashCode(), two.getState().hashCode());
        Object x = one.getState();
        Object y = two.getState();
        assertTrue(x.equals(y));
    }
    
    public void testRealTracesSame() {
        ThreadSnapshot[] snaps = new ThreadSnapshot[2];
        for (int i=0; i<2; i++) {
            snaps[i] = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(i*100L));
            snaps[i].setStackTrace(Thread.currentThread().getStackTrace());
        }
        assertEquals(snaps[0].getState(), snaps[1].getState());
        assertEquals(snaps[1].getState(), snaps[0].getState());
        assertEquals(snaps[1].getState().hashCode(), snaps[0].getState().hashCode());
    }
    public void testRealTracesDifferent() {
        ThreadSnapshot one = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(100L));
        ThreadSnapshot two = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(200L));
        one.setStackTrace(Thread.currentThread().getStackTrace());
        two.setStackTrace(Thread.currentThread().getStackTrace());
        assertFalse(one.getState().equals(two.getState()));
        assertFalse(two.getState().equals(one.getState()));                
    }
    public void testDiffObject() {
        assertFalse(new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(0L)).getState().equals("foo"));
    }
    public void testCopyIsSame() {
        ThreadSnapshot one = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(100L));
        one.setStackTrace(new StackTraceElement[0]);
        one.setLockName("lock1@123");
        ThreadSnapshot two = new ThreadSnapshotImpl(new NoCpuUsageTrackingInfoImpl(200L), one.getState());
        assertEquals(one.getState(), two.getState());
        assertEquals(two.getState(), one.getState());        
        assertEquals(one.getState().hashCode(), two.getState().hashCode());
    }
}
