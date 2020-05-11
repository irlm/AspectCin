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
 * Cette classe represente l'objet IDL Enum
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:38 $
 */

public class IdlEnum extends IdlObject implements org.openorb.compiler.idl.reflect.idlEnum
{
    // ------------
    // CONSTRUCTEUR
    // ------------
    /**
    * Cree un objet IDL Enum
    */
    public IdlEnum( IdlObject father )
    {
        super( IdlType.e_enum, father );
        _is_container = true;
    }

    /**
     * This method returns true if this object is the same as the given name.
     */
    public boolean isSame( String name )
    {
        if ( name().equals( name ) )
            return true;

        for ( int i = 0; i < _list.size(); i++ )
        {
            if ( ( ( IdlObject ) _list.elementAt( i ) ).isSame( name ) )
            {
                return true;
            }
        }

        return false;
    }

    /**
    * Return the equivalent object for the given name
    */
    public IdlObject sameAs( String name )
    {
        if ( name().equals( name ) )
            return this;

        for ( int i = 0; i < _list.size(); i++ )
        {
            if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).name() != null )
            {
                if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).isSame( name ) )
                    return ( IdlObject ) ( _list.elementAt( i ) );
            }
        }

        return null;
    }

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------

    public String [] members()
    {
        String [] _members = new String[ length() ];

        reset();
        int i = 0;

        while ( end() != true )
        {
            _members[ i++ ] = ( ( IdlEnumMember ) current() ).name();
            next();
        }

        return _members;
    }

    public java.util.Enumeration content()
    {
        return new org.openorb.compiler.idl.reflect.idlEnumeration( null );
    }
}

