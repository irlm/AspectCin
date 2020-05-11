/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.launcher;

import java.io.File;

import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * The <code>Start</code> class is used to launch OpenORB examples and services
 * without any special setup requirements.
 */
public class Start
{
    /** CORBA ORBClass property key */
    private static final String ORB_CLASS_KEY               = "org.omg.CORBA.ORBClass";
    /** CORBA ORBSingletonClass property key */
    private static final String ORB_SINGLETON_CLASS_KEY     = "org.omg.CORBA.ORBSingletonClass";

    /** OpenORB ORBClass value */
    private static final String OPENORB_ORB_CLASS           = "org.openorb.orb.core.ORB";
    /** OpenORB ORBSingleton value */
    private static final String OPENORB_ORB_SINGLETON_CLASS = "org.openorb.orb.core.ORBSingleton";

    private URLClassLoader m_classLoader;

    /**
     * Creates a new instance of Start.
     *
     * @param args the command line arguments
     */
    public Start( final String[] args )
    {
        String mainClass = args[ 0 ];
        try
        {
            m_classLoader = new ModuleClassLoader( mainClass );
        }
        catch ( Exception e )
        {
            layout();
            e.printStackTrace();
            return;
        }

        Thread.currentThread().setContextClassLoader( m_classLoader );

        fixURLProtocolHandler();

        String[] mainArgs = null;

        if ( args.length > 1 )
        {
            mainArgs = new String[ args.length - 1 ];
            System.arraycopy( args, 1, mainArgs, 0, mainArgs.length );
        }

        if ( mainArgs == null )
        {
            mainArgs = new String[ 0 ];
        }

        launchClass( mainClass, mainArgs );
    }

    /**
     * @param args the command line arguments
     */
    public static void main( String[] args )
    {
        if ( args.length < 1 )
        {
            usage();
            return;
        }

        // Set the environment variables to use OpenORB

        // use OpenORB as the ORB.
        System.setProperty( ORB_CLASS_KEY,
                OPENORB_ORB_CLASS );
        System.setProperty( ORB_SINGLETON_CLASS_KEY,
                OPENORB_ORB_SINGLETON_CLASS );

        // use OpenORB for RMIoverIIOP
        System.setProperty( "javax.rmi.CORBA.StubClass",
              "org.openorb.orb.rmi.StubDelegateImpl" );
        System.setProperty( "javax.rmi.CORBA.UtilClass",
              "org.openorb.orb.rmi.UtilDelegateImpl" );
        System.setProperty( "javax.rmi.CORBA.PortableRemoteObjectClass",
              "org.openorb.orb.rmi.PortableRemoteObjectDelegateImpl" );

        new Start( args );
    }

    private static void usage()
    {
        File file = new File(
              Start.class.getProtectionDomain().getCodeSource().getLocation().getFile() );

        String name = "-jar " + file.getName();

        if ( !name.endsWith( ".jar" ) )
        {
            name = Start.class.getName();
        }

        System.err.println( "java " + name + " <class_to_start> [parameters]" );
    }

    private void layout()
    {
        System.err.println( "" );
        System.err.println( "The Community OpenORB (TCOO) environment has not been"
              + " setup properly!" );
        System.err.println( "The following folder layout is required:" );
        System.err.println( "" );
        System.err.println( "    <TCOO_HOME>" );
        System.err.println( "        |- tools" );
        System.err.println( "        |- OpenORB" );
        System.err.println( "" );
    }

    private void showClassPath()
    {
        System.err.println( "ClassPath: " );
        URL[] cp = m_classLoader.getURLs();

        if ( cp != null )
        {
            for ( int i = 0; i < cp.length; i++ )
            {
                System.err.println( "   " + cp[ i ].toExternalForm() );
            }
        }
    }

    /**
     * Using the configured class loader, call the main() method in the
     * specified class.
     *
     * @param className the name of the class to launch
     * @param args the arguments that will be passed to main()
     */
    private void launchClass( final String className, final String[] args )
    {
        try
        {
            Class mainClass = m_classLoader.loadClass( className );
            Method mainMethod = mainClass.getMethod( "main", new Class[] { args.getClass() } );

            mainMethod.invoke( null, new Object[] { args } );
        }
        catch ( ClassNotFoundException cnf )
        {
            System.err.println( "Unable to locate class: " + className );
            showClassPath();
        }
        catch ( NoSuchMethodException nsm )
        {
            System.err.println( "No main() method in class: " + className );
        }
        catch ( NoClassDefFoundError ncdf )
        {
            ncdf.printStackTrace();
            showClassPath();
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
    }

    private void fixURLProtocolHandler()
    {
        PropertyManager.JAVA_PROTOCOL_HANDLER_PKGS.postfixValue(
                "org.openorb.util.urlhandler" );
    }
}

