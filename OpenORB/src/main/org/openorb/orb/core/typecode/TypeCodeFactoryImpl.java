/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.typecode;

import java.util.HashMap;

import org.omg.CORBA.TypeCodeFactory;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.CompletionStatus;

import org.openorb.orb.core.MinorCodes;

import org.openorb.util.ExceptionTool;
import org.openorb.util.RepoIDHelper;

/**
 * This class implements the TypeCodeFactory, it is used to create type codes.
 * It uses the tie implementation method. To get an instance,
 * call ORB.resolve_initial_references("TypeCodeFactory")
 *
 * @author Chris Wood
 * @version $Revision: 1.7 $ $Date: 2004/02/17 22:13:55 $
 */
public final class TypeCodeFactoryImpl
    extends org.omg.CORBA.LocalObject
    implements TypeCodeFactory
{
    private static TypeCodeFactoryImpl s_instance = null;

    /**
     * Default constructor. Please use
     * ORB.resolve_initial_references("TypeCodeFactory")
     * to get a proper instance.
     */
    private TypeCodeFactoryImpl()
    {
    }

    /**
     * Used by pre 3.0 ORBSingleton classes to create an instance for
     * manufacturing typecodes
     */
    public static TypeCodeFactoryImpl getInstance()
    {
        if ( s_instance == null )
        {
            s_instance = new TypeCodeFactoryImpl();
        }
        return s_instance;
    }

    private org.omg.CORBA.TypeCode fix_recursive( TypeCodeBase tc )
    {
        tc._fix_recursive( new HashMap() );
        return tc;
    }

    /**
     * Create a struct typecode
     */
    public org.omg.CORBA.TypeCode create_struct_tc( String id, String name,
          org.omg.CORBA.StructMember[] members )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
              && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        return fix_recursive( new TypeCodeStruct( TCKind.tk_struct, id, name, members ) );
    }

    /**
     * Create an union typecode
     */
    public org.omg.CORBA.TypeCode create_union_tc( String id,
            String name,
            org.omg.CORBA.TypeCode discriminator_type,
            org.omg.CORBA.UnionMember[] members )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
              && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        // check discriminator type is compatable
        org.omg.CORBA.TypeCode base = discriminator_type;

        try
        {
            while ( base.kind() == TCKind.tk_alias )
            {
                base = base.content_type();
            }
        }
        catch ( final org.omg.CORBA.TypeCodePackage.BadKind ex )
        {
            throw ExceptionTool.initCause( new org.omg.CORBA.BAD_PARAM(
                    "Unexpected BadKind exception",
                    org.omg.CORBA.OMGVMCID.value | 20,
                    CompletionStatus.COMPLETED_MAYBE ), ex );
        }

        switch ( base.kind().value() )
        {

        case TCKind._tk_long:

        case TCKind._tk_longlong:

        case TCKind._tk_short:

        case TCKind._tk_ulong:

        case TCKind._tk_ulonglong:

        case TCKind._tk_ushort:

        case TCKind._tk_char:

        case TCKind._tk_wchar:

        case TCKind._tk_boolean:

        case TCKind._tk_enum:
            break;

        default:
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 20,
                  CompletionStatus.COMPLETED_MAYBE );
        }

        boolean hasDefault = false;
        // check label types
        for ( int i = 0; i < members.length; ++i )
        {
            if ( !members[ i ].label.type().equivalent( discriminator_type ) )
            {
                if ( !hasDefault && members[ i ].label.type().equal( TypeCodePrimitive.TC_OCTET )
                        && members[ i ].label.extract_octet() == 0 )
                {
                    hasDefault = true;
                }
                else
                {
                    throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 19,
                          CompletionStatus.COMPLETED_MAYBE );
                }
            }
        }
        // check label values
        for ( int i = 0; i < members.length; ++i )
        {
            for ( int j = i + 1; j < members.length; ++j )
            {
                if ( members[ i ].label.equal( members[ j ].label ) )
                {
                    throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 18,
                          CompletionStatus.COMPLETED_MAYBE );
                }
            }
        }
        return fix_recursive( new TypeCodeUnion( id, name, discriminator_type, members ) );
    }

    /**
     * Create an enum typecode
     */
    public org.omg.CORBA.TypeCode create_enum_tc( String id,
            String name,
            String[] members )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
              && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        return new TypeCodeEnum( id, name, members );
    }

    /**
     * Create an alias typecode
     */
    public org.omg.CORBA.TypeCode create_alias_tc( String id,
            String name,
            org.omg.CORBA.TypeCode original_type )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
              && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        return fix_recursive( new TypeCodeAlias( TCKind.tk_alias, id, name, original_type ) );
    }

    /**
     * Create an exception typecode
     */
    public org.omg.CORBA.TypeCode create_exception_tc( String id,
            String name,
            org.omg.CORBA.StructMember[] members )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
              && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        return fix_recursive( new TypeCodeStruct( TCKind.tk_except, id, name, members ) );
    }

    /**
     * Create an interface typecode
     */
    public org.omg.CORBA.TypeCode create_interface_tc( String id, String name )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
              && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        return new TypeCodeObject( TCKind.tk_objref, id, name );
    }

    /**
     * Create a native typecode
     */
    public org.omg.CORBA.TypeCode create_native_tc( String id, String name )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
              && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        return new TypeCodeObject( TCKind.tk_native, id, name );
    }


    /**
     * Create a string typecode
     */
    public org.omg.CORBA.TypeCode create_string_tc( int bound )
    {
        if ( bound == 0 )
        {
            return TypeCodeString.TC_STRING_0;
        }
        return new TypeCodeString( TCKind.tk_string, bound );
    }

    /**
     * Create a wstring typecode
     */
    public org.omg.CORBA.TypeCode create_wstring_tc( int bound )
    {
        if ( bound == 0 )
        {
            return TypeCodeString.TC_WSTRING_0;
        }
        return new TypeCodeString( TCKind.tk_wstring, bound );
    }

    /**
     * Create a sequence typecode
     */
    public org.omg.CORBA.TypeCode create_sequence_tc( int bound,
          org.omg.CORBA.TypeCode element_type )
    {
        return fix_recursive( new TypeCodeArray( TCKind.tk_sequence, bound, element_type ) );
    }

    /**
     * Create a recursive sequence typecode
      *
      * @deprecated
     */
    public org.omg.CORBA.TypeCode create_recursive_sequence_tc( int bound, int offset )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Create a recursive typecode
     */
    public org.omg.CORBA.TypeCode create_recursive_tc( String id )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        return new TypeCodeRecursive( id );
    }

    /**
     * Create an array typecode
     */
    public org.omg.CORBA.TypeCode create_array_tc( int length, org.omg.CORBA.TypeCode element_type )
    {
        return fix_recursive( new TypeCodeArray( TCKind.tk_array, length, element_type ) );
    }

    /**
     * Create a fixed typecode
     */
    public org.omg.CORBA.TypeCode create_fixed_tc( short digits, short scale )
    {
        return new TypeCodeFixed( digits, scale );
    }

    /**
     * Create a valuetype typecode
     */
    public org.omg.CORBA.TypeCode create_value_tc( String id, String name,
            short type_modifier,
            org.omg.CORBA.TypeCode concrete_base,
            org.omg.CORBA.ValueMember [] members )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
              && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( concrete_base == null )
        {
            concrete_base = TypeCodePrimitive.TC_NULL;
        }
        //else if(concrete_base.kind() != TCKind.tk_null
        //      && concrete_base.kind() != TCKind.tk_value)
        //  throw new org.omg.CORBA.BAD_PARAM(org.omg.CORBA.OMGVMCID.value | 20,
        //        CompletionStatus.COMPLETED_MAYBE);

        return fix_recursive(
              new TypeCodeValue( id, name, type_modifier, concrete_base, members ) );
    }

    /**
     * Create a value box typecode
     */
    public org.omg.CORBA.TypeCode create_value_box_tc( String id, String name,
          org.omg.CORBA.TypeCode boxed_type )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
              && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        return fix_recursive( new TypeCodeAlias( TCKind.tk_value_box, id, name, boxed_type ) );
    }

    /**
     * Create an abstract interface typecode
     */
    public org.omg.CORBA.TypeCode create_abstract_interface_tc( String id, String name )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
              && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        return new TypeCodeObject( TCKind.tk_abstract_interface, id, name );
    }

    /**
     * Create a local interface typecode. Currently this returns an ordinary
     * interface typecode.
     */
    public org.omg.CORBA.TypeCode create_local_interface_tc( String id, String name )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
              && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        // TODO: change this to tk_local_interface
        return new TypeCodeObject( TCKind.tk_objref, id, name );
    }

    /**
      * Create a component home typecode.
      *
      * @since CORBA 3.0
      */
    public org.omg.CORBA.TypeCode create_home_tc( String id, String name )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
             && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        throw new org.omg.CORBA.NO_IMPLEMENT();

        //return new TypeCodeObject(TCKind.tk_home, id, name);
    }

    /**
     * Create a component typecode.
     *
     * @since CORBA 3.0
     */
    public org.omg.CORBA.TypeCode create_component_tc( String id, String name )
    {
        if ( !RepoIDHelper.checkID( id ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 16,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        if ( id.startsWith( "IDL:" ) && name.length() != 0
              && !RepoIDHelper.checkIdentifier( name ) )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 15,
                  CompletionStatus.COMPLETED_MAYBE );
        }
        throw new org.omg.CORBA.NO_IMPLEMENT();

        //return new TypeCodeObject(TCKind.tk_component, id, name);
    }

    /**
     * This operation returns a primitive typecode from the corresponding TC kind.
      * Note this is not a member of the TypeCodeFactory interface, but is put here
      * for convienience.
     */
    public org.omg.CORBA.TypeCode get_primitive_tc( org.omg.CORBA.TCKind tcKind )
    {
        switch ( tcKind.value() )
        {

        case TCKind._tk_null:
            return TypeCodePrimitive.TC_NULL;

        case TCKind._tk_void:
            return TypeCodePrimitive.TC_VOID;

        case TCKind._tk_short:
            return TypeCodePrimitive.TC_SHORT;

        case TCKind._tk_long:
            return TypeCodePrimitive.TC_LONG;

        case TCKind._tk_ushort:
            return TypeCodePrimitive.TC_USHORT;

        case TCKind._tk_ulong:
            return TypeCodePrimitive.TC_ULONG;

        case TCKind._tk_float:
            return TypeCodePrimitive.TC_FLOAT;

        case TCKind._tk_double:
            return TypeCodePrimitive.TC_DOUBLE;

        case TCKind._tk_boolean:
            return TypeCodePrimitive.TC_BOOLEAN;

        case TCKind._tk_char:
            return TypeCodePrimitive.TC_CHAR;

        case TCKind._tk_octet:
            return TypeCodePrimitive.TC_OCTET;

        case TCKind._tk_any:
            return TypeCodePrimitive.TC_ANY;

        case TCKind._tk_TypeCode:
            return TypeCodePrimitive.TC_TYPECODE;

        case TCKind._tk_Principal:
            return TypeCodePrimitive.TC_PRINCIPAL;

        case TCKind._tk_longlong:
            return TypeCodePrimitive.TC_LONGLONG;

        case TCKind._tk_ulonglong:
            return TypeCodePrimitive.TC_ULONGLONG;

        case TCKind._tk_longdouble:
            return TypeCodePrimitive.TC_LONGDOUBLE;

        case TCKind._tk_wchar:
            return TypeCodePrimitive.TC_WCHAR;

        case TCKind._tk_string:
            return TypeCodeString.TC_STRING_0;

        case TCKind._tk_wstring:
            return TypeCodeString.TC_WSTRING_0;

        case TCKind._tk_objref:
            return TypeCodeObject.TC_OBJECT;

        case TCKind._tk_abstract_interface:
            return TypeCodeObject.TC_ABSTRACT_INTERFACE;

        case TCKind._tk_value:
            return TypeCodeValue.TC_VALUEBASE;

        default:
            // throw the exception below.
            break;
        }

        throw new org.omg.CORBA.BAD_PARAM( "Illegal kind for primative typecode",
             MinorCodes.BAD_PARAM_PRIMITIVE_KIND, CompletionStatus.COMPLETED_MAYBE );
    }
}

