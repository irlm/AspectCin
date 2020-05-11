/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.adapter.fwd;

import junit.framework.TestSuite;

import org.openorb.orb.corbaloc.CorbalocService;
import org.openorb.orb.corbaloc.CorbalocServiceHelper;

import org.openorb.orb.test.ORBTestCase;

import org.openorb.orb.test.adapter.poa.Hello;
import org.openorb.orb.test.adapter.poa.HelloPOA;
import org.openorb.orb.test.adapter.poa.HelloHelper;

/**
 * Tests range of POA policies and features.
 *
 * @author Chris Wood
 */
public class FWDTest
    extends ORBTestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public FWDTest( String name )
    {
        super( name );
    }

    private static final String PORT = "23412";

    /**
     * Overloaded to allow the persistant test case to work.
     */
    protected void setUp()
    {
        java.util.Properties props = new java.util.Properties();
        // set known iiop port since persistant references are used.
        props.setProperty( "ImportModule.Corbaloc",
              "${openorb.home}config/default.xml#CorbalocService" );
        props.setProperty( "iiop.port", PORT );
        setUp( props );
        System.out.println( "Executing the " + FWDTest.class.getName() + "..." );
    }

    /**
     * Test that init reference service can be resolved.
     *
     * @throws org.omg.CORBA.UserException When an error occurs.
     */
    public void testResolve() throws org.omg.CORBA.UserException
    {
        final String testName = "adapter.fwd.FWDTest.testResolve";
        enteringTest( testName );
        try
        {
            _testResolve();
        }
        finally
        {
            exitingTest( testName );
        }
    }
    public void _testResolve() throws org.omg.CORBA.UserException
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testResolve" );
        org.omg.CORBA.ORB orb = getORB();
        CorbalocService service = CorbalocServiceHelper.narrow(
              orb.resolve_initial_references( "CorbalocService" ) );
        Hello hello = new HelloImpl()._this( getORB() );
        service.put( "Test", hello );
        assertTrue( "Get object is not equivalent", hello._is_equivalent( service.get( "Test" ) ) );
        org.omg.CORBA.Object obj = orb.string_to_object( "corbaloc::localhost:" + PORT + "/Test" );
        Hello ini = HelloHelper.narrow( obj );
        ini.hello( "Mouse" );
    }

    /**
     * Test that init reference service can be resolved.
     *
     * @throws org.omg.CORBA.UserException When an error occurs.
     */
    public void testLocalCorbaloc() throws org.omg.CORBA.UserException
    {
        final String testName = "adapter.fwd.FWDTest.testLocalCorbaloc";
        enteringTest( testName );
        try
        {
            _testLocalCorbaloc();
        }
        finally
        {
            exitingTest( testName );
        }
    }
    public void _testLocalCorbaloc() throws org.omg.CORBA.UserException
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testLocalCorbaloc" );
        org.omg.CORBA.ORB orb = getORB();
        CorbalocService service = CorbalocServiceHelper.narrow(
              orb.string_to_object( "corbaloc::localhost:" + PORT + "/CorbalocService" ) );
        Hello hello = new HelloImpl()._this( getORB() );
        service.put( "Test", hello );
        assertTrue( "Get object is not equivalent", hello._is_equivalent( service.get( "Test" ) ) );
        org.omg.CORBA.Object obj = orb.string_to_object( "corbaloc::localhost:" + PORT + "/Test" );
        Hello ini = HelloHelper.narrow( obj );
        ini.hello( "Mouse" );
    }

    /**
     * Test that init reference service can be resolved.
     *
     * @throws org.omg.CORBA.UserException When an error occurs.
     */
    public void testRemoteCorbaloc() throws org.omg.CORBA.UserException
    {
        final String testName = "adapter.fwd.FWDTest.testRemoteCorbaloc";
        enteringTest( testName );
        try
        {
            _testRemoteCorbaloc();
        }
        finally
        {
            exitingTest( testName );
        }
    }
    public void _testRemoteCorbaloc() throws org.omg.CORBA.UserException
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testRemoteCorbaloc" );
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( ( String[] ) null, null );
        Thread.currentThread().interrupt();
        orb.run();
        Thread.interrupted();
        CorbalocService service = CorbalocServiceHelper.narrow(
              orb.string_to_object( "corbaloc::localhost:" + PORT + "/CorbalocService" ) );
        HelloImpl helloImpl = new HelloImpl();
        Hello hello = helloImpl._this( getORB() );
        helloImpl._default_POA().the_POAManager().activate();
        service.put( "Test", hello );
        assertTrue( "Get object is not equivalent", hello._is_equivalent( service.get( "Test" ) ) );
        org.omg.CORBA.Object obj = orb.string_to_object( "corbaloc::localhost:" + PORT + "/Test" );
        Hello ini = HelloHelper.narrow( obj );
        ini.hello( "Mouse" );
        orb.shutdown( true );
    }

    /**
     *  Utility classes.
     */
    static class HelloImpl
        extends HelloPOA
    {
        public void hello( String msg )
        {
            System.out.println( msg );
        }
    }

    /**
     * The main entry point of the test case.
     *
     * @param args The command line parameters.
     */
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( new TestSuite( FWDTest.class ) );
    }
}
