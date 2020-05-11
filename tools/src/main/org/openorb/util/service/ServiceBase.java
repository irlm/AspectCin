/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.service;

import java.io.File;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;

import org.omg.CORBA.ORB;

import org.openorb.util.NamingUtils;
import org.openorb.util.ORBUtils;


/**
 * This abstract class is the base class for all Service classes.
 * It provides the lifecycle framework for initializing,
 * contextualizing, starting, stopping, and disposing a service.
 * <p>
 * The Avalon lifecyle startup phase consists of:
 * <ol>
 * <li>constructor (as a consequence of instantiation)
 * <li>enableLogging
 * <li>contextualize
 * <li>parameterize
 * <li>initialize
 * <li>start
 * </ol>
 * See
 * <a href="http://avalon.apache.org/framework/reference-the-lifecycle.html">
 * Avalon Framework - The Component Lifecyle</a> for more details.
 * <p>
 * Writing a Service with an additional command line argument would
 * look like this:
 * <pre>
 * public class Service
 *     extends ServiceBase
 * {
 *     private POA m_poa = null;
 *
 *     public void initializeService()
 *     {
 *         try
 *         {
 *             ORB orb = getORB();
 *             if ( orb != null )
 *             {
 *                 m_poa = POAHelper.narrow( ( POA ) createPOA() );
 *                 MyServiceServant ms = new MyServiceServant();
 *                 org.omg.CORBA.Object obj = ms._this( orb );
 *                 registerObject( "MyService", obj );
 *             }
 *         }
 *         catch ( Exception ex )
 *         {
 *             // handle errors
 *         }
 *     }
 *
 *     public void startService()
 *     {
 *         try
 *         {
 *             if ( m_poa != null )
 *             {
 *                 m_poa.the_POAManager().activate();
 *             }
 *         }
 *         catch ( AdapterInactive ex )
 *         {
 *             // handle errors
 *         }
 *     }
 *
 *     public void stopService()
 *     {
 *         try
 *         {
 *             if ( m_poa != null )
 *             {
 *                 m_poa.the_POAManager().deactivate( false, true );
 *             }
 *         }
 *         catch ( AdapterInactive ex )
 *         {
 *             // handle errors
 *         }
 *     }
 * }
 * </pre>
 *<p>
 * The POA handling can't be moved into the ServiceBase class because JDK1.3.x does not
 * have POA support and because OpenORB still supports JDK 1.3.x compilation under JDK 1.3.x
 * would not be possible
 * (This will eventually be changed when support for JDK 1.3.x will be dropped).
 * <p>
 * The protected xxxService() mehods are callback methods that are called by the actual methods
 * from the Avalon lifecycle interfaces. The actual methods do some consistency checking before
 * the callback methods are invoked. The callback methods can be overridden to adapt the behaviour
 * of the service in the corresponding state of the Avalon lifecycle.
 * <p>
 * The registerObject() method tries to register an object at the NamingService and at the OpenORB
 * CorbalocService. This method must be used because all the bootstrapping issues are hidden
 * behind this method. When the NamingService can't be found the file-system fallback will be used
 * instead. Also the registerObject() method keeps track of registered objects and will deregister
 * them automatically when the service is taken down, i.e. disposed.
 */
