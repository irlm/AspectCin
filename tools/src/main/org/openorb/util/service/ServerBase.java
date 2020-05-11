/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util.service;

import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;

import org.apache.avalon.framework.CascadingRuntimeException;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogKitLogger;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;

import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Priority;

import org.apache.log.format.Formatter;
import org.apache.log.format.ExtendedPatternFormatter;

import org.apache.log.output.io.FileTarget;
import org.apache.log.output.io.StreamTarget;

import org.omg.CORBA.ORB;

import org.openorb.util.ORBUtils;
import org.openorb.util.logger.NullLoggerTeam;


/**
 * A server controls the start-up of a servce.
 * This abstract class must be overridden to handle specific command
 * line arguments, or to create a special logger.
 * <p>
 * The default command line switches are:
 * <pre>
 * -d,--debug <level>      enable debugging output. (Optional) Level
 *                         values: debug, info, warn, error. Default is debug.
 * -e,--default            try to use the DefaultCorbalocService (IIOP port
 *                         683.)
 * -f,--writeIORFile       write the ior of the service to a file in the
 *                         current working folder
 * -h,--help               print this message and exit
 * -i,--printIOR           print the ior of the service
 * -l,--logfile <file>     log to a file in the current working folder.
 *                         Default file is <service>.log
 * -n,--noBind <service>   do not bind to the specified service.
 *                         Services: Naming, Corbaloc.
 * -u,--printCorbalocURL   print the corbaloc url for the service
 * -v,--version            print the version information and exit
 * </pre>
 *
 * A server class without any customization would look like this:
 * <pre>
 * public class Server
 *     extends ServerBase
 * {
 *     public static void main( String[] args )
 *     {
 *         Server srv = new Server();
 *         srv.init( args );
 *         srv.run();
 *     }
 * }
 * </pre>
 *
 * Adding further command line arguments is not complicated:
 * <pre>
 * public class Server
 *     extends ServerBase
 * {
 *     public void handleArguments( CommandLine cmdline, Parameters params )
 *     {
 *         params.setParameter( Service.OPT_MY_SPECIAL,
 *               Boolean.toString( cmdline.hasOption( Service.OPT_MY_SPECIAL ) ) );
 *     }
 *
 *     public static void main( String[] args )
 *     {
 *         Server srv = new Server();
 *         Options options = new Options();
 *         options.addOption( new Option( Service.MY_SPECIAL,
 *               Service.OPT_MY_SPECIAL_LONG, false,
 *               Service.OPT_MY_SPECIAL_DESCRIP ) );
 *         srv.init( args, options );
 *         srv.run();
 *     }
 * }
 * </pre>
 *
 * A custom logger can be created by overriding the createLogger method.
 */
public abstract class ServerBase
{
    /** The default logger format. */
    public static final String DEFAULT_FORMAT
            = "[%{thread}] [%5.5{priority}] (%{category}): %{message}\\n%{throwable}";

    /** The default logger file format. */
    public static final String DEFAULT_FILE_FORMAT
            = "[%{rtime}] [%{thread}] [%5.5{priority}] (%{category}): %{message}\\n%{throwable}";

    /** The process logger instance. */
    private Logger m_logger = null;

    /** The orb instance. */
    private ORB m_orb = null;

    /** Flag whether the server has been initialized. */
    private boolean m_initialized = false;

    /** The service instance. */
    private ServiceBase m_service = null;

    /** The service context. */
    private ServiceContext m_svc_ctx = null;

    /** The service parameters object. */
    private Parameters m_svc_params = null;

    /** Optional service class name entered via 'setServiceClassName()'. */
    private String m_serviceClassNameStr = null;

    /**
     * Flag indicating whether we are logging to a file (true)
     * or System.out (false).
     */
    private boolean m_isFileLogging = false;

