/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.complex;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

/**
 * This class is used to test the mapping for IDLEntities other
 * than org.omg.CORBA.Any and org.omg.CORBA.TypeCode.
 *
 * @author Michael Rumpf
 */
public abstract class IDLStructHelper
{
    private static String s_id = "IDL:com/sun/cts/tests/rmiiiop/ee/marshaltests/IDLStruct:1.0";
    private static TypeCode s_typeCode = null;
    private static boolean s_active = false;

    private IDLStructHelper()
    {
    }

    public static void insert( Any a, IDLStruct that )
    {
        OutputStream out = a.create_output_stream();
        a.type( type() );
        write( out, that );
        a.read_value( out.create_input_stream(), type() );
    }

    public static IDLStruct extract( Any a )
    {
        return read( a.create_input_stream() );
    }

    public static synchronized TypeCode type()
    {
        if ( s_typeCode == null )
        {
            if ( s_active )
            {
                TypeCode typecode = ORB.init().create_recursive_tc( s_id );
                return typecode;
            }
            s_active = true;
            StructMember[] members = new StructMember[ 1 ];
            TypeCode tcmember = ORB.init().get_primitive_tc( TCKind.tk_short );
            members[ 0 ] = new StructMember( "x", tcmember, null );
            s_typeCode = ORB.init().create_struct_tc( id(), "IDLStruct", members );
            s_active = false;
        }
        return s_typeCode;
    }

    public static String id()
    {
        return s_id;
    }

    public static IDLStruct read( InputStream istream )
    {
        IDLStruct value = new IDLStruct();
        value.setValue( istream.read_short() );
        return value;
    }

    public static void write( OutputStream ostream, IDLStruct value )
    {
        ostream.write_short( value.getValue() );
    }
}

