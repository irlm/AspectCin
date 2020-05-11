/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.idl.util;

import org.openorb.compiler.CompilerProperties;

import org.openorb.compiler.generator.IdlToJava;

import org.openorb.compiler.object.IdlComment;
import org.openorb.compiler.object.IdlCommentField;
import org.openorb.compiler.object.IdlCommentSection;
import org.openorb.compiler.object.IdlObject;

import org.openorb.compiler.parser.IdlType;

/**
 * This class provides several tools to help user to define its own translation.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.6 $ $Date: 2004/02/10 21:02:38 $
 */
public class tools
{
    public static final String tab = "    ";

    /**
     * Reference to the generator
     */
    private static IdlToJava getGenerator( CompilerProperties cp )
    {
        return new IdlToJava( cp );
    }

    /**
     * This operation creates and returns a print access to a new file.
     */
    public static java.io.PrintWriter createNewFile( java.io.File into, String name )
    {
        String path;
        java.io.PrintWriter printout = null;

        path = new String( into.getPath() + java.io.File.separator + name + ".java" );

        java.io.File file = new java.io.File( path );

        try
        {
            org.openorb.util.DiffFileOutputStream output = new org.openorb.util.DiffFileOutputStream( file );
            java.io.DataOutputStream dataout = new java.io.DataOutputStream( output );
            printout = new java.io.PrintWriter( dataout );
        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
        }

        return printout;
    }

    /**
     * This operation prints the object package name ( according to the IDL to Java translation rules ).
     */
    public static void writePackageName( CompilerProperties cp, java.io.PrintWriter writer, org.openorb.compiler.idl.reflect.idlObject obj )
    {
        if ( obj.idlDefinedIn() != null )
            writer.println( "package " + fullname( cp, obj.idlDefinedIn() ) + ";" );
    }

    /**
     * This operation returns a Java name for an IDL object accroding to the IDL to Java mapping rules.
     */
    public static String javaName( CompilerProperties cp, org.openorb.compiler.idl.reflect.idlObject obj )
    {
        if ( obj.idlType() == org.openorb.compiler.idl.reflect.idlType.IDENTIFIER )
        {
            return fullname( cp, ( ( org.openorb.compiler.idl.reflect.idlIdentifier ) obj ).original() );
        }
        return fullname( cp, obj );
    }

