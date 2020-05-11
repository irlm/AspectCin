/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import java.util.Iterator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Random;

import org.openorb.util.ExceptionTool;
import org.openorb.util.ConfigUtils;

import org.openorb.util.logger.LoggerTeam;

import org.apache.avalon.framework.logger.Logger;

import org.openorb.orb.config.ORBLoader;
import org.openorb.orb.config.Property;

import org.openorb.orb.util.Trace;

import org.omg.CORBA.INITIALIZE;

/**
 * A socket factory that creates pre-configured sockets.
 *
 * @author Richard G Clark
 * @author Michael Rumpf
 * @version $Revision: 1.13 $ $Date: 2004/05/18 16:40:49 $
 */
public final class ConfiguredSocketFactory implements SocketFactory
{
    private static final int MAX_PORT = 0xFFFF;
    private static final int INVALID_PORT = -1;

    private final Random m_random = new Random();
    private final Map m_addressMap = new HashMap();

    private final LoggerTeam m_logger;
    private final Logger m_diagnosticsLogger;

    private final boolean m_noDelay;
    private final boolean m_keepAlive;
    private final int m_backlogQueueLength;
    private final int m_maxSocketTimeout;
    private final int m_minSocketTimeout;
    private final int m_overrideSocketTimeout;
    private final int m_sendBufferSize;
    private final int m_receiveBufferSize;
    private final int m_localPortMin;
    private final int m_localPortMax;
    private final boolean m_localPortConstrained;

    private final SocketStreamDecorationStrategy m_streamDecorationStrategy;

