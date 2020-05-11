/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * Various reflection utilities.
 */
public final class ReflectionUtils
{
    /**
     * Utility class. Do not instantiate.
     */
    private ReflectionUtils()
    {
    }

    /**
     * Invoke a method on the specified object.
     *
     * @param obj The object on which to invoke the method.
     * @param methName The name of the method.
     * @param types The parameter types.
     * @param params The parameter values.
     */
    public static Object invokeMethod( Object obj, String methName, Class[] types, Object[] params )
    {
        Object result = null;
        try
        {
            Class clz = obj.getClass();
            Method meth = clz.getMethod( methName, types );
            result = meth.invoke( obj, params );
        }
        catch ( Exception ex )
        {
            throw new CascadingRuntimeException( "Unable to invoke method '"
                  + methName + "' on object '" + obj.getClass() + "'", ex );
        }
        return result;
    }

    /**
     * Return an instance of the specified stub and set the delegate to
     * the one from the specified object.
     *
     * @param clz The stub class.
     * @param obj The object to take the delegate from.
     * @return The instantiated and initialized stub, null when an error occured.
     */
    public static Object getStubInstance( Class clz, org.omg.CORBA.Object obj )
    {
        Object result = null;
        try
        {
            try
            {
                // JDK 1.4
                // create an instance of _NamingContextStub (default constructor)
                result = clz.newInstance();
                java.lang.reflect.Method setDelegate = clz.getMethod(
                      "_set_delegate", new java.lang.Class[] {
                      org.omg.CORBA.portable.Delegate.class } );
                setDelegate.invoke( result, new java.lang.Object[] { ( (
                      org.omg.CORBA.portable.ObjectImpl ) obj )._get_delegate() } );
            }
            catch ( InstantiationException ex )
            {
                // JDK 1.3
                // create an instance of _NamingContextStub (constructor takes Delegate)
                Constructor ctor = clz.getConstructor(
                      new Class[] { org.omg.CORBA.portable.Delegate.class } );
                result = ctor.newInstance( new Object[] { ( (
                      org.omg.CORBA.portable.ObjectImpl ) obj )._get_delegate() } );
            }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Do the isAssignableFrom check but without loading the class.
     *
     * @param generalClzName The class which should be checked whether it is assignable from.
     * @param specialClz The special class that is checked to be assignable to the general one.
     * @return True when the classes are assignable.
     */
    public static boolean isAssignableFrom( String generalClzName, Class specialClz )
    {
        boolean result = false;
        if ( generalClzName != null && specialClz != null )
        {
            Class superClz = specialClz;
            do
            {
                if ( superClz != null )
                {
                    if ( superClz.getName().equals( generalClzName ) )
                    {
                        result = true;
                    }
                    else
                    {
                        // TODO: search the interfaces...
                    }
                    superClz = superClz.getSuperclass();
                }
            }
            while ( superClz != null && !result );
        }
        return result;
    }

    /**
     * Check whether the class has a readObject( java.io.ObjectInputStream )
     * method.
     *
     * @param clz The class to check.
     * @return True when a readObject() method was found, false otherwise.
     */
    public static boolean hasWriteObjectMethod( Class clz )
    {
        return ( getWriteObjectMethod( clz ) != null );
    }

    /**
     * Get the readObject( java.io.ObjectInputStream ) method from the class
     * and set accessibility to true.
     *
     * @param clz The class to get the writeObject() method from.
     * @return The writeObject method when found, null otherwise.
     */
    public static Method getWriteObjectMethod( Class clz )
    {
        Method meth = null;
        if ( clz != null )
        {
            try
            {
                meth = clz.getDeclaredMethod( "writeObject",
                       new Class [] { java.io.ObjectOutputStream.class } );
                if ( Modifier.isPrivate( meth.getModifiers() ) )
                {
                    meth.setAccessible( true );
                }
            }
            catch ( NoSuchMethodException ex )
            {
                // return null
            }
        }
        return meth;
    }

    public static boolean hasReadObjectMethod( Class clz )
    {
        return ( getReadObjectMethod( clz ) != null );
    }

    /**
     * Get the readObject( java.io.ObjectInputStream ) method from the class
     * and set accessibility to true.
     *
     * @param clz The class to get the readObject() method from.
     * @return The readObject method when found, null otherwise.
     */
    public static Method getReadObjectMethod( Class clz )
    {
        Method meth = null;
        if ( clz != null )
        {
            try
            {
                meth = clz.getDeclaredMethod( "readObject",
                      new Class[] { java.io.ObjectInputStream.class } );
                if ( Modifier.isPrivate( meth.getModifiers() ) )
                {
                    meth.setAccessible( true );
                }
            }
            catch ( NoSuchMethodException ex )
            {
                // return null
            }
        }
        return meth;
    }

    /**
     * Get the readResolve() method from the class
     * and set accessibility to true.
     *
     * @param clz The class to get the readResolve() method from.
     * @return The readResolve method when found, null otherwise.
     */
    public static Method getReadResolveMethod( Class clz )
    {
        Method meth = null;
        if ( clz != null )
        {
            try
            {
                meth = clz.getDeclaredMethod( "readResolve", new Class[ 0 ] );
                meth.setAccessible( true );
            }
            catch ( NoSuchMethodException ex )
            {
                Class thatDoes = clz.getSuperclass();
                boolean packageOK = ( clz.getPackage() != null );
                while ( thatDoes != null
                      && java.io.Serializable.class.isAssignableFrom( thatDoes ) )
                {
                    if ( packageOK && !clz.getPackage().equals( thatDoes.getPackage() ) )
                    {
                        packageOK = false;
                    }
                    try
                    {
                        Method m = thatDoes.getDeclaredMethod( "readResolve",
                                new Class[ 0 ] );
                        int mod = m.getModifiers();
                        if ( Modifier.isProtected( mod ) || ( packageOK
                              && !Modifier.isPrivate( mod ) ) )
                        {
                            m.setAccessible( true );
                            meth = m;
                        }
                        break;
                    }
                    catch ( NoSuchMethodException ex1 )
                    {
                        // the class does not have the readResolve() methods
                    }
                    thatDoes = thatDoes.getSuperclass();
                }
            }
        }
        return meth;
    }

    /**
     * Get the writeReplace() method from the class
     * and set accessibility to true.
     *
     * @param clz The class to get the writeReplace() method from.
     * @return The writeReplace() method when found, null otherwise.
     */
    public static Method getWriteReplaceMethod( Class clz )
    {
        Method meth = null;
        if ( clz != null )
        {
            try
            {
                meth = clz.getDeclaredMethod( "writeReplace",
                        new Class[ 0 ] );
                meth.setAccessible( true );
            }
            catch ( NoSuchMethodException ex )
            {
                Class thatDoes = clz.getSuperclass();
                boolean packageOK = ( clz.getPackage() != null );
                while ( thatDoes != null
                      && java.io.Serializable.class.isAssignableFrom( thatDoes ) )
                {
                    if ( packageOK && !clz.getPackage().equals( thatDoes.getPackage() ) )
                    {
                        packageOK = false;
                    }
                    try
                    {
                        Method m = thatDoes.getDeclaredMethod( "writeReplace",
                                new Class[ 0 ] );
                        int mod = m.getModifiers();
                        if ( Modifier.isProtected( mod )
                              || ( packageOK && !Modifier.isPrivate( mod ) ) )
                        {
                            m.setAccessible( true );
                            meth = m;
                        }
                        break;
                    }
                    catch ( NoSuchMethodException ex1 )
                    {
                        // the class does not have the writeReplace() methods
                    }
                    thatDoes = thatDoes.getSuperclass();
                }
            }
        }
        return meth;
    }

    /**
     * Call the readObject method.
     *
     * @param read_object The readObject() method.
     * @param target The object on which to call the readObject() method.
     * @param is The input stream to pass to the readObject() method.
     * @return True if readObject was called, false otherwise.
     * @throws IOException An exception that might occur during the readObject() call.
     */
    public static boolean readObject( Method read_object, Object target, ObjectInputStream is )
        throws IOException, ClassNotFoundException
    {
        if ( isAssignableFrom( "org.omg.CORBA.portable.IDLEntity", target.getClass() ) )
        {
            throw new IllegalStateException( "IDLEntity" );
        }
        if ( read_object == null )
        {
            return false;
        }
        try
        {
            read_object.invoke( target, new Object[] { is } );
            return true;
        }
        catch ( InvocationTargetException ex )
        {
            Throwable nex = ex.getTargetException();
            if ( nex instanceof IOException )
            {
                throw ( IOException ) nex;
            }
            if ( nex instanceof ClassNotFoundException )
            {
                throw ( IOException ) nex;
            }
            if ( nex instanceof RuntimeException )
            {
                throw ( RuntimeException ) nex;
            }
            if ( nex instanceof Error )
            {
                throw ( Error ) nex;
            }
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( Exception ex )
        {
            // should be impossible
            throw new Error( "Unexpected exception (" + ex + ")" );
        }
    }

    /**
     * Call the writeObject method.
     *
     * @param write_object The write_object method.
     * @param target The object on which to call the writeObject() method.
     * @param os The output stream to pass to the writeObject() method.
     * @throws IOException That might occur during the writeReplace() call.
     */
    public static void writeObject( Method write_object, Object target, ObjectOutputStream os )
        throws IOException
    {
        if ( isAssignableFrom( "org.omg.CORBA.portable.IDLEntity", target.getClass() ) )
        {
            throw new IllegalStateException( "IDLEntity" );
        }
        try
        {
            write_object.invoke( target, new Object[] { os } );
        }
        catch ( InvocationTargetException ex )
        {
            Throwable nex = ex.getTargetException();
            if ( nex instanceof IOException )
            {
                throw ( IOException ) nex;
            }
            if ( nex instanceof RuntimeException )
            {
                throw ( RuntimeException ) nex;
            }
            if ( nex instanceof Error )
            {
                throw ( Error ) nex;
            }
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( Exception ex )
        {
            // should be impossible
            throw new Error( "Unexpected exception (" + ex + ")" );
        }
    }

    /**
     * Call the readResolve method.
     *
     * @param read_resolve The readResolve() method.
     * @param target The object on which to call the readResolve() method.
     * @return The resolved object returned from readResolve().
     */
    public static Object readResolve( Method read_resolve, Object target )
        throws ObjectStreamException
    {
        if ( read_resolve == null )
        {
            return target;
        }
        try
        {
            return read_resolve.invoke( target, new Object[ 0 ] );
        }
        catch ( InvocationTargetException ex )
        {
            Throwable nex = ex.getTargetException();
            if ( nex instanceof ObjectStreamException )
            {
                throw ( ObjectStreamException ) nex;
            }
            if ( nex instanceof RuntimeException )
            {
                throw ( RuntimeException ) nex;
            }
            if ( nex instanceof Error )
            {
                throw ( Error ) nex;
            }
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( Exception ex )
        {
            // should be impossible
            throw new Error( "Unexpected exception (" + ex + ")" );
        }
    }

    /**
     * Call the writeReplace method.
     *
     * @param write_replace The writeReplace() method.
     * @param target The object on which to call the writeReplace() method.
     * @return The replaced object returned from writeReplace().
     */
    public static Object writeReplace( Method write_replace, Object target )
        throws ObjectStreamException
    {
        if ( write_replace == null )
        {
            return target;
        }
        try
        {
            return write_replace.invoke( target, new Object[ 0 ] );
        }
        catch ( InvocationTargetException ex )
        {
            Throwable nex = ex.getTargetException();
            if ( nex instanceof ObjectStreamException )
            {
                throw ( ObjectStreamException ) nex;
            }
            if ( nex instanceof RuntimeException )
            {
                throw ( RuntimeException ) nex;
            }
            if ( nex instanceof Error )
            {
                throw ( Error ) nex;
            }
            throw new Error( "Unexpected exception (" + ex.getTargetException() + ")" );
        }
        catch ( Exception ex )
        {
            // should be impossible
            throw new Error( "Unexpected exception (" + ex + ")" );
        }
    }

/*
    // test
    public static void main( String[] args )
    {
        try
        {
            Class clz = Class.forName( "org.omg.CORBA.INITIALIZE" );

            boolean isAssignable = org.omg.CORBA.SystemException.class.isAssignableFrom( clz );
            System.out.println( "org.omg.CORBA.SystemException is assignable from"
                  + " org.omg.CORBA.INITIALIZE: " + isAssignable );

            isAssignable = isAssignableFrom( "org.omg.CORBA.SystemException", clz );
            System.out.println( "org.omg.CORBA.SystemException is assignable from"
                  + " org.omg.CORBA.INITIALIZE: " + isAssignable );

        }
        catch ( Throwable th )
        {
            th.printStackTrace();
        }
    }
*/
}

