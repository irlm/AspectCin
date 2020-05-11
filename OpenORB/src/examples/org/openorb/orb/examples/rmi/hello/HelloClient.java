/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.hello;

import javax.naming.InitialContext;

/**
 * The client application for the hello server.
 *
 * @author Chris Wood
 */
public final class HelloClient
{
    // do not instantiate
    private HelloClient()
    {
    }

    /**
     * The entry point for the application.
     *
     * @param args The command line parameters.
     */
    public static void main( String[] args )
    {
        try
        {
            java.util.Properties env = new java.util.Properties ();
            env.setProperty( "java.naming.factory.initial", "org.openorb.ns.jndi.CtxFactory" );
            InitialContext context = new InitialContext( env );
            Object obj = context.lookup ( "hello" );
            RemoteHello hello = null;
            hello = ( RemoteHello ) javax.rmi.PortableRemoteObject.narrow( obj, RemoteHello.class );
            hello.print( "This message is sent via RMI over IIOP..." );
        }
        catch ( java.lang.Exception ex )
        {
            ex.printStackTrace();
            System.exit( 0 );
        }
    }
}

