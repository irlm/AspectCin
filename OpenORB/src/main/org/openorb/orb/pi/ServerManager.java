/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import org.omg.PortableInterceptor.ServerRequestInfo;

/**
 * This interface describes all operations that must be implemented to
 * provide a server request interceptor manager.<p>
 * Overrides must have a constructor with exact signature:
 * <pre>
 * public ServerManager(
 *         org.omg.PortableInterceptor.ServerRequestInterceptor [] list,
 *         org.openorb.orb.pi.CurrentImpl current)
 * </pre>
 * The default implementation can be overriden by setting the
 * openorb.PI.ServerManagerClass property with the classname of the
 * override. To disable server side interception set this property to the
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
public interface ServerManager
{
    /**
     * Handles the recieve_request_service_contexts interception point.
     */
    void receive_request_service_contexts( ServerRequestInfo info,
            RequestCallback cb );

    /**
     * Handles the recieve_request interception point.
     */
    void receive_request( ServerRequestInfo info, RequestCallback cb );

    /**
     * Handles the send_reply interception point.
     */
    void send_reply( ServerRequestInfo info, RequestCallback cb );

    /**
     * Handles the send_exception interception point.
     */
    void send_exception( ServerRequestInfo info, RequestCallback cb );

    /**
     * Handles the send_other interception point.
     */
    void send_other( ServerRequestInfo info, RequestCallback cb );
}

