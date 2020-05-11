/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import org.omg.PortableInterceptor.IORInfo;

/**
 * This interface must be implemented by the IOR interceptor manager.<p>
 * Overrides must have a constructor with exact signature:
 * <pre>
 * public IORManager(org.omg.PortableInterceptor.IORInterceptor [] list)
 * </pre>
 * The default implementation can be overriden by setting the
 * org.openorb.PI.IORManagerClass property with the classname of the
 * override. To disable IOR construction altogether set this property to the
 * empty string.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:51 $
 */
public interface IORManager
{
    /**
     * This methods establishes IOR components for an IOR.
     *
     * @param info The IOR descriptor.
     */
    void establish_components( IORInfo info );
}

