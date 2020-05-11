/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter;

/**
 * An object with this interface is output from the predispatch operation
 * and is passed into the dispatch or cancel_dispatch operations. It holds
 * information about the target.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:45 $
 */
public interface TargetInfo
{
    /**
     * Most derrived repository IDs supported by the target.
     */
    String getRepositoryID();

    /**
     * Test if the target supports the given repository id.
     */
    boolean targetIsA( String repo_id );

    /**
     * Adapter ID of the target adapter.
     */
    byte [] getAdapterID();

    /**
     * Object ID of the target.
     */
    byte [] getObjectID();
}

