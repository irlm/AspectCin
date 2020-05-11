/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

import java.io.Serializable;
import java.io.IOException;

/**
 * This class extends the standard InputStream to unmarshal RMI specific types.
 *
 * @author Jerome Daniel
 */
class RMIObjectInputStream
    extends java.io.ObjectInputStream
{
    private static org.omg.CORBA.WStringValueHelper s_string_helper
        = new org.omg.CORBA.WStringValueHelper();

    private ValueHandlerImpl m_handler;

    private org.omg.CORBA_2_3.portable.InputStream m_is;

    private Serializable m_value;

    private RMIObjectStreamClass m_value_data;

    void setContext( Serializable value, RMIObjectStreamClass valueData )
    {
        m_value = value;
        m_value_data = valueData;
    }

    /**
     * Constructor used for serializable objects
     */
    RMIObjectInputStream( ValueHandlerImpl handler, org.omg.CORBA_2_3.portable.InputStream is )
        throws IOException
    {
        m_handler = handler;
        m_is = is;
    }

    // object input stream interface.
    public void close()
        throws java.io.IOException
    {
        return;
    }

    public int available()
        throws java.io.IOException
    {
        try
        {
            return m_is.available();
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public int skipBytes( int param )
        throws java.io.IOException
    {
        try
        {
            return ( int ) m_is.skip( param );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public long skip( long param )
        throws java.io.IOException
    {
        try
        {
            return ( int ) m_is.skip( param );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public int read()
        throws java.io.IOException
    {
        try
        {
            return m_is.read();
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public int read( byte[] values )
        throws java.io.IOException
    {
        try
        {
            return m_is.read( values );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public int read( byte[] values, int param, int param2 )
        throws java.io.IOException
    {
        try
        {
            return m_is.read( values, param, param2 );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public boolean readBoolean()
        throws java.io.IOException
    {
        try
        {
            return m_is.read_boolean();
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public int readUnsignedByte()
        throws java.io.IOException
    {
        try
        {
            return m_is.read_octet() & 0xFF;
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public byte readByte()
        throws java.io.IOException
    {
        try
        {
            return m_is.read_octet();
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public int readUnsignedShort()
        throws java.io.IOException
    {
        try
        {
            return m_is.read_ushort() & 0xFFFF;
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public short readShort()
        throws java.io.IOException
    {
        try
        {
            return m_is.read_short();
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public int readInt()
        throws java.io.IOException
    {
        try
        {
            return m_is.read_long();
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public long readLong()
        throws java.io.IOException
    {
        try
        {
            return m_is.read_longlong();
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public double readDouble()
        throws java.io.IOException
    {
        try
        {
            return m_is.read_double();
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public float readFloat()
        throws java.io.IOException
    {
        try
        {
            return m_is.read_float();
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public char readChar()
        throws java.io.IOException
    {
        try
        {
            return m_is.read_wchar();
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public java.lang.String readUTF()
        throws java.io.IOException
    {
        try
        {
            Object val = m_is.read_value( s_string_helper );
            if ( val == null || ( val instanceof String ) )
            {
                return ( String ) val;
            }
            throw new java.io.InvalidObjectException( "Value read was not a string" );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    /**
     * @deprecated This method does not properly convert bytes to characters.
     * see DataInputStream for the details and alternatives.
     */
    public java.lang.String readLine()
        throws java.io.IOException
    {
        // a very dangerous function, but what the hey!
        StringBuffer ret = new StringBuffer();
        int r = read();
        char c;
        while ( r >= 0 && ( c = ( char ) r ) != '\n' )
        {
            if ( c != '\r' )
            {
                ret.append( c );
            }
            r = read();
        }
        return ret.toString();
    }

    public void readFully( byte[] values )
        throws java.io.IOException
    {
        try
        {
            m_is.read_octet_array( values, 0, values.length );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void readFully( byte[] values, int param, int param2 )
        throws java.io.IOException
    {
        try
        {
            m_is.read_octet_array( values, param, param2 );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    protected java.lang.Object readObjectOverride()
        throws java.io.OptionalDataException, java.lang.ClassNotFoundException, java.io.IOException
    {
        try
        {
            return m_is.read_abstract_interface();
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public void defaultReadObject()
        throws java.io.IOException, java.lang.ClassNotFoundException, java.io.NotActiveException
    {
        if ( m_value == null )
        {
            throw new java.io.NotActiveException();
        }
        try
        {
            m_handler.defaultReadObject( m_is, m_value, m_value_data );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            throw UtilDelegateImpl.mapSysIOException( ex );
        }
    }

    public java.io.ObjectInputStream.GetField readFields()
        throws java.io.IOException, java.lang.ClassNotFoundException, java.io.NotActiveException
    {
        if ( m_value == null )
        {
            throw new java.io.NotActiveException();
        }
        return m_handler.readFields( m_is, m_value, m_value_data );
    }

    public void registerValidation( java.io.ObjectInputValidation objectInputValidation, int param )
        throws java.io.NotActiveException, java.io.InvalidObjectException
    {
        throw new RuntimeException( "The ObjectInputStream.registerValidation() has not been"
              + " implemented yet!" );
    }
}

