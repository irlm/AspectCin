/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.parser;

/**
 * This class represents the constants describing the IDL types
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:41 $
 */

public class IdlType
{
    public static final int e_root = 0;
    public static final int e_module = 1;
    public static final int e_enum = 2;
    public static final int e_struct = 3;
    public static final int e_union = 4;
    public static final int e_string = 5;
    public static final int e_wstring = 6;
    public static final int e_const = 7;
    public static final int e_simple = 8;
    public static final int e_sequence = 9;
    public static final int e_ident = 10;
    public static final int e_struct_member = 11;
    public static final int e_union_member = 12;
    public static final int e_typedef = 13;
    public static final int e_exception = 14;
    public static final int e_interface = 15;
    public static final int e_operation = 16;
    public static final int e_attribute = 17;
    public static final int e_forward_interface = 18;
    public static final int e_param = 19;
    public static final int e_raises = 20;
    public static final int e_context = 21;
    public static final int e_enum_member = 22;
    public static final int e_any = 23;
    public static final int e_array = 24;
    public static final int e_native = 25;
    public static final int e_fixed = 26;
    public static final int e_value_box = 27;
    public static final int e_value = 28;
    public static final int e_state_member = 29;
    public static final int e_factory = 30;
    public static final int e_factory_member = 31;
    public static final int e_value_inheritance = 32;
    public static final int e_forward_value = 33;
    public static final int e_include = 34;
    public static final int e_import = 35;


    public static String toString( int type )
    {
        String result = null;
        switch ( type )
        {
            case e_root:
                result = new String( "IdlType::e_root" );
                break;
            case e_module:
                result = new String( "IdlType::e_module" );
                break;
            case e_enum:
                result = new String( "IdlType::e_enum" );
                break;
            case e_struct:
                result = new String( "IdlType::e_struct" );
                break;
            case e_union:
                result = new String( "IdlType::e_union" );
                break;
            case e_string:
                result = new String( "IdlType::e_string" );
                break;
            case e_wstring:
                result = new String( "IdlType::e_wstring" );
                break;
            case e_const:
                result = new String( "IdlType::e_const" );
                break;
            case e_simple:
                result = new String( "IdlType::e_simple" );
                break;
            case e_sequence:
                result = new String( "IdlType::e_sequence" );
                break;
            case e_ident:
                result = new String( "IdlType::e_ident" );
                break;
            case e_struct_member:
                result = new String( "IdlType::e_struct_member" );
                break;
            case e_union_member:
                result = new String( "IdlType::e_union_member" );
                break;
            case e_typedef:
                result = new String( "IdlType::e_typedef" );
                break;
            case e_exception:
                result = new String( "IdlType::e_exception" );
                break;
            case e_interface:
                result = new String( "IdlType::e_interface" );
                break;
            case e_operation:
                result = new String( "IdlType::e_operation" );
                break;
            case e_attribute:
                result = new String( "IdlType::e_attribute" );
                break;
            case e_forward_interface:
                result = new String( "IdlType::e_forward_interface" );
                break;
            case e_param:
                result = new String( "IdlType::e_param" );
                break;
            case e_raises:
                result = new String( "IdlType::e_raises" );
                break;
            case e_context:
                result = new String( "IdlType::e_context" );
                break;
            case e_enum_member:
                result = new String( "IdlType::e_enum_member" );
                break;
            case e_any:
                result = new String( "IdlType::e_any" );
                break;
            case e_array:
                result = new String( "IdlType::e_array" );
                break;
            case e_native:
                result = new String( "IdlType::e_native" );
                break;
            case e_fixed:
                result = new String( "IdlType::e_fixed" );
                break;
            case e_value_box:
                result = new String( "IdlType::e_value_box" );
                break;
            case e_value:
                result = new String( "IdlType::e_value" );
                break;
            case e_state_member:
                result = new String( "IdlType::e_state_member" );
                break;
            case e_factory:
                result = new String( "IdlType::e_factory" );
                break;
            case e_factory_member:
                result = new String( "IdlType::e_factory_member" );
                break;
            case e_value_inheritance:
                result = new String( "IdlType::e_value_inheritance" );
                break;
            case e_forward_value:
                result = new String( "IdlType::e_forward_value" );
                break;
            case e_include:
                result = new String( "IdlType::e_include" );
                break;
            case e_import:
                result = new String( "IdlType::e_import" );
                break;
            default:
                result = new String( "Unknown type" );
        }
        return result;
    }
}
