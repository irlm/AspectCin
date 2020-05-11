/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.typecode;

import java.util.Map;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.CompletionStatus;
import org.openorb.util.ExceptionTool;

/**
 * This base class is inherited by all the type code classes and provides some
 * extra functionality used in the any classes and when constructing the
 * typecode.
 *
 * @author Chris Wood
 * @version $Revision: 1.5 $ $Date: 2004/02/10 21:02:48 $
 */
public abstract class TypeCodeBase
    extends org.omg.CORBA.TypeCode
{
    abstract boolean _is_recursive();

    abstract boolean _fix_recursive( Map recursive );

    public abstract TypeCodeBase _base_type();

    public abstract boolean _is_compact();

    public boolean equals( Object tc )
    {
        if ( tc instanceof org.omg.CORBA.TypeCode )
        {
            return equal( ( org.omg.CORBA.TypeCode ) tc );
        }
        else
        {
            return false;
        }
    }

    public static org.omg.CORBA.TypeCode _base_type( org.omg.CORBA.TypeCode type )
    {
        if ( type instanceof TypeCodeBase )
        {
            return ( ( TypeCodeBase ) type )._base_type();
        }
        else
        {
            try
            {
                while ( type.kind() == TCKind.tk_alias )
                {
                    type = type.content_type();
                }
            }
            catch ( final org.omg.CORBA.TypeCodePackage.BadKind ex1 )
            {
                throw ExceptionTool.initCause( new org.omg.CORBA.BAD_PARAM(
                        "Unexpected BadKind exception",
                        org.omg.CORBA.OMGVMCID.value | 20,
                        CompletionStatus.COMPLETED_MAYBE ), ex1 );
            }
            return type;
        }
    }
}

