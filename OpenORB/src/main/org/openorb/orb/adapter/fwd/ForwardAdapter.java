/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.fwd;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.apache.avalon.framework.logger.Logger;

import org.openorb.orb.adapter.AdapterDestroyedException;
import org.openorb.orb.adapter.TargetInfo;
import org.openorb.orb.adapter.ObjectAdapter;

import org.openorb.orb.corbaloc.CorbalocServiceOperations;
import org.openorb.orb.corbaloc.CorbalocServiceHelper;

import org.openorb.orb.core.MinorCodes;

import org.openorb.orb.net.ServerManager;

import org.openorb.util.ExceptionTool;

/**
 * This class provides the implementation for registering objects that can
 * easily accessed by plain-text corbaloc references.
 *
 * @author Chris Wood
 * @version $Revision: 1.8 $ $Date: 2004/02/10 21:02:45 $
 */
public class ForwardAdapter
    implements ObjectAdapter, CorbalocServiceOperations
{
    /**
     * This is the string sent by the JDK ORB when trying to resolve
     * corbaloc references.
     */
    public static final byte [] JDK_ID = "INIT".getBytes();

    /** The name of the service. */
    public static final String CORBALOC_SVC_NAME = "CorbalocService";
    /** The byte representation of the service's name. */
    public static final byte [] CORBALOC_SVC_ID = CORBALOC_SVC_NAME.getBytes();

    /** An zero length byte array as adapter id. */
    private static final byte [] NO_ADAPTER_ID = new byte[ 0 ];

    /** An empty policy array constant. */
    private static final org.omg.CORBA.Policy [] NO_POLICIES =
        new org.omg.CORBA.Policy[ 0 ];

    private Map m_target_map = new HashMap();

    private org.omg.CORBA.Object m_forwardIDef = null;

    private org.omg.CORBA.ORB m_orb;

    private Logger m_logger;

    /**
     * Constructor.
     */
    public ForwardAdapter( ServerManager server_manager )
    {
        server_manager.register_adapter( NO_ADAPTER_ID, this );
        m_orb = server_manager.orb();
    }

    /**
     * Resolve using the given string. Used by JDK clients.
     *
     * @param  initRef String name of initial reference. Should be
     * RFC2396 encoded.
     * @return  the target object.
     * @throws  org.omg.CORBA.BAD_PARAM if the initial reference
     * does not exist.
     */
    public org.omg.CORBA.Object get( String initRef )
    {
        return resolve( initRef.getBytes() );
    }

    /**
     * Add a new initial reference.
     *
     * @param  initRef String name of initial reference. Should be
     * RFC2396 encoded.
     * @param  obj the target object.
     * @throws  org.omg.CORBA.BAD_PARAM if the initial reference
     * already exists.
     */
    public void put( String initRef, org.omg.CORBA.Object obj )
    {
        register( initRef.getBytes(), obj );
    }

    /**
     * Resolve using given byte array.
     *
     * @param  key Initial reference key.
     * @return  the target object.
     * @throws  org.omg.CORBA.BAD_PARAM if the initial reference
     * does not exist.
     */
    public org.omg.CORBA.Object resolve( byte[] key )
    {
        Target target = getTarget( key );
        if ( target == null )
        {
            throw new org.omg.CORBA.BAD_PARAM( "No target registered",
            0, org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        return target.getDest();
    }

    /**
     * Add a new initial reference.
     *
     * @param  oid Name of initial reference. Should be
     * RFC2396 encoded.
     * @param  obj the target object.
     * @throws  org.omg.CORBA.BAD_PARAM if obj is null.
     */
    public void register( byte[] oid, org.omg.CORBA.Object obj )
    {
        if ( obj == null )
        {
            Target target = new Target( oid );
            synchronized ( m_target_map )
            {
                m_target_map.remove( target );
            }
        }
        else
        {
            Target target = new Target( oid, obj );
            synchronized ( m_target_map )
            {
                m_target_map.put( target, target );
            }
        }
    }

    /**
     * Add a new initial reference.
     *
     * @param  object the object to register
     * @param  path String name of initial reference. Should be
     * RFC2396 encoded.
     * @return String the corbaloc URL
     * @throws org.omg.CORBA.BAD_PARAM if the supplied object is null.
     */
    public String put_object( org.omg.CORBA.Object object, String path )
    {
        if ( object == null )
        {
            throw new org.omg.CORBA.BAD_PARAM( "Null object argument." );
        }
        org.openorb.orb.core.Delegate delegate = ( org.openorb.orb.core.Delegate )
          ( ( org.omg.CORBA.portable.ObjectImpl ) object )._get_delegate();
        org.openorb.orb.net.Address [] address = delegate.getAddresses( object );
        String endpoint = null;
        for ( int i = 0; i < address.length; ++i )
        {
            if ( address[ i ].getProtocol().equals( "iiop" ) )
            {
                endpoint = address[ i ].getEndpointString();
                break;
            }
        }
        if ( endpoint == null )
        {
            endpoint = address[ 0 ].getEndpointString();
        }
        String name = path;
        if ( path.startsWith( "/" ) )
        {
            name = path.substring( 1, path.length() );
        }
        else
        {
            name = path;
        }
        put( name, object );
        return "corbaloc:" + endpoint + "/" + name;
    }

    /**
     * Get the operation target.
     */
    private Target getTarget( byte [] object_key )
    {
        synchronized ( m_target_map )
        {
            return ( Target ) m_target_map.get( new Target( object_key ) );
        }
    }

    /**
     * Expected lifetime of the adapter. Higher numbers are more likley to be
     * dropped from the lookup cache. If this returns 0 then the adapter
     * should never be dropped. This value should be stable throughout the
     * lifetime of the adapter. The highest byte will be used for determining
     * the binding priority.
     *
     * Suggested values:
     * = 0          Root adapters. Always keep.
     * &lt; 0x1000000  Adpaters created directly.
     * &lt; 0x2000000  Adapters created dynamicaly.
     * &lt; 0x3000000  Objects created directly.
     * &lt; 0x4000000  Objects created dynamicaly.
     * &lt; 0          Never cache adapter, single invocation only.
     *              These should not be returned from find_adapter.
     */
    public int cache_priority()
    {
        return 0;
    }

    /**
     * Adapter is single threaded. Calls to all single threaded Adapters are
     * serialized.
     */
    public boolean single_threaded()
    {
        return false;
    }

    /**
     * Etherialize the adapter. When this function returns the adapter's
     * memory resident state should have been minimized. This function will
     * always be called before purging the adapter from the cache. If
     * cleanup_in_progress is true the adapter is being perminently
     * deactivated and will no longer have to dispatch operations.
     */
    public void etherealize( boolean cleanup_in_progress )
    {
    }

    /**
     * Queue manager for the adapter. This may return null for an adapter
     * which is always active. To create an adapter manager for an adapter use
     * the create_manager operation on the ServerManager.
     */
    public org.openorb.orb.net.AdapterManager getAdapterManager()
    {
        return null;
    }

    /**
     * If this adapter serves this object directly it should return itself,
     * otherwise it should return an adapter that does. If an adapter for
     * an object with the given object id cannot be found this returns null.
     * The returned adapter may be cached for future use.
     */
    public ObjectAdapter find_adapter( byte[] object_key )
        throws AdapterDestroyedException
    {
        synchronized ( m_target_map )
        {
            if ( m_target_map.containsKey( new Target( object_key ) ) )
            {
                return this;
            }
        }
        if ( Arrays.equals( object_key, JDK_ID )
              || Arrays.equals( object_key, CORBALOC_SVC_ID ) )
        {
            return this;
        }
        return null;
    }

    /**
     * Return the adapter id. This should be a prefix of the object_key,
     * it should be stable with respect to a given object_key and will be
     * treated as read-only. If an object with the given object_key is not
     * served by this adapter this may return null.
     */
    public byte[] adapter_id( byte[] object_key )
    {
        synchronized ( m_target_map )
        {
            if ( m_target_map.containsKey( new Target( object_key ) ) )
            {
                return NO_ADAPTER_ID;
            }
        }
        if ( Arrays.equals( object_key, JDK_ID )
              || Arrays.equals( object_key, CORBALOC_SVC_ID ) )
        {
            return NO_ADAPTER_ID;
        }
        return null;
    }

    /**
     * Return the object id. This should should be a suffix of the object_key
     * if the object id is cacheable, it should be stable with respect to a
     * given object_key and will be treated as read-only. If an object with
     * the given object_key is not served by this adapter this returns null.
     */
    public byte[] object_id( byte [] object_key )
    {
        synchronized ( m_target_map )
        {
            if ( m_target_map.containsKey( new Target( object_key ) ) )
            {
                return object_key;
            }
        }
        if ( Arrays.equals( object_key, JDK_ID )
              || Arrays.equals( object_key, CORBALOC_SVC_ID ) )
        {
            return object_key;
        }
        return null;
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
    public org.omg.CORBA.portable.ServantObject servant_preinvoke(
        byte[] object_key, String operation, Class expectedType )
        throws org.omg.PortableInterceptor.ForwardRequest,
               AdapterDestroyedException
    {
        if ( !locate( object_key ) )
        {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0,
                org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        org.omg.CORBA.portable.ServantObject ret =
            new org.omg.CORBA.portable.ServantObject();
        ret.servant = this;
        return ret;
    }

    /**
     * Close off a local operation. Always paired with a call to
     * servant_preinvoke
     */
    public void servant_postinvoke( byte[] object_key,
        org.omg.CORBA.portable.ServantObject srvObject )
    {
    }

    /**
     * Respond to a local locate request. This returns true if the object
     * is located locally, false if the object is unknown and throws a
     * forward request for a location forward. This should not throw a
     * system exception.
     */
    public boolean locate( byte[] object_key )
        throws org.omg.PortableInterceptor.ForwardRequest,
               AdapterDestroyedException
    {
        if ( Arrays.equals( object_key, JDK_ID )
              || Arrays.equals( object_key, CORBALOC_SVC_ID ) )
        {
            return true;
        }
        Target target = getTarget( object_key );
        if ( target == null )
        {
            return false;
        }
        throw new org.omg.PortableInterceptor.ForwardRequest( target.getDest() );
    }

    /**
     * is_a operation.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    public boolean is_a( byte[] object_key, String repository_id )
        throws org.omg.PortableInterceptor.ForwardRequest,
               AdapterDestroyedException
    {
        if ( Arrays.equals( object_key, JDK_ID )
              || Arrays.equals( object_key, CORBALOC_SVC_ID ) )
        {
            return repository_id.equals( CorbalocServiceHelper.id() );
        }
        Target target = getTarget( object_key );
        if ( target == null )
        {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0,
                org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        return target.getDest()._is_a( repository_id );
    }

    /**
     * get_interface_def operation.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    public org.omg.CORBA.Object get_interface_def( byte[] object_key )
        throws org.omg.PortableInterceptor.ForwardRequest,
               AdapterDestroyedException
    {
        if ( Arrays.equals( object_key, JDK_ID )
              || Arrays.equals( object_key, CORBALOC_SVC_ID ) )
        {
            return get_interface_def();
        }
        Target target = getTarget( object_key );
        if ( target == null )
        {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0,
                org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        return target.getDest()._get_interface_def();
    }

    /**
     *
     */
    private org.omg.CORBA.Object get_interface_def()
    {
        if ( m_forwardIDef == null )
        {
            org.omg.CORBA.Object obj;
            try
            {
                obj = m_orb.resolve_initial_references(
                    "InterfaceRepository" );
            }
            catch ( final org.omg.CORBA.ORBPackage.InvalidName ex )
            {
                getLogger().error( "Could not resolve InterfaceRepository.",
                    ex );
                throw ExceptionTool.initCause( new org.omg.CORBA.INTF_REPOS(
                    MinorCodes.INF_REPOS_FIND,
                    org.omg.CORBA.CompletionStatus.COMPLETED_NO ), ex );
            }
            if ( obj._non_existent() )
            {
                throw new org.omg.CORBA.INTF_REPOS(
                    MinorCodes.INF_REPOS_FIND,
                    org.omg.CORBA.CompletionStatus.COMPLETED_NO );
            }
            org.omg.CORBA.Repository rep;
            try
            {
                rep = org.omg.CORBA.RepositoryHelper.narrow( obj );
            }
            catch ( final org.omg.CORBA.BAD_PARAM ex )
            {
                getLogger().error( "Could not narrow obj to type Repository.",
                    ex );
                throw ExceptionTool.initCause( new org.omg.CORBA.INTF_REPOS(
                    MinorCodes.INF_REPOS_FIND,
                    org.omg.CORBA.CompletionStatus.COMPLETED_NO ), ex );
            }
            m_forwardIDef = rep.lookup_id( CorbalocServiceHelper.id() );
            if ( m_forwardIDef == null )
            {
                throw new org.omg.CORBA.INTF_REPOS(
                    MinorCodes.INF_REPOS_LOOKUP,
                    org.omg.CORBA.CompletionStatus.COMPLETED_NO );
            }
        }
        return m_forwardIDef;
    }

    /**
     * get_domain_manager operation.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    public org.omg.CORBA.DomainManager[] get_domain_managers(
        byte[] object_key )
        throws org.omg.PortableInterceptor.ForwardRequest,
               AdapterDestroyedException
    {
        if ( Arrays.equals( object_key, JDK_ID )
              || Arrays.equals( object_key, CORBALOC_SVC_ID ) )
        {
            return new org.omg.CORBA.DomainManager[ 0 ];
        }
        Target target = getTarget( object_key );
        if ( target == null )
        {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0,
                org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        return target.getDest()._get_domain_managers();
    }

    /**
     * get_componenent operation.
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     *
     * @param object_key The key of the object.
     * @return The object specified by the object key.
     */
    public org.omg.CORBA.Object get_component( byte[] object_key )
        throws org.omg.PortableInterceptor.ForwardRequest,
               AdapterDestroyedException
    {
        if ( Arrays.equals( object_key, JDK_ID )
              || Arrays.equals( object_key, CORBALOC_SVC_ID ) )
        {
            return null;
        }
        Target target = getTarget( object_key );
        if ( target == null )
        {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0,
                org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        org.omg.CORBA.Object obj = target.getDest();
        if ( !( obj instanceof org.omg.CORBA.portable.ObjectImpl ) )
        {
            throw new org.omg.CORBA.BAD_OPERATION( 0,
                org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        org.omg.CORBA.portable.Delegate deleg = ( (
            org.omg.CORBA.portable.ObjectImpl ) obj )._get_delegate();
        if ( !( deleg instanceof org.openorb.orb.core.Delegate ) )
        {
            throw new org.omg.CORBA.BAD_OPERATION( 0,
                org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        return ( ( org.openorb.orb.core.Delegate ) deleg ).get_component( obj );
    }

    /**
     * Locate the servant object for a request. The returned object is
     * handed to the dispatch operation. This may throw a system exception or
     * respond with a forward request if one is indicated, in which case the
     * dispatch operation will not be called.
     *
     * @param req out parameter holding repository ids of all available
     * interfaces with the most derived interface appearing first.
     * @return the 'target' of the operation. This is simply passed to the
     * dispatch operation and is not interpreted in any way.
     */
    public TargetInfo predispatch( org.openorb.orb.net.ServerRequest req )
        throws org.omg.PortableInterceptor.ForwardRequest,
               AdapterDestroyedException
    {
        byte [] object_key = req.object_key();
        locate( object_key );
        if ( Arrays.equals( object_key, JDK_ID ) )
        {
            return FORWARD_TI_JDK;
        }
        if ( Arrays.equals( object_key, CORBALOC_SVC_ID ) )
        {
            return FORWARD_TI_INIT;
        }
        throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0,
            org.omg.CORBA.CompletionStatus.COMPLETED_NO );
    }

    /**
     * Dispatch a request from a client. This may throw a system exception at
     * any time or call the ResponseHandler interface on the request to create
     * a standard reply. If this returns without calling a response handler an
     * empty reply is constructed, this is the usual situation for a locate
     * request.
     */
    public void dispatch( org.openorb.orb.net.ServerRequest req, TargetInfo ti )
    {
        // this will only occour when the object ID is INIT.
        String opName = req.operation();
        org.omg.CORBA.portable.InputStream is = req.argument_stream();
        if ( opName.charAt( 0 ) == '_' )
        {
            // we may have a system operation.
            if ( opName.equals( "_is_a" ) )
            {
                String repo_id = is.read_string();
                req.createReply().write_boolean( repo_id.equals(
                    CorbalocServiceHelper.id() ) );
                return;
            }
            else if ( opName.equals( "_get_domain_managers" ) )
            {
                org.omg.CORBA.DomainManagersListHelper.write(
                    req.createReply(), new org.omg.CORBA.DomainManager[ 0 ] );
                return;
            }
            else if ( opName.equals( "_interface" ) )
            {
                org.omg.CORBA.Object ret = get_interface_def();
                req.createReply().write_Object( ret );
                return;
            }
            else if ( opName.equals( "_non_existent" )
                  || opName.equals( "_not_existent" ) )
            {
                req.createReply().write_boolean( false );
                return;
            }
            else if ( opName.equals( "_component" ) )
            {
                req.createReply().write_Object( null );
                return;
            }
        }

        if ( opName.equals( "get" ) )
        {
            java.lang.String arg0_in = is.read_string();
            org.omg.CORBA.Object argResult = get( arg0_in );
            org.omg.CORBA.portable.OutputStream os = req.createReply();
            os.write_Object( argResult );
        }
        else if ( opName.equals( "put" ) )
        {
            java.lang.String arg0_in = is.read_string();
            org.omg.CORBA.Object arg1_in = is.read_Object();
            put( arg0_in, arg1_in );
            req.createReply();
        }
        else if ( opName.equals( "resolve" ) )
        {
            byte[] arg0_in = org.omg.CORBA.OctetSeqHelper.read( is );
            org.omg.CORBA.Object argResult = resolve( arg0_in );
            org.omg.CORBA.portable.OutputStream os = req.createReply();
            os.write_Object( argResult );
        }
        else if ( opName.equals( "register" ) )
        {
            byte[] arg0_in = org.omg.CORBA.OctetSeqHelper.read( is );
            org.omg.CORBA.Object arg1_in = is.read_Object();
            register( arg0_in, arg1_in );
            req.createReply();
        }
        else
        {
            throw new org.omg.CORBA.BAD_OPERATION();
        }
    }

    /**
     * Cancel a dispatch. This may follow a predispatch or dispatch call to
     * indicate that the client no longer expects any reply from the request
     * and the server can stop expending effort towards completing it.
     */
    public void cancel_dispatch( org.openorb.orb.net.ServerRequest req,
        TargetInfo target )
    {
        // empty.
    }

    private static final TargetInfo FORWARD_TI_JDK =
      new TargetInfo()
        {
          public byte[] getAdapterID()
          {
            return NO_ADAPTER_ID;
          }

          public String getRepositoryID()
          {
             return CorbalocServiceHelper.id();
          }

          public byte[] getObjectID()
          {
             return JDK_ID;
          }

          public boolean targetIsA( String id )
          {
            if ( id.startsWith( "IDL:omg.orb/CORBA/Object:1." ) )
            {
                return true;
            }
            if ( id.equals( CorbalocServiceHelper.id() ) )
            {
                return true;
            }
            return false;
          }
      };

    private static final TargetInfo FORWARD_TI_INIT = new TargetInfo()
    {
        public byte[] getAdapterID()
        {
            return NO_ADAPTER_ID;
        }

        public String getRepositoryID()
        {
            return CorbalocServiceHelper.id();
        }

        public byte[] getObjectID()
        {
            return CORBALOC_SVC_ID;
        }

        public boolean targetIsA( String id )
        {
            if ( id.startsWith( "IDL:omg.orb/CORBA/Object:1." ) )
            {
                return true;
            }
            if ( id.equals( CorbalocServiceHelper.id() ) )
            {
                return true;
            }
            return false;
        }
    };

    /**
     * ???
     */
    private static class Target
    {
        private org.omg.CORBA.Object m_dest;
        private byte [] m_key;
        private int m_hash;

        public Target( byte [] key )
        {
            this( key, null );
        }

        public Target( byte [] key, org.omg.CORBA.Object target )
        {
            m_key = key;
            m_dest = target;
            m_hash = 0;
            for ( int i = 0; i < key.length; ++i )
            {
                m_hash = 31 * m_hash + ( key[ i ] & 0xFF );
            }
        }

        public int hashCode()
        {
            return m_hash;
        }

        public org.omg.CORBA.Object getDest()
        {
            return m_dest;
        }

        public byte[] getKey()
        {
            return m_key;
        }

        public boolean equals( Object obj )
        {
            if ( !( obj instanceof Target ) )
            {
                return false;
            }
            Target t2 = ( Target ) obj;
            if ( m_hash != t2.m_hash || m_key.length != t2.m_key.length )
            {
                return false;
            }
            for ( int i = 0; i < m_key.length; ++i )
            {
                if ( m_key[ i ] != t2.m_key[ i ] )
                {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Return the logger instance.
     */
    private Logger getLogger()
    {
        if ( null == m_logger )
        {
            m_logger = ( ( org.openorb.orb.core.ORBSingleton ) m_orb ).getLogger();
        }
        return m_logger;
    }
}

