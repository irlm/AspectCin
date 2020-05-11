/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

import java.util.Properties;

import org.omg.CORBA.ORB;

/**
 * This class is used a an Initializer for RMI over IIOP. It can be used to
 * configure the orb returned to the RMI over IIOP subsystem before any RMI
 * over IIOP code is entered.
 *
 * @author Jerome Daniel
 */
public final class DefaultORB
{
    private static ORB s_default;

    /**
     * Private constructor to not instantiate this class.
     */
    private DefaultORB()
    {
    }

    /**
     * Used to locate the default orb for use by the stubs, tie classes, and
     * for resolving the name service in the JNDI context.
     *
     * @return The default ORB.
     */
    public static synchronized ORB getORB()
    {
        if ( s_default == null )
        {
            setInitParams( null, null );
        }
        return s_default;
    }

    /**
     * Set the default orb. This can only be called once
     * and before any RMI over IIOP code is entered.
     *
     * @param orb An external orb for the default orb instance.
     * @throws IllegalStateException if the orb has already been initialized.
     */
    public static synchronized void setORB( ORB orb )
    {
        if ( s_default != null )
        {
            throw new IllegalStateException( "RMI orb has already been initialized" );
        }
        s_default = orb;
    }

    /**
     * Set the properties used to initialize the orb. Can only be called once,
     * and only before any RMI over IIOP code is entered. Best to call it
     * immediatly upon application startup.
     *
     * @param args An array with command line arguments.
     * @param props ORB properties.
     * @throws IllegalStateException if the orb has already been initialized.
     */
    public static synchronized void setInitParams( String [] args, Properties props )
    {
        if ( s_default != null )
        {
            throw new IllegalStateException( "RMI orb has already been initialized" );
        }
        if ( args == null )
        {
            args = new String[ 0 ];
        }
        if ( props == null )
        {
            props = new Properties();
        }
        props.setProperty( "ImportModule.RMIoverIIOP", "${openorb.home}config/default.xml#rmi" );
        s_default = ORB.init( args, props );
    }
}

