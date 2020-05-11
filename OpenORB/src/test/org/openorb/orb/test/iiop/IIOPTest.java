/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

import org.openorb.orb.test.iiop.bidir.BiDirTest;
import org.openorb.orb.test.iiop.codec.CodecTest;
import org.openorb.orb.test.iiop.complex.ComplexTest;
//import org.openorb.orb.test.iiop.fragmentedmessage.FragmentedMessageTest;
import org.openorb.orb.test.iiop.primitive.PrimitiveTest;
import org.openorb.orb.test.iiop.state.StateTest;
import org.openorb.orb.test.iiop.value.ValuetypeTest;

/**
 * Tests marshaling and unmarshaling of various iiop types.
 *
 * @author Chris Wood
 */
public class IIOPTest
    extends TestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public IIOPTest( String name )
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
        suite.addTestSuite( CodecTest.class );
        suite.addTestSuite( PrimitiveTest.class );
        suite.addTestSuite( ValuetypeTest.class );
        suite.addTestSuite( BiDirTest.class );
        suite.addTestSuite( StateTest.class );
        suite.addTestSuite( ComplexTest.class );
        //suite.addTestSuite( FragmentedMessageTest.class );
        return suite;
    }

    /**
     * The main entry point of the test case.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        System.out.println( "Executing the " + IIOPTest.class.getName() + "..." );
        junit.textui.TestRunner.run( suite() );
    }
}

