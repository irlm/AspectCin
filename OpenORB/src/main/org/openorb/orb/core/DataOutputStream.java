/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core;

/**
 * This class is the implementation of DataOutputStream used for
 * custom marshalling of value type.
 *
 * It simply delegates to OutputStream all functions.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.5 $ $Date: 2004/02/10 21:46:22 $
 */
public class DataOutputStream
    implements org.omg.CORBA.DataOutputStream
{
    /**
     * Reference to the InputStream
     */
    private org.omg.CORBA.portable.OutputStream m_output;

    /**
     * Constructor
     */
    public DataOutputStream( org.omg.CORBA.portable.OutputStream ostream )
    {
        m_output = ostream;
    }

    /**
     * List of truncatable _ids
     */
    public String [] _truncatable_ids()
    {
        return null;
    }

    /**
     * Operation write_any
     */
    public void write_any( org.omg.CORBA.Any value )
    {
        m_output.write_any( value );
    }

    /**
     * Operation write_boolean
     */
    public void write_boolean( boolean value )
    {
        m_output.write_boolean( value );
    }

    /**
     * Operation write_char
     */
    public void write_char( char value )
    {
        m_output.write_char( value );
    }

    /**
     * Operation write_wchar
     */
    public void write_wchar( char value )
    {
        m_output.write_wchar( value );
    }

    /**
     * Operation write_octet
     */
    public void write_octet( byte value )
    {
        m_output.write_octet( value );
    }

    /**
     * Operation write_short
     */
    public void write_short( short value )
    {
        m_output.write_short( value );
    }

    /**
     * Operation write_ushort
     */
    public void write_ushort( short value )
    {
        m_output.write_ushort( value );
    }

    /**
     * Operation write_long
     */
    public void write_long( int value )
    {
        m_output.write_long( value );
    }

    /**
     * Operation write_ulong
     */
    public void write_ulong( int value )
    {
        m_output.write_ulong( value );
    }

    /**
     * Operation write_longlong
     */
    public void write_longlong( long value )
    {
        m_output.write_longlong( value );
    }

    /**
     * Operation write_ulonglong
     */
    public void write_ulonglong( long value )
    {
        m_output.write_ulonglong( value );
    }

    /**
     * Operation write_float
     */
    public void write_float( float value )
    {
        m_output.write_float( value );
    }

    /**
     * Operation write_double
     */
    public void write_double( double value )
    {
        m_output.write_double( value );
    }

    /**
     * Operation write_longdouble. This is not implemented.
     */
    public void write_longdouble( double value )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation write_string
     */
    public void write_string( java.lang.String value )
    {
        m_output.write_string( value );
    }

    /**
     * Operation write_wstring
     */
    public void write_wstring( java.lang.String value )
    {
        m_output.write_wstring( value );
    }

    /**
     * Operation write_Object
     */
    public void write_Object( org.omg.CORBA.Object value )
    {
        m_output.write_Object( value );
    }

    /**
     * Operation write_Abstract
     */
    public void write_Abstract( java.lang.Object value )
    {
        ( ( org.omg.CORBA_2_3.portable.OutputStream ) m_output ).write_abstract_interface( value );
    }

    /**
     * Operation write_value
     */
    public void write_Value( java.io.Serializable value )
    {
        ( ( org.omg.CORBA_2_3.portable.OutputStream ) m_output ).write_value( value );
    }

    /**
     * Operation write_TypeCode
     */
    public void write_TypeCode( org.omg.CORBA.TypeCode value )
    {
        m_output.write_TypeCode( value );
    }

    /**
     * Operation write_any_array
     */
    public void write_any_array( org.omg.CORBA.Any[] seq, int offset, int length )
    {
        for ( int i = offset; i < offset + length; i++ )
        {
            m_output.write_any( seq[ i ] );
        }
    }

    /**
     * Operation write_boolean_array
     */
    public void write_boolean_array( boolean[] seq, int offset, int length )
    {
        m_output.write_boolean_array( seq, offset, length );
    }

    /**
     * Operation write_char_array
     */
    public void write_char_array( char[] seq, int offset, int length )
    {
        m_output.write_char_array( seq, offset, length );
    }

    /**
     * Operation write_wchar_array
     */
    public void write_wchar_array( char[] seq, int offset, int length )
    {
        m_output.write_wchar_array( seq, offset, length );
    }

    /**
     * Operation write_octet_array
     */
    public void write_octet_array( byte[] seq, int offset, int length )
    {
        m_output.write_octet_array( seq, offset, length );
    }

    /**
     * Operation write_short_array
     */
    public void write_short_array( short[] seq, int offset, int length )
    {
        m_output.write_short_array( seq, offset, length );
    }

    /**
     * Operation write_ushort_array
     */
    public void write_ushort_array( short[] seq, int offset, int length )
    {
        m_output.write_ushort_array( seq, offset, length );
    }

    /**
     * Operation write_long_array
     */
    public void write_long_array( int[] seq, int offset, int length )
    {
        m_output.write_long_array( seq, offset, length );
    }

    /**
     * Operation write_ulong_array
     */
    public void write_ulong_array( int[] seq, int offset, int length )
    {
        m_output.write_ulong_array( seq, offset, length );
    }

    /**
     * Operation write_longlong_array
     */
    public void write_longlong_array( long[] seq, int offset, int length )
    {
        m_output.write_longlong_array( seq, offset, length );
    }

    /**
     * Operation write_ulonglong_array
     */
    public void write_ulonglong_array( long[] seq, int offset, int length )
    {
        m_output.write_ulonglong_array( seq, offset, length );
    }

    /**
     * Operation write_float_array
     */
    public void write_float_array( float[] seq, int offset, int length )
    {
        m_output.write_float_array( seq, offset, length );
    }

    /**
     * Operation write_double_array
     */
    public void write_double_array( double[] seq, int offset, int length )
    {
        m_output.write_double_array( seq, offset, length );
    }
}
