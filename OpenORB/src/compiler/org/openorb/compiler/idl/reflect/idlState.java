/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.idl.reflect;

/**
 * This interface is implemented by all IDL objects that represent a state description
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public interface idlState extends idlObject
{
    /**
     * Return TRUE if this state member is public
     */
    public boolean isPublic();

    /**
     * Return the state member type
     */
    public idlObject stateType();
}
