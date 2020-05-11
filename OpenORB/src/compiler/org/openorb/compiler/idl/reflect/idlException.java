/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.idl.reflect;

/**
 * This class is implemented by all IDL objects that represent a IDL exception.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public interface idlException extends idlObject
{
    /**
     * Return all members of this exception. The 'content' operation return internal descriptions.
     */
    public java.util.Enumeration members();
}