/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.parser;

import java.io.PushbackReader;
import java.net.URL;

/**
 * Compilation context, holds current lexical element and file information.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:41 $
 */
public class CompilationContext
{
    /**
     * Source input.
     */
    public PushbackReader is;

    /**
     * Current symbol
     */
    public int symb;

    /**
     * Delayed symbol
     */
    public int one;

    /**
     * Current line number
     */
    public int line;

    /**
     * Number of errors.
     */
    public int nberrors;

    /**
     * Number of warnings
     */
    public int nbwarning;

    /**
     * File name
     */
    public String name;

    /**
     * Source URL.
     */
    public URL sourceURL;

    /**
     * Current character
     */
    public char car;

    /**
     * Current text type value
     */
    public String value;

    /**
     * Current numeric type value
     * 0 = integer, 1 = float
     */
    public int type;

    /**
     * Numeric value base
     * 0 = decimal, 1 = hexa
     */
    public int base;

    /**
     * Current prefix
     */
    public String prefix;

    // ------------
    // Constructor
    // ------------
    /**
     * Creates a compilation context
     */
    public CompilationContext()
    {
        one = Token.t_none;
    }

}
