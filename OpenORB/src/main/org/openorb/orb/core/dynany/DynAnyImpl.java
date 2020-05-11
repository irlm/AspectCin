/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dynany;

import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;

import org.openorb.orb.core.typecode.TypeCodeBase;

/**
 * DynAny implementation.
 * This class provides all standard operation that could be applied on each DynXXXX.
 * Some of its operation must be overloaded by sub classes to implement specific
 * mechanism.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.10 $ $Date: 2004/02/10 21:46:22 $
 */
abstract class DynAnyImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.DynamicAny.DynAny
{
    /**
     * Static counter for DynAny instances
     */
    protected static long s_dany_count = 0;

    /**
     * Reference to the ORB
     */
    protected org.omg.CORBA.ORB m_orb;

    /**
     * Reference to the DynAny factory
     */
    protected org.omg.DynamicAny.DynAnyFactory m_factory;

    /**
     * The DynAny TypeCode
     */
    protected org.omg.CORBA.TypeCode m_type;

    /**
     * The current typecode
     */
    protected org.omg.CORBA.TypeCode m_tc;

    /**
     * Reference to the current any
     */
    protected org.openorb.orb.core.Any m_any;

    /**
     * This instance count value
     */
    protected long m_count;

    /**
     *
     */
    private Logger m_logger;

    public DynAnyImpl( org.omg.DynamicAny.DynAnyFactory factory, org.omg.CORBA.ORB orb )
    {
        m_orb = orb;
        m_factory = factory;
        m_count = s_dany_count;
        s_dany_count++;
    }

    /**
     * Operation type
     */
    public org.omg.CORBA.TypeCode type()
    {
        return m_type;
    }

    /**
     * Operation assign
     */
    public abstract void assign( org.omg.DynamicAny.DynAny dyn_any )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

    /**
     * Operation from_any
     */
    public abstract void from_any( org.omg.CORBA.Any value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

    /**
     * Operation to_any
     */
    public abstract org.omg.CORBA.Any to_any();

    /**
     * Operation destroy
     */
    public abstract void destroy();

    /**
     * Operation copy
     */
    public abstract org.omg.DynamicAny.DynAny copy();

    /**
     * Operantion equal
     */
    public boolean equal( org.omg.DynamicAny.DynAny dyn_any )
    {
        if ( !dyn_any.type().equivalent( m_type ) )
        {
            return false;
        }
        org.omg.CORBA.Any a1 = to_any();

        org.omg.CORBA.Any a2 = dyn_any.to_any();

        if ( a1.equal( a2 ) )
        {
            return true;
        }
        return false;
    }

    /**
     * Operation insert_boolean
     */
    public void insert_boolean( boolean value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_boolean );

        verifyInsertionTypes();

        m_any.insert_boolean( value );
    }

    /**
     * Operation insert_octet
     */
    public void insert_octet( byte value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_octet );

        verifyInsertionTypes();

