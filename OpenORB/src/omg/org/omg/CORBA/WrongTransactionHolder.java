/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * Holder class for deprecated WrongTransaction exception.
 *
 * @deprecated Use the WRONG_TRANSACTION system exception
 */
public final class WrongTransactionHolder
    implements org.omg.CORBA.portable.Streamable
{
    /**
     * Internal holder variable for the WrongTransaction exception.
     */
    public WrongTransaction value;

    /**
     * Default Constructor.
     */
    public WrongTransactionHolder()
    {
    }

    /**
     * Constructor.
     *
     * @param initial WrongTransaction exception for initializing this class.
     */
    public WrongTransactionHolder( WrongTransaction initial )
    {
        value = initial;
    }

    /**
     * Read a WrongTransaction exception from an InputStream.
     *
     * @param in InputStream to read the exception from.
     */
    public void _read( org.omg.CORBA.portable.InputStream in )
    {
        value = WrongTransactionHelper.read( in );
    }

    /**
     * Write a WrongTransaction exception into an OutputStream.
     *
     * @param out OutputStream to write the exception into
     */
    public void _write( org.omg.CORBA.portable.OutputStream out )
    {
        WrongTransactionHelper.write( out, value );
    }

    /**
     * Return the type code of the WrongTransaction exception.
     *
     * @return The typcode of the transaction.
     */
    public TypeCode _type()
    {
        return WrongTransactionHelper.type();
    }
}
