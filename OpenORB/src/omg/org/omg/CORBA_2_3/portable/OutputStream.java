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
public abstract class OutputStream
    extends org.omg.CORBA.portable.OutputStream
{
    public void write_value( java.io.Serializable value )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_value( java.io.Serializable value,
                             java.lang.String rep_id )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_value( java.io.Serializable value, Class clz )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_value( java.io.Serializable value,
                             org.omg.CORBA.portable.BoxedValueHelper factory )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void write_abstract_interface( java.lang.Object object )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
