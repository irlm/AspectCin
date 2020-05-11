/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.omg.CORBA.ORB;


/**
 * Contains common methods used by OpenORB modules.
 *
 * @author Michael Rumpf
 * @author Shawn Boyce
 */
public abstract class ORBUtils
{
    /**
     * Utility class. Do not instantiate.
     */
    private ORBUtils()
    {
    }

    /** CORBA ORBClass property key */
    public static final String ORB_CLASS_KEY               = "org.omg.CORBA.ORBClass";
    /** CORBA ORBSingletonClass property key */
    public static final String ORB_SINGLETON_CLASS_KEY     = "org.omg.CORBA.ORBSingletonClass";

    /** OpenORB ORBClass value */
    public static final String OPENORB_ORB_CLASS           = "org.openorb.orb.core.ORB";
    /** OpenORB ORBSingleton value */
    public static final String OPENORB_ORB_SINGLETON_CLASS = "org.openorb.orb.core.ORBSingleton";


    //
    // ORB Initialization
    //

    /** ORB Initialization options. */
    public static final String ORB_ARG_PREFIX = "-ORB";
    /** CORBA 3.0 init flag options (without a value). */
    public static final String[] ORB_FLAG_ARGS = new String[]
        {
            "-ORBNoProprietaryActivation"
        };
    private static final Vector ORB_FLAG_ARG_LIST = new Vector( ORB_FLAG_ARGS.length );

    /**
     * CORBA 3.0 init options with an additional value. Syntax:
     * <ul>
     *   <li>&quot;-ORB&lt;KEY&gt; &lt;VALUE&gt;&quot;,</li>
     *   <li>&quot;-ORB&lt;KEY&gt;&lt;VALUE&gt;&quot;. </li>
     * </ul>
     * General: &quot;-ORB&lt;suffix&gt;&lt;optional white space&gt;&lt;value&gt;
     */
    public static final String[] ORB_VALUE_ARGS = new String[]
        {
            "-ORBPort",
            "-ORBid",
            "-ORBServerid",
            "-ORBListenEndpoints",
            "-ORBInitRef",
            "-ORBDefaultInitRef"
        };
    private static final Vector ORB_VALUE_ARG_LIST = new Vector( ORB_VALUE_ARGS.length );


    //
    // Bootstrap
    //

    /** Property for OpenORB home directory */
    private static final String PROPERTY_OO_HOME = "openorb.home.path";

    /** Property for user's home directory */
    private static final String PROPERTY_USER_HOME = "user.home";

    /** System property for the temp folder. */
    private static final String PROPERTY_TMP_DIR = "java.io.tmpdir";

    /** openorb temp folder name. */
    private static final String OO_TMP_DIRNAME   = ".OpenORB";


    /** File extension of IOR files. */
    public static final String IOR_FILE_EXT = ".ior";

    /** File extension of URL files. */
    public static final String URL_FILE_EXT = ".url";

    //
    // Service naming constants
    //

    /* Versions */
    public static final int MAJOR                           = 1;
    public static final int MINOR                           = 4;
    public static final int BUGFIX                          = 0;
    public static final String VERSION                      = MAJOR + "." + MINOR + "." + BUGFIX;

    /* Global names */
    public static final String NAME                         = "The Community OpenORB";
    public static final String YEAR                         = "2002-2005";
    public static final String COPYRIGHT                    = "Copyright (c) " + YEAR + " " + NAME;

    /** OpenORB package prefix */
    public static final String OPENORB_PKG_PREFIX           = "org.openorb";


    /** Sync object for hash maps. */
    private static final Object SYNC_MAP = new Object();

    /** Resolve initial references map. */
    private static final HashMap RIR_MAP = new HashMap();

    /** Short name to long name map. */
    private static final HashMap SHORT_LONG_MAP = new HashMap();

    /** Short name to version map. */
    private static final HashMap VERSION_MAP = new HashMap();



    //
    // Methods
    //

    /** Initialize some statics. */
    static
    {
        for ( int i = 0; i < ORB_FLAG_ARGS.length; i++ )
        {
           ORB_FLAG_ARG_LIST.add( ORB_FLAG_ARGS[ i ] );
        }

        for ( int i = 0; i < ORB_VALUE_ARGS.length; i++ )
        {
           ORB_VALUE_ARG_LIST.add( ORB_VALUE_ARGS[ i ] );
        }
    }

    /**
     * Check whether the ORB init option is a
     * single flag argument.
     *
     * @param arg The argument to check.
     * @return True when the arg is a single flag argument, false otherwise.
     */
    public static boolean isFlagArg( String arg )
    {
        return ORB_FLAG_ARG_LIST.contains( arg );
    }

