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

public class IdlFactoryMember extends IdlObject implements org.openorb.compiler.idl.reflect.idlParameter
{
    /**
     * Cree un objet IDL Attribute
     */
    public IdlFactoryMember( IdlObject father )
    {
        super( IdlType.e_factory_member, father );
    }

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------

    public int paramMode()
    {
        return PARAM_IN;
    }

    public org.openorb.compiler.idl.reflect.idlObject paramType()
    {
        reset();
        return current();
    }

    public java.util.Enumeration content()
    {
        return new org.openorb.compiler.idl.reflect.idlEnumeration( null );
    }
}


