/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openorb.util.NumberCache;
import org.openorb.util.CharacterCache;

/**
 * This is an implementation of the interface DeserializationKernel
 * for the IBM JDKs 1.4.x.
 *
 * @author Michael Rumpf
 */
public class DeserializationKernelIBM14
    implements DeserializationKernel
{
    private static final String
        REFLECT_FIELD_CLASS = "com.ibm.rmi.io.PureReflectField";

    private static final boolean LIBORB_LOADED = loadORBLibrary();

    private static final Method
        ALLOCATE_NEW_OBJECT = getPrivateMethod(
                "java.io.ObjectStreamClass",
                "allocateNewObject",
                new Class[] { java.lang.Class.class, java.lang.Class.class } );

    private static final Constructor
        REFLECT_FIELD_CONSTRUCTOR = getReflectFieldConstructor();

    private static final Method
        SET = getPublicMethod(
                REFLECT_FIELD_CLASS,
                "set",
                new Class [] { Object.class, Object.class } );

    private static final Method
        SET_BOOLEAN = getPublicMethod(
                REFLECT_FIELD_CLASS,
                "setBoolean",
                new Class [] { Object.class, boolean.class } );

    private static final Method
        SET_BYTE = getPublicMethod(
                REFLECT_FIELD_CLASS,
                "setByte",
                new Class [] { Object.class, byte.class } );

    private static final Method
        SET_CHAR = getPublicMethod(
                REFLECT_FIELD_CLASS,
                "setChar",
                new Class [] { Object.class, char.class } );

    private static final Method
        SET_SHORT = getPublicMethod(
                REFLECT_FIELD_CLASS,
                "setShort",
                new Class [] { Object.class, short.class } );

    private static final Method
        SET_INT = getPublicMethod(
                REFLECT_FIELD_CLASS,
                "setInt",
                new Class [] { Object.class, int.class } );

    private static final Method
        SET_LONG = getPublicMethod(
                REFLECT_FIELD_CLASS,
                "setLong",
                new Class [] { Object.class, long.class } );

    private static final Method
        SET_FLOAT = getPublicMethod(
                REFLECT_FIELD_CLASS,
                "setFloat",
                new Class [] { Object.class, float.class } );

    private static final Method
        SET_DOUBLE = getPublicMethod(
                REFLECT_FIELD_CLASS,
                "setDouble",
                new Class [] { Object.class, double.class } );


    /**
     * Constructor is package protected so that it can be instantiated via the factory only.
     */
    DeserializationKernelIBM14()
    {
        if ( ALLOCATE_NEW_OBJECT == null )
        {
            throw new Error( "Unable to get method 'allocateNewObject' from library!" );
        }
        else
        {
            Object dummy = null;
            try
            {
                dummy = allocateNewObject( String.class, String.class );
            }
            catch ( Throwable th )
            {
                throw new Error( "Unable to execute the method 'allocateNewObject'"
                      + " from library! (" + th  + ")" );
            }
        }
    }

    /**
     * This method causes the static initializer of the class
     * com.ibm.jvm.ExtendedSystem to be executed which does an
     * initialization of the native layer.
     */
    private static boolean loadORBLibrary()
    {
        boolean bSuccess = false;
        try
        {
            Class clz = Class.forName( "com.ibm.jvm.ExtendedSystem" );
            bSuccess = true;
        }
        catch ( ClassNotFoundException ex )
        {
            // ignore, return false
        }
        return bSuccess;
    }

    /**
     * Get the field constructor from the class ReflectField.
     */
    private static Constructor getReflectFieldConstructor()
    {
        Constructor result = null;
        try
        {
            Class clz = Class.forName( REFLECT_FIELD_CLASS );
            result = clz.getConstructor( new Class[] { Field.class } );
        }
        catch ( ClassNotFoundException ex )
        {
            // error, return null
        }
        catch ( NoSuchMethodException ex )
        {
            // error, return null
        }
        return result;
    }

    /**
     * This method does the reflection stuff and handles the errors.
     */
    private static Method getPrivateMethod( String className, String methodName,
          Class[] params )
    {
        Method method = null;
        try
        {
            Class clz = Class.forName( className );
            method = clz.getDeclaredMethod( methodName, params );
            method.setAccessible( true );
        }
        catch ( ClassNotFoundException ex )
        {
            throw new Error( "Couldn't find class '" + className + "'" );
        }
        catch ( NoSuchMethodException ex )
        {
            throw new Error ( "Couldn't find method '" + methodName + "'" );
        }
        catch ( Throwable th )
        {
            throw new Error ( "An unexpected error occured! " + th );
        }
        return method;
    }

    /**
     * This method does the reflection stuff and handles the errors.
     */
    private static Method getPublicMethod( String className, String methodName,
          Class[] params )
    {
        Method method = null;
        try
        {
            Class clz = Class.forName( className );
            method = clz.getDeclaredMethod( methodName, params );
        }
        catch ( ClassNotFoundException ex )
        {
            throw new Error( "Couldn't find class '" + className + "'" );
        }
        catch ( NoSuchMethodException ex )
        {
            throw new Error ( "Couldn't find method '" + methodName + "'" );
        }
        catch ( Throwable th )
        {
            throw new Error ( "An unexpected error occured! " + th );
        }
        return method;
    }

    /**
     * This method invokes the public methods on ReflectField.
     */
    private void invokeSetMethod( Class c, String n, Method m, Object o, Object v )
    {
        Field fld = null;
        try
        {
            fld = c.getDeclaredField( n );
        }
        catch ( Exception ex )
        {
            throw new Error( "Unexpected exception (" + ex + ")" );
        }
        Object jniReflectFieldObj = null;
        try
        {
            jniReflectFieldObj = REFLECT_FIELD_CONSTRUCTOR.newInstance( new Object[] { fld } );
        }
        catch ( Exception ex )
        {
            throw new Error( "Unexpected exception (" + ex + ")" );
        }
        try
        {
            m.invoke( jniReflectFieldObj, new Object[] { o, v } );
        }
        catch ( Exception ex )
        {
            throw new Error( "Unexpected exception (" + ex + ")" );
        }
    }

    //
    // public methods from interface DeserializationKernel
    //

    /**
     * @see DeserializationKernel
     */
    public Object allocateNewObject( Class c, Class base )
        throws InstantiationException, IllegalAccessException
    {
        try
        {
            return ALLOCATE_NEW_OBJECT.invoke( null, new Object[] { c, base } );
        }
        catch ( InvocationTargetException ex )
        {
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
    }

    /**
     * @see DeserializationKernel
     */
    public void setObjectField ( Class c, String n, Object o, Object v )
    {
        invokeSetMethod( c, n, SET, o, v );
    }

    /**
     * @see DeserializationKernel
     */
    public void setBooleanField( Class c, String n, Object o, boolean v )
    {
        invokeSetMethod( c, n, SET_BOOLEAN, o, v ? Boolean.TRUE : Boolean.FALSE );
    }

    /**
     * @see DeserializationKernel
     */
    public void setByteField( Class c, String n, Object o, byte v )
    {
        invokeSetMethod( c, n, SET_BYTE, o, NumberCache.getByte( v ) );
    }

    /**
     * @see DeserializationKernel
     */
    public void setCharField( Class c, String n, Object o, char v )
    {
        invokeSetMethod( c, n, SET_CHAR, o, CharacterCache.getCharacter( v ) );
    }

    /**
     * @see DeserializationKernel
     */
    public void setShortField( Class c, String n, Object o, short v )
    {
        invokeSetMethod( c, n, SET_SHORT, o, NumberCache.getShort( v ) );
    }

    /**
     * @see DeserializationKernel
     */
    public void setIntField( Class c, String n, Object o, int v )
    {
        invokeSetMethod( c, n, SET_INT, o, NumberCache.getInteger( v ) );
    }

    /**
     * @see DeserializationKernel
     */
    public void setLongField( Class c, String n, Object o, long v )
    {
        invokeSetMethod( c, n, SET_LONG, o, NumberCache.getLong( v ) );
    }

    /**
     * @see DeserializationKernel
     */
    public void setFloatField( Class c, String n, Object o, float v )
    {
        invokeSetMethod( c, n, SET_FLOAT, o, NumberCache.getFloat( v ) );
    }

    /**
     * @see DeserializationKernel
     */
    public void setDoubleField( Class c, String n, Object o, double v )
    {
        invokeSetMethod( c, n, SET_DOUBLE, o, NumberCache.getDouble( v ) );
    }
}

