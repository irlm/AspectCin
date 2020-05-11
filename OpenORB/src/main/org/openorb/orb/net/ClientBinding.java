/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import java.util.Comparator;

import org.omg.CORBA.CompletionStatus;

import org.openorb.orb.adapter.ObjectAdapter;

/**
 * A client binding represents a potential request path between client
 * and server.
 *
 * @author Chris Wood
 * @version $Revision: 1.8 $ $Date: 2004/02/07 13:09:07 $
 */
public class ClientBinding
{
    public static final int DEAD_PRIORITY = 0x7FFFFFFF;

    public static final int FORCE_UNSET = -1;
    public static final int FORCE_FALSE = 0;
    public static final int FORCE_TRUE = 1;

    private Address m_address;
    private ClientChannel m_channel;
    private ObjectAdapter m_adapter = null;
    private org.omg.CORBA.SystemException m_exception;
    private int m_priority = 0;
    private byte [] m_oid = null;
    private int m_forced_marshal;

    private ServerManager m_server_manager;

    /**
     * Constructor.
     */
    protected ClientBinding( Address address )
    {
        m_address = address;
        m_forced_marshal = FORCE_UNSET;

        m_priority = ( int ) address.getPriority();
    }

    /**
     * Bind address to remote invocation channel.
     */
    public ClientBinding( Address address, ClientChannel channel )
    {
        m_address = address;
        m_channel = channel;

        m_forced_marshal = FORCE_TRUE;

        m_priority = ( int ) address.getPriority();
    }

    /**
     * Bind address to local adapter.
     */
    public ClientBinding( Address address, ClientChannel channel, ServerManager serverManager )
    {
        m_address = address;
        m_channel = channel;
        m_server_manager = serverManager;

        m_forced_marshal = FORCE_UNSET;

        m_priority = ( int ) address.getPriority();
    }

    /**
     * Bind address to a system exception. Placeholder for failed bindings
     */
    public ClientBinding( Address address, org.omg.CORBA.SystemException ex )
    {
        m_address = address;
        m_exception = ex;
        m_priority = DEAD_PRIORITY;
        m_forced_marshal = FORCE_FALSE;
    }

    /**
     * Return the client address.
     */
    public Address getAddress()
    {
        return m_address;
    }

    /**
     * Return the client channel. This is always available for both local and
     * nonlocal bindings.
     */
    public ClientChannel getClientChannel()
    {
        return m_channel;
    }

    /**
     * Return the object adapter. This will return null if the binding is non-local.
     */
    public ObjectAdapter getObjectAdapter()
    {
        if ( m_server_manager == null )
        {
            return null;
        }
        if ( m_oid == null )
        {
            m_oid = m_address.getTargetAddress( org.omg.GIOP.KeyAddr.value ).object_key();
            m_adapter = m_server_manager.find_adapter( m_oid );
        }

        if ( m_adapter == null )
        {
            return null;
        }
        m_forced_marshal = FORCE_UNSET;

        return m_adapter;
    }

    /**
     * Get binding priority. Low priorities are used first.
     * The lowest order byte of the priority is inter-component priority,
     * The second order byte is inter-profile priority,
     * The third order byte is inter-refrerence prioriy
     * The highest order byte is the channel state / adapter priority.
     */
    public int getPriority()
    {
        if ( m_exception != null )
        {
            return DEAD_PRIORITY;
        }
        if ( m_adapter != null )
        {
            return m_priority | ( m_adapter.cache_priority() & 0xFF000000 );
        }
        else
        {
            return m_priority | m_channel.state();
        }
    }

    /**
     * Set the binding priority. Lower values are used first.
     * This should be set with respect to the current priority, changing only
     * the appropriate bits.
     */
    public void setPriority( int priority )
    {
        if ( priority > 0 )
        {
            m_priority = priority & ( MASK_PROFILE_PRIORITY | MASK_IOR_PRIORITY )
                  | m_priority & ~( MASK_PROFILE_PRIORITY | MASK_IOR_PRIORITY );
        }
        else
        {
            m_priority = priority;
        }
    }

    /**
     * Significant bits in selecting between alternate addresses from the
     * same profile. These bits are not settable by the setPriority operation.
     */
    public static final int MASK_ADDRESS_PRIORITY = 0x0FFF;

    /**
     * Significant bits in selecting between alternate addresses from different
     * profiles.
     */
    public static final int MASK_PROFILE_PRIORITY = 0xF000;

    /**
     * Significant bits in selecting between alternate IORs
     */
    public static final int MASK_IOR_PRIORITY = 0xFF0000;

    public int hashCode()
    {
        return m_address.hashCode();
    }

    public boolean equals( Object obj )
    {
        if ( !( obj instanceof ClientBinding ) )
        {
            return false;
        }
        return m_address.equals( ( ( ClientBinding ) obj ).m_address );
    }

