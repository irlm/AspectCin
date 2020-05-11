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

public abstract class Request
{
    public abstract org.omg.CORBA.Object target();
    public abstract String operation();
    public abstract org.omg.CORBA.NVList arguments();
    public abstract org.omg.CORBA.NamedValue result();
    public abstract org.omg.CORBA.Environment env();
    public abstract org.omg.CORBA.ExceptionList exceptions();
    public abstract org.omg.CORBA.ContextList contexts();

    public abstract void ctx( org.omg.CORBA.Context ctx );
    public abstract org.omg.CORBA.Context ctx();

    public abstract org.omg.CORBA.Any add_in_arg();
    public abstract org.omg.CORBA.Any add_named_in_arg( String name );
    public abstract org.omg.CORBA.Any add_inout_arg();
    public abstract org.omg.CORBA.Any add_named_inout_arg( String name );
    public abstract org.omg.CORBA.Any add_out_arg();
    public abstract org.omg.CORBA.Any add_named_out_arg( String name );
    public abstract void set_return_type( org.omg.CORBA.TypeCode tc );
    public abstract org.omg.CORBA.Any return_value();

    public abstract void invoke();
    public abstract void send_oneway();
    public abstract void send_deferred();
    public abstract void get_response()
        throws org.omg.CORBA.WrongTransaction;
    public abstract boolean poll_response();
}
