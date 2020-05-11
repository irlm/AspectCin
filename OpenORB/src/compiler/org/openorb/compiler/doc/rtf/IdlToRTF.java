/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.doc.rtf;

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
 * This class takes an IDL graph and generates its RTF documentation.
 */
public class IdlToRTF
{
    /**
     * Reference to the Root object
     */
    private IdlObject _root;

    /**
     * Nb entry in the color table
     */
    private int nb_color_entry = 1;

    /**
     * Title model
     */
    private org.openorb.compiler.doc.rtf.model title_model;

    /**
     * Header model
     */
    private org.openorb.compiler.doc.rtf.model header_model;

    /**
     * Footer model
     */
    private org.openorb.compiler.doc.rtf.model footer_model;

    /**
     * Section model : title
     */
    private org.openorb.compiler.doc.rtf.model section_title_model;

    /**
     * Section model : id
     */
    private org.openorb.compiler.doc.rtf.model section_id_model;

    /**
     * Section model : hierarchy
     */
    private org.openorb.compiler.doc.rtf.model section_hierarchy_model;

    /**
     * Section model : description
     */
    private org.openorb.compiler.doc.rtf.model section_desc_model;

    /**
     * Summary model : title
     */
    private org.openorb.compiler.doc.rtf.model summary_title_model;

    /**
     * Summary model : entry name
     */
    private org.openorb.compiler.doc.rtf.model summary_entry_name_model;

    /**
     * Summary model : entry description
     */
    private org.openorb.compiler.doc.rtf.model summary_entry_desc_model;

    /**
     * Summary model : inheritance title
     */
    private org.openorb.compiler.doc.rtf.model summary_inher_title_model;

    /**
     * Summary model : inheritance description
     */
    private org.openorb.compiler.doc.rtf.model summary_inher_desc_model;

    /**
     * Summary model : inheritance list
     */
    private org.openorb.compiler.doc.rtf.model summary_inher_list_model;

    /**
     * Detail model : title
     */
    private org.openorb.compiler.doc.rtf.model detail_title_model;

    /**
     * Detail model : id
     */
    private org.openorb.compiler.doc.rtf.model detail_id_model;

    /**
     * Detail model : java
     */
    private org.openorb.compiler.doc.rtf.model detail_java_model;

    /**
     * Detail model : description
     */
    private org.openorb.compiler.doc.rtf.model detail_desc_model;

    /**
     * Detail model : comment
     */
    private org.openorb.compiler.doc.rtf.model detail_comment_model;

    /**
     * Detail model : highlight
     */
    private org.openorb.compiler.doc.rtf.model detail_highlight_model;


    /**
     * This method is used to convert a style sheet attribute to a RTF attribute
     */
    private String convertTextAttribute( String attr )
    {
        String s = "";
        String token = null;
        java.util.StringTokenizer str = new java.util.StringTokenizer( attr, "," );

        while ( str.hasMoreTokens() )
        {
            token = str.nextToken();

            if ( token.equalsIgnoreCase( "italic" ) )
                s = s + "\\i";
            else
                if ( token.equalsIgnoreCase( "bold" ) )
                    s = s + "\\b";
                else
                    if ( token.equalsIgnoreCase( "underline" ) )
                        s = s + "\\ul";
                    else
                        if ( token.equalsIgnoreCase( "uppercase" ) )
                            s = s + "\\caps";
                        else
                            if ( token.equalsIgnoreCase( "left" ) )
                                s = s + "\\ql";
                            else
                                if ( token.equalsIgnoreCase( "right" ) )
                                    s = s + "\\qr";
                                else
                                    if ( token.equalsIgnoreCase( "center" ) )
                                        s = s + "\\qc";
                                    else
                                        if ( token.equalsIgnoreCase( "justified" ) )
                                            s = s + "\\qj";
        }

        return s;
    }

    /**
     * This method is used to convert border attributes to a RTF attribute
     */
    private String convertBorderAttribute( String attr, boolean table )
    {
        String s = "";
        String style = "";
        String extra = "";
        boolean bottom = false, top = false, left = false, right = false;
        String token = null;
        java.util.StringTokenizer str = new java.util.StringTokenizer( attr, "," );

        while ( str.hasMoreTokens() )
        {
            token = str.nextToken();

            if ( token.equalsIgnoreCase( "box" ) )
            {
                bottom = true;
                top = true;
                left = true;
                right = true;
            }
            else
                if ( token.equalsIgnoreCase( "bottom" ) )
                    bottom = true;
                else
                    if ( token.equalsIgnoreCase( "top" ) )
                        top = true;
                    else
                        if ( token.equalsIgnoreCase( "left" ) )
                            left = true;
                        else
                            if ( token.equalsIgnoreCase( "right" ) )
                                right = true;
                            else
                                if ( token.equalsIgnoreCase( "double" ) )
                                    style = "\\brdrdb";
                                else
                                    if ( token.equalsIgnoreCase( "simple" ) )
                                        style = "\\brdrs";
                                    else
                                        if ( token.equalsIgnoreCase( "dotted" ) )
                                            style = "\\brdrdot";
                                        else
                                            if ( token.equalsIgnoreCase( "shadowed" ) )
                                                extra = "\\brdrsh";
        }

        if ( bottom )
        {
            if ( table )
                s = s + "\\clbrdrb" + style + extra + "\\brsp10" + " ";
            else
                s = s + "\\brdrb" + style + extra + "\\brsp10" + " ";

        }

        if ( top )
        {
            if ( table )
                s = s + "\\clbrdrt" + style + extra + "\\brsp10" + " ";
            else
                s = s + "\\brdrt" + style + extra + "\\brsp10" + " ";
        }

        if ( left )
        {
            if ( table )
                s = s + "\\clbrdrl" + style + extra + "\\brsp10" + " ";
            else
                s = s + "\\brdrl" + style + extra + "\\brsp10" + " ";
        }

        if ( right )
        {
            if ( table )
                s = s + "\\clbrdrr" + style + extra + "\\brsp10" + " ";
            else
                s = s + "\\brdrr" + style + extra + "\\brsp10" + " ";
        }

        return s;
    }

    /**
     * This method is used to load a model type
     */
    private void loadModel( java.util.Properties p, org.openorb.compiler.doc.rtf.model model, String type, boolean table )
    {
        String s = null;

        // Load font name
        model.fontName = p.getProperty( type + ".font" );
        model.fontFamily = p.getProperty( type + ".family" );

        // Load color
        model.color = p.getProperty( type + ".color" );
        model.backcolor = p.getProperty( type + ".background" );

        // Load font size
        s = p.getProperty( type + ".size" );

        model.fontSize = "" + ( ( new Integer( s ) ).intValue() * 2 );

        // Load text attribute
        s = p.getProperty( type + ".attributes" );

        if ( s != null )
            model.attribute = convertTextAttribute( s );

        // Load border attribute
        s = p.getProperty( type + ".border" );

        if ( s != null )
            model.border = convertBorderAttribute( s, table );
    }

