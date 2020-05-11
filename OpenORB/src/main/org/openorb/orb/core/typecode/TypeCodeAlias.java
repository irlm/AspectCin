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

import org.omg.CORBA.TypeCodePackage.BadKind;

/**
 * This class is the implementation for alias and valuebox typecodes.
 *
 * @author Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:48 $
 */
public class TypeCodeAlias
    extends TypeCodeBase
{
    private TCKind m_kind;
    private String m_id;
    private String m_name = null;
    private TypeCodeBase m_content;
    private TypeCodeAlias m_compact = null;
    private TypeCodeBase m_base = null;
    private boolean m_fixed = false;

    /** Creates new TypeCodeAlias */
    TypeCodeAlias( TCKind kind, String id, String name, org.omg.CORBA.TypeCode content )
    {
        m_kind = kind;
        m_id = id;
        m_content = ( TypeCodeBase ) content;

        if ( name != null && name.length() > 0 )
        {
            m_name = name;
        }
        else if ( m_content._is_compact() )
        {
            m_compact = this;
        }
        if ( kind != TCKind.tk_alias )
        {
            m_base = this;
        }
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

        if ( m_content._is_recursive() )
        {
            TypeCodeRecursive tmp = ( TypeCodeRecursive ) m_content;
            String id = "";
            try
            {
                id = tmp.id();
            }
            catch ( BadKind ex )
            {
                // never thrown by id()
            }
            if ( ( m_content = ( TypeCodeBase ) recursive.get( id ) ) == null )
            {
                m_fixed = false;
                m_content = tmp;
            }
        }
        else
        {
            m_fixed = m_content._fix_recursive( recursive );
        }

        return m_fixed;
    }

    public boolean _is_compact()
    {
        return ( m_compact == this );
    }

    public TypeCodeBase _base_type()
    {
        if ( m_base == null )
        {
            m_base = m_content._base_type();
        }

        return m_base;
    }

    public org.omg.CORBA.TCKind kind()
    {
        return m_kind;
    }

    public boolean equivalent( org.omg.CORBA.TypeCode tc )
    {
        return _base_type().equal( ( ( TypeCodeBase ) tc )._base_type() );
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
        TypeCodeAlias tc2 = ( TypeCodeAlias ) tc;

        if ( m_id.length() > 0 && tc2.m_id.length() > 0 )
        {
            return m_id.equals( tc2.m_id );
        }
        return m_content.equal( tc2.m_content );
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
            m_compact = new TypeCodeAlias( m_kind, m_id, null, m_content );
            m_compact.m_compact = m_compact;
            m_compact.m_content = ( TypeCodeBase ) m_content.get_compact_typecode();
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

    public org.omg.CORBA.TypeCode content_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        return m_content;
    }

    public int length()
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

    public org.omg.CORBA.TypeCode discriminator_type()
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

