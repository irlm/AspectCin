/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb;

/**
 * This is the base interface for all Initializers.
 *
 * @author Michael Rumpf
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:44 $
 */

public interface Initializer
{
    /**
     * Return the name of the initializer.
     *
     * @return The name of the initializer.
     */
    String getName();
}
