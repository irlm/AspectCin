/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.apache.orb.config;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.net.URL;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.excalibur.configuration.CascadingConfiguration;

import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.PortableInterceptor.ORBInitializer;

import org.openorb.orb.pi.FeatureInitializer;
import org.openorb.orb.pi.ORBInitInfo;
import org.openorb.orb.config.ORBLoader;
import org.openorb.orb.config.Properties;
import org.openorb.orb.config.Property;
import org.openorb.orb.config.PropertyNotFoundException;
import org.openorb.orb.config.Configurator;
import org.openorb.orb.util.Trace;
import org.openorb.util.ExceptionTool;

import org.xml.sax.SAXException;

/**
 * The <code>DefaultLoader</code> provides support for the configuration
 * of a new ORB instance based on default and supplied properties, and
 * lifecycle processing of pluggable <code>Initializer</code>s encountered
 * during the configuration process.  The implementation also handles the
 * decommissioning of initializers as part of loader disposal.
 *
 * <h4>Property Management</h4>
 *
 * <p>The property set profile for an ORB instance is created based on
 * command line arguments, supplied properties, and an optional embedded
 * configuration.</p>
 *
 * <p><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tr bgcolor="#ccccff">
 * <td colspan="2"><b>Property resolution.</b></td>
 *
 * <tr><td width="20%"><b>Source</b></td><td><b>Description</b></td></tr>
 * <tr><td width="20%" valign="top">args</td>
 * <td>
 * Property declarations supplied under the init method take precedence.
 * The args array is passed to the <code>Configuration</code> for resolution.
 * </td></tr>
 *
 * <tr><td width="20%" valign="top">properties</td>
 * <td>
 * The <code>properties</code> argument to the init method is the second source
 * of property declarations.  The <code>DefaultLoader</code> uses the properties
 * argument to establish a new <code<Properties</code> instance that is populated
 * with additional properties based on the <code>DefaultLoader</code> default
 * properties resource, and properties declared under an embedded configuration
 * value.
 * </td></tr>
 *
 * <tr><td width="20%" valign="top">configuration</td>
 * <td>
 * A <code>Configuration</code> value may be embeded in the supplied
 * <code>Properties</code> value under the <code>"CONFIGURATION"</code> key.
 * Properties declared in the configuration instance that are recognized by
 * the loader are detailed here.
 * <pre>
 *     &lt;property name="propertyName" value="propertyValue"/&gt;
 *     &lt;property name="propertyName" file="fileName"/&gt;
 *     &lt;initializer class="org.apache.pss.Initializer" name="pss"/&gt;
 * </pre>
 * </td></tr>
 *
 * <tr><td width="20%" valign="top">defaults</td>
 * <td>
 * A set of static default properties are used to suppliment the user defined
 * properties.  Default properties are contained in the
 * <code>org/apache/orb/config/orb.properties</code> and
 * <code>org/apache/orb/config/DefaultLoader.xml</code> configuration
 * resources bundeled in the distribution jar file.
 * </td></tr>
 * </table>
 *
 * <h4>Embedded Resources</h4>
 *
 * <p>Resources required to manage pluggable component lifecycle handling are
 * provided to the loader using special keyed values.  Keys and the associated
 * values are described in the following table.</p>
 *
 * <p><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tr bgcolor="#ccccff">
 * <td colspan="2"><b>Embedded Avalon Resources.</b></td>
 * <tr><td width="20%"><b>Key</b></td><td><b>Description</b></td></tr>
 *
 * <tr><td width="20%" valign="top"><code>LOGGER</code></td>
 * <td>
 * The logging channel to be assigned to the ORB.  The logger is used as
 * the relative root when creatiing and assigning logging channels to
 * pluggable initializers.
 * </td></tr>
 *
 * <tr><td width="20%" valign="top"><code>CONFIGURATION</code></td>
 * <td>
 * A configuration instance that may be used to supply supplimentary property
 * declarations and initializers configuration blocks.  Initializers that
 * implement the Avalon <code>Cascading</code> interface will be supplied with
 * its configuration during initializer loading.
 * </td></tr>
 *
 * <tr><td width="20%" valign="top"><code>CONTEXT</code></td>
 * <td>
 * The <code>Context</code> is an application context that will be supplied to
 * any initializers that implement the <code>Contextualizable</code> interface.
 * </td></tr>
 *
 * </table>
 *
 * <h4>Logging Catagory Management</h4>
 * <p>The logger supplied to the loader under the embedded properties <code>LOGGER</code>
 * key is assigned as the root logging catagory for the ORB.  Catagories for
 * initializers are created as child catagories. If the supplied logger has the catagory
 * name of <code>demo.orb</code>, an initializer catagory for PSS would be
 * <code>demo.orb.pss</code>.  Initializer sub-catagories are resolved from the supplied
 * configuration.  The <code>DefaultLoader</code> locates an initializer declaration based
 * on an initilizer element with a <code>class</code> attribute value matching that
 * initializer implementation class name.  The <code>name</code> attribute value is used
 * as the logging channel sub-catagory name.
 * <pre>
 *     &lt;initializer class="org.apache.pss.Initializer" name="pss"/&gt;
 * </pre>
 *
 * <h4>Example Usage</h4>
 * <p>The following code fragment demonstrates the creation of a new ORB instance
 * using embedded resources and the <code>DefaultLoader</code> class.
 * </p>
 * <pre>
 *       Properties properties = new Properties();
 *
 *       <font color="blue"><i>// declare the ORB and Singleton implementations</i></font>
 *
 *       properties.setProperty( "org.omg.CORBA.ORBClass",
 *         "org.openorb.orb.core.ORB" );
 *       properties.setProperty("org.omg.CORBA.ORBSingletonClass",
 *         "org.openorb.orb.core.ORBSingleton" );
 *
 *       <font color="blue">
 *          <i>// declare the loader that will be instatiated by the ORB</i></font>
 *
 *       properties.setProperty("openorb.ORBLoader",
 *         "org.apache.orb.config.DefaultLoader");
 *
 *       <font color="blue">
 *         <i>// add the embedded configuration, logger and context resources</i></font>
 *
 *       properties.put( "CONFIGURATION", m_config );
 *       properties.put( "LOGGER", getLogger().getChildLogger("orb") );
 *       properties.put( "CONTEXT", m_context );
 *
 *       <font color="blue"><i>// create the ORB</i></font>
 *
 *       orb = ORB.init( m_args, properties );
 * </pre>
 * @author Stephen McConnell
 */
