/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.CurrentOperations;

import org.apache.avalon.framework.logger.Logger;
import org.openorb.util.ExceptionTool;

/**
 * This interface describes all operations that must be implemented to provide a server request
 * interceptor manager.
 *
 * @author Jerome Daniel
 * @author Chris Wood
 * @version $Revision: 1.7 $ $Date: 2004/02/10 22:09:10 $
 */
public class SimpleServerManager
    implements org.openorb.orb.pi.ServerManager
{
    /**
     * The server request interceptor list.
     */
    private ServerRequestInterceptor [] m_list;

    /**
     * The PI current
     */
    private CurrentImpl m_current;

    /**
     * minor code of INTERNAL exception thrown when an interceptor gives
     * and invalid response.
     */
    public static final int BAD_INTERCEPTOR_RESPONSE = 0;

    /**
     * Current Logger
     */
    private Logger m_logger = null;

    /**
     * Set the interceptors list
     */
    public SimpleServerManager( ServerRequestInterceptor [] list, CurrentImpl current )
    {
        m_list = list;
        m_current = current;
        m_logger = ( ( org.openorb.orb.core.ORB ) current._orb() ).getLogger();
    }

    /**
     * This operation must be called from the server interception point.
     */
    public void receive_request_service_contexts( ServerRequestInfo info, RequestCallback cb )
    {
        int index = 0;
        CurrentOperations table = m_current.remove();

        try
        {
            for ( ; index < m_list.length; index++ )
            {
                m_list[ index ].receive_request_service_contexts( info );
                m_current.remove();
            }
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            m_current.remove();

            // A system exception has been received from an interceptor
            // Force completion status
            if ( ex.completed != org.omg.CORBA.CompletionStatus.COMPLETED_NO )
            {
                ex = ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                        "receive_request_service_context has thrown a CORBA System exception"
                        + " with completion status not equal to COMPLETED_NO",
                        BAD_INTERCEPTOR_RESPONSE,
                        org.omg.CORBA.CompletionStatus.COMPLETED_NO ), ex );
            }

            cb.reply_system_exception( ex );

            send_exception_point( index - 1, info, cb );
        }
        catch ( org.omg.PortableInterceptor.ForwardRequest ex )
        {
            m_current.remove();

            // A forward exception has been received from an interceptor

            cb.reply_location_forward( ex.forward, false );

            send_other_point( index - 1, info, cb );
        }
        catch ( java.lang.RuntimeException ex )
        {
            m_current.remove();

            // A runtime exception has been received from an interceptor
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Received an unexcepted exception : ", ex );
            }
            cb.reply_runtime_exception( ex );

            send_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.Error ex )
        {
            m_current.remove();

            // An error has been received from an interceptor
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Received an unexcepted exception : ", ex );
            }
            cb.reply_error( ex );

            send_exception_point( index - 1, info, cb );
        }

        m_current.set( table );
    }

    /**
     * This operation must be called from the server interception point.
     */
    public void receive_request( ServerRequestInfo info, RequestCallback cb )
    {
        int index = 0;
        CurrentOperations table = m_current.remove();

        try
        {
            for ( ; index < m_list.length; index++ )
            {
                m_list[ index ].receive_request( info );
                m_current.remove();
            }
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            m_current.remove();

            // A system exception has been received from an interceptor

            // Force completion status
            if ( ex.completed != org.omg.CORBA.CompletionStatus.COMPLETED_NO )
            {
                ex = ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                        BAD_INTERCEPTOR_RESPONSE,
                        org.omg.CORBA.CompletionStatus.COMPLETED_NO ), ex );
            }

            cb.reply_system_exception( ex );

            send_exception_point( index - 1, info, cb );
        }
        catch ( org.omg.PortableInterceptor.ForwardRequest ex )
        {
            m_current.remove();

            // A forward exception has been received from an interceptor

            cb.reply_location_forward( ex.forward, false );

            send_other_point( index - 1, info, cb );
        }
        catch ( java.lang.RuntimeException ex )
        {
            m_current.remove();

            // A runtime exception has been received from an interceptor

            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Received an unexcepted exception : ", ex );
            }
            cb.reply_runtime_exception( ex );

            send_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.Error ex )
        {
            m_current.remove();

            // An error has been received from an interceptor

            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Received an unexcepted exception : ", ex );
            }
            cb.reply_error( ex );

            send_exception_point( index - 1, info, cb );
        }

        m_current.set( table );
    }

    /**
     * This operation must be called from the server interception point.
     */
    public void send_reply( ServerRequestInfo info, RequestCallback cb )
    {
        int index = m_list.length - 1;
        CurrentOperations table = m_current.remove();

        try
        {
            for ( ; index >= 0; index-- )
            {
                m_list[ index ].send_reply( info );
                m_current.remove();
            }
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            m_current.remove();

            // A system exception has been received from an interceptor

            if ( ex.completed != org.omg.CORBA.CompletionStatus.COMPLETED_YES )
            {
                ex = ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                        BAD_INTERCEPTOR_RESPONSE,
                        org.omg.CORBA.CompletionStatus.COMPLETED_YES ), ex );
            }

            cb.reply_system_exception( ex );

            send_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.RuntimeException ex )
        {
            m_current.remove();

            // A runtime exception has been received from an interceptor

            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Received an unexcepted exception : ", ex );
            }
            cb.reply_runtime_exception( ex );

            send_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.Error ex )
        {
            m_current.remove();

            // An error has been received from an interceptor

            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Received an unexcepted exception : ", ex );
            }
            cb.reply_error( ex );

            send_exception_point( index - 1, info, cb );
        }

        m_current.set( table );
    }

    /**
     * This operation must be called from the server interception point.
     */
    public void send_exception( ServerRequestInfo info, RequestCallback cb )
    {
        CurrentOperations table = m_current.remove();
        send_exception_point( m_list.length - 1, info, cb );
        m_current.set( table );
    }

    /**
     * This operation is invoked to activate the 'send_exception'
     * operation on each server interceptor.
     */
    private void send_exception_point( int index, ServerRequestInfo info, RequestCallback cb )
    {
        if ( index < 0 )
        {
            return;
        }
        org.omg.CORBA.CompletionStatus status;

        if ( info.reply_status() == org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value )
        {
            status = org.openorb.orb.core.SystemExceptionHelper.extract(
                    info.sending_exception() ).completed;
        }
        else
        {
            status = org.omg.CORBA.CompletionStatus.COMPLETED_YES;
        }
        for ( ; index >= 0; --index )
        {
            try
            {
                m_list[ index ].send_exception( info );
                m_current.remove();
            }
            catch ( org.omg.CORBA.SystemException ex )
            {
                m_current.remove();

                // A system exception has been received from an interceptor

                if ( ex.completed != status )
                {
                    ex = ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                            BAD_INTERCEPTOR_RESPONSE, status ), ex );
                }

                cb.reply_system_exception( ex );
            }
            catch ( java.lang.RuntimeException ex )
            {
                m_current.remove();

                // A runtime exception has been received from an interceptor
                if ( getLogger().isErrorEnabled() )
                {
                    getLogger().error( "Received an unexcepted exception : ", ex );
                }
                cb.reply_runtime_exception( ex );
            }
            catch ( java.lang.Error ex )
            {
                m_current.remove();

                // An error has been received from an interceptor
                if ( getLogger().isErrorEnabled() )
                {
                    getLogger().error( "Received an unexcepted exception : ", ex );
                }
                cb.reply_error( ex );
            }
            catch ( org.omg.PortableInterceptor.ForwardRequest ex )
            {
                m_current.remove();

                if ( status != org.omg.CORBA.CompletionStatus.COMPLETED_YES )
                {
                    cb.reply_system_exception( ExceptionTool.initCause(
                            new org.omg.CORBA.INTERNAL(
                            BAD_INTERCEPTOR_RESPONSE,
                            org.omg.CORBA.CompletionStatus.COMPLETED_YES ), ex ) );
                    continue;
                }

                cb.reply_location_forward( ex.forward, false );

                send_other_point( index - 1, info, cb );

                return;
            }
        }
    }

    /**
     * This operation must be called from the server interception point.
     */
    public void send_other( ServerRequestInfo info, RequestCallback cb )
    {
        CurrentOperations table = m_current.remove();
        send_other_point( m_list.length - 1, info, cb );
        m_current.set( table );
    }

    /**
     * This operation is invoked to activate the 'send_other' operation on each server interceptor.
     */
    private void send_other_point( int index, ServerRequestInfo info, RequestCallback cb )
    {
        if ( index < 0 )
        {
            return;
        }
        try
        {
            try
            {
                for ( ; index >= 0; --index )
                {
                    m_list[ index ].send_other( info );
                    m_current.remove();
                }
            }
            catch ( org.omg.PortableInterceptor.ForwardRequest ex )
            {
                m_current.remove();
                // A forward exception has been received from an interceptor
                cb.reply_location_forward( ex.forward, false );
            }
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            m_current.remove();
            // A system exception has been received from an interceptor
            if ( ex.completed != org.omg.CORBA.CompletionStatus.COMPLETED_NO )
            {
                ex = ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                        BAD_INTERCEPTOR_RESPONSE,
                        org.omg.CORBA.CompletionStatus.COMPLETED_NO ), ex );
            }

            cb.reply_system_exception( ex );
            send_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.RuntimeException ex )
        {
            m_current.remove();
            // A runtime exception has been received from an interceptor
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Received an unexcepted exception : ", ex );
            }
            cb.reply_runtime_exception( ex );
            send_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.Error ex )
        {
            m_current.remove();
            // An error has been received from an interceptor
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Received an unexcepted exception : ", ex );
            }
            cb.reply_error( ex );
            send_exception_point( index - 1, info, cb );
        }
    }

    /**
     * Current Logger
     */
    private Logger getLogger()
    {
        return m_logger;
    }
}

