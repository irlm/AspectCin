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
 * Cette classe represente l'objet IDL Enum Member
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:38 $
 */

public class IdlEnumMember extends IdlObject
{
    /**
     * Valeur du membre de l'enumeration
     */
    private int value;

    /**
     * Cree un objet IDL Enum Member
    *
    * @param father l'objet proprietaire
     */
    public IdlEnumMember( IdlObject father )
    {
        super( IdlType.e_enum_member, father );
    }

    /**
     * Fixe la valeur du membre de l'enumeration
     *
     * @param val la valeur du membre
     */
    public void setValue( int val )
    {
        value = val;
    }

    /**
     * Retourne la valeur du membre de l'enumeration
     *
     * @return la valeur
     */
    public int getValue()
    {
        return value;
    }
}

