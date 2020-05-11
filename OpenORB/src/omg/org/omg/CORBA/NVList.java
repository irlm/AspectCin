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

public abstract class NVList
{
    public abstract int count();
    public abstract org.omg.CORBA.NamedValue add( int flags );
    public abstract org.omg.CORBA.NamedValue add_item( String item_name, int flags );
    public abstract org.omg.CORBA.NamedValue add_value( String name, org.omg.CORBA.Any value,
          int flags );
    public abstract org.omg.CORBA.NamedValue item( int index )
        throws org.omg.CORBA.Bounds;
    public abstract void remove( int index )
        throws org.omg.CORBA.Bounds;
}
