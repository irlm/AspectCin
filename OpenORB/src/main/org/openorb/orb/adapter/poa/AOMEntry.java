/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.poa;

import org.omg.PortableServer.Servant;

/**
 * This class represents an entry in the Active Object Map (AOM).
 *
 * @author Unknown
 */
class AOMEntry
{
    private Servant m_servant;
    private byte [] m_object_id;

    /**
     * Constructor.
     *
     * @param object_id The object id for the new entry.
     * @param servant The servant associated with the object id.
     */
    public AOMEntry( byte [] object_id, Servant servant )
    {
        m_servant = servant;
        m_object_id = object_id;
    }

    /**
     * Return the servant.
     *
     * @return The servant of this entry.
     */
    public Servant getServant()
    {
        return m_servant;
    }

    /**
     * Return the object id.
     *
     * @return A byte array containing the object id of this entry.
     */
    public byte[] getObjectId()
    {
        return m_object_id;
    }
}

