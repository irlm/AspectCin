/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.poa;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Arrays;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.ServantObject;

import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.DynamicImplementation;
import org.omg.PortableServer.AdapterActivator;
import org.omg.PortableServer.ID_UNIQUENESS_POLICY_ID;
import org.omg.PortableServer.IdAssignmentPolicy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;
import org.omg.PortableServer.IdUniquenessPolicy;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID;
import org.omg.PortableServer.ImplicitActivationPolicy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LIFESPAN_POLICY_ID;
import org.omg.PortableServer.LifespanPolicy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID;
import org.omg.PortableServer.RequestProcessingPolicy;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;
import org.omg.PortableServer.ServantRetentionPolicy;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.THREAD_POLICY_ID;
import org.omg.PortableServer.ThreadPolicy;
import org.omg.PortableServer.ThreadPolicyValue;

import org.omg.PortableServer.portable.Delegate;

import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.AdapterNonExistent;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAPackage.NoServant;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongAdapter;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import org.openorb.orb.adapter.ObjectAdapter;
import org.openorb.orb.adapter.TargetInfo;
import org.openorb.orb.adapter.IORUtil;
import org.openorb.orb.adapter.AdapterDestroyedException;

import org.openorb.orb.net.ClientManager;
import org.openorb.orb.net.ServerManager;
import org.openorb.orb.net.AdapterManager;

import org.openorb.orb.policy.ForceMarshalPolicy;
import org.openorb.orb.policy.FORCE_MARSHAL_POLICY_ID;

import org.openorb.util.ExceptionTool;
import org.openorb.util.NumberCache;

/**
 * This is the implementation of the POA interface.
 */
