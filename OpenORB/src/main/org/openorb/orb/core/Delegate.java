/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.Context;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.DomainManagersListHelper;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.InterfaceDef;
import org.omg.CORBA.InterfaceDefHelper;
import org.omg.CORBA.InvalidPolicies;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.NO_RESPONSE;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.OBJ_ADAPTER;
import org.omg.CORBA.OMGVMCID;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyCurrent;
import org.omg.CORBA.PolicyCurrentHelper;
import org.omg.CORBA.PolicyListHolder;
import org.omg.CORBA.PolicyManager;
import org.omg.CORBA.PolicyManagerOperations;
import org.omg.CORBA.REBIND;
import org.omg.CORBA.SetOverrideType;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TIMEOUT;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.UNKNOWN;

import org.omg.CORBA.ORBPackage.InvalidName;

import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ServantObject;

import org.omg.GIOP.IORAddressingInfo;
import org.omg.GIOP.ReferenceAddr;

import org.omg.IOP.IOR;
import org.omg.IOP.TaggedProfile;

import org.omg.Messaging.REBIND_POLICY_TYPE;
import org.omg.Messaging.RELATIVE_RT_TIMEOUT_POLICY_TYPE;
import org.omg.Messaging.RelativeRoundtripTimeoutPolicy;

import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.omg.PortableInterceptor.TRANSPORT_RETRY;
import org.omg.PortableInterceptor.USER_EXCEPTION;

import org.openorb.orb.core.dii.Environment;

import org.openorb.orb.pi.CurrentImpl;

import org.openorb.orb.iiop.IIOPMinorCodes;

import org.openorb.util.HexPrintStream;

import org.openorb.orb.net.Address;
import org.openorb.orb.net.ClientBinding;
import org.openorb.orb.net.ClientChannel;
import org.openorb.orb.net.ClientManager;
import org.openorb.orb.net.ClientRequest;

import org.openorb.orb.policy.FORCE_MARSHAL_POLICY_ID;
import org.openorb.orb.policy.ForceMarshalPolicy;
import org.openorb.orb.policy.ORBPolicyManagerImpl;
import org.openorb.orb.policy.PolicyReconciler;
import org.openorb.orb.policy.PolicySetManager;

import org.openorb.orb.util.Trace;

import org.openorb.util.ExceptionTool;
import org.openorb.util.RepoIDHelper;

/**
 * This class constitutes the top layer in the object request sequence. It
 * manages selecting between alternative paths for invoking requests and
 * manages the invocation sequence, in concert with the server stubs.
 *
 * @author Chris Wood
 * @author Richard G Clark
 */
