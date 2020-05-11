/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.rmi.parser;

import java.io.FileNotFoundException;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import org.openorb.compiler.CompilerHost;

import org.openorb.compiler.object.IdlInclude;
import org.openorb.compiler.object.IdlObject;
import org.openorb.compiler.object.IdlRoot;

import org.openorb.compiler.parser.CompilationException;
import org.openorb.compiler.parser.IdlParser;

import org.openorb.compiler.rmi.RmiCompilerProperties;

import org.openorb.compiler.orb.Configurator;

/**
 * This class builds the java data tree needed for the generation of
 * idl code.
 *
 * @author Jerome Daniel
 * @author Michael Rumpf
 */
public class JavaParser
{
    /**
     * Compilation tree vector. This vector holds all the sub-trees
     * created by all JavaParser instances.
     */
    private List m_compilationTreeVector;

    /** Classes that have been already processed. */
    private List m_alreadyProcessedClasses;

    /** Association between the Java name and the IDL linked mapping. */
    private Map m_mappingNames;

    private RmiCompilerProperties m_rcp;

    private CompilerHost m_ch;

    /** The root node of the IDL tree. A per instance variable. */
    private IdlObject m_idlTreeRoot;

    private IdlParser m_idlparser;

    /** The current parsed class. */
    private Class m_currentClass;

    /**
     * Constructor.
     *
     * @param rcp The RMI compiler properties.
     * @param cp The IDL compiler properties.
     * @param j2i The Java2IDL class instance from which the compiler was started.
     * @param compilationTreeVector A vector with all the JavaParser instances.
     * @param mappingNames A table of class names associated with their IdlRoot sub-tree elements.
     * @param alreadyProcessedClasses A vector of classes that have already been processed
     * by this JavaParser tree.
     */
    public JavaParser( final RmiCompilerProperties rcp, final CompilerHost ch,
          final List compilationTreeVector, final Map mappingNames,
          final List alreadyProcessedClasses )
    {
        m_ch = ch;
        m_rcp = rcp;
        m_rcp.setM_packageName( "" );

        display( "JavaParser" );

        display( "JavaParser::init" );
        // Create the root of the tree
        m_idlparser = new IdlParser( m_rcp );

        // ??? The previous version did not pass the parser here!!!
        m_idlTreeRoot = new IdlRoot( m_rcp, m_idlparser );
        display( "IDLRoot created." );

        if ( alreadyProcessedClasses != null )
        {
            m_alreadyProcessedClasses = alreadyProcessedClasses;
        }
        else
        {
            m_alreadyProcessedClasses = new ArrayList();
        }

        if ( mappingNames != null )
        {
            m_mappingNames = mappingNames;
        }
        else
        {
            m_mappingNames = new HashMap();
        }

        if ( compilationTreeVector != null )
        {
            m_compilationTreeVector = compilationTreeVector;
        }
        else
        {
            m_compilationTreeVector = new ArrayList();
        }

        // The IdlRoot is a new instance for each JavaParser instance
        m_compilationTreeVector.add( getIdlTreeRoot() );

        // add the standard mappings only once
        if ( compilationTreeVector != null && mappingNames != null
              && alreadyProcessedClasses != null )
        {
            m_mappingNames.put( "java.lang.Object",       "::java::lang::_Object" );
            m_mappingNames.put( "org.omg.CORBA.Object",   "::CORBA::Object" );
            m_mappingNames.put( "java.rmi.Remote",        "::java::rmi::Remote" );
            m_mappingNames.put( "java.io.Serializable",   "::java::io::Serializable" );
            m_mappingNames.put( "java.io.Externalizable", "::java::io::Externalizable" );
            m_mappingNames.put( "java.lang.Class",        "::javax::rmi::CORBA::ClassDesc" );
            m_mappingNames.put( "java.lang.String",       "::CORBA::WStringValue" );
        }
    }

    /**
     * Return the compilation tree vector.
     *
     * @return The compilation tree vector.
     */
    public List getCompilationTree()
    {
        return m_compilationTreeVector;
    }

    /**
     * Return the vector of classes that have been processed.
     *
     * @return The already processed classes vector.
     */
    public List getAlreadyProcessedClasses()
    {
        return m_alreadyProcessedClasses;
    }

    /**
     * Return the Map of Java - IDL name mappings.
     *
     * @return The Map of Java - IDL name mappings.
     */
    public Map getMappingNames()
    {
        return m_mappingNames;
    }

    public IdlParser getIdlParser()
    {
        return m_idlparser;
    }

