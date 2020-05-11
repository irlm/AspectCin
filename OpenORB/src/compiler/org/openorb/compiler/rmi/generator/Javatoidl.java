/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.rmi.generator;

import java.io.File;

import java.util.StringTokenizer;

import org.openorb.compiler.CompilerHost;

import org.openorb.compiler.object.IdlArray;
import org.openorb.compiler.object.IdlAttribute;
import org.openorb.compiler.object.IdlConst;
import org.openorb.compiler.object.IdlContext;
import org.openorb.compiler.object.IdlEnumMember;
import org.openorb.compiler.object.IdlIdent;
import org.openorb.compiler.object.IdlInclude;
import org.openorb.compiler.object.IdlInterface;
import org.openorb.compiler.object.IdlObject;
import org.openorb.compiler.object.IdlOp;
import org.openorb.compiler.object.IdlParam;
import org.openorb.compiler.object.IdlRaises;
import org.openorb.compiler.object.IdlSimple;
import org.openorb.compiler.object.IdlStateMember;
import org.openorb.compiler.object.IdlStructMember;
import org.openorb.compiler.object.IdlUnion;
import org.openorb.compiler.object.IdlUnionMember;
import org.openorb.compiler.object.IdlValue;
import org.openorb.compiler.object.IdlValueBox;
import org.openorb.compiler.object.IdlValueInheritance;

import org.openorb.compiler.parser.IdlType;
import org.openorb.compiler.parser.Token;

import org.openorb.compiler.rmi.RmiCompilerProperties;

import org.openorb.util.ReflectionUtils;

/**
 * This class takes an IDL graph and generates its IDL description.
 *
 * @author Jerome Daniel
 */
