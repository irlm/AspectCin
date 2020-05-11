/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.doc;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
import java.util.List;

import org.openorb.compiler.CompilerProperties;
import org.openorb.compiler.IdlCompiler;
import org.openorb.compiler.doc.html.IdlToHTML;
import org.openorb.compiler.doc.rtf.IdlToRTF;
import org.openorb.compiler.object.IdlObject;
import org.openorb.compiler.parser.IdlParser;

/**
 * This class is the IDL parser for generating documentations.
 */
public class IdlDoc
{
    private static final String IDL2JAVA_COMPILER = "IDL Documentation Tool";

    /**
     * IDL file name
     */
    public static String [] idl_file_name;

    /**
     * Generated package name
     */
    public static String packageName = null;

    /**
     * Indicates if the package name is used
     */
    public static boolean use_package = true;

    /**
     * Indicates the output directory
     */
    public static String outdir = null;

    /**
     * Stylesheet to use
     */
    public static boolean stylesheet = false;

    /**
     * Is the target HTML file
     */
    public static boolean htmlDoc = false;

    /**
     * Documentation title
     */
    public static String title = "IDL Documentation";
    /**
     * Documentation header
     */
    public static String header = "";

    /**
     * Documentation footer
     */
    public static String footer = "";

    /**
     * Doc file name
     */
    public static String filename = "";

    /**
     * Doc codepage for html files.
     */
    public static String codepage = "ISO-8859-1";

    /**
     * Indicated of the prefixes are used
     */
    public static boolean usePrefix = true;

    /**
     * Print help
     */
    public static void printUsage()
    {
        System.out.println( "Usage: java org.openorb.compiler.doc.IdlDoc [Options] idl-files..." );
        System.out.println( "Options:" );
        System.out.println( "--------" );
        System.out.println( "  -html" );
        System.out.println( "              Create HTML output" );
        System.out.println( "  -rtf" );
        System.out.println( "              Create RTF output" );
        System.out.println( "  -outdir <dir>" );
        System.out.println( "              Provide a way to specify the ouput dir. This option" );
        System.out.println( "              will not use the corba_pkg directory." );
        System.out.println( "              Example:" );
        System.out.println( "                idldoc -html demo.idl -outdir:/home/me/" );
        System.out.println( "  -nopackage" );
        System.out.println( "              Don't use a package name" );
        System.out.println( "  -package <package_name>" );
        System.out.println( "              Generate files in package_name" );
        System.out.println( "              Example:" );
        System.out.println( "                idldoc -html demo.idl -package:exemple" );
        System.out.println( "  -I" );
        System.out.println( "              Allow specification of include directory" );
        System.out.println( "              Example:" );
        System.out.println( "                idldoc -html demo.idl -I/home/me/idl -I../other" );
        System.out.println( "  -D" );
        System.out.println( "              Define a symbol. It is equivalent to #define" );
        System.out.println( "  -all" );
        System.out.println( "              Generate documentation for included files." );
        System.out.println( "  -stylesheet ( for HTML documentation only )" );
        System.out.println( "              Generate a style sheet for HTML documentation." );
        System.out.println( "  -title <name>" );
        System.out.println( "              Set the documentation title." );
        System.out.println( "              Example:" );
        System.out.println( "                idldoc -html demo.idl \"-title:My Doc Title\"" );
        System.out.println( "  -codepage:<name>" );
        System.out.println( "              Set the codepage for the generated html files." );
        System.out.println( "              Example:" );
        System.out.println( "                idldoc -html demo.idl -codepage:ISO-8859-5" );
        System.out.println( "  -noprefix" );
        System.out.println( "              Disable usage of prefix for package name." );
        System.out.println( "  -header ( for RTF documentation only ) :" );
        System.out.println( "              Set the documentation header." );
        System.out.println( "              Example:" );
        System.out.println( "                idldoc -rtf demo.idl \"-header:My Doc header\"" );
        System.out.println( "  -footer ( for RTF documentation only ) :" );
        System.out.println( "              Set the documentation footer." );
        System.out.println( "              Example:" );
        System.out.println( "                idldoc -rtf demo.idl \"-footer:My Doc footer\"" );
        System.out.println( "  -docname:<name>" );
        System.out.println( "              Set the documentation file name for RTF documentation." );
        System.out.println( "              Example:" );
        System.out.println( "                idldoc -rtf demo.idl -docname:mydoc.rtf" );
        System.exit( 0 );
    }

