/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.omg.PortableInterceptor.USER_EXCEPTION;
import org.omg.PortableInterceptor.LOCATION_FORWARD;

import org.omg.CORBA.Object;
import org.omg.CORBA.OMGVMCID;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.SystemException;
import org.openorb.orb.util.Trace;

/**
 * This is a very hacked version of the IIOPClientRequest to route calls
 * inside the ORB.
 *
 * This is necessary because of some IDL compilers (as JDK1.4) that
 * leave to the ORB all the local calls instead of handling them inside the stubs
 *
 * @author <a href="erik.putrycz@int-evry.fr">Erik Putrycz</a>
 * @version $Revision: 1.8 $ $Date: 2004/02/19 07:21:31 $
 */
public class LocalClientRequest
            extends AbstractClientRequest
{
    private int m_request_id = 0;
    private Object m_target = null;
    private byte m_sync_scope = -1;
    private LocalServerRequest m_server_request = null;
    private org.omg.CORBA.portable.InputStream m_reply_stream = null;

    private Address[] m_adresses;
    private ServerManager m_serverManager = null;

    private int m_reply_status;
    private org.omg.IOP.ServiceContext [] m_reply_service_contexts;

    // request state
    private boolean m_response_expected;
    private String m_operation;

    // interceptor stuff
    private org.openorb.orb.pi.ClientManager m_client_manager = null;
    private org.openorb.orb.pi.RequestCallback m_callback = null;

    // synchronize access to all the nonvolitile state variables (below here)
    private java.lang.Object m_sync_state = new java.lang.Object ();

    private int m_state = STATE_CREATED;

    private org.omg.CORBA.portable.OutputStream m_request_stream;

    // reply state.
    private org.omg.CORBA.portable.InputStream m_response_stream;
    private boolean m_response_arrived = false;
    private boolean m_last_response_arrived = false;

    private Object m_forward_reference = null;
    private org.omg.IOP.IOR m_forward_reference_ior;

    private SystemException m_received_exception;
    private org.omg.CORBA.Any m_received_exception_any = null;
    private String m_received_exception_id;

    //private static final short REPLY_STATUS_UNSET = Short.MIN_VALUE;

    /**
     * Creates a new LocalClientRequest
     *
     * @param orb ORB
     * @param request_id the current request id
     * @param target target object for the request
     * @param operation operation
     * @param response_expected oneway call or not
     * @param adresses all the adresses of the target
     */
    public LocalClientRequest ( org.omg.CORBA.ORB orb, int request_id, Object target,
        String operation, boolean response_expected, Address[] adresses )
    {
        super ( request_id, target, adresses[ 0 ], null );
        m_request_id = request_id;
        m_adresses = adresses;
        m_operation = operation;
        m_target = target;
        m_response_expected = response_expected;
        // load ServerCPCManager
        m_serverManager = ( ServerManager )
                          ( ( org.openorb.orb.core.ORB ) orb ).getFeature ( "ServerCPCManager" );

        m_request_stream = new org.openorb.orb.io.LocalOutputStream ();
        m_sync_scope = ( m_response_expected )
                       ? ( byte ) org.omg.Messaging.SYNC_WITH_TARGET.value
                       : ( byte ) org.omg.Messaging.SYNC_WITH_SERVER.value;

        m_client_manager = ( org.openorb.orb.pi.ClientManager )
            ( ( org.openorb.orb.core.ORB ) orb () ).getFeature ( "ClientInterceptorManager" );

        if ( m_client_manager != null )
        {
            m_callback = new org.openorb.orb.net.LocalClientRequest.LocalRequestCallback ();
        }
    }

    /**
     * Current request state.
     *
     * @return state
     */
    public int state ()
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
    public boolean is_request ()
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
    public boolean is_locate ()
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
     *
     * @return true if is a poll
     */
    public boolean is_poll ()
    {
        return false;
    }

    /**
     * Returns the sync scope of the request (depends if it is one way or not)
     *
     * @return sync_scope
     */
    public short sync_scope ()
    {
        return m_sync_scope;
    }

    /**
     */
    public boolean response_expected ()
    {
        return m_response_expected;
    }

    /**
     * Request operation
     */
    public String operation ()
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
    public boolean cancel ( SystemException ex )
    {
        if ( m_state == STATE_COMPLETE )
        {
            return false;
        }
        ex.completed = org.omg.CORBA.CompletionStatus.COMPLETED_YES;

        handle_system_exception ( ex );

        return true;
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
    public org.omg.CORBA.portable.OutputStream begin_marshal ()
    {
        return m_request_stream;
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
    public int send_request ()
    {
        // create the input stream from the request outstream
        org.omg.CORBA.portable.InputStream is = m_request_stream.create_input_stream ();
        // we need the object key... not sure this is the best way
        byte[] object_id =
            m_adresses[ 0 ].getTargetAddress ( org.omg.GIOP.KeyAddr.value ).object_key ();
        org.omg.IOP.ServiceContext[] request_service_contexts =
            get_request_service_contexts ();

        m_server_request = new LocalServerRequest ( m_serverManager, m_request_id, is, object_id,
                                            m_operation, m_sync_scope, request_service_contexts );

        //here are all the steps to before dispatch
        m_server_request.begin_request ();
        m_server_request.adapter ( m_serverManager.find_adapter ( object_id ) );

        // here occurs the real marshalling

        try
        {
            m_server_request.dispatch ();
        }
        catch ( SystemException ex )
        {
            m_state = STATE_COMPLETE;
            ex.completed = org.omg.CORBA.CompletionStatus.COMPLETED_NO;
            handle_system_exception ( ex );

            if ( m_client_manager != null )
            {
                m_client_manager.receive_exception ( this, m_callback );
                m_client_manager = null;
            }

            return m_state;
        }
        catch ( org.openorb.orb.adapter.AdapterDestroyedException ex )
        {
            // should never happen the find_adapter is already responsible
            Trace.signalIllegalCondition ( getLogger (),
                                           "AdapterDestroyed on local object." );
        }

        //here we handle the reply
        m_reply_stream = m_server_request.getReplyStream ().create_input_stream ();

        if ( m_server_request.is_reply_exception () )
        {
            m_received_exception_id = m_reply_stream.read_string ();

            try
            {
                m_reply_stream.reset ();
            }
            catch ( java.io.IOException ioex )
            {
                // impossible
            }
        }

        m_state = STATE_WAITING;
        return m_state;
    }

    /**
     * Poll to see if a response is available from the target. If in the WAITING
     * state this will return true if wait_for_response would not have to wait
     * for a response. In the UNMARSHAL and COMPLETE states this returns true,
     * It is illegal to call this function in all other states.
     */
    public boolean poll_response ()
    {
        switch ( m_state )
        {

        case STATE_WAITING:
            return m_response_arrived;

        case STATE_CREATED:

        case STATE_MARSHAL:
            throw new BAD_INV_ORDER ( OMGVMCID.value | 10,
                                      state_completion_status () );

        case STATE_UNMARSHAL:

        case STATE_COMPLETE:
            return true;

        default:
            Trace.signalIllegalCondition ( getLogger (),
                                           "Invalid state of the state machine." );
        }

        // never reached
        return false;
    }

    /**
     * Get the response output stream. This function is valid in the
     * UNMARSHAL state when reply_status is SUCESSFUL or USER_EXCEPTION.
     * The returned input stream may throw a system exception with status
     * COMPLETED_YES at any time.
     * With the local server request no error can happen at this stage
     */
    public org.omg.CORBA.portable.InputStream receive_response ()
    {
        return m_server_request.getReplyStream ().create_input_stream ();
    }

    /**
     * Handle a system exception. Synchronized access to variables synchronized
     * on sync_state must be assured.
     */
    private void handle_system_exception ( SystemException ex )
    {
        m_received_exception_any = null;
        m_received_exception = ex;
        m_received_exception_id = org.openorb.orb.core.SystemExceptionHelper.id ( ex );
        m_reply_status = SYSTEM_EXCEPTION.value;
    }

    /**
     * This is called only by the interceptors. Synchronized access to variables synchronized
     * on sync_state must be assured.
     */
    private void handle_location_forward ( Object forward, boolean permanent )
    {
        m_forward_reference = forward;
        m_forward_reference_ior = null;
        m_reply_status = LOCATION_FORWARD.value;
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
    public int wait_for_response ( long timeout )
    {
        synchronized ( m_sync_state )
        {
            switch ( m_state )
            {

            case STATE_WAITING:
                break;

            case STATE_COMPLETE:
                return m_state;

            default:
                Trace.signalIllegalCondition ( getLogger (),
                                               "Invalid state of the state machine." );
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

            case USER_EXCEPTION.value:
                m_response_stream.mark ( 0 );

                m_received_exception_id = m_response_stream.read_string ();

                try
                {
                    m_response_stream.reset ();
                }
                catch ( java.io.IOException ex )
                {
                    if ( getLogger ().isErrorEnabled () )
                    {
                        getLogger ().error ( "Error while resetting RequestStream.", ex );
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

            case SYSTEM_EXCEPTION.value:
                m_received_exception_id = m_response_stream.read_string ();

                m_received_exception = org.openorb.orb.core.SystemExceptionHelper.create (
                    m_received_exception_id, "Server Exception", m_response_stream.read_ulong (),
                    org.omg.CORBA.CompletionStatus.from_int ( m_response_stream.read_ulong () ) );

                m_received_exception_any = null;

                break;

            case LOCATION_FORWARD.value:
                m_forward_reference_ior = org.omg.IOP.IORHelper.read ( m_response_stream );

                m_forward_reference = null;

                // fallthrough

            case org.omg.PortableInterceptor.TRANSPORT_RETRY.value:

            case UNKNOWN_OBJECT:

            case OBJECT_HERE:
                break;

            default:
                Trace.signalIllegalCondition ( getLogger (),
                                               "Invalid state of the state machine." );
            }

            // last response has arrived, call interceptors.
            if ( !m_last_response_arrived )
            {
                Trace.signalIllegalCondition ( getLogger (),
                                               "Last response arrived." );
            }

            m_state = STATE_UNMARSHAL;

            lastReplyMessage ( );

            return m_state;
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
    private void lastReplyMessage ( )
    {
        SystemException ex = m_received_exception;

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
            handle_system_exception ( ex );
        }
        // now call the interceptors
        if ( m_client_manager != null )
        {
            switch ( m_reply_status )
            {

            case org.omg.PortableInterceptor.SUCCESSFUL.value:
                m_client_manager.receive_reply ( this, m_callback );
                break;

            case SYSTEM_EXCEPTION.value:

            case USER_EXCEPTION.value:
                m_client_manager.receive_exception ( this, m_callback );
                break;

            case LOCATION_FORWARD.value:

            case org.omg.PortableInterceptor.TRANSPORT_RETRY.value:
                m_client_manager.receive_other ( this, m_callback );
                break;

            case UNKNOWN_OBJECT:

            case OBJECT_HERE:
                break;

            default:
                Trace.signalIllegalCondition ( getLogger (),
                                               "Invalid state of the state machine." );
            }

        }

        m_response_arrived = true;

        // no need to call release_reply on the channel, the last fragment releases
    }

    /**
     * Returns the reply status of the request
     * @return reply
     */
    public short reply_status ()
    {
        if ( m_server_request.is_reply_exception () )
        {
            return USER_EXCEPTION.value;
        }
        else
        {
            return org.omg.PortableInterceptor.SUCCESSFUL.value;
        }
    }

    /**
     * Returns the service context of the request.
     *
     * @param id
     * @return The service context of the request.
     */
    public org.omg.IOP.ServiceContext get_reply_service_context ( int id )
    {
        if ( m_reply_service_contexts == null )
        {
            throw new BAD_INV_ORDER ( OMGVMCID.value | 10,
                                      state_completion_status () );
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
            throw new org.omg.CORBA.BAD_PARAM ( OMGVMCID.value | 23,
                                                state_completion_status () );
        }

        throw new BAD_INV_ORDER ( OMGVMCID.value | 10,
                                  state_completion_status () );
    }

    /**
     * Return and eventually create a forward reference from a LOCATION_FORWARD
     * reply.
     *
     * @return The forward reference in case one exists.
     */
    public Object forward_reference ()
    {
        switch ( reply_status () )
        {

        case LOCATION_FORWARD.value:

            if ( m_forward_reference == null )
            {
                m_forward_reference =
                    new org.openorb.orb.core.ObjectStub ( orb (), m_forward_reference_ior );
            }
            return m_forward_reference;
        }

        throw new BAD_INV_ORDER ( OMGVMCID.value | 10,
                                  state_completion_status () );
    }

    /**
     * Get the ior associated with a forward reference. Calling this function
     * instead of forward_reference avoids creating an enclosing delegate/object.
     * This function is valid in the COMPLETE/LOCATION_FORWARD state.
     */
    public org.omg.IOP.IOR forward_reference_ior ()
    {
        // reply_status throws bad inv order if not in complete state.

        switch ( reply_status () )
        {

        case LOCATION_FORWARD.value:

            if ( m_forward_reference_ior == null )
            {
                if ( m_forward_reference == null || !( m_forward_reference
                      instanceof org.omg.CORBA.portable.ObjectImpl ) )
                {
                    throw new org.omg.CORBA.INTERNAL ( "Forward object is unknown type" );
                }
                org.omg.CORBA.portable.Delegate deleg = ( ( org.omg.CORBA.portable.ObjectImpl )
                                                        m_forward_reference )._get_delegate ();

                if ( deleg == null || !( deleg instanceof org.openorb.orb.core.Delegate ) )
                {
                    throw new org.omg.CORBA.INTERNAL ( "Object delegate is unknown type" );
                }
                m_forward_reference_ior = ( ( org.openorb.orb.core.Delegate ) deleg ).ior ();
            }

            return m_forward_reference_ior;
        }

        throw new BAD_INV_ORDER ( OMGVMCID.value | 10,
                                  state_completion_status () );
    }

    /**
     * Get the system exception which would be contained in the any returned
     * from receive_exception. This function is valid in the
     * COMPLETE/SYSTEM_EXCEPTION state.
     */
    public SystemException received_system_exception ()
    {
        // reply_status throws bad inv order if not in complete state.

        synchronized ( m_sync_state )
        {
            if ( reply_status () == SYSTEM_EXCEPTION.value )
            {
                return m_received_exception;
            }
        }

        throw new BAD_INV_ORDER ( OMGVMCID.value | 10,
                                  state_completion_status () );
    }

    /**
     * Returns the forward reference.
     */
    public org.omg.CORBA.Any received_exception ()
    {
        // reply_status throws bad inv order if not in complete state.

        switch ( reply_status () )
        {

        case SYSTEM_EXCEPTION.value:

            if ( m_received_exception_any == null )
            {
                m_received_exception_any = orb ().create_any ();
                org.openorb.orb.core.SystemExceptionHelper.insert ( m_received_exception_any,
                        m_received_exception );
            }

            return m_received_exception_any;

        case USER_EXCEPTION.value:
            throw new org.omg.CORBA.NO_RESOURCES ( OMGVMCID.value | 1,
                                                   state_completion_status () );
        }

        throw new BAD_INV_ORDER ( OMGVMCID.value | 10,
                                  state_completion_status () );
    }

    /**
     * Rreturns the exception id if an exception has been sent by the server.
     */
    public String received_exception_id ()
    {
        // reply_status throws bad inv order if not in complete state.

        switch ( reply_status () )
        {

        case SYSTEM_EXCEPTION.value:

        case USER_EXCEPTION.value:
            return m_received_exception_id;
        }

        throw new BAD_INV_ORDER ( OMGVMCID.value | 10,
                                  state_completion_status () );
    }

    /**
     * We define the finalize method so that the request is canceled if a
     * deferred request is discarded.
     */
    protected void finalize () throws java.lang.Throwable
    {
        cancel ( new org.omg.CORBA.TIMEOUT () );
    }

    private class LocalRequestCallback implements org.openorb.orb.pi.RequestCallback
    {
        /** handle a system exception
         */
        public void reply_system_exception ( SystemException ex )
        {
            handle_system_exception ( ex );
        }

        /** handles a location forward
         */
        public void reply_location_forward ( Object forward, boolean permanent )
        {
            handle_location_forward ( forward, permanent );
        }

        /** handles a runtime exception
         */
        public void reply_runtime_exception ( java.lang.RuntimeException ex )
        {
            // TODO: set the minor code to be 'unknown exception in interceptor'
            org.omg.CORBA.portable.UnknownException uex =
                new org.omg.CORBA.portable.UnknownException ( ex );
            uex.completed = state_completion_status ();
            handle_system_exception ( uex );
        }

        /**
         * Handles an error.
         */
        public void reply_error ( java.lang.Error ex )
        {
            if ( ex instanceof ThreadDeath )
            {
                throw ex;
            }
            else if ( ex instanceof OutOfMemoryError )
            {
                handle_system_exception ( new org.omg.CORBA.NO_MEMORY () );
            }
            else if ( ex instanceof StackOverflowError )
            {
                handle_system_exception ( new org.omg.CORBA.NO_RESOURCES ( "Stack Overflow",
                    org.openorb.orb.iiop.IIOPMinorCodes.NO_RESOURCES_STACK_OVERFLOW,
                    state_completion_status () ) );
            }
            else
            {
                // TODO: set the minor code to be 'unknown exception in interceptor'
                org.omg.CORBA.portable.UnknownException uex =
                    new org.omg.CORBA.portable.UnknownException ( ex );
                uex.completed = state_completion_status ();
                handle_system_exception ( uex );
            }
        }
    }
}

