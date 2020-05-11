/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

/**
 * Instances of this interface manage listening for incoming connection
 * requests.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:50 $
 */
public interface TransportServerInitializer
{
    /**
     * Start listening for incoming connections. Idempotent.
     *
     * @throws org.omg.CORBA.COMM_FAILURE If unable to listen. This will result
     *   in server shutdown.
     * @throws org.omg.CORBA.TRANSIENT If unable to listen, and try again later.
     */
    void open();

    /**
     * Stop listening for a connection. Idempotent.
     */
    void close();

    /**
     * Is is the transport open?
     */
    boolean isOpen();

    /**
     * Listen for an incoming connection.
     *
     * @return transport for new connection, or null if no connection received.
     * @throws org.omg.CORBA.COMM_FAILURE If some permanent comms problem occours
     *   this will result in server shutdown.
     */
    Transport accept( int timeout );
}

