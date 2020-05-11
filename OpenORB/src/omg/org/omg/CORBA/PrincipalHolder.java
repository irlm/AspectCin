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

/**
 * @deprecated Deprecated by CORBA2.2.
 */
public final class PrincipalHolder
    implements org.omg.CORBA.portable.Streamable
{
    public org.omg.CORBA.Principal value;

    public PrincipalHolder()
    {
    }

    public PrincipalHolder( org.omg.CORBA.Principal initial )
    {
        value = initial;
    }

    public void _read( org.omg.CORBA.portable.InputStream is )
    {
        value = is.read_Principal();
    }

    public void _write( org.omg.CORBA.portable.OutputStream os )
    {
        os.write_Principal( value );
    }

    public org.omg.CORBA.TypeCode _type()
    {
        return org.omg.CORBA.ORB.init().get_primitive_tc( TCKind.tk_Principal );
    }
}
