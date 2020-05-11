/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.rmi.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import java.io.Externalizable;

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.IDLEntity;

import org.openorb.compiler.CompilerHost;

import org.openorb.compiler.object.IdlAttribute;
import org.openorb.compiler.object.IdlConst;
import org.openorb.compiler.object.IdlExcept;
import org.openorb.compiler.object.IdlFactory;
import org.openorb.compiler.object.IdlIdent;
import org.openorb.compiler.object.IdlInclude;
import org.openorb.compiler.object.IdlInterface;
import org.openorb.compiler.object.IdlModule;
import org.openorb.compiler.object.IdlObject;
import org.openorb.compiler.object.IdlOp;
import org.openorb.compiler.object.IdlParam;
import org.openorb.compiler.object.IdlRaises;
import org.openorb.compiler.object.IdlRoot;
import org.openorb.compiler.object.IdlSequence;
import org.openorb.compiler.object.IdlSimple;
import org.openorb.compiler.object.IdlStateMember;
import org.openorb.compiler.object.IdlStructMember;
import org.openorb.compiler.object.IdlTypeDef;
import org.openorb.compiler.object.IdlValue;
import org.openorb.compiler.object.IdlValueBox;
import org.openorb.compiler.object.IdlValueInheritance;

import org.openorb.compiler.parser.Symbole;
import org.openorb.compiler.parser.SymboleDef;
import org.openorb.compiler.parser.CompilationException;
import org.openorb.compiler.parser.Token;

import org.openorb.compiler.rmi.RmiCompilerProperties;

import org.openorb.util.ReflectionUtils;
import org.openorb.util.RepoIDHelper;
import org.openorb.util.NumberCache;

/**
 * This class provides the needed methods to map the java tree to the idl tree.
 *
 * @author   Vincent Vallee, Jerome Daniel
 */
public class MappingAPI
{
    /** This bit is set for all corba types.  */
    public static final int CORBA_TYPE_MASK = 0x100;

    /** This bit is set for all exceptions.  */
    public static final int EXCEPTION_TYPE_MASK = 0x200;

    /** This bit is set for all exceptions.  */
    public static final int REMOTE_TYPE_MASK = 0x400;

    /** NOT TO MAP */
    public static final int NOT_TO_MAP = 0;

    /** RMI/IDL remote interface type. */
    public static final int RMI_IDL_REMOTE_INTERFACE_TYPE = 0x401;

    /** RMI/IDL value types type */
    public static final int RMI_IDL_VALUE_TYPES_TYPE = 2;

    /** RMI/IDL customvalue types type */
    public static final int RMI_IDL_CUSTOM_VALUE_TYPES_TYPE = 3;

    /**  Rmi Implementation class */
    public static final int RMI_IMPLEMENTATION_CLASS = 0x404;

    /** RMI/IDL ARRAYS type */
    public static final int RMI_IDL_ARRAYS_TYPE = 5;

    /** Abstract interface type */
    public static final int RMI_IDL_ABSTRACT_INTERFACE_TYPE = 6;

    /** RMI/IDL EXCEPTION type */
    public static final int RMI_IDL_EXCEPTION_TYPE = 0x207;

    /** Non Conforming CLASSES & interface type */
    public static final int NON_CONFORMING_TYPE = 8;

    /** CORBA Reference type */
    public static final int CORBA_REFERENCE_TYPE = 0x509;

    /** CORBA Reference type */
    public static final int CORBA_ABSTRACT_INTERFACE_TYPE = 0x10A;

    /** CORBA Reference type */
    public static final int CORBA_VALUE_TYPE = 0x10B;

    /** IDL entity type */
    public static final int CORBA_COMPLEX_TYPE = 0x10C;

    /** IDL entity type */
    public static final int CORBA_USER_EXCEPTION = 0x30D;

    /** IDL entity type */
    public static final int CORBA_SYSTEM_EXCEPTION = 0x30E;

    /**
     * Show the type of the identified class in a human readable way.
     */
    public static String toString( final int type )
    {
        return toStringPrefix( type ) + toStringPostfix( type );
    }

    private static String toStringPrefix( final int type )
    {
        if ( ( type & CORBA_TYPE_MASK ) == CORBA_TYPE_MASK )
        {
            return "CORBA TYPE -> ";
        }
        if ( ( type & EXCEPTION_TYPE_MASK ) == EXCEPTION_TYPE_MASK )
        {
            return "EXCEPTION TYPE -> ";
        }
        if ( ( type & REMOTE_TYPE_MASK ) == REMOTE_TYPE_MASK )
        {
            return "REMOTE TYPE -> ";
        }
        return "NO_TYPE_FLAG -> ";
    }

    private static String toStringPostfix( final int type )
    {
        switch ( type )
        {
            case NOT_TO_MAP:
                return "NOT_TO_MAP";
            case RMI_IDL_REMOTE_INTERFACE_TYPE:
                return "RMI_IDL_REMOTE_INTERFACE_TYPE";
            case RMI_IDL_VALUE_TYPES_TYPE:
                return "RMI_IDL_VALUE_TYPES_TYPE";
            case RMI_IDL_CUSTOM_VALUE_TYPES_TYPE:
                return "RMI_IDL_CUSTOM_VALUE_TYPES_TYPE";
            case RMI_IMPLEMENTATION_CLASS:
                return "RMI_IMPLEMENTATION_CLASS";
            case RMI_IDL_ARRAYS_TYPE:
                return "RMI_IDL_ARRAYS_TYPE";
            case RMI_IDL_ABSTRACT_INTERFACE_TYPE:
                return "RMI_IDL_ABSTRACT_INTERFACE_TYPE";
            case RMI_IDL_EXCEPTION_TYPE:
                return "RMI_IDL_EXCEPTION_TYPE";
            case NON_CONFORMING_TYPE:
                return "NON_CONFORMING_TYPE";
            case CORBA_REFERENCE_TYPE:
                return "CORBA_REFERENCE_TYPE";
            case CORBA_ABSTRACT_INTERFACE_TYPE:
                return "CORBA_ABSTRACT_INTERFACE_TYPE";
            case CORBA_VALUE_TYPE:
                return "CORBA_VALUE_TYPE";
            case CORBA_COMPLEX_TYPE:
                return "CORBA_COMPLEX_TYPE";
            case CORBA_USER_EXCEPTION:
                return "CORBA_USER_EXCEPTION";
            case CORBA_SYSTEM_EXCEPTION:
                return "CORBA_SYSTEM_EXCEPTION";
            default:
                return "UNKNOWN TYPE";
        }
    }


    /** Hashtable that stocks the already parsed method class key is the class name... */
    private Map m_knownMethodsTable = new HashMap();

    /** List that stocks the already parsed method name */
    private List m_knownMethods = new ArrayList();

    /** List that stocks the already parsed field name */
    private List m_knownFields = new ArrayList();

    /** List that stocks the methods that are definitely not accessors */
    private List m_definitelyNotAccessors = new ArrayList();

    /** Type of the current class */
    private int m_currentClassType = -1;

    /** parent JavaParser */
    private JavaParser m_javaParser;

    private CompilerHost m_ch;

    private RmiCompilerProperties m_rcp = null;

    public MappingAPI( RmiCompilerProperties rcp, CompilerHost ch, JavaParser javaParser )
    {
        m_ch = ch;

        m_javaParser = javaParser;

        m_rcp = rcp;
    }

    private ParserResult parseClass( final Class c )
    {
        final JavaParser parser = new JavaParser( m_rcp, m_ch,
                m_javaParser.getCompilationTree(),
                m_javaParser.getMappingNames(),
                m_javaParser.getAlreadyProcessedClasses() );
        final IdlObject result = parser.parse_java( c.getName() );

        // must be called after parse_java because the IdlRoot is chnaged for some types...
        m_javaParser.getCompilationTree().add( parser.getIdlTreeRoot() );

        return new ParserResult( parser, result );
    }

    private final class ParserResult
    {
        private final JavaParser m_parser;
        private final IdlObject m_result;

        ParserResult( final JavaParser parser, final IdlObject result )
        {
            m_parser = parser;
            m_result = result;
        }

        IdlObject getResult()
        {
            return m_result;
        }

        IdlInclude makeRootInclude()
        {
            return new IdlInclude( m_javaParser.getIdlTreeRoot(),
                    convertName( m_parser.getIdlTreeRoot()._name ) );
        }

        IdlInclude makeResultInclude()
        {
            return new IdlInclude( m_javaParser.getIdlTreeRoot(),
                    convertName( m_result.name() ) );
        }

        private String convertName( final String name )
        {
            return name.replace( '.', '/' );
        }
    }

    /**
     * Map the java package to an idlModule object in the idl tree
     *
     * @param c The current class
     * @param parent The parent node in the tree
     *
     * @return The last idlobject
     */
    public IdlObject map_package( final String full_path, final IdlObject parent )
    {
        // If a package exists
        if ( full_path.indexOf( '.' ) != -1 )
        {
            // Retrieve the package name
            final String locPackageName = get_package_name( full_path );
            // Create the module tree
            IdlObject previousIdlObject = parent;
            int lastIdx = -1;
            int firstIdx = 0;
            while ( true )
            {
                boolean locFinish = false;
                // Works out the indexes
                firstIdx = lastIdx + 1;
                lastIdx = locPackageName.indexOf( '.', firstIdx );
                // Retrieve the module name
                if ( lastIdx == -1 )
                {
                    lastIdx = locPackageName.length();
                    locFinish = true;
                }
                String locModuleName = locPackageName.substring( firstIdx, lastIdx );
                // Checks the validity of the name through the mapping rules
                locModuleName = process_name( locModuleName );
                /* "differing in case" not implemented in the spec */
                // Create the node
                IdlObject locIdlModule = new IdlModule( previousIdlObject );
                locIdlModule._name = locModuleName;
                previousIdlObject.addIdlObject( locIdlModule );
                if ( locFinish )
                {
                    return locIdlModule;
                }
                previousIdlObject = locIdlModule;
            }
        }
        return parent;
    }

    /**
     * Map the interface to the correct idl object in the tree
     *
     * @param c The interface class
     * @param parent The parent node in the tree
     */
    public void map_interface( final Class c, final IdlObject parent )
    {
        display( "MappingAPI::map_interface((Class)[", c, "], (IdlObject)[", parent, "])" );
        // *** Set the type of the mapping ***
        m_currentClassType = setClassType( c );
        // *** Process the name ***
        String locInterfaceName = c.getName();
        boolean anyStep = false;
        // 1 - Process name rules
        locInterfaceName = process_name( locInterfaceName );
        // Add to the class List
        if ( !anyStep )
        {
            m_javaParser.getAlreadyProcessedClasses().add( locInterfaceName );
        }
        // *** Mapping of interface ***
        IdlObject locIdlobj = null;
        switch ( m_currentClassType )
        {
        case CORBA_REFERENCE_TYPE:
        case RMI_IDL_REMOTE_INTERFACE_TYPE:
            locIdlobj = new IdlInterface( parent );
            // Retrieve the name of the interface
            locIdlobj._name = get_relative_name( locInterfaceName );
            if ( m_currentClassType == CORBA_REFERENCE_TYPE )
            {
                IdlTypeDef typeDef = new IdlTypeDef( parent );
                typeDef._name = get_relative_name( locInterfaceName );
                typeDef.type ( locIdlobj );
                locIdlobj = typeDef;
            }
            break;

        case CORBA_ABSTRACT_INTERFACE_TYPE:
        case RMI_IDL_ABSTRACT_INTERFACE_TYPE:
            locIdlobj = new IdlInterface( parent );
            // Retrieve the name of the interface
            locIdlobj._name = get_relative_name( locInterfaceName );
            ( ( IdlInterface ) locIdlobj ).abstract_interface( true );
            break;

        case NOT_TO_MAP:
            // *** Replace IdlRoot ***
            m_javaParser.setIdlTreeRoot( new IdlRoot( m_rcp, m_javaParser.getIdlParser() ) );
            return;

        case RMI_IDL_VALUE_TYPES_TYPE:
            locIdlobj = new IdlValue( parent );
            // Retrieve the name of the interface
            locIdlobj._name = get_relative_name( locInterfaceName );
            ( ( IdlValue ) locIdlobj ).abstract_value( true );
            // *** Add the interface to the parent tree node ***
            parent.addIdlObject( locIdlobj );
            // *** Add to the hashtable ***
            m_javaParser.getMappingNames().put( c.getName(),
                  process_new_full_class_name( c, locIdlobj._name ) );
            // *** Give to idlroot a name that would be the idl file name ***
            String pkgName = get_package_name( c.getName() );
            if ( pkgName.equals( "" ) )
            {
                m_javaParser.getIdlTreeRoot()._name = locIdlobj._name;
            }
            else
            {
                m_javaParser.getIdlTreeRoot()._name = pkgName + "." + locIdlobj._name;
            }
            // ** Generates methods **
            if ( m_rcp.getGenerateValueMethods() )
            {
                map_methods( c.getDeclaredMethods(), locIdlobj, c );
            }
            return;

        default:
            throw new InternalError();
        }

        // *** Add the interface to the parent tree node ***
        parent.addIdlObject( locIdlobj );
        if ( ( m_currentClassType & CORBA_TYPE_MASK ) != 0 )
        {
            // *** Add to the hashtable ***
            m_javaParser.getMappingNames().put( c.getName(),
                  process_new_full_class_name( c, locIdlobj._name ) );
            map_id( c.getName(), locIdlobj, c );
            return;
        }

        // *** Add to the hashtable ***
        m_javaParser.getMappingNames().put( c.getName(),
              process_new_full_class_name( c, locIdlobj._name ) );
        // *** Give to idlroot a name that would be the idl file name ***
        String pkgName = get_package_name( c.getName() );
        if ( pkgName.equals( "" ) )
        {
            m_javaParser.getIdlTreeRoot()._name = locIdlobj._name;
        }
        else
        {
            m_javaParser.getIdlTreeRoot()._name = get_package_name( c.getName() )
                  + "." + locIdlobj._name;
        }
        map_inheritances( c.getInterfaces(), locIdlobj );
        map_constants( c.getDeclaredFields(), locIdlobj );
        map_methods( c.getDeclaredMethods(), locIdlobj, c );
        map_id( c.getName(), locIdlobj, c );
    }

