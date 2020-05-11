/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler;

/**
 * This interface must be implemented by a configurator ( that retrieves info from
 * the config file ).
 *
 * @author Jerome Daniel
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:36 $
 */
public interface Configurator
{
    void updateInfo( java.util.Vector includeList, java.util.Vector importLink );
}

