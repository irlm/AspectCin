/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.policy;

import java.util.NoSuchElementException;

import org.apache.avalon.framework.CascadingRuntimeException;

import org.openorb.util.CurrentStack;

/**
 *
 * @author Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:51 $
 */
public class PolicyCurrentImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.CORBA.PolicyCurrent
{
    private CurrentStack m_curr_stack = new CurrentStack();
    private ORBPolicyManagerImpl m_manager;

    public PolicyCurrentImpl( ORBPolicyManagerImpl manager )
    {
        m_manager = manager;
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
        peek().set_policy_overrides( policies, set_add );
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
        return peek().get_policy_overrides( ts );
    }

    private PolicySet peek()
    {
        PolicySet nset = null;

        try
        {
            nset = ( PolicySet ) m_curr_stack.peek();

            if ( nset == null )
            {
                nset = new PolicySet( m_manager,
                        PolicySetManager.CLIENT_POLICY_DOMAIN );
                m_curr_stack.set( nset );
            }
        }
        catch ( NoSuchElementException ex )
        {
            nset = new PolicySet( m_manager,
                    PolicySetManager.CLIENT_POLICY_DOMAIN );
            m_curr_stack.push( nset );
        }

        return nset;
    }

    public void push()
    {
        m_curr_stack.push( null );
    }

    public void pop()
    {
        try
        {
            m_curr_stack.pop();
        }
        catch ( NoSuchElementException ex )
        {
            throw new CascadingRuntimeException( "No more elements on the stack.", ex );
        }
    }
}