    /**
     * Map the class or the array
     *
     * @param c The class to map
     * @param parent The parent node in the tree
     */
    public void map_class( final Class c, final IdlObject parent )
    {
        display( "MappingAPI::map_class((Class)[", c, "], (IdlObject)[", parent, "])" );
        // *** Set the type of the mapping ***
        m_currentClassType = setClassType( c );
        display( "MappingAPI::map_class : " + c.getName() + " ==> " + m_currentClassType );
        // *** Mapping of class ***
        IdlObject locIdlobj = null;

        switch ( m_currentClassType )
        {

        case RMI_IDL_ARRAYS_TYPE:
            display( "MappingAPI::map_class-->Array detected" );
            map_array( c, parent, 0 );
            break;

        case CORBA_SYSTEM_EXCEPTION:
        case CORBA_USER_EXCEPTION:
            display( "MappingAPI::map_class-->Corba Exception detected " + c.getName() );
            map_corba_exception_class( c, parent );
            break;

        case RMI_IDL_EXCEPTION_TYPE:
            display( "MappingAPI::map_class-->Exception detected " + c.getName() );
            map_exception_class( c, parent );
            break;

        case CORBA_COMPLEX_TYPE:
            display( "MappingAPI::map_class-->Idl Entity detected " + c.getName() );
            map_entity( c, parent );
            break;

        case RMI_IDL_CUSTOM_VALUE_TYPES_TYPE:
            // *** IDL value type Object ***
            locIdlobj = new IdlValue( parent );
            // Become custom
            ( ( IdlValue ) locIdlobj ).custom_value( true );
            parent.addIdlObject( locIdlobj );
            // *** Use the common mapping method ***
            map_value_type( c, parent, locIdlobj );
            break;

        case RMI_IDL_VALUE_TYPES_TYPE:
            // *** IDL Object ***
            locIdlobj = new IdlValue( parent );
            parent.addIdlObject( locIdlobj );
            // *** Use the common mapping method ***
            map_value_type( c, parent, locIdlobj );
            break;

        case RMI_IMPLEMENTATION_CLASS:
            locIdlobj = new IdlInterface( parent );
            // *** Process the name ***
            String locImpl_ClassName = c.getName();
            locImpl_ClassName = get_relative_name( locImpl_ClassName );
            boolean locImpl_anyStep = false;
            // 1 - Process name rules
            locImpl_ClassName = process_name( locImpl_ClassName );
            // Add to the classes List
            if ( !locImpl_anyStep )
            {
                m_javaParser.getAlreadyProcessedClasses().add( locImpl_ClassName );
            }
            // *** Assign the name ***
            locIdlobj._name = locImpl_ClassName;
            // *** Map implements ***
            map_implements( c.getInterfaces(), locIdlobj );
            // *** Map Id ***
            String pkgName = get_package_name( c.getName() );
            if ( pkgName.equals( "" ) )
            {
                map_id( locIdlobj._name, locIdlobj, c );
            }
            else
            {
                map_id( pkgName + "." + locIdlobj._name, locIdlobj, c );
            }
            // *** Add to the hashtable ***
            m_javaParser.getMappingNames().put( c.getName(),
                  process_new_full_class_name( c, locIdlobj._name ) );
            // *** Give to idlroot a name that would be the idl file name ***
            if ( pkgName.equals( "" ) )
            {
                m_javaParser.getIdlTreeRoot()._name = locIdlobj._name;
            }
            else
            {
                m_javaParser.getIdlTreeRoot()._name =
                      get_package_name( c.getName() ) + "." + locIdlobj._name;
            }
            break;

        case NOT_TO_MAP:
        case CORBA_REFERENCE_TYPE:
        case CORBA_ABSTRACT_INTERFACE_TYPE:
        case CORBA_VALUE_TYPE:
            // *** Replace IdlRoot ***
            m_javaParser.setIdlTreeRoot( new IdlRoot( m_rcp, m_javaParser.getIdlParser() ) );
            break;

        case RMI_IDL_REMOTE_INTERFACE_TYPE:
        case RMI_IDL_ABSTRACT_INTERFACE_TYPE:
        case NON_CONFORMING_TYPE:
            locIdlobj = new IdlValue( parent );
            // *** Process the name ***
            String locClassName2 = c.getName();
            locClassName2 = get_relative_name( locClassName2 );
            boolean anyStep2 = false;
            // 1 - Process name rules
            locClassName2 = process_name( locClassName2 );
            // Add to the classes List
            if ( !anyStep2 )
            {
                m_javaParser.getAlreadyProcessedClasses().add( locClassName2 );
            }
            // Retrieve the name of the interface
            locIdlobj._name = get_relative_name( locClassName2 );
            // The interface becomes abstract
            ( ( IdlValue ) locIdlobj ).abstract_value( true );
            // *** Add the interface to the parent tree node ***
            parent.addIdlObject( locIdlobj );
            // *** Mapping of inheritance ***
            if ( c.getSuperclass() != null )
            {
                map_inheritance( c.getSuperclass(), locIdlobj );
            }
            // *** Add to the hashtable ***
            m_javaParser.getMappingNames().put( c.getName(),
                  process_new_full_class_name( c, locIdlobj._name ) );
            // *** Map methods ***
            if ( m_rcp.getGenerateValueMethods() )
            {
                map_methods( c.getDeclaredMethods(), locIdlobj, c );
            }
            // *** Give a name to the idl root ***
            pkgName = get_package_name( c.getName() );
            if ( pkgName.equals( "" ) )
            {
                m_javaParser.getIdlTreeRoot()._name = locIdlobj._name;
            }
            else
            {
                m_javaParser.getIdlTreeRoot()._name = pkgName + "." + locIdlobj._name;
            }
        }
    }

    /**
     * Common mapping method to value type and custom value type
     *
     * @param c The classto map
     * @param parent The parent node in the compilation tree
     * @param locIdlobj The current object
     */
    public void map_value_type( final Class c, final IdlObject parent, final IdlObject locIdlobj )
    {
        display( "MappingAPI::map_value_type((Class)[", c, "], (IdlObject)[", parent,
                "], (IdlObject)[", locIdlobj, "])" );
        // *** Process the name ***
        String locClassName = c.getName();
        locClassName = get_relative_name( locClassName );
        boolean anyStep = false;
        // 1 - Process name rules
        locClassName = process_name( locClassName );
        // Add to the classes List
        if ( !anyStep )
        {
            m_javaParser.getAlreadyProcessedClasses().add( locClassName );
        }
        // *** Assign the name ***
        locIdlobj._name = locClassName;
        // *** Add to the hashtable ***
        m_javaParser.getMappingNames().put( c.getName(),
              process_new_full_class_name( c, locIdlobj._name ) );
        // *** Give a name to the idl rot ***
        String pkgName = get_package_name( c.getName() );
        // *** Mapping of inheritance ***
        if ( c.getSuperclass() != null )
        {
            map_inheritance( c.getSuperclass(), locIdlobj );
        }
        map_implements( c.getInterfaces(), locIdlobj );
        map_constants( c.getDeclaredFields(), locIdlobj );
        map_fields( c.getDeclaredFields(), locIdlobj );
        if ( m_rcp.getGenerateValueMethods() )
        {
            map_constructors( c.getConstructors(), locIdlobj, locClassName );
            map_methods( c.getDeclaredMethods(), locIdlobj, c );
        }
        map_id( c.getName(), locIdlobj, c );
        if ( pkgName.equals( "" ) )
        {
            m_javaParser.getIdlTreeRoot()._name = locIdlobj._name;
        }
        else
        {
            m_javaParser.getIdlTreeRoot()._name = pkgName + "." + locIdlobj._name;
        }
    }

    /**
     * Map an idl entity
     *
     * @param c The classto map
     * @param parent The parent node in the compilation tree
     */
    public void map_entity( final Class c, final IdlObject parent )
    {
        display( "MappingAPI::map_entity((Class)[", c, "], (IdlObject)[", parent, "])" );
        // *** Replace IdlRoot ***
        // this is necessary because IDLEntity will be mapped into
        // a module that is not related to its Java package hierarchy.
        // org.omg.CORBA.Any -> org.omg.boxedIDL.CORBA.Any
        m_javaParser.setIdlTreeRoot( new IdlRoot( m_rcp, m_javaParser.getIdlParser() ) );
        // *** Create the package ***
        // Any and TypeCode need a special mapping,
        // see 1.3.9 Mapping IDLEntity Types (formal/01-06-07)
        String newMod = "org.omg.boxedIDL.";
        if ( c.getName().equals( "org.omg.CORBA.Any" ) )
        {
            newMod += "CORBA.Any";
        }
        else if ( c.getName().equals( "org.omg.CORBA.TypeCode" ) )
        {
            newMod += "CORBA.TypeCode";
        }
        else
        {
            newMod += c.getName();
        }
        IdlObject locNewParentMod = map_package( newMod, m_javaParser.getIdlTreeRoot() );
        // *** Process name ***
        String locClassName = get_relative_name( c.getName() );
        // 1 - Process name
        locClassName = process_name( locClassName );
        // *** Create the value box ***
        IdlObject locIdlObj = new IdlValueBox( locNewParentMod );
        // Assign the name to the value box
        locIdlObj._name = locClassName;
        // *** Map id ***
        map_id( c.getName(), locIdlObj, c );
        // *** Add the value box to the mod ***
        locNewParentMod.addIdlObject( locIdlObj );
        // *** Add this to the mapping hashtable ***
        m_javaParser.getMappingNames().put( c.getName(), get_absolute_idl_name( newMod ) );
        // *** Add idl root name ***
        m_javaParser.getIdlTreeRoot()._name = newMod;
    }

    /**
     * Map the inheritance
     *
     * @param classes The classes that the current class inherits from
     * @param parent The parent node in the compilation tree
     */
    public void map_inheritances( final Class[] classes, final IdlObject parent )
    {
        display( "MappingAPI::map_inheritances((Class[])[", classes,
                "], (IdlObject)[", parent, "])" );
        int locSize = classes.length;
        for ( int i = 0; i < locSize; i++ )
        {
            boolean locToMap = true;
            switch ( m_currentClassType )
            {
            case RMI_IDL_REMOTE_INTERFACE_TYPE:
                if ( classes[ i ].getName().equals( "java.rmi.Remote" ) )
                {
                    locToMap = false;
                }
                break;

            case RMI_IDL_VALUE_TYPES_TYPE:
                if ( classes[ i ].getName().equals( "java.lang.Object" ) )
                {
                    locToMap = false;
                }
                break;
            }
            if ( locToMap )
            {
                map_inheritance( classes[ i ], parent );
            }
        }
    }

    /**
     * Map the inheritance
     *
     * @param c The class that the current class inherits from
     * @param parent The parent node in the compilation tree
     */
    public void map_inheritance( final Class c, final IdlObject parent )
    {
        display( "MappingAPI::map_inheritance((Class)[", c, "], (IdlObject)[", parent, "])" );
        if ( c.getName().equals( "java.lang.Object" ) )
        {
            return;
        }
        // *** Search for the object ***
        IdlObject locIdlObject = null;
        // Use the association hashtable to figure out the idl name
        String locFullIdlName = null;
        locFullIdlName = ( String ) m_javaParser.getMappingNames().get( c.getName() );
        if ( locFullIdlName != null )
        {
            locIdlObject = return_existing_object( locFullIdlName );
            if ( locIdlObject != null )
            {
                if ( parent instanceof IdlInterface )
                {
                    ( ( IdlInterface ) parent ).addInheritance( locIdlObject );
                }
                if ( parent instanceof IdlValue && locIdlObject instanceof IdlValue )
                {
                    IdlValueInheritance locIdlValueinheritance = new IdlValueInheritance( parent );
                    locIdlValueinheritance.addIdlObject( locIdlObject );
                    ( ( IdlValue ) parent ).addInheritance( locIdlValueinheritance );
                }
                else
                {
                    if ( !( locIdlObject instanceof IdlValue )
                          && !( locIdlObject instanceof IdlInterface )
                          && !( locIdlObject instanceof IdlExcept ) )
                    {
                        display( "<1> Error with the mapping of the super class : "
                              + c.getName() + " ( " + locIdlObject + " ) " );
                    }
                }
            }
        }
        else
        {
            // Parse & map the unknown class
            display( "map_inheritance -> map unknown class : " + c.getName() );
            final ParserResult result = parseClass( c );


            locIdlObject = result.getResult();

            // Get the good object (Exception = value type + exception)
            display( "map_inheritance -> look for : " + ( String )
                  m_javaParser.getMappingNames().get( c.getName() ) + " ( " + c.getName() + " ) " );
            locIdlObject = locIdlObject.returnObject(
                  ( String ) m_javaParser.getMappingNames().get( c.getName() ), true );
            // Add it to the include files
            add_idl_object_as_first( m_javaParser.getIdlTreeRoot(),
                   result.makeRootInclude() );
            // Add the inheritance
            if ( parent instanceof IdlInterface )
            {
                ( ( IdlInterface ) parent ).addInheritance( locIdlObject );
            }
            else
            {
                if ( parent instanceof IdlValue && locIdlObject instanceof IdlValue )
                {
                    IdlValueInheritance locIdlValueinheritance = new IdlValueInheritance( parent );
                    locIdlValueinheritance.addIdlObject( locIdlObject );
                    ( ( IdlValue ) parent ).addInheritance( locIdlValueinheritance );
                }
                else
                {
                    if ( !( locIdlObject instanceof IdlValue )
                          && !( locIdlObject instanceof IdlInterface )
                          && !( locIdlObject instanceof IdlExcept ) )
                    {
                        System.out.println( "<2> Error with the mapping of the super class : "
                              + c.getName() + " ( " + locIdlObject + " ) " );
                    }
                }
            }
        }
    }

    /**
     * Map the implemented classes in the case of a class
     *
     * @param classes The classes to analyse
     * @param parent The parent node in the tree
     */
    public void map_implements( final Class[] classes, final IdlObject parent )
    {
        display( "MappingAPI::map_implements((Class[])[", classes,
                "], (IdlObject)[", parent, "])" );
        int locSize = classes.length;
        for ( int i = 0; i < locSize; i++ )
        {
            boolean locToMap = true;
            switch ( m_currentClassType )
            {
            case RMI_IDL_VALUE_TYPES_TYPE:
            case RMI_IDL_CUSTOM_VALUE_TYPES_TYPE:
                if ( java.io.Serializable.class.equals( classes[ i ] )
                      || java.io.Externalizable.class.equals( classes[ i ] ) )
                {
                    locToMap = false;
                }
                break;

            default:
            }
            // *** Map the Class to implement ***
            if ( locToMap )
            {
                map_implement( classes[ i ], parent );
            }
        }
    }

