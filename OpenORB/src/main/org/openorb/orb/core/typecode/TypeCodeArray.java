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
 * This class holds the implementation for sequence and array typecodes.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:48 $
 */
public class TypeCodeArray
    extends TypeCodeBase
{
    private TCKind m_kind;
    private int m_len;
    private TypeCodeBase m_element_type;

    private TypeCodeArray m_compact = null;
    private boolean m_fixed = false;

    /** Creates new TypeCodeObject */
    TypeCodeArray( TCKind kind, int len, org.omg.CORBA.TypeCode element_type )
    {
        m_kind = kind;
        m_len = len;
        m_element_type = ( TypeCodeBase ) element_type;

        if ( m_element_type._is_compact() )
        {
            m_compact = this;
        }
    }

    boolean _is_recursive()
    {
        return false;
    }

    boolean _fix_recursive( Map recursive )
    {
        if ( m_fixed )
        {
            return true;
        }
        m_fixed = true;

        if ( m_element_type._is_recursive() )
        {
            TypeCodeRecursive tmp = ( TypeCodeRecursive ) m_element_type;
            String tmp_id = "";
            try
            {
                tmp_id = tmp.id();
            }
            catch ( BadKind ex )
            {
                // never thrown by id()
            }
            if ( ( m_element_type = ( TypeCodeBase ) recursive.get( tmp_id ) ) == null )
            {
                m_fixed = false;
                m_element_type = tmp;
            }
        }
        else
        {
            m_fixed = m_element_type._fix_recursive( recursive );
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
        TypeCodeArray tc2 = ( TypeCodeArray ) tc;

        return ( m_len == tc2.m_len && m_element_type.equal( tc2.m_element_type ) );
    }

    public int hashCode()
    {
        return m_kind.value() << 24 | m_len;
    }

    public org.omg.CORBA.TypeCode get_compact_typecode()
    {
        if ( m_compact == null )
        {
            m_compact = new TypeCodeArray( m_kind, m_len, m_element_type );
            m_compact.m_compact = m_compact;
            m_compact.m_element_type = ( TypeCodeBase ) m_element_type.get_compact_typecode();
        }

        return m_compact;
    }

    public int length()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        return m_len;
    }

    public org.omg.CORBA.TypeCode content_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        return m_element_type;
    }

    public java.lang.String id()
        throws org.omg.CORBA.TypeCodePackage.BadKind
    {
        throw new org.omg.CORBA.TypeCodePackage.BadKind();
    }

    public java.lang.String name()
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

