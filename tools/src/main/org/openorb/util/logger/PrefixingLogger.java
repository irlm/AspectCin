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
 * A lightweight logger decorator that uses prefixes for child loggers
 *
 * @author Richard G Clark
 * @version $Revision: 1.3 $ $Date: 2003/08/27 17:01:45 $
 */
public final class PrefixingLogger implements Logger
{
    /**
     * The logger.
     * @link aggregation
     */
    private final Logger m_logger;

    private final String m_prefix;
    private final String m_prefixString;

    /**
     * Constructs this object with the specified logger and control.
     *
     * @param logger the logger
     */
    public PrefixingLogger( final Logger logger )
    {
        this( logger, null );
    }

    /**
     * Constructs this object with the specified logger and control.
     *
     * @param logger the logger
     * @param prefix the inital prefix
     */
    public PrefixingLogger( final Logger logger, final String prefix )
    {
        m_logger = logger;
        m_prefix = prefix;
        m_prefixString = ( null == prefix ) ? null : ( "(" + prefix + ") " );
    }

    /**
     * @see Logger#getChildLogger(String)
     */
    public Logger getChildLogger( final String name )
    {
        return new PrefixingLogger( m_logger,
                ( null == m_prefix ) ? name : ( m_prefix + name ) );
    }

    private String prefixMessage( final String message )
    {
        return ( null == m_prefixString ) ? message : ( m_prefixString + message );
    }

    /**
     * @see Logger#debug(String)
     */
    public void debug( final String message )
    {
        if ( !isDebugEnabled() )
        {
            return;
        }
        m_logger.debug( prefixMessage( message ) );
    }

    /**
     * @see Logger#debug(String,Throwable)
     */
    public void debug( final String message, final Throwable throwable )
    {
        if ( !isDebugEnabled() )
        {
            return;
        }
        m_logger.debug( prefixMessage( message ), throwable );
    }

    /**
     * @see Logger#isDebugEnabled()
     */
    public boolean isDebugEnabled()
    {
        return m_logger.isDebugEnabled();
    }

    /**
     * @see Logger#info(String)
     */
    public void info( final String message )
    {
        if ( !isInfoEnabled() )
        {
            return;
        }
        m_logger.info( prefixMessage( message ) );
    }

    /**
     * @see Logger#info(String,Throwable)
     */
    public void info( final String message, final Throwable throwable )
    {
        if ( !isInfoEnabled() )
        {
            return;
        }
        m_logger.info( prefixMessage( message ), throwable );
    }

    /**
     * @see Logger#isInfoEnabled()
     */
    public boolean isInfoEnabled()
    {
        return m_logger.isInfoEnabled();
    }

    /**
     * @see Logger#warn(String)
     */
    public void warn( final String message )
    {
        if ( !isWarnEnabled() )
        {
            return;
        }
        m_logger.warn( prefixMessage( message ) );
    }

    /**
     * @see Logger#warn(String,Throwable)
     */
    public void warn( final String message, final Throwable throwable )
    {
        if ( !isWarnEnabled() )
        {
            return;
        }
        m_logger.warn( prefixMessage( message ), throwable );
    }

    /**
     * @see Logger#isWarnEnabled()
     */
    public boolean isWarnEnabled()
    {
        return m_logger.isWarnEnabled();
    }

    /**
     * @see Logger#error(String)
     */
    public void error( final String message )
    {
        if ( !isErrorEnabled() )
        {
            return;
        }
        m_logger.error( prefixMessage( message ) );
    }

    /**
     * @see Logger#error(String,Throwable)
     */
    public void error( final String message, final Throwable throwable )
    {
        if ( !isErrorEnabled() )
        {
            return;
        }
        m_logger.error( prefixMessage( message ), throwable );
    }

    /**
     * @see Logger#isErrorEnabled()
     */
    public boolean isErrorEnabled()
    {
        return m_logger.isErrorEnabled();
    }

    /**
     * @see Logger#fatalError(String)
     */
    public void fatalError( final String message )
    {
        if ( !isFatalErrorEnabled() )
        {
            return;
        }
        m_logger.fatalError( prefixMessage( message ) );
    }

    /**
     * @see Logger#fatalError(String,Throwable)
     */
    public void fatalError( final String message, final Throwable throwable )
    {
        if ( !isFatalErrorEnabled() )
        {
            return;
        }
        m_logger.fatalError( prefixMessage( message ), throwable );
    }

    /**
     * @see Logger#isFatalErrorEnabled()
     */
    public boolean isFatalErrorEnabled()
    {
        return m_logger.isFatalErrorEnabled();
    }
}

