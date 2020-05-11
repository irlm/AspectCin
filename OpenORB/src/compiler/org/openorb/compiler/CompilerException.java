/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler;

/**
 * This exception is raised when an error is detected while trying
 * to run any of the compilers.
 *
 * @author Michael Rumpf
 */
public class CompilerException
    extends java.lang.RuntimeException
{
    public CompilerException()
    {
        super();
    }

    public CompilerException( String s )
    {
        super( s );
    }
}

