/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.hello;

/**
 * The stubs and skeletons must be generated manually.
 *
 * The RemoteHello.idl can be found in the folder
 *    build/gensrc/examples/org/openorb/orb/examples/rmi/hello
 *
 * To create a stub for the IDL interface use the following command
 *    java org.openorb.compiler.IdlCompiler
 *       -I build/gensrc/examples/org/openorb/orb/examples/rmi/hello
 *       -d build/gensrc/examples RemoteHello.idl
 *
 * The generated files must be compiled and available in your CLASSPATH.
 * Then you can start our RMI-IIOP server HelloServer and access it from
 * the CORBA client HelloCORBAClient.
 *
 * @deprecated This class also uses CosNaming IDL APIs and should either
 * be converted to resolve the dependency to the NamingService or it should
 * also be moved to the NameSerivce.
 *
 * @author Chris Wood
 */
public final class HelloCORBAClient
{
    // do not instantiate
    private HelloCORBAClient()
    {
    }

    /**
     * The entry point for this application.
     *
     * @param args The command line parameters.
     */
    public static void main( String[] args )
    {
        System.out.println( "Hello client for CORBA..." );
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, null );
        org.omg.CosNaming.NamingContext naming = null;
        try
        {
            org.omg.CORBA.Object obj = orb.resolve_initial_references( "NameService" );
            naming = org.omg.CosNaming.NamingContextHelper.narrow( obj );
        }
        catch ( org.omg.CORBA.ORBPackage.InvalidName ex )
        {
            System.out.println( "InavlidName exception : " + ex );
            System.exit( 1 );
        }
        try
        {
            org.omg.CosNaming.NameComponent[] name = new org.omg.CosNaming.NameComponent[ 1 ];
            name[ 0 ] = new org.omg.CosNaming.NameComponent( "hello", "" );
            org.omg.CORBA.Object obj = naming.resolve( name );
            org.openorb.orb.examples.rmi.hello.RemoteHello rmi =
                org.openorb.orb.examples.rmi.hello.RemoteHelloHelper.narrow( obj );
            rmi.print( "Hello from a CORBA client..." );
        }
        catch ( java.lang.Exception ex )
        {
            System.out.println( "Exception : " + ex.toString() );
            System.exit( 1 );
        }
    }
}

