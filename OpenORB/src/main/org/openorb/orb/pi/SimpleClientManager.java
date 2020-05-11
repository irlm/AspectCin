/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import org.openorb.orb.util.Trace;

import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.CurrentOperations;

import org.apache.avalon.framework.logger.Logger;

import org.openorb.util.ExceptionTool;

/**
 * This class is the manager for portable interceptor on client side.
 *
 * @author Jerome Daniel
 * @author Chris Wood
 * @version $Revision: 1.7 $ $Date: 2004/02/10 22:09:10 $
 */

public class SimpleClientManager implements ClientManager
{
    /**
     * The client request interceptor list.
     */
    private ClientRequestInterceptor [] m_list = null;

    /**
     * Current impl. Pushes and pops around the send/recieve functions
     */
    private CurrentImpl m_current;

    /**
     * Current logger
     */
    private Logger m_logger;

    /**
     * exception code for exception thrown when an interceptor
     * gives a non-complient response.
     */
    public static final int BAD_INTERCEPTOR_RESPONSE = 0;

    /**
     * Constructor
     */
    public SimpleClientManager( org.omg.PortableInterceptor.ClientRequestInterceptor [] list ,
                                CurrentImpl current )
    {
        m_list = list;
        m_current = current;
        m_logger = ( ( org.openorb.orb.core.ORBSingleton )
            current._orb() ).getLogger();
    }

    /**
     * This operation must be called from the client interception point.
     */
    public void send_request( ClientRequestInfo info, RequestCallback cb )
    {
        int index = 0;
        CurrentOperations table = m_current.remove();

        // Call 'send_request' on each interceptor
        try
        {
            for ( ; index < m_list.length; index++ )
            {
                m_list[ index ].send_request( info );
                m_current.remove();
            }
        }
        catch ( org.omg.PortableInterceptor.ForwardRequest ex )
        {
            m_current.remove();

            // A forward request exception has been received from one of the interceptors.
            cb.reply_location_forward( ex.forward, false );

            receive_other_point( index - 1, info, cb );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            m_current.remove();

            // A CORBA System exception has been received from one of the interceptors.

            if ( ex.completed != org.omg.CORBA.CompletionStatus.COMPLETED_NO )
            {
                ex = ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                        BAD_INTERCEPTOR_RESPONSE,
                        org.omg.CORBA.CompletionStatus.COMPLETED_NO ), ex );
            }

            ex.completed = org.omg.CORBA.CompletionStatus.COMPLETED_NO;

            cb.reply_system_exception( ex );

