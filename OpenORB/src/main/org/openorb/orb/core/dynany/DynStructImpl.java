/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dynany;

/**
 * DynStruct implementation.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.5 $ $Date: 2004/02/10 21:02:47 $
 */
class DynStructImpl
    extends org.openorb.orb.core.dynany.DynAnyImpl
    implements org.omg.DynamicAny.DynStruct
{
    /**
     * Current index in the struct
     */
    private int m_current;

    /**
     * Struct members
     */
    private org.omg.DynamicAny.DynAny [] m_members;

    /**
     * The spec for DynAny never mentions alias/typedef, and while it
     * never specifically says "drill down through any aliases to the
     * first non-alias type", it is implied by the frequent references
     * to TypeCode equivalence.
     */
    private final org.omg.CORBA.TypeCode m_baseType;

    /**
     * Constructor
     */
    public DynStructImpl( org.omg.DynamicAny.DynAnyFactory factory,
          org.omg.CORBA.ORB orb, org.omg.CORBA.TypeCode type )
    {
        super( factory, orb );

        m_type = type;
        m_baseType = ( ( org.openorb.orb.core.typecode.TypeCodeBase ) m_type )._base_type();

        m_members = create_dyn_any_graph( type );

        /*
         * 9-8, v2.5
         * The create_dyn_any operation sets the current position of
         * the created DynAny to zero if the passed value has
         * components; otherwise the current position is set to -1.
         */
        if ( m_members.length == 0 )
        {
            invalidateCurrent ();
        }
        else
        {
            rewind();
        }

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
        m_members = ( ( DynStructImpl ) dyn_any ).copy_dyn_any_graph(
            ( ( DynStructImpl ) dyn_any ).m_members );
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
        DynStructImpl dyn_st = new DynStructImpl( m_factory, m_orb, m_type );

        try
        {
            dyn_st.assign( this );
        }
        catch ( org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex )
        {
            handleTypeMismatchException ( ex );
        }

        return dyn_st;
    }

    /**
     * Operation current_component
     */
    public org.omg.DynamicAny.DynAny current_component()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {

        if ( m_members.length == 0 )
        {
            /*
             * 9-16, v2.5
             * Calling current_component on a DynAny that cannot have
             * components, such as a DynEnum or an empty exception,
             * raises TypeMismatch.
             */
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        else if ( m_current == -1 )
        {
            /*
             * 9-16, v2.5
             * Calling current_component on a DynAny whose current
             * position is -1 returns a nil reference.
             */
            return null;
        }
        else
        {
            return m_members[ m_current ];
        }
    }

    /**
     * Operation next
     */
    public boolean next()
    {
        m_current++;

        /*
         * 9-15, v2.5
         * The operation returns true while the resulting current
         * position indicates a component, false otherwise.
         */

        if ( m_current < m_members.length )
        {
            if ( m_members[ m_current ] != null )
            {
                m_any = ( org.openorb.orb.core.Any ) m_members[ m_current ].to_any();
            }
            return true;
        }
        else
        {
            /*
             * 9-15, v2.5
             * A false return value leaves the current position at
             * -1.  Invoking next on a DynAny without components
             *  leaves the current position at -1 and returns
             *  false.
             */
            invalidateCurrent ();
            return false;
        }
    }

    /**
     * Operation seek
     */
    public boolean seek( int index )
    {
        if ( index < 0 )
        {
            /*
             * 9-15, v2.5
             * Calling seek with a negative index is legal.  It
             * sets the current position to -1 to indicate no
             * component and returns false.
             */
            invalidateCurrent ();
            return false;
        }
        else if ( index < m_members.length )
        {
            m_current = index;

            if ( m_members[ m_current ] != null )
            {
                m_any = ( org.openorb.orb.core.Any ) m_members[ m_current ].to_any();
            }
            return true;
        }
        else
        {
            /*
             * 9-15, v2.5
             * Passing a non-negative index value for a DynAny
             * that does not have a component at the
             * corressponding position sets the current position
             * to -1 and returns false.
             */
            invalidateCurrent ();
            return false;
        }
    }

    /**
     * Operation rewind
     */
    public void rewind()
    {
        /*
         * 9-15, v2.5
         * The rewind operation is equivalent to calling seek(0)
         */
        seek( 0 );
    }

    /**
     * Operation component_count
     */
    public int component_count()
    {
        return m_members.length;
    }

    /**
     * Operation current_member_name
     */
    public java.lang.String current_member_name()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
            org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( m_current < 0 || m_current >= m_members.length )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue ();
        }

        try
        {
            return m_baseType.member_name ( m_current );
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            handleBadKindException ( ex );
        }
        catch ( org.omg.CORBA.TypeCodePackage.Bounds ex )
        {
            handleBoundsException ( ex );
        }

        throw new IllegalStateException ();
    }

    /**
     * Operation current_member_kind
     */
    public org.omg.CORBA.TCKind current_member_kind()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
            org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {

        if ( m_current < 0 || m_current >= m_members.length )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue ();
        }

        try
        {
            return m_baseType.member_type ( m_current ).kind();
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            handleBadKindException ( ex );
        }
        catch ( org.omg.CORBA.TypeCodePackage.Bounds ex )
        {
            handleBoundsException ( ex );
        }

        throw new IllegalStateException ();
    }

    /**
     * Operation get_members
     */
    public org.omg.DynamicAny.NameValuePair[] get_members()
    {
        org.omg.DynamicAny.NameValuePair [] list
            = new org.omg.DynamicAny.NameValuePair[ m_members.length ];

        try
        {
            for ( int i = 0; i < m_members.length; i++ )
            {
                list[ i ] = new org.omg.DynamicAny.NameValuePair();
                list[ i ].id = m_baseType.member_name( i );
                list[ i ].value = m_members[ i ].to_any();
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            handleBadKindException ( ex );
        }
        catch ( org.omg.CORBA.TypeCodePackage.Bounds ex )
        {
            handleBoundsException ( ex );
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
        /*
         * 9-18, v2.5
         * If the passed sequence has a number of elements that
         * disagrees with the number of members as indicated by the
         * DynStruct's TypeCode, the operation raises InvalidValue.
         *
         * Since m_members was built by traversing the TypeCode, its
         * length is "indicated by the DynStruct's TypeCode".
         */
        if ( value.length != m_members.length )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue ();
        }

        /*
         * 9-18, v2.5
         * The operation sets the current position to zero if the
         * passed sequence has non-zero length; otherwise, if an empty
         * sequence is passed, the current position is set to -1.
         */
        if ( value.length == 0 )
        {
            invalidateCurrent ();
        }
        else
        {
            rewind ();
        }


        try
        {
            for ( int i = 0; i < m_baseType.member_count(); i++ )
            {
                /*
                 * 9-18, v2.5
                 * Members must appear in the NameValuePairSeq in the
                 * order in which they appear in the IDL specification
                 * of the struct.
                 */
                if ( value[ i ].id.equals( "" ) )
                {
                    /*
                     * 9-18, v2.5
                     * If one or more sequence elements have a type
                     * that is not equivalent to the TypeCode of the
                     * corresponding member, the operation raises
                     * TypeMismatch.
                     */
                    if ( !value[ i ].value.type().equivalent( m_baseType.member_type( i ) ) )
                    {
                        throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
                    }
                }
                else
                {
                    /*
                     * 9-18, v2.5
                     * If member names are supplied in the passed
                     * sequence, they must either match the corresponding
                     * member name in the DynStruct's TypeCode or must be
                     * empty strings, otherwise, the operation raises
                     * TypeMismatch.
                     */
                    if ( !m_baseType.member_name( i ).equals( value[ i ].id ) )
                    {
                        throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
                    }
                }

                m_members[ i ].from_any( value[ i ].value );
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            handleBadKindException ( ex );
        }
        catch ( org.omg.CORBA.TypeCodePackage.Bounds ex )
        {
            handleBoundsException ( ex );
        }
    }

    /**
     * Operation get_members_as_dyn_any
     */
    public org.omg.DynamicAny.NameDynAnyPair [] get_members_as_dyn_any()
    {
        org.omg.DynamicAny.NameDynAnyPair [] list
              = new org.omg.DynamicAny.NameDynAnyPair[ m_members.length ];

        try
        {
            for ( int i = 0; i < m_members.length; i++ )
            {
                list[ i ] = new org.omg.DynamicAny.NameDynAnyPair();
                list[ i ].id = m_baseType.member_name( i );
                list[ i ].value = m_members[ i ];
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            handleBadKindException ( ex );
        }
        catch ( org.omg.CORBA.TypeCodePackage.Bounds ex )
        {
            handleBoundsException ( ex );
        }

        return list;
    }

    /**
     * Set the members from the given NameDynAnyPairSeq.  This code is
     * virtually identical to that of set_members(), but for
     * there seems to be no option but to duplicate it here if a major
     * performance hit is to be avoided.
     */
    public void set_members_as_dyn_any( org.omg.DynamicAny.NameDynAnyPair [] value )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
            org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        /*
         * 9-18, v2.5
         * If the passed sequence has a number of elements that
         * disagrees with the number of members as indicated by the
         * DynStruct's TypeCode, the operation raises InvalidValue.
         *
         * Since m_members was built by traversing the TypeCode, its
         * length is "indicated by the DynStruct's TypeCode".
         */
        if ( value.length != m_members.length )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue ();
        }

        /*
         * 9-18, v2.5
         * The operation sets the current position to zero if the
         * passed sequence has non-zero length; otherwise, if an empty
         * sequence is passed, the current position is set to -1.
         */
        if ( value.length == 0 )
        {
            invalidateCurrent ();
        }
        else
        {
            rewind ();
        }

        try
        {
            for ( int i = 0; i < m_baseType.member_count(); i++ )
            {
                /*
                 * 9-18, v2.5
                 * Members must appear in the NameDynAnyPairSeq in the
                 * order in which they appear in the IDL specification
                 * of the struct.
                 */
                if ( value[ i ].id.equals( "" ) )
                {
                    /*
                     * 9-18, v2.5
                     * If one or more sequence elements have a type
                     * that is not equivalent to the TypeCode of the
                     * corresponding member, the operation raises
                     * TypeMismatch.
                     */
                    if ( !value[ i ].value.type().equivalent ( m_baseType.member_type( i ) ) )
                    {
                        throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
                    }
                }
                else
                {
                    /*
                     * 9-18, v2.5
                     * If member names are supplied in the passed
                     * sequence, they must either match the corresponding
                     * member name in the DynStruct's TypeCode or must be
                     * empty strings, otherwise, the operation raises
                     * TypeMismatch.
                     */
                    if ( !m_baseType.member_name( i ).equals( value[ i ].id ) )
                    {
                        throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
                    }
                }

                m_members[ i ] = value[ i ].value;
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            handleBadKindException ( ex );
        }
        catch ( org.omg.CORBA.TypeCodePackage.Bounds ex )
        {
            handleBoundsException ( ex );
        }
    }

    private void invalidateCurrent ()
    {
        m_current = -1;
        m_any = null;
    }

    private void handleBadKindException( org.omg.CORBA.TypeCodePackage.BadKind e )
    {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error ( "BadKind", e );
            }
            throw new org.omg.CORBA.INTERNAL (
                org.omg.CORBA.OMGVMCID.value,
                org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE );
    }

    private void handleBoundsException( org.omg.CORBA.TypeCodePackage.Bounds e )
    {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error ( "Bounds", e );
            }
            throw new org.omg.CORBA.INTERNAL (
                org.omg.CORBA.OMGVMCID.value,
                org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE );
    }

    private void handleTypeMismatchException(
          org.omg.DynamicAny.DynAnyPackage.TypeMismatch e )
    {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error ( "TypeMismatch", e );
            }
            throw new org.omg.CORBA.INTERNAL (
                org.omg.CORBA.OMGVMCID.value,
                org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE );
    }
}

