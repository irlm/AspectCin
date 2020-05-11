/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.bootstrap.log;

import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Category;
import org.apache.log4j.Level;

public class BootstrapLog {
    private static final String LOG_NAME = "glassbox.bootstrap";

    public static void error(String msg) {        
        try {
            jclLogError(msg);
        } catch (NoClassDefFoundError nolog) {
            System.err.println("error: "+msg);
        }
    }

    public static void error(String msg, Throwable t) {        
        try {
            jclLogError(msg, t);
        } catch (NoClassDefFoundError nolog) {
            System.err.println(msg);
            t.printStackTrace();
        }
    }

    public static void warning(String msg) {        
        try {
            jclLogWarning(msg);
        } catch (NoClassDefFoundError nolog) {
            System.err.println("warning: "+msg);
        }
    }

    public static void info(String msg) {
        try {
            jclLogInfo(msg);
        } catch (NoClassDefFoundError nolog) {
            System.out.println(msg);
        }
    }
        
    public static void debug(String msg) {
        try {
            jclLogDebug(msg);
        } catch (NoClassDefFoundError nolog) {
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
        
    public static void debug(String msg, Throwable throwable) {
        try {
            jclLogDebug(msg, throwable);
        } catch (NoClassDefFoundError nolog) {
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
        
    private static void jclLogError(String msg) {
        getJclLog().error(msg);
    }

    private static void jclLogError(String msg, Throwable t) {
        getJclLog().error(msg, t);
    }

    private static void jclLogWarning(String msg) {
        getJclLog().warn(msg);
    }

    private static void jclLogInfo(String msg) {
        getJclLog().info(msg);
    }
    
    private static void jclLogDebug(String msg) {
        getJclLog().debug(msg);
    }
    
    private static void jclLogDebug(String msg, Throwable t) {
        getJclLog().debug(msg, t);
    }
    
    private static Log getJclLog() {
        return LogFactory.getLog(LOG_NAME);
    }   

    static void setLogWarning(Object logObject, String className) throws Exception {
        if (logObject instanceof Log) {
            Log log = (Log)logObject;
            if (log.isInfoEnabled()) {// && !log.isDebugEnabled()) {
                if (log.getClass().getClassLoader() != BootstrapLog.class.getClassLoader()) {
                    log.info("Changing log level for shared class "+className+" to WARNING: to view (verbose) INFO level logging, please edit glassbox.properties in the Glassbox web application to disable overriding this setting.");
                }
                if (log instanceof Jdk14Logger) {
                    setWarningLevelOnJdkLogger((Jdk14Logger)log);
                } else {
                    // try log4j - the only other one we support
                    try {
                        setWarningLevelOnLog4j(log);
                    } catch (Throwable t) { ; } // I guess not!
                }
            }
        }
    }

    static void setWarningLevelOnLog4j(Log log) throws Exception {
        // logic to handle JBoss's additional Log4j proxy...
        Object log4jProxy = InitializeLog.getField(log, "theLogger");
        if (log4jProxy instanceof Log) {
            log = (Log)log4jProxy;
        }
        Category nativeLogger = (Category)InitializeLog.getField(log, "logger");
        
        if (nativeLogger != null) {
            nativeLogger.setLevel(Level.WARN);
        }
    }

    static void setWarningLevelOnJdkLogger(Jdk14Logger log) throws Exception {
        Logger nativeLogger = (Logger)InitializeLog.getField(log, "logger");
        
        if (nativeLogger != null) {
            nativeLogger.setLevel(java.util.logging.Level.WARNING);
        }
    }
    
//    private void initializeLog4j(Logger loggers[]) {
//        String fileAppenders[] = { "file", "diagnostics", "iterations" };
//        
//        // report the first error, since we want to handle the file logger first and to report it as the error for general config. problems
//        String firstWarning = null;
//        String firstError = null;
//        for (int i = 0; i < fileAppenders.length; i++) {
//            try {
//                String appenderName = fileAppenders[i];
//                org.apache.log4j.Logger logger = 
//                Appender appender = (Appender)loggers[i].getAppender(appenderName);
//                if (appender instanceof FileAppender) {
//                    String warnMsg = configureLog4jLogger(homeDirectory, (FileAppender)appender);
//                    if (firstWarning == null) {
//                        firstWarning = warnMsg;
//                    }
//                } else if (firstError == null) {
//                    firstError = "Glassbox logging not configured properly, missing appender: "+appenderName;
//                }
//            } catch (IllegalStateException e) {
//                if (firstError == null) {
//                    firstError = e.getMessage();
//                }
//            }
//        }
//        if (firstError != null) {
//            System.err.println(firstError);
//        } else if (firstWarning != null) {
//            System.err.println(firstWarning);
//        }
//    }   
//
//    /** reconfigure file appenders to write in the glassbox installation directory */ 
//    private String configureLogger(File homeDirectory, FileAppender appender) throws IllegalStateException {
//        String result = null;
//        
//        File logFile = new File(homeDirectory, appender.getFile());
//        boolean success = false;
//        try {
//            success = logFile.canWrite() || logFile.createNewFile();
//        } catch (IOException e) {
//            // can't write?
//        }
//        if (!success) {
//            // fallback on current working directory
//            logFile = new File(appender.getFile());
//            result = "Glassbox logger unable to write to "+homeDirectory+" trying to write log file to "+getPath(logFile);
//        }
//        try {
//            appender.setFile(logFile.getPath(), true, true, 8*1024);
//        } catch (IOException e) {
//            throw new IllegalStateException("Glassbox logger also unable to write to "+getPath(logFile)+
//                    ".\nFile logger disabled: Glassbox continuing with initialization.");
//            // allow it to proceed
//        }
//        return result;
//    }
//    
//    private String getPath(File logFile) {
//        try {
//            return logFile.getCanonicalPath();
//        } catch (IOException e) {
//            return logFile.getAbsolutePath();
//        }
//    }

}