    /**
     * Map the implemented class in the case of a class
     *
     * @param c The class to map
     * @param parent The parent node in the tree
     */
    public void map_implement( final Class c, final IdlObject parent )
    {
        display( "MappingAPI::map_implement((Class)[", c, "], (IdlObject)[", parent, "])" );
        // *** Create the idl objects ***
        IdlObject locIdlObj = null;
        // *** Find an associated class or parse it ***
        String locIdlFullName = null;
        locIdlFullName = ( String ) m_javaParser.getMappingNames().get( c.getName() );
        if ( locIdlFullName != null )
        {
            locIdlObj = return_existing_object( locIdlFullName );
            if ( locIdlObj == null )
            {
                throw new CompilationException( "<1>Coherance error between hashtable and name"
                + " (map_implement) locIdlFullName=" + locIdlFullName );
            }
            else
            {
                int ctype = setClassType( c );
                if ( ctype == RMI_IDL_VALUE_TYPES_TYPE || ctype == RMI_IDL_CUSTOM_VALUE_TYPES_TYPE )
                {
                    IdlValueInheritance locIdlInheritance = new IdlValueInheritance( parent );
                    locIdlInheritance.addIdlObject( locIdlObj );
                    ( ( IdlValue ) parent ).addInheritance( locIdlInheritance );
                }
                else
                {
                    if ( ctype == RMI_IDL_ABSTRACT_INTERFACE_TYPE )
                    {
                        ( ( IdlValue ) parent ).supports().add( locIdlObj );
                    }
                    else
                    {
                        System.out.println( "<2>Coherance error for inheritance (map_implement)"
                              + " ctype=" + toString( ctype ) );
                    }
                }
            }
        }
        else
        {
            final ParserResult result = parseClass( c );
            locIdlObj = result.getResult();

            // Find the good object in this tree
            locIdlFullName = ( String ) m_javaParser.getMappingNames().get( c.getName() );
            if ( locIdlFullName != null )
            {
                locIdlObj = return_existing_object( locIdlFullName );
                // Add it to the include files
                add_idl_object_as_first( m_javaParser.getIdlTreeRoot(),
                      result.makeRootInclude() );
                // Add it to the tree
                int ctype = setClassType( c );
                if ( ctype == RMI_IDL_VALUE_TYPES_TYPE || ctype == RMI_IDL_CUSTOM_VALUE_TYPES_TYPE )
                {
                    IdlValueInheritance locIdlInheritance = new IdlValueInheritance( parent );
                    locIdlInheritance.addIdlObject( locIdlObj );
                    ( ( IdlValue ) parent ).addInheritance( locIdlInheritance );
                    map_value_type( c, null, locIdlObj );
                }
                else
                {
                    if ( ctype == RMI_IDL_ABSTRACT_INTERFACE_TYPE )
                    {
                        ( ( IdlValue ) parent ).supports().add( locIdlObj );
                    }
                    else
                    {
                        if ( ctype == RMI_IDL_REMOTE_INTERFACE_TYPE )
                        {
                            parent.addIdlObject( locIdlObj );
                            map_interface( c, locIdlObj );
                        }
                        else
                        {
                            System.out.println( "<!>Coherance error for inheritance"
                                  + " (map_implement) ctype=" + toString( ctype ) );
                            Thread.dumpStack();
                        }
                    }
                }
            }
            else
            {
                throw new CompilationException(
                    "<!>Coherance error between hashtable and array name"
                    + " (map_implement) c=" + c );
            }
        }
    }

    /**
     * Map a #pragma id flag
     *
     * @param str The full name of the class
     * @param parent The parent node in the tree
     */
    public void map_id( final String str, final IdlObject parent, final Class c )
    {
        display( "MappingAPI::map_id((String)[", str, "], (IdlObject)[", parent,
                "], (Class)[", c, "])" );
        String locStr;
        switch ( setClassType( c ) )
        {
        case RMI_IDL_ABSTRACT_INTERFACE_TYPE :
        case RMI_IDL_REMOTE_INTERFACE_TYPE:
        case RMI_IMPLEMENTATION_CLASS:
        case NON_CONFORMING_TYPE:
            locStr = "RMI:" + str + ":0000000000000000";
            break;

        case RMI_IDL_VALUE_TYPES_TYPE:
        case RMI_IDL_CUSTOM_VALUE_TYPES_TYPE:
        case RMI_IDL_EXCEPTION_TYPE:
        case CORBA_COMPLEX_TYPE:
        case RMI_IDL_ARRAYS_TYPE:
            locStr = RepoIDHelper.getRepoID( c );
            if ( locStr == null )
            {
                locStr = "RMI:" + str + ":0000000000000000";
            }
            break;

        default:
            locStr = "";
            return;
        }
        // Assign the id
        parent._id = locStr;
    }

    /**
     * Maps the methods of the class
     *
     * @param methods The array of class that are the methods
     * @param parent The parent node in the tree
     */
    public void map_methods( final Method[] rawMethods, final IdlObject parent, final Class c )
    {
        display( "MappingAPI::map_methods((Method[])[", rawMethods, "], (IdlObject)[", parent,
                "], (Class)[", c, "])" );
        final Method[] methods = filterMethods( rawMethods, c );
        // Map the accessors
        if ( methods.length != 0 )
        {
            map_accessors( methods, parent );
        }
        // Map the methods
        for ( int i = 0; i < methods.length; i++ )
        {
            if ( methods[i].getName().length() > 2 && methods[i].getName().startsWith( "is" ) )
            {
                boolean pair_ok = false;
                String pair = "set" + methods[ i ].getName().substring( 2,
                      methods[ i ].getName().length() );
                for ( int j = 0; j < methods.length; j++ )
                {
                    if ( methods[ j ].getName().equals( pair ) )
                    {
                        pair_ok = true;
                    }
                }
                if ( !pair_ok )
                {
                    map_method( methods[ i ], parent, c );
                }
            }

            if ( methods[i].getName().length() > 3 && methods[i].getName().startsWith( "get" ) )
            {
                if ( !throws_remote_exception( methods[ i ] ) )
                {
                    boolean pair_ok = false;
                    String pair = "set" + methods[ i ].getName().substring( 2,
                          methods[ i ].getName().length() );
                    for ( int j = 0; j < methods.length; j++ )
                    {
                        if ( methods[ j ].getName().equals( pair ) )
                        {
                            pair_ok = true;
                        }
                    }
                    if ( !pair_ok )
                    {
                        map_method( methods[ i ], parent, c );
                    }
                }
            }
            if ( !( methods[i].getName().startsWith( "is" )
                 && methods[i].getName().length() > 2
                 && methods[i].getReturnType().getName().equals( "boolean" ) )
                 && !( methods[i].getName().startsWith( "set" )
                       && methods[i].getName().length() > 3 )
                 && !( methods[i].getName().startsWith( "get" )
                       && methods[i].getName().length() > 3 ) )
            {
                map_method( methods[ i ], parent, c );
            }
        }
        // Map the methods that seem originally to be accessors but are not
        final int size = m_definitelyNotAccessors.size();
        for ( int i = 0; i < size; i++ )
        {
            map_method( ( Method ) m_definitelyNotAccessors.get( i ), parent, c );
        }
    }

    /**
     * Maps the accessors of the class
     *
     * @param methods The array of class that are the ptential accessors
     * @param parent The parent node in the tree
     */
    public void map_accessors( final Method[] methods, final IdlObject parent )
    {
        display( "MappingAPI::map_accessors((Method[])[", methods,
                "], (IdlObject)[", parent, "])" );
        // Sort in two Lists the accessor methods
        List locReadingAccessors = new ArrayList();
        List locWritingAccessors = new ArrayList();
        int locSize = methods.length;
        for ( int i = 0; i < locSize; i++ )
        {
            if ( methods[ i ].getName().length() > 3 )
            {
                // Processes the writing accessor
                if ( methods[ i ].getName().startsWith( "set" ) )
                {
                    locWritingAccessors.add( methods[ i ] );
                }
                // Processes the reading accessor
                if ( methods[i].getName().length() > 3 && methods[i].getName().startsWith( "get" ) )
                {
                    if ( throws_remote_exception( methods[ i ] ) )
                    {
                        locReadingAccessors.add( methods[ i ] );
                    }
                }
                if ( methods[i].getName().length() > 2 && methods[i].getName().startsWith( "is" ) )
                {
                    if ( methods[ i ].getReturnType().getName().equals( "boolean" ) )
                    {
                        String pair = "set" + methods[ i ].getName().substring( 2,
                              methods[ i ].getName().length() );
                        for ( int j = 0; j < methods.length; j++ )
                        {
                            if ( methods[ j ].getName().equals( pair ) )
                            {
                                locReadingAccessors.add( methods[ i ] );
                                break;
                            }
                        }
                    }
                }
            }
        }
        // *** Processes the accessors ***
        locSize = locReadingAccessors.size();
        int locSize1 = locWritingAccessors.size();
        // *** Check if each supposed writing accessor is a a function or an accessor ***
        for ( int i = 0; i < locSize1; i++ )
        {
            Method locWriteAccesor = ( Method ) locWritingAccessors.get( i );
            boolean no_reader = true;
            for ( int j = 0; j < locSize; j++ )
            {
                Method locMeth = ( Method ) locReadingAccessors.get( j );
                // *** Check if the writing accessor name is the same as the reading one ***
                if ( split_accessor_name( locMeth.getName() ).equals(
                      split_accessor_name( locWriteAccesor.getName() ) ) )
                {
                    if ( locMeth.getParameterTypes().length == 0 )
                    {
                        Class [] paramW = locWriteAccesor.getParameterTypes();
                        if ( paramW.length == 1 )
                        {
                            Class ret = locMeth.getReturnType();
                            if ( ret.equals( paramW[ 0 ] ) )
                            {
                                no_reader = false;
                                break;
                            }
                        }
                    }
                }
            }
            if ( no_reader )
            {
                m_definitelyNotAccessors.add( locWriteAccesor );
            }
        }
        for ( int i = 0; i < locSize; i++ )
        {
            Method locMeth = ( Method ) locReadingAccessors.get( i );
            // *** Checks the rules for read-only property ***
            // 1 - No arguments
            if ( locMeth.getParameterTypes().length != 0 )
            {
                m_definitelyNotAccessors.add( locMeth );
                continue;
            }
            // 2 - Non-void return type
            if ( locMeth.getReturnType().getName().equals( "void" ) )
            {
                m_definitelyNotAccessors.add( locMeth );
                continue;
            }
            // 3 - Does not throw any checked exceptions except for
            // java.rmi.RemoteException and its subclasses
            Class [] locExceptions = locMeth.getExceptionTypes();
            boolean locOtherException = false;
            int locExceptionsSize = locExceptions.length;
            for ( int j = 0; j < locExceptionsSize; j++ )
            {
                if ( !java.rmi.RemoteException.class.isAssignableFrom( locExceptions[ j ] ) )
                {
                    locOtherException = true;
                    break;
                }
            }
            if ( locOtherException )
            {
                m_definitelyNotAccessors.add( locMeth );
                continue;
            }
            // 4 - Is an associated "set" method existing?
            boolean locSetMethodExist = false;
            Class locGoodSetArgType = null;
            for ( int j = 0; j < locSize1; j++ )
            {
                Method locWriteAccesor = ( Method ) locWritingAccessors.get( j );
                Class locSetArgType = null;
                // *** Check if the writing accessor name is the same as the reading one ***
                if ( !split_accessor_name( locMeth.getName() ).equals(
                      split_accessor_name( locWriteAccesor.getName() ) ) )
                {
                    continue;
                }
                // *** Applies the rules for write property ***
                // 1 - A single argument
                if ( locWriteAccesor.getParameterTypes().length != 1 )
                {
                    if ( !m_definitelyNotAccessors.contains( locWriteAccesor ) )
                    {
                        m_definitelyNotAccessors.add( locWriteAccesor );
                    }
                    continue;
                }
                locSetArgType = locWriteAccesor.getParameterTypes() [ 0 ];
                // 2 - A void return type
                if ( !locWriteAccesor.getReturnType().getName().equals( "void" ) )
                {
                    if ( !m_definitelyNotAccessors.contains( locWriteAccesor ) )
                    {
                        m_definitelyNotAccessors.add( locWriteAccesor );
                    }
                    continue;
                }
                // 3 - Does not throw any checked exceptions except for
                // java.rmi.RemoteException and its subclasses
                Class [] locExceptions1 = locWriteAccesor.getExceptionTypes();
                boolean locOtherException1 = false;
                int locExceptionsSize1 = locExceptions1.length;
                for ( int k = 0; k < locExceptionsSize1; k++ )
                {
                    if ( !java.rmi.RemoteException.class.isAssignableFrom( locExceptions1[ k ] ) )
                    {
                        locOtherException1 = true;
                        break;
                    }
                }
                if ( locOtherException1 )
                {
                    if ( !m_definitelyNotAccessors.contains( locWriteAccesor ) )
                    {
                        m_definitelyNotAccessors.add( locWriteAccesor );
                    }
                    continue;
                }
                // 4 - The result type of the get must be the same as the parameter type
                // of the set
                if ( locSetArgType.equals( locMeth.getReturnType() ) )
                {
                    locSetMethodExist = true;
                    locGoodSetArgType = locSetArgType;
                }
                else
                {
                    if ( !m_definitelyNotAccessors.contains( locWriteAccesor ) )
                    {
                        m_definitelyNotAccessors.add( locWriteAccesor );
                    }
                }
            }
            // *** Process the name ***
            String locName = split_accessor_name( locMeth.getName() );
            boolean anyStep = false;
            // 1 - Process name
            locName = process_name( locName );
            // 2 - Differing case
            if ( stringContained( m_knownFields, locName, false )
                  && !stringContained( m_knownFields, locName, true ) )
            {
                IdlObject._case_sensitive = false;
                IdlObject locExistingObj = parent.returnObject( locName, true );
                IdlObject._case_sensitive = true;
                if ( locExistingObj != null )
                {
                    // Processing differring case
                    locExistingObj._name = process_name_differing_in_case( locExistingObj._name );
                    m_knownFields.add( locExistingObj._name );
                }
                locName = process_name_differing_in_case( locName );
                m_knownFields.add( locName );
                anyStep = true;
            }
            // e - Checks if the method name is already known in the field List
            // Case of the colliding
            if ( stringContained( m_knownMethods, locName, false ) )
            {
                display( "3 - Overloading " + locName );
                // Processes colliding case
                IdlObject locIdlObj = parent.returnObject( locName, true );
                if ( locIdlObj != null )
                {
                    if ( locIdlObj instanceof IdlOp )
                    {
                        locName = process_c_or_f_colliding_with_method_name( locName );
                    }
                }
            }
            if ( !anyStep )
            {
                m_knownFields.add( locName );
            }
            // *** Create the idlAttribute ***
            IdlObject locIdlReadOnlyAttribute = new IdlAttribute( parent );
            display( "accessor name : " + locName );
            if ( locSetMethodExist )
            {
                // Create an readonly IdlAttribute
                locIdlReadOnlyAttribute._name = locName;
                ( ( IdlAttribute ) locIdlReadOnlyAttribute ).readOnly( false );
                locIdlReadOnlyAttribute.opaque( (
                      ( Method ) locReadingAccessors.get( i ) ).getName() );
                map_type( locGoodSetArgType, locIdlReadOnlyAttribute );
            }
            else
            {
                // Create an readonly IdlAttribute
                locIdlReadOnlyAttribute._name = locName;
                ( ( IdlAttribute ) locIdlReadOnlyAttribute ).readOnly( true );
                locIdlReadOnlyAttribute.opaque( (
                      ( Method ) locReadingAccessors.get( i ) ).getName() );
                map_type( locMeth.getReturnType(), locIdlReadOnlyAttribute );
            }
            // Add it to the tree
            parent.addIdlObject( locIdlReadOnlyAttribute );
        }
    }

