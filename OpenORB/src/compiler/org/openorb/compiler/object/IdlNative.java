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
 * Cette classe represente un type natif IDL
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlNative extends IdlObject implements org.openorb.compiler.idl.reflect.idlNative
{
    /**
     * Cree un objet IDL Native
     */
    public IdlNative( IdlObject father )
    {
        super( IdlType.e_native, father );
    }

}

