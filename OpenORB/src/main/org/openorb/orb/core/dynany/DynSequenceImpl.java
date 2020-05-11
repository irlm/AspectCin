/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dynany;

import org.openorb.orb.core.MinorCodes;
import org.omg.CORBA.CompletionStatus;

/**
 * Interface definition : DynSequence
 *
 * @author Jerome Daniel
 * @version $Revision: 1.4 $ $Date: 2004/02/13 11:43:05 $
 */
class DynSequenceImpl
    extends org.openorb.orb.core.dynany.DynAnyImpl
    implements org.omg.DynamicAny.DynSequence
{
    /**
     * Position de l'element actuel
     */
    private int m_current;

    /**
     * Liste des membres de la structure
     */
    private org.omg.DynamicAny.DynAny [] m_members;

    /**
     * Constructeur
     */
    public DynSequenceImpl( org.omg.DynamicAny.DynAnyFactory factory,
          org.omg.CORBA.ORB orb, org.omg.CORBA.TypeCode type )
    {
        super( factory, orb );

        m_type = type;
        m_current = 0;

        m_members = create_dyn_any_graph( type );

        rewind();
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
        m_members = ( ( DynSequenceImpl ) dyn_any ).copy_dyn_any_graph(
              ( ( DynSequenceImpl ) dyn_any ).m_members );
    }

    /**
     * Operation from_any
     */
    public void from_any( org.omg.CORBA.Any value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        org.omg.CORBA.portable.InputStream stream;

        if ( !value.type().equivalent( m_type ) )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        stream = value.create_input_stream();

        org.omg.CORBA.portable.InputStream input = value.create_input_stream();

        int max = input.read_long();

        if ( m_members.length != max )
        {
            try
            {
                m_members = new org.omg.DynamicAny.DynAny[ max ];

                for ( int i = 0; i < m_members.length; i++ )
                {
                    m_members[ i ] = create_dyn_any(
                          ( ( org.openorb.orb.core.typecode.TypeCodeBase )
                          type() )._base_type().content_type() );
                }
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
            {
                // TODO: ???
            }
        }
        stream_to_dyn_any_graph( m_members, stream );
    }

    /**
     * Operation to_any
     */
    public org.omg.CORBA.Any to_any()
    {
        org.omg.CORBA.Any any = m_orb.create_any();

        any.type( m_type );

        org.omg.CORBA.portable.OutputStream stream = any.create_output_stream();

        dyn_any_graph_to_stream( m_members, stream );

        return any;
    }

    /**
     * Operation destroy
     */
    public void destroy()
    {
        m_members = null;
        System.gc();
    }

    /**
     * Operation copy
     */
    public org.omg.DynamicAny.DynAny copy()
    {
        DynSequenceImpl dyn_sq = new DynSequenceImpl( m_factory, m_orb, m_type );

        try
        {
            dyn_sq.assign( this );
        }
        catch ( org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex )
        {
            // TODO: ???
        }
        return dyn_sq;
    }

    /**
     * Operation current_component
     */
    public org.omg.DynamicAny.DynAny current_component()
    {
        return m_members[ m_current ];
    }

    /**
     * Operation next
     */
    public boolean next()
    {
        m_current++;

        if ( m_current < m_members.length )
        {
            m_any = ( org.openorb.orb.core.Any ) m_members[ m_current ].to_any();

            return true;
        }

        m_current--;

        return false;
    }

    /**
     * Operation seek
     */
    public boolean seek( int index )
    {
        if ( index == -1 )
        {
            return false;
        }
        if ( index < m_members.length )
        {
            m_current = index;
            m_any = ( org.openorb.orb.core.Any ) m_members[ m_current ].to_any();
            return true;
        }

        return false;
    }

    /**
     * Operation rewind
     */
    public void rewind()
    {
        m_current = 0;

        if ( m_members.length != 0 )
        {
            m_any = ( org.openorb.orb.core.Any ) m_members[ 0 ].to_any();
        }
    }

    /**
     * Operation component_count
     */
    public int component_count()
    {
        return m_members.length;
    }

    /**
     * Read accessor for length attribute
     * @return the attribute value
     */
    public int get_length()
    {
        return m_members.length;
    }

    /**
     * Write accessor for length attribute
     * @param value the attribute value
     */
    public void set_length( int value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        org.omg.CORBA.TypeCode tc = null;

        if ( value < 0 )
        {
            throw new org.omg.CORBA.BAD_PARAM( MinorCodes.BAD_PARAM_ARRAY_INDEX,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        try
        {
            tc = ( ( org.openorb.orb.core.typecode.TypeCodeBase )
                  m_type )._base_type().content_type();
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            // TODO: ???
        }

        org.omg.DynamicAny.DynAny [] tmp = new org.omg.DynamicAny.DynAny[ value ];

        int max = m_members.length;

        if ( value < max )
        {
            max = value;
        }
        for ( int i = 0; i < max; i++ )
        {
            tmp[ i ] = m_members[ i ];
        }
        for ( int i = max; i < value; i++ )
        {
            tmp[ i ] = create_dyn_any( tc );
        }
        m_members = tmp;
    }

    /**
     * Operation get_elements
     */
    public org.omg.CORBA.Any[] get_elements()
    {
        org.omg.CORBA.Any [] list = new org.omg.CORBA.Any[ m_members.length ];

        for ( int i = 0; i < m_members.length; i++ )
        {
            list[ i ] = m_members[ i ].to_any();
        }

        return list;
    }

    /**
     * Operation set_elements
     */
    public void set_elements( org.omg.CORBA.Any[] value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( value == null )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        if ( value.length != m_members.length )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        if ( value.length != 0 )
        {
            try
            {
                if ( !value[ 0 ].type().equal( ( ( org.openorb.orb.core.typecode.TypeCodeBase )
                      m_type )._base_type().content_type() ) )
                {
                    throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
                }
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
            {
                // TODO: ???
            }
        }

        for ( int i = 0; i < value.length; i++ )
        {
            m_members[ i ].from_any( value[ i ] );
        }
    }

    /**
     * Operation get_elements_as_dyn_any
     */
    public org.omg.DynamicAny.DynAny [] get_elements_as_dyn_any()
    {
        return m_members;
    }

    /**
     * Operation set_elements_as_dyn_any
     */
    public void set_elements_as_dyn_any( org.omg.DynamicAny.DynAny [] value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( value.length != m_members.length )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        if ( value.length != 0 )
        {
            try
            {
                if ( !value[ 0 ].type().equivalent( ( ( org.openorb.orb.core.typecode.TypeCodeBase )
                      m_type )._base_type().content_type() ) )
                {
                    throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
                }
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
            {
                // TODO: ???
            }
        }

        for ( int i = 0; i < value.length; i++ )
        {
            m_members[ i ] = value[ i ].copy();
        }
    }
}

