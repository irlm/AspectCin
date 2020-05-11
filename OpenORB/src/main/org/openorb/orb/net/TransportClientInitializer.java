/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

/**
 * Interface for creating sockets.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:50 $
 */
public interface TransportClientInitializer
{
    /**
     * Set the MessageTransport constructor for each of the addresses.
     * All the addresses will be alternative endpoints from a single IOR profile.
     *
     * @return new list of addresses.
     */
    Address [] establishTransports( Address [] addresses );
}

