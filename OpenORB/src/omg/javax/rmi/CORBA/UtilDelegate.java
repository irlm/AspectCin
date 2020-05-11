/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/
package javax.rmi.CORBA;

/**
 * The implementation delegate class for javax.rmi.CORBA.Util must implement the
 * following interface for per-class delegation
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:53 $
 */

public interface UtilDelegate
{
    java.rmi.RemoteException mapSystemException( org.omg.CORBA.SystemException ex );

    void writeAny( org.omg.CORBA.portable.OutputStream out, java.lang.Object obj );

    java.lang.Object readAny( org.omg.CORBA.portable.InputStream in );

    void writeRemoteObject( org.omg.CORBA.portable.OutputStream out, java.lang.Object obj );

    void writeAbstractObject( org.omg.CORBA.portable.OutputStream out, java.lang.Object obj );

    public void registerTarget( Tie tie, java.rmi.Remote target );

    public void unexportObject( java.rmi.Remote target );

    public Tie getTie( java.rmi.Remote target );

    public ValueHandler createValueHandler();

    public String getCodebase( Class clz );

    public Class loadClass( String className,
                            String remoteCodebase,
                            Class loadingContext )
    throws ClassNotFoundException;

    public Class loadClass( String className,
                            String remoteCodebase,
                            ClassLoader loadingContext )
    throws ClassNotFoundException;

    public boolean isLocal( Stub s )
        throws java.rmi.RemoteException;

    public java.rmi.RemoteException wrapException( Throwable obj );

    public java.lang.Object copyObject( java.lang.Object obj, org.omg.CORBA.ORB orb )
        throws java.rmi.RemoteException;

    public java.lang.Object [] copyObjects( java.lang.Object [] obj, org.omg.CORBA.ORB orb )
        throws java.rmi.RemoteException;


}
