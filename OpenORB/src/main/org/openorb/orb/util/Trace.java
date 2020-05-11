/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.util;

import java.io.OutputStream;

import java.util.Properties;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogKitLogger;

import org.apache.log.Hierarchy;
import org.apache.log.Priority;

import org.apache.log.format.ExtendedPatternFormatter;
import org.apache.log.output.io.StreamTarget;

import org.openorb.util.HexPrintStream;

import org.openorb.util.logger.ControllableLogger;

/**
 * This class provides methods to display trace messages.
 * The trace levels are defined by the Avalon LogKit Priority
 * class.
 * In order to control the verbostity of the DEBUG priority
 * there are three levels available:
 * <pre>
 * OpenORB Debug Verbostity    Description
 * ---------------------------------------------------------------------
 * - level 0 / LOW   : low verbosity
 * - level 1 / MEDIUM: medium verbosity
 * - level 2 / HIGH  : high verbosity
 * </pre>
 *
 * @author Jerome Daniel
 * @author Stephen McConnell
 * @author Michael Rumpf
 * @author Richard G Clark
 */
public final class Trace
{
    /**
     * Utility class, do not instantiate.
     */
    private Trace()
    {
    }

    /**
     * A logger control that uses the debug level setting of this class.
     */
    public static final ControllableLogger.Control LOGGER_CONTROL
            = new LoggerControl();

    /**
     * The sync object for access and modification of the default logger.
     */
    private static final Object LOGGER_SYNC = new byte[0];

    /**
     * The default logging format.
     */
    private static final String DEFAULT_FORMAT
            = "[%{thread}] [%5.5{priority}] (%{category}): %{message}\\n%{throwable}";

    /**
     * The name of the default assertion logger.
     */
    private static final String DEFAULT_ASSERTION_LOGGER_NAME
            = "default-assertion-logger";

    /**
     * Debug verbosity OFF (Only applies when priority DEBUG).
     */
    public static final int OFF = 0;
    /**
     * Debug verbosity LOW (Only applies when priority DEBUG).
     */
    public static final int LOW = 1;
    /**
     * Debug verbosity MEDIUM (Only applies when priority DEBUG).
     */
    public static final int MEDIUM = 2;
    /**
     * Debug verbosity HIGH (Only applies when priority DEBUG).
     */
    public static final int HIGH = 3;

    /**
     * The names of the internal verbosity levels.
     */
    private static final String[] LEVEL_NAMES = new String[]
            { "OFF", "LOW", "MEDIUM", "HIGH" };

    /**
     * The debug verbosity level.
     */
    private static volatile int s_level = OFF;

    /**
     * Output stream for logging.
     */
    private static OutputStream s_os = System.out;

    /**
     * The logger hierarchy instance.
     */
    private static Hierarchy s_hierarchy;

    /**
     * The logging stream target.
     */
    private static StreamTarget s_target;

    /**
     * The logger assembled from the hierarchy and the stream target objects.
     */
    private static Logger s_logger;

    /**
     * Returns the default logger.
     *
     * @return a logger
     */
    public static Logger getLogger()
    {
        return getLogger( null );
    }

    /**
     * Returns the default logger.
     *
     * @param props Properties that may contain a
     * "openorb.debug.trace=FATAL|ERROR|WARN|INFO|DEBUG or 0|1|2|3|4"
     * property which uses the value to create a priority.
     * @return A logger instance.
     */
    public static Logger getLogger( Properties props )
    {
        synchronized ( LOGGER_SYNC )
        {
            if ( null == s_logger )
            {
                String strPriority = null;
                if ( props != null )
                {
                    strPriority = ( String ) props.get( "openorb.debug.trace" );
                }
                if ( strPriority == null )
                {
                    strPriority = System.getProperty( "openorb.debug.trace" );
                }
                Priority priority = getPriorityFromName( strPriority == null
                      ? "ERROR" : strPriority );
                s_logger = createLogger( priority );
            }
            return s_logger;
        }
    }

    /**
     * Set another output stream for the logger. This is important
     * when a mapping between different logging APIs is necessary.
     *
     * @param os The OutputStream to send log output to.
     */
    public static void setLoggerOutputStream( OutputStream os )
    {
        s_os = os;
    }

