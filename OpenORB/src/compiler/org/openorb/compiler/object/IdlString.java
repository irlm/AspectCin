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
 * Cette classe represente l'objet IDL String
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlString extends IdlObject implements org.openorb.compiler.idl.reflect.idlString
{
    /**
    * Taille de la chaine
    */
    private int stringSize;

    /**
    * Cree un objet IDL String
    */
    public IdlString( int size, IdlObject father )
    {
        super( IdlType.e_string, father );
        stringSize = size;
    }

    /**
     * Retourne la taille maximale de la chaine
     *
     * @return la taille max
     */
    public int maxSize()
    {
        return stringSize;
    }

    /**
     * Change prefix to my self but also to all contained objects
     */
    public void changePrefix( String prefix )
    {}

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------



    public int max()
    {
        return stringSize;
    }
}

