/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.logger;

import org.apache.avalon.framework.logger.Logger;
/**
 * An extension to the <code>Logger</code> interface to allow a team of
 * supplementary loggers to associated with a main logger.
 * It is the main logger which handles the standard logger requests.
 *
 * @author Richard G Clark
 * @version $Revision: 1.2 $ $Date: 2003/05/02 14:23:57 $
 */
public interface LoggerTeam extends Logger
{
    /**
     * Returns the memeber logger associated with the tag.
     * This operation will always return a logger as an undefined tag
     * is implicitly associated with a <code>NullLogger</code>.
     *
     * @param tag the logger's tag
     * @return a logger
     */
    Logger getMember( Object tag );

    /**
     * Creates a new child <code>LoggerTeam</code> with the specified name.
     * This operation has the same function as the <code>getChildLogger</code>
     * operation, but avoids the need to cast to <code>LoggerTeam</code>.
     *
     * @param name the new child's name
     * @return the new logger
     * @throws IllegalArgumentException if name is an empty string
     * @see Logger#getChildLogger(String)
     */
    LoggerTeam createChildLoggerTeam( String name );

    /**
     * Creates a prefixing version of this <code>LoggerTeam</code>.
     *
     * @return the new logger
     */
    LoggerTeam createPrefixingLoggerTeam();

    /**
     * The current set of standard tags used to identify member loggers.
     *
     * @author Richard G Clark
     * @version $Revision: 1.2 $ $Date: 2003/05/02 14:23:57 $
     */
    public interface StandardTags
    {
        /**
         * The tag which identifies the main logger.
         */
        Object MAIN_LOGGER_TAG = new Object();

        /**
         * The tag which identifies a diagnostics logger.
         */
        Object DIAGNOSTIC_LOGGER_TAG = new Object();

        /**
         * @link dependency
         * @definedIn
         */
        /*# LoggerTeam lnkLoggerTeam; */
    }
}

