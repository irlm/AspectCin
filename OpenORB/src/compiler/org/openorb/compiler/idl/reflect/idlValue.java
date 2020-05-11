/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.idl.reflect;

/**
 * This interface is implemented by the IDL object that represents an IDL valuetype.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public interface idlValue extends idlObject
{
    /**
     * Return TRUE is this value is abstract
     */
    public boolean isAbstract();

    /**
     * Return TRUE is this value is custom
     */
    public boolean isCustom();

    /**
     * Return TRUE is this value is forward
     */
    public boolean isForward();

    /**
     * Return the value description ( for forward value )
     */
    public idlValue description();

    /**
     * Return TRUE is this value contains a truncatable clause into its inheritance list
     */
    public boolean isTruncatable();

    /**
     * Return the concrete inherited value
     */
    public idlValue concrete();

    /**
     * Return the inheritance list
     */
    public idlValue [] inheritance();

    /**
     * Return the supported interfaces
     */
    public idlInterface [] supported();
}
