/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

/**
 * Interface for client requests. The client request holds all the data
 * concering a request. The client request object is responsible for request
 * state management, managing interception points and the various policies
 * affecting a single request.
 *
 * @author Chris Wood
 * @version $Revision: 1.6 $ $Date: 2004/07/22 12:25:45 $
 */
public interface ClientRequest
    extends org.omg.PortableInterceptor.ClientRequestInfo
{
    /**
     * Client channel
     */
    ClientChannel channel();

    /**
     * Client address
     */
    Address address();

    /**
     * ORB associated with the target.
     */
    org.omg.CORBA.ORB orb();

    /**
     * Get the target IOR. This calls ior() on the object passed with creation
     */
    org.omg.IOP.IOR target_ior();

    /**
     * Get the effective target IOR. This gets the IOR from the address passed
     * in creation. Calling this function instead of effective_target avoids
     * constructing an enclosing delegate/object.
     */
    org.omg.IOP.IOR effective_target_ior();

    /**
     * Current request state.
     */
    int state();

    /**
     * Cancel the request with the specified system exception reply. This is
     * valid in any state apart from COMPLETE. This may result in cancel messages
     * being sent to the server. If the cancel is successful state changes to
     * COMPLETED/SYSTEM_EXCEPTION. If the provided exception's status is set
     * to null the exception thown will set the exception status according to
     * the request's state.
     *
     * @return true if the request is succesfully canceled.
     */
    boolean cancel( org.omg.CORBA.SystemException ex );

    /**
     * This returns true if this request is a standard request.
     *
     * While in the CREATED state it is valid to call any client request
     * info operations which would be valid in the client side
     * interception points send_request. The send_request interception
     * points are called while in this state.
     */
    boolean is_request();

    /**
     * Request is a poll.
     *
     * While in the CREATED state it is valid to call any client request
     * info operations which would be valid in the client side
     * interception points send_poll. The send_poll interception
     * points are called while in this state.
     */
    boolean is_poll();

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
     * Note the extra values for reply_status, UNKNOWN_OBJECT and OBJECT_HERE
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
     * Request created. Client side interception points send_request/send_poll
     * are called while in this state.
     */
    int STATE_CREATED = 0;

    /**
     * Begin marshalling arguments. Valid in CREATED state. All client side
     * interception points send_request/send_poll complete and state changes to
     * MARSHAL. If no arguments are required for marshalling this can enter
     * the MARSHAL state and return null, this occours for locate requests and
     * polls only. This may also return null if entering
     * COMPLETE/SYSTEM_EXCEPTION/COMPLETED_NO, COMPLETE/LOCATION_FORWARD or
     * COMPLETE/LOCATION_FORWARD_PERMINENT due to communication problems or
     * client side interceptors.
     */
    org.omg.CORBA.portable.OutputStream begin_marshal();

    /**
     * Get all request service contexts that have been set. From MARSHAL onward
     * this will be a constant list.
     */
    org.omg.IOP.ServiceContext [] get_request_service_contexts();

    /**
     * While in MARSHAL arguments are marshaled into the input stream and message
     * fragments may be sent.
     */
    int STATE_MARSHAL = 1;

    /**
      * When this returns the last fragment of the request has been
      * sent. In MARSHAL state changes to WAITING or COMPLETE. This may
      * result in entering COMPLETE/SYSTEM_EXCEPTION/COMPLETED_NO,
      * COMPLETE/LOCATION_FORWARD or COMPLETE/LOCATION_FORWARD_PERMINENT due to
      * communication problems.
      *
      * @return new state. May be WAITING or COMPLETE.
      */
    int send_request();

    /**
     * Waiting for a response from the server.
     */
    int STATE_WAITING = 2;

    /**
     * Poll to see if a response is available from the target. If in the WAITING
     * state this will return true if wait_for_response would not have to wait
     * for a response. In the UNMARSHAL and COMPLETE states this returns true,
     * It is illegal to call this function in all other states.
     */
    boolean poll_response();

    /**
     * Wait for a response from the server. When this returns either the given
     * wait_time has expired (WAITING), a response has arrived from the server
     * (UNMARSHAL or COMPLETE), a transport error has occoured (COMPLETE), or
     * for requests where no response is expected the sync scope is satisfied
     * (COMPLETE). The request state will not exit the WAITING state unless
     * this function is called.
     *
     * @param timeout Maximum amount of time to wait for response.
     *                0 to wait forever, > 0 for some timeout (in ms)
     * @return new state. May be WAITING, UNMARSHAL or COMPLETE.
     */
    int wait_for_response( long timeout );

    /**
     * Response is being unmarshaled. This state is reached when a reply
     * is arriving and reply_status is either SUCESSFUL or USER_EXCEPTION.
     * Terminating client side interceptors will not have been called yet.
     */
    int STATE_UNMARSHAL = 3;

    /**
     * Get the response output stream. This function is valid in the
     * UNMARSHAL state when reply_status is SUCESSFUL or USER_EXCEPTION.
     * The returned input stream may throw a system exception with status
     * COMPLETED_YES at any time.
     */
    org.omg.CORBA.portable.InputStream receive_response();

    /**
     * Request is complete. Last fragment has been unmarshalled and all
     * terminating interception points have occoured. To determine the exact
     * response call the reply_status function.
     */
    int STATE_COMPLETE = 4;

    /**
     * Get the ior associated with a forward reference. Calling this function
     * instead of forward_reference avoids creating an enclosing delegate/object.
     * This function is valid in the COMPLETE/LOCATION_FORWARD.
     */
    org.omg.IOP.IOR forward_reference_ior();

    /**
     * Get the system exception which would be contained in the any returned
     * from receive_exception. This function is valid in the
     * COMPLETE/SYSTEM_EXCEPTION state.
     */
    org.omg.CORBA.SystemException received_system_exception();

    /**
     * Get the ID of the system exception which would be contained in the any returned
     * from receive_exception. This function is valid in the
     * COMPLETE/SYSTEM_EXCEPTION, UNMARSHAL/USER_EXCEPTION and
     * COMPLETE/USER_EXCEPTION states.
     */
    String received_exception_id();
}

