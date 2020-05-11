/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * @author Chris Wood
 */
public class ValueDFactory
    implements org.omg.CORBA.portable.ValueFactory
{
    /**
     * Read the value type from an input stream.
     *
     * @param is The input stream to read the value from.
     * @return The value read from the stream.
     */
    public java.io.Serializable read_value( org.omg.CORBA_2_3.portable.InputStream is )
    {
        ValueD ret = new ValueDImpl();
        is.read_value( ret );
        return ret;
    }
}
