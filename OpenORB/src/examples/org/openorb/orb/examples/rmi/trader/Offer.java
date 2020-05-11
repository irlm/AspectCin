/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.trader;

import java.io.Serializable;

import java.rmi.Remote;

/**
 * Implementation of an offer.
 *
 * @author Chris Wood
 */
public class Offer
    implements Serializable
{
    private String m_interface_name;
    private PropertyList m_property_list;
    private Remote m_server_reference;

    /**
     * Constructor.
     *
     * @param ref The server reference.
     * @param name The interface name.
     */
    public Offer( Remote ref, String name )
    {
        m_interface_name = name;
        m_property_list = new PropertyList();
        m_server_reference = ref;
    }

    /**
     * Add a property to the offer.
     *
     * @param name The name of the property.
     */
    public void addProperty( String name )
    {
        m_property_list.addProperty( name );
    }

    /**
     * Remove a property from the offer.
     *
     * @param name The name of the property.
     */
    public void removeProperty( String name )
    {
        m_property_list.removeProperty( name );
    }

    /**
     * Get the interface name of the offer.
     *
     * @return The interface name of the offer.
     */
    public String getName()
    {
        return m_interface_name;
    }

    /**
     * Get the remote server name of the offer.
     *
     * @return The remote server name of the offer.
     */
    public Remote getServerReference()
    {
        return m_server_reference;
    }

    /**
     * Check whether the given name is a property.
     *
     * @param name The name of the property.
     * @return True when a property is found, false otherwise.
     */
    public boolean isProperty( String name )
    {
        int i;
        String n;
        for ( i = 0; i < m_property_list.size(); i++ )
        {
            n = m_property_list.get( i );
            if ( n.equals( name ) )
            {
                return true;
            }
        }
        return false;
    }
}

