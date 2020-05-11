/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.io.PrintStream;

/**
 * This class has the ability to dump a run-time object onto a
 * <code>PrintStream</code>.
 */
public class ObjectDumper
{
    private Object m_targetObject;
    private Class m_targetClass;
    private Constructor[] m_constructors;
    private Field[] m_fields;
    private Method[] m_methods;

    /**
     * Create a object dumper with the target object
     * @param obj The target object
     */
    public ObjectDumper( Object obj )
    {
        m_targetObject = obj;
        m_targetClass = obj.getClass();
        m_constructors = m_targetClass.getDeclaredConstructors();
        m_fields = m_targetClass.getDeclaredFields();
        m_methods = m_targetClass.getDeclaredMethods();
    }

    /**
     * Dump the class information
     * @param stream The stream which the information should be dumped upon
     */
    public void dumpClass( PrintStream stream )
    {
        stream.println( "                          CLASS:" );
        stream.println( "==================================================================" );
        stream.print( "NAME:                     " );
        stream.println( m_targetClass.getName() );

        int m = m_targetClass.getModifiers();
        String modifier = "";
        if ( ( m & Modifier.PRIVATE ) != 0 )
        {
            modifier += "private ";
        }
        if ( ( m & Modifier.PROTECTED ) != 0 )
        {
            modifier += "protected ";
        }
        if ( ( m & Modifier.PUBLIC ) != 0 )
        {
            modifier += "public ";
        }
        if ( ( m & Modifier.STATIC ) != 0 )
        {
            modifier += "static ";
        }
        if ( ( m & Modifier.SYNCHRONIZED ) != 0 )
        {
            modifier += "synchronized ";
        }
        if ( ( m & Modifier.ABSTRACT ) != 0 )
        {
            modifier += "abstract ";
        }
        if ( ( m & Modifier.FINAL ) != 0 )
        {
            modifier += "final ";
        }
        if ( ( m & Modifier.INTERFACE ) != 0 )
        {
            modifier += "interface ";
        }
        stream.print( "MODIFIER:                 " );
        stream.println( modifier );

        String superClass = m_targetClass.getSuperclass().getName();
        if ( superClass != null && superClass.length() > 0 )
        {
            stream.print( "SUPER CLASS:              " );
            stream.println( superClass );
        }

        Class[] interfaces = m_targetClass.getInterfaces();
        if ( interfaces != null && interfaces.length > 0 )
        {
            stream.print( "INTERFACES:               " );
            for ( int i = 0; i < interfaces.length; i++ )
            {
                stream.print( interfaces[i].getName() );
                if ( i < interfaces.length - 1 )
                {
                    stream.print( ", " );
                }
            }
        }
        stream.flush();
    }

    /**
     * Dump the constructor information
     * @param stream The stream which the information should be dumped upon
     */
    public void dumpConstructors( PrintStream stream )
    {
        stream.println( "                          CONSTRUCTORS:" );
        stream.println( "==================================================================" );
        for ( int i = 0; i < m_constructors.length; i++ )
        {
            Constructor constructor = m_constructors[i];

            String name = constructor.getName();

            int m = constructor.getModifiers();
            String modifier = "";
            if ( ( m & Modifier.PRIVATE ) != 0 )
            {
                modifier += "private ";
            }
            if ( ( m & Modifier.PROTECTED ) != 0 )
            {
                modifier += "protected ";
            }
            if ( ( m & Modifier.PUBLIC ) != 0 )
            {
                modifier += "public ";
            }
            if ( ( m & Modifier.STATIC ) != 0 )
            {
                modifier += "static ";
            }
            if ( ( m & Modifier.SYNCHRONIZED ) != 0 )
            {
                modifier += "synchronized ";
            }
            if ( ( m & Modifier.ABSTRACT ) != 0 )
            {
                modifier += "abstract ";
            }
            if ( ( m & Modifier.FINAL ) != 0 )
            {
                modifier += "final ";
            }
            if ( ( m & Modifier.TRANSIENT ) != 0 )
            {
                modifier += "transient ";
            }
            if ( ( m & Modifier.VOLATILE ) != 0 )
            {
                modifier += "volatile ";
            }
            if ( ( m & Modifier.NATIVE ) != 0 )
            {
                modifier += "native ";
            }
            if ( ( m & Modifier.STRICT ) != 0 )
            {
                modifier += "strict ";
            }

            stream.print( "MODIFIER:                 " );
            stream.println( modifier );
            stream.print( "NAME:                     " );
            stream.println( name );

            Class[] parameters = constructor.getParameterTypes();
            if ( parameters != null && parameters.length > 0 )
            {
                stream.print( "PARAMETERS:               " );
                for ( int j = 0; j < parameters.length; j++ )
                {
                    stream.print( parameters[j].getName() );
                    if ( j < parameters.length - 1 )
                    {
                        stream.print( ", " );
                    }
                }
                stream.println();
            }

            Class[] exceptions = constructor.getExceptionTypes();
            if ( exceptions != null && exceptions.length > 0 )
            {
                stream.print( "EXCEPTIONS:               " );
                for ( int j = 0; j < exceptions.length; j++ )
                {
                    stream.print( exceptions[j].getName() );
                    if ( j < exceptions.length - 1 )
                    {
                        stream.print( ", " );
                    }
                }
                stream.println();
            }

            if ( i < m_constructors.length - 1 )
            {
                stream.println( "--------------------------------"
                                + "----------------------------------" );
            }
        }
        stream.flush();
    }

