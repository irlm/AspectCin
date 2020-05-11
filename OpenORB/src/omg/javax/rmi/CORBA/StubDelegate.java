/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/
package javax.rmi.CORBA;

/**
 * The implementation delegate class for javax.rmi.CORBA.Stub must implement
 * the following interface for per-instance delegation.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:52 $
 */

public interface StubDelegate
{
    int hashCode( Stub self );

    boolean equals( Stub self, java.lang.Object obj );

    String toString( Stub self );

    void connect( Stub self, org.omg.CORBA.ORB orb )
        throws java.rmi.RemoteException;

    void writeObject( Stub self, java.io.ObjectOutputStream s )
        throws java.io.IOException;

    void readObject( Stub self, java.io.ObjectInputStream s )
        throws java.io.IOException, ClassNotFoundException;
}
