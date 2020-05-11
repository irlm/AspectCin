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
import java.net.Socket;

/**
 * A abstract base class that provides exception checking and autoclosing
 * of streams.
 *
 * @author Richard G Clark
 * @version $Revision: 1.1 $ $Date: 2004/05/14 00:36:25 $
 */
public abstract class AbstractSocketStreamDecorationStrategy
        implements SocketStreamDecorationStrategy
{
    protected AbstractSocketStreamDecorationStrategy()
    {
    }

    /**
     * Creates a decorated <code>InputStream</code>.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>InputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    public final InputStream decorate( final Socket socket, final InputStream stream )
            throws IOException
    {
        try
        {
            return uncheckedDecorate( socket, stream );
        }
        catch ( final IOException e )
        {
            closeStream( stream );
            throw e;
        }
        catch ( final RuntimeException e )
        {
            closeStream( stream );
            throw e;
        }
    }

    /**
     * Creates a decorated <code>OutputStream</code>.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>OutputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    public final OutputStream decorate( final Socket socket,  final OutputStream stream )
            throws IOException
    {
        try
        {
            return uncheckedDecorate( socket, stream );
        }
        catch ( final IOException e )
        {
            closeStream( stream );
            throw e;
        }
        catch ( final RuntimeException e )
        {
            closeStream( stream );
            throw e;
        }
    }

    /**
     * Creates a decorated <code>InputStream</code>.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>InputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    protected InputStream uncheckedDecorate( final Socket socket, final InputStream stream )
            throws IOException
    {
        return stream;
    }

    /**
     * Creates a decorated <code>OutputStream</code>.
     *
     * @param socket the source of the original stream
     * @param stream the stream to be decorated
     * @return a decorated <code>OutputStream</code>.
     * @throws IOException if an I/O error occurs while creating the socket.
     */
    protected OutputStream uncheckedDecorate( final Socket socket,  final OutputStream stream )
            throws IOException
    {
        return stream;
    }

    /**
     * Closes the stream. If an exception is thrown while closing the stream
     * the <code>exceptionDuringStreamClose</code> is called.
     *
     * @param stream the stream to be closed
     */
    protected void closeStream( final InputStream stream )
    {
        try
        {
            stream.close();
        }
        catch ( final Exception e )
        {
            try
            {
                exceptionDuringStreamClose( stream, e );
            }
            catch ( final Exception e2 )
            {
                // discard to avoid hiding original cause
            }
        }
    }

    /**
     * Closes the stream. If an exception is thrown while closing the stream
     * the <code>exceptionDuringStreamClose</code> is called.
     *
     * @param stream the stream to be closed
     */
    protected void closeStream( final OutputStream stream )
    {
        try
        {
            stream.close();
        }
        catch ( final Exception e )
        {
            try
            {
                exceptionDuringStreamClose( stream, e );
            }
            catch ( final Exception e2 )
            {
                // discard to avoid hiding original cause
            }
        }
    }

    /**
     * Called when an exception is thrown during a call to
     * <code>closeStream</code>.
     *
     * @param stream the stream the threw the exception
     * @param e the exception thrown
     */
    protected void exceptionDuringStreamClose( final InputStream stream, final Exception e )
    {
        // discard exception
    }

    /**
     * Called when an exception is thrown during a call to
     * <code>closeStream</code>.
     *
     * @param stream the stream the threw the exception
     * @param e the exception thrown
     */
    protected void exceptionDuringStreamClose( final OutputStream stream, final Exception e )
    {
        // discard exception
    }
}

