/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.hello;

import java.rmi.RemoteException;
import javax.naming.InitialContext;

/**
 * The implementation of the interface and the server application
 * at the same time.
 *
 * @author Chris Wood
 */
public class HelloServer
    extends javax.rmi.PortableRemoteObject
    implements RemoteHello
{
    /**
     * Default constructor.
     *
     * @throws RemoteException ???
     */
    public HelloServer()
        throws RemoteException
    {
    }

    /**
     * Print a message on the server.
     *
     * @param message The message to print.
     * @throws RemoteException When an error occurs.
     */
    public void print( String message )
        throws RemoteException
    {
        System.out.println( message );
    }

    /**
     * The entry point for this application.
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
            HelloServer helloObj = new HelloServer();
            context.bind( "hello", helloObj );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            System.out.println( ex.getMessage() );
        }
    }
}

