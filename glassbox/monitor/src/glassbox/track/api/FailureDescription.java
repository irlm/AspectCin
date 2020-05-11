/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;


public interface FailureDescription {
    /**
     * @see FailureDetectionStrategy for constant values describing severity
     */
    public int getSeverity();
    public String getSummary();
    public ThreadState getThreadState();
    public String getThrowableClassName();
    
    /**
     * Can return null if there is no defined call associated with this failure...
     */
    public CallDescription getCall();
    
    public void setCall(CallDescription description);
}
