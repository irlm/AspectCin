/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.launcher;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;


/**
 * A generic class loader using directory scans to add libraries to
 * th class path.
 *
 * @author Richard G Clark
 * @version $Revision: 1.5 $ $Date: 2004/02/03 23:25:32 $
 */
public class LibraryClassLoader extends URLClassLoader
{
    /**
     * A <code>FileFilter</code> that accepts any file.
     */
    public static final FileFilter ACCEPT_ALL_FILE_FILTER
            = new FixedFileFilter( true );

    /**
     * A <code>FileFilter</code> that accepts no file.
     */
    public static final FileFilter REJECT_ALL_FILE_FILTER
            = new FixedFileFilter( false );

    /**
     * A <code>FileFilter</code> that accepts any file with.
     */
    public static final FileFilter JAR_FILE_FILTER
            = new PostfixFileFilter( ".jar" );

    /**
     * Indicates if the roots should be rescanned if a class could not be found.
     */
    private final boolean m_dynamicRescan;

    /**
     * The root files to search.
     */
    private final File[] m_roots;

    /**
     * Used to identify library files.
     */
    private final FileFilter m_libraryFilter;

    /**
     * Used to determine which directories to recursively traverse.
     */
    private final FileFilter m_recursiveFilter;

    /**
     * Used to determine which files should be inspected.
     */
    private final FileFilter m_listFilter;

    /**
     * Constructs this to search the specified roots for libraries matching
     * the filter.
     *
     * @param roots the root files to search
     * @param libraryFilter used to identify library files
     * @param recursiveFilter used to determine which directories to
     *        recursively traverse
     * @param dynamicRescan indicates if the roots should be rescanned if a
     *        class could not be found
     */
    public LibraryClassLoader( final File[] roots, final FileFilter libraryFilter,
            final FileFilter recursiveFilter, final boolean dynamicRescan )
    {
        this( roots, new AcceptOnceFileFilter( libraryFilter ), recursiveFilter,
                new ListFileFilter( libraryFilter, recursiveFilter ), dynamicRescan );
    }

    /**
     * Constructs this to search the specified roots for libraries matching
     * the filter.
     *
     * @param roots the root files to search
     * @param libraryFilter used to identify library files
     * @param recursiveFilter used to determine which directories to
     *        recursively traverse
     * @param listFilter used to determine which files should be inspected
     * @param dynamicRescan indicates if the roots should be rescanned if a
     *        class could not be found
     */
    private LibraryClassLoader( final File[] roots, final FileFilter libraryFilter,
            final FileFilter recursiveFilter, final FileFilter listFilter,
            final boolean dynamicRescan )
    {
        super( getLibraryUrls( roots, libraryFilter, recursiveFilter, listFilter ) );

        m_roots = roots;
        m_libraryFilter = libraryFilter;
        m_recursiveFilter = recursiveFilter;
        m_listFilter = listFilter;
        m_dynamicRescan = dynamicRescan;
    }

    /**
     * Constructs this to search the specified roots for libraries matching
     * the filter.
     *
     * @param roots the root files to search
     * @param parent the parent class loader to use
     * @param libraryFilter used to identify library files
     * @param recursiveFilter used to determine which directories to
     *        recursively traverse
     * @param dynamicRescan indicates if the roots should be rescanned if a
     *        class could not be found
     */
    public LibraryClassLoader( final File[] roots, final ClassLoader parent,
            final FileFilter libraryFilter, final FileFilter recursiveFilter,
            final boolean dynamicRescan )
    {
        this( roots, parent, new AcceptOnceFileFilter( libraryFilter ), recursiveFilter,
                new ListFileFilter( libraryFilter, recursiveFilter ), dynamicRescan );
    }

    /**
     * Constructs this to search the specified roots for libraries matching
     * the filter.
     *
     * @param roots the root files to search
     * @param parent the parent class loader to use
     * @param libraryFilter used to identify library files
     * @param recursiveFilter used to determine which directories to
     *        recursively traverse
     * @param listFilter used to determine which files should be inspected
     * @param dynamicRescan indicates if the roots should be rescanned if a
     *        class could not be found
     */
    private LibraryClassLoader( final File[] roots, final ClassLoader parent,
            final FileFilter libraryFilter, final FileFilter recursiveFilter,
            final FileFilter listFilter, final boolean dynamicRescan )
    {
        super( getLibraryUrls( roots, libraryFilter, recursiveFilter, listFilter ), parent );

        m_roots = roots;
        m_libraryFilter = libraryFilter;
        m_recursiveFilter = recursiveFilter;
        m_listFilter = listFilter;
        m_dynamicRescan = dynamicRescan;
    }

    /**
     * Create an array of library URLs found from the roots.
     *
     * @param roots the roots to scan
     * @param libraryFilter used to identify library files
     * @param recursiveFilter used to determine which directories to
     *        recursively traverse
     * @return a <code>URL</code> array
     */
    private static URL[] getLibraryUrls( final File[] roots,
            final FileFilter libraryFilter, final FileFilter recursiveFilter,
            final FileFilter listFilter )
    {
        final List urls = new ArrayList();

        getLibraryUrls( urls, roots, libraryFilter, recursiveFilter, listFilter );

        return ( URL[] ) urls.toArray( new URL[urls.size()] );
    }