    /**
     * Initialize the server.
     *
     * @param args The command line arguments for parsing.
     * @return The CommandLine for service specific handling.
     */
    public final CommandLine init( String[] args )
    {
        return init( args, null, null, null );
    }

    /**
     * Initialize the server.
     *
     * @param args The command line arguments for parsing.
     * @param options Service specific options.
     * @return The CommandLine for service specific handling.
     */
    public final CommandLine init( String[] args, Options options )
    {
        return init( args, options, null, null );
    }

    /**
     * Initialize the server.
     *
     * @param args The command line arguments for parsing.
     * @param props Properties for the orb init call.
     * @return The CommandLine for service specific handling.
     */
    public final CommandLine init( String[] args, Properties props )
    {
        return init( args, null, props, null );
    }

    /**
     * Initialize the server.
     *
     * @param args The command line arguments for parsing.
     * @param options Service specific options.
     * @param props Properties for the orb init call.
     * @return The CommandLine for service specific handling.
     */
    public final CommandLine init( String[] args, Options options, Properties props )
    {
        return init( args, options, props, null );
    }

    /**
     * Initialize the server.
     *
     * @param args The command line arguments for parsing.
     * @param options Service specific options.
     * @param props Properties for the orb init call.
     * @param params an initial set of service parameters.
     * @return The CommandLine for service specific handling.
     */
    public final CommandLine init( String[] args, Options options, Properties props,
          Parameters params )
    {
        // catch any exceptions during initialization to ensure System.exit occurs
        try
        {
            // create the service
            m_service = createService();

            // create the service context
            m_svc_ctx = createServiceContext();

            // create the parameters object
            m_svc_params = new Parameters();

            // Parse the command-line arguments and fill in the Parameters
            // Do this before the ORB is initialized as some options affect ORB creation
            // (--default), and before logger creation as the logger is affected as well
            // (--debug).
            CommandLine cmdline = parseArguments(
                    ORBUtils.extractNonORBArgs( args ), options, m_svc_params );

            // if service parameters given then merge them in now
            if ( params != null )
            {
                m_svc_params.merge( params );
            }

            // create the logger for the service
            m_logger = createLogger( m_service.getShortName(), m_svc_params );
            consolePrintln( getVersionString() );

            // create the ORB instance
            m_orb = createORB( ORBUtils.extractORBArgs( args ), props );

            // add the ORB to the service context
            m_svc_ctx.put( ServiceContext.ORB, m_orb );

            // handle service specific args now.
            // This must be done before the parameters object is set to read-only.
            handleArguments( cmdline, m_svc_params );

            // initialize the service
            initService( m_service, m_svc_ctx, m_svc_params );

            // set the server to initialized
            m_initialized = true;

            // return the command line object
            return cmdline;
        }
        catch ( CascadingRuntimeException ex )
        {
            ex.getCause().printStackTrace();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }

        // initialization failed
        System.exit( 5 );
        return null;
    }


    /**
     * Return the process' logger instance.
     * @return Framework logger to use for all logging
     */
    public Logger getLogger()
    {
        return m_logger;
    }


    /**
     * Create the service's root logger.
     * Uses the Avalon LogKit logger.
     * Maybe overridden to create a special logger.
     *
     * @param name The name of the logger.
     * @param svc_params service parameters
     * @return The Avalon framework logger instance used for this process.
     */
    protected Logger createLogger( final String name, final Parameters svc_params )
    {
        try
        {
            // Cmdline Option -- debugging
            final String priorityStr = m_svc_params.getParameter(
                    ServiceBase.OPT_DEBUG, Priority.INFO.getName() );

            final Priority priority = ( 0 == priorityStr.length() ) ? Priority.INFO
                    : Priority.getPriorityForName( priorityStr.toUpperCase() );

            if ( Priority.NONE.equals( priority ) )
            {
                return NullLoggerTeam.getInstance();
            }

            // we take a new hierarchy here because we don't want to
            // get influenced by the logger of the ORB instance
            final Hierarchy hierarchy = new Hierarchy();

            final Formatter formatter = new ExtendedPatternFormatter( DEFAULT_FORMAT );
            final StreamTarget systemOutTarget = new StreamTarget( System.out, formatter );

            hierarchy.setDefaultLogTarget( systemOutTarget );

            hierarchy.setDefaultPriority( priority );

            // create a LogKit logger and wrap it in an Avalon framework logger
            org.apache.log.Logger logKitLogger = hierarchy.getLoggerFor( name );
            final Logger result = new LogKitLogger( logKitLogger );

            // configure the Logger priorities
            configureLogger( logKitLogger, m_svc_params );

            return result;
        }
        catch ( final Throwable ex )
        {
            throw new CascadingRuntimeException( "Unexpected exception while creating"
                  + " logger.", ex );
        }
    }