    /**
     * Reading Accessor for the current parsed class.
     *
     * @return The root of the parsing tree.
     */
    public IdlObject getIdlTreeRoot()
    {
        return m_idlTreeRoot;
    }

    /**
     * Writing Accessor for the current parsed class.
     *
     * @param new_idl_root The root of the parsing tree.
     */
    public void setIdlTreeRoot( final IdlObject new_idl_root )
    {
        m_idlTreeRoot = new_idl_root;
    }

    /**
     * Writing Accessor for the new class to be parsed.
     *
     * @param newClass The new class to be parsed.
     */
    public void setCurrentClass( final Class newClass )
    {
        m_currentClass = newClass;
    }

    /**
     * Reading Accessor for the current parsed class.
     *
     * @return The class currently processed.
     */
    public Class getCurrentClass()
    {
        return m_currentClass;
    }

    /**
     * Look for a definition in all the compilation trees.
     *
     * @param scope ???
     * @param limit ???
     * @return The object if one was found, null otherwise.
     */
    public IdlObject returnObject( final String scope, final boolean limit )
    {
        final int size = m_compilationTreeVector.size();
        for ( int i = 0; i < size; i++ )
        {
            final IdlObject obj = ( IdlObject ) m_compilationTreeVector.get( i );
            final IdlObject newObj = obj.returnObject( scope, limit );
            if ( newObj != null )
            {
                return newObj;
            }
        }
        return null;
    }

    /**
     * This is the main method called to parse the java class file.
     *
     * @param java_file_name The name of the file to parse.
     * @return The IdlObject representing the parsed class.
     */
    public IdlObject parse_java( final String java_file_name )
    {
        return parse_java( java_file_name, false );
    }

    /**
     * This is the main method called to parse the java class file.
     *
     * @param java_file_name The name of the file to parse
     * @param check_implements Check whether the class is an implementation class
     *                         and try to get the remote interface in this case.
     * @return The IdlObject representing the parsed class.
     */
    public IdlObject parse_java( final String java_file_name, final boolean check_implements )
    {
        display( "JavaParser::parse_java((String)[", java_file_name,
                "], (boolean)[", ( check_implements ? "true" : "flase" ), "])" );
        // Loading the class
        try
        {
            m_currentClass = load_class( java_file_name );
        }
        catch ( final ClassNotFoundException cnfe )
        {
            System.out.println( "<!> Class not found exception raised : " + java_file_name );
            // TODO: compile when the class can't be found
            return null;
        }

        // *** Check whether we ar running on the impl instead of the interface
        if ( check_implements && !m_currentClass.isInterface() )
        {
            final Class[] clzs = m_currentClass.getInterfaces();
            Class remclz = null;
            // *** search for a remote interface
            for ( int i = 0; i < clzs.length; i++ )
            {
                // *** check whether the interface is of type java.rmi.Remote
                if ( java.rmi.Remote.class.isAssignableFrom( clzs[ i ] ) )
                {
                    if ( remclz != null )
                    {
                        System.out.println( "Error: Multiple java.rmi.Remote found in implements: "
                              + m_currentClass );
                        return null;
                    }
                    else
                    {
                        remclz = clzs[ i ];
                    }
                }
            }
            if ( remclz != null )
            {
                m_currentClass = remclz;
            }
            else
            {
                System.out.println( "Error: No interface java.rmi.Remote found: "
                          + m_currentClass );
                return null;
            }
        }
        // Parse the class
        parse_class( m_currentClass );
        return m_idlTreeRoot;
    }

    /**
     * Allow to load a class.
     *
     * @param filename The name of the class file to load and parse.
     * @return The loaded class.
     *
     * @exception ClassNotFoundException If no class was found.
     */
    public Class load_class( final String filename ) throws ClassNotFoundException
    {
        display( "JavaParser::load_class((String)[", filename, "])" );
        try
        {
            final Class myLocClass = m_rcp.getClassloader().loadClass( filename );
            display( "Class " + filename + " loaded." );
            return myLocClass;
        }
        catch ( final ClassNotFoundException cnfe )
        {
            display( "class loader type = " + m_rcp.getClassloader().getClass().getName() );
            display( cnfe.toString() );
            throw cnfe;
        }
    }

