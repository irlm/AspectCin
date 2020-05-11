/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.launcher;

import java.net.URL;

/**
 * A compound class loader that combines a parent and secondary class loader.
 *
 * @author Richard G Clark
 * @version $Revision: 1.3 $ $Date: 2004/02/10 22:25:31 $
 */
public final class CompoundClassLoader
    extends ClassLoader
{
    /**
     * The secondary class loader
     */
    private final ClassLoader m_secondary;

    /**
     * Constructs this with the specified parent and secondary class loaders.
     *
     * @param parent the parent class loader
     * @param secondary the secondary class loader
     */
    private CompoundClassLoader( final ClassLoader parent,
            final ClassLoader secondary )
    {
        super( parent );

        m_secondary = secondary;
    }

    /**
     * Tries to find the class in the secondary class loader.
     *
     * @param name the class name
     * @return the resulting <code>Class</code> object
     * @throws ClassNotFoundException if the class could not be found
     */
    protected Class findClass( final String name ) throws ClassNotFoundException
    {
        return m_secondary.loadClass( name );
    }

    /**
     * Tries to get the resource URL using the parent and secondary class
     * loaders.
     *
     * @param name resource name
     * @return a <code>URL</code> for reading the resource, or
     *         <code>null</code> if the resource could not be found or the
     *         caller doesn't have adequate privileges to get the resource.
     */
    public URL getResource( final String name )
    {
        final URL url = super.getResource( name );

        if ( null != url )
        {
            return url;
        }

        return m_secondary.getResource( name );
    }

    /**
     * Returns a class loader for the specified parent and secondary class
     * loaders. If the secondary class loader is <code>null</code> then
     * parent is returned.
     *
     * @param parent the parent class loader
     * @param secondary the secondary class loader, can be <code>null</code>
     *
     * @return a class loader
     */
    public static ClassLoader join( final ClassLoader parent, final ClassLoader secondary )
    {
        if ( null == secondary )
        {
            return parent;
        }

        return new CompoundClassLoader( parent, secondary );
    }
}

