/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

/**
 * This class can be used to create a deserialization kernel
 * based on the current JDK.
 * Currently this factory has been tested successfully on
 * the following operating system and VM combinations:
 *
 * Vendor: IBM Corporation
 * <ul>
 *     <li>Windows 2000, JDK 1.3.1
 *     <li>Windows 2000, JDK 1.3.0
 *     <li>AIX 5.2, JDK 1.4.2
 *     <li>AIX 5.2, JDK 1.4.1
 *     <li>AIX 5.2, JDK 1.3.1
 *     <li>AIX 5.1, JDK 1.3.1
 *     <li>AIX 4.3, JDK 1.3.1
 *     <li>AIX 4.3, JDK 1.3.0
 *     <li>Linux, JDK 1.3.1
 *     <li>Linux, JDK 1.3.0
 * </ul>
 *
 * Vendor: Sun Microsystems (http://java.sun.com)
 * <ul>
 *     <li>All, JDK 1.5.0
 *     <li>Windows 2000, JDK 1.3.0
 *     <li>Windows 2000, JDK 1.3.1
 *     <li>Windows 2000, JDK 1.4.0
 *     <li>Solaris 7, JDK 1.3.0
 *     <li>Solaris 8, JDK 1.3.0
 *     <li>Solaris 8, JDK 1.3.1
 *     <li>Solaris 8, JDK 1.4.0
 *     <li>Linux, JDK 1.3.0
 *     <li>Linux, JDK 1.3.1
 *     <li>Linux, JDK 1.4.0
 * </ul>
 *
 * Vendor: Hewlett-Packard Company
 * <ul>
 *     <li>HP UX 11, JDK 1.3.1 (relabled from Sun)
 *     <li>HP UX 11, JDK 1.4.0 (relabled from Sun)
 * </ul>
 *
 * Vendor: Apple Computer, Inc.
 * <ul>
 *     <li>Mac OS X 10.1, JDK 1.3.1 (relabled from Sun)
 * </ul>
 *
 * Vendor: BEA WebLogic JRockit (http://www.jrockit.com)
 * <ul>
 *     <li>Windows 2000, JDK 1.3.1 (relabled from Sun)
 *     <li>Linux, JDK 1.3.1 (relabled from Sun)
 * </ul>
 *
 * @author Michael Rumpf
 * @author lkuehne
 */
public final class DeserializationKernelFactory
{
    /** A static instance of the deserialization native hook. */
    private static DeserializationKernel s_kernel = null;

    /** description of the deserialization mechanism to be used by the marshaling engine. */
    private static String s_deserializationEngine = "lazy:auto";
    private static final String LAZY_PREFIX = "lazy:";

    private DeserializationKernelFactory()
    {
    }

    /**
     * Set the deserialization engine to use.
     *
     * @param deserializationEngine either auto or native, maybe prefixed with 'lazy:'.
     */
    public static void setDeserializationEngine( String deserializationEngine )
    {
        s_deserializationEngine = deserializationEngine;
    }

    /**
     * Try to find specific classes in the current VM and return an
     * appropriate wrapper.
     * Throw an error if the VM has an unknown vendor.
     * When called twice this method will return the same static instance.
     *
     * @return The DeserializationKernel wrapper for the current VM.
     */
    public static synchronized DeserializationKernel retrieveDeserializationKernel()
    {
        if ( s_kernel != null )
        {
            return s_kernel;
        }

        if ( s_deserializationEngine.startsWith( LAZY_PREFIX )
                && s_deserializationEngine.length() > LAZY_PREFIX.length() )
        {
            final String delegateShortName
                    = s_deserializationEngine.substring( LAZY_PREFIX.length() );

            LazyInitDeserializationKernel.setDelegateName( delegateShortName );
            s_kernel = new LazyInitDeserializationKernel();
        }
        else
        {
            s_kernel = createDeserializationKernel( s_deserializationEngine );
        }

        return s_kernel;
    }


    /**
     * @param deserializationEngine engine descriptor, "lazy:" prefix not allowed here.
     * @return the requested DeserializationKernel
     */
    static DeserializationKernel createDeserializationKernel( String deserializationEngine )
    {
        if ( "auto".equals( deserializationEngine ) )
        {
            return createAutoDeserializationKernel();
        }
        else if ( "native".equals( deserializationEngine ) )
        {
            return new DeserializationKernelNative();
        }
        else if ( "none".equals( deserializationEngine ) )
        {
            return new NullDeserializationKernel();
        }
        throw new IllegalArgumentException( "unsupported iiop.deserializationEngine '"
                + deserializationEngine + "', must be 'auto', 'native' or 'none'" );
    }

    private static DeserializationKernel createAutoDeserializationKernel()
    {
        if ( DeserializationKernelSun15.isSupportedPlatform() )
        {
            return new DeserializationKernelSun15();
        }

        try
        {
            Thread.currentThread().getContextClassLoader().loadClass(
                    "com.sun.corba.se.internal.io.IIOPInputStream" );
            return new DeserializationKernelSun();
        }
        catch ( ClassNotFoundException ex )
        {
            // no Sun JDK
        }

        try
        {
            Thread.currentThread().getContextClassLoader().loadClass(
                    "com.ibm.rmi.io.JNIReflectField" );
            return new DeserializationKernelIBM();
        }
        catch ( ClassNotFoundException ex )
        {
            // no IBM JDK 1.3.x
        }

        // The following check is not working under AIX JDK version 1.4.x
        // so don'teven try it instead give the following checks a chance
        // to succeed
        final String os_name = System.getProperty( "os.name" );
        final String vm_version = System.getProperty( "java.vm.version" );
        if ( !os_name.equalsIgnoreCase( "AIX" )
                || !vm_version.startsWith( "1.4." ) )
        {
            try
            {
                Thread.currentThread().getContextClassLoader().loadClass(
                        "com.ibm.rmi.io.PureReflectField" );
                return new DeserializationKernelIBM14();
            }
            catch ( ClassNotFoundException ex )
            {
                // no valid IBM JDK 1.4.x
            }
        }
        else
        {
            // This is the broken version
            return new DeserializationKernelNative();
        }

        //
        // add more VMs here !!!!!!!!!!!
        //
        /*
        try
        {
            Class clz = Class.forName( "<UNIQUE CLASS FOR VM OF VENDOR XYZ>" );
            // This class needs to be implemented for each JDK
            return new DeserializationKernelXYZ();
        }
        catch ( ClassNotFoundException ex )
        {
            // no XYZ JDK
        }
        */

        String vmName = System.getProperty( "java.vm.name" ); // Java HotSpot(TM) Client VM
        String vmVersion = System.getProperty( "java.vm.version" ); // 1.4.2-b28
        String vmVendor = System.getProperty( "java.vm.vendor" ); // Sun Microsystems Inc.

        throw new Error( "Unknown VM and iiop.deserializationEngine set to 'auto'. "
                + "RMIoverIIOP will not work with this VM ("
                + vmName + ' ' + vmVersion + ", " + vmVendor
                + "). Please try the native engine or "
                + "contact support at openorb-users@lists.sf.net." );
    }
}

