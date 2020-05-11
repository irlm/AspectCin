/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

/**
 * Feature initializers are an extention of an ORBInitializer allowing access
 * to unique OpenORB features during initialization.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:51 $
 */

public interface FeatureInitializer
{
    /**
     * This is called immediatly after any pre_init interception points in
     * ORBInitializers. The orb reference available from the FeatureInitInfo
     * should be treated with care, it can not be used for any request
     * functions or for creating object references.
     */
    void init( org.omg.PortableInterceptor.ORBInitInfo orbinfo,
        FeatureInitInfo featureinfo );
}

