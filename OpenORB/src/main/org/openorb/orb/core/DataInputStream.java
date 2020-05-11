/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core;

/**
 * This class is the implementation of DataInputStream used for custom marshalling
 * of value type.
 *
 * It simply delegates all calls to the InputStream.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.6 $ $Date: 2004/02/19 07:21:30 $
 */
public class DataInputStream
    implements org.omg.CORBA.DataInputStream
{
    /**
     * Reference to the InputStream
     */
    private org.omg.CORBA.portable.InputStream m_input;

    /**
     * Constructor
     */
    public DataInputStream( org.omg.CORBA.portable.InputStream istream )
    {
        m_input = istream;
    }

    /**
     * List of truncatable _ids
     */
    public String [] _truncatable_ids()
    {
        return null;
    }

    /**
     * Operation read_any
     */
    public org.omg.CORBA.Any read_any()
    {
        return m_input.read_any();
    }

    /**
     * Operation read_boolean
     */
    public boolean read_boolean()
    {
        return m_input.read_boolean();
    }

    /**
     * Operation read_char
     */
    public char read_char()
    {
        return m_input.read_char();
    }

    /**
     * Operation read_wchar
     */
    public char read_wchar()
    {
        return m_input.read_wchar();
    }

    /**
     * Operation read_octet
     */
    public byte read_octet()
    {
        return m_input.read_octet();
    }

    /**
     * Operation read_short
     */
    public short read_short()
    {
        return m_input.read_short();
    }

    /**
     * Operation read_ushort
     */
    public short read_ushort()
    {
        return m_input.read_ushort();
    }

    /**
     * Operation read_long
     */
    public int read_long()
    {
        return m_input.read_long();
    }

    /**
     * Operation read_ulong
     */
    public int read_ulong()
    {
        return m_input.read_ulong();
    }

    /**
     * Operation read_longlong
     */
    public long read_longlong()
    {
        return m_input.read_longlong();
    }

    /**
     * Operation read_ulonglong
     */
    public long read_ulonglong()
    {
        return m_input.read_ulonglong();
    }

    /**
     * Operation read_float
     */
    public float read_float()
    {
        return m_input.read_float();
    }

    /**
     * Operation read_double
     */
    public double read_double()
    {
        return m_input.read_double();
    }

    /**
     * Operation read_longdouble. This is not implemented.
     */
    public double read_longdouble()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation read_string
     */
    public java.lang.String read_string()
    {
        return m_input.read_string();
    }

    /**
     * Operation read_wstring
     */
    public java.lang.String read_wstring()
    {
        return m_input.read_wstring();
    }

    /**
     * Operation read_Object
     */
    public org.omg.CORBA.Object read_Object()
    {
        return m_input.read_Object();
    }

    /**
     * Operation read_Abstract
     */
    public java.lang.Object read_Abstract()
    {
        return ( ( org.omg.CORBA_2_3.portable.InputStream ) m_input ).read_abstract_interface();
    }

    /**
     * Operation read_value
     */
    public java.io.Serializable read_Value()
    {
        return ( ( org.omg.CORBA_2_3.portable.InputStream ) m_input ).read_value();
    }

    /**
     * Operation read_TypeCode
     */
    public org.omg.CORBA.TypeCode read_TypeCode()
    {
        return m_input.read_TypeCode();
    }

    /**
     * Operation read_any_array
     */
    public void read_any_array( org.omg.CORBA.AnySeqHolder seq, int offset, int length )
    {
        for ( int i = offset; i < offset + length; i++ )
        {
            seq.value[ i ] = m_input.read_any();
        }
    }

    /**
     * Operation read_boolean_array
     */
    public void read_boolean_array( org.omg.CORBA.BooleanSeqHolder seq, int offset, int length )
    {
        m_input.read_boolean_array( seq.value, offset, length );
    }

    /**
     * Operation read_char_array
     */
    public void read_char_array( org.omg.CORBA.CharSeqHolder seq, int offset, int length )
    {
        m_input.read_char_array( seq.value, offset, length );
    }

    /**
     * Operation read_wchar_array
     */
    public void read_wchar_array( org.omg.CORBA.WCharSeqHolder seq, int offset, int length )
    {
        m_input.read_wchar_array( seq.value, offset, length );
    }

    /**
     * Operation read_octet_array
     */
    public void read_octet_array( org.omg.CORBA.OctetSeqHolder seq, int offset, int length )
    {
        m_input.read_octet_array( seq.value, offset, length );
    }

    /**
     * Operation read_short_array
     */
    public void read_short_array( org.omg.CORBA.ShortSeqHolder seq, int offset, int length )
    {
        m_input.read_short_array( seq.value, offset, length );
    }

    /**
     * Operation read_ushort_array
     */
    public void read_ushort_array( org.omg.CORBA.UShortSeqHolder seq, int offset, int length )
    {
        m_input.read_ushort_array( seq.value, offset, length );
    }

    /**
     * Operation read_long_array
     */
    public void read_long_array( org.omg.CORBA.LongSeqHolder seq, int offset, int length )
    {
        m_input.read_long_array( seq.value, offset, length );
    }

    /**
     * Operation read_ulong_array
     */
    public void read_ulong_array( org.omg.CORBA.ULongSeqHolder seq, int offset, int length )
    {
        m_input.read_ulong_array( seq.value, offset, length );
    }

    /**
     * Operation read_longlong_array
     */
    public void read_longlong_array( org.omg.CORBA.LongLongSeqHolder seq, int offset, int length )
    {
        m_input.read_longlong_array( seq.value, offset, length );
    }

    /**
     * Operation read_ulonglong_array
     */
    public void read_ulonglong_array( org.omg.CORBA.ULongLongSeqHolder seq, int offset, int length )
    {
        m_input.read_ulonglong_array( seq.value, offset, length );
    }

    /**
     * Operation read_float_array
     */
    public void read_float_array( org.omg.CORBA.FloatSeqHolder seq, int offset, int length )
    {
        m_input.read_float_array( seq.value, offset, length );
    }

    /**
     * Operation read_double_array
     */
    public void read_double_array( org.omg.CORBA.DoubleSeqHolder seq, int offset, int length )
    {
        m_input.read_double_array( seq.value, offset, length );
    }
}
