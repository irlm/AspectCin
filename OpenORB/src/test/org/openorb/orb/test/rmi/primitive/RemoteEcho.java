/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.primitive;

/**
 * This interface provides primitive test operations.
 *
 * @author Jerome Daniel
 */
public interface RemoteEcho
    extends java.rmi.Remote
{
    //
    // -- simple values for RMI over IIOP --
    //

    /**
     * Echo nothing.
     *
     * @throws java.rmi.RemoteException When an error occurs.
     */
    void echo_void() throws java.rmi.RemoteException;

    /**
     * Echo a boolean type.
     *
     * @param val A boolean value.
     * @return The boolean value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    boolean echo_boolean( boolean val ) throws java.rmi.RemoteException;

    /**
     * Echo a byte type.
     *
     * @param val A byte value.
     * @return The byte value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    byte echo_byte( byte val ) throws java.rmi.RemoteException;

    /**
     * Echo a char type.
     *
     * @param val A char value.
     * @return The char value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    char echo_char( char val ) throws java.rmi.RemoteException;

    /**
     * Echo a short type.
     *
     * @param val A short value.
     * @return The short value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    short echo_short( short val ) throws java.rmi.RemoteException;

    /**
     * Echo a float type.
     *
     * @param val A float value.
     * @return The float value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    float echo_float( float val ) throws java.rmi.RemoteException;

    /**
     * Echo a double type.
     *
     * @param val A double value.
     * @return The double value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    double echo_double( double val ) throws java.rmi.RemoteException;

    /**
     * Echo a int type.
     *
     * @param val A int value.
     * @return The int value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    int echo_int( int val ) throws java.rmi.RemoteException;

    /**
     * Echo a long type.
     *
     * @param val A long value.
     * @return The long value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    long echo_long( long val ) throws java.rmi.RemoteException;

    //
    // -- array of simple values for RMI over IIOP --
    //

    /**
     * Echo a boolean array type.
     *
     * @param val A boolean array value.
     * @return The boolean array value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    boolean[] echo_boolean_array( boolean[] val ) throws java.rmi.RemoteException;

    /**
     * Echo a byte array type.
     *
     * @param val A byte array value.
     * @return The byte array value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    byte[] echo_byte_array( byte[] val ) throws java.rmi.RemoteException;

    /**
     * Echo a char array type.
     *
     * @param val A char array value.
     * @return The char array value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    char[] echo_char_array( char[] val ) throws java.rmi.RemoteException;

    /**
     * Echo a short array type.
     *
     * @param val A short array value.
     * @return The short array value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    short[] echo_short_array( short[] val ) throws java.rmi.RemoteException;

    /**
     * Echo a float array type.
     *
     * @param val A float array value.
     * @return The float array value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    float[] echo_float_array( float[] val ) throws java.rmi.RemoteException;

    /**
     * Echo a double array type.
     *
     * @param val A double array value.
     * @return The double array value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    double[] echo_double_array( double[] val ) throws java.rmi.RemoteException;

    /**
     * Echo a int array type.
     *
     * @param val A int array value.
     * @return The int array value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    int[] echo_int_array( int[] val ) throws java.rmi.RemoteException;

    /**
     * Echo a long array type.
     *
     * @param val A long array value.
     * @return The long array value.
     * @throws java.rmi.RemoteException When an error occurs.
     */
    long[] echo_long_array( long[] val ) throws java.rmi.RemoteException;
}

