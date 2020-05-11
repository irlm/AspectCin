/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * A factory implementation for the value type AbstractA2.
 *
 * @author Chris Wood
 */
public class AbstractA2Factory
    implements org.omg.CORBA.portable.ValueFactory
{
    /**
     * Creates new AbstractA2Factory
     */
    public AbstractA2Factory()
    {
    }

    /**
     * Reads a value from an input stream.
     *
     * @param is The input stream.
     * @return The value read from the stream.
     */
    public java.io.Serializable read_value( org.omg.CORBA_2_3.portable.InputStream is )
    {
        return is.read_value( new AbstractA2Impl() );
    }
}

