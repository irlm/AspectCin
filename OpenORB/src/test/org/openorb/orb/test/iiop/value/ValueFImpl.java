/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * The implementation of value type ValueF.
 *
 * @author Chris Wood
 */
public class ValueFImpl
    extends ValueF
{
    /**
     * Unmarshal the value type from the input stream.
     *
     * @param is The input stream to read the value type from.
     */
    public void unmarshal( org.omg.CORBA.DataInputStream is )
    {
        l = Integer.parseInt( is.read_string() );
    }

    /**
     * Marshal the value type into the output stream.
     *
     * @param os The output stream to write the value type to.
     */
    public void marshal( org.omg.CORBA.DataOutputStream os )
    {
        os.write_string( Integer.toString( l ) );
    }

    /**
     * Creates a stringified representation of the value-type's instance.
     *
     * @return A stringified representation of the instance.
     */
    public String toString()
    {
        return "ValueF ( " + l + ")";
    }
}

