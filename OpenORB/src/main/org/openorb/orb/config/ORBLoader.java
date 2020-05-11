/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.config;

import java.util.Iterator;
import java.net.URL;

/**
 * This interface describes what are the operations to implement in order
 * to provide an ORB loader.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:46 $
 */
public interface ORBLoader
{
    /**
     * This operation is called on the ORB to initialize it. This operation can
     * be called only once.
     */
    void init( String [] args, java.util.Properties properties,
                      org.openorb.orb.core.ORB orb );

    /**
     * This operation is used to display an OpenORB configuration.
     */
    void display_configuration();

    /**
     * Iterate over property values with the specified prefix. <p>
     *
     * @param name parent of properties. Properies of the form name + "." + xxx
     * are returned, where xxx can be anything. May be null to iterate over all
     * properies.
     * @return unmodifiable iterator over the name's decendants. This iterator
     * returns objects of type Property.
     */
    Iterator properties( String name );

    /**
     * Get the Property object with the given name.
     * @param name the property name.
     */
    Property getProperty( String name );

    /**
     * Get the string property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     */
    String getStringProperty( String name, String defl );

    /**
     * Get the string property with the given name.
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     */
    String getStringProperty( String name )
        throws PropertyNotFoundException;

    /**
     * Get the integer property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     * @throws org.omg.CORBA.INITIALIZE The property value is not parsable
     * to an int.
     */
    int getIntProperty( String name, int defl );

    /**
     * Get the integer property with the given name.
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     * @throws org.omg.CORBA.INITIALIZE The property value is not parsable
     * to an int.
     */
    int getIntProperty( String name )
        throws PropertyNotFoundException;

    /**
     * Get the boolean property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     * @throws ClassCastException The property value is not parsable to
     * a boolean.
     */
    boolean getBooleanProperty( String name, boolean defl );

    /**
     * Get the boolean property with the given name.
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     * @throws ClassCastException The property value is not parsable to
     * a boolean.
     */
    boolean getBooleanProperty( String name )
        throws PropertyNotFoundException;

    /**
     * Get the URL property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     * @throws ClassCastException The property value is not parsable to a URL.
     */
    URL getURLProperty( String name, URL defl );

    /**
     * Get the URL property with the given name.
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     * @throws ClassCastException The property value is not parsable to a URL.
     */
    URL getURLProperty( String name )
        throws PropertyNotFoundException;

    /**
     * Get the Class object property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     * @throws org.omg.CORBA.INITIALIZE the property value cannot be loaded
     * as a class.
     */
    Class getClassProperty( String name, Class defl );

    /**
     * Get the Class object property with the given name.
     * @param name the property name.
     * @param defl String name of default value to use if property not found.
     * @throws org.omg.CORBA.INITIALIZE the property value or default class
     * cannot be loaded as a class.
     */
    Class getClassProperty( String name, String defl );

    /**
     * Get the integer property with the given name.
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     * @throws org.omg.CORBA.INITIALIZE the property value cannot be loaded
     * as a class.
     */
    Class getClassProperty( String name )
        throws PropertyNotFoundException;

    /**
     * This operation is used to load a class with the given property name and
     * default class name.
     *
     * @param prop_key Property name, this string property holds the name of the
     * class. May be null if no property is used.
     * @param defl Default class name. Used if the named property is not found.
     * May be null to indicate no load should be performed if property
     * is missing.
     * @param args arguments to constructor. If any constructor arguments are
     * primitive types then the four argument version of this function
     * must be used.
     * @return the newly constructed object, or null if the property value is
     * set to the empty string.
     * @throws java.lang.reflect.InvocationTargetException an exception occoured
     * in the constructor.
     * @throws org.omg.CORBA.INITIALIZE the property value or default class
     * cannot be loaded as a class.
     * @throws IllegalArgumentException some other problem occoured.
     */
    Object constructClass( String prop_key, String defl, Object [] args )
        throws java.lang.reflect.InvocationTargetException;

    /**
     * This operation is used to load a class with the given property name and
     * default class name.
     *
     * @param prop_key Property name, this string property holds the name of the
     * class. May be null if no property is used.
     * @param defl Default class name. Used if the named property is not found.
     * May be null to indicate no load should be performed if property
     * is missing.
     * @param args arguments to constructor. If any constructor arguments are
     * primitive types then the four argument version of this function
     * must be used.
     * @param args_t types of onstructor arguments. If any of these are null
     * they will be determined from getClass on the matching arg. Length
     * must match length of args.
     * @return the newly constructed object, or null if the property value is
     * set to the empty string.
     * @throws java.lang.reflect.InvocationTargetException an exception occoured
     * in the constructor.
     * @throws org.omg.CORBA.INITIALIZE the property value or default class
     * cannot be loaded as a class.
     * @throws IllegalArgumentException some other problem occoured.
     */
    Object constructClass( String prop_key, String defl, Object [] args,
                                  Class [] args_t )
        throws java.lang.reflect.InvocationTargetException;

    java.lang.reflect.Constructor classConstructor( String prop_key,
                                               String defl, Class [] args_t );
}

