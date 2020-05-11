/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.object;

import org.openorb.compiler.parser.IdlType;

/**
 * Cette classe represente l'objet IDL Except
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:38 $
 */

public class IdlExcept extends IdlObject implements org.openorb.compiler.idl.reflect.idlException
{
    /**
     * Cree un objet IDL Except
     */
    public IdlExcept( IdlObject father )
    {
        super( IdlType.e_exception, father );
        _is_container = true;
    }

    /**
     * This method returns an contained object
     */
    public IdlObject searchObject( String name )
    {
        for ( int i = 0; i < _list.size(); i++ )
        {
            if ( _type == IdlType.e_struct )
            {
                if ( ( ( IdlObject ) ( ( ( IdlObject ) ( _list.elementAt( i ) ) )._list.elementAt( 0 ) ) ).name() != null )
                    if ( ( ( IdlObject ) ( ( ( IdlObject ) ( _list.elementAt( i ) ) )._list.elementAt( 0 ) ) ).name().equals( name ) )
                        return ( ( IdlObject ) ( ( ( IdlObject ) ( _list.elementAt( i ) ) )._list.elementAt( 0 ) ) );
            }
        }

        return null;
    }

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------

    public java.util.Enumeration members()
    {
        java.util.Vector tmp = new java.util.Vector();

        reset();

        while ( end() != true )
        {
            tmp.addElement( current() );

            next();
        }

        return new org.openorb.compiler.idl.reflect.idlEnumeration( tmp );
    }

    public java.util.Enumeration content()
    {
        java.util.Vector tmp = new java.util.Vector();

        reset();

        while ( end() != true )
        {
            current().reset();

            switch ( current().current().kind() )
            {

            case IdlType.e_union :

            case IdlType.e_struct :

            case IdlType.e_enum :
                tmp.addElement( current().current() );
                break;
            }

            next();
        }

        return new org.openorb.compiler.idl.reflect.idlEnumeration( tmp );
    }
}

