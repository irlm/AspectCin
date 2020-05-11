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

public abstract class ExceptionList
{
    public abstract int count();
    public abstract void add( org.omg.CORBA.TypeCode exc );
    public abstract org.omg.CORBA.TypeCode item( int index )
        throws org.omg.CORBA.Bounds;
    public abstract void remove( int index )
        throws org.omg.CORBA.Bounds;
}
