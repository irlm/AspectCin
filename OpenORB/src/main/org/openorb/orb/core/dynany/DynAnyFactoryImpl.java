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

/**
 * This class provides the DynAny Factory
 *
 * @author Jerome Daniel
 * @version $Revision: 1.7 $ $Date: 2004/02/10 21:02:47 $
 */
public class DynAnyFactoryImpl
    extends org.omg.CORBA.LocalObject
    implements org.omg.DynamicAny.DynAnyFactory
{
    private org.omg.CORBA.ORB m_orb;
    private Logger m_logger;

    /**
     * Constructor.
     *
     * @param orb The orb to use.
     */
    public DynAnyFactoryImpl( org.omg.CORBA.ORB orb )
    {
        m_orb = orb;
    }

    /**
     * Operation create_dyn_any
     */
    public org.omg.DynamicAny.DynAny create_dyn_any( org.omg.CORBA.Any value )
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        org.omg.DynamicAny.DynAny dany = null;
        dany = create_dyn_any_from_type_code( value.type() );
        try
        {
            dany.from_any( value );
        }
        catch ( org.omg.DynamicAny.DynAnyPackage.TypeMismatch ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Any types do not match.", ex );
            }
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        }
        catch ( org.omg.DynamicAny.DynAnyPackage.InvalidValue ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Any value is not valid.", ex );
            }
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        }
        return dany;
    }

    /**
     * Operation create_dyn_any_from_type_code
     */
    public org.omg.DynamicAny.DynAny create_dyn_any_from_type_code( org.omg.CORBA.TypeCode type )
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        org.omg.DynamicAny.DynAny dany;

        // some holders used in later switch cases
        org.omg.CORBA.Any a = null;
        org.omg.CORBA.TypeCode tn = null;

        // added insert statements
        // for corba 2.4 conformant initialization of all primitive kinds
        switch ( ( ( org.openorb.orb.core.typecode.TypeCodeBase )
                 type )._base_type().kind().value() )
        {

        case TCKind._tk_null :
        case TCKind._tk_void :
            a = m_orb.create_any();
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_short :
            a = m_orb.create_any();
            a.insert_short( ( short ) 0 );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_long :
            a = m_orb.create_any();
            a.insert_long( 0 );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_longlong :
            a = m_orb.create_any();
            a.insert_longlong( 0L );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_ushort :
            a = m_orb.create_any();
            a.insert_ushort( ( short ) 0 );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_ulong :
            a = m_orb.create_any();
            a.insert_ulong( 0 );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_ulonglong :
            a = m_orb.create_any();
            a.insert_ulonglong( 0L );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_float :
            a = m_orb.create_any();
            a.insert_float( 0 );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_double :
            a = m_orb.create_any();
            a.insert_double( 0 );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_longdouble :
            throw new org.omg.CORBA.NO_IMPLEMENT();

        case TCKind._tk_boolean :
            a = m_orb.create_any();
            a.insert_boolean( false );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_char :
            a = m_orb.create_any();
            a.insert_char( ( char ) 0 );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_wchar :
            a = m_orb.create_any();
            a.insert_wchar( ( char ) 0 );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_octet :
            a = m_orb.create_any();
            a.insert_octet( ( byte ) 0 );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_any :
            tn = m_orb.get_primitive_tc( TCKind.tk_null );
            a = m_orb.create_any();
            org.omg.CORBA.Any b = m_orb.create_any();
            b.type ( tn );
            a.insert_any( b );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_TypeCode :
            tn = m_orb.get_primitive_tc( TCKind.tk_null );
            a = m_orb.create_any();
            a.insert_TypeCode( tn );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_objref :
            a = m_orb.create_any();
            a.insert_Object( null );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_string :
            a = m_orb.create_any();
            a.insert_string( "" );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case TCKind._tk_wstring :
            a = m_orb.create_any();
            a.insert_wstring( "" );
            dany = new DynBasicImpl( this, m_orb, type, a );
            break;

        case org.omg.CORBA.TCKind._tk_except :
        case org.omg.CORBA.TCKind._tk_struct :
            dany = create_dyn_struct( type );
            break;

        case org.omg.CORBA.TCKind._tk_sequence :
            dany = create_dyn_sequence( type );
            break;

        case org.omg.CORBA.TCKind._tk_union :
            dany = create_dyn_union( type );
            break;

        case org.omg.CORBA.TCKind._tk_enum :
            dany = create_dyn_enum( type );
            break;

        case org.omg.CORBA.TCKind._tk_fixed :
            dany = create_dyn_fixed( type );
            break;

        case org.omg.CORBA.TCKind._tk_array :
            dany = create_dyn_array( type );
            break;

        case org.omg.CORBA.TCKind._tk_value :
            dany = create_dyn_value( type );
            break;

        case org.omg.CORBA.TCKind._tk_value_box :
            dany = create_dyn_value_box( type );
            break;

        default :
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        }

        return dany;
    }

    private org.omg.DynamicAny.DynValue create_dyn_value( org.omg.CORBA.TypeCode type )
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        if ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) type )._base_type().kind().value()
              != org.omg.CORBA.TCKind._tk_value )
        {
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        }
        return new org.openorb.orb.core.dynany.DynValueImpl( this, m_orb, type );
    }

    private org.omg.DynamicAny.DynValueBox create_dyn_value_box( org.omg.CORBA.TypeCode type )
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        if ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) type )._base_type().kind().value()
              != org.omg.CORBA.TCKind._tk_value_box )
        {
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        }
        return new org.openorb.orb.core.dynany.DynValueBoxImpl( this, m_orb, type );
    }

    private org.omg.DynamicAny.DynStruct create_dyn_struct( org.omg.CORBA.TypeCode type )
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        if ( ( ( ( org.openorb.orb.core.typecode.TypeCodeBase )
              type )._base_type().kind().value()
              != org.omg.CORBA.TCKind._tk_struct )
              && ( ( ( org.openorb.orb.core.typecode.TypeCodeBase )
              type )._base_type().kind().value()
              != org.omg.CORBA.TCKind._tk_except ) )
        {
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        }
        return new org.openorb.orb.core.dynany.DynStructImpl( this, m_orb, type );
    }

    private org.omg.DynamicAny.DynFixed create_dyn_fixed( org.omg.CORBA.TypeCode type )
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        if ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) type )._base_type().kind().value()
              != org.omg.CORBA.TCKind._tk_fixed )
        {
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        }
        return new org.openorb.orb.core.dynany.DynFixedImpl( this, m_orb, type );
    }

    private org.omg.DynamicAny.DynSequence create_dyn_sequence( org.omg.CORBA.TypeCode type )
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        if ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) type )._base_type().kind().value()
              != org.omg.CORBA.TCKind._tk_sequence )
        {
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        }
        return new org.openorb.orb.core.dynany.DynSequenceImpl( this, m_orb, type );
    }

    public org.omg.DynamicAny.DynArray create_dyn_array( org.omg.CORBA.TypeCode type )
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        if ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) type )._base_type().kind().value()
              != org.omg.CORBA.TCKind._tk_array )
        {
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        }
        return new org.openorb.orb.core.dynany.DynArrayImpl( this, m_orb, type );
    }

    private org.omg.DynamicAny.DynUnion create_dyn_union( org.omg.CORBA.TypeCode type )
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        if ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) type )._base_type().kind().value()
              != org.omg.CORBA.TCKind._tk_union )
        {
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        }
        return new org.openorb.orb.core.dynany.DynUnionImpl( this, m_orb, type );
    }

    private org.omg.DynamicAny.DynEnum create_dyn_enum( org.omg.CORBA.TypeCode type )
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        if ( ( ( org.openorb.orb.core.typecode.TypeCodeBase ) type )._base_type().kind().value()
              != org.omg.CORBA.TCKind._tk_enum )
        {
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        }
        return new org.openorb.orb.core.dynany.DynEnumImpl( this, m_orb, type, 0 );
    }

    private Logger getLogger()
    {
        if ( null == m_logger )
        {
            m_logger = ( ( org.openorb.orb.core.ORBSingleton ) m_orb ).getLogger();
        }
        return m_logger;
    }
}

