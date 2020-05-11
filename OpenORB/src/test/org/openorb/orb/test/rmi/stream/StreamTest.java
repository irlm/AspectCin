/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

import junit.framework.TestSuite;

import org.openorb.orb.test.rmi.RMITestCase;

/**
 * This test suit is used in order to test some other parts of the RMI
 * over IIOP implementation :
 * - narrowing
 * - stub serialization and deserialization
 *
 * @author Jerome Daniel
 */
public class StreamTest
    extends RMITestCase
{
    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public StreamTest( String name )
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
            m_orb = org.openorb.orb.rmi.DefaultORB.getORB();
            m_handler = ( ValueHandlerImpl ) javax.rmi.CORBA.Util.createValueHandler();
            m_os = ( org.omg.CORBA_2_3.portable.OutputStream ) m_orb.create_output_stream();
            m_ros = new RMIObjectOutputStream( m_handler, m_os );
            try
            {
                m_ros.reset();
            }
            catch ( java.io.IOException expected )
            {
                // a reset is not allowed yet
            }
            m_ros.useProtocolVersion( 2 );
            m_is = ( org.omg.CORBA_2_3.portable.InputStream ) m_os.create_input_stream();
            m_ris = new RMIObjectInputStream( m_handler, m_is );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            fail( ex.toString() );
        }
    }

    private org.omg.CORBA.ORB m_orb;

    private ValueHandlerImpl m_handler;

    private RMIObjectInputStream m_ris;
    private org.omg.CORBA_2_3.portable.InputStream m_is;

    private RMIObjectOutputStream m_ros;
    private org.omg.CORBA_2_3.portable.OutputStream m_os;

    /**
     * Dispose the test case.
     */
    protected void tearDown()
    {
        try
        {
            m_ris.close();
        }
        catch ( java.io.IOException expected )
        {
            // close is ot allowed yet
        }
        try
        {
            m_ros.close();
        }
        catch ( java.io.IOException expected )
        {
            // close is ot allowed yet
        }
    }

    /**
     * Test narrowing incorrectly operations. An exception must be raised.
     */
    public void testUTF()
    {
        try
        {
            String source = "ThisIsATest";
            m_ros.writeUTF( source );
            m_ros.flush();
            String echo = m_ris.readUTF();
            assertEquals( "Input/output stream not equal", source, echo );
            m_ros.writeChars( source );
            m_ros.writeBytes( source );
            try
            {
                m_ris.readLine();
            }
            catch ( java.lang.Exception expected )
            {
                // ???
            }
        }
        catch ( java.lang.Exception ex )
        {
            fail( "Exception raised." );
        }
    }

    /**
     * Test narrowing incorrectly operations. An exception must be raised.
     */
    public void testChar()
    {
        try
        {
            char source = 'c';
            m_ros.writeChar( source );
            m_ros.flush();
            char echo = m_ris.readChar();
            assertEquals( "Input/output stream not equal", source, echo );
        }
        catch ( java.lang.Exception ex )
        {
            fail( "Exception raised." );
        }
    }

    /**
     * Test narrowing incorrectly operations. An exception must be raised.
     */
    public void testBoolean()
    {
        try
        {
            boolean source = true;
            m_ros.writeBoolean( source );
            m_ros.flush();
            boolean echo = m_ris.readBoolean();
            assertEquals( "Input/output stream not equal", source, echo );
        }
        catch ( java.lang.Exception ex )
        {
            fail( "Exception raised." );
        }
    }

    /**
     * Test narrowing incorrectly operations. An exception must be raised.
     */
    public void testLong()
    {
        try
        {
            long source = 2;
            m_ros.writeLong( source );
            m_ros.flush();
            long echo = m_ris.readLong();
            assertEquals( "Input/output stream not equal", source, echo );
        }
        catch ( java.lang.Exception ex )
        {
            fail( "Exception raised." );
        }
    }

    /**
     * Test narrowing incorrectly operations. An exception must be raised.
     */
    public void testShort()
    {
        try
        {
            short source = ( short ) 1;
            m_ros.writeShort( source );
            m_ros.flush();
            short echo = m_ris.readShort();
            assertEquals( "Input/output stream not equal", source, echo );
            m_ros.writeShort( source );
            m_ros.flush();
            try
            {
                m_ris.readUnsignedShort();
            }
            catch ( java.lang.Exception expected )
            {
                // a short was written and we try to read an unsigned short
            }
            try
            {
                m_ris.available();
            }
            catch ( java.lang.Exception expected )
            {
                // no data available
            }
            try
            {
                m_ris.readFields();
            }
            catch ( java.lang.Exception expected )
            {
                // no data available
            }
            try
            {
                m_ris.readFully( new byte[ 0 ] );
            }
            catch ( java.lang.Exception expected )
            {
                // no data available
            }
            try
            {
                m_ris.readFully( new byte[ 0 ], 0, 0 );
            }
            catch ( java.lang.Exception expected )
            {
                // no data available
            }
        }
        catch ( java.lang.Exception ex )
        {
            fail( "Exception raised." );
        }
    }

    /**
     * Test narrowing incorrectly operations. An exception must be raised.
     */
    public void testByte()
    {
        try
        {
            byte source = ( byte ) 1;
            m_ros.writeByte( source );
            m_ros.flush();
            byte echo = m_ris.readByte();
            assertEquals( "Input/output stream not equal", source, echo );
            try
            {
                m_ris.readUnsignedByte();
            }
            catch ( java.lang.Exception expected )
            {
                // a byte was written and we try to read an unsigned byte
            }
        }
        catch ( java.lang.Exception ex )
        {
            fail( "Exception raised." );
        }
    }

    /**
     * Test narrowing incorrectly operations. An exception must be raised.
     */
    public void testFloat()
    {
        try
        {
            float source = ( float ) 1.1;
            m_ros.writeFloat( source );
            m_ros.flush();
            float echo = m_ris.readFloat();
            assertEquals( "Input/output stream not equal", ( int ) source, ( int ) echo );
        }
        catch ( java.lang.Exception ex )
        {
            fail( "Exception raised." );
        }
    }

    /**
     * Test narrowing incorrectly operations. An exception must be raised.
     */
    public void testDouble()
    {
        try
        {
            double source = ( double ) 1.2;
            m_ros.writeDouble( source );
            m_ros.flush();
            double echo = m_ris.readDouble();
            assertEquals( "Input/output stream not equal", ( int ) source, ( int ) echo );
        }
        catch ( java.lang.Exception ex )
        {
            fail( "Exception raised." );
        }
    }

    /**
     * Test narrowing incorrectly operations. An exception must be raised.
     */
    public void testInt()
    {
        try
        {
            int source = 1;
            m_ros.writeInt( source );
            m_ros.flush();
            int echo = m_ris.readInt();
            assertEquals( "Input/output stream not equal", source, echo );
        }
        catch ( java.lang.Exception ex )
        {
            fail( "Exception raised." );
        }
    }

    /**
     * The entry point of this test case.
     *
     * @param args The command line arguments.
     */
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( new TestSuite( StreamTest.class ) );
    }
}

