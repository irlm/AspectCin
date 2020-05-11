/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dii;

import org.omg.CORBA.CompletionStatus;

/**
 * This class provides a way to use dynamic request.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.5 $ $Date: 2004/02/10 21:02:47 $
 */
public class Request
    extends org.omg.CORBA.Request
{
    /**
     * Reference to the target object reference
     */
    private org.omg.CORBA.Object m_target;

    /**
     * Reference to the target delegate
     */
    private org.openorb.orb.core.Delegate m_delegate;

    /**
     * Operation to invoke
     */
    private String m_operation;

    /**
     * Operation arguments
     */
    private org.omg.CORBA.NVList m_arguments;

    /**
     * Result
     */
    private org.omg.CORBA.NamedValue m_result;

    /**
     * Environment variable for exception
     */
    private org.omg.CORBA.Environment m_env;

    /**
     * An exception list to describe what kind of user exception could be raised
     */
    private org.omg.CORBA.ExceptionList m_exceptions;

    /**
     * Context list
     */
    private org.omg.CORBA.ContextList m_contexts;

    /**
     * Context object
     */
    private org.omg.CORBA.Context m_context;

    /**
     * Reference to the ORB
     */
    private org.openorb.orb.core.ORB m_orb;

    /**
     * True if send_deferred was used.
     */
    private boolean m_deferred = false;

    /**
     * True if response was available during send_deferred
     */
    private boolean m_response = false;

    private org.omg.CORBA.portable.InputStream m_input;
    private org.omg.CORBA.portable.OutputStream m_output;

    /**
     * Constructor
     */
    public Request( org.omg.CORBA.Object target, String operation, org.omg.CORBA.ORB orb )
    {
        m_orb = ( org.openorb.orb.core.ORB ) orb;

        m_target = target;

        m_delegate = ( org.openorb.orb.core.Delegate )
              ( ( org.omg.CORBA.portable.ObjectImpl ) target )._get_delegate();

        m_operation = operation;

        m_arguments = m_orb.create_list( 0 );
        m_env = new org.openorb.orb.core.dii.Environment();
        m_exceptions = new org.openorb.orb.core.dii.ExceptionList();
        m_contexts = new org.openorb.orb.core.dii.ContextList();
        m_context = null;

        org.omg.CORBA.Any any = m_orb.create_any();
        any.type ( m_orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_void ) );
        m_result = new org.openorb.orb.core.dii.NamedValue( "", any, 0 );
    }


    /**
     * Constructor
     */
    public Request( org.omg.CORBA.Object target,
                    String operation,
                    org.omg.CORBA.NVList arguments,
                    org.omg.CORBA.NamedValue result,
                    org.omg.CORBA.Environment env,
                    org.omg.CORBA.ExceptionList exceptions,
                    org.omg.CORBA.ContextList contexts,
                    org.omg.CORBA.ORB orb )
    {
        m_orb = ( org.openorb.orb.core.ORB ) orb;

        m_target = target;

        m_delegate = ( org.openorb.orb.core.Delegate )
              ( ( org.omg.CORBA.portable.ObjectImpl ) target )._get_delegate();

        m_operation = operation;
        m_arguments = m_orb.create_list( 0 );
        m_env = env;
        m_exceptions = exceptions;
        m_contexts = contexts;
        m_result = result;
        m_context = null;

        for ( int i = 0; i < arguments.count(); i++ )
        {
            try
            {
                String s = arguments.item( i ).name();
                org.omg.CORBA.Any a = arguments.item( i ).value();
                int f = arguments.item( i ).flags();

                m_arguments.add_value( s, a, f );
            }
            catch ( org.omg.CORBA.Bounds ex )
            {
                // TODO: ???
            }
        }
    }

    /**
     * Return the target object reference
     */
    public org.omg.CORBA.Object target()
    {
        return m_target;
    }

    /**
     * Return the operation name
     */
    public String operation()
    {
        return m_operation;
    }

    /**
     * Return the operation arguments
     */
    public org.omg.CORBA.NVList arguments()
    {
        return m_arguments;
    }

    /**
     * Return the result value
     */
    public org.omg.CORBA.NamedValue result()
    {
        if ( !m_response )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation not completed",
                  org.omg.CORBA.OMGVMCID.value | 5, CompletionStatus.COMPLETED_MAYBE );
        }
        return m_result;
    }

    /**
     * Return the environment value
     */
    public org.omg.CORBA.Environment env()
    {
        return m_env;
    }

    /**
     * Return the exception list
     */
    public org.omg.CORBA.ExceptionList exceptions()
    {
        return m_exceptions;
    }

    /**
     * Return the context list
     */
    public org.omg.CORBA.ContextList contexts()
    {
        return m_contexts;
    }

    /**
     * Return the context object
     */
    public org.omg.CORBA.Context ctx()
    {
        return m_context;
    }

    /**
     * Set the context object
     */
    public void ctx( org.omg.CORBA.Context c )
    {
        if ( m_output != null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation already invoked",
                  org.omg.CORBA.OMGVMCID.value | 5, CompletionStatus.COMPLETED_MAYBE );
        }
        m_context = c;
    }

    /**
     * Add an IN argument
     */
    public org.omg.CORBA.Any add_in_arg()
    {
        if ( m_output != null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation already invoked",
                  org.omg.CORBA.OMGVMCID.value | 5, CompletionStatus.COMPLETED_MAYBE );
        }
        return ( m_arguments.add( org.omg.CORBA.ARG_IN.value ) ).value();
    }

    /**
     * Add an IN argument
     */
    public org.omg.CORBA.Any add_named_in_arg( String name )
    {
        if ( m_output != null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation already invoked",
                  org.omg.CORBA.OMGVMCID.value | 5, CompletionStatus.COMPLETED_MAYBE );
        }
        return ( m_arguments.add_item( name, org.omg.CORBA.ARG_IN.value ) ).value();
    }

    /**
     * Add an INOUT argument
     */
    public org.omg.CORBA.Any add_inout_arg()
    {
        if ( m_output != null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation already invoked",
                  org.omg.CORBA.OMGVMCID.value | 5, CompletionStatus.COMPLETED_MAYBE );
        }
        return ( m_arguments.add( org.omg.CORBA.ARG_INOUT.value ) ).value();
    }

    /**
     * Add an INOUT argument
     */
    public org.omg.CORBA.Any add_named_inout_arg( String name )
    {
        if ( m_output != null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation already invoked",
                  org.omg.CORBA.OMGVMCID.value | 5, CompletionStatus.COMPLETED_MAYBE );
        }
        return ( m_arguments.add_item( name, org.omg.CORBA.ARG_INOUT.value ) ).value();
    }

    /**
     * Add an OUT argument
     */
    public org.omg.CORBA.Any add_out_arg()
    {
        if ( m_output != null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation already invoked",
                  org.omg.CORBA.OMGVMCID.value | 5, CompletionStatus.COMPLETED_MAYBE );
        }
        return ( m_arguments.add( org.omg.CORBA.ARG_OUT.value ) ).value();
    }

    /**
     * Add an OUT argument
     */
    public org.omg.CORBA.Any add_named_out_arg( String name )
    {
        if ( m_output != null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation already invoked",
                  org.omg.CORBA.OMGVMCID.value | 5, CompletionStatus.COMPLETED_MAYBE );
        }
        return ( m_arguments.add_item( name, org.omg.CORBA.ARG_OUT.value ) ).value();
    }

    /**
     * Set the return type
     */
    public void set_return_type( org.omg.CORBA.TypeCode tc )
    {
        if ( m_output != null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation already invoked",
                  org.omg.CORBA.OMGVMCID.value | 5, CompletionStatus.COMPLETED_MAYBE );
        }
        m_result.value().type( tc );
    }

    /**
     * Return the value
     */
    public org.omg.CORBA.Any return_value()
    {
        return m_result.value();
    }

    private void marshal( org.omg.CORBA.portable.OutputStream argument_stream )
    {
        for ( int i = 0; i < m_arguments.count(); i++ )
        {
            try
            {
                if ( m_arguments.item( i ).flags() != org.omg.CORBA.ARG_OUT.value )
                {
                    m_arguments.item( i ).value().write_value( argument_stream );
                }
            }
            catch ( org.omg.CORBA.Bounds e )
            {
                // TODO: ???
            }
        }
        java.util.Vector allCtx = new java.util.Vector();
        if ( m_context != null )
        {
            for ( int i = 0; i < m_contexts.count(); i++ )
            {
                try
                {
                    org.omg.CORBA.NVList list = m_context.get_values( "", 0, m_contexts.item( i ) );
                    for ( int j = 0; j < list.count(); j++ )
                    {
                        allCtx.addElement( list.item( j ).name() );
                        allCtx.addElement ( list.item( j ).value().extract_string() );
                    }
                }
                catch ( org.omg.CORBA.Bounds ex )
                {
                    // TODO: ???
                }
            }
        }
        if ( allCtx.size() != 0 )
        {
            argument_stream.write_ulong( allCtx.size() );
            for ( int j = 0; j < allCtx.size(); j++ )
            {
                argument_stream.write_string( ( String ) allCtx.elementAt( j ) );
            }
        }
        else
        {
            if ( m_context != null )
            {
                argument_stream.write_ulong( 0 );
            }
        }
    }

    private void unmarshal( org.omg.CORBA.portable.InputStream response_stream )
    {
        m_result.value().read_value( response_stream, m_result.value().type() );
        for ( int i = 0; i < m_arguments.count(); i++ )
        {
            try
            {
                org.omg.CORBA.NamedValue nv = m_arguments.item( i );
                if ( nv.flags() != org.omg.CORBA.ARG_IN.value )
                {
                    nv.value().read_value( response_stream, nv.value().type() );
                }
            }
            catch ( org.omg.CORBA.Bounds e )
            {
                // TODO: ???
            }
        }
    }

    private void unmarshalException( String ex_id,
          org.omg.CORBA.portable.InputStream response_stream )
    {
        for ( int i = 0; i < m_exceptions.count(); ++i )
        {
            try
            {
                if ( m_exceptions.item( i ).id().equals( ex_id ) )
                {
                    org.omg.CORBA.UnknownUserException ex =
                          new org.omg.CORBA.UnknownUserException( m_orb.create_any() );
                    ex.except.read_value( response_stream, m_exceptions.item( i ) );
                    env().exception( ex );
                    return;
                }
            }
            catch ( org.omg.CORBA.TypeCodePackage.BadKind ex )
            {
                // TODO: ???
            }
            catch ( org.omg.CORBA.Bounds ex )
            {
                // TODO: ???
            }
        }
        env().exception( new org.omg.CORBA.UNKNOWN( "Unexcepected User Exception: " + ex_id,
              org.omg.CORBA.OMGVMCID.value | 1, CompletionStatus.COMPLETED_YES ) );
    }

    /**
     * Invoke an operation
     */
    public void invoke()
    {
        invoke( true );
    }

    /**
     * Send a oneway request
     */
    public void send_oneway()
    {
        invoke( false );
    }

    private void invoke( boolean response_expected )
    {
        if ( m_response )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Response already received",
                  org.omg.CORBA.OMGVMCID.value | 5, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_output != null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Multiple request sending",
                  org.omg.CORBA.OMGVMCID.value | 10, CompletionStatus.COMPLETED_MAYBE );
        }
        m_response = true;

        try
        {
            while ( true )
            {
                try
                {
                    m_output = m_delegate.request( m_target, m_operation, response_expected );
                    marshal( m_output );
                    m_input = m_delegate.invoke( m_target, m_output );

                    if ( response_expected )
                    {
                        unmarshal( m_input );
                    }
                    return;
                }
                catch ( org.omg.CORBA.portable.RemarshalException ex )
                {
                    continue;
                }
                catch ( org.omg.CORBA.portable.ApplicationException ex )
                {
                    unmarshalException( ex.getId(), ex.getInputStream() );
                    return;
                }
                finally
                {
                    m_delegate.releaseReply( m_target, m_input );
                }
            }
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            env().exception( ex );
            throw ex;
        }
    }

    /**
     * Send a deferred request
     */
    public void send_deferred()
    {
        if ( m_response )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Response already received",
                  org.omg.CORBA.OMGVMCID.value | 5, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_output != null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Multiple request sending",
                  org.omg.CORBA.OMGVMCID.value | 10, CompletionStatus.COMPLETED_MAYBE );
        }
        m_deferred = true;

        try
        {
            m_input = null;
            m_output = m_delegate.request( m_target, m_operation, true );
            marshal( m_output );
            m_delegate.invoke_deferred( m_target, m_output );
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            env().exception( ex );
            throw ex;
        }
    }

    /**
     * Return TRUE if a response is available
     */
    public boolean poll_response()
    {
        if ( m_output == null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation not sent",
                  org.omg.CORBA.OMGVMCID.value | 11, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( !m_deferred )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation not sent deferred",
                  org.omg.CORBA.OMGVMCID.value | 13, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_response )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Response already received",
                  org.omg.CORBA.OMGVMCID.value | 12, CompletionStatus.COMPLETED_MAYBE );
        }
        return m_delegate.poll_response( m_target, m_output );
    }

    /**
     * Get response
     */
    public void get_response()
    {
        if ( m_output == null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation not sent",
                  org.omg.CORBA.OMGVMCID.value | 11, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( !m_deferred )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Operation not sent deferred",
                  org.omg.CORBA.OMGVMCID.value | 13, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_response )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "Response already received",
                  org.omg.CORBA.OMGVMCID.value | 12, CompletionStatus.COMPLETED_MAYBE );
        }
        m_response = true;

        try
        {
            try
            {
                m_input = m_delegate.invoke( m_target, m_output );
                unmarshal( m_input );
                return;
            }
            catch ( org.omg.CORBA.portable.RemarshalException ex )
            {
                m_output = null;
                m_response = false;
            }
            catch ( org.omg.CORBA.portable.ApplicationException ex )
            {
                unmarshalException( ex.getId(), ex.getInputStream() );
                return;
            }
            finally
            {
                m_delegate.releaseReply( m_target, m_input );
            }
        }
        catch ( org.omg.CORBA.SystemException ex )
        {
            env().exception( ex );
            throw ex;
        }

        invoke( true );
    }

    public org.omg.CORBA.Object sendp()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void prepare( org.omg.CORBA.Object p )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    void sendc( org.omg.CORBA.Object handler )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}

