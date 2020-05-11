/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * This is the mainline for the OpenORB test harness. This test harness can
 * execute one or more tests depending on the input parameter. It also follows
 * the signelton design pattern.
 *
 * @author  Chris Wood
 */
public class RMIoverIIOPTest
    extends TestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public RMIoverIIOPTest( String name )
    {
        super( name );
    }

    /**
     * Prepare the ORB for local optimized invocations.
     */
    static void handleLocalInvocations()
    {
        org.omg.CORBA.ORB orb = org.openorb.orb.rmi.DefaultORB.getORB();
        org.omg.CORBA.Any value = orb.create_any();
        boolean local = "true".equals( System.getProperty( "localInvocations" ) );
        value.insert_boolean( !local );
        try
        {
            org.omg.CORBA.Policy forcePolicy = orb.create_policy(
                  org.openorb.orb.policy.FORCE_MARSHAL_POLICY_ID.value, value );
            org.omg.CORBA.PolicyManager opm = ( org.omg.CORBA.PolicyManager )
                  orb.resolve_initial_references( "ORBPolicyManager" );
            opm.set_policy_overrides( new org.omg.CORBA.Policy[] { forcePolicy },
                  org.omg.CORBA.SetOverrideType.ADD_OVERRIDE );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            fail( ex.toString() );
        }
    }

    /**
     * This method adds all sub tests to the test suite.
     *
     * @return The test suite with all the sub-tests added.
     */
    public static Test suite()
    {
        handleLocalInvocations();

        TestSuite suite = new TestSuite();
        suite.addTestSuite( org.openorb.orb.test.rmi.complex.ComplexTest.class );
        suite.addTestSuite( org.openorb.orb.test.rmi.primitive.PrimitiveTest.class );
        suite.addTestSuite( org.openorb.orb.rmi.StreamTest.class );
        suite.addTestSuite( org.openorb.orb.test.rmi.special.SpecialTest.class );

        suite.addTestSuite( org.openorb.orb.test.rmi.exceptions.ExceptionTest.class );
        return suite;
    }

    /**
     * The entry point for this test suite.
     *
     * @param args The command line parameters.
     */
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( suite() );
    }
}

