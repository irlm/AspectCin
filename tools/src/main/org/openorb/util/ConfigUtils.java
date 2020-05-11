/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

/**
 * A set of utilities for configuration.
 *
 * @author Richard G Clark
 * @version $Revision: 1.1 $ $Date: 2004/05/13 02:39:51 $
 */
public final class ConfigUtils
{
    private ConfigUtils()
    {
    }

    /**
     * Prefixes the specifed name with an optional prefix. If a prefix is
     * specified then a "." is added between the prefix and the name.
     *
     * @param prefix the optional prefix to use
     * @param name the name to be prefixed
     * @return a prefixed name
     */
    public static String prefixName( final String prefix, final String name )
    {
        if ( null == prefix )
        {
            return name;
        }

        return prefix + "." + name;
    }

}

