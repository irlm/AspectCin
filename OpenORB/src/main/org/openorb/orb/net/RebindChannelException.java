/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

/**
 * This exception is thrown to indicate the target channel has been replaced
 * with a new implementation. Update the binding and retry using the new
 * channel.
 *
 * @author  Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:50 $
 */
public class RebindChannelException
    extends java.lang.Exception
{

    /**
     * Creates new <code>RetryChannelException</code> without detail message.
     */
    public RebindChannelException( ClientChannel channel )
    {
        m_channel = channel;
    }

    private ClientChannel m_channel;

    /**
     * Get the new client channel.
     */
    public ClientChannel getClientChannel()
    {
        return m_channel;
    }

}