    /**
     * Constructs a new socket factory.
     *
     * @param logger the logger team to used
     * @param loader the orb loader to be used
     * @param prefix the property name prefix to use, e.g. "iiop"
     * @throws INITIALIZE if problems occured during setup
     */
    public ConfiguredSocketFactory( final LoggerTeam logger,
            final ORBLoader loader, final String prefix )
    {
        m_logger = logger;
        m_diagnosticsLogger = getDiagnosticsLogger( logger );

        final String keepAliveName = prefixName( prefix, "keepAlive" );
        final String noDelayName = prefixName( prefix, "noDelay" );
        final String clientNoDelayName = prefixName( prefix, "clientNoDelay" );
        final String serverNoDelayName = prefixName( prefix, "serverNoDelay" );
        final String serverBacklogQueueLengthName
                = prefixName( prefix, "serverBacklogQueueLength" );
        final String serverMaxSocketAcceptTimeoutName
                = prefixName( prefix, "serverMaxSocketAcceptTimeout" );
        final String serverMinSocketAcceptTimeoutName
                = prefixName( prefix, "serverMinSocketAcceptTimeout" );
        final String serverOverrideSocketTimeoutName
                = prefixName( prefix, "serverOverrideSocketTimeout" );

        final String sendBufferSizeName = prefixName( prefix, "sendBufferSize" );
        final String receiveBufferSizeName = prefixName( prefix, "receiveBufferSize" );
        final String clientPortMinName = prefixName( prefix, "clientPortMin" );
        final String clientPortMaxName = prefixName( prefix, "clientPortMax" );

        final Property noDelayProperty = loader.getProperty( noDelayName );
        final Property clientNoDelayProperty = loader.getProperty( clientNoDelayName );
        final Property serverNoDelayProperty = loader.getProperty( serverNoDelayName );


        if ( null != clientNoDelayProperty )
        {
            logger.warn( "Property [" + clientNoDelayName
                    + "] has been deprecated. Please used [" + noDelayName
                    + "] instead." );
        }

        if ( null != serverNoDelayProperty )
        {
            logger.warn( "Property [" + serverNoDelayName
                    + "] has been deprecated. Please used [" + noDelayName
                    + "] instead." );
        }

        if ( null != noDelayProperty )
        {
            m_noDelay = noDelayProperty.getBooleanValue();

            if ( null != clientNoDelayProperty )
            {
                logOverride( clientNoDelayName, noDelayName );
            }

            if ( null != serverNoDelayProperty )
            {
                logOverride( serverNoDelayName, noDelayName );
            }
        }
        else
        {
            final boolean clientNoDelay = ( null == clientNoDelayProperty ) ? true
                    : clientNoDelayProperty.getBooleanValue();

            final boolean serverNoDelay = ( null == serverNoDelayProperty ) ? true
                    : serverNoDelayProperty.getBooleanValue();

            if ( clientNoDelay != serverNoDelay )
            {
                logger.warn( "Properties [" + clientNoDelayName + "] and ["
                        + serverNoDelayName + "] differ, defaulting to [true]" );

                m_noDelay = true;
            }
            else
            {
                m_noDelay = clientNoDelay;
            }
        }

        m_keepAlive = loader.getBooleanProperty( keepAliveName , false );

        m_backlogQueueLength
                = loader.getIntProperty( serverBacklogQueueLengthName, 50 );

        m_maxSocketTimeout
                = loader.getIntProperty( serverMaxSocketAcceptTimeoutName, 250 );
        m_minSocketTimeout
                = loader.getIntProperty( serverMinSocketAcceptTimeoutName, 0 );
        m_overrideSocketTimeout
                = loader.getIntProperty( serverOverrideSocketTimeoutName, 250 );


        m_sendBufferSize = loader.getIntProperty( sendBufferSizeName, 0 );
        m_receiveBufferSize = loader.getIntProperty( receiveBufferSizeName, 0 );



        m_localPortMin = loader.getIntProperty( clientPortMinName, INVALID_PORT );
        m_localPortMax = loader.getIntProperty( clientPortMaxName, INVALID_PORT );

        if ( isPortInvalid( m_localPortMin ) )
        {
            logger.warn( "Property [" + clientPortMinName + "]=["
                    + m_localPortMin + "] is invalid." );

            m_localPortConstrained = false;
        }
        else if ( isPortInvalid( m_localPortMax ) )
        {
            logger.warn( "Property [" + clientPortMaxName + "]=["
                    + m_localPortMax + "] is invalid." );

            m_localPortConstrained = false;
        }
        else if ( m_localPortMax < m_localPortMin )
        {
            logger.warn( "Properties [" + clientPortMinName + "]=["
                    + m_localPortMin + "] and [" + clientPortMaxName + "]=["
                    + m_localPortMax + "] are invalid." );

            m_localPortConstrained = false;
        }
        else
        {
            m_localPortConstrained = ( ( INVALID_PORT != m_localPortMin )
                && ( INVALID_PORT != m_localPortMax ) );
        }

        try
        {
            m_streamDecorationStrategy
                    = createStreamDecorationStrategy( logger, loader, prefix );
        }
        catch ( final INITIALIZE e )
        {
            throw e;
        }
        catch ( final Exception e )
        {
            throw ExceptionTool.initCause( new INITIALIZE(
                    "Problems creating stream decoration strategy" ), e );
        }

        try
        {
            configureAddressMap( loader, prefix );
        }
        catch ( final INITIALIZE e )
        {
            throw e;
        }
        catch ( final Exception e )
        {
            throw ExceptionTool.initCause(
                    new INITIALIZE( "Problems configuring address map" ), e );
        }

        logConstruction( getDiagnosticsLogger() );
    }

    private static boolean isPortInvalid( final int port )
    {
        return ( 0 == port ) || ( port < INVALID_PORT ) || ( MAX_PORT < port );
    }

    /**
     * A helper method for logging configuration overrides.
     *
     * @param name the name of the property being overridden
     * @param overridingName the name of the overriding property
     */
    private void logOverride( final String name, final String overridingName )
    {
        if ( getLogger().isWarnEnabled() )
        {
            getLogger().warn( "Property [" + name + "] overridden by property ["
                    + overridingName + "]" );
        }
    }

    /**
     * Returns the diagnostics logger of the specified logger team.
     *
     * @param logger the logger team to use
     * @return a logger
     */
    private static Logger getDiagnosticsLogger( final LoggerTeam logger )
    {
        return logger.getMember( LoggerTeam.StandardTags.DIAGNOSTIC_LOGGER_TAG );
    }

    /**
     * Prefixes the specifed name with an optional prefix. If a prefix is
     * specified then a "." is added between the prefix and the name.
     *
     * @param prefix the optional prefix to use
     * @param name the name to be prefixed
     * @return a prefixed name
     */
    private static String prefixName( final String prefix, final String name )
    {
        return ConfigUtils.prefixName( prefix, name );
    }

