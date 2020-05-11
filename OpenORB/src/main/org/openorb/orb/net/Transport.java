/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import java.io.EOFException;

import org.openorb.orb.io.StorageBuffer;

/**
 * Transport for messages. Overload toString to give some info on the
 * transport layer. Synchronization groupings: <p>
 * ( [open, close, isOpen, (sendMessage, recvMessage)], establishAssociation )<p>
 *
 * @author Chris Wood
 * @version $Revision: 1.5 $ $Date: 2004/02/10 21:02:50 $
 */
public interface Transport
{
    /**
     * Open the connection. This is only ever called by client channels.
     *
     * @throws org.omg.CORBA.COMM_FAILURE failed to open channel. This exception
     *   will be reported to clients.
     */
    void open();

    /**
     * Close the connection.
     */
    void close();

    /**
     * Is is the transport open?
     */
    boolean isOpen();

    /**
     * Write message to comms protocol. If an interrupt occours while writing
     * the interrupt will be preserved.
     *
     * @param msg The message to be sent.
     * @throws org.omg.CORBA.COMM_FAILURE Transport failure occurred.
     */
    void sendMessage( StorageBuffer msg );

    /**
     * Read next message. This function will be regularly serviced by a worker
     * thread. This operation always reads an entire message if it can, if an
     * interupt occours while reading the interrupt will be preserved.
     *
     * @param timeout max time to wait before recieving a message. If 0 wait
     *   until the read occours or the thread performing the read is interrupted.
     * @throws EOFException end of file has been reached. This is an orderly
     *      shutdown.
     * @throws org.omg.CORBA.COMM_FAILURE Transport failure occoured. This is a
     *      disorderly shutdown.
     */
    StorageBuffer recvMessage( int timeout )
        throws EOFException;

    /**
     * Check for applicability of channel for carrying messages for the
     * specified address and setup any client transport binding.
     *
     * @param addr the address.
     * @return true if this transport can carry messages for the target. Basic
     *   checks like checking the host and port will already be done, just check
     *   specifics. If false a new channel will be opened for requests.
     */
    boolean establishAssociation( Address addr );
}