    /**
     * True if the target can accept local invocations. The object reference
     * operations such as is_a are available for local invocation whenever
     * getObjectAdapter returns non null.
     */
    public boolean local_invoke()
    {
        if ( m_forced_marshal == FORCE_UNSET )
        {
            while ( true )
            {
                getObjectAdapter();

                if ( m_adapter == null )
                {
                    m_forced_marshal = FORCE_TRUE;
                    break;
                }

                try
                {
                    m_forced_marshal = m_adapter.forced_marshal( m_oid ) ? FORCE_TRUE : FORCE_FALSE;
                    break;
                }
                catch ( org.openorb.orb.adapter.AdapterDestroyedException ex )
                {
                    m_adapter = null;
                }
            }
        }

        return m_forced_marshal == FORCE_FALSE;
    }

    // request creation.

    /**
     * Create a request. If this is the first request on this channel
     * then client_connect will be called on all ChannelInterceptor
     * before returning the request. This may return null if the channel
     * is unable to create a request. This just calls create_request
     * on the channel with the binding's address and the specified target.
     *
     * @param target The target of the request.
     * @param operation The operation to create the request for.
     * @param response_expected A flag that indicates whether a response
     *        is expected or not (oneway).
     */
    public ClientRequest create_request( org.omg.CORBA.Object target,
                                         String operation,
                                         boolean response_expected )
    {
        if ( m_exception != null )
        {
            throw m_exception;
        }
        while ( true )
        {
            if ( m_channel == null )
            {
                return null;
            }
            try
            {
                return m_channel.create_request( target, m_address, operation, response_expected );
            }
            catch ( RebindChannelException ex )
            {
                m_channel = ex.getClientChannel();
            }
        }
    }

    /**
     * This is a factory method for creation a new Local Client Request.
     * This is necessary for having a clean handling of the request_id.
     *
     * @param orb ORB
     * @param target target object for the request
     * @param operation operation
     * @param response_expected oneway call or not
     * @param adresses all the adresses of the target
     * @return A new instance of this class.
     */
    public LocalClientRequest create_request_local(
          org.omg.CORBA.ORB orb, org.omg.CORBA.Object target, String operation,
          boolean response_expected, Address[] adresses )
    {
        if ( m_exception != null )
        {
            throw m_exception;
        }
        int request_id = RequestIDAllocator.get_request_id () << 1;
        LocalClientRequest lcr =
              new LocalClientRequest( orb, request_id, target, operation,
              response_expected, adresses );
        return lcr;
    }

    /**
     * Create a locate request. This may return null if the channel
     * is unable to create a request. This just calls create_locate_request
     * on the channel with the binding's address and the specified target.
     *
     * @param target The target of the request.
     */
    public ClientRequest create_locate_request( org.omg.CORBA.Object target )
    {
        if ( m_exception != null )
        {
            throw m_exception;
        }
        while ( true )
        {
            if ( m_channel == null )
            {
                return null;
            }
            try
            {
                return m_channel.create_locate_request( target, m_address );
            }
            catch ( RebindChannelException ex )
            {
                m_channel = ex.getClientChannel();
            }
        }
    }

    // local dispatch

    /**
     * Calls servant_preinvoke on the adapter. This may return null if a retry is
     * indicated.
     */
    public org.omg.CORBA.portable.ServantObject servant_preinvoke(
            String operation, Class expectedType )
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        if ( m_exception != null )
        {
            throw m_exception;
        }
        if ( m_server_manager == null )
        {
            throw new org.omg.CORBA.INTERNAL( "ServerManager unavailable" );
        }
        ObjectAdapter adapter;

        org.omg.CORBA.portable.ServantObject servObj;

        while ( true )
        {
            adapter = getObjectAdapter();

            if ( adapter == null )
            {
                return null;
            }
            try
            {
                servObj = adapter.servant_preinvoke( m_oid, operation, expectedType );
                break;
            }
            catch ( org.openorb.orb.adapter.AdapterDestroyedException ex )
            {
                adapter = null;
            }
        }

        if ( servObj == null )
        {
            m_forced_marshal = FORCE_UNSET;
            return null;
        }

