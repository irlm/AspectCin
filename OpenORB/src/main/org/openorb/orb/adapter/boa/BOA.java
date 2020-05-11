/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.boa;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CORBA.portable.ServantObject;

import org.omg.PortableInterceptor.ForwardRequest;

import org.openorb.orb.adapter.TargetInfo;
import org.openorb.orb.adapter.AdapterDestroyedException;
import org.openorb.orb.adapter.ObjectAdapter;
import org.openorb.orb.adapter.IORUtil;

import org.openorb.orb.core.MinorCodes;

import org.openorb.orb.net.ServerManager;

import org.openorb.util.RepoIDHelper;
import org.openorb.util.NumberCache;

/**
 * This class provides the implementation of the Basic Object Adapter (BOA).
 * The BOA has been deprected as of CORBA 2.2, when the Portable Object
 * Adapter (POA) was introduced.
 *
 * @author Chris Wood
 * @version $Revision: 1.11 $ $Date: 2004/05/13 04:09:26 $
 */
public class BOA
    extends org.omg.CORBA.BOA
    implements ObjectAdapter
{
    private static final byte [] PREFIX = ( "BOA" ).getBytes();

    private static short s_next_adapter = 0;

    private static final org.omg.CORBA.Policy [] NO_POLICIES = new org.omg.CORBA.Policy[ 0 ];

    private ServerManager m_server_manager;
    private org.omg.CORBA.ORB m_orb;

    private byte [][] m_aid_parts;
    private byte [] m_aid;

    /** This member stores the objects connected with this BOA instance. */
    private int m_next_target;

    private org.openorb.orb.corbaloc.CorbalocService m_corbaloc_service;

    private org.openorb.orb.pi.ComponentSet m_comp_set;

    private Map m_targets = new HashMap();

    private Logger m_logger;

    /**
     * Constructor. Creates new BOA.
     *
     * @param server_manager The server manager the BOA will be connected to.
     */
    public BOA( ServerManager server_manager )
    {
        m_server_manager = server_manager;
        m_orb = server_manager.orb();
        m_aid_parts = new byte[ 2 ][];
        m_aid_parts[ 0 ] = PREFIX;
        synchronized ( BOA.class )
        {
            m_aid_parts[ 1 ] = new byte[ 2 ];
            m_aid_parts[ 1 ][ 0 ] = ( byte ) ( s_next_adapter >>> 8 );
            m_aid_parts[ 1 ][ 0 ] = ( byte ) ( s_next_adapter );
            ++s_next_adapter;
        }
        byte [][] tmp_parts = new byte[ m_aid_parts.length + 1 ][];
        System.arraycopy( m_aid_parts, 0, tmp_parts, 0, m_aid_parts.length );
        tmp_parts[ m_aid_parts.length ] = new byte[ 0 ];
        m_aid = server_manager.create_cacheable_object_key( true, tmp_parts );
        server_manager.register_adapter( m_aid, this );
    }

    /**
     * Connect an object to the BOA but don't activate it.
     *
     * @param obj The object to connect.
     */
    public void connect( ObjectImpl obj )
    {
        connect( obj, false );
    }

    /**
     * Connect an object to the BOA and optionaly activate it.
     *
     * @param obj The object to connect to this BOA instance.
     * @param activate Whether the object should be activated or not.
     */
    public void connect( ObjectImpl obj, boolean activate )
    {
        // object already activated.
        try
        {
            if ( obj._get_delegate() != null )
            {
                return;
            }
        }
        catch ( org.omg.CORBA.BAD_OPERATION ex )
        {
            // This exception is thrown when the delegate has not been set
            // -> ignore and create a delegate below
        }

        BOAEntry entry;

        synchronized ( m_aid )
        {
            entry = new BOAEntry( m_aid, m_next_target++ );
        }

        entry.setState( activate );
        entry.setTarget( obj );

        // construct the object id.
        byte [][] oid_parts = new byte[ 3 ][];
        oid_parts[ 0 ] = m_aid_parts[ 0 ];
        oid_parts[ 1 ] = m_aid_parts[ 1 ];
        oid_parts[ 2 ] = new byte[ 4 ];
        oid_parts[ 2 ] = entry.getObjectID();

        byte [] object_key = m_server_manager.create_cacheable_object_key( false, oid_parts );

        if ( m_comp_set == null )
        {
            m_comp_set = new org.openorb.orb.pi.ComponentSet( m_orb, null, null );
            m_comp_set.interception_point();
        }

        // create an IOR.
        org.omg.IOP.IOR ior = IORUtil.construct_ior( obj._ids() [ 0 ],
               object_key, m_comp_set, m_server_manager.get_protocol_ids(), m_orb );

        // create a delegate
        org.openorb.orb.core.Delegate deleg = new org.openorb.orb.core.Delegate( m_orb, ior );

        // set the delegate
        obj._set_delegate( deleg );

        synchronized ( m_targets )
        {
            m_targets.put( NumberCache.getInteger( entry.getId() ), entry );
        }
    }

    /**
     * Connect and object and provide its key.
     *
     * @param obj The object to connect to this BOA instance.
     * @param name The name to register the object at the corbaloc service.
     */
    public void connect( ObjectImpl obj, String name )
    {
        if ( m_corbaloc_service == null )
        {
            try
            {
                m_corbaloc_service = org.openorb.orb.corbaloc.CorbalocServiceHelper.narrow(
                      m_orb.resolve_initial_references( "CorbalocService" ) );
            }
            catch ( org.omg.CORBA.ORBPackage.InvalidName ex )
            {
                if ( getLogger().isErrorEnabled() )
                {
                    getLogger().error( "Could not resolve CorbalocService.", ex );
                }
                throw new org.omg.CORBA.INITIALIZE(
                      "Could not resolve CorbalocService (" + ex + ")" );
            }
        }
        connect( obj );
        m_corbaloc_service.put( name, obj );
    }

    /**
     * This operation is used to forward an object.
     *
     * @param objOld The old object.
     * @param objNew The new object requests should be forwarded to.
     */
    public void forward( ObjectImpl objOld, ObjectImpl objNew )
    {
        synchronized ( m_targets )
        {
            BOAEntry key = find_entry( objOld );

            if ( key != null )
            {
                objOld._set_delegate( objNew._get_delegate() );
                key.setTarget( objNew );
            }
        }
    }

    /**
     * Disconnect an object.
     *
     * @param obj The object to disconnect from this BOA.
     */
    public void disconnect ( ObjectImpl obj )
    {
        synchronized ( m_targets )
        {
            BOAEntry entry = find_entry( obj );
            if ( entry != null )
            {
                m_targets.remove( NumberCache.getInteger( entry.getId() ) );
            }
        }
    }

    /**
     * Activate an object.
     *
     * @param obj The object to activate.
     */
    public void obj_is_ready( ObjectImpl obj )
    {
        synchronized ( m_targets )
        {
            BOAEntry entry = find_entry( obj );
            if ( entry != null )
            {
                entry.activate();
            }
        }
    }

    /**
     * Deactivate an object.
     *
     * @param obj The object to deactivate.
     */
    public void deactivate_obj( ObjectImpl obj )
    {
        synchronized ( m_targets )
        {
            BOAEntry entry = find_entry( obj );
            if ( entry != null )
            {
                entry.deactivate();
            }
        }
    }

    /**
     * Run the BOA. This operation will not return.
     */
    public void impl_is_ready()
    {
        m_server_manager.register_adapter( m_aid, this );
        m_orb.run();
    }

    /**
     * Stop the BOA.
     */
    public void deactivate_impl()
    {
        m_server_manager.unregister_adapter( m_aid );
    }

    /**
     * Expected lifetime of the adapter. Higher numbers are more likley to be
     * dropped from the lookup cache. If this returns 0 then the adapter should
     * never be dropped. This value should be stable throughout the lifetime of
     * the adapter. The highest byte will be used for determining the binding
     * priority.
     *
     * Suggested values:
     * = 0          Root adapters. Always keep.
     * &lt; 0x1000000  Adpaters created directly.
     * &lt; 0x2000000  Adapters created dynamicaly.
     * &lt; 0x3000000  Objects created directly.
     * &lt; 0x4000000  Objects created dynamicaly.
     * &lt; 0          Never cache adapter, single invocation only.
     *              These should not be returned from find_adapter.
     *
     * @return Always returns 0 (???).
     */
    public int cache_priority()
    {
        return 0;
    }

    /**
     * Adapter is single threaded. Calls to all single threaded Adapters are
     * serialized.
     *
     * @return Always returns false (???).
     */
    public boolean single_threaded()
    {
        return false;
    }

    /**
     * Etherealize the adapter. When this function returns the adapter's memory
     * resident state should have been minimized. This function will always be
     * called before purging the adapter from the cache. If cleanup_in_progress
     * is true the adapter is being perminently deactivated and will no longer
     * have to dispatch operations.
     *
     * @param cleanup_in_progress ???
     */
    public void etherealize( boolean cleanup_in_progress )
    {
    }

    /**
     * Queue manager for the adapter. This may return null for an adapter which
     * is always active. To create an adapter manager for an adapter use the
     * create_manager operation on the ServerManager.
     */
    public org.openorb.orb.net.AdapterManager getAdapterManager()
    {
        return null;
    }

    /**
     * If this adapter serves this object directly it should return itself,
     * otherwise it should return an adapter that does. If an adapter
     * for an object with the given object id cannot be found this returns null.
     * The returned adapter may be cached for future use.
     */
    public ObjectAdapter find_adapter( byte[] object_key )
        throws AdapterDestroyedException
    {
        synchronized ( m_targets )
        {
            if ( find_entry( object_key ) == null )
            {
                return null;
            }
        }
        return this;
    }

    /**
     * Return the adapter id. This should be a prefix of the object_key,
     * should be stable and will be treated as read-only. If an object with
     * the given object_key is not served by this adapter this returns null.
     */
    public byte[] adapter_id( byte[] object_key )
    {
        synchronized ( m_targets )
        {
            if ( find_entry( object_key ) == null )
            {
                return null;
            }
        }
        return m_aid;
    }

    /**
     * Return the object id. This should should be a suffix of the object_key if
     * the object id is cacheable, it should be stable with respect to a given
     * object_key and will be treated as read-only. If an object with
     * the given object_key is not served by this adapter this returns null.
     */
    public byte[] object_id( byte[] object_key )
    {
        synchronized ( m_targets )
        {
            BOAEntry entry = find_entry( object_key );
            if ( entry == null )
            {
                return null;
            }
            return entry.getObjectID();
        }
    }

    /**
     * Returns a PolicyList containing the Polices set for the
     * requested PolicyTypes. If the specified sequence is empty, all
     * Policy overrides will be returned. If none of the
     * requested PolicyTypes are overridden an empty sequence is returned.
     */
    public org.omg.CORBA.Policy[] get_server_policies( int[] ts )
    {
        return NO_POLICIES;
    }

    /**
     * If this returns true then requests for the specified object id
     * must be sent through the network. This will be true for example
     * when using the DSI. This should return true if the object_key is
     * unknown to the adapter. The result should be stable with respect to a
     * given object ID.
     */
    public boolean forced_marshal( byte[] object_key )
        throws AdapterDestroyedException
    {
        return false;
    }


    /**
     * Preinvoke a local operation. Always paired with a call to
     * servant_postinvoke.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * this returns null.
     */
    public ServantObject servant_preinvoke( byte[] object_key, String operation,
            Class expectedType )
        throws ForwardRequest, AdapterDestroyedException
    {
        ServantObject ret = new ServantObject();
        ret.servant = find_target( object_key );
        return ret;
    }

    /**
     * Close off a local operation. Always paired with a call to
     * servant_preinvoke
     */
    public void servant_postinvoke( byte[] object_key, ServantObject srvObject )
    {
        // does nothing.
    }

    /**
     * Respond to a local locate request. This returns true if the object
     * is located locally, false if the object is unknown and throws a forward
     * request for a location forward. This should not throw a system exception.
     */
    public boolean locate( byte[] object_key )
        throws ForwardRequest, AdapterDestroyedException
    {
        synchronized ( m_targets )
        {
            BOAEntry entry = find_entry( object_key );
            return ( entry != null && entry.getState() );
        }
    }

    /**
     * is_a operation.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    public boolean is_a( byte[] object_key, String repository_id )
        throws ForwardRequest, AdapterDestroyedException
    {
        BOAEntry entry;

        synchronized ( m_targets )
        {
            entry = find_entry( object_key );
            // this will throw the appropriate exception.
            if ( entry == null )
            {
                find_target( object_key );
            }
        }
        return entry.targetIsA( repository_id );
    }

    /**
     * get_interface_def operation.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    public org.omg.CORBA.Object get_interface_def( byte[] object_key )
        throws org.omg.PortableInterceptor.ForwardRequest, AdapterDestroyedException
    {
        String repo_id;
        synchronized ( m_targets )
        {
            repo_id = find_target( object_key )._ids() [ 0 ];
        }
        return get_interface_def( repo_id );
    }

    private org.omg.CORBA.Object get_interface_def( String repository_id )
    {
        org.omg.CORBA.Object obj;
        try
        {
            obj = m_orb.resolve_initial_references( "InterfaceRepository" );
        }
        catch ( org.omg.CORBA.ORBPackage.InvalidName ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Could not resolve InterfaceRepository.", ex );
            }
            throw new org.omg.CORBA.INTF_REPOS( MinorCodes.INF_REPOS_FIND,
                    org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        if ( obj == null )
        {
            throw new org.omg.CORBA.INTF_REPOS( MinorCodes.INF_REPOS_FIND,
                    org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        org.omg.CORBA.Repository rep;
        try
        {
            rep = org.omg.CORBA.RepositoryHelper.narrow( obj );
        }
        catch ( org.omg.CORBA.BAD_PARAM ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Could not narrow obj to type Repository.", ex );
            }
            throw new org.omg.CORBA.INTF_REPOS( MinorCodes.INF_REPOS_FIND,
                    org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        org.omg.CORBA.Object interface_def = rep.lookup_id( repository_id );
        if ( interface_def == null )
        {
            throw new org.omg.CORBA.INTF_REPOS( MinorCodes.INF_REPOS_LOOKUP,
                    org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        return interface_def;
    }

    /**
     * get_domain_manager operation.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    public org.omg.CORBA.DomainManager[] get_domain_managers( byte[] object_key )
        throws ForwardRequest, AdapterDestroyedException
    {
        BOAEntry entry;
        synchronized ( m_targets )
        {
            entry = find_entry( object_key );
            // this will throw the appropriate exception.
            if ( entry == null )
            {
                find_target( object_key );
            }
        }
        return lookup_domain_managers( entry );
    }

    private org.omg.CORBA.DomainManager[] lookup_domain_managers( BOAEntry entry )
    {
        // Normally the domain managers must be found here!
        // But because the BOA is deprecated and rarely used this
        // will probably never be implemented...
        return new org.omg.CORBA.DomainManager[ 0 ];
    }

    /**
     * get_componenent operation.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    public org.omg.CORBA.Object get_component( byte[] object_key )
        throws ForwardRequest, AdapterDestroyedException
    {
        return target_get_component( find_target( object_key ) );
    }

    private org.omg.CORBA.Object target_get_component( ObjectImpl target )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Locate the servant object for a request. The returned object is
     * handed to the dispatch operation. This may throw a system exception or
     * respond with a forward request if one is indicated, in which case the
     * dispatch operation will not be called.
     *
     * @param req out parameter holding repository ids of all available interfaces
     *            with the most derived interface appearing first.
     * @return the 'target' of the operation. This is simply passed to the
     *         dispatch operation and is not interpreted in any way.
     */
    public TargetInfo predispatch( org.openorb.orb.net.ServerRequest req )
        throws ForwardRequest, AdapterDestroyedException
    {
        BOAEntry entry;
        synchronized ( m_targets )
        {
            entry = find_entry( req.object_key() );
            // this will throw the appropriate exception.
            if ( entry == null )
            {
                find_target( req.object_key() );
            }
        }
        return entry;
    }

    /**
     * Dispatch a request from a client. This may throw a system exception at
     * any time or call the ResponseHandler interface on the request to create
     * a standard reply. If this returns without calling a response handler an
     * empty reply is constructed, this is the usual situation for a locate
     * request.
     */
    public void dispatch( org.openorb.orb.net.ServerRequest req, TargetInfo target )
    {
        if ( req.is_locate() )
        {
            return;
        }
        String operation = req.operation();
        if ( operation.charAt( 0 ) == '_' )
        {
            // we may have a system operation
            if ( operation.equals( "_is_a" ) )
            {
                String repo_id = req.argument_stream().read_string();
                req.createReply().write_boolean( target.targetIsA( repo_id ) );
                return;
            }
            else if ( operation.equals( "_get_domain_managers" ) )
            {
                req.argument_stream();
                org.omg.CORBA.DomainManagersListHelper.write( req.createReply(),
                        lookup_domain_managers( ( BOAEntry ) target ) );
                return;
            }
            else if ( operation.equals( "_interface" ) )
            {
                req.argument_stream();
                req.createReply().write_Object( get_interface_def( target.getRepositoryID() ) );
                return;
            }
            else if ( operation.equals( "_non_existent" ) || operation.equals( "_not_existent" ) )
            {
                req.argument_stream();
                req.createReply().write_boolean( false );
            }
            else if ( operation.equals( "_component" ) )
            {
                req.argument_stream();
                req.createReply().write_Object( target_get_component(
                        ( ( BOAEntry ) target ).getTarget() ) );
                return;
            }
        }

        ObjectImpl impl = ( ( BOAEntry ) target ).getTarget();
        if ( impl instanceof org.omg.CORBA.portable.InvokeHandler )
        {
            ( ( org.omg.CORBA.portable.InvokeHandler ) impl )._invoke(
                    operation, req.argument_stream(), req );
        }
        else if ( impl instanceof org.omg.CORBA.DynamicImplementation )
        {
            org.openorb.orb.core.dsi.ServerRequest dsr =
                    new org.openorb.orb.core.dsi.ServerRequest( req );
            ( ( org.omg.CORBA.DynamicImplementation ) impl ).invoke( dsr );

            if ( req.state() == org.openorb.orb.net.ServerRequest.STATE_PROCESSING )
            {
                dsr.set_result( m_orb.create_any() );
            }
        }
    }

    /**
     * Cancel a dispatch. This may follow a predispatch or dispatch call to
     * indicate that the client no longer expects any reply from the request
     * and the server can stop expending effort towards completing it.
     */
    public void cancel_dispatch( org.openorb.orb.net.ServerRequest req, TargetInfo target )
    {
        // ignore.
    }

    private ObjectImpl find_target( byte [] object_key )
    {
        synchronized ( m_targets )
        {
            byte [][] parts = m_server_manager.extract_cacheable_object_key( object_key );
            if ( m_aid_parts.length != parts.length - 1 )
            {
                throw new org.omg.CORBA.OBJ_ADAPTER( 0,
                        org.omg.CORBA.CompletionStatus.COMPLETED_NO );
            }
            for ( int i = 0; i < m_aid_parts.length; ++i )
            {
                if ( !Arrays.equals( m_aid_parts[ i ], parts[ i ] ) )
                {
                    throw new org.omg.CORBA.OBJ_ADAPTER( 0,
                            org.omg.CORBA.CompletionStatus.COMPLETED_NO );
                }
            }
            byte [] object_id = parts[ parts.length - 1 ];
            Integer key = NumberCache.getInteger( ( ( object_id[ 0 ] & 0xFF ) << 24 )
                  | ( ( object_id[ 1 ] & 0xFF ) << 16 )
                  | ( ( object_id[ 2 ] & 0xFF ) << 8 )
                  |   ( object_id[ 3 ] & 0xFF ) );
            BOAEntry entry = ( BOAEntry ) m_targets.get( key );
            if ( entry == null || !entry.getState() )
            {
                throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0,
                        org.omg.CORBA.CompletionStatus.COMPLETED_NO );
            }
            return entry.getTarget();
        }
    }

    /**
     * Table lookup. Must own lock on m_targets.
     */
    private BOAEntry find_entry( byte [] object_key )
    {
        byte [][] parts = m_server_manager.extract_cacheable_object_key( object_key );
        if ( parts.length != m_aid_parts.length + 1 )
        {
            return null;
        }
        for ( int i = 0; i < m_aid_parts.length; ++i )
        {
            if ( !Arrays.equals( m_aid_parts[ i ], parts[ i ] ) )
            {
                return null;
            }
        }
        byte [] object_id = parts[ m_aid_parts.length ];
        Integer key = NumberCache.getInteger( ( ( object_id[ 0 ] & 0xFF ) << 24 )
              | ( ( object_id[ 1 ] & 0xFF ) << 16 )
              | ( ( object_id[ 2 ] & 0xFF ) << 8 )
              |   ( object_id[ 3 ] & 0xFF ) );
        return ( BOAEntry ) m_targets.get( key );
    }

    /**
     * Reverse table lookup. Must own lock on m_targets.
     */
    private BOAEntry find_entry( ObjectImpl obj )
    {
        Iterator itt = m_targets.values().iterator();
        BOAEntry entry;
        while ( itt.hasNext() )
        {
            entry = ( BOAEntry ) itt.next();
            if ( entry.getTarget() == obj )
            {
                return entry;
            }
        }
        return null;
    }

    private static class BOAEntry
                implements TargetInfo
    {
        private byte [] m_adapter_id;

        private byte [] m_object_id;

        private int m_id;


        private boolean m_active;

        private ObjectImpl m_target;

        BOAEntry( byte [] adapter_id, int id )
        {
            m_id = id;

            m_object_id = new byte[ 4 ];
            m_object_id[ 0 ] = ( byte ) ( id >>> 24 );
            m_object_id[ 1 ] = ( byte ) ( id >>> 16 );
            m_object_id[ 2 ] = ( byte ) ( id >>> 8 );
            m_object_id[ 3 ] = ( byte ) id;

            m_adapter_id = adapter_id;
        }

        public int getId()
        {
            return m_id;
        }

        public boolean getState()
        {
            return m_active;
        }

        public void setState( boolean active )
        {
            m_active = active;
        }

        public void activate()
        {
            m_active = true;
        }

        public void deactivate()
        {
            m_active = false;
        }

        public ObjectImpl getTarget()
        {
            return m_target;
        }

        public void setTarget( ObjectImpl target )
        {
            m_target = target;
        }

        public String getRepositoryID()
        {
            return m_target._ids() [ 0 ];
        }

        public boolean targetIsA( String id )
        {
            Object test = RepoIDHelper.createIsATest( id );
            // compare to Object
            if ( test.equals( "IDL:omg.org/CORBA/Object:1.0" ) )
            {
                return true;
            }
            // compare IDs
            String [] ids = m_target._ids();
            for ( int i = 0; i < ids.length; ++i )
            {
                if ( test.equals( ids[ i ] ) )
                {
                    return true;
                }
            }
            return false;
        }

        public byte[] getAdapterID()
        {
            return m_adapter_id;
        }

        public byte[] getObjectID()
        {
            return m_object_id;
        }
    }

    private Logger getLogger()
    {
        if ( null == m_logger )
        {
            m_logger = ( ( org.openorb.orb.core.ORBSingleton ) m_orb ).getLogger();
        }
        return m_logger;
    }
}

