/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler;

import java.io.File;
import java.io.FileNotFoundException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.Iterator;

import org.openorb.compiler.generator.IdlToJava;

import org.openorb.compiler.object.IdlObject;

import org.openorb.compiler.parser.CompilationException;
import org.openorb.compiler.parser.IdlParser;

import org.openorb.util.ORBUtils;

/**
 * This class is the IDL compiler implementation.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.23 $ $Date: 2005/03/26 06:23:17 $
 */
public class IdlCompiler
    implements CompilerHost, CompilerIF
{
    private static final String IDL2JAVA_COMPILER = "IDL to Java Compiler";

    public static final String TAB = "    ";

    private CompilerHost m_ch = null;

    /**
     * Display bad flag
     */
    private boolean m_displayBadFlag = true;

    public IdlCompiler()
    {
        m_ch = this;
    }

    /**
     * Display a help message
     */
    public void display_help()
    {
        m_ch.display( "Usage: java org.openorb.compiler.IdlCompiler [Options] idl-files..." );
        m_ch.display( "Options:" );
        m_ch.display( "--------" );
        m_ch.display( "  -all" );
        m_ch.display( "              Generate mapping for included files." );

        m_ch.display( "  -boa" );
        m_ch.display( "              Generate skeleton for the BOA approach." );

        m_ch.display( "  -d <directory_name>" );
        m_ch.display( "              Provide a way to specify the ouput dir. This option" );
        m_ch.display( "              will not use the 'generated' directory." );
        m_ch.display( "              Example:" );
        m_ch.display( "                  org.openorb.compiler.IdlCompiler -d /home/me/ demo.idl" );

        m_ch.display( "  -dynamic    Generate stub with DII and skeleton with DSI" );
        m_ch.display( "              ( portable way before CORBA 2.3 )." );

        m_ch.display( "  -D <symbol>" );
        m_ch.display( "              Define a symbol. It is equivalent to #define." );

        m_ch.display( "  -importLink <link>" );
        m_ch.display( "              ???" );

        m_ch.display( "  -I <include_folder" );
        m_ch.display( "              Allow specification of include directory." );
        m_ch.display( "              Example:" );
        m_ch.display( "                  org.openorb.compiler.IdlCompiler"
              + " -I /home/me/idl demo.idl" );

        m_ch.display( "  -native <native_name> <native_mapping>" );
        m_ch.display( "              Define native type mapping." );
        m_ch.display( "              Example:" );
        m_ch.display( "                  org.openorb.compiler.IdlCompiler" );
        m_ch.display( "                      -native cookie java.lang.Object demo.idl" );
        m_ch.display( "                  This command implies the mapping of cookie into" );
        m_ch.display( "                  java.lang.Object." );

        m_ch.display( "  -nolocalstub" );
        m_ch.display( "              Generate stubs without local invocation path." );

        m_ch.display( "  -noprefix" );
        m_ch.display( "              Don't use prefixes as packages names." );

        m_ch.display( "  -noreverseprefix" );
        m_ch.display( "              The prefixes are used as package name but they are not"
              + " reversed." );

        m_ch.display( "  -noskeleton" );
        m_ch.display( "              Don't generate skeleton." );

        m_ch.display( "  -nostub" );
        m_ch.display( "              Don't generate stub." );

        m_ch.display( "  -notie" );
        m_ch.display( "              Don't generate TIE classes for delegation mode." );

        m_ch.display( "  -package <package_name>" );
        m_ch.display( "              Generate files in package_name." );
        m_ch.display( "              Example:" );
        m_ch.display( "              org.openorb.compiler.IdlCompiler -package example demo.idl" );

        m_ch.display( "  -pidl" );
        m_ch.display( "              ???" );

        m_ch.display( "  -portablehelper" );
        m_ch.display( "              Generate portable helper classes. Nonportable helpers"
              + " must be" );
        m_ch.display( "              compiled using OpenORB, but the generated class files"
              + " will work" );
        m_ch.display( "              anywhere. Portable helpers can be compiled anywhere"
              + " and will" );
        m_ch.display( "              work identically on all orbs with Any.extract_Streamable." );

        m_ch.display( "  -quiet" );
        m_ch.display( "              Suppress any output. Same as -silence." );

        m_ch.display( "  -silence" );
        m_ch.display( "              Suppress any output. Same as -quiet." );

        m_ch.display( "  -verbose" );
        m_ch.display( "              Show debug output." );

        m_ch.display( "  -jdk1.4" );
        m_ch.display( "              Generate classes that use JDK1.4 features." );
        m_ch.display( "              The generate classes will not compile on previous versions." );

        m_ch.display( "  -invokeMethod <Classes|Reflection|Switch>" );
        m_ch.display( "              The method used to implement the xxxPOA invoke method." );
        m_ch.display( "              If not specified Classes is used." );

        m_ch.display( "  -minTableSize <size>" );
        m_ch.display( "              The minimum size of method table." );

        m_ch.display( "  -retainPossibleCause" );
        m_ch.display( "              Add extra instrumentation to generated classes to"
              + " ensure that" );
        m_ch.display( "              possible causes are retained. Debugging option:"
              + " non portable." );

        m_ch.display( "  -XgenerateValueFactory <impl_postfix>" );
        m_ch.display( "              Generate default value factories for valuetypes." );
        m_ch.display( "              Creates instance of <valuetype name><impl postfix>." );

        m_ch.display( "  -XgenerateValueImpl <postfix>" );
        m_ch.display( "              Generatae default implementation of valuetypes." );
        m_ch.display( "              Creates classes with name <valuetype name><postfix>." );
    }

    /**
     * Scan command line arguments
     */
    public void scan_args( String[] args, CompilerProperties cp )
    {
        for ( int i = 0; i < args.length; i++ )
        {
            if ( args[ i ].charAt( 0 ) != '-' )
            {
                CompileListEntry cle = new CompileListEntry( args[i] );
                cp.getM_compileList().add( cle );
            }
            else
            {
                if ( args[ i ].equals( "-h" ) || args[ i ].equals( "-help" ) )
                {
                    display_help();
                    System.exit( 1 );
                }
                else if ( args[ i ].equals( "-silence" ) || args[ i ].equals( "-quiet" ) )
                {
                    cp.setM_silentMode( true );
                }
                else if ( args[ i ].equals( "-verbose" ) )
                {
                    cp.setM_verbose( true );
                }
                else if ( args[ i ].equals( "-noprefix" ) )
                {
                    cp.setM_usePrefix( false );
                }
                else if ( args[ i ].equals( "-noreverseprefix" ) )
                {
                    cp.setM_reversePrefix( false );
                }
                else if ( args[ i ].equals( "-nostub" ) )
                {
                    cp.setM_map_stub( false );
                }
                else if ( args[ i ].equals( "-nolocalstub" ) )
                {
                    cp.setM_local_stub( false );
                }
                else if ( args[ i ].equals( "-noskeleton" ) )
                {
                    cp.setM_map_skeleton( false );
                }
                else if ( args[ i ].equals( "-notie" ) )
                {
                    cp.setM_map_tie( false );
                }
                else if ( args[ i ].equals( "-portablehelper" ) )
                {
                    cp.setM_portableHelper( true );
                }
                else if ( args[ i ].equals( "-boa" ) )
                {
                    cp.setM_map_poa( false );
                }
                else if ( args[ i ].equals( "-dynamic" ) )
                {
                    cp.setM_dynamic( true );
                }
                else if ( args[ i ].equals( "-all" ) )
                {
                    cp.setM_map_all( true );
                }
                else if ( args[ i ].equals( "-pidl" ) )
                {
                    cp.setM_pidl( true );
                    cp.setM_map_stub( false );
                    cp.setM_map_skeleton( false );
                    cp.setM_map_tie( false );
                }
                else if ( "-jdk1.4".equals( args[ i ] ) )
                {
                    cp.setM_jdk1_4( true );
                }
                else if ( "-retainPossibleCause".equals( args[ i ] ) )
                {
                    cp.setM_retainPossibleCause( true );
                }
                else if ( "-XgenerateValueFactory".equals( args[ i ] ) )
                {
                    if ( ( i + 1 ) == args.length )
                    {
                        final String msg = "Argument expected after '-XgenerateValueFactory'";
                        System.out.println( msg );
                        throw new CompilerException( msg );
                    }
                    i++;
                    cp.setM_generateValueFactory( args[ i ] );
                }
                else if ( "-XgenerateValueImpl".equals( args[ i ] ) )
                {
                    if ( ( i + 1 ) == args.length )
                    {
                        final String msg = "Argument expected after '-XgenerateValueImpl'";
                        System.out.println( msg );
                        throw new CompilerException( msg );
                    }
                    i++;
                    cp.setM_generateValueImpl( args[ i ] );
                }
                else if ( "-invokeMethod".equals( args[ i ] ) )
                {
                    if ( ( i + 1 ) == args.length )
                    {
                        final String msg = "Argument expected after '-invokeMethod'";
                        System.out.println( msg );
                        throw new CompilerException( msg );
                    }
                    i++;
                    cp.setM_useReflection( false );
                    cp.setM_useSwitch( false );
                    cp.setM_useClasses( false );

                    if ( "Classes".equals( args[ i ] ) )
                    {
                        cp.setM_useClasses( true );
                    }
                    else if ( "Reflection".equals( args[i] ) )
                    {
                        cp.setM_useReflection( true );
                    }
                    else if ( "Switch".equals( args[ i ] ) )
                    {
                        cp.setM_useSwitch( true );
                    }
                    else
                    {
                        final String msg = "'-invokeMethod' support arguments: "
                              + "'Classes', 'Switch' and 'Reflection'";
                        System.out.println( msg );
                        throw new CompilerException( msg );
                    }
                }
                else if ( "-minTableSize".equals( args[ i ] ) )
                {
                    if ( i + 1 == args.length )
                    {
                        final String msg = "Argument expected after '-minTableSize'";
                        System.out.println( msg );
                        throw new CompilerException( msg );
                    }

                    cp.setM_minTableSize( Integer.parseInt( args[ ++i ] ) );
                }
                else if ( args[ i ].equals( "-package" ) )
                {
                    if ( i + 1 == args.length )
                    {
                        final String msg = "Argument expected after '-package'";
                        System.out.println( msg );
                        throw new CompilerException( msg );
                    }

                    cp.setM_packageName( args[ ++i ] );
                    cp.setM_use_package( false );
                }
                else if ( args[ i ].equals( "-importlink" ) )
                {
                    if ( i + 1 == args.length )
                    {
                        final String msg = "Argument expected after '-importlink'";
                        System.out.println( msg );
                        throw new CompilerException( msg );
                    }

                    cp.getM_importLink().addElement( args[ ++i ] );
                }
                else if ( args[ i ].equals( "-d" ) )
                {
                    if ( i + 1 == args.length )
                    {
                        final String msg = "Argument expected after '-d'";
                        System.out.println( msg );
                        throw new CompilerException( msg );
                    }

                    cp.setM_destdir( new File( args[ ++i ] ) );

                    if ( cp.getM_packageName() == null )
                    {
                        cp.setM_packageName( "" );
                        cp.setM_use_package( false );
                    }
                }
                else if ( args[ i ].equals( "-I" ) )
                {
                    if ( ++i == args.length )
                    {
                        final String msg = "Argument expected after '-I'";
                        System.out.println( msg );
                        throw new CompilerException( msg );
                    }

                    URL url = null;

                    try
                    {
                        url = new URL( args[ i ] );
                    }
                    catch ( MalformedURLException ex )
                    {
                        try
                        {
                            url = new java.io.File( args[ i ] ).toURL();
                        }
                        catch ( MalformedURLException ex1 )
                        {
                            // ignore
                        }
                    }
                    if ( url != null )
                    {
                        cp.getM_includeList().addElement( url );
                    }
                }
                else if ( args[ i ].startsWith( "-I" ) )
                {
                    String path = args[ i ].substring( 2 );
                    URL url = null;
                    try
                    {
                        url = new URL( path );
                    }
                    catch ( MalformedURLException ex )
                    {
                        try
                        {
                            url = new java.io.File( path ).toURL();
                        }
                        catch ( MalformedURLException ex1 )
                        {
                            // ignore
                        }
                    }
                    if ( url != null )
                    {
                        cp.getM_includeList().addElement( url );
                    }
                }
                else if ( args[ i ].startsWith( "-D" ) )
                {
                    try
                    {
                        int idx = args[ i ].indexOf( '=' );
                        if ( idx < 0 )
                        {
                            cp.getM_macros().put( args[ i ].substring( 2,
                                  args[ i ].length() ) , "" );
                        }
                        else
                        {
                            cp.getM_macros().put( args[ i ].substring( 2, idx ),
                                  args[ i ].substring( idx + 1 ) );
                        }
                    }
                    catch ( StringIndexOutOfBoundsException ex )
                    {
                        // ignore
                    }
                }
                else if ( args[ i ].equals( "-native" ) )
                {
                    if ( i + 2 == args.length )
                    {
                        final String msg = "Argument expected after '-native'";
                        System.out.println( msg );
                        throw new CompilerException( msg );
                    }
                    cp.getM_nativeDefinition().addElement( args[ ++i ] + ":" + args[ ++i ] );
                }
                else if ( m_displayBadFlag )
                {
                    final String msg = "Bad parameter: " + args[ i ]
                          + "\nPlease, use no flag to display all compiler option flags";
                    System.out.println( msg );
                    throw new CompilerException( msg );
                }
            }
        }

        if ( cp.getM_destdir() == null )
        {
            cp.setM_destdir( new File( "generated" ) );
            if ( cp.getM_packageName() == null )
            {
                cp.setM_packageName( "" );
                cp.setM_use_package( false );
            }
        }
    }

    /**
     * Get argument from configuration file.
     */
    public static void configFile( CompilerProperties cp )
    {
        // -- try to load the configurator --
        Configurator config = null;
        try
        {
            config = ( org.openorb.compiler.Configurator )
                  Thread.currentThread().getContextClassLoader().loadClass(
                  "org.openorb.compiler.orb.DefaultConfigurator" ).newInstance();
        }
        catch ( java.lang.Exception ex )
        {
            if ( cp.getM_verbose() )
            {
                ex.printStackTrace();
            }
            return;
        }

        // -- Invoke the configurator --
        config.updateInfo( cp.getM_includeList(), cp.getM_importLink() );
    }

    /**
     * @see org.openorb.compiler.CompilerIF#init_compiler
     */
    public void init_compiler( CompilerHost ch, CompilerProperties cp )
    {
        // define some standard macros
        cp.getM_macros().put( "__IDL_TO_JAVA__", "" );
        try
        {
            configFile( cp );
        }
        catch ( Throwable e )
        {
            final String warning =
              "Bypassing configuration file setup.\nReason:" + e.getMessage();
            System.err.println( warning );
        }
    }

    /**
     * Display a welcome message.
     */
    public void execute_compiler( CompilerHost ch, CompilerProperties cp )
    {
        if ( !cp.getM_silentMode() )
        {
            ch.display( IDL2JAVA_COMPILER + ", " + ORBUtils.COPYRIGHT );
            if ( cp.getM_verbose() )
            {
                ch.display( "ClassPath: " );
                URL[] ucp = ( ( URLClassLoader )
                      Thread.currentThread().getContextClassLoader() ).getURLs();
                if ( ucp != null )
                {
                    for ( int i = 0; i < ucp.length; i++ )
                    {
                        display( "   " + ucp[ i ].toExternalForm() );
                    }
                }
            }
        }
    }

    /**
     * This operation is used to compile an IDL file
     */
    public void compile_file( CompileListEntry cle , CompilerProperties cp )
        throws org.openorb.compiler.parser.CompilationException
    {
        File file = new File( cle.getSrcPath(), cle.getFileName() );
        IdlParser parser = new IdlParser( cp );

        IdlObject compilationGraph;

        try
        {
            compilationGraph = parser.compile_idl( file.getAbsolutePath() );
        }
        catch ( FileNotFoundException e )
        {
            throw new CompilationException( "File " + file + " does not exist" );
        }

        if ( parser.getTotalErrors() != 0 )
        {
            System.out.println( "there are errors..." );
            System.out.println( "compilation process stopped !" );
            throw new CompilationException();
        }

        // -- Start to generate Java code --
        if ( cp.getM_verbose() )
        {
            System.out.println( "Now translate to Java..." );
        }

        IdlToJava toJava = new IdlToJava( cp );
        toJava.translateData( compilationGraph, cp.getM_packageName() );

        if ( cp.getM_map_stub() )
        {
            toJava.translateStub( compilationGraph, cp.getM_packageName() );
        }

        if ( cp.getM_map_skeleton() )
        {
            toJava.translateSkeleton( compilationGraph, cp.getM_packageName() );
        }

        if ( cp.getM_map_tie() )
        {
            toJava.translateTIE( compilationGraph, cp.getM_packageName() );
        }
    }

    public CompilerProperties createEmptyProperties()
    {
        return new CompilerProperties();
    }

    /**
     * Sets the m_displayBadFlag.
     *
     * @param m_displayBadFlag The m_displayBadFlag to set
     */
    public void setM_displayBadFlag( boolean m_displayBadFlag )
    {
        this.m_displayBadFlag = m_displayBadFlag;
    }

    /**
     * @see org.openorb.compiler.CompilerHost#display(java.lang.String)
     */
    public void display( String s )
    {
        System.out.println( s );
    }

    /**
     * Generic function for executing all compilers on command line.
     *
     * @param args  command line arguments list
     */
    public static int genCompilerExec( CompilerIF comp, CompilerHost ch, String[] args )
    {
        CompilerProperties cp = comp.createEmptyProperties();

        comp.init_compiler( ch , cp );

        if ( args.length == 0 )
        {
            ch.display( "no arguments: displaying help" );
            comp.display_help();
            return 1;
        }

        // load the arguments from the command line
        comp.scan_args( args, cp );

        comp.execute_compiler( ch , cp );

        if ( cp.getM_compileList().size() == 0 )
        {
            ch.display( "no files to compile !!" );
            comp.display_help();
            return 1;
        }

        comp.init_compiler( ch, cp );

        // -- Starts the compilation --
        try
        {
            Iterator it = cp.getM_compileList().iterator();

            while ( it.hasNext() )
            {
                CompileListEntry cle = ( CompileListEntry ) it.next();

                //definedMacros = definedCopy;
                if ( !cp.getM_silentMode() )
                {
                    ch.display( "compile : " + cle.getFileName() );
                }
                comp.compile_file( cle, cp );
            }
        }
        catch ( org.openorb.compiler.parser.CompilationException ex )
        {
            System.out.println( "there are errors..." );
            System.out.println( "compilation process stopped !" );
            return 2;
        }
        return 0;
    }

    /**
     * Perform a compile without exiting the VM upon error.
     *
     * @param args The command line arguments list.
     */
    public static int compile( String[] args )
    {
        IdlCompiler idlcomp = new IdlCompiler();
        return genCompilerExec( idlcomp, idlcomp, args );
    }

    /**
     * The IDL compiler starts here
     *
     * @param args  command line arguments list
     */
    public static void main( String[] args )
    {
        IdlCompiler idlcomp = new IdlCompiler();
        int return_code = 3;
        try
        {
            return_code = genCompilerExec( idlcomp, idlcomp, args );
        }
        catch ( Throwable th )
        {
            th.printStackTrace();
            // ignore, return code
        }
        System.exit( return_code );
    }
}

