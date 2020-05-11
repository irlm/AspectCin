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

import org.openorb.orb.config.ORBLoader;

import org.openorb.util.logger.LoggerTeam;

/**
 * A strategy for decorating sockets streams.
 *
 * @author Richard G Clark
 * @version $Revision: 1.1 $ $Date: 2004/05/14 00:36:25 $
 */
public interface SocketStreamDecorationStrategy
{
    /**
     * Creates a decorated <code>InputStream</code>.
     * Note that the implementation which throws an exception must also
     * close the original stream.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>InputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    InputStream decorate( Socket socket, InputStream stream ) throws IOException;

    /**
     * Creates a decorated <code>OutputStream</code>.
     * Note that the implementation which throws an exception must also
     * close the original stream.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>OutputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    OutputStream decorate( Socket socket, OutputStream stream ) throws IOException;

    /**
     * Factory interface for creating instances of
     * <code>SocketStreamDecorationStrategy</code>.
     * Implementations must provide a zero argument constructor.
     */
    public interface Factory
    {
        /**
         * Optionally creates a new <code>SocketStreamDecorationStrategy</code>
         * based on the configuration values.
         *
         * @param logger the logger team to used
         * @param loader the orb loader to be used
         * @param prefix the optional property name prefix to use, e.g. "iiop"
         *
         * @return a new object or <code>null</code>.
         *
         * @throws INITIALIZE if problems occured during setup
         */
        SocketStreamDecorationStrategy create( LoggerTeam logger,
                ORBLoader loader, String prefix );
    }
}
