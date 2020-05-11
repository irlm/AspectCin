/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

/**
 * This is the interface for an object adapter manager.
 *
 * @author Unknown
 */
public interface AdapterManager
    extends org.omg.PortableServer.POAManager
{
    /**
     * Set the maximum number of held requests.
     */
    void setMaxManagerHeldRequests( int max );
}

