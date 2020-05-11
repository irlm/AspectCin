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
 * Provides a common base class for <code>LoggerTeam</code> implementations.
 *
 * @author Richard G Clark
 * @version $Revision: 1.3 $ $Date: 2003/05/02 15:40:03 $
 */
public abstract class AbstractLoggerTeam implements LoggerTeam,
        LoggerTeam.StandardTags
{
    /**
     * The null logger to be used for undefined tags.
     */
    protected static final LoggerTeam NULL_LOGGER = NullLoggerTeam.getInstance();

    /**
     * The main logger.
     * @link aggregation
     */
    private final Logger m_mainLogger;

    /**
     * Constructs this object with the specified main logger.
     * If no main logger is supplied then the null logger is used.
     *
     * @param mainLogger the main logger of the team
     */
    protected AbstractLoggerTeam( final Logger mainLogger )
    {
        m_mainLogger = ( null == mainLogger ) ? NULL_LOGGER : mainLogger;
    }

    /**
     * Indicates if this logger team is made up of only null loggers.
     *
     * @return <code>true</code> if members are all null loggers,
     *         <code>false</code> otherwise
     */
    protected boolean isNullLogger()
    {
        return m_mainLogger.equals( NULL_LOGGER );
    }

    /**
     * Creates a prefixing version of the supplied logger.
     *
     * @param logger the backing logger
     * @return the new logger
     */
    protected final Logger createPrefixingLogger( final Logger logger )
    {
        return NULL_LOGGER.equals( logger ) ? ( Logger ) NULL_LOGGER
                : ( Logger ) new PrefixingLogger( logger );
    }

    /**
     * @see LoggerTeam#getMember(Object)
     */
    public Logger getMember( final Object tag )
    {
        return MAIN_LOGGER_TAG.equals( tag ) ? m_mainLogger : NULL_LOGGER;
    }

    /**
     * @see Logger#getChildLogger(String)
     */
    public final Logger getChildLogger( final String name )
    {
        return createChildLoggerTeam( name );
    }

    /**
     * @see Logger#debug(String)
     */
    public final void debug( final String message )
    {
        m_mainLogger.debug( message );
    }

    /**
     * @see Logger#debug(String,Throwable)
     */
    public final void debug( final String message, final Throwable throwable )
    {
        m_mainLogger.debug( message, throwable );
    }

    /**
     * @see Logger#isDebugEnabled()
     */
    public final boolean isDebugEnabled()
    {
        return m_mainLogger.isDebugEnabled();
    }

    /**
     * @see Logger#info(String)
     */
    public final void info( final String message )
    {
        m_mainLogger.info( message );
    }

    /**
     * @see Logger#info(String,Throwable)
     */
    public final void info( final String message, final Throwable throwable )
    {
        m_mainLogger.info( message, throwable );
    }

    /**
     * @see Logger#isInfoEnabled()
     */
    public final boolean isInfoEnabled()
    {
        return m_mainLogger.isInfoEnabled();
    }

    /**
     * @see Logger#warn(String)
     */
    public final void warn( final String message )
    {
        m_mainLogger.warn( message );
    }

    /**
     * @see Logger#warn(String,Throwable)
     */
    public final void warn( final String message, final Throwable throwable )
    {
        m_mainLogger.warn( message, throwable );
    }

    /**
     * @see Logger#isWarnEnabled()
     */
    public final boolean isWarnEnabled()
    {
        return m_mainLogger.isWarnEnabled();
    }

    /**
     * @see Logger#error(String)
     */
    public final void error( final String message )
    {
        m_mainLogger.error( message );
    }

    /**
     * @see Logger#error(String,Throwable)
     */
    public final void error( final String message, final Throwable throwable )
    {
        m_mainLogger.error( message, throwable );
    }

    /**
     * @see Logger#isErrorEnabled()
     */
    public final boolean isErrorEnabled()
    {
        return m_mainLogger.isErrorEnabled();
    }

    /**
     * @see Logger#fatalError(String)
     */
    public final void fatalError( final String message )
    {
        m_mainLogger.fatalError( message );
    }

    /**
     * @see Logger#fatalError(String,Throwable)
     */
    public final void fatalError( final String message, final Throwable throwable )
    {
        m_mainLogger.fatalError( message, throwable );
    }

    /**
     * @see Logger#isFatalErrorEnabled()
     */
    public final boolean isFatalErrorEnabled()
    {
        return m_mainLogger.isFatalErrorEnabled();
    }

}

