/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.track.api;

public class ThreadState {
    private StackTraceElement[] stackTrace;
    /** Stores the class of the lock, unless locking on java.lang.Class, in which case stores the full lock */
    private String lockName;
    private int executionState;
    public static final int RUNNABLE_JAVA = 0;
    public static final int RUNNABLE_NATIVE = 1;
    public static final int BLOCKED = 2;
    public static final int WAITING = 3;
    public static final int NUMBER_OF_STATES = 4;
    static private final long serialVersionUID = 2;
    
    /**
     * @return The class of the lock, unless locking on java.lang.Class, in which case it returns the full lock id. 
     * Returns null if no lock
     */
    public String getLockName() {
        return lockName;
    }
    public void setLockName(String lockName) {
        if (lockName != null) {
            int pos = lockName.indexOf('@');
            if (pos == -1) {
                throw new IllegalArgumentException("invalid lock name "+lockName);
            }
            String className = lockName.substring(0, pos); 
            if (!className.equals("java.lang.Class")) {
                // store just the class name for instance locks 
                lockName = className;
            }
            // else store the full lock for class locks
        }
        this.lockName = lockName;
    }
    
    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }
    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * This is like an equals method but it ignores the time stamps, i.e., was it the same join point.
     * 
     * @param other
     * @return
     */
    public boolean equals(Object otherObj) {
        if (this == otherObj) {
            return true;
        }
        if (otherObj==null || !(otherObj instanceof ThreadState)) {
            return false;
        }
        ThreadState other = (ThreadState)otherObj;
        
        if (getLockName()==other.getLockName() || (getLockName()!=null && getLockName().equals(other.getLockName()))) {
            // same lock, compare stack traces
        	StackTraceElement[] stack = getStackTrace();
        	StackTraceElement[] ostack = other.getStackTrace();
        	
            if (stack==null && ostack == null) return true;
            if (stack == null || ostack == null) return false;
            // both non-null from here out
            if (stack.length != ostack.length) return false;
            // both same length
            for (int i = 0; i < stack.length; i++) {
                if (!stack[i].equals(ostack[i])) return false; // StackTraceElement equals() compares filename, classname, methodname, line number, 
            }  // line numbers may not exist, which means we'll aggregate stats for all invocations within the same method, from the same object 
            return true;
        }
        return false;
    }
    
    public int hashCode() {
        int code = (lockName==null ? 0 : lockName.hashCode() * 0x2347231f);
        if (stackTrace==null) {
            code+=0xa1890712;
        } else {
            code = 0x234b87d2*stackTrace.length;
            for (int i = 0; i < stackTrace.length; i++) {
                code *= 0xfa834dd2;
                code += stackTrace[i].hashCode();
            }
        }
        return code;
    }
    
    public static ThreadState createFromThrowable(Throwable t) {
        ThreadState state = new ThreadState();
        state.setStackTrace(t.getStackTrace());
        return state;
    }       
        
    /**
     * @return the executionState
     */
    public int getExecutionState() {
        return executionState;
    }
    /**
     * @param executionState the executionState to set
     */
    public void setExecutionState(int executionState) {
        this.executionState = executionState;
    } 
}