/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

import java.lang.reflect.Field;

import java.util.HashMap;

/**
 * This is an implementation of the interface DeserializationKernel
 * for the IBM JDKs 1.4.x.
 *
 * @author Michael Rumpf
 */
public class DeserializationKernelNative
    implements DeserializationKernel
{
    private static final boolean LIBORB_LOADED = loadORBLibrary();

    /**
     * Signature cache for object fields. A hash map for each class, storing
     * a hash map for each field of the class.
     */
    private static final HashMap
        CLASS_FIELD_CACHE = new HashMap();

    /**
     * Constructor is package protected so that it can be instantiated via the factory only.
     */
    public DeserializationKernelNative()
    {
    }

    /**
     * Just load the openorb lib.
     */
    private static boolean loadORBLibrary()
    {
        boolean bSuccess = false;
        try
        {
            System.loadLibrary( "openorb" );
            bSuccess = true;
        }
        catch ( Throwable th )
        {
            // ignore, return false
        }
        return bSuccess;
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
     * This method allocates heap space for a instance of an object
     * of the specified class without initializing the members.
     *
     * @param aclass The class to allocate space for.
     * @param initClass Not currently used.
     */
    public Object allocateNewObject( Class aclass, Class initClass )
    {
        return nativeAllocateNewObject( aclass, initClass );
    }
    private static native Object nativeAllocateNewObject( Class aclass, Class initClass );

    /**
     * @see DeserializationKernel
     */
    public void setObjectField ( Class c, String n, Object o, Object v )
    {
        String sig = getCachedSignature( c, n );
        nativeSetObjectField( c, n, o, v, sig );
    }
    private static native void nativeSetObjectField( Class c, String n, Object o, Object v,
          String sig );

    /**
     * @see DeserializationKernel
     */
    public final void setBooleanField( Class c, String n, Object o, boolean v )
    {
        nativeSetBooleanField( c, n, o, v, "Z" );
    }
    private static native void nativeSetBooleanField( Class c, String n, Object o, boolean v,
          String sig );

    /**
     * @see DeserializationKernel
     */
    public final void setByteField( Class c, String n, Object o, byte v )
    {
        nativeSetByteField( c, n, o, v, "B" );
    }
    private static native void nativeSetByteField( Class c, String n, Object o, byte v,
          String sig );

    /**
     * @see DeserializationKernel
     */
    public void setCharField( Class c, String n, Object o, char v )
    {
        nativeSetCharField( c, n, o, v, "C" );
    }
    private static native void nativeSetCharField( Class c, String n, Object o, char v,
          String sig );

    /**
     * @see DeserializationKernel
     */
    public void setShortField( Class c, String n, Object o, short v )
    {
        nativeSetShortField( c, n, o, v, "S" );
    }
    private static native void nativeSetShortField( Class c, String n, Object o, short v,
          String sig );

    /**
     * @see DeserializationKernel
     */
    public void setIntField( Class c, String n, Object o, int v )
    {
        nativeSetIntField( c, n, o, v, "I" );
    }
    private static native void nativeSetIntField( Class c, String n, Object o, int v, String sig );

    /**
     * @see DeserializationKernel
     */
    public void setLongField( Class c, String n, Object o, long v )
    {
        nativeSetLongField( c, n, o, v, "J" );
    }
    private static native void nativeSetLongField( Class c, String n, Object o, long v,
          String sig );

    /**
     * @see DeserializationKernel
     */
    public void setFloatField( Class c, String n, Object o, float v )
    {
        nativeSetFloatField( c, n, o, v, "F" );
    }
    private static native void nativeSetFloatField( Class c, String n, Object o, float v,
          String sig );

    /**
     * @see DeserializationKernel
     */
    public void setDoubleField( Class c, String n, Object o, double v )
    {
        nativeSetDoubleField( c, n, o, v, "D" );
    }
    private static native void nativeSetDoubleField( Class c, String n, Object o, double v,
          String sig );
}

