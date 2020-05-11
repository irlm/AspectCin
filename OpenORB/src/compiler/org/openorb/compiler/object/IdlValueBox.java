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
 * This class represents IDL Value Box object
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlValueBox extends IdlObject implements org.openorb.compiler.idl.reflect.idlValueBox
{
    /**
     * Creates IDL Value Box object
     */
    public IdlValueBox( IdlObject father )
    {
        super( IdlType.e_value_box, father );
    }

    /**
     * Returns the type
     *
     * @return le type
     */
    public IdlObject type()
    {
        return ( IdlObject ) _list.elementAt( 0 );
    }

    /**
     * Set the type
     *
     * @param tp the type
     */
    public void type ( IdlObject tp )
    {
        _list.removeAllElements();
        _list.addElement( tp );
    }

    /**
     * Returns is a simple type
     */
    public boolean simple()
    {
        reset();

        if ( current() instanceof IdlSimple )
            return true;

        return false;
    }

    // ------------------------------------------------------------------------------------------
    // IDL Reflection
    // ------------------------------------------------------------------------------------------

    public boolean isPrimitive()
    {
        return simple();
    }

    public org.openorb.compiler.idl.reflect.idlObject original()
    {
        return type();
    }

    public java.util.Enumeration content()
    {
        return new org.openorb.compiler.idl.reflect.idlEnumeration( null );
    }
}
