/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.trader;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface for a trading component.
 *
 * @author Chris Wood
 */
public interface TraderInterface
    extends Remote
{
    /**
     * Add a service offer to the trader.
     *
     * @param offer The offer to register.
     * @throws RemoteException ???
     */
    void addServiceOffer( Offer offer )
        throws RemoteException;

    /**
     * Query the offers with a set of properties.
     *
     * @param name The name of the offer.
     * @param required The property list for which a match must be found.
     * @return The offer that matches the query of null if not matching service was found.
     * @throws RemoteException ???
     * @throws PropertyMismatch If the properties don't match.
     * @throws ServiceNotFound If no services have been registered yet.
     */
    Remote getServiceOffer( String name, PropertyList required )
        throws RemoteException, PropertyMismatch, ServiceNotFound;
}

