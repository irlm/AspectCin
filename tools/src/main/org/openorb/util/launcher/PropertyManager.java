/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.launcher;

import java.util.Properties;

/**
 * Class for managing properties.
 *
 * @author Richard G Clark
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:28:45 $
 */
public final class PropertyManager
{
    public static final PropertyManager JAVA_PROTOCOL_HANDLER_PKGS
            = getPropertyManager( "java.protocol.handler.pkgs", "|" );

    private final Properties m_properties;
    private final String m_name;
    private final String m_separator;


    private PropertyManager( final Properties properties, final String name,
            final String separator )
    {
        m_properties = properties;
        m_name = name;
        m_separator = separator;
    }

    public static PropertyManager getPropertyManager( final Properties properties,
            final String name, final String separator )
    {
        return new PropertyManager( properties, name, separator );

    }

    public static PropertyManager getPropertyManager( final Properties properties,
            final String name )
    {
        return getPropertyManager( properties, name, " " );
    }


    public static PropertyManager getPropertyManager( final String name,
            final String separator )
    {
        return getPropertyManager( System.getProperties(), name, separator );

    }

    public static PropertyManager getPropertyManager( final String name )
    {
        return getPropertyManager( name, " " );
    }


    public String getSeparator()
    {
        return m_separator;
    }

    public String getName()
    {
        return m_name;
    }

    public String getValue()
    {
        return m_properties.getProperty( m_name );
    }

    public Object setValue( final String value )
    {
        return m_properties.setProperty( m_name, value );
    }

    public Object setValue( final String prefix, final String postfix )
    {
        if ( null != prefix )
        {
            if ( null != postfix )
            {
                return setValue( prefix + m_separator + postfix );
            }

            return setValue( prefix );
        }

        if ( null != postfix )
        {
            return setValue( postfix );
        }

        return setValue( null );
    }

    public void prefixValue( final String value )
    {
        setValue( value, getValue() );
    }

    public void postfixValue( final String value )
    {
        setValue( getValue(), value );
    }
}

