/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.corba;

import junit.framework.TestSuite;

/**
 * An ORB singleton test case.
 *
 * @author Chris Wood
 */
public class SingletonTest
    extends CORBATestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public SingletonTest( String name )
    {
        super( name );
    }

    /**
     * Set up the test case,
     */
    public void setUp()
    {
        super.setUp();

        m_orb = getORB();
        m_singleton = ( org.omg.CORBA_2_3.ORB ) org.omg.CORBA.ORB.init();
        m_any = m_orb.create_any();
    }

    private org.omg.CORBA.ORB m_orb;
    private org.omg.CORBA_2_3.ORB m_singleton;
    private org.omg.CORBA.Any m_any;

    /**
     * Test the ORBSingleton operations to create TypeCodes. This test covers
     * the create_XXX_tc operations passing by the right parameters.
     * The created TypeCodes are struct, union, enum, alias, exception, interface,
     * native, string, wstring, sequence, recursive, array, fixed, value_box, abstract interface,
     * local interface.
     *
     * @exception org.omg.CORBA.UserException if any of the test case fails
     */
    public void testTypeCodeCreate() throws org.omg.CORBA.UserException
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testTypeCodeCreate" );
        m_singleton.create_struct_tc( "IDL:Dummy:1.0", "ReallyDummy",
              new org.omg.CORBA.StructMember[ 0 ] );
        m_singleton.create_union_tc( "IDL:Dummy:1.0", "ReallyDummy",
              m_singleton.get_primitive_tc( org.omg.CORBA.TCKind.tk_short ),
              new org.omg.CORBA.UnionMember[ 0 ] );
        m_singleton.create_enum_tc( "IDL:Dummy:1.0", "ReallyDummy", new String[ 0 ] );
        m_singleton.create_alias_tc( "IDL:Dummy:1.0", "ReallyDummy",
              m_singleton.create_string_tc( 1 ) );
        m_singleton.create_exception_tc( "IDL:Dummy:1.0", "ReallyDummy",
              new org.omg.CORBA.StructMember[ 0 ] );
        m_singleton.create_interface_tc( "IDL:Dummy:1.0", "ReallyDummy" );
        m_singleton.create_native_tc( "IDL:Dummy:1.0", "ReallyDummy" );
        m_singleton.create_string_tc( 1 );
        m_singleton.create_wstring_tc( 1 );
        m_singleton.create_sequence_tc( 3, m_singleton.create_wstring_tc( 1 ) );
        //m_singleton.create_recursive_sequence_tc(3, 0 );
        m_singleton.create_recursive_tc( "IDL:Dummy:1.0" );
        m_singleton.create_array_tc( 3, m_singleton.create_wstring_tc( 1 ) );
        m_singleton.create_fixed_tc( ( short ) 5, ( short ) 2 );
        m_singleton.create_value_box_tc( "IDL:Dummy:1.0", "ReallyDummy",
              m_singleton.create_wstring_tc( 1 ) );
        m_singleton.create_abstract_interface_tc( "IDL:Dummy:1.0", "ReallyDummy" );
        ( ( org.openorb.orb.core.ORBSingleton ) m_singleton ).create_local_interface_tc(
              "IDL:Dummy:1.0", "ReallyDummy" );
    }


    /**
     * Test the not implemented operations of the ORB Singleton. The whole not implemented
     * operations of the ORB Singleton (implement in the ORB) are invoked and the thrown
     * NO_IMPLEMENT exception is checked.
     *
     * @exception org.omg.CORBA.UserException if any of the test case fails
     */
    public void testORBSingletonNoImplement()
        throws org.omg.CORBA.UserException
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testORBSingletonNoImplement" );
        try
        {
            ( ( org.openorb.orb.core.ORBSingleton ) m_singleton ).list_initial_services();
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.resolve_initial_references( "Dummy" );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.run();
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.shutdown( false );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.work_pending();
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.perform_work();
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.create_list( 3 );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.create_named_value( "Test", null, 0 );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.create_exception_list();
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.create_context_list();
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.get_default_context();
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.create_environment();
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.connect( null );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.disconnect( null );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.create_output_stream();
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.object_to_string( null );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.string_to_object( "Dummy" );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.send_multiple_requests_oneway ( new org.omg.CORBA.Request [ 0 ] );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.send_multiple_requests_deferred ( new org.omg.CORBA.Request [ 0 ] );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.poll_next_response();
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.get_next_response();
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.register_value_factory ( "Dummy", null );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.unregister_value_factory ( "Dummy" );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.lookup_value_factory ( "Dummy" );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.set_delegate ( null );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.get_service_information( ( short ) 0, null );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }

        try
        {
            m_singleton.create_policy( 0, null );
            fail( "NO_IMPLEMENT expected!" );
        }
        catch ( org.omg.CORBA.NO_IMPLEMENT ex )
        {
            // expected
        }
    }

    /**
     * The entry point of the test case.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        System.out.println( "Executing test " + SingletonTest.class.getName() + "..." );
        junit.textui.TestRunner.run( new TestSuite( SingletonTest.class ) );
    }
}