    /**
     * Map the method to an idl tree format node
     *
     * @param m The method to map
     * @param parent The parent node in the tree
     */
    public void map_method( final Method m, final IdlObject parent, final Class c )
    {
        display( "MappingAPI::map_method((Method)[", m, "], (IdlObject)[", parent,
                "], (Class)[", c, "])" );
        if ( Modifier.isPublic( m.getModifiers() ) )
        {
            IdlObject locIdlMethod = new IdlOp( parent );
            String locMethodName = m.getName();
            String locOriginalMethodName = locMethodName;
            display( "method name : " + locMethodName );
            display( "MappingAPI::map_method-->Not an accessor" );
            // *** Processes the name ***
            boolean anyStep = false;
            // 1 - Processes method name
            locMethodName = process_name( locMethodName );
            locOriginalMethodName = locMethodName;
            display( "MappingAPI::map_method-->1 - Process name" );
            // 2 - Processes the differing case
            if ( stringContained( m_knownMethods, locMethodName, false )
                  && !stringContained( m_knownMethods, locMethodName, true ) )
            {
                IdlObject._case_sensitive = false;
                IdlObject locIdlObj = parent.returnObject( locMethodName, true );
                IdlObject._case_sensitive = true;
                if ( locIdlObj != null )
                {
                    if ( locIdlObj instanceof IdlOp )
                    {
                        // Processing differring case
                        String locOldName = locIdlObj._name;
                        locIdlObj._name = process_name_differing_in_case( locIdlObj._name );
                        m_knownMethods.add( locIdlObj._name );
                        m_knownMethodsTable.put( locIdlObj._name, locOldName );
                    }
                }
                String locOldName = locMethodName;
                locMethodName = process_name_differing_in_case( locMethodName );
                m_knownMethods.add( locMethodName );
                m_knownMethodsTable.put( locMethodName, locOldName );
                anyStep = true;
            }
            display( "MappingAPI::map_method-->2 - Differring case" );
            // 4 - Checks if the method name is already known (overloading)
            Class superClz = c.getSuperclass();
            if ( superClz != null )
            {
                Method[] allMethods = superClz.getMethods();
                for ( int i = allMethods.length; --i >= 0; )
                {
                    if ( allMethods[ i ].getName().equals( locMethodName ) )
                    {
                        locMethodName = process_overloaded_method_name( m, locMethodName );
                        break;
                    }
                }
            }
            Class [] itfClz = c.getInterfaces();
            if ( itfClz.length != 0 )
            {
                for ( int j = itfClz.length; --j >= 0; )
                {
                    Method[] allMethods = itfClz[ j ].getMethods();

                    for ( int i = allMethods.length; --i >= 0; )
                    {
                        if ( allMethods[ i ].getName().equals( locMethodName ) )
                        {
                            locMethodName = process_overloaded_method_name( m, locMethodName );
                            break;
                        }
                    }
                }
            }
            // 3 - Checks if the method name is already known (overloading)
            if ( stringContained( m_knownMethods, locMethodName, false ) )
            {
                // Processes overloading case
                IdlObject._case_sensitive = false;
                IdlObject locIdlObj = parent.returnObject( locMethodName, true );
                IdlObject._case_sensitive = true;
                // Search the object with the same name
                if ( locIdlObj != null )
                {
                    if ( locIdlObj instanceof IdlOp )
                    {
                        String locOldName = locIdlObj._name;

                        // Overload the name of the occurrence found in the tree
                        locIdlObj._name = process_overloaded_method_name(
                              get_associated_method_class( locIdlObj._name ), locIdlObj._name );
                        m_knownMethods.add( locIdlObj._name );
                        m_knownMethodsTable.put( locIdlObj._name, locOldName );
                    }
                }
                String locOldName = locMethodName;
                // Overload the current method name
                locMethodName = process_overloaded_method_name( m, locMethodName );
                m_knownMethods.add( locMethodName );
                m_knownMethodsTable.put( locMethodName, locOldName );
                anyStep = true;
            }
            display( "MappingAPI::map_method-->3 - Overloading" );
            // 4 - Checks if the method name is already known in the field List
            // Case of the colliding
            if ( stringContained( m_knownFields, locMethodName, false ) )
            {
                // Processes colliding case
                IdlObject locIdlObj = parent.returnObject( locMethodName, true );
                if ( locIdlObj != null )
                {
                    if ( locIdlObj instanceof IdlAttribute )
                    {
                        locIdlObj._name =
                              process_c_or_f_colliding_with_method_name( locIdlObj._name );
                        m_knownFields.add( locIdlObj._name );
                    }
                }
            }
            display( "MappingAPI::map_method-->4 - Colliding" );
            // Add the name to the method name List
            if ( !anyStep )
            {
                m_knownMethods.add( locMethodName );
                m_knownMethodsTable.put( locMethodName, m );
            }
            // Assigns the method name
            locIdlMethod._name = locMethodName;
            // *** Processes the params ***
            // 1 - Processes the return type
            map_type( m.getReturnType(), locIdlMethod );
            display( "MappingAPI::map_method-->Map return type" );
            // 2 - Processes the params
            map_parameters( m.getParameterTypes(), locIdlMethod );
            display( "MappingAPI::map_method-->Map parameters" );
            // *** Processes the exceptions ***
            map_exceptions( m.getExceptionTypes(), locIdlMethod );
            display( "MappingAPI::map_method-->Map Exceptions" );
            // *** Add to the tree ***
            locIdlMethod.opaque( m.getName() );
            parent.addIdlObject( locIdlMethod );
        }
    }

    /**
     * Map the constructors
     *
     * @param constructors The constructors to map
     * @param parent The parent node in the tree
     * @param class_name The class name that is the same for the constructors
     */
    public void map_constructors( final Constructor[] constructors, final IdlObject parent,
            final String class_name )
    {
        display( "MappingAPI::map_constructors((Constructor)[", constructors,
                "], (IdlObject)[", parent, "], (String)[", class_name, "])" );
        int locSize = constructors.length;
        for ( int i = 0; i < locSize; i++ )
        {
            if ( !Modifier.isPrivate( constructors[ i ].getModifiers() ) )
            {
                map_constructor( constructors[ i ], parent, class_name );
            }
        }
    }

    /**
     * Map the constructor to an idl tree format node
     *
     * @param c The constructor to map
     * @param parent The parent node in the tree
     * @param class_name The class name that is the same for the constructors
     */
    public void map_constructor( final Constructor c, final IdlObject parent,
            final String class_name )
    {
        display( "MappingAPI::map_constructor((Constructor)[", c, "], (IdlObject)[", parent,
                "], (String)[", class_name, "])" );
        IdlFactory locIdlFac = new IdlFactory( parent );
        // *** Assigns the method name ***
        locIdlFac._name = process_name( "create" );
        locIdlFac._name = process_overloaded_constructor_name( c, locIdlFac._name );
        // *** Processes the params ***
        map_parameters( c.getParameterTypes(), locIdlFac );
        display( "MappingAPI::map_constructor-->Map parameters" );
        parent.addIdlObject( locIdlFac );
    }

    /**
     * Maps the given parameters in the idl tree in the array order
     *
     * @param parameters The classes tab for the params type
     * @param parent The parent node in the tree
     */
    public void map_parameters( final Class[] parameters, final IdlObject parent )
    {
        display( "MappingAPI::map_parameters((Class[])[", parameters,
                "], (IdlObject)[", parent, "])" );
        int locSize = parameters.length;
        for ( int i = 0; i < locSize; i++ )
        {
            map_parameter( parameters[ i ], parent, i );
        }
    }

    /**
     * Maps the given parameter in the idl tree
     *
     * @param c The class for the param type
     * @param parent The parent node in the tree
     */
    public void map_parameter( final Class c, final IdlObject parent, final int arg_nb )
    {
        display( "MappingAPI::map_parameter((Class)[", c, "], (IdlObject)[", parent,
                "], (int)[", NumberCache.getInteger( arg_nb ), "])" );
        // Create the idlParam object
        IdlObject locIdlParam = new IdlParam( parent );
        // Give a name to this param
        if ( arg_nb >= 0 )
        {
            locIdlParam._name = "arg" + arg_nb;
        }
        // Map the attr of the param (in)
        ( ( IdlParam ) locIdlParam ).param_attr( 0 );
        // Map the type
        map_type( c, locIdlParam );
        // Add the idlParam to the tree
        parent.addIdlObject( locIdlParam );
    }

    /**
     * Map the exceptions threw by the methods
     *
     * @param classes The table of exceptions
     * @param parent The parent node
     */
    public void map_exceptions( final Class[] classes, final IdlObject parent )
    {
        display( "MappingAPI::map_exceptions((Class[])[", classes,
                "], (IdlObject)[", parent, "])" );
        int locSize = classes.length;
        IdlObject locIdlRaises = null;
        for ( int i = 0; i < locSize; i++ )
        {
            // Depends on the exception type
            boolean locToMap = true;
            switch ( m_currentClassType )
            {
            case RMI_IDL_REMOTE_INTERFACE_TYPE:
                if ( java.rmi.RemoteException.class.isAssignableFrom( classes[ i ] ) )
                {
                    locToMap = false;
                }
                break;

            default:
                locToMap = true;
            }
            // Map the exception
            if ( locToMap )
            {
                if ( locIdlRaises == null )
                {
                    locIdlRaises = new IdlRaises( parent );
                    // *** Add it to the parent node ***
                    parent.addIdlObject( locIdlRaises );
                }
                map_exception( classes[ i ], locIdlRaises );
            }
        }
    }

    /**
     * Map an exception to the existing idl type
     *
     * @param c The exception class
     * @param parent The parent node in the tree
     */
    public void map_exception( final Class c, final IdlObject parent )
    {
        display( "MappingAPI::map_exception((Class)[", c, "], (IdlObject)[", parent, "])" );
        // *** Get the name of the Exception ***
        String locExcepName = get_relative_name( c.getName() );
        // *** Process the suffix of the exception name ***
        if ( !ReflectionUtils.isAssignableFrom( "org.omg.CORBA.SystemException", c )
              && !ReflectionUtils.isAssignableFrom( "org.omg.CORBA.UserException", c ) )
        {
            locExcepName = process_exception_suffix( locExcepName );
        }
        // *** Processing "name rules" ***
        // 1 - Process name
        locExcepName = process_name( locExcepName );
        // 2 - Processing differring case
        // *** Check if it already exists in the compilation tree ***
        IdlObject locIdlObj = return_existing_object( 
                process_new_full_class_name( c, locExcepName ) );
        if ( locIdlObj == null )
        {
            // Parse & map the unknown class
            final ParserResult result = parseClass( c );
            locIdlObj = result.getResult();

            // Get the good object (Exception = value type + exception
            locIdlObj = locIdlObj.returnObject(
                  process_new_full_class_name( c, locExcepName ), true );
            // Add it to the include files
            add_idl_object_as_first( m_javaParser.getIdlTreeRoot(),
                  result.makeRootInclude() );
        }
        // *** Set opaque type if not intialized ***
        if ( locIdlObj.opaque() == null )
        {
            locIdlObj.opaque( c.getName() );
        }
        // *** Get the exception ***
        parent.addIdlObject( locIdlObj );
    }

    /**
     * Map an exception class to the existing idl type
     *
     * @param c The exception class
     * @param parent The parent node in the tree
     */
    public void map_exception_class( final Class c, final IdlObject parent )
    {
        display( "MappingAPI::map_exception_class((Class)[", c, "], (IdlObject)[", parent, "])" );
        // ** Create the idl object value type as defined in the spec ***
        IdlObject locIdlExcep = new IdlValue( parent );
        // *** Get the relative class name of the Exception ***
        String locExcepName = get_relative_name( c.getName() );
        // *** Processing "name rules" ***
        // 1 - Process name
        locExcepName = process_name( locExcepName );
        // *** Assign the exception name ***
        locIdlExcep._name = locExcepName;
        locIdlExcep.opaque( c.getName() );
        // *** Add it to the tree ***
        parent.addIdlObject( locIdlExcep );
        // *** Add to the hashtable ***
        m_javaParser.getMappingNames().put( c.getName(),
              process_new_full_class_name( c, locIdlExcep._name ) );
        // ----------------- Exception type ----------------
        // *** Create the idl object exception as defined in the spec ***
        IdlObject locIdlExcep1 = new IdlExcept( parent );
        locIdlExcep1.opaque( c.getName() );
        // *** Process the name ***
        locIdlExcep1._name = process_exception_suffix( locExcepName );
        // *** Add it to the tree ***
        parent.addIdlObject( locIdlExcep1 );
        // ----------- Parse inheritance, constants.... ------
        // *** Mapping of inheritance ***
        if ( c.getSuperclass() != null )
        {
            map_inheritance( c.getSuperclass(), locIdlExcep );
        }
        // *** Mapping of Constants ***
        map_constants( c.getDeclaredFields(), locIdlExcep );
        // *** Map the fields ***
        map_fields( c.getDeclaredFields(), locIdlExcep );
        // *** Map constructors ***
        map_constructors( c.getConstructors(), locIdlExcep, locExcepName );
        // *** Map the methods ***
        map_methods( c.getDeclaredMethods(), locIdlExcep, c );
        // *** Add the previous object as field ***
        IdlObject locIdlExcepValue = new IdlStructMember( locIdlExcep1 );
        locIdlExcepValue._name = "value";
        locIdlExcepValue.type( locIdlExcep );
        // *** Map the id ***
        map_id( c.getName(), locIdlExcep, c );
        locIdlExcep1.addIdlObject( locIdlExcepValue );
        locIdlExcep1.opaque( c.getName() );
        // *** Give to the idl root the name that will be used for generating idl file ***
        String pkgName = get_package_name( c.getName() );
        if ( pkgName.equals( "" ) )
        {
            m_javaParser.getIdlTreeRoot()._name = locIdlExcep1._name;
        }
        else
        {
            m_javaParser.getIdlTreeRoot()._name =
                  get_package_name( c.getName() ) + "." + locIdlExcep1._name;
        }
    }

