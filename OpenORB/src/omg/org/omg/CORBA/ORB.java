/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA;

import java.io.File;
import java.io.FileInputStream;

import java.lang.reflect.Method;

import java.util.Properties;

/**
 * This class is the implementation base for CORBA implementation.
 *
 * @author Jerome DANIEL.
 * @author Stephen McConnell, OSM
 */
public abstract class ORB
{
    //
    // This is the ORB implementation used when nothing else is specified.
    // Whoever provides this class customizes this string to
    // point at their ORB implementation.
    //
    private static final String ORB_CLASS_KEY = "org.omg.CORBA.ORBClass";
    private static final String ORB_SINGLETON_CLASS_KEY = "org.omg.CORBA.ORBSingletonClass";

    //
    // The last resort fallback ORB implementation classes in case
    // no ORB implementation class is dynamically configured through
    // properties or applet parameters. Change these values to
    // vendor-specific class names.
    //
    private static final String DEFAULT_ORB = "org.openorb.orb.core.ORB";
    private static final String DEFAULT_ORB_SINGLETON = "org.openorb.orb.core.ORBSingleton";

    /**
     * A reference to the singleton
     */
    private static org.omg.CORBA.ORB s_singleton;

    /**
     * Properties from the orb.properties file
     */
    private static Properties s_file_props;

    private static final Method INIT_CAUSE_METHOD;

    static
    {
        Method method;
        try
        {
            final Class[] parameterTypes = {Throwable.class};
            method = Throwable.class.getMethod( "initCause", parameterTypes );
        }
        catch ( final NoSuchMethodException e )
        {
            method = null;
        }
        INIT_CAUSE_METHOD = method;
    }

    private static Throwable initCause( final Throwable target, final Throwable cause )
    {
        if ( null == INIT_CAUSE_METHOD )
        {
            return target;
        }
        try
        {
            INIT_CAUSE_METHOD.invoke( target, new java.lang.Object[] {cause} );
        }
        catch ( final Exception e )
        {
            // ignore as is only best effort
        }

        return target;
    }

    private static RuntimeException initCause( final RuntimeException target,
            final Throwable cause )
    {
        initCause( ( Throwable ) target, cause );
        return target;
    }

    /**
     * This method retrieves the value of a particular system property.
     * It returns <code>null</code> if the property does not exist or
     * there is a security violation.
     */
    private static String getSystemProperty( final String name )
    {
        try
        {
            return System.getProperty( name );
        }
        catch ( SecurityException ex )
        {
            return null;
        }
    }

    /**
     * This method searchs a properties from the <code>orb.properties</code> file
     * found in the following search order:
     * <ol>
     *   <li>The users home directory, given by the <code>user.home</code>
     *       system property.</li>
     *   <li>The <i>java-home</i><code>/lib</code> directory, where
     *       <i>java-home</i> is the value of the System property
     *       <code>java.home</code>.
     * </ol>
     * It returns <code>null</code> if it does not find this property,
     * or if the <code>orb.properties</code> file does not exist.
     */
    private static String getPropertyFromFile( final String name )
    {
        if ( s_file_props == null )
        {
            s_file_props = new Properties();
            File propFile = null;

            // Check if orb.properties exists in user home
            try
            {
                String userHome = System.getProperty( "user.home" );

                File userPropFile = new File( userHome + File.separator + "orb.properties" );
                if ( userPropFile.exists() )
                {
                    propFile = userPropFile;
                }
            }
            catch ( SecurityException ex )
            {
                // Ignore and try next
            }

            if ( propFile == null )
            {
                // Check if orb.properties exists in java home
                try
                {
                    String javaHome = System.getProperty( "java.home" );

                    File javaPropFile = new File( javaHome + File.separator + "lib"
                          + File.separator + "orb.properties" );
                    if ( javaPropFile.exists() )
                    {
                        propFile = javaPropFile;
                    }
                }
                catch ( SecurityException ex )
                {
                    // Ignore and fall through
                }
            }

            // Check whether the orb.properties file exists or not
            if ( propFile == null )
            {
                return null;
            }

            // Load properties from the orb.properties file
            try
            {
                FileInputStream fis = new FileInputStream( propFile );
                try
                {
                    s_file_props.load( fis );
                }
                finally
                {
                    fis.close();
                }
            }
            catch ( Exception ex )
            {
                // Ignore and check the properties loaded so far
            }
        }
        return s_file_props.getProperty( name );
    }

