/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * The default factory implementation of the value type ValueG.
 *
 * @author Chris Wood
 */
public class ValueGDefaultFactory
    implements org.omg.CORBA.portable.ValueFactory
{
    /**
     * Read the value from an InputStream.
     *
     * @param is The InputStream instance to read the value from.
     * @return The value read from the stream.
     */
    public java.io.Serializable read_value( org.omg.CORBA_2_3.portable.InputStream is )
    {
        return is.read_value( new ValueGImpl() );
    }
}

