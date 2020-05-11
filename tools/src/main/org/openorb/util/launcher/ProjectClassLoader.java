/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.launcher;

import java.io.File;
import java.io.FileFilter;


/**
 * A class loader that uses the <code>openorb.home.path</code> system property
 * to load OpenORB project libraries. Basically it looks for a
 * <code>lib</code> subdirectory in each of the subdirectories of
 * <code>openorb.home.apth</code>, then recursively scans for available libraries.
 * <p>
 * If the <code>openorb.home.path</code> system property is not set then this
 * classloader has no effect.
 *
 * @author Richard G Clark
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:28:45 $
 */
public class ProjectClassLoader extends LibraryClassLoader
{
    private static final String OPENORB_HOME_KEY = "openorb.home.path";
    private static final String LIBRARY_DIRECTORY_NAME = "lib";
    private static final FileFilter DIRECTORY_FILTER = new DirectoryFileFilter();
    private static final FileFilter RECURSIVE_FILTER = new RecursiveFileFilter();

    /**
     * Creates a new class loader using the specified parent class loader
     * for delegation.
     */
    public ProjectClassLoader( final ClassLoader parent )
    {
        super( getRootFiles(), parent, JAR_FILE_FILTER, RECURSIVE_FILTER, false );
    }

    /**
     * Returns the root files to be used by the class loader.
     *
     * @return the root files
     */
    private static File[] getRootFiles()
    {
        final String homePathName = System.getProperty( OPENORB_HOME_KEY );

        if ( null == homePathName )
        {
            throw new IllegalStateException( "System property [" + OPENORB_HOME_KEY
                    + "] has not been set." );
        }

        final File homePath = new File( homePathName );

        final File[] files = homePath.listFiles( DIRECTORY_FILTER );

        for ( int i = 0; i < files.length; i++ )
        {
            files[i] = new File( files[i], LIBRARY_DIRECTORY_NAME );
        }

        return files;
    }

    /**
     * A <code>FileFilter</code> that accepts only directories.
     */
    private static final class DirectoryFileFilter implements FileFilter
    {
        /**
         * Tests if the file is a directory.
         *
         * @param file the file to be tested
         * @return <code>true</code> if condition is met
         */
        public boolean accept( final File file )
        {
            return file.isDirectory();
        }
    }

    /**
     * A <code>FileFilter</code> that doesn't accept CVS directories.
     */
    private static final class RecursiveFileFilter implements FileFilter
    {
        private static final String CVS_DIRECTORY_NAME = "CVS";

        /**
         * Tests is the file is not called "CVS".
         *
         * @param file the file to be tested.
         * @return <code>true</code> if condition is met
         */
        public boolean accept( final File file )
        {
            return !CVS_DIRECTORY_NAME.equals( file.getName() );
        }
    }
}

