/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dynany;

/**
 * DynEnum implementation.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:47 $
 */
class DynEnumImpl
    extends org.openorb.orb.core.dynany.DynAnyImpl
    implements org.omg.DynamicAny.DynEnum
{
    /**
     * Enum value
     */
    private int m_enumValue;

    /**
     * Constructor
     */
    public DynEnumImpl( org.omg.DynamicAny.DynAnyFactory factory,
          org.omg.CORBA.ORB orb, org.omg.CORBA.TypeCode type, int value )
    {
        super( factory, orb );

        m_type = type;
        m_enumValue = value;
    }

    /**
     * Operation assign
     */
    public void assign( org.omg.DynamicAny.DynAny dyn_any )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( !dyn_any.type().equivalent( m_type ) )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        m_enumValue = ( ( DynEnumImpl ) dyn_any ).m_enumValue;
    }

    /**
     * Operation from_any
     */
    public void from_any( org.omg.CORBA.Any value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        org.omg.CORBA.portable.InputStream input;

        if ( !value.type().equal( m_type ) )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        input = value.create_input_stream();

        m_enumValue = input.read_ulong();
    }

    /**
     * Operation to_any
     */
    public org.omg.CORBA.Any to_any()
    {
        org.omg.CORBA.Any any = m_orb.create_any();

        org.omg.CORBA.portable.OutputStream value;

        any.type( m_type );
        value = any.create_output_stream();

        value.write_ulong( m_enumValue );

        return any;
    }

    /**
     * Operation destroy
     */
    public void destroy()
    {
        // Nothing to do...
    }

    /**
     * Operation copy
     */
    public org.omg.DynamicAny.DynAny copy()
    {
        return new DynEnumImpl( m_factory, m_orb, m_type, m_enumValue );
    }

    /**
     * Operation current_component
     */
    public org.omg.DynamicAny.DynAny current_component()
    {
        return this;
    }

    /**
     * Operation next
     */
    public boolean next()
    {
        return false;
    }

    /**
     * Operation seek
     */
    public boolean seek( int index )
    {
        if ( index == 0 )
        {
            return true;
        }
        return false;
    }

    /**
     * Operation rewind
     */
    public void rewind()
    {
    }

    /**
     * Operation component_count
     */
    public int component_count()
    {
        return 0;
    }

    /**
     * Read accessor for value_as_string attribute
     * @return the attribute value
     */
    public java.lang.String get_as_string()
    {
        String s = null;
        try
        {
            s = org.openorb.orb.core.typecode.TypeCodeBase._base_type(
                  m_type ).member_name( m_enumValue );
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            // null will be returned
        }
        catch ( org.omg.CORBA.TypeCodePackage.Bounds ex )
        {
            // null will be returned
        }
        return s;
    }

    /**
     * Write accessor for value_as_string attribute
     * @param value the attribute value
     */
    public void set_as_string( java.lang.String value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        try
        {
            org.omg.CORBA.TypeCode type =
                  org.openorb.orb.core.typecode.TypeCodeBase._base_type( m_type );

            for ( int i = 0; i < type.member_count(); i++ )
            {
                if ( type.member_name( i ).equals( value ) )
                {
                    m_enumValue = i;
                    return;
                }
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Trying to overwrite any value with String failed.", ex );
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.Bounds ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Any Bounds exception.", ex );
            }
        }
        throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
    }

    /**
     * Read accessor for value_as_ulong attribute
     * @return the attribute value
     */
    public int get_as_ulong()
    {
        return m_enumValue;
    }

    /**
     * Write accessor for value_as_ulong attribute
     * @param value the attribute value
     */
    public void set_as_ulong( int value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        m_enumValue = value;
    }
}