    /**
     * This method is used to load style sheet properties
     */
    private void loadStyleSheet()
    {
        java.util.Properties stylesheet = new java.util.Properties();
        // TODO: fixme.
        //org.openorb.compiler.OpenORBProperties props = new org.openorb.compiler.OpenORBProperties();
        java.io.InputStream input = null; //props.openFile("rtf.cs");

        if ( input != null )
        {
            try
            {
                stylesheet.load( input );
                input.close();
            }
            catch ( java.io.IOException ex )
            { }

        }
        else
        {
            System.out.println( "IDL to RTF fatal error : Unable to open style sheet model ( rtf.cs )" );
            System.exit( 0 );
        }

        title_model = new org.openorb.compiler.doc.rtf.model();
        header_model = new org.openorb.compiler.doc.rtf.model();
        footer_model = new org.openorb.compiler.doc.rtf.model();
        section_title_model = new org.openorb.compiler.doc.rtf.model();
        section_id_model = new org.openorb.compiler.doc.rtf.model();
        section_hierarchy_model = new org.openorb.compiler.doc.rtf.model();
        section_desc_model = new org.openorb.compiler.doc.rtf.model();
        summary_title_model = new org.openorb.compiler.doc.rtf.model();
        summary_entry_name_model = new org.openorb.compiler.doc.rtf.model();
        summary_entry_desc_model = new org.openorb.compiler.doc.rtf.model();
        summary_inher_title_model = new org.openorb.compiler.doc.rtf.model();
        summary_inher_list_model = new org.openorb.compiler.doc.rtf.model();
        summary_inher_desc_model = new org.openorb.compiler.doc.rtf.model();
        detail_title_model = new org.openorb.compiler.doc.rtf.model();
        detail_id_model = new org.openorb.compiler.doc.rtf.model();
        detail_java_model = new org.openorb.compiler.doc.rtf.model();
        detail_desc_model = new org.openorb.compiler.doc.rtf.model();
        detail_comment_model = new org.openorb.compiler.doc.rtf.model();
        detail_highlight_model = new org.openorb.compiler.doc.rtf.model();

        loadModel( stylesheet, title_model, "title", false );
        loadModel( stylesheet, header_model, "header", false );
        loadModel( stylesheet, footer_model, "footer", false );
        loadModel( stylesheet, section_title_model, "section.title", false );
        loadModel( stylesheet, section_id_model, "section.id", false );
        loadModel( stylesheet, section_hierarchy_model, "section.hierarchy", false );
        loadModel( stylesheet, section_desc_model, "section.description", false );
        loadModel( stylesheet, summary_title_model, "summary.title", true );
        loadModel( stylesheet, summary_entry_name_model, "summary.entry_name", true );
        loadModel( stylesheet, summary_entry_desc_model, "summary.entry_description", true );
        loadModel( stylesheet, summary_inher_title_model, "summary.inheritance_title", true );
        loadModel( stylesheet, summary_inher_desc_model, "summary.inheritance_description", true );
        loadModel( stylesheet, summary_inher_list_model, "summary.inheritance_list", true );
        loadModel( stylesheet, detail_title_model, "detail.title", true );
        loadModel( stylesheet, detail_id_model, "detail.id", true );
        loadModel( stylesheet, detail_java_model, "detail.java", true );
        loadModel( stylesheet, detail_desc_model, "detail.description", true );
        loadModel( stylesheet, detail_comment_model, "detail.comment", true );
        loadModel( stylesheet, detail_highlight_model, "detail.highlight", true );
    }

    /**
     * This method return a stringified model
     */
    private String getModel( org.openorb.compiler.doc.rtf.model model )
    {
        String desc = "";

        desc = desc + "\\plain ";

        desc = desc + "\\f" + model.fontNumber;

        desc = desc + "\\fs" + model.fontSize;

        desc = desc + "\\cf" + model.colorNumber;

        if ( model.backcolor != null )
            desc = desc + "\\cbpat" + model.backgroundNumber;

        if ( model.border != null )
            desc = desc + model.border;

        if ( model.attribute != null )
            desc = desc + model.attribute;

        desc = desc + " ";

        return desc;
    }

    /**
     * This method is used to add RTF attribute from a model
     */
    private void write_model( org.openorb.compiler.doc.rtf.model model, java.io.PrintWriter rtf )
    {
        rtf.print( "\\plain " );

        rtf.print( "\\f" + model.fontNumber );

        rtf.print( "\\fs" + model.fontSize );

        rtf.print( "\\cf" + model.colorNumber );

        if ( model.backcolor != null )
            rtf.print( "\\cbpat" + model.backgroundNumber );

        if ( model.border != null )
            rtf.print( model.border );

        if ( model.attribute != null )
            rtf.print( model.attribute + " " );
    }

    /**
     * This method is used to add RTF attribute from a model
     */
    private void write_cell_model( org.openorb.compiler.doc.rtf.model model, java.io.PrintWriter rtf )
    {
        rtf.print( "\\f" + model.fontNumber );

        rtf.print( "\\fs" + model.fontSize );

        rtf.print( "\\cf" + model.colorNumber );

        if ( model.attribute != null )
            rtf.print( model.attribute );

        rtf.print( " " );
    }

    /**
     * This method is used to add RTF attribute from a model
     */
    private void write_cell_border( org.openorb.compiler.doc.rtf.model model, java.io.PrintWriter rtf )
    {
        if ( model.border != null )
            rtf.print( model.border );
    }

    /**
     * This method is used to add a color for a cell
     */
    private void write_cell_color( org.openorb.compiler.doc.rtf.model model, java.io.PrintWriter rtf )
    {
        if ( model.backcolor != null )
            rtf.print( "\\clcbpat" + model.backgroundNumber );
    }

    /**
     * This method is used to create a font entry into the font table.
     */
    private void createFontEntry( org.openorb.compiler.doc.rtf.model model, java.io.PrintWriter rtf )
    {
        rtf.print( "{\\f" + model.fontNumber + "\\f" );

        rtf.print( model.fontFamily + " " );

        rtf.print( model.fontName + "}" );
    }

    /**
     * This method is used to create a font entry into the font table.
     */
    private void createColorEntry( org.openorb.compiler.doc.rtf.model model, java.io.PrintWriter rtf )
    {
        nb_color_entry++;

        if ( model.color.equalsIgnoreCase( "black" ) )
            rtf.print( "\\red0\\green0\\blue0;" );
        else
            if ( model.color.equalsIgnoreCase( "green" ) )
                rtf.print( "\\red0\\green128\\blue0;" );
            else
                if ( model.color.equalsIgnoreCase( "red" ) )
                    rtf.print( "\\red128\\green0\\blue0;" );
                else
                    if ( model.color.equalsIgnoreCase( "blue" ) )
                        rtf.print( "\\red0\\green0\\blue128;" );
                    else
                        if ( model.color.equalsIgnoreCase( "yellow" ) )
                            rtf.print( "\\red128\\green128\\blue0;" );
                        else
                            if ( model.color.equalsIgnoreCase( "lightyellow" ) )
                                rtf.print( "\\red255\\green255\\blue0;" );
                            else
                                if ( model.color.equalsIgnoreCase( "cyan" ) )
                                    rtf.print( "\\red0\\green255\\blue255;" );
                                else
                                    if ( model.color.equalsIgnoreCase( "brown" ) )
                                        rtf.print( "\\red128\\green128\\blue0;" );
                                    else
                                        if ( model.color.equalsIgnoreCase( "grey" ) )
                                            rtf.print( "\\red128\\green128\\blue128;" );
                                        else
                                            if ( model.color.equalsIgnoreCase( "purple" ) )
                                                rtf.print( "\\red128\\green0\\blue128;" );
                                            else
                                                if ( model.color.equalsIgnoreCase( "lightgrey" ) )
                                                    rtf.print( "\\red192\\green192\\blue192;" );
                                                else
                                                    if ( model.color.equalsIgnoreCase( "lightpurple" ) )
                                                        rtf.print( "\\red255\\green0\\blue255;" );
                                                    else
                                                        if ( model.color.equalsIgnoreCase( "lightblue" ) )
                                                            rtf.print( "\\red0\\green0\\blue255;" );
                                                        else
                                                            if ( model.color.equalsIgnoreCase( "lightred" ) )
                                                                rtf.print( "\\red255\\green0\\blue0;" );
                                                            else
                                                                if ( model.color.equalsIgnoreCase( "lightgreen" ) )
                                                                    rtf.print( "\\red0\\green255\\blue0;" );
                                                                else
                                                                    if ( model.color.equalsIgnoreCase( "white" ) )
                                                                        rtf.print( "\\red255\\green255\\blue255;" );
                                                                    else
                                                                    {
                                                                        java.util.StringTokenizer str = new java.util.StringTokenizer( model.color, "," );

                                                                        while ( str.hasMoreTokens() )
                                                                            rtf.print( "\\" + str.nextToken() );

                                                                        rtf.print( ";" );
                                                                    }
    }

