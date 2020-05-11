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
 * Cette classe represente l'objet IDL Value Box
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:38 $
 */

public class IdlFactory extends IdlObject implements org.openorb.compiler.idl.reflect.idlFactory
{
    /**
     * Indique di une ValueType est abstract
     */
    private boolean _abstract;

    /**
     * Cree un objet IDL Attribute
     */
    public IdlFactory( IdlObject father )
    {
        super( IdlType.e_factory, father );
        _abstract = false;
    }

    /**
     * Fixe le fait que cette valeur est abstraite
     */
    public void abstract_value( boolean value )
    {
        _abstract = value;
    }

    /**
     * Retourne le flag qui indique si une valeur est abstraite
     */
    public boolean abstract_value()
    {
        return _abstract;
    }

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------

    public org.openorb.compiler.idl.reflect.idlParameter [] parameters()
    {
        org.openorb.compiler.idl.reflect.idlParameter [] params = new org.openorb.compiler.idl.reflect.idlParameter[ length() ];

        reset();

        for ( int i = 0; i < length(); i++ )
        {
            params[ i ] = ( org.openorb.compiler.idl.reflect.idlParameter ) current();
            next();
        }

        return params;
    }

    public java.util.Enumeration content()
    {
        return new org.openorb.compiler.idl.reflect.idlEnumeration( null );
    }
}
