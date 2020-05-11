/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.CORBA;

/**
 * @deprecated This class is deprecated and will be removed in future versions.
 */
public class ORB
    extends org.openorb.orb.core.ORB
{
    /**
     * Set the ORB parameters for a standalone application.
     */
    protected void set_parameters( String[] args, java.util.Properties properties )
    {
        super.set_parameters( args, properties );
        System.out.println( "WARNING: The usage of this class '" + this.getClass().getName()
              + "' is deprecated and it will be removed in a future version." );
        System.out.println( "WARNING: Please use the following properties instead:\n"
              + "\torg.omg.CORBA.ORBClass=org.openorb.orb.core.ORB\n"
              + "\torg.omg.CORBA.ORBSingletonClass=org.openorb.orb.core.ORBSingleton" );
    }

    /**
     * Set the ORB parameters for an applet.
     */
    protected void set_parameters( java.applet.Applet app, java.util.Properties properties )
    {
        super.set_parameters( app, properties );
        System.out.println( "WARNING: The usage of this class '" + this.getClass().getName()
              + "' is deprecated and it will be removed in a future version." );
        System.out.println( "WARNING: Please use the following properties instead:\n"
              + "\torg.omg.CORBA.ORBClass=org.openorb.orb.core.ORB\n"
              + "\torg.omg.CORBA.ORBSingletonClass=org.openorb.orb.core.ORBSingleton" );
    }
}

