/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.boa;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.omg.PortableInterceptor.ORBInitInfo;

import org.openorb.orb.Initializer;

import org.openorb.orb.net.ServerManager;

import org.openorb.orb.pi.FeatureInitInfo;
import org.openorb.orb.pi.FeatureInitializer;

/**
 * This class initializes a BOA instance.
 *
 * @author Chris Wood
 * @version $Revision: 1.6 $ $Date: 2004/02/10 21:02:45 $
 */
public class BOAInitializer
    extends AbstractLogEnabled
    implements FeatureInitializer, Initializer
{
    public String getName()
    {
        return "boa";
    }

    /**
     * Creates a BOA instance and attaches it with the server manager.
     * @param orbinfo ORB standard information.
     * @param featureinfo OpenORB specific information.
     */
    public void init( ORBInitInfo orbinfo, FeatureInitInfo featureinfo )
    {
        ServerManager svrmgr = ( ServerManager ) featureinfo.getFeature( "ServerCPCManager" );
        if ( svrmgr != null )
        {
            BOA boa = new BOA( svrmgr );
            if ( featureinfo.orb().getFeature( "BOA" ) == null )
            {
                featureinfo.orb().setFeature( "BOA", boa );
            }
        }
    }
}

