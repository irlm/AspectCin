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
 * A null implementation of <code>LoggerTeam</code>.
 *
 * @author Richard G Clark
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:28:45 $
 */
public final class NullLoggerTeam implements LoggerTeam
{
    /**
     * The singleton instance of the class.
     */
    private static final LoggerTeam INSTANCE = new NullLoggerTeam();

    /**
     * Constructs the a null logger
     */
    private NullLoggerTeam()
    {
    }

    public static LoggerTeam getInstance()
    {
        return INSTANCE;
    }

    /**
     * @see LoggerTeam#getMember(Object)
     */
    public Logger getMember( final Object tag )
    {
        return this;
    }

    /**
     * @see Logger#getChildLogger(String)
     */
    public Logger getChildLogger( final String name )
    {
        return this;
    }


    /**
     * @see LoggerTeam#createChildLoggerTeam(String)
     */
    public LoggerTeam createChildLoggerTeam( final String name )
    {
        return this;
    }

    /**
     * @see LoggerTeam#createPrefixingLoggerTeam()
     */
    public LoggerTeam createPrefixingLoggerTeam()
    {
        return this;
    }

    /**
     * @see Logger#debug(String)
     */
    public void debug( final String message )
    {
    }

    /**
     * @see Logger#debug(String,Throwable)
     */
    public void debug( final String message, final Throwable throwable )
    {
    }

    /**
     * @see Logger#isDebugEnabled()
     */
    public boolean isDebugEnabled()
    {
        return false;
    }

    /**
     * @see Logger#info(String)
     */
    public void info( final String message )
    {
    }

    /**
     * @see Logger#info(String,Throwable)
     */
    public void info( final String message, final Throwable throwable )
    {
    }

    /**
     * @see Logger#isInfoEnabled()
     */
    public boolean isInfoEnabled()
    {
        return false;
    }

    /**
     * @see Logger#warn(String)
     */
    public void warn( final String message )
    {
    }

    /**
     * @see Logger#warn(String,Throwable)
     */
    public void warn( final String message, final Throwable throwable )
    {
    }

    /**
     * @see Logger#isWarnEnabled()
     */
    public boolean isWarnEnabled()
    {
        return false;
    }

    /**
     * @see Logger#error(String)
     */
    public void error( final String message )
    {
    }

    /**
     * @see Logger#error(String,Throwable)
     */
    public void error( final String message, final Throwable throwable )
    {
    }

    /**
     * @see Logger#isErrorEnabled()
     */
    public boolean isErrorEnabled()
    {
        return false;
    }

    /**
     * @see Logger#fatalError(String)
     */
    public void fatalError( final String message )
    {
    }

    /**
     * @see Logger#fatalError(String,Throwable)
     */
    public void fatalError( final String message, final Throwable throwable )
    {
    }

    /**
     * @see Logger#isFatalErrorEnabled()
     */
    public boolean isFatalErrorEnabled()
    {
        return false;
    }

}

