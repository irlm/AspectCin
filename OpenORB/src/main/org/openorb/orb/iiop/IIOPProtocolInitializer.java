/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import java.util.List;
import java.util.ArrayList;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.openorb.orb.Initializer;

import org.openorb.orb.net.ServerManager;
import org.openorb.orb.net.ClientManager;
import org.openorb.orb.net.SocketFactory;
import org.openorb.orb.net.ConfiguredSocketFactory;

import org.openorb.orb.pi.FeatureInitializer;
import org.openorb.orb.pi.FeatureInitInfo;
import org.openorb.orb.pi.CodecFactoryManager;
import org.openorb.orb.pi.SimpleIORInterceptor;

import org.openorb.orb.util.Trace;

import org.openorb.util.logger.ControllableLogger;
import org.openorb.util.logger.DiagnosticsLoggerTeam;
import org.openorb.util.logger.LoggerTeam;

import org.openorb.util.ExceptionTool;
import org.openorb.util.ReflectionUtils;

import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;

import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TAG_CODE_SETS;
import org.omg.IOP.TAG_INTERNET_IOP;

import org.omg.CONV_FRAME.CodeSetComponentInfo;
import org.omg.CONV_FRAME.CodeSetComponentInfoHelper;

import org.omg.IIOP.ListenPoint;
import org.omg.IIOP.ListenPointHelper;

/**
 *
 * @author Chris Wood
 * @version $Revision: 1.13 $ $Date: 2004/02/19 06:35:34 $
 */
