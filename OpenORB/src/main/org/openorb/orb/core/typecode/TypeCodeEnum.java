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
 * Typecode implementation for enum typecodes.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:48 $
 */
public class TypeCodeEnum
    extends TypeCodeBase
{
    private String m_id;
    private String m_name = null;
    private String [] m_members;
    private TypeCodeEnum m_compact = null;

    /** Creates new TypeCodeEnum */
    TypeCodeEnum( String id, String name, String[] members )
    {
        m_id = id;

        if ( name != null && name.length() > 0 )
        {
            m_name = name;
        }
        else
        {
            m_compact = this;
        }
        m_members = members;
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
        return m_compact == this;
    }

    public TypeCodeBase _base_type()
    {
        return this;
    }

    public org.omg.CORBA.TCKind kind()
    {
        return TCKind.tk_enum;
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
        if ( tc.kind() != TCKind.tk_enum )
        {
            return false;
        }
        TypeCodeEnum tc2 = ( TypeCodeEnum ) tc;

        if ( m_id.length() > 0 && tc2.m_id.length() > 0 )
        {
            return ( m_id.equals( tc2.m_id ) );
        }
        if ( m_members.length != tc2.m_members.length )
        {
            return false;
        }
        for ( int i = 0; i < m_members.length; ++i )
        {
            if ( !m_members[ i ].equals( tc2.m_members[ i ] ) )
            {
                return false;
            }
        }
        return true;
    }

    public int hashCode()
    {
        if ( m_id == null || m_id.length() == 0 )
        {
            return super.hashCode();
        }
        else
        {
            return m_id.hashCode();
        }
    }

    public org.omg.CORBA.TypeCode get_compact_typecode()
    {
        if ( m_compact == null )
        {
            m_compact = new TypeCodeEnum( m_id, null, m_members );
        }
        return m_compact;
    }

    public java.lang.String id()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        return m_id;
    }

    public java.lang.String name()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        if ( m_name == null )
        {
            return "";
        }
        return m_name;
    }

    public int member_count()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        return m_members.length;
    }

    public java.lang.String member_name( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds
    {
        if ( index < 0 || index >= m_members.length )
        {
            throw new org.omg.CORBA.TypeCodePackage.Bounds();
        }
        return m_members[ index ];
    }

    public int length()
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

