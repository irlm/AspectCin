/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.parser;

/**
 * This class represents the reserved words SymboleDefs of the IDL+Java grammar.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:41 $
 */

public class SymboleJava
{
    // -------------------------------
    // Reserved words for Java
    // -------------------------------

    /**
     * Reserved word list.
     */
    public static java.util.Vector liste_mots_reserves = null;

    static
    {
        liste_mots_reserves = new java.util.Vector();

        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "abstract" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "boolean" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "break" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "byte" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "case" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "catch" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "char" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "class" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "const" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "continue" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "default" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "do" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "double" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "else" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "extends" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "final" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "finally" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "float" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "for" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "goto" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "if" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "implements" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "import" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "instanceof" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "int" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "interface" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "long" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "native" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "new" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "package" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "private" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "protected" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "public" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "return" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "short" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "static" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "super" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "switch" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "synchronized" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "this" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "throw" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "throws" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "transient" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "try" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "void" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "volatile" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "while" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "true" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "false" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "null" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "clone" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "notify" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "equals" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "notifyAll" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "finalize" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "toString" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "getClass" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "wait" ) );
        liste_mots_reserves.addElement( ( Object ) new SymboleDef( 0, "hashCode" ) );
    }
}
