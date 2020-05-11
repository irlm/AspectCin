/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dynany;

/**
 * DynFixed  implementation.
 *
 * @author Jerome Daniel
 */
public class DynFixedImpl
    extends org.openorb.orb.core.dynany.DynAnyImpl
    implements org.omg.DynamicAny.DynFixed
{
    private java.math.BigDecimal m_value;

    /**
     * Constructor
     */
    public DynFixedImpl( org.omg.DynamicAny.DynAnyFactory factory,
          org.omg.CORBA.ORB orb, org.omg.CORBA.TypeCode type )
    {
        super( factory, orb );
        m_type = type;
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
        m_value = ( ( DynFixedImpl ) dyn_any ).m_value;
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
        m_value = input.read_fixed();
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
        value.write_fixed( m_value );
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
        DynFixedImpl fixed = new DynFixedImpl( m_factory, m_orb, m_type );

        fixed.m_value = new java.math.BigDecimal( get_value() );

        return fixed;
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

    //
    // Operation get_value
    //
    public java.lang.String get_value()
    {
        return m_value.toString();
    }

    //
    // Operation set_value
    //
    public boolean set_value( java.lang.String val )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
              org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        boolean truncated = false;
        // -- removes optional 'd' or 'D' --
        if ( val.endsWith( "D" ) || val.endsWith( "d" ) )
        {
            val = val.substring( 0, val.length() - 1 );
        }
        // -- truncates fractional digits if required --
        String scale = val.substring( val.lastIndexOf( "." ), val.length() );
        String digits = val.substring( 0, val.lastIndexOf( "." ) );
        try
        {
            if ( scale.length() > m_type.fixed_scale() )
            {
                int delta = scale.length() - m_type.fixed_scale();
                val = val.substring( 0, scale.length() - delta );
                truncated = true;
            }
            if ( digits.length() > m_type.fixed_digits() )
            {
                throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
            }
            m_value = new java.math.BigDecimal( val );
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Trying to overwrite any value with String failed.", ex );
            }
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return !truncated;
    }
}

