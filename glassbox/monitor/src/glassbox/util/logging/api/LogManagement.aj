/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.util.logging.api;

import glassbox.monitor.MonitoredType;
import glassbox.util.jmx.*;
import glassbox.util.org.sl4j.ILoggerFactory;
import glassbox.util.org.sl4j.Logger;

import java.lang.ref.WeakReference;
import java.util.*;

/*
 * Nondelegating loaders + many popular but not universal logging APIs + different container implementations =
 * lots of fun trying to configure pluggable portable logging for use in system ClassLoader classes that are
 * using private classes to implement logging (i.e., not requiring a specific logging API for the world)
 */
public aspect LogManagement pertypewithin(LogOwner+) {
    private transient Logger logger;
    private static ILoggerFactory factory;
    private Class managedClass;
    private static List toInitialize = null;

    // don't put logging into the public API...
    // we could sure use !isInterface() here
    declare parents: !*..*ManagementInterface && ((glassbox..* && !glassbox.client..* && !glassbox.version..* && !glassbox.util.org.sl4j..* && !glassbox..api..* && !MonitoredType+  && !LogManagement+ && !LogManagement.* &&
            !glassbox.monitor.resource.Monitored* && !glassbox.monitor.AbstractMonitorClass+) || glassbox.track.api.* && 
            !EagerlyRegisteredManagedBean && !RequestMonitorMBean) implements LogOwner;
    
    after() returning: staticinitialization(*) {
        managedClass = thisJoinPointStaticPart.getSignature().getDeclaringType();
        initialize();
    }

    private void initialize() {
        if (factory != null) {
            logger = getLogger(managedClass);            
        } else {
            synchronized(LogManagement.class) {
                logger = getStubLogger();
                if (toInitialize == null) {
                    toInitialize = new LinkedList();
                }
                toInitialize.add(new WeakReference(this));
            }
        }
    }
    
    public static Logger getLogger(String name) {
        return factory.getLogger(name);
    }
    
    public static Logger getLogger(Class clazz) {
        return getLogger(clazz.getName());
    }
    
    public static Logger getLogger() {
        return null;
    }
    
    public Logger LogOwner.getLogger() {
        return null;
    }
    Logger around() : call(Logger LogOwner.getLogger()) {
        return logger;
    }

    public boolean LogOwner.isDebugEnabled() {
        return false;
    }    
    boolean around() : call(boolean LogOwner.isDebugEnabled()) {
        return logger.isDebugEnabled();
    }

    public boolean LogOwner.isWarnEnabled() {
        return false;
    }    
    boolean around() : call(boolean LogOwner.isWarnEnabled()) {
        return logger.isWarnEnabled();
    }
    
    public void LogOwner.logError(String message) {
    }    
    void around(String message) : call(void LogOwner.logError(String)) && args(message) {
        logger.error(message);
    }

    public void LogOwner.logError(String message, Throwable throwable) {
    }    
    void around(String message, Throwable throwable) : 
            call(void LogOwner.logError(String, Throwable)) && args(message, throwable) {
        logger.error(message, throwable);
    }

    public void LogOwner.logWarn(String message) {
    }
    void around(String message) : call(void LogOwner.logWarn(String)) && args(message) {
        logger.warn(message);
    }
    
    public void LogOwner.logWarn(String message, Throwable throwable) {
    }
    void around(String message, Throwable throwable) : 
        call(void LogOwner.logWarn(String, Throwable)) && args(message, throwable) {
        logger.warn(message, throwable);
    }

    public void LogOwner.logInfo(String message) {
    }
    void around(String message) : call(void LogOwner.logInfo(String)) && args(message) {
        logger.info(message);
    }
    
    public void LogOwner.logInfo(String message, Throwable throwable) {
    }
    void around(String message, Throwable throwable) : 
        call(void LogOwner.logInfo(String, Throwable)) && args(message, throwable) {
        logger.info(message, throwable);
    }

    public void LogOwner.logDebug(String message) {
    }
    void around(String message) : call(void LogOwner.logDebug(String)) && args(message) {
        logger.debug(message);
    }

    public void LogOwner.logDebug(String message, Throwable throwable) {
    }
    void around(String message, Throwable throwable) : 
        call(void LogOwner.logDebug(String, Throwable)) && args(message, throwable) {
        logger.debug(message, throwable);
    }
    
    declare error: call(* LogOwner.*(..)) && !call(* Object.*(..)) && !within(LogOwner+) 
//            && !(call(* getLogger()) || !within(LogManagement)) // exempt work-around
        :
        "Only call log owner methods from within a log owner, on itself";
         
    public synchronized static void setLoggerFactory(ILoggerFactory logFactory) {
        factory = logFactory;
        if (toInitialize != null) {
            for (Iterator iter = toInitialize.iterator(); iter.hasNext();) {
                WeakReference ref = (WeakReference)iter.next();
                LogManagement logManager = (LogManagement) ref.get();
                if (logManager != null) {
                    logManager.initialize();
                }
            }
            toInitialize = null;
        }
        
    }

    public static ILoggerFactory getLoggerFactory() {
        return factory;
    }

    public static Logger getStubLogger() {
        return new Logger() {
            public void debug(String format, Object arg1, Object arg2) {
            }

            public void debug(String format, Object arg) {
            }

            public void debug(String format, Object[] argArray) {
            }

            public void debug(String msg, Throwable t) {
            }

            public void debug(String msg) {
            }

            public void error(String format, Object arg1, Object arg2) {
                System.err.println("Error! "+format);                                                
            }

            public void error(String format, Object arg) {
                System.err.println("Error! "+format);                                                
            }

            public void error(String format, Object[] argArray) {
                System.err.println("Error! "+format);                                                
            }

            public void error(String msg, Throwable t) {
                System.err.println("Error! "+msg);
                t.printStackTrace();
            }

            public void error(String msg) {
                System.err.println("Error! "+msg);
            }

            public String getName() {
                return null;
            }

            public void info(String format, Object arg1, Object arg2) {
            }

            public void info(String format, Object arg) {
            }

            public void info(String format, Object[] argArray) {
            }

            public void info(String msg, Throwable t) {
            }

            public void info(String msg) {
            }

            public boolean isDebugEnabled() {
                return false;
            }

            public boolean isErrorEnabled() {
                return true;
            }

            public boolean isInfoEnabled() {
                return false;
            }

            public boolean isWarnEnabled() {
                return true;
            }

            public void warn(String format, Object arg1, Object arg2) {
                System.out.println("Warning! "+format);
            }

            public void warn(String format, Object arg) {
                System.out.println("Warning! "+format);
            }

            public void warn(String format, Object[] argArray) {
                System.out.println("Warning! "+format);
            }

            public void warn(String msg, Throwable t) {
                System.out.println("Warning! "+msg);
                t.printStackTrace(System.out);
            }

            public void warn(String msg) {
                System.out.println("Warning! "+msg);
            }
            
        };
    }
    
//    private static aspect CheckInvariant {
//        declare precedence: LogManagement, CheckInvariant;
//        before() : execution(* LogOwner.*(..)) || execution(Logger LogOwner.getLogger()) {
//            throw new IllegalStateException("Logging system not initialized properly at "+thisJoinPointStaticPart);
//        }
//    }
    private static final long serialVersionUID = 1;
}