    /**
     * Map a corba exception class to the existing idl type
     *
     * @param c The exception class
     * @param parent The parent node in the tree
     */
    public void map_corba_exception_class( final Class c, final IdlObject parent )
    {
        display( "MappingAPI::map_corba_exception_class((Class)[", c,
                "], (IdlObject)[", parent, "])" );
        // ** Create the idl object value type as defined in the spec ***
        IdlObject locIdlExcep = new IdlExcept( parent );
        // *** Get the relative class name of the Exception ***
        String locExcepName = get_relative_name( c.getName() );
        // *** Processing "name rules" ***
        // 1 - Process name
        locExcepName = process_name( locExcepName );
        // *** Assign the exception name ***
        locIdlExcep._name = locExcepName;
        locIdlExcep.opaque( c.getName() );
        // *** Add it to the tree ***
        parent.addIdlObject( locIdlExcep );
        // *** Add to the hashtable ***
        m_javaParser.getMappingNames().put( c.getName(),
              process_new_full_class_name( c, locIdlExcep._name ) );
        // ----------- Parse inheritance, constants.... ------
        // *** Mapping of inheritance ***
        if ( c.getSuperclass() != null )
        {
            map_inheritance( c.getSuperclass(), locIdlExcep );
        }
        // *** Mapping of Constants ***
        map_constants( c.getDeclaredFields(), locIdlExcep );
        // *** Map the fields ***
        map_fields( c.getDeclaredFields(), locIdlExcep );
        // *** Map constructors ***
        map_constructors( c.getConstructors(), locIdlExcep, locExcepName );
        // *** Map the methods ***
        map_methods( c.getDeclaredMethods(), locIdlExcep, c );
        // *** Map the id ***
        map_id( c.getName(), locIdlExcep, c );
        // *** Give to the idl root the name that will be used for generating idl file ***
        String pkgName = get_package_name( c.getName() );
        if ( pkgName.equals( "" ) )
        {
            m_javaParser.getIdlTreeRoot()._name = locIdlExcep._name;
        }
        else
        {
            m_javaParser.getIdlTreeRoot()._name =
                  get_package_name( c.getName() ) + "." + locIdlExcep._name;
        }
    }

    /**
     * Map the fields to equivalent ild type
     *
     * @param fields The fields of the class to map
     * @param parent The parent node in the tree
     */
    public void map_fields( final Field[] fields, final IdlObject parent )
    {
        display( "MappingAPI::void map_fields((Field[])[", fields,
                "], (IdlObject)[", parent, "])" );
        // The rules expect an ordering for fields:
        // 1 - all non-constant fields whose Java type is a primitive precede
        // all other non-constant field
        int locSize = fields.length;
        for ( int i = 0; i < locSize; i++ )
        {
            if ( fields[ i ].getType().isPrimitive() )
            {
                map_field( fields[ i ], parent );
            }
        }
        for ( int i = 0; i < locSize; i++ )
        {
            if ( !fields[ i ].getType().isPrimitive() )
            {
                map_field( fields[ i ], parent );
            }
        }
    }

    /**
     * Map the given field to equivalent ild type
     *
     * @param f The field to map
     * @param parent The parent node in the tree
     */
    public void map_field( final Field f, final IdlObject parent )
    {
        display( "MappingAPI::void map_field((Field)[", f, "], (IdlObject)[", parent, "])" );
        IdlObject locIdlObj = null;
        if ( m_currentClassType == RMI_IDL_CUSTOM_VALUE_TYPES_TYPE )
        {
            locIdlObj = new IdlStateMember( parent );
            int locModifiers = f.getModifiers();
            if ( !Modifier.isStatic( locModifiers ) && !Modifier.isFinal( locModifiers )
                  && !Modifier.isTransient( locModifiers ) && Modifier.isPublic( locModifiers ) )
            {
                ( ( IdlStateMember ) locIdlObj ).public_member( true );
                // *** Process the name ***
                String locFieldName = f.getName();
                boolean anyStep = false;
                // 1 - Process the name
                locFieldName = process_name( locFieldName );
                // 2 - Processes the differing case (Fields level)
                if ( stringContained( m_knownFields, locFieldName, false )
                      && !stringContained( m_knownFields, locFieldName, true ) )
                {
                    IdlObject._case_sensitive = false;
                    IdlObject locIdlObj1 = parent.returnObject( locFieldName, true );
                    IdlObject._case_sensitive = true;
                    if ( locIdlObj1 != null )
                    {
                        if ( locIdlObj1 instanceof IdlConst || locIdlObj1 instanceof IdlAttribute
                              || locIdlObj1 instanceof IdlStateMember )
                        {
                            // Processing differring case
                            locIdlObj1._name = process_name_differing_in_case( locIdlObj1._name );
                            m_knownFields.add( locIdlObj1._name );
                        }
                    }
                    locFieldName = process_name_differing_in_case( locFieldName );
                    m_knownFields.add( locFieldName );
                    anyStep = true;
                }
                if ( !anyStep )
                {
                    m_knownFields.add( locFieldName );
                }
                // *** Assign the name ***
                locIdlObj._name = locFieldName;
                if ( f.getType().isPrimitive() )
                {
                    map_primitive_type( f.getType(), locIdlObj );
                }
                else
                {
                    map_type( f.getType(), locIdlObj );
                }
                // Add the object to the tree
                parent.addIdlObject( locIdlObj );
            }
        }
        if ( m_currentClassType == RMI_IDL_VALUE_TYPES_TYPE
              || m_currentClassType == RMI_IDL_EXCEPTION_TYPE )
        {
            locIdlObj = new IdlStateMember( parent );
            int locModifiers = f.getModifiers();
            if ( !Modifier.isStatic( locModifiers ) && !Modifier.isFinal( locModifiers )
                  && !Modifier.isTransient( locModifiers ) )
            {
                if ( Modifier.isPublic( locModifiers ) )
                {
                    ( ( IdlStateMember ) locIdlObj ).public_member( true );
                }
                else
                {
                    ( ( IdlStateMember ) locIdlObj ).public_member( false );
                }
                // *** Process the name ***
                String locFieldName = f.getName();
                boolean anyStep = false;
                // 1 - Process the name
                locFieldName = process_name( locFieldName );
                // 2 - Processes the differing case (Fields level)
                if ( stringContained( m_knownFields, locFieldName, false )
                      && !stringContained( m_knownFields, locFieldName, true ) )
                {
                    IdlObject._case_sensitive = false;
                    IdlObject locIdlObj1 = parent.returnObject( locFieldName, true );
                    IdlObject._case_sensitive = true;
                    if ( locIdlObj1 != null )
                    {
                        if ( locIdlObj1 instanceof IdlConst || locIdlObj1 instanceof IdlAttribute
                              || locIdlObj1 instanceof IdlStateMember )
                        {
                            // Processing differring case
                            locIdlObj1._name = process_name_differing_in_case( locIdlObj1._name );
                            m_knownFields.add( locIdlObj1._name );
                        }
                    }
                    locFieldName = process_name_differing_in_case( locFieldName );
                    m_knownFields.add( locFieldName );
                    anyStep = true;
                }
                if ( !anyStep )
                {
                    m_knownFields.add( locFieldName );
                }
                // *** Assign the name ***
                locIdlObj._name = locFieldName;
                if ( f.getType().isPrimitive() )
                {
                    map_primitive_type( f.getType(), locIdlObj );
                }
                else
                {
                    map_type( f.getType(), locIdlObj );
                }
                // Add the object to the tree
                parent.addIdlObject( locIdlObj );
            }
        }
    }

    /**
     * Map the constants
     *
     * @param fields The fields to map
     * @param parent The parent node in the idl tree
     */
    public void map_constants( final Field[] fields, final IdlObject parent )
    {
        display( "MappingAPI::map_constants((Field)[", fields, "], (IdlObject)[", parent, "])" );
        for ( int i = 0; i < fields.length; i++ )
        {
            final Field field = fields[i];
            final Class type = field.getType();
            final int locModifier = fields[i].getModifiers();

            if ( Modifier.isPublic( locModifier )
                    && Modifier.isFinal( locModifier )
                    && Modifier.isStatic( locModifier )
                    && ( type.isPrimitive() || type.equals( String.class ) ) )
            {
                map_constant( field, parent );
            }
        }
    }

    /**
     * Map a constant field
     *
     * @param c The constant field to map
     * @param parent The parent node in the idl tree
     */
    public void map_constant( final Field c, final IdlObject parent )
    {
        display( "MappingAPI::map_constant((Field)[", c, "], (IdlObject)[", parent, "])" );
        // *** Create the new node ***
        IdlObject locIdlObj = new IdlConst( parent );
        // *** Process the field name ***
        boolean anyStep = false;
        String locConstantName = c.getName();
        // 1 - Process name
        locConstantName = process_name( locConstantName );
        String locOriginalConstantName = locConstantName;
        display( "MappingAPI::map_constant-->1 - Processing name" );
        // 2 - Processes the differing case (Fields level)
        if ( stringContained( m_knownFields, locConstantName, false )
              && !stringContained( m_knownFields, locConstantName, true ) )
        {
            IdlObject._case_sensitive = false;
            IdlObject locIdlObj1 = parent.returnObject( locConstantName, true );
            IdlObject._case_sensitive = true;
            if ( locIdlObj1 != null )
            {
                if ( locIdlObj1 instanceof IdlConst || locIdlObj1 instanceof IdlAttribute )
                {
                    // Processing differring case
                    locIdlObj1._name = process_name_differing_in_case( locIdlObj1._name );
                    m_knownFields.add( locIdlObj1._name );
                }
            }
            locConstantName = process_name_differing_in_case( locConstantName );
            m_knownFields.add( locConstantName );
            anyStep = true;
        }
        display( "MappingAPI::map_constant-->2 - Differring case" );
        if ( !anyStep )
        {
            m_knownFields.add( locConstantName );
        }
        // Assign the name
        locIdlObj._name = locConstantName;
        // *** Map the constant type ***
        boolean string = false;
        if ( java.lang.String.class.isAssignableFrom( c.getType() ) )
        {
            locIdlObj.addIdlObject( new IdlSimple( Token.t_wstring, m_javaParser.getIdlParser() ) );
            string = true;
        }
        else
        {
            map_type( c.getType(), locIdlObj );
        }
        // *** Add the value of the constant ***
        try
        {
            if ( string )
            {
                ( ( IdlConst ) locIdlObj ).expression( "\"" + c.get( c ).toString() + "\"" );
            }
            else
            {
                ( ( IdlConst ) locIdlObj ).expression( c.get( c ).toString() );
            }
        }
        catch ( IllegalAccessException iae )
        {
            ( ( IdlConst ) locIdlObj ).expression( "" );
        }
        catch ( IllegalArgumentException iae )
        {
            ( ( IdlConst ) locIdlObj ).expression( "" );
        }
        // *** Add the idlobj to the tree ***
        parent.addIdlObject( locIdlObj );
    }

    /**
     * Map the corresponding type of the given class
     *
     * @param c The class to map
     * @param parent The parent node in the idl tree
     */
    public void map_type( final Class c, final IdlObject parent )
    {
        display( "MappingAPI::map_type((Class)[", c, "], (IdlObject)[", parent, "])" );
        display( "MappingAPI::map_type-->Name of type : " + c.getName() );
        /*** Check if the class is an exception ***
        System.out.prinln( "map_type "+ c );
        if( Throwable.class.isAssignableFrom( c ) )
        {
            map_exception( c, parent );
            return;
        }
        */
        /** Check whether the class is of type ClassDesc */
        if ( c.getName().equals( "javax.rmi.CORBA.ClassDesc" ) )
        {
            final String msg = "<1> Class " + c.getName()
                  + " is not supported as interface type (see Issue 2479).";
            System.out.println( msg );
            throw new RuntimeException( msg );
        }
        // *** Check if the class is an array ***
        boolean locIsAnArray = c.isArray();
        IdlObject locIdlObj = null;
        if ( !locIsAnArray )
        {
            // *** If the current class is a simple type ***
            if ( c.isPrimitive() )
            {
                map_primitive_type( c, parent );
                return;
            }
            // *** Check if the current class exists in this tree
            //     (important in case of recursive class call) ***
            String locStrName = c.getName();
            IdlObject._case_sensitive = false;
            locIdlObj = m_javaParser.returnObject( get_absolute_idl_name( locStrName ), true );
            if ( locIdlObj != null )
            {
                IdlObject locIdlIdent = new IdlIdent( "", parent, locIdlObj );
                parent.addIdlObject( locIdlIdent );
                IdlObject._case_sensitive = true;
                return;
            }
            IdlObject._case_sensitive = true;
            // *** Check if the current class exists in the tree List ***
            locIdlObj = null;
            String loc_full_class_name = null;
            if ( ( loc_full_class_name =
                  ( String ) m_javaParser.getMappingNames().get( c.getName() ) ) != null )
            {
                locIdlObj = return_existing_object( loc_full_class_name );
                if ( locIdlObj != null )
                {
                    IdlObject locIdlIdent = new IdlIdent( "", parent, locIdlObj );
                    parent.addIdlObject( locIdlIdent );
                    return;
                }
                else
                {
                    final ParserResult result = parseClass( c );
                    final IdlObject locIdlObj1 = result.getResult();
                    // Add it to the include files
                    add_idl_object_as_first( m_javaParser.getIdlTreeRoot(),
                          result.makeRootInclude() );
                }
            }
            else
            {
                // Parse & map the unknown class
                final ParserResult result = parseClass( c );
                final IdlObject locIdlObj1 = result.getResult();

                // Add it to the include files
                add_idl_object_as_first( m_javaParser.getIdlTreeRoot(),
                      result.makeRootInclude() );
            }
            // Check one more time if the class exists now
            loc_full_class_name = ( String ) m_javaParser.getMappingNames().get( c.getName() );
            if ( loc_full_class_name != null )
            {
                locIdlObj = return_existing_object( loc_full_class_name );
                if ( locIdlObj != null )
                {
                    IdlObject locIdlIdent = new IdlIdent( "", parent, locIdlObj );
                    parent.addIdlObject( locIdlIdent );
                    return;
                }
                else
                {
                    final String msg = "<1> Class " + c.getName() + " unreachable";
                    System.out.println( msg );
                    throw new RuntimeException( msg );
                }
            }
            else
            {
                final String msg = "<2> Class " + c.getName() + " unreachable";
                System.out.println( msg );
                throw new RuntimeException( msg );
            }
        }
        else
        {
            // *** Search for it in the hashtable ***
            String locArrayIdlFullName = null;
            locArrayIdlFullName = ( String ) m_javaParser.getMappingNames().get( c.getName() );
            if ( locArrayIdlFullName != null )
            {
                locIdlObj = return_existing_object( locArrayIdlFullName );
                if ( locIdlObj == null )
                {
                    throw new CompilationException(
                        "<1>Coherance error between hashtable and array name"
                        + " (map_type)" );
                }
                else
                {
                    // Add it to the tree
                    IdlObject locIdlIdent = new IdlIdent( "", parent, locIdlObj );
                    parent.addIdlObject( locIdlIdent );
                }
            }
            else
            {
                // *** Parse & map the unknown class ***
                final ParserResult result = parseClass( c );
                locIdlObj = result.getResult();

                // Find the good object in this tree
                locArrayIdlFullName = ( String ) m_javaParser.getMappingNames().get( c.getName() );
                if ( locArrayIdlFullName != null )
                {
                    locIdlObj = return_existing_object( locArrayIdlFullName );
                    // Add it to the include files
                    add_idl_object_as_first( m_javaParser.getIdlTreeRoot(),
                          result.makeRootInclude() );
                    // Add it to the tree
                    IdlObject locIdlIdent = new IdlIdent( "", parent, locIdlObj );
                    parent.addIdlObject( locIdlIdent );
                }
                else
                {
                    throw new CompilationException( "<2>Coherance error between"
                     + " hashtable and array name" );
                }
            }
        }
    }

