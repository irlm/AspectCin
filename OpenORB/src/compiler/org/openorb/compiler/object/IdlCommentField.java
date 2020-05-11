/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.object;

/**
 * This class corresponds to an IDL comment section.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public class IdlCommentField implements java.io.Serializable
{
    /**
     * Section kind
     */
    private int _kind;

    /**
     * Author section
     */
    final public static int _author_field = 0;
    final public static IdlCommentField author_field = new IdlCommentField( _author_field );

    /**
     * Exception section
     */
    final public static int _exception_field = 1;
    final public static IdlCommentField exception_field = new IdlCommentField( _exception_field );

    /**
     * Version section
     */
    final public static int _version_field = 2;
    final public static IdlCommentField version_field = new IdlCommentField( _version_field );

    /**
     * Param section
     */
    final public static int _param_field = 3;
    final public static IdlCommentField param_field = new IdlCommentField( _param_field );

    /**
     * Return section
     */
    final public static int _return_field = 4;
    final public static IdlCommentField return_field = new IdlCommentField( _return_field );

    /**
     * See section
     */
    final public static int _see_field = 5;
    final public static IdlCommentField see_field = new IdlCommentField( _see_field );

    /**
     * Deprecated section
     */
    final public static int _deprecated_field = 6;
    final public static IdlCommentField deprecated_field = new IdlCommentField( _deprecated_field );

    /**
     * Unknown section
     */
    final public static int _unknown_field = 7;
    final public static IdlCommentField unknown_field = new IdlCommentField( _unknown_field );

    /**
     * constructor
     */
    public IdlCommentField( int kind )
    {
        _kind = kind;
    }

    /**
     * Return the field value
     */
    public int value()
    {
        return _kind;
    }
}