    /**
     * A utility method for creating the stream decoration strategy.
     *
     * @param loader the configuration source
     * @param prefix the property name prefix to use
     */
    private static SocketStreamDecorationStrategy createStreamDecorationStrategy(
            final LoggerTeam logger, final ORBLoader loader, final String prefix )
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException
    {
        final String namePrefix = prefixName( prefix, "stream-decoration" );
        final Map sortedFactories = new TreeMap();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        for ( Iterator it = loader.properties( namePrefix ); it.hasNext(); )
        {
            final Property property = ( Property ) it.next();
            final Class factoryClass = classLoader.loadClass( property.getValue() );

            sortedFactories.put( property.getName(), factoryClass );
        }

        if ( sortedFactories.isEmpty() )
        {
            sortedFactories.put( "1",
                    PriorityBoostingSocketStreamDecorationStrategy.Factory.class );

            sortedFactories.put( "2",
                    BufferingSocketStreamDecorationStrategy.Factory.class );

            sortedFactories.put( "3",
                    LegacySocketStreamDecorationStrategy.Factory.class );
        }

        SocketStreamDecorationStrategy masterStrategy = null;

        for ( Iterator it = sortedFactories.values().iterator(); it.hasNext(); )
        {
            final Class factoryClass = ( Class ) it.next();

            final SocketStreamDecorationStrategy.Factory factory
                    = ( SocketStreamDecorationStrategy.Factory ) factoryClass.newInstance();

            final SocketStreamDecorationStrategy strategy
                    = factory.create( logger, loader, prefix );

            if ( null != strategy )
            {
                if ( null == masterStrategy )
                {
                    masterStrategy = strategy;
                }
                else
                {
                    masterStrategy = new CompositeSocketStreamDecorationStrategy(
                            masterStrategy, strategy );
                }
            }
        }

        if ( null == masterStrategy )
        {
            return NullSocketStreamDecorationStrategy.getInstance();
        }

        return masterStrategy;
    }

    /**
     * A utility method for configuring the address mapping feature.
     *
     * @param loader the configuration source
     * @param prefix the property name prefix to use
     * @throws IOException if there were problems creating network resources
     */
    private void configureAddressMap( final ORBLoader loader, final String prefix )
            throws IOException
    {
        final String namePrefix = prefixName( prefix, "address-mapping" );
        final Iterator it = loader.properties( namePrefix );

        while ( it.hasNext() )
        {
            final Property mapping = ( Property ) it.next();

            addToAddressMap( mapping.getName().substring( namePrefix.length() + 1 ),
                    mapping.getValue() );
        }
    }

    /**
     * Adds a address mapping.
     *
     * @param keyAddress the original endpoint address pattern
     * @param valueAddress the endpoint adresss mapping pattern
     * @throws IOException if there were problems creating network resources
     */
    private void addToAddressMap( final String keyAddress, final String valueAddress )
            throws IOException
    {
        final InetAddress keyHost = getHost( keyAddress );
        final int keyPort = getPort( keyAddress );

        final InetAddress valueHost = getHost( valueAddress );
        final int valuePort = getPort( valueAddress );

        final Object key = ( 0 == keyPort ) ? ( Object ) keyHost
                : ( Object ) new InetSocketAddress( keyHost, keyPort );

        final Object value = ( 0 == valuePort ) ? ( Object ) valueHost
                : ( Object ) new InetSocketAddress( valueHost, valuePort );

        m_addressMap.put( key, value );
    }

    /**
     * Returns the <code>InetAddress</code> for the given pattern.
     *
     * @param address the endpoint address pattern
     * @return a <code>InetAddress</code>
     * @throws IOException if there were problems creating network resources
     */
    private static InetAddress getHost( final String address )
            throws IOException
    {
        final int i = address.indexOf( ":" );

        if ( -1 == i )
        {
            return InetAddress.getByName( address );
        }

        return InetAddress.getByName( address.substring( 0 , i ) );
    }