    /**
     * Dump the field information
     * @param stream The stream which the information should be dumped upon
     */
    public void dumpFields( PrintStream stream )
    {
        stream.println( "                          FIELDS:" );
        stream.println( "==================================================================" );
        for ( int i = 0; i < m_fields.length; i++ )
        {
            try
            {
                Field field = m_fields[i];

                String name = field.getName();

                int m = field.getModifiers();
                String modifier = "";
                if ( ( m & Modifier.PRIVATE ) != 0 )
                {
                    modifier += "private ";
                }
                if ( ( m & Modifier.PROTECTED ) != 0 )
                {
                    modifier += "protected ";
                }
                if ( ( m & Modifier.PUBLIC ) != 0 )
                {
                    modifier += "public ";
                }
                if ( ( m & Modifier.STATIC ) != 0 )
                {
                    modifier += "static ";
                }
                if ( ( m & Modifier.SYNCHRONIZED ) != 0 )
                {
                    modifier += "synchronized ";
                }
                if ( ( m & Modifier.ABSTRACT ) != 0 )
                {
                    modifier += "abstract ";
                }
                if ( ( m & Modifier.FINAL ) != 0 )
                {
                    modifier += "final ";
                }
                if ( ( m & Modifier.TRANSIENT ) != 0 )
                {
                    modifier += "transient ";
                }
                if ( ( m & Modifier.VOLATILE ) != 0 )
                {
                    modifier += "volatile ";
                }

                String className = field.getType().getName();

                stream.print( "MODIFIER:                 " );
                stream.println( modifier );
                stream.print( "CLASS:                    " );
                stream.println( className );
                stream.print( "NAME:                     " );
                stream.println( name );
                Object value = field.get( m_targetObject );
                stream.println( "VALUE:" );
                if ( className.startsWith( "[" ) )
                {
                    printArray( value, stream );
                    stream.println();
                }
                else
                {
                    stream.println( value );
                }

                if ( i < m_fields.length - 1 )
                {
                    stream.println( "---------------------------------"
                                    + "---------------------------------" );
                }
            }
            catch ( IllegalAccessException iae )
            {
                String fieldName = m_fields[i].getName();
                if ( fieldName.length() > 1 )
                {
                    fieldName = fieldName.substring( 0, 1 ).toUpperCase()
                        + fieldName.substring( 1 );
                }
                else
                {
                    fieldName = fieldName.toUpperCase();
                }
                Method m = null;

                // Check for getX()
                try
                {
                    m = m_targetClass.getDeclaredMethod( "get" + fieldName, null );
                }
                catch ( Exception e )
                {
                    // Method not found; ignore
                }

                if ( m == null )
                {
                    // Check for isX()
                    try
                    {
                        m = m_targetClass.getDeclaredMethod( "is" + fieldName, null );
                    }
                    catch ( Exception e )
                    {
                        // Method not found; ignore
                    }
                }

                if ( m != null )
                {
                    try
                    {
                        Object o = m.invoke( m_targetObject, null );
                        String n = o.getClass().getName();
                        stream.println( "VALUE:" );
                        if ( n.startsWith( "[" ) )
                        {
                            printArray( o, stream );
                        }
                        else
                        {
                            stream.print( o );
                        }
                        stream.println();
                    }
                    catch ( Exception e )
                    {
                        // The object could not be invoked; ignore
                    }
                }

                if ( i < m_fields.length - 1 )
                {
                    stream.println( "-------------------------------"
                                    + "-----------------------------------" );
                }
            }
        }
        stream.flush();
    }

