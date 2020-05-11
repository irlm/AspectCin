/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.policy;

/**
 *
 * @author Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/07/22 12:25:45 $
 */
public interface PolicySetManager
{
    /** ??? */
    int GLOBAL_POLICY_DOMAIN = -1;
    /** ??? */
    int CLIENT_POLICY_DOMAIN = 0;
    /** ??? */
    int SERVER_POLICY_DOMAIN = 1;

    /**
     * Create a new policy set with no membership restrictions.
     */
    org.omg.CORBA.PolicyManagerOperations create_policy_set();

    /**
     * This operation creates new policy set objects. To create a policy set
     * which excludes certain policy types use this method.
     */
    org.omg.CORBA.PolicyManagerOperations create_policy_set( int policy_domain );

    /**
     * Add an invalid policy type to a policy domain.
     */
    void add_invalid_policy_type( int policy_domain, int policy_type );

    /**
     * Add a new policy domain. A policy domain excludes certain policy types,
     * attempting to set override policies of that type results in InvalidPolicies
     * exceptions.
     *
     * @return new policy domain id.
     */
    int add_policy_domain();
}

