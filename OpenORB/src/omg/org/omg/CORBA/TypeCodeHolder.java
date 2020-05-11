/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

/*
 * Copyright (c) 1999 Object Management Group. Unlimited rights to
 * duplicate and use this code are hereby granted provided that this
 * copyright notice is included.
 */

package org.omg.CORBA;

public final class TypeCodeHolder
    implements org.omg.CORBA.portable.Streamable
{
    public org.omg.CORBA.TypeCode value;

    public TypeCodeHolder()
    {
    }

    public TypeCodeHolder( org.omg.CORBA.TypeCode initial )
    {
        value = initial;
    }

    public void _read( org.omg.CORBA.portable.InputStream is )
    {
        value = is.read_TypeCode();
    }

    public void _write( org.omg.CORBA.portable.OutputStream os )
    {
        os.write_TypeCode( value );
    }

    public org.omg.CORBA.TypeCode _type()
    {
        return org.omg.CORBA.ORB.init().get_primitive_tc( TCKind.tk_TypeCode );
    }
}
