/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.urlhandler.resource;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.lang.reflect.Method;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * This class is an extension of the URLStreamHandler class. It overrides
 * the method openConnection and adds a level of indirection to the way
 * resources are loaded from a URL.
 *
 * @author  Chris Wood
 * @version $Revision: 1.6 $ $Date: 2004/02/10 21:28:45 $
 */
public final class Handler extends URLStreamHandler
{
    private static final Method INIT_CAUSE_METHOD;

    private final ClassLoader m_classloader;

    static
    {
        Method method;
        try
        {
            final Class[] parameterTypes = {Throwable.class};
            method = Throwable.class.getMethod( "initCause", parameterTypes );
        }
        catch ( final NoSuchMethodException e )
        {
            method = null;
        }
        INIT_CAUSE_METHOD = method;
    }

    /**
     * The empty default constructor is important because the java URL
     * class does a newInstance.
     */
    public Handler()
    {
        this( null );
    }

    /**
     * Creates a new Handler
     * @param cl the classloader used for loading ressources, null falls back
     * to the current thread classloader
     */
    public Handler( ClassLoader cl )
    {
        m_classloader = cl;
    }

    /**
     * Opens a connection to the specified URL.
     *
     * @param url A URL to open a connection to.
     * @return The established connection.
     * @throws IOException When the file specified by the
     * URL could not be found.
     */
    protected URLConnection openConnection( final URL url )
        throws IOException
    {
        final String className = url.getHost();
        final String resourceName = url.getFile().substring( 1 );
        final URL resourceURL;
        if ( ( null != className ) && ( 0 < className.length() ) )
        {
            final Class clazz;
            try
            {
                clazz = getClassLoader().loadClass( className );
            }
            catch ( final ClassNotFoundException cause )
            {
                final MalformedURLException e = new MalformedURLException(
                        "Class " + className + " cannot be found (" + cause + ")" );

                if ( null != INIT_CAUSE_METHOD )
                {
                    try
                    {
                        INIT_CAUSE_METHOD.invoke( e, new Object[] {cause} );
                    }
                    catch ( final Exception e2 )
                    {
                        // ignore as is only best effort
                    }
                }

                throw e;

            }
            resourceURL = clazz.getResource( resourceName );
            if ( resourceURL == null )
            {
                throw new FileNotFoundException(
                      "Class resource " + resourceName + " of class "
                      + className + " cannot be found" );
            }
        }
        else
        {
            resourceURL = getClassLoader().getResource( resourceName );

            if ( resourceURL == null )
            {
                throw new FileNotFoundException( "Resource "
                      + resourceName + " cannot be found" );
            }
        }
        return resourceURL.openConnection();
    }

    /**
     * A short hand method for retrieving the context class loader.
     *
     * @return the context class loader
     */
    private ClassLoader getClassLoader()
    {
        if ( m_classloader == null )
        {
            return Thread.currentThread().getContextClassLoader();
        }
        else
        {
            return m_classloader;
        }
    }
}

