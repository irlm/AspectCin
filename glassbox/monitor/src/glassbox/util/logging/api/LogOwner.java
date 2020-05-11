/********************************************************************
 * Copyright (c) 2005 Glassbox Corporation, Contributors.
 * All rights reserved. 
 * This program along with all accompanying source code and applicable materials are made available 
 * under the terms of the Lesser Gnu Public License v2.1, 
 * which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 *  
 * Contributors: 
 *     Ron Bodkin     initial implementation 
 *******************************************************************/
package glassbox.util.logging.api;

import glassbox.util.org.sl4j.Logger;

/** 
 * Interface of operations that an object that owns a log can perform on that log.
 * This would be cleaner with Java 5's static import feature, with advice defined on calls.
 */
public interface LogOwner {
    boolean isDebugEnabled();
    boolean isWarnEnabled();
    void logError(String message);
    void logError(String message, Throwable throwable);
    void logWarn(String message);
    void logWarn(String message, Throwable throwable);
    void logInfo(String message);
    void logInfo(String message, Throwable throwable);
    void logDebug(String message);
    void logDebug(String message, Throwable throwable);
    Logger getLogger();
}
