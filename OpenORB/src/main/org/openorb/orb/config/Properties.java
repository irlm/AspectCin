/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.config;

import java.net.URL;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Comparator;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;

import org.openorb.util.ExceptionTool;
import org.openorb.util.NumberCache;

/**
 * This class contains all kernel properties.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.8 $ $Date: 2004/05/13 04:09:26 $
 */
public class Properties
    implements LogEnabled
{
    private Logger m_logger = null;

    /**
     * The property set.
     */
    private SortedMap m_properties = Collections.synchronizedSortedMap(
          new TreeMap( new Comparator()
          {
              public int compare( Object o1, Object o2 )
              {
                  return ( ( String ) o1 ).compareToIgnoreCase( ( String ) o2 );
              }
          } ) );

    /**
     * Setup current logger
     */
    public void enableLogging( final Logger logger )
    {
        m_logger = logger;
    }

    /**
     * Add a string property.
     *
     * @param name the property name.
     * @param value the property value. May be null to delete named property.
     */
    public void addProperty( String name, String value )
    {
        if ( value == null )
        {
            m_properties.remove( name );
        }
        else
        {
            m_properties.put( name, new Property( name, value, this ) );
        }
    }

    /**
     * Add an integer property.
     *
     * @param name the property name.
     * @param value the property value.
     */
    public void addProperty( String name, int value )
    {
        m_properties.put( name, new Property( name, Integer.toString( value ),
              NumberCache.getInteger( value ) ) );
    }

    /**
     * Add a boolean property.
     *
     * @param name the property name.
     * @param value the property value.
     */
    public void addProperty( String name, boolean value )
    {
        Boolean booleanValue = value ? Boolean.TRUE : Boolean.FALSE;
        m_properties.put( name, new Property( name, booleanValue.toString(), booleanValue ) );
    }

    /**
     * Add a URL property.
     *
     * @param name the property name.
     * @param value the property value.
     */
    public void addProperty( String name, URL value )
    {
        m_properties.put( name, new Property( name, value.toString(), value ) );
    }

    /**
     * Add a Class property.
     *
     * @param name The property name.
     * @param clz The class. May be null to delete named property.
     */
    public void addProperty( String name, Class clz )
    {
        if ( clz == null )
        {
            m_properties.remove( name );
        }
        else
        {
            m_properties.put( name, new Property( name, clz.getName(), clz ) );
        }
    }

    /**
     * Iterate over property values with the specified prefix. <p>
     *
     * @param name parent of properties. Properies of the form name + "." + xxx
     * are returned, where xxx can be anything. May be null to iterate over all
     * properies.
     * There may also be the '*' wildcard at the end of the name. In this case the plain
     * name is used as prefix, no "." is appended.
     * @return unmodifiable iterator over the name's decendants.
     */
    public Iterator properties( String name )
    {
        if ( name == null )
        {
            return Collections.unmodifiableCollection( m_properties.values() ).iterator();
        }
        if ( name.endsWith( "*" ) )
        {
            name = name.substring( 0, name.length() - 1 );
        }
        else if ( !name.endsWith( "." ) )
        {
            name = name + ".";
        }
        return Collections.unmodifiableCollection( m_properties.subMap(
              name + "\0", name + "\uFFFF" ).values() ).iterator();
    }

    /**
     * Display properties.
     */
    public void display()
    {
        Iterator itt = properties( null );

        while ( itt.hasNext() )
        {
            Property prop = ( Property ) itt.next();
            getLogger().info( prop.getName() + "=" + prop.getValue() );
        }
    }

    /**
     * Display properties.
     */
    public void display( Logger logger )
    {
        Iterator itt = properties( null );
        while ( itt.hasNext() )
        {
            Property prop = ( Property ) itt.next();
            logger.debug( prop.getName() + "=" + prop.getValue() );
        }
    }


    /**
     * Format the string argument, replacing ${prop.name} with the value of
     * the property. Any ${prop.url} at position 0 will result in the remainder
     * of the string being resolved relative to the base URL.
     *
     * @throws IllegalArgumentException if the string has unbalanced ${ }
     */
    public String formatString( String str )
    {
        // replace the base URL
        URL base = null;
        int in;
        String spec = str;

        if ( str.startsWith( "${" ) )
        {
            in = str.indexOf( "}" );

            if ( in < 0 )
            {
                throw new IllegalArgumentException( "Unbalanced ${ } in \"" + str );
            }
            try
            {
                base = getURLProperty( str.substring( 2, in ), null );
                spec = str.substring( in + 1 );

                // avoid jdk1.2 bug
                if ( base != null && spec.startsWith( "#" ) )
                {
                    return base.toString() + spec;
                }
            }
            catch ( org.omg.CORBA.INITIALIZE ex )
            {
                // try as a string (below)
            }

        }

        int ix = spec.indexOf( "${" );

        if ( ix >= 0 )
        {
            in = 0;
            String exp;
            String prop;
            StringBuffer sb = new StringBuffer();

            do
            {
                sb.append( spec.substring( in, ix ) );
                in = spec.indexOf( "}", ix );

                if ( in < 0 )
                {
                    throw new IllegalArgumentException( "Unbalanced ${ } in \"" + str + "\"" );
                }
                prop = spec.substring( ix + 2, in );

                exp = getStringProperty( prop, null );

                if ( exp == null )
                {
                    return null;
                }
                sb.append( exp );

                ix = spec.indexOf( "${", in );
            }
            while ( ix >= 0 );

            spec = sb.toString();
        }

        if ( base != null )
        {
            try
            {
                return new URL( base, spec ).toString();
            }
            catch ( java.net.MalformedURLException ex )
            {
                return base.toString() + spec;
            }
        }

        return spec;
    }

    /**
     * Get the Property object with the given name.
     * @param name the property name.
     * @return the property, or null if none is defined.
     */
    public Property getProperty( String name )
    {
        return ( Property ) m_properties.get( name );
    }

    /**
     * Get the string property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     */
    public String getStringProperty( String name, String defl )
    {
        Property prop = ( Property ) m_properties.get( name );

        if ( prop == null )
        {
            return defl;
        }
        return prop.getValue();
    }

    /**
     * Get the string property with the given name.
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     */
    public String getStringProperty( String name )
        throws PropertyNotFoundException
    {
        Property prop = ( Property ) m_properties.get( name );

        if ( prop == null )
        {
            throw new PropertyNotFoundException( "Property '" + name + "' could not be found" );
        }
        return prop.getValue();
    }

    /**
     * Get the integer property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     * @throws org.omg.CORBA.INITIALIZE The property value is not parsable to an int.
     */
    public int getIntProperty( String name, int defl )
    {
        Property prop = ( Property ) m_properties.get( name );

        if ( prop == null )
        {
            return defl;
        }
        return prop.getIntValue();
    }

    /**
     * Get the integer property with the given name.
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     * @throws org.omg.CORBA.INITIALIZE The property value is not parsable to an int.
     */
    public int getIntProperty( String name )
        throws PropertyNotFoundException
    {
        Property prop = ( Property ) m_properties.get( name );

        if ( prop == null )
        {
            throw new PropertyNotFoundException( "Property '" + name + "' could not be found" );
        }
        return prop.getIntValue();
    }

    /**
     * Get the boolean property with the given name. The default value is used
     * if the property is missing. Property values of false or no are parsed
     * as false, all other non-missing values are parsed as true.
     *
     * @param name the property name.
     * @param defl default value to use if property not found.
     */
    public boolean getBooleanProperty( String name, boolean defl )
    {
        Property prop = ( Property ) m_properties.get( name );

        if ( prop == null )
        {
            return defl;
        }
        return prop.getBooleanValue();
    }

    /**
     * Get the boolean property with the given name. The default value is used
     * if the property is missing. Property values of false or no are parsed
     * as false, all other non-missing values are parsed as true.
     *
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     */
    public boolean getBooleanProperty( String name )
        throws PropertyNotFoundException
    {
        Property prop = ( Property ) m_properties.get( name );

        if ( prop == null )
        {
            throw new PropertyNotFoundException( "Property '" + name + "' could not be found" );
        }
        return prop.getBooleanValue();
    }

    /**
     * Get the URL property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     * @throws org.omg.CORBA.INITIALIZE The property value is not parsable to a URL.
     */
    public URL getURLProperty( String name, URL defl )
    {
        Property prop = ( Property ) m_properties.get( name );

        if ( prop == null )
        {
            return defl;
        }
        return prop.getURLValue();
    }

    /**
     * Get the integer property with the given name.
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     * @throws org.omg.CORBA.INITIALIZE The property value is not parsable to an int.
     */
    public URL getURLProperty( String name )
        throws PropertyNotFoundException
    {
        Property prop = ( Property ) m_properties.get( name );

        if ( prop == null )
        {
            throw new PropertyNotFoundException( "Property '" + name + "' could not be found" );
        }
        return prop.getURLValue();
    }

    /**
     * Get the Class object property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     * @throws org.omg.CORBA.INITIALIZE the property value cannot be loaded as a class.
     */
    public Class getClassProperty( String name, Class defl )
    {
        Property prop = ( Property ) m_properties.get( name );

        if ( prop == null )
        {
            return defl;
        }
        return prop.getClassValue();
    }

    /**
     * Get the Class object property with the given name.
     * @param name the property name.
     * @param defl String name of default value to use if property not found.
     * @throws org.omg.CORBA.INITIALIZE the property value or default class cannot
     *                    be loaded as a class.
     */
    public Class getClassProperty( String name, String defl )
    {
        Property prop = ( Property ) m_properties.get( name );

        if ( prop == null )
        {
            try
            {
                return Thread.currentThread().getContextClassLoader().loadClass( defl );
            }
            catch ( final ClassNotFoundException ex )
            {
                final String msg = "Unable to load default class \"" + defl + "\"";
                getLogger().error( msg, ex );

                throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                      msg + " (" + ex + ")" ), ex );
            }
        }

        return prop.getClassValue();
    }

    /**
     * Get the integer property with the given name.
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     * @throws org.omg.CORBA.INITIALIZE the property value cannot be loaded as a class.
     */
    public Class getClassProperty( String name )
        throws PropertyNotFoundException
    {
        Property prop = ( Property ) m_properties.get( name );

        if ( prop == null )
        {
            throw new PropertyNotFoundException( "Property '" + name + "' could not be found" );
        }
        return prop.getClassValue();
    }

    /**
     * return current Logger
     */
    private Logger getLogger()
    {
        return m_logger;
    }
}

