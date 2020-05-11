/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * The implementation of the ValueG value type.
 *
 * @author Chris Wood
 */
public class ValueGImpl
    extends ValueG
{
    /**
     * Creates a stringified representation of the internal state of this instance.
     *
     * @return A stringified representation of this instance.
     */
    public String toString()
    {
        return "(\""
               + name + "\","
               + idx
               + ","
               + ( ( left == null ) ? "()" : left.toString()
               + ( ( left.parent == this ) ? "*" : "" ) )
               + ","
               + ( ( middle == null ) ? "()" : middle.toString()
               + ( ( left.parent == this ) ? "*" : "" ) )
               + ","
               + ( ( right == null ) ? "()" : right.toString()
               + ( ( left.parent == this ) ? "*" : "" ) )
               + ")";
    }
}

