/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import java.io.IOException;

import org.openorb.orb.io.BufferSource;

import org.openorb.orb.net.AbstractClientRequest;
import org.openorb.orb.net.Address;

import org.openorb.orb.core.SystemExceptionHelper;
import org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE;
import org.omg.Messaging.SyncScopePolicy;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.CompletionStatus;

/**
 *
 * @author Chris Wood
 * @version $Revision: 1.7 $ $Date: 2004/02/19 07:21:31 $
 */
class IIOPClientRequest extends AbstractClientRequest
{
    private static final short REPLY_STATUS_UNSET = Short.MIN_VALUE;

    // request state
    private IIOPClientChannel m_channel;
    private boolean m_response_expected;
    private String m_operation;
    private short m_sync_scope;

    // interceptor stuff
    private org.openorb.orb.pi.ClientManager m_client_manager = null;
    private org.openorb.orb.pi.RequestCallback m_callback = null;

    // synchronize access to all the nonvolitile state variables (below here)
    private Object m_sync_state = new Object();

    private int m_state = STATE_CREATED;

    private org.omg.CORBA_2_3.portable.OutputStream m_request_stream;

    // reply state.
    private short m_reply_status = REPLY_STATUS_UNSET;
    private org.omg.IOP.ServiceContext [] m_reply_service_contexts;

    private org.omg.CORBA_2_3.portable.InputStream m_response_stream;
    private boolean m_response_arrived = false;
    private boolean m_last_response_arrived = false;

    private org.omg.CORBA.Object m_forward_reference;
    private org.omg.IOP.IOR m_forward_reference_ior;

    private org.omg.CORBA.SystemException m_received_exception;
    private org.omg.CORBA.Any m_received_exception_any = null;
    private String m_received_exception_id;

    // reply source.
    private BufferSource m_reply_source = null;

    /**
     * Inciated if a call to reply_status() is in progress.
     */
    private boolean m_replyStatusInProgress;

    /**
     * Creates a request
     */
    IIOPClientRequest( int req_id, org.omg.CORBA.Object target, Address address,
                       IIOPClientChannel channel, String operation,
                       boolean response_expected )
    {
        super( req_id, target, address, channel );

        m_channel = channel;
        m_operation = operation;
        m_response_expected = response_expected;

        if ( m_response_expected )
        {
            m_sync_scope = org.omg.Messaging.SYNC_WITH_TARGET.value;
        }
        else
        {
            m_sync_scope = org.omg.Messaging.SYNC_WITH_SERVER.value;

            if ( requestPolicyExists( SYNC_SCOPE_POLICY_TYPE.value ) )
            {
                try
                {
                    final SyncScopePolicy pol = ( SyncScopePolicy )
                            get_request_policy( SYNC_SCOPE_POLICY_TYPE.value );

                    if ( null != pol )
                    {
                        m_sync_scope = pol.synchronization();
                    }
                }
                catch ( org.omg.CORBA.INV_POLICY ex )
                {
                    // Do nothing, use the default value instead
                }
            }
        }

        m_client_manager = ( org.openorb.orb.pi.ClientManager )
                ( ( org.openorb.orb.core.ORB ) orb() ).getFeature( "ClientInterceptorManager" );

        if ( m_client_manager != null )
        {
            m_callback = new IIOPRequestCallback();
        }
    }

    /**
     * Create a locate_request.
     */
    IIOPClientRequest( int req_id, org.omg.CORBA.Object target, Address address,
                       IIOPClientChannel channel )
    {
        super( req_id, target, address, channel );

        m_channel = channel;
        m_operation = null;
        m_response_expected = true;
        m_sync_scope = -1;
    }

    /**
     * Current request state.
     */
    public int state()
    {
        return m_state;
    }

    /**
     * This returns true if this request is a standard request.
     *
     * While in the CREATED state it is valid to call any client request
     * info operations which would be valid in the client side
     * interception points send_request. The send_request interception
     * points are called while in this state.
     */
    public boolean is_request()
    {
        return ( m_operation != null );
    }

