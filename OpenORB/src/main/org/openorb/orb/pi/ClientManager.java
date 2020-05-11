/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import org.omg.PortableInterceptor.ClientRequestInfo;

/**
 * This interface defines operations that must be implemented by a client
 * request interceptor manager.<p>
 * Overrides must have a constructor with exact signature:
 * <pre>
 * public ClientManager(
 *     org.omg.PortableInterceptor.ClientRequestInterceptor [] list,
 *     org.openorb.orb.pi.CurrentImpl current )
 * </pre>
 * The default implementation can be overriden by setting the
 * openorb.PI.ClientManagerClass property with the classname of the
 * override. To disable client side interception set this property to the
 * empty string.<p>
 * Each interception point must manage pushes and pops to the PICurrent as
 * each interceptor is called.<p>
 * It is allowable to call interceptors in different threads, however
 * calls to the request callback must occour in the same thread as the one
 * which called this function.
 *
 * @author Jerome Daniel
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:51 $
 */
public interface ClientManager
{
    /**
     * Handles the send_request interception point.
     *
     * @param info The client request descriptor.
     * @param cb The callback instance.
     */
    void send_request( ClientRequestInfo info, RequestCallback cb );

    /**
     * Handles the send_poll interception point.
     *
     * @param info The client request descriptor.
     * @param cb The callback instance.
     */
    void send_poll( ClientRequestInfo info, RequestCallback cb );

    /**
     * Handles the recieve_reply interception point.
     *
     * @param info The client request descriptor.
     * @param cb The callback instance.
     */
    void receive_reply( ClientRequestInfo info, RequestCallback cb );

    /**
     * Handles the recieve_exception interception point.
     *
     * @param info The client request descriptor.
     * @param cb The callback instance.
     */
    void receive_exception( ClientRequestInfo info, RequestCallback cb );

    /**
     * Handles the recieve_other interception point.
     *
     * @param info The client request descriptor.
     * @param cb The callback instance.
     */
    void receive_other( ClientRequestInfo info, RequestCallback cb );
}

