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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

import org.openorb.orb.config.ORBLoader;

import org.openorb.util.logger.LoggerTeam;

import org.openorb.util.ConfigUtils;

/**
 * A strategy for buffering socket streams
 *
 * @author Richard G Clark
 * @version $Revision: 1.1 $ $Date: 2004/05/14 00:36:25 $
 */
public final class BufferingSocketStreamDecorationStrategy
        implements SocketStreamDecorationStrategy
{
    /**
     * The input buffer size.
     */
    private final int m_inputBufferSize;

    /**
     * The output buffer size.
     */
    private final int m_outputBufferSize;

    /**
     * Constructs a decoration strategy using the specified buffer sizes.
     * Buffer sizes less than or equal to zero indicate that no buffer is
     * required,
     *
     * @param inputBufferSize the size of input buffer to be used
     * @param outputBufferSize the size of the output buffer to be used
     */
    public BufferingSocketStreamDecorationStrategy( final int inputBufferSize,
            final int outputBufferSize )
    {
        m_inputBufferSize = inputBufferSize;
        m_outputBufferSize = outputBufferSize;
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
        if ( m_inputBufferSize <= 0 )
        {
            return stream;
        }

        return new BufferedInputStream( stream, m_inputBufferSize );
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
        if ( m_outputBufferSize <= 0 )
        {
            return stream;
        }

        return new BufferedOutputStream( stream, m_outputBufferSize );
    }

    /**
     * Factory for creating instances of
     * <code>BufferingSocketStreamDecorationStrategy</code>.
     */
    public static final class Factory implements SocketStreamDecorationStrategy.Factory
    {

        /**
         * The required no-arg constructor
         */
        public Factory()
        {
        }

        /**
         * Optionally creates a new
         * <code>BufferingSocketStreamDecorationStrategy</code>
         * based on the <code>[&lt;prefix&gt;.]bufferedInputStreamSize</code>
         * <code>[&lt;prefix&gt;.]bufferedOutputStreamSize</code> properties
         * defined in the loader. If both of these properties are
         * absent or zero then no object is created.
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

            final String bufferedInputStreamSizeName
                    = ConfigUtils.prefixName( prefix, "bufferedInputStreamSize" );

            final String bufferedOutputStreamSizeName
                    = ConfigUtils.prefixName( prefix, "bufferedOutputStreamSize" );

            final int inputBufferSize
                    = loader.getIntProperty( bufferedInputStreamSizeName, 0 );

            final int outputBufferSize
                    = loader.getIntProperty( bufferedOutputStreamSizeName, 0 );


            if ( ( 0 < inputBufferSize ) || ( 0 < outputBufferSize ) )
            {
                return new BufferingSocketStreamDecorationStrategy(
                        inputBufferSize, outputBufferSize );
            }

            return null;
        }
    }

}

