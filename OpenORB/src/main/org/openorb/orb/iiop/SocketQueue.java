/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import java.io.EOFException;

import java.util.LinkedList;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.omg.CORBA.OctetSeqHolder;
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INITIALIZE;

import org.openorb.orb.io.StorageBuffer;
import org.openorb.orb.io.BufferSource;
import org.openorb.orb.net.Transport;

import org.openorb.orb.util.Trace;
import org.openorb.util.ExceptionTool;

/**
 *
 * @author Chris Wood
 * @version $Revision: 1.11 $ $Date: 2004/08/12 12:54:24 $
 */
public final class SocketQueue
    implements LogEnabled
{
    SocketQueue( Transport trans )
    {
        m_transport = trans;
        m_open = m_transport.isOpen();
    }

    private Logger m_logger;

    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

    public Logger getLogger()
    {
        return m_logger;
    }

    public static final int MAX_FRAG_SIZE = 120000;

    /**
     * This should be set to match SCRAP_SIZE_DEFAULT
     */
    //private static final int BUFFER_SIZE = 2048;

    private Transport m_transport;
    private org.omg.CORBA.ORB m_orb;

    // sync on this object to change. This is the socket state.
    private IIOPServerChannel m_server_channel = null;
    private IIOPClientChannel m_client_channel = null;
    private boolean m_connection_originator;
    private boolean m_use_odd_even = false;

    private Object m_sync_send = new Object();
    private boolean m_open = false;

    private java.lang.reflect.Constructor m_os_ctor = null;

    private LinkedList m_incoming_queue = new LinkedList();

    private int m_request_id_1 = -1;
    private boolean m_receive_reply_1 = true;

    Transport getTransport()
    {
        return m_transport;
    }

    public String toString()
    {
        return m_transport.toString();
    }

    synchronized void setClientChannel( IIOPClientChannel clientChannel )
    {
        if ( m_client_channel == null )
        {
            m_client_channel = clientChannel;
            if ( m_server_channel == null )
            {
                m_connection_originator = true;
                m_orb = m_client_channel.orb();
                try
                {
                    final Class [] cargs = new Class[] { org.omg.CORBA.ORB.class, boolean.class,
                            org.omg.GIOP.Version.class, BufferSource.class };
                    m_os_ctor = ( ( org.openorb.orb.core.ORB ) m_orb ).getLoader().classConstructor(
                            "iiop.CDRInputStreamClass",
                            "org.openorb.orb.iiop.CDRInputStream", cargs );
                }
                catch ( Exception ex )
                {
                    if ( getLogger().isErrorEnabled() )
                    {
                        getLogger().error( "Unable to create CDROutputStream class.", ex );
                    }
                    throw ExceptionTool.initCause( new INITIALIZE(
                            "Unable to create CDROutputStream class (" + ex + ")" ), ex );
                }
            }
            else
            {
                m_use_odd_even = true;
            }
        }
    }

    synchronized void setServerChannel( IIOPServerChannel serverChannel )
    {
        m_server_channel = serverChannel;
        if ( m_client_channel == null )
        {
            m_connection_originator = false;
            m_orb = m_server_channel.orb();
            try
            {
                final Class [] cargs = new Class[] { org.omg.CORBA.ORB.class, boolean.class,
                        org.omg.GIOP.Version.class, BufferSource.class };
                m_os_ctor = ( ( org.openorb.orb.core.ORB ) m_orb ).getLoader().classConstructor(
                        "iiop.CDRInputStreamClass", "org.openorb.orb.iiop.CDRInputStream", cargs );
            }
            catch ( final Exception ex )
            {
                if ( getLogger().isErrorEnabled() )
                {
                    getLogger().error( "Unable to create CDROutputStream class.", ex );
                }
                throw ExceptionTool.initCause( new INITIALIZE(
                        "Unable to create CDROutputStream class (" + ex + ")" ), ex );
            }
        }
        else
        {
            m_use_odd_even = true;
        }
    }

    public synchronized boolean isOpen()
    {
        return m_transport.isOpen();
    }

    public synchronized void open()
    {
        m_transport.open();
        m_open = true;
    }

    public synchronized void close()
    {
        synchronized ( m_sync_send )
        {
            m_open = false;
        }
        m_transport.close();
    }

    // outgoing queue management.

    public boolean send( StorageBuffer buffer )
    {
        if ( !m_open )
        {
            return false;
        }
        synchronized ( m_sync_send )
        {
            if ( !m_open )
            {
                return false;
            }
            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( Trace.bufferToString( "Sending message", buffer ) );
            }
            m_transport.sendMessage( buffer );
            return true;
        }
    }

    /**
     * tempory vars. here to avoid creating new every time
     */
    private OctetSeqHolder m_tmpBuf = new OctetSeqHolder();

    private IntHolder m_tmpOff = new IntHolder();

    private IntHolder m_tmpLen = new IntHolder();

    public boolean receive( int timeout )
    {
        return process_or_enqueue( true, timeout );
    }

    private boolean process_or_enqueue( boolean process, int timeout )
    {
        int req_id = 0;
        if ( process && !m_incoming_queue.isEmpty() )
        {
            IncommingToProcess rtp = ( IncommingToProcess ) m_incoming_queue.removeFirst();
            switch ( rtp.getMessageType() )
            {

            case org.omg.GIOP.MsgType_1_1._Reply:
            case org.omg.GIOP.MsgType_1_1._LocateReply:
                req_id = m_client_channel.process_reply( rtp.getMinor(), rtp.getInputStream(),
                        rtp.getMessageType(), rtp.getFragmentFollows(), rtp.getSourceBuffer() );
                if ( rtp.getMinor() == 1 )
                {
                    m_request_id_1 = req_id;
                }
                return true;

            case org.omg.GIOP.MsgType_1_1._Request:
            case org.omg.GIOP.MsgType_1_1._LocateRequest:
            case org.omg.GIOP.MsgType_1_1._CancelRequest:
                req_id = m_server_channel.process_request( rtp.getMinor(), rtp.getInputStream(),
                        rtp.getMessageType(), rtp.getSourceBuffer() );
                if ( rtp.getMinor() == 1 )
                {
                    m_request_id_1 = req_id;
                }
                return true;

            case org.omg.GIOP.MsgType_1_1._Fragment:
                if ( rtp.getMinor() == 1 )
                {
                    rtp.setRequestId( m_request_id_1 );
                }
                if ( rtp.getReplyFragment() )
                {
                    m_client_channel.process_fragment( rtp.getRequestId(),
                            rtp.getFragment(), rtp.getFragmentFollows() );
                }
                else
                {
                    m_server_channel.process_fragment( rtp.getRequestId(),
                            rtp.getFragment(), rtp.getFragmentFollows() );
                }
                return true;
            }
        }
        StorageBuffer next_msg = null;
        try
        {
            next_msg = m_transport.recvMessage( timeout );
        }
        catch ( EOFException ex )
        {
            if ( getLogger().isDebugEnabled() && Trace.isMedium() )
            {
                getLogger().debug( "Error receiving message in process_or_enqueue", ex );
            }
            if ( m_client_channel != null )
            {
                m_client_channel.server_pause( ExceptionTool.initCause(
                        new TRANSIENT( 0, CompletionStatus.COMPLETED_NO ), ex ) );
            }
            else
            {
                m_server_channel.close();
            }
            return false;
        }
        catch ( org.omg.CORBA.COMM_FAILURE ex )
        {
            if ( getLogger().isDebugEnabled() && Trace.isMedium() )
            {
                getLogger().debug( "Error receiving message in process_or_enqueue", ex );
            }
            if ( m_use_odd_even ? m_connection_originator : ( m_client_channel != null ) )
            {
                m_client_channel.server_pause( ex );
            }
            else
            {
                m_server_channel.close();
            }
            return false;
        }

        if ( next_msg == null )
        {
            return true;
        }
        if ( getLogger() != null && getLogger().isDebugEnabled()
              && org.openorb.orb.util.Trace.isHigh() )
        {
            getLogger().debug( Trace.bufferToString( "Incoming message", next_msg ) );
        }
        byte minor = -1;
        byte msg_type = -1;
        boolean swap = false;
        boolean fragFollows = false;
        // read header information from buffer.
        m_tmpLen.value = 8;
        int r = next_msg.next( m_tmpBuf, m_tmpOff, m_tmpLen );
        switch ( r )
        {

        case 8:
            // the normal case..
            msg_type = m_tmpBuf.value[ m_tmpOff.value + 7 ];
            swap = ( ( m_tmpBuf.value[ m_tmpOff.value + 6 ] & 1 ) == 1 );
            fragFollows = ( ( m_tmpBuf.value[ m_tmpOff.value + 6 ] & 2 ) == 2 );
            minor = m_tmpBuf.value[ m_tmpOff.value + 5 ];
            break;

        case 7:
            swap = ( ( m_tmpBuf.value[ m_tmpOff.value + 6 ] & 1 ) == 1 );
            fragFollows = ( ( m_tmpBuf.value[ m_tmpOff.value + 6 ] & 2 ) == 2 );
            // fallthrough
        case 6:
            minor = m_tmpBuf.value[ m_tmpOff.value + 5 ];
            // fallthrough
        default:
            // handle shorter reads. This will most likely never occour
            while ( r < 5 )
            {
                m_tmpLen.value = 5 - r;
                r += next_msg.next( m_tmpBuf, m_tmpOff, m_tmpLen );
            }
            while ( r < 8 )
            {
                m_tmpLen.value = 1;
                r += next_msg.next( m_tmpBuf, m_tmpOff, m_tmpLen );
                switch ( r )
                {
                case 6:
                    minor = m_tmpBuf.value[ m_tmpOff.value ];
                    break;

                case 7:
                    swap = ( ( m_tmpBuf.value[ m_tmpOff.value ] & 1 ) == 1 );
                    fragFollows = ( ( m_tmpBuf.value[ m_tmpOff.value ] & 2 ) == 2 );
                    break;

                case 8:
                    msg_type = m_tmpBuf.value[ m_tmpOff.value ];
                    break;
                }
            }
        }

        // handle message fragments.
        if ( msg_type == org.omg.GIOP.MsgType_1_1._Fragment )
        {
            m_tmpLen.value = 4;
            if ( !( next_msg.skip( m_tmpLen ) == 4 ) )
            {
                Trace.signalIllegalCondition( getLogger(),
                        "Unable to read header from buffer." );
            }
            boolean replyFrag = true;
            switch ( minor )
            {
            case 1:
                req_id = m_request_id_1;
                replyFrag = m_receive_reply_1;
                break;

            case 2:
                {
                    // read request ID. This is copied from the CDRInputStream.
                    int got;
                    m_tmpLen.value = 4;
                    if ( ( got = next_msg.next( m_tmpBuf, m_tmpOff, m_tmpLen ) ) == 4 )
                    {
                        // optimize for normal case.
                        req_id = ( ( m_tmpBuf.value[ m_tmpOff.value ] & 0xFF )
                              << ( swap ? 0 : 24 ) )
                              | ( ( m_tmpBuf.value[ m_tmpOff.value + 1 ] & 0xFF )
                              << ( swap ? 8 : 16 ) )
                              | ( ( m_tmpBuf.value[ m_tmpOff.value + 2 ] & 0xFF )
                              << ( swap ? 16 : 8 ) )
                              | ( ( m_tmpBuf.value[ m_tmpOff.value + 3 ] & 0xFF )
                              << ( swap ? 24 : 0 ) );
                    }
                    else
                    {
                        // general case. This will probably never occour.
                        req_id = 0;
                        int shf = swap ? 0 : 24;
                        while ( true )
                        {
                            for ( int i = 0; i < got; ++i )
                            {
                                req_id = req_id | ( ( m_tmpBuf.value[ m_tmpOff.value + i ] & 0xFF )
                                        << shf );
                                shf += swap ? 8 : -8;
                            }
                            if ( swap ? ( shf < 24 ) : ( shf > 0 ) )
                            {
                                got = next_msg.next( m_tmpBuf, m_tmpOff, m_tmpLen );
                                continue;
                            }
                            break;
                        }
                    }
                    replyFrag = m_use_odd_even
                          ? ( ( ( req_id % 2 ) == 0 ) == m_connection_originator )
                          : ( m_client_channel != null );
                }
                break;
            }

            if ( process )
            {
                if ( replyFrag )
                {
                    m_client_channel.process_fragment( req_id, next_msg, fragFollows );
                }
                else
                {
                    m_server_channel.process_fragment( req_id, next_msg, fragFollows );
                }
            }
            else
            {
                m_incoming_queue.addLast( new IncommingToProcess( req_id, fragFollows, replyFrag,
                        next_msg ) );
            }
            return true;
        }

        // setup CDRInputStream
        BufferSource source = new BufferSource( next_msg, !fragFollows );
        if ( fragFollows )
        {
            try
            {
                source.addWaitingForBufferListener( m_waitingForBufferListener );
            }
            catch ( java.util.TooManyListenersException ex )
            {
                getLogger().error( "Unable to handle too many Listeners.", ex );
            }
        }
        CDRInputStream is;
        try
        {
            is = ( CDRInputStream )
                  m_os_ctor.newInstance( new Object[] { m_orb, swap ? Boolean.FALSE
                  : Boolean.TRUE, new org.omg.GIOP.Version( ( byte ) 1, minor ), source} );
            if ( LogEnabled.class.isAssignableFrom( is.getClass() ) )
            {
                is.enableLogging( getLogger().getChildLogger( "is" ) );
            }
        }
        catch ( final Exception ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Unable to create input stream class.", ex );
            }
            throw ExceptionTool.initCause( new INITIALIZE(
                    "Unable to create input stream class (" + ex + ")" ), ex );
        }

        // skip over the buffer length (already dealt with)
        if ( !( is.skip( 4 ) == 4 ) )
        {
            org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                    "Unable to read header from buffer." );
        }

        // look at the message
        switch ( msg_type )
        {
        case org.omg.GIOP.MsgType_1_1._Reply:
        case org.omg.GIOP.MsgType_1_1._LocateReply:
            {
                if ( minor == 1 && fragFollows )
                {
                    m_receive_reply_1 = true;
                }
                if ( process )
                {
                    if ( m_client_channel != null )
                    {
                        req_id = m_client_channel.process_reply( minor, is, msg_type, fragFollows,
                                source );

                        if ( minor == 1 && fragFollows )
                        {
                            m_request_id_1 = req_id;
                        }
                    }
                }
                else
                {
                    m_incoming_queue.addLast( new IncommingToProcess( msg_type, minor, fragFollows,
                            is, source ) );
                }
            }
            break;

        case org.omg.GIOP.MsgType_1_1._Request:
        case org.omg.GIOP.MsgType_1_1._LocateRequest:
        case org.omg.GIOP.MsgType_1_1._CancelRequest:
            {
                if ( minor == 1 && fragFollows )
                {
                    m_receive_reply_1 = false;
                }
                if ( process )
                {
                    if ( m_server_channel != null )
                    {
                        req_id = m_server_channel.process_request( minor, is, msg_type, source );

                        if ( minor == 1 && fragFollows )
                        {
                            m_request_id_1 = req_id;
                        }
                    }
                }
                else
                {
                    m_incoming_queue.addLast( new IncommingToProcess( msg_type, minor, fragFollows,
                            is, source ) );
                }
            }
            break;
        }
        return true;
    }

    private BufferSource.WaitingForBufferListener m_waitingForBufferListener =
            new BufferSource.WaitingForBufferListener()
    {
        public boolean waitForBuffer( BufferSource source )
        {
            process_or_enqueue( false, 0 );
            return true;
        }
    };

    private static class IncommingToProcess
    {
        private byte m_minor;
        private CDRInputStream m_in_stream;
        private byte m_message_type;
        private BufferSource m_source_buffer;
        private boolean m_fragment_follows;
        private int m_request_id;
        private StorageBuffer m_fragment;
        private boolean m_reply_fragment;

        public IncommingToProcess( byte msg_type )
        {
            m_message_type = msg_type;
        }

        public IncommingToProcess( byte msg_type, byte minor, boolean fragFollows,
                                   CDRInputStream is, BufferSource source )
        {
            m_minor = minor;
            m_in_stream = is;
            m_message_type = msg_type;
            m_source_buffer = source;
            m_fragment_follows = fragFollows;
        }

        public IncommingToProcess( int req_id, boolean fragFollows,
                boolean replyFrag, StorageBuffer frag )
        {
            m_message_type = org.omg.GIOP.MsgType_1_1._Fragment;
            m_request_id = req_id;
            m_fragment_follows = fragFollows;
            m_reply_fragment = replyFrag;
            m_fragment = frag;
        }

        public byte getMinor()
        {
            return m_minor;
        }

        public byte getMessageType()
        {
            return m_message_type;
        }

        public boolean getFragmentFollows()
        {
            return m_fragment_follows;
        }

        public StorageBuffer getFragment()
        {
            return m_fragment;
        }

        public int getRequestId()
        {
            return m_request_id;
        }

        public void setRequestId( int request_id )
        {
            m_request_id = request_id;
        }

        public BufferSource getSourceBuffer()
        {
            return m_source_buffer;
        }

        public CDRInputStream getInputStream()
        {
            return m_in_stream;
        }

        public boolean getReplyFragment()
        {
            return m_reply_fragment;
        }
    }
}

