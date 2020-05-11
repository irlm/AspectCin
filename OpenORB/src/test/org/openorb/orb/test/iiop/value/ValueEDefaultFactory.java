/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * The default factory of the value type ValueE.
 *
 * @author Chris Wood
 */
public class ValueEDefaultFactory
    implements org.omg.CORBA.portable.ValueFactory
{
    /**
     * Read the value from an input stream.
     *
     * @param is The input stream to read the value from.
     * @return The value read from the stream.
     */
    public java.io.Serializable read_value( org.omg.CORBA_2_3.portable.InputStream is )
    {
        ValueE val = new ValueEImpl( "Transported" );
        is.read_value( val );
        return val;
    }
}

