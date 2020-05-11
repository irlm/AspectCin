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
 * Cette classe represente l'objet IDL Module
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlModule extends IdlObject implements org.openorb.compiler.idl.reflect.idlModule
{
    // ------------
    // CONSTRUCTEUR
    // ------------
    /**
    * Cree un objet IDL Module
    */
    public IdlModule( IdlObject father )
    {
        super( IdlType.e_module, father );
        _is_container = true;
    }

    /**
    * Return the equivalent object for the given name
    */
    public IdlObject sameAs( String name )
    {
        for ( int i = 0; i < _list.size(); i++ )
        {
            if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).name() != null )
            {
                if ( ( ( IdlObject ) ( _list.elementAt( i ) ) ).isSame( adaptName( name ) ) )
                    return ( IdlObject ) ( _list.elementAt( i ) );
            }
        }

        if ( isSame( adaptName( name ) ) )
            return this;


        return null;
    }
}

