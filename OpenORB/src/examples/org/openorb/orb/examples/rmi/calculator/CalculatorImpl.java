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
 * The implementation of the simple calculator interface.
 *
 * @author Chris Wood
 */
public class CalculatorImpl
    implements CalculatorInterface
{
    /**
     * Default constructor.
     *
     * @throws RemoteException ???
     */
    public CalculatorImpl()
        throws RemoteException
    {
        super();
    }

    /**
     * The add operation.
     *
     * @param a The first value.
     * @param b The second value.
     * @return The result of a + b.
     * @throws RemoteException When an error occurs.
     */
    public long add( long a, long b )
        throws RemoteException
    {
        return a + b;
    }

    /**
     * The div operation.
     *
     * @param a The first value.
     * @param b The second value.
     * @return The result of a / b.
     * @throws RemoteException When an error occurs.
     * @throws DivisionByZero When the divisor is zero.
     */
    public long div( long a, long b )
        throws RemoteException, DivisionByZero
    {
        if ( b == 0 )
        {
            throw new DivisionByZero();
        }
        return ( a / b );
    }
}

