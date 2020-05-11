/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dynany;

/**
 * DynValue implementation.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.7 $ $Date: 2004/02/10 21:02:48 $
 */
class DynValueImpl
    extends org.openorb.orb.core.dynany.DynAnyImpl
    implements org.omg.DynamicAny.DynValue
{
    /**
     * Current index
     */
    private int m_current;

    /**
     * Value member list
     */
    private org.omg.DynamicAny.DynAny [] m_members;

    private class TCContainer
    {
        private org.omg.CORBA.TypeCode m_tc;

        private String m_name;
    }

    private TCContainer [] m_tcMembers;

    private boolean m_null;

    /**
     * Constructor
     */
    public DynValueImpl( org.omg.DynamicAny.DynAnyFactory factory,
          org.omg.CORBA.ORB orb, org.omg.CORBA.TypeCode type )
    {
        super( factory, orb );

        m_type = type;
        m_current = 0;
        m_null = true;

        m_members = create_dyn_any_graph( type );

        m_tcMembers = create_tc_members( type );

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
        m_members = ( ( DynValueImpl ) dyn_any ).m_members;
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
        org.omg.CORBA.portable.StreamableValue val =
              ( org.omg.CORBA.portable.StreamableValue ) value.extract_Value();
        org.omg.CORBA.portable.OutputStream output = m_orb.create_output_stream();
        val._write( output );
        stream_to_dyn_any_graph( m_members, output.create_input_stream() );
    }

    /**
     * Operation to_any
     */
    public org.omg.CORBA.Any to_any()
    {
        try
        {
            org.openorb.orb.io.TypeCodeStreamableValue value =
                  new org.openorb.orb.io.TypeCodeStreamableValue( m_orb, m_type );

            value.create_output_stream();

            org.omg.CORBA.portable.OutputStream output = m_orb.create_output_stream();

            dyn_any_graph_to_stream( m_members, output );

            value._write( output );

            org.omg.CORBA.Any any = m_orb.create_any();
            any.type( m_type );
            any.insert_Value( value );

            return any;
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
        m_members = null;
        System.gc();
    }

    /**
     * Operation copy
     */
    public org.omg.DynamicAny.DynAny copy()
    {
        DynArrayImpl dyn_ay = new DynArrayImpl( m_factory, m_orb, m_type );

        try
        {
            dyn_ay.assign( this );
        }
        catch ( org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex )
        {
            // TODO: ???
        }
        return dyn_ay;
    }

    /**
     * Operation current_component
     */
    public org.omg.DynamicAny.DynAny current_component()
    {
        if ( m_members.length > 0 )
        {
            return m_members[ m_current ];
        }
        else
        {
            return null;
        }
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

        if ( m_members.length > 0 )
        {
            if ( m_members[ m_current ] != null )
            {
                m_any = ( org.openorb.orb.core.Any ) m_members[ 0 ].to_any();
            }
        }
    }

    /**
     * Operation component_count
     */
    public int component_count()
    {
        return m_members.length;
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
        m_members = null;
    }

    //
    // Operation set_to_value
    //
    public void set_to_value()
    {
        m_null = false;
        m_members = create_dyn_any_graph( m_type );
    }

    // REMOVE FROM SPECIFICATION
    protected String [] getValueIds( org.omg.CORBA.TypeCode typeCode )
    {
        String [] ret = null;
        String [] tmp = null;

        try
        {
            if ( typeCode.kind() == org.omg.CORBA.TCKind.tk_value )
            {
                if ( typeCode.concrete_base_type().kind() != org.omg.CORBA.TCKind.tk_null )
                {
                    tmp = getValueIds( typeCode.concrete_base_type() );
                    ret = new String[ tmp.length + 1 ];
                    ret[ 0 ] = typeCode.id();

                    for ( int i = 0; i < tmp.length; i++ )
                    {
                        ret[ 1 + i ] = tmp[ i ];
                    }
                }
                else
                {
                    ret = new String[ 1 ];
                    ret[ 0 ] = typeCode.id();
                }

            }
            else
            {
                ret = new String[ 1 ];
                ret[ 0 ] = typeCode.id();
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            // TODO: ???
        }
        return ret;
    }


    /**
     * Operation current_member_kind
     */
    public org.omg.CORBA.TCKind current_member_kind()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        try
        {
            if ( m_type.kind() == org.omg.CORBA.TCKind.tk_value )
            {
                return m_tcMembers[ m_current ].m_tc.kind();
            }
            else
            {
                return m_type.content_type().kind();
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            // TODO: ???
        }
        return org.omg.CORBA.TCKind.tk_null;
    }

    /**
     * Operation current_member_name
     */
    public java.lang.String current_member_name()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        try
        {
            if ( m_type.kind() == org.omg.CORBA.TCKind.tk_value )
            {
                return m_tcMembers[ m_current ].m_name;
            }
            else
            {
                return m_type.content_type().name();
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            // TODO: ???
        }
        return null;
    }

    /**
     * Operation get_members
     */
    public org.omg.DynamicAny.NameValuePair[] get_members()
    {
        org.omg.DynamicAny.NameValuePair [] list =
              new org.omg.DynamicAny.NameValuePair[ m_members.length ];

        try
        {
            if ( m_type.kind() == org.omg.CORBA.TCKind.tk_value )
            {
                for ( int i = 0; i < m_members.length; i++ )
                {
                    list[ i ] = new org.omg.DynamicAny.NameValuePair();
                    list[ i ].id = m_tcMembers[ i ].m_name;
                    list[ i ].value = m_members[ i ].to_any();
                }
            }
            else
            {
                list[ 0 ] = new org.omg.DynamicAny.NameValuePair();
                list[ 0 ].id = m_type.content_type().name();
                list[ 0 ].value = m_members[ 0 ].to_any();
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            // TODO: ???
        }
        return list;
    }

    /**
     * Operation set_members
     */
    public void set_members( org.omg.DynamicAny.NameValuePair[] value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        try
        {
            if ( m_type.kind() == org.omg.CORBA.TCKind.tk_value )
            {
                for ( int i = 0; i < m_members.length; i++ )
                {
                    if ( !m_type.member_name( i ).equals( value[ i ].id ) )
                    {
                        throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
                    }
                    m_members[ i ].from_any( value[ i ].value );
                }
            }
            else
            {
                if ( value.length != 1 )
                {
                    throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
                }
                if ( !m_type.content_type().name().equals( value[ 0 ].id ) )
                {
                    throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
                }
                m_members[ 0 ].from_any( value[ 0 ].value );
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            // TODO: ???
        }
        catch ( org.omg.CORBA.TypeCodePackage.Bounds ex )
        {
            // TODO: ???
        }
    }

    /**
     * Operation get_members_as_dyn_any
     */
    public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any()
    {
        org.omg.DynamicAny.NameDynAnyPair [] list =
              new org.omg.DynamicAny.NameDynAnyPair[ m_members.length ];

        try
        {
            if ( m_type.kind() == org.omg.CORBA.TCKind.tk_value )
            {
                for ( int i = 0; i < m_members.length; i++ )
                {
                    list[ i ] = new org.omg.DynamicAny.NameDynAnyPair();
                    list[ i ].id = m_tcMembers[ i ].m_name;
                    list[ i ].value = m_members[ i ];
                }
            }
            else
            {
                list[ 0 ] = new org.omg.DynamicAny.NameDynAnyPair();
                list[ 0 ].id = m_type.content_type().name();
                list[ 0 ].value = m_members[ 0 ];
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            // TODO: ???
        }
        return list;
    }

    /**
     * Operation set_members_as_dyn_any
     */
    public void set_members_as_dyn_any( org.omg.DynamicAny.NameDynAnyPair[] value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        try
        {
            if ( m_type.kind() == org.omg.CORBA.TCKind.tk_value )
            {
                for ( int i = 0; i < m_members.length; i++ )
                {
                    if ( !m_type.member_name( i ).equals( value[ i ].id ) )
                    {
                        throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
                    }
                    m_members[ i ] = value[ i ].value.copy();
                }
            }
            else
            {
                if ( value.length != 1 )
                {
                    throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
                }
                if ( !m_type.content_type().name().equals( value[ 0 ].id ) )
                {
                    throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
                }
                m_members[ 0 ] = value[ 0 ].value.copy();
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            // TODO: ???
        }
        catch ( org.omg.CORBA.TypeCodePackage.Bounds ex )
        {
            // TODO: ???
        }
    }


    /**
     * This operation creates a TypeCode list from the top inheritance.
     */
    private TCContainer [] create_tc_members( org.omg.CORBA.TypeCode tc )
    {
        TCContainer [] list = null;
        TCContainer [] base_list = null;

        if ( m_type.kind() == org.omg.CORBA.TCKind.tk_value )
        {
            try
            {
                org.omg.CORBA.TypeCode base = tc.concrete_base_type();

                if ( base.kind() != org.omg.CORBA.TCKind.tk_null )
                {
                    base_list = create_tc_members( base );
                }
                if ( base_list != null )
                {
                    list = new TCContainer[ base_list.length + tc.member_count() ];

                    for ( int i = 0; i < base_list.length; i++ )
                    {
                        list[ i ] = base_list[ i ];
                    }
                    for ( int i = 0; i < tc.member_count(); i++ )
                    {
                        list[ i + base_list.length ] = new TCContainer();
                        list[ i + base_list.length ].m_tc = tc.member_type( i );
                        list[ i + base_list.length ].m_name = tc.member_name( i );
                    }
                }
                else
                {
                    list = new TCContainer[ tc.member_count() ];

                    for ( int i = 0; i < list.length; i++ )
                    {
                        list[ i ] = new TCContainer();
                        list[ i ].m_tc = tc.member_type( i );
                        list[ i ].m_name = tc.member_name( i );
                    }
                }
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
            {
                // TODO: ???
            }
            catch ( org.omg.CORBA.TypeCodePackage.Bounds ex )
            {
                // TODO: ???
            }
        }
        return list;
    }
}

