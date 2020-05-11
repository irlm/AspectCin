/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Create the orb.properties file in the correct location.
 * It also includes support for the RMI properties.
 *
 * @author Unknown
 */
public final class CreateORBProperties
{
    private CreateORBProperties()
    {
    }

    /**
     * Show usage information of this tool.
     */
    private static void printUsage()
    {
        System.err.println( "Usage: java org.openorb.orb.util.CreateORBProperties [Options]" );
        System.err.println( "Options:" );
        System.err.println( "  --force             Overwrite the properties file." );
        System.err.println( "  --pwd               Write to current working dir rather than" );
        System.err.println( "                      ${java.home}/lib." );
        System.err.println( "  --config <URL|File> Specify a default URL or filename for the" );
        System.err.println( "                      OpenORB config file." );
        System.err.println( "                      This automatically sets the force option." );
        System.err.println( "  --rmi               In addition to the normal properties," );
        System.err.println( "                      also add the RMIoverIIOP properies." );
    }

    /**
     * The entry point for this tool.
     *
     * @param args The command line parameters.
     */
    public static void main( String [] args )
    {
        boolean rmi = false;
        boolean force = false;
        boolean pwd = false;
        String config = null;

        for ( int i = 0; i < args.length; ++i )
        {
            if ( args[ i ].equals( "-rmi" ) || args[ i ].equals( "--rmi" ) )
            {
                rmi = true;
            }
            else if ( args[ i ].equals( "-force" ) || args[ i ].equals( "--force" ) )
            {
                force = true;
            }
            else if ( args[ i ].equals( "-pwd" ) || args[ i ].equals( "--pwd" ) )
            {
                pwd = true;
            }
            else if ( args[ i ].equals( "-config" ) || args[ i ].equals( "--config" ) )
            {
                if ( args.length < i + 1 || args[ i + 1 ].startsWith( "-" ) )
                {
                    printUsage();
                    System.exit( 0 );
                }
                force = true;
                config = args[ ++i ];
            }
            else if ( args[ i ].equals( "-help" ) || args[ i ].equals( "--help" ) )
            {
                printUsage();
                System.exit( 0 );
            }
            else
            {
                printUsage();
                System.exit( 1 );
            }
        }

        File javaHomeLib = null;
        if ( !pwd )
        {
            String javaHome = "";
            try
            {
                javaHome = System.getProperty( "java.home" );
            }
            catch ( SecurityException ex )
            {
                System.err.println( "Unable to read the java.home property, access denied" );
                System.exit( 1 );
            }

            javaHomeLib = new File( javaHome, "lib" );
            if ( !javaHomeLib.exists() )
            {
                System.err.println( "Directory \"" + javaHomeLib + "\" does not exist." );
                System.exit( 1 );
            }
            if ( !javaHomeLib.isDirectory() )
            {
                System.err.println( "File \"" + javaHomeLib + "\" is not a directory." );
                System.exit( 1 );
            }
        }
        File propsFile = new File( javaHomeLib, "orb.properties" );
        if ( !force && propsFile.exists() )
        {
            System.err.println( "File \"" + propsFile + "\" exists." );
            System.exit( 1 );
        }

        PrintStream os = null;
        try
        {
            os = new PrintStream( new FileOutputStream( propsFile ) );
        }
        catch ( IOException ex )
        {
            System.err.println( "Can't create orb.properties file \"" + javaHomeLib + "\"." );
            System.exit( 1 );
        }
        os.println( "org.omg.CORBA.ORBClass=org.openorb.orb.core.ORB" );
        os.println( "org.omg.CORBA.ORBSingletonClass=org.openorb.orb.core.ORBSingleton" );
        if ( rmi )
        {
            os.println( "javax.rmi.CORBA.StubClass=org.openorb.orb.rmi.StubDelegateImpl" );
            os.println( "javax.rmi.CORBA.UtilClass=org.openorb.orb.rmi.UtilDelegateImpl" );
            os.println( "javax.rmi.CORBA.PortableRemoteObjectClass="
                  + "org.openorb.orb.rmi.PortableRemoteObjectDelegateImpl" );
        }
        if ( config != null )
        {
            os.println( "openorb.config=" + config );
        }
        os.close();
        if ( os.checkError() )
        {
            System.err.println( "Error while writing orb.properties file \""
                  + javaHomeLib + "\"." );
            System.exit( 1 );
        }
    }
}