    /**
     * This method is used to create a font entry into the font table.
     */
    private void createBackColorEntry( org.openorb.compiler.doc.rtf.model model, java.io.PrintWriter rtf )
    {
        if ( model.backcolor == null )
            return;

        nb_color_entry++;

        if ( model.backcolor.equalsIgnoreCase( "black" ) )
            rtf.print( "\\red0\\green0\\blue0;" );
        else
            if ( model.backcolor.equalsIgnoreCase( "green" ) )
                rtf.print( "\\red0\\green128\\blue0;" );
            else
                if ( model.backcolor.equalsIgnoreCase( "red" ) )
                    rtf.print( "\\red128\\green0\\blue0;" );
                else
                    if ( model.backcolor.equalsIgnoreCase( "blue" ) )
                        rtf.print( "\\red0\\green0\\blue128;" );
                    else
                        if ( model.backcolor.equalsIgnoreCase( "yellow" ) )
                            rtf.print( "\\red128\\green128\\blue0;" );
                        else
                            if ( model.backcolor.equalsIgnoreCase( "lightyellow" ) )
                                rtf.print( "\\red255\\green255\\blue0;" );
                            else
                                if ( model.backcolor.equalsIgnoreCase( "cyan" ) )
                                    rtf.print( "\\red0\\green255\\blue255;" );
                                else
                                    if ( model.backcolor.equalsIgnoreCase( "brown" ) )
                                        rtf.print( "\\red128\\green128\\blue0;" );
                                    else
                                        if ( model.backcolor.equalsIgnoreCase( "grey" ) )
                                            rtf.print( "\\red128\\green128\\blue128;" );
                                        else
                                            if ( model.backcolor.equalsIgnoreCase( "purple" ) )
                                                rtf.print( "\\red128\\green0\\blue128;" );
                                            else
                                                if ( model.backcolor.equalsIgnoreCase( "lightgrey" ) )
                                                    rtf.print( "\\red192\\green192\\blue192;" );
                                                else
                                                    if ( model.backcolor.equalsIgnoreCase( "lightpurple" ) )
                                                        rtf.print( "\\red255\\green0\\blue255;" );
                                                    else
                                                        if ( model.backcolor.equalsIgnoreCase( "lightblue" ) )
                                                            rtf.print( "\\red0\\green0\\blue255;" );
                                                        else
                                                            if ( model.backcolor.equalsIgnoreCase( "lightred" ) )
                                                                rtf.print( "\\red255\\green0\\blue0;" );
                                                            else
                                                                if ( model.backcolor.equalsIgnoreCase( "lightgreen" ) )
                                                                    rtf.print( "\\red0\\green255\\blue0;" );
                                                                else
                                                                    if ( model.backcolor.equalsIgnoreCase( "white" ) )
                                                                        rtf.print( "\\red255\\green255\\blue255;" );
                                                                    else
                                                                    {
                                                                        java.util.StringTokenizer str = new java.util.StringTokenizer( model.backcolor, "," );

                                                                        while ( str.hasMoreTokens() )
                                                                            rtf.print( "\\" + str.nextToken() );

                                                                        rtf.print( ";" );
                                                                    }
    }

    /**
     * This method is used to create a RTF header.
     */
    private void createRTFHeader( java.io.PrintWriter rtf )
    {
        rtf.print( "{" );    // Begin RTF
        rtf.print( "\\rtf1" );  // Version is RTF 1.0
        rtf.print( "\\ainsi" );  // Character set
        rtf.print( "\\deff0" );  // Default font has number 0

        // Font section

        rtf.print( "{" );
        rtf.print( "\\fonttbl" );

        section_title_model.fontNumber = "0";
        createFontEntry( section_title_model, rtf );

        section_id_model.fontNumber = "1";
        createFontEntry( section_id_model, rtf );

        summary_title_model.fontNumber = "2";
        createFontEntry( summary_title_model, rtf );

        summary_entry_name_model.fontNumber = "3";
        createFontEntry( summary_entry_name_model, rtf );

        summary_entry_desc_model.fontNumber = "4";
        createFontEntry( summary_entry_desc_model, rtf );

        section_hierarchy_model.fontNumber = "5";
        createFontEntry( section_hierarchy_model, rtf );

        section_desc_model.fontNumber = "6";
        createFontEntry( section_desc_model, rtf );

        detail_title_model.fontNumber = "7";
        createFontEntry( detail_title_model, rtf );

        detail_desc_model.fontNumber = "8";
        createFontEntry( detail_desc_model, rtf );

        detail_comment_model.fontNumber = "9";
        createFontEntry( detail_comment_model, rtf );

        detail_id_model.fontNumber = "10";
        createFontEntry( detail_id_model, rtf );

        detail_java_model.fontNumber = "11";
        createFontEntry( detail_java_model, rtf );

        detail_highlight_model.fontNumber = "12";
        createFontEntry( detail_highlight_model, rtf );

        summary_inher_title_model.fontNumber = "13";
        createFontEntry( summary_inher_title_model, rtf );

        summary_inher_desc_model.fontNumber = "14";
        createFontEntry( summary_inher_desc_model, rtf );

        summary_inher_list_model.fontNumber = "15";
        createFontEntry( summary_inher_list_model, rtf );

        header_model.fontNumber = "16";
        createFontEntry( header_model, rtf );

        footer_model.fontNumber = "17";
        createFontEntry( footer_model, rtf );

        footer_model.fontNumber = "18";
        createFontEntry( title_model, rtf );

        rtf.print( "}" );

        // Color section

        rtf.print( "{" );
        rtf.print( "\\colortbl;" );

        section_title_model.colorNumber = "" + nb_color_entry;
        createColorEntry( section_title_model, rtf );

        section_title_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( section_title_model, rtf );

        section_id_model.colorNumber = "" + nb_color_entry;
        createColorEntry( section_id_model, rtf );

        section_id_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( section_id_model, rtf );

        section_hierarchy_model.colorNumber = "" + nb_color_entry;
        createColorEntry( section_hierarchy_model, rtf );

        section_hierarchy_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( section_hierarchy_model, rtf );

        section_desc_model.colorNumber = "" + nb_color_entry;
        createColorEntry( section_desc_model, rtf );

        section_desc_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( section_desc_model, rtf );

        summary_title_model.colorNumber = "" + nb_color_entry;
        createColorEntry( summary_title_model, rtf );

        summary_title_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( summary_title_model, rtf );

        summary_entry_name_model.colorNumber = "" + nb_color_entry;
        createColorEntry( summary_entry_name_model, rtf );

        summary_entry_name_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( summary_entry_name_model, rtf );

        summary_entry_desc_model.colorNumber = "" + nb_color_entry;
        createColorEntry( summary_entry_desc_model, rtf );

        summary_entry_desc_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( summary_entry_desc_model, rtf );

        summary_inher_title_model.colorNumber = "" + nb_color_entry;
        createColorEntry( summary_inher_title_model, rtf );

        summary_inher_title_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( summary_inher_title_model, rtf );

        summary_inher_desc_model.colorNumber = "" + nb_color_entry;
        createColorEntry( summary_inher_desc_model, rtf );

        summary_inher_desc_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( summary_inher_desc_model, rtf );

        summary_inher_list_model.colorNumber = "" + nb_color_entry;
        createColorEntry( summary_inher_list_model, rtf );

        summary_inher_list_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( summary_inher_list_model, rtf );

        detail_title_model.colorNumber = "" + nb_color_entry;
        createColorEntry( detail_title_model, rtf );

        detail_title_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( detail_title_model, rtf );

        detail_desc_model.colorNumber = "" + nb_color_entry;
        createColorEntry( detail_desc_model, rtf );

        detail_desc_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( detail_desc_model, rtf );

        detail_comment_model.colorNumber = "" + nb_color_entry;
        createColorEntry( detail_comment_model, rtf );

        detail_comment_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( detail_comment_model, rtf );

        detail_id_model.colorNumber = "" + nb_color_entry;
        createColorEntry( detail_id_model, rtf );

        detail_id_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( detail_id_model, rtf );

        detail_java_model.colorNumber = "" + nb_color_entry;
        createColorEntry( detail_java_model, rtf );

        detail_java_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( detail_java_model, rtf );

        detail_highlight_model.colorNumber = "" + nb_color_entry;
        createColorEntry( detail_highlight_model, rtf );

        detail_highlight_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( detail_highlight_model, rtf );

        header_model.colorNumber = "" + nb_color_entry;
        createColorEntry( header_model, rtf );

        header_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( header_model, rtf );

        footer_model.colorNumber = "" + nb_color_entry;
        createColorEntry( footer_model, rtf );

        footer_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( footer_model, rtf );

        title_model.colorNumber = "" + nb_color_entry;
        createColorEntry( title_model, rtf );

        title_model.backgroundNumber = "" + nb_color_entry;
        createBackColorEntry( title_model, rtf );

        rtf.print( "}" );
    }

