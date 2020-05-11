/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.thread;

import glassbox.monitor.thread.ThreadMonitor;

/**
 * Factory that creates a thread monitor. Currently we only provide one for Java 1.5.
 * On other platforms, we will not do thread monitoring (yet).
 *  
 * @author Ron Bodkin
 *
 */
public class ThreadMonitorFactory {    

    /**
     * 
     * @param interval in nanos
     * @return a monitor, or null if not supported on this VM
     */
    public static ThreadMonitor createThreadMonitor(long interval) {
        try {
            Class.forName("java.lang.management.ManagementFactory");
            return new ThreadMonitor15Impl(interval);
        } catch (ClassNotFoundException e) {
            // dummy implementation since Spring doesn't like factories that return null (!) 
            return new NoThreadMonitorImpl();
        }
    }

    private static final long serialVersionUID = 1L;
}
