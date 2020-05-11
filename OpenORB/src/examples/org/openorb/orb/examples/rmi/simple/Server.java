/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.simple;

import java.rmi.Remote;

import java.util.Properties;

import javax.rmi.PortableRemoteObject;

public final class Server
{
    // do not instantiate
    private Server()
    {
    }

    public static void main( String[] args )
    {
        try
        {
            Properties props = new Properties();
            props.put( "org.omg.CORBA.ORBClass", "org.openorb.orb.core.ORB" );
            props.put( "org.omg.CORBA.ORBSingletonClass", "org.openorb.orb.core.ORBSingleton" );
            props.put( "javax.rmi.CORBA.StubClass", "org.openorb.orb.rmi.StubDelegateImpl" );
            props.put( "javax.rmi.CORBA.UtilClass", "org.openorb.orb.rmi.UtilDelegateImpl" );
            props.put( "javax.rmi.CORBA.PortableRemoteObjectClass",
                  "org.openorb.orb.rmi.PortableRemoteObjectDelegateImpl" );
             // This is necessary to send and receive UnknownExceptionInfo service contexts
             props.put( "org.omg.PortableInterceptor.ORBInitializerClass."
                  + "org.openorb.orb.rmi.RMIInitializer", "" );
            // This property is necessary to set the default orb with the ORB initialized below
            props.put( "rmi.defaultorbSingleton", "true" );
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, props );

            SimpleImpl simple = new SimpleImpl();
            Remote remobj = PortableRemoteObject.toStub( simple );

            String ref = orb.object_to_string( ( org.omg.CORBA.Object ) remobj );
            java.io.FileOutputStream file = new java.io.FileOutputStream( "ObjectId" );
            java.io.PrintStream pfile = new java.io.PrintStream( file );
            pfile.println( ref );
            pfile.close();
        }
        catch ( java.io.IOException ex )
        {
            ex.printStackTrace( );
            System.exit( 0 );
        }

        try
        {
            System.out.println( "The server is ready..." );
            while ( true )
            {
                Thread.sleep( 1000 );
            }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
}
