/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * An implementation of the ValueE value type.
 *
 * @author Chris Wood
 */
public class ValueEImpl
    extends ValueE
{
    /**
     * Creates new ValueEImpl.
     *
     * @param notransport A string member's initial value.
     */
    public ValueEImpl( String notransport )
    {
        m_no_transport = notransport;
    }

    private String m_no_transport;

    /**
     * Print a message about the internal state of the value type.
     */
    public void print()
    {
        System.out.println( prefix + "\" untransported state: \"" + m_no_transport + "\"" );
    }
}

