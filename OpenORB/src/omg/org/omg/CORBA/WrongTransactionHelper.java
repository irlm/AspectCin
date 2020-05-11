/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * Helper class for deprecated WrongTransaction exception
 *
 * @deprecated Use the WRONG_TRANSACTION system exception
 */
public final class WrongTransactionHelper
{
    public static void insert( org.omg.CORBA.Any any, WrongTransaction value )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public static WrongTransaction extract( org.omg.CORBA.Any any )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public static org.omg.CORBA.TypeCode type()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public static java.lang.String id()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public static WrongTransaction read(
        org.omg.CORBA.portable.InputStream input )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public static void write( org.omg.CORBA.portable.OutputStream output,
                              WrongTransaction value )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