class POA
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableServer.POA, ObjectAdapter, LogEnabled
{
    private static PolicyFactoryImpl s_policy_factory = PolicyFactoryImpl.getInstance();

    private ORB m_orb;

    /** This member is referenced in class RootPOA */
    private ServerManager m_server_manager;
    private ClientManager m_client_manager;

    private RootPOA m_root;
    private POA m_parent;
    private Map m_child_poas = new HashMap();
    private Set m_in_create = new HashSet();
    private Logger m_logger;

    private Delegate m_delegate;

    private AdapterManager m_poa_manager;

    //
    // policy settings.
    //

    // Thread Policy (RootPOA -> ORB_CTRL_MODEL)
    private boolean m_single_threaded     = false;
    // Lifespan Policy (RootPOA -> TRANSIENT)
    private boolean m_persistent          = false;
    // Object Id Uniqueness Policy (RootPOA -> UNIQUE_ID)
    private boolean m_multiple_ids        = false;
    // Id Assignment Policy (RootPOA -> SYSTEM_ID)
    private boolean m_user_ids            = false;
    // Servant Retention Policy (RootPOA -> RETAIN)
    private boolean m_non_retain          = false;
    // Request Processing Policy (RootPOA -> USE_ACTIVE_OBJECT_MAP_ONLY)
    private boolean m_use_default         = false;
    private boolean m_use_manager         = false;
    // Implicit Activation Policy (RootPOA -> IMPLICIT_ACTIVATION)
    private boolean m_implicit_activation = false;

    private boolean m_force_marshal = false;
    private boolean m_interrupt_on_cancel = false;
    private boolean m_per_reference_domain_managers = false;

    private org.omg.CORBA.PolicyManagerOperations m_policy_set;

    // adapter id settings
    private String [] m_poa_name;
    private byte [][] m_aid_parts;
    private byte [] m_aid;

    // active object map.
    private Object m_aom_sync = null;
    private Map m_aom = null;

    private CurrentImpl m_poa_current;

    private Object m_sync_requests = new Object();
    private boolean m_destroyed = false;
    private boolean m_rejecting = false;
    private boolean m_etherealize = false;
    private int m_active_requests = 0;

    // domain manager stuff. both of these will be null if
    // per_reference_domain_managers is true.
    private org.openorb.orb.pi.ComponentSet m_poa_comp_set = null;
    private org.omg.CORBA.DomainManager m_poa_domain_manager = null;

    // adapter activator
    private AdapterActivator m_adapter_activator;

    // request processors
    private Servant m_default_servant;
    private ServantActivator m_servant_activator;
    private ServantLocator m_servant_locator;

    // system ids.
    private Object m_next_system_id_sync = null;
    private long m_next_system_id;
    private long m_first_system_id;

    // poa numbers. transient poas are assigned an extra number to
    // ensure they are not recreated.
    //private static Object m_poa_num_sync = new Object();
    //private static int m_poa_num = 0;

    private void initializePolicies( org.omg.CORBA.Policy [] policies, int [] types )
        throws InvalidPolicy
    {
        // initialize the policy settings.
        for ( int i = 0; i < policies.length; ++i )
        {
            switch ( policies[ i ].policy_type() )
            {

            case THREAD_POLICY_ID.value:
                m_single_threaded = ( ( ThreadPolicy ) policies[ i ] ).value()
                      != ThreadPolicyValue.ORB_CTRL_MODEL;
                break;

            case LIFESPAN_POLICY_ID.value:
                m_persistent = ( ( LifespanPolicy ) policies[ i ] ).value()
                      == LifespanPolicyValue.PERSISTENT;
                break;

            case ID_UNIQUENESS_POLICY_ID.value:
                m_multiple_ids = ( ( IdUniquenessPolicy ) policies[ i ] ).value ()
                      == IdUniquenessPolicyValue.MULTIPLE_ID;
                break;

            case ID_ASSIGNMENT_POLICY_ID.value:
                m_user_ids = ( ( IdAssignmentPolicy ) policies[ i ] ).value()
                      == IdAssignmentPolicyValue.USER_ID;
                break;

            case SERVANT_RETENTION_POLICY_ID.value:
                m_non_retain = ( ( ServantRetentionPolicy ) policies[ i ] ).value()
                      == ServantRetentionPolicyValue.NON_RETAIN;
                if ( m_non_retain
                      && Arrays.binarySearch( types, ID_UNIQUENESS_POLICY_ID.value ) < 0 )
                {
                    m_multiple_ids = true;
                }
                break;

            case REQUEST_PROCESSING_POLICY_ID.value:
                switch ( ( ( RequestProcessingPolicy ) policies[ i ] ).value().value() )
                {

                case RequestProcessingPolicyValue._USE_DEFAULT_SERVANT:
                    m_use_default = true;
                    if ( Arrays.binarySearch( types, ID_UNIQUENESS_POLICY_ID.value ) < 0 )
                    {
                        m_multiple_ids = true;
                    }
                    break;

                case RequestProcessingPolicyValue._USE_SERVANT_MANAGER:
                    m_use_manager = true;
                    break;

                case RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY:
                    // this is the default, do nothing
                    break;

                default:
                    if ( getLogger().isWarnEnabled() )
                    {
                        getLogger().warn( "Unkown request processing policy value: "
                              + ( ( RequestProcessingPolicy ) policies[ i ] ).value().value() );
                    }
                    break;
                }
                break;

            case IMPLICIT_ACTIVATION_POLICY_ID.value:
                m_implicit_activation = ( ( ImplicitActivationPolicy ) policies[ i ] ).value()
                      == ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION;
                break;

            case FORCE_MARSHAL_POLICY_ID.value:
                m_force_marshal = ( ( ForceMarshalPolicy ) policies[ i ] ).forceMarshal();
                break;

            default:
                // Do not show a warning as it is quite common that more policies have been
                // specified than actually needed to create the POA. These policies will be
                // handled later on.
                break;
            }
        }
        // test for conflicting policies
        if ( m_non_retain && ( ( !m_use_default && !m_use_manager ) || !m_multiple_ids ) )
        {
            for ( int i = 0; i < policies.length; ++i )
            {
                switch ( policies[ i ].policy_type() )
                {
                    case SERVANT_RETENTION_POLICY_ID.value:
                    case REQUEST_PROCESSING_POLICY_ID.value:
                    case ID_UNIQUENESS_POLICY_ID.value:
                        throw new InvalidPolicy( ( short ) i );

                    default:
                        // Do nothing in this case
                        break;
                }
            }
            throw new InvalidPolicy( ( short ) 0 );
        }

        if ( m_use_default && !m_multiple_ids )
        {
            for ( int i = 0; i < policies.length; ++i )
            {
                switch ( policies[ i ].policy_type() )
                {
                    case REQUEST_PROCESSING_POLICY_ID.value:
                    case ID_UNIQUENESS_POLICY_ID.value:
                        throw new InvalidPolicy( ( short ) i );

                    default:
                        // Do nothing in this case
                        break;
                }
            }
            throw new InvalidPolicy( ( short ) 0 );
        }

        if ( m_implicit_activation && m_user_ids )
        {
            for ( int i = 0; i < policies.length; ++i )
            {
                switch ( policies[ i ].policy_type() )
                {
                    case IMPLICIT_ACTIVATION_POLICY_ID.value:
                    case ID_ASSIGNMENT_POLICY_ID.value:
                        throw new InvalidPolicy( ( short ) i );

                    default:
                        // Do nothing in this case
                        break;
                }
            }
            throw new InvalidPolicy( ( short ) 0 );
        }
    }

    /** Creates new POA */
    POA( org.omg.CORBA.ORB orb, POA parent, String [] poa_name,
         AdapterManager poa_manager,
         org.omg.CORBA.Policy [] policies )
        throws InvalidPolicy
    {
        // remove null policies
        int c = policies.length;
        for ( int i = 0; i < policies.length; ++i )
        {
            if ( policies[ i ] == null )
            {
                --c;
            }
        }
        if ( c != policies.length )
        {
            org.omg.CORBA.Policy [] tmp = policies;
            policies = new org.omg.CORBA.Policy[ c ];
            for ( int i = tmp.length - 1; c > 0; --i )
            {
                if ( tmp[ i ] != null )
                {
                    policies[ --c ] = tmp[ i ];
                }
            }
        }

        // check for duplicate policy settings
        int [] types = new int[ policies.length ];
        for ( int i = 0; i < types.length; ++i )
        {
            types[ i ] = policies[ i ].policy_type();
        }
        Arrays.sort( types );
        for ( int i = 1; i < types.length; ++i )
        {
            if ( types[ i ] == types[ i - 1 ] )
            {
                for ( int j = 0; j < policies.length; ++j )
                {
                    if ( policies[ j ].policy_type() == types[ i ] )
                    {
                        throw new InvalidPolicy( ( short ) j );
                    }
                }
            }
        }
        m_poa_name = poa_name;
        m_orb = orb;
        enableLogging( ( ( org.openorb.orb.core.ORBSingleton ) m_orb ).getLogger() );
        m_parent = parent;
        if ( m_parent != null )
        {
            m_root = m_parent.getRootPOA();
            m_server_manager = m_root.getServerManager();
        }
        else
        {
            m_root = ( RootPOA ) this;
            m_server_manager = ( org.openorb.orb.net.ServerManager )
                    ( ( org.openorb.orb.core.ORB ) m_orb ).getFeature( "ServerCPCManager" );
        }
        initializePolicies( policies, types );
        if ( m_single_threaded )
        {
            m_force_marshal = true;
        }
        // create the policy set
        org.openorb.orb.policy.PolicySetManager psm = ( org.openorb.orb.policy.PolicySetManager )
               ( ( org.openorb.orb.core.ORB ) m_orb ).getFeature( "PolicySetManager" );
        m_policy_set = psm.create_policy_set(
               org.openorb.orb.policy.PolicySetManager.SERVER_POLICY_DOMAIN );
        try
        {
            m_policy_set.set_policy_overrides( policies,
                    org.omg.CORBA.SetOverrideType.SET_OVERRIDE );
        }
        catch ( final org.omg.CORBA.InvalidPolicies ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Invalid policies passed to set_policy_overrides().", ex );
            }
            if ( ex.indices.length == 0 )
            {
                throw ( org.omg.PortableServer.POAPackage.InvalidPolicy )
                        ExceptionTool.initCause(
                        new org.omg.PortableServer.POAPackage.InvalidPolicy(
                        ( short ) -1 ), ex );
            }
            else
            {
                throw ( org.omg.PortableServer.POAPackage.InvalidPolicy )
                        ExceptionTool.initCause(
                        new org.omg.PortableServer.POAPackage.InvalidPolicy(
                        ex.indices[ 0 ] ), ex );
            }
        }

        // create poa manager.
        if ( poa_manager == null )
        {
            m_poa_manager = m_server_manager.create_adapter_manager();
        }
        else
        {
            m_poa_manager = poa_manager;
        }
        // setup the aom.
        if ( !m_non_retain )
        {
            m_aom_sync = new Object();
            m_aom = new HashMap();
        }

        // setup the id counters
        if ( !m_user_ids )
        {
            m_next_system_id_sync = new Object();
            if ( m_persistent )
            {
                m_next_system_id = System.currentTimeMillis() * 121L;
            }
            else
            {
                m_first_system_id =
                       ( System.currentTimeMillis() * 11L ) & 0xFFFFFFFFL;
                m_next_system_id = m_first_system_id;
            }
        }

        // find the poa current
        try
        {
            m_poa_current = ( CurrentImpl ) m_orb.resolve_initial_references( "POACurrent" );
        }
        catch ( org.omg.CORBA.ORBPackage.InvalidName ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Unable to locate POA Current", ex );
            }
        }

        // initialize the adapter id.
        if ( m_poa_name.length > 1 )
        {
            m_aid_parts = new byte[ m_parent.m_aid_parts.length + 1 ][];
            System.arraycopy( m_parent.m_aid_parts, 0, m_aid_parts, 0,
                    m_parent.m_aid_parts.length );
        }
        else
        {
            m_aid_parts = new byte[ 1 ][];
        }

        try
        {
            m_aid_parts[ m_aid_parts.length - 1 ] =
                   m_poa_name[ m_aid_parts.length - 1 ].getBytes( "UTF-8" );
        }
        catch ( final java.io.UnsupportedEncodingException ex )
        {
            getLogger().error( "UTF-8 encoding unknown.", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                    "UTF-8 encoding unknown (" + ex + ")" ), ex );
        }

        byte [][] tmp_parts = new byte[ m_aid_parts.length + 1 ][];
        System.arraycopy( m_aid_parts, 0, tmp_parts, 0, m_aid_parts.length );
        tmp_parts[ m_aid_parts.length ] = new byte[ 0 ];
        m_aid = m_server_manager.create_cacheable_object_key( m_persistent, tmp_parts );

        // find delegate
        m_delegate = ( org.omg.PortableServer.portable.Delegate )
                ( ( org.openorb.orb.core.ORB ) m_orb ).getFeature( "POADelegate" );
    }

    public org.omg.CORBA.ORB orb()
    {
        return m_orb;
    }

    public ServerManager getServerManager()
    {
        return m_server_manager;
    }

    public ServerManager setServerManager( ServerManager server_manager )
    {
        m_server_manager = server_manager;
        return m_server_manager;
    }

    public org.omg.CORBA.PolicyManagerOperations getPolicySet()
    {
        return m_policy_set;
    }

    public org.omg.CORBA.PolicyManagerOperations setPolicySet(
            org.omg.CORBA.PolicyManagerOperations policy_set )
    {
        m_policy_set = policy_set;
        return m_policy_set;
    }

    public static org.openorb.orb.adapter.poa.PolicyFactoryImpl getPolicyFactory()
    {
        return s_policy_factory;
    }

    public static PolicyFactoryImpl setPolicyFactory( PolicyFactoryImpl policy_factory )
    {
        s_policy_factory = policy_factory;
        return s_policy_factory;
    }

    public RootPOA getRootPOA()
    {
        return m_root;
    }

    public byte [] getAid()
    {
        return m_aid;
    }

    public byte [] setAid( byte [] aid )
    {
        m_aid = aid;
        return m_aid;
    }

    public org.openorb.orb.pi.ComponentSet getPoaCompSet()
    {
        return m_poa_comp_set;
    }

    public org.openorb.orb.pi.ComponentSet setPoaCompSet(
           org.openorb.orb.pi.ComponentSet poa_comp_set )
    {
        m_poa_comp_set = poa_comp_set;
        return m_poa_comp_set;
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
     */
    public int cache_priority()
    {
        return 1;
    }

    /**
     * Adapter is single threaded. Calls to all single threaded Adapters are
     * serialized.
     */
    public boolean single_threaded()
    {
        return m_single_threaded;
    }

    /**
     * Etherealize the adapter. When this function returns the adapter's memory
     * resident state should have been minimized. This function will always be
     * called before purging the adapter from the cache. If cleanup_in_progress
     * is true the adapter is being perminently deactivated and will no longer
     * have to dispatch operations. This will not be called if there are any
     * in process requests.
     */
    public void etherealize( boolean cleanup_in_progress )
    {
        if ( cleanup_in_progress )
        {
            synchronized ( m_sync_requests )
            {
                m_rejecting = true;
            }
        }

        if ( m_non_retain )
        {
            return;
        }
        synchronized ( m_aom_sync )
        {
            if ( m_servant_activator != null )
            {
                // call etherealize.
                Iterator itt = m_aom.entrySet().iterator();

                while ( itt.hasNext() )
                {
                    Map.Entry me = ( Map.Entry ) itt.next();

                    if ( !( me.getKey() instanceof ServantKey ) )
                    {
                        AOMEntry entry = ( AOMEntry ) me.getValue();
                        boolean remaining = false;

                        if ( m_multiple_ids )
                        {
                            IntHolder active = ( IntHolder ) m_aom.get(
                                   new ServantKey( entry.getServant() ) );

                            if ( --( active.value ) > 0 )
                            {
                                remaining = true;
                            }
                        }

                        m_servant_activator.etherealize( entry.getObjectId(), this,
                               entry.getServant(), cleanup_in_progress, remaining );
                    }
                }
            }

            m_aom.clear();
        }
    }

    /**
     * Queue manager for the adapter. This may return null for an adapter which
     * is always active. To create an adapter manager for an adapter use the
     * create_manager operation on the ServerManager.
     */
    public AdapterManager getAdapterManager()
    {
        return m_poa_manager;
    }

    /**
     * Find an adapter or an ancestor adapter to serve requests to the specified
     * object key.
     *
     * If this adapter serves this object directly it should return
     * itself, if it can find a decendant adapter which serves the request
     * without entering user code the decendant is returned. If user code
     * associated with a decendant adapter which does not share this adapter's
     * adapter manager must be excecuted in order to create an adapter then
     * that decendant adapter is returned.
     *
     * In essence the requirement that the adapter manager must be consulted
     * before excecuting user code is preserved.
     *
     * If a decendant adapter is in the process of being destroyed the
     * AdapterDestroyedException is thrown. The find operation can be re-tried
     * once the adapter has been destroyed.
     *
     * The object key passed to this argument will always be prefixed by the
     * adapter id as registered in the server manager. If only one registration
     * is present then the prefix does not need to be checked for a match with
     * the adapter id.
     */
    public ObjectAdapter find_adapter( byte[] object_key )
        throws AdapterDestroyedException
    {
        byte [][] parts = m_server_manager.extract_cacheable_object_key( object_key );
        boolean isPersist = m_server_manager.is_suid_object_key( object_key );

        if ( isPersist == m_persistent && m_aid_parts.length == parts.length - 1 )
        {
            synchronized ( m_sync_requests )
            {
                if ( m_destroyed )
                {
                    throw new AdapterDestroyedException( this, m_aid );
                }
                if ( m_rejecting )
                {
                    return null;
                }
            }

            return this;
        }

        POA ret = this;
        int i = m_aid_parts.length;

        try
        {
            // can create the first poa in the line, manager's permission
            // will have been received.
            String childName = new String( parts[ i++ ], "UTF-8" );
            ret = ( POA ) ret.find_POA( childName, true );

            for ( ; i < parts.length - 1; ++i )
            {
                childName = new String( parts[ i ], "UTF-8" );

                try
                {
                    ret = ( POA ) ret.find_POA( childName, false );
                }
                catch ( AdapterNonExistent ex )
                {
                    // attempt to create the POA using the AdapterActivator.
                    // we can create child adapters if the managers are identical
                    // and the point we are up to is not single threaded.
                    if ( ret.getAdapterManager() == m_poa_manager && !ret.single_threaded() )
                    {
                        ret = ( POA ) ret.find_POA( childName, true );
                    }
                    else
                    {
                        return ret;
                    }
                }
            }
        }
        catch ( java.io.UnsupportedEncodingException ex )
        {
            return null;
        }
        catch ( AdapterNonExistent ex )
        {
            return null;
        }
        catch ( final org.omg.CORBA.OBJECT_NOT_EXIST ex )
        {
            synchronized ( ret.m_sync_requests )
            {
                if ( ret.m_destroyed )
                {
                    getLogger().error( "Unable to find the first POA.", ex );

                    throw ( AdapterDestroyedException ) ExceptionTool.initCause(
                            new AdapterDestroyedException( ret, ret.m_aid ), ex );
                }
            }

            return null;
        }

        return ret;
    }

    /**
     * Return the adapter id. This should be a prefix of the object_key if the
     * object_key is cacheable, it should be stable with respect to a given
     * object_key and will be treated as read-only. If an object with
     * the given object_key is not served by this adapter this returns null.
     */
    public byte[] adapter_id( byte [] object_key )
    {
        byte [][] parts = m_server_manager.extract_cacheable_object_key( object_key );

        if ( parts == null || parts.length != m_aid_parts.length + 1 )
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
        return m_aid;
    }

    /**
     * Return the object id. This should should be a suffix of the object_key if
     * the object_key is cacheable, it should be stable with respect to a given
     * object_key and will be treated as read-only. If an object with
     * the given object_key is not served by this adapter this returns null.
     */
    public byte[] object_id( byte [] object_key )
    {
        byte [][] parts = m_server_manager.extract_cacheable_object_key( object_key );

        if ( parts == null || parts.length != m_aid_parts.length + 1 )
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
        return parts[ parts.length - 1 ];
    }

    /**
     * Returns a PolicyList containing the Polices set for the
     * requested PolicyTypes. If the specified sequence is empty, all
     * Policy overrides will be returned. If none of the
     * requested PolicyTypes are overridden an empty sequence is returned.
     */
    public org.omg.CORBA.Policy[] get_server_policies( int[] ts )
    {
        return m_policy_set.get_policy_overrides( ts );
    }

    /**
     * If this returns true then requests for the specified object_key
     * must be sent through the network. This will be true for example
     * when using the DSI. This should return true if the object_key is
     * unknown to the adapter.
     */
    public boolean forced_marshal( byte[] object_key )
        throws AdapterDestroyedException
    {
        if ( m_force_marshal )
        {
            return true;
        }
        try
        {
            org.omg.CORBA.portable.ServantObject so =
                   servant_preinvoke( object_key, "_non_existent", Servant.class, false );

            if ( so == null )
            {
                return true;
            }
            boolean ret = ( so.servant instanceof DynamicImplementation );

            servant_postinvoke( object_key, so );

            return ret;
        }
        catch ( org.omg.PortableInterceptor.ForwardRequest ex )
        {
            return false;
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            return false;
        }
    }

    /**
     * Preinvoke a local operation. A successfull call is always paired with a
     * call to servant_postinvoke.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * this throws an OBJECT_NOT_EXIST exception.
     *
     * If the target of the invocation is a dynamic servant or forced marshaling
     * is in place this will return null.
     */
    public ServantObject servant_preinvoke( byte[] object_key,
            String operation, Class expectedType )
        throws org.omg.PortableInterceptor.ForwardRequest, AdapterDestroyedException
    {
        return servant_preinvoke( object_key, operation, expectedType, true );
    }

    /**
     * Preinvoke a remote operation. A successfull call is always paired with a
     * call to servant_postinvoke.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * this throws an OBJECT_NOT_EXIST exception.
     *
     * If the target of the invocation is a dynamic servant or forced marshaling
     * is in place this will return null.
     */
    public ServantObject servant_preinvoke( byte[] object_key, String operation,
            Class expectedType, boolean isLocal )
        throws org.omg.PortableInterceptor.ForwardRequest, AdapterDestroyedException
    {
        synchronized ( m_sync_requests )
        {
            if ( m_destroyed )
            {
                throw new AdapterDestroyedException( this, m_aid );
            }
            if ( m_rejecting )
            {
                throw new org.omg.CORBA.OBJECT_NOT_EXIST( org.omg.CORBA.OMGVMCID.value | 2,
                       CompletionStatus.COMPLETED_NO );
            }
            ++m_active_requests;
        }

        DispatchState state = null;
        try
        {
            byte [] object_id = object_id( object_key );

            if ( object_id == null )
            {
                throw new org.omg.CORBA.OBJ_ADAPTER( 0, CompletionStatus.COMPLETED_NO );
            }
            if ( !m_non_retain )
            {
                synchronized ( m_aom_sync )
                {
                    AOMEntry entry = ( AOMEntry ) m_aom.get( id_key( object_id ) );

                    if ( entry != null )
                    {
                        if ( entry.getServant()._non_existent() )
                        {
                            throw new org.omg.CORBA.OBJECT_NOT_EXIST(
                                   org.omg.CORBA.OMGVMCID.value | 2,
                                   CompletionStatus.COMPLETED_NO );
                        }
                        if ( isLocal && javax.rmi.CORBA.Tie.class.isInstance( entry.getServant() ) )
                        {
                            state = new DispatchState( this,
                                    ( ( javax.rmi.CORBA.Tie ) entry.getServant() ).getTarget(),
                                    entry.getServant(), object_id, m_poa_current );
                        }
                        else
                        {
                            if ( !expectedType.isInstance( entry.getServant() ) )
                            {
                                if ( entry.getServant() instanceof DynamicImplementation )
                                {
                                    return null;
                                }
                                else
                                {
                                    throw new org.omg.CORBA.BAD_OPERATION(
                                           org.omg.CORBA.OMGVMCID.value | 5,
                                           CompletionStatus.COMPLETED_NO );
                                }
                            }

                            state = new DispatchState( this, entry, m_poa_current );
                        }
                    }
                    else if ( m_use_default )
                    {
                        if ( m_default_servant == null )
                        {
                            throw new org.omg.CORBA.OBJ_ADAPTER(
                                   org.omg.CORBA.OMGVMCID.value | 3,
                                   CompletionStatus.COMPLETED_NO );
                        }
                        if ( m_default_servant._non_existent() )
                        {
                            throw new org.omg.CORBA.OBJECT_NOT_EXIST(
                                   org.omg.CORBA.OMGVMCID.value | 2,
                                   CompletionStatus.COMPLETED_NO );
                        }
                        if ( isLocal && javax.rmi.CORBA.Tie.class.isInstance( m_default_servant ) )
                        {
                            state = new DispatchState( this,
                                   ( ( javax.rmi.CORBA.Tie ) m_default_servant ).getTarget(),
                                   m_default_servant, object_id, m_poa_current );
                        }
                        else
                        {
                            if ( !expectedType.isInstance( m_default_servant ) )
                            {
                                if ( m_default_servant instanceof DynamicImplementation )
                                {
                                    return null;
                                }
                                else
                                {
                                    throw new org.omg.CORBA.BAD_OPERATION(
                                           org.omg.CORBA.OMGVMCID.value | 5,
                                           CompletionStatus.COMPLETED_NO );
                                }
                            }

                            state = new DispatchState( this, m_default_servant, object_id,
                                    m_poa_current );
                        }
                    }
                    else if ( m_use_manager )
                    {
                        if ( m_servant_activator == null )
                        {
                            throw new org.omg.CORBA.OBJ_ADAPTER(
                                   org.omg.CORBA.OMGVMCID.value | 4,
                                   CompletionStatus.COMPLETED_NO );
                        }
                        Servant serv;
                        try
                        {
                            serv = m_servant_activator.incarnate( object_id, this );
                        }
                        catch ( final ForwardRequest ex )
                        {
                            throw ( org.omg.PortableInterceptor.ForwardRequest )
                                    ExceptionTool.initCause(
                                    new org.omg.PortableInterceptor.ForwardRequest(
                                    ex.forward_reference ), ex );
                        }

                        if ( serv == null )
                        {
                            throw new org.omg.CORBA.OBJ_ADAPTER(
                                    org.omg.CORBA.OMGVMCID.value | 3,
                                    CompletionStatus.COMPLETED_NO );
                        }
                        serv._set_delegate( m_delegate );

                        if ( !m_multiple_ids && m_aom.containsKey( new ServantKey( serv ) ) )
                        {
                            // the unequeness policy is broken (don't eterialize however)
                            throw new org.omg.CORBA.OBJ_ADAPTER( 0, CompletionStatus.COMPLETED_NO );
                        }

                        if ( serv._non_existent() )
                        {
                            // etherealize in this case, there doesn't seem to be much sense
                            // in putting the servant in the AOM if it claims it doesn't exist
                            boolean remaining = m_multiple_ids && m_aom.containsKey(
                                    new ServantKey( serv ) );
                            m_servant_activator.etherealize( object_id, this, serv,
                                    false, remaining );
                            throw new org.omg.CORBA.OBJ_ADAPTER( 0, CompletionStatus.COMPLETED_NO );
                        }

                        if ( !expectedType.isInstance( serv ) )
                        {
                            if ( serv instanceof DynamicImplementation )
                            {
                                // just return null, the next pass through will find nonlocal
                                return null;
                            }
                            else
                            {
                                // etherealize in this case, there doesn't seem to be much sense
                                // in putting the servant in the AOM.
                                boolean remaining = m_multiple_ids && m_aom.containsKey(
                                        new ServantKey( serv ) );
                                m_servant_activator.etherealize( object_id, this, serv,
                                        false, remaining );
                                throw new org.omg.CORBA.BAD_OPERATION(
                                        org.omg.CORBA.OMGVMCID.value | 5,
                                        CompletionStatus.COMPLETED_NO );
                            }
                        }
                        state = new DispatchState( this, create_aom_entry( serv, object_id ),
                                m_poa_current );
                    }
                    else
                    {
                        throw new org.omg.CORBA.OBJECT_NOT_EXIST(
                                org.omg.CORBA.OMGVMCID.value | 2, CompletionStatus.COMPLETED_NO );
                    }
                }
            }
            else if ( m_use_default )
            {
                if ( m_default_servant == null )
                {
                    throw new org.omg.CORBA.OBJ_ADAPTER( org.omg.CORBA.OMGVMCID.value | 3,
                            CompletionStatus.COMPLETED_NO );
                }
                if ( m_default_servant._non_existent() )
                {
                    throw new org.omg.CORBA.OBJ_ADAPTER( org.omg.CORBA.OMGVMCID.value | 3,
                            CompletionStatus.COMPLETED_NO );
                }
                if ( isLocal && javax.rmi.CORBA.Tie.class.isInstance( m_default_servant ) )
                {
                    state = new DispatchState( this, ( ( javax.rmi.CORBA.Tie )
                            m_default_servant ).getTarget(), m_default_servant, object_id,
                            m_poa_current );
                }
                else
                {
                    if ( !expectedType.isInstance( m_default_servant ) )
                    {
                        if ( m_default_servant instanceof DynamicImplementation )
                        {
                            return null;
                        }
                        else
                        {
                            throw new org.omg.CORBA.BAD_OPERATION(
                                org.omg.CORBA.OMGVMCID.value | 5, CompletionStatus.COMPLETED_NO );
                        }
                    }
                    state = new DispatchState( this, m_default_servant, object_id, m_poa_current );
                }
            }
            else if ( m_use_manager )
            {
                // call predispatch
                if ( m_servant_locator == null )
                {
                    throw new org.omg.CORBA.OBJ_ADAPTER( org.omg.CORBA.OMGVMCID.value | 4,
                            CompletionStatus.COMPLETED_NO );
                }
                Servant serv;
                org.omg.PortableServer.ServantLocatorPackage.CookieHolder cookie_holder =
                        new org.omg.PortableServer.ServantLocatorPackage.CookieHolder();
                try
                {
                    serv = m_servant_locator.preinvoke( object_id, this, operation, cookie_holder );
                }
                catch ( final ForwardRequest ex )
                {
                    throw ( org.omg.PortableInterceptor.ForwardRequest )
                            ExceptionTool.initCause(
                            new org.omg.PortableInterceptor.ForwardRequest(
                            ex.forward_reference ), ex );
                }

                if ( serv == null )
                {
                    throw new org.omg.CORBA.OBJ_ADAPTER( org.omg.CORBA.OMGVMCID.value | 3,
                            CompletionStatus.COMPLETED_NO );
                }
                serv._set_delegate( m_delegate );
                if ( serv._non_existent() )
                {
                    m_servant_locator.postinvoke( object_id, this, operation,
                            cookie_holder.value, serv );
                    throw new org.omg.CORBA.OBJ_ADAPTER( org.omg.CORBA.OMGVMCID.value | 3,
                            CompletionStatus.COMPLETED_NO );
                }

                if ( isLocal && javax.rmi.CORBA.Tie.class.isInstance( serv ) )
                {
                    state = new DispatchState( this, ( ( javax.rmi.CORBA.Tie ) serv ).getTarget(),
                            serv, object_id, m_poa_current );
                }
                else
                {
                    if ( !expectedType.isInstance( serv ) )
                    {
                        boolean isDynamic = serv instanceof DynamicImplementation;
                        m_servant_locator.postinvoke( object_id, this, operation,
                                cookie_holder.value, serv );

                        if ( isDynamic )
                        {
                            return null;
                        }
                        else
                        {
                            throw new org.omg.CORBA.BAD_OPERATION(
                                    org.omg.CORBA.OMGVMCID.value | 5,
                                    CompletionStatus.COMPLETED_NO );
                        }
                    }
                    state = new DispatchState( this, serv, object_id, m_poa_current );
                }
                state.setCookie( cookie_holder.value );
                state.setOperation( operation );
            }
        }
        finally
        {
            if ( state == null )
            {
                synchronized ( m_sync_requests )
                {
                    m_active_requests--;

                    if ( m_active_requests == 0 && m_rejecting )
                    {
                        m_sync_requests.notifyAll();
                    }
                }
            }
        }
        m_poa_current.push( state );

        return state;
    }

    /**
     * Close off a local operation. Always paired with a call to
     * servant_preinvoke
     */
    public void servant_postinvoke( byte[] object_key, ServantObject srvObject )
    {
        DispatchState state = null;
        try
        {
            state = m_poa_current.pop();
        }
        catch ( org.omg.PortableServer.CurrentPackage.NoContext ex )
        {
            // handled by following if statement
        }
        if ( srvObject != state )
        {
            org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                    "Postinvoke step does not match preinvoke step" );
        }

        if ( m_non_retain && m_use_manager )
        {
            // call postinvoke on the servant manager.
            if ( m_servant_locator == null )
            {
                throw new org.omg.CORBA.OBJ_ADAPTER( 0, CompletionStatus.COMPLETED_YES );
            }
            m_servant_locator.postinvoke( state.getObjectID(), this, state.getOperation(),
                    state.getCookie(), state.getServant() );
        }

        synchronized ( m_sync_requests )
        {
            m_active_requests--;

            if ( m_rejecting && m_active_requests == 0 )
            {
                complete_destroy();
            }
        }
    }

    /**
     * Respond to a local locate request. This returns true if the object
     * is located locally, false if the object is unknown and throws a forward
     * request for a location forward. This should not throw a system exception.
     */
    public boolean locate( byte[] object_key )
        throws org.omg.PortableInterceptor.ForwardRequest, AdapterDestroyedException
    {
        try
        {
            ServantObject target = servant_preinvoke( object_key, "_locate", Servant.class, false );
            servant_postinvoke( object_key, target );
            return true;
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            // return false
        }
        return false;
    }

    /**
     * is_a operation.
     *
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    public boolean is_a( byte[] object_key, String repository_id )
        throws org.omg.PortableInterceptor.ForwardRequest, AdapterDestroyedException
    {
        ServantObject target = servant_preinvoke( object_key, "_is_a", Servant.class, false );

        try
        {
            Servant serv = ( Servant ) target.servant;
            return serv._is_a( repository_id );
        }
        finally
        {
            servant_postinvoke( object_key, target );
        }
    }

    /**
     * get_interface_def operation.
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    public org.omg.CORBA.Object get_interface_def( byte[] object_key )
        throws org.omg.PortableInterceptor.ForwardRequest, AdapterDestroyedException
    {
        ServantObject target = servant_preinvoke( object_key, "_interface", Servant.class, false );

        try
        {
            Servant srv = ( Servant ) target.servant;
            return srv._get_interface_def();
        }
        finally
        {
            servant_postinvoke( object_key, target );
        }
    }

    /**
     * get_domain_manager operation.
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    public org.omg.CORBA.DomainManager[] get_domain_managers( byte[] object_key )
        throws org.omg.PortableInterceptor.ForwardRequest, AdapterDestroyedException
    {
        ServantObject target =
                servant_preinvoke( object_key, "_get_domain_managers", Servant.class, false );

        try
        {
            DispatchState state = ( DispatchState ) target;
            return get_domain_managers( state.getRepositoryID(), state.getObjectID() );
        }
        finally
        {
            servant_postinvoke( object_key, target );
        }
    }

    private org.omg.CORBA.DomainManager[] get_domain_managers( String repo_id, byte[] object_id )
    {
        if ( m_poa_domain_manager == null )
        {
            m_poa_domain_manager = m_root.create_poa_domain_manager( m_aid, m_persistent );
        }
        if ( !m_per_reference_domain_managers )
        {
            org.omg.CORBA.DomainManager [] ret = new org.omg.CORBA.DomainManager[ 1 ];
            ret[ 0 ] = m_poa_domain_manager;
            return ret;
        }

        // TODO: we will have some policy to find domain managers depending
        // on the repository id and the object_id.
        org.omg.CORBA.DomainManager [] frompol = new org.omg.CORBA.DomainManager[ 0 ];
        org.omg.CORBA.DomainManager [] ret = new org.omg.CORBA.DomainManager[ 1 + frompol.length ];
        ret[ 0 ] = m_poa_domain_manager;
        System.arraycopy( frompol, 0, ret, 1, frompol.length );
        return ret;
    }

    /**
     * get_componenent operation.
     * If the given object_key was not created by this adapter an
     * OBJ_ADAPTER exception is thrown, if the object does not exist
     * OBJECT_NOT_EXIST is thrown.
     */
    public org.omg.CORBA.Object get_component( byte[] object_key )
        throws org.omg.PortableInterceptor.ForwardRequest, AdapterDestroyedException
    {
        org.omg.CORBA.portable.ServantObject target = servant_preinvoke( object_key, "_component",
                Servant.class, false );
        try
        {
            return get_component( ( Servant ) target.servant, object_key );
        }
        finally
        {
            servant_postinvoke( object_key, target );
        }
    }

    private org.omg.CORBA.Object get_component( Servant servant, byte[] object_key )
    {
        // TODO: implement this.
        return null;
    }

    /**
     * Locate the servant object for a request. The returned object is
     * handed to the dispatch operation. This may throw a system exception or
     * respond with a forward request if one is indicated, in which case the
     * dispatch operation will not be called.
     *
     * @param req Out parameter holding repository ids of all available interfaces
     *            with the most derived interface appearing first.
     * @return The 'target' of the operation. This is simply passed to the
     *         dispatch operation and is not interpreted in any way.
     */
    public TargetInfo predispatch( org.openorb.orb.net.ServerRequest req )
        throws org.omg.PortableInterceptor.ForwardRequest, AdapterDestroyedException
    {
        byte [] object_key = req.object_key();
        org.omg.CORBA.portable.ServantObject target =
                servant_preinvoke( object_key, ( req.is_locate() ? "_locate" : req.operation() ),
                Servant.class, false );

        if ( target == null )
        {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST( 0, CompletionStatus.COMPLETED_NO );
        }
        try
        {
            m_poa_current.pop();
        }
        catch ( org.omg.PortableServer.CurrentPackage.NoContext ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Lost current object", ex );
            }
        }
        return ( TargetInfo ) target;
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
        DispatchState state = ( DispatchState ) target;
        synchronized ( state )
        {
            if ( state.isCanceled() )
            {
                return;
            }
            state.setWorkThread( Thread.currentThread() );
        }

        byte [] object_key = req.object_key();
        try
        {
            m_poa_current.push( ( DispatchState ) target );

            if ( req.is_locate() )
            {
                return;
            }
            org.omg.PortableServer.Servant servant = state.getServant();
            String operation = req.operation();

            if ( operation.charAt( 0 ) == '_' )
            {
                // we may have a system operation.
                if ( operation.equals( "_is_a" ) )
                {
                    String repo_id = req.argument_stream().read_string();
                    boolean ret = servant._is_a( repo_id );
                    req.createReply().write_boolean( ret );
                    return;
                }
                else if ( operation.equals( "_get_domain_managers" ) )
                {
                    req.argument_stream();
                    org.omg.CORBA.DomainManager [] ret = get_domain_managers(
                            state.getRepositoryID(), state.getObjectID() );
                    org.omg.CORBA.DomainManagersListHelper.write( req.createReply(), ret );
                    return;
                }
                else if ( operation.equals( "_interface" ) )
                {
                    req.argument_stream();
                    org.omg.CORBA.Object ret = servant._get_interface_def();
                    req.createReply().write_Object( ret );
                    return;
                }
                else if ( operation.equals( "_non_existent" )
                      || operation.equals( "_not_existent" ) )
                {
                    req.argument_stream();
                    req.createReply().write_boolean( false );
                    return;
                }
                else if ( operation.equals( "_component" ) )
                {
                    req.argument_stream();
                    org.omg.CORBA.Object ret = get_component( servant, object_key );
                    req.createReply().write_Object( ret );
                    return;
                }
            }

            if ( servant instanceof org.omg.CORBA.portable.InvokeHandler )
            {
                ( ( org.omg.CORBA.portable.InvokeHandler ) servant )._invoke(
                      operation, req.argument_stream(), req );
            }
            else if ( servant instanceof DynamicImplementation )
            {
                org.openorb.orb.core.dsi.ServerRequest dsr =
                      new org.openorb.orb.core.dsi.ServerRequest( req );
                ( ( DynamicImplementation ) servant ).invoke( dsr );

                if ( req.state() == org.openorb.orb.net.ServerRequest.STATE_PROCESSING )
                {
                    dsr.set_result( m_orb.create_any() );
                }
            }
        }
        finally
        {
            servant_postinvoke( object_key, state );
        }
    }

    /**
     * Cancel a dispatch. This may follow a predispatch or dispatch call to
     * indicate that the client no longer expects any reply from the request
     * and the server can stop expending effort towards completing it.
     */
    public void cancel_dispatch( org.openorb.orb.net.ServerRequest req, TargetInfo target )
    {
        DispatchState state = ( DispatchState ) target;

        synchronized ( state )
        {
            if ( state.isCanceled() )
            {
                return;
            }
            state.cancel();

            if ( state.getWorkThread() != null )
            {
                if ( m_interrupt_on_cancel )
                {
                    state.getWorkThread().interrupt();
                }
                return;
            }
        }

        m_poa_current.push( state );
        servant_postinvoke( req.object_key(), state );
    }

    /**
     * id operation. This is prefixed to object_ids to create object_keys
     *
     * @since CORBA 3.0
     */
    public byte [] id()
    {
        return m_aid;
    }

    /**
     * Create a new POA instance with the specified policies.
     *
     */
    public org.omg.PortableServer.POA create_POA( String adapter_name,
            POAManager a_POAManager,
            org.omg.CORBA.Policy[] policies )
        throws AdapterAlreadyExists, InvalidPolicy
    {
        if ( a_POAManager != null
              && !( a_POAManager instanceof AdapterManager ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( 0, CompletionStatus.COMPLETED_NO );
        }
        synchronized ( m_sync_requests )
        {
            if ( m_rejecting )
            {
                throw new org.omg.CORBA.OBJECT_NOT_EXIST();
            }
            POA child = ( POA ) m_child_poas.get( adapter_name );

            if ( child != null )
            {
                throw new AdapterAlreadyExists();
            }
            String [] child_name = new String[ m_poa_name.length + 1 ];

            System.arraycopy( m_poa_name, 0, child_name, 0, m_poa_name.length );

            child_name[ m_poa_name.length ] = adapter_name;

            child = new POA( m_orb, this, child_name,
                             ( AdapterManager ) a_POAManager,
                             policies );

            m_child_poas.put( adapter_name, child );

            if ( !m_in_create.contains( adapter_name ) )
            {
                m_server_manager.register_adapter( child.m_aid, child );
            }
            return child;
        }
    }

    /**
     * Find child POA with given adapter_name.
     *
     * @param adapter_name The name of the child poa.
     * @param activate_it If this is true and an AdapterActivator is registered
     *              then if the specified adapter does not exist it may be
     *              called to activate the adapter.
     */
    public org.omg.PortableServer.POA find_POA( String adapter_name, boolean activate_it )
        throws AdapterNonExistent
    {
        synchronized ( m_sync_requests )
        {
            if ( m_rejecting )
            {
                throw new org.omg.CORBA.OBJECT_NOT_EXIST();
            }
            POA child = ( POA ) m_child_poas.get( adapter_name );
            if ( child != null )
            {
                return child;
            }
            if ( !activate_it || m_adapter_activator == null )
            {
                throw new AdapterNonExistent();
            }
            try
            {
                m_in_create.add( adapter_name );
                if ( !m_adapter_activator.unknown_adapter( this, adapter_name ) )
                {
                    throw new AdapterNonExistent();
                }
            }
            catch ( final org.omg.CORBA.SystemException ex )
            {
                getLogger().error( "Problem adding the adapter.", ex );

                throw ExceptionTool.initCause( new org.omg.CORBA.OBJ_ADAPTER(
                        1, CompletionStatus.COMPLETED_MAYBE ), ex );
            }
            finally
            {
                m_in_create.remove( adapter_name );
            }
            child = ( POA ) m_child_poas.get( adapter_name );
            if ( child == null )
            {
                throw new AdapterNonExistent();
            }
            m_server_manager.register_adapter( child.m_aid, child );

            return child;
        }
    }

    /**
     * Destroy the POA. Incoming requests will be queued if this is
     * a persistant poa or rejected if this is a transient poa. If the parent
     * poa has an AdapterActivator registered with it that recreates this poa
     * then no requests will be lost between this call and the recreation of
     * the adapter.
     *
     * Care should be taken not to call destroy in the context of a request and
     * then call some object which results in a callback to an object served
     * by this POA or a deadlock will occour.
     */
    public void destroy( boolean etherealize_objects, boolean wait_for_completion )
    {
        if ( wait_for_completion )
        {
            try
            {
                m_poa_current.peek();
                throw new org.omg.CORBA.BAD_INV_ORDER();
            }
            catch ( org.omg.PortableServer.CurrentPackage.NoContext ex )
            {
                // This exception must occur to indicate that no further requests
                // are pending. If it does not occur a BAD_INV_ORDER exception
                // is thrown.
            }
        }

        POA [] child;
        synchronized ( m_sync_requests )
        {
            if ( m_rejecting )
            {
                return;
            }
            m_destroyed = true;
            m_rejecting = true;
            m_etherealize = etherealize_objects;
            child = new POA[ m_child_poas.size() ];
            m_child_poas.values().toArray( child );
        }

        for ( int i = 0; i < child.length; ++i )
        {
            child[ i ].destroy( etherealize_objects, false );
        }
        synchronized ( m_sync_requests )
        {
            if ( m_active_requests == 0 )
            {
                complete_destroy();
                return;
            }
            if ( wait_for_completion )
            {
                try
                {
                    while ( m_active_requests != 0 && m_child_poas.size() != 0 )
                    {
                        m_sync_requests.wait();
                    }
                }
                catch ( InterruptedException ex )
                {
                    // the enclosing request has been canceled
                }
            }
        }
    }

    private void complete_destroy()
    {
        if ( m_etherealize )
        {
            etherealize( true );
        }
        if ( m_parent != null )
        {
            synchronized ( m_parent.m_sync_requests )
            {
                m_parent.m_child_poas.remove( m_poa_name[ m_poa_name.length - 1 ] );
                m_server_manager.unregister_adapter( m_aid );
                m_parent.m_sync_requests.notifyAll();
                synchronized ( m_sync_requests )
                {
                    m_sync_requests.notifyAll();
                }
            }
        }
    }

    public ThreadPolicy create_thread_policy( ThreadPolicyValue value )
    {
        return s_policy_factory.create_thread_policy( value.value() );
    }

    public LifespanPolicy create_lifespan_policy( LifespanPolicyValue value )
    {
        return s_policy_factory.create_lifespan_policy( value.value() );
    }

    public IdUniquenessPolicy create_id_uniqueness_policy( IdUniquenessPolicyValue value )
    {
        return s_policy_factory.create_id_uniqueness_policy( value.value() );
    }

    public IdAssignmentPolicy create_id_assignment_policy( IdAssignmentPolicyValue value )
    {
        return s_policy_factory.create_id_assignment_policy( value.value() );
    }

    public ImplicitActivationPolicy create_implicit_activation_policy(
            ImplicitActivationPolicyValue value )
    {
        return s_policy_factory.create_implicit_activation_policy( value.value() );
    }

    public ServantRetentionPolicy create_servant_retention_policy(
            ServantRetentionPolicyValue value )
    {
        return s_policy_factory.create_servant_retention_policy( value.value() );
    }

    public RequestProcessingPolicy create_request_processing_policy(
            RequestProcessingPolicyValue value )
    {
        return s_policy_factory.create_request_processing_policy( value.value() );
    }

    public String the_name()
    {
        return m_poa_name[ m_poa_name.length - 1 ];
    }

    public org.omg.PortableServer.POA the_parent()
    {
        return m_parent;
    }

    public org.omg.PortableServer.POA[] the_children()
    {
        synchronized ( m_child_poas )
        {
            org.omg.PortableServer.POA[] ret =
                    new org.omg.PortableServer.POA[ m_child_poas.size() ];
            m_child_poas.values().toArray( ret );
            return ret;
        }
    }

    public POAManager the_POAManager()
    {
        return m_poa_manager;
    }

    public AdapterActivator the_activator()
    {
        return m_adapter_activator;
    }

    public void the_activator( AdapterActivator value )
    {
        m_adapter_activator = value;
    }

    public ServantManager get_servant_manager()
        throws WrongPolicy
    {
        if ( !m_use_manager )
        {
            throw new WrongPolicy();
        }
        if ( m_servant_activator != null )
        {
            return m_servant_activator;
        }
        return m_servant_locator;
    }

    public void set_servant_manager( ServantManager imgr )
        throws WrongPolicy
    {
        if ( !m_use_manager )
        {
            throw new WrongPolicy();
        }
        if ( imgr == null )
        {
            throw new org.omg.CORBA.OBJ_ADAPTER( "Servant manager is null",
                  org.omg.CORBA.OMGVMCID.value | 4, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( !( imgr instanceof org.omg.CORBA.LocalObject )
              && !( imgr instanceof Servant ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( "Servant manager must be local object" );
        }
        if ( m_non_retain )
        {
            if ( m_servant_locator != null )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( "Servant manager already set",
                      org.omg.CORBA.OMGVMCID.value | 6, CompletionStatus.COMPLETED_MAYBE );
            }
            if ( !( imgr instanceof ServantLocator ) )
            {
                throw new org.omg.CORBA.OBJ_ADAPTER(
                      "Servant manager support the ServantLocator interface",
                      org.omg.CORBA.OMGVMCID.value | 4, CompletionStatus.COMPLETED_MAYBE );
            }
            m_servant_locator = ( ServantLocator ) imgr;
        }
        else
        {
            if ( m_servant_activator != null )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( "Servant manager already set",
                        org.omg.CORBA.OMGVMCID.value | 6, CompletionStatus.COMPLETED_MAYBE );
            }
            if ( !( imgr instanceof ServantActivator ) )
            {
                throw new org.omg.CORBA.OBJ_ADAPTER(
                        "Servant manager support the ServantActivator interface",
                        org.omg.CORBA.OMGVMCID.value | 4, CompletionStatus.COMPLETED_MAYBE );
            }
            m_servant_activator = ( ServantActivator ) imgr;
        }
    }

    public Servant get_servant()
        throws NoServant, WrongPolicy
    {
        if ( !m_use_default )
        {
            throw new WrongPolicy();
        }
        if ( m_default_servant == null )
        {
            throw new NoServant();
        }
        return m_default_servant;
    }

    public void set_servant( Servant p_servant )
        throws WrongPolicy
    {
        if ( !m_use_default )
        {
            throw new WrongPolicy();
        }
        p_servant._set_delegate( m_delegate );

        m_default_servant = p_servant;
    }

    public byte[] activate_object( Servant p_servant )
        throws ServantAlreadyActive, WrongPolicy
    {
        if ( m_user_ids || m_non_retain )
        {
            throw new WrongPolicy();
        }
        byte [] id = next_system_id();
        synchronized ( m_aom_sync )
        {
            if ( !m_multiple_ids && m_aom.containsKey( new ServantKey( p_servant ) ) )
            {
                throw new ServantAlreadyActive();
            }
            create_aom_entry( p_servant, id );
        }
        return id;
    }

    public void activate_object_with_id( byte[] id, Servant p_servant )
        throws ServantAlreadyActive, ObjectAlreadyActive, WrongPolicy
    {
        if ( m_non_retain )
        {
            throw new WrongPolicy();
        }
        Object id_key = id_key( id );
        if ( !m_user_ids )
        {
            // ensure proper format
            if ( id_key == null )
            {
                throw new org.omg.CORBA.BAD_PARAM();
            }
            // make sure the id was generated in the past.
            long id_val;
            if ( m_persistent )
            {
                id_val = ( ( Number ) id_key ).longValue();
            }
            else
            {
                id_val = ( ( Number ) id_key ).intValue() & 0xFFFFFFFFL;
            }
            synchronized ( m_next_system_id_sync )
            {
                // for persistent poas m_first_system_id == 0
                if ( ( ( m_next_system_id >= m_first_system_id )
                      ? ( id_val < m_first_system_id || id_val >= m_next_system_id )
                      : ( id_val < m_first_system_id && id_val >= m_next_system_id ) ) )
                {
                    throw new org.omg.CORBA.BAD_PARAM();
                }
            }
        }
        synchronized ( m_aom_sync )
        {
            if ( m_aom.containsKey( id_key ) )
            {
                throw new ObjectAlreadyActive();
            }
            if ( !m_multiple_ids && m_aom.containsKey( new ServantKey( p_servant ) ) )
            {
                throw new ServantAlreadyActive();
            }
            create_aom_entry( p_servant, id );
        }
    }

    public void deactivate_object( byte[] oid )
        throws ObjectNotActive, WrongPolicy
    {
        if ( m_non_retain )
        {
            throw new WrongPolicy();
        }
        Object id_key = id_key( oid );
        if ( id_key == null )
        {
            throw new ObjectNotActive();
        }
        synchronized ( m_aom_sync )
        {
            AOMEntry entry = ( AOMEntry ) m_aom.remove( id_key );
            if ( entry == null )
            {
                throw new ObjectNotActive();
            }
            boolean remaining = false;

            if ( !m_multiple_ids )
            {
                m_aom.remove( new ServantKey( entry.getServant() ) );
            }
            else if ( m_use_manager )
            {
                ServantKey key = new ServantKey( entry.getServant() );
                IntHolder active = ( IntHolder ) m_aom.get( key );

                if ( --( active.value ) == 0 )
                {
                    m_aom.remove( key );
                }
                else
                {
                    remaining = true;
                }
            }

            if ( m_servant_activator != null )
            {
                m_servant_activator.etherealize( oid, this, entry.getServant(), false, remaining );
            }
        }
    }

    public org.omg.CORBA.Object create_reference( java.lang.String intf )
        throws WrongPolicy
    {
        if ( m_user_ids )
        {
            throw new WrongPolicy();
        }
        return create_reference( next_system_id(), intf );
    }

    public org.omg.CORBA.Object create_reference_with_id( byte[] oid, java.lang.String intf )
    {
        if ( !m_user_ids && id_key( oid ) == null )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 14,
                    CompletionStatus.COMPLETED_MAYBE );
        }
        return create_reference( oid, intf );
    }

    public byte[] servant_to_id( Servant p_servant )
        throws ServantNotActive, WrongPolicy
    {
        if ( m_use_default && m_default_servant == p_servant )
        {
            try
            {
                return m_poa_current.get_object_id();
            }
            catch ( final org.omg.PortableServer.CurrentPackage.NoContext ex )
            {
                getLogger().error( "No context available.", ex );
                throw ( ServantNotActive ) ExceptionTool.initCause(
                        new ServantNotActive(), ex );
            }
        }
        if ( m_non_retain )
        {
            throw new WrongPolicy();
        }
        synchronized ( m_aom_sync )
        {
            // lookup the aom
            if ( !m_multiple_ids )
            {
                AOMEntry entry = ( AOMEntry ) m_aom.get( new ServantKey( p_servant ) );
                if ( entry != null )
                {
                    return entry.getObjectId();
                }
            }

            // implicit activation.
            if ( m_implicit_activation )
            {
                byte [] id = next_system_id();

                create_aom_entry( p_servant, id );

                return id;
            }
            throw new WrongPolicy();
        }
    }

    public org.omg.CORBA.Object servant_to_reference( Servant p_servant )
        throws ServantNotActive, WrongPolicy
    {
        try
        {
            return id_to_reference( servant_to_id( p_servant ) );
        }
        catch ( final ObjectNotActive ex )
        {
            getLogger().error( "The servant is not active yet.", ex );

            throw ( ServantNotActive ) ExceptionTool.initCause(
                    new ServantNotActive(), ex );
        }
    }

    public Servant reference_to_servant( org.omg.CORBA.Object reference )
        throws ObjectNotActive, WrongPolicy
    {
        try
        {
            return id_to_servant( reference_to_id( reference ) );
        }
        catch ( final WrongAdapter ex )
        {
            getLogger().error( "Servant associated with wrong adapter.", ex );

            throw ( ObjectNotActive ) ExceptionTool.initCause(
                    new ObjectNotActive(), ex );
        }
    }

    public byte[] reference_to_id( org.omg.CORBA.Object reference )
        throws WrongAdapter, WrongPolicy
    {
        org.omg.IOP.IOR ior;
        try
        {
            ior = ( ( org.openorb.orb.core.Delegate )
                    ( ( org.omg.CORBA.portable.ObjectImpl ) reference )._get_delegate() ).ior();
        }
        catch ( final ClassCastException ex )
        {
            getLogger().error( "Casting to Delegate or to ObjectImpl failed.", ex );

            throw ( WrongAdapter ) ExceptionTool.initCause( new WrongAdapter(), ex );
        }
        catch ( final NullPointerException ex )
        {
            getLogger().error( "Either the reference or the return value of "
                  + "_get_delegate() was null.", ex );

            throw ( WrongAdapter ) ExceptionTool.initCause( new WrongAdapter(), ex );
        }

        // we use the client manager to get the bindings
        if ( m_client_manager == null )
        {
            m_client_manager = ( org.openorb.orb.net.ClientManager )
                    ( ( org.openorb.orb.core.ORB ) m_orb ).getFeature( "ClientCPCManager" );
            if ( m_client_manager == null )
            {
                // if the client manager is not available then we
                // throw a wrong policy exception
                throw new WrongPolicy();
            }
        }

        org.openorb.orb.net.ClientBinding [] bindings =
                m_client_manager.create_bindings( reference, ior );
        for ( int i = 0; i < bindings.length; ++i )
        {
            if ( bindings[ i ].getObjectAdapter() != this )
            {
                continue;
            }
            byte [] object_key = bindings[ i ].getAddress().getTargetAddress(
                    org.omg.GIOP.KeyAddr.value ).object_key();

            byte [] object_id = object_id( object_key );
            if ( object_id == null )
            {
                continue;
            }
            return object_id;
        }
        throw new WrongAdapter();
    }

    public Servant id_to_servant( byte[] oid )
        throws ObjectNotActive, WrongPolicy
    {
        if ( !m_non_retain )
        {
            Object id_key = id_key( oid );
            if ( id_key != null )
            {
                synchronized ( m_aom_sync )
                {
                    AOMEntry entry = ( AOMEntry ) m_aom.get( id_key );

                    if ( entry != null )
                    {
                        return entry.getServant();
                    }
                }
            }

            if ( m_default_servant != null )
            {
                return m_default_servant;
            }
            throw new ObjectNotActive();
        }
        if ( m_default_servant != null )
        {
            return m_default_servant;
        }
        throw new WrongPolicy();
    }

    public org.omg.CORBA.Object id_to_reference( byte[] oid )
        throws ObjectNotActive, WrongPolicy
    {
        if ( m_non_retain )
        {
            throw new WrongPolicy();
        }
        Object id_key = id_key( oid );
        if ( id_key == null )
        {
            throw new ObjectNotActive();
        }
        String repo_id;
        synchronized ( m_aom_sync )
        {
            AOMEntry entry = ( AOMEntry ) m_aom.get( id_key );
            if ( entry == null )
            {
                throw new ObjectNotActive();
            }
            repo_id = entry.getServant()._all_interfaces( this, oid ) [ 0 ];
        }
        // this creates a fresh, unbound reference every time it's called.
        return create_reference( oid, repo_id );
    }

    /**
     * AOM sync must be held, all checks must have been done.
     */
    private AOMEntry create_aom_entry( Servant p_servant, byte [] id )
    {
        AOMEntry entry = new AOMEntry( id, p_servant );
        m_aom.put( id_key( id ), entry );
        if ( !m_multiple_ids )
        {
            m_aom.put( new ServantKey( p_servant ), entry );
        }
        else if ( m_use_manager )
        {
            ServantKey key = new ServantKey( p_servant );
            IntHolder active = ( IntHolder ) m_aom.get( key );

            if ( active == null )
            {
                m_aom.put( key, new IntHolder( 1 ) );
            }
            else
            {
                ++( active.value );
            }
        }
        p_servant._set_delegate( m_delegate );
        return entry;
    }

    protected org.omg.CORBA.Object create_reference( byte[] oid, java.lang.String intf )
    {
        byte [][] parts = new byte[ m_aid_parts.length + 1 ][];
        System.arraycopy( m_aid_parts, 0, parts, 0, m_aid_parts.length );
        parts[ parts.length - 1 ] = oid;

        byte [] object_key = m_server_manager.create_cacheable_object_key( m_persistent, parts );
        org.openorb.orb.pi.ComponentSet comp_set;
        if ( m_per_reference_domain_managers )
        {
            org.omg.CORBA.DomainManager [] domain_managers = get_domain_managers( intf, oid );
            comp_set = new org.openorb.orb.pi.ComponentSet( m_server_manager.orb(),
                    m_policy_set, domain_managers );
            comp_set.interception_point();
        }
        else
        {
            if ( m_poa_comp_set == null )
            {
                org.omg.CORBA.DomainManager [] domain_managers = get_domain_managers( intf, oid );
                m_poa_comp_set = new org.openorb.orb.pi.ComponentSet( m_server_manager.orb(),
                        m_policy_set, domain_managers );
                m_poa_comp_set.interception_point();
            }
            comp_set = m_poa_comp_set;
        }
        org.omg.IOP.IOR ior = IORUtil.construct_ior( intf, object_key, comp_set,
                m_server_manager.get_protocol_ids(), m_server_manager.orb() );
        return new org.openorb.orb.core.ObjectStub( m_orb, ior );
    }

    private byte [] next_system_id()
    {
        long next;
        synchronized ( m_next_system_id_sync )
        {
            if ( !m_persistent )
            {
                next = m_next_system_id;
                m_next_system_id = ( m_next_system_id + 1L ) & 0xFFFFFFFFL;
                if ( m_next_system_id == m_first_system_id )
                {
                    m_first_system_id = m_next_system_id;
                }
            }
            else
            {
                next = m_next_system_id++;
            }
        }

        byte [] id;
        if ( m_persistent )
        {
            id = new byte[ 8 ];
            id[ 0 ] = ( byte ) ( next >>> 56L );
            id[ 1 ] = ( byte ) ( next >>> 48L );
            id[ 2 ] = ( byte ) ( next >>> 40L );
            id[ 3 ] = ( byte ) ( next >>> 32L );
            id[ 4 ] = ( byte ) ( next >>> 24L );
            id[ 5 ] = ( byte ) ( next >>> 16L );
            id[ 6 ] = ( byte ) ( next >>>  8L );
            id[ 7 ] = ( byte ) next;
        }
        else
        {
            id = new byte[ 4 ];
            id[ 0 ] = ( byte ) ( next >>> 24L );
            id[ 1 ] = ( byte ) ( next >>> 16L );
            id[ 2 ] = ( byte ) ( next >>>  8L );
            id[ 3 ] = ( byte ) next;
        }
        return id;
    }

    private Object id_key( byte [] id )
    {
        if ( m_user_ids )
        {
            return new UserIdKey( id );
        }
        if ( m_persistent )
        {
            if ( id.length != 8 )
            {
                return null;
            }
            return NumberCache.getLong( ( ( id[ 0 ] & 0xFFL ) << 56L )
                  |   ( ( id[ 1 ] & 0xFFL ) << 48L )
                  |   ( ( id[ 2 ] & 0xFFL ) << 40L )
                  |   ( ( id[ 3 ] & 0xFFL ) << 32L )
                  |   ( ( id[ 4 ] & 0xFFL ) << 24L )
                  |   ( ( id[ 5 ] & 0xFFL ) << 16L )
                  | ( ( id[ 6 ] & 0xFFL ) <<  8L )
                  |   ( id[ 7 ] & 0xFFL ) );
        }

        if ( id.length != 4 )
        {
            return null;
        }
        Integer ret = NumberCache.getInteger( ( ( id[ 0 ] & 0xFF ) << 24 )
              | ( ( id[ 1 ] & 0xFF ) << 16 )
              | ( ( id[ 2 ] & 0xFF ) << 8 )
              |   ( id[ 3 ] & 0xFF ) );

        return ret;
    }

    private static class UserIdKey
    {
        private int m_hash;
        private byte [] m_id;

        UserIdKey( byte [] id )
        {
            m_id = id;
            for ( int i = 0; i < m_id.length; ++i )
            {
                m_hash = 31 * m_hash + m_id[ i ];
            }
        }

        public int hashCode()
        {
            return m_hash;
        }

        public boolean equals( Object obj )
        {
            if ( obj == this )
            {
                return true;
            }
            UserIdKey uk2;
            try
            {
                uk2 = ( ( UserIdKey ) obj );
            }
            catch ( ClassCastException ex )
            {
                return false;
            }

            if ( m_hash != uk2.m_hash )
            {
                return false;
            }
            return Arrays.equals( m_id, uk2.m_id );
        }
    }

    private static class ServantKey
    {
        private Servant m_target;

        ServantKey( Servant target )
        {
            m_target = target;
        }

        public int hashCode()
        {
            return System.identityHashCode( m_target );
        }

        public boolean equals( Object obj )
        {
            try
            {
                return m_target == ( ( ServantKey ) obj ).m_target;
            }
            catch ( ClassCastException ex )
            {
                return false;
            }
        }
    }

    /**
     * Provide component with a logger.
     *
     * @param logger the logger
     */
    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

    protected Logger getLogger()
    {
        return m_logger;
    }
}

