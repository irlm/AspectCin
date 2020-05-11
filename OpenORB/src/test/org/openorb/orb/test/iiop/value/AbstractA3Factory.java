/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * An implementation for the value factory of value type AbstractA3.
 *
 * @author Chris Wood
 */
public class AbstractA3Factory
    implements org.omg.CORBA.portable.ValueFactory
{
    /**
     * Creates new AbstractA3Factory.
     */
    public AbstractA3Factory()
    {
    }

    /**
     * Read the value type from an input stream.
     *
     * @param is The input stream to read the value type from.
     * @return The value type read from the stream.
     */
    public java.io.Serializable read_value( org.omg.CORBA_2_3.portable.InputStream is )
    {
        return is.read_value( new AbstractA3Impl( "transported" ) );
    }
}

