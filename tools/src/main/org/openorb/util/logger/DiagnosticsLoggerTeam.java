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
 * Provides a main and diagnostics logger team.
 *
 * @author Richard G Clark
 * @version $Revision: 1.4 $ $Date: 2003/05/02 15:40:03 $
 */
public final class DiagnosticsLoggerTeam extends AbstractLoggerTeam
{
    /**
     * The diagnostics logger.
     * @link aggregation
     */
    private final Logger m_diagnosticsLogger;

    /**
     * Constructs this object with the specified main and diagnostics logger.
     * If no loggers are supplied then the null logger is used.
     *
     * @param mainLogger the main logger of the team
     * @param diagnosticsLogger the diagnostics logger of the team
     */
    public DiagnosticsLoggerTeam( final Logger mainLogger,
            final Logger diagnosticsLogger )
    {
        super( mainLogger );
        m_diagnosticsLogger = ( null == diagnosticsLogger ) ? NULL_LOGGER
                : diagnosticsLogger;
    }

    /**
     * @see AbstractLoggerTeam#isNullLogger()
     */
    protected boolean isNullLogger()
    {
        return super.isNullLogger() && m_diagnosticsLogger.equals( NULL_LOGGER );
    }

    /**
     * @see LoggerTeam#getMember(Object)
     */
    public Logger getMember( final Object tag )
    {
        return DIAGNOSTIC_LOGGER_TAG.equals( tag ) ? m_diagnosticsLogger
                : super.getMember( tag );
    }

    /**
     * @see LoggerTeam#createChildLoggerTeam(String)
     */
    public LoggerTeam createChildLoggerTeam( final String name )
    {
        return isNullLogger() ? NULL_LOGGER : new DiagnosticsLoggerTeam(
                getMember( MAIN_LOGGER_TAG ).getChildLogger( name ),
                getMember( DIAGNOSTIC_LOGGER_TAG ).getChildLogger( name ) );
    }

    /**
     * @see LoggerTeam#createPrefixingLoggerTeam()
     */
    public LoggerTeam createPrefixingLoggerTeam()
    {
        return isNullLogger() ? NULL_LOGGER : new DiagnosticsLoggerTeam(
                createPrefixingLogger( getMember( MAIN_LOGGER_TAG ) ),
                createPrefixingLogger( getMember( DIAGNOSTIC_LOGGER_TAG ) ) );
    }

    /**
     * Narrow a <code>Logger</code> to a <code>LoggerTeam</code> by either
     * casting or wrapping in and instance of
     * <code>DiagnosticsLoggerTeam</code> using the specified child logger.
     *
     * @param logger the logger to be narrowed
     * @param diagnosticsParent the parent logger to create the child from
     * @param childName the child name to be used
     * @return the narrowed logger
     */
    public static LoggerTeam narrow( final Logger logger,
            final Logger diagnosticsParent, final String childName )
    {
        if ( logger instanceof LoggerTeam )
        {
            return ( LoggerTeam ) logger;
        }
        return new DiagnosticsLoggerTeam( logger,
                diagnosticsParent.getChildLogger( childName ) );
    }
}

