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
import java.net.Socket;

/**
 * A strategy for decorating sockets streams using 2 other strategies in sequence.
 *
 * @author Richard G Clark
 * @version $Revision: 1.1 $ $Date: 2004/05/14 00:36:25 $
 */
public final class CompositeSocketStreamDecorationStrategy
        implements SocketStreamDecorationStrategy
{
    private final SocketStreamDecorationStrategy m_strategy1;
    private final SocketStreamDecorationStrategy m_strategy2;

    /**
     * Constructs a composite strategy from two other strategies.
     *
     * @param strategy1 the first strategy to apply
     * @param strategy2 the second strategy to apply
     */
    public CompositeSocketStreamDecorationStrategy(
            final SocketStreamDecorationStrategy strategy1,
            final SocketStreamDecorationStrategy strategy2 )
    {
        m_strategy1 = strategy1;
        m_strategy2 = strategy2;
    }

    /**
     * Creates a decorated <code>InputStream</code>, by first applying
     * decoration strategy1 then applying strategy2 to the resulting stream.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>InputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    public InputStream decorate( final Socket socket, final InputStream stream )
            throws IOException
    {
        final InputStream stream1 = m_strategy1.decorate( socket, stream );
        return m_strategy2.decorate( socket, stream1 );
    }

    /**
     * Creates a decorated <code>InputStream</code>, by first applying
     * decoration strategy1 then applying strategy2 to the resulting stream.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>OutputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    public OutputStream decorate( final Socket socket,  final OutputStream stream )
            throws IOException
    {
        final OutputStream stream1 = m_strategy1.decorate( socket, stream );
        return m_strategy2.decorate( socket, stream1 );
    }
}

