/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

/**
 * General interface for a communications channel. A communications channel can
 * either send or recieve requests and typicaly has two associated worker
 * threads, one for reading messages and one for writing messages.
 *
 * @author Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:50 $
 */
public interface Channel
{
    /**
     * Active request count. This is the number of requests which have not
     * yet been sent or are still expecting a reply. This will return -1 if the
     * channel has been perminently closed.
     */
    int active_requests();

    /**
     * Indication of channel age. This will result in a call to peek_request_id
     * if active_requests would be non-zero, otherwise it returns whatever
     * peek_request_id returned the last time active_requests dropped to zero.
     */
    int channel_age();

    /**
     * Donate a thread for recieving messages. This function returns when
     * interrupt is called on the thread or the channel is closed.
     */
    void run_recv();

    /**
     * Wait the specified amount of time for an incoming message.
     * @return false if the channel is closed.
     */
    boolean recv( int timeout );

    /**
     * Obtain the transport which created this channel.
     *
     * @return The Transport which created this channel.
     */
    Transport transport();
}

