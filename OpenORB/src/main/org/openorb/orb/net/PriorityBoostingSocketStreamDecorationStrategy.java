/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FilterInputStream;
import java.net.Socket;

import org.openorb.orb.config.ORBLoader;

import org.openorb.util.logger.LoggerTeam;

import org.openorb.util.ConfigUtils;

/**
 * A strategy for boosting the priority of reading threads.
 *
 * @author Richard G Clark
 * @version $Revision: 1.1 $ $Date: 2004/05/14 00:36:25 $
 */
public final class PriorityBoostingSocketStreamDecorationStrategy
        implements SocketStreamDecorationStrategy
{
    /**
     * The min thread priority for receiving data.
     */
    private final int m_minPriority;

    public PriorityBoostingSocketStreamDecorationStrategy( final int minPriority )
    {
        m_minPriority = minPriority;
    }

    /**
     * Creates a decorated <code>InputStream</code>.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>InputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    public InputStream decorate( final Socket socket, final InputStream stream )
            throws IOException
    {
        return new MinPriorityInputStream( stream );
    }

    /**
     * Creates a decorated <code>OutputStream</code>.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>OutputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    public OutputStream decorate( final Socket socket,  final OutputStream stream )
            throws IOException
    {
        return stream;
    }

    /**
     * Factory for creating instances of
     * <code>PriorityBoostingSocketStreamDecorationStrategy</code>.
     */
    public static final class Factory implements SocketStreamDecorationStrategy.Factory
    {

        /**
         * The required no-arg constructor
         */
        public Factory()
        {
        }

        /**
         * Optionally creates a new
         * <code>PriorityBoostingSocketStreamDecorationStrategy</code>
         * based on the <code>[&lt;prefix&gt;.]boostReceivePriority</code>
         * property defined in the loader. If this property is false or absent
         * then no object is created.
         *
         * @param logger the logger team to used
         * @param loader the orb loader to be used
         * @param prefix the optional property name prefix to use, e.g. "iiop"
         *
         * @return a new object or <code>null</code>.
         *
         * @throws INITIALIZE if problems occured during setup
         */
        public SocketStreamDecorationStrategy create( final LoggerTeam logger,
                final ORBLoader loader, final String prefix )
        {

            final String boostReceivePriorityName
                    = ConfigUtils.prefixName( prefix, "boostReceivePriority" );

            if ( loader.getBooleanProperty( boostReceivePriorityName, false ) )
            {
                return new PriorityBoostingSocketStreamDecorationStrategy( Thread.MAX_PRIORITY );
            }

            return null;
        }
    }

    /**
     * A priority boosting input stream
     */
    private final class MinPriorityInputStream extends FilterInputStream
    {
        public MinPriorityInputStream( final InputStream in )
        {
            super( in );
        }

        private int setPriority( final Thread thread )
        {
            final int oldPriority = thread.getPriority();

            if ( oldPriority < m_minPriority )
            {
                thread.setPriority( m_minPriority );
            }

            return oldPriority;
        }

        private void restorePriority( final Thread thread, final int oldPriority )
        {
            if ( oldPriority < m_minPriority )
            {
                thread.setPriority( oldPriority );
            }
        }

        public int read() throws IOException
        {
            final Thread thread = Thread.currentThread();
            final int oldPriority = setPriority( thread );
            try
            {
                return super.read();
            }
            finally
            {
                restorePriority( thread, oldPriority );
            }
        }

        public int read( final byte[] buffer ) throws IOException
        {
            final Thread thread = Thread.currentThread();
            final int oldPriority = setPriority( thread );
            try
            {
                return super.read( buffer );
            }
            finally
            {
                restorePriority( thread, oldPriority );
            }
        }

        public int read( final byte[] buffer, final int offset, final int length )
                throws IOException
        {
            final Thread thread = Thread.currentThread();
            final int oldPriority = setPriority( thread );
            try
            {
                return super.read( buffer, offset, length );
            }
            finally
            {
                restorePriority( thread, oldPriority );
            }
        }
    }
}

