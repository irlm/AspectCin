/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter;

/**
 * This exception indicates that the request requires a target adapter which
 * is currently in the process of being destroyed. The request can be safely
 * be reissued once the destruction of the specified adapter has occoured.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:44 $
 */
public class AdapterDestroyedException
    extends Exception // ??? extends RuntimeException
{
    private ObjectAdapter m_adapter;
    private byte [] m_aid;

    /**
     * Create new AdapterDestroyedException.
     */
    public AdapterDestroyedException( ObjectAdapter adapter, byte [] aid )
    {
        m_adapter = adapter;
        m_aid = aid;
    }

    /**
     * Object adapter which is currently in the process of being destroyed
     */
    public ObjectAdapter getObjectAdapter()
    {
        return m_adapter;
    }

    /**
     * Adapter ID of the adapter being destroyed. This will still be available
     * once the adapter has completed it's destruction sequence.
     */
    public byte [] getAdapterID()
    {
        return m_aid;
    }
}