    /**
     * Configure the logger.
     * This method can be overriden to configure
     * the logging priorities, etc.
     * <p>
     * By default, the cmdline option (--debug)
     * is checked to set the default logging priority.
     *
     * @param logger real logger instance.
     * The class depends on the actual logging package
     * being used, e.g.
     * LogKit (org.apache.log.Logger),
     * log4J (org.apache.log4j.Logger)
     * JDK1.4 (java.util.logging.Logger)
     * @param svc_params service parameters.
     * Passed here since getServiceParameters() will not work
     */
    protected void configureLogger( Object logger, Parameters svc_params )
    {
        /* verify it is an Avalon LogKit logger */
        if ( logger instanceof org.apache.log.Logger )
        {
            org.apache.log.Logger logKitLogger = ( org.apache.log.Logger ) logger;

            // Cmdline Option -- log to a log file
            if ( svc_params.isParameter( ServiceBase.OPT_LOG_FILE ) )
            {
                // logfile name (default is <svc>.log)
                String defaultLogfileName = m_service.getShortName() + ".log";
                String logfileName = svc_params.getParameter( ServiceBase.OPT_LOG_FILE,
                        defaultLogfileName );
                if ( logfileName.length() == 0 )
                {
                    // --logfile was specified but no logfile value
                    logfileName = defaultLogfileName;
                }

                try
                {
                    final File file = new File( logfileName );

                    Formatter formatter = new ExtendedPatternFormatter( DEFAULT_FILE_FORMAT );
                    FileTarget target = new FileTarget( file, true, formatter );

                    logKitLogger.setLogTargets( new LogTarget[] { target } );

                    m_isFileLogging = true;
                }
                catch ( Exception ex )
                {
                    logKitLogger.error( "unable to write to " + logfileName + " logfile", ex );
                }
            }

            // Cmdline Option -- debugging level
            String priorityStr = m_svc_params.getParameter( ServiceBase.OPT_DEBUG,
                    Priority.INFO.getName() ).toUpperCase();
            if ( priorityStr.length() == 0 ) // --debug specified but not level
            {
                priorityStr = Priority.INFO.getName();
            }
            logKitLogger.setPriority(
                    Priority.getPriorityForName( priorityStr.toUpperCase() ) );
        }
    }


    /**
     * Create the ServiceContext.
     * This method may be overriden to create a subclassed
     * ServiceContext or to provide a ServiceContext with
     * initial values.
     * @return ServiceContext instance
     */
    protected ServiceContext createServiceContext()
    {
        return new ServiceContext();
    }



