/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.idl.reflect;

/**
 * This interface is implemented by all objects that represent an IDL constant
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public interface idlConst extends idlObject
{
    /**
     * Return the constant expression
     */
    public String expression();

    /**
     * Return the constant value
     */
    public Object value();

    /**
     * Return the constant type
     */
    public idlObject constantType();
}
