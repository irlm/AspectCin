/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * This class is used to return all component tag required to complete an IOR.
 *
 * @author Jerome Daniel
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:51 $
 */
public class ComponentSet
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.IORInfo
{
    /**
     * Reference to the ORB
     */
    private org.omg.CORBA.ORB m_orb;

    /**
    * Reference to the policy reconciler
    */
    private org.openorb.orb.policy.PolicyReconciler m_orb_reconciler;

    /**
     * Reference to the domain managers
     */
    private org.omg.CORBA.DomainManager [] m_domain_managers;

    /**
     * Reference to the Policy manager
     */
    private org.omg.CORBA.PolicyManagerOperations m_oa_policies;

    /**
     * Specific tagged components
     */
    private Map m_profile_components = new HashMap();

    /**
     * Constructor
     */
    public ComponentSet( org.omg.CORBA.ORB orb,
                         org.omg.CORBA.PolicyManagerOperations oaPolicies,
                         org.omg.CORBA.DomainManager [] domainManagers )
    {
        m_orb = orb;

        org.openorb.orb.core.ORB openorb = ( org.openorb.orb.core.ORB ) orb;
        m_orb_reconciler = ( org.openorb.orb.policy.PolicyReconciler )
            openorb.getFeature( "PolicyReconciler" );

        m_oa_policies = oaPolicies;

        m_domain_managers = domainManagers;
    }

    /**
     * This operation is used to apply the interception point for
     * IOR Interceptors.
     */
    public void interception_point()
    {
        m_profile_components.clear();

        org.openorb.orb.pi.IORManager ior_manager = ( org.openorb.orb.pi.IORManager )
            ( ( org.openorb.orb.core.ORB )
            m_orb ).getFeature( "IORInterceptorManager" );

        if ( ior_manager != null )
        {
            ior_manager.establish_components( this );
        }
    }

    /**
     * Return an effective policy.
     */
    public org.omg.CORBA.Policy get_effective_policy( int type )
    {
        org.omg.CORBA.Policy oa_policy = null;

        if ( m_oa_policies != null )
        {
            int [] types = new int[ 1 ];
            types[ 0 ] = type;

            org.omg.CORBA.Policy [] policies =
                m_oa_policies.get_policy_overrides( types );

            if ( policies != null && policies.length > 0 )
            {
                oa_policy = policies[ 0 ];
            }
        }

        return m_orb_reconciler.reconcile_policies( type, null,
            oa_policy, m_domain_managers );
    }

    /**
     * Add a tagged component for all profiles.
     */
    public void add_ior_component( org.omg.IOP.TaggedComponent component )
    {
        add_ior_component_to_profile( component,
            org.omg.IOP.TAG_MULTIPLE_COMPONENTS.value );
    }

    /**
     * Add a tagged component for a specific profile.
     */
    public void add_ior_component_to_profile(
        org.omg.IOP.TaggedComponent component, int profile_id )
    {
        synchronized ( m_profile_components )
        {
            ArrayList components = ( ArrayList )
                m_profile_components.get( new Integer( profile_id ) );

            if ( components == null )
            {
                components = new ArrayList();

                m_profile_components.put(
                    new Integer( profile_id ), components );
            }

            if ( component != null )
            {
                components.add( component );
            }
        }
    }

    /**
     * Return tagged component for a specific profile.
     */
    public org.omg.IOP.TaggedComponent [] getComponents( int profile_id )
    {
        synchronized ( m_profile_components )
        {
            ArrayList list = ( ArrayList ) m_profile_components.get(
                new Integer( profile_id ) );

            if ( list == null )
            {
                return new org.omg.IOP.TaggedComponent[ 0 ];
            }
            org.omg.IOP.TaggedComponent [] tagged =
                new org.omg.IOP.TaggedComponent[ list.size() ];

            list.toArray( tagged );

            return tagged;
        }
    }
}

