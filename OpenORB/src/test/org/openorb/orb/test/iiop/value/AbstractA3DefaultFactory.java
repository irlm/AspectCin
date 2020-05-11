/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * Implementation of the AbstractA3 value type default factory.
 *
 * @author Chris Wood
 */
public class AbstractA3DefaultFactory
    implements org.omg.CORBA.portable.ValueFactory
{
    /**
     * Read the value type from an inputs stream.
     *
     * @param is The input stream.
     * @return The value read from the stream.
     */
    public java.io.Serializable read_value( org.omg.CORBA_2_3.portable.InputStream is )
    {
        return is.read_value( new AbstractA3Impl( "transported" ) );
    }
}