public class DefaultLoader implements ORBLoader, Disposable
{

    //=========================================================================
    // static
    //=========================================================================

    private static final String ORB_SINGLETON_CLASS_KEY =
        "org.omg.CORBA.ORBSingletonClass";
    private static final String ORB_SINGLETON_KEY =
        "org.openorb.orb.core.ORBSingleton";
    private static java.util.Properties s_defaultProperties;
    private static Configuration s_defaults;

    //=========================================================================
    // state
    //=========================================================================

    private Logger m_logger;
    private Configuration m_config;
    private Context m_context;
    private Hashtable m_table = new Hashtable();

    private Properties m_properties;
    private File m_base;
    private org.openorb.orb.core.ORB m_orb;
    private ORBInitInfo m_init_info;

    //=========================================================================
    // implementation
    //=========================================================================

    /**
     * This operation is used to load the OpenORB kernel.
     * <p>Depending on the property <b>openorb.server.enable</b>
     * ( default true ) and <b>openorb.client.enable</b>
     * ( default true ) this method instantiates the
     * ServerCPCManager and the ClientCPCMamager.
     * This instantiation is pluggable as it is driven by
     * properties:
     * <ul>
     *   <li><b>openorb.server.ServerManagerClass</b>
     *      ( default org.openorb.orb.net.ServerManagerImpl )</li>
     *   <li><b>openorb.client.ClientManagerClass</b>
     *      ( default org.openorb.orb.net.ClientManagerImpl )</li>
     * </ul><br />
     * The interface of the ServerCPCManager is defined by the
     * org.openorb.orb.net.ServerManager interface and by the
     * org.openorb.orb.net.ClientManager interface for the
     * ClientCPCManager respectively.</p>
     *
     * <p>The property <b>openorb.dynany.enable</b> (default true)
     * allows to avoid loading the DynAnyFactory if it is
     * not used by the application. This can save you some amount
     * memory.</p>
     *
     * <p>The property <b>openorb.client.enable</b> also decides whether
     * to load the initiali references ORBPolicyManager and
     * PolicyCurrent into the ORB.</b>
     */
    public void load_kernel()
    {
        boolean enable_server = getBooleanProperty(
                "openorb.server.enable", true );
        if ( enable_server )
        {
            Object[] args = new Object[ 1 ];
            Class[] args_t = new Class[ 1 ];
            args[ 0 ] = m_orb;
            args_t[ 0 ] = org.omg.CORBA.ORB.class;

            try
            {
                Object serverManager = constructClass(
                    "openorb.server.ServerManagerClass",
                    "org.openorb.orb.net.ServerManagerImpl", args, args_t );
                m_orb.setFeature( "ServerCPCManager", serverManager );
            }
            catch ( Exception ex )
            {
                Logger logger = m_orb.getLogger();

                logger.error( "Unable to initialize CPC server manager", ex );

                throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                      "Unable to initialize CPC server manager (" + ex
                      + ")" ), ex );
            }
        }

        boolean enable_client = getBooleanProperty(
                "openorb.client.enable", true );
        if ( enable_client )
        {
            Object[] args = new Object[ 1 ];
            Class[] args_t = new Class[ 1 ];
            args[ 0 ] = m_orb;
            args_t[ 0 ] = org.omg.CORBA.ORB.class;

            try
            {
                Object clientManager = constructClass(
                    "openorb.client.ClientManagerClass",
                    "org.openorb.orb.net.ClientManagerImpl", args, args_t );
                m_orb.setFeature( "ClientCPCManager", clientManager );
            }
            catch ( Exception ex )
            {
                Logger logger = m_orb.getLogger();

                logger.error( "Unable to initialize CPC client manager", ex );

                throw ExceptionTool.initCause( new org.omg.CORBA.INITIALIZE(
                      "Unable to initialize CPC client manager (" + ex
                      + ")" ), ex );
            }
        }

        // Load per ORB specificities
        if ( getBooleanProperty( "openorb.dynany.enable", true ) )
        {
            org.omg.DynamicAny.DynAnyFactory dfac =
              new org.openorb.orb.core.dynany.DynAnyFactoryImpl( m_orb );

            m_orb.addInitialReference( "DynAnyFactory", dfac );
        }

        // set reference for the TypeCodeFactory this is never optional.
        m_orb.addInitialReference( "TypeCodeFactory",
            org.openorb.orb.core.typecode.TypeCodeFactoryImpl.getInstance() );

        // set references for the policy manaagement stuff
        org.openorb.orb.policy.ORBPolicyManagerImpl policymanagerimpl =
            new org.openorb.orb.policy.ORBPolicyManagerImpl();

        m_orb.setFeature( "PolicyReconciler", policymanagerimpl );

        m_orb.setFeature( "PolicySetManager", policymanagerimpl );

        m_orb.setFeature( "PolicyFactory", policymanagerimpl );

        m_orb.setFeature( "PolicyFactoryManager", policymanagerimpl );

        if ( enable_client )
        {
            m_orb.addInitialReference( "ORBPolicyManager", policymanagerimpl );
            org.openorb.orb.policy.PolicyCurrentImpl policycurrent
              = new org.openorb.orb.policy.PolicyCurrentImpl( policymanagerimpl );
            m_orb.addInitialReference( "PolicyCurrent", policycurrent );
        }

        // this will register any policy factories it creates.
        new org.openorb.orb.policy.OpenORBPolicyFactoryImpl( m_orb,
            policymanagerimpl, enable_server, enable_client );
    }

    /**
     * ORB Initialization.
     * @param args command line arguments
     * @param props properties argument
     * @param orb the ORB instance
     */
    public void init( String[] args, java.util.Properties props,
                      org.openorb.orb.core.ORB orb )
    {
        // make sure this operation is not being invoked on a
        // previously initalized ORB
        if ( m_orb != null )
        {
            throw new INITIALIZE( "Illegal attempt to reinitialize the ORB." );
        }
        m_orb = orb;

        // ensure the correct singleton orb is used.
        try
        {
            System.setProperty( ORB_SINGLETON_CLASS_KEY, ORB_SINGLETON_KEY );
        }
        catch ( SecurityException ex )
        {
            System.err.println( "Unable to set System properties: " + ex );
        }

        org.omg.CORBA.ORB singleton = org.omg.CORBA.ORB.init();
        if ( !( singleton instanceof org.openorb.orb.core.ORBSingleton ) )
        {
            throw new org.omg.CORBA.INITIALIZE(
                "Unable to complete init orb singleton is not openorb"
                + "singleton .\nPlease use: System.setProperty(\""
                + ORB_SINGLETON_CLASS_KEY + "\", \""
                + ORB_SINGLETON_KEY + "\");\n"
                + "As the first statement in your application." );
        }

        // Validate the configuration and build an initializer element
        // lookup table.
        try
        {
             if ( s_defaults == null )
             {
                 s_defaults = loadDefaultConfiguration();
             }
        }
        catch ( Throwable e )
        {
            throw new CascadingRuntimeException(
              "Internal error while attempting to resolve default configuration.", e );
        }

        Configuration config = null;
        if ( props != null )
        {
            config = ( Configuration ) props.get( "CONFIGURATION" );
        }
        if ( config == null )
        {
            config = new DefaultConfiguration( "default", null );
        }
        m_config = new CascadingConfiguration( config, s_defaults );
        Configuration[] inits = m_config.getChildren( "initializer" );
        try
        {
            for ( int i = 0; i < inits.length; i++ )
            {
                m_table.put( inits[i].getAttribute( "class" ), inits[ i ] );
            }
        }
        catch ( Throwable e )
        {
            throw new CascadingRuntimeException(
              "Internal error while attempt to read initalizer configurations.", e );
        }

        // Validate the context object.
        if ( props != null )
        {
            m_context = ( Context ) props.get( "CONTEXT" );
        }
        try
        {
            m_context.get( "APP_DIR" );
        }
        catch ( Throwable e )
        {
            // context is null or context supplied without a base directory
            DefaultContext context = new DefaultContext( m_context );
            context.put( "APP_DIR",
                new File( System.getProperty( "user.dir" ) ) );
            context.makeReadOnly();
            m_context = context;
        }

        // get the base directory to use for resolution of property
        // declarations that uses file attributes
        try
        {
            m_base = ( File ) m_context.get( "APP_DIR" );
        }
        catch ( Throwable e )
        {
            final String error = "Unresolved base directory.";
            throw new CascadingRuntimeException( error, e );
        }

        // Create the properties argument using the default ORB properties as
        // defaults.
        if ( s_defaultProperties == null )
        {
            s_defaultProperties = getDefaultProperties();
        }
        java.util.Properties properties = new java.util.Properties(
            s_defaultProperties );

        // Add any "initializer" element that are included in the
        // configuration as properties.
        Enumeration names = m_table.keys();
        while ( names.hasMoreElements() )
        {
            String name = ( String ) names.nextElement();
            String pname = "org.omg.PortableInterceptor.ORBInitializerClass." + name;
            properties.setProperty( pname, "" );
        }

        // Add any "property" elements that are included in the configuration
        // as properties.
        try
        {
            mergeProperties( m_base, m_config, properties );
        }
        catch ( Throwable e )
        {
            final String error =
                "Unexpected exception while attempting to merge properties.";
            throw new CascadingRuntimeException( error, e );
        }

        // Overwrite the derived property values with the values from the
        // supplied properties argument
        if ( props != null )
        {
            Enumeration enumeration = props.propertyNames();
            while ( enumeration.hasMoreElements() )
            {
                String name = ( String ) enumeration.nextElement();
                properties.setProperty( name, props.getProperty( name, "" ) );
            }
        }

        // Validate the logger.
        try
        {
            if ( props != null )
            {
                m_logger = ( Logger ) props.get( "LOGGER" );
            }
        }
        catch ( Throwable e )
        {
           throw new CascadingRuntimeException(
               "Unexpected exception while attempting to resolve logger.", e );
        }

        // either props has been null or an exception accessing props has
        // occured
        if ( m_logger == null )
        {
            m_logger = Trace.getLogger( props );
        }

        // print deprecation warnings
        if ( m_logger.isWarnEnabled() )
        {
            if ( properties.get( "openorb.debug" ) != null
                  || properties.get( "debug" ) != null
                  || properties.get( "verbose" ) != null
                  || System.getProperty( "openorb.debug" ) != null
                  || System.getProperty( "debug" ) != null
                  || System.getProperty( "verbose" ) != null )
            {
                m_logger.warn( "The properties \"openorb.debug\", \"debug\", and "
                      + "\"verbose\" have been deprecated and will be ignored."
                      + " Please use \"openorb.debug.trace\" and "
                      + "\"openorb.debug.level\" instead." );
            }
        }

        // set the debug level independently of any logger passed from outside
        String dbglvl = ( String ) properties.get( "openorb.debug.level" );
        if ( dbglvl == null )
        {
            dbglvl = System.getProperty( "openorb.debug.level" );
        }
        Trace.setDebugLevel( Trace.getDebugLevelFromName( dbglvl ) );

        //
        // Store the loader, context and configuration
        //
        m_orb.enableLogging( m_logger );
        m_orb.setFeature( "ORBLoader", this );
        m_orb.setFeature( "CONTEXT", m_context );
        m_orb.setFeature( "CONFIGURATION", m_config );

        //
        // create the orb init info.
        //
        try
        {
            Object[] cargs = new Object[ 4 ];
            Class[] targs = new Class [ 4 ];
            cargs[ 0 ] = ( args == null ) ? new String[ 0 ] : args;
            targs[ 0 ] = String[].class;
            cargs[ 1 ] = orb;
            targs[ 1 ] = org.openorb.orb.core.ORB.class;
            Configurator conf = new Configurator( args, properties,
                  m_logger.getChildLogger( "configurator" ) );
            m_properties = conf.getProperties();
            // print the logging header
            if ( m_logger.isDebugEnabled() && Trace.isMedium() )
            {
                m_logger.debug( "\n"
                      + "-----------------------------------------------------------------\n"
                      + "OpenORB\nopenorb.debug.level=" + dbglvl
                      + " (OFF(0)/LOW(1)/MEDIUM(2)/HIGH(3))\n"
                      + "-----------------------------------------------------------------\n" );
                display_configuration( m_logger );
                m_logger.debug( "\n"
                      + "-----------------------------------------------------------------" );
            }
            load_initializers( cargs, conf.getInitializers() );
            targs[ 2 ] = ORBInitializer[].class;
            targs[ 3 ] = FeatureInitializer[].class;
            m_init_info = ( ORBInitInfo ) constructClass( "openorb.pi.ORBInitInfoClass",
                  "org.openorb.orb.pi.OpenORBInitInfo", cargs, targs );
            if ( m_init_info instanceof LogEnabled )
            {
               ( ( LogEnabled ) m_init_info ).enableLogging( m_logger );
            }
        }
        catch ( Throwable ex )
        {
            throw new CascadingRuntimeException(
              "Internal exception while attempting to create ORBInitInfo", ex );
        }

        m_init_info.pre_init();
        try
        {
            load_kernel();
        }
        catch ( Throwable ex )
        {
            throw new CascadingRuntimeException(
              "Exception during load_kernel() call", ex );
        }

        // load and register any valuetypes
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if ( m_config != null )
        {
            try
            {
                Configuration[] values = m_config.getChildren( "value" );
                for ( int i = 0; i < values.length; i++ )
                {
                    final Configuration vconfig = values[i];
                    final String idl = vconfig.getAttribute( "idl" );
                    final String factory = vconfig.getAttribute( "factory" );
                    try
                    {
                        final ValueFactory instance =
                              ( ValueFactory ) loader.loadClass( factory ).newInstance();
                        m_orb.register_value_factory( idl, instance, vconfig );
                    }
                    catch ( Throwable ex )
                    {
                        final String error =
                          "Unable to load value factory: " + factory
                          + ", for: " + idl;
                         throw new CascadingRuntimeException( error, ex );
                    }
                }
            }
            catch ( Throwable e )
            {
                final String error = "ORB initialization - could not load a valuetype factory.";
                throw new CascadingRuntimeException( error, e );
            }
        }
        if ( m_logger.isDebugEnabled() && Trace.isLow() )
        {
            m_logger.debug( "loading orb" );
        }
        m_init_info.post_init();
    }

    /**
     * This operation is used to display an OpenORB configuration.
     */
    public void display_configuration()
    {
        m_properties.display( m_logger );
    }

    /**
     * This operation is used to display an OpenORB configuration.
     * @param logger the logging channel to direct display of properties
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
     * @param name parent of properties. Properies of the form name + "." + xxx
     * are returned, where xxx can be anything. May be null to iterate over all
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
     * @return Property the named property
     */
    public Property getProperty( String name )
    {
        return m_properties.getProperty( name );
    }

    /**
     * Get the string property with the given name.
     * @param name the property name.
     * @param defl default value to use if property not found.
     * @return String the property value
     */
    public String getStringProperty( String name, String defl )
    {
        return m_properties.getStringProperty( name, defl );
    }

    /**
     * Get the string property with the given name.
     * @param name the property name.
     * @return String the property value
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
     * @return int the property value
     */
    public int getIntProperty( String name, int defl )
    {
        return m_properties.getIntProperty( name, defl );
    }

    /**
     * Get the integer property with the given name.
     * @param name the property name.
     * @return int the property value
     * @throws PropertyNotFoundException the property cannot be found.
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
     * @return boolean the property value
     */
    public boolean getBooleanProperty( String name, boolean defl )
    {
        return m_properties.getBooleanProperty( name, defl );
    }

    /**
     * Get the boolean property with the given name.
     * @param name the property name.
     * @return boolean the property value
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
     * @return URL the property value
     */
    public URL getURLProperty( String name, URL defl )
    {
        return m_properties.getURLProperty( name, defl );
    }

    /**
     * Get the URL property with the given name.
     * @param name the property name.
     * @return URL the property value
     * @throws PropertyNotFoundException the property cannot be found.
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
     * @return Class the property value
     */
    public Class getClassProperty( String name, Class defl )
    {
        return m_properties.getClassProperty( name, defl );
    }

    /**
     * Get the Class object property with the given name.
     * @param name the property name.
     * @param defl String name of default value to use if property not found.
     * @return Class the property value
     */
    public Class getClassProperty( String name, String defl )
    {
        return m_properties.getClassProperty( name, defl );
    }

    /**
     * Get the integer property with the given name.
     * @param name the property name.
     * @return Class the property value
     * @throws PropertyNotFoundException the property cannot be found.
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
     *    class. May be null if no property is used.
     * @param defl Default class name. Used if the named property is not found.
     *    May be null to indicate no load should be performed if property
     *    is missing.
     * @param args arguments to constructor. If any constructor arguments are
     *    primitive types then the four argument version of this function
     *    must be used.
     * @return the newly constructed object, or null if the property value is
     *    set to the empty string.
     * @throws java.lang.reflect.InvocationTargetException an exception occoured
     *    in the constructor.
     */
    public Object constructClass( String prop_key, String defl, Object[] args )
        throws java.lang.reflect.InvocationTargetException
    {
        return constructClass( prop_key, defl, args, null );
    }

    /**
     * This operation is used to load a class with the given property name and
     * default class name.
     *
     * @param prop_key Property name, this string property holds the name of the
     *    class. May be null if no property is used.
     * @param defl Default class name. Used if the named property is not found.
     *    May be null to indicate no load should be performed if property
     *    is missing.
     * @param args arguments to constructor. If any constructor arguments are
     *    primitive types then the four argument version of this function
     *    must be used.
     * @param args_t types of onstructor arguments. If any of these are null they
     *    will be determined from getClass on the matching arg. Length
     *    must match length of args.
     * @return the newly constructed object, or null if the property value is
     *    set to the empty string.
     * @throws java.lang.reflect.InvocationTargetException an exception occoured
     *    in the constructor.
     */
    public Object constructClass( String prop_key, String defl, Object[] args,
                                  Class[] args_t )
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
                throw new IllegalArgumentException(
                 "Length of args and args_t do not match" );
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
            throw ExceptionTool.initCause( new IllegalArgumentException(
                    "Illegal argument when constructing a class (" + ex + ")" ), ex );
        }
        catch ( final IllegalAccessException ex )
        {
            throw ExceptionTool.initCause( new IllegalArgumentException(
                    "Illegal access when constructing a class (" + ex + ")" ), ex );
        }
    }

   /**
    * Creation of a class constructor.
    * @param key property key
    * @param defl default value
    * @param args constructor arguments
    * @return java.lang.reflect.Constructor the constructor
    */
    public java.lang.reflect.Constructor classConstructor( String key, String defl, Class[] args )
    {
        Class clz = getClassProperty( key, defl );

        try
        {
            return clz.getConstructor( args );
        }
        catch ( final NoSuchMethodException ex )
        {
            throw ExceptionTool.initCause( new IllegalArgumentException(
                    "No constructor found in " + clz.getName() + " (" + ex + ")" ), ex );
        }
    }

    private void load_initializers( Object[] args, String[] cls_names )
    {
        ArrayList orbInits = new ArrayList( cls_names.length );
        ArrayList openOrbInits = new ArrayList( cls_names.length );

        if ( m_logger.isDebugEnabled() && Trace.isLow() )
        {
            m_logger.debug(
              "handling " + cls_names.length + " initializers" );
        }
        for ( int i = 0; i < cls_names.length; ++i )
        {
            Object instance;
            try
            {
                instance = Thread.currentThread().getContextClassLoader().
                  loadClass( cls_names[ i ] ).newInstance();
            }
            catch ( Throwable ex )
            {
                final String error = "Unable to load initializer class: "
                  +  cls_names[ i ];
                throw new CascadingRuntimeException( error, ex );
            }
            boolean ok = false;
            Configuration config = ( Configuration ) m_table.get( cls_names[i] );
            if ( config == null )
            {
                config = new DefaultConfiguration( "-", null );
            }
            final String name = config.getAttribute( "name", "initializer-"
                  + System.identityHashCode( instance ) );
            if ( m_logger.isDebugEnabled() && Trace.isMedium() )
            {
                m_logger.debug( "loading initializer: " + cls_names[i]
                      + " (catagory: " + name + ")" );
            }

            // provide the initalizer with a logger
            if ( instance instanceof LogEnabled )
            {
                if ( name != null )
                {
                    ( ( LogEnabled ) instance ).enableLogging( m_logger.getChildLogger( name ) );
                }
                else
                {
                    ( ( LogEnabled ) instance ).enableLogging( m_logger );
                }
            }

            // contextualize the initalizer
            if ( instance instanceof Contextualizable )
            {
                try
                {
                    ( ( Contextualizable ) instance ).contextualize( m_context );
                }
                catch ( Throwable e )
                {
                    final String error = "Unexpected exeption while contextualizing interceptor.";
                    throw new CascadingRuntimeException( error, e );
                }
            }

            // configure the initalizer
            if ( instance instanceof Configurable )
            {
                try
                {
                    ( ( Configurable ) instance ).configure( config );
                }
                catch ( Throwable e )
                {
                    final String error = "Unexpected exeption while configuring interceptor.";
                    throw new CascadingRuntimeException( error, e );
                }
            }

            // Some initializer's implement both interfaces, so they will be added
            // to both lists (i.e. ForwardInitializer, RMIInitializer)
            if ( instance instanceof ORBInitializer )
            {
                orbInits.add( instance );
                ok = true;
            }
            if ( instance instanceof FeatureInitializer )
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
        args[ 2 ] = new ORBInitializer[ orbInits.size() ];
        orbInits.toArray( ( Object[] ) args[ 2 ] );
        args[ 3 ] = new FeatureInitializer[ openOrbInits.size() ];
        openOrbInits.toArray( ( Object[] ) args[ 3 ] );
    }

    //============================================================================
    // static methods to load default ORB properties
    //============================================================================

   /**
    * Returns the system wide default ORB properties
    * @return java.util.Properties default properties
    */
    public static java.util.Properties getDefaultProperties( )
    {
        final String path = "org/apache/orb/config/orb.properties";
        java.util.Properties properties = new java.util.Properties();
        try
        {
            InputStream is = DefaultLoader.class.getClassLoader().getResourceAsStream( path );
            if ( is == null )
            {
                throw new RuntimeException( "Could not find the default 'orb.properties' "
                      + "resource from path: " + path );
            }
            properties.load( is );
        }
        catch ( Throwable e )
        {
            throw new CascadingRuntimeException(
              "Unexpected exception while loading configration.", e );
        }
        return properties;
    }

   /**
    * Returns the system default ORB configuration resource.
    * @return Configuration the default ORB configuration
    * @exception MissingResourceException if a null input stream is encountered
    * @exception ConfigurationException if a configuration exception occurs
    */
    private static Configuration loadDefaultConfiguration( )
        throws MissingResourceException, ConfigurationException
    {
        try
        {
            InputStream is = DefaultLoader.class.getClassLoader().getResourceAsStream(
              "org/apache/orb/config/DefaultLoader.xml" );
            if ( is != null )
            {
                DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder( );
                return builder.build( is );
            }
            else
            {
                return new DefaultConfiguration( "default", "DefaultLoader" );
                //throw new MissingResourceException(
                //  "Null input stream.", DefaultLoader.class.getName(),
                //  "org/apache/orb/config/DefaultLoader.xml"  );
            }
        }
        catch ( SAXException e )
        {
            throw new ConfigurationException(
              "Internal SAX exception while attempting to load configuration.", e );
        }
        catch ( IOException e )
        {
            throw new ConfigurationException(
              "Internal IO exception while attempting to load configuration.", e );
        }
    }

   /**
    * Adds property declarations to the supplied Properties argument from the
    * supplied configuration.
    * Any configuration elements of the following form will be translated
    * to property values and added to the property set.
    *
    * <pre>
    *   &lt;any-containing-element&gt;
    *      &lt;property name="myProperty" value="someValue"/&gt;
    *      &lt;property name="info" file="eggs.xml"/&gt;
    *   &lt;/any-containing-element&gt;
    * </pre>
    *
    * @param root the file path to be used in resolution of property
    *   element in the configuration that contain 'file' attributes as
    *   the property value
    * @param config a configuration containing 'property' element declarations
    * @param properties the properties to merge
    * @exception Exception if an merge error occurs
    */
    public void mergeProperties( File root, Configuration config, java.util.Properties properties )
        throws Exception
    {
        Configuration[] props = config.getChildren( "property" );
        for ( int i = 0; i < props.length; i++ )
        {
            Configuration child = props[i];

            //
            // every property must have a name
            //

            String name = "";
            try
            {
                name = child.getAttribute( "name" );
            }
            catch ( ConfigurationException noName )
            {
                final String error = "encountered a property without a name";
                throw new CascadingException ( error, noName );
            }

            //
            // The value of a property is either declared directly under a value attribute,
            // or indirectory under a 'file' attribute.  In the case of 'file' attributes
            // we need to resolve this relative to this file before setting the
            // property value.
            //

            String value = "";
            try
            {
                value = child.getAttribute( "value" );
            }
            catch ( ConfigurationException noValueAttribute )
            {
                try
                {
                    final String s = child.getAttribute( "file" );
                    File f = new File( root, s );
                    value = f.getAbsolutePath();
                }
                catch ( ConfigurationException noFileAttribute )
                {
                    String s = null;
                    try
                    {
                        s = child.getAttribute( "url" );
                    }
                    catch ( Exception noURL )
                    {
                        final String error =
                          "Found a property without a 'value', 'file' or 'url' attribute";
                        throw new CascadingException( error, noURL );
                    }
                    if ( s.startsWith( "file:" ) )
                    {
                        try
                        {
                            URL base = root.toURL();
                            URL url = new URL( base, s );
                            value = url.toString();
                        }
                        catch ( Exception unknown )
                        {
                            final String error =
                              "Unexpected exception while creating file:// URL value.";
                            throw new CascadingException( error, unknown );
                        }
                    }
                    else
                    {
                        try
                        {
                            URL url = new URL( s );
                            value = url.toString();
                        }
                        catch ( Exception unknown )
                        {
                            final String error = "Unexpected exception while creating URL value.";
                            throw new CascadingException( error, unknown );
                        }
                    }
                }
            }
            properties.setProperty( name, value );
        }
    }

    //=====================================================================
    // Disposable
    //=====================================================================

   /**
    * Called by the ORB during shutdown enabling the initalizer to clean up.
    */
    public void dispose()
    {
        if ( m_logger.isDebugEnabled() && Trace.isLow() )
        {
            m_logger.debug( "ORB Loader disposal" );
        }

        try
        {
            if ( m_init_info instanceof Disposable )
            {
                ( ( Disposable ) m_init_info ).dispose();
            }
        }
        catch ( Throwable e )
        {
            // log message but don't throw an exception
            final String warning = "Ignoring error during initializer disposal.";
            m_logger.warn( warning, e );
        }
        finally
        {
            m_config = null;
            m_context = null;
            m_table = null;
            m_properties = null;
            m_base = null;
            m_orb = null;
            m_init_info = null;
        }
    }
}
