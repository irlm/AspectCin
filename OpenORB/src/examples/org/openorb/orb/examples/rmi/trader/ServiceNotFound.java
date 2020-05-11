/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.trader;

import java.rmi.RemoteException;

/**
 * The exception to indicate that a service can't be found.
 *
 * @author Chris Wood
 */
public class ServiceNotFound
    extends RemoteException
{
    /**
     * Default Constructor.
     */
    public ServiceNotFound()
    {
        super( "Service not exported" );
    }
}

