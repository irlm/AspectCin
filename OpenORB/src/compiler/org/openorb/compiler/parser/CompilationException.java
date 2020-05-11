/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.parser;

/**
 * This exception is raised when a fatal error is detected during the compilation time.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:41 $
 */

public class CompilationException extends java.lang.RuntimeException
{

    public CompilationException()
    {
        super();
    }

    public CompilationException( String s )
    {
        super( s );
    }

}
