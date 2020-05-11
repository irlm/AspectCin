/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

import java.lang.reflect.Constructor;

import java.util.Arrays;

import org.apache.avalon.framework.CascadingRuntimeException;

import org.omg.GIOP.TargetAddress;
import org.omg.GIOP.IORAddressingInfo;

import org.openorb.util.HexPrintStream;

import org.openorb.util.ExceptionTool;

/**
 * Base class which implements much of the Address functionality.
 *
 * @author Chris Wood
 * @version $Revision: 1.7 $ $Date: 2004/02/10 21:02:50 $
 */
public abstract class AbstractAddress
    implements Address, Cloneable
{
    private TargetAddress m_key_addr;
    private TargetAddress m_profile_addr;
    private TargetAddress m_reference_addr;
    private org.omg.CORBA.PolicyManagerOperations m_policies;
    private org.omg.IOP.TaggedComponent[] m_components;
    private Object[] m_component_data;
    private int m_from_this_profile;

    private Constructor m_ctor = null;
    private Object [] m_args = null;

    private short m_priority = 0;

    protected void set_ior( org.omg.IOP.IOR ior, int selected_profile )
    {
        if ( ior != null )
        {
            m_reference_addr = new TargetAddress();
            m_reference_addr.ior( new IORAddressingInfo( selected_profile, ior ) );
            m_profile_addr = new TargetAddress();
            m_profile_addr.profile( ior.profiles[ selected_profile ] );
        }
    }

    protected void set_oid( byte [] oid )
    {
        if ( oid != null )
        {
            m_key_addr = new TargetAddress();
            m_key_addr.object_key( oid );
        }
    }

    protected void set_policies( org.omg.CORBA.PolicyManagerOperations policies )
    {
        m_policies = policies;
    }

    protected void set_components( org.omg.IOP.TaggedComponent[] components, int fromThisProfile )
    {
        m_components = components;
        m_from_this_profile = fromThisProfile;
        m_component_data = new Object[ components.length ];
    }

    /**
     * Get target address with specified addressing disposition.
     */
    public TargetAddress getTargetAddress( short adressingDisposition )
    {
        switch ( adressingDisposition )
        {

        case org.omg.GIOP.ReferenceAddr.value:

            if ( m_reference_addr != null )
            {
                return m_reference_addr;
            }
            break;

        case org.omg.GIOP.ProfileAddr.value:
            if ( m_profile_addr != null )
            {
                return m_profile_addr;
            }
            break;

        case org.omg.GIOP.KeyAddr.value:
            if ( m_key_addr != null )
            {
                return m_key_addr;
            }
            break;
        }

        throw new org.omg.CORBA.NO_IMPLEMENT( 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
    }

    /**
     * Returns a PolicyList containing the requested PolicyTypes set for the
     * address. If the specified sequence is empty, all
     * Policys set for the address will be returned. If none of the
     * requested PolicyTypes are overridden at the target, an empty sequence
     * is returned.
     */
    public org.omg.CORBA.Policy[] get_target_policies( int[] ts )
    {
        if ( m_policies == null )
        {
            return new org.omg.CORBA.Policy[ 0 ];
        }
        return m_policies.get_policy_overrides( ts );
    }

    /**
     * Returns all components associated with this address.
     */
    public org.omg.IOP.TaggedComponent[] get_components()
    {
        if ( m_components == null )
        {
            return new org.omg.IOP.TaggedComponent[ 0 ];
        }
        return m_components;
    }

    /**
     * Returns the number of components associated only with the profile
     * which created this address. Components past this index in the array
     * returned from get_components are sourced from a multi-component profile.
     */
    public int get_profile_components()
    {
        return m_from_this_profile;
    }

    /**
     * Returns any components with the specified tag.
     */
    public org.omg.IOP.TaggedComponent[] get_components( int tag )
    {
        if ( m_components == null )
        {
            return new org.omg.IOP.TaggedComponent[ 0 ];
        }
        int cnt = 0;

        for ( int i = 0; i < m_components.length; ++i )
        {
            if ( m_components[ i ].tag == tag )
            {
                cnt++;
            }
        }
        org.omg.IOP.TaggedComponent[] ret = new org.omg.IOP.TaggedComponent[ cnt ];

        for ( int i = m_components.length - 1; i >= 0 && cnt > 0; --i )
        {
            if ( m_components[ i ].tag == tag )
            {
                ret[ --cnt ] = m_components[ i ];
            }
        }
        return ret;
    }

    /**
     * Get the component at the specified index in the get_components array.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public org.omg.IOP.TaggedComponent get_component( int idx )
    {
        if ( m_components == null || idx < 0 || idx >= m_components.length )
        {
            throw new IndexOutOfBoundsException( "No component at that index" );
        }
        return m_components[ idx ];
    }

    /**
     * Returns the data associated with a particular component, or null if
     * the component at that index has no associated data.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public Object get_component_data( int idx )
    {
        if ( m_components == null || idx < 0 || idx >= m_components.length )
        {
            throw new IndexOutOfBoundsException( "No component at that index" );
        }
        return m_component_data[ idx ];
    }

    /**
     * Set the data associated with a particular component, or null if
     * the component at that index has no associated data.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public void set_component_data( int idx, Object obj )
    {
        if ( m_components == null || idx < 0 || idx >= m_components.length )
        {
            throw new IndexOutOfBoundsException( "No component at that index" );
        }
        m_component_data[ idx ] = obj;
    }

    /**
     * Set the transport constructor for the address.
     */
    public void setTransportConstructor( Constructor ctor, Object[] args )
    {
        m_ctor = ctor;
        m_args = args;
    }

    /**
     * Use the transport constructor to construct a new transport.
     */
    public Transport createTransport()
    {
        try
        {
            return ( Transport ) m_ctor.newInstance( m_args );
        }
        catch ( final Exception ex )
        {
            throw ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                  "AbstractAddress::createTransport: "
                  + "Exception during creation of new Transport instance ("
                  + ex + ")" ), ex );
        }
    }

    /**
     * Get the addresses priority. This will be the low order word of the
     * binding priority.
     */
    public short getPriority()
    {
        return m_priority;
    }

    /**
     * Get the addresses priority. This will be the low order word of the
     * binding priority.
     * @param mask mask of bits to get
     */
    public short getPriority( short mask )
    {
        return ( short ) ( m_priority & mask );
    }

    /**
     * Set the address priority. This will be the low order word of the
     * binding priority.
     */
    public void setPriority( short priority )
    {
        m_priority = ( short ) ( priority & MASK_ADDRESS_PRIORITY );
    }

    /**
     * Set the address priority. This will be the low order word of the
     * binding priority.
     */
    public short setPriority( short priority, short mask )
    {
        return m_priority = ( short ) ( ( m_priority & ~mask )
              | ( priority & MASK_ADDRESS_PRIORITY & mask ) );
    }

    /**
     * A string which contains an RFC2396 encoding of the object key, as could
     * be included in a corbaloc style address.
     */
    public String getObjectKeyString()
    {
        byte [] obj_key = m_key_addr.object_key();

        // TODO: this is broken at the moment.
        try
        {
            String str = new String( obj_key, "UTF-8" );
            return org.openorb.util.NamingUtils.encodeRFC2396( str );
        }
        catch ( java.io.UnsupportedEncodingException ex )
        {
            throw new CascadingRuntimeException( "Encoding is not supported ", ex );
        }
    }

    /**
     * Human readable string describing the object key.
     */
    public String getObjectKeyDescription()
    {
        String oks = getObjectKeyString();

        if ( oks.indexOf( "%" ) < 0 )
        {
            return "Corbaloc Object Key: " + oks + "\n";
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        HexPrintStream hps = new HexPrintStream( os, HexPrintStream.FORMAT_MIXED );

        PrintStream ps = new PrintStream( os );

        byte [] obj_key = m_key_addr.object_key();

        if ( obj_key[ 0 ] != 0 || obj_key[ 1 ] != ( byte ) 'O' || obj_key[ 2 ] != ( byte ) 'O' )
        {
            ps.println( "Non OpenORB Object Key:" );
        }
        else if ( ( obj_key[ 3 ] & 0x01 ) == 0 )
        {
            ps.println( "OpenORB Persistent Object Key:" );
        }
        else
        {
            ps.println( "OpenORB Nonpersistent Object Key:" );
        }

        ps.flush();

        try
        {
            hps.write( obj_key );
            hps.flush();
        }
        catch ( java.io.IOException ex )
        {
            throw new CascadingRuntimeException( "IOException during write or flush", ex );
        }

        return os.toString();
    }

    public int hashCode()
    {
        byte [] oid = m_key_addr.object_key();
        int hash = 0;

        for ( int i = 0; i < oid.length; ++i )
        {
            hash = 311 * hash + oid[ i ];
        }
        return hash;
    }

    public boolean equals( Object obj )
    {
        if ( !( obj instanceof Address ) || hashCode() != obj.hashCode() )
        {
            return false;
        }
        // a non-AbstractAddress will never be equal.
        if ( !( obj instanceof AbstractAddress ) )
        {
            return false;
        }
        AbstractAddress aa2 = ( AbstractAddress ) obj;

        // compare object keys
        if ( m_key_addr != aa2.m_key_addr
              && !Arrays.equals( m_key_addr.object_key(), aa2.m_key_addr.object_key() ) )
        {
            return false;
        }
        aa2.m_key_addr = m_key_addr;

        // compare transport constructor arguments. Only compare on length,
        if ( !m_ctor.equals( aa2.m_ctor ) || m_args.length != aa2.m_args.length )
        {
            return false;
        }
        // compare arguments of string or number types. Other arguments may not
        // define equals
        for ( int i = 0; i < m_args.length; ++i )
        {
            if ( ( ( m_args[ i ] instanceof String ) || ( m_args[ i ] instanceof Number ) )
                  && !m_args[ i ].equals( aa2.m_args[ i ] ) )
            {
                return false;
            }
        }
        // compare components.
        if ( m_components.length != aa2.m_components.length )
        {
            return false;
        }
        if ( m_components != aa2.m_components )
        {
            boolean [] foundJ = new boolean[ m_components.length ];

            for ( int i = 0; i < m_components.length; ++i )
            {
                boolean foundI = false;

                for ( int j = 0; j < m_components.length; ++j )
                {
                    if ( m_components[ i ] == m_components[ j ]
                          || ( m_components[ i ].tag == aa2.m_components[ j ].tag
                          && Arrays.equals( m_components[ i ].component_data,
                          aa2.m_components[ j ].component_data ) ) )
                    {
                        // might as well discard the extra component, it's just using up memory
                        aa2.m_components[ j ] = m_components[ i ];
                        foundJ[ j ] = true;
                        foundI = true;
                        break;
                    }
                }
                if ( !foundI )
                {
                    return false;
                }
            }

            for ( int i = 0; i < foundJ.length; ++i )
            {
                if ( !foundJ[ i ] )
                {
                    return false;
                }
            }
        }
        return true;
    }
}

