/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;

import org.openorb.orb.Initializer;

import org.openorb.orb.core.LoggableLocalObject;

import org.openorb.orb.pi.FeatureInitializer;
import org.openorb.orb.pi.FeatureInitInfo;

import org.openorb.orb.util.Trace;

/**
 * This class is used a an Initializer for RMI over IIOP. It just registers
 * interceptors used to transmit java errors and runtime exceptions over a
 * corba connection.
 *
 * @author Chris Wood
 */
public class RMIInitializer
    extends LoggableLocalObject
    implements FeatureInitializer, ORBInitializer, Initializer
{
    private org.omg.CORBA.ORB m_orb;

    /**
     * Return the name of the initializer.
     *
     * @return The name of the initializer.
     */
    public String getName()
    {
        return "rmi";
    }

    /**
     * Do nothing.
     *
     * @param info The ORBInitInfo instance for this initializer.
     */
    public void pre_init( ORBInitInfo info )
    {
    }

    /**
     * Register the unknown exception info interceptors.
     *
     * @param info The ORBInitInfo instance for this initializer.
     */
    public void post_init( ORBInitInfo info )
    {
        org.omg.IOP.Codec codec;
        try
        {
            codec = info.codec_factory().create_codec( new org.omg.IOP.Encoding(
                  org.omg.IOP.ENCODING_CDR_ENCAPS.value, ( byte ) 1, ( byte ) 2 ) );
        }
        catch ( org.omg.IOP.CodecFactoryPackage.UnknownEncoding ex )
        {
            return;
        }
        try
        {
            ClientInterceptor clt = new ClientInterceptor( codec, m_orb );
            clt.enableLogging( getLogger() );
            info.add_client_request_interceptor( clt );
            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( clt.name() + " installed." );
            }
        }
        catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ex )
        {
            // not possible
        }
        try
        {
            ServerInterceptor srv = new ServerInterceptor( codec, m_orb );
            srv.enableLogging( getLogger() );
            info.add_server_request_interceptor( srv );
            if ( getLogger().isDebugEnabled() && Trace.isHigh() )
            {
                getLogger().debug( srv.name() + " installed." );
            }
        }
        catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ex )
        {
            // not possible
        }

    }

    /**
     * This is called immediately after any pre_init interception points in
     * ORBInitializers. The orb reference available from the FeatureInitInfo
     * should be treated with care, it can not be used for any request functions
     * or for creating object references.
     *
     * @param orbinfo The ORBInitInfo instance for this initializer.
     * @param featureinfo The ORB's feature info.
     */
    public void init( ORBInitInfo orbinfo, FeatureInitInfo featureinfo )
    {
        // create a rmi child logger
        enableLogging( getLogger().getChildLogger( "rmi" ) );
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "init" );
        }
        m_orb = featureinfo.orb();
    }

    private static class ClientInterceptor
        extends LoggableLocalObject
        implements ClientRequestInterceptor
    {
        ClientInterceptor( org.omg.IOP.Codec codec, org.omg.CORBA.ORB orb )
        {
            m_codec = codec;
            m_orb = orb;
        }

        private org.omg.IOP.Codec m_codec;
        private org.omg.CORBA.ORB m_orb;

        public String name()
        {
            return "ClientInterceptor";
        }

        public void receive_exception( ClientRequestInfo ri )
            throws ForwardRequest
        {
            // check if the recieved exception is an unknown exception.
            if ( ri.reply_status() != org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value
                    || !ri.received_exception_id().equals( org.omg.CORBA.UNKNOWNHelper.id() ) )
            {
                return;
            }
            // check for the presence of the unknown exception info service context
            org.omg.IOP.ServiceContext sc;

            try
            {
                sc = ri.get_reply_service_context( org.omg.IOP.UnknownExceptionInfo.value );
            }
            catch ( org.omg.CORBA.BAD_PARAM ex )
            {
                return;
            }

            // find the old system exception.
            org.omg.CORBA.SystemException sys;
            if ( ri instanceof org.openorb.orb.net.ClientRequest )
            {
                sys = ( ( org.openorb.orb.net.ClientRequest ) ri ).received_system_exception();
            }
            else
            {
                sys = org.omg.CORBA.UNKNOWNHelper.extract( ri.received_exception() );
            }
            // decode the exception in the service context
            org.omg.CORBA.Any cex_any;
            try
            {
                cex_any = m_codec.decode_value( sc.context_data,
                        m_orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_value ) );
            }
            catch ( org.omg.IOP.CodecPackage.FormatMismatch ex )
            {
                return;
            }
            catch ( org.omg.IOP.CodecPackage.TypeMismatch ex )
            {
                return;
            }

            Throwable cex;
            try
            {
                cex = ( Throwable ) cex_any.extract_Value();
            }
            catch ( org.omg.CORBA.SystemException ex )
            {
                return;
            }
            catch ( ClassCastException ex )
            {
                return;
            }

            // throw the unknown exception.
            org.omg.CORBA.portable.UnknownException uex
                = new org.omg.CORBA.portable.UnknownException( cex );
            uex.minor = sys.minor;
            uex.completed = sys.completed;
            throw uex;
        }

        public void send_request( ClientRequestInfo ri )
            throws ForwardRequest
        {
        }

        public void send_poll( ClientRequestInfo ri )
        {
        }

        public void receive_other( ClientRequestInfo ri )
            throws ForwardRequest
        {
        }

        public void receive_reply( ClientRequestInfo ri )
        {
        }

        public void destroy()
        {
        }
    }

    private static class ServerInterceptor
        extends LoggableLocalObject
        implements ServerRequestInterceptor
    {
        ServerInterceptor( org.omg.IOP.Codec codec, org.omg.CORBA.ORB orb )
        {
            m_codec = codec;
            m_orb = orb;
        }

        private org.omg.IOP.Codec m_codec;
        private org.omg.CORBA.ORB m_orb;

        public String name()
        {
            return "ServerInterceptor";
        }

        public void send_exception( ServerRequestInfo ri )
            throws ForwardRequest
        {
            // check for reply status and for presence of our own server.
            // this interceptor is non-portable, there is no portable way
            // of extracting an UnknownException from an any.
            if ( ri.reply_status() != org.omg.PortableInterceptor.SYSTEM_EXCEPTION.value
                    || !( ri instanceof org.openorb.orb.net.ServerRequest ) )
            {
                return;
            }
            org.omg.CORBA.SystemException sys
                = ( ( org.openorb.orb.net.ServerRequest ) ri ).sending_system_exception();

            if ( !( sys instanceof org.omg.CORBA.portable.UnknownException ) )
            {
                return;
            }
            org.omg.CORBA.portable.UnknownException uex =
                    ( org.omg.CORBA.portable.UnknownException ) sys;
            org.omg.CORBA.Any any = m_orb.create_any();
            any.insert_Value( uex.originalEx );
            byte [] encode;
            try
            {
                encode = m_codec.encode_value( any );
            }
            catch ( org.omg.IOP.CodecPackage.InvalidTypeForEncoding ex )
            {
                return;
            }
            org.omg.IOP.ServiceContext sc = new org.omg.IOP.ServiceContext(
                  org.omg.IOP.UnknownExceptionInfo.value, encode );
            ri.add_reply_service_context( sc, true );
        }

        public void receive_request_service_contexts( ServerRequestInfo ri )
            throws ForwardRequest
        {
        }

        public void receive_request( ServerRequestInfo ri )
            throws ForwardRequest
        {
        }

        public void send_reply( ServerRequestInfo ri )
        {
        }

        public void send_other( ServerRequestInfo ri )
            throws ForwardRequest
        {
        }

        public void destroy()
        {
        }
    }
}

