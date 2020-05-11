/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.launcher;

import java.io.File;
import java.io.PrintStream;

import java.lang.reflect.Method;

/**
 * A wrapped utility class for running the Ant build process.
 * It is important that JVM is used from the JDK installation or else
 * the tools.jar file cannot be found.
 *
 * @author Richard G Clark
 * @version $Revision: 1.7 $ $Date: 2004/02/10 22:25:31 $
 */
public final class Build
{
    private static final String TCOO_HOME = "openorb.home.path";
    private static final String MODULE_NAME = "openorb.module.name";

    private static final PrintStream LOG_STREAM = System.out;

    private static final Object SYNC = new byte[0];

    private static File s_openOrbHomeDir;

    private Build()
    {
    }

    /**
     * Start the build proccess.
     *
     * @param args the command line arguments
     */
    public static void main( final String[] args ) throws Exception
    {
        final File openOrbHomeDir = getOpenOrbHomeDir();
        final File toolsHome = new File( openOrbHomeDir, "tools" );
        final File libDir = new File( toolsHome, "lib" );
        final File extDir = new File( libDir, "ext" );
        final File buildDir = new File( extDir, "build" );
        final File jdkDir = new File( System.getProperty( "java.home" ) ).getParentFile();
        final File[] roots = new File[] {
            new File( new File( jdkDir, "lib" ), "tools.jar" ),
            new File( extDir, "xml-apis.jar" ),
            new File( extDir, "xercesImpl.jar" ),
            buildDir
        };

        final ClassLoader classLoader = new LibraryClassLoader( roots,
                LibraryClassLoader.JAR_FILE_FILTER,
                LibraryClassLoader.REJECT_ALL_FILE_FILTER, false );

        Thread.currentThread().setContextClassLoader( classLoader );

        System.setProperty( "ant.home", buildDir.getPath() );

        final Class mainClass = classLoader.loadClass( "org.apache.tools.ant.Main" );
        final Method mainMethod = mainClass.getMethod( "main", new Class[] { args.getClass() } );

        // choose the build file.
        final String[] args2 = new String[args.length + 2];
        System.arraycopy( args, 0, args2, 2, args.length );

        addBuildfileArg( args2, openOrbHomeDir );

        mainMethod.invoke( null, new Object[] { args2 } );
    }

    private static void log( final Object msg )
    {
        LOG_STREAM.println( msg );
    }

    private static void log( final Object prefix, final Object msg )
    {
        LOG_STREAM.print( prefix );
        LOG_STREAM.print( ": " );
        LOG_STREAM.println( msg );
    }

    private static File getOpenOrbHomeDir()
    {
        synchronized ( SYNC )
        {
            if ( null != s_openOrbHomeDir )
            {
                return s_openOrbHomeDir;
            }

            final String pathname = System.getProperty( TCOO_HOME );

            if ( null != pathname )
            {
                return s_openOrbHomeDir = new File( pathname );
            }

            // search down from the current directory to find the home
            File currentDir = new File( System.getProperty( "user.dir" ) );
            log( "Warning", "Property [" + TCOO_HOME + "] was not set." );
            log( "Info", "Searching for OpenORB Home down from [" + currentDir + "]." );

            while ( null != currentDir )
            {
                if ( new File( currentDir, "tools" ).exists() )
                {
                    log( "Info", "Found OpenORB Home [" + currentDir + "]" );
                    return s_openOrbHomeDir = currentDir;
                }
                currentDir = currentDir.getParentFile();
            }

            log( "Error", "Could not find OpenORB Home." );

            throw new IllegalStateException( "Property [" + TCOO_HOME + "] was not set." );
        }
    }

    private static void addBuildfileArg( final String[] args, final File openOrbHomeDir )
    {
        args[0] = "-buildfile";

        final String moduleName = System.getProperty( MODULE_NAME );
        final File moduleDir;

        if ( null == moduleName )
        {
            log( "Warning", "Property [" + MODULE_NAME + "] was not set." );
            moduleDir = new File( System.getProperty( "user.dir" ) );
        }
        else
        {
            moduleDir = new File( openOrbHomeDir, moduleName );
        }

        args[1] = new File( new File( moduleDir, "src" ), "build.xml" ).getPath();
    }

}

