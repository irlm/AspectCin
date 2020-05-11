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

import org.omg.CORBA.CompletionStatus;

import org.omg.CORBA.TypeCodePackage.BadKind;

/**
 * Typecode implementation for structs and exceptions.
 *
 * @author Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:48 $
 */
public class TypeCodeStruct
    extends TypeCodeBase
{
    private TCKind m_kind;
    private String m_id;
    private String m_name = null;
    private org.omg.CORBA.StructMember[] m_members;

    private TypeCodeStruct m_compact = null;
    private boolean m_fixed = false;

    /** Creates new TypeCodeObject */
    TypeCodeStruct( TCKind kind, String id, String name, org.omg.CORBA.StructMember[] members )
    {
        m_kind = kind;
        m_id = id;
        m_members = members;

        if ( name != null && name.length() > 0 )
        {
            m_name = name;
        }
        else
        {
            m_compact = this;

            for ( int i = 0; i < members.length; ++i )
            {
                if ( !( ( members[ i ].name == null || members[ i ].name.length() == 0 )
                        && ( ( TypeCodeBase ) members[ i ].type )._is_compact() ) )
                {
                    m_compact = null;
                    break;
                }
            }
        }
    }

    private TypeCodeStruct( TCKind kind, String id, org.omg.CORBA.StructMember[] members )
    {
        m_kind = kind;
        m_id = id;
        m_members = members;
        m_compact = this;
    }

    boolean _is_recursive()
    {
        return false;
    }

    boolean _fix_recursive( Map recursive )
    {
        recursive.put( m_id, this );

        if ( m_fixed )
        {
            return true;
        }
        m_fixed = true;

        for ( int i = 0; i < m_members.length; ++i )
        {
            if ( m_members[ i ].type instanceof TypeCodeRecursive )
            {
                TypeCodeRecursive tmp = ( TypeCodeRecursive ) m_members[ i ].type;
                String tmp_id = "";
                try
                {
                    tmp_id = tmp.id();
                }
                catch ( BadKind ex )
                {
                    // never thrown by id()
                }
                if ( ( m_members[ i ].type = ( TypeCodeBase ) recursive.get( tmp_id ) ) == null )
                {
                    m_fixed = false;
                    m_members[ i ].type = tmp;
                    return false;
                }
            }
            else
            {
                TypeCodeBase tcb = ( TypeCodeBase ) m_members[ i ].type;

                if ( tcb != null )
                {
                    m_fixed = tcb._fix_recursive( recursive );

                    if ( !m_fixed )
                    {
                        return false;
                    }
                }
            }
        }

        return m_fixed;
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
        TypeCodeStruct tc2 = ( TypeCodeStruct ) tc;

        if ( m_id.length() > 0 && tc2.m_id.length() > 0 )
        {
            return m_id.equals( tc2.m_id );
        }
        if ( m_members.length != tc2.m_members.length )
        {
            return false;
        }
        for ( int i = 0; i < m_members.length; ++i )
        {
            if ( !( m_members[ i ].name == null || m_members[ i ].name.length() == 0
                    || tc2.m_members[ i ].name == null || tc2.m_members[ i ].name.length() == 0
                    || m_members[ i ].name.equals( tc2.m_members[ i ].name ) )
                    || !m_members[ i ].type.equal( tc2.m_members[ i ].type ) )
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
        if ( m_name == null )
        {
            return this;
        }
        if ( m_compact == null )
        {
            m_compact = new TypeCodeStruct( m_kind, m_id,
                  ( org.omg.CORBA.StructMember[] ) m_members.clone() );
            m_compact.m_compact = m_compact;

            for ( int i = 0; i < m_members.length; ++i )
            {
                m_compact.m_members[ i ].name = null;
                m_compact.m_members[ i ].type = m_members[ i ].type.get_compact_typecode();
            }
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

    public org.omg.CORBA.TypeCode member_type( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds
    {
        if ( index >= m_members.length || index < 0 )
        {
            throw new org.omg.CORBA.TypeCodePackage.Bounds();
        }
        if ( m_members[ index ].type instanceof TypeCodeRecursive )
        {
            throw new org.omg.CORBA.BAD_TYPECODE( "Attempt to access incomplete typecode",
                  org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_MAYBE );
        }
        return m_members[ index ].type;
    }

    public java.lang.String member_name( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds
    {
        if ( index >= m_members.length || index < 0 )
        {
            throw new org.omg.CORBA.TypeCodePackage.Bounds();
        }
        return ( m_members[ index ].name == null ) ? "" : m_members[ index ].name;
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

    public org.omg.CORBA.Any member_label( int index )
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds
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

