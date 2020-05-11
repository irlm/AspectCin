/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import org.omg.CORBA.SystemException;

/**
 * This interface is handed to the PIManager when performing interception tasks.
 *
 * @author Jerome Daniel
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:51 $
 */
public interface RequestCallback
{
    void reply_system_exception( SystemException ex );

    void reply_runtime_exception( RuntimeException ex );

    void reply_error( Error ex );

    /**
     *
     * @param forward
     * @param permanent
     */
    void reply_location_forward( org.omg.CORBA.Object forward, boolean permanent );
}

