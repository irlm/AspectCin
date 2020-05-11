/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.calculator;

import javax.naming.InitialContext;

/**
 * The server application.
 *
 * @author Chris Wood
 */
public final class Server
    extends javax.rmi.PortableRemoteObject
{
    /**
     * Default constructor.
     *
     * @throws java.rmi.RemoteException When a problem during instantiation occurs.
     */
    public Server()
        throws java.rmi.RemoteException
    {
    }

    /**
     * This method creates the object and registers it to the NamingService.
     */
    public void run()
    {
        try
        {
            java.util.Properties env = new java.util.Properties();
            env.setProperty( "java.naming.factory.initial", "org.openorb.ns.jndi.CtxFactory" );
            InitialContext context = new InitialContext( env );
            CalculatorImpl addObj = new CalculatorImpl();
            javax.rmi.PortableRemoteObject.exportObject( addObj );
            context.bind( "addition", addObj );
            System.out.println( "The calculator server is now ready..." );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            System.out.println( ex.getMessage() );
        }
    }

    /**
     * The entry point for this application.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        try
        {
            Server srv = new Server();
            srv.run();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
}