    /**
     * Creates an instance of logger.
     *
     * @param priority the priority of the logger to create
     */
    private static Logger createLogger( Priority priority )
    {
        s_hierarchy = Hierarchy.getDefaultHierarchy();
        s_target = new StreamTarget( s_os, new ExtendedPatternFormatter( DEFAULT_FORMAT ) );
        s_hierarchy.setDefaultPriority ( priority );
        s_hierarchy.setDefaultLogTarget( s_target );
        return new LogKitLogger( s_hierarchy.getLoggerFor( "orb" ) );
    }

    /**
     * Creates a new instance of the logger.
     * this is necessary in case of a priority change !
     *
     * @param priority the priority of the logger to create
     */
     public static Logger getNewLogger( Priority priority )
     {
         s_logger = createLogger( priority );
         return s_logger;
     }

    /**
     * Sets the default logger.
     *
     * @param logger A logger instance or null.
     */
    public static void setLogger( final Logger logger )
    {
        synchronized ( LOGGER_SYNC )
        {
            s_logger = logger;
        }
    }


    /**
     * Convert the stringified representation into the trace priority.
     * This method always returns a valid priority. If there is no match
     * found or the parameter is null the lowest priority FATAL_ERROR
     * is assumed.
     *
     * @param priority The priority name.
     * @return The priority instance corresponding to the name or
     * FATAL_ERROR when the name can't be mapped.
     */
    public static Priority getPriorityFromName( final String priority )
    {
        if ( priority == null )
        {
            return Priority.FATAL_ERROR;
        }
        if ( priority.compareToIgnoreCase( "FATAL" ) == 0
             || priority.equals( "0" ) )
        {
            return Priority.FATAL_ERROR;
        }
        if ( priority.compareToIgnoreCase( "ERROR" ) == 0
             || priority.equals( "1" ) )
        {
            return Priority.ERROR;
        }
        if ( priority.compareToIgnoreCase( "WARN" ) == 0
             || priority.equals( "2" ) )
        {
            return Priority.WARN;
        }
        if ( priority.compareToIgnoreCase( "INFO" ) == 0
             || priority.equals( "3" ) )
        {
            return Priority.INFO;
        }
        if ( priority.compareToIgnoreCase( "DEBUG" ) == 0
             || priority.equals( "4" ) )
        {
            return Priority.DEBUG;
        }
        return Priority.FATAL_ERROR;
    }

    /**
     * Convert the stringified representation into the debug level.
     * This method always returns a valid debug level. If there is no match
     * found or the parameter is null the lowest debug level OFF is assumed.
     *
     * @param level The name of the debug level.
     * @return The corresponding integer value or OFF if the level
     * name is not valid.
     */
    public static int getDebugLevelFromName( final String level )
    {
        if ( level == null )
        {
            return OFF;
        }
        if ( level.compareToIgnoreCase( "OFF" ) == 0
              || level.equals( "0" ) )
        {
            return OFF;
        }
        if ( level.compareToIgnoreCase( "LOW" ) == 0
              || level.equals( "1" ) )
        {
            return LOW;
        }
        if ( level.compareToIgnoreCase( "MEDIUM" ) == 0
              || level.equals( "2" ) )
        {
            return MEDIUM;
        }
        if ( level.compareToIgnoreCase( "HIGH" ) == 0
              || level.equals( "3" ) )
        {
            return HIGH;
        }
        return OFF;
    }

    /**
     * Set the debug verbosity level. This applies to DEBUG Priority only.
     * This method is called by the OpenORBLoader following ORB
     * parameterization.
     *
     * @param newLevel The new debug verbosity level for this process.
     */
    public static void setDebugLevel( final int newLevel )
    {
        s_level = newLevel;
    }

    /**
     * Get the debug verbosity level.
     *
     * @return The debug verbosity level.
     */
    public static int getDebugLevel()
    {
        return s_level;
    }

