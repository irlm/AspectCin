/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * Implementation of the dafault factory for value type ValueA.
 *
 * @author Chris Wood
 */
public class ValueADefaultFactory
    implements org.omg.CORBA.portable.ValueFactory
{
    /**
     * Read the value type from an input stream.
     *
     * @param is The input stream to read the value type from.
     * @return The value type read from the stream.
     */
    public java.io.Serializable read_value( org.omg.CORBA_2_3.portable.InputStream is )
    {
        ValueA val = new ValueAImpl();
        is.read_value( val );
        return val;
    }
}

