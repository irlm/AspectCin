/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler;

/**
 * Methods to connect a compiler (either on command line or in ant task)
 * @author Erik Putrycz
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:36 $
 */
public interface CompilerHost
{
    void display( String s );
}
