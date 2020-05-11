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
 * A stretegy that does nothing.
 *
 * @author Richard G Clark
 * @version $Revision: 1.1 $ $Date: 2004/05/17 08:23:00 $
 */
public final class NullSocketStreamDecorationStrategy
        implements SocketStreamDecorationStrategy
{
    private static final NullSocketStreamDecorationStrategy INSTANCE
            = new NullSocketStreamDecorationStrategy();

    private NullSocketStreamDecorationStrategy()
    {
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return the singleton instance
     */
    public static NullSocketStreamDecorationStrategy getInstance()
    {
        return INSTANCE;
    }

    /**
     * Returns the passed stream without modifcation.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>InputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    public InputStream decorate( final Socket socket, final InputStream stream )
            throws IOException
    {
        return stream;
    }

    /**
     * Returns the passed stream without midifcation.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>OutputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    public OutputStream decorate( final Socket socket,  final OutputStream stream )
            throws IOException
    {
        return stream;
    }

}

