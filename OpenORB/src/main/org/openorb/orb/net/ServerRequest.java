/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import org.openorb.orb.adapter.ObjectAdapter;
import org.openorb.orb.adapter.AdapterDestroyedException;

/**
 * Interface for server requests. The server request holds all the data
 * concering a request. It is responsible for interpreting incoming requests,
 * locating an adapter containing the target servant, calling interception
 * points and construting a reply.
 *
 * @author Unknown
 */
public interface ServerRequest
    extends org.omg.PortableInterceptor.ServerRequestInfo,
    org.omg.CORBA.portable.ResponseHandler
{
    /**
     * ORB reference.
     */
    org.omg.CORBA.ORB orb();

    /**
     * Server channel
     */
    ServerChannel channel();

    /**
     * Current request state.
     */
    int state();

    /**
     * Get the object_key for the request. This is always available.
     */
    byte [] object_key();

    /**
     * Get all request service contexts that have been set. Valid in REPLY and
     * COMPLETE.
     */
    org.omg.IOP.ServiceContext [] get_reply_service_contexts();

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
    boolean is_locate();

    /**
     * Extra reply status denoting a locate request found the object.
     */
    short OBJECT_HERE = -1;

    /**
     * Extra reply status denoting a locate request where the object is not found
     */
    short UNKNOWN_OBJECT = -2;

    /**
     * The request has arrived but has not had any of it's service contexts
     * examined. A cancelation in this state will result in COMPLETED_NO status.
     * Calls valid in receive_request_service_contexts are valid, this
     * interception point will occour while in this state if the request is
     * not a locate request.
     */
    int STATE_CREATED = 0;

    /**
     * Client intiated request cancelation. This may be called at any time to
     * indicate that a response from the request is no longer expected.
     */
    void client_cancel();

    /**
     * Server intiated request cancelation. This may be called at any time apart
     * from during a find_adapter or dispatch call to indicate a server
     * initiated cancel. This will send a reply to the client.
     */
    void server_cancel( Throwable ex );

    /**
     * Called by the worker thread before every attempt to run the request. This
     * should call the recieve_request_service_context interception point.
     * @return the state of the request.
     */
    int begin_request();

    /**
     * Set the object adapter and transfer to the QUEUED state. This is legal
     * only in the FIND_ADAPTER or COMPLETE states. If COMPLETE this has no effect.
     *
     * @return the state of the request. This will be either QUEUED or COMPLETE.
     */
    int adapter( ObjectAdapter adapter );

    /**
     * Server adapter. This operation is valid in all states apart from
     * CREATED. When a dispatch operation fails this will return the original
     * adapter.
     */
    ObjectAdapter adapter();

    /**
     * This state is entered if a parent adapter is either holding requests
     * or is in the process of being destroyed. To continue find_adapter must
     * be called again.
     */
    int STATE_FIND_ADAPTER = 1;

    /**
     * Request has had an adapter found and is enqueued awaiting dispatch.
     * Calls valid in receive_request_service_contexts are valid. Request
     * fragments may still be arriving.
     */
    int STATE_QUEUED = 2;

    /**
     * Dispatch a request to it's destination. This is valid in the QUEUED state
     * Call predispatch on the adapter to locate the target, call the
     * receive_request interception points, enter the PROCESSING state and
     * dispatch the request through the adapter. If sync scope is
     * SYNC_WITH_SERVER an empty response will be sent before the request
     * is dispatched. When this function returns all the terminating
     * interception points will have been called, and state will be COMPLETE.
     * If the target adapter is destroyed between the find_adapter and dispatch
     * this function will return false and the CREATED state is re-entered, in
     * this case the find_adapter function will be called again once the
     * unregister adapter operation is called on the server manager with the
     * target adapter.
     */
    void dispatch()
        throws AdapterDestroyedException;

    /**
     * Request is being processed. This is the only state where the
     * ReplyHandler functions can be successfully called. Request
     * fragments may still be arriving.
     */
    int STATE_PROCESSING = 3;

    /**
     * Get the request input stream. This function can be called only once
     * while in the PROCESSING state. The returned input stream may throw a
     * system exception if some problem occours while unmarshalling the request
     * arguments. This function, and the reply functions must be called by the
     * same thread, the thread that called the dispatch function.
     */
    org.omg.CORBA.portable.InputStream argument_stream();


    // response handler interface.

    /**
     * Create a stream for marshaling the reply. The send_reply interception
     * points are called, if this results in a system exception the exception
     * is thrown, otherwise a stream is retured. The returned stream may raise
     * a system exception when attempting to write to it indicating a
     * transmission problem. Such an exception is not reported to interceptors.
     * This function is inherited from the ResponseHandler interface.
     */
    org.omg.CORBA.portable.OutputStream createReply();

    /**
     * Create a stream for marshaling a user exception. The send_exception
     * interception points are called, if this results in a system exception the
     * exception is thrown, otherwise a stream is retured. The returned stream
     * may raise a system exception when attempting to write to it indicating a
     * transmission problem. Such an exception is not reported to interceptors.
     * This function is inherited from the ResponseHandler interface.
     */
    org.omg.CORBA.portable.OutputStream createExceptionReply();

    /**
     * Replying, one of the response handlers functions have been called or an
     * an internal response is being constructed. The send_* interception
     * points are called in this state.
     */
    int STATE_REPLY = 4;

    /**
     * Request has completed. Terminating state.
     */
    int STATE_COMPLETE = 5;

    /**
     * Get the ior associated with a forward reference. Calling this function
     * instead of forward_reference avoids creating an enclosing delegate/object.
     * This function is valid in REPLY and COMPLETE states where reply_status
     * would return LOCATION_FORWARD.
     */
    org.omg.IOP.IOR forward_reference_ior();

    /**
     * Get the system exception which would be contained in the any returned
     * from sending_exception. This function is valid in REPLY and COMPLETE
     * states where reply_status would return SYSTEM_EXCEPTION.
     */
    org.omg.CORBA.SystemException sending_system_exception();

    /**
     * Get the ID of the system exception which would be contained in the
     * any returned from sending_exception. This function is valid in REPLY
     * and COMPLETE states where reply_status would return SYSTEM_EXCEPTION.
     */
    String sending_system_exception_id();
}

