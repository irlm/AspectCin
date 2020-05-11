/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.textui.TestRunner;

/**
 * The OpenORB tools test suite.
 *
 * @author Richard G Clark
 * @version $Revision: 1.1 $ $Date: 2004/06/23 07:13:19 $
 */
public class ToolsTest extends TestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public ToolsTest( final String name )
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
        suite.addTestSuite( NumberCacheTest.class );
        suite.addTestSuite( CharacterCacheTest.class );
        return suite;
    }

    /**
     * The entry point of the test case.
     *
     * @param args The command line parameters.
     */
    public static void main( final String[] args )
    {
        System.out.println( "Executing tools JUnit test suite..." );
        TestRunner.run( suite() );
    }
}

