/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.trader;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Properties;
import java.util.Vector;

import javax.naming.InitialContext;

import javax.rmi.PortableRemoteObject;

/**
 * The implementation of the trader interface.
 *
 * @author Chris Wood
 */
public class TraderImpl
    extends PortableRemoteObject
    implements TraderInterface
{
    private Vector m_offers_list;

    /**
     * Default constructor.
     *
     * @throws RemoteException ???
     */
    public TraderImpl()
        throws RemoteException
    {
        m_offers_list = new Vector();
    }

    /**
     * Add a service offer to the trader.
     *
     * @param offer The offer to register.
     * @throws RemoteException ???
     */
    public void addServiceOffer( Offer offer )
        throws RemoteException
    {
        m_offers_list.addElement( offer );
    }

    /**
     * Query the offers with a set of properties.
     *
     * @param name The name of the offer.
     * @param required The property list for which a match must be found.
     * @return The offer that matches the query of null if not matching service was found.
     * @throws RemoteException ???
     * @throws PropertyMismatch If the properties don't match.
     * @throws ServiceNotFound If no services have been registered yet.
     */
    public Remote getServiceOffer( String name, PropertyList required )
        throws RemoteException, PropertyMismatch, ServiceNotFound
    {
        int i, j;
        Offer offer;

        System.out.println( name );

        for ( i = 0; i < m_offers_list.size(); i++ )
        {
            offer = ( Offer ) m_offers_list.elementAt( i );

            if ( offer.getName().equals( name ) )
            {
                for ( j = 0; j < required.size(); j++ )
                {
                    if ( !offer.isProperty( required.get( j ) ) )
                    {
                        throw new PropertyMismatch( required.get( j ) );
                    }
                }
                return offer.getServerReference();
            }
        }
        throw new ServiceNotFound();
    }

    /**
     * Entry point of the application.
     *
     * @param args The command line options.
     */
    public static void main ( String[] args )
    {
        try
        {
            Properties env = new Properties ();
            TraderImpl trader_server = new TraderImpl();
            InitialContext context = new InitialContext ( env );
            context.bind( "Trader", trader_server );
        }
        catch ( Exception e )
        {
            System.out.println( "Unable to register the server object !" );
            e.printStackTrace();
        }
    }
}

