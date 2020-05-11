/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.doc.html;

import org.openorb.compiler.CompilerProperties;
import org.openorb.compiler.object.IdlArray;
import org.openorb.compiler.object.IdlAttribute;
import org.openorb.compiler.object.IdlComment;
import org.openorb.compiler.object.IdlCommentField;
import org.openorb.compiler.object.IdlCommentSection;
import org.openorb.compiler.object.IdlConst;
import org.openorb.compiler.object.IdlContext;
import org.openorb.compiler.object.IdlEnumMember;
import org.openorb.compiler.object.IdlIdent;
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


/**
 * This class takes an IDL graph and generates its HTML documentation
 */
public class IdlToHTML
{
    /**
     * Reference to the mapping level
     */
    private int level;

    /**
     * Reference to the Root object
     */
    private IdlObject _root;

    /**
     * Compiler properties
     */
    private CompilerProperties m_cp = null;

    /**
     * Constructor
     */
    public IdlToHTML( CompilerProperties cp )
    {
        m_cp = cp;
        level = 0;
    }

    /**
     * Inverse prefix : omg.org -> org.omg
     */
    public String inversedPrefix ( String prefix )
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
        { }

        seq.addElement( new String( prefix.substring( previous_index, prefix.length() ) ) );

        for ( int i = seq.size() - 1; i >= 0; i-- )
        {
            if ( !inversed.equals( "" ) )
                inversed = inversed + ".";

            inversed = inversed + ( String ) seq.elementAt( i );
        }

