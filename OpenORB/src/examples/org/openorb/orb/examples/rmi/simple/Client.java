/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.simple;

import java.util.Properties;

import javax.rmi.PortableRemoteObject;

public final class Client
{
    // do not instantiate
    private Client()
    {
    }

    public static void main( String[] args )
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
        org.omg.CORBA.Object obj = null;
        try
        {
            java.io.FileInputStream file = new java.io.FileInputStream( "ObjectId" );
            java.io.InputStreamReader myInput = new java.io.InputStreamReader( file );
            java.io.BufferedReader reader = new java.io.BufferedReader( myInput );
            String ref = reader.readLine();
            obj = orb.string_to_object( ref );
        }
        catch ( java.io.IOException ex )
        {
            ex.printStackTrace( );
        }

        try
        {
            SimpleInterface itf = ( SimpleInterface ) PortableRemoteObject.narrow(
                  obj, SimpleInterface.class );

            String msg = "Hello Server!";
            System.out.println( "Sending to server '" + msg + "'..." );
            String result = ( String ) itf.echo( msg );
            System.out.println( "...received from server '" + result + "'." );

            System.out.println( "Calling method that throws RemoteException..." );
            itf.throwException( "A remote exception message!" );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
}

