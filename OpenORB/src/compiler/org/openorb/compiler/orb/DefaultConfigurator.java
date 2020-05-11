/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.orb;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.openorb.compiler.Configurator;

/**
 * This class provides a default configurator.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:40 $
 */
public class DefaultConfigurator
    implements Configurator
{
    public void updateInfo( java.util.Vector includeList, java.util.Vector importLink )
    {
        org.openorb.compiler.orb.Configurator cfg = new org.openorb.compiler.orb.Configurator( new String[ 0 ], null );

        Properties props = cfg.getProperties();

        // get directories for file includes
        Iterator itt = props.properties( "compiler.idl" );

        while ( itt.hasNext() )
        {
            Property prop = ( Property ) itt.next();

            try
            {
                includeList.addElement( prop.getURLValue() );
            }
            catch ( CascadingRuntimeException ex )
            {
                // ignore non-urlable compiler.idl properties.
            }

        }

        // add default include directory. This should be last on the include list.


        URL openorbDir = props.getURLProperty( "openorb.home", null );

        if ( openorbDir != null )
        {
            try
            {
                includeList.addElement( new URL( openorbDir, "idl/" ) );
            }
            catch ( MalformedURLException ex )
            {}

        }

        // get import list


        itt = props.properties( "compiler.import" );

        while ( itt.hasNext() )
        {
            Property next = ( Property ) itt.next();
            String key = next.getName().substring( "compiler.import".length() + 1 ) + ":"
                         + next.getValue();
            importLink.addElement( key );
        }
    }
}
