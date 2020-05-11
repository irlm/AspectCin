/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.config;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Set;
import java.util.TreeSet;

import org.openorb.util.ExceptionTool;
import org.openorb.util.NumberCache;

import org.omg.CORBA.INITIALIZE;

/**
 * This class provides information about an OpenORB property. The class cannot
 * be constructed outside of the Properties class.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.7 $ $Date: 2004/11/20 13:11:44 $
 */
public class Property
{
    private static final Set TRUE_SET = new TreeSet( String.CASE_INSENSITIVE_ORDER );
    private static final Set FALSE_SET = new TreeSet( String.CASE_INSENSITIVE_ORDER );

    static
    {
        TRUE_SET.add( "true" );
        FALSE_SET.add( "false" );

        TRUE_SET.add( "yes" );
        FALSE_SET.add( "no" );

        TRUE_SET.add( "on" );
        FALSE_SET.add( "off" );
    }

    private final String m_name;
    private final String m_value;
    private Object m_typed;
    private Properties m_props;

    Property( final String name, final String value, final Properties props )
    {
        m_name = name;
        m_value = value;
        m_props = props;
    }

    Property( final String name, final String value, final Object typed )
    {
        m_name = name;
        m_value = value;
        m_typed = typed;
    }

    /**
     * Get the property name.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Get the property value as a string.
     */
    public String getValue()
    {
        return m_value;
    }

    /**
     * Get the property value as an integer.
     * @throws INITIALIZE the property value cannot be parsed as an int.
     */
    public int getIntValue()
    {
        if ( m_typed != null && m_typed instanceof Integer )
        {
            return ( ( Integer ) m_typed ).intValue();
        }
        int base = 10;

        String str = m_value;

        // check for hex and octal values
        if ( str.startsWith( "0x" ) && str.length() > 2 )
        {
            base = 16;
            str = str.substring( 2 );
        }
        else if ( str.startsWith( "-0x" ) && str.length() > 3 )
        {
            base = 16;
            str = "-" + str.substring( 3 );
        }
        else if ( str.startsWith( "0" ) || str.startsWith( "-0" ) )
        {
            base = 8;
        }

        // convert
        int ret;

        try
        {
            ret = Integer.parseInt( str, base );
        }
        catch ( final NumberFormatException ex )
        {
            throw ExceptionTool.initCause( new INITIALIZE(
                  "The property value \"" + m_name
                  + "\" cannot be parsed as an integer (" + ex + ")" ), ex );
        }

        m_typed = NumberCache.getInteger( ret );
        return ret;
    }

    /**
     * Get the property value as a boolean.
     * @throws INITIALIZE the property value cannot be parsed as a boolean.
     */
    public boolean getBooleanValue()
    {
        if ( m_typed != null && m_typed instanceof Boolean )
        {
            return ( ( Boolean ) m_typed ).booleanValue();
        }

        if ( TRUE_SET.contains( m_value ) )
        {
            m_typed = Boolean.TRUE;
            return true;
        }
        else if ( FALSE_SET.contains( m_value ) )
        {
            m_typed = Boolean.FALSE;
            return false;
        }
        throw new INITIALIZE( "The property value \"" + m_name
                  + "\" cannot be parsed as a boolean (" + m_value + ")" );
    }

    /**
     * Get the property value as a Class object.
     * @throws INITIALIZE the property value cannot be loaded as a class.
     */
    public Class getClassValue()
    {
        if ( m_typed != null && m_typed instanceof Class )
        {
            return ( Class ) m_typed;
        }
        try
        {
            Class ret = Thread.currentThread().getContextClassLoader().loadClass( m_value );
            m_typed = ret;
            return ret;
        }
        catch ( final ClassNotFoundException ex )
        {
            throw ExceptionTool.initCause( new INITIALIZE(
                  "The property value \"" + m_name
                  + "\" cannot be loaded as a class (" + ex + ")" ), ex );
        }
    }

    /**
     * Get the property value as a URL.
     * @throws INITIALIZE the property value cannot be parsed as a URL.
     */
    public URL getURLValue()
    {
        if ( m_typed != null && m_typed instanceof URL )
        {
            return ( URL ) m_typed;
        }
        String str = m_props.formatString( m_value );

        try
        {
            URL url = new URL( str );
            m_typed = url;
            return url;
        }
        catch ( final MalformedURLException ex )
        {
            java.io.File file = new java.io.File( str );

            if ( file.exists() )
            {
                try
                {
                    URL url = file.toURL();
                    m_typed = url;
                    return url;
                }
                catch ( MalformedURLException ex1 )
                {
                    // An exception will be thrown below
                }
            }

            throw ExceptionTool.initCause( new INITIALIZE(
                  "The property value \"" + m_name
                  + "\" cannot be parsed as a URL (" + ex + ")" ), ex );
        }
    }
}
