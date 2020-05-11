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
 * This class is a base class for all RMI/IDL stub.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:52 $
 */

public abstract class Stub extends org.omg.CORBA_2_3.portable.ObjectImpl
            implements java.io.Serializable
{
    protected Stub()
    {}

    // -------------------------------------------------------------------------
    //

    // Key for stub class


    private static final String stubClassKey = "javax.rmi.CORBA.StubClass";

    // Default value for this key
    private static final String defaultStub = "org.openorb.orb.rmi.StubDelegateImpl";

    // Stub Class
    private static java.lang.Class delegateClass;

    // Reference to stub delegate
    private transient javax.rmi.CORBA.StubDelegate _delegate;

    // Load Stub class
    static
    {
        String className = System.getProperty( stubClassKey );

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

                    className = fileProps.getProperty( stubClassKey );
                }
            }
            catch ( Exception ex )
            {}

        }

        if ( className == null )
            className = defaultStub;

        try
        {
            delegateClass = Thread.currentThread().getContextClassLoader().loadClass( className );
        }
        catch ( java.lang.Exception ex )
        {
            throw new org.omg.CORBA.INTERNAL( "Unable to load RMI over IIOP stub class" );
        }
    }

    /**
     * Return the "delegate"
     */
    private javax.rmi.CORBA.StubDelegate _delegate()
    {
        if ( _delegate != null )
            return _delegate;

        if ( delegateClass != null )
        {
            try
            {
                java.lang.Object obj = delegateClass.newInstance();

                if ( obj instanceof javax.rmi.CORBA.StubDelegate )
                    _delegate = ( javax.rmi.CORBA.StubDelegate ) obj;
            }
            catch ( java.lang.Exception ex )
            {
                // I know this is evil, but we are going to remove the javax.rmi.CORBA
                // classes soon, as they are part of JDK 1.3.x and above...
            }
        }

        return _delegate;
    }

    //
    // -------------------------------------------------------------------------

    /**
     * This method shall return the same hashcode for all stubs that represent the same
     * remote object.
     */
    public int hashCode()
    {
        return _delegate().hashCode( this );
    }

    /**
     * The equals method shall return true when used to compare stubs that represent the
     * same remote object.
     */
    public boolean equals( java.lang.Object obj )
    {
        return _delegate().equals( this, obj );
    }

    /**
     * This method shall return the same string for all stubs that represent the same
     * remote object
     */
    public String toString()
    {
        return _delegate().toString( this );
    }

    /**
     * This method makes the stub ready for remote communication using the specified ORB object.
     * Connection normally happends implicitly when the stub is received or sent as an argument on
     * a remote method calln but it sometimes useful to do this by making an explicit call, e.g.,
     * following deserialization.
     */
    public void connect( org.omg.CORBA.ORB orb )
        throws java.rmi.RemoteException
    {
        _delegate().connect( this, orb );
    }

    /**
     * This method supports stub serialization by saving the IOR associated withn the stub.
     */
    private void writeObject( java.io.ObjectOutputStream s )
        throws java.io.IOException
    {
        _delegate().writeObject( this, s );
    }

    /**
     * This method supports stub deserialization by restoring the IOR associated with the stub.
     */
    private void readObject( java.io.ObjectInputStream s )
        throws java.io.IOException, ClassNotFoundException
    {
        _delegate().readObject( this, s );
    }
}
