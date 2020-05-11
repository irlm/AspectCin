/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import org.omg.CORBA.portable.OutputStream;

/**
 * This class represents a server request for a server that is hosted on the same ORB
 * than the client
 *
 * @author <a href="erik.putrycz@int-evry.fr">Erik Putrycz</a>
 * @version $Revision: 1.3 $ $Date: 2003/11/27 22:38:47 $
 */

public final class LocalServerRequest extends org.openorb.orb.net.AbstractServerRequest
{

    private OutputStream m_reply_stream;
    private org.omg.CORBA.portable.InputStream m_argument_stream;
    // if the reply is an exception
    private boolean m_exception = false;

    /**
     * Constructor.
     * @param serverManager The server manager associated with this request.
     * @param request_id The request id of this request.
     * @param argument_stream the argument stream that will be given to the server
     * @param object_id The object id of this request.
     * @param operation The name of the operation to execute.
     * @param sync_scope The synchronization scope (See Messaging spec.)
     * @param request_service_contexts An array of service contexts. associated
     * with this request.
     */
    public LocalServerRequest ( ServerManager serverManager, int request_id,
                                org.omg.CORBA.portable.InputStream argument_stream,
                                byte [] object_id, String operation, byte sync_scope,
                                org.omg.IOP.ServiceContext[] request_service_contexts )
    {
        super ( serverManager, null, request_id, argument_stream, object_id,
                operation, sync_scope, request_service_contexts );
        m_argument_stream = argument_stream;
        m_reply_stream = new org.openorb.orb.io.LocalOutputStream ();
    }

    /** Create a stream for marshaling a successful response. This is paired
     * with a call to complete_marshal. The returned stream may throw a system
     * exception at any time to indicate transport problems.
     */
    protected OutputStream begin_marshal_reply ()
    {
        return m_reply_stream;
    }


    /** Returns the ReplyStream of this request
     * @return the request output stream
     */
    public OutputStream getReplyStream ()
    {
        return m_reply_stream;
    }


    /** I removed all the additional marshalling parameters
     * for exception and I kept only the effective arguments
     *
     * @return true if the reply of this request is an exception
     */
    public boolean is_reply_exception ()
    {
        return m_exception;
    }

    /** Create a stream for marshaling a user exception response. This is paired
     * with a call to complete_marshal. The returned stream may throw a system
     * exception at any time to indicate transport problems.
     */
    protected OutputStream begin_marshal_user_exception ()
    {
        m_exception = true;
        return m_reply_stream;
    }

    /** Complete the marshaling process. Paired with a call to begin_marshal_* .
     * This may throw a system exception to indicate transport problems.
     */
    protected void complete_reply ( OutputStream os )
    {
        // nothing to do this is a local call
    }

    /** Send a forward request result. This may throw a system
     * exception indicating a transport problem.
     */
    protected void marshal_forward_request ( org.omg.CORBA.Object target, boolean permanent )
    {
        // nothing to do this is a local call
    }

    /** Reply to a locate request. This argument to this function will be true
     * when called from this class, however marshal_system_exception may convert
     * a system exception response into a locate failure. This may throw a system
     * exception indicating a transport problem.
     */
    protected void marshal_locate_reply ( boolean object_is_here )
    {
        // nothing to do this is a local call
    }

    /** Send a system exception result. Note that a failed locate request will
     * always result in an OBJECT_NOT_FOUND system exception, which this
     * function is free to convert into a failed locate reply. This may
     * throw a system exception indicating a transport problem.
     */
    protected void marshal_system_exception ( String repo_id, org.omg.CORBA.SystemException ex )
    {
        // nothing to do this is a local call
    }

    /** Release any resources associated with the request. This is called when the
     * complete state is entered.
     */
    protected void release_request ()
    {
        // nothing to do this is a local call
    }
}

