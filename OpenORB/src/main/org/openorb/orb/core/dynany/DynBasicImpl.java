/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dynany;

/**
 * DynBasic implementation. This class manages DynAny operations for a basic type.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:47 $
 */
class DynBasicImpl
    extends DynAnyImpl
{
    /**
     * Constructor
     */
    public DynBasicImpl( org.omg.DynamicAny.DynAnyFactory factory,
          org.omg.CORBA.ORB orb, org.omg.CORBA.TypeCode type, org.omg.CORBA.Any value )
    {
        super( factory, orb );

        m_type = type;
        m_any = ( org.openorb.orb.core.Any ) value;
        m_any.type( m_type );
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
        m_any = ( ( DynBasicImpl ) dyn_any ).m_any;
    }

    /**
     * Operation from_any
     */
    public void from_any( org.omg.CORBA.Any value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        org.omg.CORBA.portable.InputStream input;

        if ( !value.type().equivalent( m_type ) )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        input = value.create_input_stream();

        //try {
        // if( input.available() == 0 )

        if ( ( ( org.openorb.orb.io.ListInputStream ) input ).getSourceSize() == 0 )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        //}
        //catch(java.io.IOException ex) {}

        m_any = ( org.openorb.orb.core.Any ) m_orb.create_any();

        m_any.type( m_type );

        m_any.read_value( input, m_type );
    }

    /**
     * Operation to_any
     */
    public org.omg.CORBA.Any to_any()
    {
        return m_any;
    }

    /**
     * Operation destroy
     */
    public void destroy()
    {
        m_any = null;
        System.gc();
    }

    /**
     * Operation copy
     */
    public org.omg.DynamicAny.DynAny copy()
    {
        return new DynBasicImpl( m_factory, m_orb, m_type, m_any );
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
}

