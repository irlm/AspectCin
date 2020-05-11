/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.rmi.trader;

import java.rmi.RemoteException;

/**
 * An exception for indicating property mismatch.
 *
 * @author Chris Wood
 */
public class PropertyMismatch
    extends RemoteException
{
    private String m_property_name;

    /**
     * Constructor.
     *
     * @param name The name of the property.
     */
    public PropertyMismatch( String name )
    {
        super( "Property " + name + " was not found !" );
        m_property_name = name;
    }

    /**
     * Return the name of the property that was not found.
     *
     * @return The name of the property.
     */
    public String getName()
    {
        return m_property_name;
    }
}

