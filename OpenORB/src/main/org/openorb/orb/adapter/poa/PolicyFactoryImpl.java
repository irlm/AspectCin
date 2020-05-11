/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.poa;

import org.omg.PortableServer.IdAssignmentPolicy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicy;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicy;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicy;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.ThreadPolicy;
import org.omg.PortableServer.ThreadPolicyValue;
import org.omg.PortableServer.THREAD_POLICY_ID;
import org.omg.PortableServer.ThreadPolicyValueHelper;
import org.omg.PortableServer.LIFESPAN_POLICY_ID;
import org.omg.PortableServer.LifespanPolicyValueHelper;
import org.omg.PortableServer.ID_UNIQUENESS_POLICY_ID;
import org.omg.PortableServer.IdUniquenessPolicyValueHelper;
import org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID;
import org.omg.PortableServer.IdAssignmentPolicyValueHelper;
import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;
import org.omg.PortableServer.ServantRetentionPolicyValueHelper;
import org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID;
import org.omg.PortableServer.RequestProcessingPolicyValueHelper;
import org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID;
import org.omg.PortableServer.ImplicitActivationPolicyValueHelper;

/**
 * This class is the implemenatation of the PolicyFactory for the POA.
 * It provides methods for creating the POA related policies.
 *
 * @author Unknown
 */
final class PolicyFactoryImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.PolicyFactory
{
    private static PolicyFactoryImpl s_instance = null;

    private PolicyFactoryImpl()
    {
    }

    public static PolicyFactoryImpl getInstance()
    {
        if ( s_instance == null )
        {
            s_instance = new PolicyFactoryImpl();
        }
        return s_instance;
    }

    public org.omg.CORBA.Policy create_policy( int type, org.omg.CORBA.Any value )
        throws org.omg.CORBA.PolicyError
    {
        switch ( type )
        {

        case THREAD_POLICY_ID.value:

            if ( !value.type().equal( ThreadPolicyValueHelper.type() ) )
            {
                throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY_TYPE.value );
            }
            return create_thread_policy( ThreadPolicyValueHelper.extract( value ).value() );

        case LIFESPAN_POLICY_ID.value:
            if ( !value.type().equal( LifespanPolicyValueHelper.type() ) )
            {
                throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY_TYPE.value );
            }
            return create_lifespan_policy( LifespanPolicyValueHelper.extract( value ).value() );