public abstract class ServiceBase implements LogEnabled, Contextualizable,
        Parameterizable, Initializable, Startable, Disposable
{
    //
    // Standard command line options
    //

    public static final String OPT_HELP                   = "h";
    public static final String OPT_HELP_LONG              = "help";
    public static final String OPT_HELP_DESCRIP
            = "print this message and exit";

    public static final String OPT_DEBUG                  = "d";
    public static final String OPT_DEBUG_ARG              = "level";
    public static final String OPT_DEBUG_LONG             = "debug";
    public static final String OPT_DEBUG_DESCRIP
            = "enable debugging output. (Optional) Level values: none, debug, info, warn, error. "
            + "Default is debug.";

    /* Binding to the Naming & Corbaloc service is true by default.
     * This command line option then turns this off.
     */
    public static final String OPT_BIND_CORBALOC          = "bindCorbaloc";
    public static final String OPT_BIND_NS                = "bindNaming";

    public static final String OPT_NOBIND                 = "n";
    public static final String OPT_NOBIND_ARG             = "service";
    public static final String OPT_NOBIND_LONG            = "noBind";
    public static final String OPT_NOBIND_DESCRIP
            = "do not bind to the specified service. Service values: Naming, Corbaloc.";

    public static final String OPT_DEFAULT_CS             = "e";
    public static final String OPT_DEFAULT_CS_LONG        = "default";

    // too long for CLI: " NOTE: Port 683 requires root permission under UNIX)";
    public static final String OPT_DEFAULT_CS_DESCRIP
            = "use the DefaultCorbalocService (IIOP port 683).";

    public static final String OPT_VERSION                = "v";
    public static final String OPT_VERSION_LONG           = "version";
    public static final String OPT_VERSION_DESCRIP
            = "print the version information and exit";

    public static final String OPT_PRINT_URL              = "u";
    public static final String OPT_PRINT_URL_LONG         = "printCorbalocURL";
    public static final String OPT_PRINT_URL_DESCRIP
            = "print the corbaloc url for the service";

    public static final String OPT_WRITE_URL_FILE         = "U";
    public static final String OPT_WRITE_URL_FILE_ARG     = "directory";
    public static final String OPT_WRITE_URL_FILE_LONG    = "writeCorbalocURL";
    public static final String OPT_WRITE_URL_FILE_DESCRIP
            = "write the corbaloc url to a file in the specified directory."
              + " Default is current working folder.";

    public static final String OPT_PRINT_IOR              = "i";
    public static final String OPT_PRINT_IOR_LONG         = "printIOR";
    public static final String OPT_PRINT_IOR_DESCRIP
            = "print the ior of the service";

    public static final String OPT_WRITE_IOR_FILE         = "f";
    public static final String OPT_WRITE_IOR_FILE_ARG     = "directory";
    public static final String OPT_WRITE_IOR_FILE_LONG    = "writeIORFile";
    public static final String OPT_WRITE_IOR_FILE_DESCRIP
            = "write the ior to a file in the specified directory."
              + " Default is current working folder.";

    public static final String OPT_LOG_FILE         = "l";
    public static final String OPT_LOG_FILE_ARG     = "logfile";
    public static final String OPT_LOG_FILE_LONG    = "logfile";
    public static final String OPT_LOG_FILE_DESCRIP
            = "log to a file in the current working folder. Default file is <service>.log";

    //
    // Avalon lifecycle
    //

    /** Flag whether the service is log enabled. */
    private boolean m_logenabled = false;
    /** The logger instance. */
    private Logger m_logger = null;

    /** Flag whether the service is contextualized. */
    private boolean m_contextualized = false;
    /** The context instance. */
    private Context m_context = null;

    /** Flag whether the service is parameterized. */
    private boolean m_parameterized = false;
    /** The parameters object. */
    private Parameters m_parameters = null;

    /** Flag whether the service is initialized. */
    private boolean m_initialized = false;
    /** Flag whether the service is started. */
    private boolean m_started = false;


    //
    // Service Names
    //

    /** The service long name. */
    private String m_long_name = null;

    /** The service short name. */
    private String m_short_name = null;

    /** The service version. */
    private String m_version = null;


    //
    // Variables
    //

    /** The ORB */
    private ORB m_orb;

    /** This map stores name object associations. */
    private final HashMap m_nameObjectMap = new HashMap();


    //
    // Avalon lifecycle
    //


    /**
     * Provide the service with a logger. This must be the first
     * operation in a service lifecycle.
     *
     * @param logger The logger instance for this service.
     */
    public void enableLogging( Logger logger )
    {
        m_logger = logger;
        m_logenabled = true;
    }


    /**
     * Return the logger instance associated with this service.
     *
     * @return The logger instance.
     */
    public Logger getLogger()
    {
        return m_logger;
    }


    /**
     * This callback method is called from contextualize() and may be overridden by the
     * actual Service class.
     * It is called from the contextualize() method after doing some initial consistency
     * check.
     *
     * @param context The context for the service.
     * @throws ContextException When an error accessing the context occurs.
     */
    protected void contextualizeService( Context context )
            throws ContextException
    {
    }


    /**
     * This is the actual contextualize operation that calls the
     * contextualizeService method.
     * It will be called by the ServerBase.initService() method.
     *
     * @param context The context for the service.
     * @throws ContextException When an error accessing the context occurs.
     */
    public final synchronized void contextualize( Context context )
            throws ContextException
    {
        if ( !m_logenabled )
        {
            throw new IllegalStateException( "not log enabled" );
        }
        m_contextualized = true;
        m_context = context;

        getLogger().debug( "Contextualizing the service" );

        // get the standard parameters

        m_orb = ( ORB ) getContext().get( ServiceContext.ORB );
        if ( m_orb == null )
        {
            throw new ContextException( "null or missing ORB" );
        }

        // now call the user defined method.
        contextualizeService( context );
    }


    /**
     * Return the context of this instance.
     *
     * @return The context of the instance.
     */
    protected Context getContext()
    {
        if ( !m_contextualized )
        {
            throw new IllegalStateException ( "not contextualized" );
        }
        return m_context;
    }


    /**
     * This callback method is called from parameterize() and may be overridden by the
     * actual Service class.
     * It is called from the parameterize() method after doing some initial consistency
     * check.
     * @param params configuration parameters
     */
    protected void parameterizeService( Parameters params )
    {
    }


    /**
     * This is the actual parameterize operation which calls parameterizeService.
     * It will be called by the ServerBase.initService() method.
     * @param params Parameters
     */
    public final synchronized void parameterize( Parameters params )
    {
        if ( !m_contextualized )
        {
            throw new IllegalStateException ( "not contextualized" );
        }
        m_parameterized = true;
        m_parameters = params;

        getLogger().debug( "Parameterizing the service" );

        // now call the user defined method.
        parameterizeService( params );
    }


    /**
     * Return the parameters of this instance.
     *
     * @return The parameters of the instance.
     */
    protected Parameters getParameters()
    {
        if ( !m_parameterized )
        {
            throw new IllegalStateException ( "not parameterized" );
        }
        return m_parameters;
    }


    /**
     * This callback method is called from initialize() and may be overridden by the
     * actual Service class.
     * It is called from the initialize() method after doing some initial consistency
     * check.
     */
    protected void initializeService()
    {
    }


    /**
     * This is the actual initialize operation which calls initializeService.
     * It will be called by the ServerBase.initService() method.
     */
    public final synchronized void initialize()
    {
        if ( !m_parameterized )
        {
            throw new IllegalStateException ( "not parameterized" );
        }
        m_initialized = true;

        getLogger().debug( "Initializing the service" );

        // now call the user defined method.
        initializeService();
    }


    /**
     * This callback method is called from start() and may be overridden by the
     * actual Service class.
     * It is called from the start() method after doing some initial consistency
     * check.
     */
    protected void startService()
    {
    }


    /**
     * This is the actual start operation which calls startService.
     * It will be called by the ServerBase.initService() method.
     */
    public final synchronized void start()
    {
        if ( !m_initialized )
        {
            throw new IllegalStateException( "not initialized" );
        }
        m_started = true;

        getLogger().debug( "Starting the service" );

        // now call the user defined method.
        startService();
    }


    /**
     * This callback method is called from stop() and may be overridden by the
     * actual Service class.
     * It is called from the stop() method after doing some initial consistency
     * check.
     */
    protected void stopService()
    {
    }


    /**
     * This is the actual stop operation which calls stopService.
     * It will be called by the ServerBase.initService() method.
     */
    public final synchronized void stop()
    {
        if ( !m_started )
        {
            throw new IllegalStateException( "not started" );
        }
        m_started = false;

        getLogger().debug( "Stopping the service" );

        // now call the user defined method.
        stopService();
    }


    /**
     * This callback method is called from dispose() and may be overridden by the
     * actual Service class.
     * It is called from the dispose() method after doing some initial consistency
     * check.
     */
    protected void disposeService()
    {
    }


    /**
     * This is the actual dispose operation which calls disposeService.
     * It will be called by the ServerBase.initService() method.
     */
    public final synchronized void dispose()
    {
        if ( !m_initialized )
        {
            throw new IllegalStateException( "not initialized" );
        }
        if ( m_initialized && m_started )
        {
            throw new IllegalStateException( "not stopped yet" );
        }
        m_initialized = false;

        getLogger().debug( "Disposing the service" );

        // deregister objects from NamingService and/or CorbalocService
        deregisterObjects();

        // now call the user defined method.
        disposeService();
    }


    /**
     * Indicates whether the Service has been logEnabled.
     * @return true if logEnabled; false if not
     */
    public boolean isLogEnabled()
    {
        return m_logenabled;
    }


    /**
     * Indicates whether the Service has been contextualized.
     * @return true if contextualized; false if not
     */
    public boolean isContextualized()
    {
        return m_contextualized;
    }


    /**
     * Indicates whether the Service has been parameterized.
     * @return true if parameterized; false if not
     */
    public boolean isParameterized()
    {
        return m_parameterized;
    }


    /**
     * Indicates whether the Service has been initialized.
     * @return true if initialized; false if not
     */
    public boolean isInitialized()
    {
        return m_initialized;
    }


    /**
     * Indicates whether the Service has been started.
     * @return true if started; false if not
     */
    public boolean isStarted()
    {
        return m_started;
    }


    /**
     * Returns the ORB used by the Service
     * @return ORB object
     */
    public ORB getORB()
    {
        return m_orb;
    }


    //
    // Service Names
    //

    /**
     * Return the long name for the service.
     *
     * @return The long name.
     */
    public final String getLongName()
    {
        if ( m_long_name == null )
        {
            m_long_name = ORBUtils.getLongFromShortName( getShortName() );
        }

        return m_long_name;
    }


    /**
     * Return the short name for the service.
     *
     * @return The short name.
     */
    public final String getShortName()
    {
        if ( m_short_name == null )
        {
            m_short_name = ORBUtils.getShortNameFromClass( this.getClass() );
        }

        return m_short_name;
    }


    /**
     * Return the version for the service.
     *
     * @return The version.
     */
    public final String getVersion()
    {
        if ( m_version == null )
        {
            m_version = ORBUtils.getVersionFromShortName( getShortName() );
        }

        return m_version;
    }


    //
    // Helper methods
    //


    /**
     * This method is used to get the parent POA.
     * First, any "POA" passed in the Context will be used.
     * Otherwise, the RootPOA will be taken from the orb passed.
     * <p>
     * This method may be overridden to create a special POA.
     * <p>
     * The return type is java.lang.Object
     * because the POA class is not available in JDK 1.3.x. Using the POA class
     * in the method signature would introduce a dependency to JDK 1.4.x.
     *
     * @return POA object (cast to org.omg.PortableServer.POA)
     */
    protected Object createPOA()
    {
        Object poa = null;

        // get the parent POA from the Context (if any)
        try
        {
            poa = getContext().get( ServiceContext.POA );
        }
        catch ( ContextException ex )
        {
            // not an error yet
        }

        if ( poa == null )
        {
            // use the RootPOA
            try
            {
                poa = m_orb.resolve_initial_references( NamingUtils.IR_ROOT_POA );
            }
            catch ( Exception ex )
            {
                getLogger().error( "Getting the parent poa failed", ex );
            }
        }

        return poa;
    }


    /**
     * Register the object at the NamingService and/or at the CorbalocService.
     * Deregistration will be done automatically upon service shutdown.
     *
     * @param name The name of the object.
     * @param obj The object itself.
     */
    protected final void registerObject( String name, org.omg.CORBA.Object obj )
    {
        // add the object to the map so that we can unbind later without
        // the user explicitly having to do that
        m_nameObjectMap.put( name, obj );

        try
        {
            boolean bForcePrintIOR = false;
            String corbaloc = null;

            // handle the command line switches

            // Option -- bind to Naming Service
            if ( m_parameters.getParameterAsBoolean( OPT_BIND_NS, true ) )
            {
                 bForcePrintIOR = bindToNamingService( name, obj, m_orb );
            }

            // Option -- bind to Corbaloc Service
            if ( m_parameters.getParameterAsBoolean( OPT_BIND_CORBALOC, true ) )
            {
                corbaloc = bindToCorbalocService( obj, m_orb );
            }

            // Option -- write the IOR to a file
            //    modified so that if OPT_WRITE_IOR_FILE isn't a directory, then
            //    treat it like a filename instead, which lets a user control
            //    the name of the IOR file instead of just the directory...
            if ( m_parameters.isParameter( OPT_WRITE_IOR_FILE ) )
            {
                String arg = m_parameters.getParameter( OPT_WRITE_IOR_FILE, "" );
                String fileName = ORBUtils.getIORFileName( arg, name );

                ORBUtils.writeIORToFileName( m_orb, fileName, obj );
            }

            // Option -- print the IOR
            if ( m_parameters.getParameterAsBoolean( OPT_PRINT_IOR, false ) || bForcePrintIOR )
            {
                String ior = m_orb.object_to_string( obj );

                consolePrintln( name + "=" + ior );
            }

            // Option -- write Corbaloc URL to a file
            if ( corbaloc != null && m_parameters.isParameter( OPT_WRITE_URL_FILE ) )
            {
                String dirname = m_parameters.getParameter( OPT_WRITE_URL_FILE, "" );
                if ( dirname.length() != 0 && !dirname.endsWith( File.separator ) )
                {
                    dirname += File.separator;
                }

                ORBUtils.writeURLToFile( dirname + getLongName(), corbaloc );
            }

            // Option -- print Corbaloc URL
            if ( corbaloc != null && m_parameters.getParameterAsBoolean( OPT_PRINT_URL, false ) )
            {
                consolePrintln( getLongName() + "=" + corbaloc );
            }
        }
        catch ( Exception ex )
        {
            getLogger().error( "An unexpected exception occured", ex );
        }
    }


    /**
     * Bind the Service into Corbaloc Service.
     * @param obj object to bind
     * @param orb ORB
     * @return Corbaloc name bound
     */
    private String bindToCorbalocService( org.omg.CORBA.Object obj, ORB orb )
    {
        String corbaloc = null;
        try
        {
            corbaloc = NamingUtils.bindObjectToCorbalocService( orb, getLongName(), obj );
            if ( corbaloc == null )
            {
                getLogger().warn( "Cannot bind '" + getLongName() + "' to the CorbalocService."
                          + " This service is only available with the OpenORB orb." );
            }
        }
        catch ( Exception ex )
        {
            getLogger().error( "Binding to CorbalocService failed", ex );
        }

        return corbaloc;
    }


    /**
     * Bind into the Naming Service
     * @param name name to bind
     * @param obj object to bind
     * @param orb ORB
     * @return true if need to print the IOR
     */
    protected boolean bindToNamingService( String name, org.omg.CORBA.Object obj, ORB orb )
    {
        String long_name = getLongName();
        boolean bNoNS = NamingUtils.NS_NAME_LONG.equals( long_name );
        boolean bForcePrintIOR = false;

        try
        {
            String ns_name = NamingUtils.ROOT_COS_CONTEXT + "/" + long_name + "/" + name;

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Trying to bind: '" + ns_name + "' ( NoNS = " + bNoNS + " )" );
            }

            boolean result = false;

            if ( !bNoNS )
            {
               result = NamingUtils.dynamicRebind( orb, ns_name, obj );
               if ( !result )
               {
                   getLogger().warn( "Failed to bind '" + name + "' to the NamingService."
                             + " Check whether your NamingService is running!" );
               }
            }

            if ( !result )
            {
                result = NamingUtils.bindObjectToFileSystem( orb, ns_name, obj );
                if ( !result )
                {
                    getLogger().warn( "Failed to write the '" + name
                                      + "' IOR to the File System." );

                    // if NS bind failed and no ior file be written then at least show the IOR
                    if ( !m_parameters.isParameter( OPT_WRITE_IOR_FILE ) )
                    {
                        bForcePrintIOR = true;
                    }
                }
            }
        }
        catch ( Exception ex )
        {
            getLogger().error( "Binding to NamingService failed", ex );
        }

        return bForcePrintIOR;
    }


    /**
     * Deregister the objects that have been bound at the NamingService and/or at the
     * CorbalocService.
     */
    private void deregisterObjects()
    {
        String long_name = getLongName();
        boolean bNoNS = NamingUtils.NS_NAME_LONG.equals( long_name );

        Iterator iter = m_nameObjectMap.keySet().iterator();
        while ( iter.hasNext() )
        {
            String name = ( String ) iter.next();

            // don't unbind when we did not bind
            if ( m_parameters.getParameterAsBoolean( OPT_BIND_NS, true ) )
            {
                // binding to naming service enabled
                String ns_name = NamingUtils.ROOT_COS_CONTEXT + "/" + long_name + "/" + name;

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Trying to unbind: " + ns_name + " ( NoNS = "
                          + bNoNS + " )" );
                }

                boolean result = NamingUtils.unbindObjectFromNamingService( m_orb, ns_name, bNoNS );
                if ( !result )
                {
                    getLogger().warn( "Cannot unbind '" + name + "' from the NamingService."
                              + " Check whether your NamingService is running!" );
                }
            }

            if ( m_parameters.isParameter( OPT_WRITE_IOR_FILE ) )
            {
                String dirname = m_parameters.getParameter( OPT_WRITE_IOR_FILE, "" );
                String filename = ORBUtils.getIORFileName( dirname, name );

                // try to delete the IOR file again
                File f = new File( filename );
                if ( !f.delete() )
                {
                    getLogger().warn( "Couldn't delete file: " + filename );
                }
            }
        }
    }


    /**
     * Ensures the message is displayed on System.out
     * @param msg message to print
     */
    protected void consolePrintln( String msg )
    {
        // always log it
        if ( m_logger != null && m_logger.isInfoEnabled() )
        {
            getLogger().info( msg );
        }
        else
        {
            // output to console if the logging priority would prevent it from displaying
            System.out.println( msg );
        }
    }
}

