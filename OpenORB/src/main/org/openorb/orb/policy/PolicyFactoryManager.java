/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.policy;

import org.omg.PortableInterceptor.PolicyFactory;

/**
 * This interface must be implemented by the policy factory manager.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:51 $
 */
public interface PolicyFactoryManager
{
    /**
     * add a policy factory.
     */
    void add_policy_factory( int type, PolicyFactory factory );
}