    /**
     * Inverse prefix : omg.org -> org.omg
     */
    private String inversedPrefix ( String prefix )
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
     * This method returns a full name for a IDL item.
     */
    private String fullname ( IdlObject obj )
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
            desc = desc + "{" + getModel( detail_highlight_model ) + fullname( obj ) + "}";
            break;

        case IdlType.e_typedef :
            obj.reset();
            translateType( obj.current(), desc, name, write, current );
            break;

        case IdlType.e_sequence :
            desc = desc + "sequence<";
            desc = translateType( obj.current(), desc, name, false, current );
            desc = desc + ">";
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
            {
                desc = desc + "{" + getModel( detail_highlight_model ) + fullname( ( ( IdlIdent ) obj ).internalObject() ) + "}";
            }

            break;

        case IdlType.e_value_box :

            if ( ( ( IdlValueBox ) obj ).simple() )
                desc = desc + fullname( obj );
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
    private String translateParameter( IdlObject obj, String desc )
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
     * Return the java name for an IDL type
     */
    private String getJavaName( IdlObject obj )
    {
        String desc = "";
        IdlSimple simple = null;

        switch ( obj.kind() )
        {

        case IdlType.e_simple :
            simple = ( IdlSimple ) obj;

            switch ( simple.internal() )
            {

            case Token.t_void :
                desc = "void";
                break;

            case Token.t_float :
                desc = "float";
                break;

            case Token.t_double :
                desc = "double";
                break;

            case Token.t_short :
                desc = "short";
                break;

            case Token.t_ushort :
                desc = "short";
                break;

            case Token.t_long :
                desc = "int";
                break;

            case Token.t_ulong :
                desc = "int";
                break;

            case Token.t_longlong :
                desc = "long";
                break;

            case Token.t_ulonglong :
                desc = "long";
                break;

            case Token.t_char :
                desc = "char";
                break;

            case Token.t_wchar :
                desc = "char";
                break;

            case Token.t_boolean :
                desc = "boolean";
                break;

            case Token.t_octet :
                desc = "byte";
                break;

            case Token.t_any :
                desc = "org.omg.CORBA.Any";
                break;

            case Token.t_typecode :
                desc = "org.omg.CORBA.TypeCode";
                break;

            case Token.t_object :
                desc = "org.omg.CORBA.Object";
                break;

            case Token.t_ValueBase :
                desc = "org.omg.CORBA.ValueBase";
                break;
            }

            break;

        case IdlType.e_fixed :
            desc = "java.lang.BidDecimal";
            break;

        case IdlType.e_string :
            desc = "java.lang.String";
            break;

        case IdlType.e_wstring :
            desc = "java.lang.String";
            break;

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_enum :

        case IdlType.e_interface :

        case IdlType.e_forward_interface :

        case IdlType.e_exception :

        case IdlType.e_native :

        case IdlType.e_value :

        case IdlType.e_attribute :

        case IdlType.e_operation :

        case IdlType.e_state_member :
            desc = fullname( obj );
            break;

        case IdlType.e_typedef :
            obj.reset();
            desc = getJavaName( obj.current() );
            break;

        case IdlType.e_sequence :

        case IdlType.e_array :
            desc = getJavaName( obj.current() );
            desc = desc + "[]";
            break;

        case IdlType.e_const :
            desc = fullname( obj ) + ".value";
            break;

        case IdlType.e_ident :

            if ( ( ( IdlIdent ) obj ).internalObject().name().equals( "TypeCode" ) )
                desc = "org.omg.CORBA.TypeCode";
            else
                desc = getJavaName( ( ( IdlIdent ) obj ).internalObject() );

            break;

        case IdlType.e_value_box :
            if ( ( ( IdlValueBox ) obj ).simple() )
                desc = fullname( obj );
            else
            {
                obj.reset();
                desc = getJavaName( obj.current() );
            }

            break;
        }

        return desc;
    }

    /**
     * This method is used to create a RTF footer.
     */
    private void createRTFFooter( java.io.PrintWriter rtf )
    {
        rtf.print( "}" );    // End RTF
    }

    /**
     * This method is used to add an entry in the index
     */
    private void write_index_entry( String entry, String sub, String see, java.io.PrintWriter rtf )
    {
        rtf.print( "{\\xe {\\v " + entry + "}" );

        if ( sub != null )
            rtf.print( "{\\rxe " + sub + "}" );


        if ( see != null )
            rtf.print( "{\\txe see " + see + "}" );

        rtf.print( "}" );
    }

    /**
     * This section is used to write a section title
     */
    private void write_section_title( String title, IdlObject obj, java.io.PrintWriter rtf )
    {
        rtf.print( "{" );

        write_model( section_title_model, rtf );

        rtf.println( title );

        rtf.println( "\\par }" );

        write_index_entry( fullname( obj ), null, null, rtf );

        rtf.println( "{\\pard}" );

        rtf.print( "{" );

        write_model( section_id_model, rtf );

        rtf.println( "\\par" );
        rtf.println( "CORBA ID = " + obj.getId() );

        rtf.println( "\\par }" );
        rtf.println( "{\\par\\pard}" );
    }

    /**
     * This method tests if an object is deprecated
     */
    private boolean is_deprecated( IdlObject obj )
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
     * This section is used to write a section title
     */
    private void write_detail_title( String title, IdlObject obj, java.io.PrintWriter rtf )
    {
        rtf.print( "{\\li567" );

        write_model( detail_title_model, rtf );

        rtf.println( title );

        rtf.println( "\\par }" );

        write_index_entry( obj.name(), null, fullname( obj.upper() ), rtf );

        rtf.println( "{\\pard}" );

        rtf.print( "{" );

        write_model( detail_id_model, rtf );

        rtf.println( "CORBA ID = " + obj.getId() );

        rtf.println( "\\par }" );
        rtf.println( "{\\pard}" );

        rtf.print( "{" );

        write_model( detail_java_model, rtf );

        rtf.println( "JAVA NAME = " + getJavaName( obj ) );

        rtf.println( "\\par }" );
        rtf.println( "{\\par}\\pard" );
    }

    /**
     * This section is used to write a small detail title
     */
    private void write_smalldetail_title( String title, IdlObject obj, java.io.PrintWriter rtf )
    {
        rtf.print( "{\\li567" );

        write_model( detail_title_model, rtf );

        rtf.println( title );

        rtf.println( "\\par }" );

        if ( obj.upper().upper() != null )
        {
            write_index_entry( obj.name(), null, fullname( obj.upper() ), rtf );
        }

        rtf.println( "{\\pard}" );

        rtf.print( "{" );

        write_model( detail_id_model, rtf );

        rtf.println( "DETAILS AT = " + fullname( obj ) );

        rtf.println( "\\par }" );
        rtf.println( "{\\par}\\pard" );
    }

