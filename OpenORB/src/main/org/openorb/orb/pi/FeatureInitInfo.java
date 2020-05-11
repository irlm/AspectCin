/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

/**
 * Init info interface passed to feature initializers.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:51 $
 */
public interface FeatureInitInfo
{
    /**
     * Return the orb being configured.
     * The orb returned will not have been fully initialized, it should
     * not be used for activating objects or making requests.
     */
    org.openorb.orb.core.ORB orb();

    /**
     * Return a reference to the orb loader.
     * This is a shortcut to orb().getLoader().
     */
    org.openorb.orb.config.ORBLoader getLoader();

    /**
     * Set an openorb feature.
     */
    void setFeature( String feature, java.lang.Object reference );

    /**
     * Get an OpenORB feature.
     */
    java.lang.Object getFeature( String feature );
}