public class Delegate
    extends org.omg.CORBA_2_3.portable.Delegate
{
    /**
     * Reference to the controlling orb.
     */
    private final ORB m_orb;

    /**
     * IOR of the object. This is used in comparisons and hashing.
     */
    private final IOR m_ior;

    /**
     * Logger used for this delegate
     */
    private final Logger m_logger;

    /**
     * Policy override set. If no overrides are set this is null
     */
    private final PolicyManagerOperations m_policyOver;

    /**
     * Reference to the policy Current.
     */
    private final PolicyCurrent m_policyCurr;

    /**
     * Reference to policy reconciler.
     */
    private final PolicyReconciler m_orbReconciler;

    /**
     * Reference to the orb policy manager.
     */
    private final PolicyManager m_orbPolicyManager;

    /**
     * Reference to the policy set manager.
     */
    private final PolicySetManager m_policySetManager;

    /**
     * Reference to the portable interceptor current implementation
     * note that out PI current has some extra operations.
     */
    private final CurrentImpl m_piCurrent;

    /**
     * Reference to the client manager.
     */
    private ClientManager m_clientManager;

    /**
     * hash code. The object is always hashed on the original IOR.
     * Not protected by any synchronization but all calls to hash
     * will write the same primitive value. Note that this might
     * cause multiple hash code calculations.
     */
    private long m_hash = -1;

    /**
     * Synchronization object for the bindings.
     */
    private final Object m_bindingSync = new byte[0];

    /**
     * IOR as written to input/output stream. This can change as a
     * result of a perminent redirect.
     * Modified when synchronized to m_bindingSync
     */
    private IOR m_effectiveIor;

    /**
     * Available bindings. Set of ClientBinding. This may grow over time if
     * redirections occour. Must synchronize on m_bindingSync to access.
     * Modified when synchronized to m_bindingSync.
     * <p>
     * The reason why a Set has been chosen over e.g. a List is that the same
     * endpoints should not remain in the list as different entries.
     * The {@link org.openorb.orb.net.ClientBinding#equals(java.lang.Object)} is
     * responsible for deciding whether an object is already in the set or not.
     */
    private final Set m_bindings = new HashSet();

    /**
     * Binding list version. Each time the binding list is updated due to a
     * redirection this value is incremented. Must synchronize on m_bindingSync
     * to access.
     * Modified when synchronized to m_bindingSync
     */
    private int m_bindingVersion = -1;

    /**
     * The 'selected binding'. This is the binding that new invocations will
     * use. It is set when an invocation returns successfully and unset when
     * the channel closes or an invocation on it fails.
     * Modified when synchronized to m_bindingSync
     */
    private ClientBinding m_selectedBinding;

    /**
     * The max client policy cache size.
     */
    private static final int MAX_CLIENT_POLICY_CACHE_SIZE = 4;

    /**
     * A flag whether the policy cache is activated or not.
     * This feature caches policy objects and speeds up the call
     * to get_client_policy(). The whole policy reconciliation is
     * a very costly process but necessary for compliant policy
     * handling. In certain environments it makes no sense to
     * mess around with ORB level policies, like e.g. EJB containers.
     * In these scenarios it should be allowed to turn on the
     * performance optimization by caching policy objects.
     */
    private boolean m_policy_cache_enabled = false;

    /**
     * The actual client policy cache size.
     */
    private int m_policy_cache_size = 0;

    /**
     * The policy type cache.
     */
    private final int[] m_policy_types = new int[ MAX_CLIENT_POLICY_CACHE_SIZE ];

    /**
     * The policy cache.
     */
    private final Policy[] m_policies = new Policy[ MAX_CLIENT_POLICY_CACHE_SIZE ];

    /**
     * Construct new delegate from orb and IOR.
     *
     * @param orb the client orb.
     * @param ior the target IOR.
     */
    public Delegate( final org.omg.CORBA.ORB orb, final IOR ior )
    {
        this( orb, ior, null );
    }

    /**
     * This constructor can be used from a derived class if it is clear
     * in the local optimized case that the object will not be exported.
     * In this particular case most of the initialization can be avoided.
     */
    protected Delegate( final org.omg.CORBA.ORB orb )
    {
        m_orb = orb;
        m_ior = null;
        m_policyOver = null;
        m_policyCurr = null;
        m_orbReconciler = null;
        m_orbPolicyManager = null;
        m_policySetManager = null;
        m_piCurrent = null;
        m_logger = null;
    }

    /**
     * Construct new delegate from orb and IOR.
     *
     * @param orb the client orb.
     * @param ior the target IOR.
     * @param policyOverides the policy overides for this delegate
     */
    protected Delegate( final org.omg.CORBA.ORB orb, final IOR ior,
          final PolicyManagerOperations policyOverides )
    {
        if ( ( ior == null ) || ( orb == null ) )
        {
            throw new NullPointerException();
        }
        m_ior = ior;
        m_effectiveIor = ior;
        m_orb = orb;
        m_policyOver = policyOverides;
        m_logger = ( ( ORBSingleton ) m_orb ).getLogger();
        m_piCurrent = ( ( org.openorb.orb.core.ORB ) m_orb ).getPICurrent();
        // load ClientCPCManager
        m_clientManager = ( ClientManager )
              ( ( org.openorb.orb.core.ORB ) m_orb ).getFeature( "ClientCPCManager" );
        if ( null == m_clientManager )
        {
            throw Trace.signalIllegalCondition( m_logger, "ClientCPCManager unavailable" );
        }
        // load PolicyReconciler
        m_orbReconciler = ( PolicyReconciler )
              ( ( org.openorb.orb.core.ORB ) m_orb ).getFeature( "PolicyReconciler" );
        if ( null == m_orbReconciler )
        {
            m_logger.warn( "PolicyReconciler unavailable" );
        }
        // load PolicySetManager
        m_policySetManager = ( PolicySetManager )
              ( ( org.openorb.orb.core.ORB ) m_orb ).getFeature( "PolicySetManager" );
        if ( null == m_policySetManager )
        {
            m_logger.warn( "PolicySetManager unavailable" );
        }
        // load PolicyCurrent
        PolicyCurrent policyCurrent = null;
        try
        {
            policyCurrent = PolicyCurrentHelper.narrow(
                  m_orb.resolve_initial_references( "PolicyCurrent" ) );
        }
        catch ( final InvalidName e )
        {
            m_logger.warn( "Unable to resolve PolicyCurrent.", e );
        }
        m_policyCurr = policyCurrent;
        // load ORBPolicyManager
        PolicyManager policyManager = null;
        try
        {
            policyManager = ( ORBPolicyManagerImpl )
                  m_orb.resolve_initial_references( "ORBPolicyManager" );
        }
        catch ( final InvalidName e )
        {
            m_logger.warn( "Unable to resolve ORBPolicyManager.", e );
        }
        m_orbPolicyManager = policyManager;
        // check the value for the policy cache
        m_policy_cache_enabled = ( ( org.openorb.orb.core.ORB ) m_orb
                ).getLoader().getBooleanProperty( "openorb.client.enable_policy_cache", false );
    }

    /**
     * Return the logger for this instance.
     *
     * @return The logger instance.
     */
    private Logger getLogger()
    {
        return m_logger;
    }

    private CurrentImpl getPICurrent()
    {
        return m_piCurrent;
    }

    /**
     * Return the ORB with which this object is associated.
     *
     * @return The ORB of the object.
     */
    protected ORB _getORB()
    {
        return m_orb;
    }

    /**
     * Return the IOR of the object.
     *
     * @return The IOR of the object.
     */
    protected IOR _getIOR()
    {
        return m_ior;
    }

    /**
     * Return the value for the flag whether the policy cache is enabled or not.
     *
     * @return The value of the flag.
     */
    public boolean getPolicyCacheEnabled()
    {
        return m_policy_cache_enabled;
    }

    /**
     * Return the interface for the PolicyManager object.
     *
     * @return The PolicyManager interface.
     */
    protected PolicyManagerOperations _getPolicyManagerOperations()
    {
        return m_policyOver;
    }

    /**
     * Set the value for the flag whether the policy cache is enabled or not.
     *
     * @param policy_cache_enabled The new value for the flag.
     */
    public void setPolicyCacheEnabled( boolean policy_cache_enabled )
    {
        m_policy_cache_enabled = policy_cache_enabled;
    }

    // Public functions from org.omg.CORBA.portable.Delegate

    /**
     * Return the object interface definition.
     * @deprecated Deprecated by CORBA 2.3
     */
    public InterfaceDef get_interface( final org.omg.CORBA.Object self )
    {
        return InterfaceDefHelper.narrow( get_interface_def( self ) );
    }

    /**
     * Creates the standard form of <code>UNKNOWN</code> that should be
     * thrown when an unexpected <code>ApplicationException</code> is
     * recieved.
     *
     * @param e the recieved <code>ApplicationException</code>
     *
     * @return an <code>UNKNOWN</code> exception.
     */
    private UNKNOWN createUnexpectedException( final ApplicationException e )
    {
        return ( UNKNOWN ) ExceptionTool.initCause( new UNKNOWN( "Unexpected User Exception: "
              + e.getId() + " (" + e + ")", OMGVMCID.value | 1,
              CompletionStatus.COMPLETED_YES ), e );
    }

    /**
     * Return the object interface definition.
     */
    public org.omg.CORBA.Object get_interface_def( final org.omg.CORBA.Object self )
    {
        for ( ;; )
        {
            final RequestState state = begin_invocation( self, false );
            if ( state.getBinding().getObjectAdapter() == null )
            {
                try
                {
                    InputStream inStream = null;
                    try
                    {
                        final OutputStream outStream = request( self, "_interface", true );
                        inStream = invoke( self, outStream );
                        return inStream.read_Object();
                    }
                    finally
                    {
                        releaseReply( self, inStream );
                    }
                }
                catch ( final RemarshalException e )
                {
                    continue;
                }
                catch ( final ApplicationException e )
                {
                    getLogger().error( "Application exception", e );
                    throw createUnexpectedException( e );
                }
            }
            else
            {
                boolean failed = true;
                state.incrementLocalLevel();
                try
                {
                    final org.omg.CORBA.Object ret = state.getBinding().get_interface_def();
                    failed = false;
                    return ret;
                }
                catch ( final SystemException e )
                {
                    state.receiveSystemException( self, e );
                    failed = false;
                    return null;
                }
                catch ( final ForwardRequest e )
                {
                    final IOR fwd = ( ( Delegate )
                          ( ( ObjectImpl ) e.forward )._get_delegate() ).ior();
                    state.receiveRedirect( self, fwd, false );
                    failed = false;
                    return null;
                }
                finally
                {
                    state.completeInvocation( !failed );
                }
            }
        }
    }

    /**
     * Return the object's component. This is currently unused.
     */
    public org.omg.CORBA.Object get_component( final org.omg.CORBA.Object self )
    {
        for ( ;; )
        {
            final RequestState state = begin_invocation( self, false );
            if ( state.getBinding().getObjectAdapter() == null )
            {
                try
                {
                    InputStream inStream = null;
                    try
                    {
                        final OutputStream outStream = request( self, "_component", true );
                        inStream = invoke( self, outStream );
                        return inStream.read_Object();
                    }
                    finally
                    {
                        releaseReply( self, inStream );
                    }
                }
                catch ( final RemarshalException e )
                {
                    continue;
                }
                catch ( final ApplicationException e )
                {
                    getLogger().error( "Application exception:", e );
                    throw createUnexpectedException( e );
                }
            }
            else
            {
                boolean failed = true;
                state.incrementLocalLevel();
                try
                {
                    final org.omg.CORBA.Object ret =
                            state.getBinding().get_component();
                    failed = false;
                    return ret;
                }
                catch ( final SystemException e )
                {
                    state.receiveSystemException( self, e );
                    failed = false;
                    return null;
                }
                catch ( final ForwardRequest e )
                {
                    final IOR fwd = ( ( Delegate )
                          ( ( ObjectImpl ) e.forward )._get_delegate() ).ior();
                    state.receiveRedirect( self, fwd, false );
                    failed = false;
                    return null;
                }
                finally
                {
                    state.completeInvocation( !failed );
                }
            }
        }
    }

    /**
     * This operation is used to test if an object implements an
     * interface. This
     * may result in invoking a remote operation.
     */
    public boolean is_a( final org.omg.CORBA.Object self, final String id )
    {
        final Object test = RepoIDHelper.createIsATest( id );
        if ( test.equals( "IDL:omg.org/CORBA/Object:1.0" ) )
        {
            return true;
        }
        // use the original ior
        if ( test.equals( _getIOR().type_id ) )
        {
            return true;
        }
        final String[] ids = ( ( ObjectImpl ) self )._ids();
        for ( int i = 0; i < ids.length; i++ )
        {
            if ( test.equals( ids[i] ) )
            {
                return true;
            }
        }
        for ( ;; )
        {
            final RequestState state = begin_invocation( self, false );
            if ( state.getBinding().getObjectAdapter() == null )
            {
                try
                {
                    InputStream inStream = null;
                    try
                    {
                        final OutputStream outStream = request( self, "_is_a", true );
                        outStream.write_string( id );
                        inStream = invoke( self, outStream );
                        return inStream.read_boolean();
                    }
                    finally
                    {
                        releaseReply( self, inStream );
                    }
                }
                catch ( final RemarshalException e )
                {
                    continue;
                }
                catch ( final ApplicationException e )
                {
                    getLogger().error( "Application exception:", e );
                    throw createUnexpectedException( e );
                }
            }
            else
            {
                boolean failed = true;
                state.incrementLocalLevel();
                try
                {
                    boolean ret = state.getBinding().is_a( id );
                    failed = false;
                    return ret;
                }
                catch ( final SystemException e )
                {
                    state.receiveSystemException( self, e );
                    failed = false;
                    return false;
                }
                catch ( final ForwardRequest e )
                {
                    IOR fwd = ( ( Delegate ) ( ( ObjectImpl ) e.forward )._get_delegate() ).ior();
                    state.receiveRedirect( self, fwd, false );
                    failed = false;
                    return false;
                }
                finally
                {
                    state.completeInvocation( !failed );
                }
            }
        }
    }

    /**
     * Find the domain manager list associated to this object.
     *
     * @return the list of domain managers.
     */
    public DomainManager[] get_domain_managers( final org.omg.CORBA.Object self )
    {
        return get_domain_managers_and_policies( self, null, null );
    }

    /**
     * This retrieves the effective profile policy when the
     * invocation succeeds.
     */
    private DomainManager[] get_domain_managers_and_policies(
          final org.omg.CORBA.Object self, final int[] policy_types,
          final PolicyListHolder policies )
    {
        for ( ;; )
        {
            final RequestState state = begin_invocation( self, false );
            if ( state.getBinding().getObjectAdapter() == null )
            {
                try
                {
                    InputStream inStream = null;
                    try
                    {
                        final OutputStream outStream =
                              request( self, "_get_domain_managers", true );
                        inStream = invoke( self, outStream );
                        DomainManager[] ret = DomainManagersListHelper.read( inStream );
                        if ( policies != null )
                        {
                            policies.value =
                                  state.getBinding().getAddress().get_target_policies(
                                  policy_types );
                        }
                        return ret;
                    }
                    finally
                    {
                        releaseReply( self, inStream );
                    }
                }
                catch ( final RemarshalException e )
                {
                    continue;
                }
                catch ( final ApplicationException e )
                {
                    getLogger().error( "Application exception:", e );
                    throw createUnexpectedException( e );
                }
            }
            else
            {
                boolean failed = true;
                state.incrementLocalLevel();
                try
                {
                    final DomainManager[] ret = state.getBinding().get_domain_managers();
                    failed = false;
                    return ret;
                }
                catch ( final SystemException e )
                {
                    state.receiveSystemException( self, e );
                    failed = false;
                    return null;
                }
                catch ( final ForwardRequest e )
                {
                    final IOR fwd = ( ( Delegate )
                          ( ( ObjectImpl ) e.forward )._get_delegate() ).ior();
                    state.receiveRedirect( self, fwd, false );
                    failed = false;
                    return null;
                }
                finally
                {
                    state.completeInvocation( !failed );
                }
            }
        }
    }

    /**
     * Check if an object exists
     */
    public boolean non_existent( final org.omg.CORBA.Object self )
    {
        final RequestState state = begin_invocation( self, false );
        return state.locateAndGetPolicies( self, null );
    }

    /**
     * Dulicate a reference. This simply returns the same delegate.
     */
    public org.omg.CORBA.Object duplicate( final org.omg.CORBA.Object self )
    {
        return self;
    }

    /**
     * Remove a reference. This is an empty operation.
     */
    public void release( final org.omg.CORBA.Object self )
    {
    }

    /**
     * Check if two object refereces are equivalent. Two
     * references are considered
     * equivalent if their IORs are identical.
     */
    public boolean is_equivalent( final org.omg.CORBA.Object obj1,
            final org.omg.CORBA.Object obj2 )
    {
        if ( obj1 == obj2 )
        {
            return true;
        }
        final org.omg.CORBA.portable.Delegate portableDelegate2 =
              ( ( ObjectImpl ) obj2 )._get_delegate();
        if ( !( portableDelegate2 instanceof Delegate ) )
        {
            return false;
        }
        final Delegate delegate2 = ( Delegate ) portableDelegate2;
        if ( this == delegate2 )
        {
            return true;
        }
        // check the IORs for equivalance.
        final TaggedProfile[] profiles1 = _getIOR().profiles;
        final TaggedProfile[] profiles2 = delegate2._getIOR().profiles;
        if ( profiles2.length != profiles2.length )
        {
            return false;
        }
        for ( int i = 0; i < profiles1.length; i++ )
        {
            final TaggedProfile profile1 = profiles1[ i ];
            final TaggedProfile profile2 = profiles2[ i ];
            if ( profile1.tag != profile2.tag
                  || !Arrays.equals( profile1.profile_data, profile2.profile_data ) )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Use is_equivalent definition of equals so objects can be stored in
     * hash tables.
     */
    public boolean equals( final org.omg.CORBA.Object self, final Object obj )
    {
        if ( !( obj instanceof org.omg.CORBA.Object ) )
        {
            return false;
        }
        return is_equivalent( self, ( org.omg.CORBA.Object ) obj );
    }

    /**
     * Return an hashcode for an object (with a max value)
     * Note that this hash code will not change even if the object is
     * redirected.
     */
    public int hash( final org.omg.CORBA.Object self, final int max )
    {
        if ( m_hash == -1 )
        {
            // always use the original ior for doing a hash.
            m_hash = 0;
            final TaggedProfile[] profiles = _getIOR().profiles;
            for ( int i = 0; i < profiles.length; i++ )
            {
                final TaggedProfile profile = profiles[ i ];
                final byte[] profileData = profile.profile_data;
                for ( int j = 0; j < profileData.length; j++ )
                {
                    m_hash = 31L * m_hash + ( long ) ( profileData[ j ] );
                }
                m_hash = 31L * m_hash + ( long ) ( profile.tag );
            }
        }
        return ( ( int ) ( m_hash % ( long ) ( max + 1 ) ) ) & Integer.MAX_VALUE;
    }

    /**
     * Use the IOR hash code so that objects can be used in hash tables.
     */
    public int hashCode( final org.omg.CORBA.Object self )
    {
        return hash( self, Integer.MAX_VALUE );
    }

    /**
     * Create a dynamic request
     */
    public org.omg.CORBA.Request create_request( final org.omg.CORBA.Object self,
            final Context ctx, final String operation, final NVList arg_list,
            final NamedValue result )
    {
        return new org.openorb.orb.core.dii.Request( self, operation,
                arg_list, result, new Environment(),
                new org.openorb.orb.core.dii.ExceptionList(),
                new org.openorb.orb.core.dii.ContextList(), m_orb );
    }

    /**
     * Create a dynamic request
     */
    public org.omg.CORBA.Request create_request( final org.omg.CORBA.Object self,
          final Context ctx, final String operation, final NVList arg_list,
          final NamedValue result, final org.omg.CORBA.ExceptionList excepts,
          final org.omg.CORBA.ContextList contexts )
    {
        return new org.openorb.orb.core.dii.Request( self, operation, arg_list,
                result, new Environment(), excepts, contexts, m_orb );
    }

    /**
     * Create a dynamic request
     */
    public org.omg.CORBA.Request request( final org.omg.CORBA.Object self,
          final String operation )
    {
        return new org.openorb.orb.core.dii.Request( self, operation, m_orb );
    }

    /**
     * Return true if this object is local. This fuction also sets up a request
     * sequence.
     */
    public boolean is_local( final org.omg.CORBA.Object self )
    {
        // test for presence of client policy.
        final ForceMarshalPolicy pol = ( ForceMarshalPolicy ) client_policy( self,
                FORCE_MARSHAL_POLICY_ID.value );
        if ( pol != null && pol.forceMarshal() )
        {
            return false;
        }
        // bind, and return local state.
        final RequestState state = begin_invocation( self, false );
        return state.getBinding().local_invoke();
    }

    /**
     * Create a request based on stream stub
     *
     * @param self the object to apply this operation
     * @param operation the operation name
     * @param responseExpected True if a response is expected
     * @return an Outputstream to marshal data
     */
    public OutputStream request( final org.omg.CORBA.Object self,
          final String operation, final boolean responseExpected )
    {
        final RequestState state = begin_invocation( self, false );
        for ( ;; )
        {
            try
            {
                if ( is_local( self ) )
                {
                    state.setRequest( state.getBinding().create_request_local(
                          m_orb, self, operation, responseExpected, state.getAddresses() ) );
                }
                else
                {
                    state.setRequest( state.getBinding().create_request(
                          self, operation, responseExpected ) );
                }
            }
            catch ( final SystemException e )
            {
                state.receiveSystemException( self, e );
                continue;
            }
            final ClientRequest request = state.getRequest();
            final OutputStream ret = request.begin_marshal();
            if ( ret != null )
            {
                return ret;
            }
            // a client side interceptor has rejected the request.
            switch ( request.reply_status() )
            {
                case SYSTEM_EXCEPTION.value:
                    state.receiveSystemException( self, request.received_system_exception() );
                    break;
                case LOCATION_FORWARD.value:
                    state.receiveRedirect( self, request.forward_reference_ior(), false );
                    break;
                default:
                    Trace.signalIllegalCondition( getLogger(), "Invalid reply_status." );
            }
        }
    }

    /**
     * Invoke the request as a deferred request.
     *
     * @param self the object to apply this operation
     * @param os the output stream returned from the request operation.
     */
    public void invoke_deferred( final org.omg.CORBA.Object self, final OutputStream os )
    {
        if ( self == null || os == null )
        {
            throw new BAD_PARAM();
        }
        final RequestState state = locate_state();
        if ( state == null || state.getRequest() == null )
        {
            throw new BAD_INV_ORDER( MinorCodes.BAD_INV_ORDER_DELEGATE,
                    CompletionStatus.COMPLETED_NO );
        }
        state.getRequest().send_request();
        final CurrentImpl current = getPICurrent();
        current.store_invocation_ctx( os );
        current.set_invocation_ctx( state.getParentState() );
        state.setParentState( null );
    }

    /**
     * Poll for a response for a defered request. Note that this operation
     * can be called from a different thread to the original request, providing
     * that the invocation state is migrated.
     *
     * @param self the object to apply this operation
     * @param os the output stream returned from the request operation.
     * @return true if the invoke operation would return immediatly without
     *         waiting.
     */
    public boolean poll_response( final org.omg.CORBA.Object self, final OutputStream os )
    {
        if ( self == null || os == null )
        {
            throw new BAD_PARAM();
        }
        final CurrentImpl current = getPICurrent();
        final RequestState state = ( RequestState ) current.retrieve_invocation_ctx( os, false );
        if ( state == null )
        {
            throw new BAD_INV_ORDER( "Operation not sent deferred",
                    OMGVMCID.value | 13, CompletionStatus.COMPLETED_MAYBE );
        }
        // return true if a response has arrived or the request timeout has expired.
        return state.pollResponse();
    }

    /**
     * Invoke a remote operation for stream based stub
     *
     * @param self the object to apply this operation
     * @param os the output stream returned from the request operation.
     * @return the marshalled data return from remote object
     */
    public InputStream invoke( final org.omg.CORBA.Object self, final OutputStream os )
        throws ApplicationException, RemarshalException
    {
        final CurrentImpl current = getPICurrent();
        if ( self == null || os == null )
        {
            throw new BAD_PARAM();
        }
        int request_state;
        RequestState state = ( RequestState ) current.retrieve_invocation_ctx( os, true );
        if ( state != null )
        {
            // TODO: we must check here to ensure this thread is in the same
            // transaction as the thread which began the invocation.
            state.setParentState( locate_state() );
            current.set_invocation_ctx( state );
            request_state = state.getRequest().state();
        }
        else
        {
            state = locate_state();
            if ( state == null || state.getRequest() == null )
            {
                throw new BAD_INV_ORDER( MinorCodes.BAD_INV_ORDER_DELEGATE,
                        CompletionStatus.COMPLETED_NO );
            }
            request_state = state.getRequest().send_request();
        }
        switch ( request_state )
        {
            case ClientRequest.STATE_WAITING:
                request_state = state.waitForResponse();
                break;
            case ClientRequest.STATE_COMPLETE:
                // client side interceptors set reply, communication problem
                // or syncState == 0
                break;
            default:
                throw Trace.signalIllegalCondition( getLogger(), "Unexpected case" );
        }
        switch ( request_state )
        {
            case ClientRequest.STATE_UNMARSHAL:
                switch ( state.getRequest().reply_status() )
                {
                    case SUCCESSFUL.value:
                        return state.getRequest().receive_response();
                    case USER_EXCEPTION.value:
                        ApplicationException ae = new ApplicationException(
                                state.getRequest().received_exception_id(),
                                state.getRequest().receive_response() );
                        if ( getLogger().isDebugEnabled() )
                        {
                            getLogger().debug( "Application exception", ae );
                        }
                        throw ae;
                    default:
                        throw Trace.signalIllegalCondition( getLogger(), "Unexpected case" );
                }
            case ClientRequest.STATE_COMPLETE:
                switch ( state.getRequest().reply_status() )
                {
                    case SUCCESSFUL.value:
                        // possible with a void response.
                        return state.getRequest().receive_response();
                    case USER_EXCEPTION.value:
                        // possible with a bodyless exception.
                        ApplicationException ae = new ApplicationException(
                                state.getRequest().received_exception_id(),
                                state.getRequest().receive_response() );
                        getLogger().debug( "Application exception", ae );
                        throw ae;
                    case SYSTEM_EXCEPTION.value:
                        state.receiveSystemException( self,
                                state.getRequest().received_system_exception() );
                        throw new RemarshalException();
                    case LOCATION_FORWARD.value:
                        state.receiveRedirect( self,
                                state.getRequest().forward_reference_ior(), false );
                        throw new RemarshalException();
                    case TRANSPORT_RETRY.value:
                        state.failoverTransient( self, true );
                        throw new RemarshalException();
                    default:
                        throw Trace.signalIllegalCondition( getLogger(), "Unexpected case" );
                }

            case ClientRequest.STATE_WAITING:
                // timeout has expired (currently impossible)
                final TIMEOUT e = new TIMEOUT( 0, CompletionStatus.COMPLETED_MAYBE );
                state.getRequest().cancel( e );
                state.completeInvocation( false );
                throw e;
            default:
                throw Trace.signalIllegalCondition( getLogger(), "Unexpected case" );
        }
    }

    /**
     * This function is always called at the end of an invocation sequence
     * regardless of the outcome.
     *
     * @param self the object to apply this operation
     * @param is the input stream for marshalled data from remote object
     */
    public void releaseReply( final org.omg.CORBA.Object self, final InputStream is )
    {
        final RequestState state = locate_state();
        if ( null == state )
        {
            return;
        }
        final ClientRequest request = state.getRequest();
        if ( null == request )
        {
            return;
        }
        switch ( request.state() )
        {
            case ClientRequest.STATE_MARSHAL:
                state.completeInvocation( false );
                final MARSHAL e1 = new MARSHAL(
                        "Exception thrown during marshal",
                        MinorCodes.MARSHAL_REQUEST_UNKNOWN,
                        CompletionStatus.COMPLETED_NO );
                request.cancel( e1 );
                // usualy caused by an exception during the request
                // process, so don't throw another.
                break;

            case ClientRequest.STATE_UNMARSHAL:
                state.completeInvocation( false );
                final MARSHAL e2 = new MARSHAL(
                        "Buffer Underread",
                        MinorCodes.MARSHAL_REPLY_UNKNOWN_OR_UNDERREAD,
                        CompletionStatus.COMPLETED_YES );
                request.cancel( e2 );
                // usualy caused by an exception during the request
                // process, so don't throw another.
            case ClientRequest.STATE_WAITING:
                // cancel a polling request. (not currently used)
                final TIMEOUT e = new TIMEOUT( 0, CompletionStatus.COMPLETED_MAYBE );
                request.cancel( e );
                state.completeInvocation( false );
                break;

            case ClientRequest.STATE_COMPLETE:
                // every state which reaches here will have a yes completion status,
                // only an exception during unmarshaling or thrown by interceptors
                // will have come through.
                state.completeInvocation( true );
                break;

            case ClientRequest.STATE_CREATED:
                // This is the case when there is no ORB listening at the server side.
                break;

            default:
                throw Trace.signalIllegalCondition( getLogger(), "Unexpected case: "
                      + request.state() );
        }
    }

    /**
     * This function is used for local invocation
     */
    public ServantObject servant_preinvoke( final org.omg.CORBA.Object self,
            final String operation, final Class expectedType )
    {
        return begin_invocation( self, false ).servantPreInvoke( self, operation, expectedType );
    }

    /**
     * This function is used after a local invocation
     */
    public void servant_postinvoke( final org.omg.CORBA.Object self,
            final ServantObject servant )
    {
        locate_state().servantPostInvoke( self, servant );
    }

    /**
     * Return the policy associated to the policy type passed as parameter.
     * This will get
     *
     * @param policy_type the policy type to search and to return
     * @return the policy associated to the policy type
     */
    public Policy get_policy( final org.omg.CORBA.Object self, final int policy_type )
    {
        final Policy cltpol = client_policy( self, policy_type );
        final int[] pt = new int[] { policy_type };
        final PolicyListHolder policies = new PolicyListHolder();
        final DomainManager[] managers =
                get_domain_managers_and_policies( self, pt, policies );
        final Policy profpol =
            ( policies.value == null || policies.value.length == 0 )
              ? null : policies.value[ 0 ];
        if ( null == m_orbReconciler )
        {
            final INTERNAL e = new INTERNAL( "PolicyReconciler unavailable" );
            getLogger().error( e.getMessage(), e );
            throw e;
        }
        return m_orbReconciler.reconcile_policies( policy_type, cltpol,
                profpol, managers );
    }

    /**
     * Return the effective client side policy.
     *
     * @param policy_type the policy type to search and to return
     * @return the policy associated to the policy type
     */
    public Policy get_client_policy( final org.omg.CORBA.Object self,
            final int policy_type )
    {
        Policy policy = null;
        boolean found = false;
        if ( m_policy_cache_enabled )
        {
            for ( int i = 0; i < MAX_CLIENT_POLICY_CACHE_SIZE; i++ )
            {
                if ( m_policy_types[ i ] == policy_type )
                {
                    found = true;
                    policy = m_policies[i];
                }
            }
        }
        if ( !found )
        {
            policy = client_policy( self, policy_type );
        }
        if ( m_policy_cache_enabled )
        {
            int currSize = m_policy_cache_size;
            if ( !found && currSize < MAX_CLIENT_POLICY_CACHE_SIZE )
            {
                m_policy_types[ currSize ] = policy_type;
                m_policies[ currSize ] = policy;
                synchronized ( this )
                {
                    m_policy_cache_size++;
                }
            }
        }
        return policy;
    }

    private Policy client_policy( final org.omg.CORBA.Object self, final int policy_type )
    {
        final RequestState state = locate_state();
        if ( ( null != state ) && state.isInIgnoreRebindMode()
              && ( policy_type == REBIND_POLICY_TYPE.value ) )
        {
            // TODO: the RebindPolicy may be temporarily overwritten with a TRANSPARENT
            // value if in the context of an invocation which has state.ignore_rebind_mode
            // as true.
        }
        final int[] policyTypes = new int[] { policy_type };
        if ( null != m_policyOver )
        {
            final Policy[] polices = m_policyOver.get_policy_overrides( policyTypes );
            if ( polices.length > 0 )
            {
                return polices[ 0 ];
            }
        }
        if ( null == m_policyCurr )
        {
            final INTERNAL e = new INTERNAL( "Unable to resolve PolicyCurrent (" + null + ")" );
            getLogger().error( "Unable to resolve PolicyCurrent.", e );
            throw e;
        }
        Policy[] polices = m_policyCurr.get_policy_overrides( policyTypes );
        if ( polices.length > 0 )
        {
            return polices[ 0 ];
        }
        if ( null == m_orbPolicyManager )
        {
            final INTERNAL e =  new INTERNAL( "Unable to resolve ORBPolicyManager (" + null + ")" );
            getLogger().error( "Unable to resolve ORBPolicyManager.", e );
            throw e;
        }
        polices = m_orbPolicyManager.get_policy_overrides( policyTypes );
        if ( polices.length > 0 )
        {
            return polices[ 0 ];
        }
        return null;
    }

    /**
     * Return a copy of this object with the specified policies overriden.
     * @throws NO_PERMISSION some of the overriden policies are invalid.
     */
    public org.omg.CORBA.Object set_policy_override( final org.omg.CORBA.Object self,
            final Policy[] policies, final SetOverrideType set_add )
    {
        try
        {
            return set_policy_overrides( self, policies, set_add );
        }
        catch ( final InvalidPolicies e )
        {
            getLogger().error( "Invalid policy passed to set_policy_overrides().", e );
            throw ExceptionTool.initCause( new NO_PERMISSION( 0,
                    CompletionStatus.COMPLETED_NO ), e );
        }
    }

    /**
     * Return a copy of this object with the specified policies overriden.
     * @throws org.omg.CORBA.InvalidPolicies some of the overriden policies
     * are invalid.
     */
    public org.omg.CORBA.Object set_policy_overrides(
            final org.omg.CORBA.Object self, final Policy[] policies,
            final SetOverrideType set_add )
        throws InvalidPolicies
    {
        if ( null == m_policySetManager )
        {
            final INTERNAL e = new INTERNAL( "PolicySetManager unavailable" );
            getLogger().error( e.getMessage(), e );
            throw e;
        }
        final PolicyManagerOperations newPolices;
        if ( policies.length == 0 )
        {
            // all old set
            if ( ( m_policyOver == null ) || ( set_add == SetOverrideType.SET_OVERRIDE ) )
            {
                newPolices = null;
            }
            else
            {
                newPolices = m_policySetManager.create_policy_set(
                        PolicySetManager.CLIENT_POLICY_DOMAIN );
                newPolices.set_policy_overrides(
                        m_policyOver.get_policy_overrides( new int[ 0 ] ),
                        SetOverrideType.SET_OVERRIDE );
            }
        }
        else if ( ( m_policyOver == null ) || ( set_add == SetOverrideType.SET_OVERRIDE ) )
        {
            // all new set
            newPolices = m_policySetManager.create_policy_set(
                    PolicySetManager.CLIENT_POLICY_DOMAIN );
            newPolices.set_policy_overrides( policies, set_add );
        }
        else
        {
            // union set.
            newPolices = m_policySetManager.create_policy_set(
                    PolicySetManager.CLIENT_POLICY_DOMAIN );
            newPolices.set_policy_overrides(
                    m_policyOver.get_policy_overrides( new int[ 0 ] ),
                    SetOverrideType.SET_OVERRIDE );
            newPolices.set_policy_overrides( policies, set_add );
        }
        // we now have a property set to give to the new reference/delegate.
        // try to instantiate a new stub, of the same type as the old one
        ObjectImpl obj = null;
        if ( !( self instanceof ObjectStub ) )
        {
            try
            {
                obj = ( ObjectImpl ) self.getClass().newInstance();
            }
            catch ( final Exception e )
            {
                getLogger().error( "Failed to create instance of ObjectImpl class.", e );
            }
        }
        if ( null == obj )
        {
            obj = new ObjectStub();
        }
        final Delegate delegate = new Delegate( m_orb, ior(), newPolices );
        obj._set_delegate( delegate );
        return obj;
    }

    /**
     * Returns the list of Policy overrides (of the specified policy types)
     * set at the Object scope. If the specified sequence is empty, all
     * Policy overrides at this scope will be returned. If none of the
     * requested PolicyTypes are overridden at the Object scope,
     * an empty sequence is returned.
     */
    public Policy[] get_policy_overrides( final int[] ts )
    {
        if ( m_policyOver == null )
        {
            return new Policy[ 0 ];
        }
        return m_policyOver.get_policy_overrides( ts );
    }

    /**
     * Validate the connection to the client. This operation ignores the rebind
     * policy.
     */
    public boolean validate_connection( final org.omg.CORBA.Object self,
            final PolicyListHolder inconsistent_policies )
    {
        final PolicyListHolder policies = new PolicyListHolder();
        // set the force rebind flag
        final RequestState state = begin_invocation( self, true );
        try
        {
            final boolean ret = state.locateAndGetPolicies( self, policies );
            inconsistent_policies.value = new Policy[ 0 ];
            return ret;
        }
        catch ( final INV_POLICY e )
        {
            // we have some invalid policies.
            inconsistent_policies.value = new Policy[ 0 ];
            // TODO: find which policies are inconsistent.
            return false;
        }
    }

    /**
     * Return a reference to the ORB
     */
    public org.omg.CORBA.ORB orb( final org.omg.CORBA.Object self )
    {
        return m_orb;
    }

    private boolean compareIORs( final IOR ior1, final IOR ior2 )
    {
        if ( ior1 == ior2 )
        {
            return true;
        }
        if ( !ior1.type_id.equals( ior2.type_id )
              || ior1.profiles.length != ior2.profiles.length )
        {
            return false;
        }
        for ( int i = 0; i < ior1.profiles.length; ++i )
        {
            if ( ior1.profiles[ i ].tag != ior2.profiles[ i ].tag
                  || !Arrays.equals( ior1.profiles[ i ].profile_data,
                  ior2.profiles[ i ].profile_data ) )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Return codebase for stub class.
     */
    public String get_codebase( final org.omg.CORBA.Object self )
    {
        return null;
    }

    /**
     * Get the ior of the object as it should be serialized / marshalled. This
     * will change on a permanent redirection.
     */
    public IOR ior()
    {
        synchronized ( m_bindingSync )
        {
            return m_effectiveIor;
        }
    }

    /**
     * Get a snapshot list of the target addresses. This is subject to grow
     * when an object forward is received.
     */
    public Address[] getAddresses( final org.omg.CORBA.Object self )
    {
        final CurrentImpl current = getPICurrent();
        // locate any current state.
        final Object oldCtx = current.get_invocation_ctx();
        current.set_invocation_ctx( null );
        try
        {
            // begin a dummy invocation.
            return begin_invocation( self, false ).getAddresses();
        }
        finally
        {
            // throw away the dummy invocation.
            current.set_invocation_ctx( oldCtx );
        }
    }

    private RequestState begin_invocation( final org.omg.CORBA.Object obj,
            final boolean ignore_rebind_mode )
    {
        final CurrentImpl current = getPICurrent();
        RequestState state = ( RequestState ) current.get_invocation_ctx();
        if ( state == null )
        {
            state = createRequestState( null );
        }
        else if ( state.isTargetDelegate( this ) )
        {
            if ( state.getRequest() != null )
            {
                Trace.signalIllegalCondition( getLogger(), "Invalid state at begin_invocation" );
            }
            if ( ignore_rebind_mode )
            {
                state.setIgnoreRebindMode( true );
            }
            return state;
        }
        else
        {
            state = createRequestState( state );
        }
        state.beginInvocation( this, current, obj, ignore_rebind_mode );
        return state;
    }

    private RequestState locate_state()
    {
        final CurrentImpl current = getPICurrent();
        RequestState state = ( RequestState ) current.get_invocation_ctx();
        while ( state != null && !state.isTargetDelegate( this ) )
        {
            if ( state.getRequest() != null || state.isLocalLevelGreaterThanZero() )
            {
                return null;
            }
            state = state.getParentState();
        }
        return state;
    }

    private RequestState createRequestState( final RequestState parent )
    {
        boolean discard_old = ( ( org.openorb.orb.core.ORB ) m_orb ).getLoader().getBooleanProperty(
              "openorb.client.bindings.discard_old", false );
        RequestState rs = new RequestState( getLogger(), parent );
        if ( discard_old )
        {
            rs.discardOldBindings();
        }
        return rs;
    }

    private static class RequestState
    {
        private static final long NO_DEADLINE = -1;
        private boolean m_discard_old = false;
        private final Logger m_logger;
        private ClientBinding m_binding;
        private ClientRequest m_request;
        private boolean m_ignoreRebindMode;
        private int m_version;
        private int m_idx;
        private ClientBinding[] m_binds;
        private boolean[] m_used;
        private int m_unusedBindingsCount;
        private int m_exceptionLevel = -1;
        private SystemException m_exception;
        private long m_roundtripDeadline = NO_DEADLINE;
        private Delegate m_targetDelegate;
        private int m_localLevel = 0;
        private RequestState m_parentState;

        protected RequestState( final Logger logger, final RequestState parentState )
        {
            if ( logger == null )
            {
                throw new NullPointerException( "RequestState requires a logger" );
            }
            m_logger = logger;
            m_parentState = parentState;
        }

        public Logger getLogger()
        {
            return m_logger;
        }

        public void incrementLocalLevel()
        {
            m_localLevel++;
        }

        public void decrementLocalLevel()
        {
            m_localLevel--;
        }

        public boolean isLocalLevelZero()
        {
            return m_localLevel == 0;
        }

        public boolean isLocalLevelGreaterThanZero()
        {
            return 0 > m_localLevel;
        }

        public boolean isLocalStateLessThanOrEqualToZero()
        {
            return m_localLevel <= 0;
        }

        public int getExceptionLevel()
        {
            return m_exceptionLevel;
        }

        public void setExceptionLevel( final int level )
        {
            m_exceptionLevel = level;
        }

        public SystemException getException()
        {
            return m_exception;
        }

        public void setException( final SystemException e )
        {
            m_exception = e;
        }

        public void discardOldBindings()
        {
            m_discard_old = true;
        }

        public boolean isTargetDelegate( final Delegate delegate )
        {
            return m_targetDelegate == delegate;
        }

        public void setTargetDelegate( final Delegate delegate )
        {
            m_targetDelegate = delegate;
        }

        public int getVersion()
        {
            return m_version;
        }

        public void setVersion( final int version )
        {
            m_version = version;
        }

        public int getIdx()
        {
            return m_idx;
        }

        public void setIdx( final int value )
        {
            m_idx = value;
        }

        public ClientBinding[] getBindings()
        {
            return m_binds;
        }

        public void setBindings( final ClientBinding[] bindings )
        {
            m_binds = bindings;
        }

        public boolean isMoreThanOneUnusedBinding()
        {
            return 1 < m_unusedBindingsCount;
        }

        public void setUnusedBindingsCount( final int value )
        {
            m_unusedBindingsCount = value;
        }

        public void decrementUnusedBindingsCount()
        {
            m_unusedBindingsCount--;
        }

        public boolean areUnusedBindings()
        {
            return 0 != m_unusedBindingsCount;
        }

        public boolean[] getUsed()
        {
            return m_used;
        }

        public void setUsed( final boolean[] used )
        {
            m_used = used;
        }

        /**
         * Sets the relative round trip dealine from the current time
         */
        public void setRelativeRoundtripTimeout( final long timeout100ns )
        {
            setRelativeRoundtripTimeout( timeout100ns,
                    System.currentTimeMillis() );
        }

        /**
         * Sets the relative round trip dealine from the specified time.
         * If the timeout is over 30000 years then no dealine is set.
         *
         * @param timeout100ns the TimeT value of the timeout
         * @param currentTime1ms the specified set time.
         */
        public void setRelativeRoundtripTimeout( final long timeout100ns,
                final long currentTime1ms )
        {
            if ( timeout100ns < 0 )
            {
                m_roundtripDeadline = NO_DEADLINE;
                if ( Trace.isHigh() )
                {
                    m_logger.debug( "RoundtripDeadline=[NO_DEADLINE]" );
                }
            }
            else
            {
                m_roundtripDeadline = currentTime1ms
                      + ( timeout100ns / 10000L );
                if ( Trace.isHigh() && m_logger.isDebugEnabled() )
                {
                    m_logger.debug( "RoundtripDeadline=["
                          + m_roundtripDeadline + "]" );
                }
            }
        }

        /**
         * Indicates if the roundtrip deadline has been set.
         */
        public boolean isRoundtripDeadlineSet()
        {
            return NO_DEADLINE != m_roundtripDeadline;
        }

        /**
         * Calculates the realtive time between now and the deadline.
         * If the result is zero or negative then the dealine has passed.
         */
        public long calculateTimeToDeadline()
        {
            return m_roundtripDeadline - System.currentTimeMillis();
        }

        /**
         * Waits for a response using the current roundtrip deadline if set.
         */
        public int waitForResponse()
        {
            if ( isRoundtripDeadlineSet() )
            {
                final long timeToDeadline = calculateTimeToDeadline();
                return ( timeToDeadline < 1L )
                     ?  ClientRequest.STATE_WAITING
                     :  m_request.wait_for_response( timeToDeadline );
            }
            else
            {
                return m_request.wait_for_response( 0 );
            }
        }

        /**
         * Polls the current request to see if a call to invoke would not
         * block using the current roundtrip deadline if set.
         */
        public boolean pollResponse()
        {
            return ( isRoundtripDeadlineSet()
                  ? ( calculateTimeToDeadline() <= 0L )
                  : m_request.poll_response() );
        }

        public ClientBinding getBinding()
        {
            return m_binding;
        }

        public void setBinding( final ClientBinding binding )
        {
            m_binding = binding;
        }

        public ClientRequest getRequest()
        {
            return m_request;
        }

        public void setRequest( final ClientRequest request )
        {
            m_request = request;
        }

        public boolean isInIgnoreRebindMode()
        {
            return m_ignoreRebindMode;
        }

        public void setIgnoreRebindMode( final boolean ignoreRebindMode )
        {
            m_ignoreRebindMode = ignoreRebindMode;
        }

        public RequestState getParentState()
        {
            return m_parentState;
        }

        public void setParentState( final RequestState state )
        {
            m_parentState = state;
        }

        public ServantObject servantPreInvoke( final org.omg.CORBA.Object self,
                final String operation, final Class expectedType )
        {
            incrementLocalLevel();
            boolean failed = true;
            try
            {
                final ServantObject ret = getBinding().servant_preinvoke( operation, expectedType );
                failed = false;
                return ret;
            }
            catch ( final SystemException e )
            {
                receiveSystemException( self, e );
                failed = false;
                return null;
            }
            catch ( final ForwardRequest e )
            {
                final IOR fwd = ( ( Delegate ) ( ( ObjectImpl ) e.forward )._get_delegate() ).ior();
                receiveRedirect( self, fwd, false );
                failed = false;
                return null;
            }
            finally
            {
                if ( failed )
                {
                    completeInvocation( false );
                }
            }
        }

        /**
         * This function is used after a local invocation
         */
        public void servantPostInvoke( final org.omg.CORBA.Object self,
                final ServantObject servant )
        {
            try
            {
                if ( null != getBinding() )
                {
                    getBinding().servant_postinvoke( servant );
                }
            }
            catch ( final Exception e )
            {
                m_logger.error( "Delegate.servant_postinvoke()", e );
            }
            finally
            {
                completeInvocation( true );
            }
        }

        /**
         * Update selected binding.
         */
        public void completeInvocation( final boolean success )
        {
            synchronized ( m_targetDelegate.m_bindingSync )
            {
                if ( success == ( m_targetDelegate.m_selectedBinding == null ) )
                {
                    if ( success )
                    {
                        if ( m_targetDelegate.m_selectedBinding == null )
                        {
                            m_targetDelegate.m_selectedBinding = getBinding();
                        }
                    }
                    else
                    {
                        if ( m_targetDelegate.m_selectedBinding == getBinding() )
                        {
                            m_targetDelegate.m_selectedBinding = null;
                        }
                    }
                }
            }
            decrementLocalLevel();
            if ( isLocalStateLessThanOrEqualToZero() )
            {
                m_targetDelegate.getPICurrent().set_invocation_ctx( getParentState() );
            }
        }

        public void beginInvocation( final Delegate delegate,
                final CurrentImpl current, final org.omg.CORBA.Object obj,
                final boolean ignoreRebindMode )
        {
            setTargetDelegate( delegate );
            final RelativeRoundtripTimeoutPolicy pol = ( RelativeRoundtripTimeoutPolicy )
                  m_targetDelegate.get_client_policy( obj, RELATIVE_RT_TIMEOUT_POLICY_TYPE.value );
            if ( pol != null )
            {
                setRelativeRoundtripTimeout( pol.relative_expiry() );
            }
            current.set_invocation_ctx( this );
            final ClientBinding[] blst;
            synchronized ( m_targetDelegate.m_bindingSync )
            {
                setIgnoreRebindMode( ignoreRebindMode );
                setVersion( m_targetDelegate.m_bindingVersion );
                setIdx( -1 );
                if ( m_targetDelegate.m_bindingVersion < 0 )
                {
                    // this is the first attempt to make a request
                    // TODO: is this correct or do we force use of validate_connection
                    // even for the first bind?
                    setIgnoreRebindMode( true );
                    receiveRedirect( obj, m_targetDelegate._getIOR(), true );
                    return;
                }
                // first try the selected binding if there is one.
                if ( m_targetDelegate.m_selectedBinding != null )
                {
                    setBinding( m_targetDelegate.m_selectedBinding );
                    if ( m_targetDelegate.m_selectedBinding.local_invoke()
                          || m_targetDelegate.m_selectedBinding.getClientChannel().state()
                          != ClientChannel.STATE_CLOSED )
                    {
                        return;
                    }
                    // TODO: if we have a NO_REBIND (but not NO_RECONNECT)policy and
                    // the state of the channel is paused we want to at least attempt
                    // to reopen the known good connection before rebinding.
                    m_targetDelegate.m_selectedBinding = null;
                }
                blst = ( ClientBinding[] ) m_targetDelegate.m_bindings.toArray(
                        new ClientBinding[ m_targetDelegate.m_bindings.size() ] );
            }
            refreshBindingList( blst );
            // try for the next available binding in the list
            findNextBinding();
        }

        public boolean locateAndGetPolicies( final org.omg.CORBA.Object self,
                final PolicyListHolder policies )
        {
            int[] policyTypes = new int[ 0 ];
            try
            {
                for ( ;; )
                {
                    if ( policies != null )
                    {
                        policies.value = getBinding().getAddress().get_target_policies(
                              policyTypes );
                    }
                    if ( getBinding().getObjectAdapter() == null )
                    {
                        try
                        {
                            setRequest( getBinding().create_locate_request( self ) );
                        }
                        catch ( final SystemException e )
                        {
                            receiveSystemException( self, e );
                            continue;
                        }
                        // this will return null
                        m_request.begin_marshal();
                        int request_state = m_request.state();
                        if ( request_state == ClientRequest.STATE_MARSHAL )
                        {
                            request_state = m_request.send_request();
                            if ( request_state == ClientRequest.STATE_WAITING )
                            {
                                request_state = waitForResponse();
                                if ( request_state == ClientRequest.STATE_WAITING )
                                {
                                    // timeout has expired (currently impossible)
                                    final TIMEOUT e = new TIMEOUT( 0,
                                          CompletionStatus.COMPLETED_MAYBE );
                                    m_request.cancel( e );
                                    throw e;
                                }
                            }
                        }
                        if ( request_state != ClientRequest.STATE_COMPLETE )
                        {
                            Trace.signalIllegalCondition( m_logger, "Unexpected request_state" );
                        }
                        switch ( m_request.reply_status() )
                        {
                            case ClientRequest.OBJECT_HERE:
                                completeInvocation( true );
                                return false;
                            case ClientRequest.UNKNOWN_OBJECT:
                                failoverPermanent( self,
                                        new OBJECT_NOT_EXIST( 0, CompletionStatus.COMPLETED_NO ) );
                                break;
                            case SYSTEM_EXCEPTION.value:
                                receiveSystemException( self,
                                        m_request.received_system_exception() );
                                break;
                            case LOCATION_FORWARD.value:
                                receiveRedirect( self,
                                        m_request.forward_reference_ior(), false );
                                break;
                            case TRANSPORT_RETRY.value:
                                failoverTransient( self, true );
                                break;
                            default:
                                throw Trace.signalIllegalCondition( m_logger,
                                        "Unexpected reply_status" );
                        }
                    }
                    else
                    {
                        try
                        {
                            if ( getBinding().locate() )
                            {
                                completeInvocation( true );
                                return false;
                            }
                            else
                            {
                                failoverPermanent( self,
                                        new OBJECT_NOT_EXIST( 0, CompletionStatus.COMPLETED_NO ) );
                            }
                        }
                        catch ( final ForwardRequest e )
                        {
                            final IOR fwd = ( ( Delegate )
                                  ( ( ObjectImpl ) e.forward )._get_delegate() ).ior();
                            receiveRedirect( self, fwd, false );
                        }
                    }
                }
            }
            catch ( final NO_RESPONSE e )
            {
                return true;
            }
            catch ( final COMM_FAILURE e )
            {
                return true;
            }
            catch ( final NO_IMPLEMENT e )
            {
                return true;
            }
            catch ( final OBJ_ADAPTER e )
            {
                return true;
            }
            catch ( final OBJECT_NOT_EXIST e )
            {
                return true;
            }
        }

        /**
         * Returns a snapshot of the target addresses used
         */
        public Address[] getAddresses()
        {
            // ensure we have the entire binding list.
            if ( null == m_binds )
            {
                setBinding( null );
                refreshBindingList( null );
            }
            // extract the addresses.
            final Address[] addresses = new Address[ m_binds.length ];
            for ( int i = 0; i < addresses.length; ++i )
            {
                addresses[ i ] = m_binds[ i ].getAddress();
            }
            return addresses;
        }

        /**
         * refreshes the binding list, adding the bindings in blst.
         */
        public void refreshBindingList( final ClientBinding[] bindingList )
        {
            final ClientBinding[] blst;
            if ( bindingList == null )
            {
                synchronized ( m_targetDelegate.m_bindingSync )
                {
                    if ( getBindings() != null
                        && ( getVersion() >= m_targetDelegate.m_bindingVersion ) )
                    {
                        return;
                    }
                    blst = ( ClientBinding[] ) m_targetDelegate.m_bindings.toArray(
                            new ClientBinding[ m_targetDelegate.m_bindings.size() ] );
                    setVersion( m_targetDelegate.m_bindingVersion );
                }
            }
            else
            {
                blst = bindingList;
            }
            final boolean[] newUsedBindings = new boolean[ blst.length ];
            // not needed, default value is false
            // Arrays.fill(nused, false);
            setUnusedBindingsCount( blst.length );
            // mark the previously used bindings as still used.
            if ( m_binds != null )
            {
                for ( int i = 0; i < m_binds.length; i++ )
                {
                    if ( m_used[i] )
                    {
                        for ( int j = 0; j < blst.length; j++ )
                        {
                            if ( blst[ j ].equals( m_binds[ i ] ) )
                            {
                                newUsedBindings[ j ] = true;
                                decrementUnusedBindingsCount();
                                break;
                            }
                        }
                    }
                }
            }
            else if ( null != m_binding )
            {
                for ( int j = 0; j < blst.length; ++j )
                {
                    if ( blst[ j ] != null && blst[ j ].equals( m_binding ) )
                    {
                        newUsedBindings[ j ] = true;
                        decrementUnusedBindingsCount();
                        break;
                    }
                }
            }
            setBindings( blst );
            setUsed( newUsedBindings );
            if ( isMoreThanOneUnusedBinding() )
            {
                // sort the states
                ClientBinding[] blstsrc = ( ClientBinding[] ) blst.clone();
                final boolean[] usedsrc = ( boolean[] ) m_used.clone();
                mergeSort( blstsrc, usedsrc, blst, m_used, 0, blst.length );
            }
            setIdx( 0 );
        }

        public void findNextBinding()
        {
            // first look for locals or connected channels, then for
            // paused channels.
            for ( int j = 0, i = getIdx(); j < m_used.length; ++j, i = ( i + 1 ) % m_used.length )
            {
                if ( !m_used[i] && ( m_binds[i].local_invoke()
                      || m_binds[i].getClientChannel().state()
                      == ClientChannel.STATE_CONNECTED ) )
                {
                    setBinding( m_binds[ i ] );
                    setIdx( i );
                    return;
                }
            }
            for ( int j = 0, i = getIdx(); j < m_used.length;
                    ++j, i = ( i + 1 ) % m_used.length )
            {
                if ( !m_used[ i ] )
                {
                    if ( m_binds[ i ].getClientChannel().state()
                          != ClientChannel.STATE_CLOSED )
                    {
                        setBinding( m_binds[ i ] );
                        setIdx( i );
                        return;
                    }
                    m_used[ i ] = true;
                    decrementUnusedBindingsCount();
                }
            }
            if ( areUnusedBindings() )
            {
                Trace.signalIllegalCondition( m_logger,
                        "There are still bindings available." );
            }
            completeInvocation( false );
            if ( getExceptionLevel() < 0 )
            {
                throw ExceptionTool.initCause(
                        new TRANSIENT( 2, CompletionStatus.COMPLETED_NO ), getException() );
            }
            try
            {
                throw getException();
            }
            catch ( final COMM_FAILURE e )
            {
                throw isConnectException( e ) ? ExceptionTool.initCause(
                        new TRANSIENT( 2, CompletionStatus.COMPLETED_NO ), e ) : e;
            }
        }

        private boolean isConnectException( final COMM_FAILURE e )
        {
            return CompletionStatus.COMPLETED_NO.equals( e.completed )
                  && ( ( IIOPMinorCodes.COMM_FAILURE_IO_EXCEPTION == e.minor )
                  || ( IIOPMinorCodes.COMM_FAILURE_NO_ROUTE == e.minor )
                  || ( IIOPMinorCodes.COMM_FAILURE_NO_CONNECT == e.minor ) );
        }

        /**
         * Request failed but a retry on the binding is indicated.
         */
        public void failoverTransient( final org.omg.CORBA.Object self,
              final boolean transportRetry )
        {
            // set the request to null
            setRequest( null );
            // don't mark any binding as used.
            ClientBinding oldBinding = getBinding();
            if ( m_binds == null )
            {
                setBinding( null );
            }
            refreshBindingList( null );
            setIdx( 1 );
            // TODO: if we have a NO_REBIND policy then set transport_retry to true
            if ( transportRetry )
            {
                // reuse the same connection.
                setBinding( oldBinding );
                for ( int i = 0; i < m_binds.length; ++i )
                {
                    if ( oldBinding == m_binds[ i ] )
                    {
                        setIdx( i );
                        return;
                    }
                }
                Trace.signalIllegalCondition( m_logger, "Old binding not found." );
            }
            else
            {
                setIdx( ( getIdx() + 1 ) % m_binds.length );
                findNextBinding();
            }
        }

        public void failoverFatal(
                final org.omg.CORBA.Object self, final SystemException exception )
        {
            // set the request to null
            setRequest( null );
            if ( !isInIgnoreRebindMode() )
            {
                // TODO: check the rebind mode policy to ensure it is not
                // equal to NO_REBIND or NO_RECONNECT.
                if ( false )
                {
                    completeInvocation( false );
                    throw ExceptionTool.initCause( new REBIND( 0,
                            CompletionStatus.COMPLETED_NO ), exception );
                }
            }
            // mark selected binding as used.
            if ( m_binds != null )
            {
                m_used[ getIdx() ] = true;
                decrementUnusedBindingsCount();
            }
            refreshBindingList( null );
            if ( getExceptionLevel() <= 1 )
            {
                setExceptionLevel( 1 );
                setException( exception );
            }
            findNextBinding();
        }

        public void failoverPermanent(
                final org.omg.CORBA.Object self, final SystemException exception )
        {
            // set the request to null
            setRequest( null );
            if ( !isInIgnoreRebindMode() )
            {
                // TODO: check the rebind mode policy to ensure it is not
                // equal to NO_REBIND or NO_RECONNECT.
                if ( false )
                {
                    completeInvocation( false );
                    throw ExceptionTool.initCause( new REBIND( 0,
                            CompletionStatus.COMPLETED_NO ), exception );
                }
            }
            // lower the binding's priority to a last gasp level
            getBinding().setPriority( ( getBinding().getPriority() & 0xFFFF ) | 0xFB0000 );
            // mark as used.
            if ( m_binds != null )
            {
                m_used[ getIdx() ] = true;
                decrementUnusedBindingsCount();
            }
            refreshBindingList( null );
            if ( getExceptionLevel() <= 3 )
            {
                setExceptionLevel( 3 );
                setException( exception );
            }
            findNextBinding();
        }

        /**
         * Cleans up after recieving an exception. If a retry is indicated
         * this function returns normally, otherwise it throws the
         * system exception.
         */
        private void receiveSystemException(
                final org.omg.CORBA.Object self, final SystemException exception )
        {
            if ( exception.completed == CompletionStatus.COMPLETED_NO )
            {
                if ( exception instanceof TRANSIENT )
                {
                    failoverTransient( self, false );
                    return;
                }
                // NO_IMPLEMENT, INV_POLICY, NO_PERMISSION nonstandard failover conds.
                if ( ( exception instanceof NO_RESPONSE )
                      || ( exception instanceof COMM_FAILURE )
                      || ( exception instanceof NO_IMPLEMENT )
                      || ( exception instanceof INV_POLICY )
                      || ( exception instanceof NO_PERMISSION ) )
                {
                    failoverFatal( self, exception );
                    return;
                }
                if ( exception instanceof OBJ_ADAPTER )
                {
                    failoverPermanent( self, exception );
                    return;
                }
            }
            completeInvocation( exception.completed == CompletionStatus.COMPLETED_YES );
            throw exception;
        }

        /**
         * This method decides by the property 'openorb.client.bindings.discard_old' whether the
         * new redirect handler should be used. Per default the old handler is used, but when the
         * property is set to true the new handler will be used instead.
         */
        private void receiveRedirect(
             final org.omg.CORBA.Object self, final IOR ior, final boolean permanent )
        {
            if ( m_discard_old )
            {
                receiveRedirectNew( self, ior, permanent );
            }
            else
            {
                receiveRedirectOld( self, ior, permanent );
            }
        }

        /**
         * Do not sort new bindings based on priorities. This method just discards any old bindings
         * and uses the new ones instead.
         */
        private void receiveRedirectNew (
              final org.omg.CORBA.Object self, final IOR ior, final boolean permanent )
        {
            // set the request to null
            setRequest( null );
            if ( !isInIgnoreRebindMode() )
            {
                // TODO: check the rebind mode policy to ensure it is not
                // equal to NO_REBIND or NO_RECONNECT.
                if ( false )
                {
                    completeInvocation( false );
                    throw new REBIND( 0, CompletionStatus.COMPLETED_NO );
                }
            }
            if ( m_binds != null )
            {
               setUnusedBindingsCount( 0 );
               m_binds = null;
            }
            ClientBinding[] new_binding_list =
                  m_targetDelegate.m_clientManager.create_bindings( self, ior );
            m_targetDelegate.m_bindings.clear();
            m_targetDelegate.m_bindingVersion = 1;
            m_targetDelegate.m_effectiveIor = ior;
            setVersion( m_targetDelegate.m_bindingVersion );
            boolean[] usedBindings = new boolean[ new_binding_list.length ];
            for ( int i = 0; i < usedBindings.length; i++ )
            {
               usedBindings[ i ] = false;
               m_targetDelegate.m_bindings.add( new_binding_list[ i ] );
            }
            setBindings( new_binding_list );
            setUsed( usedBindings );
            setUnusedBindingsCount( m_binds.length );
            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Delegate<" + this + ">: "
                      + "Set new bindings (" + m_targetDelegate.m_bindings.size() + "):" );
                for ( Iterator it = m_targetDelegate.m_bindings.iterator(); it.hasNext(); )
                {
                    ClientBinding cb = ( ClientBinding ) it.next();
                    Address clientAddress = cb.getAddress();
                    getLogger().debug( "Delegate<" + this + ">: "
                          + "New binding : " + clientAddress.getEndpointString() );
                }
            }
            setBinding( m_binds[ 0 ] );
            setIdx( 0 );
            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Delegate<" + this + ">: "
                      + "Use binding with index " + getIdx() );
            }
        }

        /**
         * Deals with a redirection.
         *
         * <p>Ordinary IIOP endpoints are tried in the following order:</p>
         *
         * <ol>
         * <li>SSLIOP endpoints</li>
         * <li>The IIOP endpoint from each profile</li>
         * <li>Alternate endpoints from TAG_ALTERNATE_IIOP_ADDRESS components</li>
         * </ol>
         *
         * <p>In each class as above, endpoints using higher versions of IIOP are used
         * before lower ones, and if there's still endpoints at the same priority,
         * endpoints appearing earlier in the IOR are used first.</p>
         *
         * <p>Profiles that have the TAG_FT_PRIMARY component are used before any
         * other component (in the same order as above).</p>
         *
         * <p>Expressed in another way, the priority it a bit field, with lower values
         * used first. The bits are (true == 1)</p>
         *
         * <p>Alternate, non-primary, secure, (1-version & 0xF).</p>
         */
        private void receiveRedirectOld(
                final org.omg.CORBA.Object self, final IOR ior, final boolean permanent )
        {
            // set the request to null
            setRequest( null );
            if ( !isInIgnoreRebindMode() )
            {
                // TODO: check the rebind mode policy to ensure it is not
                // equal to NO_REBIND or NO_RECONNECT.
                if ( false )
                {
                    completeInvocation( false );
                    throw new REBIND( 0, CompletionStatus.COMPLETED_NO );
                }
            }
            // mark the binding which raised the redirect as used
            if ( m_binds != null )
            {
                m_used[getIdx()] = true;
                decrementUnusedBindingsCount();
            }
            ClientBinding[] new_binding_list =
                  m_targetDelegate.m_clientManager.create_bindings( self, ior );
            synchronized ( m_targetDelegate.m_bindingSync )
            {
                if ( permanent )
                {
                    m_targetDelegate.m_effectiveIor = ior;
                }
                int new_binding_list_len = new_binding_list.length;
                // check to make sure the new bindings aren't already
                // present in the current binding list
                for ( int i = 0; i < new_binding_list.length; ++i )
                {
                    if ( m_targetDelegate.m_bindings.contains( new_binding_list[ i ] ) )
                    {
                        new_binding_list[ i ] = null;
                        new_binding_list_len--;
                    }
                }
                // if we got new bindings
                if ( new_binding_list_len > 0 )
                {
                    // count the number of changes to the client side bindings
                    m_targetDelegate.m_bindingVersion++;
                    // reshuffle the priorities for binding whose version is below 250
                    if ( m_targetDelegate.m_bindingVersion < 250 )
                    {
                        for ( int i = 0; i < new_binding_list.length; ++i )
                        {
                            if ( new_binding_list[ i ] != null )
                            {
                                int pri = new_binding_list[ i ].getPriority();
                                if ( pri >= 0 )
                                {
                                    int newpri =
                                          ( ( 250 - m_targetDelegate.m_bindingVersion ) << 16 );
                                    pri = ( pri & 0xFFFF ) | newpri;
                                    new_binding_list[ i ].setPriority( pri );
                                    m_targetDelegate.m_bindings.add( new_binding_list[ i ] );
                                }
                            }
                        }
                    }
                    else
                    {
                        for ( Iterator it = m_targetDelegate.m_bindings.iterator(); it.hasNext(); )
                        {
                            ClientBinding cb = ( ClientBinding ) it.next();
                            int pri = cb.getPriority();
                            if ( pri >= 0 )
                            {
                                int newpri = ( pri & 0xFF0000 ) + 0x10000;
                                pri = ( pri & 0xFFFF )
                                      | ( ( newpri > 0xFA0000 ) ? 0xFA0000 : newpri );
                                cb.setPriority( pri );
                            }
                        }
                        for ( int i = 0; i < new_binding_list.length; ++i )
                        {
                            m_targetDelegate.m_bindings.add( new_binding_list[ i ] );
                        }
                    }
                    new_binding_list = ( ClientBinding[] )
                          m_targetDelegate.m_bindings.toArray(
                          new ClientBinding[ m_targetDelegate.m_bindings.size() ] );
                }
            }
            if ( m_targetDelegate.m_bindingVersion >= 0 )
            {
                refreshBindingList( new_binding_list );
                setVersion( m_targetDelegate.m_bindingVersion );
            }
            findNextBinding();
        }
    }

    /**
     * Merge sort operation. Sorts used and binding list arrays together.
     */
    private static void mergeSort( final ClientBinding[] blstsrc,
                                   final boolean[] usedsrc, final ClientBinding[] blst,
                                   final boolean[] used, final int low, final int high )
    {
        final int length = high - low;
        // Insertion sort on smallest arrays
        if ( length < 7 )
        {
            ClientBinding t;
            boolean ut;
            for ( int i = low; i < high; i++ )
            {
                for ( int j = i;
                      j > low && statecomp( blst[ j - 1 ], used[ j - 1 ], blst[ j ], used[ j ] )
                      > 0; j-- )
                {
                    t = blst[ j ];
                    ut = used[ j ];
                    blst[ j ] = blst[ j - 1 ];
                    used[ j ] = used[ j - 1 ];
                    blst[ j - 1 ] = t;
                    used[ j - 1 ] = ut;
                }
            }
            return;
        }
        // Recursively sort halves of dest into src
        final int mid = ( low + high ) / 2;
        mergeSort( blstsrc, usedsrc, blst, used, low, mid );
        mergeSort( blstsrc, usedsrc, blst, used, mid, high );
        // If list is already sorted, just copy from src to dest.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if ( statecomp( blstsrc[ mid - 1 ], usedsrc[ mid - 1 ], blstsrc[ mid ],
              usedsrc[ mid ] ) <= 0 )
        {
            System.arraycopy( blstsrc, low, blst, low, length );
            System.arraycopy( usedsrc, low, used, low, length );
            return;
        }
        // Merge sorted halves (now in src)into dest
        for ( int i = low, p = low, q = mid; i < high; i++ )
        {
            if ( ( q >= high ) || ( p < mid )
                  && ( statecomp( blstsrc[ p ], usedsrc[ p ], blstsrc[ q ], usedsrc[ q ] ) <= 0 ) )
            {
                blst[ i ] = blstsrc[ p ];
                used[ i ] = usedsrc[ p ];
                p++;
            }
            else
            {
                blst[ i ] = blstsrc[ q ];
                used[ i ] = usedsrc[ q ];
                q++;
            }
        }
    }

    /**
     * Truth table
     * <table>
     *   <thead>
     *     <tr>
     *       <th>u1</th>
     *       <th>u2</th>
     *       <th>result</th>
     *     </tr>
     *   </thead>
     *   <tbody>
     *     <tr>
     *       <td>true</td>
     *       <td>true</td>
     *       <td>0</td>
     *     </tr>
     *     <tr>
     *       <td>true</td>
     *       <td>false</td>
     *       <td>1</td>
     *     </tr>
     *     <tr>
     *       <td>false</td>
     *       <td>true</td>
     *       <td>-1</td>
     *     <tr>
     *       <td>false</td>
     *       <td>false</td>
     *       <td>ClientBinding.priorityComp.compare(b1, b2) </td>
     *     </tr>
     *     </tr>
     *   </tbody>
     * </table>
     */
    private static int statecomp( final ClientBinding b1, final boolean u1,
            final ClientBinding b2, final boolean u2 )
    {
        return u1 ? ( u2 ? 0 : 1 ) : ( u2 ? -1 : ClientBinding.PRIORITY_COMP.compare( b1, b2 ) );
    }

    /**
     * Pass to string this object
     */
    public String toString( final org.omg.CORBA.Object self )
    {
        try
        {
            final Address[] addrs = getAddresses( self );
            final List iorList = new ArrayList();
            for ( int i = 0; i < addrs.length; ++i )
            {
                if ( addrs[ i ] != null )
                {
                    final IORAddressingInfo info =
                            addrs[ i ].getTargetAddress( ReferenceAddr.value ).ior();
                    final List[] profs = new ArrayList[info.ior.profiles.length];
                    iorList.add( profs );
                    List prof =
                            profs[ info.selected_profile_index ] = new ArrayList();
                    prof.add( addrs[ i ] );
                    for ( int j = i + 1; j < addrs.length; ++j )
                    {
                        if ( addrs[ j ] != null )
                        {
                            final IORAddressingInfo info2 =
                                    addrs[ j ].getTargetAddress( ReferenceAddr.value ).ior();
                            if ( compareIORs( info.ior, info2.ior ) )
                            {
                                prof = profs[ info2.selected_profile_index ];
                                if ( prof == null )
                                {
                                    prof = new ArrayList();
                                    profs[ info2.selected_profile_index ] = prof;
                                }
                                prof.add( addrs[ j ] );
                                addrs[ j ] = null;
                            }
                        }
                    }
                }
            }
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final HexPrintStream hps =
                    new HexPrintStream( os, HexPrintStream.FORMAT_MIXED );
            final PrintStream ps = new PrintStream( os );
            ps.println( "Binding information for object " + self.getClass()
                  + ":" + Integer.toHexString( System.identityHashCode( self ) ) );
            final String[] ids = ( ( ObjectImpl ) self )._ids();
            if ( ids.length != 0 )
            {
                ps.println( "Repository IDs from Object: " );
                for ( int i = 0; i < ids.length; ++i )
                {
                    ps.println( "    " + ids[i] );
                }
            }
            if ( iorList.size() > 1 )
            {
                ps.println( "There have been " + ( iorList.size() - 1 )
                      + " redirections of this object" );
            }
            for ( int i = 0; i < iorList.size(); ++i )
            {
                final ArrayList[] profs = ( ArrayList[] ) iorList.get( i );
                int totProfs = 0;
                int profOne = -1;
                for ( int j = profs.length - 1; j >= 0; --j )
                {
                    if ( profs[ j ] != null )
                    {
                        totProfs++;
                        profOne = j;
                    }
                }
                if ( totProfs == 0 )
                {
                    continue;
                }
                final IOR ior = ( ( Address )
                      profs[ profOne ].get( 0 ) ).getTargetAddress( ReferenceAddr.value ).ior().ior;
                ps.print( "IOR #" + i );
                if ( compareIORs( ior, _getIOR() ) )
                {
                    ps.println( " (orginal IOR)" );
                }
                else if ( compareIORs( ior, ior() ) )
                {
                    ps.println( " (from latest permanent redirect)" );
                }
                else
                {
                    ps.println( " (from redirect)" );
                }
                ps.println( "Published RepoID: " + ior.type_id );
                ps.println( "Bound profiles: " + totProfs );
                Address addr0 = null;
                for ( int j = 0; j < profs.length; ++j )
                {
                    if ( profs[ j ] != null )
                    {
                        ps.print( "Profile #" + j );
                        addr0 = ( Address ) profs[ j ].get( 0 );
                        switch ( profs[ j ].size() )
                        {
                            case 0:
                                ps.println( " no endpoints found" );
                                break;

                            case 1:
                                ps.println( " endpoint: "
                                      + addr0.getEndpointDescription() );
                                break;

                            default:
                                ps.println( " endpoints:" );

                                for ( int k = 0; k < profs[ j ].size(); ++k )
                                {
                                    ps.println( "    "
                                          + ( ( Address ) profs[ j ].get( k )
                                          ).getEndpointDescription() );
                                }
                        }

                        ps.print( addr0.getObjectKeyDescription() );
                        int profCpts = addr0.get_profile_components();

                        if ( profCpts > 0 )
                        {
                            ps.println( "Components:" );

                            for ( int k = 0; k < profCpts; ++k )
                            {
                                Object data = addr0.get_component_data( k );

                                if ( data == null )
                                {
                                    ps.println( "Unknown Component. Tag: "
                                          + addr0.get_component( k ).tag + " Data:" );

                                    ps.flush();
                                    hps.write( addr0.get_component( k ).component_data );
                                    hps.flush();
                                    ps.println();
                                }
                                else
                                {
                                    ps.println( data.toString() );
                                }
                            }
                        }
                    }
                }

                final int profCpts = addr0.get_profile_components();
                final int total = addr0.get_components().length;
                if ( profCpts < total )
                {
                    ps.println( "Components from Multi-Component Profile:" );
                    for ( int k = profCpts; k < total; ++k )
                    {
                        final Object data = addr0.get_component_data( k );
                        if ( data == null )
                        {
                            ps.println( "Unknown Component. Tag: "
                                  + addr0.get_component( k ).tag + " Data:" );
                            ps.flush();
                            hps.write( addr0.get_component( k ).component_data );
                            hps.flush();
                            ps.println();
                        }
                        else
                        {
                            ps.println( data.toString() );
                        }
                    }
                }
            }
            return os.toString();
        }
        catch ( final IOException e )
        {
            getLogger().error( "Unexpected IOException.", e );
            throw ExceptionTool.initCause( new RuntimeException(
                    "Unexpected Exception (" + e + ")" ), e );
        }
    }
}
