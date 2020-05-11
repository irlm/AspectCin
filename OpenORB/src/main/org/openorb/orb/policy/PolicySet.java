/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.policy;

import java.util.Map;
import java.util.HashMap;

import org.openorb.util.NumberCache;

/**
 * Basic policy set management.
 *
 * @author Chris Wood
 * @version $Revision: 1.6 $ $Date: 2004/07/22 12:25:45 $
 */
class PolicySet
    implements org.omg.CORBA.PolicyManagerOperations
{
    private static final org.omg.CORBA.Policy[] EMPTY = new org.omg.CORBA.Policy[ 0 ];
    private int m_policy_domain;
    private ORBPolicyManagerImpl m_manager;
    private Map m_policy_set;

    public PolicySet( ORBPolicyManagerImpl manager, int policyDomain )
    {
        m_manager = ( policyDomain < 0 ) ? null : manager;
        m_policy_domain = policyDomain;
    }

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
    public void set_policy_overrides( org.omg.CORBA.Policy[] policies,
            org.omg.CORBA.SetOverrideType set_add )
    throws org.omg.CORBA.InvalidPolicies
    {
        if ( m_manager != null && policies != null )
        {
            int invalid = 0;
            int [] invalids = null;

            for ( int i = 0; i < policies.length; ++i )
            {
                if ( policies[ i ] != null
                      && m_manager.invalid_policy( m_policy_domain, policies[ i ].policy_type() ) )
                {
                    if ( invalids == null )
                    {
                        invalids = new int[ policies.length - i ];
                    }
                    invalids[ invalid++ ] = i;
                }
            }
            if ( invalid > 0 )
            {
                org.omg.CORBA.InvalidPolicies pol = new org.omg.CORBA.InvalidPolicies();
                pol.indices = new short[ invalid ];

                for ( int i = 0; i < invalid; ++i )
                {
                    pol.indices[ i ] = ( short ) invalids[ i ];
                }
                throw pol;
            }
        }

        synchronized ( this )
        {
            if ( m_policy_set == null )
            {
                m_policy_set = new HashMap();
            }
            else if ( set_add == org.omg.CORBA.SetOverrideType.SET_OVERRIDE )
            {
                m_policy_set.clear();
            }
            if ( policies == null || policies.length == 0 )
            {
                return;
            }
            for ( int i = 0; i < policies.length; ++i )
            {
                if ( policies[ i ] != null )
                {
                    m_policy_set.put( NumberCache.getInteger( policies[ i ].policy_type() ),
                            policies[ i ] );
                }
            }
        }
    }

    /**
     * Returns a PolicyList containing the overridden Polices for the
     * requested PolicyTypes. If the specified sequence is empty, all
     * Policy overrides at this scope will be returned. If none of the
     * requested PolicyTypes are overridden at the target
     * PolicyManager, an empty sequence is returned.
     */
    public org.omg.CORBA.Policy[] get_policy_overrides( int[] ts )
    {
        int fill;
        org.omg.CORBA.Policy [] ret;

        synchronized ( this )
        {
            if ( m_policy_set == null )
            {
                return EMPTY;
            }
            if ( ts == null || ts.length == 0 )
            {
                org.omg.CORBA.Policy [] active = new org.omg.CORBA.Policy[ m_policy_set.size() ];
                active = ( org.omg.CORBA.Policy[] ) m_policy_set.values().toArray( active );
                return active;
            }

            fill = 0;
            ret = new org.omg.CORBA.Policy[ ts.length ];

            for ( int i = 0; i < ts.length; ++i )
            {
                ret[ fill ] = ( org.omg.CORBA.Policy ) m_policy_set.get(
                        NumberCache.getInteger( ts[ i ] ) );

                if ( ret[ fill ] != null )
                {
                    ++fill;
                }
            }
        }

        if ( fill != ret.length )
        {
            org.omg.CORBA.Policy [] tmp = new org.omg.CORBA.Policy[ fill ];
            System.arraycopy( ret, 0, tmp, 0, fill );
            ret = tmp;
        }
        return ret;
    }
}