    /**
     * Initialize the ORB singleton.
     * <p>
     * It uses the following order for searching the ORB singleton class name:
     * <ol>
     *   <li>Check in the System properties.</li>
     *   <li>Check in the <code>orb.properties</code> file, if it exists.</li>
     *   <li>Fall back on a hardcoded default behavior.</li>
     * </ol>
     * <p>
     * The search order for the <code>orb.properties</code> file is the same
     * as the <code>init</code> methods for creating an ORB.
     */
    public static ORB init()
    {
        if ( s_singleton == null )
        {
            String className = getSystemProperty( ORB_SINGLETON_CLASS_KEY );
            if ( className == null )
            {
                className = getPropertyFromFile( ORB_SINGLETON_CLASS_KEY );
            }
            if ( className == null )
            {
                className = DEFAULT_ORB_SINGLETON;
            }
            try
            {
                s_singleton = ( ORB )
                Thread.currentThread().getContextClassLoader().
                      loadClass( className ).newInstance();
            }
            catch ( final Exception ex )
            {
                throw initCause( new INITIALIZE(
                      "can't instantiate ORB implementation " + className ), ex );
            }
        }
        return s_singleton;
    }

    private static ORB init_orb( java.applet.Applet app, java.util.Properties props )
    {
        String className = null;
        if ( app != null )
        {
            className = app.getParameter( ORB_CLASS_KEY );
        }
        if ( className == null && props != null )
        {
            className = props.getProperty( ORB_CLASS_KEY );
        }
        if ( className == null )
        {
            className = getSystemProperty( ORB_CLASS_KEY );
        }
        if ( className == null )
        {
            className = getPropertyFromFile( ORB_CLASS_KEY );
        }
        if ( className == null )
        {
            className = DEFAULT_ORB;
        }
        try
        {
            return ( ORB ) Thread.currentThread().getContextClassLoader().loadClass(
                  className ).newInstance();
        }
        catch ( final Exception ex )
        {
            throw initCause( new INITIALIZE(
                  "can't instantiate ORB implementation " + className ), ex );
        }
    }

    /**
     * Create a new ORB for an application.
     * <p>
     * The IDL to Java Mapping 1.2 (formal/02-08-05), Section 1.21.9, specifies
     * the following order for searching the ORB class name:
     * <ol>
     *   <li>Check in Applet parameter, if any.</li>
     *   <li>Check in properties parameter, if any.</li>
     *   <li>Check in the System properties.</li>
     *   <li>Check in the <code>orb.properties</code> file, if it exists.</li>
     *   <li>Fall back on a hardcoded default behavior.</li>
     * </ol>
     * <p>
     * The search order for the <code>orb.properties</code> file is as follows:
     * <ol>
     *   <li>The users home directory, given by the <code>user.home</code>
     *       system property.</li>
     *   <li>The <i>java-home</i><code>/lib</code> directory, where
     *       <i>java-home</i> is the value of the System property
     *       <code>java.home</code>.
     * </ol>
     */
    public static ORB init( String[] args, java.util.Properties props )
    {
        org.omg.CORBA.ORB orb = init_orb( null, props );
        orb.set_parameters( args, props );
        return orb;
    }

