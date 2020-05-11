/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * Helper class for : AbstractBase
 *
 * @author OpenORB Compiler
 */
public class AbstractBaseHelper
{
    /** Internal TypeCode value */
    private static org.omg.CORBA.TypeCode s_tc = null;

    private static final String ID = "IDL:omg.org/CORBA/AbstractBase:1.0";

    /**
     * Insert AbstractBase into an any
     * @param a an any
     * @param t AbstractBase value
     */
    public static void insert( org.omg.CORBA.Any a, java.lang.Object t )
    {
        a.type( type() );
        write( a.create_output_stream(), t );
    }

    /**
     * Extract AbstractBase from an any
     * @param a an any
     * @return the extracted AbstractBase value
     */
    public static java.lang.Object extract( org.omg.CORBA.Any a )
    {
        if ( !a.type().equal( type() ) )
        {
            throw new org.omg.CORBA.MARSHAL();
        }
        return read( a.create_input_stream() );
    }

    /**
     * Return the AbstractBase TypeCode
     * @return a TypeCode
     */
    public static org.omg.CORBA.TypeCode type()
    {
        if ( s_tc == null )
        {
            s_tc = org.omg.CORBA.ORB.init().create_abstract_interface_tc( id(), "AbstractBase" );
        }
        return s_tc;
    }

    /**
     * Return the AbstractBase IDL ID
     * @return an ID
     */
    public static String id()
    {
        return ID;
    }

    /**
     * Read AbstractBase from a marshalled stream
     * @param istream the input stream
     * @return the readed AbstractBase value
     */
    public static java.lang.Object read( org.omg.CORBA.portable.InputStream istream )
    {
        return ( ( org.omg.CORBA_2_3.portable.InputStream ) istream ).read_abstract_interface();
    }

    /**
     * Write AbstractBase into a marshalled stream
     * @param ostream the output stream
     * @param value AbstractBase value
     */
    public static void write( org.omg.CORBA.portable.OutputStream ostream,
          java.lang.Object value )
    {
        ( ( org.omg.CORBA_2_3.portable.OutputStream ) ostream ).write_abstract_interface( value );
    }
}