    /**
     * This returns true if this request is a locate request.
     *
     * Client side interceptors are not called for locate requests.
     *
     * ClientRequestInfo operations: arguments, exceptions, contexts,
     * operation_context, result, get_request_service_context,
     * get_reply_service_context, add_request_service_context are not valid.
     *
     * operation returns the empty string "", response_expected returns true.
     *
     * Note the extra value for reply_status, UNKNOWN_OBJECT.
     */
    public boolean is_locate()
    {
        return ( m_operation == null );
    }

    /**
     * Request is a poll.
     *
     * While in the CREATED state it is valid to call any client request
     * info operations which would be valid in the client side
     * interception points send_poll. The send_poll interception
     * points are called while in this state.
     */
    public boolean is_poll()
    {
        return false;
    }

    public short sync_scope()
    {
        return m_sync_scope;
    }

    public boolean response_expected()
    {
        return m_response_expected;
    }

    public String operation()
    {
        return ( m_operation == null ) ? "" : m_operation;
    }

    /**
     * Cancel the request with the specified system exception reply. This is
     * valid in any state apart from COMPLETE. This may result in cancel messages
     * being sent to the server. If this is successful state changes to
     * COMPLETED/SYSTEM_EXCEPTION. If the provided exception's status is set
     * to null the exception thown will set the exception status according to
     * the request's state.
     *
     * @return true if the request is succesfully canceled.
     */
    public boolean cancel( org.omg.CORBA.SystemException ex )
    {
        synchronized ( m_sync_state )
        {
            if ( m_state == STATE_COMPLETE )
            {
                return false;
            }
            switch ( m_state )
            {

            case STATE_MARSHAL:
                m_reply_status = org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value;
                m_state = STATE_COMPLETE;
                ex.completed = CompletionStatus.COMPLETED_NO;
                handle_system_exception( ex );

                if ( m_client_manager != null )
                {
                    m_client_manager.receive_exception( this, m_callback );
                    m_client_manager = null;
                }

                if ( m_request_stream instanceof CDROutputStream )
                {
                    // send the cancel message through the stream.
                    try
                    {
                        ( ( CDROutputStream ) m_request_stream ).cancel( ex );
                    }
                    catch ( org.omg.CORBA.SystemException ex1 )
                    {
                        // ignored.
                    }

                }
                else
                {
                    // cancel directly. This will result in sending a cancel
                    // message, we don't know if a fragment has been sent.
                    m_channel.cancel_request( this, true );
                }

                break;

            case STATE_WAITING:

                if ( m_reply_source != null )
                {
                    ex.completed = CompletionStatus.COMPLETED_YES;
                    m_reply_source.setException( ex );
                    break;
                }

                m_state = STATE_COMPLETE;

                if ( m_channel.cancel_request( this, true ) )
                {
                    ex.completed = CompletionStatus.COMPLETED_MAYBE;
                }
                else
                {
                    ex.completed = CompletionStatus.COMPLETED_NO;
                }
                handle_system_exception( ex );

                if ( m_client_manager != null )
                {
                    m_client_manager.receive_exception( this, m_callback );
                    m_client_manager = null;
                }

                m_sync_state.notifyAll();
                break;

            case STATE_UNMARSHAL:
                ex.completed = CompletionStatus.COMPLETED_YES;
                m_reply_source.setException( ex );
                break;
            }

            return true;
        }
    }

    /**
     * Begin marshalling arguments. Valid in CREATED state. State changes to
     * MARSHAL. If no arguments are required for marshalling this can enter
     * the MARSHAL state and return null, this occours for locate requests and
     * polls only. This may also return null if entering
     * COMPLETE/SYSTEM_EXCEPTION/COMPLETED_NO, COMPLETE/LOCATION_FORWARD or
     * COMPLETE/LOCATION_FORWARD_PERMINENT due to communication problems or
     * client side interceptors.
     */
    public org.omg.CORBA.portable.OutputStream begin_marshal()
    {
        synchronized ( m_sync_state )
        {
            switch ( m_state )
            {

            case STATE_CREATED:
                break;

            case STATE_MARSHAL:

            case STATE_WAITING:

            case STATE_UNMARSHAL:
                throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );

            case STATE_COMPLETE:
                return null;

            default:
                org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                        "Invalid state of the state machine." );
            }