            receive_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.RuntimeException ex )
        {
            m_current.remove();

            // A runtime exception has been received from an interceptor
            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Received an unexcepted exception : " + ex.toString() );
            }
            cb.reply_runtime_exception( ex );

            receive_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.Error ex )
        {
            m_current.remove();

            // An error has been received from an interceptor

            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Received an unexcepted exception : " + ex.toString() );
            }
            cb.reply_error( ex );

            receive_exception_point( index - 1, info, cb );
        }

        m_current.set( table );
    }

    /**
     * This operation must be called from the client interception point.
     */
    public void send_poll( ClientRequestInfo info, RequestCallback cb )
    {
        int index = 0;
        CurrentOperations table = m_current.remove();

        // Call 'send_poll' on each interceptor
        try
        {
            for ( ; index < m_list.length; index++ )
            {
                m_list[ index ].send_poll( info );
                m_current.remove();
            }
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            m_current.remove();

            // A CORBA System exception has been received from one of the interceptors.

            if ( ex.completed != org.omg.CORBA.CompletionStatus.COMPLETED_NO )
            {
                ex = ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                        BAD_INTERCEPTOR_RESPONSE,
                        org.omg.CORBA.CompletionStatus.COMPLETED_NO ), ex );
            }

            cb.reply_system_exception( ex );

            receive_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.RuntimeException ex )
        {
            m_current.remove();

            // A runtime exception has been received from an interceptor

            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Received an unexcepted exception : " + ex.toString() );
            }
            cb.reply_runtime_exception( ex );

            receive_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.Error ex )
        {
            m_current.remove();

            // An error has been received from an interceptor

            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Received an unexcepted exception : " + ex.toString() );
            }
            cb.reply_error( ex );

            receive_exception_point( index - 1, info, cb );
        }

        m_current.set( table );
    }

    /**
     * This operation must be called from the client interception point when a reply is received.
     */
    public void receive_reply( ClientRequestInfo info, RequestCallback cb )
    {
        int index = m_list.length - 1;
        CurrentOperations table = m_current.remove();

        // Call 'receive_reply' on each interceptor
        try
        {
            for ( ; index >= 0; index-- )
            {
                m_list[ index ].receive_reply( info );
                m_current.remove();
            }
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            m_current.remove();

            // A CORBA System exception has been received from one of the interceptors.

            if ( ex.completed != org.omg.CORBA.CompletionStatus.COMPLETED_YES )
            {
                ex = ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                        BAD_INTERCEPTOR_RESPONSE,
                        org.omg.CORBA.CompletionStatus.COMPLETED_YES ), ex );
            }

            cb.reply_system_exception( ex );

            receive_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.RuntimeException ex )
        {
            m_current.remove();

            // A runtime exception has been received from an interceptor
            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Received an unexcepted exception : " + ex.toString() );
            }
            cb.reply_runtime_exception( ex );
            receive_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.Error ex )
        {
            m_current.remove();

            // An error has been received from an interceptor

            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Received an unexcepted exception : " + ex.toString() );
            }
            cb.reply_error( ex );

            receive_exception_point( index - 1, info, cb );
        }

        m_current.set( table );
    }

    /**
     * This operation must be called from the client interception point.
     */
    public void receive_exception( ClientRequestInfo info, RequestCallback cb )
    {
        CurrentOperations table = m_current.remove();
        receive_exception_point( m_list.length - 1, info, cb );
        m_current.set( table );
    }

    /**
     * This operation is called to apply the 'receive_exception'
     * operation on each client interceptor.
     */
    private void receive_exception_point( int index, ClientRequestInfo info, RequestCallback cb )
    {
        if ( index < 0 )
        {
            return;
        }
        org.omg.CORBA.CompletionStatus status;

        if ( info.reply_status() == org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value )
        {
            status = org.openorb.orb.core.SystemExceptionHelper.extract(
                    info.received_exception() ).completed;
        }
        else
        {
            status = org.omg.CORBA.CompletionStatus.COMPLETED_YES;
        }
        // Call 'receive_exception' on each interceptor
        for ( ; index >= 0; index-- )
        {
            try
            {
                m_list[ index ].receive_exception( info );
            }
            catch ( org.omg.CORBA.SystemException ex )
            {
                if ( ex.completed != status )
                {
                    ex = ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                            BAD_INTERCEPTOR_RESPONSE, status ), ex );
                }

                cb.reply_system_exception( ex );
            }
            catch ( java.lang.RuntimeException ex )
            {
                // A runtime exception has been received from an interceptor

            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Received an unexcepted exception : " + ex.toString() );
            }
                cb.reply_runtime_exception( ex );
            }
            catch ( java.lang.Error ex )
            {
                // An error has been received from an interceptor

                if ( getLogger().isDebugEnabled() && Trace.isHigh() )
                {
                    getLogger().debug( "Received an unexcepted exception : " + ex.toString() );
                }
                cb.reply_error( ex );
            }
            catch ( final org.omg.PortableInterceptor.ForwardRequest ex )
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

                // A FrowardRequest exception has been received from one of the interceptors.
                cb.reply_location_forward( ex.forward, false );

                receive_other_point( index - 1, info, cb );

                return;
            }

            m_current.remove();
        }
    }

    /**
     * This operation must be called from the client interception point.
     */
    public void receive_other( ClientRequestInfo info, RequestCallback cb )
    {
        CurrentOperations table = m_current.remove();
        receive_other_point( m_list.length - 1, info, cb );
        m_current.set( table );
    }

    /**
     * This operation is called to apply the 'receive_exception'
     * operation on each client interceptor.
     */
    private void receive_other_point( int index, ClientRequestInfo info, RequestCallback cb )
    {
        if ( index < 0 )
        {
            return;
        }
        // Call 'receive_exception' on each interceptor
        try
        {
            for ( ; index >= 0; index-- )
            {
                try
                {
                    m_list[ index ].receive_other( info );
                }
                catch ( org.omg.PortableInterceptor.ForwardRequest ex )
                {
                    // A FrowardRequest exception has been received from one of the interceptors.
                    cb.reply_location_forward( ex.forward, false );
                }

                m_current.remove();
            }
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            m_current.remove();

            // A CORBA System exception has been received from one of the interceptors.

            if ( ex.completed != org.omg.CORBA.CompletionStatus.COMPLETED_NO )
            {
                ex = ExceptionTool.initCause( new org.omg.CORBA.INTERNAL(
                        BAD_INTERCEPTOR_RESPONSE,
                        org.omg.CORBA.CompletionStatus.COMPLETED_NO ), ex );
            }

            cb.reply_system_exception( ex );

            receive_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.RuntimeException ex )
        {
            m_current.remove();

            // A runtime exception has been received from an interceptor

            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Received an unexcepted exception : " + ex.toString() );
            }
            cb.reply_runtime_exception( ex );

            receive_exception_point( index - 1, info, cb );
        }
        catch ( java.lang.Error ex )
        {
            m_current.remove();

            // An error has been received from an interceptor

            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( "Received an unexcepted exception : " + ex.toString() );
            }
            cb.reply_error( ex );

            receive_exception_point( index - 1, info, cb );
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }
}

