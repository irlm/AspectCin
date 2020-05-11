/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

/**
 * This class is the IOR interceptor manager.
 *
 * @author Jerome Daniel
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:51 $
 */
public class SimpleIORManager
    implements org.openorb.orb.pi.IORManager
{
    /**
     * IOR interceptors list
     */
    private org.omg.PortableInterceptor.IORInterceptor [] m_list;

    /**
     * Constructor
     */
    public SimpleIORManager( org.omg.PortableInterceptor.IORInterceptor [] list )
    {
        m_list = list;
    }

    /**
     * This operation must be called from the IOR interception point.
     */
    public void establish_components( org.omg.PortableInterceptor.IORInfo info )
    {
        for ( int i = 0; i < m_list.length; i++ )
        {
            try
            {
                m_list[ i ].establish_components( info );
            }
            catch ( Exception ex )
            {
                System.err.println( "An unexpteced exception occured: " + ex );
                ex.printStackTrace();
            }
        }
    }
}

