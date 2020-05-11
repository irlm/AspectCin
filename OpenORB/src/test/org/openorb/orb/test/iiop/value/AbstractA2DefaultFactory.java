/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * A default factory implementation for the AbstractA2 value type.
 *
 * @author Chris Wood
 */
public class AbstractA2DefaultFactory
    implements org.omg.CORBA.portable.ValueFactory
{
    /**
     * Read the value from an input stream.
     *
     * @param is The input stream.
     * @return The value read from the input stream.
     */
    public java.io.Serializable read_value( org.omg.CORBA_2_3.portable.InputStream is )
    {
        return is.read_value( new AbstractA2Impl() );
    }
}

