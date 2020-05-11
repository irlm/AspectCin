/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;


public class FailureDescriptionImpl implements FailureDescription {
    public int severity;
    public String summary;
    public ThreadState threadState;
    public String throwableClassName;
    private CallDescription callDescription;
    
    public FailureDescriptionImpl(Throwable t) {
        threadState = ThreadState.createFromThrowable(t);
        throwableClassName = t.getClass().getName();
    }
    
    public int getSeverity() {
        return severity;
    }

    public String getSummary() {
        return summary;
    }

    public ThreadState getThreadState() {
        return threadState;
    }
    
    public String getThrowableClassName() {
        return throwableClassName;
    }
    
    // n.b. we do NOT include summary in the notion of equality, because it can include extraneous info
    // like times, specific data values, etc., that shouldn't be counted for equality
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        } else if (oth.getClass() != getClass()) {
            return false;
        }
        FailureDescriptionImpl other = (FailureDescriptionImpl)oth;
        if (severity != other.severity) {
            return false;
        }
        return throwableClassName.equals(other.throwableClassName) && eq(threadState, other.threadState);
    }    
    
    public int hashCode() {
        return severity * (threadState.hashCode()+74) * throwableClassName.hashCode()*0xa2d3c472;
    }
    
    protected static boolean eq(Object a, Object b) {
        return a==b || (a!=null && a.equals(b));
    }

    public CallDescription getCall() {
        return callDescription;
    }

    public void setCall(CallDescription callDescription) {
        this.callDescription = callDescription;
    }    
    
}