    /**
     * Create a new ORB for an Applet.
     * <p>
     * The IDL to Java Mapping 1.2 (formal/02-08-05), Section 1.21.9, specifies
     * the following order for searching the ORB class name:
     * <ol>
     *   <li>Check in Applet parameter, if any.</li>
     *   <li>Check in properties parameter, if any.</li>
     *   <li>Check in the System properties.</li>
     *   <li>Check in the <code>orb.properties</code> file, if it exists.</li>
     *   <li>Fall back on a hardcoded default behavior.</li>
     * </ol>
     * <p>
     * The search order for the <code>orb.properties</code> file is as follows:
     * <ol>
     *   <li>The users home directory, given by the <code>user.home</code>
     *       system property.</li>
     *   <li>The <i>java-home</i><code>/lib</code> directory, where
     *       <i>java-home</i> is the value of the System property
     *       <code>java.home</code>.
     * </ol>
     */
    public static ORB init( java.applet.Applet app, java.util.Properties props )
    {
        org.omg.CORBA.ORB orb = init_orb( app, props );
        orb.set_parameters( app, props );
        return orb;
    }

    protected abstract void set_parameters( String[] args,
                                            java.util.Properties props );

    protected abstract void set_parameters( java.applet.Applet app,
                                            java.util.Properties props );

    public void connect( org.omg.CORBA.Object obj )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void disconnect( org.omg.CORBA.Object obj )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public abstract String[] list_initial_services();

    public abstract org.omg.CORBA.Object resolve_initial_references( String object_name )
        throws org.omg.CORBA.ORBPackage.InvalidName;

    public abstract String object_to_string( org.omg.CORBA.Object obj );

    public abstract org.omg.CORBA.Object string_to_object( String str );

    public abstract NVList create_list( int count );

    public NVList create_operation_list( org.omg.CORBA.Object oper )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public abstract NamedValue create_named_value( String s, Any any, int flags );

    public abstract ExceptionList create_exception_list();

    public abstract ContextList create_context_list();

    public abstract Context get_default_context();

    public abstract Environment create_environment();

    public abstract org.omg.CORBA.portable.OutputStream create_output_stream();

    public abstract void send_multiple_requests_oneway( Request[] req );

    public abstract void send_multiple_requests_deferred( Request[] req );

    public abstract boolean poll_next_response();

    public abstract Request get_next_response() throws WrongTransaction;

    public abstract TypeCode get_primitive_tc( TCKind tcKind );

    public abstract TypeCode create_struct_tc( String id, String name,
            StructMember[] members );

    public abstract TypeCode create_union_tc( String id, String name,
            TypeCode discriminator_type,
            UnionMember[] members );

    public abstract TypeCode create_enum_tc( String id, String name,
            String[] members );

    public abstract TypeCode create_alias_tc( String id, String name,
            TypeCode original_type );

    public abstract TypeCode create_exception_tc( String id, String name,
          StructMember[] members );

    public abstract TypeCode create_interface_tc( String id, String name );

    public abstract TypeCode create_string_tc( int bound );

    public abstract TypeCode create_wstring_tc( int bound );

    public abstract TypeCode create_sequence_tc( int bound,
          TypeCode element_type );

    public abstract TypeCode create_recursive_sequence_tc( int bound, int offset );

    public abstract TypeCode create_array_tc( int length, TypeCode element_type );

    public org.omg.CORBA.TypeCode create_native_tc( String id, String name )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode create_abstract_interface_tc( String id, String name )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode create_fixed_tc( short digits, short scale )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode create_value_tc( String id, String name, short type_modifier,
          TypeCode concrete_base, ValueMember[] members )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode create_recursive_tc( String id )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode create_value_box_tc( String id, String name,
          TypeCode boxed_type )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public abstract Any create_any();

    public org.omg.CORBA.Current get_current()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void run()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void shutdown( boolean wait_for_completion )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void destroy()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean work_pending()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void perform_work()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean get_service_information( short service_type,
          ServiceInformationHolder service_info )
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Policy create_policy( int type, org.omg.CORBA.Any val )
        throws org.omg.CORBA.PolicyError
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