    /**
     * Dump an array based object
     * @param obj The object
     * @param stream The output stream
     */
    private void printArray( Object obj, PrintStream stream )
    {
        int i = 0;
        boolean first = true;
        stream.print( '{' );
        try
        {
            while ( true )
            {
                Object o = Array.get( obj, i );
                if ( !first )
                {
                    stream.print( ", " );
                }
                else
                {
                    first = false;
                }
                String n = o.getClass().getName();
                if ( n.startsWith( "[" ) )
                {
                    printArray( o, stream );
                }
                else
                {
                    stream.print( o );
                }
                i += 1;
                if ( ( i % 50 ) == 0 )
                {
                    stream.println();
                }
            }
        }
        catch ( Exception e )
        {
            // No more elements
        }
        stream.print( '}' );
    }

    /**
     * Dump the method information
     * @param stream The stream which the information should be dumped upon
     */
    public void dumpMethods( PrintStream stream )
    {
        stream.println( "                          METHODS:" );
        stream.println( "==================================================================" );
        for ( int i = 0; i < m_methods.length; i++ )
        {
            Method method = m_methods[i];

            String name = method.getName();

            int m = method.getModifiers();
            String modifier = "";
            if ( ( m & Modifier.PRIVATE ) != 0 )
            {
                modifier += "private ";
            }
            if ( ( m & Modifier.PROTECTED ) != 0 )
            {
                modifier += "protected ";
            }
            if ( ( m & Modifier.PUBLIC ) != 0 )
            {
                modifier += "public ";
            }
            if ( ( m & Modifier.STATIC ) != 0 )
            {
                modifier += "static ";
            }
            if ( ( m & Modifier.SYNCHRONIZED ) != 0 )
            {
                modifier += "synchronized ";
            }
            if ( ( m & Modifier.ABSTRACT ) != 0 )
            {
                modifier += "abstract ";
            }
            if ( ( m & Modifier.FINAL ) != 0 )
            {
                modifier += "final ";
            }
            if ( ( m & Modifier.TRANSIENT ) != 0 )
            {
                modifier += "transient ";
            }
            if ( ( m & Modifier.VOLATILE ) != 0 )
            {
                modifier += "volatile ";
            }
            if ( ( m & Modifier.NATIVE ) != 0 )
            {
                modifier += "native ";
            }
            if ( ( m & Modifier.STRICT ) != 0 )
            {
                modifier += "strict ";
            }

            String returnType = method.getReturnType().getName();

            stream.print( "MODIFIER:                 " );
            stream.println( modifier );
            stream.print( "RETURN TYPE:              " );
            stream.println( returnType );
            stream.print( "NAME:                     " );
            stream.println( name );

            Class[] parameters = method.getParameterTypes();
            if ( parameters != null && parameters.length > 0 )
            {
                stream.print( "PARAMETERS:               " );
                for ( int j = 0; j < parameters.length; j++ )
                {
                    stream.print( parameters[j].getName() );
                    if ( j < parameters.length - 1 )
                    {
                        stream.print( ", " );
                    }
                }
                stream.println();
            }

            Class[] exceptions = method.getExceptionTypes();
            if ( exceptions != null && exceptions.length > 0 )
            {
                stream.print( "EXCEPTIONS:               " );
                for ( int j = 0; j < exceptions.length; j++ )
                {
                    stream.print( exceptions[j].getName() );
                    if ( j < exceptions.length - 1 )
                    {
                        stream.print( ", " );
                    }
                }
                stream.println();
            }

            if ( i < m_methods.length - 1 )
            {
                stream.println( "---------------------------------"
                                + "---------------------------------" );
            }
        }
        stream.flush();
    }

    /**
     * Dump all information
     * @param stream The stream which the information should be dumped upon
     */
    public void dumpAll( PrintStream stream )
    {
        dumpClass( stream );
        stream.println( "------------------------------------------------------------------" );
        dumpConstructors( stream );
        stream.println( "------------------------------------------------------------------" );
        dumpFields( stream );
        stream.println( "------------------------------------------------------------------" );
        dumpMethods( stream );
    }
}
