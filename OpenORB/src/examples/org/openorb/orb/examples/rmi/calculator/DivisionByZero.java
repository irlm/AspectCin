/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.calculator;

import java.rmi.RemoteException;

/**
 * The exception to indicate a division by zero.
 *
 * @author Chris Wood
 */
public class DivisionByZero
    extends RemoteException
{
    /**
     * Default constructor.
     */
    public DivisionByZero()
    {
    }
}
