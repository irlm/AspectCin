/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.Socket;

import org.omg.CORBA.INITIALIZE;

import org.openorb.orb.config.ORBLoader;
import org.openorb.util.ConfigUtils;
import org.openorb.util.NumberCache;
import org.openorb.util.ExceptionTool;
import org.openorb.util.logger.LoggerTeam;

/**
 * This strategy handles the functionality of the
 * <code>iiop.SocketInputStreamClass</code> an
 * <code>iiop.SocketOutputStreamClass</code> properties and is only a
 * temporary measure for backward compatibility.
 *
 * @author Richard G Clark
 * @version $Revision: 1.3 $ $Date: 2004/05/17 08:23:00 $
 */
public final class LegacySocketStreamDecorationStrategy
        implements SocketStreamDecorationStrategy
{
    private static final Long TIMESTAMP
            = NumberCache.getLong( System.currentTimeMillis() );

    private final LoggerTeam m_logger;
    private final Constructor m_inputStreamConstructor;
    private final Constructor m_outputStreamConstructor;

    private LegacySocketStreamDecorationStrategy( final LoggerTeam logger,
            final Constructor inputStreamConstructor,
            final Constructor outputStreamConstructor )
    {
        m_logger = logger;
        m_inputStreamConstructor = inputStreamConstructor;
        m_outputStreamConstructor = outputStreamConstructor;
    }

    /**
     * Creates a decorated <code>InputStream</code>.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>InputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    public InputStream decorate( final Socket socket, final InputStream stream )
            throws IOException
    {
        if ( null == m_inputStreamConstructor )
        {
            return stream;
        }
        try
        {
            final Object[] params = new Object[] { stream, TIMESTAMP,
                    createSocketDescription( socket ) };

            return ( InputStream ) m_inputStreamConstructor.newInstance( params );
        }
        catch ( final Exception e )
        {
            // do nothing, just return the parent
            m_logger.warn( "Couldn't create decorator for socket input stream", e );

            return stream;
        }

    }

    /**
     * Creates a decorated <code>OutputStream</code>.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>OutputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    public OutputStream decorate( final Socket socket,  final OutputStream stream )
            throws IOException
    {
        if ( null == m_outputStreamConstructor )
        {
            return stream;
        }
        try
        {
            final Object[] params = new Object[] { stream, TIMESTAMP,
                    createSocketDescription( socket ) };

            return ( OutputStream ) m_outputStreamConstructor.newInstance( params );
        }
        catch ( final Exception e )
        {
            // do nothing, just return the parent
            m_logger.warn( "Couldn't create decorator for socket output stream", e );

            return stream;
        }
    }

    private String createSocketDescription( final Socket socket )
    {
        return "[" + socket.getLocalAddress().getHostName()
                + "].[" + socket.getLocalPort()
                + "]-[" + socket.getInetAddress().getHostName()
                + "].[" + socket.getPort() + "]";
    }

    /**
     * Factory for creating instances of
     * <code>LegacySocketStreamDecorationStrategy</code>.
     */
    public static final class Factory implements SocketStreamDecorationStrategy.Factory
    {
        private static final Class[] INPUT_PARAM_TYPES
                = new Class[] { InputStream.class, Long.TYPE, String.class };

        private static final Class[] OUTPUT_PARAM_TYPES
                = new Class[] { OutputStream.class, Long.TYPE, String.class };

        /**
         * The required no-arg constructor
         */
        public Factory()
        {
        }

        /**
         * Optionally creates a new
         * <code>LegacySocketStreamDecorationStrategy</code>
         * based on the <code>[&lt;prefix&gt;.]SocketInputStreamClass</code>
         * <code>[&lt;prefix&gt;.]SocketOutputStreamClass</code> properties
         * defined in the loader. If both of these properties are
         * absent then no object is created.
         *
         * @param logger the logger team to used
         * @param loader the orb loader to be used
         * @param prefix the optional property name prefix to use, e.g. "iiop"
         *
         * @return a new object or <code>null</code>.
         *
         * @throws INITIALIZE if problems occured during setup
         */
        public SocketStreamDecorationStrategy create( final LoggerTeam logger,
                final ORBLoader loader, final String prefix )
        {
            final String socketInputStreamClassName
                    = ConfigUtils.prefixName( prefix, "SocketInputStreamClass" );
            final String socketOutputStreamClassName
                    = ConfigUtils.prefixName( prefix, "SocketOutputStreamClass" );

            String inputStreamClassName
                    = loader.getStringProperty( socketInputStreamClassName, null );
            String outputStreamClassName
                    = loader.getStringProperty( socketOutputStreamClassName, null );

            if ( "".equals( inputStreamClassName ) )
            {
                inputStreamClassName = null;
            }

            if ( "".equals( outputStreamClassName ) )
            {
                outputStreamClassName = null;
            }

            if ( ( null == inputStreamClassName ) && ( null == outputStreamClassName ) )
            {
                return null;
            }

            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            logger.warn( "Using using deprecated legacy stream decorator" );
            logger.warn( "Using socket input stream : " + inputStreamClassName );
            logger.warn( "Using socket output stream: " + outputStreamClassName );

            final Constructor inputStreamConstructor = getConstructor(
                    logger, classLoader, inputStreamClassName, INPUT_PARAM_TYPES );

            final Constructor outputStreamConstructor = getConstructor(
                    logger, classLoader, outputStreamClassName, OUTPUT_PARAM_TYPES );

            return new LegacySocketStreamDecorationStrategy( logger,
                    inputStreamConstructor, outputStreamConstructor );
        }


        private Constructor getConstructor( final LoggerTeam logger,
                final ClassLoader loader, final String className,
                final Class[] paramTypes )
        {
            if ( null == className )
            {
                return null;
            }
            try
            {
                final Class clazz = loader.loadClass( className );

                return clazz.getConstructor( paramTypes );
            }
            catch ( final ClassNotFoundException cause )
            {
                final String msg = "Class [" + className + "] could not be found";

                logger.error( msg, cause );

                final INITIALIZE e = new INITIALIZE( msg );
                ExceptionTool.initCause( e, cause );

                throw e;
            }
            catch ( final NoSuchMethodException cause )
            {
                final String msg = "Class [" + className
                        + "] did not have required constructor";

                logger.error( msg, cause );

                final INITIALIZE e = new INITIALIZE( msg );
                ExceptionTool.initCause( e, cause );

                throw e;
            }
        }
    }

}

