/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

/**
 * A server channel represents a connection with a client through which
 * requests can be issued.
 *
 * @author Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/07/22 12:25:45 $
 */
public interface ServerChannel
    extends Channel
{
    /**
     * An orb reference.
     */
    org.omg.CORBA.ORB orb();

    /**
     * Return the state of the channel.
     */
    int state();

    /**
     * Connection open and ready for use.
     */
    int STATE_CONNECTED = 0;

    /**
     * Connection closed perminently. All requests currently procesing
     * will fail to return.
     */
    int STATE_CLOSED = 1;

    // state management

    /**
     * Close the connection after all currently processing requests are
     * complete. If there are no currently active requests the channel will
     * close immediatly otherwise if ifActive is true then the channel will
     * close once all currently active requests have completed, and in the
     * mean time new incomming requests will be silently discarded. This
     * call returns immediatly, use the state function to interrogate the
     * state while a close is pending.
     */
    void soft_close( boolean reject_new );

    /**
     * Close the connection. This closes the channel immediatly, all replys to
     * current requests will be discarded.
     */
    void close();
}

