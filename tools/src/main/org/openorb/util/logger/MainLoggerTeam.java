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
 * Provides a main logger team.
 *
 * @author Richard G Clark
 * @version $Revision: 1.3 $ $Date: 2003/05/02 15:40:03 $
 */
public final class MainLoggerTeam extends AbstractLoggerTeam
{
    /**
     * Constructs this object with the specified main logger.
     * If no main logger is supplied then the null logger is used.
     *
     * @param mainLogger the main logger of the team
     */
    public MainLoggerTeam( final Logger mainLogger )
    {
        super( mainLogger );
    }

    /**
     * @see LoggerTeam#createChildLoggerTeam(String)
     */
    public LoggerTeam createChildLoggerTeam( final String name )
    {
        return new MainLoggerTeam( getMember( MAIN_LOGGER_TAG ).getChildLogger( name ) );
    }

    /**
     * @see LoggerTeam#createPrefixingLoggerTeam()
     */
    public LoggerTeam createPrefixingLoggerTeam()
    {
        return isNullLogger() ? NULL_LOGGER : new MainLoggerTeam(
                createPrefixingLogger( getMember( MAIN_LOGGER_TAG ) ) );
    }

    /**
     * Narrow a <code>Logger</code> to a <code>LoggerTeam</code> by either
     * casting or wrapping in and instance of <code>MainLoggerTeam</code>
     *
     * @param logger the logger to be narrowed
     * @return the narrowed logger
     */
    public static LoggerTeam narrow( final Logger logger )
    {
        if ( logger instanceof LoggerTeam )
        {
            return ( LoggerTeam ) logger;
        }
        return new MainLoggerTeam( logger );
    }

}

