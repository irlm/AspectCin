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
 * Cette classe represente l'objet IDL Fixed
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:38 $
 */

public class IdlFixed extends IdlObject implements org.openorb.compiler.idl.reflect.idlFixed
{
    /**
     * Digits du fixed
     */
    private int fixedDigits;

    /**
     * Scale du fixed
     */
    private int fixedScale;

    /**
     * Cree un objet IDL Fixed
     */
    public IdlFixed( int digits, int scale, IdlObject father )
    {
        super( IdlType.e_fixed, father );
        fixedDigits = digits;
        fixedScale = scale;
    }

    /**
     * Retourne le nombre de digits du type fixed
     *
     * @return le nombre de digits
     */
    public int digits()
    {
        return fixedDigits;
    }

    /**
     * Retourne le scale du type fixed
     *
     * @return le scale
     */
    public int scale()
    {
        return fixedScale;
    }
}
