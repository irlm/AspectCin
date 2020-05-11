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

package org.omg.CORBA.TypeCodePackage;

public final class Bounds
    extends org.omg.CORBA.UserException
{
    public Bounds()
    {
        super( "IDL:omg.org/CORBA/TypeCode/Bounds:1.0" );
    }

    public Bounds( String reason )
    {
        // full constructor
        super( "IDL:omg.org/CORBA/TypeCode/Bounds:1.0 " + reason );
    }
}
