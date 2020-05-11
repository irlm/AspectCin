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
 * This class represents the IDL Value Box object
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlValueInheritance extends IdlObject
{
    /**
     * Indicates if this member is truncatable
     */
    public boolean _trunc;

    /**
     * Creates an IDL Attribute object
     */
    public IdlValueInheritance( IdlObject father )
    {
        super( IdlType.e_value_inheritance, father );
        _trunc = false;
    }

    /**
     * Set if this member is truncatable
     */
    public boolean truncatable_member()
    {
        return _trunc;
    }

    /**
     * Returns is member truncatable
     */
    public void truncatable_member ( boolean value )
    {
        _trunc = value;
    }

    /**
     * Returns the value definition
     */
    public IdlValue getValue()
    {
        return ( IdlValue ) _list.elementAt( 0 );
    }


}