    /**
     * This method sorts all descriptions.
     */
    private IdlObject [] sort_description_by_name( java.util.Vector list )
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
     * This method sorts the content of an IDL type
     */
    private void sort_type_content( IdlObject obj, org.openorb.compiler.doc.html.content c, boolean limit )
    {
        if ( c == null )
            c = new org.openorb.compiler.doc.html.content();

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
    private org.openorb.compiler.doc.html.content get_sorted_content( IdlObject obj, boolean limit )
    {
        org.openorb.compiler.doc.html.content ret = new org.openorb.compiler.doc.html.content();

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
     * This method returns a summary for the object description
     */
    private String get_summary_description( IdlObject obj )
    {
        IdlComment comment = obj.getComment();
        String description = null;

        if ( comment != null )
        {
            description = comment.get_description();

            if ( description != null )
            {
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
     * This method is used to add an array
     */
    private void write_begin_table( String caption, java.io.PrintWriter rtf )
    {
        rtf.print( "\\li709 " );

        rtf.print( "\\trowd " );

        rtf.print( "\\trgaph70\\trrh296\\trleft567\\trkeep " );

        write_cell_border( summary_title_model, rtf );

        write_cell_color( summary_title_model, rtf );

        rtf.print( "\\cellx9498\\pard \\intbl" );

        rtf.print( "{" );

        write_cell_model( summary_title_model, rtf );

        rtf.print( caption );

        rtf.print( "\\cell}" );

        rtf.print( "{\\row}" );
    }

    /**
     * This method is used to add an array
     */
    private void write_begin_table_inheritance( String caption, java.io.PrintWriter rtf )
    {
        rtf.print( "\\li709 " );

        rtf.print( "\\trowd " );

        rtf.print( "\\trgaph70\\trrh296\\trleft567\\trkeep " );

        write_cell_border( summary_inher_title_model, rtf );

        write_cell_color( summary_inher_title_model, rtf );

        rtf.print( "\\cellx9498\\pard \\intbl" );

        rtf.print( "{" );

        write_cell_model( summary_inher_title_model, rtf );

        rtf.print( caption );

        rtf.print( "\\cell}" );

        rtf.print( "{\\row}" );
    }

    /**
     * This method is used to add an entry into an array
     */
    private void write_table_entry( String entry, String desc, java.io.PrintWriter rtf )
    {
        rtf.print( "\\li709 " );

        rtf.print( "\\trowd " );

        rtf.print( "\\trgaph70\\trrh286\\trleft567\\trkeep " );

        write_cell_border( summary_entry_name_model, rtf );

        write_cell_color( summary_entry_name_model, rtf );

        rtf.print( " \\cellx2693\\clvertalt " );

        write_cell_border( summary_entry_desc_model, rtf );

        write_cell_color( summary_entry_desc_model, rtf );

        rtf.print( "\\cellx9498\\pard \\fi-72\\li72\\intbl" );

        rtf.print( "{" );

        write_cell_model( summary_entry_name_model, rtf );

        rtf.print( entry );

        rtf.print( "\\cell} " );

        rtf.print( "\\fi-72\\li72\\intbl {" );

        write_cell_model( summary_entry_desc_model, rtf );

        rtf.print( desc );

        rtf.print( "\\cell}" );

        rtf.print( "{\\row}" );
    }

    /**
     * This method is used to add an entry into an array
     */
    private void write_one_cell( String desc, java.io.PrintWriter rtf )
    {
        rtf.print( "\\li709 " );

        rtf.print( "\\trowd " );

        rtf.print( "\\trgaph70\\trrh286\\trleft567\\trkeep " );

        write_cell_border( summary_inher_list_model, rtf );

        write_cell_color( summary_inher_list_model, rtf );

        rtf.print( "\\cellx9498\\pard \\intbl" );

        rtf.print( "{" );

        write_cell_model( summary_inher_list_model, rtf );

        rtf.print( desc );

        rtf.print( "\\cell}" );

        rtf.print( "{\\row}" );
    }


    /**
     * This method is used to add an entry into an array
     */
    private void write_end_table( java.io.PrintWriter rtf )
    {
        rtf.println( "\\pard \\plain { \\par \\pard }" );
    }

    /**
     * This method translates into a table all sub types details of the current object
     */
    private void translateSubTypeDetails( IdlObject[] list,
          java.io.PrintWriter output, String name )
    {
        write_begin_table( name, output );
        write_end_table( output );

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
                translateTypeDef( list[ i ], output );
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

            case IdlType.e_interface :
                translateInterfaceDetail( list[ i ], output );
                break;

            case IdlType.e_module :
                translateModuleDetail( list[ i ], output );
                break;

            case IdlType.e_value :
                translateValueDetail( list[ i ], output );
                break;
            }
        }
    }

    /**
     * This method translates into a table all sub types summary of the current object
     */
    private void translateSubTypeSummary( IdlObject[] list, java.io.PrintWriter rtf, String name )
    {
        String desc;
        write_begin_table( name, rtf );
        for ( int i = 0; i < list.length; i++ )
        {
            desc = get_summary_description( list[ i ] );
            if ( is_deprecated( list[ i ] ) )
            {
                desc = "{\b deprecated} - " + desc;
            }
            write_table_entry( list[ i ].name(), desc, rtf );
        }
        write_end_table( rtf );
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
        org.openorb.compiler.doc.html.content c = get_sorted_content( obj, true );

        // --
        // Summary
        // --

        if ( ( type == 1 ) || ( type == 3 ) )
        {
            if ( c._sorted_module.length != 0 )
                translateSubTypeSummary( c._sorted_module, output, "Module Summary" );

            if ( c._sorted_interface.length != 0 )
                translateSubTypeSummary( c._sorted_interface, output, "Interface Summary" );

            if ( c._sorted_valuetype.length != 0 )
                translateSubTypeSummary( c._sorted_valuetype, output, "ValueType Summary" );

            if ( c._sorted_valuebox.length != 0 )
                translateSubTypeSummary( c._sorted_valuebox, output, "ValueBox Summary" );

            if ( c._sorted_exception.length != 0 )
                translateSubTypeSummary( c._sorted_exception, output, "Exception Summary" );

            if ( c._sorted_struct.length != 0 )
                translateSubTypeSummary( c._sorted_struct, output, "Struct Summary" );

            if ( c._sorted_union.length != 0 )
                translateSubTypeSummary( c._sorted_union, output, "Union Summary" );

            if ( c._sorted_enum.length != 0 )
                translateSubTypeSummary( c._sorted_enum, output, "Enum Summary" );

            if ( c._sorted_typedef.length != 0 )
                translateSubTypeSummary( c._sorted_typedef, output, "TypeDef Summary" );

            if ( c._sorted_const.length != 0 )
                translateSubTypeSummary( c._sorted_const, output, "Const Summary" );

            if ( c._sorted_native.length != 0 )
                translateSubTypeSummary( c._sorted_native, output, "Native Summary" );

            if ( c._sorted_operation.length != 0 )
                translateSubTypeSummary( c._sorted_operation, output, "Operation Summary" );

            if ( c._sorted_attribute.length != 0 )
                translateSubTypeSummary( c._sorted_attribute, output, "Attribute Summary" );

            if ( c._sorted_member.length != 0 )
                translateSubTypeSummary( c._sorted_member, output, "Member Summary" );

            if ( c._sorted_factory.length != 0 )
                translateSubTypeSummary( c._sorted_factory, output, "Factory Summary" );
        }

        // --
        // Details
        // --

        if ( ( type == 2 ) || ( type == 3 ) )
        {
            if ( c._sorted_module.length != 0 )
                translateSubTypeDetails( c._sorted_module, output, "Module Details" );

            if ( c._sorted_interface.length != 0 )
                translateSubTypeDetails( c._sorted_interface, output, "Interface Details" );

            if ( c._sorted_valuetype.length != 0 )
                translateSubTypeDetails( c._sorted_valuetype, output, "ValueType Details" );

            if ( c._sorted_valuebox.length != 0 )
                translateSubTypeDetails( c._sorted_valuebox, output, "ValueBox Details" );

            if ( c._sorted_exception.length != 0 )
                translateSubTypeDetails( c._sorted_exception, output, "Exception Details" );

            if ( c._sorted_struct.length != 0 )
                translateSubTypeDetails( c._sorted_struct, output, "Struct Details" );

            if ( c._sorted_union.length != 0 )
                translateSubTypeDetails( c._sorted_union, output, "Union Details" );

            if ( c._sorted_enum.length != 0 )
                translateSubTypeDetails( c._sorted_enum, output, "Enum Details" );

            if ( c._sorted_typedef.length != 0 )
                translateSubTypeDetails( c._sorted_typedef, output, "TypeDef Details" );

            if ( c._sorted_const.length != 0 )
                translateSubTypeDetails( c._sorted_const, output, "Const Details" );

            if ( c._sorted_native.length != 0 )
                translateSubTypeDetails( c._sorted_native, output, "Native Details" );

            if ( c._sorted_operation.length != 0 )
                translateSubTypeDetails( c._sorted_operation, output, "Operation Details" );

            if ( c._sorted_attribute.length != 0 )
                translateSubTypeDetails( c._sorted_attribute, output, "Attribute Details" );

            if ( c._sorted_member.length != 0 )
                translateSubTypeDetails( c._sorted_member, output, "Member Details" );

            if ( c._sorted_factory.length != 0 )
                translateSubTypeDetails( c._sorted_factory, output, "Factory Details" );
        }
    }

    /**
     * This method returns an object description
     */
    private String get_description( IdlObject obj )
    {
        IdlComment comment = obj.getComment();

        if ( comment != null )
        {
            if ( comment.get_description() != null )
                return comment.get_description();
        }

        return "";
    }

    /**
     * This method returns a comment section
     */
    private IdlCommentSection [] get_sections( IdlCommentSection [] src, int section_type )
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
     * This method translates a hierarchy between contents
     */
    private void translateHierarchy( IdlObject obj, java.io.PrintWriter rtf )
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

        rtf.print( "{\\li709 " );

        write_model( section_hierarchy_model, rtf );

        if ( v.size() > 1 )
        {
            String dec = "";

            for ( int i = v.size() - 1; i >= 0; i-- )
            {
                dec = dec + "   ";

                if ( i == 0 )
                    rtf.println( "{\\b " + fullname( ( IdlObject ) v.elementAt( i ) ) + "}\\par" );
                else
                    rtf.println( fullname( ( IdlObject ) v.elementAt( i ) ) + "\\par" );

                if ( i != 0 )
                {
                    rtf.println( dec + "|\\par" );
                    rtf.print( dec + "+--" );
                }
            }

            rtf.print( "\\par" );
        }

        rtf.println( "} \\pard\\plain " );
    }

    /**
     * This method is used to remove a Tabulation
     */
    private String removeTab( String str )
    {
        byte [] b = new byte[ str.length() ];

        int j = 0;

        for ( int i = 0; i < b.length; i++ )
            if ( str.charAt( i ) != '\t' )
                b[ j++ ] = ( byte ) str.charAt( i );

        return new String( b, 0, j );
    }

    /**
     * This method writes a comment section
     */
    private void write_section( IdlCommentSection [] sections, int section_type, String section_title, java.io.PrintWriter output, boolean highlight_first )
    {
        IdlCommentSection [] section = get_sections( sections, section_type );
        String desc = "";
        String first = "";

        if ( section.length != 0 )
        {
            output.println( "\\li800 {\\b " + section_title + "\\par}" );
            output.println( "\\li1000 {" );

            for ( int i = 0; i < section.length; i++ )
            {
                desc = section[ i ].get_description().trim();

                if ( highlight_first )
                {
                    output.print( "{" );

                    write_model( detail_highlight_model, output );

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

                        output.print( first );
                    }

                    output.print( "} - " );
                }

                output.println( removeTab( desc ) + "\\par" );

            }

            output.println( "}{\\par}\\pard" );
        }
    }

    /**
     * This method writes an object description
     */
    private void write_description( IdlObject obj, java.io.PrintWriter output, org.openorb.compiler.doc.rtf.model model )
    {
        IdlComment comment = obj.getComment();

        output.print( "{\\li567" );

        write_model( model, output );

        output.println( get_description( obj ).trim() + "\\par\\par" );

        if ( comment != null )
        {
            IdlCommentSection [] sections = comment.get_sections();

            write_section( sections, IdlCommentField._param_field, "Parameter", output, true );

            write_section( sections, IdlCommentField._return_field, "Return", output, false );

            write_section( sections, IdlCommentField._exception_field, "Exception", output, true );

            write_section( sections, IdlCommentField._see_field, "See", output, true );

            write_section( sections, IdlCommentField._deprecated_field, "Deprecated", output, false );
        }

        output.println( "\\par} \\pard\\plain" );
    }

    /**
     * This method translates an Enum
     */
    private void translateEnum( IdlObject obj, java.io.PrintWriter output )
    {
        write_detail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        IdlEnumMember member = null;

        output.print( "enum " + obj.name() + "\\par" );

        output.print( "\\{\\par" );

        output.println( "{\\li800" );

        obj.reset();

        while ( obj.end() != true )
        {

            member = ( IdlEnumMember ) obj.current();

            output.print( member.name() );

            obj.next();

            if ( !obj.end() )
                output.print( "," );

            output.println( "\\par" );
        }

        output.print( "}" );

        output.print( "\\}\\par" );

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );
    }

