/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.simple;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface for simple RMI-IIOP server.
 *
 * @author Michael Rumpf
 */
public interface SimpleInterface
    extends Remote
{
    /**
     * Echo a Java object.
     *
     * @param obj The obj to be returned.
     * @return The object passed to this method.
     * @throws RemoteException When an error occurs.
     */
    String echo( String obj )
        throws RemoteException;

    /**
     * Throw a RemoteException.
     *
     * @param msg The exception message.
     * @throws RemoteException Always.
     */
    void throwException( String msg )
        throws RemoteException;

}

