/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.io.BufferedInputStream;
import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

/**
 * This class is used as a class loader to load class from an URL.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:28:45 $
 */
public class URLLoader
    extends ClassLoader
{
    /** URL list. */
    private URL [] m_urls;

    /**
     * Constructor.
     *
     * @param urls The list of urls to load classes from.
     */
    public URLLoader( URL [] urls )
    {
        m_urls = urls;
    }

    /**
     * Return the url array.
     *
     * @return The array of URLs to load classes from.
     */
    public URL [] getURLs()
    {
        return m_urls;
    }

    /**
     * Try different ways to load a class.
     *
     * @param name The name of the class to load.
     * @param resolve When true try to resolve the class.
     * @return The class if one was found.
     * @throws ClassNotFoundException When the class can't be found
     * either via the current class loader or the system class loader.
     */
    protected Class loadClass( String name, boolean resolve )
        throws ClassNotFoundException
    {
        Class c = null;

        // Try to find amongs the loaded classes
        c = findLoadedClass( name );

        // Now, try to use to system loader
        if ( c == null )
        {
            try
            {
                c = findSystemClass( name );
            }
            catch ( ClassNotFoundException ex )
            {
                // ignore, try more ways to load the class below
            }
        }

        // Try to find a class from an URL.
        if ( c == null )
        {
            c = findClass( name );
        }
        // Try local
        if ( c == null )
        {
            c = java.lang.Thread.currentThread().getContextClassLoader().loadClass( name );
        }
        if ( resolve && c != null )
        {
            resolveClass( c );
        }
        if ( c == null )
        {
            throw new ClassNotFoundException();
        }
        return c;
    }

    /**
     * This function is used to find a class from an URL.
     *
     * @param name The class to find
     * @return The class if found.
     */
    protected Class findClass( String name )
    {
        byte[] b = null;
        // Try to get a class
        for ( int i = 0; i < m_urls.length; ++i )
        {
            try
            {
                b = extractClassFromURL( new URL( m_urls[ i ], name ) );
                if ( b != null )
                {
                    break;
                }
            }
            catch ( MalformedURLException ex )
            {
                // try the remaining URLs before giving up
            }
        }
        if ( b == null )
        {
            return null;
        }
        // Then define the class
        return defineClass( name, b, 0, b.length );
    }

    /**
     * Get a class from an URL site.
     *
     * @param url The URL to extract the class from.
     * @return The byte code of the class.
     */
    private byte [] extractClassFromURL( URL url )
    {
        try
        {
            URLConnection conn = url.openConnection();

            byte [] buf = new byte[ conn.getContentLength() ];

            BufferedInputStream is = new BufferedInputStream( url.openStream() );

            for ( int i = 0; i < buf.length; i += is.read( buf, i, buf.length - 1 ) )
            {
                // do nothing
            }

            is.close();

            return buf;
        }
        catch ( IOException ex )
        {
            // we will return null below
        }

        return null;
    }
}