    /**
     * Create the Command-line Options
     * @param options initial value of Options or null
     * @return fully populated Options
     */
    private Options createCmdineOptions( Options options )
    {
        // build a collection of command line options
        if ( options == null )
        {
            options = new Options();
        }

        options.addOption( new Option( ServiceBase.OPT_HELP,
              ServiceBase.OPT_HELP_LONG, false,
              ServiceBase.OPT_HELP_DESCRIP ) );
        options.addOption(  OptionBuilder.withArgName( ServiceBase.OPT_DEBUG_ARG )
                            .hasOptionalArg()
                            .withLongOpt( ServiceBase.OPT_DEBUG_LONG )
                            .withDescription( ServiceBase.OPT_DEBUG_DESCRIP )
                            .create( ServiceBase.OPT_DEBUG )
                            );
        options.addOption( new Option( ServiceBase.OPT_VERSION,
              ServiceBase.OPT_VERSION_LONG, false,
              ServiceBase.OPT_VERSION_DESCRIP ) );
        options.addOption(  OptionBuilder.withArgName( ServiceBase.OPT_LOG_FILE_ARG )
                            .hasOptionalArg()
                            .withLongOpt( ServiceBase.OPT_LOG_FILE_LONG )
                            .withDescription( ServiceBase.OPT_LOG_FILE_DESCRIP )
                            .create( ServiceBase.OPT_LOG_FILE )
                            );
        options.addOption( new Option( ServiceBase.OPT_PRINT_URL,
              ServiceBase.OPT_PRINT_URL_LONG, false,
              ServiceBase.OPT_PRINT_URL_DESCRIP ) );
        options.addOption( new Option( ServiceBase.OPT_PRINT_IOR,
              ServiceBase.OPT_PRINT_IOR_LONG, false,
              ServiceBase.OPT_PRINT_IOR_DESCRIP ) );
        options.addOption(  OptionBuilder.withArgName( ServiceBase.OPT_WRITE_IOR_FILE_ARG )
                            .hasOptionalArg()
                            .withLongOpt( ServiceBase.OPT_WRITE_IOR_FILE_LONG )
                            .withDescription( ServiceBase.OPT_WRITE_IOR_FILE_DESCRIP )
                            .create( ServiceBase.OPT_WRITE_IOR_FILE )
                            );
        options.addOption(  OptionBuilder.withArgName( ServiceBase.OPT_WRITE_URL_FILE_ARG )
                            .hasOptionalArg()
                            .withLongOpt( ServiceBase.OPT_WRITE_URL_FILE_LONG )
                            .withDescription( ServiceBase.OPT_WRITE_URL_FILE_DESCRIP )
                            .create( ServiceBase.OPT_WRITE_URL_FILE )
                            );
        options.addOption( new Option( ServiceBase.OPT_DEFAULT_CS,
              ServiceBase.OPT_DEFAULT_CS_LONG, false,
              ServiceBase.OPT_DEFAULT_CS_DESCRIP ) );
        options.addOption(  OptionBuilder.withArgName( ServiceBase.OPT_NOBIND_ARG )
                            .hasArg()
                            .withLongOpt( ServiceBase.OPT_NOBIND_LONG )
                            .withDescription( ServiceBase.OPT_NOBIND_DESCRIP )
                            .create( ServiceBase.OPT_NOBIND )
                            );
        return options;
    }


    /**
     * Parse the command line options.
     *
     * @param args The command line arguments array.
     * @param opts The service specific options.
     * @param svc_params The service parameters to be filled in
     * @return The parsed command line.
     */
    private CommandLine parseArguments( String[] args,
                                        Options opts,
                                        Parameters svc_params )
    {
        // build a collection of command line options
        Options options = createCmdineOptions( opts );

        CommandLine line = null;
        try
        {
            CommandLineParser parser = new GnuParser();
            // parse the command line arguments
            line = parser.parse( options, args );
        }
        catch ( UnrecognizedOptionException ex )
        {
            System.err.println( ex.getMessage() + "\n" );
            printUsage( options, 2 );
        }
        catch ( MissingArgumentException ex )
        {
            System.err.println( "\nMissing argument: " + ex.getMessage() + "\n" );
            printUsage( options, 2 );
        }
        catch ( ParseException ex )
        {
            System.err.println( "Unexpected exception occured: " + ex + "\n" );
            System.exit( 2 );
        }

        if ( line != null )
        {
            // handle arguments and exit the VM
            if ( line.hasOption( ServiceBase.OPT_HELP ) )
            {
                printUsage( options, 1 );
            }
            else if ( line.hasOption( ServiceBase.OPT_VERSION ) )
            {
                System.out.println( getVersionString() );
                System.exit( 1 );
            }

            // handle the standard arguments
            handleStdArguments( line, svc_params, options );
        }

        return line;
    }


