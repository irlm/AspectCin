/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import java.util.Map;
import java.util.HashMap;

import org.omg.CORBA.Policy;

import org.apache.avalon.framework.logger.Logger;
import org.openorb.util.NumberCache;

/**
 * Base class which implements some of the ClientRequest functionality.
 *
 * @author Chris Wood
 * @version $Revision: 1.9 $ $Date: 2004/05/13 23:54:13 $
 */
public abstract class AbstractClientRequest
    extends org.omg.CORBA.LocalObject
    implements ClientRequest
{
    private org.omg.CORBA.ORB m_orb;

    private org.omg.CORBA.Object m_target;
    private org.openorb.orb.core.Delegate m_target_deleg;
    private org.omg.IOP.IOR m_target_ior;

    private org.omg.CORBA.Object m_effective_target;
    private org.omg.IOP.IOR m_effective_target_ior;

    private ClientChannel m_channel;
    private Address m_address;

    private org.openorb.orb.pi.CurrentImpl m_pi_current;
    private org.omg.PortableInterceptor.CurrentOperations m_pi_curr_entry;

    private org.openorb.orb.policy.PolicyReconciler m_orb_reconciler = null;

    private Map m_service_contexts = new HashMap();

    private int m_request_id;

    private Logger m_logger = null;


    /** Creates new AbstractClientRequest */
    public AbstractClientRequest( int request_id, org.omg.CORBA.Object target,
            Address address, ClientChannel channel )
    {
        m_address = address;
        m_channel = channel;
        m_target = target;
        m_request_id = request_id;

        m_target_deleg = ( org.openorb.orb.core.Delegate )
                ( ( org.omg.CORBA.portable.ObjectImpl ) target )._get_delegate();

        m_target_ior = m_target_deleg.ior();

        m_orb = ( ( org.omg.CORBA.portable.ObjectImpl ) target )._orb();

        m_pi_current = ( ( org.openorb.orb.core.ORB ) m_orb ).getPICurrent();
        m_pi_curr_entry = m_pi_current.copy( m_pi_current.get() );
    }

    /**
     * Client channel
     */
    public ClientChannel channel()
    {
        return m_channel;
    }

    /**
     * Client address
     */
    public Address address()
    {
        return m_address;
    }

    /**
     * Request ID.
     */
    public int request_id()
    {
        return m_request_id;
    }

    /**
     * Equality depends on request IDs
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof ClientRequest )
        {
            return m_request_id == ( ( ClientRequest ) obj ).request_id();
        }
        return false;
    }

    /**
     * Makes sure that equal requests have the same hashCode.
     */
    public int hashCode()
    {
        return m_request_id;
    }

    /**
     * Get the target IOR. This ior is the marshalling ior of the object passed
     * at creation time.
     */
    public org.omg.IOP.IOR target_ior()
    {
        return m_target_ior;
    }

    /**
     * Object the request was created with.
     */
    public org.omg.CORBA.Object target()
    {
        return m_target;
    }

    /**
     * ORB associated with the target.
     */
    public org.omg.CORBA.ORB orb()
    {
        return m_orb;
    }

    /**
     * Get the effective target IOR. This gets the IOR from the address passed
     * in creation. Calling this function instead of effective_target avoids
     * constructing an enclosing delegate/object.
     */
    public org.omg.IOP.IOR effective_target_ior()
    {
        if ( m_effective_target_ior == null )
        {
            m_effective_target_ior =
                    m_address.getTargetAddress( org.omg.GIOP.ReferenceAddr.value ).ior().ior;
        }
        return m_effective_target_ior;
    }

    /**
     * Return the effective_target object. This is the destination for this request.
     */
    public org.omg.CORBA.Object effective_target()
    {
        if ( m_effective_target == null )
        {
            m_effective_target = new org.openorb.orb.core.ObjectStub(
                  m_orb, effective_target_ior() );
        }
        return m_effective_target;
    }

    /**
      * Profile as used in this request.
      */
    public org.omg.IOP.TaggedProfile effective_profile()
    {
        return m_address.getTargetAddress( org.omg.GIOP.ProfileAddr.value ).profile();
    }

    public org.omg.IOP.TaggedComponent get_effective_component( int id )
    {
        return ( get_effective_components( id ) ) [ 0 ];
    }

    public org.omg.IOP.TaggedComponent[] get_effective_components( int id )
    {
        org.omg.IOP.TaggedComponent[] ret = m_address.get_components( id );

        if ( ret == null || ret.length == 0 )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 25,
                    state_completion_status() );
        }
        return ret;
    }

    /**
     * Checks to see if the specified request policy exists.
     *
     * @param type the policy type
     * @return true if the policy exists, false otherwise
     */
    protected boolean requestPolicyExists( final int type )
    {
        final Policy clientPolicy = m_target_deleg.get_client_policy( m_target, type );

        final int[] types = new int[] { type };
        final Policy[] targetPols = m_address.get_target_policies( types );
        final Policy targetPolicy = ( 0 < targetPols.length ) ? targetPols[0] : null;

        return ( null != clientPolicy ) || ( null != targetPolicy );
    }

    public org.omg.CORBA.Policy get_request_policy( int type )
    {
        org.omg.CORBA.Policy client_policy = m_target_deleg.get_client_policy( m_target, type );

        org.omg.CORBA.Policy target_policy = null;
        {
            int [] types = new int[ 1 ];
            types[ 0 ] = type;
            org.omg.CORBA.Policy [] target_pols = m_address.get_target_policies( types );

            if ( target_pols.length > 0 )
            {
                target_policy = target_pols[ 0 ];
            }
        }

        if ( client_policy == null && target_policy == null )
        {
            throw new org.omg.CORBA.INV_POLICY( "Policy type not found",
                    org.omg.CORBA.OMGVMCID.value | 1, state_completion_status() );
        }

        if ( m_orb_reconciler == null )
        {
            m_orb_reconciler = ( org.openorb.orb.policy.PolicyReconciler )
                    ( ( org.openorb.orb.core.ORB ) m_orb ).getFeature( "PolicyReconciler" );

            if ( m_orb_reconciler == null )
            {
                throw new org.omg.CORBA.INTERNAL( "PolicyReconciler unavailable." );
            }
        }

        return m_orb_reconciler.reconcile_policies( type, client_policy, target_policy, null );
    }

    public org.omg.Dynamic.Parameter[] arguments()
    {
        throw new org.omg.CORBA.NO_RESOURCES( org.omg.CORBA.OMGVMCID.value | 1,
                state_completion_status() );
    }

    public org.omg.CORBA.TypeCode[] exceptions()
    {
        throw new org.omg.CORBA.NO_RESOURCES( org.omg.CORBA.OMGVMCID.value | 1,
                state_completion_status() );
    }

    public java.lang.String[] contexts()
    {
        throw new org.omg.CORBA.NO_RESOURCES( org.omg.CORBA.OMGVMCID.value | 1,
                state_completion_status() );
    }

    public java.lang.String[] operation_context()
    {
        throw new org.omg.CORBA.NO_RESOURCES( org.omg.CORBA.OMGVMCID.value | 1,
                state_completion_status() );
    }

    public org.omg.CORBA.Any result()
    {
        throw new org.omg.CORBA.NO_RESOURCES( org.omg.CORBA.OMGVMCID.value | 1,
                state_completion_status() );
    }

    public org.omg.CORBA.Any get_slot( int id )
        throws org.omg.PortableInterceptor.InvalidSlot
    {
        synchronized ( m_service_contexts )
        {
            if ( m_pi_curr_entry == null )
            {
                m_pi_curr_entry = m_pi_current.create();
            }
        }

        return m_pi_curr_entry.get_slot( id );
    }

    public void add_request_service_context( org.omg.IOP.ServiceContext service_context,
            boolean replace )
    {
        if ( state() != STATE_CREATED )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                    state_completion_status() );
        }
        Integer key = NumberCache.getInteger( service_context.context_id );

        synchronized ( m_service_contexts )
        {
            if ( !replace && m_service_contexts.containsKey( key ) )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 11,
                        state_completion_status() );
            }
            m_service_contexts.put( key, service_context );
        }
    }

    public org.omg.IOP.ServiceContext get_request_service_context( int id )
    {
        org.omg.IOP.ServiceContext ret = null;

        synchronized ( m_service_contexts )
        {
            ret = ( org.omg.IOP.ServiceContext ) m_service_contexts.get(
                    NumberCache.getInteger( id ) );
        }

        if ( ret == null )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 23,
                    state_completion_status() );
        }
        return ret;
    }

    /**
     * Get all request service contexts that have been set. From MARSHAL onward
     * this will be a constant list.
     */
    public org.omg.IOP.ServiceContext [] get_request_service_contexts()
    {
        synchronized ( m_service_contexts )
        {
            return ( org.omg.IOP.ServiceContext[] )
                    m_service_contexts.values().toArray(
                    new org.omg.IOP.ServiceContext[ m_service_contexts.size() ] );
        }
    }

    /**
     * This utility function returns the correct completion status to use
     * in a system exception depending on the current state.
     */
    public org.omg.CORBA.CompletionStatus state_completion_status()
    {
        switch ( state() )
        {

        case STATE_CREATED:

        case STATE_MARSHAL:
            return org.omg.CORBA.CompletionStatus.COMPLETED_NO;

        case STATE_WAITING:
            return org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE;

        case STATE_UNMARSHAL:
            return org.omg.CORBA.CompletionStatus.COMPLETED_YES;

        case STATE_COMPLETE:

            switch ( reply_status() )
            {

            case OBJECT_HERE:

            case UNKNOWN_OBJECT:

            case org.omg.PortableInterceptor.SUCCESSFUL.value:

            case org.omg.PortableInterceptor.USER_EXCEPTION.value:
                return org.omg.CORBA.CompletionStatus.COMPLETED_YES;

            case org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value:
                return received_system_exception().completed;

            case org.omg.PortableInterceptor.LOCATION_FORWARD.value:

            case org.omg.PortableInterceptor.TRANSPORT_RETRY.value:
                return org.omg.CORBA.CompletionStatus.COMPLETED_NO;
            }
        }

        final Error e = new Error( "Invalid state of the state machine." );
        getLogger().fatalError( e.getMessage(), e );
        throw e;
    }

    protected Logger getLogger()
    {
        if ( null == m_logger )
        {
            m_logger = ( ( org.openorb.orb.core.ORBSingleton ) orb() ).getLogger();
        }
        return m_logger;
    }
}

