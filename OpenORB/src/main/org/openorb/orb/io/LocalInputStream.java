/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.io;

import java.util.List;
import java.util.Iterator;

import org.openorb.orb.iiop.IIOPMinorCodes;

import org.omg.CORBA.CompletionStatus;

/**
 * implementation of {@link org.omg.CORBA.portable.InputStream}
 * for streaming local calls (that are not handled by stubs as JDK1.4 idl compiler)
 *
 * @author <a href="erik.putrycz@int-evry.fr">Erik Putrycz</a>
 */
public class LocalInputStream
    extends org.omg.CORBA_2_3.portable.InputStream
{
    private List m_arglist;
    private int m_pos = 0;

    /**
     * Creates a stringified representation of the stream contents.
     *
     * @return The result of the toString() method for each element of the stream.
     */
    public String toString ()
    {
        String res = "{";
        Iterator it = m_arglist.iterator ();

        while ( it.hasNext () )
        {
            Object obj = it.next ();
            res = res + obj.getClass ().getName () + "=" + obj.toString () + ",";
        }

        res = res + "}";
        return res;
    }

    /**
     * Set the pointer of the list to its beginning.
     */
    public void reset ()
    {
        m_pos = 0;
    }

    /**
     * Creates a new instance of LocalInputStream.
     *
     * @param arglist The {@link java.util.List} used for reading elements.
     */
    public LocalInputStream ( List arglist )
    {
        m_arglist = arglist;
    }

    //
    // org.omg.CORBA.portable.InputStream overrides
    //

    /**
     * Read an Object.
     *
     * @param clz Class for result type.
     * @return The object read from the stream.
     */
    public org.omg.CORBA.Object read_Object ( Class clz )
    {
        if ( m_pos >= m_arglist.size() )
        {
            return null;
        }
        Object res = m_arglist.get ( m_pos++ );

        if ( res == null )
        {
            return null;
        }
        Class[] interfaces_exp = clz.getInterfaces ();
        Class[] interfaces = res.getClass ().getInterfaces ();

        if ( interfaces_exp[ 0 ].isAssignableFrom ( interfaces[ 0 ] ) )
        {
            return ( org.omg.CORBA.Object ) res;
        }
        else
        {
            throw new org.omg.CORBA.BAD_PARAM (
                  "Incompatible class",
                  IIOPMinorCodes.BAD_PARAM_OBJ_CLASS,
                  CompletionStatus.COMPLETED_NO );
        }
    }

    /**
     * Read an Object.
     *
     * @return The object read from the stream.
     */
    public org.omg.CORBA.Object read_Object ()
    {
        Object res = m_arglist.get ( m_pos++ );
        return ( org.omg.CORBA.Object ) res;
    }

    /**
     * Reads a typecode.
     *
     * @return The type code read from the stream.
     */
    public org.omg.CORBA.TypeCode read_TypeCode ()
    {
        Object res = m_arglist.get ( m_pos++ );
        return ( org.omg.CORBA.TypeCode ) res;
    }

    /**
     * Read an any value.
     *
     * @return The any read from the stream.
     */
    public org.omg.CORBA.Any read_any ()
    {
        Object res = m_arglist.get ( m_pos++ );
        return ( org.omg.CORBA.Any ) res;
    }

    /**
     * Read a boolean.
     *
     * @return The boolean read from the stream.
     */
    public boolean read_boolean ()
    {
        Object res = m_arglist.get ( m_pos++ );
        return ( ( Boolean ) res ).booleanValue ();
    }

    /**
     * Read a boolean array.
     *
     * @param value The array to put the values in.
     * @param offset The offset from which to start reading.
     * @param length The number of boolean to read.
     */
    public void read_boolean_array ( boolean[] value, int offset, int length )
    {
        boolean[] v_array = ( boolean[] ) m_arglist.get ( m_pos++ );
        System.arraycopy ( v_array, 0, value, offset, length );
    }

    /**
     * Read a char.
     *
     * @return The char read from the stream.
     */
    public char read_char ()
    {
        Object res = m_arglist.get ( m_pos++ );
        return ( ( Character ) res ).charValue ();
    }

    /**
     * Read a char array.
     *
     * @param value The array to put the values into.
     * @param offset The offset from which to start reading.
     * @param length The number of chars to read.
     */
    public void read_char_array ( char[] value, int offset, int length )
    {
        char[] v_array = ( char[] ) m_arglist.get ( m_pos++ );
        System.arraycopy ( v_array, 0, value, offset, length );
    }

    public double read_double ()
    {
        Object res = m_arglist.get ( m_pos++ );
        return ( ( Double ) res ).doubleValue ();
    }

    public void read_double_array ( double[] value, int offset, int length )
    {
        double[] v_array = ( double[] ) m_arglist.get ( m_pos++ );
        System.arraycopy ( v_array, 0, value, offset, length );
    }

    public float read_float ()
    {
        Object res = m_arglist.get ( m_pos++ );
        return ( ( Float ) res ).floatValue ();
    }

    public void read_float_array ( float[] value, int offset, int length )
    {
        float[] v_array = ( float[] ) m_arglist.get ( m_pos++ );
        System.arraycopy ( v_array, 0, value, offset, length );
    }

    public int read_long ()
    {
        Object res = m_arglist.get ( m_pos++ );
        return ( ( Integer ) res ).intValue ();
    }

    public void read_long_array ( int[] value, int offset, int length )
    {
        int[] v_array = ( int[] ) m_arglist.get ( m_pos++ );
        System.arraycopy ( v_array, 0, value, offset, length );
    }

    public long read_longlong ()
    {
        Object res = m_arglist.get ( m_pos++ );
        return ( ( Long ) res ).longValue ();
    }

    public void read_longlong_array ( long[] value, int offset, int length )
    {
        long[] v_array = ( long[] ) m_arglist.get ( m_pos++ );
        System.arraycopy ( v_array, 0, value, offset, length );
    }

    public byte read_octet ()
    {
        Object res = m_arglist.get ( m_pos++ );
        return ( ( Byte ) res ).byteValue ();
    }

    public void read_octet_array ( byte[] value, int offset, int length )
    {
        byte[] v_array = ( byte[] ) m_arglist.get ( m_pos++ );
        System.arraycopy ( v_array, 0, value, offset, length );
    }

    public short read_short ()
    {
        Object res = m_arglist.get ( m_pos++ );
        return ( ( Short ) res ).shortValue ();
    }

    public void read_short_array ( short[] value, int offset, int length )
    {
        short[] v_array = ( short[] ) m_arglist.get ( m_pos++ );
        System.arraycopy ( v_array, 0, value, offset, length );
    }

    public String read_string ()
    {
        Object res = m_arglist.get ( m_pos++ );
        return ( String ) res;
    }

    public int read_ulong ()
    {
        return read_long ();
    }

    public void read_ulong_array ( int[] value, int offset, int length )
    {
        read_long_array ( value, offset, length );
    }

    public long read_ulonglong ()
    {
        return read_longlong ();
    }

    public void read_ulonglong_array ( long[] value, int offset, int length )
    {
        read_longlong_array ( value, offset, length );
    }

    public short read_ushort ()
    {
        return read_short ();
    }

    public void read_ushort_array ( short[] value, int offset, int length )
    {
        read_short_array ( value, offset, length );
    }

    public char read_wchar ()
    {
        return read_char ();
    }

    public void read_wchar_array ( char[] value, int offset, int length )
    {
        read_char_array ( value, offset, length );
    }

    public String read_wstring ()
    {
        return read_string ();
    }

    //
    // org.omg.CORBA_2_3.portable.InputStream overrides
    //

    public java.io.Serializable read_value()
    {
        return ( java.io.Serializable ) m_arglist.get ( m_pos++ );
    }

    public java.io.Serializable read_value( java.lang.String rep_id )
    {
        return read_value();
    }

    public java.io.Serializable read_value( java.lang.Class clz )
    {
        return read_value();
    }

    public java.io.Serializable read_value(
        org.omg.CORBA.portable.BoxedValueHelper factory )
    {
        return read_value();
    }

    public java.io.Serializable read_value( java.io.Serializable value )
    {
        return read_value();
    }

    public java.lang.Object read_abstract_interface()
    {
        return read_Object();
    }

    public java.lang.Object read_abstract_interface( java.lang.Class clz )
    {
        return read_Object();
    }
}

