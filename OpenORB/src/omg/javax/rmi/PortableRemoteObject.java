/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/
package javax.rmi;

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;

/**
 * This class is intended to act as a base class for RMI/IDL server implementation classes.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:52 $
 */
public class PortableRemoteObject
{
    // Key for Util class
    private static final String portableRemoteObjectClassKey =
            "javax.rmi.CORBA.PortableRemoteObjectClass";

    // Default value for this key
    private static final String defaultPortableRemoteObject =
            "org.openorb.orb.rmi.PortableRemoteObjectDelegateImpl";

    // Stub Class
    static private java.lang.Class delegateClass;

    // Reference to stub delegate
    static private javax.rmi.CORBA.PortableRemoteObjectDelegate _delegate;

    // Load Stub class
    static {
        String className = System.getProperty( portableRemoteObjectClassKey );

        if ( className == null )
        {
            Properties fileProps = new Properties();

            try
            {
                // Check if orb.properties exists
                String javaHome = System.getProperty( "java.home" );
                File propFile = new File( javaHome + File.separator + "lib"
                                          + File.separator + "orb.properties" );

                if ( propFile.exists() )
                {
                    // Load properties from orb.properties
                    FileInputStream fis = new FileInputStream( propFile );

                    try
                    {
                        fileProps.load( fis );
                    }
                    finally
                    {
                        fis.close();
                    }

                    className = fileProps.getProperty( portableRemoteObjectClassKey );
                }
            }
            catch ( Exception ex )
            {}

        }

        if ( className == null )
            className = defaultPortableRemoteObject;

        try
        {
            delegateClass = Thread.currentThread().getContextClassLoader().loadClass( className );

            _delegate = ( javax.rmi.CORBA.PortableRemoteObjectDelegate )
                    delegateClass.newInstance();
        }
        catch ( java.lang.Exception ex )
        {
            throw new org.omg.CORBA.INTERNAL( "Unable to load RMI over IIOP Util class" );
        }
    }

    /**
     * This constructor is called by the derived implementation class to initialize the
     * base class state
     */
    protected PortableRemoteObject()
        throws java.rmi.RemoteException
    {
        if ( this instanceof java.rmi.Remote )
            exportObject( ( java.rmi.Remote ) this );
    }

    /**
     * A call to this method with no objects exported creates a non-daemon thread wich keeps the
     * java virtual machine alive until all exported objects have been unexported by calling
     * unexportObject.
     */
    public static void exportObject( java.rmi.Remote obj )
        throws java.rmi.RemoteException
    {
        _delegate.exportObject( obj );
    }

    /**
     * This method takes a server implementation object and returns a stubobject that can
     * be used to access that server object. The argument object must currently be exported,
     * either because it is a subclass f PortableRemoteObject or be virtue of a previous call
     * to PortableRemoteObject.exportObject.
     */
    public static java.rmi.Remote toStub( java.rmi.Remote obj )
        throws java.rmi.NoSuchObjectException
    {
        return _delegate.toStub( obj );
    }

    /**
     * This method is used to deregister a currently exported server object from the ORB
     * runtimes, allowing the object to become available for garbage collection.
     */
    public static void unexportObject( java.rmi.Remote obj )
        throws java.rmi.NoSuchObjectException
    {
        _delegate.unexportObject( obj );
    }

    /**
     * This method takes an object reference or an object of an RMI/IDL abstract interface type
     * and attemps to narrow it to conform to the given newClass RMI/IDL type.
     */
    public static java.lang.Object narrow( java.lang.Object obj, Class newClass )
        throws ClassCastException
    {
        return _delegate.narrow( obj, newClass );
    }

    /**
     * This method makes the remote object target ready for remote communication using the
     * same communications runtime as source.
     */
    public static void connect( java.rmi.Remote target, java.rmi.Remote source )
        throws java.rmi.RemoteException
    {
        _delegate.connect( target, source );
    }
}
