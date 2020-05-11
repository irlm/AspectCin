/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import java.util.Map;
import org.openorb.util.WeakValueHashMap;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.openorb.orb.net.Address;
import org.openorb.orb.net.ClientBinding;
import org.openorb.orb.net.ClientChannel;
import org.openorb.orb.net.ClientManager;
import org.openorb.orb.net.ClientProtocol;
import org.openorb.orb.net.RebindChannelException;
import org.openorb.orb.net.ServerManager;
import org.openorb.orb.net.Transport;
import org.openorb.orb.net.TransportClientInitializer;

import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.CONV_FRAME.CodeSetComponent;
import org.omg.IIOP.ListenPoint;

import org.openorb.util.ExceptionTool;

import org.apache.avalon.framework.logger.Logger;

/**
 * Implements the {@link org.openorb.orb.net.ClientProtocol} interface for IIOP.
 *
 * @author Chris Wood
 * @version $Revision: 1.5 $ $Date: 2004/02/10 21:02:49 $
 */
public class IIOPClientProtocol
    implements ClientProtocol
{
    private ClientManager m_client_manager;

    private org.omg.CORBA.ORB m_orb;

    private org.omg.IOP.Codec m_codec;

    private IIOPServerProtocol m_server_protocol = null;

    private ServerManager m_server_manager = null;

    private Map m_channels = new WeakValueHashMap();

    private CodeSetComponentInfo m_codeset_component_info;

    private TransportClientInitializer m_transport_client_init;

    private org.omg.IOP.ServiceContext m_bidir_service_ctxt;

    private Logger m_logger;


    /** Creates new IIOPClientProtocol */
    public IIOPClientProtocol( ClientManager clientManager, CDRCodecFactory codec_factory,
            CodeSetComponentInfo codesetInfo, TransportClientInitializer tpInit,
            ListenPoint [] biDirListenPoints )
    {
        try
        {
            m_codec = codec_factory.create_codec( new org.omg.IOP.Encoding(
                    org.omg.IOP.ENCODING_CDR_ENCAPS.value, ( byte ) 1, ( byte ) 2 ) );
        }
        catch ( final org.omg.IOP.CodecFactoryPackage.UnknownEncoding ex )
        {
            getLogger().error( "Could not find codec", ex );
        }

        m_client_manager = clientManager;
        m_orb = m_client_manager.orb();

        m_codeset_component_info = codesetInfo;

        m_transport_client_init = tpInit;

        if ( biDirListenPoints != null )
        {
            m_bidir_service_ctxt = null;

            try
            {
                org.omg.IIOP.BiDirIIOPServiceContext scd =
                        new org.omg.IIOP.BiDirIIOPServiceContext( biDirListenPoints );

                org.omg.CORBA.Any any = m_orb.create_any();
                org.omg.IIOP.BiDirIIOPServiceContextHelper.insert( any, scd );
                byte [] data = m_codec.encode_value( any );

                if ( data != null )
                {
                    m_bidir_service_ctxt =
                            new org.omg.IOP.ServiceContext( org.omg.IOP.BI_DIR_IIOP.value, data );
                }
            }
            catch ( final org.omg.CORBA.UserException ex )
            {
                getLogger().error( "UserException occured during init of IIOPClientProtocol.", ex );

                throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                      "UserException occured during init of IIOPClientProtocol ("
                      + ex + ")" ), ex );
            }
        }
    }

    /**
     * Sets the server protocol.
     *
     * @param protocol The server protocol.
     */
    public void setServerProtocol( IIOPServerProtocol protocol )
    {
        m_server_protocol = protocol;
        m_server_manager = protocol.getServerManager();
    }

    /**
     * Get the codec for this channel.
     *
     * @return The codec.
     */
    public org.omg.IOP.Codec getCodec()
    {
        return m_codec;
    }

    /**
     * An orb reference.
     */
    public org.omg.CORBA.ORB orb()
    {
        return m_orb;
    }

    /**
     * Get a reference to the orb's client manager.
     */
    public ClientManager getClientManager()
    {
        return m_client_manager;
    }

    /**
     * Create addresses from component in IOR
     */
    public Address [] createAddresses( org.omg.GIOP.IORAddressingInfo address )
    {
        return m_transport_client_init.establishTransports(
                IIOPAddress.get_addresses( m_codec, address.ior,
                address.selected_profile_index, getLogger() ) );
    }

    /**
     * Returns a Collection of ClientBinding objects, prioritised at the per-profile
     * (inter-component) level. The client addresses in the returned bindings
     * should return identical results for each of the addressing disposition
     * types.
     */
    public ClientBinding createBinding( Address addr )
    {
        if ( !( addr instanceof IIOPAddress ) )
        {
            return null;
        }
        IIOPAddress address = ( IIOPAddress ) addr;


        ClientBinding binding;

        synchronized ( m_channels )
        {
            try
            {
                IIOPClientChannel channel = findCreateChannel( address, null );

                if ( m_server_manager == null || m_server_protocol == null
                      || !m_server_protocol.servesAddress( address.get_hostname(),
                      address.get_port() ) )
                {
                    binding = new ClientBinding( address, channel );
                }
                else
                {
                    binding = new ClientBinding( address, channel, m_server_manager );
                }
            }
            catch ( final org.omg.CORBA.SystemException ex )
            {
                binding = new ClientBinding( address, ex );
            }
        }

        return binding;
    }

    /**
     * Find a channel. Lock on channels must be owned.
     */
    protected IIOPClientChannel findCreateChannel( IIOPAddress address, IIOPClientChannel replace )
    {
        InetAddress host = address.get_host();

        if ( host == null )
        {
            throw new org.omg.CORBA.COMM_FAILURE( "Host not found",
                    IIOPMinorCodes.COMM_FAILURE_HOST_NOT_FOUND,
                    org.omg.CORBA.CompletionStatus.COMPLETED_NO );
        }
        LookupKey key = new LookupKey( address );

        IIOPClientChannel channel = ( IIOPClientChannel ) m_channels.get( key );

        if ( channel == null || replace == channel )
        {
            if ( address.get_port() == 0 )
            {
                throw new org.omg.CORBA.NO_PERMISSION( "Port number published in IOR is 0.",
                       IIOPMinorCodes.NO_PERMISSION_INVALID_PORT,
                       org.omg.CORBA.CompletionStatus.COMPLETED_NO );
            }
            // select codesets
            int tcsc, tcsw;

            CodeSetComponentInfo ci = address.getCodesetComponentInfo();

            if ( ci == null )
            {
                tcsc = 0;
                tcsw = 0;
            }
            else
            {
                tcsc = selectCodeset(
                        m_codeset_component_info.ForCharData, ci.ForCharData, true );
                tcsw = selectCodeset(
                        m_codeset_component_info.ForWcharData, ci.ForWcharData, false );
            }

            // create the transport
            Transport transport = address.createTransport();

            transport.establishAssociation( address );

            // create the channel
            channel = new IIOPClientChannel( this, transport, m_server_protocol, tcsc, tcsw );

            m_channels.put( new HostPortPair( host, address.get_port(),
                    tcsc, tcsw, transport ), channel );
        }

        return channel;
    }

    private static boolean checkCodeset( int tcs, CodeSetComponent comp )
    {
        // check for fallbacks
        if ( tcs == 0x05010001 /* UTF-8 */ || tcs == 0x00010109 /* UTF-16 */ )
        {
            return true;
        }
        // compare codesets
        if ( comp.native_code_set != tcs )
        {
            for ( int i = 0; i < comp.conversion_code_sets.length; ++i )
            {
                if ( comp.conversion_code_sets[ i ] == tcs )
                {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    /**
     * Select a conversion code set.
     * This algorithm is taken from 13.10.2.6 of the corba spec.
     */
    private static int selectCodeset( CodeSetComponent local,
            CodeSetComponent server, boolean singleByte )
    {
        if ( local.native_code_set == server.native_code_set )
        {
            return local.native_code_set;
        }
        for ( int i = 0; i < local.conversion_code_sets.length; ++i )
        {
            if ( server.native_code_set == local.conversion_code_sets[ i ] )
            {
                return server.native_code_set;
            }
        }
        for ( int i = 0; i < server.conversion_code_sets.length; ++i )
        {
            if ( local.native_code_set == server.conversion_code_sets[ i ] )
            {
                return local.native_code_set;
            }
        }
        for ( int i = 0; i < server.conversion_code_sets.length; ++i )
        {
            for ( int j = 0; j < local.conversion_code_sets.length; ++j )
            {
                if ( server.conversion_code_sets[ i ] == local.conversion_code_sets[ j ] )
                {
                    return server.conversion_code_sets[ i ];
                }
            }
        }
        // use fallback, UTF-8 for sbcs, UTF-16 for mbcs
        int fallback = singleByte ? 0x05010001 : 0x00010109;

        if ( CodeSet.compatible( local.native_code_set, server.native_code_set ) )
        {
            return fallback;
        }
        for ( int i = 0; i < local.conversion_code_sets.length; ++i )
        {
            if ( CodeSet.compatible( server.native_code_set, local.conversion_code_sets[ i ] ) )
            {
                return fallback;
            }
        }
        for ( int i = 0; i < server.conversion_code_sets.length; ++i )
        {
            if ( CodeSet.compatible( local.native_code_set, server.conversion_code_sets[ i ] ) )
            {
                return fallback;
            }
        }
        for ( int i = 0; i < server.conversion_code_sets.length; ++i )
        {
            for ( int j = 0; j < local.conversion_code_sets.length; ++j )
            {
                if ( CodeSet.compatible( server.conversion_code_sets[ i ],
                        local.conversion_code_sets[ j ] ) )
                {
                    return fallback;
                }
            }
        }
        // no compatible codesets found.
        throw new org.omg.CORBA.CODESET_INCOMPATIBLE( 0,
                org.omg.CORBA.CompletionStatus.COMPLETED_NO );
    }

    /**
     * This is called by a delegated client channel when it's peer channel closes
     * and a new channel must be created to replace the current one.
     */
    void rebindBidirDelegate( IIOPClientChannel deleg, IIOPAddress addr )
        throws RebindChannelException
    {
        IIOPClientChannel channel;

        synchronized ( m_channels )
        {
            channel = findCreateChannel( addr, deleg );
        }

        throw new RebindChannelException( channel );
    }

    /**
     * Get bidirectional service context, or null if no bidir is allowed
     */
    org.omg.IOP.ServiceContext getBiDirSC()
    {
        return m_bidir_service_ctxt;
    }

    /**
     * This is called by a server channel when a BiDirService context arrives.
     */
    IIOPClientChannel createBidirDelegate( IIOPServerChannel parent,
            org.omg.IIOP.ListenPoint [] listen_points )
    {
        IIOPClientChannel deleg;

        synchronized ( m_channels )
        {
            HostPortPair [] keys = new HostPortPair[ listen_points.length ];
            int j = 0;
            InetAddress addr;

            for ( int i = 0; i < listen_points.length; ++i )
            {
                try
                {
                    addr = InetAddress.getByName( listen_points[ i ].host );
                }
                catch ( final UnknownHostException ex )
                {
                    continue;
                }

                // with null for the transport this acts as a lookup key.
                HostPortPair key = new HostPortPair( addr, listen_points[ i ].port & 0xFFFF,
                                                     parent.getTCSC(), parent.getTCSW(), null );

                IIOPClientChannel chan = ( IIOPClientChannel ) m_channels.get( key );

                if ( chan != null )
                {
                    switch ( chan.state() )
                    {

                    case ClientChannel.STATE_CONNECTED:
                        return null;

                    case ClientChannel.STATE_PAUSED:

                        if ( !chan.isDelegated() )
                        {
                            return null;
                        }
                    case ClientChannel.STATE_CLOSED:
                        chan = null;

                        break;
                    }
                }

                if ( chan == null )
                {
                    keys[ j++ ] = new HostPortPair( addr, listen_points[ i ].port & 0xFFFF,
                            parent.getTCSC(), parent.getTCSW(),
                            parent.getSocketQueue().getTransport() );
                }
            }

            if ( j == 0 )
            {
                return null;
            }
            deleg = new IIOPClientChannel( this, parent );

            // delegate channels are not registered with the client manager
            // as their work and state management is performed via the parent
            // channel.
            for ( int i = 0; i < j; ++i )
            {
                if ( keys[ i ] != null )
                {
                    m_channels.put( keys[ i ], deleg );
                }
            }
        }

        return deleg;
    }

    private static class LookupKey
    {
        LookupKey( IIOPAddress addr )
        {
            m_host = addr.get_host();
            m_port = addr.get_port();
            m_hash = m_host.hashCode() ^ m_port;

            m_address = addr;
            m_codeset_component_info = addr.getCodesetComponentInfo();
        }

        private int m_hash;

        private InetAddress m_host;
        private int m_port;

        private CodeSetComponentInfo m_codeset_component_info;
        private IIOPAddress m_address;

        public int hashCode()
        {
            return m_hash;
        }

        public boolean equals( Object obj )
        {
            if ( !( obj instanceof HostPortPair ) )
            {
                return false;
            }
            HostPortPair hp2 = ( HostPortPair ) obj;

            if ( hp2.m_hash != m_hash || hp2.m_port != m_port || !hp2.m_host.equals( m_host ) )
            {
                return false;
            }
            if ( m_address == null )
            {
                return true;
            }
            if ( m_codeset_component_info != null
                  && ( !checkCodeset( hp2.m_tcsc, m_codeset_component_info.ForCharData )
                  || !checkCodeset( hp2.m_tcsw, m_codeset_component_info.ForWcharData ) ) )
            {
                return false;
            }
            return hp2.m_transport.establishAssociation( m_address );
        }
    }

    private static class HostPortPair
    {
        public HostPortPair( InetAddress host, int port, int tcsc, int tcsw, Transport transport )
        {
            m_host = host;
            m_port = port;
            m_hash = host.hashCode() ^ port;

            m_tcsc = tcsc;
            m_tcsw = tcsw;
            m_transport = transport;
        }

        private int m_hash;

        private InetAddress m_host;
        private int m_port;

        private int m_tcsc = -1;
        private int m_tcsw = -1;
        private Transport m_transport;

        public int hashCode()
        {
            return m_hash;
        }

        public boolean equals( Object obj )
        {
            if ( obj instanceof LookupKey )
            {
                return obj.equals( this );
            }
            HostPortPair hp2 = ( HostPortPair ) obj;

            if ( m_transport != null && hp2.m_transport != null )
            {
                return this == hp2;
            }
            return hp2.m_hash == m_hash && hp2.m_port == m_port
                   && hp2.m_host.equals( m_host ) && m_tcsc == hp2.m_tcsc
                   && m_tcsw == hp2.m_tcsw;
        }
    }

    private Logger getLogger()
    {
        if ( null == m_logger )
        {
            m_logger = ( ( org.openorb.orb.core.ORBSingleton ) orb() ).getLogger();
        }
        return m_logger;
    }
}