public class IIOPProtocolInitializer
    extends AbstractLogEnabled
    implements FeatureInitializer, Initializer
{
    /**
     * Default constructor.
     */
    public IIOPProtocolInitializer()
    {
    }

    /**
     * Return the name of the initailizer.
     *
     * @return The name of the initializer.
     */
    public String getName()
    {
        return "iiop";
    }

    /**
     * Initializes this instance.
     *
     * @param orbinfo
     * @param featureinfo
     */
    public void init( ORBInitInfo orbinfo, FeatureInitInfo featureinfo )
    {
        if ( getLogger().isErrorEnabled() && Trace.isLow() )
        {
            getLogger().debug( "init" );
        }

        // initialize the codeset database
        CodeSetDatabaseInitializer csdb = new CodeSetDatabaseInitializer();
        csdb.enableLogging( getLogger().getChildLogger( "csdb" ) );
        csdb.initialize();
        featureinfo.setFeature( "CodeSetDatabase", csdb );

        org.omg.CORBA.ORB orb = featureinfo.orb();

        // initialize codec factory.
        CDRCodecFactory codec_factory = new CDRCodecFactory( orb );

        org.omg.IOP.Codec codec = null;

        CodecFactoryManager cfm = ( CodecFactoryManager )
                featureinfo.getFeature( "CodecFactoryManager" );

        if ( cfm != null )
        {
            cfm.register_codec_factory( new org.omg.IOP.Encoding(
              org.omg.IOP.ENCODING_CDR_ENCAPS.value, ( byte ) 1, ( byte ) 0 ), codec_factory );
            cfm.register_codec_factory( new org.omg.IOP.Encoding(
              org.omg.IOP.ENCODING_CDR_ENCAPS.value, ( byte ) 1, ( byte ) 1 ), codec_factory );
            org.omg.IOP.Encoding enc = new org.omg.IOP.Encoding(
              org.omg.IOP.ENCODING_CDR_ENCAPS.value, ( byte ) 1, ( byte ) 2 );
            cfm.register_codec_factory( enc, codec_factory );

            try
            {
                codec = codec_factory.create_codec( enc );
                if ( LogEnabled.class.isAssignableFrom( codec.getClass() ) )
                {
                    ( ( LogEnabled ) codec ).enableLogging( getLogger().getChildLogger( "codec" ) );
                }
            }
            catch ( final Exception ex )
            {
                getLogger().error( "Error during create_codec().", ex );
                return;
            }
        }

        ListenPoint [] biDirListenPoints = null;

        org.openorb.orb.config.ORBLoader loader = featureinfo.getLoader();

        IIOPServerProtocol svrproto = null;

        if ( null == featureinfo.getFeature( "IIOP.SocketFactory" ) )
        {
            final LoggerTeam socketLogger = DiagnosticsLoggerTeam.narrow(
                    getLogger(), new ControllableLogger( getLogger(),
                    Trace.LOGGER_CONTROL ), "diagnostics"
                    ).createChildLoggerTeam( "socket-factory" );

            final SocketFactory socketFactory = new ConfiguredSocketFactory(
                    socketLogger, loader, "iiop" );

            featureinfo.setFeature( "IIOP.SocketFactory", socketFactory );
        }

        ServerManager svrmgr = ( ServerManager ) featureinfo.getFeature( "ServerCPCManager" );
        if ( svrmgr != null )
        {
            // create codeset component IOR interceptor
            org.omg.CORBA.Any any = orb.create_any();

            CodeSetComponentInfo codesetInfo = csdb.getServerCodeSets();
            CodeSetComponentInfoHelper.insert( any, codesetInfo );

            List iorComponents = new ArrayList();

            try
            {
                byte[] buf = codec.encode_value( any );

                if ( buf != null )
                {
                    iorComponents.add( new TaggedComponent( TAG_CODE_SETS.value, buf ) );
                }
            }
            catch ( final org.omg.IOP.CodecPackage.InvalidTypeForEncoding ex )
            {
                final String error = "Unable to encode code set component.";
                getLogger().error( error, ex );
                throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE( error ), ex );
            }

            // get alternate IIOP endpoints to publish in IORs
            java.util.Iterator itt = loader.properties( "iiop.alternateAddr" );

            while ( itt.hasNext() )
            {
                String hostport = ( ( org.openorb.orb.config.Property ) itt.next() ).getValue();
                int idx = hostport.indexOf( ':' );

                if ( idx < 0 )
                {
                    continue;
                }
                String host = hostport.substring( 0, idx );

                int port;

                try
                {
                    port = Integer.parseInt( hostport.substring( idx + 1 ) );
                }
                catch ( final NumberFormatException ex )
                {
                    continue;
                }

                if ( port > 0xFFFF )
                {
                    continue;
                }
                ListenPoint lp = new ListenPoint( host, ( short ) port );

                ListenPointHelper.insert( any, lp );

                try
                {
                    byte[] buf = codec.encode_value( any );

                    if ( buf != null )
                    {
                        iorComponents.add( new TaggedComponent(
                                org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS.value, buf ) );
                    }
                }
                catch ( final org.omg.IOP.CodecPackage.InvalidTypeForEncoding ex )
                {
                    final String error = "Unable to encode alternate endpoint component.";
                    getLogger().error( error, ex );
                    throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE( error ), ex );
                }
            }

            // create IOR interceptor for assigning components
            if ( !iorComponents.isEmpty() )
            {
                TaggedComponent [] altAddrComponents = new TaggedComponent[ iorComponents.size() ];
                iorComponents.toArray( altAddrComponents );

                try
                {
                    orbinfo.add_ior_interceptor( new SimpleIORInterceptor( "codesets and alts",
                                                 TAG_INTERNET_IOP.value, altAddrComponents ) );
                }
                catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ex )
                {
                    // ignore.
                }

            }

            boolean listen = true;

            // setup server interceptor for activating bidir.
            if ( loader.getBooleanProperty( "iiop.allowBiDir", true ) )
            {
                try
                {
                    orbinfo.add_server_request_interceptor( BIDIR_INTERCEPTOR );
                }
                catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName ex )
                {
                    // ignore.
                }
                listen = !loader.getBooleanProperty( "iiop.biDirOnlyServer", false );
            }

            IIOPTransportServerInitializer svrInit;
            try
            {
                // create an instance of the initializer
                Class clz = loader.getClassProperty(
                      "iiop.TransportServerInitializerClass",
                      "org.openorb.orb.iiop.IIOPTransportServerInitializer" );
                svrInit = ( IIOPTransportServerInitializer ) clz.newInstance();
            }
            catch ( Exception ex )
            {
                throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                      "Unable to create TransportServerInitializer instance ("
                      + ex + ")", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO ), ex );
            }

            if ( LogEnabled.class.isAssignableFrom( svrInit.getClass() ) )
            {
                String logger_name = "iioptsi";
                if ( svrInit instanceof Initializer )
                {
                    Initializer init = ( Initializer ) svrInit;
                    logger_name = init.getName();
                }
                ( ( LogEnabled ) svrInit ).enableLogging(
                      getLogger().getChildLogger( logger_name ) );
            }

            // call initialize
            ReflectionUtils.invokeMethod( svrInit, "init",
                    new Class[] { ORBInitInfo.class, FeatureInitInfo.class },
                    new Object [] { orbinfo, featureinfo } );

            if ( getLogger().isDebugEnabled() && Trace.isLow() )
            {
                getLogger().debug( "IIOP transport server initializer started: " + svrInit );
            }

            if ( listen )
            {
                svrInit.open();
            }
            svrproto = new IIOPServerProtocol( svrmgr, codec_factory, svrInit );

            svrmgr.register_protocol( org.omg.IOP.TAG_INTERNET_IOP.value, svrproto );

            if ( listen )
            {
                svrproto.open();
            }
            // get the bidir listen points so the client side can sent them in
            // the bidir service context.
            biDirListenPoints = svrInit.getBiDirEndpoints();
        }

        IIOPClientProtocol cltproto = null;
        ClientManager cltmgr = ( ClientManager ) featureinfo.getFeature( "ClientCPCManager" );
        if ( cltmgr != null )
        {
            IIOPTransportClientInitializer cltInit;
            try
            {
                // create an instance of the initializer
                Class clz = loader.getClassProperty(
                       "iiop.TransportClientInitializerClass",
                       "org.openorb.orb.iiop.IIOPTransportClientInitializer" );
                cltInit = ( IIOPTransportClientInitializer ) clz.newInstance();
            }
            catch ( Exception ex )
            {
                throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                      "Unable to create TransportClientInitializer instance ("
                      + ex + ")", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO ), ex );
            }

            // log enable
            if ( cltInit instanceof LogEnabled )
            {
                String logger_name = "iioptci";
                if ( cltInit instanceof Initializer )
                {
                    Initializer init = ( Initializer ) cltInit;
                    logger_name = init.getName();
                }
                ( ( LogEnabled ) cltInit ).enableLogging(
                      getLogger().getChildLogger( logger_name ) );
            }

            // call initialize
            ReflectionUtils.invokeMethod( cltInit, "init",
                    new Class[] { ORBInitInfo.class, FeatureInitInfo.class },
                    new Object [] { orbinfo, featureinfo } );

            if ( getLogger().isDebugEnabled() && Trace.isLow() )
            {
                getLogger().debug( "IIOP transport client initializer started: " + cltInit );
            }

            cltproto = new IIOPClientProtocol( cltmgr, codec_factory,
                    csdb.getClientCodeSets(), cltInit, biDirListenPoints );
            cltmgr.register_protocol( org.omg.IOP.TAG_INTERNET_IOP.value, cltproto );

            // setup bidirectional interceptor / access.
            if ( svrproto != null )
            {
                cltproto.setServerProtocol( svrproto );
                svrproto.setClientProtocol( cltproto );
            }
        }
    }


    private static final ServerRequestInterceptor BIDIR_INTERCEPTOR =
            new BiDirServerInterceptor();

    private static final class BiDirServerInterceptor
        extends org.omg.CORBA.LocalObject
        implements ServerRequestInterceptor
    {
        public String name()
        {
            return "IIOPBiDirServerInterceptor";
        }

        public void receive_request_service_contexts( ServerRequestInfo ri )
            throws ForwardRequest
        {
        }

        public void receive_request( ServerRequestInfo ri )
            throws ForwardRequest
        {
            if ( ri instanceof IIOPServerRequest )
            {
                IIOPServerRequest request = ( IIOPServerRequest ) ri;
                IIOPServerChannel channel = ( IIOPServerChannel ) request.channel();

                channel.checkBiDirActivation( request );
            }
        }

        public void send_other( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws ForwardRequest
        {
        }

        public void send_reply( org.omg.PortableInterceptor.ServerRequestInfo ri )
        {
        }

        public void send_exception( org.omg.PortableInterceptor.ServerRequestInfo ri )
            throws ForwardRequest
        {
        }

        public void destroy()
        {
        }
    }
}