    /**
     * Get the stringified representation of the debug verbosity level.
     *
     * @param level The debug verbosity's integer value.
     * @return The stringified debug verbosity level.
     */
    public static String getNameOfDebugLevel( final int level )
    {
        if ( ( level < 0 ) || ( LEVEL_NAMES.length <= level ) )
        {
            return "UNKNOWN";
        }
        return LEVEL_NAMES[ level ];
    }

    /**
     * Returns true if the trace level is at equal to or greater
     * than the supplied value.
     *
     * @param value the debug verbosity level to test.
     * @return boolean TRUE if the current verbosity level is set
     * equal or higher than the supplied level.
     */
    public static boolean isEnabled( final int value )
    {
        return s_level <= value;
    }

   /**
    * Returns true if the debugging verbosity level is set below or equal
    * to LOW.
    *
    * @return boolean True if the verbosity level is at or above LOW.
    */
    public static boolean isLow()
    {
        return s_level >= LOW;
    }

   /**
    * Returns true if the debugging verbosity level is set below or
    * equal to MEDIUM.
    * @return boolean True if the verbosity level is at or above MEDIUM.
    */
    public static boolean isMedium()
    {
        return s_level >= MEDIUM;
    }

   /**
    * Returns true is the debugging verbosity level is set below or
    * equal to HIGH.
    * @return boolean True if the verbosity level is at or above HIGH.
    */
    public static boolean isHigh()
    {
        return s_level >= HIGH;
    }

    /**
     * Convert a byte buffer into its hexadecimal string representation.
     * This new version has the great advantage that there are not two
     * arrays allocated. The given buffer is not modified at all. It
     * maybe takes some more time to convert, but that is the price for
     * having the hex dump of an arbitrary buffer.
     *
     * @param msg A text describing the buffer to be shown.
     * @param buffer The buffer to show the hex dump of.
     * @return The hex dump of the buffer.
     */
    public static String bufferToString( final String msg, final byte[] buffer )
    {
        StringBuffer out = new StringBuffer();
        if ( buffer != null && buffer.length > 0 )
        {
            int buflen = buffer.length;
            out.append(
                "\n------------------------------------------------------\n" );
            out.append(
                  "( " + msg + " ) Displaying a buffer, size = "
                  + buffer.length + "\n" );
            out.append(
                "------------------------------------------------------\n" );
            int i;
            for ( i = 0; i < buflen; i += 16 )
            {
                for ( int j = i; j < i + 16 && j < buflen; j++ )
                {
                    if ( Character.isISOControl( ( char ) ( buffer[ j ] & 0x7F ) ) )
                    {
                        out.append( '.' );
                    }
                    else
                    {
                        out.append( ( char ) buffer[ j ] );
                    }
                    if ( j == i + 7 )
                    {
                        out.append( ' ' );
                    }
                }

                // Special treatement for the last line
                if ( i + 16 > buflen )
                {
                    int rest = 16 - ( buflen % 16 );
                    for ( int j = 0; j < rest; j++ )
                    {
                        out.append( ' ' );
                    }
                    if ( rest >= 8 )
                    {
                        out.append( ' ' );
                    }
                }
                out.append( ' ' );
                out.append( ' ' );
                for ( int j = i; j < i + 16 && j < buflen; j++ )
                {
                    out.append( HexPrintStream.toHex( buffer[ j ] ) );
                    if ( j == i + 3 || j == i + 7 || j == i + 11 )
                    {
                        out.append( ' ' );
                    }
                }
                out.append( "\n" );
            }
            out.append(
                "------------------------------------------------------\n" );

        }
        return out.toString();
    }

    /**
     * Convert a byte buffer into its hexadecimal string representation.
     *
     * @param msg A text describing the buffer to be shown.
     * @param buf The StorageBuffer to show the hex dump of. ATTENTION: This
     * is an expensive operation, because internally buf.linearize() is called
     * which creates a copy of the buffer. For large buffers this can be a
     * problem concerning memory consumption.
     * @return The hex dump of the buffer.
     */
    public static String bufferToString( final String msg,
            final org.openorb.orb.io.StorageBuffer buf )
    {
        return bufferToString( msg, buf.linearize() );
    }

