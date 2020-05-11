/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.idl.reflect;

/**
 * This interface is implemented by all IDL objects that represent a parameter description
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public interface idlParameter extends idlObject
{
    public static final int PARAM_IN = 0;

    public static final int PARAM_OUT = 1;

    public static final int PARAM_INOUT = 2;

    /**
     * Return the parameter mode
     */
    public int paramMode();

    /**
     * Return the parameter type
     */
    public idlObject paramType();
}