    /**
     * Map the corresponding type of the given class
     *
     * @param c The class to map
     * @param parent The parent node in the idl tree
     */
    public void map_primitive_type( final Class c, final IdlObject parent )
    {
        display( "MappingAPI::map_primitive_type((Class)[", c, "], (IdlObject)[", parent, "])" );
        // Map to the correct simple type
        String locStr = new String( c.getName() );
        int tc_kind = -1;
        if ( locStr.equals( "String" ) && parent instanceof IdlConst )
        {
            tc_kind = Token.t_wstring;
        }
        else if ( locStr.equals( "void" ) )
        {
            tc_kind = Token.t_void;
        }
        else if ( locStr.equals( "boolean" ) )
        {
            tc_kind = Token.t_boolean;
        }
        else if ( locStr.equals( "char" ) )
        {
            tc_kind = Token.t_wchar;
        }
        else if ( locStr.equals( "byte" ) )
        {
            tc_kind = Token.t_octet;
        }
        else if ( locStr.equals( "short" ) )
        {
            tc_kind = Token.t_short;
        }
        else if ( locStr.equals( "int" ) )
        {
            tc_kind = Token.t_long;
        }
        else if ( locStr.equals( "long" ) )
        {
            tc_kind = Token.t_longlong;
        }
        else if ( locStr.equals( "float" ) )
        {
            tc_kind = Token.t_float;
        }
        else if ( locStr.equals( "double" ) )
        {
            tc_kind = Token.t_double;
        }
        // Create a simple object
        IdlObject locIdlSimple = new IdlSimple( tc_kind, m_javaParser.getIdlParser() );
        // Add it to the parent node
        parent.addIdlObject( locIdlSimple );
        display( "MappingAPI::map_primitive_type" );
    }

    /**
     * Map the array to the idl corresponding type
     * An RMI/IDL is mapped to a "boxed" value type containing an idl
     * sequence.
     *
     * @param c The class to map
     * @param parent The parent node in the idl tree
     * @param nb_dim The number of dimension of the array
     * @return The object value mapped
     */
    public IdlObject map_array( final Class c, final IdlObject parent, final int locSeqNumber )
    {
        display( "MappingAPI::map_array((Class)[", c, "], (IdlObject)[", parent,
                "], (int)[", NumberCache.getInteger( locSeqNumber ), "])" );
        // *** Name of the component type of the array ***
        IdlObject locIdlObj = null;
        Class cmpt = c.getComponentType();
        // *** Direct to the right processing method ***
        if ( !cmpt.isArray() )   // If it is the last dimension with the correct array type
        {
            locIdlObj = map_array1( c, cmpt, parent );
            // *** Add to the hashtable ***
            if ( locSeqNumber == 0 )
            {
                String locIdlRootName = null;
                if ( !cmpt.isPrimitive() )
                {
                    // *** Give the idl root the name of the final seq for the idl file name ***
                    String pkgName = get_package_name( cmpt.getName() );
                    if ( pkgName.equals( "" ) )
                    {
                        locIdlRootName = "org.omg.boxedRMI." + locIdlObj._name;
                        m_javaParser.getMappingNames().put( c.getName(),
                              "::org::omg::boxedRMI::"
                              + ( ( IdlObject ) parent._list.lastElement() )._name );
                    }
                    else
                    {
                        locIdlRootName = "org.omg.boxedRMI." + pkgName + "." + locIdlObj._name;
                        m_javaParser.getMappingNames().put( c.getName(),
                              "::org::omg::boxedRMI"
                              + get_absolute_idl_name( pkgName ) + "::"
                              + ( ( IdlObject ) parent._list.lastElement() )._name );
                    }
                }
                else
                {
                    // *** Give the idl root the name of the final seq for the idl file name ***
                    locIdlRootName = "org.omg.boxedRMI." + locIdlObj._name;
                    // *** Add to the hashtable ***
                    m_javaParser.getMappingNames().put( c.getName(),
                          "::org::omg::boxedRMI::"
                          + ( ( IdlObject ) parent._list.lastElement() )._name );
                }
                // *** Give the idl root the name of the final seq for the idl file name ***
                m_javaParser.getIdlTreeRoot()._name = locIdlRootName;
            }
            return locIdlObj;
        }
        // Case of one more dimension
        locIdlObj = map_array( cmpt, parent, locSeqNumber + 1 );
        int locDimNumber = 1;
        while ( cmpt.isArray() )
        {
            cmpt = cmpt.getComponentType();
            locDimNumber++;
        }
        // *** Map the value type thanks to the previous IdlObject ***
        Package pkg = cmpt.getPackage();
        String locValueTypeName;
        if ( pkg == null || pkg.getName().length() == 0 )
        {
            locValueTypeName = cmpt.getName();
        }
        else
        {
            locValueTypeName = cmpt.getName().substring( pkg.getName().length() + 1 );
        }
        locIdlObj = map_array2( locValueTypeName, locIdlObj, parent,
              locSeqNumber + 1, locDimNumber, c );
        // *** Add to the hashtable ***
        if ( locSeqNumber == 0 )
        {
            // *** Give the idl root the name of the final seq for the idl file name ***
            String pkgName = get_package_name( cmpt.getName() );
            String locIdlRootName = null;
            if ( pkgName.length() == 0 )
            {
                locIdlRootName = "org.omg.boxedRMI." + locIdlObj._name;
                m_javaParser.getMappingNames().put( c.getName(), "::org::omg::boxedRMI::"
                      + ( ( IdlObject ) parent._list.lastElement() )._name );
            }
            else
            {
                locIdlRootName = "org.omg.boxedRMI." + pkgName + "." + locIdlObj._name;
                m_javaParser.getMappingNames().put( c.getName(), "::org::omg::boxedRMI::"
                      + get_absolute_idl_name( pkgName ) + "::"
                      + ( ( IdlObject ) parent._list.lastElement() )._name );
            }
            m_javaParser.getIdlTreeRoot()._name = locIdlRootName;
        }
        return locIdlObj;
    }

    /**
     * Map to the value type
     *
     * @param c The component type f the array
     * @param parent The parent node in the idl tree
     * @return The object (value type) resulting from the mapping
     */
    private IdlObject map_array1( final Class c, final Class cmpt, final IdlObject parent )
    {
        display( "MappingAPI::map_array1((Class)[", c, "], (Class)[", cmpt,
                "], (IdlObject)[", parent, "])" );
        // *** Create an idl object ***
        IdlObject locIdlValueType = new IdlValueBox( parent );
        // *** Build the value type name ***
        String locStr = "seq1_";
        String arrayNm = "[";
        if ( cmpt.isPrimitive() )
        {
            if ( long.class.equals( cmpt ) )
            {
                locStr += "long_long";
            }
            else if ( void.class.equals( cmpt ) )
            {
                locStr += "void";
            }
            else if ( boolean.class.equals( cmpt ) )
            {
                locStr += "boolean";
            }
            else if ( char.class.equals( cmpt ) )
            {
                locStr += "wchar";
            }
            else if ( byte.class.equals( cmpt ) )
            {
                locStr += "octet";
            }
            else if ( short.class.equals( cmpt ) )
            {
                locStr += "short";
            }
            else if ( int.class.equals( cmpt ) )
            {
                locStr += "long";
            }
            else if ( float.class.equals( cmpt ) )
            {
                locStr += "float";
            }
            else if ( double.class.equals( cmpt ) )
            {
                locStr += "double";
            }
        }
        else
        {
            // *** Search for the class in the hashtable ***
            IdlObject locIdlObj = null;
            String locIdlObjName = null;
            locIdlObjName = ( String ) m_javaParser.getMappingNames().get( cmpt.getName() );
            if ( locIdlObjName != null )
            {
                locIdlObj = return_existing_object( locIdlObjName );

                if ( locIdlObj != null )
                {
                    locStr += locIdlObj._name;
                }
                else
                {
                    final String msg = "<!>mapping_api::map_array1-->Error";
                    System.out.println( msg );
                    throw new RuntimeException( msg );
                }
            }
            else
            {
                // Parse & map the unknown class
                final ParserResult result = parseClass( cmpt );
                locIdlObj = result.getResult();

                // Add it to the include files
                add_idl_object_as_first( m_javaParser.getIdlTreeRoot(),
                      result.makeRootInclude() );
                // Find the associated name
                locIdlObjName = ( String ) m_javaParser.getMappingNames().get( cmpt.getName() );
                locIdlObj = return_existing_object( locIdlObjName );
                if ( locIdlObj != null )
                {
                    IdlObject locIdlIdent = new IdlIdent( "", parent, locIdlObj );
                    parent.addIdlObject( locIdlIdent );
                    locStr += locIdlObj._name;
                }
                else
                {
                    final String msg = "<!>mapping_api::map_array1-->Error1";
                    System.out.println( msg );
                    throw new RuntimeException( msg );
                }
            }
        }
        // *** Assign the name to the value type ***
        locIdlValueType._name = locStr;
        // *** Preventing from redefinition ***
        locIdlValueType.use_diese( true );
        // *** Create the sequence to add to the value type ***
        IdlObject locIdlSequence = new IdlSequence( locIdlValueType );
        // *** Map the component type ***
        map_type( cmpt, locIdlSequence );
        // *** Add the type to the value box ***
        ( ( IdlValueBox ) locIdlValueType ).type( locIdlSequence );
        // *** Map the id of the sequence ***
        map_id( c.getName(), locIdlValueType, c );
        // *** Add to the parent node ***
        parent.addIdlObject( locIdlValueType );
        return locIdlValueType;
    }

    /**
     * Map to the value type
     *
     * @param c The array class
     * @param idlObj The previous IdlValueType Object
     * @param parent The parent node in the idl tree
     * @param seq_nb The sequence number to concatenate to the name
     * @param nb_dim The number of dimension of the array
     * @return The idl object mapped
     */
    private IdlObject map_array2( final String name, final IdlObject idlObj,
          final IdlObject parent, final int seq_nb, final int nb_dim, final Class c )
    {
        display( "MappingAPI::map_array2((String)[", name, "], (IdlObject)[", idlObj,
                "], (IdlObject)[", parent, "], (int)[", NumberCache.getInteger( seq_nb ),
                "], (int)[", NumberCache.getInteger( nb_dim ), "], (Class)[", c, "])" );
        // *** Create an idl object ***
        IdlObject locIdlValueType = new IdlValueBox( parent );
        // *** Build the value type name ***
        String locStr = "seq" + ( nb_dim - seq_nb + 1 ) + "_" + name;
        // *** Assign the name to the value type ***
        locIdlValueType._name = locStr;
        // *** Preventing from redefinition ***
        locIdlValueType.use_diese( true );
        // *** Create the sequence to add to the value type ***
        IdlObject locIdlSequence = new IdlSequence( locIdlValueType );
        IdlObject locIdlIdent = new IdlIdent( "", locIdlSequence, idlObj );
        locIdlSequence.addIdlObject( locIdlIdent );
        // *** Add the type to the value box ***
        ( ( IdlValueBox ) locIdlValueType ).type( locIdlSequence );
        // *** Map the id ***
        map_id( c.getName(), locIdlValueType, c );
        // *** Add to the parent node ***
        parent.addIdlObject( locIdlValueType );
        return locIdlValueType;
    }

