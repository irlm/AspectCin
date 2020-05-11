/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;

/**
 * An abstraction for creating sockets.
 *
 * @author Richard G Clark
 * @version $Revision: 1.2 $ $Date: 2003/10/29 14:50:08 $
 */
public interface SocketFactory
{
    /**
     * Creates a new socket to the specified host and port.
     *
     * @param host the remote host
     * @param port the remote port
     * @return a configured <code>Socket</code> instance.
     * @throws IOException if an I/O error occurs when creating the socket.
     */
    Socket createSocket( InetAddress host, int port ) throws IOException;

    /**
     * Creates a new socket listening to the specified host and port.
     *
     * @param host the local host
     * @param port the local port
     * @return a configured <code>ServerSocket</code> instance.
     * @throws IOException if an I/O error occurs when creating the socket.
     */
    ServerSocket createServerSocket( InetAddress host, int port ) throws IOException;
}

