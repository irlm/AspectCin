/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.doc.html;

import org.openorb.compiler.object.IdlObject;

/**
 * This class is a container that includes all IDL parts
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:36 $
 */

public class content
{
    /**
     * Module
     */
    public java.util.Vector _module;
    public IdlObject [] _sorted_module;

    /**
     * Interface
     */
    public java.util.Vector _interface;
    public IdlObject [] _sorted_interface;

    /**
     * Value Type
     */
    public java.util.Vector _valuetype;
    public IdlObject [] _sorted_valuetype;

    /**
     * Value Box
     */
    public java.util.Vector _valuebox;
    public IdlObject [] _sorted_valuebox;

    /**
     * Exception
     */
    public java.util.Vector _exception;
    public IdlObject [] _sorted_exception;

    /**
     * Struct
     */
    public java.util.Vector _struct;
    public IdlObject [] _sorted_struct;

    /**
     * Union
     */
    public java.util.Vector _union;
    public IdlObject [] _sorted_union;

    /**
     * Enum
     */
    public java.util.Vector _enum;
    public IdlObject [] _sorted_enum;

    /**
     * TypeDef
     */
    public java.util.Vector _typedef;
    public IdlObject [] _sorted_typedef;

    /**
     * Const
     */
    public java.util.Vector _const;
    public IdlObject [] _sorted_const;

    /**
     * Native
     */
    public java.util.Vector _native;
    public IdlObject [] _sorted_native;

    /**
     * Operation
     */
    public java.util.Vector _operation;
    public IdlObject [] _sorted_operation;

    /**
     * Attribute
     */
    public java.util.Vector _attribute;
    public IdlObject [] _sorted_attribute;

    /**
     * Value member
     */
    public java.util.Vector _member;
    public IdlObject [] _sorted_member;

    /**
     * Factory
     */
    public java.util.Vector _factory;
    public IdlObject [] _sorted_factory;

    /**
     * Constructor
     */
    public content()
    {
        _module = new java.util.Vector();
        _interface = new java.util.Vector();
        _valuetype = new java.util.Vector();
        _valuebox = new java.util.Vector();
        _exception = new java.util.Vector();
        _struct = new java.util.Vector();
        _union = new java.util.Vector();
        _enum = new java.util.Vector();
        _typedef = new java.util.Vector();
        _const = new java.util.Vector();
        _native = new java.util.Vector();
        _operation = new java.util.Vector();
        _attribute = new java.util.Vector();
        _member = new java.util.Vector();
        _factory = new java.util.Vector();
    }
}