    /**
     * Handle the standard command-line arguments
     * @param svc_params parameters to fill in
     * @param options cmdline options
     * @param line command line args
     */
    private void handleStdArguments( CommandLine line,
                                     Parameters svc_params,
                                     Options options )
    {
        if ( line.hasOption( ServiceBase.OPT_DEBUG ) )
        {
            svc_params.setParameter( ServiceBase.OPT_DEBUG,
                                     line.getOptionValue( ServiceBase.OPT_DEBUG,
                                     "" ) );
        }
        if ( line.hasOption( ServiceBase.OPT_LOG_FILE ) )
        {
            svc_params.setParameter( ServiceBase.OPT_LOG_FILE,
                    line.getOptionValue( ServiceBase.OPT_LOG_FILE, "" ) );
        }
        svc_params.setParameter( ServiceBase.OPT_PRINT_URL,
              line.hasOption( ServiceBase.OPT_PRINT_URL )
              ? Boolean.TRUE.toString()
              : Boolean.FALSE.toString() );
        svc_params.setParameter( ServiceBase.OPT_PRINT_IOR,
              line.hasOption( ServiceBase.OPT_PRINT_IOR )
              ? Boolean.TRUE.toString()
              : Boolean.FALSE.toString() );
        if ( line.hasOption( ServiceBase.OPT_WRITE_IOR_FILE ) )
        {
            svc_params.setParameter( ServiceBase.OPT_WRITE_IOR_FILE,
              line.getOptionValue( ServiceBase.OPT_WRITE_IOR_FILE, "" ) );
        }
        if ( line.hasOption( ServiceBase.OPT_WRITE_URL_FILE ) )
        {
            svc_params.setParameter( ServiceBase.OPT_WRITE_URL_FILE,
              line.getOptionValue( ServiceBase.OPT_WRITE_URL_FILE, "" ) );
        }
        svc_params.setParameter( ServiceBase.OPT_DEFAULT_CS,
              line.hasOption( ServiceBase.OPT_DEFAULT_CS )
              ? Boolean.TRUE.toString()
              : Boolean.FALSE.toString() );

        // Default is to bind to the services (true)
        Boolean optBindNS = Boolean.TRUE;
        Boolean optBindCorbaloc = Boolean.TRUE;

        if ( line.hasOption( ServiceBase.OPT_NOBIND ) )
        {
            String[] values = line.getOptionValues( ServiceBase.OPT_NOBIND );
            for ( int ii = 0; ii < values.length; ii++ )
            {
                if ( values[ii].equalsIgnoreCase( "Corbaloc" ) )
                {
                    optBindCorbaloc = Boolean.FALSE;
                }
                else if ( values[ii].equalsIgnoreCase( "Naming" ) )
                {
                    optBindNS = Boolean.FALSE;
                }
                else // invalid service value
                {
                    System.err.println( "\nInvalid service for --"
                            + ServiceBase.OPT_NOBIND_LONG + " argument\n" );
                    printUsage( options, 2 );
                }
            }
        }

        svc_params.setParameter( ServiceBase.OPT_BIND_CORBALOC,
                  optBindCorbaloc.toString() );
        svc_params.setParameter( ServiceBase.OPT_BIND_NS,
                  optBindNS.toString() );
    }


    /**
     * Print the Service version info
     * @return version string
     */
    private String getVersionString()
    {
        return m_service.getLongName() + " "
            + m_service.getVersion() + " "
            + ORBUtils.COPYRIGHT;
    }


