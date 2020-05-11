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
import org.apache.avalon.framework.logger.AvalonFormatter;

import org.apache.log.output.io.StreamTarget;
import org.apache.log.Hierarchy;
import org.apache.log.Priority;

import org.omg.CORBA.ORB;

/**
 * This factory class can be used to create an ORB instance that is
 * equipped with a logger.
 *
 * @author Michael Rumpf
 */
public final class ORBFactory
{
    /**
     * Utility class, do not instantiate.
     */
    private ORBFactory()
    {
    }

    /**
     * Create and ORB and attach a logger instance to it.
     * All parameters for the logger will be default parameters.
     *
     * @param args The command line arguments to initialize the ORB with.
     * @param props The properties to initialize the ORB with.
     * @return The ORB instance.
     */
    public static ORB createORB ( String[] args, Properties props )
    {
        return createORB ( args, props, null, null, null, null );
    }

    /**
     * Create and ORB and attach a logger instance to it.
     * This method uses the default stream target, i.e. System.out, the
     * the default output format, and the default priority ERROR.
     *
     * @param args The command line arguments to initialize the ORB with.
     * @param props The properties to initialize the ORB with.
     * @param id The ORB id which will be the logger's root category.
     * @return The ORB instance.
     */
    public static ORB createORB ( String[] args, Properties props, String id )
    {
        return createORB ( args, props, id, null, null, null );
    }

    /**
     * Create and ORB and attach a logger instance to it.
     * This method uses the default stream target, i.e. System.out and the
     * the default output format.
     *
     * @param args The command line arguments to initialize the ORB with.
     * @param props The properties to initialize the ORB with.
     * @param id The ORB id which will be the logger's root category.
     * @param prio The logging priority.
     * @return The ORB instance.
     */
    public static ORB createORB ( String[] args, Properties props, String id, String prio )
    {
        return createORB ( args, props, id, prio, null, null );
    }

    /**
     * Create and ORB and attach a logger instance to it.
     * This method uses the default stream target, i.e. System.out.
     *
     * @param args The command line arguments to initialize the ORB with.
     * @param props The properties to initialize the ORB with.
     * @param id The ORB id which will be the logger's root category.
     * @param prio The logging priority.
     * @param fmt The logging format.
     * @return The ORB instance.
     */
    public static ORB createORB ( String[] args,
            Properties props, String id, String prio, String fmt )
    {
        return createORB ( args, props, id, prio, fmt, null );
    }

    /**
     * The logger created by this factory method can be utilized with several parameters:
     * <ul>
     *   <li>id or category name</li>
     *   <li>priority string</li>
     *   <li>log format string</li>
     *   <li>alternative logging stream</li>
     * </ul>
     * If these parameters are omitted default values will be used.
     *
     * The id that is used as a category name can be a starting point for
     * supporting the new ORBid that has been introduced by CORBA 2.5 and
     * serves also to distinguish several ORB instances. The default is "orb".
     *
     * The priority string can be FATAL_ERROR, INFO, WARN, and DEBUG. The
     * reason for making this a string and not an instance of the LogKit's
     * Priority class is that with this approach the user of this method
     * does not get into a direct dependency to the avalon-framework and
     * logkit jars. The default priority is Priority.ERROR.
     *
     * If you need to integrate OpenORB into an existing logging system that
     * already has a distinction of the logging levels you don't want to have
     * this info twice in each output line. Therefore it is necessary to
     * control the format from the outside. The default is
     *   "[%7.7{priority}] (%{category}): %{message}\\n%{throwable}"
     *
     * If the ORB logging is integrated into a logging implementation that is
     * not supported by a wrapper to Avalon LogKit, then it is necessary to
     * pass an instance of a output stream which can be used from the Avalon
     * LogKit implementation to redirect its output to.
     *
     * @param args The command line arguments to initialize the ORB with.
     * @param props The properties to initialize the ORB with.
     * @param id The ORB id which will be the logger's root category.
     * @param prio The logging priority.
     * @param fmt The logging format.
     * @param ostream The stream where to direct the logger output to.
     * @return The ORB instance.
     */
    public static ORB createORB ( String[] args,
            Properties props, String id, String prio,
            String fmt, OutputStream ostream )
    {
        if ( props == null )
        {
            props = ( Properties ) System.getProperties().clone();
        }
        if ( props.get( "org.omg.CORBA.ORBClass" ) == null )
        {
            props.put( "org.omg.CORBA.ORBClass", "org.openorb.orb.core.ORB" );
        }
        if ( props.get( "org.omg.CORBA.ORBSingletonClass" ) == null )
        {
            props.put( "org.omg.CORBA.ORBSingletonClass", "org.openorb.orb.core.ORBSingleton" );
        }
        String orbid = id;
        if ( orbid == null || orbid.length() == 0 )
        {
            orbid = "orb";
        }
        Priority priority = Priority.ERROR;
        if ( prio != null )
        {
            if ( prio.equals( "FATAL_EROR" ) )
            {
                priority = Priority.FATAL_ERROR;
            }
            if ( prio.equals( "WARN" ) )
            {
                priority = Priority.WARN;
            }
            if ( prio.equals( "INFO" ) )
            {
                priority = Priority.INFO;
            }
            if ( prio.equals( "DEBUG" ) )
            {
                priority = Priority.DEBUG;
            }
        }

        String format = fmt;
        if ( format == null || format.length() == 0 )
        {
            format = "[%7.7{priority}] (%{category}): %{message}\\n%{throwable}";
        }
        OutputStream os = ostream;
        if ( os == null )
        {
            os = System.out;
        }
        Hierarchy hierarchy = Hierarchy.getDefaultHierarchy();
        StreamTarget target = new StreamTarget( os, new AvalonFormatter( format ) );
        hierarchy.setDefaultLogTarget( target );
        hierarchy.setDefaultPriority( priority );
        Logger logger = new LogKitLogger( hierarchy.getLoggerFor( orbid ) );

        props.put( "LOGGER", logger );

        // Use the new DefaultLoader
        props.put( "openorb.ORBLoader", "org.apache.orb.config.DefaultLoader" );

        return ORB.init( args, props );
    }
}

