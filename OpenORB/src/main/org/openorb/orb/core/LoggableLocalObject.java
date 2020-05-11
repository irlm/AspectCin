/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core;

import org.omg.CORBA.LocalObject;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;

/**
 * Utility class extending LocalObject with support for logging
 * operations.
 */
public class LoggableLocalObject
    extends LocalObject
    implements LogEnabled
{
    private Logger m_logger;

   /**
    * Supply a logging channel to the instance.
    */
    public void enableLogging( Logger logger )
    {
        m_logger = logger;
    }

   /**
    * Return the logging channel used by the instance.
    * @return the <code>Logger</code>
    * @exception IllegalStateException if the logging channel has not been set
    */
    protected Logger getLogger( )
    {
        if ( m_logger == null )
        {
            throw new IllegalStateException( "Logging channel has not been assigned." );
        }
        return m_logger;
    }
}
