/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.corba;

import java.util.Properties;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * An ORB test case.
 *
 * @author Chris Wood
 */
public class ORBTest
    extends TestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public ORBTest( String name )
    {
        super( name );
    }

    /**
     * Set up the test case.
     */
    public void setUp()
    {
        Properties props = new Properties();
        props.setProperty( "openorb.useStaticThreadGroup", "true" );
        props.setProperty( "openorb.server.enable", "false" );
        m_orb = org.omg.CORBA.ORB.init( ( String[] ) null, props );
        m_any = m_orb.create_any();
    }

    /**
     * Dispose the test case.
     */
    protected void tearDown()
    {
        m_orb.shutdown( true );
    }

    private org.omg.CORBA.ORB m_orb;
    private org.omg.CORBA.Any m_any;

    /**
     * ORBClass test. Test all ORB operations like resolve_initial_references
     * and create operations.
     *
     * @exception org.omg.CORBA.UserException if any of the test case fails
     */
    public void testCorbaloc()
        throws org.omg.CORBA.UserException
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testCorbaloc" );
        // testing corbaloc: [#510234] ArrayIndexOutOfBoundsException
        String corbalocURL =
              "corbaloc:iiop:1.2@www.sun.com:10000,iiop:1.2@www.microsoft.com:10000/NameService";
        org.omg.CORBA.Object obj = m_orb.string_to_object( corbalocURL );
        m_orb.object_to_string( obj );
    }

    /**
     * ORBClass test. Test all ORB operations like resolve_initial_references
     * and create operations.
     *
     * @exception org.omg.CORBA.UserException if any of the test case fails
     */
    public void testORB()
        throws org.omg.CORBA.UserException
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testORB" );
        m_orb.list_initial_services();
        // resolve a local service
        m_orb.resolve_initial_references( "ORBPolicyManager" );

        // mru 20030221: Removed InitRef corbaloc::1.2@localhost:2001/NameService from
        // the config, therefore the resolve_initiali_reference is not working anymore
        // without previous configuration. The reference that was returned before was
        // not working at all the reference was just pointing to a non existent object.
        //
        // resolve a nonlocal reference
        // m_orb.resolve_initial_references( "NameService" );

        // work ops.
        m_orb.work_pending();
        m_orb.perform_work();
        // factory ops
        m_orb.create_list( 3 );
        m_orb.create_named_value( "Test", null, 0 );
        m_orb.create_exception_list();
        m_orb.create_context_list();
        m_orb.create_environment();
        m_orb.get_service_information( ( short ) 0, null );

        /*
        m_orb.send_multiple_requests_oneway ( new org.omg.CORBA.Request [0] );
        m_orb.send_multiple_requests_deferred ( new org.omg.CORBA.Request [0] );
        m_orb.poll_next_response();
        m_orb.get_next_response();
        */
    }

    /**
     * The entry point of the test case.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        System.out.println( "Executing test " + ORBTest.class.getName() + "..." );
        junit.textui.TestRunner.run( new TestSuite( ORBTest.class ) );
    }
}