        m_any.insert_octet( value );
    }

    /**
     * Operation insert_char
     */
    public void insert_char( char value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_char );

        verifyInsertionTypes();

        m_any.insert_char( value );
    }

    /**
     * Operation insert_short
     */
    public void insert_short( short value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_short );

        verifyInsertionTypes();

        m_any.insert_short( value );
    }

    /**
     * Operation insert_ushort
     */
    public void insert_ushort( short value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_ushort );

        verifyInsertionTypes();

        m_any.insert_ushort( value );
    }

    /**
     * Operation insert_long
     */
    public void insert_long( int value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_long );

        verifyInsertionTypes();

        m_any.insert_long( value );
    }

    /**
     * Operation insert_ulong
     */
    public void insert_ulong( int value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_ulong );

        verifyInsertionTypes();

        m_any.insert_ulong( value );
    }

    /**
     * Operation insert_float
     */
    public void insert_float( float value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_float );

        verifyInsertionTypes();

        m_any.insert_float( value );
    }

    /**
     * Operation insert_double
     */
    public void insert_double( double value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_double );

        verifyInsertionTypes();

        m_any.insert_double( value );
    }

    /**
     * Operation insert_string
     */
    public void insert_string( java.lang.String value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        /*
         * For string, type equivalence is too strong a test -- type
         * equivalence does not allow for different sized strings.  Do
         * equivalent() (10-53, v2.5) by hand up to the length
         * comparison, where the test must determine if it is dealing
         * with a bounded string, and if so, whether the value
         * parameter will fit in the string.
         */

        if ( m_any == null )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        TypeCode anyBaseType = ( ( TypeCodeBase ) m_any.type() )._base_type();
        if ( anyBaseType.kind() != TCKind.tk_string )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        int length;
        try
        {
            length = anyBaseType.length();
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind e )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error ( "BadKind", e );
            }
            throw new org.omg.CORBA.INTERNAL (
                org.omg.CORBA.OMGVMCID.value,
                org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE );
        }

        if ( length > 0 && length < value.length() )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        /*
         * If the type() of this any is an aliased string, then a
         * simple insert() would change the type().  Make sure the
         * type is not changed.
         */
        TypeCode tc = m_any.type ();
        m_any.insert_string( value );
        m_any.type ( tc );
    }

    /**
     * Operation insert_reference
     */
    public void insert_reference( org.omg.CORBA.Object value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) m_any.type() )._base_type().kind()
              != TCKind.tk_objref )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        m_any.insert_Object( value );
    }

    /**
     * Operation insert_typecode
     */
    public void insert_typecode( org.omg.CORBA.TypeCode value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_TypeCode );

        verifyInsertionTypes();

        m_any.insert_TypeCode( value );
    }

    /**
     * Operation insert_longlong
     */
    public void insert_longlong( long value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_longlong );

        verifyInsertionTypes();

        m_any.insert_longlong( value );
    }

    /**
     * Operation insert_ulonglong
     */
    public void insert_ulonglong( long value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_ulonglong );

        verifyInsertionTypes();

        m_any.insert_ulonglong( value );
    }

    /**
     * Operation insert_wchar
     */
    public void insert_wchar( char value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_wchar );

        verifyInsertionTypes();

        m_any.insert_wchar( value );
    }

    /**
     * Operation insert_wstring
     */
    public void insert_wstring( java.lang.String value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc( TCKind.tk_wstring );

        try
        {
            verifyInsertionTypes();
        }
        catch ( org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex )
        {
            m_tc = m_orb.create_wstring_tc( value.length() );

            verifyInsertionTypes();
        }

        m_any.insert_wstring( value );
        m_any.type( m_tc );
    }

    /**
     * Operation insert_any
     */
    public void insert_any( org.omg.CORBA.Any value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_any );

        verifyInsertionTypes();

        m_any.insert_any( value );
    }

    /**
     * Operation insert_dyn_any
     */
    public void insert_dyn_any( org.omg.DynamicAny.DynAny value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        org.omg.CORBA.Any a = value.to_any();

        // a corba 2.4 conformant implementation
        // has to insert the any one level below,
        // i.e. the anys are NESTED.
        // see insert_any.
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_any );

        verifyInsertionTypes();

        m_any.insert_any( a );
    }

    /**
     * Operation insert_val
     */
    public void insert_val( java.io.Serializable value )
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( value instanceof org.omg.CORBA.portable.StreamableValue )
        {
            m_tc = m_orb.get_primitive_tc ( TCKind.tk_value );
        }
        else
        {
            m_tc = m_orb.get_primitive_tc ( TCKind.tk_value_box );
        }
        verifyInsertionTypes();

        m_any.insert_Value( value );
    }

    //
    // Operation insert_abstract
    //
    public void insert_abstract( java.lang.Object value )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
              org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if ( m_type.kind().value() != org.omg.CORBA.TCKind._tk_abstract_interface )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        if ( !( value instanceof java.io.Serializable ) )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        m_any.insert_Value( ( java.io.Serializable ) value, m_type );
    }

    /**
     * Operation get_boolean
     */
    public boolean get_boolean()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_boolean );

        verifyExtractionTypes();

        boolean b = m_any.extract_boolean();

        return b;
    }

    /**
     * Operation get_octet
     */
    public byte get_octet()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_octet );

        verifyExtractionTypes();

        byte b = m_any.extract_octet();

        return b;
    }

    /**
     * Operation get_char
     */
    public char get_char()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_char );

        verifyExtractionTypes();

        char c = m_any.extract_char();

        return c;
    }

    /**
     * Operation get_short
     */
    public short get_short()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_short );

        verifyExtractionTypes();

        short s = m_any.extract_short();

        return s;
    }

    /**
     * Operation get_ushort
     */
    public short get_ushort()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_ushort );

        verifyExtractionTypes();

        short s = m_any.extract_ushort();

        return s;
    }

    /**
     * Operation get_long
     */
    public int get_long()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_long );

        verifyExtractionTypes();

        int l = m_any.extract_long();

        return l;
    }

    /**
     * Operation get_ulong
     */
    public int get_ulong()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_ulong );

        verifyExtractionTypes();

        int l = m_any.extract_ulong();

        return l;
    }

    /**
     * Operation get_float
     */
    public float get_float()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_float );

        verifyExtractionTypes();

        float f = m_any.extract_float();

        return f;
    }

    /**
     * Operation get_double
     */
    public double get_double()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_double );

        verifyExtractionTypes();

        double d = m_any.extract_double();

        return d;
    }

    /**
     * Operation get_string
     */
    public java.lang.String get_string()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc( TCKind.tk_string );

        verifyExtractionTypes();

        String s = m_any.extract_string();

        return s;
    }

    /**
     * Operation get_reference
     */
    public org.omg.CORBA.Object get_reference()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) m_any.type() )._base_type().kind()
              != TCKind.tk_objref )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        org.omg.CORBA.Object obj = m_any.extract_Object();

        return obj;
    }

    /**
     * Operation get_typecode
     */
    public org.omg.CORBA.TypeCode get_typecode()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_TypeCode );

        verifyExtractionTypes();

        org.omg.CORBA.TypeCode tc = m_any.extract_TypeCode();

        return tc;
    }

    /**
     * Operation get_longlong
     */
    public long get_longlong()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_longlong );

        verifyExtractionTypes();

        long ll = m_any.extract_longlong();

        return ll;
    }

    /**
     * Operation get_ulonglong
     */
    public long get_ulonglong()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_ulonglong );

        verifyExtractionTypes();

        long ll = m_any.extract_ulonglong();

        return ll;
    }

    /**
     * Operation get_wchar
     */
    public char get_wchar()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_wchar );

        verifyExtractionTypes();

        char wc = m_any.extract_wchar();

        return wc;
    }

    /**
     * Operation get_wstring
     */
    public java.lang.String get_wstring()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc( TCKind.tk_wstring );

        verifyExtractionTypes();

        String ws = m_any.extract_wstring();

        return ws;
    }

    /**
     * Operation get_any
     */
    public org.omg.CORBA.Any get_any()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_any );

        verifyExtractionTypes();

        org.omg.CORBA.Any any = m_any.extract_any();

        return any;
    }

    /**
     * Operation get_dyn_any
     */
    public org.omg.DynamicAny.DynAny get_dyn_any()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        // see get_any.
        m_tc = m_orb.get_primitive_tc ( TCKind.tk_any );

        verifyExtractionTypes();

        org.omg.DynamicAny.DynAny dany = null;

        try
        {
            // a corba 2.4 conformant implementation
            // has to extract the any from one level below,
            // i.e. the anys were NESTED.
            org.omg.CORBA.Any any = m_any.extract_any();
            TypeCode tc = any.type();
            dany = m_factory.create_dyn_any_from_type_code( tc );
            dany.from_any( any );
        }
        catch ( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode ex )
        {
            // TODO: ???
        }

        return dany;
    }

    /**
     * Operation get_val
     */
    public java.io.Serializable get_val()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
              org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        try
        {
            m_tc = m_orb.get_primitive_tc ( TCKind.tk_value );

            verifyExtractionTypes();
        }
        catch ( org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex )
        {
            m_tc = m_orb.get_primitive_tc ( TCKind.tk_value_box );

            verifyExtractionTypes();
        }

        java.io.Serializable ser = m_any.extract_Value();

        return ser;
    }

    /**
     * Operation get_abstract
     */
    public java.lang.Object get_abstract()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
              org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if ( m_any.type().kind().value() != org.omg.CORBA.TCKind._tk_abstract_interface )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
        return m_any.extract_Object( );
    }

    /**
     * Operation current_component
     */
    public abstract org.omg.DynamicAny.DynAny current_component()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch;

    /**
     * Operation component_count
     */
    public abstract int component_count();

    /**
     * Operation next
     */
    public abstract boolean next();

    /**
     * Operation seek
     */
    public abstract boolean seek( int index );

    /**
     * Operation rewind
     */
    public abstract void rewind();

    /**
     * This operation is used to check if two typecodes are compatible.
     */
    protected void verifyInsertionTypes()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
              org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if ( m_any == null )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        if ( !( ( org.openorb.orb.core.typecode.TypeCodeBase ) m_tc )._base_type().equivalent(
              ( ( org.openorb.orb.core.typecode.TypeCodeBase ) m_any.type() )._base_type() ) )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
    }

    /**
     * This operation checks if a value to extract is compliant with the expected type
     */
    protected void verifyExtractionTypes()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
              org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if ( m_any == null )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
        if ( !( ( org.openorb.orb.core.typecode.TypeCodeBase ) m_tc )._base_type().equal(
              ( ( org.openorb.orb.core.typecode.TypeCodeBase ) m_any.type() )._base_type() ) )
        {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
    }

    /**
     * This operation creates a DynAny from a TypeCode
     */
    protected org.omg.DynamicAny.DynAny create_dyn_any( org.omg.CORBA.TypeCode tc )
    {
        try
        {
            return m_factory.create_dyn_any_from_type_code( tc );
        }
        catch ( java.lang.Exception ex )
        {
            return null;
        }
    }

    /**
     * This operation creates a DynAny graph from a TypeCode. A "Graph" means that
     * a DynAny is created for each element of the TypeCode. For example, if the
     * TypeCode corresponds to a Struct, the "Graph" will contain a DynAny for
     * each struct member.
     */
    protected org.omg.DynamicAny.DynAny[] create_dyn_any_graph( org.omg.CORBA.TypeCode tc )
    {
        org.omg.DynamicAny.DynAny [] result = null;

        switch ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) tc )._base_type().kind().value() )
        {
        case TCKind._tk_null :
        case TCKind._tk_void :
        case TCKind._tk_short :
        case TCKind._tk_long :
        case TCKind._tk_longlong :
        case TCKind._tk_ushort :
        case TCKind._tk_ulong :
        case TCKind._tk_ulonglong :
        case TCKind._tk_float :
        case TCKind._tk_double :
        case TCKind._tk_boolean :
        case TCKind._tk_char :
        case TCKind._tk_wchar :
        case TCKind._tk_octet :
        case TCKind._tk_any :
        case TCKind._tk_TypeCode :
        case TCKind._tk_objref :
        case TCKind._tk_string :
        case TCKind._tk_wstring :
            // primitive types must be initialized correctly
            result = new org.omg.DynamicAny.DynAny[ 1 ];
            result[ 0 ] = create_dyn_any( tc );
            break;

        // longdoubles are not handled
        case TCKind._tk_longdouble :
            throw new org.omg.CORBA.NO_IMPLEMENT();

        case TCKind._tk_union :
            try
            {
                // union fields must be initialized correctly
                org.omg.CORBA.TypeCode base = ( ( org.openorb.orb.core.typecode.TypeCodeBase )
                         tc )._base_type();
                result = new org.omg.DynamicAny.DynAny[ 2 ];
                result[ 0 ] = create_dyn_any( base.discriminator_type() );
                result[ 1 ] = create_dyn_any( base.member_type( 0 ) );
            }
            catch ( org.omg.CORBA.TypeCodePackage.Bounds ex )
            {
                // TODO: ???
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
            {
                // TODO: ???
            }
            break;

        case TCKind._tk_except :
        case TCKind._tk_struct :
            try
            {
                result = new org.omg.DynamicAny.DynAny[ ( (
                      org.openorb.orb.core.typecode.TypeCodeBase )
                      tc )._base_type().member_count() ];

                for ( int i = 0; i < result.length; i++ )
                {
                    result[ i ] = create_dyn_any( ( (
                          org.openorb.orb.core.typecode.TypeCodeBase )
                          tc )._base_type().member_type( i ) );
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
            break;

        case TCKind._tk_enum :
            result = new org.omg.DynamicAny.DynAny[ 1 ];
            result[ 0 ] = new DynEnumImpl( m_factory, m_orb, tc, 0 );
            break;

        case TCKind._tk_array :
            try
            {
                result = new org.omg.DynamicAny.DynAny[ ( (
                      org.openorb.orb.core.typecode.TypeCodeBase ) tc )._base_type().length() ];

                for ( int i = 0; i < result.length; i++ )
                {
                    result[ i ] = create_dyn_any( ( (
                          org.openorb.orb.core.typecode.TypeCodeBase )
                          tc )._base_type().content_type() );
                }
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
            {
                // TODO: ???
            }
            break;

        case TCKind._tk_sequence :
            result = new org.omg.DynamicAny.DynAny[ 0 ];
            break;

        case TCKind._tk_fixed :
            result = new org.omg.DynamicAny.DynAny[ 1 ];
            result[ 0 ] = new DynFixedImpl( m_factory, m_orb, tc );
            break;

        case TCKind._tk_value :
            org.omg.CORBA.TypeCode [] member_list = getValueMember( tc );
            result = new org.omg.DynamicAny.DynAny[ member_list.length ];
            for ( int i = 0; i < result.length; i++ )
            {
                result[ i ] = create_dyn_any( member_list[ i ] );
            }
            break;

        case TCKind._tk_value_box :
            result = new org.omg.DynamicAny.DynAny[ 1 ];
            try
            {
                result[ 0 ] = create_dyn_any( ( (
                      org.openorb.orb.core.typecode.TypeCodeBase )
                      tc )._base_type().content_type() );
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
            {
                // TODO: ???
            }
            break;
        }

        return result;
    }

    /**
     * This operation is used to copy a DynAny graph.
     */
    protected org.omg.DynamicAny.DynAny[] copy_dyn_any_graph( org.omg.DynamicAny.DynAny[] src )
    {
        org.omg.DynamicAny.DynAny [] result = new org.omg.DynamicAny.DynAny[ src.length ];

        for ( int i = 0; i < src.length; i++ )
        {
            result[ i ] = src[ i ].copy();
        }
        return result;
    }

    /**
     * This operation is used to marshal a DynAny
     */
    protected void dyn_any_to_stream( org.omg.CORBA.TypeCode tc,
          org.omg.CORBA.portable.OutputStream stream )
    {
        switch ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) tc )._base_type().kind().value() )
        {

        case TCKind._tk_null :
        case TCKind._tk_void :
            break;

        case TCKind._tk_short :

            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_short( m_any.extract_short() );
            }
            else
            {
                stream.write_short( ( short ) 0 );
            }
            break;

        case TCKind._tk_long :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_long( m_any.extract_long() );
            }
            else
            {
                stream.write_long( 0 );
            }
            break;

        case TCKind._tk_longlong :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_longlong( m_any.extract_longlong() );
            }
            else
            {
                stream.write_longlong( 0L );
            }
            break;

        case TCKind._tk_ushort :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_ushort( m_any.extract_ushort() );
            }
            else
            {
                stream.write_ushort( ( short ) 0 );
            }
            break;

        case TCKind._tk_ulong :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_ulong( m_any.extract_ulong() );
            }
            else
            {
                stream.write_ulong( 0 );
            }
            break;

        case TCKind._tk_ulonglong :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_ulonglong( m_any.extract_ulonglong() );
            }
            else
            {
                stream.write_ulonglong( 0L );
            }
            break;

        case TCKind._tk_float :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_float( m_any.extract_float() );
            }
            else
            {
                stream.write_float( ( float ) 0 );
            }
            break;

        case TCKind._tk_double :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_double( m_any.extract_double() );
            }
            else
            {
                stream.write_double( ( double ) 0 );
            }
            break;

        case TCKind._tk_boolean :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_boolean( m_any.extract_boolean() );
            }
            else
            {
                stream.write_boolean( false );
            }
            break;

        case TCKind._tk_char :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_char( m_any.extract_char() );
            }
            else
            {
                stream.write_char( ( char ) 0 );
            }
            break;

        case TCKind._tk_wchar :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_wchar( m_any.extract_wchar() );
            }
            else
            {
                stream.write_wchar( ( char ) 0 );
            }
            break;

        case TCKind._tk_octet :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_octet( m_any.extract_octet() );
            }
            else
            {
                stream.write_octet( ( byte ) 0 );
            }
            break;

        case TCKind._tk_any :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_any( m_any.extract_any() );
            }
            else
            {
                stream.write_any( m_orb.create_any() );
            }
            break;

        case TCKind._tk_TypeCode :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_TypeCode( m_any.extract_TypeCode() );
            }
            else
            {
                stream.write_TypeCode( m_orb.get_primitive_tc( TCKind.tk_null ) );
            }
            break;

        case TCKind._tk_objref :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_Object( m_any.extract_Object() );
            }
            else
            {
                stream.write_Object( null );
            }
            break;

        case TCKind._tk_string :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_string( m_any.extract_string() );
            }
            else
            {
                stream.write_string( "" );
            }
            break;

        case TCKind._tk_wstring :
            if ( m_any.type().kind() != TCKind.tk_void )
            {
                stream.write_wstring( m_any.extract_wstring() );
            }
            else
            {
                stream.write_wstring( "" );
            }
            break;

        case TCKind._tk_value :
        case TCKind._tk_value_box :
        case TCKind._tk_enum :
        case TCKind._tk_array :
        case TCKind._tk_sequence :
        case TCKind._tk_union :
        case TCKind._tk_struct :
        case TCKind._tk_except :
        case TCKind._tk_fixed :
            org.omg.CORBA.Any a = to_any();

            a.write_value( stream );

            break;
        }

    }

    /**
     * This operation is used to marshal a DynAny graph
     */
    protected void dyn_any_graph_to_stream( org.omg.DynamicAny.DynAny[] src,
                                            org.omg.CORBA.portable.OutputStream stream )
    {
        try
        {
            switch ( ( ( org.openorb.orb.core.typecode.TypeCodeBase )
                  m_type )._base_type().kind().value() )
            {
            case TCKind._tk_null :
            case TCKind._tk_void :
            case TCKind._tk_short :
            case TCKind._tk_long :
            case TCKind._tk_longlong :
            case TCKind._tk_ushort :
            case TCKind._tk_ulong :
            case TCKind._tk_ulonglong :
            case TCKind._tk_float :
            case TCKind._tk_double :
            case TCKind._tk_longdouble :
            case TCKind._tk_boolean :
            case TCKind._tk_char :
            case TCKind._tk_wchar :
            case TCKind._tk_octet :
            case TCKind._tk_any :
            case TCKind._tk_TypeCode :
            case TCKind._tk_objref :
            case TCKind._tk_string :
            case TCKind._tk_wstring :
                ( ( DynBasicImpl ) ( src[ 0 ] ) ).dyn_any_to_stream( m_type, stream );
                break;

            case TCKind._tk_union :
                ( ( DynAnyImpl ) ( src[ 0 ] ) ).dyn_any_to_stream( src[ 0 ].type(), stream );
                ( ( DynAnyImpl ) ( src[ 1 ] ) ).dyn_any_to_stream( src[ 1 ].type(), stream );
                break;

            case TCKind._tk_except :

                try
                {
                    stream.write_string( ( (
                          org.openorb.orb.core.typecode.TypeCodeBase ) m_type )._base_type().id() );
                }
                catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
                {
                    // TODO: ???
                }

                for ( int i = 0; i < src.length; i++ )
                {
                    ( ( DynAnyImpl ) ( src[ i ] ) ).dyn_any_to_stream( src[ i ].type(), stream );
                }

                break;

            case TCKind._tk_struct :

                for ( int i = 0; i < src.length; i++ )
                {
                    ( ( DynAnyImpl ) ( src[ i ] ) ).dyn_any_to_stream( src[ i ].type(), stream );
                }

                break;

            case TCKind._tk_enum :
                ( ( DynEnumImpl ) ( src[ 0 ] ) ).dyn_any_to_stream( m_type, stream );
                break;

            case TCKind._tk_fixed :
                ( ( DynFixedImpl ) ( src[ 0 ] ) ).dyn_any_to_stream( m_type, stream );
                break;

            case TCKind._tk_array :

                for ( int i = 0; i < src.length; i++ )
                {
                    ( ( DynAnyImpl ) ( src[ i ] ) ).dyn_any_to_stream( src[ i ].type(), stream );
                }
                break;

            case TCKind._tk_sequence :
                stream.write_ulong( src.length );

                for ( int i = 0; i < src.length; i++ )
                {
                    ( ( DynAnyImpl ) ( src[ i ] ) ).dyn_any_to_stream( src[ i ].type(), stream );
                }
                break;

            case TCKind._tk_value :

                for ( int i = 0; i < src.length; i++ )
                {
                    ( ( DynAnyImpl ) ( src[ i ] ) ).dyn_any_to_stream( src[ i ].type(), stream );
                }

                break;

            case TCKind._tk_value_box :
                ( ( DynAnyImpl ) ( src[ 0 ] ) ).dyn_any_to_stream( src[ 0 ].type(), stream );
                break;
            }
        }
        catch ( Exception ex )
        {
            // TODO: ???
        }
    }

    /**
     * This operation is used to unmarshal a DynAny
     */
    protected void stream_to_dyn_any( org.omg.CORBA.TypeCode tc,
                                      org.omg.CORBA.portable.InputStream stream )
    {
        switch ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) tc )._base_type().kind().value() )
        {

        case TCKind._tk_null :
        case TCKind._tk_void :
            break;

        case TCKind._tk_short :
            m_any.insert_short( stream.read_short() );
            break;

        case TCKind._tk_long :
            m_any.insert_long( stream.read_long() );
            break;

        case TCKind._tk_longlong :
            m_any.insert_longlong( stream.read_longlong() );
            break;

        case TCKind._tk_ushort :
            m_any.insert_ushort( stream.read_ushort() );
            break;

        case TCKind._tk_ulong :
            m_any.insert_ulong( stream.read_ulong() );
            break;

        case TCKind._tk_ulonglong :
            m_any.insert_ulonglong( stream.read_ulonglong() );
            break;

        case TCKind._tk_float :
            m_any.insert_float( stream.read_float() );
            break;

        case TCKind._tk_double :
            m_any.insert_double( stream.read_double() );
            break;

        case TCKind._tk_boolean :
            m_any.insert_boolean( stream.read_boolean() );
            break;

        case TCKind._tk_char :
            m_any.insert_char( stream.read_char() );
            break;

        case TCKind._tk_wchar :
            m_any.insert_wchar( stream.read_wchar() );
            break;

        case TCKind._tk_octet :
            m_any.insert_octet( stream.read_octet() );
            break;

        case TCKind._tk_any :
            m_any.insert_any( stream.read_any() );
            break;

        case TCKind._tk_TypeCode :
            m_any.insert_TypeCode( stream.read_TypeCode() );
            break;

        case TCKind._tk_objref :
            m_any.insert_Object( stream.read_Object() );
            break;

        case TCKind._tk_string :
            m_any.insert_string( stream.read_string() );
            break;

        case TCKind._tk_wstring :
            m_any.insert_wstring( stream.read_wstring() );
            break;

        case TCKind._tk_value :
        case TCKind._tk_value_box :
        case TCKind._tk_union :
        case TCKind._tk_struct :
        case TCKind._tk_except :
        case TCKind._tk_enum :
        case TCKind._tk_array :
        case TCKind._tk_sequence :
        case TCKind._tk_fixed :

            try
            {
                m_any = ( org.openorb.orb.core.Any ) m_orb.create_any();

                m_any.read_value( stream, ( ( org.openorb.orb.core.typecode.TypeCodeBase )
                      tc )._base_type() );

                from_any( m_any );
            }
            catch ( org.omg.DynamicAny.DynAnyPackage.InvalidValue ex )
            {
                getLogger().warn( "read value from stream", ex );
            }
            catch ( org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex )
            {
                getLogger().warn( "read value from stream", ex );
            }

            break;
        }

        m_any.type( tc );
    }

    /**
     * This operation is used to unmarshal a DynAny graph
     */
    protected void stream_to_dyn_any_graph( org.omg.DynamicAny.DynAny[] src,
          org.omg.CORBA.portable.InputStream stream )
    {
        switch ( ( ( org.openorb.orb.core.typecode.TypeCodeBase )
              m_type )._base_type().kind().value() )
        {
        case TCKind._tk_null :
        case TCKind._tk_void :
        case TCKind._tk_short :
        case TCKind._tk_long :
        case TCKind._tk_longlong :
        case TCKind._tk_ushort :
        case TCKind._tk_ulong :
        case TCKind._tk_ulonglong :
        case TCKind._tk_float :
        case TCKind._tk_double :
        case TCKind._tk_longdouble :
        case TCKind._tk_boolean :
        case TCKind._tk_char :
        case TCKind._tk_wchar :
        case TCKind._tk_octet :
        case TCKind._tk_any :
        case TCKind._tk_TypeCode :
        case TCKind._tk_objref :
        case TCKind._tk_string :
        case TCKind._tk_wstring :
            ( ( DynBasicImpl ) src[ 0 ] ).stream_to_dyn_any( m_type, stream );
            break;

        case TCKind._tk_union :
            ( ( DynAnyImpl ) src[ 0 ] ).stream_to_dyn_any( src[ 0 ].type(), stream );

            org.omg.CORBA.Any an = src[ 0 ].to_any();
            org.omg.CORBA.TypeCode t = ( ( org.openorb.orb.core.typecode.TypeCodeBase )
                  m_type )._base_type();
            boolean found = false;

            try
            {
                for ( int ll = 0; ll < t.member_count(); ll++ )
                {
                    if ( an.equal( t.member_label( ll ) ) )
                    {
                        found = true;

                        if ( src[ 1 ] != null )
                        {
                            if ( src[ 1 ].type().equals( t.member_type( ll ) ) )
                            {
                                break;
                            }
                        }
                        src[ 1 ] = create_dyn_any( t.member_type( ll ) );
                    }
                }

                if ( !found )
                {
                    if ( t.default_index() == -1 )
                    {
                        throw new org.omg.CORBA.MARSHAL();
                    }
                    src[ 1 ] = create_dyn_any( t.member_type( t.default_index() ) );
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

            ( ( DynAnyImpl ) src[ 1 ] ).stream_to_dyn_any( src[ 1 ].type(), stream );
            break;

        case TCKind._tk_except :
            stream.read_string();

            for ( int i = 0; i < src.length; i++ )
            {
                ( ( DynAnyImpl ) src[ i ] ).stream_to_dyn_any( src[ i ].type(), stream );
            }

            break;

        case TCKind._tk_struct :

            for ( int i = 0; i < src.length; i++ )
            {
                ( ( DynAnyImpl ) src[ i ] ).stream_to_dyn_any( src[ i ].type(), stream );
            }

            break;

        case TCKind._tk_fixed :
            ( ( DynAnyImpl ) src[ 0 ] ).stream_to_dyn_any( m_type, stream );
            break;

        case TCKind._tk_enum :
            ( ( DynAnyImpl ) src[ 0 ] ).stream_to_dyn_any( m_type, stream );
            break;

        case TCKind._tk_array :

            try
            {
                read_array_from_stream( src, stream, src.length );
            }
            catch ( org.omg.DynamicAny.DynAnyPackage.InvalidValue ex )
            {
                getLogger().warn( "read_array_from_stream", ex );
            }
            catch ( org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex )
            {
                getLogger().warn( "read_array_from_stream", ex );
            }

            break;

        case TCKind._tk_sequence :
            int max = stream.read_ulong();

            try
            {
                read_array_from_stream( src, stream, max );
            }
            catch ( org.omg.DynamicAny.DynAnyPackage.InvalidValue ex )
            {
                getLogger().warn( "read_array_from_stream", ex );
            }
            catch ( org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex )
            {
                getLogger().warn( "read_array_from_stream", ex );
            }

            break;

        case TCKind._tk_value :

            for ( int i = 0; i < src.length; i++ )
            {
                ( ( DynAnyImpl ) src[ i ] ).stream_to_dyn_any( src[ i ].type(), stream );
            }

            break;

        case TCKind._tk_value_box :
            ( ( DynAnyImpl ) src[ 0 ] ).stream_to_dyn_any( src[ 0 ].type(), stream );
            break;
        }

    }

    /**
     * This operation returns all the value member typecodes
     */
    public org.omg.CORBA.TypeCode [] getValueMember( org.omg.CORBA.TypeCode tc )
    {
        org.omg.CORBA.TypeCode [] ret = null;
        org.omg.CORBA.TypeCode [] tmp = null;

        try
        {
            if ( tc.kind() == TCKind.tk_value )
            {
                if ( tc.concrete_base_type().kind() != TCKind.tk_null )
                {
                    tmp = getValueMember( tc.concrete_base_type() );
                    ret = new org.omg.CORBA.TypeCode[ tmp.length + tc.member_count() ];

                    for ( int i = 0; i < tmp.length; i++ )
                    {
                        ret[ i ] = tmp[ i ];
                    }
                    for ( int i = 0; i < tc.member_count(); i++ )
                    {
                        ret[ i + tmp.length ] = tc.member_type( i );
                    }
                }
                else
                {
                    ret = new org.omg.CORBA.TypeCode[ tc.member_count() ];

                    for ( int i = 0; i < tc.member_count(); i++ )
                    {
                        ret[ i ] = tc.member_type( i );
                    }
                }

            }
            else
            {
                ret = new org.omg.CORBA.TypeCode[ 1 ];
                ret[ 0 ] = tc.content_type();
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

        return ret;
    }

    protected Logger getLogger()
    {
        if ( null == m_logger )
        {
            m_logger = ( ( org.openorb.orb.core.ORBSingleton ) m_orb ).getLogger();
        }
        return m_logger;
    }


    /**
     * Handles reading the array/sequence from the input stream
     * and inserts into the DynAny
     */
    private void read_array_from_stream( org.omg.DynamicAny.DynAny[] src,
                                         org.omg.CORBA.portable.InputStream stream,
                                         int len )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if ( len == 0 )
        {
            return;
        }
/***
        for ( int ii = 0; ii < len; ii++ )
        {
            ( ( DynAnyImpl ) src[ ii ] ).stream_to_dyn_any( src[ ii ].type(), stream );
        }
***/

        org.omg.CORBA.TypeCode tc = src[0].type();

        switch ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) tc )._base_type().kind().value() )
        {
        case TCKind._tk_boolean:
        {
            boolean[] array = new boolean[len];

            stream.read_boolean_array( array, 0, len );

            for ( int ii = 0; ii < len; ii++ )
            {
                src[ii].insert_boolean( array[ii] );
            }
        }
        break;

        case TCKind._tk_octet:
        {
            byte[] array = new byte[len];

            stream.read_octet_array( array, 0, len );

            for ( int ii = 0; ii < len; ii++ )
            {
                src[ii].insert_octet( array[ii] );
            }
        }
        break;

        case TCKind._tk_char:
        {
            char[] array = new char[len];

            stream.read_char_array( array, 0, len );

            for ( int ii = 0; ii < len; ii++ )
            {
                src[ii].insert_char( array[ii] );
            }
        }
        break;

        case TCKind._tk_wchar:
        {
            char[] array = new char[len];

            stream.read_wchar_array( array, 0, len );

            for ( int ii = 0; ii < len; ii++ )
            {
                src[ii].insert_wchar( array[ii] );
            }
        }
        break;

        case TCKind._tk_short:
        {
            short[] array = new short[len];

            stream.read_short_array( array, 0, len );

            for ( int ii = 0; ii < len; ii++ )
            {
                src[ii].insert_short( array[ii] );
            }
        }
        break;

        case TCKind._tk_ushort:
        {
            short[] array = new short[len];

            stream.read_ushort_array( array, 0, len );

            for ( int ii = 0; ii < len; ii++ )
            {
                src[ii].insert_ushort( array[ii] );
            }
        }
        break;

        case TCKind._tk_float:
        {
            float[] array = new float[len];

            stream.read_float_array( array, 0, len );

            for ( int ii = 0; ii < len; ii++ )
            {
                src[ii].insert_float( array[ii] );
            }
        }
        break;

        case TCKind._tk_double:
        {
            double[] array = new double[len];

            stream.read_double_array( array, 0, len );

            for ( int ii = 0; ii < len; ii++ )
            {
                src[ii].insert_double( array[ii] );
            }
        }
        break;

        case TCKind._tk_long:
        {
            int[] array = new int[len];

            stream.read_long_array( array, 0, len );

            for ( int ii = 0; ii < len; ii++ )
            {
                src[ii].insert_long( array[ii] );
            }
        }
        break;

        case TCKind._tk_ulong:
        {
            int[] array = new int[len];

            stream.read_ulong_array( array, 0, len );

            for ( int ii = 0; ii < len; ii++ )
            {
                src[ii].insert_ulong( array[ii] );
            }
        }
        break;

        case TCKind._tk_longlong:
        {
            long[] array = new long[len];

            stream.read_longlong_array( array, 0, len );

            for ( int ii = 0; ii < len; ii++ )
            {
                src[ii].insert_longlong( array[ii] );
            }
        }
        break;

        case TCKind._tk_ulonglong:
        {
            long[] array = new long[len];

            stream.read_ulonglong_array( array, 0, len );

            for ( int ii = 0; ii < len; ii++ )
            {
                src[ii].insert_ulonglong( array[ii] );
            }
        }
        break;

        default:
            for ( int ii = 0; ii < len; ii++ )
            {
                ( ( DynAnyImpl ) src[ ii ] ).stream_to_dyn_any( src[ ii ].type(), stream );
            }
        }
    }
}

