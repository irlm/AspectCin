/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

import org.openorb.orb.adapter.ObjectAdapter;

/**
 * Base class which implements much of the Address functionality.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:50 $
 */

public class AdapterHoldingException extends Exception
{
    AdapterHoldingException( ObjectAdapter adapter )
    {
        m_adapter = adapter;
    }

    private ObjectAdapter m_adapter;

    public ObjectAdapter getObjectAdapter()
    {
        return m_adapter;
    }
}

