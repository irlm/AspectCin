/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * Copyright (c) 1999 Object Management Group. Unlimited rights to
 * duplicate and use this code are hereby granted provided that this
 * copyright notice is included.
 */
public final class ValueBaseHolder
    implements org.omg.CORBA.portable.Streamable
{
    public java.io.Serializable value;

    public ValueBaseHolder()
    {
    }

    public ValueBaseHolder( java.io.Serializable initial )
    {
        value = initial;
    }

    public void _read( org.omg.CORBA.portable.InputStream is )
    {
        value = ValueBaseHelper.read( is );
    }

    public void _write( org.omg.CORBA.portable.OutputStream os )
    {
        ValueBaseHelper.write( os, value );
    }

    public org.omg.CORBA.TypeCode _type()
    {
        return org.omg.CORBA.ORB.init().get_primitive_tc( TCKind.tk_value );
    }
}
