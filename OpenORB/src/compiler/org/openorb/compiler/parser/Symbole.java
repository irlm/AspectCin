/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.parser;

/**
 * This class represents the reserved word SymboleDefs of the IDL grammar.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:41 $
 */

public class Symbole
{
    // -----------------------
    // Reserved words list
    // -----------------------

    /**
     * Reserved words list
     */
    public static java.util.Vector liste_mots_reserves = null;

    static
    {
        liste_mots_reserves = new java.util.Vector();

        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_module, "module" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_any, "any" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_attribute, "attribute" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_boolean, "boolean" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_case, "case" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_char, "char" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_const, "const" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_context, "context" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_default, "default" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_double, "double" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_enum, "enum" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_exception, "exception" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_false, "FALSE" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_float, "float" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_in, "in" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_inout, "inout" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_interface, "interface" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_long, "long" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_object, "Object" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_octet, "octet" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_oneway, "oneway" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_out, "out" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_raises, "raises" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_readonly, "readonly" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_sequence, "sequence" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_short, "short" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_string, "string" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_struct, "struct" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_switch, "switch" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_true, "TRUE" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_typedef, "typedef" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_unsigned, "unsigned" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_union, "union" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_void, "void" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_pragma, "pragma" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_include, "include" ) );
        //-liste_mots_reserves.addElement( ( Object )new  SymboleDef(Token.t_typecode, "TypeCode"));
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_wchar, "wchar" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_wstring, "wstring" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_native, "native" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_define, "define" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_ifdef, "ifdef" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_ifndef, "ifndef" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_endif, "endif" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_undef, "undef" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_else, "else" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_fixed, "fixed" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_abstract, "abstract" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_ValueBase, "ValueBase" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_valuetype, "valuetype" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_supports, "supports" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_custom, "custom" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_truncatable, "truncatable" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_private, "private" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_public, "public" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_factory, "factory" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_import, "import" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_local, "local" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_typeId, "typeId" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( Token.t_typePrefix, "typePrefix" ) );
    }

}
