/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.dynamic;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * Tests marshaling and unmarshaling of various iiop types.
 *
 * @author Chris Wood
 */
public class DynamicTestSuite
    extends TestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public DynamicTestSuite( String name )
    {
        super( name );
    }

    /**
     * Creates a collection of sub test-cases.
     *
     * @return The collection of test cases.
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite( DynAnyTest.class );
        suite.addTestSuite( DIITest.class );
        suite.addTestSuite( DSITest.class );
        suite.addTestSuite( DynamicScenarioTest.class );
        return suite;
    }

    /**
     * The entry point of the test-case.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        System.out.println( "Executing the " + DynamicTestSuite.class.getName() + "..." );
        junit.textui.TestRunner.run( suite() );
    }
}

