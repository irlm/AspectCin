/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import java.net.Socket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.ConnectException;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.InterruptedIOException;

import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.COMM_FAILURE;

import org.openorb.orb.io.StorageBuffer;

import org.openorb.orb.net.Address;
import org.openorb.orb.net.Transport;
import org.openorb.orb.net.SocketFactory;

import org.openorb.util.ExceptionTool;

/**
 * Interface for creating sockets.
 *
 * @author Unknown
 * @version $Revision: 1.18 $ $Date: 2004/11/17 15:26:08 $
 */
public class IIOPTransport
    implements Transport
{
    /**
     * Maximum time to wait for EOF when initiating close. Each time data
     * arrives this starts again.
     */
    //private static final long MAX_CLOSE_LINGER = 1000;

    private final SocketFactory m_socketFactory;

    private final Logger m_logger;

    private InetAddress m_host = null;
    private int m_port = -1;
    private String m_connection_string;

    private Socket m_socket;
    private InputStream m_in_stream;
    private OutputStream m_out_stream;

    // scratch space for unmarshaling header.
    private byte [] m_header = null;

    private boolean m_message_error = false;
    private boolean m_remote_close = false;
    private int m_minor_version = 0;

    private boolean m_open = false;


    private static final byte [][] MESSAGE_ERROR
    = { { ( byte ) 'G', ( byte ) 'I', ( byte ) 'O', ( byte ) 'P',
          1, 0, 0, org.omg.GIOP.MsgType_1_1._MessageError, 0, 0, 0, 0 },
        { ( byte ) 'G', ( byte ) 'I', ( byte ) 'O', ( byte ) 'P',
          1, 1, 0, org.omg.GIOP.MsgType_1_1._MessageError, 0, 0, 0, 0 },
        { ( byte ) 'G', ( byte ) 'I', ( byte ) 'O', ( byte ) 'P',
          1, 2, 0, org.omg.GIOP.MsgType_1_1._MessageError, 0, 0, 0, 0 } };
    private static final byte [][] CLOSE_CONNECTION
    = { { ( byte ) 'G', ( byte ) 'I', ( byte ) 'O', ( byte ) 'P',
          1, 0, 0, org.omg.GIOP.MsgType_1_1._CloseConnection, 0, 0, 0, 0 },
        { ( byte ) 'G', ( byte ) 'I', ( byte ) 'O', ( byte ) 'P',
          1, 1, 0, org.omg.GIOP.MsgType_1_1._CloseConnection, 0, 0, 0, 0 },
        { ( byte ) 'G', ( byte ) 'I', ( byte ) 'O', ( byte ) 'P',
          1, 2, 0, org.omg.GIOP.MsgType_1_1._CloseConnection, 0, 0, 0, 0 } };


    public IIOPTransport( final InetAddress host, final int port,
            final Logger logger )
    {
        this( host, port, logger, null );
    }

    /**
     * Constructor.
     * @param host Host of the endpoint.
     * @param port Port of the endoint.
     */
    public IIOPTransport( final InetAddress host, final int port,
            final Logger logger, final SocketFactory socketFactory )
    {
        m_host = host;
        m_port = port;
        m_logger = logger;

        m_connection_string = m_host.getHostName() + ":" + m_port;

        m_socketFactory = socketFactory;
    }

    /**
     * Constructor.
     * @param sock Client socket.
     * @param serverPort Port of the endoint.
     * @param logger The logger to use in this instance.
     */
    public IIOPTransport( final Socket sock, final int serverPort,
            final Logger logger )
    {
        m_socket = sock;
        m_port = serverPort;
        m_logger = logger;
        m_socketFactory = null;

        try
        {
            m_in_stream =  m_socket.getInputStream();
            m_out_stream = m_socket.getOutputStream();
        }
        catch ( final IOException ex )
        {
            getLogger().error( "IOException getting input and output streams.", ex );

            throw ExceptionTool.initCause( new COMM_FAILURE( 0,
                    CompletionStatus.COMPLETED_NO ), ex );
        }

        m_open = true;

        m_connection_string = serverPort + " (" + m_socket.getLocalPort() + " <- "
                      + m_socket.getInetAddress().getHostName() + ":" + m_socket.getPort() + ")";
    }

    /**
     * Open the connection. This can throw a CORBA system exception. This is never
     * called by server channels.
     */
    public void open()
    {
        if ( m_host == null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER();
        }
        if ( m_open )
        {
            return;
        }
        m_message_error = false;

        m_remote_close = false;

        try
        {
            m_socket = createSocket( m_host, m_port );
            m_in_stream =  m_socket.getInputStream();
            m_out_stream = m_socket.getOutputStream();
        }
        catch ( final NoRouteToHostException ex )
        {
            getLogger().error( "No route to host '" + m_host
                    + "', port " + m_port + ". Check your network.", ex );

            throw ExceptionTool.initCause( new COMM_FAILURE(
                    "No route to host '" + m_host + "', port "
                    + m_port + ". Check your network (" + ex + ")",
                    IIOPMinorCodes.COMM_FAILURE_NO_ROUTE,
                    CompletionStatus.COMPLETED_NO ), ex );
        }
        catch ( final ConnectException ex )
        {

            // don't log this error because (a) we see it in the exception, and (b) this
            // normally occurs during startup with interceptors that use IIOP

            // if ( getLogger().isErrorEnabled() )
            //    getLogger().error( "Cannot connect to target.", ex );

            throw ExceptionTool.initCause( new COMM_FAILURE(
                    "Cannot connect to host '" + m_host + "', port " + m_port + " (" + ex + ")",
                    IIOPMinorCodes.COMM_FAILURE_NO_CONNECT,
                    CompletionStatus.COMPLETED_NO ), ex );
        }
        catch ( final IOException ex )
        {
            getLogger().error( "IOException during connect.", ex );

            throw ExceptionTool.initCause( new COMM_FAILURE(
                    "IOException during connect to host '"
                    + m_host + "', port " + m_port + "(" + ex + ")", 0,
                    CompletionStatus.COMPLETED_NO ), ex );
        }

        m_open = true;

        m_connection_string = m_host.getHostName() + ":" + m_port + " (" + m_socket.getLocalPort()
                      + " -> " + m_socket.getPort() + ")";
    }

    /**
     * Open the socket to the specified host and port.
     */
    protected Socket createSocket( final InetAddress host, final int port )
            throws IOException
    {
        return m_socketFactory.createSocket( host, port );
    }

    /**
     * Return the socket.
     *
     * @return The socket instance associated with this transport.
     */
    protected Socket getSocket()
    {
        return m_socket;
    }

    /**
     * Close the connection this always succeeds.
     */
    public void close()
    {
        if ( m_open )
        {
            boolean interrupt = Thread.interrupted();
            if ( !m_remote_close )
            {
                try
                {
                    writeCloseMessage();
                    // try the shutdownOutput method.
                    try
                    {
                        m_socket.getClass().getMethod( "shutdownOutput", null ).invoke(
                                m_socket, null );
                    }
                    catch ( Exception ex )
                    {
                        // ignore any exceptions we get here, it's a best effort anyhow
                    }

                    // wait for EOF from other end.
                    if ( m_message_error || m_minor_version == 2 || m_host == null )
                    {
                        // read and discard any extra incoming messages.
                        Object recv;
                        do
                        {
                            recv = recvMessage( 1000 );
                            interrupt = Thread.interrupted() || interrupt;
                        }
                        while ( recv != null );
                    }
                }
                catch ( org.omg.CORBA.SystemException ex )
                {
                    // TODO: ???
                }
                catch ( EOFException ex )
                {
                    // communication partner has closed socket
                    // this is expected behaviour during close(), so don't log errors here!
                    getLogger().debug( "An EOFException occured during recvMessage() "
                            + "while closing connection!", ex );
                }
            }

            m_open = false;
            try
            {
                m_socket.close();
            }
            catch ( IOException ex )
            {
                getLogger().warn( "An IOException occured during socket close()!", ex );
            }

            if ( interrupt )
            {
                Thread.currentThread().interrupt();
            }
            if ( m_host != null )
            {
                m_connection_string = m_host.getHostName() + ":" + m_port;
            }
            else
            {
                m_connection_string = Integer.toString( m_port );
            }
        }
    }

    /**
     * Send the close message.
     */
    protected void writeCloseMessage()
    {
        if ( m_message_error )
        {
            write( MESSAGE_ERROR[ m_minor_version ], 0, 12 );
        }
        else if ( m_minor_version == 2 || m_host == null )
        {
            write( CLOSE_CONNECTION[ m_minor_version ], 0, 12 );
        }
    }

    /**
     * Is is the transport open?
     */
    public boolean isOpen()
    {
        return m_open;
    }

    /**
     * Write message to comms protocol.
     *
     * @param msg The message to be sent.
     * @throws org.omg.CORBA.COMM_FAILURE permanent transport failure occoured.
     *     Cleanup and then call close.
     */
    public void sendMessage( StorageBuffer msg )
    {
        if ( !m_open )
        {
            throw new org.omg.CORBA.COMM_FAILURE( "Transport is closed" );
        }
        try
        {
            msg.writeTo( m_out_stream );
            m_out_stream.flush();
        }
        catch ( final IOException ex )
        {
            getLogger().error( "IOException while writing to output stream.", ex );

            throw ExceptionTool.initCause( new COMM_FAILURE(
                    "IOException while writing to output stream." ), ex );
        }
    }

    /**
     * Write bytes directly to socket. Not this function *is not* called for
     * every write and is provided to allow arbitary data to be written to
     * the socket.
     *
     * @param buf message buffer.
     * @param off offset of beginning of message in buffer.
     * @param len length of message in buffer.
     *
     * @throws org.omg.CORBA.COMM_FAILURE permanent transport failure occoured.
     */
    protected void write( byte [] buf, int off, int len )
    {
        boolean interrupt = Thread.interrupted();

        do
        {
            try
            {
                m_out_stream.write( buf, off, len );
                break;
            }
            catch ( InterruptedIOException ex )
            {
                interrupt = true;
                off += ex.bytesTransferred;
                len -= ex.bytesTransferred;
            }
            catch ( IOException ex )
            {
                //        if( getLogger().isErrorEnabled() )
                //          getLogger().error( "IOException while writing to output stream.", ex );
                throw ExceptionTool.initCause( new COMM_FAILURE(), ex );
            }
        }
        while ( len > 0 );

        if ( interrupt )
        {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Read next message. This function will be regularly serviced by a worker
     * thread.
     *
     * @param timeout max time to wait before recieving a message.
     * @throws EOFException end of file has been reached. Cleanup and call close.
     * @throws org.omg.CORBA.COMM_FAILURE permanent transport failure occoured.
     *      Cleanup and then call close.
     */
    public StorageBuffer recvMessage( int timeout )
        throws EOFException
    {
        // this is realy an illegal state.
        if ( !m_open )
        {
            throw new org.omg.CORBA.COMM_FAILURE( "Transport is closed" );
        }

        if ( null == m_header )
        {
            m_header = new byte[ 12 ];
        }

        final Thread thread = Thread.currentThread();


        if ( thread.isInterrupted() )
        {
            return null;
        }

        if ( !readMagic( timeout, m_header, 0 ) )
        {
            return null;
        }

        boolean interrupt = Thread.interrupted();

        read( m_header, 4, 8 );

        final int bodyLength = checkGIOPHeader( m_header, 0 );

        interrupt = interrupt || Thread.interrupted();

        StorageBuffer ret = readBuffer( m_header, 0, 12, bodyLength + 12 );

        if ( interrupt )
        {
            thread.interrupt();
        }

        return ret;
   }

    /**
     * If this gets called then the close operation will close by sending a
     * MessageError message.
     */
    public void setMessageError()
    {
        m_message_error = true;
    }

    /**
     * Returns true if setMessageError has been called.
     */
    public boolean isMessageError()
    {
        return m_message_error;
    }

    /**
     * Reads four bytes from the input stream into the buffer at the
     * specified offset. Returns true if the read was successful before
     * the timeout occoured.
     */
    protected boolean readMagic( int timeout, byte [] buf, int off )
        throws EOFException
    {
        if ( !m_open )
        {
            throw new org.omg.CORBA.COMM_FAILURE( "Transport is closed" );
        }
        int m;

        try
        {
            boolean interrupt = false;

            try
            {
                if ( timeout > 0 )
                {
                    m_socket.setSoTimeout( timeout );
                }
                m = m_in_stream.read( buf, off, 4 );
            }
            catch ( InterruptedIOException ex )
            {
                // TODO: use the SocketCaps string for this test.
                // timeout
                if ( timeout > 0 && ex.getMessage().equals( "Read timed out" ) )
                {
                    return false;
                }
                // interrupt during read
                m = ex.bytesTransferred;

                // early interrupt.
                if ( m == 0 )
                {
                    Thread.currentThread().interrupt();
                    return false;
                }

                // we have started reading
                interrupt = true;
            }
            finally
            {
                if ( timeout > 0 )
                {
                    try
                    {
                        m_socket.setSoTimeout( 0 );
                    }
                    catch ( IOException ex )
                    {
                        // TODO: ???
                    }
                }
            }


            if ( m == 0 )
            {
                return false;
            }
            if ( m == 4 )
            {
                return true;
            }
            if ( m > 0 )
            {
                // read the remainder of the message
                int r = m;

                while ( r < 4 )
                {
                    try
                    {
                        if ( ( m = m_in_stream.read( buf, off + r, 4 - r ) ) < 0 )
                        {
                            throw new org.omg.CORBA.COMM_FAILURE( "Unexpected end of stream",
                                    IIOPMinorCodes.COMM_FAILURE_EOF,
                                    org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE );
                        }
                    }
                    catch ( InterruptedIOException ex )
                    {
                        m = ex.bytesTransferred;
                        interrupt = true;
                    }

                    r += m;
                }

                if ( interrupt )
                {
                    Thread.currentThread().interrupt();
                }
                return true;
            }
        }
        catch ( final IOException ex )
        {
            throw ExceptionTool.initCause( new COMM_FAILURE(
                    "IOException while reading from input stream (" + ex + ")",
                    IIOPMinorCodes.COMM_FAILURE_IO_EXCEPTION,
                    org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE ), ex );
        }

        // m == -1 so the end of the stream has been reached
        throw new EOFException( "The end of the stream has been reached, no bytes available!" );
    }

    /**
     * Check the IIOP header and return the length.
     *
     * @throws EOFException If header is a CloseConnection message
     * @throws COMM_FAILURE If header is a MsgError message.
     */
    protected int checkGIOPHeader( byte [] buf, int off )
        throws EOFException
    {
        if ( buf[ off + 0 ] != 'G' || buf[ off + 1 ] != 'I'
                || buf[ off + 2 ] != 'O' || buf[ off + 3 ] != 'P'
                || buf[ off + 4 ] != 1 || buf[ off + 5 ] > 2 )
        {
            m_message_error = true;
            throw new org.omg.CORBA.COMM_FAILURE( "Bad magic",
                    IIOPMinorCodes.COMM_FAILURE_BAD_DATA,
                    org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE );
        }

        if ( m_minor_version < buf[ off + 5 ] )
        {
            m_minor_version = buf[ off + 5 ];
        }
        switch ( buf[ off + 7 ] )
        {

        case org.omg.GIOP.MsgType_1_1._CloseConnection:
            m_remote_close = true;
            throw new EOFException( "CloseConnection message received from peer." );

        case org.omg.GIOP.MsgType_1_1._MessageError:
            m_remote_close = true;
            throw new org.omg.CORBA.COMM_FAILURE( "Message Error recieved from remote",
                    IIOPMinorCodes.COMM_FAILURE_BAD_DATA,
                    org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE );
        }

        // FALSE(0): big-endian, TRUE(1): little-endian
        // GIOP_1_0: boolean byte_order;
        // GIOP_1_1: octet flags; // byte_order = flags & 0x01
        boolean swap = ( ( buf[ off + 6 ] & 1 ) == 1 );

        // swap the msg length (already affected by byte_order)
        return ( ( buf[ off + 8 ] & 0xFF ) << ( swap ? 0 : 24 ) )
             | ( ( buf[ off + 9 ] & 0xFF ) << ( swap ? 8 : 16 ) )
             | ( ( buf[ off + 10 ] & 0xFF ) << ( swap ? 16 : 8 ) )
             | ( ( buf[ off + 11 ] & 0xFF ) << ( swap ? 24 : 0 ) );
    }

    /**
     * Read bytes into the array. Can be used but not overriden
     * by subclasses.
     */
    protected final void read( byte [] buf, int off, int len )
    {
        if ( !m_open )
        {
            throw new org.omg.CORBA.COMM_FAILURE( "Transport is closed" );
        }
        try
        {
            int r;
            boolean interrupt = false;

            while ( len > 0 )
            {
                try
                {
                    if ( ( r = m_in_stream.read( buf, off, len ) ) < 0 )
                    {
                        throw new org.omg.CORBA.COMM_FAILURE( "Unexpected end of stream",
                                IIOPMinorCodes.COMM_FAILURE_EOF,
                                org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE );
                    }
                }
                catch ( InterruptedIOException ex )
                {
                    interrupt = true;
                    r = ex.bytesTransferred;
                }

                off += r;
                len -= r;
            }

            if ( interrupt )
            {
                Thread.currentThread().interrupt();
            }
        }
        catch ( IOException ex )
        {
            throw ExceptionTool.initCause( new org.omg.CORBA.COMM_FAILURE(
                    "IOException while reading from input stream",
                    IIOPMinorCodes.COMM_FAILURE_IO_EXCEPTION,
                    org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE ), ex );
        }
    }

    /**
     * Create a storage buffer from the passed buffer, reading extra bytes
     * from the stream.
     */
    protected StorageBuffer readBuffer( byte [] head, int off, int len, int total_len )
    {
        if ( !m_open )
        {
            throw new org.omg.CORBA.COMM_FAILURE( "Transport is closed" );
        }
        try
        {
            return new StorageBuffer( head, off, len, m_in_stream, total_len );
        }
        catch ( final EOFException ex )
        {
            throw ExceptionTool.initCause( new COMM_FAILURE(
                    "Unexpected end of stream (" + ex + ")",
                    IIOPMinorCodes.COMM_FAILURE_EOF,
                    CompletionStatus.COMPLETED_MAYBE ), ex );
        }
        catch ( IOException ex )
        {
            throw ExceptionTool.initCause( new COMM_FAILURE(
                    "IOException while reading from input stream (" + ex + ")",
                    IIOPMinorCodes.COMM_FAILURE_IO_EXCEPTION,
                    CompletionStatus.COMPLETED_MAYBE ), ex );
        }
    }

    /**
     * Check for applicability of channel for carrying messages for the
     * specified address and setup any client transport binding.
     *
     * @param addr the address.
     * @return true if this transport can carry messages for the target. Basic
     *  checks like checking the host and port will already be done, just check
     *  specifics. If false a new channel will be opened for requests.
      */
    public boolean establishAssociation( Address addr )
    {
        return true;
    }

    /**
     * Print out socket information.
     */
    public String toString()
    {
        return "(iiop) " + getConnString();
    }

    /**
     * This returns a string describing the connection endpoints.
     */
    protected String getConnString()
    {
        return m_connection_string;
    }

    private Logger getLogger()
    {
        return m_logger;
    }
}

