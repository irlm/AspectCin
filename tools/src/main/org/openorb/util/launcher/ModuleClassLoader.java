/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The <code>ModuleClassLoader</code> class is a class loader that attempts to
 * initialize itself based on the directory structure required by OpenORB.
 * <p>
 * The following folder layout is required:
 * <pre>
 *
 *     &lt;TCOO_HOME&gt;
 *         |- tools
 *         |- OpenORB
 * </pre>
 *
 * @author Tony Thompson
 * @author Michael Rumpf
 * @version $Revision: 1.18 $ $Date: 2004/02/07 13:24:29 $
 */
public class ModuleClassLoader
    extends URLClassLoader
{
    /** Define a property which can be used to pass an additional classpath to the launcher. */
    private static final String LAUNCHER_CP_KEY = "openorb.env.cp";

    /**
     * Creates a new instance of ModuleClassLoader.
     */
    public ModuleClassLoader()
        throws Exception
    {
        super( new URL[] {}, null );

        initialize( locateDirectories( null ) );

        setupAdditionalPath();
    }

    /**
     * Creates a new instance of ModuleClassLoader.  ModuleClassLoader will
     * attempt to determine if there are any additional JAR files that should be
     * in the classpath based on the name of the module.
     *
     * @param module a class name that indicates what module this class loader
     * will be launching classes from.
     */
    public ModuleClassLoader( final String module )
        throws Exception
    {
        super( new URL[] {}, null );

        initialize( locateDirectories( module ) );

        setupAdditionalPath();
    }

    /**
     * Setup the additional path.
     *
     * This method parses a special environment variable and adds paths and jars
     * found there to the classloader. This is necessary for the JavaToIdl
     * compiler because the compiler loads the classes it parses and therefore
     * the classpath must be user definable.
     */
    private void setupAdditionalPath()
    {
        // add the additional classpath
        String cp = System.getProperty( LAUNCHER_CP_KEY );
        if ( cp != null && cp.length() > 0 )
        {
            StringTokenizer strtokizer = new StringTokenizer( cp, File.pathSeparator );
            while ( strtokizer.hasMoreElements() )
            {
                String strtok = strtokizer.nextToken();
                File fil = new File( strtok );
                if ( fil.exists() )
                {
                    try
                    {
                        super.addURL( fil.toURL() );
                    }
                    catch ( MalformedURLException mue )
                    {
                        System.err.println( "Unable to add path element: " + fil );
                    }
                }
            }
        }
    }

    /**
     * Set the classpath for this class loader from the list of directories.
     *
     * @param directories a list of directories to search for JAR files.  Any
     * JAR files found in these directories will be added to the classpath.
     */
    private void initialize( final File[] directories )
    {
        JarFiles jarFilter = new JarFiles();

        for ( int i = 0; i < directories.length; i++ )
        {
            File[] dirList = directories[ i ].listFiles( jarFilter );

            if ( dirList == null )
            {
                continue;
            }

            for ( int loop = 0; loop < dirList.length; loop++ )
            {
                try
                {
                    super.addURL( dirList[ loop ].toURL() );
                }
                catch ( MalformedURLException mue )
                {
                    System.err.println( "Unable to add JAR file: " + dirList[ loop ] );
                }
            }
        }
    }

    /**
     * Determine the home directory of TCOO based on the location that we were
     * launched from.
     */
    private File[] locateDirectories( final String module )
        throws Exception
    {
        URL from = getClass().getProtectionDomain().getCodeSource().getLocation();

        File fromFile;

        try
        {
            fromFile = new File( from.getFile() ).getCanonicalFile();
        }
        catch ( IOException ioe )
        {
            throw new Exception( "Unable to locate: " + from.toExternalForm() );
        }

        // fromFile should be something like /tcoo/OpenORB/lib/OpenORB.jar
        // Strip it down to the root.
        String root = fromFile.getParentFile().getParentFile().getParent();

        // Check for the required directory structure.
        File orbRoot = new File( root, "OpenORB" );

        if ( !orbRoot.exists() )
        {
            throw new Exception( "ERROR: missing 'OpenORB' directory in root: " + root );
        }

        File toolsRoot = new File( root, "tools" );

        if ( !toolsRoot.exists() )
        {
            throw new Exception( "ERROR: missing 'tools' directory in root: " + root );
        }

        Vector fileList = new Vector( 4 );
        fileList.add( new File( orbRoot, "lib" ).getCanonicalFile() );
        fileList.add( new File( root, "SSL/lib" ).getCanonicalFile() );
        fileList.add( new File( root, "SSL/lib/ext" ).getCanonicalFile() );
        fileList.add( new File( toolsRoot, "lib" ).getCanonicalFile() );
        fileList.add( new File( toolsRoot, "lib/ext" ).getCanonicalFile() );
        fileList.add( new File( toolsRoot, "lib/ext/build" ).getCanonicalFile() );

        final String ourPrefix = "org.openorb.";
        if ( ( module != null ) && module.startsWith( ourPrefix ) )
        {
            String shortName = module.substring( ourPrefix.length() );
            int dot = shortName.indexOf( '.' );
            if ( dot > 0 )
            {
                shortName = shortName.substring( 0, dot );
            }

            if ( "ccs".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "ConcurrencyControlService/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib/ext" ) );
            }
            else if ( "orb".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "NamingService/lib" ) );
            }
            else if ( "constraint".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "EvaluatorUtility/lib" ) );
            }
            else if ( "event".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "EventService/lib" ) );
            }
            else if ( "ir".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "InterfaceRepository/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib/ext" ) );
                addDependency( fileList, new File( root, "PersistentStateService/lib" ) );
            }
            else if ( "board".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "ManagementBoard/lib" ) );
                addDependency( fileList, new File( root, "ManagementBoard/lib/ext" ) );
                addDependency( fileList, new File( root, "TransactionService/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib/ext" ) );
                addDependency( fileList, new File( root, "PersistentStateService/lib" ) );
                addDependency( fileList, new File( root, "EvaluatorUtility/lib" ) );
                // notify plugin
                addDependency( fileList, new File( root, "NotificationService/lib" ) );
                // ir plugin
                addDependency( fileList, new File( root, "InterfaceRepository/lib" ) );
                // trader plugin
                addDependency( fileList, new File( root, "TradingService/lib" ) );
                // ns plugin
                addDependency( fileList, new File( root, "NamingService/lib" ) );
            }
            else if ( "tns".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "NamingService/lib" ) );
            }
            else if ( "ins".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "NamingService/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib" ) );
                addDependency( fileList, new File( root, "PersistentStateService/lib" ) );
            }
            else if ( "notify".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "NotificationService/lib" ) );
                addDependency( fileList, new File( root, "EvaluatorUtility/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib/ext" ) );
                addDependency( fileList, new File( root, "PersistentStateService/lib" ) );
            }
            else if ( "pss".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "PersistentStateService/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib/ext" ) );
            }
            else if ( "property".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "PropertyService/lib" ) );
            }
            else if ( "time".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "EventService/lib" ) );
                addDependency( fileList, new File( root, "TimeService/lib" ) );
            }
            else if ( "trader".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "TradingService/lib" ) );
                addDependency( fileList, new File( root, "EvaluatorUtility/lib" ) );
                addDependency( fileList, new File( root, "PersistentStateService/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib/ext" ) );
                addDependency( fileList, new File( root, "InterfaceRepository/lib" ) );
            }
            else if ( "ots".equals( shortName ) )
            {
                addDependency( fileList, new File( root, "TransactionService/lib" ) );
                addDependency( fileList, new File( root, "TransactionService/lib/ext" ) );
            }
        }

        File[] retValue = new File[fileList.size()];
        fileList.copyInto( retValue );

        return retValue;
    }

    /**
     * Add the specified dependency to the dependency list.  The dependency will
     * only be added if it exists.
     *
     * @throws Exception if the dependency does not exist.
     */
    private void addDependency( final Vector depList, final File directory )
        throws Exception
    {
        if ( directory.exists() )
        {
            depList.add( directory );
        }
        else
        {
            throw new Exception(
                "Unable to configure module because of a missing dependency: " + directory );
        }
    }

    /**
     * A <code>FilenameFilter</code> that only accepts files with a ".jar"
     * extension.
     */
    private class JarFiles
        implements FilenameFilter
    {
        /**
         */
        public boolean accept( File dir, String name )
        {
            return name.endsWith( ".jar" );
        }
    }
}