    private static String fullname ( CompilerProperties cp, org.openorb.compiler.idl.reflect.idlObject obj )
    {
        java.util.Vector v = new java.util.Vector();
        org.openorb.compiler.object.IdlObject obj2 = ( org.openorb.compiler.object.IdlObject ) obj;
        String name = new String( "" );
        String s;
        boolean first = false;

        while ( obj2 != null )
        {
            if ( first )
            {
                if ( ( obj2.kind() == org.openorb.compiler.parser.IdlType.e_interface ) ||
                        ( obj2.kind() == org.openorb.compiler.parser.IdlType.e_value ) ||
                        ( obj2.kind() == org.openorb.compiler.parser.IdlType.e_struct ) ||
                        ( obj2.kind() == org.openorb.compiler.parser.IdlType.e_union ) ||
                        ( obj2.kind() == org.openorb.compiler.parser.IdlType.e_exception ) )
                {
                    v.addElement( ( obj2.name() + "Package" ) );
                }
                else
                {
                    v.addElement( obj2.name() );
                }
            }
            else
            {
                v.addElement( obj2.name() );
            }

            if ( obj2.upper() != null )
            {
                if ( obj2.upper().kind() == org.openorb.compiler.parser.IdlType.e_root )
                {
                    break;
                }
            }

            obj2 = obj2.upper();

            first = true;
        }

        if ( cp.getM_packageName() != null )
        {
            if ( !obj.included() )
            {
                if ( !cp.getM_packageName().equals( "" ) )
                {
                    if ( !( ( cp.getM_packageName().equals( "generated" ) ) && ( cp.getM_use_package() == false ) ) )
                    {
                        name = adaptToDot( cp.getM_packageName() );
                    }
                }
            }
        }

        if ( cp.getM_usePrefix() )
        {
            if ( ( ( org.openorb.compiler.object.IdlObject ) obj ).getPrefix() != null )
            {
                if ( !name.equals( "" ) )
                    name = name + ".";

                name = name + inversedPrefix( ( ( org.openorb.compiler.object.IdlObject ) obj ) .getPrefix() );
            }
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

    public static String adaptToDot( String path )
    {
        char [] tmp = new char[ path.length() ];

        for ( int i = 0; i < path.length(); i++ )
        {
            if ( ( path.charAt( i ) == '/' ) || ( path.charAt( i ) == '\\' ) )
            {
                tmp[ i ] = '.';
            }
            else
            {
                tmp[ i ] = path.charAt( i );
            }
        }

        return new String( tmp );
    }

    public static String inversedPrefix ( String prefix )
    {
        int index = 0;
        int previous_index = 0;
        java.util.Vector seq = new java.util.Vector();
        String inversed = new String( "" );

        try
        {
            while ( index != -1 )
            {
                index = prefix.indexOf( '.', previous_index );

                if ( index != -1 )
                {
                    seq.addElement( new String( prefix.substring( previous_index, index ) ) );
                    previous_index = index + 1;
                }
            }
        }
        catch ( StringIndexOutOfBoundsException ex )
        {
            // ???
        }

        seq.addElement( new String( prefix.substring( previous_index, prefix.length() ) ) );

        for ( int i = seq.size() - 1; i >= 0; i-- )
        {
            if ( !inversed.equals( "" ) )
            {
                inversed = inversed + ".";
            }
            inversed = inversed + ( String ) seq.elementAt( i );
        }

        return inversed;
    }

    /**
     * This operations translates an IDL object to java
     */
    public static void javaType( CompilerProperties cp, org.openorb.compiler.idl.reflect.idlObject obj, java.io.PrintWriter output )
    {
        IdlToJava generator = getGenerator( cp );
        if ( obj.idlType() == org.openorb.compiler.idl.reflect.idlType.IDENTIFIER )
        {
            generator.translate_type( ( org.openorb.compiler.object.IdlObject ) ( ( org.openorb.compiler.idl.reflect.idlIdentifier ) obj ).original(), output );
            return;
        }

        generator.translate_type( ( org.openorb.compiler.object.IdlObject ) obj, output );
    }

    /**
     * This operations marshals an IDL object to java
     */
    public static void marshal( CompilerProperties cp, org.openorb.compiler.idl.reflect.idlObject obj, java.io.PrintWriter output, String streamName, String typeName )
    {
        IdlToJava generator = getGenerator( cp );
        generator.translate_marshalling_member( ( org.openorb.compiler.object.IdlObject ) obj, output, streamName, typeName, tab + tab );
    }

    /**
     * This operations unmarshals an IDL object to java
     */
    public static void unmarshal( CompilerProperties cp, org.openorb.compiler.idl.reflect.idlObject obj, java.io.PrintWriter output, String streamName, String typeName )
    {
        IdlToJava generator = getGenerator( cp );
        generator.translate_unmarshalling_member( ( org.openorb.compiler.object.IdlObject ) obj, output, streamName, typeName, tab + tab );
    }


    /**
     * Translate a JavaDoc comments section
     */
    public static void translate_comment_section( java.io.PrintWriter output, String description, IdlObject obj )
    {
        translate_comment_section_base( output, description, obj );
    }

    /**
     * Translate a JavaDoc comments section
     */
    public static void translate_comment_section_base( java.io.PrintWriter output, String description, IdlObject obj )
    {
        int i = 0;

        while ( i < description.length() )
        {
            if ( description.charAt( i ) == '\n' )
            {
                if ( i != description.length() - 1 )
                {
                    output.println( "" );

                    if ( ( obj.kind() == IdlType.e_attribute ) || ( obj.kind() == IdlType.e_operation ) || ( obj.kind() == IdlType.e_state_member ) )
                    {
                        output.print( tab );
                    }
                    output.print( " * " );
                }
                else
                {
                    output.println( "" );
                    return;
                }
            }
            else
            {
                output.print( description.charAt( i ) );
            }
            i++;
        }
    }

    /**
     * Add a JavaDoc comment
     *
     * @param output  the target file
     * @param obj   the object the header has to be added
     */
    public static void javadoc ( java.io.PrintWriter output, IdlObject obj )
    {
        javadoc_base( output, obj );
    }

    /**
     * Add a JavaDoc comment
     *
     * @param output  the target file
     * @param obj   the object the header has to be added
     */
    public static void javadoc_base ( java.io.PrintWriter output, IdlObject obj )
    {
        IdlComment comment = obj.getComment();
        String description = null;

        if ( comment != null )
        {
            description = comment.get_description();

            if ( ( obj.kind() == IdlType.e_attribute ) || ( obj.kind() == IdlType.e_operation ) || ( obj.kind() == IdlType.e_state_member ) )
            {
                output.print( tab );
            }
            output.println( "/**" );

            if ( ( obj.kind() == IdlType.e_attribute ) || ( obj.kind() == IdlType.e_operation ) || ( obj.kind() == IdlType.e_state_member ) )
            {
                output.print( tab );
            }
            output.print( " * " );

            translate_comment_section( output, description, obj );

            IdlCommentSection [] sections = comment.get_sections();

            for ( int i = 0; i < sections.length; i++ )
            {
                switch ( sections[ i ].kind().value() )
                {

                case IdlCommentField._author_field :

                    if ( ( obj.kind() == IdlType.e_attribute ) || ( obj.kind() == IdlType.e_operation ) )
                    {
                        output.print( tab );
                    }
                    output.print( " * @author " );

                    break;

                case IdlCommentField._deprecated_field :
                    if ( ( obj.kind() == IdlType.e_attribute ) || ( obj.kind() == IdlType.e_operation ) )
                    {
                        output.print( tab );
                    }
                    output.print( " * @deprecated " );

                    break;

                case IdlCommentField._exception_field :
                    if ( ( obj.kind() == IdlType.e_attribute ) || ( obj.kind() == IdlType.e_operation ) )
                    {
                        output.print( tab );
                    }
                    output.print( " * @exception " );

                    break;

                case IdlCommentField._return_field :
                    if ( ( obj.kind() == IdlType.e_attribute ) || ( obj.kind() == IdlType.e_operation ) )
                    {
                        output.print( tab );
                    }
                    output.print( " * @return " );

                    break;

                case IdlCommentField._param_field :
                    if ( ( obj.kind() == IdlType.e_attribute ) || ( obj.kind() == IdlType.e_operation ) )
                    {
                        output.print( tab );
                    }
                    output.print( " * @param " );

                    break;

                case IdlCommentField._see_field :
                    if ( ( obj.kind() == IdlType.e_attribute ) || ( obj.kind() == IdlType.e_operation ) )
                    {
                        output.print( tab );
                    }
                    output.print( " * @see " );

                    break;

                case IdlCommentField._version_field :
                    if ( ( obj.kind() == IdlType.e_attribute ) || ( obj.kind() == IdlType.e_operation ) )
                    {
                        output.print( tab );
                    }
                    output.print( " * @version " );

                    break;

                case IdlCommentField._unknown_field :
                    if ( ( obj.kind() == IdlType.e_attribute ) || ( obj.kind() == IdlType.e_operation ) )
                    {
                        output.print( tab );
                    }
                    output.print( " * @" + sections[ i ].get_title() + " " );

                    break;
                }

                description = sections[ i ].get_description();
                translate_comment_section( output, description, obj );
            }

            if ( ( obj.kind() == IdlType.e_attribute ) || ( obj.kind() == IdlType.e_operation ) || ( obj.kind() == IdlType.e_state_member ) )
            {
                output.print( tab + "" );
            }
            output.println( " */" );
        }
    }
}