    /**
     * Check whether the ORB init option has an
     * additional value argument.
     *
     * @param arg The argument to check.
     * @return True when the arg has an additional value, false otherwise.
     */
    public static boolean isValueArg( String arg )
    {
        return ORB_VALUE_ARG_LIST.contains( arg );
    }

    /**
     * Extract ORB arguments from the command line option array.
     *
     * @param args The original argument array.
     * @return An array with options that all start with the "-ORB" prefix.
     */
    public static String[] extractORBArgs( String[] args )
    {
        Vector v = new Vector( args.length );
        for ( int i = 0; i < args.length; i++ )
        {
            if ( args[ i ].startsWith( ORB_ARG_PREFIX ) )
            {
                v.add( args[ i ] );
                if ( isValueArg( args[ i ] ) )
                {
                    // test for ArrayIndexOutOfBounds
                    if ( i + 1 <= args.length )
                    {
                        v.add( args[ ++i ] );
                    }
                }
            }
        }
        return ( String[] ) v.toArray( new String[ v.size() ] );
    }

    /**
     * Extract non ORB arguments from the command line option array.
     *
     * @param args The original argument array.
     * @return An array without options that start with the "-ORB" prefix.
     */
    public static String[] extractNonORBArgs( String[] args )
    {
        Vector v = new Vector( args.length );
        for ( int i = 0; i < args.length; i++ )
        {
            if ( args[ i ].startsWith( ORB_ARG_PREFIX ) )
            {
                if ( isValueArg( args[ i ] ) )
                {
                    i++;
                }
            }
            else
            {
                v.add( args[ i ] );
            }
        }
        return ( String[] ) v.toArray( new String[ v.size() ] );
    }


    /**
     * Extract the short name from the class name.
     *
     * @param clz The OpenORB class from which to extract the short name.
     * @return The short name when the class was an OpenORB class, null otherwise.
     */
    public static String getShortNameFromClass( Class clz )
    {
        String result = null;
        if ( clz != null && clz.getName().startsWith( OPENORB_PKG_PREFIX ) )
        {
            int skip = OPENORB_PKG_PREFIX.length() + 1;
            String clzName = clz.getName();
            int len = clzName.length();
            String suffix = clzName.substring( skip, len );

            // get the package prefix for the Service class
            int dot = suffix.indexOf( '.' );
            result = suffix.substring( 0, dot );
        }
        return result;
    }

    /**
     * Provides a mapping between service short and their long names.
     *
     * @param short_name The short name of the service.
     * @return The long name of the service.
     */
    public static String getLongFromShortName( String short_name )
    {
        synchronized ( SYNC_MAP )
        {
            if ( SHORT_LONG_MAP.isEmpty() )
            {
                // special for NamingService: ins + tns
                SHORT_LONG_MAP.put( "ins",
                      NamingUtils.NS_NAME_LONG );
                SHORT_LONG_MAP.put( "tns",
                      NamingUtils.NS_NAME_LONG );

                SHORT_LONG_MAP.put( NamingUtils.CCS_NAME,
                      NamingUtils.CCS_NAME_LONG );
                SHORT_LONG_MAP.put( NamingUtils.EVENT_SERVICE_NAME,
                      NamingUtils.EVENT_SERVICE_NAME_LONG );
                SHORT_LONG_MAP.put( NamingUtils.IR_NAME,
                      NamingUtils.IR_NAME_LONG );
                SHORT_LONG_MAP.put( NamingUtils.NOTIFICATION_SERVICE_NAME,
                      NamingUtils.NOTIFICATION_SERVICE_NAME_LONG );
                SHORT_LONG_MAP.put( NamingUtils.PROPERTY_SERVICE_NAME,
                      NamingUtils.PROPERTY_SERVICE_NAME_LONG );
                SHORT_LONG_MAP.put( NamingUtils.TIME_SERVICE_NAME,
                      NamingUtils.TIME_SERVICE_NAME_LONG );
                SHORT_LONG_MAP.put( NamingUtils.TRADING_SERVICE_NAME,
                      NamingUtils.TRADING_SERVICE_NAME_LONG );
                SHORT_LONG_MAP.put( NamingUtils.OTS_NAME,
                      NamingUtils.OTS_NAME_LONG );
            }
        }
        return ( String ) SHORT_LONG_MAP.get( short_name );
    }

