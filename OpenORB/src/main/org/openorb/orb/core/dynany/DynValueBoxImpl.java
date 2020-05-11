/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dynany;

/**
 * DynValueBox implementation.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:48 $
 */
public class DynValueBoxImpl
    extends org.openorb.orb.core.dynany.DynAnyImpl
    implements org.omg.DynamicAny.DynValueBox
{
    /**
     * Is a null value
     */
    private boolean m_null;

    /**
     * The boxed value
     */
    private org.omg.DynamicAny.DynAny m_boxed;

    /**
     * Constructor
     */
    public DynValueBoxImpl( org.omg.DynamicAny.DynAnyFactory factory,
          org.omg.CORBA.ORB orb, org.omg.CORBA.TypeCode type )
    {
        super( factory, orb );

        m_type = type;

        m_null = false;

        try
        {
            m_boxed = create_dyn_any( type.content_type() );
        }
        catch ( Exception ex )
        {
            // TODO: ???
        }
    }

    /**
     * Operation assign
     */
    public void assign( org.omg.DynamicAny.DynAny dynm_any )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( !dynm_any.type().equivalent( m_type ) )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        m_boxed = ( ( DynValueBoxImpl ) dynm_any ).m_boxed;
    }

    /**
     * Operation from_any
     */
    public void from_any( org.omg.CORBA.Any value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( !value.type().equivalent( m_type ) )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        try
        {
            org.openorb.orb.io.TypeCodeValueBoxHelper box =
                  new org.openorb.orb.io.TypeCodeValueBoxHelper( m_orb, m_type );

            org.omg.CORBA.portable.OutputStream output = m_orb.create_output_stream();

            box.write_value( output, value );

            ( ( DynAnyImpl ) m_boxed ).stream_to_dyn_any( m_boxed.type(),
                  output.create_input_stream() );
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Any BadKind exception.", ex );
            }
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
    }

    /**
     * Operation to_any
     */
    public org.omg.CORBA.Any to_any()
    {
        try
        {
            org.openorb.orb.io.TypeCodeValueBoxHelper box =
                  new org.openorb.orb.io.TypeCodeValueBoxHelper( m_orb, m_type );

            org.omg.CORBA.portable.OutputStream output = m_orb.create_output_stream();

            ( ( DynAnyImpl ) m_boxed ).dyn_any_to_stream( m_boxed.type(), output );

            org.omg.CORBA.portable.InputStream input = output.create_input_stream();

            return ( org.omg.CORBA.Any ) box.read_value( input );
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            // impossible
            return null;
        }
    }

    /**
     * Operation destroy
     */
    public void destroy()
    {
    }

    /**
     * Operation copy
     */
    public org.omg.DynamicAny.DynAny copy()
    {
        DynValueBoxImpl dyn_val = new DynValueBoxImpl( m_factory, m_orb, m_type );

        m_boxed = dyn_val.m_boxed.copy();

        return dyn_val;
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
    // Operation is_null
    //
    public boolean is_null()
    {
        return m_null;
    }

    //
    // Operation set_to_null
    //
    public void set_to_null()
    {
        m_null = true;
    }

    //
    // Operation set_to_value
    //
    public void set_to_value()
    {
        m_null = false;
    }

    //
    // Operation get_boxed_value
    //
    public org.omg.CORBA.Any get_boxed_value()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if ( m_null )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        return m_boxed.to_any();
    }

    //
    // Operation set_boxed_value
    //
    public void set_boxed_value( org.omg.CORBA.Any boxed )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        try
        {
            if ( !m_type.content_type().equal( boxed.type() ) )
            {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
            }
            m_boxed.from_any( boxed );
        }
        catch ( Exception ex )
        {
            // TODO: ???
        }
    }

    //
    // Operation get_boxed_value_as_dyn_any
    //
    public org.omg.DynamicAny.DynAny get_boxed_value_as_dyn_any()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if ( m_null )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        return m_boxed;
    }

    //
    // Operation set_boxed_value_as_dyn_any
    //
    public void set_boxed_value_as_dyn_any( org.omg.DynamicAny.DynAny boxed )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        try
        {
            if ( !m_type.content_type().equal( boxed.type() ) )
            {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
            }
            m_boxed = boxed;
        }
        catch ( java.lang.Exception ex )
        {
            // TODO: ???
        }
    }
}