    /**
     * Prints the Cmdline Usage help and exits.
     * @param options cmdline options
     * @param exitCode exitCode to use in System.exit()
     */
    private void printUsage( Options options, int exitCode )
    {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        String name = "start " + this.getClass().getName();
        formatter.printHelp( name, options );

        System.exit( exitCode );
    }


    /**
     * Handle service specific command line options.
     * This method may be overridden by the deriving class in order
     * to add service specific command line option handling. The
     * standard options have already been added to the parameters object.
     * A typical line for adding a custom option is:
     * <pre>
     * params.setParameter( Service.OPT_MY_SPECIAL,
     *       Boolean.toString( cmdline.hasOption( Service.OPT_MY_SPECIAL ) ) );
     * </pre>
     *
     * @param cmdline The parsed command line options.
     * @param params The Parameters object of the service.
     */
    protected void handleArguments( CommandLine cmdline, Parameters params )
    {
        // do nothing here
    }

    /**
     * Create the ORB instance.
     * Unless already set in the properties, it will set the
     * org.omg.CORBA.ORBClass and org.omg.CORBA.ORBSingletonClass
     * properties to use OpenORB.
     *
     * @param args The ORB specific arguments.
     * @param props ORB properties. May be null.
     * @return ORB instance created
     */
    private ORB createORB( String[] args, Properties props )
    {
        getLogger().debug( "Initializing ORB instance" );

        Properties p = null;
        if ( props == null )
        {
            p = new Properties();
        }
        else
        {
            p = ( Properties ) props.clone();
        }

        // check whether we should use OpenORB or not
        if ( shallUseOpenORB( p ) )
        {
            // use OpenORB
            p.put( ORBUtils.ORB_CLASS_KEY, ORBUtils.OPENORB_ORB_CLASS );
            p.put( ORBUtils.ORB_SINGLETON_CLASS_KEY, ORBUtils.OPENORB_ORB_SINGLETON_CLASS );

            boolean bDefault = false;
            try
            {
                bDefault = m_svc_params.getParameterAsBoolean( ServiceBase.OPT_DEFAULT_CS );
            }
            catch ( ParameterException ex )
            {
                // the option was not set, bDefault is false then
            }
            // enable the OpenORB CorbalocService
            if ( bDefault )
            {
                p.put( "ImportModule.CorbalocService",
                      "${openorb.home}config/default.xml#DefaultCorbalocService" );
            }
            else
            {
                p.put( "ImportModule.CorbalocService",
                      "${openorb.home}config/default.xml#CorbalocService" );
            }
        }

        getLogger().info( "calling ORB.init" );
        getLogger().debug( "calling ORB.init" );
        return org.omg.CORBA.ORB.init( args, p );
    }

    /**
     * This method checks whether we should use OpenORB to run this service
     * or not.
     * It uses the following order for searching the ORB class name according
     * to the IDL to Java Mapping Spec 1.2 (formal/02-08-05), Section 1.21.9.
     * <ol>
     *   <li>Check in properties parameter, if any.</li>
     *   <li>Check in the System properties.</li>
     *   <li>Check in the <code>orb.properties</code> file, in either the
     *       the user home or Java home, if it exists.</li>
     * </ol>
     * If no ORB class is specified in the above order, this method returns
     * <code>true</code> to indicate that OpenORB shall be used as the default
     * ORB.
     */
    private boolean shallUseOpenORB( Properties props )
    {
        String orbclass = null;
        if ( props != null )
        {
            orbclass = props.getProperty( ORBUtils.ORB_CLASS_KEY );
        }
        if ( orbclass == null )
        {
            orbclass = getSystemProperty( ORBUtils.ORB_CLASS_KEY );
        }
        if ( orbclass == null )
        {
            orbclass = getPropertyFromFile( ORBUtils.ORB_CLASS_KEY );
        }

        // Check the ORB class name
        return ( orbclass == null
             || orbclass.length() == 0
             || orbclass.equals( ORBUtils.OPENORB_ORB_CLASS ) );
    }

