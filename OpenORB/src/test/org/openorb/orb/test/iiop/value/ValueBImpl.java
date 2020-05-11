/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * Implementation of value type ValueB.
 *
 * @author Chris Wood
 */
public class ValueBImpl
    extends ValueB
{
    /**
     * Creates new ValueBImpl for unmarshalling.
     */
    public ValueBImpl()
    {
    }

    /**
     * Factory init method, creates ValueB.
     *
     * @param l Initial value for member l.
     */
    public ValueBImpl( int l )
    {
        this.l = l;
    }

    /**
     * Return the value of member l.
     *
     * @return The value for l.
     */
    public int ls()
    {
        return l;
    }

    /**
     * Creates a stringified representation of the value type instance.
     *
     * @return A stringified representation of the instance.
     */
    public String toString()
    {
        return "ValueB (\"" + str + "\", " + l + ")";
    }
}