    /**
     * Provides a mapping between service short and the version number.
     *
     * @param short_name The short name of the service.
     * @return The version number of the service.
     */
    public static String getVersionFromShortName( String short_name )
    {
        synchronized ( SYNC_MAP )
        {
            if ( VERSION_MAP.isEmpty() )
            {
                // e.g. a special version for ccs
                //SHORT_LONG_MAP.put( CCS_NAME,
                //      "1.4.1" );
            }
        }
        String version = ( String ) VERSION_MAP.get( short_name );
        // do we have a special mapping, if not return the default version
        if ( version == null )
        {
            version = VERSION;
        }
        return version;
    }

    /**
     * This function checks an address for the correct format.
     *
     * @param addr The address to check.
     * @return True if the format is correct, false otherwise.
     */
    public static boolean checkAddress( String addr )
    {
        int end = addr.length();
        int start;
        do
        {
            start = addr.lastIndexOf( ",", end );
            if ( start < 0 )
            {
                start = 0;
            }
            int proto = addr.indexOf( ":", start );
            // parse the protocol
            if ( proto < 0 || proto > end )
            {
                return false;
            }
            String strProtocol = addr.substring( start + 1, proto );
            // only "iiop" is currently supported
            if ( strProtocol != null && strProtocol.length() > 0
                  && !strProtocol.equals( "iiop" ) )
            {
                return false;
            }
            int vers = addr.indexOf( "@", proto );
            if ( vers > 0 && vers < end )
            {
                // parse the version.
                int major = addr.indexOf( ".", proto );
                if ( major < 0 || major > vers )
                {
                    return false;
                }
                int min;
                int maj;
                try
                {
                    maj = Integer.parseInt( addr.substring( proto + 1, major ) );
                    min = Integer.parseInt( addr.substring( major + 1, vers ) );
                }
                catch ( NumberFormatException ex )
                {
                    return false;
                }
                if ( maj != 1 )
                {
                    return false;
                }
                if ( min < 0 || min > 2 /* 3 */ )
                {
                    return false;
                }
            }
            else
            {
                vers = proto;
            }
            int host = addr.indexOf( ":", vers + 1 );
            if ( host > 0 && host < end )
            {
                // parse the port
                int iPort;
                try
                {
                    iPort = Integer.parseInt( addr.substring( host + 1, end ) );
                }
                catch ( NumberFormatException ex )
                {
                    return false;
                }
                if ( iPort < 0 || iPort > 0xFFFF )
                {
                    return false;
                }
            }
            else
            {
                host = end;
            }
            // parse the host
            String strHost = addr.substring( vers + 1, host );
            if ( strHost == null || strHost.length() == 0 )
            {
                return false;
            }
            end = start;
        }
        while ( start > 0 );
        return true;
    }


    /**
     * Write the URL to a file.
     * File name will be object.url.
     * @param name name of the object
     * @param url url of the object
     */
    public static void writeURLToFile( String name, String url )
    {
        String fileName = name + URL_FILE_EXT;

        System.out.println( "Exporting URL into " + fileName + " file." );

        try
        {
            java.io.FileOutputStream file = new java.io.FileOutputStream( fileName );
            java.io.PrintWriter pfile = new java.io.PrintWriter( file );

            pfile.println( url );

            pfile.close();
        }
        catch ( java.io.IOException ex )
        {
            System.err.println( "Unable to generate " + fileName
                                + ": " + ex.getMessage() );
        }
    }

    /**
     * Write the IOR to a file.
     * File name will be name + ".ior"
     * @param orb ORB
     * @param name name of the object
     * @param obj object whose IOR to write
     */
    public static void writeIORToFile( ORB orb, String name, org.omg.CORBA.Object obj )
    {
        writeIORToFileName( orb, name + IOR_FILE_EXT, obj );
    }

    /**
     * Given the --writeIORFile argument, figure out what the IOR filename
     * should be.
     * @param arg can be either a directory or a full filename
     * @param name object name used to create default IOR filename
     * @return the end-result IOR filename. If 'arg' is a directory, then
     *   we'll return: arg + '/' + name + IOR_FILE_EXT
     */
    public static String getIORFileName( String arg, String name )
    {
        String fileName;
        if ( arg == null || arg.length() == 0 )
        {
            // not specified, default to 'name' + ".ior"
            fileName = name + IOR_FILE_EXT;
        }
        else
        {
            // figure out if they gave us a non-separator terminated directory, or
            // a specific filename...
            fileName = arg;
            File file = new File( fileName );
            if ( file.isDirectory() )
            {
                // it's a directory, append 'name' + ".ior" to it
                if ( !fileName.endsWith( File.separator ) )
                {
                    fileName += File.separator;
                }
                fileName += name + IOR_FILE_EXT;
            }

            // If 'arg' was not a directory, then we will treat it as a full filename,
            // and use it just like it was given to us...
        }

        return fileName;
    }

