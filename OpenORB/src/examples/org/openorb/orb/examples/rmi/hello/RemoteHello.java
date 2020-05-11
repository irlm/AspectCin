/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.hello;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The remote interface for the hello examples.
 *
 * @author Chris Wood
 */
public interface RemoteHello
    extends Remote
{
    /**
     * Print a message on the server.
     *
     * @param message The message to print.
     * @throws RemoteException When an error occurs.
     */
    void print( String message ) throws RemoteException;
}

