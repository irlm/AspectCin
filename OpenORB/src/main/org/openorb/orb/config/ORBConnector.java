/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.config;

import org.apache.avalon.framework.logger.LogEnabled;

/**
 * The ORB Connector is the entity which connects all OpenORB features
 * to the OpenORB Kernel.
 *
 * NOTE: But what exactly do you understand by the OpenORB "kernel"
 * and why is the loading of the kernel separated into another
 * interface/class ???
 * In order to understand this interface/class we need a precise
 * definition of what the OpenORB kernel is.
 * The description is also confusing:
 * "connects ... to the OpenORB kernel". This implies that the kernel
 * is already started and the interface/class just attaches some
 * additional stuff.
 * In this context I find the name "load_kernel" most confusing !
 *
 * @author Jerome Daniel
 * @version $Revision: 1.5 $ $Date: 2004/02/10 21:02:46 $
 *
 * @deprecated Because of doubts concerning the purpose of this
 * interface and in order to simplify the kernel's architecture this
 * class will be removed in future versions of OpenORB.
 */
public interface ORBConnector
    extends LogEnabled
{
    /**
     * This operation is used to load the OpenORB kernel.
     */
    void load_kernel( org.openorb.orb.core.ORB orb,
                             org.openorb.orb.config.ORBLoader loader );
}

