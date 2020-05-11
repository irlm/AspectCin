/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.urlhandler;

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * A Utility class to load the URL handlers into the system classloader.
 * This is a workaround for JRE's older than 1.5.
 *
 * @author  Richard G Clark
 * @version $Revision: 1.2 $ $Date: 2004/02/03 23:25:32 $
 */
public final class HandlerLoader
{
    private static final int INITIAL_BUFFER_SIZE = 1024;
    private static final String ROOT_PACKAGE_DIR = "org/openorb/util/urlhandler";
    private static final String ROOT_PACKAGE_NAME = "org.openorb.util.urlhandler";
    private static final String CLASS_NAME = "Handler";
    private static final Method DEFINE_CLASS_METHOD;

    static
    {
        try
        {
            DEFINE_CLASS_METHOD = ClassLoader.class.getDeclaredMethod(
                    "defineClass", new Class[] { String.class, byte[].class,
                    Integer.TYPE, Integer.TYPE } );
        }
        catch ( final NoSuchMethodException e )
        {
            throw new NoSuchMethodError( e.getMessage() );
        }
        DEFINE_CLASS_METHOD.setAccessible( true );
    }

    /**
     * Instances of this class should not be created!
     */
    private HandlerLoader()
    {
        throw new Error( "Instances of this class should not be created!" );
    }

    /**
     * Ensures that the required resource handlers are loaded by the
     * the system class loader.
     *
     * @param classSource the classLoader to load the handler bytecode from
     *        if necessary.
     *
     * @throws ClassNotFoundException if the classes could not be loaded.
     */
    public static void loadHandlers( final ClassLoader classSource )
            throws ClassNotFoundException
    {
        loadHandler( classSource, "resource" );
        loadHandler( classSource, "classpath" );
    }


    /**
     * Ensures that the handler for the specified schema is loaded in the
     * system class loader.
     *
     * @param classSource the loader to load the resource from
     * @param schema the URL schema to be handled
     *
     * @throws ClassNotFoundException if a problem occured during loading.
     */
    private static void loadHandler( final ClassLoader classSource, final String schema )
            throws ClassNotFoundException
    {
        final String classResource
                = ROOT_PACKAGE_DIR + "/" + schema + "/" + CLASS_NAME + ".class";
        final String className
                = ROOT_PACKAGE_NAME + "." + schema + "." + CLASS_NAME;

        try
        {
            ClassLoader.getSystemClassLoader().loadClass( className );
            // class already loaded or accessible from classpath
            return;
        }
        catch ( final ClassNotFoundException e )
        {
            loadIntoSystemClasLoader( classSource, classResource, className );
        }

    }

    /**
     * Loads a class into the system class loader using the resource from the
     * specified source class loader.
     *
     * @param classSource the loader to load the resource from
     * @param classResource the resource name to of the class byte codes
     * @param className the name of the class to be loaded
     *
     * @throws ClassNotFoundException if a problem occured during loading.
     */
    private static void loadIntoSystemClasLoader( final ClassLoader classSource,
            final String classResource, final String className )
            throws ClassNotFoundException
    {
        final InputStream is = classSource.getResourceAsStream( classResource );

        if ( null == is )
        {
            throw new ClassNotFoundException( "Could not load class ["
                    + className + "] as resource [" + classResource
                    + "] could not be found." );
        }

        try
        {
            try
            {
                defineClass( className, is );
            }
            finally
            {
                is.close();
            }
        }
        catch ( final Exception e )
        {
            final ClassNotFoundException rethrow = new ClassNotFoundException(
                    "Could not load class [" + className + "] due to exception ["
                    + e.getMessage() + "] reading resource [" + classResource + "]" );
        }
    }

    /**
     * This method is used to define a class in the system class laoder
     *
     * @param className the name of the class
     * @param is the stream to load the class from
     *
     * @throws IOException if an problem occured reading the stream.
     * @throws IllegalAccessException if accessiblity overrides didn't work.
     * @throws InvocationTargetException if the system class loader's
     *         <code>defineClass</code> method threw an exception.
     */
    private static void defineClass( final String className, final InputStream is )
            throws IOException, IllegalAccessException, InvocationTargetException
    {
        byte[] buffer = new byte[INITIAL_BUFFER_SIZE];
        int length = 0;
        int readCount = is.read( buffer, 0, buffer.length );

        while ( -1 != readCount )
        {
            length += readCount;
            if ( 0 == ( buffer.length - length ) )
            {
                final byte[] newBuffer = new byte[buffer.length << 1];
                System.arraycopy( buffer, 0, newBuffer, 0, buffer.length );
                buffer = newBuffer;
            }

            readCount = is.read( buffer, length, buffer.length - length );
        }

        final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();

        DEFINE_CLASS_METHOD.invoke( systemLoader, new Object[] { className,
                buffer, new Integer( 0 ), new Integer( length ) } );
    }



}
