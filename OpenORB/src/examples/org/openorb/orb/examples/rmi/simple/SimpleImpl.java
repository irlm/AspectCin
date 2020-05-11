/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.simple;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

/**
 * The implementation of the SimpleInterface.
 *
 * @author Michael Rumpf
 */
public class SimpleImpl
    extends PortableRemoteObject
    implements SimpleInterface
{
    public SimpleImpl()
        throws RemoteException
    {
    }

    /**
     * @see SimpleInterface
     */
    public String echo( String msg )
        throws RemoteException
    {
        return msg;
    }
    /**
     * @see SimpleInterface
     */
    public void throwException( String msg )
        throws RemoteException
    {
        throw new RemoteException( msg );
    }
}

