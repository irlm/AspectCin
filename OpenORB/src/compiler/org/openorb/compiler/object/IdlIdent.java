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
 * Cette classe represente un identificateur vers un objet IDL
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:38 $
 */

public class IdlIdent extends IdlObject implements org.openorb.compiler.idl.reflect.idlIdentifier
{
    /**
     * Objet represente
     */
    private IdlObject internal;

    /**
     * L'identificateur
     */
    private String objectName;

    /**
     * Cree un objet IDL Ident
     */
    public IdlIdent( String name, IdlObject father, IdlObject def )
    {
        super( IdlType.e_ident, father );
        objectName = name;
        internal = def;
    }

    /**
     * Retourne l'objet interne
     *
     * @return l'objet interne
     */
    public IdlObject internalObject()
    {
        return internal;
    }

    /**
     * Retourne le nom de l'objet interne
     */
    public String internalObjectName()
    {
        return objectName;
    }

    /**
     * Change prefix to my self but also to all contained objects
     */
    public void changePrefix( String prefix )
    {
        if ( _prefix_explicit != true )
        {
            _prefix = prefix;
        }
    }

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------

    public org.openorb.compiler.idl.reflect.idlObject original()
    {
        return internal;
    }

    public java.util.Enumeration content()
    {
        return new org.openorb.compiler.idl.reflect.idlEnumeration( null );
    }
}