public class Javatoidl
    extends org.openorb.compiler.generator.IdlToJava
{
    /** The default name of the folder where the generated files will be placed. */
    private static final String DEFAULT_GENERATED_FOLDER_NAME = "generated";

    /** Reference to the mapping m_level */
    private int m_level;

    /** Reference to the current m_prefix */
    private String m_prefix = "";

    /** Reference to the Root object */
    private IdlObject m_root;

    private RmiCompilerProperties m_rcp = null;
    private CompilerHost m_ch = null;

    /**
     * Constructor
     */
    public Javatoidl( RmiCompilerProperties rcp, CompilerHost ch )
    {
        super( rcp );
        m_rcp = rcp;
        m_ch = ch;
        m_level = 0;
    }

    /**
     * This method indents a file
     */
    public void indent( java.io.PrintWriter writeInto )
    {
        for ( int i = 0; i < m_level; i++ )
        {
            writeInto.print( "    " );
        }
    }

    /**
     * This method indents a file
     */
    public void indent( String msg, java.io.PrintWriter writeInto )
    {
        for ( int i = 0; i < m_level; i++ )
        {
            writeInto.print( "    " );
        }
        writeInto.println( msg );
    }

    /**
     * This method creates an IDL file.
     */
    public java.io.PrintWriter create_idl_file( String file_name )
    {
        java.io.File file = new java.io.File( file_name );
        if ( m_rcp.getM_clistener() != null )
        {
            m_rcp.getM_clistener().addTargetJavaFile( file );
        }
        // -- Create sub directories --
        file.getParentFile().mkdirs();
        // -- Create the files --
        java.io.PrintWriter printout = null;
        try
        {
            org.openorb.util.DiffFileOutputStream writeInto =
                  new org.openorb.util.DiffFileOutputStream( file );
            java.io.DataOutputStream dataout = new java.io.DataOutputStream( writeInto );
            printout = new java.io.PrintWriter( dataout );
        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
        }

        printout.println( "// -----------------------------------------------------------"
              + "-----------------" );
        printout.println( "// OpenORB Java To IDL compiler" );
        printout.println( "//" );
        printout.println( "// (c) 2002 The Community OpenORB Project" );
        printout.println( "// -----------------------------------------------------------"
              + "-----------------" );
        printout.println( "" );
        printout.println( "" );
        printout.println( "#ifndef __" + file_name.replace( '.', '_' ).
              replace( '/', '_' ).replace( '\\', '_' ).replace( '-', '_' ).toUpperCase()
              + "_FROM_JAVA__" );
        printout.println( "#define __" + file_name.replace( '.', '_' ).
              replace( '/', '_' ).replace( '\\', '_' ).replace( '-', '_' ).toUpperCase()
              + "_FROM_JAVA__" );
        printout.println( "" );
        printout.println( "#include <orb.idl>" );
        printout.println( "#include <_std_java.idl>" );
        printout.println( "#include <_std_javax.idl>" );
        printout.println( "" );
        return printout;
    }

    /**
     * This method returns a full name for a IDL item.
     */
    public String fullname_idl( IdlObject obj )
    {
        java.util.Vector v = new java.util.Vector();
        IdlObject obj2 = obj;
        String name = new String( "" );
        String s;
        boolean first = false;
        while ( obj2 != null )
        {
            if ( obj2._underscore )
            {
                v.addElement( "_" + obj2.name() );
            }
            else
            {
                v.addElement( obj2.name() );
            }
            if ( obj2.upper() != null )
            {
                if ( obj2.upper().kind() == IdlType.e_root )
                {
                    break;
                }
            }
            obj2 = obj2.upper();
            first = true;
        }
        for ( int i = v.size() - 1; i >= 0; i-- )
        {
            s = ( String ) v.elementAt( i );
            if ( s != null )
            {
                if ( !name.equals( "" ) )
                {
                    name = name + "::";
                }
                name = name + s;
            }
        }
        return "::" + name;
    }

    /**
     * This method translates a IDL type
     */
    public void translateType( IdlObject obj, java.io.PrintWriter writeInto,
          String name, boolean write )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateType-> obj='" + obj.name()
                  + "', name='" + name + "'" );
        }

        IdlSimple simple = null;
        switch ( obj.kind() )
        {
        case IdlType.e_simple:
            simple = ( IdlSimple ) obj;
            switch ( simple.internal() )
            {
            case Token.t_void:
                writeInto.print( "void" );
                break;

            case Token.t_float:
                writeInto.print( "float" );
                break;

            case Token.t_double:
                writeInto.print( "double" );
                break;

            case Token.t_short:
                writeInto.print( "short" );
                break;

            case Token.t_ushort:
                writeInto.print( "unsigned short" );
                break;

            case Token.t_long:
                writeInto.print( "long" );
                break;

            case Token.t_ulong:
                writeInto.print( "unsigned long" );
                break;

            case Token.t_longlong:
                writeInto.print( "long long" );
                break;

            case Token.t_ulonglong:
                writeInto.print( "unsigned long long" );
                break;

            case Token.t_char:
                writeInto.print( "char" );
                break;

            case Token.t_wchar:
                writeInto.print( "wchar" );
                break;

            case Token.t_boolean:
                writeInto.print( "boolean" );
                break;

            case Token.t_octet:
                writeInto.print( "octet" );
                break;

            case Token.t_any:
                writeInto.print( "any" );
                break;

            case Token.t_typecode:
                writeInto.print( "CORBA::TypeCode" );
                break;

            case Token.t_object:
                writeInto.print( "Object" );
                break;

            case Token.t_ValueBase:
                writeInto.print( "valuebase" );
                break;

            case Token.t_wstring:
                writeInto.print( "wstring" );
                break;
            }
            break;

        case IdlType.e_fixed:
            writeInto.print( "fixed" );
            break;

        case IdlType.e_string:
            writeInto.print( "string" );
            break;

        case IdlType.e_wstring:
            writeInto.print( "wstring" );
            break;

        case IdlType.e_struct:
        case IdlType.e_union:
        case IdlType.e_enum:
        case IdlType.e_interface:
        case IdlType.e_forward_interface:
        case IdlType.e_exception:
        case IdlType.e_native:
        case IdlType.e_value:
            writeInto.print( fullname_idl( obj ) );
            break;

        case IdlType.e_typedef:
            obj.reset();
            translateType( obj.current(), writeInto, name, write );
            break;

        case IdlType.e_sequence:
            writeInto.print( "sequence<" );
            translateType( obj.current(), writeInto, name, false );
            writeInto.print( ">" );
            break;

        case IdlType.e_array:
            translateType( obj.current(), writeInto, name + "["
                  + ( ( IdlArray ) obj ).getDimension() + "]", false );
            if ( obj.current().kind() != IdlType.e_array )
            {
                writeInto.print( " " + name );
                writeInto.print( "[" + ( ( IdlArray ) obj ).getDimension() + "]" );
            }
            return;

        case IdlType.e_ident:
            writeInto.print( fullname_idl( ( ( IdlIdent ) obj ).internalObject() ) );
            break;

        case IdlType.e_value_box:
            if ( ( ( IdlValueBox ) obj ).simple() )
            {
                writeInto.print( fullname_idl( obj ) );
            }
            else
            {
                obj.reset();
                translateType( obj.current(), writeInto, name, write );
            }
            break;
        }
        if ( write )
        {
            writeInto.print( " " + name );
        }
    }

    /**
     * This method translates a IDL parameter
     */
    public void translateParameter( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateParameter-> obj='" + obj.name() + "'" );
        }

        IdlParam p = ( IdlParam ) obj;
        IdlSimple simple = null;
        p.reset();
        switch ( p.param_attr() )
        {
        case 0:
            writeInto.print( "in " );
            break;

        case 1:
            writeInto.print( "out " );
            break;

        case 2:
            writeInto.print( "inout " );
            break;
        }
        translateType( p.current(), writeInto, p.name(), true );
    }

    /**
     * This method translates a Module
     */
    public void translateModule( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateModule-> obj='" + obj.name() + "'" );
        }

        indent( "/**", writeInto );
        indent( " * Module " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( "module " + obj.name(), writeInto );
        indent( "{", writeInto );
        m_level++;
        translateDescription( obj, writeInto );
        m_level--;
        indent( writeInto );
        writeInto.print( "}" );
    }

    /**
     * This method translates an Enum
     */
    public void translateEnum( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateEnum-> obj='" + obj.name() + "'" );
        }

        indent( "/**", writeInto );
        indent( " * Enum " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( "enum " + obj.name(), writeInto );
        indent( "{", writeInto );
        IdlEnumMember member = null;
        obj.reset();
        int i = 1;
        m_level++;
        indent( writeInto );
        while ( !obj.end() )
        {
            if ( ( i % 6 ) == 0 )
            {
                writeInto.println( "" );
                indent( writeInto );
                i = 1;
            }
            member = ( IdlEnumMember ) obj.current();
            i++;
            writeInto.print( member.name() );
            obj.next();
            if ( !obj.end() )
            {
                writeInto.print( ", " );
            }
        }
        m_level--;
        writeInto.println( "" );
        indent( writeInto );
        writeInto.print( "}" );
    }

    /**
     * This method translates a Struct
     */
    public void translateStruct( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateStruct-> obj='" + obj.name() + "'" );
        }

        indent( "/**", writeInto );
        indent( " * Struct " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( "struct " + obj.name(), writeInto );
        indent( "{", writeInto );
        IdlStructMember member = null;
        obj.reset();
        m_level++;
        while ( !obj.end() )
        {
            member = ( IdlStructMember ) obj.current();
            indent( writeInto );
            member.reset();
            switch ( member.current().kind() )
            {
            case org.openorb.compiler.parser.IdlType.e_enum:
                writeInto.println( "" );
                m_level++;
                translateEnum( member.current(), writeInto );
                writeInto.print( " " + member.name() );
                m_level--;
                break;

            case org.openorb.compiler.parser.IdlType.e_struct:
                writeInto.println( "" );
                m_level++;
                translateStruct( member.current(), writeInto );
                writeInto.print( " " + member.name() );
                m_level--;
                break;

            case org.openorb.compiler.parser.IdlType.e_union:
                writeInto.println( "" );
                m_level++;
                translateUnion( member.current(), writeInto );
                writeInto.print( " " + member.name() );
                m_level--;
                break;

            default:
                translateType( member.current(), writeInto, member.name(), true );
                break;
            }
            writeInto.println( ";" );
            obj.next();
        }
        m_level--;
        indent( writeInto );
        writeInto.print( "}" );
    }

    /**
     * This method translates an Union
     */
    public void translateUnion( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateUnion-> obj='" + obj.name() + "'" );
        }

        IdlUnionMember member = null;
        int default_index, index;
        indent( "/**", writeInto );
        indent( " * Union " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( writeInto );
        writeInto.print( "union " + obj.name() + " switch ( " );
        obj.reset();
        member = ( IdlUnionMember ) obj.current();
        member.reset();
        translateType( member.current(), writeInto, member.name(), false );
        writeInto.println( " )" );
        indent( "{", writeInto );
        m_level++;
        obj.next();
        default_index = ( ( IdlUnion ) obj ).index();
        index = 0;
        while ( !obj.end() )
        {
            member = ( IdlUnionMember ) obj.current();
            indent( writeInto );
            member.reset();
            if ( index == default_index )
            {
                writeInto.print( "default: " );
            }
            else
            {
                writeInto.print( "case " );
                writeInto.print( adaptExpression( member.getExpression() ) );
                writeInto.print( ": " );
            }
            switch ( member.current().kind() )
            {
            case org.openorb.compiler.parser.IdlType.e_enum:
                writeInto.println( "" );
                m_level++;
                translateEnum( member.current(), writeInto );
                writeInto.print( " " + member.name() );
                m_level--;
                break;

            case org.openorb.compiler.parser.IdlType.e_struct:
                writeInto.println( "" );
                m_level++;
                translateStruct( member.current(), writeInto );
                writeInto.print( " " + member.name() );
                m_level--;
                break;

            case org.openorb.compiler.parser.IdlType.e_union:
                writeInto.println( "" );
                m_level++;
                translateUnion( member.current(), writeInto );
                writeInto.print( " " + member.name() );
                m_level--;
                break;

            default:
                translateType( member.current(), writeInto, member.name(), true );
                break;
            }
            writeInto.println( ";" );
            obj.next();
            index++;
        }
        m_level--;
        indent( writeInto );
        writeInto.print( "}" );
    }

    /**
     * This method translates an Exception
     */
    public void translateException( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateException-> obj='" + obj.name() + "'" );
        }

        indent( "/**", writeInto );
        indent( " * Exception " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( "exception " + obj.name(), writeInto );
        indent( "{", writeInto );
        IdlStructMember member = null;
        obj.reset();
        m_level++;
        while ( !obj.end() )
        {
            member = ( IdlStructMember ) obj.current();
            indent( writeInto );
            member.reset();
            switch ( member.current().kind() )
            {
            case org.openorb.compiler.parser.IdlType.e_enum:
                writeInto.println( "" );
                m_level++;
                translateEnum( member.current(), writeInto );
                writeInto.print( " " + member.name() );
                m_level--;
                break;

            case org.openorb.compiler.parser.IdlType.e_struct:
                writeInto.println( "" );
                m_level++;
                translateStruct( member.current(), writeInto );
                writeInto.print( " " + member.name() );
                m_level--;
                break;

            case org.openorb.compiler.parser.IdlType.e_union:
                writeInto.println( "" );
                m_level++;
                translateUnion( member.current(), writeInto );
                writeInto.print( " " + member.name() );
                m_level--;
                break;

            default:
                translateType( member.current(), writeInto, member.name(), true );
                break;
            }
            writeInto.println( ";" );
            obj.next();
        }
        m_level--;
        indent( writeInto );
        writeInto.print( "}" );
    }

    /**
     * This method translates a TypeDef
     */
    public void translateTypedef( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateTypedef-> obj='" + obj.name() + "'" );
        }

        indent( "/**", writeInto );
        indent( " * TypeDef " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( writeInto );
        writeInto.print( "typedef " );
        obj.reset();
        switch ( obj.current().kind() )
        {
        case org.openorb.compiler.parser.IdlType.e_enum:
            writeInto.println( "" );
            m_level++;
            translateEnum( obj.current(), writeInto );
            writeInto.print( " " + obj.name() );
            m_level--;
            break;

        case org.openorb.compiler.parser.IdlType.e_struct:
            writeInto.println( "" );
            m_level++;
            translateStruct( obj.current(), writeInto );
            writeInto.print( " " + obj.name() );
            m_level--;
            break;

        case org.openorb.compiler.parser.IdlType.e_union:
            writeInto.println( "" );
            m_level++;
            translateUnion( obj.current(), writeInto );
            writeInto.print( " " + obj.name() );
            m_level--;
            break;

        default:
            translateType( obj.current(), writeInto, obj.name(), true );
            break;
        }
    }

    /**
     * This method translates a ValueBox
     */
    public void translateValueBox( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateValueBox-> obj='" + obj.name() + "'" );
        }

        indent( "/**", writeInto );
        indent( " * ValueBox " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( writeInto );
        writeInto.print( "valuetype " + obj.name() + " " );
        obj.reset();
        switch ( obj.current().kind() )
        {
        case org.openorb.compiler.parser.IdlType.e_enum:
            writeInto.println( "" );
            m_level++;
            translateEnum( obj.current(), writeInto );
            m_level--;
            break;

        case org.openorb.compiler.parser.IdlType.e_struct:
            writeInto.println( "" );
            m_level++;
            translateStruct( obj.current(), writeInto );
            m_level--;
            break;

        case org.openorb.compiler.parser.IdlType.e_union:
            writeInto.println( "" );
            m_level++;
            translateUnion( obj.current(), writeInto );
            m_level--;
            break;

        default:
            translateType( obj.current(), writeInto, obj.name(), false );
            break;
        }
    }

    /**
     * This method translates an init member
     */
    public void translateInit( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateInit-> obj='" + obj.name() + "'" );
        }

        indent( "/**", writeInto );
        indent( " * Factory " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( writeInto );
        writeInto.print( "factory " + obj.name() + "(" );
        obj.reset();
        while ( !obj.end() )
        {
            writeInto.print( "in " );
            obj.current().reset();
            translateType( obj.current().current(), writeInto, obj.current().name(), true );
            obj.next();
            if ( !obj.end() )
            {
                writeInto.print( ", " );
            }
        }
        writeInto.print( ")" );
    }

    /**
     * This method translates a state member
     */
    public void translateStateMember( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateStateMember-> obj='" + obj.name() + "'" );
        }

        IdlStateMember member = null;
        indent( "/**", writeInto );
        indent( " * State member " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        member = ( IdlStateMember ) obj;
        indent( writeInto );
        if ( member.public_member() )
        {
            writeInto.print( "public " );
        }
        else
        {
            writeInto.print( "private " );
        }
        obj.reset();
        switch ( obj.current().kind() )
        {
        case org.openorb.compiler.parser.IdlType.e_enum:
            writeInto.println( "" );
            m_level++;
            translateEnum( obj.current(), writeInto );
            writeInto.print( " " + obj.name() );
            m_level--;
            break;

        case org.openorb.compiler.parser.IdlType.e_struct:
            writeInto.println( "" );
            m_level++;
            translateStruct( obj.current(), writeInto );
            writeInto.print( " " + obj.name() );
            m_level--;
            break;

        case org.openorb.compiler.parser.IdlType.e_union:
            writeInto.println( "" );
            m_level++;
            translateUnion( obj.current(), writeInto );
            writeInto.print( " " + obj.name() );
            m_level--;
            break;

        default:
            translateType( obj.current(), writeInto, obj.name(), true );
            break;
        }
    }

    /**
     * This method translates an Attribute
     */
    public void translateAttribute( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateAttribute-> obj='" + obj.name() + "'" );
        }

        IdlAttribute attr = ( IdlAttribute ) obj;
        indent( "/**", writeInto );
        indent( " * Attribute " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( writeInto );
        if ( attr.readOnly() )
        {
            writeInto.print( "readonly " );
        }
        writeInto.print( "attribute " );
        obj.reset();
        switch ( obj.current().kind() )
        {
        case org.openorb.compiler.parser.IdlType.e_enum:
            writeInto.println( "" );
            m_level++;
            translateEnum( obj.current(), writeInto );
            writeInto.print( " " + obj.name() );
            m_level--;
            break;

        case org.openorb.compiler.parser.IdlType.e_struct:
            writeInto.println( "" );
            m_level++;
            translateStruct( obj.current(), writeInto );
            writeInto.print( " " + obj.name() );
            m_level--;
            break;

        case org.openorb.compiler.parser.IdlType.e_union:
            writeInto.println( "" );
            m_level++;
            translateUnion( obj.current(), writeInto );
            writeInto.print( " " + obj.name() );
            m_level--;
            break;

        default:
            translateType( obj.current(), writeInto, obj.name(), true );
            break;
        }
    }

    /**
     * This method translates an Operation
     */
    public void translateOperation( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateOperation-> obj='" + obj.name() + "'" );
        }

        IdlOp op = ( IdlOp ) obj;
        indent( "/**", writeInto );
        indent( " * Operation " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( writeInto );
        if ( op.oneway() )
        {
            writeInto.print( "oneway " );
        }
        op.reset();
        translateType( op.current(), writeInto, op.name(), false );
        op.next();
        writeInto.print( " " + op.name() + "(" );
        while ( !op.end() )
        {
            if ( op.current().kind() != IdlType.e_param )
            {
                break;
            }
            translateParameter( op.current(), writeInto );
            op.next();
            if ( !op.end() )
            {
                if ( op.current().kind() == IdlType.e_param )
                {
                    writeInto.print( ", " );
                }
            }
        }
        writeInto.print( ")" );
        if ( !op.end() )
        {
            if ( op.current().kind() == IdlType.e_raises )
            {
                writeInto.print( " raises (" );
                IdlRaises raises = ( IdlRaises ) op.current();
                raises.reset();
                while ( !raises.end() )
                {
                    writeInto.print( fullname_idl( raises.current() ) );
                    raises.next();
                    if ( !raises.end() )
                    {
                        writeInto.print( ", " );
                    }
                }
                writeInto.print( ")" );
                op.next();
            }
        }
        if ( !op.end() )
        {
            if ( op.current().kind() == IdlType.e_context )
            {
                writeInto.print( " context (" );
                IdlContext ctx = ( IdlContext ) op.current();
                java.util.Vector list = ctx.getValues();
                int index = 0;
                while ( index < list.size() )
                {
                    writeInto.print( "\"" + ( String ) list.elementAt( index ) + "\"" );
                    if ( ( index + 1 ) < list.size() )
                    {
                        writeInto.print( ", " );
                    }
                    index++;
                }
                writeInto.print( ")" );
            }
        }
    }

    /**
     * This method translates a ValueType
     */
    public void translateValueType( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateValueType-> obj='" + obj.name() + "'" );
        }

        IdlValue value = ( IdlValue ) obj;
        indent( "/**", writeInto );
        if ( value.forward() )
        {
            indent( " * Forward ValueType " + obj.name() , writeInto );
        }
        else
        {
            indent( " * ValueType " + obj.name() , writeInto );
        }
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( writeInto );
        if ( value.abstract_value() )
        {
            writeInto.print( "abstract " );
        }
        if ( value.custom_value() )
        {
            writeInto.print( "custom " );
        }
        writeInto.print( "valuetype " + obj.name() );

        if ( value.forward() )
        {
            return;
        }
        java.util.Vector inheritance = value.getInheritanceList();
        if ( inheritance.size() != 0 )
        {
            writeInto.print( " : " );
            for ( int i = 0; i < inheritance.size(); i++ )
            {
                if ( ( ( IdlValueInheritance ) inheritance.elementAt( i ) ).truncatable_member() )
                {
                    writeInto.print( "truncatable " );
                }
                writeInto.print( fullname_idl( ( ( IdlValueInheritance )
                      ( inheritance.elementAt( i ) ) ).getValue() ) );
                if ( ( i + 1 ) < inheritance.size() )
                {
                    writeInto.print( ", " );
                }
            }
        }
        // -- Translate supports --
        org.openorb.compiler.idl.reflect.idlInterface [] supports = value.supported();
        if ( supports.length != 0 )
        {
            writeInto.print( " supports " );
            for ( int i = 0; i < supports.length; i++ )
            {
                writeInto.print( fullname_idl( ( IdlObject ) supports[ i ] ) );
                if ( ( i + 1 ) != supports.length )
                {
                    writeInto.print( ", " );
                }
            }
        }
        writeInto.println( "" );
        indent( "{", writeInto );
        m_level++;
        translateInternalDescription( obj, writeInto );
        m_level--;
        indent( writeInto );
        writeInto.print( "}" );
    }

    /**
     * This method translates an Interface
     */
    public void translateInterface( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateInterface-> obj='" + obj.name() + "'" );
        }

        IdlInterface itf = ( IdlInterface ) obj;
        indent( "/**", writeInto );
        if ( itf.isForward() )
        {
            indent( " * Forward Interface " + obj.name() , writeInto );
        }
        else
        {
            indent( " * Interface " + obj.name() , writeInto );
        }
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( writeInto );
        if ( itf.abstract_interface() )
        {
            writeInto.print( "abstract " );
        }
        writeInto.print( "interface " + itf.name() );
        if ( itf.isForward() )
        {
            return;
        }
        java.util.Vector inheritance = itf.getInheritance();
        if ( inheritance.size() != 0 )
        {
            writeInto.print( " : " );
            for ( int i = 0; i < inheritance.size(); i++ )
            {
                writeInto.print( fullname_idl( ( ( IdlObject ) inheritance.elementAt( i ) ) ) );
                if ( ( i + 1 ) < inheritance.size() )
                {
                    writeInto.print( ", " );
                }
            }
        }
        writeInto.println( "" );
        indent( "{", writeInto );
        m_level++;
        translateInternalDescription( obj, writeInto );
        m_level--;
        indent( writeInto );
        writeInto.print( "}" );
    }

    /**
     * This method corrects an identifier to be a valid IDL identifier
     */
    public String correctIdentifier( String expr )
    {
        String correct_identifier = "";
        int index = 0, last_index = 0;
        java.util.Vector list = new java.util.Vector();
        int i = 0;
        String item = null;
        while ( true )
        {
            index = expr.indexOf( ".", last_index );
            if ( index == last_index )
            {
                break;
            }
            if ( index == -1 )
            {
                item = expr.substring( last_index, expr.length() );
                list.addElement( item );
                break;
            }
            item = expr.substring( last_index, index );
            list.addElement( item );
            last_index = index + 1;
        }
        for ( i = 0; i < list.size(); i++ )
        {
            item = ( String ) list.elementAt( i );
            if ( m_root.isDefined( item, false ) )
            {
                break;
            }
        }

        for ( int j = i; j < list.size(); j++ )
        {
            correct_identifier = correct_identifier + ( String ) list.elementAt( j );
            if ( ( j + 1 ) < list.size() )
            {
                correct_identifier = correct_identifier + "::";
            }
        }
        return correct_identifier;
    }

    /**
     * This method adapts an expression to be valid in an IDL description
     */
    public String adaptExpression( String expr )
    {
        char [] tmp = new char[ 500 ];
        String adapt_expr = "";
        int index = 0;
        int tmp_index = 0;

        while ( true )
        {
            if ( index == expr.length() )
            {
                break;
            }
            if ( Character.isDigit( expr.charAt( index ) ) )
            {
                while ( ( index < expr.length() )
                      && ( ( Character.isDigit( expr.charAt( index ) )
                      || ( expr.charAt( index ) == '.' ) ) ) )
                {
                    tmp[ tmp_index++ ] = expr.charAt( index++ );
                }
                tmp[ tmp_index ] = 0;
                adapt_expr = adapt_expr + new String( tmp, 0, tmp_index );
                tmp_index = 0;
            }
            else
            {
                if ( Character.isLetter( expr.charAt( index ) ) )
                {
                    while ( ( index < expr.length() )
                          && ( Character.isLetterOrDigit( expr.charAt( index ) )
                          || ( expr.charAt( index ) == '.' ) || ( expr.charAt( index ) == '_' ) ) )
                    {
                        tmp[ tmp_index++ ] = expr.charAt( index++ );
                    }
                    adapt_expr = adapt_expr + correctIdentifier( new String( tmp ) );
                    tmp_index = 0;
                }
                else
                {
                    if ( expr.charAt( index ) == '\"' )
                    {
                        index++;
                        while ( expr.charAt( index ) != '\"' )
                        {
                            tmp[ tmp_index++ ] = expr.charAt( index++ );
                        }
                        tmp[ tmp_index ] = 0;
                        adapt_expr = adapt_expr + "\"" + new String( tmp, 0, tmp_index ) + "\"";
                        tmp_index = 0;
                        index++;
                    }
                    else
                    {
                        tmp[ 0 ] = expr.charAt( index++ );
                        tmp[ 1 ] = 0;
                        adapt_expr = adapt_expr + new String( tmp, 0, 1 );
                    }
                }
            }
        }
        return adapt_expr;
    }

    /**
     * This method translates a Constant
     */
    public void translateConstant( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateConstant-> obj='" + obj.name() + "'" );
        }

        indent( "/**", writeInto );
        indent( " * Constant " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( writeInto );
        writeInto.print( "const " );
        translateType( obj.current(), writeInto, obj.name(), true );
        writeInto.print( " = " + adaptExpression( ( ( IdlConst ) obj ).expression() ) + "" );
    }

    /**
     * This method translates a Native
     */
    public void translateNative( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateNative-> obj='" + obj.name() + "'" );
        }

        indent( "/**", writeInto );
        indent( " * Native " + obj.name() , writeInto );
        indent( " */", writeInto );
        writeInto.println( "" );
        indent( writeInto );
        writeInto.print( "native " + obj.name() );
    }

    /**
     * This method translates an internal IDL description to an IDL file.
     */
    public void translateInternalDescription( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateInternalDescription-> obj='" + obj.name() + "'" );
        }

        obj.reset();
        while ( !obj.end() )
        {
            if ( !obj.current().included() )
            {
                switch ( obj.current().kind() )
                {
                case IdlType.e_const:
                    translateConstant( obj.current(), writeInto );
                    break;

                case IdlType.e_enum:
                    translateEnum( obj.current(), writeInto );
                    break;

                case IdlType.e_struct:
                    translateStruct( obj.current(), writeInto );
                    break;

                case IdlType.e_union:
                    translateUnion( obj.current(), writeInto );
                    break;

                case IdlType.e_typedef:
                    translateTypedef( obj.current(), writeInto );
                    break;

                case IdlType.e_exception:
                    translateException( obj.current(), writeInto );
                    break;

                case IdlType.e_native:
                    translateNative( obj.current(), writeInto );
                    break;

                case IdlType.e_state_member:
                    translateStateMember( obj.current(), writeInto );
                    break;

                case IdlType.e_factory:
                    translateInit( obj.current(), writeInto );
                    break;

                case IdlType.e_attribute:
                    translateAttribute( obj.current(), writeInto );
                    break;

                case IdlType.e_operation:
                    translateOperation( obj.current(), writeInto );
                    break;

                default:
                    System.out.println( "Default = " + obj.current().kind()
                          + " / " + obj.name() + " ( " + obj.current().name() );
                    break;
                }
            }
            obj.next();
            writeInto.println( ";" );
            writeInto.println( "" );
        }
    }

    /**
     * This method returns a pragma name
     */
    public String pragmaName( String name )
    {
        return name.replace( ':', '_' ).replace( '.', '_' );
    }

    /**
     * This method translates IDL description to an IDL file.
     */
    public void translateDescription( IdlObject obj, java.io.PrintWriter writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateDescription-> obj='" + obj.name() + "'" );
        }

        obj.reset();
        while ( !obj.end() )
        {
            if ( !obj.current().included() )
            {
                if ( obj.current().getPrefix() != null )
                {
                    if ( !obj.current().getPrefix().equals( m_prefix ) )
                    {
                        m_prefix = obj.current().getPrefix();
                        writeInto.println( "#pragma m_prefix \"" + m_prefix + "\"" );
                        writeInto.println( "" );
                    }
                }
                if ( obj.current().use_diese() )
                {
                    writeInto.println( "#ifndef __"
                          + pragmaName( fullname( obj.current() ) ) + "__" );
                    writeInto.println( "#define __"
                          + pragmaName( fullname( obj.current() ) ) + "__" );
                    writeInto.println( "" );
                }
                switch ( obj.current().kind() )
                {
                case IdlType.e_module:
                    translateModule( obj.current(), writeInto );
                    break;

                case IdlType.e_const:
                    translateConstant( obj.current(), writeInto );
                    break;

                case IdlType.e_enum:
                    translateEnum( obj.current(), writeInto );
                    break;

                case IdlType.e_struct:
                    translateStruct( obj.current(), writeInto );
                    break;

                case IdlType.e_union:
                    translateUnion( obj.current(), writeInto );
                    break;

                case IdlType.e_typedef:
                    translateTypedef( obj.current(), writeInto );
                    break;

                case IdlType.e_exception:
                    translateException( obj.current(), writeInto );
                    break;

                case IdlType.e_native:
                    translateNative( obj.current(), writeInto );
                    break;

                case IdlType.e_value_box:
                    translateValueBox( obj.current(), writeInto );
                    break;

                case IdlType.e_forward_value:
                case IdlType.e_value:
                    translateValueType( obj.current(), writeInto );
                    break;

                case IdlType.e_forward_interface:
                case IdlType.e_interface:
                    translateInterface( obj.current(), writeInto );
                    break;

                case IdlType.e_include:
                    writeInto.println( "#include \""
                          + ( ( IdlInclude ) obj.current() ).file_name() + ".idl\"" );
                    writeInto.println( "" );
                    obj.next();
                    continue;
                }
            }
            if ( !obj.current().included() )
            {
                writeInto.println( ";" );
                writeInto.println( "" );
                if ( !obj.current().getId().startsWith( "IDL" ) )
                {
                    writeInto.println( "#pragma ID " + obj.current().name()
                          + " \"" + obj.current().getId() + "\"" );
                    writeInto.println( "" );
                }
                if ( obj.current().use_diese() )
                {
                    writeInto.println( "#endif" );
                    writeInto.println( "" );
                }
            }
            obj.next();
        }
    }

    /**
     * This method translates IDL descriptions to an IDL file and creates
     * the IDL file.
     */
    public void translateToIDL( IdlObject root, String file_name )
    {
        File dest_dir = m_cp.getM_destdir();
        if ( dest_dir == null )
        {
            dest_dir = new File( DEFAULT_GENERATED_FOLDER_NAME );
        }
        File nf = new File( dest_dir, file_name );

        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateToIDL-> obj='" + root.name()
                  + "', output file '" + nf + "'" );
        }

        java.io.PrintWriter idl = create_idl_file( nf.getAbsolutePath() );
        m_root = root;
        translateDescription( root, idl );
        idl.println( "#endif" );
        idl.close();
    }

    /**
     * Adapt an identifier to RMI naming
     */
    public String adaptToRMI ( IdlObject obj )
    {
        java.util.Vector v = new java.util.Vector();
        IdlObject obj2 = obj;
        String name = new String( "" );
        String s;
        boolean first = false;
        while ( obj2 != null )
        {
            if ( first )
            {
                v.addElement( obj2.name() );
            }
            else
            {
                v.addElement( obj2.name() );
            }
            if ( obj2.upper() != null )
            {
                if ( obj2.upper().kind() == IdlType.e_root )
                {
                    break;
                }
            }
            obj2 = obj2.upper();
            first = true;
        }
        for ( int i = v.size() - 1; i >= 0; i-- )
        {
            s = ( String ) v.elementAt( i );
            if ( s != null )
            {
                if ( !name.equals( "" ) )
                {
                    name = name + ".";
                }
                name = name + s;
            }
        }
        return name;
    }

    /**
     * Returns true if the value type contains a sequence
     */
    private boolean include_sequence( IdlObject obj )
    {
        IdlObject o = obj;
        o.reset();
        if ( !o.end() )
        {
            if ( o.current().kind() == IdlType.e_sequence )
            {
                return true;
            }
            if ( o.current().kind() == IdlType.e_value )
            {
                return include_sequence( o.current() );
            }
        }

        return false;
    }

    private static String convertIDLToJava( final String idltype )
    {
        StringBuffer buf = new StringBuffer();
        StringTokenizer strtok = new StringTokenizer( idltype, "::" );
        while ( strtok.hasMoreTokens() )
        {
            String tok = strtok.nextToken();
            buf.append( tok );
            if ( strtok.hasMoreTokens() )
            {
                buf.append( "." );
            }
        }
        return buf.toString();
    }

    /**
     * Translate a type
     */
    public void translate_type( IdlObject obj, java.io.PrintWriter output )
    {
        String fullname = fullname_idl( obj );
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_type-> obj='" + obj.name()
                  + "', fullname_idl='" + fullname + "'" );
        }

        if ( fullname.equals( "::javax::rmi::CORBA::ClassDesc" ) )
        {
            output.print( "java.lang.Class" );
        }
        else if ( fullname.equals( "::java::lang::_Object" ) )
        {
            output.print( "java.lang.Object" );
        }
        else if ( fullname.equals( "::java::rmi::Remote" ) )
        {
            output.print( "java.rmi.Remote" );
        }
        else if ( fullname.equals( "::java::io::Serializable" ) )
        {
            output.print( "java.io.Serializable" );
        }
        else if ( fullname.equals( "::java::io::Externalizable" ) )
        {
            output.print( "java.io.Externalizable" );
        }
        else if ( fullname.equals( "::org::omg::boxedIDL::CORBA::Any" ) )
        {
            output.print( "org.omg.CORBA.Any" );
        }
        else if ( fullname.equals( "::org::omg::boxedIDL::CORBA::TypeCode" ) )
        {
            output.print( "org.omg.CORBA.TypeCode" );
        }
        else if ( fullname.startsWith( "::org::omg::boxedIDL::" ) )
        {
            String javaName = fullname.substring( "::org::omg::boxedIDL::".length() );
            output.print( convertIDLToJava( javaName ) );
        }
        else
        {
            super.translate_type( obj, output );
        }
    }

    /**
     * Translate a parameter
     */
    public void translate_parameter( IdlObject obj, java.io.PrintWriter output, int attr )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_parameter-> obj='" + obj.name() + "'" );
        }

        if ( fullname_idl( obj ).equals( "::javax::rmi::CORBA::ClassDesc" ) )
        {
            output.print( "java.lang.Class" );
        }
        else if ( fullname_idl( obj ).equals( "::java::lang::_Object" ) )
        {
            output.print( "java.lang.Object" );
        }
        else if ( fullname_idl( obj ).equals( "::java::rmi::Remote" ) )
        {
            output.print( "java.rmi.Remote" );
        }
        else if ( fullname_idl( obj ).equals( "::java::io::Serializable" ) )
        {
            output.print( "java.io.Serializable" );
        }
        else if ( fullname_idl( obj ).equals( "::java::io::Externalizable" ) )
        {
            output.print( "java.io.Externalizable" );
        }
        else if ( fullname_idl( obj ).equals( "::org::omg::boxedIDL::CORBA::Any" ) )
        {
            output.print( "org.omg.CORBA.Any" );
        }
        else if ( fullname_idl( obj ).equals( "::org::omg::boxedIDL::CORBA::TypeCode" ) )
        {
            output.print( "org.omg.CORBA.TypeCode" );
        }
        else if ( fullname_idl( obj ).startsWith( "::org::omg::boxedIDL::" ) )
        {
            String javaName = fullname_idl( obj ).substring( "::org::omg::boxedIDL::".length() );
            output.print( convertIDLToJava( javaName ) );
        }
        else
        {
            super.translate_parameter( obj, output, attr );
        }
    }

    /**
     * Translate an unmarshalling for a RMI data
     */
    public void translate_marshalling_data ( IdlObject obj, java.io.PrintWriter output,
          String outname, String tname )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_marshalling_data-> obj='" + obj.name()
                  + "', outname='" + outname + "', tname='" + tname + "'" );
        }

        IdlSimple simple = null;
        if ( fullname_idl( obj ).equals( "::java::lang::_Object" ) )
        {
            output.println( "javax.rmi.CORBA.Util.writeAny( " + outname + "," + tname + " );" );
            return;
        }
        else if ( fullname_idl( obj ).equals( "::javax::rmi::CORBA::ClassDesc" ) )
        {
            output.print( outname + ".write_value( " + tname + ", " );
            translate_type( obj, output );
            output.println( ".class );" );
            return;
        }
        else if ( fullname_idl( obj ).equals( "::java::io::Serializable" ) )
        {
            output.println( "javax.rmi.CORBA.Util.writeAny( " + outname + "," + tname + " );" );
            return;
        }
        else if ( fullname_idl( obj ).equals( "::java::io::Externalizable" ) )
        {
            output.println( "javax.rmi.CORBA.Util.writeAny( " + outname + ", " + tname + " );" );
            return;
        }
        else if ( fullname_idl( obj ).equals( "::CORBA::_Object" ) )
        {
            output.println( outname + ".write_Object( " + tname + " );" );
            return;
        }
        else if ( fullname_idl( obj ).equals( "::java::rmi::Remote" ) )
        {
            output.println( "javax.rmi.CORBA.Util.writeRemoteObject( " + outname + ", "
                  + tname + "  );" );
            return;
        }
        switch ( obj.kind() )
        {

        case IdlType.e_simple:
            simple = ( IdlSimple ) obj;
            if ( simple.internal() == Token.t_ValueBase )
            {
                output.println( "org.omg.CORBA.portable.ValueBaseHelper.write( " + outname
                      + ", ( org.omg.CORBA.portable.ValueBase )" + tname + " );" );
                return;
            }
            else
            {
                if ( simple.internal() == Token.t_object )
                {
                    output.println( "javax.rmi.CORBA.Util.writeRemoteObject( " + outname + ", "
                          + tname + " );" );
                    return;
                }
            }
            output.print( outname + ".write" );
            switch ( simple.internal() )
            {
            case Token.t_float:
                output.print( "_float" );
                break;

            case Token.t_double:
                output.print( "_double" );
                break;

            case Token.t_short:
                output.print( "_short" );
                break;

            case Token.t_ushort:
                output.print( "_ushort" );
                break;

            case Token.t_long:
                output.print( "_long" );
                break;

            case Token.t_ulong:
                output.print( "_ulong" );
                break;

            case Token.t_longlong:
                output.print( "_longlong" );
                break;

            case Token.t_ulonglong:
                output.print( "_ulonglong" );
                break;

            case Token.t_char:
                output.print( "_char" );
                break;

            case Token.t_wchar:
                output.print( "_wchar" );
                break;

            case Token.t_boolean:
                output.print( "_boolean" );
                break;

            case Token.t_octet:
                output.print( "_octet" );
                break;

            case Token.t_any:
                output.print( "_any" );
                break;

            case Token.t_typecode:
                output.print( "_TypeCode" );
                break;
            }
            output.println( "( " + tname + " );" );
            break;

        case IdlType.e_fixed:
            output.println( outname + ".write_fixed( " + tname + " );" );
            break;

        case IdlType.e_string:
            output.println( outname + ".write_string( " + tname + " );" );
            break;

        case IdlType.e_wstring:
            output.println( outname + ".write_wstring( " + tname + " );" );
            break;

        case IdlType.e_struct:
        case IdlType.e_union:
        case IdlType.e_enum:
        case IdlType.e_native:
        case IdlType.e_forward_interface:
            output.println( "javax.rmi.CORBA.Util.writeRemoteObject( " + outname + ", "
                  + tname + " );" );
            break;

        case IdlType.e_interface:
            if ( ( ( IdlInterface ) obj ).abstract_interface() )
            {
                output.println( "javax.rmi.CORBA.Util.writeAbstractObject( " + outname
                      + ", " + tname + " );" );
            }
            else
            {
                output.println( "javax.rmi.CORBA.Util.writeRemoteObject( " + outname
                      + ", " + tname + " );" );
            }
            break;

        case IdlType.e_value_box:
        case IdlType.e_value:
            if ( include_sequence( obj ) )
            {
                output.print( outname
                      + ".write_value( ( java.io.Serializable ) ( ( java.lang.Object ) "
                      + tname + " ), " );
            }
            else
            {
                output.print( outname + ".write_value( ( java.io.Serializable )" + tname + ", " );
            }
            translate_type( obj, output );
            output.println( ".class );" );
            break;

        case IdlType.e_typedef:
            output.print( fullname( obj ) );
            output.println( "Helper.write( " + outname + "," + tname + " );" );
            break;

        case IdlType.e_array:
        case IdlType.e_sequence:
            break;

        case IdlType.e_ident:
            translate_marshalling_data( ( ( IdlIdent ) obj ).internalObject(), output,
                  outname, tname );
            break;
        }
    }

    /**
     * Translate an unmarshalling for a RMI data
     */
    public void translate_unmarshalling_data( IdlObject obj, java.io.PrintWriter output,
          String inname )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_unmarshalling_data-> obj='"
                  + obj.name() + "', inname='" + inname + "'" );
        }

        IdlSimple simple = null;
        if ( fullname_idl( obj ).equals( "::java::lang::_Object" ) )
        {
            output.println( "javax.rmi.CORBA.Util.readAny( " + inname + " );" );
            return;
        }
        else if ( fullname_idl( obj ).equals( "::javax::rmi::CORBA::ClassDesc" ) )
        {
            output.print( "( java.lang.Class )" );
            output.print( inname + ".read_value( " );
            translate_type( obj, output );
            output.println( ".class );" );
            return;
        }
        else if ( fullname_idl( obj ).equals( "::CORBA::_Object" ) )
        {
            output.println( inname + ".read_Object();" );
            return;
        }
        else if ( fullname_idl( obj ).equals( "::java::io::Serializable" ) )
        {
            output.println( "( java.io.Serializable )javax.rmi.CORBA.Util.readAny( "
                  + inname + " );" );
            return;
        }
        else if ( fullname_idl( obj ).equals( "::java::io::Externalizable" ) )
        {
            output.println( "( java.io.Externalizable )javax.rmi.CORBA.Util.readAny( "
                  + inname + " );" );
            return;
        }
        else if ( fullname_idl( obj ).equals( "::java::rmi::Remote" ) )
        {
            output.print( "( " + fullname( obj ) + " ) javax.rmi.PortableRemoteObject.narrow( " );
            output.print( inname + ".read_Object()" );
            output.println( ", java.rmi.Remote.class );" );
            return;
        }
        switch ( obj.kind() )
        {
        case IdlType.e_simple:
            simple = ( IdlSimple ) obj;
            if ( simple.internal() == Token.t_ValueBase )
            {
                output.println( "org.omg.CORBA.portable.ValueBaseHelper.read( " + inname + " );" );
                return;
            }
            output.print( inname + ".read" );
            switch ( simple.internal() )
            {
            case Token.t_float:
                output.println( "_float();" );
                break;

            case Token.t_double:
                output.println( "_double();" );
                break;

            case Token.t_short:
                output.println( "_short();" );
                break;

            case Token.t_ushort:
                output.println( "_ushort();" );
                break;

            case Token.t_long:
                output.println( "_long();" );
                break;

            case Token.t_ulong:
                output.println( "_ulong();" );
                break;

            case Token.t_longlong:
                output.println( "_longlong();" );
                break;

            case Token.t_ulonglong:
                output.println( "_ulonglong();" );
                break;

            case Token.t_char:
                output.println( "_char();" );
                break;

            case Token.t_wchar:
                output.println( "_wchar();" );
                break;

            case Token.t_boolean:
                output.println( "_boolean();" );
                break;

            case Token.t_octet:
                output.println( "_octet();" );
                break;

            case Token.t_any:
                output.println( "_any();" );
                break;

            case Token.t_typecode:
                output.println( "_TypeCode();" );
                break;

            case Token.t_object:
                output.println( "_Object();" );
                break;
            }
            break;

        case IdlType.e_fixed:
            output.println( inname + ".read_fixed();" );
            break;

        case IdlType.e_string:
            output.println( inname + ".read_string();" );
            break;

        case IdlType.e_wstring:
            output.println( inname + ".read_wstring();" );
            break;

        case IdlType.e_struct:
        case IdlType.e_union:
        case IdlType.e_enum:
        case IdlType.e_native:
        case IdlType.e_forward_interface:
            output.print( "( " + fullname( obj ) + " ) javax.rmi.PortableRemoteObject.narrow( " );
            output.print( inname + ".read_Object()" );
            output.println( ", " + fullname( obj ) + ".class );" );
            break;

        case IdlType.e_interface:
            if ( ( ( IdlInterface ) obj ).abstract_interface() )
            {
                output.print( "( " + fullname( obj )
                      + " )javax.rmi.PortableRemoteObject.narrow( " );
                output.print( "( ( org.omg.CORBA_2_3.portable.InputStream )" + inname
                      + " ).read_abstract_interface()" );
                output.println( ", " + fullname( obj ) + ".class );" );
            }
            else
            {
                output.print( "( " + fullname( obj )
                      + " )javax.rmi.PortableRemoteObject.narrow( " );
                output.print( inname + ".read_Object()" );
                output.println( ", " + fullname( obj ) + ".class );" );
            }
            break;

        case IdlType.e_value_box:
        case IdlType.e_value:
        case IdlType.e_forward_value:
            output.print( "( " );
            translate_type( obj, output );
            output.print( " )" );
            if ( include_sequence( obj ) )
            {
                output.print( "( java.lang.Object )" );
            }
            output.print( "( ( org.omg.CORBA_2_3.portable.InputStream )" + inname
                  + " ).read_value( " );
            translate_type( obj, output );
            output.println( ".class );" );
            break;

        case IdlType.e_typedef:
            output.print( fullname( obj ) );
            output.println( "Helper.read( " + inname + " );" );
            break;

        case IdlType.e_array:
        case IdlType.e_sequence:
            break;

        case IdlType.e_ident:
            translate_unmarshalling_data( ( ( IdlIdent ) obj ).internalObject(), output, inname );
            break;
        }
    }

    /**
     * Translate a read attribute for RMI Tie
     */
    public void translate_read_attribute_rmi_tie( IdlObject obj, java.io.PrintWriter output )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_read_attribute_rmi_tie-> obj='"
                  + obj.name() + "'" );
        }

        obj.reset();
        output.print( "                " );
        ( ( org.openorb.compiler.generator.IdlToJava ) this ).translate_type(
              obj.current(), output );
        output.println( " arg = target." + ( String ) obj.opaque() + "();" );
        output.println( "                _output = "
              + "( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();" );
        output.print( "                " );
        translate_marshalling_data( obj.current(), output, "_output", "arg" );
        output.println( "                return _output;" );
    }

    /**
     * Translate a write attribute for RMI Tie
     */
    public void translate_write_attribute_rmi_tie ( IdlObject obj, java.io.PrintWriter output )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_write_attribute_rmi_tie-> obj='"
                  + obj.name() + "'" );
        }

        obj.reset();
        output.print( "                " );
        ( ( org.openorb.compiler.generator.IdlToJava ) this ).translate_type(
              obj.current(), output );
        output.print( " result = " );
        translate_unmarshalling_data( obj.current(), output, "_is" );
        output.println( "" );
        String op = ( String ) obj.opaque();
        if ( op.startsWith( "get" ) )
        {
            op = op.substring( 3 );
        }
        else
        {
            op = op.substring( 2 );
        }
        output.println( "                target.set" + op + "( result );" );
        output.println( "                _output = "
              + "( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();" );
        output.println( "                return _output;" );
    }

    /**
     * Translate an operation for RMI Tie
     */
    public void translate_operation_rmi_tie( IdlObject obj, java.io.PrintWriter output )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_operation_rmi_tie-> obj='" + obj.name() + "'" );
        }

        IdlRaises r;
        int i = 0;
        boolean raises = false;
        boolean someParams = false;
        // Extract parameters
        obj.reset();
        obj.next();
        if ( !obj.end() )
        {
            if ( obj.current().kind() == IdlType.e_param )
            {
                while ( !obj.end() )
                {
                    obj.current().reset();
                    switch ( ( ( IdlParam ) obj.current() ).param_attr() )
                    {
                    case 0:
                        output.print( "                " );
                        ( ( org.openorb.compiler.generator.IdlToJava )
                              this ).translate_type( obj.current().current(), output );
                        output.print( " arg" + i + "_in = " );
                        translate_unmarshalling_data( obj.current().current(), output, "_is" );
                        break;

                    case 1:
                    case 2:
                        System.out.println( "Warning: OUT and INOUT parameters do not work in"
                              + " RMIoverIIOP" );
                        break;
                    }
                    i++;
                    obj.next();
                    if ( !obj.end() )
                    {
                        if ( obj.current().kind() != IdlType.e_param )
                        {
                            break;
                        }
                    }
                }
            }
        }
        i = 0;
        output.println( "" );
        // Regarde s'il faut gerer des exceptions
        if ( !obj.end() )
        {
            if ( obj.current().kind() == IdlType.e_raises )
            {
                output.println( "                try" );
                output.println( "                {" );
                raises = true;
            }
        }
        obj.reset();
        // Effectue l'appel
        if ( obj.current().kind() == IdlType.e_simple )
        {
            if ( ( ( IdlSimple ) obj.current() ).internal() != Token.t_void )
            {
                if ( raises )
                {
                    output.print( "    " );
                }
                output.print( "                " );
                ( ( org.openorb.compiler.generator.IdlToJava ) this ).translate_type(
                      obj.current(), output );
                output.print( " _arg_result = " );
            }
            else
            {
                if ( raises )
                {
                    output.print( "    " );
                }
                output.print( "                " );
            }
        }
        else
        {
            if ( raises )
            {
                output.print( "    " );
            }
            output.print( "                " );
            ( ( org.openorb.compiler.generator.IdlToJava ) this ).translate_type(
                  obj.current(), output );
            output.print( " _arg_result = " );
        }
        output.print( "target." + ( String ) obj.opaque() + "( " );
        obj.next();
        if ( !obj.end() )
        {
            if ( obj.current().kind() == IdlType.e_param )
            {
                someParams = true;
                while ( !obj.end() )
                {
                    obj.current().reset();
                    switch ( ( ( IdlParam ) obj.current() ).param_attr() )
                    {
                        case 0:
                            output.print( "arg" + i + "_in" );
                            break;
                        case 1:
                        case 2:
                            // Not needed for RMI
                            break;
                    }
                    i++;
                    obj.next();
                    if ( !obj.end() )
                    {
                        if ( obj.current().kind() != IdlType.e_param )
                        {
                            break;
                        }
                        else
                        {
                            output.print( ", " );
                        }
                    }
                }
            }
        }
        i = 0;
        output.println( " );" );
        output.println( "" );
        if ( raises )
        {
            output.print( "    " );
        }
        output.println( "                _output = "
              + "( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();" );
        // Encode les parametres de retour
        obj.reset();
        if ( obj.current().kind() == IdlType.e_simple )
        {
            if ( ( ( IdlSimple ) obj.current() ).internal() != Token.t_void )
            {
                if ( raises )
                {
                    output.print( "    " );
                }
                output.print( "                " );
                translate_marshalling_data( obj.current(), output, "_output", "_arg_result" );
            }
        }
        else
        {
            if ( raises )
            {
                output.print( "    " );
            }
            output.print( "                " );
            translate_marshalling_data( obj.current(), output, "_output", "_arg_result" );
        }
        output.println( "" );
        obj.next();
        if ( !obj.end() )
        {
            if ( obj.current().kind() == IdlType.e_param )
            {
                while ( !obj.end() )
                {
                    obj.next();
                    if ( !obj.end() )
                    {
                        if ( obj.current().kind() != IdlType.e_param )
                        {
                            break;
                        }
                    }
                }
            }
        }
        // Capture les diverses exceptions possibles
        if ( !obj.end() )
        {
            if ( obj.current().kind() == IdlType.e_raises )
            {
                r = ( IdlRaises ) obj.current();
                r.reset();
                java.util.LinkedList excepts = new java.util.LinkedList();
                // add a first element to the sorted exception list
                excepts.add( r.current() );
                // proceed to the first one to compare
                r.next();
                // loop through the list of (unsorted) exceptions
                while ( !r.end() )
                {
                    try
                    {
                        Class clz1 = Thread.currentThread().getContextClassLoader().loadClass(
                              fullnameOpaque( r.current() ) );
                        // find the position where to insert the exception
                        int index = 0;
                        java.util.ListIterator iter = excepts.listIterator( index );
                        while ( iter.hasNext() )
                        {
                            IdlObject obj2 = ( IdlObject ) iter.next();
                            Class clz2 = Thread.currentThread().getContextClassLoader().loadClass(
                                  fullnameOpaque( obj2 ) );
                            if ( clz1.isAssignableFrom( clz2 ) )
                            {
                                index++;
                            }
                        }
                        // insert the object at the right position
                        excepts.add( index, r.current() );
                    }
                    catch ( ClassNotFoundException ex )
                    {
                        // TODO...
                    }
                    r.next();
                }
                output.println( "                }" );
                java.util.ListIterator li = excepts.listIterator( 0 );
                while ( li.hasNext() )
                {
                    IdlObject curobj = ( IdlObject ) li.next();
                    curobj.reset();
                    output.print( "                catch ( " );
                    Class clz = null;
                    try
                    {
                        clz = Thread.currentThread().getContextClassLoader().loadClass(
                              ( String ) curobj.opaque() );
                    }
                    catch ( ClassNotFoundException ex )
                    {
                        // ??
                    }
                    if ( ReflectionUtils.isAssignableFrom( "org.omg.CORBA.SystemException", clz )
                        || ReflectionUtils.isAssignableFrom( "org.omg.CORBA.UserException", clz ) )
                    {
                        translate_type( curobj, output );
                        output.println( " _exception )" );
                        output.println( "                {" );
                        output.println( "                    _output = "
                              + "( org.omg.CORBA_2_3.portable.OutputStream ) "
                              + "handler.createExceptionReply();" );
                        output.println( "                    " + fullnameOpaque( curobj )
                              + "Helper.write( _output, _exception );" );
                    }
                    else
                    {
                        curobj.current().reset();
                        translate_type( curobj.current().current(), output );
                        output.println( " _exception )" );
                        output.println( "                {" );
                        output.println( "                    String exid = \"" + curobj.getId()
                              + "\";" );
                        output.println( "                    _output = "
                              + "( org.omg.CORBA_2_3.portable.OutputStream ) "
                              + "handler.createExceptionReply();" );

                        output.println( "                    _output.write_string( exid );" );
                        output.println( "                    _output.write_value( _exception );" );
                    }
                    output.println( "                }" );
                }
            }
        }
        // Retourne le output
        output.println( "                return _output;" );
    }

    /**
     * Translate an interface for the RMI Tie
     */
    public void translate_rmi_tie_interface( IdlObject obj, java.io.File writeInto )
    {
        java.io.PrintWriter output = null;
        output = newFile( writeInto, "_" + obj.name() + "_Tie" );

        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_rmi_tie_interface-> obj='" + obj.name()
                  + "', output file '" + output + "'" );
        }

        addDescriptiveHeader( output, obj );
        java.util.List impList = getImportList( m_root );
        if ( impList != null && !org.openorb.util.JREVersion.V1_4  )
        {
            for ( int j = 0; j < impList.size(); j++ )
            {
                output.println( "import " + ( String ) impList.get( j ) + ";" );
            }
            output.println( "" );
        }
        java.util.List inhList = new java.util.Vector();
        inhList = getInheritanceList( obj, inhList );
        // Creation du package correspond au definitions internes de l'interface
        java.io.File intoMe = getDirectory( obj.name() + "Package", writeInto );
        // En-tete de l'interface
        if ( !m_cp.getM_map_poa() )
        {
            output.println( "public class _" + obj.name()
                  + "_Tie extends org.omg.CORBA_2_3.portable.ObjectImpl" );
        }
        else
        {
            output.println( "public class _" + obj.name()
                  + "_Tie extends org.omg.PortableServer.Servant" );
        }
        output.println( "    implements javax.rmi.CORBA.Tie" );
        output.println( "{" );
        output.println( "" );
        // Construit la liste des _ids
        output.println( "    static final String[] _ids_list =" );
        output.println( "    {" );
        for ( int i = 0; i < inhList.size(); i++ )
        {
            output.print( "        \"" + ( String ) inhList.get( i ) + "\"" );
            if ( i + 1 < inhList.size() )
            {
                output.println( ", " );
            }
        }
        output.println( "" );
        output.println( "    };" );
        output.println( "" );
        // Traduit l'operation _id ou _all_interfaces
        if ( !m_cp.getM_map_poa() )
        {
            output.println( "    public String[] _ids()" );
            output.println( "    {" );
            output.println( "        return _ids_list;" );
            output.println( "    }" );
            output.println( "" );
        }
        else
        {
            output.println( "    private org.omg.PortableServer.POA _poa;" );
            output.println( "    private byte [] _oid;" );
            output.println();
            output.println( "    public String[] _all_interfaces( "
                  + "org.omg.PortableServer.POA poa, byte [] oid )" );
            output.println( "    {" );
            output.println( "        _poa = poa;" );
            output.println( "        _oid = oid;" );
            output.println( "        return _ids_list;" );
            output.println( "    }" );
            output.println( "" );
        }
        // Target reference
        output.println( "    /**" );
        output.println( "     * Private reference to implementation object" );
        output.println( "     */" );
        output.println( "    private " + adaptToRMI( obj ) + " target;" );
        output.println( "" );
        // ORB reference
        output.println( "    /**" );
        output.println( "     * Private reference to the ORB" );
        output.println( "     */" );
        output.println( "    private org.omg.CORBA_2_3.ORB _orb;" );
        output.println( "" );
        // SetTarget
        output.println( "    /**" );
        output.println( "     * Set target object" );
        output.println( "     */" );
        output.println( "    public void setTarget( java.rmi.Remote targ )" );
        output.println( "    {" );
        output.println( "        target = ( " + obj.name() + " ) targ;" );
        output.println( "    }" );
        output.println( "" );
        // GetTarget
        output.println( "    /**" );
        output.println( "     * Get target object" );
        output.println( "     */" );
        output.println( "    public java.rmi.Remote getTarget()" );
        output.println( "    {" );
        output.println( "        return target;" );
        output.println( "    }" );
        output.println( "" );
        // thisObject
        output.println( "    /**" );
        output.println( "     * Returns an object reference for the target object" );
        output.println( "     */" );
        output.println( "    public org.omg.CORBA.Object thisObject()" );
        output.println( "    {" );
        if ( !m_cp.getM_map_poa() )
        {
            output.println( "        return this;" );
        }
        else
        {
            output.println( "        return _this_object();" );
        }
        output.println( "    }" );
        output.println( "" );
        // Deactivate
        output.println( "    /**" );
        output.println( "     * Deactivate the target object" );
        output.println( "     */" );
        output.println( "    public void deactivate()" );
        output.println( "    {" );
        if ( !m_cp.getM_map_poa() )
        {
            output.println( "        _orb.disconnect( this );" );
        }
        else
        {
            output.println( "        try" );
            output.println( "        {" );
            output.println( "            _poa.deactivate_object( _oid );" );
            output.println( "        }" );
            output.println( "        catch ( "
                  + "org.omg.PortableServer.POAPackage.ObjectNotActive ex )" );
            output.println( "        {}" );
            output.println( "        catch ( org.omg.PortableServer.POAPackage.WrongPolicy ex )" );
            output.println( "        {}" );
        }
        output.println( "        target = null;" );
        output.println( "    }" );
        output.println( "" );
        // ORB()
        output.println( "    /**" );
        output.println( "     * Return the ORB" );
        output.println( "     */" );
        output.println( "    public org.omg.CORBA.ORB orb()" );
        output.println( "    {" );
        output.println( "        return _orb;" );
        output.println( "    }" );
        output.println( "" );
        // ORB( ... )
        output.println( "    /**" );
        output.println( "     * Set the ORB" );
        output.println( "     */" );
        output.println( "    public void orb( org.omg.CORBA.ORB orb )" );
        output.println( "    {" );
        output.println( "        _orb = ( org.omg.CORBA_2_3.ORB ) orb;" );
        if ( !m_cp.getM_map_poa() )
        {
            output.println( "        _orb.connect( this );" );
        }
        else
        {
            output.println( "        _orb.set_delegate( this );" );
        }
        output.println( "    }" );
        output.println( "" );
        if ( m_rcp.getMapEJBExceptions() )
        {
            output.println( "    public Throwable mapException(Throwable exception)" );
            output.println( "    {" );
            output.println( "        if ( exception instanceof java.rmi.NoSuchObjectException)" );
            output.println( "            return new org.omg.CORBA.OBJECT_NOT_EXIST();" );
            output.println( "        else if ( exception instanceof java.rmi.AccessException )" );
            output.println( "            return new org.omg.CORBA.NO_PERMISSION();" );
            output.println( "        else if ( exception instanceof java.rmi.MarshalException )" );
            output.println( "            return new org.omg.CORBA.MARSHAL();" );
            output.println( "        else if ( exception instanceof "
                  + "javax.transaction.TransactionRolledbackException )" );
            output.println( "            return new org.omg.CORBA.TRANSACTION_ROLLEDBACK();" );
            output.println( "        else if ( exception instanceof "
                  + "javax.transaction.TransactionRequiredException )" );
            output.println( "            return new org.omg.CORBA.TRANSACTION_REQUIRED();" );
            output.println( "        else if ( exception instanceof "
                  + "javax.transaction.InvalidTransactionException )" );
            output.println( "            return new org.omg.CORBA.INVALID_TRANSACTION();" );
            output.println( "        return new org.omg.CORBA.portable.UnknownException( "
                  + "exception );" );
            output.println( "    }" );
            output.println( "" );
        }
        // Invoke
        output.println( "    /**" );
        output.println( "     * Invoke method ( for remote call )" );
        output.println( "     */" );
        output.println( "    public org.omg.CORBA.portable.OutputStream _invoke(String opName, "
              + "org.omg.CORBA.portable.InputStream is, org.omg.CORBA.portable.ResponseHandler "
              + "handler)" );
        output.println( "    {" );
        output.println( "        org.omg.CORBA_2_3.portable.InputStream _is = "
              + "( org.omg.CORBA_2_3.portable.InputStream ) is;" );
        output.println( "        org.omg.CORBA_2_3.portable.OutputStream _output = null;" );
        output.println( "        try" );
        output.println( "        {" );
        java.util.List intoList = getInheritanceOpList( obj, new java.util.Vector() );
        for ( int i = 0; i < intoList.size(); i++ )
        {
            switch ( ( ( IdlObject ) intoList.get( i ) ).kind() )
            {
            case IdlType.e_operation:
                output.println( "            if ( opName.equals( \""
                      + ( ( IdlObject ) intoList.get( i ) ).name() + "\" ) )" );
                output.println( "            {" );
                translate_operation_rmi_tie( ( ( IdlObject ) intoList.get( i ) ), output );
                output.println( "            }" );
                break;

            case IdlType.e_attribute:
                output.println( "            if ( opName.equals( \"_get_"
                      + ( ( IdlObject ) intoList.get( i ) ).name() + "\" ) )" );
                output.println( "            {" );
                translate_read_attribute_rmi_tie( ( ( IdlObject ) intoList.get( i ) ), output );
                output.println( "            }" );
                if ( !( ( IdlAttribute ) ( ( IdlObject ) intoList.get( i ) ) ).readOnly() )
                {
                    output.println( "            else" );
                    output.println( "            if ( opName.equals( \"_set_"
                          + ( ( IdlObject ) intoList.get( i ) ).name() + "\" ) )" );
                    output.println( "            {" );
                    translate_write_attribute_rmi_tie( ( ( IdlObject ) intoList.get( i ) ),
                          output );
                    output.println( "            }" );
                }
                break;
            }
            output.println( "            else" );
        }
        output.println( "                throw new org.omg.CORBA.BAD_OPERATION();" );
        output.println( "        }" );
        output.println( "        catch ( org.omg.CORBA.SystemException ex )" );
        output.println( "        {" );
        output.println( "            throw ex;" );
        output.println( "        }" );
        output.println( "        catch ( Throwable ex )" );
        output.println( "        {" );
        if ( m_rcp.getMapEJBExceptions() )
        {
            output.println( "            throw ( RuntimeException ) mapException( ex );" );
        }
        else
        {
            output.println( "            throw new "
                  + "org.omg.CORBA.portable.UnknownException( ex );" );
        }
        output.println( "        }" );
        output.println( "    }" );
        output.println( "}" );
        output.close();
    }

    /**
     * Translate a module for the RMI Tie
     */
    public void translate_rmi_tie_module( IdlObject obj, java.io.File writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_rmi_tie_module-> obj='" + obj.name() + "'" );
        }

        String old_pkg;
        java.io.File intoModule;
        if ( ( obj.getPrefix() != null ) && ( obj.upper().kind() == IdlType.e_root ) )
        {
            writeInto = getPrefixDirectories( obj.getPrefix(), writeInto );
        }
        intoModule = getDirectory( obj.name(), writeInto );
        old_pkg = current_pkg;
        if ( ( obj.getPrefix() != null ) && ( obj.upper().kind() == IdlType.e_root ) )
        {
            addToPkg( obj, inversedPrefix( obj.getPrefix() ) + "." + obj.name() );
        }
        else
        {
            addToPkg( obj, obj.name() );
        }
        translate_rmi_tie( obj, intoModule );
        current_pkg = old_pkg;
    }

    /**
     * Translate an object for the RMI Tie
     */
    public void translate_rmi_tie( IdlObject obj, java.io.File writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_rmi_tie-> obj='" + obj.name() + "'" );
        }

        obj.reset();
        while ( !obj.end() )
        {
            if ( !obj.current().included() )
            {
                switch ( obj.current().kind() )
                {
                case IdlType.e_module:
                    translate_rmi_tie_module( obj.current(), writeInto );
                    break;

                case IdlType.e_interface:
                    if ( !( ( IdlInterface ) ( obj.current() ) ).abstract_interface() )
                    {
                        translate_rmi_tie_interface( obj.current(), writeInto );
                    }
                    break;
                }
            }
            obj.next();
        }
    }

    /**
     * This method creates an Tie class for a RMI object.
     */
    public void translateRMITie( IdlObject root )
    {
        m_root = root;
        File dest_dir = m_cp.getM_destdir();
        if ( dest_dir == null )
        {
            dest_dir = new File( DEFAULT_GENERATED_FOLDER_NAME );
        }
        File writeInto = dest_dir;

        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateRMITie-> root='" + root.name()
                  + "', output folder '" + writeInto + "'" );
        }

        translate_rmi_tie( root, writeInto );
    }

    /**
     * This "fullname" with Opaque value as last part
     */
    public String fullnameOpaque ( IdlObject obj )
    {
        // MR: The exceptions have always the fully qualified name
        // stored in the opaque field: "obj.opaque( c.getName() );"
        if ( obj.kind() == IdlType.e_exception )
        {
            return ( String ) obj.opaque();
        }
        IdlObject upper = obj.upper();
        String full = null;
        if ( upper != null )
        {
            if ( upper.kind() == IdlType.e_root )
            {
                full = fullname( upper );
            }
        }
        if ( full != null )
        {
            full = full + ".";
        }
        else
        {
            full = "";
        }
        return full;
    }

    /**
     * Translate an operation for a RMI stub
     */
    public void translate_operation_rmi_stub( IdlObject obj, IdlObject base,
          java.io.PrintWriter output )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_operation_rmi_stub-> obj='" + obj.name() + "'" );
        }

        IdlRaises r;
        boolean someParams = false;
        boolean noReturn = false;
        int i = 0, p;
        output.println( "    /**" );
        output.println( "     * Operation " + obj.name() );
        output.println( "     */" );
        output.print( "    public " );
        obj.reset();
        translate_type( obj.current(), output );
        output.print( " " + ( String ) obj.opaque() + "(" );
        obj.next();
        if ( !obj.end() )
        {
            if ( obj.current().kind() == IdlType.e_param )
            {
                someParams = true;
                while ( !obj.end() )
                {
                    obj.current().reset();
                    translate_parameter( obj.current().current(), output,
                          ( ( IdlParam ) obj.current() ).param_attr() );
                    output.print( " " + obj.current().name() );
                    obj.next();
                    if ( !obj.end() )
                    {
                        if ( obj.current().kind() == IdlType.e_param )
                        {
                            output.print( ", " );
                        }
                        else
                        {
                            break;
                        }
                    }
                }
            }
        }
        output.print( ")" );
        output.println( "" );
        output.print( "        throws " );
        if ( !obj.end() )
        {
            if ( obj.current().kind() == IdlType.e_raises )
            {
                r = ( IdlRaises ) obj.current();
                r.reset();
                while ( !r.end() )
                {
                    String ex = fullnameOpaque( r.current() );
                    output.print( ex );
                    r.next();
                    output.print( ", " );
                }
            }
        }
        output.println( "java.rmi.RemoteException" );
        output.println( "    {" );
        output.println( "        while( true )" );
        output.println( "        {" );
        if ( m_cp.getM_map_poa() )
        {
            if ( m_rcp.getMapLocal() )
            {
                output.println( "            if ( !javax.rmi.CORBA.Util.isLocal( this ) )" );
                output.println( "            {" );
            }
        }
        output.println( "                org.omg.CORBA_2_3.portable.InputStream _input = null;" );
        output.println( "                try" );
        output.println( "                {" );
        if ( ( ( IdlOp ) obj ).oneway() )
        {
            output.println( "                    org.omg.CORBA_2_3.portable.OutputStream "
                  + "_output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request( \""
                  + obj.name() + "\", false );" );
        }
        else
        {
            output.println( "                    org.omg.CORBA_2_3.portable.OutputStream "
                  + "_output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request( \""
                  + obj.name() + "\", true );" );
        }
        obj.reset();
        obj.next();
        if ( !obj.end() )
        {
            if ( obj.current().kind() == IdlType.e_param )
            {
                while ( !obj.end() )
                {
                    obj.current().reset();
                    switch ( ( ( IdlParam ) obj.current() ).param_attr() )
                    {
                        case 0:
                            output.print( "                    " );
                            obj.current().reset();
                            translate_marshalling_data( obj.current().current(), output,
                                  "_output", obj.current().name() );
                            break;
                    }
                    obj.next();
                    if ( !obj.end() )
                    {
                        if ( obj.current().kind() != IdlType.e_param )
                        {
                            break;
                        }
                    }
                }
            }
        }
        output.println( "                    _input = "
              + "( org.omg.CORBA_2_3.portable.InputStream ) this._invoke( _output );" );
        obj.reset();
        if ( obj.current().kind() == IdlType.e_simple )
        {
            if ( ( ( IdlSimple ) obj.current() ).internal() != Token.t_void )
            {
                output.print( "                    " );
                translate_type( obj.current(), output );
                output.print( " _arg_ret = " );
                translate_unmarshalling_data( obj.current(), output, "_input" );
            }
        }
        else
        {
            output.print( "                    " );
            translate_type( obj.current(), output );
            output.print( " _arg_ret = " );
            translate_unmarshalling_data( obj.current(), output, "_input" );
        }
        obj.next();
        if ( !obj.end() )
        {
            if ( obj.current().kind() == IdlType.e_param )
            {
                while ( !obj.end() )
                {
                    obj.next();
                    if ( !obj.end() )
                    {
                        if ( obj.current().kind() != IdlType.e_param )
                        {
                            break;
                        }
                    }
                }
            }
        }
        p = obj.pos();
        obj.reset();
        if ( obj.current().kind() == IdlType.e_simple )
        {
            if ( ( ( IdlSimple ) obj.current() ).internal() != Token.t_void )
            {
                output.println( "                    return _arg_ret;" );
            }
            else
            {
                output.println( "                    return;" );
                noReturn = true;
            }
        }
        else
        {
            output.println( "                    return _arg_ret;" );
        }
        output.println( "                }" );
        output.println( "                catch ( "
              + "org.omg.CORBA.portable.RemarshalException _exception )" );
        output.println( "                {" );
        output.println( "                    continue;" );
        output.println( "                }" );

        output.println( "                catch ( "
              + "org.omg.CORBA.portable.ApplicationException _exception )" );
        output.println( "                {" );
        output.println( "                    _input = "
              + "( org.omg.CORBA_2_3.portable.InputStream ) _exception.getInputStream();" );
        output.println( "                    java.lang.String "
              + "_exception_id = _exception.getId();" );
        obj.pos( p );
        if ( !obj.end() )
        {
            if ( obj.current().kind() == IdlType.e_raises )
            {
                r = ( IdlRaises ) obj.current();
                r.reset();
                while ( !r.end() )
                {
                    output.println( "                    if ( _exception_id.equals( \""
                          + r.current().getId() + "\" ) )" );
                    output.println( "                    {" );
                    Class clz = null;
                    try
                    {
                        clz = Thread.currentThread().getContextClassLoader().loadClass(
                              ( String ) r.current().opaque() );
                    }
                    catch ( ClassNotFoundException ex )
                    {
                        // ??
                    }
                    String ex = fullnameOpaque( r.current() );

                    if ( ReflectionUtils.isAssignableFrom( "org.omg.CORBA.UserException", clz ) )
                    {
                        // for corba types the id is read by the xyzHelper.read() method
                        output.println( "                        throw " + ex
                              + "Helper.read( _input );" );
                    }
                    else
                    {
                        // we need to read the id in this case only
                        output.println( "                        _input.read_string();" );
                        output.println( "                        throw ( " + ex
                              + " ) _input.read_value( " + ex + ".class );" );
                    }
                    output.println( "                    }" );
                    output.println( "" );
                    r.next();
                }
            }
        }
        output.println( "                    throw new "
              + "java.rmi.UnexpectedException( _exception_id );" );
        output.println( "                }" );
        output.println( "                catch ( org.omg.CORBA.SystemException _exception )" );
        output.println( "                {" );
        output.println( "                    throw "
              + "javax.rmi.CORBA.Util.mapSystemException( _exception );" );
        output.println( "                }" );
        output.println( "                finally" );
        output.println( "                {" );
        output.println( "                    this._releaseReply( _input );" );
        output.println( "                }" );
        if ( m_cp.getM_map_poa() )
        {
            if ( m_rcp.getMapLocal() )
            {
                output.println( "            }" );
                output.println( "            else" );
                output.println( "            {" );
                output.println( "                org.omg.CORBA.portable.ServantObject "
                      + "_so = _servant_preinvoke( \"" + obj.name() + "\", _opsClass );" );
                output.println( "                if ( _so == null )" );
                if ( noReturn )
                {
                    output.print( "                   " + ( String ) obj.opaque() + "(" );
                }
                else
                {
                    output.print( "                   return " + ( String ) obj.opaque() + "( " );
                }
                obj.reset();
                obj.next();
                if ( !obj.end() )
                {
                    if ( obj.current().kind() == IdlType.e_param )
                    {
                        someParams = true;

                        while ( !obj.end() )
                        {
                            obj.current().reset();
                            output.print( " " + obj.current().name() );
                            obj.next();
                            if ( !obj.end() )
                            {
                                if ( obj.current().kind() == IdlType.e_param )
                                {
                                    output.print( ", " );
                                }
                                else
                                {
                                    break;
                                }
                            }
                        }
                    }
                }
                output.println( " );" );
                output.println( "                try" );
                output.println( "                {" );
                String methodName = ( String ) obj.opaque();
                obj.reset();
                obj.next();
                if ( !obj.end() )
                {
                    while ( !obj.end() )
                    {
                        if ( obj.current().kind() == IdlType.e_param )
                        {
                            obj.current().reset();
                            output.print( "                    " );
                            translate_parameter( obj.current().current(), output,
                                  ( ( IdlParam ) obj.current() ).param_attr() );
                            output.print( " " + obj.current().name() + "Copy = " );
                            if ( obj.current().current().kind() == IdlType.e_simple )
                            {
                                output.println( obj.current().name() + ";" );
                            }
                            else
                            {
                                output.print( "( " );
                                translate_parameter( obj.current().current(), output,
                                      ( ( IdlParam ) obj.current() ).param_attr() );
                                output.println( " ) javax.rmi.CORBA.Util.copyObject( "
                                      + obj.current().name() + ", _orb() );" );
                            }
                        }
                        obj.next();
                    }
                }
                obj.reset();
                IdlObject returnType = obj.current();
                if ( noReturn )
                {
                    output.print( "                    ( ( " + fullname( base )
                          + " ) _so.servant )." + methodName + "( " );
                }
                else
                {
                    output.print( "                    " );
                    translate_type( returnType, output );
                    output.print( " _arg_ret = ( ( " + fullname( base )
                          + " ) _so.servant )." + methodName + "( " );
                }
                obj.next();
                if ( !obj.end() )
                {
                    if ( obj.current().kind() == IdlType.e_param )
                    {
                        someParams = true;
                        while ( !obj.end() )
                        {
                            obj.current().reset();
                            output.print( " " + obj.current().name() + "Copy" );
                            obj.next();
                            if ( !obj.end() )
                            {
                                if ( obj.current().kind() == IdlType.e_param )
                                {
                                    output.print( ", " );
                                }
                                else
                                {
                                    break;
                                }
                            }
                        }
                    }
                }
                output.println( ");" );
                if ( noReturn )
                {
                    output.println( "                    return;" );
                }
                else
                {
                    if ( returnType.kind() == IdlType.e_simple )
                    {
                        output.println( "                    return _arg_ret;" );
                    }
                    else
                    {
                        output.print( "                    return ( " );
                        translate_type( returnType, output );
                        output.print( ") javax.rmi.PortableRemoteObject.narrow( "
                              + "javax.rmi.CORBA.Util.copyObject( _arg_ret, _orb() ), " );
                        translate_type( returnType, output );
                        output.println( ".class);" );
                    }
                }
                output.println( "                }" );
                output.println( "                catch ( Throwable ex )" );
                output.println( "                {" );
                output.println( "                    Throwable ex2 = ( Throwable )"
                      + " javax.rmi.CORBA.Util.copyObject( ex, _orb() );" );
                obj.pos( p );
                if ( !obj.end() )
                {
                    if ( obj.current().kind() == IdlType.e_raises )
                    {
                        r = ( IdlRaises ) obj.current();
                        r.reset();
                        while ( !r.end() )
                        {
                            String ex = fullnameOpaque( r.current() );
                            output.println( "                    if ( ex2 instanceof " + ex
                                  + " )" );
                            output.println( "                        throw ( " + ex + " ) ex2;" );
                            output.println( "" );
                            r.next();
                        }

                    }
                }
                output.println( "                    throw "
                      + "javax.rmi.CORBA.Util.wrapException( ex2 );" );
                output.println( "                }" );
                output.println( "                finally" );
                output.println( "                {" );
                output.println( "                    _servant_postinvoke( _so );" );
                output.println( "                }" );
                output.println( "            }" );
            }
        }
        output.println( "        }" );
        output.println( "    }" );
        output.println( "" );
    }

    /**
     * Translate an read attribute for a RMI stub
     */
    public void translate_read_attribute_rmi_stub( IdlObject obj, IdlObject base,
          java.io.PrintWriter output )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_read_attribute_rmi_stub-> obj='"
                  + obj.name() + "'" );
        }

        IdlRaises r;
        boolean someParams = false;
        boolean noReturn = false;
        int i = 0, p;
        output.println( "    /**" );
        output.println( "     * Attribute " + ( String ) obj.opaque() );
        output.println( "     */" );
        output.print( "    public " );
        obj.reset();
        translate_type( obj.current(), output );
        output.println( " " + ( String ) obj.opaque() + "()" );
        output.println( "        throws java.rmi.RemoteException" );
        output.println( "    {" );
        output.println( "        while( true )" );
        output.println( "        {" );
        if ( m_cp.getM_map_poa() )
        {
            if ( m_rcp.getMapLocal() )
            {
                output.println( "            if ( !javax.rmi.CORBA.Util.isLocal( this ) )" );
                output.println( "            {" );
            }
        }
        output.println( "                org.omg.CORBA_2_3.portable.InputStream _input = null;" );
        output.println( "                try" );
        output.println( "                {" );
        output.println( "                    org.omg.CORBA_2_3.portable.OutputStream "
              + "_output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request( \"_get_"
              + obj.name() + "\", true );" );
        // Effectue l'appel
        output.println( "                    _input = "
              + "( org.omg.CORBA_2_3.portable.InputStream ) this._invoke( _output );" );
        // Decode les parametres
        obj.reset();
        if ( obj.current().kind() == IdlType.e_simple )
        {
            if ( ( ( IdlSimple ) obj.current() ).internal() != Token.t_void )
            {
                output.print( "                    " );
                translate_type( obj.current(), output );
                output.print( " _arg_ret = " );
                translate_unmarshalling_data( obj.current(), output, "_input" );
            }
        }
        else
        {
            output.print( "                    " );
            translate_type( obj.current(), output );
            output.print( " _arg_ret = " );
            translate_unmarshalling_data( obj.current(), output, "_input" );
        }
        // Renvoie le resultat
        obj.reset();
        if ( obj.current().kind() == IdlType.e_simple )
        {
            if ( ( ( IdlSimple ) obj.current() ).internal() != Token.t_void )
            {
                output.println( "                    return _arg_ret;" );
            }
            else
            {
                output.println( "                    return;" );
                noReturn = true;
            }
        }
        else
        {
            output.println( "                    return _arg_ret;" );
        }
        output.println( "                }" );
        output.println( "                catch ( "
              + "org.omg.CORBA.portable.RemarshalException _exception )" );
        output.println( "                {" );
        output.println( "                    continue;" );
        output.println( "                }" );
        // Recupere les exceptions utilisateurs
        output.println( "                catch ( "
              + "org.omg.CORBA.portable.ApplicationException _exception )" );
        output.println( "                {" );
        output.println( "                    java.lang.String _exception_id = "
              + "_exception.getId();" );
        output.println( "                    throw "
              + "new java.rmi.UnexpectedException( _exception_id );" );
        output.println( "                }" );
        output.println( "                catch ( org.omg.CORBA.SystemException _exception )" );
        output.println( "                {" );
        output.println( "                    throw "
              + "javax.rmi.CORBA.Util.mapSystemException( _exception );" );
        output.println( "                }" );
        output.println( "                finally" );
        output.println( "                {" );
        output.println( "                    this._releaseReply( _input );" );
        output.println( "                }" );
        if ( m_cp.getM_map_poa() )
        {
            if ( m_rcp.getMapLocal() )
            {
                output.println( "            }" );
                output.println( "            else" );
                output.println( "            {" );
                output.println( "                org.omg.CORBA.portable.ServantObject "
                      + "_so = _servant_preinvoke( \"_get_" + obj.name() + "\", _opsClass );" );
                output.println( "                if ( _so == null )" );
                if ( noReturn )
                {
                    output.println( "                   " + ( String ) obj.opaque() + "();" );
                }
                else
                {
                    output.println( "                   return "
                          + ( String ) obj.opaque() + "();" );
                }
                output.println( "                try" );
                output.println( "                {" );
                if ( noReturn )
                {
                    output.print( "                    ( ( "
                          + fullname( base ) + " ) _so.servant )."
                          + ( String ) obj.opaque() + "();" );
                    output.println( "                    return;" );
                }
                else
                {
                    output.print( "                    " );
                    translate_type( obj.current(), output );
                    output.println( " _arg_ret = ( ( " + fullname( base )
                          + ") _so.servant )." + ( String ) obj.opaque() + "();" );
                    if ( obj.current().kind() == IdlType.e_simple )
                    {
                        output.println( "                    return _arg_ret;" );
                    }
                    else
                    {
                        output.print( "                    return ( " );
                        translate_type( obj.current(), output );
                        output.print( ") javax.rmi.PortableRemoteObject.narrow( "
                              + "javax.rmi.CORBA.Util.copyObject( _arg_ret, _orb() ), " );
                        translate_type( obj.current(), output );
                        output.println( ".class);" );
                    }
                }
                output.println( "                }" );
                output.println( "                catch ( Throwable ex )" );
                output.println( "                {" );
                output.println( "                    Throwable ex2 = ( Throwable ) "
                      + "javax.rmi.CORBA.Util.copyObject( ex, _orb() );" );
                output.println( "                    throw javax.rmi.CORBA.Util.wrapException( "
                      + "ex2 );" );
                output.println( "                }" );
                output.println( "                finally" );
                output.println( "                {" );
                output.println( "                    _servant_postinvoke( _so );" );
                output.println( "                }" );
                output.println( "            }" );
            }
        }
        output.println( "        }" );
        output.println( "    }" );
        output.println( "" );
    }

    /**
     * Translate a write attribute for a RMI stub
     */
    public void translate_write_attribute_rmi_stub( IdlObject obj, IdlObject base,
          java.io.PrintWriter output )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_write_attribute_rmi_stub-> obj='"
                  + obj.name() + "', base='" + base.name() + "'" );
        }

        IdlRaises r;
        boolean someParams = false;
        boolean noReturn = false;
        int i = 0, p;
        String op = ( String ) obj.opaque();
        if ( op.startsWith( "get" ) )
        {
            op = op.substring( 3 );
        }
        else
        {
            op = op.substring( 2 );
        }
        output.println( "    /**" );
        output.println( "     * Write Attribute set" + op );
        output.println( "     */" );
        output.print( "    public void " );
        output.print( " set" + op + "( " );
        ( ( org.openorb.compiler.generator.IdlToJava ) this ).translate_type(
              obj.current(), output );
        output.print( " arg )" );
        output.println( "" );
        output.println( "        throws java.rmi.RemoteException" );
        output.println( "    {" );
        output.println( "        while( true )" );
        output.println( "        {" );
        if ( m_cp.getM_map_poa() )
        {
            if ( m_rcp.getMapLocal() )
            {
                output.println( "            if ( !javax.rmi.CORBA.Util.isLocal( this ) )" );
                output.println( "            {" );
            }
        }
        output.println( "                org.omg.CORBA_2_3.portable.InputStream _input = null;" );
        output.println( "                try" );
        output.println( "                {" );
        output.println( "                    org.omg.CORBA_2_3.portable.OutputStream "
              + "_output = ( org.omg.CORBA_2_3.portable.OutputStream ) this._request( \"_set_"
              + obj.name() + "\", true );" );
        // Encode les parametres
        output.print( "                    " );
        translate_marshalling_data( obj.current(), output, "_output", "arg" );
        // Effectue l'appel
        output.println( "                    _input = "
              + "( org.omg.CORBA_2_3.portable.InputStream ) this._invoke( _output );" );
        output.println( "                    return;" );
        noReturn = true;
        output.println( "                }" );
        output.println( "                catch ( "
              + "org.omg.CORBA.portable.RemarshalException _exception )" );
        output.println( "                {" );
        output.println( "                    continue;" );
        output.println( "                }" );
        // Recupere les exceptions utilisateurs
        output.println( "                catch ( "
              + "org.omg.CORBA.portable.ApplicationException _exception )" );
        output.println( "                {" );
        output.println( "                    java.lang.String _exception_id = "
              + "_exception.getId();" );
        output.println( "                    throw "
              + "new org.omg.CORBA.UNKNOWN( \"Unexcepected User Exception: \"+ _exception_id );" );
        output.println( "                }" );
        output.println( "                catch ( org.omg.CORBA.SystemException _exception )" );
        output.println( "                {" );
        output.println( "                    throw "
              + "javax.rmi.CORBA.Util.mapSystemException( _exception );" );
        output.println( "                }" );
        output.println( "                finally" );
        output.println( "                {" );
        output.println( "                    this._releaseReply( _input );" );
        output.println( "                }" );
        if ( m_cp.getM_map_poa() )
        {
            if ( m_rcp.getMapLocal() )
            {
                output.println( "            }" );
                output.println( "            else" );
                output.println( "            {" );
                output.println( "                org.omg.CORBA.portable.ServantObject "
                      + "_so = _servant_preinvoke( \"_set_" + obj.name() + "\", _opsClass );" );
                output.println( "                if ( _so == null )" );
                output.println( "                   set" + op + "( arg );" );
                output.println( "                try" );
                output.println( "                {" );
                output.print( "                    " );
                ( ( org.openorb.compiler.generator.IdlToJava ) this ).translate_type(
                      obj.current(), output );
                output.print( " argCopy = " );
                if ( obj.current().kind() == IdlType.e_simple )
                {
                    output.println( "arg;" );
                }
                else
                {
                    output.print( "( " );
                    ( ( org.openorb.compiler.generator.IdlToJava ) this ).translate_type(
                          obj.current(), output );
                    output.println( " ) javax.rmi.CORBA.Util.copyObject( arg, _orb() );" );
                }
                output.println( "                    ( ( " + fullname( base )
                      + " ) _so.servant ).set" + op + "( argCopy );" );
                output.println( "                    return;" );
                output.println( "                }" );
                output.println( "                catch ( Throwable ex )" );
                output.println( "                {" );
                output.println( "                    Throwable ex2 = ( Throwable ) "
                      + "javax.rmi.CORBA.Util.copyObject( ex, _orb() );" );
                output.println( "                    throw "
                      + "javax.rmi.CORBA.Util.wrapException( ex2 );" );
                output.println( "                }" );
                output.println( "                finally" );
                output.println( "                {" );
                output.println( "                    _servant_postinvoke( _so );" );
                output.println( "                }" );
                output.println( "            }" );
            }
        }
        output.println( "        }" );
        output.println( "    }" );
        output.println( "" );
    }

    /**
     * Translate an interface for the RMI Stub
     */
    public void translate_rmi_stub_interface( IdlObject obj, java.io.File writeInto )
    {
        java.io.PrintWriter output = null;
        output = newFile( writeInto, "_" + obj.name() + "_Stub" );

        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_rmi_stub_interface-> obj='" + obj.name()
                  + "', output file '" + output + "'" );
        }

        addDescriptiveHeader( output, obj );
        java.util.List impList = getImportList( m_root );
        if ( impList != null && !org.openorb.util.JREVersion.V1_4 )
        {
            for ( int j = 0; j < impList.size(); j++ )
            {
                output.println( "import " + ( String ) impList.get( j ) + ";" );
            }
            output.println( "" );
        }
        java.util.List inhList = new java.util.Vector();
        inhList = getInheritanceList( obj, inhList );
        // Creation du package correspond au definitions internes de l'interface
        java.io.File intoMe = getDirectory( obj.name() + "Package", writeInto );
        output.println( "public class _" + obj.name() + "_Stub extends javax.rmi.CORBA.Stub" );
        output.println( "    implements " + obj.name() );
        output.println( "{" );
        output.println( "" );
        // Create the typeid array
        output.println( "    static final String[] _ids_list =" );
        output.println( "    {" );
        for ( int i = 0; i < inhList.size(); i++ )
        {
            output.print( "        \"" + ( String ) inhList.get( i ) + "\"" );
            if ( i + 1 < inhList.size() )
            {
                output.println( ", " );
            }
        }
        output.println( "" );
        output.println( "    };" );
        output.println( "" );
        // Create the _id method
        output.println( "    public String[] _ids()" );
        output.println( "    {" );
        output.println( "        return _ids_list;" );
        output.println( "    }" );
        output.println( "" );
        if ( m_cp.getM_map_poa() )
        {
            output.println( "    final public static java.lang.Class _opsClass = " + obj.name()
                  + ".class;" );
            output.println( "" );
        }
        java.util.List intoList = getInheritanceOpList( obj, new java.util.Vector() );
        for ( int i = 0; i < intoList.size(); i++ )
        {
            switch ( ( ( IdlObject ) intoList.get( i ) ).kind() )
            {
            case IdlType.e_operation:
                translate_operation_rmi_stub( ( ( IdlObject ) intoList.get( i ) ), obj, output );
                break;

            case IdlType.e_attribute:
                translate_read_attribute_rmi_stub( ( ( IdlObject ) intoList.get( i ) ),
                      obj, output );
                if ( !( ( IdlAttribute ) ( ( IdlObject ) intoList.get( i ) ) ).readOnly() )
                {
                    translate_write_attribute_rmi_stub( ( ( IdlObject ) intoList.get( i ) ),
                          obj, output );
                }
                break;
            }
        }
        output.println( "}" );
        output.close();
    }

    /**
     * Translate a module for the RMI Stub
     */
    public void translate_rmi_stub_module( IdlObject obj, java.io.File writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_rmi_stub_module-> obj='" + obj.name() + "'" );
        }

        String old_pkg;
        java.io.File intoModule;
        if ( ( obj.getPrefix() != null ) && ( obj.upper().kind() == IdlType.e_root ) )
        {
            writeInto = getPrefixDirectories( obj.getPrefix(), writeInto );
        }
        intoModule = getDirectory( obj.name(), writeInto );
        old_pkg = current_pkg;
        if ( ( obj.getPrefix() != null ) && ( obj.upper().kind() == IdlType.e_root ) )
        {
            addToPkg( obj, inversedPrefix( obj.getPrefix() ) + "." + obj.name() );
        }
        else
        {
            addToPkg( obj, obj.name() );
        }
        translate_rmi_stub( obj, intoModule );
        current_pkg = old_pkg;
    }

    /**
     * Translate an object for the RMI stub
     */
    public void translate_rmi_stub( IdlObject obj, java.io.File writeInto )
    {
        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translate_rmi_stub-> obj='" + obj.name() + "'" );
        }

        obj.reset();
        while ( !obj.end() )
        {
            if ( !obj.current().included() )
            {
                switch ( obj.current().kind() )
                {
                case IdlType.e_module:
                    translate_rmi_stub_module( obj.current(), writeInto );
                    break;

                case IdlType.e_interface:
                    if ( !( ( IdlInterface ) ( obj.current() ) ).abstract_interface() )
                    {
                        translate_rmi_stub_interface( obj.current(), writeInto );
                    }
                    break;
                }
            }
            obj.next();
        }
    }

    /**
     * This method creates an Stub class for a RMI object.
     */
    public void translateRMIStub( IdlObject root )
    {
        m_root = root;
        File dest_dir = m_cp.getM_destdir();
        if ( dest_dir == null )
        {
            dest_dir = new File( DEFAULT_GENERATED_FOLDER_NAME );
        }
        File writeInto = dest_dir;

        if ( m_rcp.getM_verbose() )
        {
            m_ch.display( "RMIGenerator::translateRMIStub-> root='" + root.name()
                  + "', output folder '" + writeInto + "'" );
        }

        translate_rmi_stub( root, writeInto );
    }
}

