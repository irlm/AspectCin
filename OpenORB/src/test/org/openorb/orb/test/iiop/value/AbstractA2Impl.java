/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.value;

/**
 * The implementation of the value type AbstractA2.
 *
 * @author Chris Wood
 */
public class AbstractA2Impl
    extends AbstractA2
{
    /**
     * Creates new AbstractA2Impl.
     */
    public AbstractA2Impl()
    {
    }

    /**
     * Print a message about this instance.
     */
    public void print()
    {
        System.out.println( "Local invocation on AbstractA2Impl" );
    }
}

