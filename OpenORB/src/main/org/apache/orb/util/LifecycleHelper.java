/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.apache.orb.util;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

/**
 * Static utility to process a component through is lifecycle.
 *
 * @author Stephen McConnell
 */
public final class LifecycleHelper
{
   /**
    * Utility class, do not instantiate.
    */
   private LifecycleHelper()
   {
   }

   /**
    * Processes a supplied object through a series of component lifecycle
    * phases based on the supplied parameters. Supported phases include:
    * <ul>
    * <li>LogEnababled
    * <li>Contextualizable
    * <li>Configurable
    * <li>Serviceable
    * <li>Initializable
    * </ul>
    * <p>If the target object implements one of the above phases, and the
    * corresponding argument is null, the pipeline implementation will throw a
    * <code>NullPointerException</code>.
    * @param object the object to apply lifecycle processing to
    * @param logger the logging channel to apply
    * @param context the context to apply
    * @param config the configuration to apply
    * @param manager the service manager to apply
    * @exception Exception if an error occurs dureing pipeline processing
    */
    public static void pipeline( Object object,
                                 Logger logger,
                                 Context context,
                                 Configuration config,
                                 ServiceManager manager )
       throws Exception
    {
        if ( object == null )
        {
            throw new NullPointerException(
              "Illegal null object argument." );
        }

        if ( object instanceof LogEnabled )
        {
            if ( logger == null )
            {
                throw new NullPointerException(
                  "Illegal null logger argument." );
            }
            else
            {
                ( ( LogEnabled ) object ).enableLogging( logger );
            }
        }

        if ( object instanceof Contextualizable )
        {
            if ( context == null )
            {
                throw new NullPointerException(
                  "Illegal null context argument." );
            }
            else
            {
                ( ( Contextualizable ) object ).contextualize( context );
            }
        }

        if ( object instanceof Configurable )
        {
            if ( config == null )
            {
                throw new NullPointerException(
                  "Illegal null configuration argument." );
            }
            else
            {
                ( ( Configurable ) object ).configure( config );
            }
        }

        if ( object instanceof Serviceable )
        {
            if ( manager == null )
            {
                throw new NullPointerException(
                  "Illegal null manager argument." );
            }
            else
            {
                ( ( Serviceable ) object ).service( manager );
            }
        }

        if ( object instanceof Initializable )
        {
            ( ( Initializable ) object ).initialize();
        }
    }
}

