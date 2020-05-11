/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

import org.omg.PortableServer.POA;

/**
 * Implementation of the AbstractA1 value type.
 *
 * @author Chris Wood
 */
public class AbstractA1Impl
    extends AbstractA1POA
{
    private POA m_poa;

    /**
     * Constructor.
     *
     * @param poa The default POA instance.
     */
    public AbstractA1Impl( POA poa )
    {
        m_poa = poa;
    }

    /**
     * Return the default POA stored with this instance.
     *
     * @return The default POA.
     */
    public POA _default_POA()
    {
        return m_poa;
    }

    /**
     * Print a message.
     */
    public void print()
    {
        System.out.println( "Remote invocation on AbstractA1Impl" );
    }
}

