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
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlStateMember extends IdlObject implements org.openorb.compiler.idl.reflect.idlState
{
    /**
     * Indique si un membre est public
     */
    private boolean _public;

    /**
     * Cree un objet IDL Attribute
     */
    public IdlStateMember( IdlObject father )
    {
        super( IdlType.e_state_member, father );
        _public = false;
    }

    /**
     * Fixe le fait que ce membre est public
     */
    public void public_member( boolean value )
    {
        _public = value;
    }

    /**
     * Retourne le flag qui indique si un membre est public
     */
    public boolean public_member()
    {
        return _public;
    }

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------

    public boolean isPublic()
    {
        return _public;
    }

    public org.openorb.compiler.idl.reflect.idlObject stateType()
    {
        reset();
        return current();
    }

    public java.util.Enumeration content()
    {
        return new org.openorb.compiler.idl.reflect.idlEnumeration( null );
    }
}
