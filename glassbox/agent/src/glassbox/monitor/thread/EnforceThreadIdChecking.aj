/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.monitor.thread;

// this would be a private static aspect but we prefer to keep the aspect in a separate source file...
aspect EnforceThreadIdChecking {
    declare error: call(* Thread.getId()) && within(glassbox.monitor.thread.ThreadMonitor15Impl) && !withincode(long getThreadId(..)):
        "use getThreadId to recover hidden thread id's";    
}
