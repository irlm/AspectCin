/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * Implementation of the value type AbstractA3.
 *
 * @author Chris Wood
 */
public class AbstractA3Impl
    extends AbstractA3
{
    private String m_no_transport;

    /**
     * Creates new AbstractA3Impl.
     *
     * @param notransport A string parameter.
     */
    public AbstractA3Impl( String notransport )
    {
        m_no_transport = notransport;
    }

    /**
     * Print a stringified representation of the value type.
     */
    public void print()
    {
        System.out.println( "AbstractA3Impl untransported state: \"" + m_no_transport + "\"" );
    }
}

