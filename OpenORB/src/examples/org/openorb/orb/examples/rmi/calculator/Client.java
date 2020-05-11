/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.calculator;

import javax.naming.InitialContext;

/**
 * The clien application for the calculator.
 *
 * @author Chris Wood
 */
public final class Client
{
    // do not instantiate
    private Client()
    {
    }

    /**
     * The entry point for this application.
     *
     * @param args The command line parameters.
     */
    public static void main( String[] args )
    {
        CalculatorInterface calc = null;
        try
        {
            InitialContext context = new InitialContext();
            Object o = context.lookup ( "Calculator" );
            calc = ( CalculatorInterface )
                  javax.rmi.PortableRemoteObject.narrow( o, CalculatorInterface.class );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            System.exit( 0 );
        }

        try
        {
            System.out.println( "6 + 2 = " + calc.add( 6, 2 ) );
            System.out.println( "6 / 2 = " + calc.div( 6, 2 ) );
            System.out.println( "6 / 0 !!!!" + calc.div( 6, 0 ) );
        }
        catch ( DivisionByZero e )
        {
            System.out.println( "\n==> Error : Division by zero" );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
}

