/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.org.sl4j;

import org.apache.commons.logging.Log;

public class CommonsLoggingAdapter implements Logger {
    final org.apache.commons.logging.Log logger;
    final String name;

    CommonsLoggingAdapter(Log logger, String name) {
        this.logger = logger;
        this.name = name;
    }
    
    public void debug(String msg) {
        logger.debug(msg);
    }

    public void debug(String format, Object arg) {
        if (logger.isDebugEnabled()) {
            String msgStr = MessageFormatter.format(format, arg);
            logger.debug(msgStr);
        }
    }

    public void debug(String format, Object arg1, Object arg2) {
        if (logger.isDebugEnabled()) {
            String msgStr = MessageFormatter.format(format, arg1, arg2);
            logger.debug(msgStr);
        }
    }

    public void debug(String format, Object[] argArray) {
        if (logger.isDebugEnabled()) {
            String msgStr = MessageFormatter.arrayFormat(format, argArray);
            logger.debug(msgStr);
        }
    }

    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    public void error(String msg) {
        logger.error(msg);
    }

    public void error(String format, Object arg) {
        if (logger.isErrorEnabled()) {
            String msgStr = MessageFormatter.format(format, arg);
            logger.error(msgStr);
        }
    }

    public void error(String format, Object arg1, Object arg2) {
        if (logger.isErrorEnabled()) {
            String msgStr = MessageFormatter.format(format, arg1, arg2);
            logger.error(msgStr);
        }
    }

    public void error(String format, Object[] argArray) {
        if (logger.isErrorEnabled()) {
            String msgStr = MessageFormatter.format(format, argArray);
            logger.error(msgStr);
        }
    }

    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    public String getName() {
        return name;
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void info(String format, Object arg) {
        if (logger.isInfoEnabled()) {
            String msgStr = MessageFormatter.format(format, arg);
            logger.info(msgStr);
        }
    }

    public void info(String format, Object arg1, Object arg2) {
        if (logger.isInfoEnabled()) {
            String msgStr = MessageFormatter.format(format, arg1, arg2);
            logger.info(msgStr);
        }
    }

    public void info(String format, Object[] argArray) {
        if (logger.isInfoEnabled()) {
            String msgStr = MessageFormatter.format(format, argArray);
            logger.info(msgStr);
        }
    }

    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    public void warn(String msg) {
        logger.warn(msg);
    }

    public void warn(String format, Object arg) {
        if (logger.isWarnEnabled()) {
            String msgStr = MessageFormatter.format(format, arg);
            logger.warn(msgStr);
        }
    }

    public void warn(String format, Object arg1, Object arg2) {
        if (logger.isWarnEnabled()) {
            String msgStr = MessageFormatter.format(format, arg1, arg2);
            logger.warn(msgStr);
        }
    }

    public void warn(String format, Object[] argArray) {
        if (logger.isWarnEnabled()) {
            String msgStr = MessageFormatter.format(format, argArray);
            logger.warn(msgStr);
        }
    }

    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public Object getNativeLogger() {
        return logger;
    }
}
