/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

/**
 * Client channel interface. A client channel creates and sends requests
 * and recieves replies.
 *
 * @author Chris Wood
 * @version $Revision: 1.5 $ $Date: 2004/07/22 12:25:45 $
 */
public interface ClientChannel
    extends Channel
{
    /**
     * An orb reference.
     */
    org.omg.CORBA.ORB orb();

    /**
     * Connection state. Note that state values are 0x1000000 apart,
     * Allowing this value to be ORed with the binding priority on the
     * client side.
     */
    int state();

    /**
     * Connection open. In this state requests can be created without
     * checking the rebind policy.
     */
    int STATE_CONNECTED = 0x11000000;

    /**
     * Channel paused. The channel has been shut down temporaraly to conserve
     * resources or has recently been created. Creating new requests with
     * with rebind policy set to NO_RECONNECT will result in a REBIND exception.
     * Channels are created in this state.
     */
    int STATE_PAUSED = 0x12000000;

    /**
     * Channel closed. This state is typicaly reached if transmission
     * difficulties are experienced. Attempting to create requests on closed
     * channels will cause a system exception. The channel can be reopened only
     * using the open operation.
     */
    int STATE_CLOSED = 0x13000000;

    // state management

    /**
     * Pause the channel. If no requests are currently active enter the
     * paused state.
     */
    void pause();

    /**
     * Change to the CLOSED state and reject new requests by throwing a system
     * exception with status COMPLETED_NO. If kill_requests is true, call cancel
     * on any active ClientRequests, otherwise wait until active requests
     * complete before closing the commmunication channel.
     */
    void close( boolean kill_requests, org.omg.CORBA.SystemException ex );

    // request creation.

    /**
     * Create a request. If this is the first request on this channel
     * then client_connect will be called on all ChannelInterceptor
     * before returning the request. This may throw a system exception if the
     * channel cannot establish a connection for some reason, for example
     * INV_POLICY if client side policies prevent a successfull invocation,
     * COMM_FAILURE if a communication problem occours, or REBIND if channel
     * is temporaraly closed and a NO_REBIND policy is in effect.
     *
     * @param target The target of the request.
     * @param address The target address. If the target has been redirected
     *            this may not correspond to the target's ior.
     */
    ClientRequest create_request( org.omg.CORBA.Object target,
                                         Address address,
                                         String operation,
                                         boolean response_expected )
        throws RebindChannelException;

    /**
     * Create a locate request. This may throw a system exception if the
     * channel cannot establish a connection for some reason, for example
     * INV_POLICY if client side policies prevent a successfull invocation,
     * COMM_FAILURE if a communication problem occours, or REBIND if channel
     * is temporaraly closed and a NO_REBIND policy is in effect.
     *
     * @param target The target of the request.
     * @param address The target address. If the target has been redirected
     *            this may not correspond to the target's ior.
     */
    ClientRequest create_locate_request( org.omg.CORBA.Object target,
            Address address )
        throws RebindChannelException;
}
