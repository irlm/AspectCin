/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.rmi;

import org.openorb.compiler.CompilerException;
import org.openorb.compiler.CompilerHost;
import org.openorb.compiler.CompilerIF;
import org.openorb.compiler.CompileListEntry;
import org.openorb.compiler.CompilerProperties;
import org.openorb.compiler.IdlCompiler;

import org.openorb.compiler.parser.CompilationException;

import org.openorb.compiler.rmi.parser.JavaParser;

import org.openorb.util.ORBUtils;

/**
 * This class is an implementation of the specification Java To IDL.
 *
 * @author Jerome Daniel
 */
public class JavaToIdl
    implements CompilerHost, CompilerIF
{
    private static final String JAVA2IDL_COMPILER = "Java to IDL Compiler";

    private CompilerHost m_ch = null;

    private IdlCompiler m_idlcomp;

    /**
     * This function parse all arguments from the command line.
     *
     * @param args The application arguments array.
     */
    public void scan_args( String[] args, CompilerProperties cp )
    {
        RmiCompilerProperties rcp = ( RmiCompilerProperties ) cp;

        for ( int i = 0; i < args.length; i++ )
        {
            if ( cp.getM_verbose() )
            {
                m_ch.display( "Parsing argument " + i + ": " + args[ i ] );
            }
            if ( args[ i ].charAt( 0 ) != '-' )
            {
                CompileListEntry cle = new CompileListEntry( args [i] );
                cp.getM_compileList().add( cle );
            }
            else if ( args[ i ].equals( "-noidl" ) )
            {
                rcp.setMapIDL( false );
            }
            else if ( args[ i ].equals( "-tie" ) )
            {
                rcp.setM_map_tie( true );
            }
            else if ( args[ i ].equals( "-ejb" ) )
            {
                rcp.setMapEJBExceptions( true );
            }
            else if ( args[ i ].equals( "-silence" ) || args[ i ].equals( "-quiet" ) )
            {
                cp.setM_silentMode( true );
            }
            else if ( args[ i ].equals( "-local" ) )
            {
                rcp.setM_local_stub( true );
            }
            else if ( args[ i ].equals( "-stub" ) )
            {
                rcp.setM_map_stub( true );
            }
            else if ( args[ i ].equals( "-boa" ) )
            {
                rcp.setM_map_poa( false );
            }
            else if ( args[ i ].equals( "-all" ) )
            {
                rcp.setM_map_all( true );
            }
            else if ( args[ i ].equals( "-verbose" ) )
            {
                rcp.setM_verbose( true );
            }
            else if ( args[ i ].equals( "-no_value_meth" ) )
            {
                rcp.setGenerateValueMethods( false );
            }
            else if ( args[ i ].equals( "-d" ) )
            {
                if ( i + 1 == args.length )
                {
                    final String msg = "Argument expected after '-d'";
                    System.out.println( msg );
                    throw new CompilerException( msg );
                }

                rcp.setM_destdir( new java.io.File( args[ ++i ] ) );

                if ( rcp.getM_packageName() == null )
                {
                    rcp.setM_packageName( "" );
                    rcp.setM_use_package( false );
                }
            }
            else if ( args[ i ].startsWith( "-idl_include" ) )
            {
                if ( i + 1 == args.length )
                {
                    final String msg = "Argument expected after '-idl_include'";
                    System.out.println( msg );
                    throw new CompilerException( msg );
                }

                rcp.getIncludedFiles().addElement( args[ ++i ] );
            }
            else if ( args[ i ].startsWith( "-I" ) )
            {
                if ( i + 1 == args.length )
                {
                    final String msg = "Argument expected after '-I'";
                    System.out.println( msg );
                    throw new CompilerException( msg );
                }
                rcp.getM_includeList().addElement( args[ ++i ] );
            }
            else if ( args[ i ].startsWith( "-D" ) )
            {
                if ( i + 1 == args.length )
                {
                    final String msg = "Argument expected after '-D'";
                    System.out.println( msg );
                    throw new CompilerException( msg );
                }
                try
                {
                    int idx = args[ i ].indexOf( '=' );
                    if ( idx < 0 )
                    {
                        rcp.getM_macros().put( args[ i ].substring( 2,
                              args[ i ].length() ) , "" );
                    }
                    else
                    {
                        rcp.getM_macros().put( args[ i ].substring( 2, idx ),
                              args[ i ].substring( idx + 1 ) );
                    }
                }
                catch ( StringIndexOutOfBoundsException ex )
                {
                    // ignore
                }
            }
            else if ( args[ i ].equals( "-help" ) || args[ i ].equals( "-h" ) )
            {
                display_help();
                System.exit( 1 );
            }
            else
            {
                final String msg = "Bad parameter: " + args[ i ]
                      + "\nPlease, use no flag to display all compiler option flags";
                System.out.println( msg );
                throw new CompilerException( msg );
            }
        }
    }


    /**
     * This function prints on screen help for this compiler.
     *
     */
    public void display_help()
    {
        m_ch.display( "Usage: java org.openorb.compiler.rmi.JavaToIdl [Options]"
              + " class-files..." );
        m_ch.display( "Options:" );
        m_ch.display( "--------" );

        m_ch.display( "  -all" );
        m_ch.display( "              Generates IDL files for dependencies." );

        m_ch.display( "  -boa" );
        m_ch.display( "              Generates classes for the BOA approach." );

        m_ch.display( "  -d <output_folder>" );
        m_ch.display( "              Provides a way to specify the ouput dir." );
        m_ch.display( "              Example:" );
        m_ch.display( "                  org.openorb.compiler.rmi.JavaToIdl -d /home/me/"
              + " demo.class" );

        m_ch.display( "  -D <symbol>" );
        m_ch.display( "              Defines a symbol. It is equivalent to #define." );

        m_ch.display( "  -ejb" );
        m_ch.display( "              Generates Ties that provide the mapping of RMI"
              + " exceptions to" );
        m_ch.display( "              CORBA system exceptions (see EJB2.0, 19.5.3"
              + " Mapping of system" );
        m_ch.display( "              exceptions)." );

        m_ch.display( "  -idl_include <file>" );
        m_ch.display( "              Specifies IDL files to include." );

        m_ch.display( "  -I <include_folder>" );
        m_ch.display( "              Supplies include directory for IDL descriptions." );

        m_ch.display( "  -local" );
        m_ch.display( "              Generates Stub optimizations for intra-process"
              + " access." );

        m_ch.display( "  -no_value_meth" );
        m_ch.display( "              Don't generate the methods for value type data." );

        m_ch.display( "  -noidl" );
        m_ch.display( "              Don't generate an IDL file for RMI objects." );

        m_ch.display( "  -quiet" );
        m_ch.display( "              Suppress any output. Same as -silence." );

        m_ch.display( "  -silence" );
        m_ch.display( "              Suppress any output. Same as -quiet." );

        m_ch.display( "  -stub" );
        m_ch.display( "              Generate Stub classes for RMI objects." );

        m_ch.display( "  -tie" );
        m_ch.display( "              Generate Tie classes for RMI objects." );

        m_ch.display( "  -verbose" );
        m_ch.display( "              Show debugging information of the compiler." );

    }

    /**
     * Default constructor.
     */
    public JavaToIdl()
    {
        m_ch = this;
        m_idlcomp = new IdlCompiler();
    }

    /**
     * Entry point for the compiler with a return value.
     *
     * @param args The application arguments.
     */
    public static int compile( String [] args )
    {
        JavaToIdl j2i = new JavaToIdl();
        return IdlCompiler.genCompilerExec( j2i, j2i, args );
    }

    /**
     * Entry point for the compiler that exits the VM.
     *
     * @param args The application arguments.
     */
    public static void main( String [] args )
    {
        JavaToIdl j2i = new JavaToIdl();
        int return_code = 3;
        try
        {
            return_code = IdlCompiler.genCompilerExec( j2i, j2i, args );
        }
        catch ( Throwable th )
        {
            System.out.println( "An error occured!" );
            th.printStackTrace();
        }
        System.exit( return_code );
    }

    /**
     * @see org.openorb.compiler.CompilerHost#display(java.lang.String)
     */
    public void display( String s )
    {
        System.out.println( s );
    }

    /**
     * @see org.openorb.compiler.CompilerIF#init_compiler
     * (org.openorb.compiler.CompilerHost, org.openorb.compiler.CompilerProperties)
     */
    public void init_compiler( CompilerHost ch, CompilerProperties cp )
    {
        IdlCompiler.configFile( cp );
    }

    /**
     * Display a welcome message.
     * @see org.openorb.compiler.CompilerIF#execute_compiler
     * (org.openorb.compiler.CompilerHost, org.openorb.compiler.CompilerProperties)
     */
    public void execute_compiler( CompilerHost ch, CompilerProperties cp )
    {
        if ( !cp.getM_silentMode() )
        {
            ch.display( JAVA2IDL_COMPILER + ", " + ORBUtils.COPYRIGHT );
        }
    }

    /**
     * @see org.openorb.compiler.CompilerIF#compile_file
     * (java.lang.String, org.openorb.compiler.CompilerProperties)
     */
    public void compile_file( CompileListEntry cle, CompilerProperties cp )
        throws CompilationException
    {
        RmiCompilerProperties rcp = ( RmiCompilerProperties ) cp;
        // ------------------
        // Now, begin parsing
        // ------------------
        JavaParser javaParser = new JavaParser( rcp, this, null, null, null );
        org.openorb.compiler.orb.Configurator configurator =
              new org.openorb.compiler.orb.Configurator( new String[0],
              new java.util.Properties() );

        if ( cp.getM_verbose() )
        {
            m_ch.display( "loading standard IDL" );
        }

        javaParser.load_standard_idl( configurator, rcp.getM_includeList() );

        // Load the included files
        if ( cp.getM_verbose() )
        {
            m_ch.display( "loading included IDL" );
        }
        javaParser.add_idl_files( rcp.getIncludedFiles(), rcp.getM_includeList() );

        // Assign default names to IDLroot
        int locTaille = javaParser.getCompilationTree().size();

        // Remove the ".class" suffix if it is there
        String cl_name = cle.getFileName();
        if ( cl_name.endsWith( ".class" ) )
        {
            cl_name = cl_name.substring( 0, cl_name.length() - 6 );
            cl_name = cl_name.replace( '/', '.' );
            cl_name = cl_name.replace( '\\', '.' );
        }

        if ( cp.getM_verbose() )
        {
            m_ch.display( "Parse class file : " + cl_name );
        }

        org.openorb.compiler.object.IdlObject compilationGraph = null;
        try
        {
            compilationGraph = javaParser.parse_java( cl_name, true );
        }
        catch ( final Throwable th )
        {
            m_ch.display( "An exception occured while parsing file: '" + cl_name + "'! ("
                  + th + ")" );
            return;
        }
        if ( compilationGraph == null )
        {
            m_ch.display( "A parser error occured for file: '" + cl_name + "'!" );
            return;
        }

        // ----------
        // map to IDL
        // ----------
        org.openorb.compiler.rmi.generator.Javatoidl toIDL =
              new org.openorb.compiler.rmi.generator.Javatoidl( rcp, m_ch );
        if ( rcp.getMapIDL() )
        {
            if ( !cp.getM_silentMode() )
            {
                m_ch.display( "Translating to IDL..." );
            }
            toIDL.translateToIDL( compilationGraph,
                  compilationGraph._name.replace( '.', java.io.File.separatorChar ) + ".idl" );
        }
        if ( rcp.getM_map_all() && rcp.getMapIDL() )
        {
            if ( !cp.getM_silentMode() )
            {
                m_ch.display( "Translating all IDL dependencies..." );
            }
            int locSize = javaParser.getCompilationTree().size();
            for ( int i = locTaille; i < locSize; i++ )
            {
                toIDL.translateToIDL( ( org.openorb.compiler.object.IdlRoot )
                      javaParser.getCompilationTree().get( i ),
                      ( ( org.openorb.compiler.object.IdlRoot )
                      javaParser.getCompilationTree().get( i ) )._name.replace(
                      '.', java.io.File.separatorChar ) + ".idl" );
            }
        }

        // -------------
        // map tie class
        // -------------
        if ( rcp.getM_map_all() && rcp.getM_map_tie() )
        {
            if ( !cp.getM_silentMode() )
            {
                m_ch.display( "Translating all Tie dependencies..." );
            }
            int locSize = javaParser.getCompilationTree().size();
            for ( int i = locTaille; i < locSize; i++ )
            {
                toIDL.translateRMITie( ( org.openorb.compiler.object.IdlRoot )
                      javaParser.getCompilationTree().get( i ) );
            }
        }
        if ( rcp.getM_map_tie() )
        {
            if ( !cp.getM_silentMode() )
            {
                 m_ch.display( "Generating Tie file..." );
            }
            toIDL.translateRMITie( compilationGraph );
        }

        // --------------
        // map stub class
        // --------------
        if ( rcp.getM_map_all() && rcp.getM_map_stub() )
        {
            if ( !cp.getM_silentMode() )
            {
                 m_ch.display( "Translating all Stub dependencies..." );
            }
            int locSize = javaParser.getCompilationTree().size();
            for ( int i = locTaille; i < locSize; i++ )
            {
                toIDL.translateRMIStub( ( org.openorb.compiler.object.IdlRoot )
                      javaParser.getCompilationTree().get( i ) );
            }
        }
        if ( rcp.getM_map_stub() )
        {
            if ( !cp.getM_silentMode() )
            {
                m_ch.display( "Generating Stub file..." );
            }
            toIDL.translateRMIStub( compilationGraph );
        }
    }
    /**
     * @see org.openorb.compiler.CompilerIF#createEmptyProperties()
     */
    public CompilerProperties createEmptyProperties()
    {
        RmiCompilerProperties rcp = new RmiCompilerProperties();
        rcp.setClassloader( Thread.currentThread().getContextClassLoader() );
        return rcp;
    }
}

