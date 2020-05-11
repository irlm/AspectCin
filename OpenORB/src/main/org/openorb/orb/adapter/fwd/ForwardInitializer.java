/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.fwd;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.omg.PortableInterceptor.ORBInitializer;

import org.openorb.orb.Initializer;

import org.openorb.orb.adapter.IORUtil;

import org.openorb.orb.corbaloc.CorbalocServiceHelper;
import org.openorb.orb.corbaloc._CorbalocServiceStub;

import org.openorb.orb.pi.FeatureInitializer;
import org.openorb.orb.pi.FeatureInitInfo;

import org.openorb.orb.net.ServerManager;

/**
 * This is the initializer for the forward adapter.
 *
 * @author Unknown
 */
public class ForwardInitializer
    extends org.omg.CORBA.LocalObject
    implements FeatureInitializer, ORBInitializer, Initializer, LogEnabled
{
    private Logger m_logger = null;
    private ServerManager m_svr_mgr;

    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

    public Logger getLogger()
    {
        return m_logger;
    }

    public String getName()
    {
        return "fwd";
    }

    public void pre_init( org.omg.PortableInterceptor.ORBInitInfo info )
    {
    }

    public void init( org.omg.PortableInterceptor.ORBInitInfo orbinfo, FeatureInitInfo featureinfo )
    {
        m_svr_mgr = ( ServerManager ) featureinfo.getFeature( "ServerCPCManager" );
        if ( m_svr_mgr != null )
        {
            new ForwardAdapter( m_svr_mgr );
        }
    }

    public void post_init( org.omg.PortableInterceptor.ORBInitInfo info )
    {
        if ( m_svr_mgr != null )
        {
            org.openorb.orb.core.ORB orb = ( org.openorb.orb.core.ORB ) m_svr_mgr.orb();

            org.openorb.orb.pi.ComponentSet cset =
                  new org.openorb.orb.pi.ComponentSet( orb, null, null );
            cset.interception_point();

            org.omg.IOP.IOR ior = IORUtil.construct_ior( CorbalocServiceHelper.id(),
                    ForwardAdapter.CORBALOC_SVC_ID, cset, m_svr_mgr.get_protocol_ids(), orb );

            org.omg.CORBA.portable.Delegate delegate =
                  new org.openorb.orb.core.Delegate( orb, ior );

            _CorbalocServiceStub service = new _CorbalocServiceStub();

            service._set_delegate( delegate );

            orb.addInitialReference( ForwardAdapter.CORBALOC_SVC_NAME, service );
        }
    }
}

