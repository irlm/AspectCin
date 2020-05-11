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
 * Cette classe represente une information sur une inclusion dans un fichier IDL
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:39 $
 */

public class IdlInclude extends IdlObject
{
    /**
     * Nom du fichier
     */
    public String file_name;

    /**
        * Cree un objet IDL Include
        */
    public IdlInclude( IdlObject father, String file_name )
    {
        super( IdlType.e_include, father );
        name( "include__" + file_name );
        this.file_name = file_name;
    }

    /**
     * Retourne le nom du fichier
     */
    public String file_name()
    {
        return file_name;
    }
}
