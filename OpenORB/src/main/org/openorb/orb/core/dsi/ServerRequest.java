/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dsi;

import org.omg.CORBA.CompletionStatus;

import org.openorb.orb.core.MinorCodes;

/**
 * This class implements the ServerRequest standard class.
 * It delegates mostly to the ServerRequest in net.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:47 $
 */
public class ServerRequest
    extends org.omg.CORBA.ServerRequest
{
    /**
     * Reference to the ORB
     */
    private org.omg.CORBA.ORB m_orb;

    /**
     * Server request.
     */
    private org.openorb.orb.net.ServerRequest m_server_request;

    /**
     * Input stream
     */
    private org.omg.CORBA.portable.InputStream m_input = null;

    /**
    * Parameters list
    */
    private org.omg.CORBA.NVList m_args = null;

    /**
     * Context.
     */
    private org.omg.CORBA.Context m_context = null;

    /**
     * A constructor with the request name as parameter
     */
    public ServerRequest( org.openorb.orb.net.ServerRequest server_request )
    {
        m_server_request = server_request;
        m_orb = server_request.orb();
    }

    /**
     * Return the operation name
     * @deprecated Use operation() instead.
     */
    public String op_name()
    {
        return operation();
    }

    /**
     * Return the operation name
     */
    public String operation()
    {
        return m_server_request.operation();
    }

    /**
     * Get parameters
     * @deprecated Use arguments instead.
     */
    public void params( org.omg.CORBA.NVList params )
    {
        arguments( params );
    }

    /**
     * Return all parameters
     */
    public void arguments( org.omg.CORBA.NVList params )
    {
        if ( m_args != null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "DSI inv order",
                  org.omg.CORBA.OMGVMCID.value | 7, CompletionStatus.COMPLETED_MAYBE );
        }
        m_args = params;

        m_input = m_server_request.argument_stream();

        for ( int i = 0; i < params.count(); i++ )
        {
            try
            {
                if ( ( params.item( i ).flags() == org.omg.CORBA.ARG_IN.value )
                      || ( params.item( i ).flags() == org.omg.CORBA.ARG_INOUT.value ) )
                {
                    params.item( i ).value().read_value(
                          m_input, params.item( i ).value().type() );
                }
            }
            catch ( org.omg.CORBA.Bounds e )
            {
                // TODO: ???
            }
        }
    }

    /**
     * Return operation context
     */
    public org.omg.CORBA.Context ctx()
    {
        if ( m_args == null || m_input == null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "DSI inv order",
                  org.omg.CORBA.OMGVMCID.value | 8, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( m_context == null )
        {
            org.omg.CORBA.NVList nv = m_orb.create_list( 0 );

            int max = 0;

            try
            {
                max = ( m_input.read_ulong() / 2 );
            }
            catch ( org.omg.CORBA.MARSHAL ex )
            {
                // test for buffer overread buffer overread.
                if ( ex.minor != MinorCodes.MARSHAL_BUFFER_OVERREAD )
                {
                    throw ex;
                }
            }

            for ( int i = 0; i < max; i++ )
            {
                org.omg.CORBA.Any a = m_orb.create_any();
                String name = m_input.read_string();
                a.insert_string( m_input.read_string() );
                nv.add_value( name, a, 0 );
            }

            m_context = new org.openorb.orb.core.dii.Context( "", null, m_orb );
            m_context.set_values( nv );
        }

        return m_context;
    }

    /**
     * Set the request result.
     * @deprecated Use set_result() instead.
     */
    public void result( org.omg.CORBA.Any a )
    {
        set_result( a );
    }

    /**
     * Set the request result
     */
    public void set_result( org.omg.CORBA.Any a )
    {
        if ( m_args == null || m_input == null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "DSI inv order",
                  org.omg.CORBA.OMGVMCID.value | 9, CompletionStatus.COMPLETED_MAYBE );
        }
        m_input = null;

        org.omg.CORBA.portable.OutputStream os = m_server_request.createReply();

        a.write_value( os );

        for ( int i = 0; i < m_args.count(); i++ )
        {
            try
            {
                if ( ( m_args.item( i ).flags() == org.omg.CORBA.ARG_OUT.value )
                      || ( m_args.item( i ).flags() == org.omg.CORBA.ARG_INOUT.value ) )
                {
                    m_args.item( i ).value().write_value( os );
                }
            }
            catch ( org.omg.CORBA.Bounds e )
            {
                // TODO: ???
            }
        }
    }

    /**
     * Set the request exception.
     * @deprecated Use set_exception() instead.
     */
    public void except( org.omg.CORBA.Any a )
    {
        set_exception( a );
    }

    /**
     * Set the request exception.
     */
    public void set_exception( org.omg.CORBA.Any a )
    {
        if ( m_args == null || m_input == null )
        {
            throw new org.omg.CORBA.BAD_INV_ORDER( "DSI inv order",
                  org.omg.CORBA.OMGVMCID.value | 9, CompletionStatus.COMPLETED_MAYBE );
        }
        if ( a.type().kind() != org.omg.CORBA.TCKind.tk_except )
        {
            throw new org.omg.CORBA.BAD_PARAM( "Parameter does not contain exception",
                  org.omg.CORBA.OMGVMCID.value | 21, CompletionStatus.COMPLETED_MAYBE );
        }
        m_input = null;

        org.omg.CORBA.portable.OutputStream os = m_server_request.createExceptionReply();

        a.write_value( os );
    }
}

