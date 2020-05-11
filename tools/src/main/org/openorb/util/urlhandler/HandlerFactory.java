/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.urlhandler;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import org.openorb.util.urlhandler.resource.Handler;

/**
 * This is the factory class to create a custom URL stream handler.
 *
 * @author  Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:28:45 $
 */
public class HandlerFactory implements URLStreamHandlerFactory
{

    private ClassLoader m_classloader = null;

    /**
     * Creates a new HandlerFactory with the context classloader for loading
     * ressources
     */
    public HandlerFactory()
    {
    }

    /**
     * Creates a new HandlerFactory with a specific classloader
     * @param cl specific classloader for loading resources, null falls back to
     * the context class loader
     */
    public HandlerFactory( ClassLoader cl )
    {
        m_classloader = cl;
    }

    /**
     * Creates a <code>URLStreamHandler</code> for supported protocols.
     * Currently &quot;classpath&quot; and &quot;resource&quot; are supported.
     *
     * @param protocol the protocol to be handled.
     * @return a <code>URLStreamHandler</code> if the protocol is supported,
     *         null if not.
     */
    public URLStreamHandler createURLStreamHandler( final String protocol )
    {
        if ( "classpath".equals( protocol ) || "resource".equals( protocol ) )
        {
            return new Handler( m_classloader );
        }
        return null;
    }
}
