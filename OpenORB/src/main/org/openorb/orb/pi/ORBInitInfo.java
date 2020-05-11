/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

/**
 * This interface must be implemented by the ORB Init Info class. The class
 * is responsible for initializing the orb and setting up the various
 * interceptors.<p>
 * Overrides must have a constructor with exact signature:
 * <pre>
 * public ORBInitInfo( String [] args, org.openorb.orb.core.ORB orb,
 *  org.omg.PortableInterceptor.ORBInitializer [] orbInits,
 *    org.openorb.orb.pi.FeatureInitializer [] featureInits)
 * </pre>
 * The default implementation can be overriden by setting the
 * org.openorb.orb.pi.ORBInitInfo property with the classname of the
 * override. Failure to load an instance results in an exception.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:51 $
 */
public interface ORBInitInfo
{
    /**
     * Call pre-init on the orb initializers and init on feature
     * initializers.
     */
    void pre_init();

    /**
     * Calls init on feature initializers and post init on orb initializers.
     * During the post_init phase, calls can be made on references returned
     * from resolve_initial_reference, however client interceptors will not
     * be used, the PICurrent will not have any active slots, and IORs cannot
     * be constructed.
     */
    void post_init();
}