    /**
     * Write the IOR to a given fileName.
     * @param orb ORB
     * @param fileName file name
     * @param obj object whose IOR to write
     */
    public static void writeIORToFileName( ORB orb, String fileName, org.omg.CORBA.Object obj )
    {
        System.out.println( "Exporting IOR into " + fileName + " file." );

        try
        {
            java.io.FileOutputStream file = new java.io.FileOutputStream( fileName );
            java.io.PrintWriter pfile = new java.io.PrintWriter( file );

            pfile.println( orb.object_to_string( obj ) );

            pfile.close();
        }
        catch ( java.io.IOException ex )
        {
            System.err.println( "Unable to generate " + fileName
                                + ": " + ex.getMessage() );
        }
    }


    /**
     * Returns a temporary Directory.
     * The directory is created if it does not exist.
     * <p>
     * Tries the following locations in order:
     * <ol>
     * <li>System Property: OpenORB home directory</li>
     * <li>System Property: User home directory</li>
     * <li>Current Directory</li>
     * </ol>
     * @return File directory
     * @exception IOException occurs if unable to create temporary directory
     */
    public static File getTemporaryDir( String firstDirName,
           String subdirName )
            throws IOException
    {
        // try the first directory passed in the method
        if ( firstDirName != null )
        {
            String rootdirName = firstDirName;
            if ( rootdirName != null && rootdirName.length() != 0 )
            {
                File dir = makeOpenORBTempDirectory( rootdirName, subdirName );
                if ( dir != null )
                {
                    return dir;
                }
            }
        }

        // try openorb home directory
        String rootdirName = System.getProperty( PROPERTY_OO_HOME );
        if ( rootdirName != null && rootdirName.length() != 0 )
        {
            File dir = makeOpenORBTempDirectory( rootdirName, subdirName );
            if ( dir != null )
            {
                return dir;
            }
        }

        // try user's home directory
        rootdirName = System.getProperty( PROPERTY_USER_HOME );
        if ( rootdirName != null && rootdirName.length() != 0 )
        {
            File dir = makeOpenORBTempDirectory( rootdirName, subdirName );
            if ( dir != null )
            {
                return dir;
            }
        }

        // try the current directory
        rootdirName = ".";
        if ( rootdirName != null && rootdirName.length() != 0 )
        {
            File dir = makeOpenORBTempDirectory( rootdirName, subdirName );
            if ( dir != null )
            {
                return dir;
            }
        }

        throw new IOException( "unable to create OpenORB temporary directory:"
                               + subdirName );
    }


    /**
     * Make a temporary directory at specified root and subdirectory.
     *
     * @param rootdirName Root directory name to create.
     * @param subdirName Sub directory name to create; may be null.
     * @return File object for the full directory path.
     */
    private static File makeOpenORBTempDirectory( String rootdirName, String subdirName )
    {
        String fullname = rootdirName;

        if ( !rootdirName.endsWith( File.separator ) )
        {
            fullname += File.separator;
        }

        fullname += OO_TMP_DIRNAME;

        if ( subdirName != null )
        {
            fullname += File.separator + subdirName;

            if ( !subdirName.endsWith( File.separator ) )
            {
                fullname += File.separator;
            }
        }

        File dir = new File( fullname );
        if ( !dir.exists() )
        {
            boolean isSuccess = dir.mkdirs();
            if ( !isSuccess )
            {
                return null;
            }
        }

        return dir;
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
    public static ORB createORB( String[] args, Properties props )
    {
        Properties p = null;
        if ( props == null )
        {
            p = new Properties();
        }
        else
        {
            p = ( Properties ) props.clone();
        }

        // check whether we should run on another ORB or OpenORB
        String orbclass = ( String ) p.get( ORBUtils.ORB_CLASS_KEY );
        if ( orbclass == null || orbclass.length() == 0 )
        {
            /* true if OpenORB ORB class is available */
            boolean haveOpenORBClass = false;

            try
            {
                haveOpenORBClass =
                    Thread.currentThread().getContextClassLoader().loadClass(
                    OPENORB_ORB_CLASS ) != null;
            }
            catch ( ClassNotFoundException ex )
            {
                haveOpenORBClass = false;
            }


            if ( haveOpenORBClass )
            {
                // use OpenORB
                p.put( ORBUtils.ORB_CLASS_KEY, ORBUtils.OPENORB_ORB_CLASS );
                p.put( ORBUtils.ORB_SINGLETON_CLASS_KEY, ORBUtils.OPENORB_ORB_SINGLETON_CLASS );
            }
        }

        return org.omg.CORBA.ORB.init( args, p );
    }
}