    /**
     * Return true if this method throws a RemoteException
     */
    public static boolean throws_remote_exception( final Method method )
    {
        Class [] exceptions = method.getExceptionTypes();
        if ( exceptions.length == 0 )
        {
            return false;
        }
        for ( int i = 0; i < exceptions.length; i++ )
        {
            if ( !java.rmi.RemoteException.class.isAssignableFrom( exceptions[ i ] ) )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if the writeObject method is present
     *
     * @param methods The methods of the class
     * @return True if the writeObject method is present
     */
    public static boolean has_write_object_method( final Method[] methods )
    {
        int locSize = methods.length;
        for ( int i = 0; i < locSize; i++ )
        {
            if ( methods[ i ].getName().equals( "writeObject" ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the package name for this class
     *
     * @param n The full class name
     * @return The package name
     */
    public static String get_package_name( final String n )
    {
        String locStr = new String();
        int lastIdx = n.lastIndexOf( '.' );
        if ( lastIdx != -1 )
        {
            locStr = n.substring( 0, lastIdx );
        }
        return locStr;
    }

    /**
     * Return the package name for this class
     *
     * @param n The full class name
     * @return The package name
     */
    public static String get_array_name_without_extra_char( final String n )
    {
        String locStr = new String( n );
        locStr = remove_croc( locStr );
        return locStr.substring( 1 );
    }

    private static String remove_croc( final String n )
    {
        if ( n.startsWith( "[" ) )
        {
            return remove_croc( n.substring( 1 ) );
        }
        return n;
    }

    /**
     * Return the name of the class without the package name
     *
     * @param n The full class name
     * @return The class name
     */
    public static String get_relative_name( final String n )
    {
        String locStr = new String();
        int lastIdx = n.lastIndexOf( '.' );
        if ( lastIdx != -1 )
        {
            locStr = n.substring( lastIdx + 1, n.length() );
        }
        else
        {
            locStr = n;
        }
        return locStr;
    }

    /**
     * Return the path of the directory of the given file
     *
     * @param n The full class name (package + name)
     * @return The path where the file could be found
     */
    public static String get_path( final String n )
    {
        String locStr = new String();
        if ( n.indexOf( '.' ) != -1 )
        {
            locStr = n.replace( '.', '\\' );
            locStr = locStr.substring( 0, locStr.lastIndexOf( '\\' ) );
            locStr = locStr + "\\";
        }
        else
        {
            locStr = new String( "" );
        }
        return locStr;
    }

    /**
     * Return the absolute name of the class
     * ::toto::titi
     *
     * @param n The full class name
     * @return The absolute name
     */
    public static String get_absolute_idl_name( final String n )
    {
        String locStr = "";
        int locFirstIdx = 0;
        int locLastIdx = 0;
        int locSize = n.length();
        while ( true )
        {
            locLastIdx = n.indexOf( '.', locFirstIdx );
            if ( locLastIdx == -1 )
            {
                locStr = locStr + "::" + n.substring( locFirstIdx, locSize );
                break;
            }
            locStr = locStr + "::" + n.substring( locFirstIdx, locLastIdx );
            locFirstIdx = locLastIdx + 1;
        }
        return locStr;
    }

    /**
     * Return the equivalent idl type
     *
     * @param str The name of the primitive type
     * @return The associated primitive type name
     */
    public String get_primitive_type( final String str )
    {
        display( "MappingAPI::get_primitive_type((String)[", str, "])" );
        if ( str.equals( "String" ) )
        {
            return "wstring";
        }
        if ( str.equals( "void" ) )
        {
            return "void";
        }
        if ( str.equals( "boolean" ) )
        {
            return "boolean";
        }
        if ( str.equals( "char" ) )
        {
            return "wchar";
        }
        if ( str.equals( "byte" ) )
        {
            return "octet";
        }
        if ( str.equals( "short" ) )
        {
            return "short";
        }
        if ( str.equals( "int" ) )
        {
            return "long";
        }
        if ( str.equals( "long" ) )
        {
            return "long_long";
        }
        if ( str.equals( "float" ) )
        {
            return "float";
        }
        if ( str.equals( "double" ) )
        {
            return "double";
        }
        return null;
    }

    /**
     * Return the name of the property
     *
     * @param n The full method name
     * @return The property name
     */
    public static String split_accessor_name( final String n )
    {
        String locStr = "";
        // Extract the property name
        if ( n.startsWith( "is" ) && n.length() > 2 )
        {
            locStr = n.substring( 2 );
        }
        if ( ( n.startsWith( "get" ) || n.startsWith( "set" ) ) && ( n.length() > 3 ) )
        {
            locStr = n.substring( 3 );
        }
        // Process the property name
        if ( !locStr.substring( 0, 2 ).equals( locStr.substring( 0, 2 ).toUpperCase() ) )
        {
            if ( Character.isUpperCase( locStr.charAt( 0 ) ) )
            {
                char c = Character.toLowerCase( locStr.charAt( 0 ) );
                locStr = c + locStr.substring( 1 );
            }
        }
        return locStr;
    }

    /**
     * Apply the mapping java names to idl names rules
     * 1 - Java names that clash
     * 2 - Java names with leading underscors
     *
     * @param name The name to process
     * @return The processed string
     */
    private String process_name( final String name )
    {
        // Processes the java name that begins with '_'
        if ( name.startsWith( "_" ) )
        {
            return "J" + name;
        }
        // Processes java name that clash with IDL keywords
        if ( collide_with_keyword( name, Symbole.liste_mots_reserves ) )
        {
            return "_" + name;
        }
        return name;
    }

    /**
     * Apply the mapping java names to idl names rules
     * 1 - Java names that clash
     * 2 - Java names with leading underscors
     *
     * @param name The name to process
     * @return The processed string
     */
    private String process_exception_suffix( final String name )
    {
        String processed_name = name;
        // Processes the java name that begins with '_'
        if ( name.endsWith( "Exception" ) )
        {
            processed_name = name.substring( 0, name.indexOf( "Exception" ) );
        }
        // Add the suffix Ex
        return processed_name + "Ex";
    }

    /**
     * Get the new full class name from the processed relative name
     *
     * @param c The class
     * @param name The new processed name of the class
     * @return The processed string
     */
    private String process_new_full_class_name( final Class c, final String name )
    {
        String processed_name = new String();
        String n = c.getName();
        String locStr = new String();
        // *** Get the idl package name ***
        int locFirstIdx = 0;
        int locLastIdx = 0;
        while ( true )
        {
            locLastIdx = n.indexOf( '.', locFirstIdx );
            if ( locLastIdx == -1 )
            {
                break;
            }
            locStr = locStr + "::" + n.substring( locFirstIdx, locLastIdx );
            locFirstIdx = locLastIdx + 1;
        }
        // *** Add the processed name to the package name ***
        processed_name = locStr + "::" + name;
        return processed_name;
    }

    /**
     * Checks if the String n is not contained in the List of SymboleDef
     *
     * @param n The string to look up
     * @param list The List of SymboleDef
     * @return True if there is a collision, otherwise false
     */
    private boolean collide_with_keyword( final String n, final List list )
    {
        final int locSize = list.size();

        for ( int i = 0; i < locSize; i++ )
        {
            final SymboleDef def = ( SymboleDef ) list.get( i );
            if ( n.equals( def.symbole_name ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Process the overloading method names case
     *
     * @param m The method
     * @param m_name The processed method name
     * @return The new string (name) associated with m name
     */
    private String process_overloaded_method_name( final Method m, final String n_name )
    {
        // Initialize the string with the name of the method
        String locStr = n_name;
        // Adds the potential parameters
        Class [] parametersTab = m.getParameterTypes();
        int locSize = parametersTab.length;
        int nbSeq = 0;
        for ( int i = 0; i < locSize; i++ )
        {
            String locName = new String();
            // Case of full type path
            if ( parametersTab[ i ].isArray() )
            {
                // *** Figure out the name in the hashtable ***
                Class c = parametersTab[ i ];
                IdlObject locIdlObj = null;
                // *** Search for it in the hashtable ***
                String locArrayIdlFullName = null;
                locArrayIdlFullName = ( String ) m_javaParser.getMappingNames().get( c.getName() );
                if ( locArrayIdlFullName != null )
                {
                    locIdlObj = return_existing_object( locArrayIdlFullName );
                    if ( locIdlObj == null )
                    {
                        throw new CompilationException( "<!>Coherance error between"
                            + " hashtable and array name"
                            + " (process_overloaded_method_name( " + m + ", " + n_name + " ))" );
                    }
                    else
                    {
                        String locStr2 = "org_omg_boxedRMI_";
                        locName = locStr2 + locIdlObj.name();
                    }
                }
                else
                {
                    // *** Parse & map the unknown class ***
                    final ParserResult result = parseClass( c );
                    locIdlObj = result.getResult();

                    // Add it to the include files
                    add_idl_object_as_first( m_javaParser.getIdlTreeRoot(),
                          result.makeResultInclude() );
                    // Add it to the tree
                    locName = locIdlObj.name().replace( '.', '_' );
                }
            }
            else
            {
                if ( parametersTab[ i ].getName().indexOf( '.' ) != -1 )
                {
                    locName = parametersTab[ i ].getName().replace( '.', '_' );
                }
                else
                {
                    if ( parametersTab[ i ].isPrimitive() )
                    {
                        locName = get_primitive_type( parametersTab[ i ].getName() );
                    }
                    else
                    {
                        locName = parametersTab[ i ].getName();
                    }
                }
            }
            // *** Process the blank space ***
            if ( parametersTab[ i ].getName().indexOf( ' ' ) != -1 )
            {
                locName = parametersTab[ i ].getName().replace( ' ', '_' );
            }
            // ** Special rules **
            locName = process_special_changes( locName );
            locStr = locStr + "__" + locName;
        }
        if ( locSize == 0 )
        {
            locStr = locStr + "__";
        }
        return locStr;
    }

    /**
     * Process the overloading constructor names case
     *
     * @param m The constuctor
     * @param m_name The processed method name
     * @return The new string (name) associated with m name
     */
    private String process_overloaded_constructor_name( final Constructor m, final String n_name )
    {
        // Initialize the string with the name of the method
        String locStr = n_name;
        // Adds the potential parameters
        Class [] parametersTab = m.getParameterTypes();
        int locSize = parametersTab.length;
        int nbSeq = 0;
        for ( int i = 0; i < locSize; i++ )
        {
            String locName = new String();
            // Case of full type path
            if ( parametersTab[ i ].isArray() )
            {
                // *** Figure out the name in the hashtable ***
                Class c = parametersTab[ i ];
                IdlObject locIdlObj = null;
                // *** Search for it in the hashtable ***
                String locArrayIdlFullName = null;
                locArrayIdlFullName = ( String ) m_javaParser.getMappingNames().get( c.getName() );
                if ( locArrayIdlFullName != null )
                {
                    locIdlObj = return_existing_object( locArrayIdlFullName );
                    if ( locIdlObj == null )
                    {
                        final String msg = "<!>Coherance error between hashtable and array name";
                        System.out.println( msg );
                        throw new RuntimeException( msg );
                    }
                    else
                    {
                        String locStr2 = "org_omg_boxedRMI_";
                        locName = locStr2 + locIdlObj.name();
                    }
                }
                else
                {
                    // *** Parse & map the unknown class ***
                    final ParserResult result = parseClass( c );
                    locIdlObj = result.getResult();

                    // Add it to the include files
                    add_idl_object_as_first( m_javaParser.getIdlTreeRoot(),
                          result.makeResultInclude() );
                    // Add it to the tree
                    locName = locIdlObj.name().replace( '.', '_' );
                }
            }
            else
            {
                if ( parametersTab[ i ].getName().indexOf( '.' ) != -1 )
                {
                    locName = parametersTab[ i ].getName().replace( '.', '_' );
                }
                else
                {
                    if ( parametersTab[ i ].isPrimitive() )
                    {
                        locName = get_primitive_type( parametersTab[ i ].getName() );
                    }
                    else
                    {
                        locName = parametersTab[ i ].getName();
                    }
                }
            }
            // *** Process the blank space ***
            if ( parametersTab[ i ].getName().indexOf( ' ' ) != -1 )
            {
                locName = parametersTab[ i ].getName().replace( ' ', '_' );
            }
            // ** Special rules **
            locName = process_special_changes( locName );
            locStr = locStr + "__" + locName;
        }
        if ( locSize == 0 )
        {
            locStr = locStr + "__";
        }
        return locStr;
    }

    /**
     * Processes the names differing only in case.
     * The rule is to concatenate the index of a upper case character to
     * the name.
     *
     * @param n The name to be processed
     * @return The new string associated with n
     */
    private String process_name_differing_in_case( final String n )
    {
        final StringBuffer result = new StringBuffer( n );
        final char [] locOriginTab = n.toLowerCase().toCharArray();
        final char [] locDestTab = n.toCharArray();
        // First step for the
        final int locSize = locOriginTab.length;
        boolean modified = false;
        for ( int i = 0; i < locSize; i++ )
        {
            if ( locDestTab[i] < locOriginTab[i] )
            {
                result.append( '_' );
                result.append( i );
                modified = true;
            }
        }
        // Change the original string
        if ( !modified )
        {
            result.append( '_' );
        }
        return result.toString();
    }

    /**
     * Used in the case of a collision between a constant or field name
     * that has the same name as a method.
     * The rule is to add a "_" at the end of the field name.
     *
     * @param c_or_f The constant or field name to be processed
     * @return The new string associated with c_or_f
     */
    private String process_c_or_f_colliding_with_method_name( final String c_or_f )
    {
        return new String( c_or_f + "_" );
    }

    /**
     * The rule is to concatenate both class name with "__".
     *
     * @param outerClass The name of the class in which the inner class is defined
     * @param innerClass The inner class name
     * @return The new string name for the inner_class
     */
    private String process_inner_class_name( final String outerClass, final String innerClass )
    {
        return new String( outerClass + "__" + innerClass );
    }

    /**
     * Method that allows to know if a method already exists in the List
     *
     * @param n The name to search
     * @param vect The needed List
     * @param case_sensitive True if the comparison uses the case sensitivity
     * @return True if the string already exists
     */
    private boolean stringContained( final List vect, final String name,
            final boolean case_sensitive )
    {
        String n = name;
        if ( !case_sensitive )
        {
            n = n.toLowerCase();
        }
        final int locSize = vect.size();
        for ( int i = 0; i < locSize; i++ )
        {
            final String current = ( String ) vect.get( i );
            if ( case_sensitive )
            {
                if ( n.equals( current ) )
                {
                    return true;
                }
            }
            else
            {
                if ( n.equals( current.toLowerCase() ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the associated class from the hashtable
     *
     * @param pName The method name to fetch
     */
    private Method get_associated_method_class( final String pName )
    {
        final java.lang.Object locObj = m_knownMethodsTable.get( pName );

        if ( locObj instanceof String )
        {
            return get_associated_method_class( ( String ) locObj );
        }
        if ( locObj instanceof Method )
        {
            return ( Method ) locObj;
        }
        return null;
    }

    /**
     * Return the object corresponding to the class if it exists
     *
     * @param c The class to search for
     * @return The object if it exists null otherwise.
     */
    public IdlObject return_existing_object( final Class c )
    {
        display( "MappingAPI::return_existing_object((Class)[", c, "])" );
        // Get the aboslute name
        final String locClassName = get_absolute_idl_name( c.getName() );
        // Process from the existing tree
        final int locSize = m_javaParser.getCompilationTree().size();
        for ( int i = 0; i < locSize; i++ )
        {
            final IdlObject obj = ( IdlObject ) m_javaParser.getCompilationTree().get( i );
            final IdlObject result = obj.returnObject( locClassName, true );
            if ( null != result )
            {
                return result;
            }
        }

        return null;
    }

    /**
     * Return the object corresponding to the class name if it exists
     *
     * @param c_full_name The full class name to search for
     * @return The object if it exists null otherwise.
     */
    public IdlObject return_existing_object( final String c_full_name )
    {
        display( "MappingAPI::return_existing_object((String)[", c_full_name, "])" );
        // Process from the existing tree
        final int locSize = m_javaParser.getCompilationTree().size();
        final String deprefixedName = removePrefix( c_full_name );

        for ( int i = 0; i < locSize; i++ )
        {
            final IdlObject obj = ( IdlObject ) m_javaParser.getCompilationTree().get( i );
            final IdlObject result = obj.returnObject( deprefixedName, true );
            if ( null != result )
            {
                return result;
            }
        }
        display( "Warning - Could not find exisiting [", deprefixedName, "]" );

        return null;
    }

    /**
     * Set the mapping type of the current class
     *
     * @param c The current class
     * @return The correct type associated to the class
     */
    private int setClassType( final Class c )
    {
        final int result;

        // *** Array case ***
        if ( c.isArray() )
        {
            result = RMI_IDL_ARRAYS_TYPE;
        }
        else if ( Throwable.class.isAssignableFrom( c ) ) // *** Exception case ***
        {
            if ( ReflectionUtils.isAssignableFrom( "org.omg.CORBA.SystemException", c ) )
            {
                result = CORBA_SYSTEM_EXCEPTION;
            }
            else if ( ReflectionUtils.isAssignableFrom( "org.omg.CORBA.UserException", c ) )
            {
                result = CORBA_USER_EXCEPTION;
            }
            else
            {
                result = RMI_IDL_EXCEPTION_TYPE;
            }
        }
        else if ( c.isInterface() ) // *** Interface case ***
        {
            // CORBA Object
            if ( ReflectionUtils.isAssignableFrom( "org.omg.CORBA.Object", c ) )
            {
                result = CORBA_REFERENCE_TYPE;
            }
            // CORBA Abstract interface
            else if ( ReflectionUtils.isAssignableFrom( "org.omg.CORBA.portable.IDLEntity", c ) )
            {
                result = CORBA_ABSTRACT_INTERFACE_TYPE;
            }
            // RMI remote interface
            else if ( java.rmi.Remote.class.isAssignableFrom( c ) )
            {
                result = RMI_IDL_REMOTE_INTERFACE_TYPE;
            }
            else
            {
                // Abstract interface (no inheritance from Remote)
                // Are all methods (inherited + declared) throwing RemoteException?
                Method [] locMethods = c.getMethods();
                int locSize1 = locMethods.length;
                if ( locSize1 == 0 )
                {
                    result = RMI_IDL_ABSTRACT_INTERFACE_TYPE;
                }
                else
                {
                    boolean locThrowARemoteExcept = false;
                    for ( int i = 0; i < locSize1; i++ )
                    {
                        Class [] locExceptionsTab = locMethods[ i ].getExceptionTypes();
                        int locSize2 = locExceptionsTab.length;
                        for ( int j = 0; j < locSize2; j++ )
                        {
                            if ( java.rmi.RemoteException.class.isAssignableFrom(
                                  locExceptionsTab[ j ] ) )
                            {
                                locThrowARemoteExcept = true;
                                break;
                            }
                        }
                    }
                    if ( !locThrowARemoteExcept )
                    {
                        result = RMI_IDL_VALUE_TYPES_TYPE;
                    }
                    else
                    {
                        result = RMI_IDL_ABSTRACT_INTERFACE_TYPE;
                    }
                }
            }
        }
        // Class Types
        else if ( java.rmi.Remote.class.isAssignableFrom( c ) )
        {
            result = RMI_IMPLEMENTATION_CLASS;
        }
        else if ( ReflectionUtils.isAssignableFrom( "org.omg.CORBA.portable.ValueBase", c ) )
        {
            result = CORBA_VALUE_TYPE;
        }
        else if ( java.io.Serializable.class.isAssignableFrom( c ) )
        {
            if ( isCustomMarshaled( c ) )
            {
                result = RMI_IDL_CUSTOM_VALUE_TYPES_TYPE;
            }
            else if ( isIDLEntity( c ) )
            {
                result = CORBA_COMPLEX_TYPE;
            }
            else
            {
                result = RMI_IDL_VALUE_TYPES_TYPE;
            }
        }
        else
        {
            result = NON_CONFORMING_TYPE;
        }
        display( "MappingAPI::setClassType: " + MappingAPI.toString( result ) );
        return result;
    }

    /**
     * Check whether the class is an IDLEntity.
     *
     * @param clz The class to check.
     * @return True when IDLEntity is assignable from c, false otherwise.
     */
    private static boolean isIDLEntity( final Class clz )
    {
        if ( null == clz )
        {
            return false;
        }
        return IDLEntity.class.isAssignableFrom( clz );
    }

    private static boolean isCustomMarshaled( final Class clz )
    {
        if ( ( null == clz ) || IDLEntity.class.isAssignableFrom( clz ) )
        {
            return false;
        }
        final Class adaptedClass;
        // adapt some general types
        if ( Any.class.isAssignableFrom( clz ) )
        {
            adaptedClass = Any.class;
        }
        else if ( TypeCode.class.isAssignableFrom( clz ) )
        {
            adaptedClass = TypeCode.class;
        }
        else
        {
            adaptedClass = clz;
        }
        // an Externalizable must have writeExternal() method
        if ( Externalizable.class.isAssignableFrom( adaptedClass ) )
        {
            return true;
        }
        else if ( !adaptedClass.isArray() )
        {
            if ( ReflectionUtils.hasWriteObjectMethod( adaptedClass ) )
            {
                return true;
            }
            else
            {
                // check parent class
                return isCustomMarshaled( adaptedClass.getSuperclass() );
            }
        }
        return false;
    }

    private String removePrefix( final String name )
    {
        final StringBuffer result = new StringBuffer();
        final StringTokenizer tokenizer = new StringTokenizer( name, "::" );
        while ( tokenizer.hasMoreTokens() )
        {
            final String currentToken = tokenizer.nextToken();
            if ( currentToken.length() != 0 )
            {
                if ( currentToken.charAt( 0 ) == '_' )
                {
                    result.append( currentToken.substring( 1 ) );
                }
                else
                {
                    result.append( currentToken );
                }
            }
            if ( tokenizer.hasMoreTokens() )
            {
                result.append( "::" );
            }
        }
        return result.toString();
    }

    /**
     * This operation is used to sort a method list to extract the operation
     * only defined into this class.
     */
    private Method[] filterMethods( final Method[] m, final Class c )
    {
        final List methods = new ArrayList();
        for ( int i = 0; i < m.length; i++ )
        {
            final Method method = m[i];
            final int mod = method.getModifiers();
            if ( !Modifier.isPublic( mod ) || Modifier.isStatic( mod ) )
            {
                continue;
            }
            if ( isInherited( method, c ) )
            {
                continue;
            }
            if ( isImplemented( method, c ) )
            {
                continue;
            }
            methods.add( method );
        }
        final Method[] meth = new Method[ methods.size() ];
        for ( int i = 0; i < meth.length; i++ )
        {
            meth[i] = ( Method ) methods.get( i );
        }
        return meth;
    }

    /**
     * This operation tests if a method is defined in the super classes
     */
    private boolean isInherited( final Method method, final Class c )
    {
        // -- Is there a super class ? --
        final Class sc = c.getSuperclass();
        if ( sc == null )
        {
            return false;
        }
        // -- Check if this method is inherited --
        final Method[] ms = sc.getMethods();
        for ( int i = 0; i < ms.length; i++ )
        {
            if ( sameMethod( method, ms[i] ) )
            {
                return true;
            }
        }
        // -- Check in the super class --
        return isInherited( method, sc );
    }

    /**
     * This operation tests if a method is defined in the implemented interfaces
     */
    private boolean isImplemented( final Method method, final Class c )
    {
        // -- Are these some immplemented interfaces ? --
        final Class[] itf = c.getInterfaces();
        if ( itf.length == 0 )
        {
            return false;
        }
        // -- Check each interface --
        for ( int i = 0; i < itf.length; i++ )
        {
            // -- Check the operations of the interface --
            final Method[] ms = itf[i].getMethods();
            for ( int j = 0; j < ms.length; j++ )
            {
                if ( sameMethod( method, ms[j] ) )
                {
                    return true;
                }
            }
            // -- Now check in the inherited interfaces --
            if ( isImplemented( method, itf[i] ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This operation is used to test if two methods are the same
     */
    private boolean sameMethod( final Method m1, final Method m2 )
    {
        if ( !m1.getName().equals( m2.getName() ) )
        {
            return false;
        }
        if ( !m1.getReturnType().equals( m2.getReturnType() ) )
        {
            return false;
        }
        final Class[] p1 = m1.getParameterTypes();
        final Class[] p2 = m2.getParameterTypes();
        if ( p1.length != p2.length )
        {
            return false;
        }
        for ( int i = 0; i < p1.length; i++ )
        {
            if ( !p1[i].equals( p2[i] ) )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check special name changes
     */
    private String process_special_changes( final String name )
    {
        if ( name.equals( "java_lang_Class" ) )
        {
            return "javax_rmi_CORBA_ClassDesc";
        }
        if ( name.equals( "java_lang_String" ) )
        {
            return "CORBA_WStringValue";
        }
        return name;
    }

    /**
     * Insert the sub idl_object at the first place of the list
     *
     * @param idlObj The parent idl object
     * @param sub_idlObj The sub idl object to add to the parent
     */
    public void add_idl_object_as_first( final IdlObject idlObj,
          final IdlObject sub_idlObj )
    {
        display( "MappingAPI::add_idl_object_as_first((IdlObject)[", idlObj,
                "], (IdlObject)[", sub_idlObj, "])" );
        idlObj._list.insertElementAt( sub_idlObj, 0 );
    }

    private void appendTo( final StringBuffer buf, final Object obj )
    {
        if ( obj instanceof Object[] )
        {
            final Object[] array = ( Object[] ) obj;
            buf.append( '{' );
            if ( 0 != array.length )
            {
                buf.append( '[' );
                appendTo( buf, array[0] );
                buf.append( ']' );
                for ( int i = 1; i < array.length; i++ )
                {
                    buf.append( ',' );
                    buf.append( '[' );
                    appendTo( buf, array[i] );
                    buf.append( ']' );
                }
            }
            buf.append( '}' );
            return;
        }
        buf.append( obj );
    }

    private void display( final Object arg0 )
    {
        if ( m_rcp.getM_verbose() )
        {
            final StringBuffer buf = new StringBuffer();
            appendTo( buf, arg0 );
            m_ch.display( buf.toString() );
        }
    }

    private void display( final Object arg0, final Object arg1, final Object arg2 )
    {
        if ( m_rcp.getM_verbose() )
        {
            final StringBuffer buf = new StringBuffer();
            appendTo( buf, arg0 );
            appendTo( buf, arg1 );
            appendTo( buf, arg2 );
            m_ch.display( buf.toString() );
        }
    }

    private void display( final Object arg0, final Object arg1, final Object arg2,
            final Object arg3, final Object arg4 )
    {
        if ( m_rcp.getM_verbose() )
        {
            final StringBuffer buf = new StringBuffer();
            appendTo( buf, arg0 );
            appendTo( buf, arg1 );
            appendTo( buf, arg2 );
            appendTo( buf, arg3 );
            appendTo( buf, arg4 );
            m_ch.display( buf.toString() );
        }
    }

    private void display( final Object arg0, final Object arg1, final Object arg2,
            final Object arg3, final Object arg4, final Object arg5, final Object arg6 )
    {
        if ( m_rcp.getM_verbose() )
        {
            final StringBuffer buf = new StringBuffer();
            appendTo( buf, arg0 );
            appendTo( buf, arg1 );
            appendTo( buf, arg2 );
            appendTo( buf, arg3 );
            appendTo( buf, arg4 );
            appendTo( buf, arg5 );
            appendTo( buf, arg6 );
            m_ch.display( buf.toString() );
        }
    }

    private void display( final Object arg0, final Object arg1, final Object arg2,
            final Object arg3, final Object arg4, final Object arg5, final Object arg6,
            final Object arg7, final Object arg8 )
    {
        if ( m_rcp.getM_verbose() )
        {
            final StringBuffer buf = new StringBuffer();
            appendTo( buf, arg0 );
            appendTo( buf, arg1 );
            appendTo( buf, arg2 );
            appendTo( buf, arg3 );
            appendTo( buf, arg4 );
            appendTo( buf, arg5 );
            appendTo( buf, arg6 );
            appendTo( buf, arg7 );
            appendTo( buf, arg8 );
            m_ch.display( buf.toString() );
        }
    }

    private void display( final Object arg0, final Object arg1, final Object arg2,
            final Object arg3, final Object arg4, final Object arg5, final Object arg6,
            final Object arg7, final Object arg8, final Object arg9, final Object arg10 )
    {
        if ( m_rcp.getM_verbose() )
        {
            final StringBuffer buf = new StringBuffer();
            appendTo( buf, arg0 );
            appendTo( buf, arg1 );
            appendTo( buf, arg2 );
            appendTo( buf, arg3 );
            appendTo( buf, arg4 );
            appendTo( buf, arg5 );
            appendTo( buf, arg6 );
            appendTo( buf, arg7 );
            appendTo( buf, arg8 );
            appendTo( buf, arg9 );
            appendTo( buf, arg10 );
            m_ch.display( buf.toString() );
        }
    }

    private void display( final Object arg0, final Object arg1, final Object arg2,
            final Object arg3, final Object arg4, final Object arg5, final Object arg6,
            final Object arg7, final Object arg8, final Object arg9, final Object arg10,
            final Object arg11, final Object arg12 )
    {
        if ( m_rcp.getM_verbose() )
        {
            final StringBuffer buf = new StringBuffer();
            appendTo( buf, arg0 );
            appendTo( buf, arg1 );
            appendTo( buf, arg2 );
            appendTo( buf, arg3 );
            appendTo( buf, arg4 );
            appendTo( buf, arg5 );
            appendTo( buf, arg6 );
            appendTo( buf, arg7 );
            appendTo( buf, arg8 );
            appendTo( buf, arg9 );
            appendTo( buf, arg10 );
            appendTo( buf, arg11 );
            appendTo( buf, arg12 );
            m_ch.display( buf.toString() );
        }
    }

}