    /**
    * Scan commang line args
    *
    * @param args  args list
    */
    public static void analyse_arguments( String[] args, CompilerProperties cp )
    {

        cp.setM_includeList(new Vector());
        cp.setM_macros(new Hashtable());

        List idlFileNameList = new java.util.Vector();

        for ( int i = 0; i < args.length; i++ )
        {
            if ( args[ i ].charAt( 0 ) != '-' )
            {
                idlFileNameList.add( args[ i ] );
            }
            else if ( args[i].equals( "-h" ) )
            {
                printUsage();
            }
            else if ( args[i].equals( "-html" ) )
            {
                            htmlDoc = true;
            }
            else if ( args[i].equals( "-help" ) )
            {
                printUsage();
            }
            else if ( args[i].equals( "-nopackage" ) )
            {
                use_package = false;
            }
            else if ( args[i].equals( "-noprefix" ) )
            {
                usePrefix = false;
            }
            else if ( args[i].equals( "-stylesheet" ) )
            {
                stylesheet = true;
            }
            else if ( args[i].equals( "-all" ) )
            {
                cp.setM_map_all(true);
            }
            else if ( args[i].startsWith( "-package:" ) )
            {
                try
                {
                    packageName = args[ i ].substring( 9, args[ i ].length() );
                }
                catch ( StringIndexOutOfBoundsException ex )
                {
                }

                if ( packageName.equals( "" ) )
                {
                    System.out.println( "Package name cannot be empty..." );
                    System.exit( 0 );
                }
            }
            else if ( args[i].startsWith( "-title:" ) )
            {
                try
                {
                    title = args[ i ].substring( 7, args[ i ].length() );
                }
                catch ( StringIndexOutOfBoundsException ex )
                { }

            }
            else if ( args[i].startsWith( "-header:" ) )
            {
                try
                {
                    header = args[ i ].substring( 7, args[ i ].length() );
                }
                catch ( StringIndexOutOfBoundsException ex )
                { }

            }
            else if ( args[i].startsWith( "-footer:" ) )
            {
                try
                {
                    footer = args[ i ].substring( 7, args[ i ].length() );
                }
                catch ( StringIndexOutOfBoundsException ex )
                {
                }
            }
            else if ( args[i].startsWith( "-docname:" ) )
            {
                try
                {
                    filename = args[ i ].substring( 9, args[ i ].length() );
                }
                catch ( StringIndexOutOfBoundsException ex )
                {
                }

            }
            else if ( args[i].startsWith( "-outdir:" ) )
            {
                try
                {
                    outdir = args[ i ].substring( 8, args[ i ].length() );
                }
                catch ( StringIndexOutOfBoundsException ex )
                {
                }

                if ( packageName == null )
                {
                    packageName = "";
                    use_package = false;
                }

            }
            else if ( args[i].startsWith( "-codepage:" ) )
            {
                try
                {
                    codepage = args[ i ].substring( 10, args[ i ].length() );
                }
                catch ( StringIndexOutOfBoundsException ex )
                {
                }
            }
            else if ( args[i].startsWith( "-I" ) )
            {
                try
                {
                    String name = args[ i ].substring( 2, args[ i ].length() );
                    URL url = null;

                    try
                    {
                        url = new URL( name );
                    }
                    catch ( MalformedURLException ex )
                    {
                        try
                        {
                            url = new java.io.File( name ).toURL();
                        }
                        catch ( MalformedURLException ex1 )
                        {
                        }
                    }

                    if ( url != null )
                    {
                        cp.getM_includeList().addElement( url );
                    }
                }
                catch ( StringIndexOutOfBoundsException ex )
                {
                }
            }
            else if ( args[i].startsWith( "-D" ) )
            {
                try
                {
                    int idx = args[ i ].indexOf( '=' );

                    if ( idx < 0 )
                    {
                        cp.getM_macros().put( args[ i ].substring( 2, args[ i ].length() ) , "" );
                    }
                    else
                    {
                        cp.getM_macros().put( args[ i ].substring( 2, idx ) , args[ i ].substring( idx + 1 ) );
                    }
                }
                catch ( StringIndexOutOfBoundsException ex )
                {
                }

            }
            else
            {
                System.out.println( "Bad parameter: '" + args[i] + "'" );
                System.out.println( "Please, use no flag to display all compiler option flags" );
                System.exit( 0 );
            }
        }

        idl_file_name = new String[idlFileNameList.size()];
        idlFileNameList.toArray(idl_file_name);

        if ( packageName == null )
        {
            packageName = "corba_pkg";
        }

        cp.setM_packageName(packageName);

    }

    /**
     * The compiler starts here
     *
     * @param args  commmand line args
     */
    public static void main( String[] args )
    {
        IdlObject CompilationGraph;
        IdlParser Parser = null;
        CompilerProperties cp = new CompilerProperties();
        // Scan the args
        if ( args.length != 0 )
        {
            analyse_arguments( args, cp );
        }
        else
        {
            printUsage();
        }
        IdlCompiler comp = new IdlCompiler();

        if ( idl_file_name.length == 0 )
        {
            printUsage();
        }
        Parser = new IdlParser( cp );
        System.out.println( "OpenORB Doc generator" );

        // Compile
        CompilationGraph = null;
        for ( int i = 0; i < idl_file_name.length; i++ )
        {
            try
            {
                CompilationGraph = Parser.compile_idl( idl_file_name[ i ] );
            }
            catch (FileNotFoundException e)
            {
                System.err.println(e.toString());
            }
        }
        if ( Parser.getTotalErrors() != 0 )
        {
            System.out.println( "there are errors..." );
            System.out.println( "compilation process stopped !" );
            System.exit( 0 );
        }

        // HTML generation
        if ( htmlDoc )
        {
            System.out.println( "IDL To HTML" );
            IdlToHTML toHTML = new IdlToHTML( cp );
            toHTML.translateToHTML( CompilationGraph, title, packageName );
        }
        else
        {
            System.out.println( "IDL To RTF" );
            IdlToRTF toRTF = new IdlToRTF();

            if ( filename == null )
            {
                System.out.println( "IDL to RTF fatal error : you must specify"
                      + " an file name for the RTF documentation with '-docname:' flag." );
                System.exit( 0 );
            }
            toRTF.translateToRTF( CompilationGraph, filename, title, header, footer );
        }
    }
}

