/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.net;

/**
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:50 $
 */
public class RequestIDAllocator
    extends Object
{
    private static int s_next_request_id;

    /**
     * Get a uneque request ID for a request.
     */
    public static synchronized int get_request_id()
    {
        return ( ( ++s_next_request_id ) > 0 ) ? s_next_request_id : ( s_next_request_id = 0 );
    }

    /**
     * Discover what the next ID will be without using it. This always
     * returns an even value.
     */
    public static synchronized int peek_request_id()
    {
        return s_next_request_id;
    }
}

