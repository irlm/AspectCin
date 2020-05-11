/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

import org.apache.avalon.framework.logger.Logger;

/**
 * This class provides a default implementation for javax.rmi.CORBA.StubDelegate
 *
 * @author Jerome Daniel
 */
public class StubDelegateImpl
    implements javax.rmi.CORBA.StubDelegate
{
    /**
     * IOR.
     */
    private transient org.omg.IOP.IOR m_ior;

    /**
     * Empty constructor.
     */
    public StubDelegateImpl()
    {
    }

    /**
     * Construct delegate with IOR. This delegate can then be connected.
     *
     * @param ior The ior for which to create a stub.
     */
    public StubDelegateImpl( org.omg.IOP.IOR ior )
    {
        m_ior = ior;
    }

    /**
     * This method shall return the same hashcode for all stubs that represent the same
     * remote object.
     *
     * @param self A stub for which to generated a hash code.
     * @return The hasc code generated for the stub.
     */
    public int hashCode( javax.rmi.CORBA.Stub self )
    {
        return self._hash( 32 );
    }

    /**
     * The equals method shall return true when used to compare stubs that represent the
     * same remote object.
     *
     * @param self The stub to check.
     * @param obj The object to check against the stub.
     * @return True is the stub and the obj are equal, false otherwise.
     */
    public boolean equals( javax.rmi.CORBA.Stub self, java.lang.Object obj )
    {
        if ( obj == self )
        {
            return true;
        }
        if ( obj instanceof org.omg.CORBA.Object )
        {
            return ( ( org.omg.CORBA.portable.ObjectImpl ) obj )._is_equivalent(
                    ( org.omg.CORBA.Object ) self );
        }
        return false;
    }

    /**
     * This method shall return the same string for all stubs that represent the same
     * remote object.
     *
     * @param self The stub for which to get a description.
     * @return The stub description.
     */
    public String toString( javax.rmi.CORBA.Stub self )
    {
        try
        {
            org.omg.CORBA.portable.Delegate deleg = self._get_delegate();
            return deleg.toString( self );
        }
        catch ( org.omg.CORBA.BAD_OPERATION ex )
        {
            return getClass().getName() + ":no delegate set";
        }
    }

    /**
     * This method makes the stub ready for remote communication using the specified ORB object.
     * Connection normally happends implicitly when the stub is received or sent as an argument on
     * a remote method calln but it sometimes useful to do this by making an explicit call, e.g.,
     * following deserialization.
     *
     * @param self The stub to connect to the ORB.
     * @param orb The orb to connect the stub to.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    public void connect( javax.rmi.CORBA.Stub self, org.omg.CORBA.ORB orb )
        throws java.rmi.RemoteException
    {
        try
        {
            self._get_delegate();
            if ( !self._orb().equals( orb ) )
            {
                throw new java.rmi.RemoteException( "Already connected to other orb" );
            }
        }
        catch ( org.omg.CORBA.BAD_OPERATION ex )
        {
            if ( m_ior == null )
            {
                Logger logger = null;
                if ( self._orb() instanceof org.openorb.orb.core.ORBSingleton )
                {
                    logger = ( ( org.openorb.orb.core.ORBSingleton ) self._orb() ).getLogger();
                    if ( logger != null && logger.isErrorEnabled() )
                    {
                        logger.getChildLogger( "rmi" ).error(
                               "Connection failed. Not deserialized.", ex );
                    }
                }
                throw new java.rmi.RemoteException( "Connection failed. Not deserialized ("
                      + ex + ")" );
            }
            org.openorb.orb.core.Delegate deleg = new org.openorb.orb.core.Delegate( orb, m_ior );
            self._set_delegate( deleg );
        }
    }

    /**
     * This method supports stub serialization by saving the IOR associated with the stub.
     *
     * @param self The stub to write.
     * @param s The output stream to write the stub to.
     * @throws java.io.IOException When an IO error occurs.
     */
    public void writeObject( javax.rmi.CORBA.Stub self, java.io.ObjectOutputStream s )
        throws java.io.IOException
    {
        org.omg.IOP.IOR ior = m_ior;
        if ( ior == null )
        {
            org.omg.CORBA.portable.Delegate deleg;
            try
            {
                deleg = self._get_delegate();
            }
            catch ( org.omg.CORBA.BAD_OPERATION ex )
            {
                Logger logger = null;
                if ( self._orb() instanceof org.openorb.orb.core.ORBSingleton )
                {
                    logger = ( ( org.openorb.orb.core.ORBSingleton ) self._orb() ).getLogger();
                    if ( logger != null && logger.isErrorEnabled() )
                    {
                        logger.getChildLogger( "rmi" ).error( "Object not exported.", ex );
                    }
                }
                throw new IllegalStateException( "Object not exported (" + ex + ")" );
            }
            if ( !( deleg instanceof org.openorb.orb.core.Delegate ) )
            {
                throw new IllegalStateException( "Object not exported to openorb orb." );
            }
            m_ior = ( ( org.openorb.orb.core.Delegate ) deleg ).ior();
            ior = m_ior;
        }

        // 1. length of IOR type id
        s.writeInt( ior.type_id.length() );

        // 2. IOR type ID
        byte [] ior_buffer = ior.type_id.getBytes();
        s.write( ior_buffer, 0, ior_buffer.length );

        // 3. number of IOR profiles
        s.writeInt( ior.profiles.length );

        // For each profile
        for ( int i = 0; i < ior.profiles.length; i++ )
        {
            // profile tag
            s.writeInt( ior.profiles[ i ].tag );

            // length of profile data
            s.writeInt( ior.profiles[ i ].profile_data.length );

            // profile data
            s.write( ior.profiles[ i ].profile_data, 0, ior.profiles[ i ].profile_data.length );
        }
    }

    /**
     * This method supports stub deserialization by restoring the IOR associated with the stub.
     *
     * @param self ??? (not used)
     * @param s The input stream to read from.
     * @throws java.io.IOException When an IO error occurs.
     * @throws ClassNotFoundException When the type of the read object can't be found.
     */
    public void readObject( javax.rmi.CORBA.Stub self, java.io.ObjectInputStream s )
        throws java.io.IOException, ClassNotFoundException
    {
        m_ior = new org.omg.IOP.IOR();

        // 1. length of IOR type id
        int size = s.readInt();

        // 2. IOR type ID
        byte [] ior_buffer = new byte[ size ];
        s.read( ior_buffer, 0, size );
        m_ior.type_id = new String( ior_buffer );

        // 3. number of IOR profiles
        size = s.readInt();
        m_ior.profiles = new org.omg.IOP.TaggedProfile[ size ];

        // For each profiles
        for ( int i = 0; i < m_ior.profiles.length; i++ )
        {
            // profile tag
            m_ior.profiles[ i ] = new org.omg.IOP.TaggedProfile();
            m_ior.profiles[ i ].tag = s.readInt();

            // length of profile data
            size = s.readInt();
            m_ior.profiles[ i ].profile_data = new byte[ size ];

            // profile data
            s.read( m_ior.profiles[ i ].profile_data, 0, m_ior.profiles[ i ].profile_data.length );
        }
    }
}

