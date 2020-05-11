/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * This is the mainline for the OpenORB test harness. This test harness can
 * execute one or more tests depending on the input parameter. It also follows
 * the signelton design pattern.
 *
 * @author Chris Wood
 */
public class OpenORBTest
    extends TestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public OpenORBTest( String name )
    {
        super( name );
    }

    /**
     * Creates a collection of sub test cases.
     *
     * @return The test case collection.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest( org.openorb.orb.test.corba.CORBATestSuite.suite() );
        suite.addTest( org.openorb.orb.test.iiop.IIOPTest.suite() );
        suite.addTest( org.openorb.orb.test.dynamic.DynamicTestSuite.suite() );
        suite.addTestSuite( org.openorb.orb.test.adapter.poa.POATest.class );
        suite.addTestSuite( org.openorb.orb.test.adapter.boa.BOATest.class );
        suite.addTestSuite( org.openorb.orb.test.adapter.fwd.FWDTest.class );
        suite.addTestSuite( org.openorb.orb.test.pi.PITest.class );
        //suite.addTest( org.openorb.orb.test.rmi.RMIoverIIOPTest.suite() );
        return suite;
    }

    /**
     * The entry point of the test case.
     *
     * @param args The command line parameters.
     */
    public static void main( String[] args )
    {
        System.out.println( "Executing OpenORB's JUnit test suite..." );
        junit.textui.TestRunner.run( suite() );
    }
}

