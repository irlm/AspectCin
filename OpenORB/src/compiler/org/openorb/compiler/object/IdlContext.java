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
 * Cette classe represente l'objet IDL Context
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:38 $
 */

public class IdlContext extends IdlObject
{

    /**
    * Liste des valeurs de context
    */
    private java.util.Vector list_value;

    /**
    * Cree un objet IDL Context
    */
    public IdlContext( IdlObject father )
    {
        super( IdlType.e_context, father );
        list_value = new java.util.Vector();
    }

    /**
    * Ajoute une valeur a la liste des contextes
    *
    * @param val la valeur a ajouter
    */
    public void addValue( String val )
    {
        list_value.addElement( val );
    }

    /**
    * Retourne la liste des contextes
    *
    * @return la liste des contextes
    */
    public java.util.Vector getValues()
    {
        return list_value;
    }

}

