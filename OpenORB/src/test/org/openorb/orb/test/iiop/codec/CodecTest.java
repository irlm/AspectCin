/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.iiop.codec;

import junit.framework.TestSuite;

import org.openorb.orb.test.ORBTestCase;

/**
 * Tests coding and decoding of data by a codec.
 *
 * @author Michael Rumpf
 */
public class CodecTest
    extends ORBTestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public CodecTest( String name )
    {
        super( name );
    }

    /**
     * Set up the test case.
     */
    protected void setUp()
    {
        super.setUp();
        try
        {
            m_orb = getORB();
            org.omg.IOP.CodecFactory factory = ( org.omg.IOP.CodecFactory )
                  m_orb.resolve_initial_references ( "CodecFactory" );
            m_codec = factory.create_codec(
                  new org.omg.IOP.Encoding( org.omg.IOP.ENCODING_CDR_ENCAPS.value,
                  ( byte ) 1, ( byte ) 2 ) );
        }
        catch ( org.omg.CORBA.UserException ex )
        {
            fail( "exception during setup:" + ex.toString() );
        }
    }

    private org.omg.CORBA.ORB m_orb;
    private org.omg.IOP.Codec m_codec;
    private org.omg.CORBA.Any m_rpl;

    public void testDecodeMismatch()
        throws Exception
    {
        System.out.println( "Test: " + this.getClass().getName() + ".testDecodeMismatch" );
        try
        {
            byte[] oid = Long.toString( ( long ) Math.random() ).getBytes();
            m_codec.decode_value( oid, org.omg.IOP.TaggedComponentSeqHelper.type() );
            fail( "No exception was thrown!" );
        }
        catch ( org.omg.IOP.CodecPackage.FormatMismatch mismatch )
        {
            // We expect this one
        }
        catch ( Throwable th )
        {
            th.printStackTrace();
            fail( "An unexpected exception was caught!" );
        }
    }

    /**
     * The main entry point of the test case.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        System.out.println( "Executing the " + CodecTest.class.getName() + "..." );
        junit.textui.TestRunner.run( new TestSuite( CodecTest.class ) );
    }
}

