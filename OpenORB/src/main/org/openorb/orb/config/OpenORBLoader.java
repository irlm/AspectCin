/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.config;

import java.io.OutputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.avalon.framework.CascadingRuntimeException;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.log.Priority;

import org.openorb.orb.Initializer;

import org.openorb.orb.pi.ORBInitInfo;

import org.openorb.orb.util.Trace;

import org.openorb.util.ExceptionTool;
import org.openorb.util.urlhandler.HandlerLoader;

/**
 * This class gets information from the command line to load OpenORB
 * features. This class also provides several methods to access some
 * information about configuration. For example, it is possible to
 * call 'getBooleanProperty' to get a boolean value that corresponds
 * to an OpenORB property ( stored into a profile ).
 *
 * @author Jerome Daniel
 * @version $Revision: 1.27 $ $Date: 2004/11/14 21:38:35 $
 */
public class OpenORBLoader
    implements ORBLoader
{

    private static final String ORBSINGLETON_CLASSKEY = "org.omg.CORBA.ORBSingletonClass";
    private static final String DEFAULT_ORBSINGLETON  = "org.openorb.orb.core.ORBSingleton";

    private Properties m_properties = null;

    private org.openorb.orb.core.ORB m_orb;

    private Logger m_logger;

    /**
     * Ensure that the custom URL schema handlers are loaded into the system class loader.
     */
    static
    {
        try
        {
            HandlerLoader.loadHandlers( Thread.currentThread().getContextClassLoader() );
        }
        catch ( final ClassNotFoundException e )
        {
            throw ExceptionTool.initCause( new NoClassDefFoundError( e.getMessage() ), e );
        }
    }

    /**
     * Constructor
     */
    public OpenORBLoader()
    {
    }

    /**
     * ORB Initialization.
     */
    public void init( String [] args, java.util.Properties properties,
          org.openorb.orb.core.ORB orb )
    {
        // ensure this is the only time.
        if ( m_orb != null )
        {
            throw new org.omg.CORBA.INITIALIZE( "Multiple initialize for orb" );
        }
        m_orb = orb;

        // ensure the correct singleton orb is used.
        try
        {
            System.setProperty( ORBSINGLETON_CLASSKEY, DEFAULT_ORBSINGLETON );
        }
        catch ( SecurityException ex )
        {
            System.out.println( "Warning: Security settings do not allow this process to "
                  + "set system properties (" + ORBSINGLETON_CLASSKEY + ")." );
        }

        org.omg.CORBA.ORB singleton = org.omg.CORBA.ORB.init();

        if ( !( singleton instanceof org.openorb.orb.core.ORBSingleton ) )
        {
           throw new org.omg.CORBA.INITIALIZE(
             "Unable to complete init orb singleton is either not an OpenORB singleton\n"
             + "or it has been loaded by another classloader and thus causes the instanceof\n"
             + "check to fail. In the first case use:\n"
             + "System.setProperty(\"" + ORBSINGLETON_CLASSKEY
             + "\", \"" + DEFAULT_ORBSINGLETON + "\");\n"
             + "As the first statement in your application.\n"
             + "In the second case make sure that the OpenORB jars are placed on the system\n"
             + "or on a parent classloader instead of the apllication classloader. E.g. this\n"
             + "has most likely been caused by putting the OpenORB jars on the classpath of\n"
             + "your web application instead of the container's classpath.\n"
             + "But because there can only be one ORBSingleton instance per VM putting the\n"
             + "OpenORB jars on the web application classpath is not possible because the\n"
             + "web container creates a new classloader for each web application and thus\n"
             + "creates different incarnations of the same class for each classloader.\n"
             + "This leads, sooner or later, to a ClassCastException from which this check\n"
             + "is meant to protect you from!" );
        }

        //
        // Enable logging on the ORB
        //
        if ( properties != null )
        {
            m_logger = ( Logger ) properties.get( "LOGGER" );
        }

        final boolean useUserSuppliedLogger = ( m_logger != null );

        // set an output stream provided by the creating application
        if ( properties != null )
        {
            OutputStream os = ( OutputStream ) properties.get( "openorb.debug.outputstream" );
            if ( os != null )
            {
                org.openorb.orb.util.Trace.setLoggerOutputStream( os );
            }
        }

        //
        // if a logging channel has not been provided then get the bootstrap
        // logger from the Trace class, otherwise use the supplied logger and
        // make sure the trace class is updated
        //
        if ( m_logger == null )
        {
            m_logger = org.openorb.orb.util.Trace.getLogger( properties );
        }
        m_orb.enableLogging( m_logger.getChildLogger( "ldr#"
              + System.identityHashCode( this ) ) );

        // load the properties and the list of initializers
        Configurator conf = new Configurator( args, properties, getLogger() );

        // initialize the properties
        // NOTE: Do not try to access properties before this point !!!!!
        m_properties = conf.getProperties();

        // set the property which RMI-IIOP marshalling engine to use
        // VM specific, native code
        String deserializationEngine = "lazy:auto";
        try
        {
            deserializationEngine = m_properties.getStringProperty(
                  "iiop.deserializationEngine" );
        }
        catch ( PropertyNotFoundException ex )
        {
            // ignore, assume false
        }

        if ( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "DeserializationKernel: iiop.deserializationEngine="
                    + deserializationEngine );
        }

        org.openorb.orb.rmi.DeserializationKernelFactory.setDeserializationEngine(
              deserializationEngine );

        // print deprecation warnings
        if ( ( m_properties.getProperty( "openorb.debug" ) != null
              || m_properties.getProperty( "debug" ) != null
              || m_properties.getProperty( "verbose" ) != null )
              && m_logger.isWarnEnabled() )
        {
            m_logger.warn( "The properties \"openorb.debug\", \"debug\", and "
                  + "\"verbose\" have been deprecated and will be ignored."
                  + " Please use \"openorb.debug.trace\" and "
                  + "\"openorb.debug.level\" instead." );
        }

        if ( !useUserSuppliedLogger )
        {
            // reset the logger priority to the value from the Configurator
            try
            {
                String trclvl = m_properties.getStringProperty( "openorb.debug.trace" );
                m_logger = Trace.getNewLogger( Trace.getPriorityFromName( trclvl ) );
            }
            catch ( final PropertyNotFoundException ex )
            {
                // we need to set a default logger when this exception occurs
                m_logger = Trace.getNewLogger( Priority.ERROR );
            }
        }

        // set the debug level independently of any logger passed from outside
        String dbglvl = null;
        try
        {
            dbglvl = m_properties.getStringProperty( "openorb.debug.level" );
        }
        catch ( final PropertyNotFoundException ex )
        {
            m_logger.warn( "openorb.debug.level not specified, using default OFF." );
        }
        Trace.setDebugLevel( Trace.getDebugLevelFromName( dbglvl ) );

        // print the logging header
        if ( m_logger.isDebugEnabled() && Trace.isMedium() )
        {
            m_logger.debug( "\n"
                  + "-----------------------------------------------------------------\n"
                  + "OpenORB\n"
                  + "openorb.debug.level=" + dbglvl + " (OFF(0)/LOW(1)/MEDIUM(2)/HIGH(3))\n"
                  + "-----------------------------------------------------------------\n" );
            display_configuration( m_logger );
            m_logger.debug( "\n"
                  + "-----------------------------------------------------------------" );
        }

        if ( m_logger.isDebugEnabled() && org.openorb.orb.util.Trace.isLow() )
        {
            m_logger.debug( "ORB loading." );
        }
        // Store the loader
        m_orb.setFeature( "ORBLoader", this );

        // set the DefaultORB when the rmi profile has been included
        boolean defaultorb_singleton = m_properties.getBooleanProperty(
              "rmi.defaultorbSingleton", false );
        if ( defaultorb_singleton )
        {
            if ( m_logger.isDebugEnabled() && org.openorb.orb.util.Trace.isLow() )
            {
                m_logger.debug( "Setting RMI-IIOP DefaultORB." );
            }
            org.openorb.orb.rmi.DefaultORB.setORB( m_orb );
        }

        // create the orb init info.
        ORBInitInfo init_info;

        try
        {
            Object [] cargs = new Object[ 4 ];
            Class [] targs = new Class [ 4 ];
            cargs[ 0 ] = ( args == null ) ? new String[ 0 ] : args;
            targs[ 0 ] = String[].class;
            cargs[ 1 ] = orb;
            targs[ 1 ] = org.openorb.orb.core.ORB.class;
            load_initializers( cargs, conf.getInitializers() );
            targs[ 2 ] = org.omg.PortableInterceptor.ORBInitializer[].class;
            targs[ 3 ] = org.openorb.orb.pi.FeatureInitializer[].class;

            init_info = ( ORBInitInfo ) constructClass(
               "openorb.pi.ORBInitInfoClass", "org.openorb.orb.pi.OpenORBInitInfo", cargs, targs );
            if ( init_info instanceof LogEnabled )
            {
               ( ( LogEnabled ) init_info ).enableLogging( m_logger.getChildLogger( "pi" ) );
            }
        }
        catch ( final java.lang.reflect.InvocationTargetException ex )
        {
            final String msg = "Exception during construction of class openorb.pi.ORBInitInfoClass";
            getLogger().error( msg, ex );
            if ( ex.getTargetException() instanceof org.omg.CORBA.SystemException )
            {
                throw ( org.omg.CORBA.SystemException ) ex.getTargetException();
            }
            throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE( msg
                  + " (" + ex + ")" ), ex );
        }

        init_info.pre_init();

        try
        {
            ORBConnector orb_connector = ( ORBConnector ) constructClass(
                  "openorb.kernel.ORBConnectorClass",
                  "org.openorb.orb.config.OpenORBConnector", null );
            orb_connector.enableLogging( m_logger.getChildLogger( "connector" ) );
            orb_connector.load_kernel( m_orb, this );
        }
        catch ( final java.lang.reflect.InvocationTargetException ex )
        {
            final String msg =
                  "Exception during construction of class openorb.kernel.ORBConnectorClass";
            getLogger().error( msg, ex.getTargetException() );

            if ( ex.getTargetException() instanceof org.omg.CORBA.SystemException )
            {
                throw ( org.omg.CORBA.SystemException ) ex.getTargetException();
            }

            throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE( msg + " ("
                  + ex + ")" ), ex );
        }
        init_info.post_init();
    }

    /**
     * This operation is used to display an OpenORB configuration.
     */
    public void display_configuration()
    {
        m_properties.display();
    }

    /**
     * This operation is used to display an OpenORB configuration.
     */
    public void display_configuration( Logger logger )
    {
        m_properties.display( logger );
    }


    // ---------------------------------------------------------------------
    //
    // The following operations return properties stored into a profile
    //
    // ---------------------------------------------------------------------

    /**
     * Iterate over property values with the specified prefix. <p>
     *
     * @param name parent of properties. Properies of the form name + "." +
     * are returned, where  can be anything. May be null to iterate over all
     * properies.
     * @return unmodifiable iterator over the name's decendants. This iterator
     *    returns objects of type Property.
     */
    public Iterator properties( String name )
    {
        return m_properties.properties( name );
    }

    /**
     * Get the Property object with the given name.
     * @param name the property name.
     */
    public Property getProperty( String name )
    {
        return m_properties.getProperty( name );
    }

    /**
     * Get the string property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     */
    public String getStringProperty( String name, String defl )
    {
        return m_properties.getStringProperty( name, defl );
    }

    /**
     * Get the string property with the given name.
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     */
    public String getStringProperty( String name )
        throws PropertyNotFoundException
    {
        return m_properties.getStringProperty( name );
    }

    /**
     * Get the integer property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     * @throws org.omg.CORBA.INITIALIZE The property value is not parsable to an int.
     */
    public int getIntProperty( String name, int defl )
    {
        return m_properties.getIntProperty( name, defl );
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
        return m_properties.getIntProperty( name );
    }

    /**
     * Get the boolean property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     */
    public boolean getBooleanProperty( String name, boolean defl )
    {
        return m_properties.getBooleanProperty( name, defl );
    }

    /**
     * Get the boolean property with the given name.
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     */
    public boolean getBooleanProperty( String name )
        throws PropertyNotFoundException
    {
        return m_properties.getBooleanProperty( name );
    }

    /**
     * Get the URL property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     * @throws ClassCastException The property value is not parsable to a URL.
     */
    public URL getURLProperty( String name, URL defl )
    {
        return m_properties.getURLProperty( name, defl );
    }

    /**
     * Get the URL property with the given name.
     * @param name the property name.
     * @throws PropertyNotFoundException the property cannot be found.
     * @throws ClassCastException The property value is not parsable to a URL.
     */
    public URL getURLProperty( String name )
        throws PropertyNotFoundException
    {
        return m_properties.getURLProperty( name );
    }

    /**
     * Get the Class object property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     * @throws org.omg.CORBA.INITIALIZE the property value cannot be loaded as a class.
     */
    public Class getClassProperty( String name, Class defl )
    {
        return m_properties.getClassProperty( name, defl );
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
        return m_properties.getClassProperty( name, defl );
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
        return m_properties.getClassProperty( name );
    }

    /**
     * This operation is used to load a class with the given property name and
     * default class name.
     *
     * @param prop_key Property name, this string property holds the name of the
     *            class. May be null if no property is used.
     * @param defl Default class name. Used if the named property is not found.
     *            May be null to indicate no load should be performed if property
     *            is missing.
     * @param args arguments to constructor. If any constructor arguments are
     *            primitive types then the four argument version of this function
     *            must be used.
     * @return the newly constructed object, or null if the property value is
     *            set to the empty string.
     * @throws java.lang.reflect.InvocationTargetException an exception occoured
     *          in the constructor.
     * @throws org.omg.CORBA.INITIALIZE the property value or default class cannot
     *                    be loaded as a class.
     * @throws IllegalArgumentException some other problem occoured.
     */
    public Object constructClass( String prop_key, String defl, Object [] args )
        throws java.lang.reflect.InvocationTargetException
    {
        return constructClass( prop_key, defl, args, null );
    }

    /**
     * This operation is used to load a class with the given property name and
     * default class name.
     *
     * @param prop_key Property name, this string property holds the name of the
     *            class. May be null if no property is used.
     * @param defl Default class name. Used if the named property is not found.
     *            May be null to indicate no load should be performed if property
     *            is missing.
     * @param args arguments to constructor. If any constructor arguments are
     *            primitive types then the four argument version of this function
     *            must be used.
     * @param args_t types of onstructor arguments. If any of these are null they
     *            will be determined from getClass on the matching arg. Length
     *            must match length of args.
     * @return the newly constructed object, or null if the property value is
     *            set to the empty string.
     * @throws java.lang.reflect.InvocationTargetException an exception occoured
     *          in the constructor.
     * @throws org.omg.CORBA.INITIALIZE the property value or default class cannot
     *                    be loaded as a class.
     * @throws IllegalArgumentException some other problem occoured.
     */
    public Object constructClass( String prop_key, String defl, Object [] args,
                                  Class [] args_t )
        throws java.lang.reflect.InvocationTargetException
    {
        if ( args != null )
        {
            if ( args_t == null )
            {
                args_t = new Class[ args.length ];
            }
            else if ( args.length != args_t.length )
            {
                throw new IllegalArgumentException( "Length of args and args_t do not match" );
            }
            for ( int i = 0; i < args.length; ++i )
            {
                if ( args_t[ i ] == null )
                {
                    args_t[ i ] = ( args[ i ] == null ) ? Void.TYPE : args[ i ].getClass();
                }
            }
        }

        try
        {
            return classConstructor( prop_key, defl, args_t ).newInstance( args );
        }
        catch ( final InstantiationException ex )
        {
            final String msg = "Illegal argument when constructing a class";
            getLogger().error( msg, ex );
            throw ExceptionTool.initCause( new IllegalArgumentException( msg
                  + " (" + ex + ")" ), ex );
        }
        catch ( final IllegalAccessException ex )
        {
            final String msg = "Illegal access when constructing a class";
            getLogger().error( msg, ex );
            throw ExceptionTool.initCause( new IllegalArgumentException( msg
                  + " (" + ex + ")" ), ex );
        }
    }

    public java.lang.reflect.Constructor classConstructor( String prop_key,
          String defl, Class [] args_t )
    {
        Class clz = getClassProperty( prop_key, defl );
        try
        {
            return clz.getConstructor( args_t );
        }
        catch ( final NoSuchMethodException ex )
        {
            final String msg = "No constructor found in " + clz.getName();
            getLogger().error( msg, ex );
            throw ExceptionTool.initCause( new IllegalArgumentException( msg
                  + " (" + ex + ")" ), ex );
        }
    }

    private void load_initializers( Object [] args, String [] cls_names )
    {
        ArrayList orbInits = new ArrayList( cls_names.length );
        ArrayList openOrbInits = new ArrayList( cls_names.length );

        if ( getLogger().isDebugEnabled() && Trace.isHigh() )
        {
            getLogger().debug( "Creating initializers now..." );
        }
        for ( int i = 0; i < cls_names.length; ++i )
        {
            Object instance;
            try
            {
                if ( getLogger().isDebugEnabled() && Trace.isHigh() )
                {
                    getLogger().debug( "Creating initializer " + cls_names[ i ] + "." );
                }
                instance = Thread.currentThread().getContextClassLoader().loadClass(
                      cls_names[ i ] ).newInstance();
            }
            catch ( final Exception ex )
            {
                final String error = "Unable to load initializer class: " +  cls_names[ i ];
                throw new CascadingRuntimeException( error, ex );
            }

            String logger_name = null;
            if ( instance instanceof Initializer )
            {
                Initializer init = ( Initializer ) instance;
                logger_name = init.getName();
            }
            else
            {
                int dot = cls_names[ i ].lastIndexOf( '.' );
                if ( dot != -1 )
                {
                    logger_name = cls_names[ i ].substring( dot + 1 );
                }
            }

            // log enable the class
            if ( instance instanceof LogEnabled )
            {
                ( ( LogEnabled ) instance ).enableLogging(
                      m_logger.getChildLogger( logger_name ) );
            }

            boolean ok = false;
            if ( instance instanceof org.omg.PortableInterceptor.ORBInitializer )
            {
                orbInits.add( instance );
                ok = true;
            }

            if ( instance instanceof org.openorb.orb.pi.FeatureInitializer )
            {
                openOrbInits.add( instance );
                ok = true;
            }

            if ( !ok )
            {
                final String error = "Unknown initalizer type: '" + cls_names[i]  + "'.";
                throw new IllegalStateException( error );
            }
        }

        args[ 2 ] = new org.omg.PortableInterceptor.ORBInitializer[ orbInits.size() ];
        orbInits.toArray( ( Object[] ) args[ 2 ] );
        args[ 3 ] = new org.openorb.orb.pi.FeatureInitializer[ openOrbInits.size() ];
        openOrbInits.toArray( ( Object[] ) args[ 3 ] );
    }

    protected Logger getLogger()
    {
        return m_logger;
    }
}