    /**
     * This method retrieves the value of a particular system property.
     * It returns <code>null</code> if the property does not exist or
     * there is a security violation.
     */
    private String getSystemProperty( final String name )
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
    private String getPropertyFromFile( final String name )
    {
        File propFile = null;

        // Check if orb.properties exists in user home
        try
        {
            String userHome = System.getProperty( "user.home" );
            if ( userHome != null )
            {
                File userPropFile = new File( userHome + File.separator + "orb.properties" );
                if ( userPropFile.exists() )
                {
                    propFile = userPropFile;
                }
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
                if ( javaHome != null )
                {
                    File javaPropFile = new File( javaHome + File.separator + "lib"
                          + File.separator + "orb.properties" );
                    if ( javaPropFile.exists() )
                    {
                        propFile = javaPropFile;
                    }
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
        Properties file_props = new Properties();
        try
        {
            FileInputStream fis = new FileInputStream( propFile );
            try
            {
                file_props.load( fis );
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
        return file_props.getProperty( name );
    }

    /**
     * Create the service instance.  Note that this method derives the
     * name of the service class from the server class name (i.e.
     * "notify.Server" => "notify.Service") unless a specific service
     * class name is specified via the 'setServiceClassName()' method.
     *
     * @return The created service instance.
     */
    private ServiceBase createService()
    {
        ServiceBase result = null;
        try
        {
            if ( m_serviceClassNameStr == null || m_serviceClassNameStr.length() <= 0 )
            {
                // service class name not previously specified
                String clzName = this.getClass().getName();

                // get the package prefix for the Service class
                int dot = clzName.lastIndexOf( '.' );

                // build the class name using the package prefix
                m_serviceClassNameStr = clzName.substring( 0, dot ) + ".Service";
            }

            // get the class
            Class clzService = Thread.currentThread().getContextClassLoader().loadClass(
                    m_serviceClassNameStr );

            // create the service instance
            result = ( ServiceBase ) clzService.newInstance();
        }
        catch ( Exception ex )
        {
            throw new CascadingRuntimeException( "The creation of the service failed", ex );
        }
        return result;
    }

    /**
     * LogEnable, contextualize, parameterize and initialize the instance.
     *
     * @param svc The service to run though the Avalon lifecycle.
     * @param context The Context to use in the contextualize operation.
     * @param params The Parameters object to use in parameterize().
     */
    private void initService( ServiceBase svc, ServiceContext context, Parameters params )
    {
        try
        {
            getLogger().debug( "Invoking preInitService" );
            preInitService( context, params );
        }
        catch ( Exception ex )
        {
            getLogger().error( "Pre-initialization of the service failed!", ex );
            throw new CascadingRuntimeException( "Pre-initialization of the service failed", ex );
        }

        getLogger().debug( "Initializing the service instance" );
        try
        {
            getLogger().debug( "LogEnabling the service instance" );
            svc.enableLogging( getLogger().getChildLogger( "svc" ) );

            getLogger().debug( "Contextualizing the service instance" );
            context.makeReadOnly();
            svc.contextualize( context );

            getLogger().debug( "Parameterizing the service instance" );
            params.makeReadOnly();
            svc.parameterize( params );

            getLogger().debug( "Initializing the service instance" );
            svc.initialize();
        }
        catch ( Exception ex )
        {
            getLogger().error( "Creation of the service failed!", ex );
            throw new CascadingRuntimeException( "The creation of the service failed", ex );
        }
    }

    /**
     * Install the shutdown hook for shuttding down the ORB and start the ORB's
     * dispatch loop.
     */
    public final void run()
    {
        // check whether the server is initialized
        if ( !m_initialized )
        {
            getLogger().error( "The server has not been initialized yet!" );
            throw new IllegalStateException( "The server has not been initialized yet" );
        }

        getLogger().debug( "Starting the service" );
        try
        {
            // start the service
            m_service.start();

            // install the shutdown hook for shutting down the orb
            Thread srvShutdownHook = new ServerShutdownHook( m_orb, m_service,
                    ( m_isFileLogging ? null : getLogger() ) );
            Runtime rt = Runtime.getRuntime();
            rt.addShutdownHook( srvShutdownHook );

            // run the ORB dispatch loop
            consolePrintln( "Service started. Press CTRL-C to stop the service!" );
            m_orb.run();
        }
        catch ( Exception ex )
        {
            getLogger().error( "Start of the service failed!", ex );
            throw new CascadingRuntimeException( "The start of the service failed", ex );
        }
    }

    /**
     * Returns the ORB used by the Service.
     * @return ORB
     */
    public ORB getORB()
    {
        return m_orb;
    }

    /**
     * Returns the ServiceContext.
     * Not available until after Server.init() has been invoked.
     * @return service context
     */
    public ServiceContext getServiceContext()
    {
        if ( !m_initialized )
        {
            throw new IllegalStateException( "not initialized" );
        }

        return m_svc_ctx;
    }


    /**
     * Returns the Parameters used by the Service.
     * Not available until after Server.init() has been invoked.
     * @return parameters
     */
    public Parameters getServiceParameters()
    {
        if ( !m_initialized )
        {
            throw new IllegalStateException( "not initialized" );
        }

        return m_svc_params;
    }


    /**
     * Specifies the name of the service class to be used.  This can useful
     * if the service class name cannot be derived from the server class
     * name.  This method needs to be called before the 'init()' method.
     *
     * @param classNameStr the name of the service class to use.
     */
    public void setServiceClassName( String classNameStr )
    {
        m_serviceClassNameStr = classNameStr;
    }


    /**
     * Service pre-initialization hook.
     * This method can be overridden to provide any necessary
     * configuration or customization before the Service is
     * initialized (i.e. logEnabled, contextualized, paramterized,
     * and initialized).
     * @param context service context
     * @param params parameters
     */
    protected void preInitService( ServiceContext context, Parameters params )
    {
    }


    /**
     * Ensures the message is displayed on System.out
     * @param msg message to print
     */
    protected void consolePrintln( String msg )
    {
        boolean wasLogged = false;

        // always log it
        if ( m_logger != null && m_logger.isInfoEnabled() )
        {
            getLogger().info( msg );
            wasLogged = true;
        }

        // output to console if we are logging to a file
        // OR the logging priority would prevent it from displaying
        if ( m_isFileLogging || !wasLogged )
        {
            // print to the console
            System.out.println( msg );
        }
    }


    /**
     * The shutdown hook for taking down the service instance when
     * the VM is going down.
     */
    private static class ServerShutdownHook
        extends Thread
    {
        private ORB m_orb;
        private ServiceBase m_svc;
        private Logger m_logger;

        /**
         * Constructor.
         *
         * @param orb An orb instance that is shutdown when the hook is invoked.
         * @param svc The service that is to be stopped/disposed when the hook is invoked.
         * @param logger Logger to use
         */
        public ServerShutdownHook( ORB orb, ServiceBase svc, Logger logger )
        {
            m_orb = orb;
            m_svc = svc;
            m_logger = logger;
        }

        /**
         * Stops and disposes the service and then shuts down the ORB.
         */
        public void run()
        {
            if ( m_logger != null )
            {
                m_logger.info( "Stopping the service." );
            }
            else
            {
                System.out.println( "Stopping the service." );
            }

            try
            {
                // stop the service instance
                m_svc.stop();
            }
            catch ( Exception ex )
            {
                // do nothing, just clean-up
            }

            // dispose the service instance
            m_svc.dispose();

            // shutdown the orb
            m_orb.shutdown( true );
        }
    }
}
