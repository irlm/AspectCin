/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;

import org.openorb.orb.io.BufferSource;

import org.openorb.orb.net.AbstractServerRequest;
import org.openorb.orb.net.ServerManager;
import org.openorb.orb.net.Transport;

import org.openorb.util.ExceptionTool;
import org.openorb.orb.util.Trace;

/**
 * This class represents a client request.
 *
 * @author Unknown
 */
public class IIOPServerRequest
    extends AbstractServerRequest
{
    /**
     * Channel this request has been received on.
     */
    private IIOPServerChannel m_channel;

    /**
     * GIOP version of this request.
     */
    private org.omg.GIOP.Version m_version;

    /**
     * used by IIOPServerChannel for 1.2 requests.
     */
    private BufferSource m_request_source;


    /**
     * Constructor.
     * @param svrmgr The server manager associated with this request.
     * @param channel Channel on which this request has been received on.
     * @param request_id The request id of this request.
     * @param argument_stream ???
     * @param object_id The object id of this request.
     * @param operation The name of the operation to execute.
     * @param sync_scope The synchronization scope (See Messaging spec.)
     * @param request_service_contexts An array of service contexts. associated
     * with this request.
     * @param version The GIOP version of this request.
     */
    public IIOPServerRequest( ServerManager svrmgr, IIOPServerChannel channel,
                       int request_id, org.omg.CORBA.portable.InputStream argument_stream,
                       byte [] object_id, String operation, byte sync_scope,
                       org.omg.IOP.ServiceContext[] request_service_contexts,
                       org.omg.GIOP.Version version )
    {
        super( svrmgr, channel, request_id, argument_stream, object_id,
                operation, sync_scope, request_service_contexts );
        m_channel = channel;
        m_version = version;
    }

    /**
     * Constructor.
     * @param svrmgr The server manager associated with this request.
     * @param channel Channel on which this request has been received on.
     * @param request_id The request id of this request.
     * @param argument_stream ???
     * @param version The GIOP version of this request.
     */
    public IIOPServerRequest( ServerManager svrmgr, IIOPServerChannel channel,
                       int request_id, org.omg.CORBA.portable.InputStream argument_stream,
                       org.omg.GIOP.Version version )
    {
        super( svrmgr, channel, request_id, argument_stream );
        m_channel = channel;
        m_version = version;
    }

    /**
     * Constructor.
     * @param svrmgr The server manager associated with this request.
     * @param channel Channel on which this request has been received on.
     * @param request_id The request id of this request.
     * @param object_id The object id of this request.
     * @param version The GIOP version of this request.
     */
    public IIOPServerRequest( ServerManager svrmgr, IIOPServerChannel channel,
                       int request_id, byte [] object_id, org.omg.GIOP.Version version )
    {
        super( svrmgr, channel, request_id, object_id );
        m_channel = channel;
        m_version = version;
    }

    /**
     * Constructor.
     * @param svrmgr The server manager associated with this request.
     * @param channel Channel on which this request has been received on.
     * @param request_id The request id of this request.
     * @param version The GIOP version of this request.
     */
    public IIOPServerRequest( ServerManager svrmgr, IIOPServerChannel channel,
                        int request_id, org.omg.GIOP.Version version )
    {
        super( svrmgr, channel, request_id );
        m_channel = channel;
        m_version = version;
    }

    /**
     * Return the GIOP version.
     */
    public org.omg.GIOP.Version version()
    {
        return m_version;
    }

    /**
     * Set the request source member.
     *
     * @param request_source The source buffer of a request.
     */
    public void setRequestSource( BufferSource request_source )
    {
        m_request_source = request_source;
    }

    /**
     * Return the request source member.
     *
     * @return The source buffer of a request.
     */
    public BufferSource getRequestSource()
    {
        return m_request_source;
    }

    /**
     * Return the underlying transport.
     * This is necessary for the SSL extension in order to get the SSLServerSocket.
     * The SSLServerSocket has the certificates connected and the CSIv2 layer needs
     * the distinguished name from the first client certificate when mutual
     * authentication is done by the SSL transport.
     *
     * @return An instance of type {@link org.openorb.orb.net.Transport}.
     */
    public Transport getTransport()
    {
        return ( ( IIOPServerChannel ) channel() ).getSocketQueue().getTransport();
    }

    /**
     * Send a system exception result. Note that a failed locate request will
     * always result in a system exception, which this function is free to
     * convert into a failed locate reply. This may throw a system
     * exception indicating a transport problem.
     */
    protected void marshal_system_exception( String repo_id, org.omg.CORBA.SystemException ex )
    {
        if (  getLogger().isDebugEnabled() && Trace.isHigh() )
        {
            // for debugging unexpected exceptions
            getLogger().debug( "Marshal SystemException", ex );
        }

        if ( is_locate() )
        {
            marshal_locate_reply( false );
        }
        else
        {
            CDROutputStream os = m_channel.create_reply_stream(
                    this, org.omg.GIOP.ReplyStatusType_1_2._SYSTEM_EXCEPTION );
            os.write_string( repo_id );
            os.write_ulong( ex.minor );
            os.write_ulong( ex.completed.value() );
            complete_reply( os );
        }
    }

    /**
     * Send a forward request result. This may throw a system
     * exception indicating a transport problem.
     */
    protected void marshal_forward_request( org.omg.CORBA.Object target, boolean permanent )
    {
        CDROutputStream os;

        if ( is_locate() )
        {
            if ( m_version.minor < ( byte ) 2 || !permanent )
            {
                os = m_channel.create_locate_reply_stream( this,
                        org.omg.GIOP.LocateStatusType_1_2._OBJECT_FORWARD );
            }
            else
            {
                os = m_channel.create_locate_reply_stream( this,
                         org.omg.GIOP.LocateStatusType_1_2._OBJECT_FORWARD_PERM );
            }
        }
        else
        {
            if ( m_version.minor < ( byte ) 2 || !permanent )
            {
                os = m_channel.create_reply_stream( this,
                        org.omg.GIOP.ReplyStatusType_1_2._LOCATION_FORWARD );
            }
            else
            {
                os = m_channel.create_reply_stream( this,
                        org.omg.GIOP.ReplyStatusType_1_2._LOCATION_FORWARD_PERM );
            }
        }

        os.write_Object( target );
        complete_reply( os );
    }

    /**
     * Reply to a locate request. This argument to this function will be true
     * when called from this class, however marshal_system_exception may convert
     * a system exception response into a locate failure. This may throw a system
     * exception indicating a transport problem.
     */
    protected void marshal_locate_reply( boolean object_is_here )
    {
        CDROutputStream os;

        if ( object_is_here )
        {
            os = m_channel.create_locate_reply_stream( this,
                    org.omg.GIOP.LocateStatusType_1_2._OBJECT_HERE );
        }
        else
        {
            os = m_channel.create_locate_reply_stream( this,
                    org.omg.GIOP.LocateStatusType_1_2._UNKNOWN_OBJECT );
        }
        complete_reply( os );
    }

    /**
     * Create a stream for marshaling a successful response. This is paired
     * with a call to complete_marshal. The returned stream may throw a system
     * exception at any time to indicate transport problems.
     */
    protected org.omg.CORBA.portable.OutputStream begin_marshal_reply()
    {
        return m_channel.create_reply_stream( this,
                org.omg.GIOP.ReplyStatusType_1_2._NO_EXCEPTION );
    }

    /**
     * Create a stream for marshaling a user exception response. This is paired
     * with a call to complete_marshal. The returned stream may throw a system
     * exception at any time to indicate transport problems.
     */
    protected org.omg.CORBA.portable.OutputStream begin_marshal_user_exception()
    {
        return m_channel.create_reply_stream( this,
                org.omg.GIOP.ReplyStatusType_1_2._USER_EXCEPTION );
    }

    /**
     * Complete the marshaling process. Paired with a call to begin_marshal_* .
     * This may throw a system exception to indicate transport problems.
     */
    protected void complete_reply( org.omg.CORBA.portable.OutputStream os )
    {
        try
        {
            os.close();
        }
        catch ( final java.io.IOException ex )
        {
            getLogger().error( "IOException closing output stream.", ex );

            throw ExceptionTool.initCause( new COMM_FAILURE( 0,
                    CompletionStatus.COMPLETED_YES ), ex );
        }
    }

    /**
     * Release any resources associated with the request. This is called when the
     * complete state is entered.
     */
    protected void release_request()
    {
        m_channel.release_request( this );
    }
}

