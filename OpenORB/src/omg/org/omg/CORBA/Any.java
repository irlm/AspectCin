/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

public abstract class Any
    implements org.omg.CORBA.portable.IDLEntity
{
    public abstract boolean equal( org.omg.CORBA.Any a );

    public abstract org.omg.CORBA.TypeCode type();
    public abstract void type( org.omg.CORBA.TypeCode type );

    public abstract void read_value( org.omg.CORBA.portable.InputStream is,
          org.omg.CORBA.TypeCode type )
        throws org.omg.CORBA.MARSHAL;
    public abstract void write_value( org.omg.CORBA.portable.OutputStream os );

    public abstract org.omg.CORBA.portable.OutputStream create_output_stream();
    public abstract org.omg.CORBA.portable.InputStream create_input_stream();

    public abstract short extract_short()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_short( short s );

    public abstract int extract_long()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_long( int i );

    public abstract long extract_longlong()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_longlong( long l );

    public abstract short extract_ushort()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_ushort( short s );

    public abstract int extract_ulong()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_ulong( int i );

    public abstract long extract_ulonglong()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_ulonglong( long l );

    public abstract float extract_float()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_float( float f );

    public abstract double extract_double()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_double( double d );

    public abstract boolean extract_boolean()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_boolean( boolean b );

    public abstract char extract_char()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_char( char c );

    public abstract char extract_wchar()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_wchar( char c );

    public abstract byte extract_octet()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_octet( byte b );

    public abstract org.omg.CORBA.Any extract_any()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_any( org.omg.CORBA.Any a );

    public abstract org.omg.CORBA.Object extract_Object()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_Object( org.omg.CORBA.Object obj );

    public abstract java.io.Serializable extract_Value()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_Value( java.io.Serializable v );
    public abstract void insert_Value( java.io.Serializable v, org.omg.CORBA.TypeCode t )
        throws org.omg.CORBA.MARSHAL;

    public abstract void insert_Object( org.omg.CORBA.Object obj, org.omg.CORBA.TypeCode type )
        throws org.omg.CORBA.BAD_PARAM;

    public abstract String extract_string()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_string( String s )
        throws org.omg.CORBA.DATA_CONVERSION, org.omg.CORBA.MARSHAL;

    public abstract String extract_wstring()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_wstring( String value )
        throws org.omg.CORBA.MARSHAL;

    public abstract TypeCode extract_TypeCode()
        throws org.omg.CORBA.BAD_OPERATION;
    public abstract void insert_TypeCode( TypeCode value );

    /**
     * @deprecated
     */
    public Principal extract_Principal()
        throws org.omg.CORBA.BAD_OPERATION
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * @deprecated
     */
    public void insert_Principal( Principal p )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.portable.Streamable extract_Streamable()
        throws org.omg.CORBA.BAD_INV_ORDER
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void insert_Streamable( org.omg.CORBA.portable.Streamable s )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public java.math.BigDecimal extract_fixed()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void insert_fixed( java.math.BigDecimal f, org.omg.CORBA.TypeCode t )
        throws org.omg.CORBA.BAD_INV_ORDER
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
