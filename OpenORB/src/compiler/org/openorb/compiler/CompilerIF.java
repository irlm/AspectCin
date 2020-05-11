/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler;

import org.openorb.compiler.parser.CompilationException;

/**
 * Methods to connect a compiler (either on command line or in ant task).
 *
 * @author Erik Putrycz
 * @version $Revision: 1.7 $ $Date: 2005/03/13 12:55:43 $
 */
public interface CompilerIF
{
    /**
     * Initializes the compiler.
     *
     * @param ch
     * @param cp
     */
    void init_compiler( CompilerHost ch, CompilerProperties cp );

    /**
     * Run before each execution of the task (ant specific).
     *
     * @param ch
     * @param cp
     */
    void execute_compiler( CompilerHost ch, CompilerProperties cp );

    /**
     * Compile a file.
     *
     * @param cle
     * @param cp
     *
     * @throws CompilationException
     */
    void compile_file( CompileListEntry cle, CompilerProperties cp )
        throws CompilationException;

    /**
     * Factory method to create properties.
     *
     * @return CompilerProperties
     */
    CompilerProperties createEmptyProperties();

    /**
     * Scan command line args.
     *
     * @param args
     * @param cp
     */
    void scan_args( String[] args, CompilerProperties cp );

    /**
     * Display command line help.
     */
    void display_help();
}

