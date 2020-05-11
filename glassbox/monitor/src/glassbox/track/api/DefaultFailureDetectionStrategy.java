/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import java.lang.reflect.Method;


//TODO: move this to the agent, by injecting it
public class DefaultFailureDetectionStrategy implements FailureDetectionStrategy {       
    /** Limits the message output length. Avoids including stack traces inline e.g., MySQL will output them */
    private static final int DEFAULT_MAX_DESCRIPTION_LENGTH = 500;    
    protected static final CauseStrategy reflectionCauseStrategy = new ReflectionCauseStrategy();
    protected static final CauseStrategy causeStrategy = makeCauseStrategy();
    protected static final Class servletExceptionClass = getServletExceptionClass();
    private static final long serialVersionUID = 2;
    private int maxDescriptionLength = DEFAULT_MAX_DESCRIPTION_LENGTH;
    
    public int getSeverity(Throwable t) {
        return FAILURE;
    }
    
    public FailureDescription getFailureDescription(Throwable t) {
        Throwable cause = causeStrategy.getRootCause(t);
        FailureDescriptionImpl description = new FailureDescriptionImpl(cause);
        description.severity = getSeverity(t);
        String m1 = message(t).trim();
        String m2 = message(cause).trim();
        if (m1.indexOf(m2) != -1) {
            m2="";
        } else {
            m2=" caused by "+message(cause);
        }
        description.summary = trim("an exception of type "+t.getClass()+": "+m1+m2);
        return description;
    }

    protected String trim(String msg) {
        if (msg.length()>maxDescriptionLength) {
            return msg.substring(0, maxDescriptionLength-3)+"...";
        }
        return msg;
    }
    
    protected String message(Throwable t) {
        String msg = t.getLocalizedMessage();
        if (msg == null) {
            return "(no description)";
        }
        
        return msg;
    }
    
    // isn't this rich - it's actually better to use reflection because backward compatibility constraints meant that sometimes classes
    // didn't chain in the JDK 1.4 way, even in Tomcat 5.5, e.g., ServletException and probably also EJB and JMS exceptions ...
    protected interface CauseStrategy {
        public Throwable getCause(Throwable t);
        public Throwable getRootCause(Throwable t);
    }
        
    public static CauseStrategy makeCauseStrategy() {
//        try {
//            Throwable.class.getMethod("getCause", null);
//            return new Java14CauseStrategy();
//        } catch (Throwable t) {
            return reflectionCauseStrategy;
//        }
    }

    public abstract static class AbstractCauseStrategy implements CauseStrategy {
        public Throwable getRootCause(Throwable t) {
            while (t != null) {
                Throwable next;
//                if (servletExceptionClass!=null && t.getClass().isAssignableFrom(servletExceptionClass)) {
//                    // the evil Servlet exception is so backwards compatible that it doesn't implement chaining
//                    // see, e.g., http://issues.apache.org/bugzilla/show_bug.cgi?id=36231
//                    // we use getRootCause to extract the cause
//                    next = reflectionCauseStrategy.getCause(t); // yuck! 
//                } else {
                    next = getCause(t);
//                }
                
                if (next == null) {
                    return t;
                }
                t = next;
            }
            return null;
        }
    }
    
//    public static class Java14CauseStrategy extends AbstractCauseStrategy {
//        public Throwable getCause(Throwable t) {
//            return t.getCause();
//        }
//    }

    public static class ReflectionCauseStrategy extends AbstractCauseStrategy {
        public Throwable getCause(Throwable t) {
            String[] methods = { "getRootCause", "getCausedByException", "getException", "getUndeclaredThrowable", "getTargetException", "getLinkedException", "getCause" };
            for (int i=0; i<methods.length; i++) {
                try {
                    Method meth = t.getClass().getMethod(methods[i], null);
                    Object result = meth.invoke(t, null);
                    if (result instanceof Throwable) {
                        return (Throwable)result;
                    }
                } catch (Throwable ignore) {
                    ; // continue                    
                }
            }
            return null;
        }
    }
    
    public static Class getServletExceptionClass() {
        try {
            return Class.forName("javax.servlet.ServletException");
        } catch (Throwable t) {
            return null;
        }
    }

    public int getMaxDescriptionLength() {
        return maxDescriptionLength;
    }

    public void setMaxDescriptionLength(int maxDescriptionLength) {
        this.maxDescriptionLength = maxDescriptionLength;
    }
        
};

