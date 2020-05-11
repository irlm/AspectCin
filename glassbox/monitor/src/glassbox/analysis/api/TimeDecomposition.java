/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

/**
 * Captures mutually exclusive breakdown of overall time by component.
 * @author Ron Bodkin
 *
 */
public interface TimeDecomposition {
    
    // components
    /** component: time spent in common code above operation */
    public static final int DISPATCH = 0;
    /** component: time spent in other areas (not identified) */
    public static final int OTHER_PROCESSING = 1;
    /** component: database access */
    public static final int DATABASE_ACCESS = 2;
    /** component: remote calls */
    public static final int REMOTE_CALLS = 3;
    
    /** resource: running Java code */ 
    public static final int RUNNABLE_JAVA = 0;
    /** resource: running native code */ 
    public static final int RUNNABLE_NATIVE = 1;
    /** resource: thread waiting, typically for I/O */ 
    public static final int THREAD_WAIT = 0;
    /** resource: thread contention */ 
    public static final int BLOCKED = 3; 
    public static final int MAX_PARTS = 8;
    
    public FrequencySummaryStats getPart(int componentId);
}
