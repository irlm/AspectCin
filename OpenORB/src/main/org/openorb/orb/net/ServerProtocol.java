/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

/**
 * The server protocol represents a listen point at which clients may create
 * a ServerChannel object through which to issue requests. The server protocol
 * is also responsible for constructing profiles to be published within an IOR.
 *
 * @author Unknown
 */
public interface ServerProtocol
{
    /**
     * An orb reference.
     */
    org.omg.CORBA.ORB orb();

    /**
     * The channel state.
     */
    int state();

    /**
     * Listening for new connections. <code>perform_work</code> waits
     * for a specified time for a new incoming connections and
     * work_pending returns true if there is a queued connection
     * request.
     */
    int STATE_LISTENING = 0;

    /**
     * Paused. Connection requests are queued. Protocols which serve requests
     * are created in this state. Listen returns immediatly and any thread used
     * in run_listen will be sent to sleep.
     */
    int STATE_PAUSED = 1;

    /**
     * Closed. Incoming connections are being refused.
     * <code>perform_work</code> returns false. Leaving this state
     * will result in re-registering with the ServerManager.
     */
    int STATE_CLOSED = 2;

    /**
     * Move to the listening state. If in the closed state this will result in
     * the protocol re-registering itself with the ServerManager. Returns true
     * if state changed.
     */
    boolean open();

    /**
     * Move to the paused state. This is only valid in the LISTENING state.
     * returns true if state is now PAUSED.
     */
    boolean pause();

    /**
     * Stop listening, refuse all incoming connections. Returns true if state
     * is now CLOSED.
     */
    void close();

    /**
     * Listen for a single connection. If a connection is available this will
     * create and register a new server socket, otherwise it will return once
     * the timeout has expired. A negative or zero timeout will be changed to
     * a maximum.
     */
    void listen( int timeout );

    /**
     * Donate a thread for listening. This function returns when interrupt
     * is called on the thread or the protocol is closed.
     */
    void run_listen();

    /**
     * Construct a tagged profile from parts. This is used by the ServerManager.
     * If the profile should not be included in an IOR this may return null.
     */
    org.omg.IOP.TaggedProfile create_profile( int profile_tag,
             org.openorb.orb.pi.ComponentSet component_set, byte [] object_key );
}

