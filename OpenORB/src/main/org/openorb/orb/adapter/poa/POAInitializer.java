/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.poa;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.omg.PortableInterceptor.ORBInitInfo;

import org.openorb.orb.Initializer;

import org.openorb.orb.net.ServerManager;

import org.openorb.orb.pi.FeatureInitializer;
import org.openorb.orb.pi.FeatureInitInfo;

import org.openorb.orb.util.Trace;

/**
 * The POAInitializer.
 *
 * @author Chris Wood
 * @version $Revision: 1.8 $ $Date: 2004/02/10 21:02:45 $
 */
public class POAInitializer
    extends AbstractLogEnabled
    implements FeatureInitializer, Initializer
{
    public String getName()
    {
        return "poa";
    }

    public void init( ORBInitInfo orbinfo, FeatureInitInfo featureinfo )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "init" );
        }

        ServerManager svrmgr = ( ServerManager ) featureinfo.getFeature( "ServerCPCManager" );
        if ( svrmgr != null )
        {
            try
            {
                orbinfo.resolve_initial_references( "RootPOA" );
                return;
            }
            catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName ex )
            {
                // This is a check whether the RootPOA is already initialized
            }
            try
            {
                CurrentImpl curr = new CurrentImpl();
                curr.enableLogging( featureinfo.orb().getLogger() );
                orbinfo.register_initial_reference( "POACurrent", curr );
            }
            catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName ex )
            {
                final String error = "Unable to register initial reference POACurrent.";
                if ( getLogger().isErrorEnabled() )
                {
                    getLogger().error( error, ex );
                }
            }

            try
            {
                DelegateImpl deleg = new DelegateImpl( featureinfo.orb() );
                featureinfo.setFeature( "POADelegate", deleg );
                POA root = RootPOA.create_root_poa( svrmgr, new org.omg.CORBA.Policy[ 0 ] );
                orbinfo.register_initial_reference( "RootPOA", root );
                deleg.init();
                PolicyFactoryImpl pf = PolicyFactoryImpl.getInstance();
                orbinfo.register_policy_factory(
                       org.omg.PortableServer.THREAD_POLICY_ID.value, pf );
                orbinfo.register_policy_factory(
                       org.omg.PortableServer.LIFESPAN_POLICY_ID.value, pf );
                orbinfo.register_policy_factory(
                       org.omg.PortableServer.ID_UNIQUENESS_POLICY_ID.value, pf );
                orbinfo.register_policy_factory(
                       org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID.value, pf );
                orbinfo.register_policy_factory(
                       org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID.value, pf );
                orbinfo.register_policy_factory(
                       org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID.value, pf );
                orbinfo.register_policy_factory(
                       org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID.value, pf );
            }
            catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName ex )
            {
                final String error = "Illegal attempt to initialize two root POAs.";
                if ( getLogger().isErrorEnabled() )
                {
                    getLogger().error( error, ex );
                }
            }
        }
    }
}

