/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

/**
 * This interface provides the methods to perform proper
 * deserialization from IIOP streams.
 *
 * Problem description:
 *   The data format in IIOP streams
 *   (described in the CORBA/IIOP 2.6 spec. chapter 15.7) is completely
 *   different to the Java standard object serialization format as
 *   described by the Java object serialization specification.
 *   From the IIOP stream the data is read on a per field basis.
 *   No restrictions apply to the Java classes that are marshaled
 *   to the IIOP stream, besides the fact that the class must be Serializable.
 *   The problem is that even fields that have non-public access modifiers and
 *   non-static inner classes can be part of a class to be marshaled.
 *   During unmarshaling the members must be assigned to the corresponding
 *   class member fields.
 *
 * Solution:
 *   Solving the problem with non-public members in pure Java is impossible
 *   without restrictions to the classes that can be marshaled.
 *   Therefore a non-Java, i.e. native, solutions must be found.
 *   Inspecting the way how the major JDK ORBs (Sun, IBM) solve this
 *   problem revealed that they use similar native methods to assign
 *   members no matter what access modifiers they have.
 *   The purpose of the DeserializationKernel is thus to hide the
 *   differences behind a common interface (DeserializationKernel)
 *   and delegate the JDK specifics to the corresponding implementation
 *   classes (DeserializationKernelSun/IBM).
 *
 * The methods in this interface allow assignments to class member fields
 * no matter what access modifiers they have. This is achieved by
 * providing access to private native method hooks from the classes
 * com.sun.corba.se.internal.io.IIOPInputStream (Sun) and
 * com.ibm.rmi.io (IBM) (more to come). The actual implementation of
 * how access to the native method hooks is achieved can be found in
 * the actual implementations of this interface.
 *
 * For more JDKs (MacOS, HP-UX) this interface must be implemented
 * by providing access to the appropriate methods.
 *
 * Security:
 *   For the implementation classes to work several RuntimPermissions
 *   must be set, otherwise PermissionDenied exceptions will occur.
 *   Please add the following permissions to your application's policy
 *   file:
 *   <pre>
 *     permission java.lang.RuntimePermission "accessDeclaredMembers";
 *     permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
 *     permission java.io.SerializablePermission "enableSubclassImplementation";
 *   </pre>
 *
 * @author Michael Rumpf
 */
public interface DeserializationKernel
{
    /**
     * This class allocates an instance of a class. The
     * method allows allocations that are not possible when using
     * pure Java's newInstance() method.
     *
     * @param c The class to allocate memory for.
     * @param base The enclosing class when c is a non-static inner class.
     * @return An instance of class c.
     * @throws InstantiationException When the object can be instantiated.
     * @throws IllegalAccessException When the object can't be accessed.
     */
    Object allocateNewObject( Class c, Class base )
        throws InstantiationException, IllegalAccessException;

    /**
     * Set the member field of a class instance.
     *
     * @param c The type of the class.
     * @param n The name of the member field.
     * @param o The instance of the class c.
     * @param v The value to assign to the field n on instance o.
     */
    void setObjectField ( Class c, String n, Object o, Object v );

    /**
     * Set the member field of a class instance.
     *
     * @param c The type of the class.
     * @param n The name of the member field.
     * @param o The instance of the class c.
     * @param v The value to assign to the field n on instance o.
     */
    void setBooleanField( Class c, String n, Object o, boolean v );

    /**
     * Set the member field of a class instance.
     *
     * @param c The type of the class.
     * @param n The name of the member field.
     * @param o The instance of the class c.
     * @param v The value to assign to the field n on instance o.
     */
    void setByteField   ( Class c, String n, Object o, byte v );

    /**
     * Set the member field of a class instance.
     *
     * @param c The type of the class.
     * @param n The name of the member field.
     * @param o The instance of the class c.
     * @param v The value to assign to the field n on instance o.
     */
    void setCharField   ( Class c, String n, Object o, char v );

    /**
     * Set the member field of a class instance.
     *
     * @param c The type of the class.
     * @param n The name of the member field.
     * @param o The instance of the class c.
     * @param v The value to assign to the field n on instance o.
     */
    void setShortField  ( Class c, String n, Object o, short v );

    /**
     * Set the member field of a class instance.
     *
     * @param c The type of the class.
     * @param n The name of the member field.
     * @param o The instance of the class c.
     * @param v The value to assign to the field n on instance o.
     */
    void setIntField    ( Class c, String n, Object o, int v );

    /**
     * Set the member field of a class instance.
     *
     * @param c The type of the class.
     * @param n The name of the member field.
     * @param o The instance of the class c.
     * @param v The value to assign to the field n on instance o.
     */
    void setLongField   ( Class c, String n, Object o, long v );

    /**
     * Set the member field of a class instance.
     *
     * @param c The type of the class.
     * @param n The name of the member field.
     * @param o The instance of the class c.
     * @param v The value to assign to the field n on instance o.
     */
    void setFloatField  ( Class c, String n, Object o, float v );

    /**
     * Set the member field of a class instance.
     *
     * @param c The type of the class.
     * @param n The name of the member field.
     * @param o The instance of the class c.
     * @param v The value to assign to the field n on instance o.
     */
    void setDoubleField ( Class c, String n, Object o, double v );
}

