/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * Implementation of the value type ValueC.
 *
 * @author Chris Wood
 */
public class ValueCImpl
    extends ValueC
{
    /**
     * Constructor.
     */
    public ValueCImpl()
    {
        str = "";
    }

    /**
     * Creates a stringified representation of the instance.
     *
     * @return A string representing the state of this instance.
     */
    public String toString()
    {
        return "ValueC (\"" + str + "\", " + l + ", " + n + ")";
    }
}