    /**
     * Adds library URLs found from the roots, into the specified list.
     *
     * @param urls the list to add library URLs to
     * @param roots the roots to scan
     * @param libraryFilter used to identify library files
     * @param recursiveFilter used to determine which directories to
     *        recursively traverse
     * @return the <code>urls</code> list passed to this method
     */
    private static List getLibraryUrls( final List urls, final File[] roots,
            final FileFilter libraryFilter, final FileFilter recursiveFilter,
            final FileFilter listFilter )
    {
        for ( int i = 0; i < roots.length; i++ )
        {
            addLibraryUrls( urls, roots[i], libraryFilter, recursiveFilter, listFilter );
        }

        return urls;
    }

    /**
     * Recursively scan directories adding any jar files found to the list.
     *
     * @param urls The list to add library URLs to.
     * @param file The file to be scanned.
     */
    private static void addLibraryUrls( final List urls, final File file,
            final FileFilter libraryFilter, final FileFilter recursiveFilter,
            final FileFilter listFilter )
    {
        if ( libraryFilter.accept( file ) )
        {
            try
            {
                urls.add( file.toURL() );
            }
            catch ( final MalformedURLException e )
            {
                throw new IllegalArgumentException( "Could not convert file ["
                    + file + "] to URL, with reason [" + e.getMessage() + "]." );
            }
        }

        final File[] contents = file.listFiles( listFilter );

        if ( null == contents )
        {
            return;
        }

        for ( int i = 0; i < contents.length; i++ )
        {
            addLibraryUrls( urls, contents[i], libraryFilter, recursiveFilter, listFilter );
        }
    }

    /**
     * Finds the specified class. If dynamic scanning is enabled then the
     * roots will be rescanned before throwing an exception.
     *
     * @param  className the name of the class
     * @return the resulting <code>Class</code> object
     * @throws ClassNotFoundException if the class could not be found
     */
    protected Class findClass( final String className ) throws ClassNotFoundException
    {
        if ( !m_dynamicRescan )
        {
            return super.findClass( className );
        }

        try
        {
            return super.findClass( className );
        }
        catch ( final ClassNotFoundException e )
        {
            final List urls = getLibraryUrls( new ArrayList(), m_roots,
                    m_libraryFilter, m_recursiveFilter, m_listFilter );

            if ( urls.isEmpty() )
            {
                throw e;
            }

            final Iterator it = urls.iterator();
            while ( it.hasNext() )
            {
                addURL( ( URL ) it.next() );
            }

            return super.findClass( className );
        }
    }

    /**
     * A <code>FileFilter</code> that accepts files which could either be
     * a library or a directory to recursively traverse.
     */
    private static final class ListFileFilter implements FileFilter
    {
        private final FileFilter m_libraryFilter;
        private final FileFilter m_recursiveFilter;

        /**
         * Constructs this to accept file matching either of the two filters.
         *
         * @param libraryFilter the filter for libraries
         * @param recursiveFilter the filter for recursive searching
         */
        public ListFileFilter( final FileFilter libraryFilter,
                final FileFilter recursiveFilter )
        {
            m_libraryFilter = libraryFilter;
            m_recursiveFilter = recursiveFilter;
        }

        /**
         * Tests if the file is a library or a directory to recursively
         * traverse.
         *
         * @param file the file to be tested
         * @return <code>true</code> if condition is met
         */
        public boolean accept( final File file )
        {
            return m_libraryFilter.accept( file )
                    || ( file.isDirectory() && m_recursiveFilter.accept( file ) );
        }
    }

    /**
     * A <code>FileFilter</code> that accepts files with a specified postfix.
     */
    public static final class PostfixFileFilter implements FileFilter
    {
        /**
         * The postfix to filter on.
         */
        private final String m_postfix;

        /**
         * Constructs this to use the specified postfix.
         *
         * @param postfix the postfix to use for filtering
         */
        public PostfixFileFilter( final String postfix )
        {
            m_postfix = postfix;
        }

        /**
         * Tests if the file ends with the specified postfix.
         *
         * @param file the file to be tested
         * @return <code>true</code> if condition is met
         */
        public boolean accept( final File file )
        {
            return file.getName().endsWith( m_postfix );
        }
    }

    /**
     * A <code>FileFilter</code> that can be created to either accept or
     * reject all files.
     */
    private static final class FixedFileFilter implements FileFilter
    {
        /**
         * The fixed response.
         */
        private final boolean m_response;

        /**
         * Constructs this to use the specified response.
         *
         * @param response the response to use for all files
         */
        private FixedFileFilter( final boolean response )
        {
            m_response = response;
        }

        /**
         * Accepts or rejects all files based on the configuration.
         *
         * @param file the file to be tested
         * @return <code>true</code> if condition is met
         */
        public boolean accept( final File file )
        {
            return m_response;
        }
    }

    /**
     * A <code>FileFilter</code> that will accept a file only once.
     */
    private static final class AcceptOnceFileFilter implements FileFilter
    {
        private final Set m_acceptedFiles = new HashSet();
        private final FileFilter m_filter;

        /**
         * Constructs this to use the specified response.
         *
         * @param filter The response to use for all files.
         */
        private AcceptOnceFileFilter( final FileFilter filter )
        {
            m_filter = filter;
        }

        /**
         * Accepts files only once if they match the orginal filter.
         *
         * @param file the file to be tested
         * @return <code>true</code> if condition is met
         */
        public boolean accept( final File file )
        {
            if ( m_acceptedFiles.contains( file ) )
            {
                return false;
            }

            if ( m_filter.accept( file ) )
            {
                m_acceptedFiles.add( file );
                return true;
            }

            return false;
        }
    }
}