    /**
     * Returns the port number, if availavle, for the given pattern.
     *
     * @param address the endpoint address pattern
     * @return the port number, 0 if no port was specified
     */
    private static int getPort( final String address )
    {
        final int i = address.indexOf( ":" );

        if ( -1 == i )
        {
            return 0;
        }

        final String portString = address.substring( i + 1 );

        if ( 0 == portString.length() )
        {
            return 0;
        }


        final int port = Integer.parseInt( portString );

        if ( ( port <= 0 ) || ( 0xFFFF < port ) )
        {
            throw new IllegalArgumentException( "Port [" + port
                    + "] of address [" + address
                    + "] is outside of range 1-" + 0xFFFF );
        }
        return port;
    }

    /**
     * Logs construction details with the specified logger.
     *
     * @param logger the logger to be used
     */
    private void logConstruction( final Logger logger )
    {
        logger.fatalError( "Constructed [ConfiguredSocketFactory]" );

        if ( !logger.isErrorEnabled() )
        {
            return;
        }

        logger.error( "Start details..." );
        logger.error( "keepAlive=[" + m_keepAlive + "]" );
        logger.error( "noDelay=[" + m_noDelay + "]" );
        logger.error( "sendBufferSize=[" + m_sendBufferSize + "]" );
        logger.error( "receiveBufferSize=[" + m_receiveBufferSize + "]" );
        logger.error( "localPortMin=[" + m_localPortMin + "]" );
        logger.error( "localPortMax=[" + m_localPortMax + "]" );
        logger.error( "localPortConstrained=[" + m_localPortConstrained + "]" );

        logger.error( "addressMap.size()=[" + m_addressMap.size() + "]" );

        final Iterator it = m_addressMap.entrySet().iterator();

        while ( it.hasNext() )
        {
            final Map.Entry entry = ( Map.Entry ) it.next();
            logger.error( "addressMap[" + entry.getKey() + "]=["
                    + entry.getValue() + "]" );
        }

        logger.error( "backlogQueueLength=[" + m_backlogQueueLength + "]" );
        logger.error( "maxSocketTimeout=[" + m_maxSocketTimeout + "]" );
        logger.error( "minSocketTimeout=[" + m_minSocketTimeout + "]" );
        logger.error( "overrideSocketTimeout=[" + m_overrideSocketTimeout + "]" );

        logger.error( "...End details." );
    }

    /**
     * Returns the main logger.
     *
     * @return a <code>LoggerTeam</code>
     */
    private LoggerTeam getLogger()
    {
        return m_logger;
    }

    /**
     * Returns the diagnostics logger.
     *
     * @return a <code>Logger</code>
     */
    private Logger getDiagnosticsLogger()
    {
        return m_diagnosticsLogger;
    }

    /**
     * @see SocketFactory#createServerSocket(InetAddress,int)
     */
    public ServerSocket createServerSocket( final InetAddress host, final int port )
            throws IOException
    {
        return new ConfiguredServerSocket( port, m_backlogQueueLength, host );
    }

    /**
     * @see SocketFactory#createSocket(InetAddress,int)
     */
    public Socket createSocket( final InetAddress host, final int port )
            throws IOException
    {
        final InetSocketAddress address = createAddress( host, port );

        final Socket socket = isLocalPortConstrained()
                ? createConstrainedSocket( address )
                : new ConfiguredSocket( address );

        return socket;
    }

    /**
     * Remap a target address
     */
    private InetSocketAddress createAddress( final InetAddress host, final int port )
    {
        final InetSocketAddress address = new InetSocketAddress( host, port );

        Object mapping = m_addressMap.get( address );

        if ( null == mapping )
        {
            // try host mapping
            mapping = m_addressMap.get( host );
        }

        if ( null != mapping )
        {
            final InetSocketAddress hostPortMapping;

            if ( mapping instanceof InetSocketAddress )
            {
                hostPortMapping = ( InetSocketAddress ) mapping;
            }
            else if ( mapping instanceof InetAddress )
            {
                hostPortMapping = new InetSocketAddress( ( InetAddress ) mapping, port );
            }
            else
            {
                throw Trace.signalIllegalCondition( getLogger(),
                        "Address map corrupted with mapping [" + mapping + "]" );
            }

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Mapped [" + address + "]->["
                        + hostPortMapping + "]" );
            }

            return hostPortMapping;
        }

