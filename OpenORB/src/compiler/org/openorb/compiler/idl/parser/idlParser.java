/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.idl.parser;

import org.openorb.compiler.CompilerProperties;

import org.openorb.compiler.object.IdlObject;

/**
 * This class is the org.openorb Parser that builds internal IDL object graph and
 * returns an enumeration
 *
 * @author Jerome Daniel
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:37 $
 */
public class idlParser
{
    /**
     * Compilation Graph
     */
    IdlObject CompilationGraph = null;

    CompilerProperties m_cp = null;

    /**
     * Constructor
     *
     * @param args the compiler arguments ( the same options as on command line )
     */
    public idlParser( CompilerProperties cp )
    {
        m_cp = cp;
    }

    /**
     * Return the compilation graph content
     */
    public java.util.Enumeration content()
    {
        return CompilationGraph.content();
    }
}

