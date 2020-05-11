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
 * A CORBA Any test case.
 *
 * @author Chris Wood
 */
public class AnyTest
    extends CORBATestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public AnyTest( String name )
    {
        super( name );
    }

    /**
     * Set up the test case.
     */
    public void setUp()
    {
        super.setUp();

        m_orb = getORB();
        m_any = m_orb.create_any();
    }

    private org.omg.CORBA.ORB m_orb;
    private org.omg.CORBA.Any m_any;

    /**
     * Test the Any insert and extract operations. For each basic type (numbers, strings, objects)
     * one invocation is made for inserting an object into the Any, and the following invocation
     * extracts the same value from the Any. Checking is made that ensure with retrieve the same
     * value;
     *
     * @exception org.omg.CORBA.UserException if any of the test case fails
     */
    public void testAny() throws org.omg.CORBA.UserException
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testAny" );
        org.omg.CORBA.Any any2 = m_orb.create_any();
        m_any.insert_any( any2 );
        assertEquals( "Values should be equal!", any2, m_any.extract_any() );

        m_any.insert_char( 'c' );
        assertEquals( "Values should be equal!", 'c', m_any.extract_char() );

        m_any.insert_wchar( 'c' );
        assertEquals( "Values should be equal!", 'c', m_any.extract_wchar() );

        m_any.insert_octet( ( byte ) 'o' );
        m_any.extract_octet();

        m_any.insert_short( ( short ) 1 );
        m_any.extract_short();

        m_any.insert_ushort( ( short ) 1 );
        m_any.extract_ushort();

        m_any.insert_long( 2 );
        m_any.extract_long();

        m_any.insert_ulong( 2 );
        m_any.extract_ulong();

        m_any.insert_longlong( 2L );
        m_any.extract_longlong();

        m_any.insert_ulonglong( 2L );
        m_any.extract_ulonglong();

        m_any.insert_float( ( float ) 3.0 );
        m_any.extract_float();

        m_any.insert_double( 3.0 );
        m_any.extract_double();

        m_any.insert_string( "str" );
        m_any.extract_string();

        m_any.insert_wstring( "str" );
        m_any.extract_wstring();

        m_any.insert_Object( null );
        m_any.extract_Object();

        // The whole Principal class has been deprecated as of CORBA 2.2
        // The following block has been removed to avoid a deprecation warning !
        /*
        org.openorb.orb.core.Principal p = new org.openorb.orb.core.Principal(
              "MyPrincipal".getBytes() );
        p.name();
        p.name( "MyPrincipal".getBytes() );
        assertTrue( "Principal object not equal to self!", p.equals( p ) );
        m_any.insert_Principal( p );
        m_any.extract_Principal();
        */
    }

    /**
     * The entry point of the test case.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        System.out.println( "Executing test " + AnyTest.class.getName() + "..." );
        junit.textui.TestRunner.run( new TestSuite( AnyTest.class ) );
    }
}

