/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * Implementation of the ValueA value type.
 *
 * @author Chris Wood
 */
public class ValueAImpl
    extends ValueA
{
    /**
     * Constructor.
     */
    public ValueAImpl()
    {
        str = "";
    }

    /**
     * Creates a stringified representation of the value type instance.
     *
     * @return A stringified representation of this instance.
     */
    public String toString()
    {
        return "ValueA (\"" + str + "\", " + l + ")";
    }
}