    private void display( final String arg0 )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( arg0 );
        }
    }

    private void display( final String arg0, final Object arg1, final Object arg2 )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( arg0 + arg1 + arg2 );
        }
    }

    private void display( final String arg0, final Object arg1, final Object arg2,
            final Object arg3, final Object arg4 )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( arg0 + arg1 + arg2 + arg3 + arg4 );
        }
    }

    private void display( final String arg0, final Object arg1, final Object arg2,
            final Object arg3, final Object arg4, final Object arg5, final Object arg6 )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( arg0 + arg1 + arg2 + arg3 + arg4 + arg5 + arg6 );
        }
    }

    /**
     * Parse the class.
     *
     * @param c The class to parse.
     */
    public void parse_class( final Class c )
    {
        display( "JavaParser::parse_class((Class)[", c, "])" );
        MappingAPI locMappingAPI = new MappingAPI( m_rcp, m_ch, this );

        final IdlObject locIdlObj;
        // *** Map the package ***
        if ( !c.isInterface() && c.isArray() )
        {
            locIdlObj = locMappingAPI.map_package( "org.omg.boxedRMI."
                  + MappingAPI.get_array_name_without_extra_char( c.getName() ), m_idlTreeRoot );
        }
        else
        {
            locIdlObj = locMappingAPI.map_package( c.getName(), m_idlTreeRoot );
        }
        // *** Checks the class type (interface or object class) ***
        if ( c.isInterface() )
        {
            display( "*** Interface " + c.getName() + " detected." );
            locMappingAPI.map_interface( c, locIdlObj );
        }
        else
        {
            display( "*** Class " + c.getName() + " detected." );
            locMappingAPI.map_class( c, locIdlObj );
        }
    }

    //
    // IDL population functions
    //

    /**
     * Add idl included files.
     *
     * @param configurator The configuration class with all the settings.
     * @param include The vector with included classes.
     */
    public void load_standard_idl( final Configurator configurator, final List include )
    {
        m_compilationTreeVector.add( add_idl_file( include, configurator,
              "_std_java.idl" ) );
        m_compilationTreeVector.add( add_idl_file( include, configurator,
              "orb.idl" ) );
        m_compilationTreeVector.add( add_idl_file( include, configurator,
              "_std_javax.idl" ) );
    }

    /**
     * Add idl included files.
     *
     * @param vect The vector with idl files to include.
     * @param include The vector with already compiled classes.
     */
    public void add_idl_files( final List vect, final List include )
    {
        final int locSize = vect.size();
        IdlObject compilationGraph = null;

        final IdlParser idlparser = new IdlParser( m_rcp );
        for ( int i = 0; i < locSize; i++ )
        {
            final String idl_file_name = ( String ) vect.get( i );
            display( "adding idl file " + idl_file_name );
            try
            {
                compilationGraph = idlparser.compile_idl( idl_file_name );
            }
            catch ( final FileNotFoundException e )
            {
                System.err.println( "Impossible to add " + idl_file_name + ":" + e.toString() );
            }
            if ( idlparser.getTotalErrors() != 0 )
            {
                throw new CompilationException( "error in parsing " + idl_file_name );
            }
            // Add to the graph compilation tree
            if ( compilationGraph != null )
            {
                m_compilationTreeVector.add( compilationGraph );
                // Add as an include
                final MappingAPI m_api = new MappingAPI( m_rcp, m_ch, this );

                m_api.add_idl_object_as_first( m_idlTreeRoot, new IdlInclude(
                      m_idlTreeRoot, idl_file_name.substring( 0,
                      idl_file_name.lastIndexOf( '.' ) ) ) );
            }
            else
            {
                throw new CompilationException( "File " + idl_file_name + " not found..." );
            }
        }
        display( "IDLFiles loaded." );
    }

    /**
     * Add idl included files.
     *
     * @param include A vector with included files.
     * @param configurator The configurator with all the settings.
     * @param idl_file_name The file name to include.
     * @return The tree as a result of parsing the file idl_file_name.
     */
    public IdlObject add_idl_file( final List include, final Configurator configurator,
            final String idl_file_name )
    {
        // -- Add RMI IDL Directory --
        //String directory = configurator.getProperties().getStringProperty("compiler.idl.rmi",
        //      null);
        //if ( directory != null )
        // org.openorb.compiler.IdlCompiler.includeList.addElement( directory );

        // -- Add OpenORB IDL Directory --
        //directory = configurator.getProperties().getStringProperty("compiler.idl.orb", null);
        //if ( directory != null )
        // org.openorb.compiler.IdlCompiler.includeList.addElement( directory );

        IdlObject compilationGraph = null;

        final IdlParser idlparser = new IdlParser( m_rcp );
        try
        {
            compilationGraph = idlparser.compile_idl( idl_file_name );
        }
        catch ( final FileNotFoundException e )
        {
            System.err.println( e.toString() );
        }
        if ( idlparser.getTotalErrors() != 0 )
        {
            throw new CompilationException( "error in parsing " + idl_file_name );
        }
        return compilationGraph;
    }
}

