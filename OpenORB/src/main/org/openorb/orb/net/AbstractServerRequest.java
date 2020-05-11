/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import java.util.Map;
import java.util.HashMap;

import org.omg.CORBA.CompletionStatus;

import org.openorb.orb.core.SystemExceptionHelper;
import org.openorb.orb.core.MinorCodes;

import org.openorb.orb.adapter.ObjectAdapter;
import org.openorb.orb.adapter.AdapterDestroyedException;

import org.apache.avalon.framework.logger.Logger;
import org.openorb.orb.util.Trace;
import org.openorb.util.NumberCache;

/**
 * Base class which implements most of the server request functionality.
 * Only protocol specific things are not implemented.
 *
 * @author Chris Wood
 * @version $Revision: 1.16 $ $Date: 2004/06/04 08:20:29 $
 */
public abstract class AbstractServerRequest
    extends org.omg.CORBA.LocalObject
    implements ServerRequest
{

    private org.omg.CORBA.ORB m_orb;
    private ServerManager m_server_manager;

    private ServerChannel m_channel;

    private int m_request_id;
    private byte [] m_object_key;
    private String m_operation;
    private byte m_sync_scope;
    private org.omg.IOP.ServiceContext[] m_request_service_contexts;

    private org.omg.CORBA.portable.InputStream m_argument_stream;

    private org.openorb.orb.pi.CurrentImpl m_pi_current;
    private org.omg.PortableInterceptor.CurrentOperations m_rs_pi_curr_entry;
    private org.omg.PortableInterceptor.CurrentOperations m_ts_pi_curr_entry;

    // self synchronized.
    private Map m_service_contexts = new HashMap();

    private org.openorb.orb.pi.ServerManager m_interceptor_manager = null;
    private org.openorb.orb.pi.RequestCallback m_callback = null;

    private Object m_sync_state = new Object();
    private volatile int m_state = -1;

    private short m_reply_status = REPLY_STATUS_UNSET;

    private org.openorb.orb.adapter.ObjectAdapter m_adapter = null;
    private org.openorb.orb.adapter.TargetInfo m_target;

    private org.omg.CORBA.portable.OutputStream m_reply_stream;

    private org.omg.CORBA.Object m_forward_reference;
    private org.omg.IOP.IOR m_forward_reference_ior;

    private org.omg.CORBA.SystemException m_sending_system_exception;
    private org.omg.CORBA.Any m_sending_system_exception_any = null;
    private String m_sending_system_exception_id;
    private Logger m_logger = null;

    private static final short REPLY_STATUS_UNSET = Short.MIN_VALUE;

    private static boolean s_throw_service_context_exceptions = true;


    /**
     * One pass constructor for request
     */
    public AbstractServerRequest( ServerManager serverManager, ServerChannel channel,
                                  int request_id,
                                  org.omg.CORBA.portable.InputStream argument_stream,
                                  byte [] object_key, String operation, byte sync_scope,
                                  org.omg.IOP.ServiceContext[] request_service_contexts )
    {
        init_one( serverManager, channel, request_id, argument_stream );
        init( object_key, operation, sync_scope, request_service_contexts );
    }

    /**
     * Two pass constructor for locate request. This constructor must be
     * followed by a call to init.
     */
    public AbstractServerRequest( ServerManager serverManager, ServerChannel channel,
                                  int request_id,
                                  org.omg.CORBA.portable.InputStream argument_stream )
    {
        init_one( serverManager, channel, request_id, argument_stream );
    }

    private void init_one( ServerManager serverManager, ServerChannel channel, int request_id,
                           org.omg.CORBA.portable.InputStream argument_stream )
    {
        m_server_manager = serverManager;
        m_channel = channel;
        m_request_id = request_id;
        m_argument_stream = argument_stream;

        m_orb = m_server_manager.orb();

        m_logger = ( ( org.openorb.orb.core.ORBSingleton ) m_orb ).getLogger();

        m_pi_current = ( ( org.openorb.orb.core.ORB ) m_orb ).getPICurrent();
    }

    /**
     * second constructor pass.
     */
    public void init( byte [] object_key, String operation, byte sync_scope,
                      org.omg.IOP.ServiceContext[] request_service_contexts )
    {
        if ( !( m_state < 0 ) )
        {
            org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(), "Invalid state." );
        }

        m_object_key = object_key;
        m_operation = operation;
        m_sync_scope = sync_scope;
        m_request_service_contexts = request_service_contexts;

        if ( m_operation != null )
        {
            m_interceptor_manager = ( org.openorb.orb.pi.ServerManager )
                    ( ( org.openorb.orb.core.ORB ) m_orb ).getFeature( "ServerInterceptorManager" );

            if ( m_interceptor_manager != null )
            {
                m_callback = new org.openorb.orb.pi.RequestCallback()
                {
                    public void reply_system_exception( org.omg.CORBA.SystemException ex )
                    {
                        handle_system_exception( ex );
                    }

                    public void reply_runtime_exception( java.lang.RuntimeException ex )
                    {
                        handle_runtime_exception( ex );
                    }

                    public void reply_error( java.lang.Error ex )
                    {
                        handle_error( ex );
                    }

                    public void reply_location_forward( org.omg.CORBA.Object forward,
                            boolean permanent )
                    {
                        handle_location_forward( forward, permanent );
                    }
                };
            }
        }

        m_state = STATE_CREATED;
    }

    /**
     * One pass constructor for locate request
     */
    public AbstractServerRequest( ServerManager serverManager, ServerChannel channel,
                                  int request_id, byte [] object_key )
    {
        init_one( serverManager, channel, request_id, null );
        init( object_key, null, ( byte ) 3, null );
    }

    /**
     * Two pass constructor for locate request. Must be followed by call to
     * init(byte [] object_key)
     */
    public AbstractServerRequest( ServerManager serverManager, ServerChannel channel,
                                  int request_id )
    {
        init_one( serverManager, channel, request_id, null );
    }

    /**
     * Second locate request construction pass.
     */
    public void init( byte [] object_key )
    {
        init( object_key, null, ( byte ) 3, null );
    }

    /**
     * Disable exceptions in service context methods.
     * This is a performance optimization as assembling the
     * stack trace is a costly operation.
     */
    public static void disableServiceContextExceptions()
    {
        s_throw_service_context_exceptions = false;
    }

    /**
     * Enable exceptions in service context methods.
     * Not throwing a performance optimization as assembling the
     * stack trace is a costly operation.
     */
    public static void enableServiceContextExceptions()
    {
        s_throw_service_context_exceptions = true;
    }

    /**
     * Orb
     */
    public org.omg.CORBA.ORB orb()
    {
        return m_orb;
    }

    /**
     * Server channel
     */
    public ServerChannel channel()
    {
        return m_channel;
    }

    /**
     * Current request state.
     */
    public int state()
    {
        return m_state;
    }

    /**
     * Returns completion status appropriate for the request state.
     */
    protected CompletionStatus state_completion_status()
    {
        switch ( m_state )
        {

        case -1:

        case STATE_CREATED:

        case STATE_FIND_ADAPTER:

        case STATE_QUEUED:
            return CompletionStatus.COMPLETED_NO;

        case STATE_PROCESSING:
            return CompletionStatus.COMPLETED_MAYBE;

        case STATE_REPLY:

        case STATE_COMPLETE:
            return CompletionStatus.COMPLETED_YES;
        }

        org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                "Wrong state of the state machine." );

        // never reached
        return null;
    }

    /**
     * This returns true if this request is a locate request. Use replyLocate
     * to set the reply.
     *
     * Interceptors are not called for locate requests.
     *
     * ServerRequestInfo operations: arguments, exceptions, contexts,
     * operation_context, result, get_request_service_context,
     * get_reply_service_context, add_reply_service_context are not valid.
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
     * Object Key. This is always available.
     */
    public byte[] object_key()
    {
        return m_object_key;
    }


    /**
     * Request ID. This is always available.
     */
    public int request_id()
    {
        return m_request_id;
    }

    /**
     * Equality depends on request IDs and channels.
     */
    public boolean equals( Object obj )
    {
        if ( obj instanceof ServerRequest )
        {
            ServerRequest srq2 = ( ServerRequest ) obj;
            return ( m_request_id == srq2.request_id() ) && ( m_channel == srq2.channel() );
        }

        return false;
    }

    /**
     * Ensure that equal requests have the same hashCode.
     */
    public int hashCode()
    {
        int result = ( m_channel != null ? m_channel.hashCode() : 0 );
        result = 29 * result + m_request_id;
        return result;
    }

    /**
     * Operation name. This will return null for locate requests.
     */
    public String operation()
    {
        return m_operation;
    }

    /**
     * Response is expected to the request. If this is true then createReply or
     * createExceptionReply is expected to be called by the dispatch operation.
     */
    public boolean response_expected()
    {
        return ( m_sync_scope == org.omg.Messaging.SYNC_WITH_TARGET.value );
    }

    /**
     * Message sync scope.
     */
    public short sync_scope()
    {
        return m_sync_scope;
    }

    /**
     * Request Arguments. This operation is not available in java.
     */
    public org.omg.Dynamic.Parameter[] arguments()
    {
        throw new org.omg.CORBA.NO_RESOURCES( org.omg.CORBA.OMGVMCID.value | 1,
                state_completion_status() );
    }

    /**
     * Request exceptions. This operation is not available in java.
     */
    public org.omg.CORBA.TypeCode[] exceptions()
    {
        throw new org.omg.CORBA.NO_RESOURCES( org.omg.CORBA.OMGVMCID.value | 1,
                state_completion_status() );
    }

    /**
     * Request contexts. This operation is not available in java.
     */
    public java.lang.String[] contexts()
    {
        throw new org.omg.CORBA.NO_RESOURCES( org.omg.CORBA.OMGVMCID.value | 1,
                state_completion_status() );
    }

    /**
     * Request contexts. This operation is not available in java.
     */
    public java.lang.String[] operation_context()
    {
        throw new org.omg.CORBA.NO_RESOURCES( org.omg.CORBA.OMGVMCID.value | 1,
                state_completion_status() );
    }

    /**
     * Request result. This operation is not available in java.
     */
    public org.omg.CORBA.Any result()
    {
        throw new org.omg.CORBA.NO_RESOURCES( org.omg.CORBA.OMGVMCID.value | 1,
                state_completion_status() );
    }

    /**
     * Access the PI current from interceptors. From the adapter this operation
     * is also available via the PI Current.
     */
    public org.omg.CORBA.Any get_slot( int id )
        throws org.omg.PortableInterceptor.InvalidSlot
    {
        synchronized ( m_sync_state )
        {
            if ( m_rs_pi_curr_entry == null )
            {
                m_rs_pi_curr_entry = m_pi_current.create();
            }
        }

        synchronized ( m_rs_pi_curr_entry )
        {
            return m_rs_pi_curr_entry.get_slot( id );
        }
    }

    /**
     * Access the PI current from interceptors. From the adapter this operation
     * is also available via the PI Current.
     */
    public void set_slot( int id, org.omg.CORBA.Any data )
        throws org.omg.PortableInterceptor.InvalidSlot
    {
        synchronized ( m_sync_state )
        {
            if ( m_rs_pi_curr_entry == null )
            {
                m_rs_pi_curr_entry = m_pi_current.create();
            }
        }

        synchronized ( m_rs_pi_curr_entry )
        {
            m_rs_pi_curr_entry.set_slot( id, data );
        }
    }

    /**
     * Get reqest service context. This operation is always available.
     */
    public org.omg.IOP.ServiceContext get_request_service_context( int id )
    {
        for ( int i = 0; i < m_request_service_contexts.length; ++i )
        {
            if ( m_request_service_contexts[ i ].context_id == id )
            {
                return m_request_service_contexts[ i ];
            }
        }
        if ( s_throw_service_context_exceptions )
        {
            throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 23,
                    state_completion_status() );
        }
        else
        {
            return null;
        }
    }

    /**
     * Client intiated request cancelation. This may be called at any time to
     * indicate that a response from the request is no longer expected.
     */
    public void client_cancel()
    {
        synchronized ( m_sync_state )
        {
            if ( m_state == STATE_COMPLETE )
            {
                return;
            }
            // client cancel not allowed once the request starts processing.
            // this is needed to catch any cancel requests which occour while
            // within the dispatch operation before dispatching the request.
            if ( m_sync_scope != org.omg.Messaging.SYNC_WITH_TARGET.value )
            {
                switch ( m_state )
                {

                case STATE_PROCESSING:

                case STATE_REPLY:
                    return;
                }
            }
            handle_system_exception( new org.omg.CORBA.TRANSIENT( org.omg.CORBA.OMGVMCID.value | 2,
                    state_completion_status() ) );

            // don't send a reply, just call the interceptors and dump the request
            if ( m_state != STATE_CREATED && m_interceptor_manager != null )
            {
                m_interceptor_manager.send_exception( this, m_callback );
                m_interceptor_manager = null;
            }

            m_state = STATE_COMPLETE;

            if ( m_target != null )
            {
                m_adapter.cancel_dispatch( this, m_target );
            }
            release_request();
        }
    }

    /**
     * Server intiated request cancelation. This may be called at any time by
     * the server to indicate a server initiated cancel. The request is returned
     * to the server. The completion status of the exception will be set
     * appropriatly.
     */
    public void server_cancel( Throwable ex )
    {
        synchronized ( m_sync_state )
        {
            if ( m_state == STATE_COMPLETE )
            {
                return;
            }
            if ( ex instanceof org.omg.CORBA.SystemException )
            {
                org.omg.CORBA.SystemException sex = ( org.omg.CORBA.SystemException ) ex;
                sex.completed = state_completion_status();
                handle_system_exception( sex );
            }
            else if ( ex instanceof RuntimeException )
            {
                handle_runtime_exception( ( RuntimeException ) ex );
            }
            else if ( ex instanceof Error )
            {
                handle_error( ( Error ) ex );
            }
            else
            {
                handle_unknown_throwable( ex );
            }
            if ( m_target != null )
            {
                m_adapter.cancel_dispatch( this, m_target );
            }
            complete_request();
        }
    }

    /**
     * Called by the worker thread before every attempt to run the request. This
     * should call the recieve_request_service_context interception point.
     * @return the state of the request.
     */
    public int begin_request()
    {
        synchronized ( m_sync_state )
        {
            if ( m_state == STATE_CREATED )
            {
                if ( m_interceptor_manager != null )
                {
                    m_interceptor_manager.receive_request_service_contexts( this, m_callback );

                    if ( m_reply_status != REPLY_STATUS_UNSET )
                    {
                        // interceptors have forced completion
                        m_interceptor_manager = null;
                        complete_request();
                        return m_state;
                    }
                }

                m_state = STATE_FIND_ADAPTER;
                m_ts_pi_curr_entry = m_pi_current.copy( m_rs_pi_curr_entry );
            }

            m_pi_current.set( m_ts_pi_curr_entry );
        }

        return m_state;
    }

    /**
     * Set the object adapter and transfer to the QUEUED state. This is legal
     * only in the FIND_ADAPTER or COMPLETE states. If COMPLETE this has no effect.
     *
     * @return the state of the request. This will be either QUEUED or COMPLETE.
     */
    public int adapter( ObjectAdapter adapter )
    {
        synchronized ( m_sync_state )
        {
            switch ( m_state )
            {

            case STATE_FIND_ADAPTER:
                m_adapter = adapter;
                m_state = STATE_QUEUED;

            case STATE_COMPLETE:
                return m_state;

            default:
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );
            }
        }
    }

    /**
    * Server adapter. This operation is valid in all states apart from
    * CREATED and FIND_ADAPTER.
    */
    public ObjectAdapter adapter()
    {
        synchronized ( m_sync_state )
        {
            if ( m_adapter == null )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );
            }
            return m_adapter;
        }
    }

    /**
     * Find the server policy of the specified type. This operation is valid in
     * all states apart from CREATED and FIND_ADAPTER.
     */
    public org.omg.CORBA.Policy get_server_policy( int type )
    {
        synchronized ( m_sync_state )
        {
            if ( m_adapter == null )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );
            }
            int [] types = new int[ 1 ];

            types[ 0 ] = type;

            org.omg.CORBA.Policy [] pols = m_adapter.get_server_policies( types );

            if ( pols.length >= 1 )
            {
                return pols[ 0 ];
            }
            throw new org.omg.CORBA.INV_POLICY( org.omg.CORBA.OMGVMCID.value | 1,
                    state_completion_status() );
        }
    }


    /**
    * Find the adapter ID.
    */
    public byte[] adapter_id()
    {
        synchronized ( m_sync_state )
        {
            if ( m_target == null )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );
            }
            return m_target.getAdapterID();
        }
    }

    /**
    * Find the object ID.
    */
    public byte[] object_id()
    {
        synchronized ( m_sync_state )
        {
            if ( m_target == null )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );
            }
            return m_target.getObjectID();
        }
    }

    /**
    * Most derrived repository ID of the target.
    */
    public String target_most_derived_interface()
    {
        synchronized ( m_sync_state )
        {
            if ( m_target == null )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );
            }
            return m_target.getRepositoryID();
        }
    }

    /**
     * Determine if the target implements the given interface.
     */
    public boolean target_is_a( String id )
    {
        synchronized ( m_sync_state )
        {
            if ( m_target == null )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 14,
                        state_completion_status() );
            }
            return m_target.targetIsA( id );
        }
    }

    /**
     * Call predispatch to locate the target, enter the PROCESSING state,
     * call the receive_request interception points and dispatch the request
     * through the adapter. If sync scope is SYNC_WITH_SERVER an empty response
     * will be sent before the request is dispatched. When this function returns
     * all the terminating interception points will have been called, and
     * state will be COMPLETE. If the target adapter is destroyed between the
     * find_adapter and dispatch requests this function will throw an
     * AdapterDestroyedException and the CREATED state is re-entered,
     * find_adapter function will be called again once the unregister adapter
     * operation is called on the server manager with the target adapter.
     */
    public void dispatch()
        throws AdapterDestroyedException
    {
        // predispatch
        synchronized ( m_sync_state )
        {
            switch ( m_state )
            {

            case STATE_COMPLETE:
                // request canceled
                return;

            case STATE_QUEUED:
                break;

            default:
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );
            }

            try
            {
                m_target = m_adapter.predispatch( this );
            }
            catch ( AdapterDestroyedException ex )
            {
                m_state = STATE_FIND_ADAPTER;
                throw ex;
            }
            catch ( org.omg.PortableInterceptor.ForwardRequest ex )
            {
                handle_location_forward( ex.forward, false );
            }
            catch ( org.omg.CORBA.SystemException ex )
            {
                ex.completed = CompletionStatus.COMPLETED_NO;
                handle_system_exception( ex );
            }
            catch ( Error ex )
            {
                handle_error( ex );
            }
            catch ( RuntimeException ex )
            {
                handle_runtime_exception( ex );
            }

            // exception thrown during predispatch
            if ( m_reply_status != REPLY_STATUS_UNSET )
            {
                complete_request();
                return;
            }

            // call interceptors.
            if ( m_interceptor_manager != null )
            {
                m_interceptor_manager.receive_request( this, m_callback );

                // handle interceptor rejection.
                if ( m_reply_status != REPLY_STATUS_UNSET )
                {
                    // interceptors have forced completion. Cancel the dispatch
                    // and complete the request.
                    m_interceptor_manager = null;
                    m_adapter.cancel_dispatch( this, m_target );

                    complete_request();
                    return;
                }
            }

            // set the request current table back to the thread scope current
            // discarding any changes made during recieve_request
            m_rs_pi_curr_entry = m_ts_pi_curr_entry;

            // send reply if needed and handle interception points for oneway calls.
            switch ( m_sync_scope )
            {

            case org.omg.Messaging.SYNC_NONE.value:
                // no need to send a reply. release the request.
                // From here on the request cannot be canceled from the client end.
                release_request();
                break;

            case org.omg.Messaging.SYNC_WITH_TRANSPORT.value:

            case org.omg.Messaging.SYNC_WITH_SERVER.value:
                // NOTE: we may possibly persist the request here, then it's asynchonous
                // and reliable.

                // send the empty reply back to the client indicating an acceptance
                try
                {
                    m_reply_stream = begin_marshal_reply();
                    complete_reply( m_reply_stream );
                }
                catch ( org.omg.CORBA.SystemException ex )
                {
                    ex.completed = CompletionStatus.COMPLETED_NO;
                    handle_system_exception( ex );
                }
                catch ( Error ex )
                {
                    handle_error( ex );
                }
                catch ( RuntimeException ex )
                {
                    handle_runtime_exception( ex );
                }

                // release the request. From here on in it's an unstoppable force
                // from the client end.
                release_request();

                // exception while sending blank response. Treat as client cancel.
                if ( m_reply_status != REPLY_STATUS_UNSET )
                {
                    m_state = STATE_COMPLETE;
                    m_adapter.cancel_dispatch( this, m_target );

                    if ( m_interceptor_manager != null )
                    {
                        m_interceptor_manager.send_exception( this, m_callback );
                    }
                    return;
                }

                break;
            }

            m_state = STATE_PROCESSING;
        }

        // dispatching the request is performed outside of the synchronized
        // block to allow cancel requests to get processed.

        Throwable tex = null;

        int ex_type;

        try
        {
            m_adapter.dispatch( this, m_target );
            ex_type = 0;
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            tex = ex;
            ex_type = 1;
        }
        catch ( Error ex )
        {
            tex = ex;
            ex_type = 2;
        }
        catch ( RuntimeException ex )
        {
            tex = ex;
            ex_type = 3;
        }

        synchronized ( m_sync_state )
        {
            // request has been canceled.
            if ( m_state == STATE_COMPLETE )
            {
                return;
            }
            switch ( ex_type )
            {

            case 0:

                if ( m_state == STATE_PROCESSING )
                {
                    if ( is_locate() )
                    {
                        // successful result for a locate request.
                        m_reply_status = OBJECT_HERE;
                    }
                    else if ( m_sync_scope == org.omg.Messaging.SYNC_WITH_TARGET.value )
                    {
                        // create an empty response if none has been created.
                        try
                        {
                            createReply();
                        }
                        catch ( org.omg.CORBA.SystemException ex )
                        {
                            ex.completed = CompletionStatus.COMPLETED_YES;
                            handle_system_exception( ex );
                        }
                        catch ( Error ex )
                        {
                            handle_error( ex );
                        }
                        catch ( RuntimeException ex )
                        {
                            handle_runtime_exception( ex );
                        }
                    }
                    else
                    {
                        // a successful asynchronous request set reply status to successful
                        m_reply_status = org.omg.PortableInterceptor.SUCCESSFUL.value;
                    }
                }

                break;

            case 1:

                if ( m_state == STATE_REPLY )
                {
                    ( ( org.omg.CORBA.SystemException ) tex ).completed =
                            CompletionStatus.COMPLETED_YES;
                }
                handle_system_exception( ( org.omg.CORBA.SystemException ) tex );

                break;

            case 2:
                handle_error( ( Error ) tex );

                break;

            case 3:
                handle_runtime_exception( ( RuntimeException ) tex );

                break;
            }

            complete_request();
        }
    }

    /**
     * Get the request input stream. This function can be called only once
     * while in the PROCESSING state. The returned input stream may throw a
     * system exception if some problem occours while unmarshalling the request
     * arguments. This function, and the reply functions must be called by the
     * same thread, the thread that called the dispatch function.
     */
    public org.omg.CORBA.portable.InputStream argument_stream()
    {
        return m_argument_stream;
    }

    /**
     * Add a service context to the reply.
     */
    public void add_reply_service_context( org.omg.IOP.ServiceContext service_context,
            boolean replace )
    {
        Integer key = NumberCache.getInteger( service_context.context_id );

        synchronized ( m_service_contexts )
        {
            if ( !replace && m_service_contexts.containsKey( key ) )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 11,
                        state_completion_status() );
            }
            m_service_contexts.put( key, service_context );
        }
    }

    /**
     * Find a reply service context with a given ID.
     */
    public org.omg.IOP.ServiceContext get_reply_service_context( int id )
    {
        org.omg.IOP.ServiceContext ret = null;

        synchronized ( m_service_contexts )
        {
            ret = ( org.omg.IOP.ServiceContext ) m_service_contexts.get(
                    NumberCache.getInteger( id ) );
        }

        if ( ret == null )
        {
            if ( s_throw_service_context_exceptions )
            {
                throw new org.omg.CORBA.BAD_PARAM( org.omg.CORBA.OMGVMCID.value | 23,
                        state_completion_status() );
            }
            else
            {
                return null;
            }
        }
        return ret;
    }

    /**
     * Get a list of all the reply service contexts which been set.
     */
    public org.omg.IOP.ServiceContext [] get_reply_service_contexts()
    {
        synchronized ( m_service_contexts )
        {
            return ( org.omg.IOP.ServiceContext[] ) m_service_contexts.values().toArray(
                    new org.omg.IOP.ServiceContext[ m_service_contexts.size() ] );
        }
    }

    /**
     * Get the reply status. This operation is available in the REPLY and COMPLETE
     * states.
     */
    public short reply_status()
    {
        synchronized ( m_sync_state )
        {
            if ( m_reply_status == REPLY_STATUS_UNSET )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                        state_completion_status() );
            }
            return m_reply_status;
        }
    }

    /**
     * The forward reference which will be sent in response to the
     * request.
     */
    public org.omg.CORBA.Object forward_reference()
    {
        synchronized ( m_sync_state )
        {
            switch ( reply_status() )
            {

            case org.omg.PortableInterceptor.LOCATION_FORWARD.value:
                return m_forward_reference;
            }

            throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                    state_completion_status() );
        }
    }

    /**
     * The IOR of the forward reference which will be sent in response to
     * the request.
     */
    public org.omg.IOP.IOR forward_reference_ior()
    {
        synchronized ( m_sync_state )
        {
            // reply_status throws bad inv order if not in complete state.
            switch ( reply_status() )
            {

            case org.omg.PortableInterceptor.LOCATION_FORWARD.value:

                if ( m_forward_reference_ior == null )
                {
                    m_forward_reference_ior = ( ( org.openorb.orb.core.Delegate )
                            ( ( org.omg.CORBA.portable.ObjectImpl )
                            m_forward_reference )._get_delegate() ).ior();
                }
                return m_forward_reference_ior;
            }

            throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                    state_completion_status() );
        }
    }

    /**
     * Exception being sent in reply. This will only contain system exceptions.
     */
    public org.omg.CORBA.Any sending_exception()
    {
        synchronized ( m_sync_state )
        {
            // reply_status throws bad inv order if not in complete state.
            switch ( reply_status() )
            {

            case org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value:

                if ( m_sending_system_exception_any == null )
                {
                    m_sending_system_exception_any = m_orb.create_any();
                    SystemExceptionHelper.insert(
                           m_sending_system_exception_any, m_sending_system_exception );
                }

                return m_sending_system_exception_any;

            case org.omg.PortableInterceptor.USER_EXCEPTION.value:
                throw new org.omg.CORBA.NO_RESOURCES( org.omg.CORBA.OMGVMCID.value | 1,
                       state_completion_status() );
            }

            throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                    state_completion_status() );
        }
    }

    /**
     * System exception being sent as a reply.
     */
    public org.omg.CORBA.SystemException sending_system_exception()
    {
        synchronized ( m_sync_state )
        {
            // reply_status throws bad inv order if not in complete state.
            if ( reply_status() == org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value )
            {
                return m_sending_system_exception;
            }
            throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                    state_completion_status() );
        }
    }

    /**
     * Repository ID of the system exception being sent as a reply.
     */
    public String sending_system_exception_id()
    {
        synchronized ( m_sync_state )
        {
            // reply_status throws bad inv order if not in complete state.
            if ( reply_status() == org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value )
            {
                return m_sending_system_exception_id;
            }
            throw new org.omg.CORBA.BAD_INV_ORDER( org.omg.CORBA.OMGVMCID.value | 10,
                    state_completion_status() );
        }
    }

    private void handle_system_exception( org.omg.CORBA.SystemException ex )
    {
        m_sending_system_exception_any = null;
        m_sending_system_exception = ex;
        m_sending_system_exception_id = SystemExceptionHelper.id( ex );
        m_reply_status = org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value;
    }

    private void handle_error( Error ex )
    {

        if ( ex instanceof ThreadDeath )
        {
            throw ex;
        }
        else if ( ex instanceof OutOfMemoryError )
        {
            handle_system_exception(
                    new org.omg.CORBA.NO_MEMORY( 0, state_completion_status() ) );
        }
        else if ( ex instanceof StackOverflowError )
        {
            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().warn( "Look like ORB has unbounded recursion", ex );
            }
            handle_system_exception(
                    new org.omg.CORBA.NO_RESOURCES( 0, state_completion_status() ) );
        }
        else
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Intercepted in AbstractServerRequest", ex );
            }
            org.omg.CORBA.portable.UnknownException uex =
                    new org.omg.CORBA.portable.UnknownException( ex );
            uex.completed = state_completion_status();
            handle_system_exception( uex );
        }
    }

    private void handle_runtime_exception( RuntimeException ex )
    {
        if ( getLogger().isErrorEnabled() )
        {
            getLogger().error( "Handle runtime exception", ex );
        }
        org.omg.CORBA.portable.UnknownException uex =
                new org.omg.CORBA.portable.UnknownException( ex );
        uex.completed = state_completion_status();
        handle_system_exception( uex );
    }

    private void handle_unknown_throwable( Throwable ex )
    {
        if ( getLogger().isErrorEnabled() )
        {
            getLogger().error( "Handle unknown throwable", ex );
        }
        org.omg.CORBA.portable.UnknownException uex =
                 new org.omg.CORBA.portable.UnknownException( ex );
        uex.completed = state_completion_status();
        handle_system_exception( uex );
    }

    private void handle_location_forward( org.omg.CORBA.Object forward, boolean permanent )
    {
        m_forward_reference = forward;
        m_forward_reference_ior = null;
        m_reply_status = org.omg.PortableInterceptor.LOCATION_FORWARD.value;
    }

    public org.omg.CORBA.portable.OutputStream createReply()
    {
        synchronized ( m_sync_state )
        {
            if ( is_locate() )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( MinorCodes.BAD_INV_ORDER_SERVER,
                        state_completion_status() );
            }
            // throw cancelation exception
            if ( m_reply_status == org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value )
            {
                throw m_sending_system_exception;
            }
            // If if we have a oneway request return a dummy buffer for
            // marshaling the response.
            if ( m_sync_scope != org.omg.Messaging.SYNC_WITH_TARGET.value )
            {
                return m_orb.create_output_stream();
            }
            m_reply_status = org.omg.PortableInterceptor.SUCCESSFUL.value;

            if ( m_interceptor_manager != null )
            {
                m_interceptor_manager.send_reply( this, m_callback );
                m_interceptor_manager = null;

                if ( m_reply_status == org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value )
                {
                    // set the interceptor manager to null, the request sequence
                    // is complete.
                    throw m_sending_system_exception;
                }
            }

            m_state = STATE_REPLY;
            m_reply_stream = begin_marshal_reply();
            return m_reply_stream;
        }
    }

    public org.omg.CORBA.portable.OutputStream createExceptionReply()
    {
        synchronized ( m_sync_state )
        {
            if ( is_locate() )
            {
                throw new org.omg.CORBA.BAD_INV_ORDER( MinorCodes.BAD_INV_ORDER_SERVER,
                        state_completion_status() );
            }
            // If in the complete state due to a cancelation or we have a oneway
            // request we return a dummy buffer for marshaling the response.
            if ( m_state == STATE_COMPLETE
                  || m_sync_scope != org.omg.Messaging.SYNC_WITH_TARGET.value )
            {
                if ( m_reply_status == org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value )
                {
                    throw m_sending_system_exception;
                }
                return m_orb.create_output_stream();
            }

            m_reply_status = org.omg.PortableInterceptor.USER_EXCEPTION.value;

            if ( m_interceptor_manager != null )
            {
                m_interceptor_manager.send_exception( this, m_callback );
                m_interceptor_manager = null;

                if ( m_reply_status == org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value )
                {
                    // set the interceptor manager to null, the request sequence
                    // is complete.
                    throw m_sending_system_exception;
                }
            }

            m_state = STATE_REPLY;
            m_reply_stream = begin_marshal_user_exception();
            return m_reply_stream;
        }
    }

    /**
     * call interceptors and send response. syncState must be owned.
     */
    private void complete_request()
    {
        if ( !( m_reply_status != REPLY_STATUS_UNSET ) )
        {
            org.openorb.orb.util.Trace.signalIllegalCondition( getLogger(),
                    "Completing request without setting reply status." );
        }

        if ( m_state != STATE_CREATED && m_interceptor_manager != null )
        {
            switch ( m_reply_status )
            {

            case org.omg.PortableInterceptor.SUCCESSFUL.value:
                m_interceptor_manager.send_reply( this, m_callback );
                break;

            case org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value:
                m_interceptor_manager.send_exception( this, m_callback );
                break;

            case org.omg.PortableInterceptor.LOCATION_FORWARD.value:
                m_interceptor_manager.send_other( this, m_callback );
                break;
            }
        }
        // send a reply if needed.
        switch ( m_sync_scope )
        {

        case 1:   /* org.omg.Messaging.SYNC_WITH_TRANSPORT.value */

        case 2:   /* org.omg.Messaging.SYNC_WITH_SERVER.value */

            if ( m_state < STATE_PROCESSING )
            {
                break;
            }
            // fallthrough

        case 0:   /* org.omg.Messaging.SYNC_NONE.value */
            // release_request called allready, take no action.
            m_state = STATE_COMPLETE;

            return;
        }

        // construct or complete a reply.

        // NOTE: the REPLY state is only entered if a reply has begun to be
        // constructed. At this stage any exception will either be a problem
        // with marshaling, or a comm channel loss. In both cases we just send
        // a new reply containing the exception. This may cause problems if the
        // reply is fragmented, and the client does not understand that an
        // exception following a fragment message results in an exception.

        try
        {
            // send reply message.
            switch ( m_reply_status )
            {

            case OBJECT_HERE:
                marshal_locate_reply( true );
                break;

            case UNKNOWN_OBJECT:
                marshal_locate_reply( false );
                break;

            case org.omg.PortableInterceptor.SUCCESSFUL.value:

            case org.omg.PortableInterceptor.USER_EXCEPTION.value:
                complete_reply( m_reply_stream );
                break;

            case org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value:
                marshal_system_exception( m_sending_system_exception_id,
                        m_sending_system_exception );
                break;

            case org.omg.PortableInterceptor.LOCATION_FORWARD.value:
                marshal_forward_request( m_forward_reference, false );
                break;
            }
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
               getLogger().error( "Transport failure while replying to request: ", ex );
            }
        }
        catch ( Error ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
               getLogger().error( "Java error while replying to request", ex );
            }
        }
        catch ( RuntimeException ex )
        {
            if ( getLogger().isErrorEnabled() )
            {
               getLogger().error( "Java runtime exception while replying to request", ex );
            }
        }

        m_state = STATE_COMPLETE;

        release_request();
    }

    /**
     * Send a system exception result. Note that a failed locate request will
     * always result in an OBJECT_NOT_FOUND system exception, which this
     * function is free to convert into a failed locate reply. This may
     * throw a system exception indicating a transport problem.
     */
    protected abstract void marshal_system_exception( String repo_id,
            org.omg.CORBA.SystemException ex );

    /**
     * Send a forward request result. This may throw a system
     * exception indicating a transport problem.
     */
    protected abstract void marshal_forward_request( org.omg.CORBA.Object target,
            boolean permanent );

    /**
     * Reply to a locate request. This argument to this function will be true
     * when called from this class, however marshal_system_exception may convert
     * a system exception response into a locate failure. This may throw a system
     * exception indicating a transport problem.
     */
    protected abstract void marshal_locate_reply( boolean object_is_here );

    /**
     * Create a stream for marshaling a successful response. This is paired
     * with a call to complete_marshal. The returned stream may throw a system
     * exception at any time to indicate transport problems.
     */
    protected abstract org.omg.CORBA.portable.OutputStream begin_marshal_reply();

    /**
     * Create a stream for marshaling a user exception response. This is paired
     * with a call to complete_marshal. The returned stream may throw a system
     * exception at any time to indicate transport problems.
     */
    protected abstract org.omg.CORBA.portable.OutputStream begin_marshal_user_exception();

    /**
     * Complete the marshaling process. Paired with a call to begin_marshal_* .
     * This may throw a system exception to indicate transport problems.
     */
    protected abstract void complete_reply( org.omg.CORBA.portable.OutputStream os );

    /**
     * Release any resources associated with the request. This is called when the
     * complete state is entered.
     */
    protected abstract void release_request();

    /**
     * Return current logger
     */
    protected Logger getLogger()
    {
        return m_logger;
    }

    public String toString()
    {
        return this.getClass().getName() + ' ' + m_request_id + '(' + super.toString() + ')';
    }
}

