/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.trader;

import java.io.Serializable;

import java.util.Vector;

/**
 * This is the property list implementation used by the trader server.
 *
 * @author Chris Wood
 */
public class PropertyList
    implements Serializable
{
    private Vector m_property_list;

    /**
     * Default constructor.
     */
    public PropertyList()
    {
        m_property_list = new Vector();
    }

    /**
     * Add a property to the list.
     *
     * @param name The name of the property to add.
     */
    public void addProperty( String name )
    {
        m_property_list.addElement( name );
    }

    /**
     * Remove a property from the list.
     *
     * @param name The name of the property to remove.
     */
    public void removeProperty( String name )
    {
        m_property_list.removeElement( name );
    }

    /**
     * Get a property from the list.
     *
     * @param i The list index of a property.
     * @return The property from the given index.
     */
    public String get( int i )
    {
        return ( String ) m_property_list.elementAt( i );
    }

    /**
     * Return the size of the property list.
     *
     * @return The size of the property list.
     */
    public int size()
    {
        return m_property_list.size();
    }
}

