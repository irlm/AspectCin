/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.special;

import junit.framework.TestSuite;

import org.openorb.orb.test.rmi.RMITestCase;

/**
 * This class provides special tests ( called dummy test that invokes
 * operations outside of a realistic invocation context ). Anyway,
 * these tests are interesting since they test some special parts of
 * the RMI over IIOP implementation : the deserialization engine for a
 * serializable object without a default constructor.
 *
 * @author Jerome Daniel
 */
public class DummyTest
    extends RMITestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public DummyTest( String name )
    {
        super( name );
    }

    /**
     * Set up the test case.
     */
    protected void setUp()
    {
        super.setUp();
    }

    /**
     * Dispose the test case.
     */
    protected void tearDown()
    {
        super.tearDown();
    }

    /**
     * The entry point for this test case.
     *
     * @param args The command line parameters.
     */
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( new TestSuite( DummyTest.class ) );
    }
}

