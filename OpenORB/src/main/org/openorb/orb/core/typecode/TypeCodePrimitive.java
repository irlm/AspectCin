/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.typecode;

import java.util.Map;

import org.omg.CORBA.TCKind;

/**
 * Type code implemetation for primitive types.
 *
 * @author Chris Wood
 * @version $Revision: 1.5 $ $Date: 2004/02/17 22:13:55 $
 */
public final class TypeCodePrimitive
    extends TypeCodeBase
{
    private TypeCodePrimitive( TCKind kind )
    {
        m_kind = kind;
    }

    private TCKind m_kind;

    static final TypeCodePrimitive TC_NULL = new TypeCodePrimitive( TCKind.tk_null );
    static final TypeCodePrimitive TC_VOID = new TypeCodePrimitive( TCKind.tk_void );
    static final TypeCodePrimitive TC_SHORT = new TypeCodePrimitive( TCKind.tk_short );
    static final TypeCodePrimitive TC_LONG = new TypeCodePrimitive( TCKind.tk_long );
    static final TypeCodePrimitive TC_USHORT = new TypeCodePrimitive( TCKind.tk_ushort );
    static final TypeCodePrimitive TC_ULONG = new TypeCodePrimitive( TCKind.tk_ulong );
    static final TypeCodePrimitive TC_FLOAT = new TypeCodePrimitive( TCKind.tk_float );
    static final TypeCodePrimitive TC_DOUBLE = new TypeCodePrimitive( TCKind.tk_double );
    static final TypeCodePrimitive TC_BOOLEAN = new TypeCodePrimitive( TCKind.tk_boolean );
    static final TypeCodePrimitive TC_CHAR = new TypeCodePrimitive( TCKind.tk_char );
    static final TypeCodePrimitive TC_OCTET = new TypeCodePrimitive( TCKind.tk_octet );
    static final TypeCodePrimitive TC_ANY = new TypeCodePrimitive( TCKind.tk_any );
    static final TypeCodePrimitive TC_TYPECODE = new TypeCodePrimitive( TCKind.tk_TypeCode );
    static final TypeCodePrimitive TC_PRINCIPAL = new TypeCodePrimitive( TCKind.tk_Principal );

    static final TypeCodePrimitive TC_LONGLONG = new TypeCodePrimitive( TCKind.tk_longlong );
    static final TypeCodePrimitive TC_ULONGLONG = new TypeCodePrimitive( TCKind.tk_ulonglong );
    static final TypeCodePrimitive TC_LONGDOUBLE = new TypeCodePrimitive( TCKind.tk_longdouble );
    static final TypeCodePrimitive TC_WCHAR = new TypeCodePrimitive( TCKind.tk_wchar );

    boolean _is_recursive()
    {
        return false;
    }

    boolean _fix_recursive( Map recursive )
    {
        return true;
    }

    public boolean _is_compact()
    {
        return true;
    }

    public TypeCodeBase _base_type()
    {
        return this;
    }

    public org.omg.CORBA.TCKind kind()
    {
        return m_kind;
    }

    public boolean equivalent( org.omg.CORBA.TypeCode tc )
    {
        if ( tc instanceof TypeCodeBase )
        {
            return ( this == ( ( TypeCodeBase ) tc )._base_type() );
        }
        return false;
    }

    public boolean equal( org.omg.CORBA.TypeCode tc )
    {
        return ( this == tc );
    }

    public int hashCode()
    {
        return m_kind.value();
    }

    public org.omg.CORBA.TypeCode get_compact_typecode()
    {
        return this;
    }

    public java.lang.String id()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public int member_count()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public org.omg.CORBA.TypeCode member_type( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public org.omg.CORBA.Any member_label( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public int length()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public int default_index()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public java.lang.String member_name( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public java.lang.String name()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public org.omg.CORBA.TypeCode discriminator_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public org.omg.CORBA.TypeCode content_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public short fixed_digits()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public short fixed_scale()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public short member_visibility( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind,
                org.omg.CORBA.TypeCodePackage.Bounds
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public short type_modifier()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public org.omg.CORBA.TypeCode concrete_base_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }
}

