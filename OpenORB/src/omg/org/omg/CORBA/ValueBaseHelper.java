/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

/**
 * Helper class for ValueBase
 *
 * This is class is in the org.omg packages from 00-02-08 which
 * and is marked as dummy but cannot be generated from idl files.
 */
public class ValueBaseHelper
{
    private static org.omg.CORBA.TypeCode s_tc = null;

    private static final String ID = "IDL:omg.org/CORBA/ValueBase:1.0";

    public static void insert( org.omg.CORBA.Any a, java.io.Serializable t )
    {
        a.insert_Value( t, type() );
    }

    public static java.io.Serializable extract( org.omg.CORBA.Any a )
    {
        return a.extract_Value();
    }

    public static org.omg.CORBA.TypeCode type()
    {
        if ( s_tc == null )
        {
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();
            org.omg.CORBA.ValueMember [] members = new org.omg.CORBA.ValueMember[ 0 ];
            org.omg.CORBA.TypeCode concreteTC = orb.get_primitive_tc(
                    org.omg.CORBA.TCKind.tk_null );
            s_tc = orb.create_value_tc( id(), "ValueBase", org.omg.CORBA.VM_NONE.value,
                    concreteTC, members );
        }
        return s_tc;
    }

    /**
     * Return the ValueBase IDL ID
     * @return an ID
     */
    public static String id()
    {
        return ID;
    }

    /**
     * Read ValueBase from a marshalled stream
     * @param istream the input stream
     * @return the readed ValueBase value
     */
    public static java.io.Serializable read( org.omg.CORBA.portable.InputStream istream )
    {
        return ( java.io.Serializable ) ( ( org.omg.CORBA_2_3.portable.InputStream )
              istream ).read_value( id() );
    }

    /**
     * Write ValueBase into a marshalled stream
     * @param ostream the output stream
     * @param value ValueBase value
     */
    public static void write( org.omg.CORBA.portable.OutputStream ostream,
          java.io.Serializable value )
    {
        ( ( org.omg.CORBA_2_3.portable.OutputStream ) ostream ).write_value( value, id() );
    }
}
