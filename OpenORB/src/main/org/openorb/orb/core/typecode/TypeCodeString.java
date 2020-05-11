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
 * Typecode implementation for strings and wstrings.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:48 $
 */
public class TypeCodeString
    extends TypeCodeBase
{
    static final TypeCodeString TC_STRING_0  = new TypeCodeString( TCKind.tk_string,  0 );
    static final TypeCodeString TC_WSTRING_0 = new TypeCodeString( TCKind.tk_wstring, 0 );

    private TCKind m_kind;
    private int m_len;

    /** Creates new TypeCodeString */
    TypeCodeString( TCKind kind, int len )
    {
        m_kind = kind;
        m_len = len;
    }

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
        return equal( ( ( TypeCodeBase ) tc )._base_type() );
    }

    public boolean equal( org.omg.CORBA.TypeCode tc )
    {
        if ( this == tc )
        {
            return true;
        }
        if ( tc.kind() != m_kind )
        {
            return false;
        }
        return ( ( ( TypeCodeString ) tc ).m_len == m_len );
    }

    public int hashCode()
    {
        return m_kind.value() << 24 | m_len;
    }

    public org.omg.CORBA.TypeCode get_compact_typecode()
    {
        return this;
    }

    public int length()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        return m_len;
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