            if ( m_client_manager != null )
            {
                m_client_manager.send_request( this, m_callback );

                if ( m_reply_status != REPLY_STATUS_UNSET )
                {
                    m_state = STATE_COMPLETE;
                    // no messages sent, no need to send cancel message.
                    m_channel.cancel_request( this, false );
                    return null;
                }
            }

            try
            {
                m_request_stream = m_channel.begin_marshal( this );
            }
            catch ( org.omg.CORBA.SystemException ex )
            {
                m_state = STATE_COMPLETE;
                ex.completed = CompletionStatus.COMPLETED_NO;
                handle_system_exception( ex );

                if ( m_client_manager != null )
                {
                    m_client_manager.receive_exception( this, m_callback );
                    m_client_manager = null;
                }

                return null;
            }

            if ( m_state == STATE_CREATED )
            {
                m_state = STATE_MARSHAL;
                return m_request_stream;
            }

            return null;
        }
    }

    /**
     * When this returns the last fragment of the request has been
     * sent. In MARSHAL state changes to WAITING or COMPLETE. This may
     * result in entering COMPLETE/SYSTEM_EXCEPTION/COMPLETED_NO,
     * COMPLETE/LOCATION_FORWARD or COMPLETE/LOCATION_FORWARD_PERMINENT due to
     * communication problems.
     *
     * @return new state. May be WAITING or COMPLETE.
     */
    public int send_request()
    {
        synchronized ( m_sync_state )
        {
            switch ( m_state )
            {

            case STATE_MARSHAL:
                break;

            case STATE_CREATED:

            case STATE_UNMARSHAL:
                throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );

            case STATE_WAITING:

            case STATE_COMPLETE:
                return m_state;

            default:
                org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                        "Invalid state of the state machine." );
            }

            try
            {
                m_request_stream.close();
                m_state = STATE_WAITING;
            }
            catch ( org.omg.CORBA.SystemException ex )
            {
                if ( m_state != STATE_COMPLETE )
                {
                    m_state = STATE_COMPLETE;
                    ex.completed = CompletionStatus.COMPLETED_NO;
                    handle_system_exception( ( org.omg.CORBA.SystemException )
                            ex.fillInStackTrace() );

                    if ( m_client_manager != null )
                    {
                        m_client_manager.receive_exception( this, m_callback );
                        m_client_manager = null;
                    }
                }
            }
            catch ( IOException ex )
            {
                if ( getLogger().isErrorEnabled() )
                {
                    getLogger().error( "Error while closing RequestStream.", ex );
                }
            }

            if ( m_sync_scope != 0 || m_state == STATE_COMPLETE )
            {
                return m_state;
            }
            // with sync sync scope of 0 there is no reply.
            m_state = STATE_COMPLETE;

            m_reply_status = org.omg.PortableInterceptor.SUCCESSFUL.value;

            if ( m_client_manager != null )
            {
                m_client_manager.receive_reply( this, m_callback );
                m_client_manager = null;
            }

            m_response_stream = ( org.omg.CORBA_2_3.portable.InputStream )
                    orb().create_output_stream().create_input_stream();
            m_reply_service_contexts = new org.omg.IOP.ServiceContext[ 0 ];
        }

        m_channel.cancel_request( this, false );
        return STATE_COMPLETE;
    }

    /**
     * Poll to see if a response is available from the target. If in the WAITING
     * state this will return true if wait_for_response would not have to wait
     * for a response. In the UNMARSHAL and COMPLETE states this returns true,
     * It is illegal to call this function in all other states.
     */
    public boolean poll_response()
    {
        synchronized ( m_sync_state )
        {
            switch ( m_state )
            {

            case STATE_WAITING:
                return m_response_arrived;

            case STATE_CREATED:

            case STATE_MARSHAL:
                throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );

            case STATE_UNMARSHAL:

            case STATE_COMPLETE:
                return true;

            default:
                org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                        "Invalid state of the state machine." );
            }
        }
        // never reached
        return false;
    }

    /**
     * Wait for a response from the server. When this returns either the given
     * wait_time has expired (WAITING), a response has arrived from the server
     * (UNMARSHAL or COMPLETE), a transport error has occoured (COMPLETE), or
     * for requests where no response is expected the sync scope is satisfied
     * (COMPLETE). The request state will not exit the WAITING state unless
     * this function is called.
     *
     * @param timeout Maximum amount of time to wait for response.
     *                <=0 to wait forever, > 0 for some timeout (in ms)
     * @return new state. May be WAITING, UNMARSHAL or COMPLETE.
     */
    public int wait_for_response( long timeout )
    {
        synchronized ( m_sync_state )
        {
            switch ( m_state )
            {

            case STATE_WAITING:
                break;

            case STATE_CREATED:

            case STATE_MARSHAL:
                throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );

            case STATE_UNMARSHAL:

            case STATE_COMPLETE:
                return m_state;

            default:
                org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                        "Invalid state of the state machine." );
            }

            if ( !m_response_arrived )
            {
                try
                {
                    m_sync_state.wait( ( timeout <= 0 ) ? 0 : timeout );
                }
                catch ( InterruptedException ex )
                {
                    // TODO: the containing request has been canceled, cancel this
                    // request.
                    Thread.currentThread().interrupt();
                }

                switch ( m_state )
                {

                case STATE_WAITING:

                    if ( !m_response_arrived )
                    {
                        return m_state;
                    }
                    break;

                case STATE_UNMARSHAL:

                case STATE_COMPLETE:
                    return m_state;

                default:
                    org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                            "Invalid state of the state machine." );
                }
            }

            m_response_arrived = false;

            // we have received a response from the server.
            switch ( m_reply_status )
            {

            case org.omg.PortableInterceptor.SUCCESSFUL.value:
                // return directly, interceptors called by lastReplyMessage
                // when the last bit of reply is unmarshaled.
                if ( !m_last_response_arrived )
                {
                    m_state = STATE_UNMARSHAL;
                    return m_state;
                }

                // this occours for a void reply.
                break;

            case org.omg.PortableInterceptor.USER_EXCEPTION.value:
                m_response_stream.mark( 0 );

                m_received_exception_id = m_response_stream.read_string();

                try
                {
                    m_response_stream.reset();
                }
                catch ( IOException ex )
                {
                    if ( getLogger().isErrorEnabled() )
                    {
                        getLogger().error( "Error while resetting RequestStream.", ex );
                    }
                }

                // return directly, interceptors called by lastReplyMessage
                // when the last bit of reply is unmarshaled.
                if ( !m_last_response_arrived )
                {
                    m_state = STATE_UNMARSHAL;
                    return m_state;
                }

                // this occours if the exception body is empty.
                break;

            case org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value:
                m_received_exception_id = m_response_stream.read_string();

                m_received_exception = SystemExceptionHelper.create(
                        m_received_exception_id, "Server Exception", m_response_stream.read_ulong(),
                        CompletionStatus.from_int( m_response_stream.read_ulong() ) );

                m_received_exception_any = null;

                break;

            case org.omg.PortableInterceptor.LOCATION_FORWARD.value:
                m_forward_reference_ior = org.omg.IOP.IORHelper.read( m_response_stream );

                m_forward_reference = null;

                // fallthrough

            case org.omg.PortableInterceptor.TRANSPORT_RETRY.value:

            case UNKNOWN_OBJECT:

            case OBJECT_HERE:
                break;

            default:
                org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                        "Invalid state of the state machine." );
            }

            // last response has arrived, call interceptors.
            if ( !m_last_response_arrived )
            {
                org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                        "Last response arrived." );
            }

            m_state = STATE_UNMARSHAL;

            lastReplyMessage( m_reply_source );

            return m_state;
        }
    }

    /**
     * Get the response output stream. This function is valid in the
     * UNMARSHAL state when reply_status is SUCESSFUL or USER_EXCEPTION.
     * The returned input stream may throw a system exception with status
     * COMPLETED_YES at any time.
     */
    public org.omg.CORBA.portable.InputStream receive_response()
    {
        synchronized ( m_sync_state )
        {
            switch ( m_state )
            {

            case STATE_UNMARSHAL:

            case STATE_COMPLETE:
                return m_response_stream;
            }

            throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                    state_completion_status() );
        }
    }

    /**
     * handle a system exception. Synchronized access to variables synchronized
     * on sync_state must be assured.
     */
    private void handle_system_exception( org.omg.CORBA.SystemException ex )
    {
        m_received_exception_any = null;
        m_received_exception = ex;
        m_received_exception_id = SystemExceptionHelper.id( ex );
        m_reply_status = org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value;
    }

    /**
     * This is called only by the interceptors. Synchronized access to variables synchronized
     * on sync_state must be assured.
     */
    private void handle_location_forward( org.omg.CORBA.Object forward, boolean permanent )
    {
        m_forward_reference = forward;
        m_forward_reference_ior = null;
        m_reply_status = org.omg.PortableInterceptor.LOCATION_FORWARD.value;
    }

    /**
     * Called by channel to set the source for the reply. Add the listener.
     * When the header has been unmarshaled handle_reply will be called.
     */
    void setReplySource( BufferSource replySource )
    {
        if ( m_reply_source != null )
        {
            // received multple replies. If in the middle of unmarshaling a framented
            // reply this exception will get thrown, rather than the correct exception.
            m_reply_source.setException( new org.omg.CORBA.MARSHAL( "Multiple replies received",
                                       0, CompletionStatus.COMPLETED_YES ) );
            return;
        }

        m_reply_source = replySource;

        try
        {
            m_reply_source.addLastMessageProcessedListener(
                new BufferSource.LastMessageProcessedListener()
                {
                    public void lastMessageProcessed( BufferSource source )
                    {
                        lastReplyMessage( source );
                    }
                }

            );
        }
        catch ( java.util.TooManyListenersException ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Unable to handle too many listeners", ex );
            }
        }
    }

    /**
     * Called by the socket queue when adding a new fragment and by the
     * channel when cancelling a request.
     */
    BufferSource getReplySource()
    {
        return m_reply_source;
    }

    /**
      * called by the channel to set a reply.
      */
    void handle_reply( int reply_status, org.omg.IOP.ServiceContext [] reply_service_contexts,
                       org.omg.CORBA_2_3.portable.InputStream response_stream )
    {
        synchronized ( m_sync_state )
        {
            m_reply_service_contexts = reply_service_contexts;
            m_response_stream = response_stream;

            m_response_arrived = true;

            if ( m_operation == null )
            {
                switch ( reply_status )
                {

                case org.omg.GIOP.LocateStatusType_1_2._OBJECT_HERE:
                    m_reply_status = OBJECT_HERE;
                    break;

                case org.omg.GIOP.LocateStatusType_1_2._UNKNOWN_OBJECT:
                    m_reply_status = UNKNOWN_OBJECT;
                    break;

                case org.omg.GIOP.LocateStatusType_1_2._LOC_SYSTEM_EXCEPTION:
                    m_reply_status = org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value;
                    break;

                case org.omg.GIOP.LocateStatusType_1_2._OBJECT_FORWARD:

                case org.omg.GIOP.LocateStatusType_1_2._OBJECT_FORWARD_PERM:
                    m_reply_status = org.omg.PortableInterceptor.LOCATION_FORWARD.value;
                    break;

                case org.omg.GIOP.LocateStatusType_1_2._LOC_NEEDS_ADDRESSING_MODE:
                    m_reply_status = org.omg.PortableInterceptor.TRANSPORT_RETRY.value;
                    break;
                }
            }
            else
            {
                switch ( reply_status )
                {

                case org.omg.GIOP.ReplyStatusType_1_2._NO_EXCEPTION:
                    m_reply_status = org.omg.PortableInterceptor.SUCCESSFUL.value;
                    break;

                case org.omg.GIOP.ReplyStatusType_1_2._USER_EXCEPTION:
                    m_reply_status = org.omg.PortableInterceptor.USER_EXCEPTION.value;
                    break;

                case org.omg.GIOP.ReplyStatusType_1_2._SYSTEM_EXCEPTION:
                    m_reply_status = org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value;
                    break;

                case org.omg.GIOP.ReplyStatusType_1_2._LOCATION_FORWARD:

                case org.omg.GIOP.ReplyStatusType_1_2._LOCATION_FORWARD_PERM:
                    m_reply_status = org.omg.PortableInterceptor.LOCATION_FORWARD.value;
                    break;

                case org.omg.GIOP.ReplyStatusType_1_2._NEEDS_ADDRESSING_MODE:
                    m_reply_status = org.omg.PortableInterceptor.TRANSPORT_RETRY.value;
                    break;
                }
            }

            m_sync_state.notifyAll();
        }
    }

    /**
    * Called when the last message fragment has been processed. If in the
    * WAITING state then this function will have been called by the IO thread,
    * otherwise it will be called by the unmarshalling thread, either way the
    * final state of the request can now be calculated.
    *
    * This function performs all interceptor handling.
     */
    private void lastReplyMessage( BufferSource source )
    {
        org.omg.CORBA.SystemException ex = source.getException();

        synchronized ( m_sync_state )
        {
            boolean notify = false;
            m_last_response_arrived = true;

            if ( m_state == STATE_WAITING )
            {
                // this function will get recalled by wait_for_response.
                if ( ex == null )
                {
                    return;
                }
                else
                {
                    notify = true;
                }
            }

            m_state = STATE_COMPLETE;

            if ( ex != null )
            {
                handle_system_exception( ex );
            }
            // now call the interceptors
            if ( m_client_manager != null )
            {
                switch ( m_reply_status )
                {

                case org.omg.PortableInterceptor.SUCCESSFUL.value:
                    m_client_manager.receive_reply( this, m_callback );
                    break;

                case org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value:

                case org.omg.PortableInterceptor.USER_EXCEPTION.value:
                    m_client_manager.receive_exception( this, m_callback );
                    break;

                case org.omg.PortableInterceptor.LOCATION_FORWARD.value:

                case org.omg.PortableInterceptor.TRANSPORT_RETRY.value:
                    m_client_manager.receive_other( this, m_callback );
                    break;

                case UNKNOWN_OBJECT:

                case OBJECT_HERE:
                    break;

                default:
                    org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                            "Invalid state of the state machine." );
                }

                if ( m_reply_status == org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value )
                {
                    source.setException( m_received_exception );
                }
            }

            if ( notify )
            {
                // handle reply won't get called, so notify the waiting thread.
                m_response_arrived = true;
                m_sync_state.notifyAll();
            }
        }

        // no need to call release_reply on the channel, the last fragment releases
    }

    public short reply_status()
    {
        if ( m_reply_status == REPLY_STATUS_UNSET )
        {
            // guard against unbounded recursion
            if ( m_replyStatusInProgress )
            {
                throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        CompletionStatus.COMPLETED_MAYBE );
            }
            m_replyStatusInProgress = true;
            try
            {
                throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );
            }
            finally
            {
                m_replyStatusInProgress = false;
            }
        }
        return m_reply_status;
    }

    public org.omg.IOP.ServiceContext get_reply_service_context( int id )
    {
        if ( m_reply_service_contexts == null )
        {
            throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                    state_completion_status() );
        }
        switch ( m_state )
        {

        case STATE_UNMARSHAL:

        case STATE_COMPLETE:

            for ( int i = 0; i < m_reply_service_contexts.length; ++i )
            {
                if ( m_reply_service_contexts[ i ].context_id == id )
                {
                    return m_reply_service_contexts[ i ];
                }
            }
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 23,
                    state_completion_status() );
        }

        throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                state_completion_status() );
    }

    public org.omg.CORBA.Object forward_reference()
    {
        switch ( reply_status() )
        {

        case org.omg.PortableInterceptor.LOCATION_FORWARD.value:

            if ( m_forward_reference == null )
            {
                m_forward_reference =
                        new org.openorb.orb.core.ObjectStub( orb(), m_forward_reference_ior );
            }
            return m_forward_reference;
        }

        throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                state_completion_status() );
    }

    /**
     * Get the ior associated with a forward reference. Calling this function
     * instead of forward_reference avoids creating an enclosing delegate/object.
     * This function is valid in the COMPLETE/LOCATION_FORWARD state.
     */
    public org.omg.IOP.IOR forward_reference_ior()
    {
        // reply_status throws bad inv order if not in complete state.
        switch ( reply_status() )
        {

        case org.omg.PortableInterceptor.LOCATION_FORWARD.value:

            if ( m_forward_reference_ior == null )
            {
                if ( m_forward_reference == null || !( m_forward_reference
                      instanceof org.omg.CORBA.portable.ObjectImpl ) )
                {
                    throw new org.omg.CORBA.INTERNAL( "Forward object is unknown type" );
                }
                org.omg.CORBA.portable.Delegate deleg = ( ( org.omg.CORBA.portable.ObjectImpl )
                        m_forward_reference )._get_delegate();

                if ( deleg == null || !( deleg instanceof org.openorb.orb.core.Delegate ) )
                {
                    throw new org.omg.CORBA.INTERNAL( "Object delegate is unknown type" );
                }
                m_forward_reference_ior = ( ( org.openorb.orb.core.Delegate ) deleg ).ior();
            }

            return m_forward_reference_ior;
        }

        throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                state_completion_status() );
    }

    /**
     * Get the system exception which would be contained in the any returned
     * from receive_exception. This function is valid in the
     * COMPLETE/SYSTEM_EXCEPTION state.
     */
    public org.omg.CORBA.SystemException received_system_exception()
    {
        // reply_status throws bad inv order if not in complete state.
        synchronized ( m_sync_state )
        {
            if ( reply_status() == org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value )
            {
                return m_received_exception;
            }
        }

        throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                state_completion_status() );
    }

    public org.omg.CORBA.Any received_exception()
    {
        // reply_status throws bad inv order if not in complete state.
        switch ( reply_status() )
        {

        case org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value:

            if ( m_received_exception_any == null )
            {
                m_received_exception_any = orb().create_any();
                SystemExceptionHelper.insert( m_received_exception_any,
                        m_received_exception );
            }

            return m_received_exception_any;

        case org.omg.PortableInterceptor.USER_EXCEPTION.value:
            throw new org.omg.CORBA.NO_RESOURCES( org.omg.CORBA.OMGVMCID.value | 1,
                    state_completion_status() );
        }

        throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                state_completion_status() );
    }

    public String received_exception_id()
    {
        // reply_status throws bad inv order if not in complete state.
        switch ( reply_status() )
        {

        case org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value:

        case org.omg.PortableInterceptor.USER_EXCEPTION.value:
            return m_received_exception_id;
        }

        throw new BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
               state_completion_status() );
    }

    /**
     * We define the finalize method so that the request is canceled if a
     * deferred request is discarded.
     */
    protected void finalize() throws Throwable
    {
        try
        {
            synchronized ( m_sync_state )
            {
                if ( m_state != STATE_COMPLETE )
                {
                    cancel( new org.omg.CORBA.TIMEOUT() );
                }
            }
        }
        finally
        {
            super.finalize();
        }
    }

    private class IIOPRequestCallback implements org.openorb.orb.pi.RequestCallback
    {
        public void reply_system_exception( org.omg.CORBA.SystemException ex )
        {
            handle_system_exception( ex );
        }

        public void reply_location_forward( org.omg.CORBA.Object forward, boolean permanent )
        {
            handle_location_forward( forward, permanent );
        }

        public void reply_runtime_exception( java.lang.RuntimeException ex )
        {
            // TODO: set the minor code to be 'unknown exception in interceptor'
            org.omg.CORBA.portable.UnknownException uex =
                     new org.omg.CORBA.portable.UnknownException( ex );
            uex.completed = state_completion_status();
            handle_system_exception( uex );
        }

        public void reply_error( java.lang.Error ex )
        {
            if ( ex instanceof ThreadDeath )
            {
                throw ex;
            }
            else if ( ex instanceof OutOfMemoryError )
            {
                handle_system_exception( new org.omg.CORBA.NO_MEMORY() );
            }
            else if ( ex instanceof StackOverflowError )
            {
                handle_system_exception( new org.omg.CORBA.NO_RESOURCES( "Stack Overflow",
                        IIOPMinorCodes.NO_RESOURCES_STACK_OVERFLOW, state_completion_status() ) );
            }
            else
            {
                // TODO: set the minor code to be 'unknown exception in interceptor'
                org.omg.CORBA.portable.UnknownException uex =
                        new org.omg.CORBA.portable.UnknownException( ex );
                uex.completed = state_completion_status();
                handle_system_exception( uex );
            }
        }
    }

}

