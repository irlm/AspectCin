/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.HashMap;

import org.openorb.util.NumberCache;
import org.openorb.util.CharacterCache;

/**
 * This is an implementation of the interface DeserializationKernel
 * for the Sun JDKs 1.3 to 1.4 (JDKs where the ioser12 shared
 * library is present).
 *
 * @author Michael Rumpf
 */
public class DeserializationKernelSun
    implements DeserializationKernel
{
    private static final String
        IIOP_INPUT_STREAM_CLASS = "com.sun.corba.se.internal.io.IIOPInputStream";

    private static final boolean LIBIOSER12_LOADED = loadIOSerLibrary();

    private static final Method
        ALLOCATE_NEW_OBJECT = getPrivateStaticMethod(
                IIOP_INPUT_STREAM_CLASS,
                "allocateNewObject",
                new Class [] { Class.class, Class.class } );

    private static final Method
        SET_OBJECT_FIELD = getPrivateStaticMethod(
                IIOP_INPUT_STREAM_CLASS,
                "setObjectField",
                new Class [] { Object.class, Class.class, String.class,
                String.class, Object.class } );

    private static final Method
        SET_BOOLEAN_FIELD = getPrivateStaticMethod(
                IIOP_INPUT_STREAM_CLASS,
                "setBooleanField",
                new Class [] { Object.class, Class.class, String.class,
                String.class, boolean.class } );

    private static final Method
        SET_BYTE_FIELD = getPrivateStaticMethod(
                IIOP_INPUT_STREAM_CLASS,
                "setByteField",
                new Class [] { Object.class, Class.class, String.class,
                String.class, byte.class } );

    private static final Method
        SET_CHAR_FIELD = getPrivateStaticMethod(
                IIOP_INPUT_STREAM_CLASS,
                "setCharField",
                new Class [] { Object.class, Class.class, String.class,
                String.class, char.class } );

    private static final Method
        SET_SHORT_FIELD = getPrivateStaticMethod(
                IIOP_INPUT_STREAM_CLASS,
                "setShortField",
                new Class [] { Object.class, Class.class, String.class,
                String.class, short.class } );

    private static final Method
        SET_INT_FIELD = getPrivateStaticMethod(
                IIOP_INPUT_STREAM_CLASS,
                "setIntField",
                new Class [] { Object.class, Class.class, String.class,
                String.class, int.class } );

    private static final Method
        SET_LONG_FIELD = getPrivateStaticMethod(
               IIOP_INPUT_STREAM_CLASS,
               "setLongField",
               new Class [] { Object.class, Class.class, String.class,
               String.class, long.class } );

    private static final Method
        SET_FLOAT_FIELD = getPrivateStaticMethod(
                IIOP_INPUT_STREAM_CLASS,
                "setFloatField",
                new Class [] { Object.class, Class.class, String.class,
                String.class, float.class } );

    private static final Method
        SET_DOUBLE_FIELD = getPrivateStaticMethod(
                IIOP_INPUT_STREAM_CLASS,
                "setDoubleField",
                new Class [] { Object.class, Class.class, String.class,
                String.class, double.class } );

    /**
     * Signature cache for object fields. A hash map for each class, storing
     * a hash map for each field of the class.
     */
    private static final HashMap
        CLASS_FIELD_CACHE = new HashMap();

    /**
     * Constructor is package protected so that it can be instantiated via the factory only.
     */
    DeserializationKernelSun ()
    {
        if ( !LIBIOSER12_LOADED )
        {
            throw new Error( "Unable to load libioser12 from JAVA_HOME/jre/lib/i386!"
                  + " Make sure your LD_LIBRARY_PATH is correctly set!" );
        }
        else if ( ALLOCATE_NEW_OBJECT == null )
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
     * We need to get the library over the internal LibraryManager because
     * otherwise we get UnsatisfiedLinkErrors that the library has already
     * been loaded by another ClassLoader.
     */
    private static boolean loadIOSerLibrary()
    {
        boolean bSuccess = false;
        final String clzName  = "com.sun.corba.se.internal.io.LibraryManager";
        final String methName = "load";
        try
        {
            Class libmgrClz = Class.forName( clzName );
            Method method = libmgrClz.getDeclaredMethod( methName, new Class[] {} );
            bSuccess = ( ( Boolean ) method.invoke( null, null ) ).booleanValue();
        }
        catch ( ClassNotFoundException ex )
        {
            throw new Error( "Couldn't find class '" + clzName + "'" );
        }
        catch ( NoSuchMethodException ex )
        {
            throw new Error( "Couldn't find method '" + methName + "' in '" + clzName + "'" );
        }
        catch ( IllegalAccessException ex )
        {
            throw new Error( "Access not allowed for method '" + methName + "' in '"
                  + clzName + "'" );
        }
        catch ( InvocationTargetException ex )
        {
            throw new Error( "Exception while calling method '" + methName + "' in '"
                  + clzName + "'" );
        }
        catch ( Throwable th )
        {
            throw new Error( "Unexpected error occured' " + th );
        }
        return bSuccess;
    }

    /**
     * This method does the reflection stuff and handles the errors.
     */
    private static Method getPrivateStaticMethod( String className,
          String methodName, Class[] params )
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
     * Calculate the Java type signature the class clazz.
     */
    private static String getCachedSignature( Class clazz, String fldName )
    {
        String result = null;
        HashMap fieldMap = null;
        // get the map with all fiels for this class
        synchronized ( CLASS_FIELD_CACHE )
        {
            fieldMap = ( HashMap ) CLASS_FIELD_CACHE.get( clazz );
            if ( fieldMap == null )
            {
                fieldMap = new HashMap();
                CLASS_FIELD_CACHE.put( clazz, fieldMap );
            }
        }
        // get the signature for the field from the cache.
        result = ( String ) fieldMap.get( fldName );
        if ( result == null )
        {
            result = getSignature( clazz, fldName );
            if ( result != null )
            {
                synchronized ( fieldMap )
                {
                    fieldMap.put( fldName, result );
                }
            }
        }
        return result;
    }

    /**
     * Calculate the Java type signature the class clazz.
     */
    private static String getSignature( Class clazz, String fldName )
    {
        Class clz = null;
        if ( fldName != null && fldName.length() > 0 )
        {
            try
            {
                Field fld = clazz.getDeclaredField( fldName );
                clz = fld.getType();
            }
            catch ( NoSuchFieldException ex )
            {
                return null;
            }
        }
        else
        {
            clz = clazz;
        }
        String type = null;
        if ( clz.isPrimitive() )
        {
            if ( clz == Integer.TYPE )
            {
                type = "I";
            }
            else if ( clz == Byte.TYPE )
            {
                type = "B";
            }
            else if ( clz == Long.TYPE )
            {
                type = "J";
            }
            else if ( clz == Float.TYPE )
            {
                type = "F";
            }
            else if ( clz == Double.TYPE )
            {
                type = "D";
            }
            else if ( clz == Short.TYPE )
            {
                type = "S";
            }
            else if ( clz == Character.TYPE )
            {
                type = "C";
            }
            else if ( clz == Boolean.TYPE )
            {
                type = "Z";
            }
            else if ( clz == Void.TYPE )
            {
                type = "V";
            }
        }
        else if ( clz.isArray() )
        {
            Class cl = clz;
            int dim = 0;
            while ( cl.isArray() )
            {
                dim++;
                cl = cl.getComponentType();
            }
            StringBuffer sb = new StringBuffer();
            for ( int i = 0; i < dim; i++ )
            {
                sb.append( "[" );
            }
            sb.append( getSignature( cl, null ) );
            type = sb.toString();
        }
        else
        {
            type = "L" + clz.getName().replace( '.', '/' ) + ";";
        }
        return type;
    }

    //
    // public methods from interface DeserializationKernel
    //

    /**
     * @see DeserializationKernel
     */
    public final Object allocateNewObject( Class c, Class base )
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
    public final void setObjectField ( Class c, String n, Object o, Object v )
    {
        try
        {
            String sig = getCachedSignature( c, n );
            SET_OBJECT_FIELD.invoke( null, new Object[] { o, c, n, sig, v } );
        }
        catch ( InvocationTargetException ex )
        {
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( IllegalAccessException ex )
        {
            throw new Error( "Invocation not allowed! (" + ex + ")" );
        }
    }

    /**
     * @see DeserializationKernel
     */
    public final void setBooleanField( Class c, String n, Object o, boolean v )
    {
        try
        {
            SET_BOOLEAN_FIELD.invoke( null,
                  new Object[] { o, c, n, "Z", v ? Boolean.TRUE : Boolean.FALSE } );
        }
        catch ( InvocationTargetException ex )
        {
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( IllegalAccessException ex )
        {
            throw new Error( "Invocation not allowed! (" + ex + ")" );
        }
    }

    /**
     * @see DeserializationKernel
     */
    public final void setByteField( Class c, String n, Object o, byte v )
    {
        try
        {
            SET_BYTE_FIELD.invoke( null,
                    new Object[] { o, c, n, "B", NumberCache.getByte( v ) } );
        }
        catch ( InvocationTargetException ex )
        {
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( IllegalAccessException ex )
        {
            throw new Error( "Invocation not allowed! (" + ex + ")" );
        }
    }

    /**
     * @see DeserializationKernel
     */
    public final void setCharField( Class c, String n, Object o, char v )
    {
        try
        {
            SET_CHAR_FIELD.invoke( null,
                    new Object[] { o, c, n, "C", CharacterCache.getCharacter( v ) } );
        }
        catch ( InvocationTargetException ex )
        {
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( IllegalAccessException ex )
        {
            throw new Error( "Invocation not allowed! (" + ex + ")" );
        }
    }

    /**
     * @see DeserializationKernel
     */
    public final void setShortField( Class c, String n, Object o, short v )
    {
        try
        {
            SET_SHORT_FIELD.invoke( null,
                    new Object[] { o, c, n, "S", NumberCache.getShort( v ) } );
        }
        catch ( InvocationTargetException ex )
        {
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( IllegalAccessException ex )
        {
            throw new Error( "Invocation not allowed! (" + ex + ")" );
        }
    }

    /**
     * @see DeserializationKernel
     */
    public final void setIntField( Class c, String n, Object o, int v )
    {
        try
        {
            SET_INT_FIELD.invoke( null,
                    new Object[] { o, c, n, "I", NumberCache.getInteger( v ) } );
        }
        catch ( InvocationTargetException ex )
        {
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( IllegalAccessException ex )
        {
            throw new Error( "Invocation not allowed! (" + ex + ")" );
        }
    }

    /**
     * @see DeserializationKernel
     */
    public final void setLongField( Class c, String n, Object o, long v )
    {
        try
        {
            SET_LONG_FIELD.invoke( null,
                    new Object[] { o, c, n, "J", NumberCache.getLong( v ) } );
        }
        catch ( InvocationTargetException ex )
        {
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( IllegalAccessException ex )
        {
            throw new Error( "Invocation not allowed! (" + ex + ")" );
        }
    }

    /**
     * @see DeserializationKernel
     */
    public final void setFloatField( Class c, String n, Object o, float v )
    {
        try
        {
            SET_FLOAT_FIELD.invoke( null,
                    new Object[] { o, c, n, "F", NumberCache.getFloat( v ) } );
        }
        catch ( InvocationTargetException ex )
        {
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( IllegalAccessException ex )
        {
            throw new Error( "Invocation not allowed! (" + ex + ")" );
        }
    }

    /**
     * @see DeserializationKernel
     */
    public final void setDoubleField( Class c, String n, Object o, double v )
    {
        try
        {
            SET_DOUBLE_FIELD.invoke( null,
                    new Object[] { o, c, n, "D", NumberCache.getDouble( v ) } );
        }
        catch ( InvocationTargetException ex )
        {
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( IllegalAccessException ex )
        {
            throw new Error( "Invocation not allowed! (" + ex + ")" );
        }
    }
}