    /**
     * This method translates a Struct
     */
    private void translateStruct( IdlObject obj, java.io.PrintWriter output )
    {
        write_detail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        IdlStructMember member = null;

        output.print( "struct " + obj.name() + "\\par" );

        output.print( "\\{\\par" );

        output.println( "{\\li800" );

        obj.reset();

        while ( obj.end() != true )
        {
            member = ( IdlStructMember ) obj.current();

            member.reset();

            output.print( translateType( member.current(), "", member.name(), true, obj ) );

            obj.next();

            output.print( ";" );

            output.println( "\\par" );
        }

        output.print( "}" );

        output.print( "\\}\\par" );

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );
    }

    /**
     * This method translates an Exception
     */
    private void translateException( IdlObject obj, java.io.PrintWriter output )
    {
        write_detail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        IdlStructMember member = null;

        output.print( "exception " + obj.name() + "\\par" );

        output.print( "\\{\\par" );

        output.println( "{\\li800" );

        obj.reset();

        while ( obj.end() != true )
        {
            member = ( IdlStructMember ) obj.current();

            member.reset();

            output.print( translateType( member.current(), "", member.name(), true, obj ) );

            obj.next();

            output.print( ";" );

            output.println( "\\par" );
        }

        output.print( "}" );

        output.print( "\\}\\par" );

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );
    }

    /**
     * This method translates an Union
     */
    private void translateUnion( IdlObject obj, java.io.PrintWriter output )
    {
        write_detail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        IdlUnionMember member = null;
        int default_index, index;

        obj.reset();
        member = ( IdlUnionMember ) obj.current();
        member.reset();

        output.print( "union " + obj.name() + " switch ( " + translateType( member.current(), "", member.name(), false, obj ) + " )" + "\\par" );

        output.print( "\\{\\par" );

        default_index = ( ( IdlUnion ) obj ).index();
        index = 0;
        obj.next();

        while ( obj.end() != true )
        {
            member = ( IdlUnionMember ) obj.current();

            output.print( "\\li800" );

            output.print( "{" );

            member.reset();

            if ( index == default_index )
            {
                output.print( "default : \\par }" );
            }
            else
            {
                output.print( "case " + adaptExpression( member.getExpression() ) + ": \\par }" );
            }

            output.println( "\\li1200 {" );
            output.print( translateType( member.current(), "", member.name(), true, obj ) );

            obj.next();

            output.print( ";" );

            output.print( "\\par }" );

            index++;
        }

        output.print( "\\li567 \\}\\par" );

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );
    }

    /**
     * This method corrects an identifier to be a valid IDL identifier
     */
    private String correctIdentifier( String expr )
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
    private String adaptExpression( String expr )
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
     * This method translates a factory member
     */
    public void translateFactory( IdlObject obj, java.io.PrintWriter output )
    {
        write_detail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        output.print( "factory " + obj.name() + "(" );

        obj.reset();

        while ( obj.end() != true )
        {
            output.print( "in " );
            obj.current().reset();
            output.print( translateType( obj.current().current(), "", obj.current().name(), true, obj ) );
            obj.next();

            if ( obj.end() != true )
                output.print( ", " );
        }

        output.print( ");" );

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );

    }

    /**
     * This method translates a state member
     */
    public void translateStateMember( IdlObject obj, java.io.PrintWriter output )
    {
        IdlStateMember member = null;

        write_detail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        member = ( IdlStateMember ) obj;
        member.reset();

        if ( member.public_member() )
            output.print( "public " );
        else
            output.print( "private " );

        output.print( translateType( member.current(), "", member.name(), true , obj ) );

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );
    }

    /**
     * This method translates a Constant
     */
    private void translateConstant( IdlObject obj, java.io.PrintWriter output )
    {
        write_detail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        output.print( "Const " );

        obj.reset();
        String desc = translateType( obj.current(), "", obj.name(), false, obj );

        output.print( desc + " " + obj.name() + " = " + adaptExpression( ( ( IdlConst ) obj ).expression() ) + ";\\par" );

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );
    }

    /**
     * This method translates a typedef
     */
    private void translateTypeDef( IdlObject obj, java.io.PrintWriter output )
    {
        write_detail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        output.print( "typedef " );

        obj.reset();
        String desc = translateType( obj.current(), "", obj.name(), true, obj );

        output.print( desc + ";\\par" );

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );
    }

    /**
     * This method translates a value box
     */
    private void translateValueBox( IdlObject obj, java.io.PrintWriter output )
    {
        write_detail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        output.print( "valuetype " + obj.name() );

        obj.reset();
        String desc = translateType( obj.current(), "", obj.name(), false, obj );

        output.print( " " + desc + ";\\par" );

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );
    }

    /**
     * This method translates a Native
     */
    private void translateNative( IdlObject obj, java.io.PrintWriter output )
    {
        write_detail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        output.print( "native " + obj.name() + "\\par" );

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );
    }

    /**
     * This method translates a detail for an Interface
     */
    private void translateInterfaceDetail( IdlObject obj, java.io.PrintWriter rtf )
    {
        IdlInterface itf = ( IdlInterface ) obj;

        write_smalldetail_title( obj.name(), obj, rtf );

        rtf.print( "{\\li567" );

        write_model( detail_desc_model, rtf );

        if ( itf.abstract_interface() )
            rtf.print( "abstract " );

        rtf.println( "interface " + obj.name() );

        java.util.Vector inheritance = itf.getInheritance();

        if ( inheritance.size() != 0 )
        {
            rtf.print( " :\\par" );

            for ( int i = 0; i < inheritance.size(); i++ )
            {
                rtf.print( "\\li800 { " + fullname( ( ( IdlInterface ) inheritance.elementAt( i ) ) ) );

                if ( ( i + 1 ) < inheritance.size() )
                    rtf.print( "," );

                rtf.println( "\\par}" );
            }
        }
        else
            rtf.print( "\\par" );

        rtf.println( "}{\\par} \\pard" );

        write_description( obj, rtf, detail_comment_model );
    }

    /**
     * This method translates a detail for a Module
     */
    private void translateModuleDetail( IdlObject obj, java.io.PrintWriter output )
    {
        write_smalldetail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        output.print( "module " + obj.name() + "\\par" );

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );
    }

    /**
     * This method translates a detail for a ValueType
     */
    private void translateValueDetail( IdlObject obj, java.io.PrintWriter rtf )
    {
        IdlValue itf = ( IdlValue ) obj;

        write_smalldetail_title( obj.name(), obj, rtf );

        rtf.print( "{\\li567" );

        write_model( detail_desc_model, rtf );

        if ( itf.abstract_value() )
            rtf.print( "abstract " );

        if ( itf.custom_value() )
            rtf.print( "abstract " );

        rtf.println( "valuetype " + obj.name() );

        java.util.Vector inheritance = itf.getInheritanceList();

        if ( inheritance.size() != 0 )
        {
            rtf.print( " :\\par" );

            for ( int i = 0; i < inheritance.size(); i++ )
            {
                rtf.print( "\\li800 { " );

                if ( ( ( IdlValueInheritance ) inheritance.elementAt( i ) ).truncatable_member() )
                    rtf.print( "truncatable " );

                rtf.print( fullname( ( ( IdlInterface ) inheritance.elementAt( i ) ) ) );

                if ( ( i + 1 ) < inheritance.size() )
                    rtf.print( "," );

                rtf.println( "\\par}" );
            }
        }
        else
            rtf.print( "\\par" );

        rtf.println( "}{\\par} \\pard" );

        write_description( obj, rtf, detail_comment_model );
    }

    /**
     * This method translates an Attribute
     */
    public void translateAttribute( IdlObject obj, java.io.PrintWriter output )
    {
        IdlAttribute attr = ( IdlAttribute ) obj;
        attr.reset();

        write_detail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        if ( attr.readOnly() )
            output.print( "readonly " );

        output.print( "attribute " );

        output.println( translateType( attr.current(), "", attr.name(), true, obj ) + "\\par" );

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );
    }

    /**
     * This method translates an Operation
     */
    public void translateOperation( IdlObject obj, java.io.PrintWriter output )
    {
        IdlOp op = ( IdlOp ) obj;

        write_detail_title( obj.name(), obj, output );

        output.print( "{\\li567" );

        write_model( detail_desc_model, output );

        if ( op.oneway() )
            output.print( "oneway " );

        op.reset();

        output.print( translateType( op.current(), "", op.name(), false, obj ) );

        op.next();

        output.print( " " + op.name() + "(" );

        while ( op.end() != true )
        {
            if ( op.current().kind() != IdlType.e_param )
                break;

            output.print( "\\par" );

            output.print( "\\li1000" + translateParameter( op.current(), "" ) );

            op.next();

            if ( op.end() != true )
                if ( op.current().kind() == IdlType.e_param )
                    output.print( ", " );
        }

        output.print( ")\\par" );

        if ( op.end() != true )
            if ( op.current().kind() == IdlType.e_raises )
            {
                output.print( "\\li800{raises (\\par}" );

                IdlRaises raises = ( IdlRaises ) op.current();
                raises.reset();

                while ( raises.end() != true )
                {
                    output.print( "\\li1000 {" + fullname( raises.current() ) );

                    raises.next();

                    if ( raises.end() != true )
                        output.print( ", \\par}" );
                    else
                        output.print( ")\\par}" );
                }

                op.next();
            }

        if ( op.end() != true )
            if ( op.current().kind() == IdlType.e_context )
            {
                output.print( "\\li800{" );

                output.print( "context (" );

                IdlContext ctx = ( IdlContext ) op.current();
                java.util.Vector list = ctx.getValues();
                int index = 0;

                while ( index < list.size() )
                {
                    output.print( "\"" + ( String ) list.elementAt( index ) + "\"" );

                    if ( ( index + 1 ) < list.size() )
                        output.print( ", " );

                    index++;
                }

                output.print( ")\\par}" );
            }

        output.print( "\\par } \\pard" );

        write_description( obj, output, detail_comment_model );
    }

    /**
     * This method translates a Module
     */
    private void translateModule( IdlObject obj, java.io.PrintWriter rtf )
    {
        write_section_title( "Module " + fullname( obj ), obj, rtf );

        translateHierarchy( obj, rtf );

        write_description( obj, rtf, section_desc_model );

        translateContentTable( obj, rtf, 3 );

        rtf.println( "{\\par} \\pard" );

        translateDescription( obj, rtf );
    }

    /**
     * This method create an summary file for the HTML navigation
     */
    public void translateSummary( String title, java.io.PrintWriter rtf )
    {
        rtf.print( "{" );

        write_model( title_model, rtf );

        rtf.print( title );

        rtf.print( "\\par}{\\par\\par\\par}\\pard" );

        translateContentTable( _root, rtf, 3 );

        rtf.print( "{\\par\\par}\\pard" );
    }

    /**
     * This method translates information
     */
    private void translateInfo( java.io.PrintWriter rtf )
    {
        rtf.print( "{" );

        rtf.print( "\\author IDL to RTF" );

        rtf.println( "}" );
    }

    /**
     * This method translates an Header
     */
    private void translateHeader( String header, java.io.PrintWriter rtf )
    {
        rtf.print( "{\\header " );

        write_model( header_model, rtf );

        rtf.println( "{" + header + "}{\\par } }" );
    }

    /**
     * This method translates a footer
     */
    private void translateFooter( String footer, java.io.PrintWriter rtf )
    {
        rtf.print( "{\\footer" );

        write_model( footer_model, rtf );

        rtf.println( "{" + footer + "}{\\par } }" );
    }

    /**
     * This method translates an Interface
     */
    private void translateInterface( IdlObject obj, java.io.PrintWriter rtf )
    {
        String desc = null;
        IdlInterface itf = ( IdlInterface ) obj;

        write_section_title( "Interface " + fullname( obj ), obj, rtf );

        translateHierarchy( obj, rtf );

        write_description( obj, rtf, section_desc_model );

        rtf.print( "{\\li567" );

        write_model( summary_inher_desc_model, rtf );

        if ( itf.abstract_interface() )
            rtf.print( "abstract " );

        rtf.println( "interface " + obj.name() );

        java.util.Vector inheritance = itf.getInheritance();

        if ( inheritance.size() != 0 )
        {
            rtf.print( " :\\par" );

            for ( int i = 0; i < inheritance.size(); i++ )
            {
                rtf.print( "\\li800 { " + fullname( ( ( IdlInterface ) inheritance.elementAt( i ) ) ) );

                if ( ( i + 1 ) < inheritance.size() )
                    rtf.print( "," );

                rtf.println( "\\par}" );
            }
        }
        else
            rtf.print( "\\par" );

        rtf.println( "}{\\par} \\pard" );

        translateContentTable( obj, rtf, 1 );

        IdlInterface inherit = null;

        if ( inheritance.size() != 0 )
        {

            for ( int i = 0; i < inheritance.size(); i++ )
            {
                inherit = ( IdlInterface ) inheritance.elementAt( i );

                if ( inherit.included() == false )
                {
                    write_begin_table_inheritance( "Attributes and operations inherited from " + fullname( inherit ), rtf );

                    desc = "";
                    inherit.reset();

                    while ( inherit.end() != true )
                    {
                        switch ( inherit.current().kind() )
                        {

                        case IdlType.e_operation :

                        case IdlType.e_attribute :

                            if ( !desc.equals( "" ) )
                                desc = desc + ", ";

                            desc = desc + inherit.current().name();

                            break;
                        }

                        inherit.next();
                    }

                    write_one_cell( desc, rtf );
                    write_end_table( rtf );
                }
            }
        }

        translateContentTable( obj, rtf, 2 );

        rtf.println( "{\\par} \\pard" );

        translateDescription( obj, rtf );
    }

    /**
     * This method translates a ValueType
     */
    private void translateValueType( IdlObject obj, java.io.PrintWriter rtf )
    {
        String desc = null;
        IdlValue itf = ( IdlValue ) obj;

        write_section_title( "Valuetype " + fullname( obj ), obj, rtf );

        translateHierarchy( obj, rtf );

        write_description( obj, rtf, section_desc_model );

        rtf.print( "{\\li567" );

        write_model( summary_inher_desc_model, rtf );

        if ( itf.abstract_value() )
            rtf.print( "abstract " );

        if ( itf.custom_value() )
            rtf.print( "abstract " );

        rtf.println( "valuetype " + obj.name() );

        java.util.Vector inheritance = itf.getInheritanceList();

        if ( inheritance.size() != 0 )
        {
            rtf.print( " :\\par" );

            for ( int i = 0; i < inheritance.size(); i++ )
            {
                rtf.print( "\\li800 { " );

                if ( ( ( IdlValueInheritance ) inheritance.elementAt( i ) ).truncatable_member() )
                    rtf.print( "truncatable " );

                rtf.print( fullname( ( ( IdlInterface ) inheritance.elementAt( i ) ) ) );

                if ( ( i + 1 ) < inheritance.size() )
                    rtf.print( "," );

                rtf.println( "\\par}" );
            }
        }
        else
            rtf.print( "\\par" );

        rtf.println( "}{\\par} \\pard" );

        translateContentTable( obj, rtf, 1 );

        IdlValue inherit = null;

        if ( inheritance.size() != 0 )
        {

            for ( int i = 0; i < inheritance.size(); i++ )
            {
                inherit = ( ( IdlValueInheritance ) inheritance.elementAt( i ) ).getValue();

                if ( inherit.included() == false )
                {
                    write_begin_table_inheritance( "Members, attributes and operations inherited from " + fullname( inherit ), rtf );

                    desc = "";
                    inherit.reset();

                    while ( inherit.end() != true )
                    {
                        switch ( inherit.current().kind() )
                        {

                        case IdlType.e_state_member :

                        case IdlType.e_operation :

                        case IdlType.e_attribute :

                            if ( !desc.equals( "" ) )
                                desc = desc + ", ";

                            desc = desc + inherit.current().name();

                            break;
                        }

                        inherit.next();
                    }

                    write_one_cell( desc, rtf );
                    write_end_table( rtf );
                }
            }
        }

        translateContentTable( obj, rtf, 2 );

        rtf.println( "{\\par} \\pard" );

        translateDescription( obj, rtf );
    }

    /**
     * This method translates IDL description to HTML.
     */
    private void translateDescription( IdlObject obj, java.io.PrintWriter rtf )
    {
        obj.reset();

        while ( obj.end() != true )
        {
            if ( obj.current().included() == false )
                switch ( obj.current().kind() )
                {

                case IdlType.e_module :
                    translateModule( obj.current(), rtf );
                    break;

                case IdlType.e_value :
                    translateValueType( obj.current(), rtf );
                    break;

                case IdlType.e_interface :
                    translateInterface( obj.current(), rtf );
                    break;
                }


            obj.next();
        }
    }

    /**
     * This method translates an IDL descriptions in RTF documentations.
     */
    public void translateToRTF( IdlObject obj, String fileName, String title, String header, String footer )
    {
        java.io.File file = null;

        _root = obj;

        // Create RTF file.

        if ( org.openorb.compiler.doc.IdlDoc.outdir != null )
            file = new java.io.File( org.openorb.compiler.doc.IdlDoc.outdir + java.io.File.separatorChar + fileName );
        else
            file = new java.io.File( fileName );

        org.openorb.util.DiffFileOutputStream out = null;

        try
        {
            out = new org.openorb.util.DiffFileOutputStream( file );
        }
        catch ( java.io.IOException ex )
        {
            System.out.println( "Error : Unable to create " + file.getAbsolutePath() );
            System.exit( 0 );
        }

        java.io.PrintWriter rtfFile = new java.io.PrintWriter( out, true );

        // Load style sheet properties.

        loadStyleSheet();

        createRTFHeader( rtfFile );

        // Add info

        translateInfo( rtfFile );

        // Add Header

        translateHeader( header, rtfFile );

        // Add Footer

        translateFooter( footer, rtfFile );

        // Add IDL descriptions

        translateSummary( title, rtfFile );

        translateDescription( obj, rtfFile );

        createRTFFooter( rtfFile );

        // Close RTF file.

        try
        {
            rtfFile.close();
            out.close();
        }
        catch ( java.io.IOException ex )
        { }

    }

}
