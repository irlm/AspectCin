/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

/**
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:50 $
 */
public interface ClientManager
{
    /**
     * Return an orb reference.
     */
    org.omg.CORBA.ORB orb();

    /**
     * Get a reference to the server manager. This is used to find the adapter
     * for local requests.
     *
     * @return the server manager, or null if this is a client-only orb.
     */
    ServerManager getServerManager();

    /**
     * Create bindings for a given IOR.
     *
     * @param obj target object.
     * @param ior IOR of target.
     * @return array of client bindings.
     * @throws org.omg.CORBA.INV_OBJREF the object reference is invalid.
     */
    ClientBinding [] create_bindings( org.omg.CORBA.Object obj, org.omg.IOP.IOR ior );

    /**
     * Register a client protocol.
     */
    void register_protocol( int profile_tag, ClientProtocol protocol );

    /**
     * Register a channel to join the work queue. This is called when the channel
     * enters the CONNECTED state.
     */
    boolean register_channel( ClientChannel channel );

    /**
     * Called when a channel exits the CONNECTED state. Returns once
     * all channel threads have completed their work cycles.
     */
    void unregister_channel( ClientChannel channel );

    /**
     * Shutdown the client side of the orb.
     *
     * @param wait_for_complete true if the operation should not return
     *     until the client side is shut down.
     * @param kill_requests true if requests which are currently being
     *     processed should be killed.
     */
    void shutdown( boolean wait_for_complete, boolean kill_requests );
}

