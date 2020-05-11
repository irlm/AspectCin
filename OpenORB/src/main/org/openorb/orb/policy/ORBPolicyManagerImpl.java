/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.policy;

import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.Policy;

import org.openorb.orb.core.MinorCodes;

import org.openorb.util.NumberCache;

/**
 *
 * @author Chris Wood
 * @version $Revision: 1.7 $ $Date: 2004/05/13 04:09:27 $
 */
public class ORBPolicyManagerImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.CORBA.PolicyManager,
               org.openorb.orb.policy.PolicyReconciler,
               org.openorb.orb.policy.PolicyFactoryManager,
               org.openorb.orb.policy.PolicySetManager,
               org.omg.PortableInterceptor.PolicyFactory
{
    private PolicySet m_policy_set = null;

    /** Reference to the policy factories. */
    private Map m_factories;

    private List m_domain_sets = new ArrayList( 2 );
    private Map m_policy_reconcilers = null;

    /**
     * Constructor.
     */
    public ORBPolicyManagerImpl()
    {
        add_policy_domain();
        add_policy_domain();
    }

    // policy manager interface

    /**
     * Modifies the current set of overrides with the requested list
     * of Policy overrides. The first parameter policies is a sequence
     * of references to Policy objects. The second parameter set_add
     * of type SetOverrideType indicates whether these policies should
     * be added onto any other overrides that already exist
     * (ADD_OVERRIDE) in the PolicyManager, or they should be added to
     * a clean PolicyManager free of any other overrides
     * (SET_OVERRIDE). Invoking set_policy_overrides with an empty
     * sequence of policies and a mode of SET_OVERRIDE removes all
     * overrides from a PolicyManager.
     */
    public void set_policy_overrides( Policy[] policies, org.omg.CORBA.SetOverrideType set_add )
        throws org.omg.CORBA.InvalidPolicies
    {
        if ( m_policy_set == null )
        {
            m_policy_set = new PolicySet( null, -1 );
        }
        m_policy_set.set_policy_overrides( policies, set_add );
    }

    /**
     * Returns a PolicyList containing the overridden Polices for the
     * requested PolicyTypes. If the specified sequence is empty, all
     * Policy overrides at this scope will be returned. If none of the
     * requested PolicyTypes are overridden at the target
     * PolicyManager, an empty sequence is returned.
     */
    public Policy[] get_policy_overrides( int[] ts )
    {
        if ( m_policy_set == null )
        {
            return new Policy[ 0 ];
        }
        return m_policy_set.get_policy_overrides( ts );
    }

    // policy set creation

    /**
     * Create a new policy set with no membership restrictions.
     */
    public org.omg.CORBA.PolicyManagerOperations create_policy_set()
    {
        return new PolicySet( null, -1 );
    }

    /**
     * This operation creates new policy manager objects. To create a policy set
     * which excludes certain policy types use this method.
     */
    public org.omg.CORBA.PolicyManagerOperations create_policy_set( int policy_domain )
    {
        return new PolicySet( this, policy_domain );
    }

    // policy domain management

    /**
     * Add an invalid policy type to a policy domain.
     */
    public void add_invalid_policy_type( int policy_domain, int policy_type )
    {
        ( ( Set ) m_domain_sets.get( policy_domain ) ).add( NumberCache.getInteger( policy_type ) );
    }

    /**
     * Add a new policy domain. A policy domain excludes certain policy types,
     * attempting to set override policies of that type results in InvalidPolicies
     * exceptions.
     *
     * @return new policy domain id.
     */
    public int add_policy_domain()
    {
        int ret = m_domain_sets.size();
        m_domain_sets.add( new TreeSet() );
        return ret;
    }

    // policy reconciler interface.

    /**
     * Merge policies of given type from the given domains.
     *
     * @param policy_type Policy type. client_policy and profile_policy must
     *                    be of this type.
     * @param client_policy effective client override. (may be null)
     * @param profile_policy policy specified in profile. (may be null)
     * @param domain_managers domain managers.
     *
     * @return Merged policy, or null for no policy of given type.
     * @throws org.omg.CORBA.INV_POLICY if merge cannot be performed.
     */
    public Policy reconcile_policies( int policy_type,
                                      Policy client_policy,
                                      Policy profile_policy,
                                      org.omg.CORBA.DomainManager[] domain_managers )
    {
        if ( ( client_policy != null && policy_type != client_policy.policy_type() )
                || ( profile_policy != null && policy_type != profile_policy.policy_type() ) )
        {
            String msg = "Policy types don't match: ";
            if ( client_policy != null && policy_type != client_policy.policy_type() )
            {
                msg += "policy type '" + policy_type + "' and client policy '"
                      + client_policy.policy_type() + "'.";
            }
            if ( profile_policy != null && policy_type != profile_policy.policy_type() )
            {
                msg += "policy type '" + policy_type + "' and profile policy '"
                      + profile_policy.policy_type() + "'.";
            }
            throw new org.omg.CORBA.INV_POLICY( msg,
                  MinorCodes.INV_POLICY_MERGE_FAILED, CompletionStatus.COMPLETED_MAYBE );
        }

        if ( m_policy_reconcilers != null )
        {
            PolicyReconciler recon = ( PolicyReconciler ) m_policy_reconcilers.get(
                    NumberCache.getInteger( policy_type ) );
            if ( recon != null )
            {
                return recon.reconcile_policies( policy_type, client_policy,
                        profile_policy, domain_managers );
            }
        }

        if ( client_policy == null && profile_policy != null )
        {
            return profile_policy;
        }
        if ( client_policy != null && profile_policy == null )
        {
            return client_policy;
        }
        if ( client_policy == null && profile_policy == null && domain_managers != null )
        {
            Policy ret = null;
            int i = 0;

            // loop until we found a domain manager on which the policy_type is known
            for ( ; i < domain_managers.length; ++i )
            {
                try
                {
                    ret = domain_managers[ i ].get_domain_policy( policy_type );
                    break;
                }
                catch ( org.omg.CORBA.INV_POLICY ex )
                {
                    // we get an INV_POLICY each time the policy_type can't be found
                }
            }

            // loop the remaining domain managers until another manager is found where the
            // policy type is known
            for ( ; i < domain_managers.length; ++i )
            {
                try
                {
                    domain_managers[ i ].get_domain_policy( policy_type );
                    ret = null;
                    break;
                }
                catch ( org.omg.CORBA.INV_POLICY ex )
                {
                    // we get an INV_POLICY each time the policy_type can't be found
                }
            }

            if ( ret != null )
            {
                return ret;
            }
        }

        throw new org.omg.CORBA.INV_POLICY( "Could't find a match for policy type '"
              + policy_type + "'.", MinorCodes.INV_POLICY_MERGE_FAILED,
              CompletionStatus.COMPLETED_MAYBE );
    }

    public void add_policy_reconciler( int policy_type, PolicyReconciler reconciler )
    {
        if ( m_policy_reconcilers == null )
        {
            m_policy_reconcilers = new HashMap();
        }
        if ( reconciler == null )
        {
            m_policy_reconcilers.remove( NumberCache.getInteger( policy_type ) );
        }
        else
        {
            m_policy_reconcilers.put( NumberCache.getInteger( policy_type ), reconciler );
        }
    }

    /**
     * This is used by policy set objects to check for policy validitiy.
     */
    boolean invalid_policy( int policy_domain, int policy_type )
    {
        if ( policy_domain < 0 )
        {
            return false;
        }
        return ( ( Set ) m_domain_sets.get( policy_domain ) ).contains(
                NumberCache.getInteger( policy_type ) );
    }

    // Policy factory interface.

    /**
      * add a policy factory.
     */
    public void add_policy_factory( int type, org.omg.PortableInterceptor.PolicyFactory factory )
    {
        if ( m_factories == null )
        {
            m_factories = new HashMap();
        }
        if ( factory == this )
        {
            return;
        }
        if ( factory == null )
        {
            m_factories.remove( NumberCache.getInteger( type ) );
        }
        else
        {
            m_factories.put( NumberCache.getInteger( type ), factory );
        }
    }

    /**
     * Invoke the manager to create a policy.
     */
    public org.omg.CORBA.Policy create_policy( int type, org.omg.CORBA.Any val )
        throws org.omg.CORBA.PolicyError
    {
        org.omg.PortableInterceptor.PolicyFactory factory =
                ( org.omg.PortableInterceptor.PolicyFactory ) m_factories.get(
                NumberCache.getInteger( type ) );

        if ( factory == null )
        {
            throw new org.omg.CORBA.PolicyError( org.omg.CORBA.BAD_POLICY_VALUE.value );
        }
        return factory.create_policy( type, val );
    }
}

