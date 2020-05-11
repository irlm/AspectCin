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
 * A logger proxy that uses a separate control to evaluate the logging
 * priority.
 *
 * @author Richard G Clark
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:28:45 $
 */
public final class ControllableLogger implements Logger
{
    /**
     * The logger.
     * @link aggregation
     */
    private final Logger m_logger;

    /**
     * The control.
     * @link aggregation
     */
    private final Control m_control;

    /**
     * Constructs this object with the specified logger and control.
     *
     * @param logger the logger
     * @param control the control
     */
    public ControllableLogger( final Logger logger, final Control control )
    {
        m_logger = logger;
        m_control = control;
    }

    /**
     * @see Logger#getChildLogger(String)
     */
    public Logger getChildLogger( final String name )
    {
        return new ControllableLogger( m_logger.getChildLogger( name ), m_control );
    }

    /**
     * @see Logger#debug(String)
     */
    public void debug( final String message )
    {
        if ( isDebugEnabled() )
        {
            m_logger.debug( message );
        }
    }

    /**
     * @see Logger#debug(String,Throwable)
     */
    public void debug( final String message, final Throwable throwable )
    {
        if ( isDebugEnabled() )
        {
            m_logger.debug( message, throwable );
        }
    }

    /**
     * @see Logger#isDebugEnabled()
     */
    public boolean isDebugEnabled()
    {
        return m_control.isDebugEnabled();
    }

    /**
     * @see Logger#info(String)
     */
    public void info( final String message )
    {
        if ( isInfoEnabled() )
        {
            m_logger.info( message );
        }
    }

    /**
     * @see Logger#info(String,Throwable)
     */
    public void info( final String message, final Throwable throwable )
    {
        if ( isInfoEnabled() )
        {
            m_logger.info( message, throwable );
        }
    }

    /**
     * @see Logger#isInfoEnabled()
     */
    public boolean isInfoEnabled()
    {
        return m_control.isInfoEnabled();
    }

    /**
     * @see Logger#warn(String)
     */
    public void warn( final String message )
    {
        if ( isWarnEnabled() )
        {
            m_logger.warn( message );
        }
    }

    /**
     * @see Logger#warn(String,Throwable)
     */
    public void warn( final String message, final Throwable throwable )
    {
        if ( isWarnEnabled() )
        {
            m_logger.warn( message, throwable );
        }
    }

    /**
     * @see Logger#isWarnEnabled()
     */
    public boolean isWarnEnabled()
    {
        return m_control.isWarnEnabled();
    }

    /**
     * @see Logger#error(String)
     */
    public void error( final String message )
    {
        if ( isErrorEnabled() )
        {
            m_logger.error( message );
        }
    }

    /**
     * @see Logger#error(String,Throwable)
     */
    public void error( final String message, final Throwable throwable )
    {
        if ( isErrorEnabled() )
        {
            m_logger.error( message, throwable );
        }
    }

    /**
     * @see Logger#isErrorEnabled()
     */
    public boolean isErrorEnabled()
    {
        return m_control.isErrorEnabled();
    }

    /**
     * @see Logger#fatalError(String)
     */
    public void fatalError( final String message )
    {
        if ( isFatalErrorEnabled() )
        {
            m_logger.fatalError( message );
        }
    }

    /**
     * @see Logger#fatalError(String,Throwable)
     */
    public void fatalError( final String message, final Throwable throwable )
    {
        if ( isFatalErrorEnabled() )
        {
            m_logger.fatalError( message, throwable );
        }
    }

    /**
     * @see Logger#isFatalErrorEnabled()
     */
    public boolean isFatalErrorEnabled()
    {
        return m_control.isFatalErrorEnabled();
    }

    /**
     * A priority control abstraction for loggers.
     */
    public interface Control
    {
        /**
         * @see Logger#isDebugEnabled()
         */
        boolean isDebugEnabled();

        /**
         * @see Logger#isInfoEnabled()
         */
        boolean isInfoEnabled();

        /**
         * @see Logger#isWarnEnabled()
         */
        boolean isWarnEnabled();

        /**
         * @see Logger#isErrorEnabled()
         */
        boolean isErrorEnabled();

        /**
         * @see Logger#isFatalErrorEnabled()
         */
        boolean isFatalErrorEnabled();

        /**
         * @link dependency
         * @definedIn
         */
        /*# ControllableLogger lnkControllableLogger; */
    }
}

