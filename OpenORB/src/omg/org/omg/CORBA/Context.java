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

public abstract class Context
{
    public abstract String context_name();
    public abstract org.omg.CORBA.Context parent();
    public abstract org.omg.CORBA.Context create_child( String child_context_name );
    public abstract void set_one_value( String prop_name, org.omg.CORBA.Any value );
    public abstract void set_values( org.omg.CORBA.NVList values );
    public abstract void delete_values( String prop_name );
    public abstract org.omg.CORBA.NVList get_values( String start_scope, int op_flags,
          String pattern );
}