        return address;
    }

    private boolean isLocalPortConstrained()
    {
        return m_localPortConstrained;
    }

    /**
     * Create a socket with a local withing a specified range.
     * To satisfy the local firewall constraints the local outgoing port is
     * specified. In that case we got to use it to open the socket connection.
     * The outgoing local port is specified in the system property or orb.properties.
     */
    private Socket createConstrainedSocket( final InetSocketAddress address )
            throws IOException
    {
        final InetAddress localHost = InetAddress.getLocalHost();

        final int portCount = m_localPortMax - m_localPortMin + 1;
        final int startPort = m_localPortMin + m_random.nextInt( portCount );

        for ( int i = 0; i < portCount; i++ )
        {
            try
            {
                return new ConfiguredSocket( address, localHost,
                    ( startPort + i ) % portCount );
            }
            catch ( final NoRouteToHostException e )
            {
                // exit early if cannot find route to host
                throw e;
            }
            catch ( final IOException ex )
            {
                //do nothing here
            }
        }
        throw new IOException( "No port available in specified range" );
    }

    /**
     * A configured server socket.
     */
    private final class ConfiguredServerSocket extends ServerSocket
    {
        private final Object m_optionSync = new byte[0];
        private final Object m_closeSync = new byte[0];
        private boolean m_closed;
        private int m_currentSocketTimeout;

        public ConfiguredServerSocket( final int port, final int backlog,
                final InetAddress bindAddress ) throws IOException
        {
            super( port, backlog, bindAddress );
        }

        public Socket accept() throws IOException
        {
            assertNotClosed();

            final ConfiguredSocket socket = new ConfiguredSocket();

            implAccept( socket );

            socket.postConnectConfig();

            return socket;
        }

        public void setSoTimeout( int timeout ) throws SocketException
        {
            synchronized ( m_optionSync )
            {
                timeout = ( ( m_minSocketTimeout < timeout )
                        && ( timeout < m_maxSocketTimeout ) ) ? timeout
                        : m_overrideSocketTimeout;

                if ( timeout == m_currentSocketTimeout )
                {
                    // no change
                    return;
                }

                super.setSoTimeout( timeout );
                m_currentSocketTimeout = timeout;
            }
        }

        public void close() throws IOException
        {
            super.close();

            // handle close called in constructor
            if ( null == m_closeSync )
            {
                m_closed = true;
                return;
            }

            synchronized ( m_closeSync )
            {
                m_closed = true;
            }
        }

        private void assertNotClosed() throws SocketException
        {
            synchronized ( m_closeSync )
            {
                if ( m_closed )
                {
                    throw new SocketException( "Socket is closed" );
                }
            }
        }

    }

    /**
     * A configured socket.
     */
    private final class ConfiguredSocket extends Socket
    {
        private final Object m_closeSync = new byte[0];
        private boolean m_closed;
        private final Object m_outLock = new byte[0];
        private OutputStream m_out;
        private boolean m_outputShutdown;
        private boolean m_shuttingDownOutput;
        private final Object m_inLock = new byte[0];
        private InputStream m_in;

        /**
         * Note that <code>postConnectConfig()</code> isn't called.
         */
        public ConfiguredSocket() throws IOException
        {
            super();
            preConnectConfig();
        }

        public ConfiguredSocket( final InetSocketAddress address ) throws IOException
        {
            super( address.getAddress(), address.getPort() );
            preConnectConfig();
            postConnectConfig();
        }

        public ConfiguredSocket( final InetSocketAddress address,
                final InetSocketAddress localAddress ) throws IOException
        {
            super( address.getAddress(), address.getPort(),
                    localAddress.getAddress(), localAddress.getPort() );
            preConnectConfig();
            postConnectConfig();
        }

        public ConfiguredSocket( final InetSocketAddress address,
                final InetAddress localAddress, final int localPort ) throws IOException
        {
            super( address.getAddress(), address.getPort(),
                    localAddress, localPort );
            preConnectConfig();
            postConnectConfig();
        }

        protected void preConnectConfig() throws IOException
        {
            try
            {
                if ( 0 < m_sendBufferSize )
                {
                    setSendBufferSize( m_sendBufferSize );
                }
                if ( 0 < m_receiveBufferSize )
                {
                    setReceiveBufferSize( m_receiveBufferSize );
                }
            }
            catch ( final IOException e )
            {
                close();
                throw e;
            }
        }

        protected void postConnectConfig() throws IOException
        {
            try
            {
                setTcpNoDelay( m_noDelay );
                setKeepAlive( m_keepAlive );
            }
            catch ( final IOException e )
            {
                close();
                throw e;
            }
        }

        public void shutdownOutput() throws IOException
        {
            synchronized ( m_closeSync )
            {
                if ( m_closed )
                {
                    // use the underlying implementation to
                    super.shutdownOutput();
                    return;
                }
            }

            synchronized ( m_outLock )
            {
                if ( !m_outputShutdown )
                {
                    if ( null != m_out )
                    {
                        m_shuttingDownOutput = true;
                        m_out.flush();
                        m_out.close();
                        m_shuttingDownOutput = false;
                    }

                    m_outputShutdown = true;
                }
            }

            super.shutdownOutput();
        }

        private void interceptedOutputStreamClose( final OutputStream out )
                throws IOException
        {
            synchronized ( m_outLock )
            {
                if ( !m_shuttingDownOutput )
                {
                    out.close();
                }
            }
        }

        public void close() throws IOException
        {
            // Handle close during construction
            if ( null == m_closeSync )
            {
                super.close();
                return;
            }


            synchronized ( m_closeSync )
            {
                if ( m_closed )
                {
                    return;
                }
                m_closed = true;
            }

            synchronized ( m_outLock )
            {
                if ( !m_outputShutdown )
                {
                    if ( null != m_out )
                    {
                        m_out.flush();
                        m_out.close();
                    }
                }
            }

            synchronized ( m_inLock )
            {
                if ( null != m_in )
                {
                    m_in.close();
                }
            }

            super.close();
        }

        public OutputStream getOutputStream() throws IOException
        {
            // call the super class method so that socket state is checked
            final OutputStream parent = super.getOutputStream();

            synchronized ( m_outLock )
            {
                if ( null != m_out )
                {
                    return m_out;
                }

                m_out = m_streamDecorationStrategy.decorate( this, parent );

                return m_out;
            }
        }

        public InputStream getInputStream() throws IOException
        {
            // call the super class method so that socket state is checked
            final InputStream parent = super.getInputStream();

            synchronized ( m_inLock )
            {
                if ( null != m_in )
                {
                    return m_in;
                }

                m_in = m_streamDecorationStrategy.decorate( this, parent );

                return m_in;
            }
        }

        private final class CloseInterceptOutputStream extends OutputStream
        {
            private final OutputStream m_out;

            private CloseInterceptOutputStream( final OutputStream out )
            {
                m_out = out;
            }

            public void write( final int b ) throws IOException
            {
                m_out.write( b );
            }

            public void write( final byte[] buf ) throws IOException
            {
                m_out.write( buf );
            }

            public void write( final byte[] buf, final int offset,
                    final int len ) throws IOException
            {
                m_out.write( buf, offset, len );
            }

            public void flush() throws IOException
            {
                m_out.flush();
            }

            public void close() throws IOException
            {
                interceptedOutputStreamClose( m_out );
            }
        }
    }


    /**
     * A temporary work around for JDK1.3.
     */
    private final class InetSocketAddress
    {
        private final InetAddress m_address;
        private final int m_port;
        private String m_string;

        public InetSocketAddress( final InetAddress address, final int port )
        {
            m_address = address;
            m_port = port;
        }

        public InetAddress getAddress()
        {
            return m_address;
        }

        public int getPort()
        {
            return m_port;
        }

        public int hashCode()
        {
            return m_address.hashCode() ^ getPort();
        }

        public boolean equals( final Object obj )
        {
            return ( obj == this ) || ( ( obj instanceof InetSocketAddress )
                    && equals( ( InetSocketAddress ) obj ) );
        }

        private boolean equals( final InetSocketAddress rhs )
        {
            return getAddress().equals( rhs.getAddress() )
                    && ( getPort() == rhs.getPort() );
        }

        public synchronized String toString()
        {
            if ( null == m_string )
            {
                m_string = getAddress() + ":" + getPort();
            }
            return m_string;
        }
    }
}