        return inversed;
    }

    /**
     * This method creates a new direcotry
     */
    public java.io.File createDirectory( String name, java.io.File writeInto )
    {
        String path;
        String fname;
        boolean init = false;

        char [] tab = new char [ name.length() + 20 ];

        int j = 0;

        for ( int i = 0; i < name.length(); i++ )
        {
            if ( name.charAt( i ) == '.' )
            {
                tab[ j++ ] = java.io.File.separator.charAt( 0 );
                init = true;
            }
            else
                tab[ j++ ] = name.charAt( i );
        }

        fname = new String( tab, 0, j );

        if ( writeInto != null )
        {
            path = new String( writeInto.getPath() + java.io.File.separator + fname );
        }
        else
            path = fname;

        java.io.File file = new java.io.File( path );

        if ( file.exists() == false )
            file.mkdirs();

        if ( init == true )
            m_cp.setM_packageName(fname);

        return file;
    }

    /**
     * This method create an index file for the HTML navigation
     */
    public void write_index_file( String title, java.io.File writeInto )
    {
        java.io.PrintWriter output = create_file( "index", writeInto );

        output.println( "<HTML>" );
        output.println( "<HEAD>" );
        output.println( "<!-- org.openorb, IDL to HTML generator  -->" );
        output.println( "<META http-equiv=\"content-type\" content=\"text/html; charset="+org.openorb.compiler.doc.IdlDoc.codepage+"\">" );
        output.println( "<TITLE>" );
        output.println( title );
        output.println( "</TITLE>" );
        output.println( "</HEAD>" );
        output.println( "<FRAMESET cols=\"20%,80%\">" );
        output.println( "<FRAMESET rows=\"30%,70%\">" );
        output.println( "<FRAME src=\"overview-frame.html\" name=\"moduleListFrame\">" );
        output.println( "<FRAME src=\"alldescriptions-frame.html\" name=\"moduleFrame\">" );
        output.println( "</FRAMESET>" );
        output.println( "<FRAME src=\"overview-summary.html\" name=\"descriptionFrame\">" );
        output.println( "</FRAMESET>" );
        output.println( "<NOFRAMES>" );
        output.println( "<H2>" );
        output.println( "Frame Alert</H2>" );
        output.println( "<P>" );
        output.println( "This document is designed to be viewed using the frames feature. If you see this message, you are using a non-frame-capable web client." );
        output.println( "<BR> Link to <A HREF=\"overview-summary.html\">Non-frame version.</A></NOFRAMES>" );
        output.println( "</HTML>" );

        output.close();
    }

    /**
     * This method create an overview file for the HTML navigation
     */
    public void write_overview_file( IdlObject obj, String title, java.io.File writeInto )
    {
        java.io.PrintWriter output = create_file( "overview-frame", writeInto );

        output.println( "<HTML>" );
        output.println( "<HEAD>" );
        output.println( "<!-- org.openorb, IDL to HTML generator  -->" );
        output.println( "<META http-equiv=\"content-type\" content=\"text/html; charset="+org.openorb.compiler.doc.IdlDoc.codepage+"\">" );
        output.println( "<TITLE>" );
        output.println( title + " : Overview" );
        output.println( "</TITLE>" );
        output.println( "<LINK REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"stylesheet.css\" TITLE=\"Style\">" );
        output.println( "<SCRIPT LANGUAGE=\"JavaScript\">" );
        output.println( "<!-- " );
        output.println( "function goTo(file1,file2)" );
        output.println( "{" );
        output.println( "window.parent.frames['moduleFrame'].location=file1;" );
        output.println( "window.parent.frames['descriptionFrame'].location=file2;" );
        output.println( "return" );
        output.println( "}" );
        output.println( "//-->" );
        output.println( "</SCRIPT>" );
        output.println( "</HEAD>" );
        output.println( "<BODY BGCOLOR=\"white\">" );
        output.println( "<TABLE BORDER=\"0\" WIDTH=\"100%\">" );
        output.println( "<TR>" );
        output.println( "<TD NOWRAP><FONT size=\"+1\" ID=\"FrameTitleFont\">" );
        output.println( "<B><b>" + title + "</b><br><font size=\"-1\"></font></B></FONT></TD>" );
        output.println( "</TR>" );
        output.println( "</TABLE>" );

        output.println( "<TABLE BORDER=\"0\" WIDTH=\"100%\">" );
        output.println( "<TR>" );
        output.println( "<TD NOWRAP><FONT ID=\"FrameItemFont\"><A HREF=\"JavaScript:goTo('alldescriptions-frame.html', 'overview-summary.html')\">All Descriptions</A></FONT>" );
        output.println( "<P>" );

        output.println( "<FONT size=\"+1\" ID=\"FrameHeadingFont\">" );
        output.println( "Modules</FONT>" );

        obj.reset();

        while ( obj.end() != true )
        {
            if ( obj.current().included() == false )
                if ( obj.current().kind() == IdlType.e_module )
                {
                    output.println( "<BR>" );
                    //output.println("<FONT ID=\"FrameItemFont\"><A HREF=\""+obj.current().name()+"/alldescriptions-frame.html"+"\" TARGET=\"moduleFrame\">"+obj.current().name()+"</A></FONT>");
                    output.println( "<FONT ID=\"FrameItemFont\"><A HREF=\"JavaScript:goTo('" + obj.current().name() + "/alldescriptions-frame.html', '" + obj.current().name() + "/" + obj.current().name() + ".html')\">" + obj.current().name() + "</A></FONT>" );
                }

            obj.next();
        }

        output.println( "</TABLE>" );

        output.close();
    }

    /**
     * This method adds a description entry for the HTML navigation
     */
    public void write_description_entry( String name, String link, java.io.PrintWriter output )
    {
        output.println( "<A HREF=\"" + link + "\" TARGET=\"descriptionFrame\"><I>" + name + "</I></A>" );
        output.println( "<BR>" );
    }

    /**
     * This method adds description entries for the HTML navigation
     */
    public void write_description_entries( IdlObject obj, IdlObject [] list, java.io.PrintWriter output, String name )
    {
        output.println( "<TABLE BORDER=\"0\" WIDTH=\"100%\">" );
        output.println( "<TR>" );
        output.println( "<TD NOWRAP><FONT size=\"+1\" ID=\"FrameHeadingFont\">" );
        output.println( name + "</FONT>&nbsp;" );

        output.println( "<FONT ID=\"FrameItemFont\">" );
        output.println( "<BR>" );

        for ( int i = 0; i < list.length; i++ )
            write_description_entry( list[ i ].name(), get_link( list[ i ], obj ), output );

        output.println( "</TR>" );

        output.println( "</TABLE>" );
    }

    /**
     * This method translates into a table all descriptions of the current object
     */
    public void write_description_content( IdlObject obj, java.io.PrintWriter output )
    {
        content c = get_sorted_content( obj, true );

        if ( c._sorted_module.length != 0 )
            write_description_entries( obj, c._sorted_module, output, "Module" );

        if ( c._sorted_interface.length != 0 )
            write_description_entries( obj, c._sorted_interface, output, "Interface" );

        if ( c._sorted_valuetype.length != 0 )
            write_description_entries( obj, c._sorted_valuetype, output, "ValueType" );

        if ( c._sorted_valuebox.length != 0 )
            write_description_entries( obj, c._sorted_valuebox, output, "ValueBox" );

        if ( c._sorted_exception.length != 0 )
            write_description_entries( obj, c._sorted_exception, output, "Exception" );

        if ( c._sorted_struct.length != 0 )
            write_description_entries( obj, c._sorted_struct, output, "Struct" );

        if ( c._sorted_union.length != 0 )
            write_description_entries( obj, c._sorted_union, output, "Union" );

        if ( c._sorted_enum.length != 0 )
            write_description_entries( obj, c._sorted_enum, output, "Enum" );

        if ( c._sorted_typedef.length != 0 )
            write_description_entries( obj, c._sorted_typedef, output, "TypeDef" );

        if ( c._sorted_const.length != 0 )
            write_description_entries( obj, c._sorted_const, output, "Const" );

        if ( c._sorted_native.length != 0 )
            write_description_entries( obj, c._sorted_native, output, "Native" );

        if ( c._sorted_operation.length != 0 )
            write_description_entries( obj, c._sorted_operation, output, "Operation" );

        if ( c._sorted_attribute.length != 0 )
            write_description_entries( obj, c._sorted_attribute, output, "Attribute" );

        if ( c._sorted_member.length != 0 )
            write_description_entries( obj, c._sorted_member, output, "Member" );

        if ( c._sorted_factory.length != 0 )
            write_description_entries( obj, c._sorted_factory, output, "Factory" );

    }

    /**
     * This method create all descriptions file for the HTML navigation
     */
    public void write_alldescriptions_file( String title, java.io.File writeInto, IdlObject obj )
    {
        java.io.PrintWriter output = create_file( "alldescriptions-frame", writeInto );

        output.println( "<HTML>" );
        output.println( "<HEAD>" );
        output.println( "<!-- org.openorb, IDL to HTML generator  -->" );
        output.println( "<META http-equiv=\"content-type\" content=\"text/html; charset="+org.openorb.compiler.doc.IdlDoc.codepage+"\">" );
        output.println( "<TITLE>" );
        output.println( "All Descriptions" );
        output.println( "</TITLE>" );
        output.println( "<LINK REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"stylesheet.css\" TITLE=\"Style\">" );
        output.println( "</HEAD>" );
        output.println( "<BODY BGCOLOR=\"white\">" );
        output.println( "<FONT size=\"+1\" ID=\"FrameHeadingFont\">" );
        output.println( "<A HREF=\"" + obj.name() + ".html\" TARGET=\"descriptionFrame\"><I>" + fullname( obj ) + "</I></A>" );
        output.println( "<BR>" );

        write_description_content( obj, output );

        output.close();
    }

    /**
     * This method translates into a table all descriptions of the current object
     */
    public void get_all_description_content( IdlObject obj, java.util.Vector list )
    {
        obj.reset();

        while ( obj.end() != true )
        {
            if ( obj.current().included() == false )
                switch ( obj.current().kind() )
                {

                case IdlType.e_enum :

                case IdlType.e_struct :

                case IdlType.e_union :

                case IdlType.e_typedef :

                case IdlType.e_value_box :

                case IdlType.e_exception :

                case IdlType.e_native :

                case IdlType.e_const :
                    list.addElement( obj.current() );
                    break;

                case IdlType.e_module :

                case IdlType.e_interface :

                case IdlType.e_value :
                    list.addElement( obj.current() );
                    get_all_description_content( obj.current(), list );
                    break;
                }

            obj.next();
        }
    }

    /**
     * This method sorts all descriptions.
     */
    public IdlObject [] sort_description_by_name( java.util.Vector list )
    {
        java.util.Vector sortedLabels = new java.util.Vector();

        for ( int j = 0; j < list.size(); j++ )
        {

            IdlObject obj = ( IdlObject ) list.elementAt( j );

            boolean inserted = false;

            for ( int i = 0; i < sortedLabels.size(); i++ )
            {
                if ( fullname( obj ).toLowerCase().compareTo( fullname( ( IdlObject ) sortedLabels.elementAt( i ) ).toLowerCase() ) < 0 )
                {
                    sortedLabels.insertElementAt( obj, i );
                    inserted = true;
                    break;
                }

            }

            if ( !inserted )
                sortedLabels.addElement( obj );
        }

        IdlObject [] ret = new IdlObject[ sortedLabels.size() ];

        for ( int i = 0; i < sortedLabels.size(); i++ )
            ret[ i ] = ( IdlObject ) sortedLabels.elementAt( i );

        return ret;
    }

    /**
     * This method sorts all descriptions.
     */
    public IdlObject [] sort_by_name( java.util.Vector list )
    {
        java.util.Vector sortedLabels = new java.util.Vector();

        for ( int j = 0; j < list.size(); j++ )
        {

            IdlObject obj = ( IdlObject ) list.elementAt( j );

            boolean inserted = false;

            for ( int i = 0; i < sortedLabels.size(); i++ )
            {
                if ( obj.name().toLowerCase().compareTo( ( ( IdlObject ) sortedLabels.elementAt( i ) ).name().toLowerCase() ) < 0 )
                {
                    sortedLabels.insertElementAt( obj, i );
                    inserted = true;
                    break;
                }

            }

            if ( !inserted )
                sortedLabels.addElement( obj );
        }

        IdlObject [] ret = new IdlObject[ sortedLabels.size() ];

        for ( int i = 0; i < sortedLabels.size(); i++ )
            ret[ i ] = ( IdlObject ) sortedLabels.elementAt( i );

        return ret;
    }

    /**
     * This method create all descriptions file for the HTML navigation
     */
    public void write_alldescriptions_file_index( String title, java.io.File writeInto, IdlObject obj )
    {
        java.io.PrintWriter output = create_file( "alldescriptions-frame", writeInto );

        output.println( "<HTML>" );
        output.println( "<HEAD>" );
        output.println( "<!-- org.openorb, IDL to HTML generator  -->" );
        output.println( "<META http-equiv=\"content-type\" content=\"text/html; charset="+org.openorb.compiler.doc.IdlDoc.codepage+"\">" );
        output.println( "<TITLE>" );
        output.println( "All Descriptions" );
        output.println( "</TITLE>" );
        output.println( "<LINK REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"stylesheet.css\" TITLE=\"Style\">" );
        output.println( "</HEAD>" );
        output.println( "<BODY BGCOLOR=\"white\">" );

        output.println( "<TABLE BORDER=\"0\" WIDTH=\"100%\">" );
        output.println( "<TR>" );
        output.println( "<TD NOWRAP><FONT size=\"+1\" ID=\"FrameHeadingFont\">" );
        output.println( "All Descriptions</FONT>&nbsp;" );

        output.println( "<FONT ID=\"FrameItemFont\">" );
        output.println( "<BR>" );

        java.util.Vector list = new java.util.Vector();
        get_all_description_content( obj, list );

        IdlObject [] sorted_list = sort_description_by_name( list );

        for ( int i = 0; i < sorted_list.length; i++ )
            write_description_entry( fullname( sorted_list[ i ] ), get_full_link( sorted_list[ i ] ), output );

        output.println( "</TR>" );

        output.println( "</TABLE>" );

        output.close();
    }

    /**
     * This method adds a title to a HTML page
     */
    public void write_title( String title, java.io.PrintWriter output )
    {
        output.println( "<HR>" );
        output.println( "<H2>" );
        output.println( title + "</H2>" );
        output.println( "<body>" );
    }

    /**
     * This method adds a title to a HTML page
     */
    public void write_title_center( String title, java.io.PrintWriter output )
    {
        output.println( "<HR>" );
        output.println( "<CENTER>" );
        output.println( "<H2>" );
        output.println( title + "</H2>" );
        output.println( "</CENTER>" );
        output.println( "<body>" );
    }

    /**
     * This method adds a page title to a HTML page
     */
    public void write_page_title( String title, java.io.PrintWriter output )
    {
        output.println( "<HTML>" );
        output.println( "<HEAD>" );
        output.println( "<!-- org.openorb, IDL to HTML generator  -->" );
        output.println( "<META http-equiv=\"content-type\" content=\"text/html; charset="+org.openorb.compiler.doc.IdlDoc.codepage+"\">" );
        output.println( "<TITLE>" );
        output.println( title );
        output.println( "</TITLE>" );
        output.print( "<LINK REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"" );

        if ( level > 0 )
        {
            for ( int i = 0; i < level; i++ )
                output.print( "../" );
        }

        output.println( "stylesheet.css\" TITLE=\"Style\">" );
        output.println( "</HEAD>" );
        output.println( "<BODY BGCOLOR=\"white\">" );
    }

    /**
     * This method adds a navigation bar to a HTML page
     */
    public void write_navigation_bar( java.io.PrintWriter output )
    {
        output.println( "<A NAME=\"navbar_top\"><!-- --></A>" );
        output.println( "<TABLE BORDER=\"0\" WIDTH=\"100%\" CELLPADDING=\"1\" CELLSPACING=\"0\">" );
        output.println( "<TR>" );
        output.println( "<TD COLSPAN=2 BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">" );
        output.println( "<A NAME=\"navbar_top_firstrow\"><!-- --></A>" );
        output.println( "<TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"3\">" );
        output.println( "<TR ALIGN=\"center\" VALIGN=\"top\">" );
        output.println( "<TD BGCOLOR=\"#FFFFFF\" ID=\"NavBarCell1Rev\"> &nbsp;<FONT ID=\"NavBarFont1Rev\"><B>Overview</B></FONT>&nbsp;</TD>" );
        output.println( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <FONT ID=\"NavBarFont1\">Module</FONT>&nbsp;</TD>" );
        output.println( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <FONT ID=\"NavBarFont1\">Interface</FONT>&nbsp;</TD>" );
        output.println( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <FONT ID=\"NavBarFont1\">Type</FONT>&nbsp;</TD>" );
        output.print( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <A HREF=\"" );

        if ( level > 0 )
        {
            for ( int i = 0; i < level; i++ )
                output.print( "../" );
        }

        output.println( "overview-tree.html\"><FONT ID=\"NavBarFont1\"><B>Tree</B></FONT></A>&nbsp;</TD>" );
        output.print( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <A HREF=\"" );

        if ( level > 0 )
        {
            for ( int i = 0; i < level; i++ )
                output.print( "../" );
        }

        output.println( "deprecated-list.html\"><FONT ID=\"NavBarFont1\"><B>Deprecated</B></FONT></A>&nbsp;</TD>" );
        output.print( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <A HREF=\"" );

        if ( level > 0 )
        {
            for ( int i = 0; i < level; i++ )
                output.print( "../" );
        }

        output.println( "overview-index.html\"><FONT ID=\"NavBarFont1\"><B>Index</B></FONT></A>&nbsp;</TD>" );
        output.println( "</TR>" );
        output.println( "</TABLE>" );
        output.println( "</TD>" );
        output.println( "<TD ALIGN=\"right\" VALIGN=\"top\" ROWSPAN=3><EM>" );
        output.println( "&nbsp;</EM>" );
        output.println( "</TD>" );
        output.println( "</TR>" );

        output.println( "<TR>" );
        output.println( "<TD BGCOLOR=\"white\" ID=\"NavBarCell2\"><FONT SIZE=\"-2\">" );
        output.println( "&nbsp;PREV&nbsp;" );
        output.println( "&nbsp;NEXT</FONT></TD>" );
        output.println( "<TD BGCOLOR=\"white\" ID=\"NavBarCell2\"><FONT SIZE=\"-2\">" );
        output.println( "<A HREF=\"index.html\" TARGET=\"_top\"><B>FRAMES</B></A>  &nbsp;" );
        output.println( "&nbsp;<A HREF=\"overview-summary.html\" TARGET=\"_top\"><B>NO FRAMES</B></A></FONT></TD>" );
        output.println( "</TR>" );
        output.println( "</TABLE>" );
    }


    /**
     * This method adds a navigation bar to a HTML page
     */
    public void write_navigation_bar_index( java.io.PrintWriter output, int index )
    {
        output.println( "<A NAME=\"navbar_top\"><!-- --></A>" );
        output.println( "<TABLE BORDER=\"0\" WIDTH=\"100%\" CELLPADDING=\"1\" CELLSPACING=\"0\">" );
        output.println( "<TR>" );
        output.println( "<TD COLSPAN=2 BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">" );
        output.println( "<A NAME=\"navbar_top_firstrow\"><!-- --></A>" );
        output.println( "<TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"3\">" );
        output.println( "<TR ALIGN=\"center\" VALIGN=\"top\">" );
        output.print( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <A HREF=\"" );

        if ( level > 0 )
        {
            for ( int i = 0; i < level; i++ )
                output.print( "../" );
        }

        output.println( "overview-summary.html\"><FONT ID=\"NavBarFont1\"><B>Overview</B></FONT></A>&nbsp;</TD>" );
        output.println( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <FONT ID=\"NavBarFont1\">Module</FONT>&nbsp;</TD>" );
        output.println( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <FONT ID=\"NavBarFont1\">Interface</FONT>&nbsp;</TD>" );
        output.println( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <FONT ID=\"NavBarFont1\">Type</FONT>&nbsp;</TD>" );

        if ( index == 1 )
            output.println( "<TD BGCOLOR=\"#FFFFFF\" ID=\"NavBarCell1Rev\"> &nbsp;<FONT ID=\"NavBarFont1Rev\"><B>Tree</B></FONT>&nbsp;</TD>" );
        else
        {
            output.print( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <A HREF=\"" );

            if ( level > 0 )
            {
                for ( int i = 0; i < level; i++ )
                    output.print( "../" );
            }

            output.println( "overview-tree.html\"><FONT ID=\"NavBarFont1\"><B>Tree</B></FONT></A>&nbsp;</TD>" );
        }

        if ( index == 2 )
            output.println( "<TD BGCOLOR=\"#FFFFFF\" ID=\"NavBarCell1Rev\"> &nbsp;<FONT ID=\"NavBarFont1Rev\"><B>Deprecated</B></FONT>&nbsp;</TD>" );
        else
        {
            output.print( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <A HREF=\"" );

            if ( level > 0 )
            {
                for ( int i = 0; i < level; i++ )
                    output.print( "../" );
            }

            output.println( "deprecated-list.html\"><FONT ID=\"NavBarFont1\"><B>Deprecated</B></FONT></A>&nbsp;</TD>" );
        }

        if ( index == 3 )
            output.println( "<TD BGCOLOR=\"#FFFFFF\" ID=\"NavBarCell1Rev\"> &nbsp;<FONT ID=\"NavBarFont1Rev\"><B>Index</B></FONT>&nbsp;</TD>" );
        else
        {
            output.print( "<TD BGCOLOR=\"#EEEEFF\" ID=\"NavBarCell1\">    <A HREF=\"" );

            if ( level > 0 )
            {
                for ( int i = 0; i < level; i++ )
                    output.print( "../" );
            }

            output.println( "overview-index.html\"><FONT ID=\"NavBarFont1\"><B>Index</B></FONT></A>&nbsp;</TD>" );
        }

        output.println( "</TR>" );
        output.println( "</TABLE>" );
        output.println( "</TD>" );
        output.println( "<TD ALIGN=\"right\" VALIGN=\"top\" ROWSPAN=3><EM>" );
        output.println( "&nbsp;</EM>" );
        output.println( "</TD>" );
        output.println( "</TR>" );

        output.println( "<TR>" );
        output.println( "<TD BGCOLOR=\"white\" ID=\"NavBarCell2\"><FONT SIZE=\"-2\">" );
        output.println( "&nbsp;PREV&nbsp;" );
        output.println( "&nbsp;NEXT</FONT></TD>" );
        output.println( "<TD BGCOLOR=\"white\" ID=\"NavBarCell2\"><FONT SIZE=\"-2\">" );
        output.println( "<A HREF=\"index.html\" TARGET=\"_top\"><B>FRAMES</B></A>  &nbsp;" );
        output.println( "&nbsp;<A HREF=\"overview-summary.html\" TARGET=\"_top\"><B>NO FRAMES</B></A></FONT></TD>" );
        output.println( "</TR>" );
        output.println( "</TABLE>" );
    }

    /**
     * This method creates a table
     */
    public void write_begin_table( String title, java.io.PrintWriter output )
    {
        output.println( "<TABLE BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">" );
        output.println( "<TR BGCOLOR=\"#CCCCFF\" ID=\"TableHeadingColor\">" );
        output.println( "<TD COLSPAN=2><FONT SIZE=\"+2\">" );
        output.println( "<B>" + title + "</B></FONT></TD>" );
        output.println( "</TR>" );
    }

    /**
     * This method creates a entry into a table
     */
    public void write_table_entry( String name, String link, String description, java.io.PrintWriter output )
    {
        output.println( "<TR BGCOLOR=\"white\" ID=\"TableRowColor\">" );
        output.println( "<TD WIDTH=\"20%\"><B><A HREF=\"" + link + "\">" + name + "</A></B></TD>" );
        output.println( "<TD>" + description );
        output.println( "</TD>" );
        output.println( "</TR>" );
    }

    /**
     * This method create an summary file for the HTML navigation
     */
    public void write_summary_file( String title, java.io.File writeInto )
    {
        java.io.PrintWriter output = create_file( "overview-summary", writeInto );

        write_page_title( title + " : overview", output );

        write_navigation_bar( output );

        write_title_center( title, output );

        translateContentTable( _root, output, 3 );

        output.println( "</TABLE>" );

        output.close();
    }

    /**
     * This method create a style sheet
     */
    public void write_style_sheet( java.io.File writeInto )
    {
        java.io.PrintWriter output = create_file( "stylesheet.css", writeInto );

        output.println( "/***************************************************************************" );
        output.println( " org.openorb, IDL to HTML Style Sheet" );
        output.println( " ***************************************************************************/" );
        output.println( "" );

        output.println( "/* Page background color */" );
        output.println( "body { background-color: #FFFFFF }" );
        output.println( "" );

        output.println( "/* Table colors */" );
        output.println( "#TableHeadingColor     { background: #CCCCFF } /* Dark mauve */" );
        output.println( "#TableSubHeadingColor  { background: #EEEEFF } /* Light mauve */" );
        output.println( "#TableRowColor         { background: #FFFFFF } /* White */" );
        output.println( "" );

        output.println( "/* Font used in left-hand frame lists */" );
        output.println( "#FrameTitleFont   { font-size: normal; font-family: normal }" );
        output.println( "#FrameHeadingFont { font-size: normal; font-family: normal }" );
        output.println( "#FrameItemFont    { font-size: normal; font-family: normal }" );
        output.println( "" );

        output.println( "/* Navigation bar fonts and colors */" );
        output.println( "#NavBarCell1    { background-color:#EEEEFF;}/* Light mauve */" );
        output.println( "#NavBarCell1Rev { background-color:#00008B;}/* Dark Blue */" );
        output.println( "#NavBarFont1    { font-family: Arial, Helvetica, sans-serif; color:#000000;}" );
        output.println( "#NavBarFont1Rev { font-family: Arial, Helvetica, sans-serif; color:#FFFFFF;}" );
        output.println( "" );

        output.println( "#NavBarCell2    { font-family: Arial, Helvetica, sans-serif; background-color:#FFFFFF;}" );
        output.println( "#NavBarCell3    { font-family: Arial, Helvetica, sans-serif; background-color:#FFFFFF;}" );
        output.close();
    }

    /**
     * This method returns all the content
     */
    public void get_all_content( IdlObject obj, java.util.Vector list, boolean limit )
    {
        obj.reset();

        while ( obj.end() != true )
        {
            if ( obj.current().included() == false )
                switch ( obj.current().kind() )
                {

                case IdlType.e_enum :

                case IdlType.e_struct :

                case IdlType.e_union :

                case IdlType.e_typedef :

                case IdlType.e_value_box :

                case IdlType.e_exception :

                case IdlType.e_native :

                case IdlType.e_const :

                case IdlType.e_operation :

                case IdlType.e_attribute :

                case IdlType.e_state_member :

                case IdlType.e_factory :
                    list.addElement( obj.current() );
                    break;

                case IdlType.e_module :

                case IdlType.e_interface :

                case IdlType.e_value :
                    list.addElement( obj.current() );

                    if ( !limit )
                        get_all_content( obj.current(), list, limit );

                    break;
                }

            obj.next();
        }
    }

    /**
     * This method writes an index file
     */
    public void write_index( java.io.File writeInto )
    {
        java.io.PrintWriter output = create_file( "overview-index", writeInto );
        IdlComment comment = null;

        output.println( "<HTML>" );
        output.println( "<HEAD>" );
        output.println( "<!-- org.openorb, IDL to HTML generator  -->" );
        output.println( "<META http-equiv=\"content-type\" content=\"text/html; charset="+org.openorb.compiler.doc.IdlDoc.codepage+"\">" );
        output.println( "<TITLE>" );
        output.println( "Index" );
        output.println( "</TITLE>" );
        output.print( "<LINK REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"" );

        if ( level > 0 )
        {
            for ( int i = 0; i < level; i++ )
                output.print( "../" );
        }

        output.println( "stylesheet.css\" TITLE=\"Style\">" );
        output.println( "</HEAD>" );

        write_navigation_bar_index( output, 3 );

        java.util.Vector content = new java.util.Vector();

        get_all_content( _root, content, false );

        IdlObject [] list = sort_by_name( content );

        char letter = list[ 0 ].name().toUpperCase().charAt( 0 );
        boolean first = true;

        output.println( "<DL>" );

        for ( int i = 0; i < list.length; i++ )
        {
            if ( letter != list[ i ].name().toUpperCase().charAt( 0 ) )
            {
                letter = list[ i ].name().toUpperCase().charAt( 0 );
                first = true;
            }

            if ( first )
            {
                output.println( "<H1>" + letter + "</H1>" );
                first = false;
            }

            output.println( "<DT>" );
            output.print( "<A HREF=\"" + get_link( list[ i ], _root ) + "\" TARGET=\"descriptionFrame\">" + list[ i ].name() + "</A>" );

            output.print( " - " );

            switch ( list[ i ].kind() )
            {

            case IdlType.e_interface :
                output.print( "Interface " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_module :
                output.print( "Module " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_value :
                output.print( "ValueType " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_value_box :
                output.print( "ValueBox " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_exception :
                output.print( "Exception " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_struct :
                output.print( "Struct " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_union :
                output.print( "Union " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_enum :
                output.print( "Enum " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_typedef :
                output.print( "TypeDef " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_const :
                output.print( "Const " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_native :
                output.print( "Native " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_operation :
                output.print( "Operation " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_attribute :
                output.print( "Attribute " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_state_member :
                output.print( "Member " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_factory :
                output.print( "Factory " + fullname_idl( list[ i ] ) );
                break;
            }

            comment = list[ i ].getComment();

            if ( comment != null )
            {
                output.print( " - " );
                output.println( get_summary_description( list[ i ] ) );
            }

        }

        output.println( "</DL>" );

        output.println( "</BODY>" );
        output.println( "</HTML>" );

        output.close();
    }

    /**
     * This method writes a tree branch
     */
    public void write_branch( IdlObject obj, java.io.PrintWriter output )
    {
        java.util.Vector v = new java.util.Vector();

        get_all_content( obj, v, true );

        IdlObject [] list = sort_by_name( v );

        output.println( "<UL>" );

        for ( int i = 0; i < list.length; i++ )
        {
            output.print( "<LI>" );

            output.print( "<A HREF=\"" + get_full_link( list[ i ] ) + "\" TARGET=\"descriptionFrame\">" + list[ i ].name() + "</A> - " );

            switch ( list[ i ].kind() )
            {

            case IdlType.e_interface :
                output.print( "Interface " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_module :
                output.print( "Module " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_value :
                output.print( "ValueType " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_value_box :
                output.print( "ValueBox " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_exception :
                output.print( "Exception " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_struct :
                output.print( "Struct " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_union :
                output.print( "Union " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_enum :
                output.print( "Enum " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_typedef :
                output.print( "TypeDef " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_const :
                output.print( "Const " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_native :
                output.print( "Native " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_operation :
                output.print( "Operation " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_attribute :
                output.print( "Attribute " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_state_member :
                output.print( "Member " + fullname_idl( list[ i ] ) );
                break;

            case IdlType.e_factory :
                output.print( "Factory " + fullname_idl( list[ i ] ) );
                break;
            }

            output.println( "" );

            switch ( list[ i ].kind() )
            {

            case IdlType.e_interface :

            case IdlType.e_value :

            case IdlType.e_module :
                write_branch( list[ i ], output );
                break;
            }
        }

        output.println( "</UL>" );
    }

    /**
     * This method writes an overview tree
     */
    public void write_tree( java.io.File writeInto )
    {
        java.io.PrintWriter output = create_file( "overview-tree", writeInto );

        output.println( "<HTML>" );
        output.println( "<HEAD>" );
        output.println( "<!-- org.openorb, IDL to HTML generator  -->" );
        output.println( "<META http-equiv=\"content-type\" content=\"text/html; charset="+org.openorb.compiler.doc.IdlDoc.codepage+"\">" );
        output.println( "<TITLE>" );
        output.println( "Index" );
        output.println( "</TITLE>" );
        output.print( "<LINK REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"" );

        if ( level > 0 )
        {
            for ( int i = 0; i < level; i++ )
                output.print( "../" );
        }

        output.println( "stylesheet.css\" TITLE=\"Style\">" );
        output.println( "</HEAD>" );

        write_navigation_bar_index( output, 1 );

        write_title_center( "Hierarchy for all IDL descriptions", output );

        write_branch( _root, output );

        output.println( "</BODY>" );
        output.println( "</HTML>" );
        output.close();
    }

    /**
     * This method writes a deprecated section
     */
    public void write_deprecated_section( IdlObject [] obj, String title, java.io.PrintWriter output )
    {
        if ( is_any_deprecated( obj ) )
        {
            write_begin_table( title, output );

            for ( int i = 0; i < obj.length; i++ )
                if ( is_deprecated( obj[ i ] ) )
                {
                    write_table_entry( obj[ i ].name(), get_full_link( obj[ i ] ) , get_deprecation( obj[ i ] ), output );
                }

            output.println( "</TABLE><BR>" );
        }
    }

    /**
     * This method writes an deprecated list
     */
    public void write_deprecated( java.io.File writeInto )
    {
        java.io.PrintWriter output = create_file( "deprecated-list", writeInto );

        output.println( "<HTML>" );
        output.println( "<HEAD>" );
        output.println( "<!-- org.openorb, IDL to HTML generator  -->" );
        output.println( "<META http-equiv=\"content-type\" content=\"text/html; charset="+org.openorb.compiler.doc.IdlDoc.codepage+"\">" );
        output.println( "<TITLE>" );
        output.println( "Index" );
        output.println( "</TITLE>" );
        output.print( "<LINK REL =\"stylesheet\" TYPE=\"text/css\" HREF=\"" );

        if ( level > 0 )
        {
            for ( int i = 0; i < level; i++ )
                output.print( "../" );
        }

        output.println( "stylesheet.css\" TITLE=\"Style\">" );
        output.println( "</HEAD>" );

        write_navigation_bar_index( output, 2 );

        write_title_center( "Deprecated IDL descriptions", output );

        content c = get_sorted_content( _root, false );

        write_deprecated_section( c._sorted_module, "Deprecated Modules", output );
        write_deprecated_section( c._sorted_interface, "Deprecated Interfaces", output );
        write_deprecated_section( c._sorted_valuetype, "Deprecated ValueTypes", output );
        write_deprecated_section( c._sorted_valuebox, "Deprecated ValueBox", output );
        write_deprecated_section( c._sorted_exception, "Deprecated Exceptions", output );
        write_deprecated_section( c._sorted_struct, "Deprecated Structs", output );
        write_deprecated_section( c._sorted_union, "Deprecated Unions", output );
        write_deprecated_section( c._sorted_enum, "Deprecated Enums", output );
        write_deprecated_section( c._sorted_typedef, "Deprecated TypeDefs", output );
        write_deprecated_section( c._sorted_const, "Deprecated Consts", output );
        write_deprecated_section( c._sorted_native, "Deprecated Natives", output );
        write_deprecated_section( c._sorted_operation, "Deprecated Operations", output );
        write_deprecated_section( c._sorted_attribute, "Deprecated Attributes", output );
        write_deprecated_section( c._sorted_member, "Deprecated Members", output );
        write_deprecated_section( c._sorted_factory, "Deprecated Factories", output );

        output.println( "</BODY>" );
        output.println( "</HTML>" );
        output.close();
    }

    /**
     * This method adapts a string to a HTML string
     */
    public String adapt_string( String src )
    {
        // Any conversion done here is plain wrong as long as
        // it is not known which codepage the input file is based upon !!
        return src;
    }

    /**
     * This method returns an object description
     */
    public String get_link( IdlObject obj, IdlObject ref )
    {
        IdlObject o1, o2;
        String ret = "", begin = "", sub = "";

        int lev = 0;

        o1 = obj;
        o2 = ref;

        String path_o1 = get_path( o1 );
        String path_o2 = get_path( o2 );

        int i = 0;
        int max = 0;

        if ( path_o1.length() > path_o2.length() )
            max = path_o2.length();
        else
            max = path_o1.length();

        while ( i < max )
        {
            if ( path_o2.charAt( i ) != path_o1.charAt( i ) )
                break;

            i++;
        }

        if ( i != 0 )
            i--;

        int j = i;

        while ( j > 0 )
        {
            if ( path_o2.charAt( j ) == '/' )
            {
                j++;
                break;
            }

            j--;
        }

        //sub = path_o2.substring( i );
        sub = path_o2.substring( j );

        if ( sub.startsWith( "/" ) )
            sub = sub.substring( 1 );

        if ( !sub.equals( "" ) )
            lev++;

        j = i;

        while ( j > 0 )
        {
            if ( path_o1.charAt( j ) == '/' )
            {
                j++;
                break;
            }

            j--;
        }

        //  begin = path_o1.substring( i );
        begin = path_o1.substring( j );

        for ( i = 0; i < sub.length(); i++ )
            if ( sub.charAt( i ) == '/' )
                lev++;

        for ( j = 0; j < lev; j++ )
            ret = ret + "../";

        if ( !begin.equals( "" ) )
            ret = ret + begin;

        if ( !ret.equals( "" ) )
            if ( !ret.endsWith( "/" ) )
                ret = ret + "/";

        switch ( o1.kind() )
        {

        case IdlType.e_module :

        case IdlType.e_interface :

        case IdlType.e_forward_interface :

        case IdlType.e_value :

        case IdlType.e_forward_value :
            ret = ret + obj.name() + ".html";
            break;

        default :

            if ( o1.upper() == _root )
                ret = ret + "overview-summary";
            else
                ret = ret + obj.upper().name();

            ret = ret + ".html#" + obj.name();

            break;
        }


        if ( ret.startsWith( "/" ) )
            ret = "." + ret;

        return ret;
    }

    /**
     * This method returns an object description
     */
    public String get_full_link( IdlObject obj )
    {
        if ( obj.kind() == IdlType.e_interface )
            return fullname_link( obj ) + "/" + obj.name() + ".html";

        if ( obj.kind() == IdlType.e_module )
            return fullname_link( obj ) + "/" + obj.name() + ".html";

        if ( obj.kind() == IdlType.e_value )
            return fullname_link( obj ) + "/" + obj.name() + ".html";

        if ( obj.upper() == _root )
            return "overview-summary.html#" + obj.name();

        return fullname_link( obj.upper() ) + "/" + obj.upper().name() + ".html#" + obj.name();
    }


    /**
     * This method returns an object description
     */
    public String get_description( IdlObject obj )
    {
        IdlComment comment = obj.getComment();

        if ( comment != null )
        {
            if ( comment.get_description() != null )
                return adapt_string( comment.get_description() );
        }

        return "";
    }

    /**
     * This method tests if an object is deprecated
     */
    public boolean is_deprecated( IdlObject obj )
    {
        IdlComment comment = obj.getComment();

        if ( comment != null )
        {
            IdlCommentSection [] sections = comment.get_sections();

            for ( int i = 0; i < sections.length; i++ )
                if ( sections[ i ].kind().value() == IdlCommentField._deprecated_field )
                    return true;
        }

        return false;
    }

    /**
     * This method tests if an object list contains a deprecated object
     */
    public boolean is_any_deprecated( IdlObject [] obj )
    {
        for ( int i = 0; i < obj.length; i++ )
            if ( is_deprecated( obj[ i ] ) )
                return true;

        return false;
    }

    /**
     * This method returns a deprecation text
     */
    public String get_deprecation( IdlObject obj )
    {
        IdlComment comment = obj.getComment();

        if ( comment != null )
        {
            IdlCommentSection [] sections = comment.get_sections();

            for ( int i = 0; i < sections.length; i++ )
                if ( sections[ i ].kind().value() == IdlCommentField._deprecated_field )
                {
                    return sections[ i ].get_description();
                }
        }

        return "";
    }

    /**
     * This method returns a summary for the object description
     */
    public String get_summary_description( IdlObject obj )
    {
        IdlComment comment = obj.getComment();
        String description = null;

        if ( comment != null )
        {
            description = comment.get_description();

            if ( description != null )
            {
                description = adapt_string( description );

                if ( description.indexOf( "." ) != -1 )
                    return description.substring( 0, description.indexOf( "." ) + 1 );

                if ( description.indexOf( "\n\n" ) != -1 )
                    return description.substring( 0, description.indexOf( "\n\n" ) + 1 );

                return description;
            }
        }

        return "";
    }

    /**
     * This method returns a comment section
     */
    public IdlCommentSection [] get_sections( IdlCommentSection [] src, int section_type )
    {
        java.util.Vector list = new java.util.Vector();

        for ( int i = 0; i < src.length; i++ )
        {
            if ( src[ i ].kind().value() == section_type )
                list.addElement( src[ i ] );
        }

        IdlCommentSection [] ret = new IdlCommentSection[ list.size() ];

        for ( int i = 0; i < list.size(); i++ )
            ret[ i ] = ( IdlCommentSection ) list.elementAt( i );

        return ret;
    }

    /**
     * This method writes a comment section
     */
    public void write_section( IdlCommentSection [] sections, int section_type, String section_title, java.io.PrintWriter output, boolean highlight_first )
    {
        String desc = "";
        String first = "";

        IdlCommentSection [] section = get_sections( sections, section_type );

        if ( section.length != 0 )
        {
            output.println( "<H4>" + section_title + "</H4><UL>" );

            for ( int i = 0; i < section.length; i++ )
            {
                desc = adapt_string( section[ i ].get_description().trim() );

                if ( highlight_first )
                {
                    int idx = desc.indexOf( " " );

                    int idx2 = desc.indexOf( "\t" );

                    if ( ( idx == -1 ) && ( idx2 != -1 ) )
                        idx = idx2;
                    else
                        if ( idx2 != -1 )
                            if ( idx2 < idx )
                                idx = idx2;

                    if ( idx != -1 )
                    {
                        first = desc.substring( 0, idx );
                        desc = desc.substring( idx + 1 );

                        output.print( "<B><I> " + first + " </I></B> - " );
                    }
                }

                output.println( desc );
                output.println( "<BR>" );
            }

            output.println( "</UL><BR>" );
        }
    }

    /**
     * This method writes an object description
     */
    public void write_description( IdlObject obj, java.io.PrintWriter output )
    {
        IdlComment comment = obj.getComment();

        output.println( get_description( obj ) );
        output.println( "<BR>" );

        if ( comment != null )
        {
            IdlCommentSection [] sections = comment.get_sections();

            write_section( sections, IdlCommentField._param_field, "Parameter", output, true );

            write_section( sections, IdlCommentField._return_field, "Return", output, false );

            write_section( sections, IdlCommentField._exception_field, "Exception", output, true );

            write_section( sections, IdlCommentField._see_field, "See", output, true );

            write_section( sections, IdlCommentField._deprecated_field, "Deprecated", output, false );
        }
    }

    /**
     * This method translates into a table all sub types summary of the current object
     */
    public void translateSubTypeSummary( IdlObject ref, IdlObject [] list, java.io.PrintWriter output, String name )
    {
        String desc;
        write_begin_table( name, output );

        for ( int i = 0; i < list.length; i++ )
        {
            desc = get_summary_description( list[ i ] );

            if ( is_deprecated( list[ i ] ) )
                desc = "<B>deprecated</B> - " + desc;

            write_table_entry( list[ i ].name(), get_link( list[ i ], ref ), desc, output );
        }

        output.println( "</TABLE>" );
        output.println( "<BR>" );
    }

    /**
     * This method translates into a table all sub types details of the current object
     */
    public void translateSubTypeDetails( IdlObject ref, IdlObject [] list, java.io.PrintWriter output, String name )
    {
        write_begin_table( name, output );
        output.println( "</TABLE>" );

        for ( int i = 0; i < list.length; i++ )
        {
            switch ( list[ i ].kind() )
            {

            case IdlType.e_value_box :
                translateValueBox( list[ i ], output );
                break;

            case IdlType.e_exception :
                translateException( list[ i ], output );
                break;

            case IdlType.e_struct :
                translateStruct( list[ i ], output );
                break;

            case IdlType.e_union :
                translateUnion( list[ i ], output );
                break;

            case IdlType.e_enum :
                translateEnum( list[ i ], output );
                break;

            case IdlType.e_typedef :
                translateTypedef( list[ i ], output );
                break;

            case IdlType.e_const :
                translateConstant( list[ i ], output );
                break;

            case IdlType.e_native :
                translateNative( list[ i ], output );
                break;

            case IdlType.e_operation :
                translateOperation( list[ i ], output );
                break;

            case IdlType.e_attribute :
                translateAttribute( list[ i ], output );
                break;

            case IdlType.e_state_member :
                translateStateMember( list[ i ], output );
                break;

            case IdlType.e_factory :
                translateFactory( list[ i ], output );
                break;
            }
        }


        output.println( "<BR>" );
    }

    /**
     * This method translates into a table al sub types of the current object
     *
     * Summary : type = 1
     * Details : type = 2
     * Summary and details : type == 3
     */
    public void translateContentTable( IdlObject obj, java.io.PrintWriter output, int type )
    {
        content c = get_sorted_content( obj, true );

        // --
        // Summary
        // --

        if ( ( type == 1 ) || ( type == 3 ) )
        {
            if ( c._sorted_module.length != 0 )
                translateSubTypeSummary( obj, c._sorted_module, output, "Module Summary" );

            if ( c._sorted_interface.length != 0 )
                translateSubTypeSummary( obj, c._sorted_interface, output, "Interface Summary" );

            if ( c._sorted_valuetype.length != 0 )
                translateSubTypeSummary( obj, c._sorted_valuetype, output, "ValueType Summary" );

            if ( c._sorted_valuebox.length != 0 )
                translateSubTypeSummary( obj, c._sorted_valuebox, output, "ValueBox Summary" );

            if ( c._sorted_exception.length != 0 )
                translateSubTypeSummary( obj, c._sorted_exception, output, "Exception Summary" );

            if ( c._sorted_struct.length != 0 )
                translateSubTypeSummary( obj, c._sorted_struct, output, "Struct Summary" );

            if ( c._sorted_union.length != 0 )
                translateSubTypeSummary( obj, c._sorted_union, output, "Union Summary" );

            if ( c._sorted_enum.length != 0 )
                translateSubTypeSummary( obj, c._sorted_enum, output, "Enum Summary" );

            if ( c._sorted_typedef.length != 0 )
                translateSubTypeSummary( obj, c._sorted_typedef, output, "TypeDef Summary" );

            if ( c._sorted_const.length != 0 )
                translateSubTypeSummary( obj, c._sorted_const, output, "Const Summary" );

            if ( c._sorted_native.length != 0 )
                translateSubTypeSummary( obj, c._sorted_native, output, "Native Summary" );

            if ( c._sorted_operation.length != 0 )
                translateSubTypeSummary( obj, c._sorted_operation, output, "Operation Summary" );

            if ( c._sorted_attribute.length != 0 )
                translateSubTypeSummary( obj, c._sorted_attribute, output, "Attribute Summary" );

            if ( c._sorted_member.length != 0 )
                translateSubTypeSummary( obj, c._sorted_member, output, "Member Summary" );

            if ( c._sorted_factory.length != 0 )
                translateSubTypeSummary( obj, c._sorted_factory, output, "Factory Summary" );
        }

        // --
        // Details
        // --

        if ( ( type == 2 ) || ( type == 3 ) )
        {
            if ( c._sorted_valuebox.length != 0 )
                translateSubTypeDetails( obj, c._sorted_valuebox, output, "ValueBox Details" );

            if ( c._sorted_exception.length != 0 )
                translateSubTypeDetails( obj, c._sorted_exception, output, "Exception Details" );

            if ( c._sorted_struct.length != 0 )
                translateSubTypeDetails( obj, c._sorted_struct, output, "Struct Details" );

            if ( c._sorted_union.length != 0 )
                translateSubTypeDetails( obj, c._sorted_union, output, "Union Details" );

            if ( c._sorted_enum.length != 0 )
                translateSubTypeDetails( obj, c._sorted_enum, output, "Enum Details" );

            if ( c._sorted_typedef.length != 0 )
                translateSubTypeDetails( obj, c._sorted_typedef, output, "TypeDef Details" );

            if ( c._sorted_const.length != 0 )
                translateSubTypeDetails( obj, c._sorted_const, output, "Const Details" );

            if ( c._sorted_native.length != 0 )
                translateSubTypeDetails( obj, c._sorted_native, output, "Native Details" );

            if ( c._sorted_operation.length != 0 )
                translateSubTypeDetails( obj, c._sorted_operation, output, "Operation Details" );

            if ( c._sorted_attribute.length != 0 )
                translateSubTypeDetails( obj, c._sorted_attribute, output, "Attribute Details" );

            if ( c._sorted_member.length != 0 )
                translateSubTypeDetails( obj, c._sorted_member, output, "Member Details" );

            if ( c._sorted_factory.length != 0 )
                translateSubTypeDetails( obj, c._sorted_factory, output, "Factory Details" );
        }
    }

    /**
     * This method creates a file and returns its write access.
     */
    public java.io.PrintWriter create_file( String file_name, java.io.File writeInto )
    {
        String path = null;

        if ( file_name.endsWith( ".css" ) )
            path = new String( writeInto.getPath() + java.io.File.separator + file_name );
        else
            path = new String( writeInto.getPath() + java.io.File.separator + file_name + ".html" );

        java.io.File file = new java.io.File( path );

        java.io.PrintWriter printout = null;

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
     * This method returns a full name for a IDL item.
     */
    public String fullname ( IdlObject obj )
    {
        java.util.Vector v = new java.util.Vector();
        IdlObject obj2 = obj;
        String name = new String( "" );
        String s;
        boolean first = false;

        while ( obj2 != null )
        {
            v.addElement( obj2.name() );

            if ( obj2.upper() != null )
                if ( obj2.upper().kind() == IdlType.e_root )
                    break;

            obj2 = obj2.upper();

            first = true;
        }

        if ( org.openorb.compiler.doc.IdlDoc.usePrefix )
            if ( obj.getPrefix() != null )
            {
                if ( !name.equals( "" ) )
                    name = name + ".";

                name = name + inversedPrefix( obj.getPrefix() );
            }

        for ( int i = v.size() - 1; i >= 0; i-- )
        {
            s = ( String ) v.elementAt( i );

            if ( s != null )
            {
                if ( !name.equals( "" ) )
                    name = name + ".";

                name = name + s;
            }
        }

        return name;
    }

    /**
     * This method returns a full name for a IDL item in an IDL notation
     */
    public String fullname_idl ( IdlObject obj )
    {
        java.util.Vector v = new java.util.Vector();
        IdlObject obj2 = obj;
        String name = new String( "" );
        String s;
        boolean first = false;

        while ( obj2 != null )
        {
            v.addElement( obj2.name() );

            if ( obj2.upper() != null )
                if ( obj2.upper().kind() == IdlType.e_root )
                    break;

            obj2 = obj2.upper();

            first = true;
        }

        for ( int i = v.size() - 1; i >= 0; i-- )
        {
            s = ( String ) v.elementAt( i );

            if ( s != null )
            {
                if ( !name.equals( "" ) )
                    name = name + "::";

                name = name + s;
            }
        }

        return name;
    }

    /**
     * This method returns a full name for a IDL item for a HTML link
     */
    public String fullname_link ( IdlObject obj )
    {
        java.util.Vector v = new java.util.Vector();
        IdlObject obj2 = obj;
        String name = new String( "" );
        String s;
        boolean first = false;

        while ( obj2 != null )
        {
            switch ( obj2.kind() )
            {

            case IdlType.e_param :
                break;

            default :
                v.addElement( obj2.name() );
                break;
            }

            if ( obj2.upper() != null )
                if ( obj2.upper().kind() == IdlType.e_root )
                    break;

            obj2 = obj2.upper();

            first = true;
        }

        for ( int i = v.size() - 1; i >= 0; i-- )
        {
            s = ( String ) v.elementAt( i );

            if ( s != null )
            {
                if ( !name.equals( "" ) )
                    name = name + "/";

                name = name + s;
            }
        }

        return name;
    }

    /**
     * This method returns a full name for a IDL item for a HTML link
     */
    public String get_path ( IdlObject obj )
    {
        java.util.Vector v = new java.util.Vector();
        IdlObject obj2 = obj;
        String name = new String( "" );
        String s;

        while ( obj2 != null )
        {
            switch ( obj2.kind() )
            {

            case IdlType.e_module :

            case IdlType.e_interface :

            case IdlType.e_forward_interface :

            case IdlType.e_value :

            case IdlType.e_forward_value :
                v.addElement( obj2.name() );
                break;
            }

            if ( obj2.upper() != null )
                if ( obj2.upper().kind() == IdlType.e_root )
                    break;

            obj2 = obj2.upper();
        }

        for ( int i = v.size() - 1; i >= 0; i-- )
        {
            s = ( String ) v.elementAt( i );

            if ( s != null )
            {
                if ( !name.equals( "" ) )
                    name = name + "/";

                name = name + s;
            }
        }

        return name;
    }

    /**
     * This method sorts the content of an IDL type
     */
    public void sort_type_content( IdlObject obj, content c, boolean limit )
    {
        if ( c == null )
            c = new content();

        obj.reset();

        while ( obj.end() != true )
        {
            if ( obj.current().included() == false )
                switch ( obj.current().kind() )
                {

                case IdlType.e_module :
                    c._module.addElement( obj.current() );

                    if ( !limit )
                        sort_type_content( obj.current(), c, limit );

                    break;

                case IdlType.e_interface :
                    c._interface.addElement( obj.current() );

                    if ( !limit )
                        sort_type_content( obj.current(), c, limit );

                    break;

                case IdlType.e_value :
                    c._valuetype.addElement( obj.current() );

                    if ( !limit )
                        sort_type_content( obj.current(), c, limit );

                    break;

                case IdlType.e_value_box :
                    c._valuebox.addElement( obj.current() );

                    break;

                case IdlType.e_exception :
                    c._exception.addElement( obj.current() );

                    break;

                case IdlType.e_struct :
                    c._struct.addElement( obj.current() );

                    sort_type_content( obj.current(), c, limit );

                    break;

                case IdlType.e_union :
                    c._union.addElement( obj.current() );

                    sort_type_content( obj.current(), c, limit );

                    break;

                case IdlType.e_enum :
                    c._enum.addElement( obj.current() );

                    break;

                case IdlType.e_typedef :
                    c._typedef.addElement( obj.current() );

                    sort_type_content( obj.current(), c, limit );

                    break;

                case IdlType.e_const :
                    c._const.addElement( obj.current() );

                    break;

                case IdlType.e_native :
                    c._native.addElement( obj.current() );

                    break;

                case IdlType.e_operation :
                    c._operation.addElement( obj.current() );

                    break;

                case IdlType.e_attribute :
                    c._attribute.addElement( obj.current() );

                    sort_type_content( obj.current(), c, limit );

                    break;

                case IdlType.e_state_member :
                    c._member.addElement( obj.current() );

                    sort_type_content( obj.current(), c, limit );

                    break;

                case IdlType.e_factory :
                    c._factory.addElement( obj.current() );

                    break;
                }

            obj.next();
        }
    }

    /**
     * This method return all an IDL tpye content ( sorted )
     */
    public content get_sorted_content( IdlObject obj, boolean limit )
    {
        content ret = new content();

        sort_type_content( obj, ret, limit );

        // Sort module
        if ( ret._module.size() != 0 )
            ret._sorted_module = sort_description_by_name( ret._module );
        else
            ret._sorted_module = new IdlObject[ 0 ];

        // Sort interface
        if ( ret._interface.size() != 0 )
            ret._sorted_interface = sort_description_by_name( ret._interface );
        else
            ret._sorted_interface = new IdlObject[ 0 ];

        // Sort valuetype
        if ( ret._valuetype.size() != 0 )
            ret._sorted_valuetype = sort_description_by_name( ret._valuetype );
        else
            ret._sorted_valuetype = new IdlObject[ 0 ];

        // Sort valuebox
        if ( ret._valuebox.size() != 0 )
            ret._sorted_valuebox = sort_description_by_name( ret._valuebox );
        else
            ret._sorted_valuebox = new IdlObject[ 0 ];

        // Sort exception
        if ( ret._exception.size() != 0 )
            ret._sorted_exception = sort_description_by_name( ret._exception );
        else
            ret._sorted_exception = new IdlObject[ 0 ];

        // Sort struct
        if ( ret._struct.size() != 0 )
            ret._sorted_struct = sort_description_by_name( ret._struct );
        else
            ret._sorted_struct = new IdlObject[ 0 ];

        // Sort union
        if ( ret._union.size() != 0 )
            ret._sorted_union = sort_description_by_name( ret._union );
        else
            ret._sorted_union = new IdlObject[ 0 ];

        // Sort enum
        if ( ret._enum.size() != 0 )
            ret._sorted_enum = sort_description_by_name( ret._enum );
        else
            ret._sorted_enum = new IdlObject[ 0 ];

        // Sort typedef
        if ( ret._typedef.size() != 0 )
            ret._sorted_typedef = sort_description_by_name( ret._typedef );
        else
            ret._sorted_typedef = new IdlObject[ 0 ];

        // Sort const
        if ( ret._const.size() != 0 )
            ret._sorted_const = sort_description_by_name( ret._const );
        else
            ret._sorted_const = new IdlObject[ 0 ];

        // Sort native
        if ( ret._native.size() != 0 )
            ret._sorted_native = sort_description_by_name( ret._native );
        else
            ret._sorted_native = new IdlObject[ 0 ];

        // Sort operation
        if ( ret._operation.size() != 0 )
            ret._sorted_operation = sort_description_by_name( ret._operation );
        else
            ret._sorted_operation = new IdlObject[ 0 ];

        // Sort attribute
        if ( ret._attribute.size() != 0 )
            ret._sorted_attribute = sort_description_by_name( ret._attribute );
        else
            ret._sorted_attribute = new IdlObject[ 0 ];

        // Sort member
        if ( ret._member.size() != 0 )
            ret._sorted_member = sort_description_by_name( ret._member );
        else
            ret._sorted_member = new IdlObject[ 0 ];

        // Sort factory
        if ( ret._factory.size() != 0 )
            ret._sorted_factory = sort_description_by_name( ret._factory );
        else
            ret._sorted_factory = new IdlObject[ 0 ];

        return ret;
    }

    /**
     * This method translates a hierarchy between contents
     */
    public void translateHierarchy( IdlObject obj, java.io.PrintWriter output )
    {
        java.util.Vector v = new java.util.Vector();
        IdlObject obj2 = obj;

        while ( obj2 != null )
        {
            switch ( obj2.kind() )
            {

            case IdlType.e_module :

            case IdlType.e_interface :

            case IdlType.e_forward_interface :

            case IdlType.e_value :

            case IdlType.e_forward_value :
                v.addElement( obj2 );
                break;
            }

            if ( obj2.upper() != null )
                if ( obj2.upper().kind() == IdlType.e_root )
                    break;

            obj2 = obj2.upper();
        }

        if ( v.size() > 1 )
        {
            String dec = "";
            output.println( "<PRE>" );

            for ( int i = v.size() - 1; i >= 0; i-- )
            {
                dec = dec + "   ";

                if ( i == 0 )
                    output.println( "<B>" + fullname( ( IdlObject ) v.elementAt( i ) ) + "</B>" );
                else
                    output.println( "<A HREF=\"" + get_link( ( ( IdlObject ) v.elementAt( i ) ), obj ) + "\" TARGET=\"descriptionFrame\">" + fullname( ( IdlObject ) v.elementAt( i ) ) + "</A>" );

                if ( i != 0 )
                {
                    output.println( dec + "|" );
                    output.print( dec + "+--" );
                }
            }

            output.println( "</PRE>" );
        }
    }

    /**
     * This method translates a IDL type
     */
    public String translateType( IdlObject obj, String desc, String name, boolean write, IdlObject current )
    {
        IdlSimple simple = null;

        switch ( obj.kind() )
        {

        case IdlType.e_simple :
            simple = ( IdlSimple ) obj;

            switch ( simple.internal() )
            {

            case Token.t_void :
                desc = desc + "void";
                break;

            case Token.t_float :
                desc = desc + "float";
                break;

            case Token.t_double :
                desc = desc + "double";
                break;

            case Token.t_short :
                desc = desc + "short";
                break;

            case Token.t_ushort :
                desc = desc + "unsigned short";
                break;

            case Token.t_long :
                desc = desc + "long";
                break;

            case Token.t_ulong :
                desc = desc + "unsigned long";
                break;

            case Token.t_longlong :
                desc = desc + "long long";
                break;

            case Token.t_ulonglong :
                desc = desc + "unsigned long long";
                break;

            case Token.t_char :
                desc = desc + "char";
                break;

            case Token.t_wchar :
                desc = desc + "wchar";
                break;

            case Token.t_boolean :
                desc = desc + "boolean";
                break;

            case Token.t_octet :
                desc = desc + "octet";
                break;

            case Token.t_any :
                desc = desc + "any";
                break;

            case Token.t_typecode :
                desc = desc + "CORBA::TypeCode";
                break;

            case Token.t_object :
                desc = desc + "Object";
                break;

            case Token.t_ValueBase :
                desc = desc + "valuebase";
                break;
            }

            break;

        case IdlType.e_fixed :
            desc = desc + "fixed";
            break;

        case IdlType.e_string :
            desc = desc + "string";
            break;

        case IdlType.e_wstring :
            desc = desc + "wstring";
            break;

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_enum :

        case IdlType.e_interface :

        case IdlType.e_forward_interface :

        case IdlType.e_exception :

        case IdlType.e_native :

        case IdlType.e_value :

        case IdlType.e_forward_value :
            desc = desc + "<A HREF=\"" + get_link( obj, current ) + "\" TARGET=\"descriptionFrame\">" + fullname_idl( obj ) + "</A>";
            break;

        case IdlType.e_typedef :
            obj.reset();
            translateType( obj.current(), desc, name, write, current );
            break;

        case IdlType.e_sequence :
            desc = desc + "sequence&lt;";
            desc = translateType( obj.current(), desc, name, false, current );
            desc = desc + "&gt;";
            break;

        case IdlType.e_array :
            desc = translateType( obj.current(), desc, name + "[" + ( ( IdlArray ) obj ).getDimension() + "]", false, current );

            if ( obj.current().kind() != IdlType.e_array )
            {
                desc = desc + " " + name;
                desc = desc + "[" + ( ( IdlArray ) obj ).getDimension() + "]";
            }

            return desc;

        case IdlType.e_ident :

            if ( ( ( IdlIdent ) obj ).internalObject().name().equals( "TypeCode" ) )
                desc = desc + "CORBA::TypeCode";
            else
                desc = desc + "<A HREF=\"" + get_link( ( ( IdlIdent ) obj ).internalObject(), current ) + "\" TARGET=\"descriptionFrame\">" + fullname_idl( ( ( IdlIdent ) obj ).internalObject() ) + "</A>";

            break;

        case IdlType.e_value_box :
            if ( ( ( IdlValueBox ) obj ).simple() )
                desc = desc + fullname_idl( obj );
            else
            {
                obj.reset();
                desc = translateType( obj.current(), desc, name, write, current );
            }

            break;
        }

        if ( write )
            desc = desc + " " + name;

        return desc;
    }

    /**
     * This method translates a IDL parameter
     */
    public String translateParameter( IdlObject obj, String desc )
    {
        IdlParam p = ( IdlParam ) obj;

        p.reset();

        switch ( p.param_attr() )
        {

        case 0 :
            desc = desc + "in ";
            break;

        case 1 :
            desc = desc + "out ";
            break;

        case 2 :
            desc = desc + "inout ";
            break;
        }

        return translateType( p.current(), desc, p.name(), true , obj );
    }


    /**
     * This method translates a Module
     */
    public void translateModule( IdlObject obj, java.io.File writeInto )
    {
        java.io.File new_path = createDirectory( obj.name(), writeInto );

        java.io.PrintWriter output = create_file( obj.name(), new_path );

        level++;

        write_page_title( obj.name(), output );

        write_navigation_bar( output );

        write_title( "Module " + obj.name(), output );

        translateHierarchy( obj, output );

        write_description( obj, output );

        output.println( "<BR><BR>" );

        translateDescription( obj, new_path );

        translateContentTable( obj, output, 3 );

        write_alldescriptions_file( obj.name() + " : All descriptions", new_path, obj );

        output.println( "</BODY>" );
        output.println( "</HTML>" );
        output.close();

        level--;
    }

    /**
     * This method translates an Enum
     */
    public void translateEnum( IdlObject obj, java.io.PrintWriter output )
    {
        String desc = "";

        output.println( "<A NAME=\"" + obj.name() + "\"><!-- --></A><H3>" );
        output.println( obj.name() + "</H3>" );
        output.println( "<PRE>" );

        IdlEnumMember member = null;
        obj.reset();
        int i = 1;
        desc = desc + "<UL>" + "enum " + obj.name() + "<BR>";
        desc = desc + "{<BR><UL>";

        while ( obj.end() != true )
        {

            member = ( IdlEnumMember ) obj.current();

            i++;

            desc = desc + member.name();

            obj.next();

            if ( !obj.end() )
                desc = desc + ", ";

            desc = desc + "<BR>";
        }

        desc = desc + "</UL>}<BR></UL>";
        output.println( desc );

        output.println( "</PRE><DL>" );
        output.println( "<DD>" );
        write_description( obj, output );
        output.println( "</DD>" );
        output.println( "</DL>" );
        output.println( "<HR>" );
    }

    /**
     * This method translates a Struct
     */
    public void translateStruct( IdlObject obj, java.io.PrintWriter output )
    {
        output.println( "<A NAME=\"" + obj.name() + "\"><!-- --></A><H3>" );
        output.println( obj.name() + "</H3>" );
        output.println( "<PRE>" );

        String desc = "";

        IdlStructMember member = null;

        desc = desc + "<UL>" + "struct " + obj.name() + "<BR>";
        desc = desc + "{<BR><UL>";

        obj.reset();

        while ( obj.end() != true )
        {
            member = ( IdlStructMember ) obj.current();

            member.reset();

            desc = translateType( member.current(), desc, member.name(), true, obj );

            obj.next();

            if ( !obj.end() )
                desc = desc + ", ";

            desc = desc + "<BR>";
        }

        desc = desc + "</UL>}<BR></UL>";
        output.println( desc );

        output.println( "</PRE><DL>" );
        output.println( "<DD>" );
        write_description( obj, output );
        output.println( "</DD>" );
        output.println( "</DL>" );
        output.println( "<HR>" );
    }

    /**
     * This method translates an Union
     */
    public void translateUnion( IdlObject obj, java.io.PrintWriter output )
    {
        IdlUnionMember member = null;
        int default_index, index;

        output.println( "<A NAME=\"" + obj.name() + "\"><!-- --></A><H3>" );
        output.println( obj.name() + "</H3>" );
        output.println( "<PRE>" );

        String desc = "";

        obj.reset();
        member = ( IdlUnionMember ) obj.current();
        member.reset();

        desc = desc + "<UL>" + "union " + obj.name() + " switch ( ";
        desc = translateType( member.current(), desc, member.name(), false, obj );
        desc = desc + " )<BR>";
        desc = desc + "{<BR><UL>";

        obj.next();
        default_index = ( ( IdlUnion ) obj ).index();
        index = 0;

        while ( obj.end() != true )
        {
            member = ( IdlUnionMember ) obj.current();

            member.reset();

            if ( index == default_index )
            {
                desc = desc + "default : <BR><UL>";
            }
            else
            {
                desc = desc + "case " + adaptExpression( member.getExpression() ) + ": <BR><UL>";
            }

            desc = translateType( member.current(), desc, member.name(), true, obj );

            desc = desc + ";<BR></UL>";

            obj.next();
            index++;
        }

        desc = desc + "</UL>}<BR></UL>";
        output.println( desc );

        output.println( "</PRE><DL>" );
        output.println( "<DD>" );
        write_description( obj, output );
        output.println( "</DD>" );
        output.println( "</DL>" );
        output.println( "<HR>" );
    }

    /**
     * This method translates an Exception
     */
    public void translateException( IdlObject obj, java.io.PrintWriter output )
    {
        String desc = "";

        output.println( "<A NAME=\"" + obj.name() + "\"><!-- --></A><H3>" );
        output.println( obj.name() + "</H3>" );
        output.println( "<PRE>" );

        IdlStructMember member = null;

        desc = desc + "<UL>" + "exception " + obj.name() + "<BR>";
        desc = desc + "{<BR><UL>";

        obj.reset();

        while ( obj.end() != true )
        {
            member = ( IdlStructMember ) obj.current();

            member.reset();

            desc = translateType( member.current(), desc, member.name(), true, obj );

            obj.next();

            if ( !obj.end() )
                desc = desc + ", ";

            desc = desc + "<BR>";
        }

        desc = desc + "</UL>}<BR></UL>";
        output.println( desc );

        output.println( "</PRE><DL>" );
        output.println( "<DD>" );
        write_description( obj, output );
        output.println( "</DD>" );
        output.println( "</DL>" );
        output.println( "<HR>" );
    }

    /**
     * This method translates a TypeDef
     */
    public void translateTypedef( IdlObject obj, java.io.PrintWriter output )
    {
        String desc = "";

        output.println( "<A NAME=\"" + obj.name() + "\"><!-- --></A><H3>" );
        output.println( obj.name() + "</H3>" );
        output.println( "<PRE>" );

        desc = desc + "<UL>" + "typedef ";

        obj.reset();
        desc = translateType( obj.current(), desc, obj.name(), true, obj );

        desc = desc + ";</UL><BR>";
        output.println( desc );

        output.println( "</PRE><DL>" );
        output.println( "<DD>" );
        write_description( obj, output );
        output.println( "</DD>" );
        output.println( "</DL>" );
        output.println( "<HR>" );
    }

    /**
     * This method translates a ValueBox
     */
    public void translateValueBox( IdlObject obj, java.io.PrintWriter output )
    {
        String desc = "";

        output.println( "<A NAME=\"" + obj.name() + "\"><!-- --></A><H3>" );
        output.println( obj.name() + "</H3>" );
        output.println( "<PRE>" );

        desc = desc + "<UL>" + "valuetype " + obj.name() + " ";

        desc = translateType( obj.current(), desc, obj.name(), false, obj );

        desc = desc + ";</UL><BR>";
        output.println( desc );

        output.println( "</PRE><DL>" );
        output.println( "<DD>" );
        write_description( obj, output );
        output.println( "</DD>" );
        output.println( "</DL>" );
        output.println( "<HR>" );
    }

    /**
     * This method translates a factory member
     */
    public void translateFactory( IdlObject obj, java.io.PrintWriter output )
    {
        String desc = "";

        output.println( "<A NAME=\"" + obj.name() + "\"><!-- --></A><H3>" );
        output.println( obj.name() + "</H3>" );
        output.println( "<PRE>" );


        desc = desc + "<UL>" + "factory " + obj.name() + "(";

        obj.reset();

        while ( obj.end() != true )
        {
            desc = desc + "in ";
            obj.current().reset();
            desc = translateType( obj.current().current(), desc, obj.current().name(), true, obj );
            obj.next();

            if ( obj.end() != true )
                desc = desc + ", ";
        }

        desc = desc + ");</UL><BR>";
        output.println( desc );

        output.println( "</PRE><DL>" );
        output.println( "<DD>" );
        write_description( obj, output );
        output.println( "</DD>" );
        output.println( "</DL>" );
        output.println( "<HR>" );
    }

    /**
     * This method translates a state member
     */
    public void translateStateMember( IdlObject obj, java.io.PrintWriter output )
    {
        IdlStateMember member = null;

        output.println( "<A NAME=\"" + obj.name() + "\"><!-- --></A><H3>" );
        output.println( obj.name() + "</H3>" );
        output.println( "<PRE>" );

        member = ( IdlStateMember ) obj;
        member.reset();

        if ( member.public_member() )
            output.print( "public " );
        else
            output.print( "private " );

        output.println( translateType( member.current(), "", member.name(), true , obj ) );

        output.println( "</PRE><DL>" );

        output.println( "<DD>" );

        write_description( member, output );

        output.println( "</DD>" );

        output.println( "</DL>" );

        output.println( "<HR>" );
    }

    /**
     * This method translates an Attribute
     */
    public void translateAttribute( IdlObject obj, java.io.PrintWriter output )
    {
        IdlAttribute attr = ( IdlAttribute ) obj;
        attr.reset();

        output.println( "<A NAME=\"" + obj.name() + "\"><!-- --></A><H3>" );
        output.println( obj.name() + "</H3>" );
        output.println( "<PRE>" );

        if ( attr.readOnly() )
            output.print( "readonly " );

        output.print( "attribute " );

        output.println( translateType( attr.current(), "", attr.name(), true, obj ) + "</PRE>" );

        output.println( "<DL>" );

        output.println( "<DD>" );

        write_description( attr, output );

        output.println( "</DD>" );

        output.println( "</DL>" );

        output.println( "<HR>" );
    }

    /**
     * This method translates an Operation
     */
    public void translateOperation( IdlObject obj, java.io.PrintWriter output )
    {
        IdlOp op = ( IdlOp ) obj;

        output.println( "<A NAME=\"" + obj.name() + "\"><!-- --></A><H3>" );
        output.println( obj.name() + "</H3>" );
        output.println( "<PRE>" );

        String desc = "";

        desc = desc + "<UL>";

        if ( op.oneway() )
            desc = desc + "oneway ";

        op.reset();

        desc = translateType( op.current(), desc, op.name(), false, obj );

        op.next();

        desc = desc + " " + op.name() + "(";

        while ( op.end() != true )
        {
            if ( op.current().kind() != IdlType.e_param )
                break;

            desc = translateParameter( op.current(), desc );

            op.next();

            if ( op.end() != true )
                if ( op.current().kind() == IdlType.e_param )
                    desc = desc + ", ";
        }

        desc = desc + ")";

        if ( op.end() != true )
            if ( op.current().kind() == IdlType.e_raises )
            {
                desc = desc + "<BR><UL> raises (<BR><UL>";

                IdlRaises raises = ( IdlRaises ) op.current();
                raises.reset();

                while ( raises.end() != true )
                {
                    //desc = desc + fullname_idl( raises.current() );

                    desc = desc + "<A HREF=\"" + get_link( raises.current(), obj ) + "\" TARGET=\"descriptionFrame\">" + fullname_idl( raises.current() ) + "</A>";

                    raises.next();

                    if ( raises.end() != true )
                        desc = desc + ", <BR>";
                }

                desc = desc + ")</UL></UL>";

                op.next();
            }

        if ( op.end() != true )
            if ( op.current().kind() == IdlType.e_context )
            {
                desc = desc + "<BR><UL>";

                desc = desc + " context (";

                IdlContext ctx = ( IdlContext ) op.current();
                java.util.Vector list = ctx.getValues();
                int index = 0;

                while ( index < list.size() )
                {
                    desc = desc + "\"" + ( String ) list.elementAt( index ) + "\"";

                    if ( ( index + 1 ) < list.size() )
                        desc = desc + ", ";

                    index++;
                }

                desc = desc + ")<BR></UL>";
            }

        output.println( desc );

        output.println( "</PRE></UL><DL>" );
        output.println( "<DD>" );
        write_description( obj, output );
        output.println( "</DD>" );
        output.println( "</DL>" );
        output.println( "<HR>" );
    }

    /**
     * This method translates a ValueType
     */
    public void translateValueType( IdlObject obj, java.io.File writeInto )
    {
        java.io.File new_path = createDirectory( obj.name(), writeInto );

        java.io.PrintWriter output = create_file( obj.name() , new_path );

        level++;

        write_alldescriptions_file( obj.name() + " : All descriptions", new_path, obj );

        IdlValue value = ( IdlValue ) obj;

        String desc = "";

        write_page_title( obj.name(), output );

        write_navigation_bar( output );

        write_title( "ValueType " + obj.name(), output );

        translateHierarchy( obj, output );

        write_description( obj, output );

        desc = desc + "<UL>";

        if ( value.abstract_value() )
            desc = desc + "abstract ";

        if ( value.custom_value() )
            desc = desc + "custom ";

        desc = desc + "valuetype " + obj.name();

        java.util.Vector inheritance = value.getInheritanceList();

        if ( inheritance.size() != 0 )
        {
            desc = desc + " : ";

            for ( int i = 0; i < inheritance.size(); i++ )
            {
                if ( ( ( IdlValueInheritance ) inheritance.elementAt( i ) ).truncatable_member() )
                    desc = desc + "truncatable ";

                desc = desc + "<A HREF=\"" + get_link( ( ( IdlValueInheritance ) inheritance.elementAt( i ) ).getValue(), obj ) + "\" TARGET=\"descriptionFrame\">" + fullname_idl( ( ( IdlValueInheritance ) inheritance.elementAt( i ) ).getValue() ) + "</A>";

                if ( ( i + 1 ) < inheritance.size() )
                    desc = desc + ", ";
            }
        }

        desc = desc + "<BR><BR></UL>";

        output.println( desc );

        translateContentTable( obj, output, 1 );

        IdlValue inherit = null;

        if ( inheritance.size() != 0 )
        {

            for ( int i = 0; i < inheritance.size(); i++ )
            {
                inherit = ( ( IdlValueInheritance ) inheritance.elementAt( i ) ).getValue();

                if ( inherit.included() == false )
                {
                    output.println( "<TABLE BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">" );
                    output.println( "<TR BGCOLOR=\"#EEEEFF\" ID=\"TableSubHeadingColor\">" );
                    output.println( "<TD><B>Members, attributes and operations inherited from <A HREF=\"" + get_link( inherit, obj ) + "\">" + fullname_idl( inherit ) + "</A></B></TD>" );
                    output.println( "</TR>" );
                    output.println( "<TR BGCOLOR=\"white\" ID=\"TableRowColor\">" );
                    output.println( "<TD><CODE>\n" );

                    inherit.reset();

                    while ( inherit.end() != true )
                    {
                        switch ( inherit.current().kind() )
                        {

                        case IdlType.e_state_member :

                        case IdlType.e_operation :

                        case IdlType.e_attribute :
                            output.println( "<A HREF=\"" + get_link( inherit.current(), obj ) + "\">" + inherit.current().name() + "</A>" );
                            break;
                        }

                        inherit.next();
                    }

                    output.println( "</CODE></TD></TR>\n" );
                    output.println( "</TABLE><BR>\n" );
                }
            }
        }

        translateContentTable( obj, output, 2 );

        level--;

        output.println( "</BODY>" );
        output.println( "</HTML>" );
        output.close();
    }

    /**
     * This method translates an Interface
     */
    public void translateInterface( IdlObject obj, java.io.File writeInto )
    {
        IdlInterface itf = ( IdlInterface ) obj;

        java.io.File new_path = createDirectory( obj.name(), writeInto );

        java.io.PrintWriter output = create_file( obj.name() , new_path );

        level++;

        write_alldescriptions_file( obj.name() + " : All descriptions", new_path, obj );

        String desc = "";

        write_page_title( obj.name(), output );

        write_navigation_bar( output );

        write_title( "interface " + obj.name(), output );

        translateHierarchy( obj, output );

        write_description( obj, output );

        desc = desc + "<UL>";

        if ( itf.abstract_interface() )
            desc = desc + "abstract ";

        desc = desc + "interface " + obj.name();

        java.util.Vector inheritance = itf.getInheritance();

        if ( inheritance.size() != 0 )
        {
            desc = desc + " : ";

            for ( int i = 0; i < inheritance.size(); i++ )
            {
                desc = desc + "<A HREF=\"" + get_link( ( ( IdlInterface ) inheritance.elementAt( i ) ), obj ) + "\" TARGET=\"descriptionFrame\">" + fullname_idl( ( ( IdlInterface ) inheritance.elementAt( i ) ) ) + "</A>";

                if ( ( i + 1 ) < inheritance.size() )
                    desc = desc + ", ";
            }
        }

        desc = desc + "<BR><BR></UL>";

        output.println( desc );

        translateContentTable( obj, output, 1 );

        IdlInterface inherit = null;

        if ( inheritance.size() != 0 )
        {

            for ( int i = 0; i < inheritance.size(); i++ )
            {
                inherit = ( IdlInterface ) inheritance.elementAt( i );

                if ( inherit.included() == false )
                {
                    output.println( "<TABLE BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">" );
                    output.println( "<TR BGCOLOR=\"#EEEEFF\" ID=\"TableSubHeadingColor\">" );
                    output.println( "<TD><B>Attributes and operations inherited from <A HREF=\"" + get_link( inherit, obj ) + "\">" + fullname_idl( inherit ) + "</A></B></TD>" );
                    output.println( "</TR>" );
                    output.println( "<TR BGCOLOR=\"white\" ID=\"TableRowColor\">" );
                    output.println( "<TD><CODE>\n" );

                    inherit.reset();

                    while ( inherit.end() != true )
                    {
                        switch ( inherit.current().kind() )
                        {

                        case IdlType.e_operation :

                        case IdlType.e_attribute :
                            output.println( "<A HREF=\"" + get_link( inherit.current(), obj ) + "\">" + inherit.current().name() + "</A>" );
                            break;
                        }

                        inherit.next();
                    }

                    output.println( "</CODE></TD></TR>\n" );
                    output.println( "</TABLE><BR>\n" );
                }
            }
        }

        translateContentTable( obj, output, 2 );

        level--;

        output.println( "</BODY>" );
        output.println( "</HTML>" );
        output.close();
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
                break;

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

            if ( _root.isDefined( item, false ) )
                break;
        }

        for ( int j = i; j < list.size(); j++ )
        {
            correct_identifier = correct_identifier + ( String ) list.elementAt( j );

            if ( ( j + 1 ) < list.size() )
                correct_identifier = correct_identifier + "::";
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
                break;

            if ( Character.isDigit( expr.charAt( index ) ) )
            {
                // Adapt a number expression

                if ( expr.charAt( index ) == '0' &&
                     ( index + 1 ) < expr.length() &&
                     ( expr.charAt( index + 1 ) == 'X' ||
                       expr.charAt( index + 1 ) == 'x' ) )
                {
                    // Consume a hexadecimal number
                    tmp[ tmp_index++ ] = expr.charAt( index++ );  // Take '0'
                    tmp[ tmp_index++ ] = expr.charAt( index++ );  // Take 'x'
                    while ( ( index < expr.length() ) &&
                            ( Character.isDigit( expr.charAt( index ) ) ||
                              ( expr.charAt( index ) >= 'A' &&
                                expr.charAt( index ) <= 'F' ) ||
                              ( expr.charAt( index ) >= 'a' &&
                                expr.charAt( index ) <= 'f' ) ) )
                        tmp[ tmp_index++ ] = expr.charAt( index++ );
                }
                else
                {
                    // Consume a non-hexadecimal number
                    while ( ( index < expr.length() ) &&
                            ( Character.isDigit( expr.charAt( index ) ) ||
                              expr.charAt( index ) == '.' ) )
                        tmp[ tmp_index++ ] = expr.charAt( index++ );
                }

                tmp[ tmp_index ] = 0;

                adapt_expr = adapt_expr + new String( tmp, 0, tmp_index );

                tmp_index = 0;
            }
            else
                if ( Character.isLetter( expr.charAt( index ) ) )
                {
                    // Adapt an identifier expression

                    while ( ( index < expr.length() ) &&
                            ( Character.isLetterOrDigit( expr.charAt( index ) ) ||
                              ( expr.charAt( index ) == '.' ) ||
                              ( expr.charAt( index ) == '_' ) ) )
                        tmp[ tmp_index++ ] = expr.charAt( index++ );

                    tmp[ tmp_index ] = 0;

                    adapt_expr = adapt_expr + correctIdentifier( new String( tmp, 0, tmp_index ) );

                    tmp_index = 0;
                }
                else
                    if ( expr.charAt( index ) == '\"' )
                    {
                        index++;

                        while ( expr.charAt( index ) != '\"' )
                            tmp[ tmp_index++ ] = expr.charAt( index++ );

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

        return adapt_expr;
    }

    /**
     * This method translates a Constant
     */
    public void translateConstant( IdlObject obj, java.io.PrintWriter output )
    {
        String desc = "";

        output.println( "<A NAME=\"" + obj.name() + "\"><!-- --></A><H3>" );
        output.println( obj.name() + "</H3>" );
        output.println( "<PRE>" );

        desc = desc + "<UL>" + "const ";

        desc = translateType( obj.current(), desc, obj.name(), true, obj );

        desc = desc + " = " + adaptExpression( ( ( IdlConst ) obj ).expression() );

        desc = desc + ";</UL><BR>";
        output.println( desc );

        output.println( "</PRE><DL>" );
        output.println( "<DD>" );
        write_description( obj, output );
        output.println( "</DD>" );
        output.println( "</DL>" );
        output.println( "<HR>" );
    }

    /**
     * This method translates a Native
     */
    public void translateNative( IdlObject obj, java.io.PrintWriter output )
    {

        String desc = "";

        output.println( "<A NAME=\"" + obj.name() + "\"><!-- --></A><H3>" );
        output.println( obj.name() + "</H3>" );
        output.println( "<PRE>" );

        desc = desc + "<UL>" + "native " + obj.name() + ";<BR></UL>";

        output.println( desc );

        output.println( "</PRE><DL>" );
        output.println( "<DD>" );
        write_description( obj, output );
        output.println( "</DD>" );
        output.println( "</DL>" );
        output.println( "<HR>" );
    }

    /**
     * This method translates an internal IDL description to an IDL file.
     */
    public void translateInternalDescription( IdlObject obj, java.io.File writeInto, java.io.PrintWriter output )
    {
        obj.reset();

        while ( obj.end() != true )
        {

            if ( obj.current().included() == false )
                switch ( obj.current().kind() )
                {

                case IdlType.e_const :
                    translateConstant( obj.current(), output );
                    break;

                case IdlType.e_enum :
                    translateEnum( obj.current(), output );
                    break;

                case IdlType.e_struct :
                    translateStruct( obj.current(), output );
                    break;

                case IdlType.e_union :
                    translateUnion( obj.current(), output );
                    break;

                case IdlType.e_typedef :
                    translateTypedef( obj.current(), output );
                    break;

                case IdlType.e_exception :
                    translateException( obj.current(), output );
                    break;

                case IdlType.e_native :
                    translateNative( obj.current(), output );
                    break;
                }

            obj.next();
        }
    }

    /**
     * This method translates IDL description to HTML.
     */
    public void translateDescription( IdlObject obj, java.io.File output )
    {
        obj.reset();

        while ( obj.end() != true )
        {
            if ( obj.current().included() == false )
                switch ( obj.current().kind() )
                {

                case IdlType.e_module :
                    translateModule( obj.current(), output );
                    break;

                case IdlType.e_value :
                    translateValueType( obj.current(), output );
                    break;

                case IdlType.e_interface :
                    translateInterface( obj.current(), output );
                    break;
                }


            obj.next();
        }
    }

    /**
     * This method translates IDL descriptions to an HTML documentation
     */
    public void translateToHTML( IdlObject root, String title, String packageName )
    {
        _root = root;

        java.io.File first = null;

        if ( org.openorb.compiler.doc.IdlDoc.outdir != null )
            first = new java.io.File( org.openorb.compiler.doc.IdlDoc.outdir );

        java.io.File writeInto = createDirectory( packageName, first );

        // Create the documentation index
        write_index_file( title, writeInto );

        // Create the full index
        write_index( writeInto );

        // Create the documentation overview
        write_overview_file( root, title, writeInto );

        // Create the documentation all descriptions
        write_alldescriptions_file_index( title, writeInto, root );

        // Create the documentation summary
        write_summary_file( title, writeInto );

        // Create the tree
        write_tree( writeInto );

        // Create the deprecated list
        write_deprecated( writeInto );

        // Create the documentation for IDL descriptions
        translateDescription( root, writeInto );

        if ( org.openorb.compiler.doc.IdlDoc.stylesheet )
            write_style_sheet( writeInto );
    }
}

