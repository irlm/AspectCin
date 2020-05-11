/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import java.io.IOException;
import java.io.InterruptedIOException;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.COMM_FAILURE;

import org.omg.IIOP.ListenPoint;

import org.omg.PortableInterceptor.ORBInitInfo;

import org.openorb.orb.Initializer;

import org.openorb.orb.net.SocketFactory;
import org.openorb.orb.net.Transport;
import org.openorb.orb.net.TransportServerInitializer;

import org.openorb.orb.pi.FeatureInitInfo;

import org.openorb.orb.util.Trace;

import org.openorb.util.ExceptionTool;

/**
 * Interface for creating sockets.
 *
 * @author Chris Wood
 * @version $Revision: 1.11 $ $Date: 2004/02/10 21:02:49 $
 */
public class IIOPTransportServerInitializer
    extends AbstractLogEnabled
    implements TransportServerInitializer, Initializer
{
    private final ListenPoint m_primary_endpoint = new ListenPoint();
    private final ListenPoint [] m_bidir_endpoints = new ListenPoint[] { m_primary_endpoint };

    private SocketFactory m_socketFactory;

    private InetAddress m_host;

    private ServerSocket m_svr_socket;
    private volatile boolean m_closed = true;

    /**
     * Any replacement classes must implement an identical constructor.
     */
    public IIOPTransportServerInitializer()
    {
    }

    public String getName()
    {
        return "iiop-server-init";
    }

    /**
     * Initialize method.
     *
     * @param orbinfo
     * @param featureinfo
     */
    public void init( final ORBInitInfo orbinfo, final FeatureInitInfo featureinfo )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "init" );
        }

        org.openorb.orb.config.ORBLoader loader = featureinfo.getLoader();

        m_socketFactory = ( SocketFactory ) featureinfo.getFeature( "IIOP.SocketFactory" );

        if ( null == m_socketFactory )
        {
            throw new INITIALIZE( "Feature [IIOP.SocketFactory] not set" );
        }

        m_primary_endpoint.host = loader.getStringProperty( "iiop.hostname", "" );

        if ( m_primary_endpoint.host.length() == 0 )
        {
            try
            {
                String publishIP = loader.getStringProperty( "iiop.publishIP", "auto" );

                if ( publishIP.equalsIgnoreCase( "auto" ) )
                {
                    m_primary_endpoint.host = InetAddress.getByName(
                            InetAddress.getLocalHost().getHostAddress() ).getHostName();

                    if ( m_primary_endpoint.host.indexOf( '.' ) < 0 )
                    {
                        m_primary_endpoint.host = InetAddress.getLocalHost().getHostAddress();
                    }
                }
                else if ( publishIP.equalsIgnoreCase( "true" ) )
                {
                    m_primary_endpoint.host = InetAddress.getLocalHost().getHostAddress();
                }
                else
                {
                    m_primary_endpoint.host = InetAddress.getByName(
                            InetAddress.getLocalHost().getHostAddress() ).getHostName();
                }
            }
            catch ( final java.net.UnknownHostException ex )
            {
                throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                        "Unable to find hostname for local host (" + ex + ")",
                        0, org.omg.CORBA.CompletionStatus.COMPLETED_NO ), ex );
            }
        }

        int port = loader.getIntProperty( "iiop.port", 0 );

        if ( port > 0xFFFF || port < 0 )
        {
            throw new org.omg.CORBA.INITIALIZE(
                    "Value for server port " + port + " is out of range" );
        }
        m_primary_endpoint.port = ( short ) port;

        try
        {
            // 0.0.0.0 means that no specific interface on a multi-homed host
            // has been specified, i.e. the primary should be used...
            m_host = InetAddress.getByName( loader.getStringProperty(
                    "iiop.listenAddress", "0.0.0.0" ) );
        }
        catch ( final java.net.UnknownHostException ex )
        {
            throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                    "Unable to find address for local listen port (" + ex + ")",
                    0, org.omg.CORBA.CompletionStatus.COMPLETED_NO ), ex );
        }
    }

    /**
     * Start listening for incoming connections. Idempotent.
     *
     * @throws org.omg.CORBA.COMM_FAILURE If unable to listen. This will result
     *  in server shutdown.
     * @throws org.omg.CORBA.TRANSIENT If unable to listen, and try again later.
     */
    public void open()
    {
        if ( m_svr_socket != null )
        {
            return;
        }
        open_port();
    }

    private void open_port()
    {
        try
        {
            int port = ( m_primary_endpoint.port & 0xFFFF );
            m_svr_socket = m_socketFactory.createServerSocket( m_host, port );
        }
        catch ( final IOException ex )
        {
            getLogger().error( "Unable to listen on " + svrString() + " (" + ex + ").", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.COMM_FAILURE(
                    "Unable to listen on " + svrString() + " (" + ex + ")" ), ex );
        }
        catch ( final SecurityException ex )
        {
            getLogger().error( "Access denied for " + svrString() + ".", ex );

            throw ExceptionTool.initCause( new org.omg.CORBA.NO_PERMISSION(
                    "Access denied for " + svrString() + " (" + ex + ")" ), ex );
        }

        m_closed = false;

        if ( m_primary_endpoint.port == 0 )
        {
            m_primary_endpoint.port = ( short ) m_svr_socket.getLocalPort();
        }
    }

    /**
     * Get the primary endpoint published in the IIOP profile.
     */
    public ListenPoint getPrimaryEndpoint()
    {
        return m_primary_endpoint;
    }

    /**
     * Get the list of endpoints allowed for bidirectional use. These will
     * be transmitted in BI_DIR_IIOP service contexts. If empty or null then
     * bidirectional IIOP will be disabled.
     */
    public ListenPoint [] getBiDirEndpoints()
    {
        return m_bidir_endpoints;
    }

    /**
     * Get the listen host.
     */
    protected InetAddress getListenHost()
    {
        return m_host;
    }

    /**
     * Stop listening for a connection. Idempotent.
     */
    public void close()
    {
        if ( m_closed || m_svr_socket == null )
        {
            return;
        }
        m_closed = true;
        try
        {
            m_svr_socket.close();
        }
        catch ( IOException ex )
        {
            // ignore and return null
        }
        m_svr_socket = null;
    }

    /**
     * Is is the transport open?
     */
    public boolean isOpen()
    {
        return !m_closed;
    }

    /**
     * Listen for an incoming connection.
     *
     * @return transport for new connection, or null if no connection received.
     * @throws org.omg.CORBA.COMM_FAILURE If some permanent comms problem occours
     *  this will result in server shutdown.
     */
    public Transport accept( final int timeout )
    {
        try
        {
            if ( m_closed )
            {
                return null;
            }

            m_svr_socket.setSoTimeout( timeout );

            try
            {
                final Socket sock = m_svr_socket.accept();

                return new IIOPTransport( sock, m_primary_endpoint.port & 0xFFFF,
                        getLogger() );

            }
            catch ( final InterruptedIOException ex )
            {
                // Solaris workaround
                if ( "operation interrupted".equals( ex.getMessage() ) )
                {
                    Thread.currentThread().interrupt();
                }

                return null;

            }
            catch ( final SocketException ex )
            {
                // On AIX a SocketException is thrown when the accept() thread is interrupted
                return null;
            }
        }
        catch ( final IOException ex )
        {
            if ( m_closed )
            {
                return null;
            }

            getLogger().error( "IOException during accept.", ex );

            throw ExceptionTool.initCause( new COMM_FAILURE(), ex );
        }
    }

    public String toString()
    {
        return "(iiop) " + svrString();
    }

    protected String svrString()
    {
        return "" + m_host + ":" + ( m_primary_endpoint.port & 0xFFFF );
    }
}

