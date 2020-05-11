/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import java.lang.reflect.Constructor;
import java.net.InetAddress;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.omg.CORBA.INITIALIZE;

import org.omg.PortableInterceptor.ORBInitInfo;

import org.openorb.orb.Initializer;

import org.openorb.orb.net.Address;
import org.openorb.orb.net.SocketFactory;
import org.openorb.orb.net.TransportClientInitializer;

import org.openorb.orb.pi.FeatureInitInfo;

import org.openorb.orb.util.Trace;

import org.openorb.util.NumberCache;

/**
 * Interface for creating sockets.
 *
 * @author Chris Wood
 * @version $Revision: 1.10 $ $Date: 2004/02/10 21:02:49 $
 */
public class IIOPTransportClientInitializer
    extends AbstractLogEnabled
    implements TransportClientInitializer, Initializer
{
    private static final Constructor IIOP_CTOR;
    private SocketFactory m_socketFactory;

    static
    {
        try
        {
            IIOP_CTOR = IIOPTransport.class.getConstructor(
                  new Class [] { InetAddress.class, Integer.TYPE,
                  Logger.class, SocketFactory.class } );
        }
        catch ( Exception ex )
        {
            throw new CascadingRuntimeException(
                  "Exception during the construction of class IIOPTransport.", ex );
        }
    }

    /**
     * Default constructor.
     */
    public IIOPTransportClientInitializer()
    {
    }

    public String getName()
    {
        return "iiop-client-init";
    }

    /**
     * Initialize the instance.
     *
     * @param orbinfo
     * @param featureinfo
     */
    public void init( final ORBInitInfo orbinfo, final FeatureInitInfo featureinfo )
    {
        if ( getLogger().isDebugEnabled() && Trace.isLow() )
        {
            getLogger().debug( "init" );
        }

        m_socketFactory = ( SocketFactory ) featureinfo.getFeature( "IIOP.SocketFactory" );
        if ( null == m_socketFactory )
        {
            throw new INITIALIZE( "Feature [IIOP.SocketFactory] not set" );
        }
    }

    /**
     * Set the MessageTransport constructor for each of the addresses.
     * All the addresses will be alternative endpoints from a single IOR profile.
     *
     * @return new list of addresses.
     */
    public Address[] establishTransports( final Address[] addresses )
    {
        for ( int i = 0; i < addresses.length; i++ )
        {
            final IIOPAddress addr = ( IIOPAddress ) addresses[ i ];
            final Object [] args = new Object[] { addr.get_host(),
                    NumberCache.getInteger( addr.get_port() ), getLogger(),
                    m_socketFactory };

            addresses[ i ].setTransportConstructor( IIOP_CTOR, args );
        }
        return addresses;
    }
}