        LocalServant ret = new LocalServant();
        ret.m_adapter = adapter;
        ret.m_originalServant = servObj;
        ret.servant = servObj.servant;
        return ret;
    }

    private static class LocalServant
        extends org.omg.CORBA.portable.ServantObject
    {
        private ObjectAdapter m_adapter;
        private org.omg.CORBA.portable.ServantObject m_originalServant;
    }

    /**
     * Calls servant_postinvoke on the adapter.
     */
    public void servant_postinvoke( org.omg.CORBA.portable.ServantObject srvObject )
    {
        if ( m_exception != null )
        {
            throw m_exception;
        }
        if ( !( srvObject instanceof LocalServant ) )
        {
            throw new org.omg.CORBA.INTERNAL( "Servant is not a LocalObject" );
        }
        LocalServant sobj = ( LocalServant ) srvObject;

        sobj.m_adapter.servant_postinvoke( m_oid, sobj.m_originalServant );
    }

    /**
     * Calls locate on the adapter.
     */
    public boolean locate()
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        if ( m_exception != null )
        {
            throw m_exception;
        }
        if ( m_server_manager == null )
        {
            throw new org.omg.CORBA.INTERNAL( "ServerManager unavailable" );
        }
        ObjectAdapter adapter;

        while ( true )
        {
            adapter = getObjectAdapter();

            if ( adapter == null )
            {
                return false;
            }
            try
            {
                return adapter.locate( m_oid );
            }
            catch ( org.openorb.orb.adapter.AdapterDestroyedException ex )
            {
                adapter = null;
            }
        }
    }

    /**
     * Calls is_a on the adapter.
     */
    public boolean is_a( String repository_id )
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        if ( m_exception != null )
        {
            throw m_exception;
        }
        if ( m_server_manager == null )
        {
            throw new org.omg.CORBA.INTERNAL( "ServerManager unavailable" );
        }
        ObjectAdapter adapter;

        while ( true )
        {
            adapter = getObjectAdapter();

            if ( adapter == null )
            {
                throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0, CompletionStatus.COMPLETED_NO );
            }
            try
            {
                return adapter.is_a( m_oid, repository_id );
            }
            catch ( org.openorb.orb.adapter.AdapterDestroyedException ex )
            {
                adapter = null;
            }
        }
    }

    /**
     * Calls get_interface_def on the adapter.
     */
    public org.omg.CORBA.Object get_interface_def()
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        if ( m_exception != null )
        {
            throw m_exception;
        }
        if ( m_server_manager == null )
        {
            throw new org.omg.CORBA.INTERNAL( "ServerManager unavailable" );
        }
        ObjectAdapter adapter;

        while ( true )
        {
            adapter = getObjectAdapter();

            if ( adapter == null )
            {
                throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0, CompletionStatus.COMPLETED_NO );
            }
            try
            {
                return adapter.get_interface_def( m_oid );
            }
            catch ( org.openorb.orb.adapter.AdapterDestroyedException ex )
            {
                adapter = null;
            }
        }
    }

    /**
     * Calls get_domain_managers on the adapter.
     */
    public org.omg.CORBA.DomainManager [] get_domain_managers()
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        if ( m_exception != null )
        {
            throw m_exception;
        }
        if ( m_server_manager == null )
        {
            throw new org.omg.CORBA.INTERNAL( "ServerManager unavailable" );
        }
        ObjectAdapter adapter;

        while ( true )
        {
            adapter = getObjectAdapter();

            if ( adapter == null )
            {
                throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0, CompletionStatus.COMPLETED_NO );
            }
            try
            {
                return adapter.get_domain_managers( m_oid );
            }
            catch ( org.openorb.orb.adapter.AdapterDestroyedException ex )
            {
                adapter = null;
            }
        }
    }

    /**
     * Calls get_component on the adapter.
     */
    public org.omg.CORBA.Object get_component()
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        if ( m_exception != null )
        {
            throw m_exception;
        }
        if ( m_server_manager == null )
        {
            throw new org.omg.CORBA.INTERNAL( "ServerManager unavailable" );
        }
        ObjectAdapter adapter;

        while ( true )
        {
            adapter = getObjectAdapter();

            if ( adapter == null )
            {
                throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0, CompletionStatus.COMPLETED_NO );
            }
            try
            {
                return adapter.get_component( m_oid );
            }
            catch ( org.openorb.orb.adapter.AdapterDestroyedException ex )
            {
                adapter = null;
            }
        }
    }

    private static class PriorityComparator
                implements Comparator
    {
        public int compare( final Object obj1, final Object obj2 )
        {
            if ( obj1 == null )
            {
                if ( obj2 == null )
                {
                    return 0;
                }
                return 1;
            }
            if ( obj2 == null )
            {
                return -1;
            }
            int pri1 = ( ( ClientBinding ) obj1 ).getPriority();
            int pri2 = ( ( ClientBinding ) obj2 ).getPriority();
            if ( pri1 < 0 )
            {
                if ( pri2 < 0 )
                {
                    return 0;
                }
                return -1;
            }
            if ( pri2 < 0 )
            {
                return 1;
            }
            return ( pri1 > pri2 ) ? 1 : ( ( pri1 < pri2 ) ? -1 : 0 );
        }
    }

    public static final Comparator PRIORITY_COMP = new PriorityComparator();
}