    /**
     * A specialization of Error for signalling the occurrence
     * of an illegal condition.
     */
    public static class IllegalConditionError
        extends Error
    {
        /**
         * Contructs the object with the passed message.
         *
         * @param message the signal's message,
         * permitted to be <code>null</code>
         */
        public IllegalConditionError( final String message )
        {
            super( message );
        }
    }

    /**
     * This method is invoked to indicate that an illegal condition
     * has occurred, typically an assertion error.
     * The method will always throw an <code>IllegalConditionError</code>
     * with the passed message.
     * <p>
     * The client can optionally
     * pass their preferred logger to be used instead the default.
     * The client also passes a message to log and throw.
     * The method will handle a <code>null</code> message gracefully so that
     * an <code>IllegalConditionError</code> will always be thrown.
     *
     * @param optionalLogger a logger to use instead of the default,
     * permitted to be <code>null</code>
     * @param message the message to log and throw,
     * permitted to be <code>null</code>
     * @return never returns
     */
    public static Error signalIllegalCondition( final Logger optionalLogger,
            final String message )
    {
        final Logger logger = ( null == optionalLogger )
              ? getAssertionLogger() : optionalLogger;
        final Error e = new IllegalConditionError( message );
        logger.fatalError( "IllegalCondition [" + message + "]", e );
        throw e;
    }

    /**
     * This method performs a sanity check of a GIOP header at the
     * specified offset. It prints a stack trace and the corrupted buffer
     * to stderr.
     *
     * @param buf The message buffer.
     * @param off The offset at which to check for the GIOP header.
     * @return True when the header is OK, false otherwise.
     */
    public static boolean isGIOPHeaderOK( byte[] buf, int off )
    {
       boolean result = true;
       if ( buf[ off + 0 ] != 'G' || buf[ off + 1 ] != 'I'
         || buf[ off + 2 ] != 'O' || buf[ off + 3 ] != 'P'
         || buf[ off + 4 ] != 1   || buf[ off + 5 ] > 2 )
       {
          System.err.println( "###############################################" );
          System.err.println( "!!Trying to send/receive corrupt GIOP message!!" );
          System.err.println( "###############################################" );
          Thread.dumpStack();
          System.err.println( "###############################################" );
          System.err.println( bufferToString( "The corrupted buffer (off=" + off + ")", buf ) );
          System.err.println( "###############################################" );
          result = false;
       }
       return result;
    }

    /**
     * Helper method for signalIllegalCondition.
     * Returns the default assertion logger.
     *
     * @return the default assertion logger
     */
    private static Logger getAssertionLogger()
    {
        return getLogger().getChildLogger( DEFAULT_ASSERTION_LOGGER_NAME );
    }

    /**
     * This class is intended to expose a stack trace for
     * debug logging. Instances of this class should not be thrown.
     */
    public static class StackSnapshot
        extends Throwable
    {
        /**
         * Creates StackSnapshot
         */
        public StackSnapshot()
        {
            super( "## Debug information - not error ##" );
        }
    }

    /**
     * A logger control that uses the debug level setting of
     * <code>Trace</code>.
     */
    private static final class LoggerControl implements ControllableLogger.Control
    {
        /**
         * Contructs a Trace logger control.
         */
        public LoggerControl()
        {
        }

        /**
         * @return <code>Trace.isHigh()</code>
         * @see Logger#isDebugEnabled()
         */
        public boolean isDebugEnabled()
        {
            return isHigh();
        }

        /**
         * @return <code>Trace.isHigh()</code>
         * @see Logger#isInfoEnabled()
         */
        public boolean isInfoEnabled()
        {
            return isHigh();
        }

        /**
         * @return <code>Trace.isMedium()</code>
         * @see Logger#isWarnEnabled()
         */
        public boolean isWarnEnabled()
        {
            return isMedium();
        }

        /**
         * @return <code>Trace.isLow()</code>
         * @see Logger#isErrorEnabled()
         */
        public boolean isErrorEnabled()
        {
            return isLow();
        }

        /**
         * @return <code>Trace.isLow()</code>
         * @see Logger#isFatalErrorEnabled()
         */
        public boolean isFatalErrorEnabled()
        {
            return isLow();
        }
    }
}

