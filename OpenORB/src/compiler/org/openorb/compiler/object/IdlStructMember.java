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
 * Cette classe represente l'objet IDL Struct member
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlStructMember extends IdlObject implements org.openorb.compiler.idl.reflect.idlStructMember
{
    /**
     * Cree un objet IDL Struct
     */
    public IdlStructMember( IdlObject father )
    {
        super( IdlType.e_struct_member, father );
    }

    /**
     * Retourne le type du membre
     *
     * @return le type
     */
    public IdlObject type()
    {
        return ( IdlObject ) _list.elementAt( 0 );
    }

    /**
     * Fixe le type du membre
     *
     * @param tp le type
     */
    public void type ( IdlObject tp )
    {
        _list.removeAllElements();
        _list.addElement( tp );
    }

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------

    public java.util.Enumeration content()
    {
        return new org.openorb.compiler.idl.reflect.idlEnumeration( null );
    }

    public org.openorb.compiler.idl.reflect.idlObject internal()
    {
        reset();
        return current();
    }
}

