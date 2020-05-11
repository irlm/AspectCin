/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.primitive;

import java.util.Arrays;

import javax.rmi.PortableRemoteObject;

import junit.framework.TestSuite;

import org.openorb.orb.test.rmi.RMITestCase;

/**
 * This test suite provides a large set of primitive data types tests.
 *
 * All RMI data types are tested:
 * <ul>
 *   <li>a client application is sending a data type.</li>
 *   <li>a server application receives the data and returns it.</li>
 * </ul>
 *
 * @author Jerome Daniel
 */
public class PrimitiveTest
    extends RMITestCase
{
    /**
     * Remote interface implementation. This remote interface is the
     * basic server used to exchange the data types.
     *
     * @author Jerome Daniel
     */
    public static class EchoImpl
        implements RemoteEcho
    {
        /**
         * Echo nothing.
         *
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public void echo_void()
            throws java.rmi.RemoteException
        {
            // Nothing...
        }

        /**
         * Echo a boolean type.
         *
         * @param val A boolean value.
         * @return The boolean value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public boolean echo_boolean( boolean val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a byte type.
         *
         * @param val A byte value.
         * @return The byte value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public byte echo_byte( byte val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a char type.
         *
         * @param val A char value.
         * @return The char value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public char echo_char( char val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a short type.
         *
         * @param val A short value.
         * @return The short value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public short echo_short( short val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a float type.
         *
         * @param val A float value.
         * @return The float value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public float echo_float( float val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a double type.
         *
         * @param val A double value.
         * @return The double value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public double echo_double( double val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a int type.
         *
         * @param val A int value.
         * @return The int value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public int echo_int( int val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a long type.
         *
         * @param val A long value.
         * @return The long value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public long echo_long( long val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a String type.
         *
         * @param val A String value.
         * @return The String value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public String echo_string( String val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a boolean array type.
         *
         * @param val A boolean array value.
         * @return The boolean array value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public boolean[] echo_boolean_array( boolean[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a byte array type.
         *
         * @param val A byte array value.
         * @return The byte array value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public byte[] echo_byte_array( byte[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a char array type.
         *
         * @param val A char array value.
         * @return The char array value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public char[] echo_char_array( char[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a short array type.
         *
         * @param val A short array value.
         * @return The short array value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public short[] echo_short_array( short[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a float array type.
         *
         * @param val A float array value.
         * @return The float array value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public float[] echo_float_array( float[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a double array type.
         *
         * @param val A double array value.
         * @return The double array value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public double[] echo_double_array( double[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a int array type.
         *
         * @param val A int array value.
         * @return The int array value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public int[] echo_int_array( int[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a long array type.
         *
         * @param val A long array value.
         * @return The long array value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public long[] echo_long_array( long[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }

        /**
         * Echo a String array type.
         *
         * @param val A String array value.
         * @return The String array value.
         * @throws java.rmi.RemoteException When an error occurs.
         */
        public String[] echo_string_array( String[] val )
            throws java.rmi.RemoteException
        {
            return val;
        }
    }

    /**
     * Implementation.
     */
    private EchoImpl m_impl;

    /**
     * Stub, used by test cases.
     */
    private RemoteEcho m_stub;

    /**
     * Constructor.
     *
     * @param name The name of the test case.
     */
    public PrimitiveTest( String name )
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
            m_impl = new EchoImpl();
            PortableRemoteObject.exportObject( m_impl );
            java.rmi.Remote remote = PortableRemoteObject.toStub( m_impl );
            m_stub = ( RemoteEcho ) PortableRemoteObject.narrow( remote, RemoteEcho.class );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            fail( ex.toString() );
        }
    }

    /**
     * Dispose the test case.
     */
    protected void tearDown()
    {
        try
        {
            PortableRemoteObject.unexportObject( m_impl );
        }
        catch ( Exception ex )
        {
            // ignore
        }
    }

    /**
     * Test ping. This test is used to establish a connection between
     * the client and the object. All basic mechanisms to establish a
     * connection are used.
     *
     * @exception Exception if any unexpected problem occurs in the test case
     */
    public void testPrimitive()
        throws Exception
    {
        m_stub.echo_void();
    }

    /**
     * Test the exchange of boolean and array of booleans.
     *
     * @exception Exception if any unexpected problem occurs in the test case
     */
    public void testBoolean()
        throws Exception
    {
        boolean[] test = new boolean[] { true, false };
        for ( int i = 0; i < test.length; ++i )
        {
            assertEquals( "boolean echo failed", test[ i ], m_stub.echo_boolean( test[ i ] ) );
        }
        assertTrue( "echo boolean array failed",
              Arrays.equals( test, m_stub.echo_boolean_array( test ) ) );
        assertNull( "echo null boolean array failed", m_stub.echo_boolean_array( null ) );
    }

    /**
     * Test the exchange of byte and array of bytes.
     *
     * @exception Exception if any unexpected problem occurs in the test case
     */
    public void testByte()
        throws Exception
    {
        byte[] test = new byte[] { ( byte ) 0, ( byte ) 1, ( byte ) -1,
              Byte.MAX_VALUE, Byte.MIN_VALUE };
        for ( int i = 0; i < test.length; ++i )
        {
            assertEquals( "byte echo failed", test[ i ], m_stub.echo_byte( test[ i ] ) );
        }
        assertTrue( "echo byte array failed",
              Arrays.equals( test, m_stub.echo_byte_array( test ) ) );
        assertNull( "echo null byte array failed", m_stub.echo_byte_array( null ) );
    }

    /**
     * Test the exchange of short and array of shorts.
     *
     * @exception Exception if any unexpected problem occurs in the test case
     */
    public void testShort()
        throws Exception
    {
        short[] test = new short[] { ( short ) 0, ( short ) 1, ( short ) -1,
            Short.MAX_VALUE, Short.MIN_VALUE };
        for ( int i = 0; i < test.length; ++i )
        {
            assertEquals( "short echo failed", test[ i ], m_stub.echo_short( test[ i ] ) );
        }
        assertTrue( "echo short array failed",
              Arrays.equals( test, m_stub.echo_short_array( test ) ) );
        assertNull( "echo null short array failed", m_stub.echo_short_array( null ) );
    }

    /**
     * Test the exchange of int and array of ints.
     *
     * @exception Exception if any unexpected problem occurs in the test case
     */
    public void testInt()
        throws Exception
    {
        int[] test = new int[] { ( int ) 0, ( int ) 1, ( int ) -1,
            Integer.MAX_VALUE, Integer.MIN_VALUE };
        for ( int i = 0; i < test.length; ++i )
        {
            assertEquals( "int echo failed", test[ i ], m_stub.echo_int( test[ i ] ) );
        }
        assertTrue( "echo int array failed",
              Arrays.equals( test, m_stub.echo_int_array( test ) ) );
        assertNull( "echo null int array failed", m_stub.echo_int_array( null ) );
    }

    /**
     * Test the exchange of char and array of chars.
     *
     * @exception Exception if any unexpected problem occurs in the test case
     */
    public void testChar()
        throws Exception
    {
        char[] test = new char[] { 'a', 'ù', '\0', '\u0FFF', '\u0086' };
        for ( char i = 0; i < test.length; ++i )
        {
            assertEquals( "char echo failed", test[ i ], m_stub.echo_char( test[ i ] ) );
        }
        assertTrue( "echo char array failed",
              Arrays.equals( test, m_stub.echo_char_array( test ) ) );
        assertNull( "echo null char array failed", m_stub.echo_char_array( null ) );
    }

    /**
     * Test the exchange of float and array of floats.
     *
     * @exception Exception if any unexpected problem occurs in the test case
     */
    public void testFloat()
        throws Exception
    {
        float[] test = new float[] { ( float ) 1.0, ( float ) 1.1, ( float ) 1.3 };
        for ( char i = 0; i < test.length; ++i )
        {
            assertEquals( "float echo failed",
                  ( int ) test[ i ], ( int ) m_stub.echo_float( test[ i ] ) );
        }
        assertTrue( "echo float array failed",
              Arrays.equals( test, m_stub.echo_float_array( test ) ) );
        assertNull( "echo null float array failed", m_stub.echo_float_array( null ) );
    }

    /**
     * Test the exchange of double and array of double.
     *
     * @exception Exception if any unexpected problem occurs in the test case
     */
    public void testDouble()
        throws Exception
    {
        double[] test = new double[] { ( double ) 1.0, ( double ) 1.1, ( double ) 1.3 };
        for ( char i = 0; i < test.length; ++i )
        {
            assertEquals( "double echo failed",
                  ( int ) test[ i ], ( int ) m_stub.echo_double( test[ i ] ) );
        }
        assertTrue( "echo double array failed",
              Arrays.equals( test, m_stub.echo_double_array( test ) ) );
        assertNull( "echo null double array failed", m_stub.echo_double_array( null ) );
    }

    /**
     * The entry point of this application.
     *
     * @param args The command line parameters.
     */
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( new TestSuite( PrimitiveTest.class ) );
    }
}

