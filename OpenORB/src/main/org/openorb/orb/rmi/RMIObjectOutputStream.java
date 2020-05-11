/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;

/**
 * This class extends the standard OutputStream to marshall RMI specific types.
 *
 * @author Jerome Daniel
 */
class RMIObjectOutputStream
    extends ObjectOutputStream
{
    private static org.omg.CORBA.WStringValueHelper s_string_helper
          = new org.omg.CORBA.WStringValueHelper();

    private ValueHandlerImpl m_handler;

    private org.omg.CORBA_2_3.portable.OutputStream m_out_stream;

    private Serializable m_value;

    private RMIObjectStreamClass m_value_stream_class;

    private PutField m_fields;

    /**
     * Constructor used for serializable objects.
     */
    RMIObjectOutputStream( ValueHandlerImpl handler, org.omg.CORBA_2_3.portable.OutputStream os,
                           Serializable value, RMIObjectStreamClass valueData )
        throws IOException
    {
        m_handler = handler;
        m_out_stream = os;
        m_value = value;
        m_value_stream_class = valueData;
    }

    /**
     * Constructor used by externalizable objects.
     */
    RMIObjectOutputStream( ValueHandlerImpl handler, org.omg.CORBA_2_3.portable.OutputStream os )
        throws IOException
    {
        m_handler = handler;
        m_out_stream = os;
    }

    void setContext( Serializable value, RMIObjectStreamClass valueData )
    {
        m_value = value;
        m_value_stream_class = valueData;
        m_fields = null;
    }

    public void useProtocolVersion( int param )
        throws java.io.IOException
    {
    }

    public void close()
        throws java.io.IOException
    {
    }

    public void flush()
        throws java.io.IOException
    {
    }

    public void reset()
        throws java.io.IOException
    {
        throw new java.io.IOException();
    }

    public void write( int param )
        throws java.io.IOException
    {
        try
        {
            m_out_stream.write_octet( ( byte ) param );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void write( byte[] values )
        throws java.io.IOException
    {
        try
        {
            m_out_stream.write_octet_array( values, 0, values.length );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void write( byte[] values, int param, int param2 )
        throws java.io.IOException
    {
        try
        {
            m_out_stream.write_octet_array( values, param, param2 );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void writeBoolean( boolean param )
        throws java.io.IOException
    {
        try
        {
            m_out_stream.write_boolean( param );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void writeByte( int param )
        throws java.io.IOException
    {
        try
        {
            m_out_stream.write_octet( ( byte ) param );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void writeShort( int param )
        throws java.io.IOException
    {
        try
        {
            m_out_stream.write_short( ( short ) param );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void writeInt( int param )
        throws java.io.IOException
    {
        try
        {
            m_out_stream.write_long( param );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void writeLong( long param )
        throws java.io.IOException
    {
        try
        {
            m_out_stream.write_longlong( param );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void writeDouble( double param )
        throws java.io.IOException
    {
        try
        {
            m_out_stream.write_double( param );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void writeFloat( float param )
        throws java.io.IOException
    {
        try
        {
            m_out_stream.write_float( param );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void writeBytes( java.lang.String str )
        throws java.io.IOException
    {
        try
        {
            byte [] buf = str.getBytes();
            m_out_stream.write_octet_array( buf, 0, buf.length );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void writeChar( int param )
        throws java.io.IOException
    {
        try
        {
            m_out_stream.write_wchar( ( char ) param );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void writeChars( java.lang.String str )
        throws java.io.IOException
    {
        char [] dst = new char[ str.length() ];
        str.getChars( 0, dst.length, dst, 0 );
        try
        {
            m_out_stream.write_wchar_array( dst, 0, dst.length );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void writeUTF( java.lang.String str )
        throws java.io.IOException
    {
        try
        {
            m_out_stream.write_value( str, s_string_helper );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    protected void writeObjectOverride( Object obj )
        throws java.io.IOException
    {
        if ( obj != null )
        {
            Class clz = obj.getClass();
            if ( !( Serializable.class.isAssignableFrom( clz )
                  || org.omg.CORBA.Object.class.isAssignableFrom( clz ) ) )
            {
                throw new java.io.NotSerializableException( clz.getName() );
            }
        }
        try
        {
            m_out_stream.write_abstract_interface( obj );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void defaultWriteObject()
        throws java.io.IOException
    {
        if ( m_value == null )
        {
            throw new java.io.NotActiveException();
        }
        try
        {
            m_handler.defaultWriteObject( m_out_stream, m_value, m_value_stream_class );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public PutField putFields()
        throws java.io.IOException
    {
        if ( m_value == null )
        {
            throw new java.io.NotActiveException();
        }
        m_fields = m_handler.putFields( m_value, m_value_stream_class );
        return m_fields;
    }

    public void writeFields()
        throws java.io.IOException
    {
        if ( m_value == null || m_fields == null )
        {
            throw new java.io.NotActiveException();
        }
        m_handler.writeFields( m_out_stream, m_value, m_fields );
        m_fields = null;
    }
}

