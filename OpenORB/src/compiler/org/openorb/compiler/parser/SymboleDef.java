/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.parser;

/**
 * IDL grammar symbols.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:41 $
 */

public class SymboleDef
{
    // ---------
    // Attributes
    // ---------

    /**
     * Describes the current symbol token.
     */
    public int symbole_token;

    /**
     * Indicates the symbol name (reserved word, for example
     * interface, module, struct).
     */
    public String symbole_name;

    // ------------
    // Constructor
    // ------------

    /**
     * Set the token and reserved word.
     *
     * @param symbole_token  symbol token
     * @param symbole_name   reserved word
     */
    public SymboleDef( int symbole_token, String symbole_name )
    {
        this.symbole_token = symbole_token;
        this.symbole_name = symbole_name;
    }

}