        case ID_UNIQUENESS_POLICY_ID.value:
            if ( !value.type().equal( IdUniquenessPolicyValueHelper.type() ) )
            {
                throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY_TYPE.value );
            }
            return create_id_uniqueness_policy(
                    IdUniquenessPolicyValueHelper.extract( value ).value() );

        case ID_ASSIGNMENT_POLICY_ID.value:
            if ( !value.type().equal( IdAssignmentPolicyValueHelper.type() ) )
            {
                throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY_TYPE.value );
            }
            return create_id_assignment_policy(
                    IdAssignmentPolicyValueHelper.extract( value ).value() );

        case SERVANT_RETENTION_POLICY_ID.value:
            if ( !value.type().equal( ServantRetentionPolicyValueHelper.type() ) )
            {
                throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY_TYPE.value );
            }
            return create_servant_retention_policy(
                    ServantRetentionPolicyValueHelper.extract( value ).value() );

        case REQUEST_PROCESSING_POLICY_ID.value:
            if ( !value.type().equal( RequestProcessingPolicyValueHelper.type() ) )
            {
                throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY_TYPE.value );
            }
            return create_request_processing_policy(
                    RequestProcessingPolicyValueHelper.extract( value ).value() );

        case IMPLICIT_ACTIVATION_POLICY_ID.value:
            if ( !value.type().equal( ImplicitActivationPolicyValueHelper.type() ) )
            {
                throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY_TYPE.value );
            }
            return create_implicit_activation_policy(
                    ImplicitActivationPolicyValueHelper.extract( value ).value() );

        default:
            throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY.value );
        }
    }

    synchronized ThreadPolicy create_thread_policy( int val )
    {
        if ( s_pol_thread == null )
        {
            s_pol_thread = new ThreadPolicyImpl[ 2 ];
        }
        if ( s_pol_thread[ val ] == null )
        {
            s_pol_thread[ val ] = new ThreadPolicyImpl( val );
        }
        return s_pol_thread[ val ];
    }

    synchronized LifespanPolicy create_lifespan_policy( int val )
    {
        if ( s_pol_lifespan == null )
        {
            s_pol_lifespan = new LifespanPolicyImpl[ 2 ];
        }
        if ( s_pol_lifespan[ val ] == null )
        {
            s_pol_lifespan[ val ] = new LifespanPolicyImpl( val );
        }
        return s_pol_lifespan[ val ];
    }

    synchronized IdUniquenessPolicy create_id_uniqueness_policy( int val )
    {
        if ( s_pol_id_uniqueness == null )
        {
            s_pol_id_uniqueness = new IdUniquenessPolicyImpl[ 2 ];
        }
        if ( s_pol_id_uniqueness[ val ] == null )
        {
            s_pol_id_uniqueness[ val ] = new IdUniquenessPolicyImpl( val );
        }
        return s_pol_id_uniqueness[ val ];
    }

    synchronized IdAssignmentPolicy create_id_assignment_policy( int val )
    {
        if ( s_pol_id_assignment == null )
        {
            s_pol_id_assignment = new IdAssignmentPolicyImpl[ 2 ];
        }
        if ( s_pol_id_assignment[ val ] == null )
        {
            s_pol_id_assignment[ val ] = new IdAssignmentPolicyImpl( val );
        }
        return s_pol_id_assignment[ val ];
    }

    synchronized ServantRetentionPolicy create_servant_retention_policy( int val )
    {
        if ( s_pol_servant_retention == null )
        {
            s_pol_servant_retention = new ServantRetentionPolicyImpl[ 2 ];
        }
        if ( s_pol_servant_retention[ val ] == null )
        {
            s_pol_servant_retention[ val ] = new ServantRetentionPolicyImpl( val );
        }
        return s_pol_servant_retention[ val ];
    }

    synchronized RequestProcessingPolicy create_request_processing_policy( int val )
    {
        if ( s_pol_request_processing == null )
        {
            s_pol_request_processing = new RequestProcessingPolicyImpl[ 3 ];
        }
        if ( s_pol_request_processing[ val ] == null )
        {
            s_pol_request_processing[ val ] = new RequestProcessingPolicyImpl( val );
        }
        return s_pol_request_processing[ val ];
    }

    synchronized ImplicitActivationPolicy create_implicit_activation_policy( int val )
    {
        if ( s_pol_implicit_activation == null )
        {
            s_pol_implicit_activation = new ImplicitActivationPolicyImpl[ 2 ];
        }
        if ( s_pol_implicit_activation[ val ] == null )
        {
            s_pol_implicit_activation[ val ] = new ImplicitActivationPolicyImpl( val );
        }
        return s_pol_implicit_activation[ val ];
    }

    private static class ThreadPolicyImpl
        extends org.omg.CORBA.LocalObject
        implements ThreadPolicy
    {
        private ThreadPolicyValue m_value;

        ThreadPolicyImpl( int val )
        {
            m_value = ThreadPolicyValue.from_int( val );
        }

        public ThreadPolicyValue value()
        {
            return m_value;
        }

        public void destroy()
        {
        }

        public org.omg.CORBA.Policy copy()
        {
            return this;
        }

        public int policy_type()
        {
            return THREAD_POLICY_ID.value;
        }
    }

    private static ThreadPolicyImpl [] s_pol_thread = null;

    private static class LifespanPolicyImpl
        extends org.omg.CORBA.LocalObject
        implements LifespanPolicy
    {
        private LifespanPolicyValue m_value;

        LifespanPolicyImpl( int val )
        {
            m_value = LifespanPolicyValue.from_int( val );
        }

        public LifespanPolicyValue value()
        {
            return m_value;
        }

        public void destroy()
        {
        }

        public org.omg.CORBA.Policy copy()
        {
            return this;
        }

        public int policy_type()
        {
            return LIFESPAN_POLICY_ID.value;
        }
    }

    private static LifespanPolicyImpl [] s_pol_lifespan = null;

    private static class IdUniquenessPolicyImpl
        extends org.omg.CORBA.LocalObject
        implements IdUniquenessPolicy
    {
        private IdUniquenessPolicyValue m_value;

        IdUniquenessPolicyImpl( int val )
        {
            m_value = IdUniquenessPolicyValue.from_int( val );
        }

        public IdUniquenessPolicyValue value()
        {
            return m_value;
        }

        public void destroy()
        {
        }

        public org.omg.CORBA.Policy copy()
        {
            return this;
        }

        public int policy_type()
        {
            return ID_UNIQUENESS_POLICY_ID.value;
        }
    }

    private static IdUniquenessPolicyImpl [] s_pol_id_uniqueness = null;

    private static class IdAssignmentPolicyImpl
        extends org.omg.CORBA.LocalObject
        implements IdAssignmentPolicy
    {
        private IdAssignmentPolicyValue m_value;

        IdAssignmentPolicyImpl( int val )
        {
            m_value = IdAssignmentPolicyValue.from_int( val );
        }

        public IdAssignmentPolicyValue value()
        {
            return m_value;
        }

        public void destroy()
        {
        }

        public org.omg.CORBA.Policy copy()
        {
            return this;
        }

        public int policy_type()
        {
            return ID_ASSIGNMENT_POLICY_ID.value;
        }
    }

    private static IdAssignmentPolicyImpl [] s_pol_id_assignment = null;

    private static class ServantRetentionPolicyImpl
        extends org.omg.CORBA.LocalObject
        implements ServantRetentionPolicy
    {
        private ServantRetentionPolicyValue m_value;

        ServantRetentionPolicyImpl( int val )
        {
            m_value = ServantRetentionPolicyValue.from_int( val );
        }

        public ServantRetentionPolicyValue value()
        {
            return m_value;
        }

        public void destroy()
        {
        }

        public org.omg.CORBA.Policy copy()
        {
            return this;
        }

        public int policy_type()
        {
            return SERVANT_RETENTION_POLICY_ID.value;
        }
    }

    private static ServantRetentionPolicyImpl [] s_pol_servant_retention = null;

    private static class RequestProcessingPolicyImpl
        extends org.omg.CORBA.LocalObject
        implements RequestProcessingPolicy
    {
        private RequestProcessingPolicyValue m_value;

        RequestProcessingPolicyImpl( int val )
        {
            m_value = RequestProcessingPolicyValue.from_int( val );
        }

        public RequestProcessingPolicyValue value()
        {
            return m_value;
        }

        public void destroy()
        {
        }

        public org.omg.CORBA.Policy copy()
        {
            return this;
        }

        public int policy_type()
        {
            return REQUEST_PROCESSING_POLICY_ID.value;
        }
    }

    private static RequestProcessingPolicyImpl [] s_pol_request_processing = null;


    private static class ImplicitActivationPolicyImpl
        extends org.omg.CORBA.LocalObject
        implements ImplicitActivationPolicy
    {
        private ImplicitActivationPolicyValue m_value;

        ImplicitActivationPolicyImpl( int val )
        {
            m_value = ImplicitActivationPolicyValue.from_int( val );
        }

        public ImplicitActivationPolicyValue value()
        {
            return m_value;
        }

        public void destroy()
        {
        }

        public org.omg.CORBA.Policy copy()
        {
            return this;
        }

        public int policy_type()
        {
            return IMPLICIT_ACTIVATION_POLICY_ID.value;
        }
    }

    private static ImplicitActivationPolicyImpl [] s_pol_implicit_activation = null;
}

