/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import org.apache.avalon.framework.logger.Logger;

import org.omg.IIOP.ListenPoint;

import org.openorb.orb.net.ServerProtocol;
import org.openorb.orb.net.ServerManager;
import org.openorb.orb.net.Transport;

import org.openorb.orb.util.Trace;

/**
 * This class provides the implementation of the IIOP protocol on the server side.
 *
 * @author Chris Wood
 * @version $Revision: 1.9 $ $Date: 2004/02/19 06:35:34 $
 */
public class IIOPServerProtocol
    implements ServerProtocol
{
    private ServerManager m_server_manager;

    private ListenPoint m_primary_endpoint;

    private org.omg.IOP.Codec m_codec;

    private org.omg.CORBA.ORB m_orb;

    private IIOPClientProtocol m_client_protocol = null;

    private Object m_sync_state = new Object();

    private int m_state = STATE_CLOSED;

    private IIOPTransportServerInitializer m_server_transport_init;

    private Logger m_logger = null;

    /**
     * Create new IIOPServerProtocol
     *
     * @param serverManager the server manager instance.
     * @param codecFactory codec factory for iiop codecs.
     * @param trans transport initializer.
     * @throws org.omg.CORBA.INITIALIZE if problem occoured during startup.
     */
    public IIOPServerProtocol( ServerManager serverManager,
                               CDRCodecFactory codecFactory, IIOPTransportServerInitializer trans )
    {
        m_server_manager = serverManager;
        m_orb = m_server_manager.orb();
        m_logger = ( ( org.openorb.orb.core.ORB ) orb() ).getLogger();

        try
        {
            m_codec = codecFactory.create_codec(
                    new org.omg.IOP.Encoding( org.omg.IOP.ENCODING_CDR_ENCAPS.value,
                    ( byte ) 1, ( byte ) 2 ) );
        }
        catch ( org.omg.IOP.CodecFactoryPackage.UnknownEncoding ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Could not find codec.", ex );
            }
        }

        m_server_transport_init = trans;

        m_state = STATE_CLOSED;
        m_primary_endpoint = m_server_transport_init.getPrimaryEndpoint();

        if ( getLogger().isDebugEnabled() && Trace.isHigh() )
        {
            getLogger().debug( this + " created" );
        }
    }

    /**
     * Return a stringified representation of the information in this class.
     */
    public String toString()
    {
        return "ServerProtocol: " + m_server_transport_init.toString();
    }

    /**
     */
    public void setClientProtocol( IIOPClientProtocol clientProtocol )
    {
        m_client_protocol = clientProtocol;
    }

    /**
     * Return the orb instance.
     */
    public org.omg.CORBA.ORB orb()
    {
        return m_orb;
    }

    /**
     * Return the server mnanager instance.
     */
    public ServerManager getServerManager()
    {
        return m_server_manager;
    }

    /**
     */
    public boolean servesAddress( String addr, int port )
    {
        if ( ( m_primary_endpoint.port & 0xFFFF ) == port
              && addr.equals( m_primary_endpoint.host ) )
        {
            return true;
        }
        ListenPoint [] biDirEndpoints = m_server_transport_init.getBiDirEndpoints();

        if ( biDirEndpoints == null )
        {
            return false;
        }
        for ( int i = 0; i < biDirEndpoints.length; ++i )
        {
            if ( ( biDirEndpoints[ i ].port & 0xFFFF ) == port
                  && addr.equals( biDirEndpoints[ i ].host ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the state.
     */
    public int state()
    {
        return m_state;
    }

    /**
     * Move to the listening state. If in the closed state this wil result in
     * the protocol re-registering itself with the ServerManager. Returns true
     * if state changed.
     */
    public boolean open()
    {
        synchronized ( m_sync_state )
        {
            switch ( m_state )
            {

            case STATE_PAUSED:
                m_state = STATE_LISTENING;
                m_sync_state.notifyAll();
                break;

            case STATE_CLOSED:

                try
                {
                    m_server_transport_init.open();
                }
                catch ( org.omg.CORBA.SystemException ex )
                {
                    return false;
                }

                m_primary_endpoint = m_server_transport_init.getPrimaryEndpoint();
                m_state = STATE_LISTENING;
                m_server_manager.protocol_listening( this );
            }

            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( this + " listening" );
            }
            return true;
        }
    }

    /**
     * Move to the paused state. This is only valid in the LISTENING state.
     * returns true if state is now PAUSED.
     */
    public boolean pause()
    {
        synchronized ( m_sync_state )
        {
            if ( m_state != STATE_CLOSED )
            {
                m_server_manager.protocol_not_listening( this, true );
                m_state = STATE_PAUSED;
                if ( getLogger().isDebugEnabled() && Trace.isHigh() )
                {
                    getLogger().debug( this + " paused" );
                }
                return true;
            }

            return false;
        }
    }

    /**
     * Stop listening, refuse all incoming connections.
     */
    public void close()
    {
        synchronized ( m_sync_state )
        {
            if ( m_state == STATE_CLOSED )
            {
                return;
            }
            m_state = STATE_CLOSED;

            m_server_transport_init.close();

            /*
             * Server transport must be closed or else listening threads
             * will not be interrupted!
             */
            m_server_manager.protocol_not_listening( this, false );

            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( this + " closed" );
            }
        }
    }

    /**
     * Listen for a single connection. If a connection is available this will
     * create and register a new server socket, otherwise it will return once
     * the timeout has expired.
     */
    public void listen( int timeout )
    {
        Transport transport = null;

        if ( m_state != STATE_LISTENING )
        {
            return;
        }
        try
        {
            transport = m_server_transport_init.accept( timeout );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                    "Fatal error while accepting connection: " + ex );
        }

        if ( transport != null )
        {
            new IIOPServerChannel( m_server_manager, transport, m_client_protocol, m_codec );
        }
    }

    /**
     * Donate a thread for listening. This function returns when interrupt
     * is called on the thread or the protocol is closed.
     */
    public void run_listen()
    {
        Thread curr = Thread.currentThread();

        while ( !curr.isInterrupted() && m_state == STATE_LISTENING )
        {
            listen( 0 );
        }
    }

    /**
     * Construct a tagged profile from parts. This is used by the ServerManager.
     * If the profile should not be included in an IOR this may return null.
     */
    public org.omg.IOP.TaggedProfile create_profile( int profile_tag,
            org.openorb.orb.pi.ComponentSet component_set, byte [] object_key )
    {
        if ( profile_tag != org.omg.IOP.TAG_INTERNET_IOP.value )
        {
            return null;
        }
        org.omg.IIOP.ProfileBody_1_1 profile_body = new org.omg.IIOP.ProfileBody_1_1();

        profile_body.iiop_version = new org.omg.IIOP.Version( ( byte ) 1, ( byte ) 2 );

        profile_body.host = m_primary_endpoint.host;

        profile_body.port = m_primary_endpoint.port;

        profile_body.object_key = object_key;

        profile_body.components = component_set.getComponents( profile_tag );

        org.omg.CORBA.Any any = m_orb.create_any();

        org.omg.IIOP.ProfileBody_1_1Helper.insert( any, profile_body );

        try
        {
            byte[] buf = m_codec.encode_value( any );

            if ( buf != null )
            {
                return new org.omg.IOP.TaggedProfile( profile_tag, buf );
            }
            else
            {
                return null;
            }
        }
        catch ( org.omg.IOP.CodecPackage.InvalidTypeForEncoding ex )
        {
            org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                "Invalid type for encoding." );
        }
        // never reached
        return null;
    }

    /**
     * Return the logger instance.
     */
    private Logger getLogger()
    {
        return m_logger;
    }
}

