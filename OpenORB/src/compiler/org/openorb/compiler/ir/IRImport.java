/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.ir;

import org.openorb.compiler.object.IdlObject;
import org.openorb.compiler.parser.IdlParser;

/**
 * This interface must be implemented to get IDL descriptions from IR.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:38 $
 */
public interface IRImport
{
    public void set_parser( IdlParser parser );

    public void getDescriptionFromIR( String scope_name, IdlObject current_scope );
}

