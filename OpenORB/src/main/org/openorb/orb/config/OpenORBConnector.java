/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.config;

import org.apache.avalon.framework.logger.Logger;

import org.openorb.orb.util.Trace;

import org.openorb.util.ExceptionTool;

/**
 * This class is the default OpenORB connector.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.6 $ $Date: 2004/02/10 21:02:46 $
 *
 * @deprecated Because of doubts concerning the purpose of the
 * ORBConnector interface in general and in order to simplify the
 * kernel's architecture this class will be removed in future
 * versions of OpenORB (see interface ORBConnector for explanation).
 */
public class OpenORBConnector
    implements ORBConnector
{
    private Logger m_logger = null;

    public Logger getLogger()
    {
        return m_logger;
    }

    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

    /**
     * This operation is used to load the OpenORB kernel.
     * <p>Depending on the property <b>openorb.server.enable</b>
     * ( default true ) and <b>openorb.client.enable</b>
     * ( default true ) this method instantiates the
     * ServerCPCManager and the ClientCPCMamager.
     * This instantiation is pluggable as it is driven by
     * properties:
     * <ul>
     *   <li><b>openorb.server.ServerManagerClass</b>
     *      ( default org.openorb.orb.net.ServerManagerImpl )</li>
     *   <li><b>openorb.client.ClientManagerClass</b>
     *      ( default org.openorb.orb.net.ClientManagerImpl )</li>
     * </ul><br />
     * The interface of the ServerCPCManager is defined by the
     * org.openorb.orb.net.ServerManager interface and by the
     * org.openorb.orb.net.ClientManager interface for the
     * ClientCPCManager respectively.</p>
     *
     * <p>The property <b>openorb.dynany.enable</b> (default true)
     * allows to avoid loading the DynAnyFactory if it is
     * not used by the application. This can save you some amount
     * memory.</p>
     *
     * <p>The property <b>openorb.client.enable</b> also decides whether
     * to load the initiali references ORBPolicyManager and
     * PolicyCurrent into the ORB.</b>
     */
    public void load_kernel( org.openorb.orb.core.ORB orb, org.openorb.orb.config.ORBLoader loader )
    {
        boolean enable_server = loader.getBooleanProperty( "openorb.server.enable", true );
        if ( enable_server )
        {
            if ( getLogger().isDebugEnabled() && Trace.isLow() )
            {
                getLogger().debug( "Enabling server manager." );
            }
            Object [] args = new Object[ 1 ];
            Class [] args_t = new Class[ 1 ];
            args[ 0 ] = orb;
            args_t[ 0 ] = org.omg.CORBA.ORB.class;

            try
            {
                Object serverManager = loader.constructClass(
                    "openorb.server.ServerManagerClass",
                    "org.openorb.orb.net.ServerManagerImpl", args, args_t );
                orb.setFeature( "ServerCPCManager", serverManager );
            }
            catch ( final Exception ex )
            {
                final String msg = "Unable to initialize CPC server manager";
                getLogger().error( msg, ex );
                throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                        msg + " (" + ex + ")" ), ex );
            }
        }

        boolean enable_client = loader.getBooleanProperty( "openorb.client.enable", true );
        if ( enable_client )
        {
            if ( getLogger().isDebugEnabled() && Trace.isLow() )
            {
                getLogger().debug( "Enabling client manager." );
            }

            Object [] args = new Object[ 1 ];
            Class [] args_t = new Class[ 1 ];
            args[ 0 ] = orb;
            args_t[ 0 ] = org.omg.CORBA.ORB.class;

            try
            {
                Object clientManager = loader.constructClass(
                    "openorb.client.ClientManagerClass",
                    "org.openorb.orb.net.ClientManagerImpl", args, args_t );
                orb.setFeature( "ClientCPCManager", clientManager );
            }
            catch ( final Exception ex )
            {
                final String msg = "Unable to initialize CPC client manager";
                getLogger().error( msg, ex );
                throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                        msg + " (" + ex + ")" ), ex );
            }
        }

        // Load per ORB specificities

        if ( loader.getBooleanProperty( "openorb.dynany.enable", true ) )
        {
            if ( getLogger().isDebugEnabled() && Trace.isLow() )
            {
                getLogger().debug( "Enabling dynanys." );
            }

            org.omg.DynamicAny.DynAnyFactory dfac =
                  new org.openorb.orb.core.dynany.DynAnyFactoryImpl( orb );

            orb.addInitialReference( "DynAnyFactory", dfac );
        }

        // set reference for the TypeCodeFactory this is never optional.
        orb.addInitialReference( "TypeCodeFactory",
              org.openorb.orb.core.typecode.TypeCodeFactoryImpl.getInstance() );

        // set references for the policy manaagement stuff
        org.openorb.orb.policy.ORBPolicyManagerImpl policymanagerimpl
              = new org.openorb.orb.policy.ORBPolicyManagerImpl();

        orb.setFeature( "PolicyReconciler", policymanagerimpl );

        orb.setFeature( "PolicySetManager", policymanagerimpl );

        orb.setFeature( "PolicyFactory", policymanagerimpl );

        orb.setFeature( "PolicyFactoryManager", policymanagerimpl );


        if ( enable_client )
        {
            orb.addInitialReference( "ORBPolicyManager", policymanagerimpl );
            org.openorb.orb.policy.PolicyCurrentImpl policycurrent
                  = new org.openorb.orb.policy.PolicyCurrentImpl( policymanagerimpl );
            orb.addInitialReference( "PolicyCurrent", policycurrent );
        }

        // this will register any policy factories it creates.
        new org.openorb.orb.policy.OpenORBPolicyFactoryImpl( orb,
               policymanagerimpl, enable_server, enable_client );
    }
}

