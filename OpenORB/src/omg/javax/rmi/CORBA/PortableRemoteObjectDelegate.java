/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/
package javax.rmi.CORBA;

/**
 * The implementation delegate class for javax.rmi.PortableRemoteObject must implement the
 * following interface for per-class delegation.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:52 $
 */

public interface PortableRemoteObjectDelegate
{
    public void exportObject( java.rmi.Remote obj )
        throws java.rmi.RemoteException;

    public java.rmi.Remote toStub( java.rmi.Remote obj )
        throws java.rmi.NoSuchObjectException;

    public void unexportObject( java.rmi.Remote obj )
        throws java.rmi.NoSuchObjectException;

    public java.lang.Object narrow( java.lang.Object obj, Class newClass )
        throws ClassCastException;

    public void connect( java.rmi.Remote target, java.rmi.Remote source )
        throws java.rmi.RemoteException;
}
