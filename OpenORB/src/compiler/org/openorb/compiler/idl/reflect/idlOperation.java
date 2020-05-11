/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.idl.reflect;

/**
 * This interface is implemented by the IDL object that represents an IDL operation.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public interface idlOperation extends idlObject
{
    /**
     * Return TRUE if this operation is marked as 'oneway'
     */
    public boolean isOneway();

    /**
     * Return the return type
     */
    public idlObject returnType();

    /**
     * Return the parameters
     */
    public idlParameter [] parameters();

    /**
     * Return the exceptions
     */
    public idlException [] exceptions();

    /**
     * Return the contexts
     */
    public String [] contexts();
}
