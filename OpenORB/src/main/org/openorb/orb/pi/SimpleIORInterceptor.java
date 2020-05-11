/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.pi;

import org.omg.IOP.TaggedComponent;

/**
 * This interceptor will add a single or multiple components to the IIOP profile
 *
 * @author Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:51 $
 */
public class SimpleIORInterceptor
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.IORInterceptor
{
    private TaggedComponent [] m_components;
    private int m_profile;
    private String m_name;

    /**
     * Add single component to the identified profile.
     * @param profile profile to add component to.
     * @param component component to add.
     */
    public SimpleIORInterceptor( int profile, TaggedComponent component )
    {
        m_components = new TaggedComponent[ 1 ];
        m_components[ 0 ] = component;
        m_profile = profile;
        m_name = "";
    }

    /**
     * Add single component to the identified profile.
     *
     * @param name name of the interceptor. Use to ensure unequeness.
     * @param profile profile to add component to.
     * @param component component to add.
     */
    public SimpleIORInterceptor( String name, int profile, TaggedComponent component )
    {
        m_components = new TaggedComponent[ 1 ];
        m_components[ 0 ] = component;
        m_profile = profile;
        m_name = name;
    }

    /**
     * Add some components to the identified profile.
     *
     * @param profile profile to add component to.
     * @param components components to add.
     */
    public SimpleIORInterceptor( int profile, TaggedComponent[] components )
    {
        m_components = components;
        m_profile = profile;
        m_name = "";
    }

    /**
     * Add some components to the identified profile.
     *
     * @param name name of the interceptor. Use to ensure unequeness.
     * @param profile profile to add component to.
     * @param components components to add.
     */
    public SimpleIORInterceptor( String name, int profile, TaggedComponent[] components )
    {
        m_name = name;
        m_components = components;
        m_profile = profile;
    }

    public void establish_components( org.omg.PortableInterceptor.IORInfo info )
    {
        for ( int i = 0; i < m_components.length; ++i )
        {
            info.add_ior_component_to_profile( m_components[ i ], m_profile );
        }
    }

    public String name()
    {
        return m_name;
    }

    public void destroy()
    {
    }
}

