/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA_2_3.portable;

/**
 * Copyright (c) 1999 Object Management Group. Unlimited rights to
 * duplicate and use this code are hereby granted provided that this
 * copyright notice is included.
 */
public abstract class InputStream
    extends org.omg.CORBA.portable.InputStream
{
    public java.io.Serializable read_value()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public java.io.Serializable read_value( java.lang.String rep_id )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public java.io.Serializable read_value( java.lang.Class clz )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public java.io.Serializable read_value(
        org.omg.CORBA.portable.BoxedValueHelper factory )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public java.io.Serializable read_value( java.io.Serializable value )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public java.lang.Object read_abstract_interface()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public java.lang.Object read_abstract_interface( java.lang.Class clz )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
