/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package javax.rmi.CORBA;

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;

/**
 * This class provides some utility methods to manage RMI objects and mechanisms over GIOP
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:52 $
 */
public class Util
{
    // Key for Util class
    private static final String utilClassKey = "javax.rmi.CORBA.UtilClass";

    // Default value for this key
    private static final String defaultUtil = "org.openorb.orb.rmi.UtilDelegateImpl";

    // Stub Class
    static private java.lang.Class delegateClass;

    // Reference to stub delegate
    static private javax.rmi.CORBA.UtilDelegate _delegate;

    // Load Stub class
    static
    {
        String className = System.getProperty( utilClassKey );

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

                    className = fileProps.getProperty( utilClassKey );
                }
            }
            catch ( Exception ex )
            {
                // TODO: ???
            }
        }

        if ( className == null )
        {
            className = defaultUtil;
        }

        try
        {
            delegateClass = Thread.currentThread().getContextClassLoader().loadClass( className );

            _delegate = ( javax.rmi.CORBA.UtilDelegate ) delegateClass.newInstance();
        }
        catch ( java.lang.Exception ex )
        {
            throw new org.omg.CORBA.INTERNAL( "Unable to load RMI over IIOP Util class: "
                    + className + " (" + ex + ")" );
        }
    }

    /**
     * This method maps a CORBA system exception to a java.rmi.RemoteException
     */
    public static java.rmi.RemoteException mapSystemException( org.omg.CORBA.SystemException ex )
    {
        return _delegate.mapSystemException( ex );
    }

    /**
     * This method writes the java object obj to the output stream out in the form of a GIOP any.
     */
    public static void writeAny( org.omg.CORBA.portable.OutputStream out,
                                 java.lang.Object obj )
    {
        _delegate.writeAny( out, obj );
    }

    /**
     * This method reads a GIOP any from the input stream in and unmarshals it as a java object.
     */
    public static java.lang.Object readAny( org.omg.CORBA.portable.InputStream in )
    {
        return _delegate.readAny( in );
    }

    /**
     * This method is an utility method for use by stubs when writing an RMI/IDL object reference
     * to an output stream.
     */
    public static void writeRemoteObject( org.omg.CORBA.portable.OutputStream out,
                                          java.lang.Object obj )
    {
        _delegate.writeRemoteObject( out, obj );
    }

    /**
     * This method is another similar utility method for used by stubs.
     */
    public static void writeAbstractObject( org.omg.CORBA.portable.OutputStream out,
                                            java.lang.Object obj )
    {
        _delegate.writeAbstractObject( out, obj );
    }

    /**
     * This method is needed to support unexportObject, because it takes a target implementation
     * object as its parameter, it is necessary for the Util class to maintain a table mapping target
     * objects back to their associated Ties.
     */
    public static void registerTarget( Tie tie, java.rmi.Remote target )
    {
        _delegate.registerTarget( tie, target );
    }

    /**
     * Thie method deactivates an implementation object and remove its associated Tie from the table
     * mainted by the Util class.
     */
    public static void unexportObject( java.rmi.Remote target )
    {
        _delegate.unexportObject( target );
    }

    /**
     * This method return the Tie object for an implementation object target, or null if no Tie is
     * registered for the target object.
     */
    public static Tie getTie( java.rmi.Remote target )
    {
        return _delegate.getTie( target );
    }

    /**
     * This method returns a singleton instance of a class that implements the ValueHandler interface.
     */
    public static ValueHandler createValueHandler()
    {
        return _delegate.createValueHandler();
    }

    /**
     * The wrapException method wraps an exception thrown by an implementation method. It returns the
     * corresponding client-side exception.
     */
    public static java.rmi.RemoteException wrapException( Throwable obj )
    {
        return _delegate.wrapException( obj );
    }

    /**
     * This method is used by local stubs to copy an actuel parameter, result object or exception.
     */
    public static java.lang.Object copyObject( java.lang.Object obj, org.omg.CORBA.ORB orb )
        throws java.rmi.RemoteException
    {
        return _delegate.copyObject( obj, orb );
    }

    /**
     * This method do the same as above on multiple objects.
     */
    public static java.lang.Object [] copyObjects( java.lang.Object [] obj, org.omg.CORBA.ORB orb )
        throws java.rmi.RemoteException
    {
        return _delegate.copyObjects( obj, orb );
    }

    /**
     * This method has the same semantics as the ObjectImpl _is_local() method, except that it can
     * throw a RemoteException
     */
    public static boolean isLocal( Stub s )
        throws java.rmi.RemoteException
    {
        return _delegate.isLocal( s );
    }

    /**
     * This method returns a java class object for the Class object clz as a space-separated list
     * of URLs.
     */
    public static String getCodebase( Class clz )
    {
        return _delegate.getCodebase( clz );
    }

    /**
     * This method loads a java class object the java class name, using additional
     * information passed in the remoteCodeBase and loadingContext parameters.
     */
    public static Class loadClass( String className,
                                   String remoteCodebase,
                                   Class loadingContext )
        throws ClassNotFoundException
    {
        return _delegate.loadClass( className, remoteCodebase, loadingContext.getClassLoader() );
    }

    /**
     * This method loads a java class object the java class name, using additional
     * information passed in the remoteCodeBase and loadingContext parameters.
     */
    public static Class loadClass( String className,
                                   String remoteCodebase,
                                   ClassLoader loadingContext )
        throws ClassNotFoundException
    {
        return _delegate.loadClass( className, remoteCodebase, loadingContext );
    }
}
