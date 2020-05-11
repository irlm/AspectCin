/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

/**
 * The client protocol is used to generate client bindings from an IOR and may
 * perform some management of the client channels.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:50 $
 */
public interface ClientProtocol
{
    /**
     * An orb reference.
     */
    org.omg.CORBA.ORB orb();

    /**
     * Create addresses from component in IOR. The client addresses in the
     * returned bindings should return identical results for each of the
     * addressing disposition types.
     */
    Address [] createAddresses( org.omg.GIOP.IORAddressingInfo address );

    /**
     * Returns a ClientBinding object, prioritised at the per-profile
     * (inter-component) level.
     */
    ClientBinding createBinding( Address address );
}

