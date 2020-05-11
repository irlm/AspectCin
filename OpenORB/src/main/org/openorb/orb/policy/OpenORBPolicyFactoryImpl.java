/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.policy;

import org.omg.CORBA.TCKind;

/**
 * Factory for Various OpenORB specific and general policies.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:51 $
 */
public class OpenORBPolicyFactoryImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.PolicyFactory
{
    private org.omg.CORBA.ORB m_orb;

    private org.omg.CORBA.Policy m_bidir_normal_policy;
    private org.omg.CORBA.Policy m_bidir_both_policy;

    private org.omg.CORBA.Policy m_local_disallow_policy;
    private org.omg.CORBA.Policy m_local_allow_policy;

    /**
     * This constructor registers this factory as the policy factory for the
     * various types of policies it creates.
     */
    public OpenORBPolicyFactoryImpl( org.omg.CORBA.ORB orb,
            PolicyFactoryManager policymanagerimpl, boolean enable_server, boolean enable_client )
    {
        m_orb = orb;

        if ( enable_server )
        {
            policymanagerimpl.add_policy_factory(
                    org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE.value, this );
            policymanagerimpl.add_policy_factory( FORCE_MARSHAL_POLICY_ID.value, this );
        }
    }

    public org.omg.CORBA.Policy create_policy( int type, org.omg.CORBA.Any value )
        throws org.omg.CORBA.PolicyError
    {
        switch ( type )
        {

        case org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE.value:
            return create_bidir( value );

        case FORCE_MARSHAL_POLICY_ID.value:
            return create_local_invoke( value );

        default:
            throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY.value );
        }
    }

    private org.omg.CORBA.Policy create_local_invoke( org.omg.CORBA.Any value )
        throws org.omg.CORBA.PolicyError
    {
        boolean disallow = true;

        switch ( value.type().kind().value() )
        {

        case TCKind._tk_boolean:
            disallow = value.extract_boolean();
            break;

        case TCKind._tk_void:
            break;

        default:
            throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY_TYPE.value );
        }

        if ( disallow )
        {
            if ( m_local_disallow_policy == null )
            {
                m_local_disallow_policy = new ForceMarshalPolicyImpl( disallow );
            }
            return m_local_disallow_policy;
        }
        else
        {
            if ( m_local_allow_policy == null )
            {
                m_local_allow_policy = new ForceMarshalPolicyImpl( disallow );
            }
            return m_local_allow_policy;
        }
    }

    private static class ForceMarshalPolicyImpl
        extends org.omg.CORBA.LocalObject
        implements ForceMarshalPolicy
    {
        /**
         * Constructor.
         */
        ForceMarshalPolicyImpl( boolean value )
        {
            m_value = value;
        }

        /**
         * policy_type read attribute
         */
        public int policy_type()
        {
            return FORCE_MARSHAL_POLICY_ID.value;
        }

        /**
         * Operation copy
         */
        public org.omg.CORBA.Policy copy()
        {
            return this;
        }

        /**
         * Operation destroy
         */
        public void destroy()
        {
        }

        /**
         * Valeur de IdUniquenessPolicy
         */
        private boolean m_value;

        /**
         * value read attribute
         */
        public boolean forceMarshal()
        {
            return m_value;
        }
    }

    private org.omg.CORBA.Policy create_bidir( org.omg.CORBA.Any value )
        throws org.omg.CORBA.PolicyError
    {
        if ( value.type().kind() != TCKind.tk_ushort )
        {
            throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY_TYPE.value );
        }
        short val = value.extract_ushort();

        switch ( val )
        {

        case org.omg.BiDirPolicy.NORMAL.value:
            if ( m_bidir_normal_policy == null )
            {
                m_bidir_normal_policy = new BiDirPolicyImpl( val );
            }
            return m_bidir_normal_policy;

        case org.omg.BiDirPolicy.BOTH.value:
            if ( m_bidir_both_policy == null )
            {
                m_bidir_both_policy = new BiDirPolicyImpl( val );
            }
            return m_bidir_both_policy;

        default:
            throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY_VALUE.value );
        }
    }

    private static class BiDirPolicyImpl
        extends org.omg.CORBA.LocalObject
        implements org.omg.BiDirPolicy.BidirectionalPolicy
    {
        /**
         * Constructor.
         */
        BiDirPolicyImpl( short value )
        {
            m_value = value;
        }

        /**
         * policy_type read attribute
         */
        public int policy_type()
        {
            return org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE.value;
        }

        /**
         * Operation copy
         */
        public org.omg.CORBA.Policy copy()
        {
            return this;
        }

        /**
         * Operation destroy
         */
        public void destroy()
        {
        }

        /**
         * Valeur de IdUniquenessPolicy
         */
        private short m_value;

        /**
         * value read attribute
         */
        public short value()
        {
            return m_value;
        }
    }
}

