/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.idl.reflect;

/**
 * This interface is implemented by the IDL object that represents an IDL interface.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public interface idlInterface extends idlObject
{
    /**
     * Return TRUE if this interface is abstract
     */
    public boolean isAbstract();

    /**
     * Return TRUE if this interface is local
     */
    public boolean isLocal();

    /**
     * Return TRUE is this interface is forward
     */
    public boolean isForward();

    /**
     * Return the interface description
     */
    public idlObject description();

    /**
     * Return the inheritance list
     */
    public idlInterface [] inheritance();
}
