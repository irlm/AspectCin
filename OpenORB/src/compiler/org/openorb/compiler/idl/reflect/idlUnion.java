/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.idl.reflect;

/**
 * This class is implemented by all IDL objects that represent a IDL union.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public interface idlUnion extends idlObject
{
    /**
     * Return TRUE is this union is forward
     */
    public boolean isForward();

    /**
     * Return the union description ( if forwarded )
     */
    public idlUnion description();

    /**
     * Return the discriminant type
     */
    public idlObject discriminant();
}
