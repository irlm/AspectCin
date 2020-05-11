/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openorb.compiler.CompilerProperties;
import org.openorb.compiler.object.IdlArray;
import org.openorb.compiler.object.IdlAttribute;
import org.openorb.compiler.object.IdlComment;
import org.openorb.compiler.object.IdlCommentField;
import org.openorb.compiler.object.IdlCommentSection;
import org.openorb.compiler.object.IdlConst;
import org.openorb.compiler.object.IdlContext;
import org.openorb.compiler.object.IdlEnumMember;
import org.openorb.compiler.object.IdlFactoryMember;
import org.openorb.compiler.object.IdlFixed;
import org.openorb.compiler.object.IdlIdent;
import org.openorb.compiler.object.IdlInclude;
import org.openorb.compiler.object.IdlInterface;
import org.openorb.compiler.object.IdlObject;
import org.openorb.compiler.object.IdlOp;
import org.openorb.compiler.object.IdlParam;
import org.openorb.compiler.object.IdlRaises;
import org.openorb.compiler.object.IdlSequence;
import org.openorb.compiler.object.IdlSimple;
import org.openorb.compiler.object.IdlStateMember;
import org.openorb.compiler.object.IdlString;
import org.openorb.compiler.object.IdlStructMember;
import org.openorb.compiler.object.IdlUnion;
import org.openorb.compiler.object.IdlUnionMember;
import org.openorb.compiler.object.IdlValue;
import org.openorb.compiler.object.IdlValueBox;
import org.openorb.compiler.object.IdlValueInheritance;
import org.openorb.compiler.object.IdlWString;
import org.openorb.compiler.parser.IdlType;
import org.openorb.compiler.parser.Token;
import org.openorb.util.CharacterCache;

/**
 * This class generates all mapping for IDL descriptions.
 *
 * @author Jerome Daniel
 */
public class IdlToJava
{
    public static final String tab = "    ";
    public static final String tab1 = tab;
    public static final String tab2 = tab + tab;
    public static final String tab3 = tab + tab + tab;
    public static final String tab4 = tab + tab + tab + tab;
    public static final String tab5 = tab + tab + tab + tab + tab;
    public static final String tab6 = tab + tab + tab + tab + tab + tab;

    /**
     * File path separator
     */
    private String sep;

    /**
     * Current package
     */
    public String current_pkg = null;

    /**
     * Reference to the compilation graph
     */
    public IdlObject _root = null;

    /**
     * Reference to the initial directory
     */
    private java.io.File initial = null;

    protected CompilerProperties m_cp = null;

    /**
     * Default constructor
     */
    public IdlToJava( CompilerProperties cp )
    {
        sep = System.getProperty("file.separator");
        m_cp = cp;
        current_pkg = adaptToDot(m_cp.getM_packageName());
    }

    /**
     * Retur true if a definition exists for a native type
     *
     * @param obj native object
     * @return true if a definition exists
     */
    public boolean isNativeDefinition( IdlObject obj )
    {
        for ( int i = 0; i < m_cp.getM_nativeDefinition().size(); i++ )
        {
            String s = ( String ) m_cp.getM_nativeDefinition().get( i );

            int index = s.indexOf( ':' );

            String word = s.substring( 0, index );

            if ( obj.name().equals( word ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Print the type translated corresponding to a natibe type definition
     *
     * @param obj  native object
     * @param output file where definition is added
     */
    public void printNativeDefinition(IdlObject obj, java.io.PrintWriter output)
    {
        for (int i = 0; i < m_cp.getM_nativeDefinition().size(); i++)
        {
            String s = (String) m_cp.getM_nativeDefinition().get(i);

            int index = s.indexOf(':');

            String word = s.substring(0, index);

            if (obj.name().equals(word))
            {
                index = s.lastIndexOf(':');

                word = s.substring(index + 1, s.length());

                output.print(word);
            }
        }
    }

    /**
     * Allows to get an access on write to a target file
     *
     * @param writeInto Target file descriptor
     * @return write access
     */
    public java.io.PrintWriter fileAccess(java.io.File writeInto)
    {
        // Deprecated
        // java.io.PrintStream printout = null;
        java.io.PrintWriter printout = null;

        try
        {
            // disable the DiffFileOutput stream
            // if used inside ant
            if ( m_cp.getM_clistener() == null )
            {
                    org.openorb.util.DiffFileOutputStream output = new org.openorb.util.DiffFileOutputStream(writeInto);
                    java.io.DataOutputStream dataout = new java.io.DataOutputStream(output);
                    printout = new PrintWriter(dataout, true);
            }
            else
            {
                printout = new PrintWriter( new FileOutputStream( writeInto ) );
            }
        }
        catch ( java.io.IOException e )
        {
            e.printStackTrace();
        }

        return printout;
    }

    /**
     * Change a prefix order : omg.org -> org.omg
     *
     * @param prefix  the prefix to inverse
     * @return the inversed prefix
     */
    public String inversedPrefix (String prefix)
    {
        int index = 0;
        int previous_index = 0;
        List seq = new ArrayList();
        String inversed = new String("");

        try
        {
            while (index != -1)
            {
                index = prefix.indexOf('.', previous_index);

                if (index != -1)
                {
                    seq.add(new String(prefix.substring(previous_index, index)));
                    previous_index = index + 1;
                }
            }
        }
        catch (StringIndexOutOfBoundsException ex)
        { }

        seq.add(new String(prefix.substring(previous_index, prefix.length())));

        for (int i = seq.size() - 1; i >= 0; i--)
        {
            if (!inversed.equals(""))
                inversed = inversed + ".";

            inversed = inversed + (String) seq.get(i);
        }

        return inversed;
    }

    /**
     * Creates a new Java file
     *
     * @param writeInto the directory where the file must be created
     * @param name  file name (without .java extension)
     * @return write access
     */
    public java.io.PrintWriter newFile(java.io.File writeInto, String name)
    {
        String path;

        if (!writeInto.exists())
            writeInto.mkdirs();

        path = new String(writeInto.getPath() + sep + name + ".java");

        java.io.File file = new java.io.File(path);

        // call the AntTask def for storing into cache the idl dependency
        if ( m_cp.getM_clistener() != null )
        {
            m_cp.getM_clistener().addTargetJavaFile(file);
        }

        return fileAccess(file);
    }

    /**
     * Creates a directory
     *
     * @param name the directory name to create
     * @param writeInto the directory in which the search has to appear
     */
    public java.io.File createDirectory(String name, java.io.File writeInto)
    {
        boolean init = false;
        char [] temp = new char [ name.length() + 20 ];
        int j = 0;
        for (int i = 0; i < name.length(); i++)
        {
            if (name.charAt(i) == '.')
            {
                temp[ j++ ] = sep.charAt(0);
                init = true;
            }
            else
            {
                temp[ j++ ] = name.charAt(i);
            }
        }

        String fname = new String(temp, 0, j);
        String path = null;
        if (writeInto != null)
        {
            path = new String(writeInto.getPath() + sep + fname);
        }
        else
        {
            path = fname;
        }
        java.io.File file = new java.io.File(path);

        if (file.exists() == false)
        {
            file.mkdirs();
        }
        if (init == true)
        {
            m_cp.setM_packageName(fname);
        }
        return file;
    }

    /**
     * Creates the directories corresponding to a CORBA ID prefix
     *
     * @param prefix  the prefix
     * @param writeInto the directory in which the search has to appear
     */
    public java.io.File createPrefixDirectories(String prefix, java.io.File writeInto)
    {
        String path;

        char [] temp = new char [ prefix.length() + 20 ];

        String name = null;

        if (m_cp.getM_reversePrefix())
            name = new String(inversedPrefix(prefix));
        else
            name = prefix;

        int j = 0;

        for (int i = 0; i < name.length(); i++)
        {
            if (name.charAt(i) == '.')
                temp[ j++ ] = sep.charAt(0);
            else
                temp[ j++ ] = name.charAt(i);
        }

        name = new String(temp, 0, j);

        if (writeInto != null)
        {
            path = new String(writeInto.getPath() + sep + name);
        }
        else
            path = name;

        java.io.File file = new java.io.File(path);

        if (file.exists() == false)
            file.mkdirs();

        return file;
    }

    /**
     * Get a write access in a directory
     *
     * @param name the directory name
     * @param writeInto the directory in which the search has to appear
     */
    public java.io.File getDirectory(String name, java.io.File writeInto)
    {
        String path;

        if (writeInto != null)
        {
            path = new String(writeInto.getPath() + sep + name);
        }
        else
            path = name;

        java.io.File file = new java.io.File(path);

        return file;
    }

    /**
     * Get a writing access to a directory corresponding to a CORBA ID prefix
     *
     * @param prefix a object prefix
     * @param writeInto the directory in which the search has to appear
     */
    public java.io.File getPrefixDirectories(String prefix, java.io.File writeInto)
    {
        String path;

        char [] temp = new char [ prefix.length() + 20 ];

        String name = null;

        if (m_cp.getM_reversePrefix())
            name = new String(inversedPrefix(prefix));
        else
            name = prefix;

        int j = 0;

        for (int i = 0; i < name.length(); i++)
        {
            if (name.charAt(i) == '.')
                temp[ j++ ] = sep.charAt(0);
            else
                temp[ j++ ] = name.charAt(i);
        }

        name = new String(temp, 0, j);

        if (writeInto != null)
        {
            path = new String(writeInto.getPath() + sep + name);
        }
        else
            path = name;

        java.io.File file = new java.io.File(path);

        return file;
    }

    /**
     * Construct a package name
     *
     * @param name name of the superior level
     */
    public void addToPkg(IdlObject obj, String name)
    {
        if (m_cp.getM_use_package() == false)
        {
            if (!current_pkg.equals("generated"))
            {
                if (!current_pkg.equals(""))
                    current_pkg = current_pkg + "." + name;
                else
                    current_pkg = name;
            }
            else
                current_pkg = name;
        }
        else
        {
            if (!current_pkg.equals(""))
                current_pkg = current_pkg + "." + name;
            else
                current_pkg = name;
        }
    }

    public void addPackageName(final PrintWriter output)
    {
        if (current_pkg != null)
        {
            if (current_pkg.equals("generated"))
            {
                if (m_cp.getM_use_package())
                {
                    output.println("package " + current_pkg + ";");
                    output.println();
                }
            }
            else
            {
                if (!current_pkg.equals(""))
                {
                    output.println("package " + current_pkg + ";");
                    output.println();
                }
            }
        }
    }

    /**
     * Add a descriptive header in a Java file
     *
     * @param output  the target file
     * @param obj object which descriptive header has to be added
     */
    public void addDescriptiveHeader (java.io.PrintWriter output, IdlObject obj)
    {
        addPackageName(output);

        switch (obj.kind())
        {

        case IdlType.e_const :

            if (obj.hasComment())
                javadoc(output, obj);
            else
            {
                output.println("/**");
                output.println(" * Constant definition: " + obj.name() + ".");
                output.println(" *");
                output.println(" * @author OpenORB Compiler");
                output.println("*/");
            }

            break;

        case IdlType.e_enum :

            if (obj.hasComment())
                javadoc(output, obj);
            else
            {
                output.println("/**");
                output.println(" * Enum definition: " + obj.name() + ".");
                output.println(" *");
                output.println(" * @author OpenORB Compiler");
                output.println("*/");
            }

            break;

        case IdlType.e_struct :

            if (obj.hasComment())
                javadoc(output, obj);
            else
            {
                output.println("/**");
                output.println(" * Struct definition: " + obj.name() + ".");
                output.println(" * ");
                output.println(" * @author OpenORB Compiler");
                output.println("*/");
            }

            break;

        case IdlType.e_union :

            if (obj.hasComment())
                javadoc(output, obj);
            else
            {
                output.println("/**");
                output.println(" * Union definition: " + obj.name() + ".");
                output.println(" * ");
                output.println(" * @author OpenORB Compiler");
                output.println(" */");
            }

            break;

        case IdlType.e_exception :

            if (obj.hasComment())
                javadoc(output, obj);
            else
            {
                output.println("/**");
                output.println(" * Exception definition: " + obj.name() + ".");
                output.println(" * ");
                output.println(" * @author OpenORB Compiler");
                output.println(" */");
            }

            break;

        case IdlType.e_interface :

            if (obj.hasComment())
                javadoc(output, obj);
            else
            {
                output.println("/**");
                output.println(" * Interface definition: " + obj.name() + ".");
                output.println(" * ");
                output.println(" * @author OpenORB Compiler");
                output.println(" */");
            }

            break;

        case IdlType.e_value_box :

            if (obj.hasComment())
                javadoc(output, obj);
            else
            {
                output.println("/**");
                output.println(" * Value box definition: " + obj.name() + ".");
                output.println(" * ");
                output.println(" * @author OpenORB Compiler");
                output.println(" */");
            }

            break;

        case IdlType.e_value :

            if (obj.hasComment())
                javadoc(output, obj);
            else
            {
                output.println("/**");
                output.println(" * Value Type definition: " + obj.name() + ".");
                output.println(" * ");
                output.println(" * @author OpenORB Compiler");
                output.println(" */");
            }

            break;

        case IdlType.e_factory :

            if (obj.hasComment())
                javadoc(output, obj);
            else
            {
                output.println("/**");
                output.println(" * Factory definition: " + obj.name() + ".");
                output.println(" * ");
                output.println(" * @author OpenORB Compiler");
                output.println(" */");
            }

            break;
        }

    }

    /**
     * Translate a JavaDoc comments section
     */
    public void translate_comment_section(java.io.PrintWriter output, String description, IdlObject obj)
    {
        int i = 0;

        while (i < description.length())
        {
            if (description.charAt(i) == '\n')
            {
                if (i != description.length() - 1)
                {
                    output.println("");

                    if ((obj.kind() == IdlType.e_attribute) || (obj.kind() == IdlType.e_operation) || (obj.kind() == IdlType.e_state_member))
                        output.print(tab + "");

                    output.print(" * ");
                }
                else
                {
                    output.println("");
                    return;
                }
            }
            else
                output.print(description.charAt(i));

            i++;
        }
    }

    /**
     * Add a JavaDoc comment
     *
     * @param output  the target file
     * @param obj   the object the header has to be added
     */
    public void javadoc (java.io.PrintWriter output, IdlObject obj)
    {
        IdlComment comment = obj.getComment();
        String description = null;

        if (comment != null)
        {
            description = comment.get_description();

            if ((obj.kind() == IdlType.e_attribute) || (obj.kind() == IdlType.e_operation) || (obj.kind() == IdlType.e_state_member))
                output.print(tab + "");

            output.println("/**");

            if ((obj.kind() == IdlType.e_attribute) || (obj.kind() == IdlType.e_operation) || (obj.kind() == IdlType.e_state_member))
                output.print(tab + "");

            output.print(" * ");

            translate_comment_section(output, description, obj);

            IdlCommentSection [] sections = comment.get_sections();

            for (int i = 0; i < sections.length; i++)
            {
                switch (sections[ i ].kind().value())
                {

                case IdlCommentField._author_field :

                    if ((obj.kind() == IdlType.e_attribute) || (obj.kind() == IdlType.e_operation))
                        output.print(tab + "");

                    output.print(" * @author ");

                    break;

                case IdlCommentField._deprecated_field :
                    if ((obj.kind() == IdlType.e_attribute) || (obj.kind() == IdlType.e_operation))
                        output.print(tab + "");

                    output.print(" * @deprecated ");

                    break;

                case IdlCommentField._exception_field :
                    if ((obj.kind() == IdlType.e_attribute) || (obj.kind() == IdlType.e_operation))
                        output.print(tab + "");

                    output.print(" * @exception ");

                    break;

                case IdlCommentField._return_field :
                    if ((obj.kind() == IdlType.e_attribute) || (obj.kind() == IdlType.e_operation))
                        output.print(tab + "");

                    output.print(" * @return ");

                    break;

                case IdlCommentField._param_field :
                    if ((obj.kind() == IdlType.e_attribute) || (obj.kind() == IdlType.e_operation))
                        output.print(tab + "");

                    output.print(" * @param ");

                    break;

                case IdlCommentField._see_field :
                    if ((obj.kind() == IdlType.e_attribute) || (obj.kind() == IdlType.e_operation))
                        output.print(tab + "");

                    output.print(" * @see ");

                    break;

                case IdlCommentField._version_field :
                    if ((obj.kind() == IdlType.e_attribute) || (obj.kind() == IdlType.e_operation))
                        output.print(tab + "");

                    output.print(" * @version ");

                    break;

                case IdlCommentField._unknown_field :
                    if ((obj.kind() == IdlType.e_attribute) || (obj.kind() == IdlType.e_operation))
                        output.print(tab + "");

                    output.print(" * @" + sections[ i ].get_title() + " ");

                    break;
                }

                description = sections[ i ].get_description();
                translate_comment_section(output, description, obj);
            }

            if ((obj.kind() == IdlType.e_attribute) || (obj.kind() == IdlType.e_operation) || (obj.kind() == IdlType.e_state_member))
                output.print(tab + "");

            output.println(" */");
        }
    }

    /**
     * Returns the final type of a data type
     *
     * @param obj the object
     * @return the final type
     */
    public int final_kind(IdlObject obj)
    {
        switch (obj.kind())
        {

        case IdlType.e_ident :
            return final_kind(((IdlIdent) obj).internalObject());

        case IdlType.e_typedef :

        case IdlType.e_union_member :

        case IdlType.e_struct_member :

        case IdlType.e_param :
            return final_kind(obj.current());

        default :
            return obj.kind();
        }
    }

    /**
     * Returns the final definition of a data type
     *
     * @param obj the object
     * @return the final definition
     */
    public IdlObject final_type(IdlObject obj)
    {
        switch (obj.kind())
        {

        case IdlType.e_ident :
            return final_type(((IdlIdent) obj).internalObject());

        case IdlType.e_typedef :

        case IdlType.e_union_member :

        case IdlType.e_struct_member :

        case IdlType.e_param :
            return final_type(obj.current());

        default :
            return obj;
        }
    }

    /**
     * This method replaces in a path the file separator by '.'
     */
    private String adaptToDot(String path)
    {
        char [] tmp = new char[ path.length() ];

        for (int i = 0; i < path.length(); i++)
        {
            if ((path.charAt(i) == '/') || (path.charAt(i) == '\\'))
                tmp[ i ] = '.';
            else
                tmp[ i ] = path.charAt(i);
        }

        return new String(tmp);
    }

    /**
    * Returns the complete name of a CORBA object
    *
    * @param obj the object the name has to be retrieved
    * @return the complete name
    */
    public String fullname (IdlObject obj)
    {
        List v = new ArrayList();
        IdlObject obj2 = obj;
        String name = "";
        boolean first = false;

        while (obj2 != null)
        {
            if (first)
            {
                if ((obj2.kind() == IdlType.e_interface) ||
                        (obj2.kind() == IdlType.e_value) ||
                        (obj2.kind() == IdlType.e_struct) ||
                        (obj2.kind() == IdlType.e_union) ||
                        (obj2.kind() == IdlType.e_exception))
                {
                    v.add((obj2.name() + "Package"));
                }
                else
                {
                    if ( obj2.kind() != IdlType.e_union_member )
                    {
                        v.add( obj2.adaptName( obj2.name() ) );
                    }
                }
            }
            else
            {
                if (obj2.kind() != IdlType.e_union_member)
                {
                    v.add(obj2.name());
                }
            }
            if (obj2.upper() != null)
            {
                if (obj2.upper().kind() == IdlType.e_root)
                {
                    break;
                }
            }

            // IdlStructMembers sometimes have a null name.
            // In that case we proceed further towards the root of the tree
            // to avoid a NPE in the next loop. See also
            // http://marc.theaimsgroup.com/?l=openorb-devel&m=111073137117247&w=2
            do
            {
            	obj2 = obj2.upper();
            } while (obj2.name() == null);

            first = true;
        }

        if (m_cp.getM_packageName() != null)
        {
            if (!obj.included())
            {
                if (m_cp.getM_packageName().length() > 0)
                {
                    if (!((m_cp.getM_packageName().equals("generated")) && (m_cp.getM_use_package() == false)))
                    {
                    	name = adaptToDot(m_cp.getM_packageName());
                    }
                }
            }
        }

        if (m_cp.getM_usePrefix())
        {
            if (obj.getPrefix() != null)
            {
                if (name.length() > 0)
                {
                	name = name + ".";
                }

                if (m_cp.getM_reversePrefix())
                {
                	name = name + inversedPrefix(obj.getPrefix());
                }
                else
                {
                	name = name + obj.getPrefix();
                }
            }
        }

        for (int i = v.size() - 1; i >= 0; i--)
        {
            String s = (String) v.get(i);

            if (s != null)
            {
                if (name.length() > 0)
                {
                    name = name + ".";
                }
                name = name + s;
            }
        }

        return name;
    }

    /**
     * Delete the package of an id
     */
    public String removePackageName(String expr)
    {
        int last = 0;
        int index = 0;
        String tmp = "";

        while (true)
        {
            index = expr.indexOf("Package", last);

            if (index == -1)
            {
                tmp = tmp + expr.substring(last, expr.length());
                break;
            }
            else
            {
                tmp = tmp + expr.substring(last, index);
                last = index + 7;
            }
        }

        return tmp;
    }

    /**
     * Check if the the id is an enum member
     */
    public boolean isEnumCase(String expr)
    {
        boolean isEnum = false;

        if (expr.indexOf("@") != -1)
            isEnum = true;

        return isEnum;
    }

    /**
     * Returns true if the element passed as argument is in the same
     * scope as the second argument.
     */
    public boolean isSameScope(String ident, IdlObject obj)
    {
        IdlObject obj2 = obj.upper().returnVisibleObject(ident, false);

        if (obj2 == null)
            return false;

        if (obj2.upper().equals(obj.upper()))
            return true;

        return false;
    }


    /**
     * Translate a Scope IDL Ident::Ident into Scoped Java Ident.Ident.value
     *
     * @param expr the IDL expression
     * @return the equivalent Java expression
     */
    public String IdlScopeToJavaScope(String expr, boolean complete, boolean fixed, IdlObject obj)
    {
        return idlScopeToJavaScope(expr, complete, fixed, true, obj);
    }

    private String idlScopeToJavaScope(String expr, boolean complete,
            final boolean fixed, final boolean useLongLiteral, IdlObject obj) {

        List s = new ArrayList();
        String mot = new String();
        int deb;
        boolean hexaValue;
        int last = 0;
        boolean isEnum = false;

        for (int i = 0; i < expr.length(); i++)
        {
            if ((Character.isDigit(expr.charAt(i))) ||
                    (expr.charAt(i) == '-'))
            {
                if (expr.charAt(i) == '-')
                {
                    s.add(CharacterCache.getCharacter('-'));

                    if (expr.charAt(i++) == ' ')
                        continue;
                }

                hexaValue = false;

                while ((i != expr.length()) && (expr.charAt(i) != ' '))
                {

                    if (expr.charAt(i) == 'x')
                        hexaValue = true;

                    s.add(CharacterCache.getCharacter(expr.charAt(i)));

                    i++;
                }

                if (!fixed && useLongLiteral) {
                    s.add(CharacterCache.getCharacter('l'));
                }
            }
            else
                if (expr.charAt(i) == '\"')
                {
                    i++;
                    s.add(CharacterCache.getCharacter('\"'));
                    boolean prev = false;
                    boolean stop = false;

                    while ((i != expr.length()) && (stop == false))
                    {
                        if ((expr.charAt(i) == '\"') && (prev == false))
                            stop = true;
                        else
                        {
                            prev = false;

                            if (expr.charAt(i) == '\\')
                                prev = true;

                            s.add(CharacterCache.getCharacter(expr.charAt(i)));

                            i++;
                        }
                    }

                    s.add(CharacterCache.getCharacter('\"'));
                }
                else
                    if (expr.charAt(i) == '\'')
                    {
                        i++;
                        s.add(CharacterCache.getCharacter('\''));
                        boolean prev = false;
                        boolean stop = false;

                        while ((i != expr.length()) && (stop == false))
                        {
                            if ((expr.charAt(i) == '\'') && (prev == false))
                                stop = true;
                            else
                            {
                                prev = false;

                                if (expr.charAt(i) == '\\')
                                    prev = true;

                                s.add(CharacterCache.getCharacter(expr.charAt(i)));

                                i++;
                            }
                        }

                        s.add(CharacterCache.getCharacter('\''));
                    }
                    else
                        if (Character.isLetter(expr.charAt(i)))
                        {
                            mot = "";
                            deb = i;

                            while ((i != expr.length()) && (expr.charAt(i) != ' '))
                            {
                                if (expr.charAt(i) == ':')
                                {
                                    i++;

                                    if (i < expr.length())
                                        if (expr.charAt(i) == ':')
                                        {
                                            s.add(CharacterCache.getCharacter('.'));
                                            last = s.size();
                                            i++;
                                        }
                                        else
                                            s.add(CharacterCache.getCharacter(':'));
                                }

                                if (expr.charAt(i) == '.')
                                    last = s.size() + 1;

                                s.add(CharacterCache.getCharacter(expr.charAt(i)));

                                mot = mot + expr.charAt(i);

                                i++;
                            }

                            if (!((mot.equals("true")) || (mot.equals("false"))))
                            {
                                isEnum = isEnumCase(expr);
                                boolean isInSameScope = isSameScope(mot, obj);

                                if (isEnum)
                                    s.remove(s.size() - 1);

                                if ((isEnum == false) && (isInSameScope == false))
                                {
                                    if (!mot.endsWith(".value"))
                                    {
                                        s.add(CharacterCache.getCharacter('.'));
                                        s.add(CharacterCache.getCharacter('v'));
                                        s.add(CharacterCache.getCharacter('a'));
                                        s.add(CharacterCache.getCharacter('l'));
                                        s.add(CharacterCache.getCharacter('u'));
                                        s.add(CharacterCache.getCharacter('e'));
                                    }
                                }
                                else
                                    if ((complete == true) && (isInSameScope == false))
                                    {
                                        s.add(last, CharacterCache.getCharacter('_'));
                                    }

                            }
                        }
                        else
                            if (expr.charAt(i) != ' ')
                                s.add (CharacterCache.getCharacter(expr.charAt(i)));
        }

        String newExpr = new String();

        for (int i = 0; i < s.size(); i++)
            newExpr = newExpr + ((Character) s.get(i)).charValue();

        return newExpr;
    }

    /**
     * Allow to test if the character is an hexa number
     *
     * @param c the character to test
     * @return true if the character is an hexa number
     */
    public boolean isHexaCar(char c)
    {
        switch (c)
        {

        case '0' :

        case '1' :

        case '2' :

        case '3' :

        case '4' :

        case '5' :

        case '6' :

        case '7' :

        case '8' :

        case 'a' :

        case 'b' :

        case 'c' :

        case 'd' :

        case 'e' :

        case 'f' :
            return true;
        }

        return false;
    }

    /**
     * Change the IDL escape characters into CORBA escape characters
     *
     * @param expr the IDL expression
     * @return the equivalent Java expression
     */
    public String IdlEspaceCharToJavaEscapeChar(String expr)
    {
        List s = new ArrayList();

        for (int i = 0; i < expr.length(); i++)
        {

            if (expr.charAt(i) == '\\')
            {
                s.add (CharacterCache.getCharacter('\\'));
                i++;

                switch (expr.charAt(i))
                {

                case '\\':
                    break;

                case 'a' :
                    s.add(CharacterCache.getCharacter('0'));
                    s.add(CharacterCache.getCharacter('0'));
                    s.add(CharacterCache.getCharacter('7'));
                    break;

                case 'v' :
                    s.add(CharacterCache.getCharacter('0'));
                    s.add(CharacterCache.getCharacter('1'));
                    s.add(CharacterCache.getCharacter('3'));
                    break;

                case 'x' :
                    i++;

                    while (isHexaCar(expr.charAt(i)))
                    {
                        i++;
                    }

                    s.add(CharacterCache.getCharacter('3'));
                    s.add(CharacterCache.getCharacter('7'));
                    s.add(CharacterCache.getCharacter('7'));

                default :
                    s.add(CharacterCache.getCharacter(expr.charAt(i)));
                }
            }
            else
                s.add (CharacterCache.getCharacter(expr.charAt(i)));
        }

        String newExpr = new String();

        for (int i = 0; i < s.size(); i++)
            newExpr = newExpr + ((Character) s.get(i)).charValue();

        return newExpr;
    }

    /**
     * Translate an IDL expression into a Java expression
     *
     * @param expr the IDL expression
     * @return the equivalent Java expression
     */
    public String translate_to_java_expression(String expr, boolean fixed, IdlObject obj)
    {
        String newExpr = IdlScopeToJavaScope(expr, true, fixed, obj);

        newExpr = IdlEspaceCharToJavaEscapeChar(newExpr);

        if (fixed)
            newExpr = "new java.math.BigDecimal(\"" + newExpr + "\")";

        return newExpr;
    }

    /**
     * Translate an IDL expression into an union expression
     *
     * @param expr the IDL expression
     * @return the equivalent Java expression
     */
    public String translate_to_union_case_expression(IdlUnionMember disc, String expr)
    {
        String header = "";
        String newExpr = IdlScopeToJavaScope(expr, false, false, disc);
        newExpr = IdlEspaceCharToJavaEscapeChar(newExpr);

        disc.reset();

        switch (final_type(disc.current()).kind())
        {

        case IdlType.e_simple :
            IdlSimple simple = (IdlSimple) final_type(disc.current());

            switch (simple.internal())
            {

            case Token.t_short :

            case Token.t_ushort :
                header = "short)";
                break;

            case Token.t_long :

            case Token.t_ulong :
                header = "int)";
                break;

            case Token.t_longlong :

            case Token.t_ulonglong :
                header = "long)";
                break;

            case Token.t_char :

            case Token.t_wchar :
                header = "char)";
                break;

            case Token.t_boolean :
                header = "boolean)";
                break;

            case Token.t_octet :
                header = "byte)";
                break;
            }

            break;

        default :
            break;
        }

        if (header.equals(""))
            return newExpr;
        else
            return "(" + header + "(" + newExpr + ")";
    }

    /**
     * Translate a data type
     *
     * @param obj the object to translate
     * @param output the write access
     */
    public void translate_type(IdlObject obj, java.io.PrintWriter output)
    {
        IdlSimple simple = null;
        switch ( obj.kind() )
        {

        case IdlType.e_simple :
            simple = (IdlSimple) obj;

            switch (simple.internal())
            {

            case Token.t_void :
                output.print("void");
                break;

            case Token.t_float :
                output.print("float");
                break;

            case Token.t_double :
                output.print("double");
                break;

            case Token.t_short :

            case Token.t_ushort :
                output.print("short");
                break;

            case Token.t_long :

            case Token.t_ulong :
                output.print("int");
                break;

            case Token.t_longlong :

            case Token.t_ulonglong :
                output.print("long");
                break;

            case Token.t_char :

            case Token.t_wchar :
                output.print("char");
                break;

            case Token.t_boolean :
                output.print("boolean");
                break;

            case Token.t_octet :
                output.print("byte");
                break;

            case Token.t_any :
                output.print("org.omg.CORBA.Any");
                break;

            case Token.t_typecode :
                output.print("org.omg.CORBA.TypeCode");
                break;

            case Token.t_object :
                output.print("org.omg.CORBA.Object");
                break;

            case Token.t_ValueBase :
                output.print("java.io.Serializable");
                break;
            }
            break;

        case IdlType.e_fixed:
            output.print("java.math.BigDecimal");
            break;

        case IdlType.e_string:
        case IdlType.e_wstring:
            output.print("String");
            break;

        case IdlType.e_struct:
        case IdlType.e_union:
        case IdlType.e_enum:
        case IdlType.e_interface:
        case IdlType.e_forward_interface:
        case IdlType.e_exception:
        case IdlType.e_value:
        case IdlType.e_forward_value:
            output.print( fullname( obj ) );
            break;

        case IdlType.e_native:

            if ( isNativeDefinition( obj ) )
            {
                printNativeDefinition(obj, output);
            }
            else
            {
                output.print( fullname( obj ) );
            }
            break;

        case IdlType.e_typedef:
            obj.reset();
            translate_type( obj.current(), output );
            break;

        case IdlType.e_sequence:
        case IdlType.e_array:
            translate_type( obj.current(), output );
            output.print("[]");
            break;

        case IdlType.e_ident:
            translate_type( ( ( IdlIdent ) obj ).internalObject(), output );
            break;

        case IdlType.e_value_box:
            if ( ( ( IdlValueBox ) obj ).simple() )
            {
                output.print( fullname( obj ) );
            }
            else
            {
                obj.reset();
                translate_type( obj.current(), output );
            }

            break;
        }
    }

    /**
     * Translate a parameter
     *
     * @param obj  param object to translate
     * @param output write access
     * @param attr parameter attribute
     */
    public void translate_parameter(IdlObject obj, java.io.PrintWriter output, int attr)
    {
        IdlSimple simple = null;

        switch (obj.kind())
        {

        case IdlType.e_simple :
            simple = (IdlSimple) obj;

            switch (simple.internal())
            {

            case Token.t_float :

                if (attr == 0)
                    output.print("float");
                else
                    output.print("org.omg.CORBA.FloatHolder");

                break;

            case Token.t_double :
                if (attr == 0)
                    output.print("double");
                else
                    output.print("org.omg.CORBA.DoubleHolder");

                break;

            case Token.t_short :

            case Token.t_ushort :
                if (attr == 0)
                    output.print("short");
                else
                    output.print("org.omg.CORBA.ShortHolder");

                break;

            case Token.t_long :

            case Token.t_ulong :
                if (attr == 0)
                    output.print("int");
                else
                    output.print("org.omg.CORBA.IntHolder");

                break;

            case Token.t_longlong :

            case Token.t_ulonglong :
                if (attr == 0)
                    output.print("long");
                else
                    output.print("org.omg.CORBA.LongHolder");

                break;

            case Token.t_char :

            case Token.t_wchar :
                if (attr == 0)
                    output.print("char");
                else
                    output.print("org.omg.CORBA.CharHolder");

                break;

            case Token.t_boolean :
                if (attr == 0)
                    output.print("boolean");
                else
                    output.print("org.omg.CORBA.BooleanHolder");

                break;

            case Token.t_octet :
                if (attr == 0)
                    output.print("byte");
                else
                    output.print("org.omg.CORBA.ByteHolder");

                break;

            case Token.t_any :
                if (attr == 0)
                    output.print("org.omg.CORBA.Any");
                else
                    output.print("org.omg.CORBA.AnyHolder");

                break;

            case Token.t_typecode :
                if (attr == 0)
                    output.print("org.omg.CORBA.TypeCode");
                else
                    output.print("org.omg.CORBA.TypeCodeHolder");

                break;

            case Token.t_object :
                if (attr == 0)
                    output.print("org.omg.CORBA.Object");
                else
                    output.print("org.omg.CORBA.ObjectHolder");

                break;

            case Token.t_ValueBase :
                if (attr == 0)
                    output.print("java.io.Serializable");
                else
                    output.print("org.omg.CORBA.ValueBaseHolder");

                break;
            }

            break;

        case IdlType.e_fixed :

            if (attr == 0)
                output.print("java.math.BigDecimal");
            else
                output.print("org.omg.CORBA.FixedHolder");

            break;

        case IdlType.e_string :

        case IdlType.e_wstring :
            if (attr == 0)
                output.print("String");
            else
                output.print("org.omg.CORBA.StringHolder");

            break;

        case IdlType.e_value_box :
            if (attr == 0)
            {
                if (((IdlValueBox) obj).simple())
                    output.print(fullname(obj));
                else
                {
                    obj.reset();
                    translate_parameter(obj.current() , output, attr);
                }
            }
            else
                output.print(fullname(obj) + "Holder");

            break;

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_enum :

        case IdlType.e_interface :

        case IdlType.e_forward_interface :

        case IdlType.e_value :

        case IdlType.e_forward_value :
            if (attr == 0)
                output.print(fullname(obj));
            else
                output.print(fullname(obj) + "Holder");

            break;

        case IdlType.e_native :
            if (attr != 0)
            {
                /*
                if (isNativeDefinition(obj))
            {
                printNativeDefinition(obj,output);
                output.print("Holder");
            }
                else*/
                output.print(fullname(obj) + "Holder");
            }
            else
            {
                if (isNativeDefinition(obj))
                    printNativeDefinition(obj, output);
                else
                    output.print(fullname(obj));
            }

            break;

        case IdlType.e_typedef :
            obj.reset();

            if (attr != 0)
            {
                /*
                if ((final_type(obj).kind() == IdlType.e_simple) ||
                (final_type(obj).kind() == IdlType.e_string) ||
                (final_type(obj).kind() == IdlType.e_wstring))
                translate_parameter(obj.current(), output, attr);
                else
                output.print(fullname(obj)+"Holder");*/

                if ((final_type(obj).kind() == IdlType.e_sequence) ||
                        (final_type(obj).kind() == IdlType.e_array))
                    output.print(fullname(obj) + "Holder");
                else
                    translate_parameter(obj.current(), output, attr);
            }
            else
                translate_parameter(obj.current(), output, attr);

            break;

        case IdlType.e_sequence :

        case IdlType.e_array :
            if (attr == 0)
            {
                translate_parameter(obj.current(), output, attr);
                output.print("[]");
            }
            else
            {
                output.print(fullname(obj.upper()) + "Holder");
            }

            break;

        case IdlType.e_ident :
            translate_parameter(((IdlIdent) obj).internalObject(), output, attr);
            break;
        }
    }

    /**
     * Find include depth for an object
     *
     * @param obj object to test
     * @return include depth
     */
    public int recursion(IdlObject obj)
    {
        String name = final_type(obj.current()).name();
        int i = 0;

        if (final_type(obj.current()) instanceof IdlSimple)
            return 0;

        obj = obj.upper();

        while (obj != null)
        {

            if (final_type(obj).name() != null)
                if (final_type(obj).name().equals(name))
                    return i;

            i++;

            obj = obj.upper();

            if (obj == null)
                return i;

            if ((obj.kind() == IdlType.e_interface) ||
                    (obj.kind() == IdlType.e_module) ||
                    (obj.kind() == IdlType.e_root))
                break;
        }

        return 0;

    }

    /**
     * Translate a TypeCode
     *
     * @param obj object to translate
     * @param output write access
     */
    public void translate_typecode (IdlObject obj, java.io.PrintWriter output)
    {
        IdlSimple simple = null;

        switch (obj.kind())
        {

        case IdlType.e_simple :
            simple = (IdlSimple) obj;
            output.print("orb.get_primitive_tc( org.omg.CORBA.TCKind.");

            switch (simple.internal())
            {

            case Token.t_float :
                output.print("tk_float )");
                break;

            case Token.t_double :
                output.print("tk_double )");
                break;

            case Token.t_short :
                output.print("tk_short )");
                break;

            case Token.t_ushort :
                output.print("tk_ushort )");
                break;

            case Token.t_long :
                output.print("tk_long )");
                break;

            case Token.t_ulong :
                output.print("tk_ulong )");
                break;

            case Token.t_longlong :
                output.print("tk_longlong )");
                break;

            case Token.t_ulonglong :
                output.print("tk_ulonglong )");
                break;

            case Token.t_char :
                output.print("tk_char )");
                break;

            case Token.t_wchar :
                output.print("tk_wchar )");
                break;

            case Token.t_boolean :
                output.print("tk_boolean )");
                break;

            case Token.t_octet :
                output.print("tk_octet )");
                break;

            case Token.t_any :
                output.print("tk_any )");
                break;

            case Token.t_typecode :
                output.print("tk_TypeCode )");
                break;

            case Token.t_object :
                output.print("tk_objref )");
                break;

            case Token.t_ValueBase :
                output.print("tk_value )");
                break;
            }

            break;

        case IdlType.e_fixed :
            output.print("orb.create_fixed_tc( ( short ) " + ((IdlFixed) obj).digits() + ", ( short ) " + ((IdlFixed) obj).scale() + " )");
            break;

        case IdlType.e_string :

            if (((IdlString) obj).maxSize() == 0)
                output.print("orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_string )");
            else
                output.print("orb.create_string_tc( " + ((IdlString) obj).maxSize() + " )");

            break;

        case IdlType.e_wstring :
            if (((IdlWString) obj).maxSize() == 0)
                output.print("orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_wstring )");
            else
                output.print("orb.create_wstring_tc( " + ((IdlWString) obj).maxSize() + " )");

            break;

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_enum :

        case IdlType.e_interface :

        case IdlType.e_native :

        case IdlType.e_forward_value :

        case IdlType.e_forward_interface :

        case IdlType.e_value_box :

        case IdlType.e_value :
            output.print(fullname(obj));

            output.print("Helper.type()");

            break;

        case IdlType.e_typedef :
            output.print(fullname(obj));

            output.print("Helper.type()");

            break;

        case IdlType.e_sequence :
            /* int rec = recursion(obj);
              if (rec != 0)
              {
              output.print("orb.create_recursive_sequence_tc(");
              output.print(((IdlSequence)obj).getSize()+","+rec);
              output.print(")");
              }
              else
              {*/
            output.print("orb.create_sequence_tc( ");

            output.print(((IdlSequence) obj).getSize() + ", ");

            obj.reset();

            translate_typecode(obj.current(), output);

            output.print(" )");

            //}
            break;

        case IdlType.e_array :
            output.print("orb.create_array_tc( ");

            output.print(((IdlArray) obj).getDimension() + ", ");

            obj.reset();

            translate_typecode(obj.current(), output);

            output.print(" )");

            break;

        case IdlType.e_ident :
            translate_typecode(((IdlIdent) obj).internalObject(), output);

            break;

        default :
            System.out.println("Unexpected... : " + obj.kind());

            break;
        }
    }

    /**
     * Translate an any insert
     *
     * @param obj object to insert
     * @param output write access
     * @param aname any type name
     * @param tname type name to insert
     */
    public void translate_any_insert (IdlObject obj, java.io.PrintWriter output, String aname, String tname)
    {
        IdlSimple simple = null;

        switch (obj.kind())
        {

        case IdlType.e_simple :
            simple = (IdlSimple) obj;

            if (simple.internal() == Token.t_ValueBase)
            {
                output.print("org.omg.CORBA.portable.ValueBaseHelper.insert( " + aname + ", ( org.omg.CORBA.portable.ValueBase)" + tname + " )");
                return;
            }

            output.print(aname + ".insert_");

            switch (simple.internal())
            {

            case Token.t_float :
                output.print("float( ( float ) " + tname + " )");
                break;

            case Token.t_double :
                output.print("double( ( double ) " + tname + " )");
                break;

            case Token.t_short :
                output.print("short( ( short ) " + tname + " )");
                break;

            case Token.t_ushort :
                output.print("ushort( ( short ) " + tname + " )");
                break;

            case Token.t_long :
                output.print("long( ( int ) " + tname + " )");
                break;

            case Token.t_ulong :
                output.print("ulong( ( int ) " + tname + " )");
                break;

            case Token.t_longlong :
                output.print("longlong( ( long ) " + tname + " )");
                break;

            case Token.t_ulonglong :
                output.print("ulonglong( ( long ) " + tname + " )");
                break;

            case Token.t_char :
                output.print("char( " + tname + " )");
                break;

            case Token.t_wchar :
                output.print("wchar( " + tname + " )");
                break;

            case Token.t_boolean :
                output.print("boolean( " + tname + " )");
                break;

            case Token.t_octet :
                output.print("octet( ( byte ) " + tname + " )");
                break;

            case Token.t_any :
                output.print("any( " + tname + " )");
                break;

            case Token.t_typecode :
                output.print("TypeCode( " + tname + " )");
                break;

            case Token.t_object :
                output.print("Object( " + tname + " )");
                break;
            }

            break;

        case IdlType.e_fixed :
            output.print(aname + ".insert_fixed( " + tname + " )");
            break;

        case IdlType.e_string :
            output.print(aname + ".insert_string( " + tname + " )");
            break;

        case IdlType.e_wstring :
            output.print(aname + ".insert_wstring( " + tname + " )");
            break;

        case IdlType.e_native :

        case IdlType.e_interface :

        case IdlType.e_forward_interface :

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_enum :

        case IdlType.e_value_box :

        case IdlType.e_value :
            output.print(fullname(obj));
            output.print("Helper.insert( " + aname + "," + tname + " )");
            break;

        case IdlType.e_typedef :
            output.print(fullname(obj));
            output.print("Helper.insert( " + aname + "," + tname + " )");
            break;

        case IdlType.e_sequence :

        case IdlType.e_array :
            break;

        case IdlType.e_ident :
            translate_any_insert(((IdlIdent) obj).internalObject(), output, aname, tname);
            break;
        }
    }

    /**
     * This function looks for a truncatable interface in value type
     * inheritance
     */
    public boolean isTruncatable(IdlObject obj)
    {
        IdlValue value = (IdlValue) obj;

        List inheritance = value.getInheritanceList();

        for (int i = 0; i < inheritance.size(); i++)
        {
            if (((IdlValueInheritance) (inheritance.get(i))).truncatable_member())
                return true;
        }

        return false;
    }

    /**
     * This function prints the concrete base value type TypeCode
     */
    public void printConcreteTypeCode(IdlObject obj, java.io.PrintWriter output)
    {
        IdlValue value = (IdlValue) obj;

        IdlValue [] inheritance = value.getInheritance();

        for (int i = 0; i < inheritance.length; i++)
        {
            if (inheritance[ i ].abstract_value() == false)
            {
                translate_typecode(inheritance[ i ], output);
                return;
            }
        }

        output.print("orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_null )");
    }

    /**
     * Construct a new TypeCode
     *
     * @param obj the typecode
     * @param output write access
     */
    public void translate_new_typecode (IdlObject obj, java.io.PrintWriter output)
    {
        int i;
        int idx;

        switch (obj.kind())
        {

        case IdlType.e_enum :
            output.println(tab3 + "org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");
            output.println(tab3 + "String []_members = new String[ " + obj.length() + " ];");

            obj.reset();
            i = 0;

            while (obj.end() != true)
            {
                output.println(tab3 + "_members[ " + i + " ] = \"" + obj.current().name() + "\";");
                i++;
                obj.next();
            }

            output.println(tab3 + "_tc = orb.create_enum_tc( id(), \"" + obj.name() + "\", _members );");
            break;

        case IdlType.e_struct :
            output.println(tab4 + "org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");
            output.println(tab4 + "org.omg.CORBA.StructMember _members[] = new org.omg.CORBA.StructMember[ " + obj.length() + " ];");
            output.println("");

            obj.reset();
            i = 0;

            while (obj.end() != true)
            {
                output.println(tab4 + "_members[ " + i + " ] = new org.omg.CORBA.StructMember();");

                output.println(tab4 + "_members[ " + i + " ].name = \"" + obj.current().name() + "\";");

                output.print(tab4 + "_members[ " + i + " ].type = ");

                obj.current().reset();
                translate_typecode(obj.current().current(), output);

                output.println(";");

                obj.next();
                i++;
            }

            output.println(tab4 + "_tc = orb.create_struct_tc( id(), \"" + obj.name() + "\", _members );");
            break;

        case IdlType.e_union :
            output.println(tab4 + "org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");
            output.println(tab4 + "org.omg.CORBA.UnionMember _members[] = new org.omg.CORBA.UnionMember[ " + (obj.length() - 1) + " ];");
            output.println(tab4 + "org.omg.CORBA.Any any;");
            output.println("");

            idx = ((IdlUnion) obj).index();

            obj.reset();
            IdlUnionMember disc = (IdlUnionMember) obj.current();
            disc.reset();
            obj.next();
            i = 0;

            while (obj.end() != true)
            {
                output.println(tab4 + "any = orb.create_any();");

                if (i != idx)
                {
                    output.print(tab4 + "");
                    translate_any_insert(disc.current(), output, "any", translate_to_union_case_expression(disc, ((IdlUnionMember) obj.current()).getExpression()));
                    output.println(";");
                }
                else
                    output.println(tab4 + "any.insert_octet( ( byte ) 0 );");

                output.println(tab4 + "_members[ " + i + " ] = new org.omg.CORBA.UnionMember();");

                output.println(tab4 + "_members[ " + i + " ].name = \"" + obj.current().name() + "\";");

                output.println(tab4 + "_members[ " + i + " ].label = any;");

                output.print(tab4 + "_members[ " + i + " ].type = ");

                obj.current().reset();

                translate_typecode(obj.current().current(), output);

                output.println(";");

                obj.next();

                i++;
            }

            obj.reset();
            output.print(tab4 + "_tc = orb.create_union_tc( id(), \"" + obj.name() + "\", ");
            translate_typecode(obj.current().current(), output);
            output.println(", _members );");
            break;

        case IdlType.e_typedef :
            output.println(tab3 + "org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");
            output.print(tab3 + "_tc = orb.create_alias_tc( id(), \"" + obj.name() + "\", ");

            obj.reset();
            translate_typecode(obj.current(), output);

            output.println(" );");
            break;

        case IdlType.e_exception :
            output.println(tab4 + "org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");
            output.println(tab4 + "org.omg.CORBA.StructMember _members[] = new org.omg.CORBA.StructMember[ " + obj.length() + " ];");
            output.println("");

            obj.reset();
            i = 0;

            while (obj.end() != true)
            {
                output.println(tab4 + "_members[ " + i + " ] = new org.omg.CORBA.StructMember();");

                output.println(tab4 + "_members[ " + i + " ].name = \"" + obj.current().name() + "\";");

                output.print(tab4 + "_members[ " + i + " ].type = ");

                obj.current().reset();
                translate_typecode(obj.current().current(), output);

                output.println(";");

                obj.next();
                i++;
            }

            output.println(tab4 + "_tc = orb.create_exception_tc( id(), \"" + obj.name() + "\", _members );");
            break;

        case IdlType.e_interface :

        case IdlType.e_forward_interface :
            output.println(tab3 + "org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");

            if (((IdlInterface) obj).abstract_interface())
                output.println(tab3 + "_tc = orb.create_abstract_interface_tc( id(), \"" + obj.name() + "\" );");
            else
                output.println(tab3 + "_tc = orb.create_interface_tc( id(), \"" + obj.name() + "\" );");

            break;

        case IdlType.e_native :
            output.println(tab3 + "org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");

            output.println(tab3 + "_tc = orb.create_native_tc( id(), \"" + obj.name() + "\" );");

            break;

        case IdlType.e_value_box :
            output.println(tab3 + "org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");

            output.print(tab3 + "_tc = orb.create_value_box_tc( id(), \"" + obj.name() + "\", ");

            obj.reset();

            translate_typecode(obj.current(), output);

            output.println(" );");

            break;

        case IdlType.e_value :
            output.println(tab4 + "org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");

            int nbvalue = 0;

            obj.reset();

            while (obj.end() != true)
            {
                if (obj.current().kind() == IdlType.e_state_member)
                    nbvalue++;

                obj.next();
            }

            output.println(tab4 + "org.omg.CORBA.ValueMember _members[] = new org.omg.CORBA.ValueMember[ " + nbvalue + " ];");
            output.println("");

            obj.reset();
            i = 0;

            while (obj.end() != true)
            {
                if (obj.current().kind() == IdlType.e_state_member)
                {
                    output.println(tab4 + "_members[ " + i + " ] = new org.omg.CORBA.ValueMember();");

                    output.println(tab4 + "_members[ " + i + " ].name = \"" + obj.current().name() + "\";");

                    output.print(tab4 + "_members[ " + i + " ].type = ");

                    obj.current().reset();
                    translate_typecode(obj.current().current(), output);

                    output.println(";");

                    output.print(tab4 + "_members[ " + i + " ].access = ");

                    if (((IdlStateMember) (obj.current())).public_member())
                        output.println("org.omg.CORBA.PUBLIC_MEMBER.value;");
                    else
                        output.println("org.omg.CORBA.PRIVATE_MEMBER.value;");

                    i++;
                }

                obj.next();
            }

            output.println("");
            output.print(tab4 + "org.omg.CORBA.TypeCode _concrete_tc = ");

            printConcreteTypeCode(obj, output);

            output.println(";");
            output.println("");

            output.print(tab4 + "_tc = orb.create_value_tc( id(), \"" + obj.name() + "\", ");

            if (((IdlValue) obj).abstract_value())
                output.print("org.omg.CORBA.VM_ABSTRACT.value");
            else
                if (((IdlValue) obj).custom_value())
                    output.print("org.omg.CORBA.VM_CUSTOM.value");
                else
                    if (isTruncatable(obj))
                        output.print("org.omg.CORBA.VM_TRUNCATABLE.value");
                    else
                        output.print("org.omg.CORBA.VM_NONE.value");

            output.println(", _concrete_tc, _members );");

            break;
        }
    }

    /**
     * Return simple array name or null else
     */
    public String get_array_name(IdlObject obj)
    {
        switch (final_kind(obj))
        {

        case IdlType.e_simple :
            IdlSimple simple = (IdlSimple) final_type(obj);

            switch (simple.internal())
            {

            case Token.t_float :
                return "float";

            case Token.t_double :
                return "double";

            case Token.t_short :
                return "short";

            case Token.t_ushort :
                return "ushort";

            case Token.t_long :
                return "long";

            case Token.t_ulong :
                return "ulong";

            case Token.t_longlong :
                return "longlong";

            case Token.t_ulonglong :
                return "ulonglong";

            case Token.t_char :
                return "char";

            case Token.t_wchar :
                return "wchar";

            case Token.t_boolean :
                return "boolean";

            case Token.t_octet :
                return "octet";

            default :
                return null;
            }

        default :
            return null;
        }
    }

    /**
     * Encode a member data type
     *
     * @param obj member to encode
     * @param output write access
     * @param outname outputstream name
     * @param tname data type name
     * @param space indent space
     */
    public void translate_marshalling_member (IdlObject obj, java.io.PrintWriter output, String outname, String tname, String space)
    {
        IdlSimple simple = null;
        String array_name = null;
        int val;

        switch (obj.kind())
        {

        case IdlType.e_simple :
            simple = (IdlSimple) obj;

            if (simple.internal() == Token.t_ValueBase)
            {
                output.println(space + "( ( org.omg.CORBA_2_3.portable.OutputStream ) " + outname + " ).write_value( " + tname + " );");
                return;
            }

            output.print(space + outname + ".write");

            switch (simple.internal())
            {

            case Token.t_float :
                output.println("_float( " + tname + " );");
                break;

            case Token.t_double :
                output.println("_double( " + tname + " );");
                break;

            case Token.t_short :
                output.println("_short( " + tname + " );");
                break;

            case Token.t_ushort :
                output.println("_ushort( " + tname + " );");
                break;

            case Token.t_long :
                output.println("_long( " + tname + " );");
                break;

            case Token.t_ulong :
                output.println("_ulong(" + tname + ");");
                break;

            case Token.t_longlong :
                output.println("_longlong( " + tname + " );");
                break;

            case Token.t_ulonglong :
                output.println("_ulonglong( " + tname + " );");
                break;

            case Token.t_char :
                output.println("_char( " + tname + " );");
                break;

            case Token.t_wchar :
                output.println("_wchar( " + tname + " );");
                break;

            case Token.t_boolean :
                output.println("_boolean( " + tname + " );");
                break;

            case Token.t_octet :
                output.println("_octet( " + tname + " );");
                break;

            case Token.t_any :
                output.println("_any( " + tname + " );");
                break;

            case Token.t_typecode :
                output.println("_TypeCode( " + tname + " );");
                break;

            case Token.t_object :
                output.println("_Object( " + tname + " );");
                break;
            }

            break;

        case IdlType.e_fixed :
            output.println(space + outname + ".write_fixed( " + tname + " );");
            break;

        case IdlType.e_string :
            output.println(space + outname + ".write_string( " + tname + " );");
            break;

        case IdlType.e_wstring :
            output.println(space + outname + ".write_wstring( " + tname + " );");
            break;

        case IdlType.e_native :

        case IdlType.e_interface :

        case IdlType.e_forward_interface :

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_enum :

        case IdlType.e_value_box :

        case IdlType.e_value :

        case IdlType.e_forward_value :
            output.print(space + fullname(obj));
            output.println("Helper.write( " + outname + ", " + tname + " );");
            break;

        case IdlType.e_typedef :
            output.print(space + fullname(obj));
            output.println("Helper.write( " + outname + ", " + tname + " );");
            break;

        case IdlType.e_array :
            array_name = get_array_name(obj.current());

            if (array_name == null)
            {
                val = space.length() - 1;
                output.println(space + "if ( " + tname + ".length != " + ((IdlArray) obj).getDimension() + " ) ");
                output.println(space + "   throw new org.omg.CORBA.MARSHAL();");
                output.println(space + "for ( int i" + val + "=0; i" + val + " < " + tname + ".length; i" + val + "++ )");
                output.println(space + "{");
                translate_marshalling_member(obj.current(), output, outname, tname + "[ i" + val + " ]", space + tab + "");
                output.println("");
                output.println(space + "}");
            }
            else
            {
                output.println(space + outname + ".write_" + array_name + "_array( " + tname + ", 0, " + tname + ".length );");
            }

            break;

        case IdlType.e_sequence :
            array_name = get_array_name(obj.current());

            if (array_name == null)
            {
                val = space.length() - 1;
                output.println(space + outname + ".write_ulong( " + tname + ".length );");
                output.println(space + "for ( int i" + val + " = 0; i" + val + " < " + tname + ".length; i" + val + "++ )");
                output.println(space + "{");
                translate_marshalling_member(obj.current(), output, outname, tname + "[ i" + val + " ]", space + tab + "");
                output.println("");
                output.println(space + "}");
            }
            else
            {
                output.println(space + outname + ".write_ulong( " + tname + ".length );");
                output.println(space + outname + ".write_" + array_name + "_array( " + tname + ", 0, " + tname + ".length );");
            }

            break;

        case IdlType.e_ident :
            translate_marshalling_member(((IdlIdent) obj).internalObject(), output, outname, tname, space);
            break;
        }
    }

    /**
     * Encode a member data type
     *
     * @param obj the member to encode
     * @param output write access
     * @param inname inputstream name
     * @param tname data type name
     * @param space indent space
     */
    public void translate_unmarshalling_member (IdlObject obj, java.io.PrintWriter output, String inname, String tname, String space)
    {
        IdlSimple simple = null;
        String array_name = null;
        IdlObject o = null;
        int val;
        int next;

        switch (obj.kind())
        {

        case IdlType.e_simple :
            simple = (IdlSimple) obj;

            if (simple.internal() == Token.t_ValueBase)
            {
                output.println(space + tname + " = ((org.omg.CORBA_2_3.portable.InputStream)" + inname + ").read_value();");
                return;
            }

            output.print(space + tname + " = " + inname + ".read");

            switch (simple.internal())
            {

            case Token.t_float :
                output.println("_float();");
                break;

            case Token.t_double :
                output.println("_double();");
                break;

            case Token.t_short :
                output.println("_short();");
                break;

            case Token.t_ushort :
                output.println("_ushort();");
                break;

            case Token.t_long :
                output.println("_long();");
                break;

            case Token.t_ulong :
                output.println("_ulong();");
                break;

            case Token.t_longlong :
                output.println("_longlong();");
                break;

            case Token.t_ulonglong :
                output.println("_ulonglong();");
                break;

            case Token.t_char :
                output.println("_char();");
                break;

            case Token.t_wchar :
                output.println("_wchar();");
                break;

            case Token.t_boolean :
                output.println("_boolean();");
                break;

            case Token.t_octet :
                output.println("_octet();");
                break;

            case Token.t_any :
                output.println("_any();");
                break;

            case Token.t_typecode :
                output.println("_TypeCode();");
                break;

            case Token.t_object :
                output.println("_Object();");
                break;

            case Token.t_ValueBase :
                output.println("_value();");
                break;
            }

            break;

        case IdlType.e_fixed :
            output.println(space + tname + " =" + inname + ".read_fixed();");
            break;

        case IdlType.e_string :
            output.println(space + tname + " = " + inname + ".read_string();");
            break;

        case IdlType.e_wstring :
            output.println(space + tname + " = " + inname + ".read_wstring();");
            break;

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_enum :

        case IdlType.e_native :

        case IdlType.e_interface :

        case IdlType.e_forward_interface :

        case IdlType.e_value_box :

        case IdlType.e_value :

        case IdlType.e_forward_value :
            output.print(space + tname + " = " + fullname(obj));
            output.println("Helper.read(" + inname + ");");
            break;

        case IdlType.e_typedef :
            output.print(space + tname + " = " + fullname(obj));
            output.println("Helper.read(" + inname + ");");
            break;

        case IdlType.e_array :
            val = space.length() - 1;
            output.println(space + "{");
            output.println(space + "int size" + val + " = " + ((IdlArray) obj).getDimension() + ";");
            output.print(space + tname + " = new ");

            obj.reset();
            o = final_type(obj.current());
            next = 0;

            while ((o.kind() == IdlType.e_array) ||
                    (o.kind() == IdlType.e_sequence))
            {
                o.reset();
                next++;
                o = final_type(o.current());
            }

            translate_type(o, output);

            output.print("[size" + val + "]");

            for (int i = 0; i < next; i++)
                output.print("[]");

            output.println(";");

            array_name = get_array_name(obj.current());

            if (array_name == null)
            {
                output.println(space + "for (int i" + val + "=0; i" + val + "<" + tname + ".length; i" + val + "++)");
                output.println(space + " {");
                translate_unmarshalling_member(obj.current(), output, inname, tname + "[i" + val + "]", space + tab + "");
                output.println("");
                output.println(space + " }");
            }
            else
            {
                output.println(space + inname + ".read_" + array_name + "_array(" + tname + ", 0, " + tname + ".length);");
            }

            output.println(space + "}");
            break;

        case IdlType.e_sequence :
            val = space.length() - 1;
            output.println(space + "{");
            output.println(space + "int size" + val + " = " + inname + ".read_ulong();");
            output.print(space + tname + " = new ");

            obj.reset();
            o = final_type(obj.current());
            next = 0;

            while ((o.kind() == IdlType.e_array) ||
                    (o.kind() == IdlType.e_sequence))
            {
                o.reset();
                next++;
                o = final_type(o.current());
            }

            translate_type(o, output);

            output.print("[size" + val + "]");

            for (int i = 0; i < next; i++)
                output.print("[]");

            output.println(";");

            array_name = get_array_name(obj.current());

            if (array_name == null)
            {
                output.println(space + "for (int i" + val + "=0; i" + val + "<" + tname + ".length; i" + val + "++)");
                output.println(space + " {");
                translate_unmarshalling_member(obj.current(), output, inname, tname + "[i" + val + "]", space + tab + "");
                output.println("");
                output.println(space + " }");
            }
            else
            {
                output.println(space + inname + ".read_" + array_name + "_array(" + tname + ", 0, " + tname + ".length);");
            }

            output.println(space + "}");
            break;

        case IdlType.e_ident :
            translate_unmarshalling_member(((IdlIdent) obj).internalObject(), output, inname, tname, space);
            break;
        }
    }

    /**
     * Encode a data type
     *
     * @param obj the member to encode
     * @param output write access
     * @param outname outputstream name
     * @param   tname data type name
     */
    public void translate_marshalling_data (IdlObject obj, java.io.PrintWriter output, String outname, String tname)
    {
        IdlSimple simple = null;

        switch (obj.kind())
        {

        case IdlType.e_simple :
            simple = (IdlSimple) obj;

            if (simple.internal() == Token.t_ValueBase)
            {
                output.println("((org.omg.CORBA_2_3.portable.OutputStream)" + outname + ").write_value(" + tname + ");");
                return;
            }

            output.print(outname + ".write");

            switch (simple.internal())
            {

            case Token.t_float :
                output.print("_float");
                break;

            case Token.t_double :
                output.print("_double");
                break;

            case Token.t_short :
                output.print("_short");
                break;

            case Token.t_ushort :
                output.print("_ushort");
                break;

            case Token.t_long :
                output.print("_long");
                break;

            case Token.t_ulong :
                output.print("_ulong");
                break;

            case Token.t_longlong :
                output.print("_longlong");
                break;

            case Token.t_ulonglong :
                output.print("_ulonglong");
                break;

            case Token.t_char :
                output.print("_char");
                break;

            case Token.t_wchar :
                output.print("_wchar");
                break;

            case Token.t_boolean :
                output.print("_boolean");
                break;

            case Token.t_octet :
                output.print("_octet");
                break;

            case Token.t_any :
                output.print("_any");
                break;

            case Token.t_typecode :
                output.print("_TypeCode");
                break;

            case Token.t_object :
                output.print("_Object");
                break;

            case Token.t_ValueBase :
                output.print("_value");
                break;
            }

            output.println("(" + tname + ");");
            break;

        case IdlType.e_fixed :
            output.println(outname + ".write_fixed(" + tname + ");");
            break;

        case IdlType.e_string :
            output.println(outname + ".write_string(" + tname + ");");
            break;

        case IdlType.e_wstring :
            output.println(outname + ".write_wstring(" + tname + ");");
            break;

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_enum :

        case IdlType.e_interface :

        case IdlType.e_native :

        case IdlType.e_forward_interface :

        case IdlType.e_value_box :

        case IdlType.e_value :

        case IdlType.e_forward_value :
            output.print(fullname(obj));
            output.println("Helper.write(" + outname + "," + tname + ");");
            break;

        case IdlType.e_typedef :
            output.print(fullname(obj));
            output.println("Helper.write(" + outname + "," + tname + ");");
            break;

        case IdlType.e_array :

        case IdlType.e_sequence :
            break;

        case IdlType.e_ident :
            translate_marshalling_data(((IdlIdent) obj).internalObject(), output, outname, tname);
            break;
        }
    }

    /**
     * Decode a data type
     *
     * @param obj the member to decode
     * @param output write access
     * @param inname inputstream name
     */
    public void translate_unmarshalling_data (IdlObject obj, java.io.PrintWriter output, String inname)
    {
        IdlSimple simple = null;

        switch (obj.kind())
        {

        case IdlType.e_simple :
            simple = (IdlSimple) obj;

            if (simple.internal() == Token.t_ValueBase)
            {
                output.println("((org.omg.CORBA_2_3.portable.InputStream)" + inname + ").read_value();");
                return;
            }

            output.print(inname + ".read");

            switch (simple.internal())
            {

            case Token.t_float :
                output.println("_float();");
                break;

            case Token.t_double :
                output.println("_double();");
                break;

            case Token.t_short :
                output.println("_short();");
                break;

            case Token.t_ushort :
                output.println("_ushort();");
                break;

            case Token.t_long :
                output.println("_long();");
                break;

            case Token.t_ulong :
                output.println("_ulong();");
                break;

            case Token.t_longlong :
                output.println("_longlong();");
                break;

            case Token.t_ulonglong :
                output.println("_ulonglong();");
                break;

            case Token.t_char :
                output.println("_char();");
                break;

            case Token.t_wchar :
                output.println("_wchar();");
                break;

            case Token.t_boolean :
                output.println("_boolean();");
                break;

            case Token.t_octet :
                output.println("_octet();");
                break;

            case Token.t_any :
                output.println("_any();");
                break;

            case Token.t_typecode :
                output.println("_TypeCode();");
                break;

            case Token.t_object :
                output.println("_Object();");
                break;
            }

            break;

        case IdlType.e_fixed :
            output.println(inname + ".read_fixed();");
            break;

        case IdlType.e_string :
            output.println(inname + ".read_string();");
            break;

        case IdlType.e_wstring :
            output.println(inname + ".read_wstring();");
            break;

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_enum :

        case IdlType.e_interface :

        case IdlType.e_native :

        case IdlType.e_forward_interface :

        case IdlType.e_value_box :

        case IdlType.e_value :

        case IdlType.e_forward_value :
            output.print(fullname(obj));
            output.println("Helper.read(" + inname + ");");
            break;

        case IdlType.e_typedef :
            output.print(fullname(obj));
            output.println("Helper.read(" + inname + ");");
            break;

        case IdlType.e_array :

        case IdlType.e_sequence :
            /*
             int val = space.length()-1;
             output.println(space+"int size"+val+" = "+inname+".read_ulong();");
             output.print(" = new ");

             obj.reset();
             translate_type(obj.current(),output);

             output.println("[size"+val+"];");

             output.println(space+"for (int i"+ val +"=0; i"+ val +"<"+tname+".length; i"+val+"++)");
             output.println(space+"{");
             translate_unmarshalling_member(obj.current(),output,inname,tname+"[i"+val+"]",space+tab + "");
             output.println("");
             output.println(space+"}");
            */
            break;

        case IdlType.e_ident :
            translate_unmarshalling_data(((IdlIdent) obj).internalObject(), output, inname);
            break;
        }
    }

    /**
     * Encode a data type
     *
     * @param obj the data type to encode
     * @param output write access
     * @param outname outputstream name
     * @param tname data type name
     */
    public void translate_marshalling (IdlObject obj, java.io.PrintWriter output, String outname, String tname)
    {
        int i;
        int idx;

        switch (obj.kind())
        {

        case IdlType.e_simple :

            if (((IdlSimple) obj).internal() == Token.t_typecode)
                output.println(tab2 + "" + outname + ".write_TypeCode(" + tname + ");");

            break;

        case IdlType.e_enum :
            output.println(tab2 + "" + outname + ".write_ulong(" + tname + ".value());");

            break;

        case IdlType.e_struct :
            obj.reset();

            while (obj.end() != true)
            {
                obj.current().reset();
                translate_marshalling_member(obj.current().current(), output, outname, tname + "." + obj.current().name(), tab2 + "");
                obj.next();
            }

            break;

        case IdlType.e_union :
            idx = ((IdlUnion) obj).index();
            obj.reset();

            obj.current().reset();
            boolean enumeration = false;

            if (final_kind(obj.current().current()) == IdlType.e_enum)
                enumeration = true;

            IdlObject d = obj.current().current();

            translate_marshalling_member(obj.current().current(), output, outname, tname + "._" + obj.current().name(), tab2 + "");

            obj.next();

            String discrim = null;

            if (((IdlUnionMember) obj.current()).getExpression().equals("true ") ||
                    ((IdlUnionMember) obj.current()).getExpression().equals("false "))
            {
                discrim = tname + ".toInt()";
            }
            else
            {
                if (enumeration)
                    discrim = tname + ".__d.value()";
                else
                    discrim = tname + ".__d";
            }

            i = 0;

            while (obj.end() != true)
            {
                if (i != idx)
                {
                    output.print(tab2 + "if (" + discrim + " ==");

                    if (((IdlUnionMember) obj.current()).getExpression().equals("true "))
                        output.println(" 1)");
                    else
                        if (((IdlUnionMember) obj.current()).getExpression().equals("false "))
                            output.println(" 0)");
                        else
                        {
                            if (!enumeration)
                            {
                                output.print("(");
                                translate_type(d, output);
                                output.print(")");
                            }

                            output.println(" " + translate_to_java_expression(((IdlUnionMember) obj.current()).getExpression(), false, ((IdlUnionMember) obj.current())) + ")");
                        }

                    output.println(tab2 + "{");

                    if (((IdlUnionMember) obj.current()).isAsNext() == false)
                    {
                        obj.current().reset();
                        translate_marshalling_member(obj.current().current(), output, outname, tname + "._" + obj.current().name(), tab3 + "");
                    }
                    else
                    {
                        IdlObject next = getAsNext(obj);

                        next.reset();
                        translate_marshalling_member(next.current(), output, outname, tname + "._" + obj.current().name(), tab3 + "");
                    }

                    output.println(tab2 + "}");
                }

                obj.next();

                if ((obj.end() != true) && ((i + 1) != idx))
                    output.println(tab2 + "else");

                i++;
            }

            if (idx != -1)
            {
                i = 0;
                obj.reset();
                obj.next();

                while (obj.end() != true)
                {
                    if (i == idx)
                    {
                        if (obj.length() != 2)
                            output.println(tab2 + "else");

                        output.println(tab2 + "{");

                        translate_marshalling_member(obj.current().current(), output, outname, tname + "._" + obj.current().name(), tab3 + "");

                        output.println(tab2 + "}");
                    }

                    obj.next();
                    i++;
                }
            }

            break;

        case IdlType.e_typedef :
            obj.reset();

            switch (obj.current().kind())
            {

            case IdlType.e_string :

            case IdlType.e_wstring :

            case IdlType.e_simple :

            case IdlType.e_sequence :

            case IdlType.e_array :

            case IdlType.e_fixed :
                translate_marshalling_member(obj.current(), output, outname, tname, tab2 + "");
                break;

            default :
                translate_marshalling(obj.current(), output, outname, tname);
            }

            break;

        case IdlType.e_ident :
            translate_marshalling(((IdlIdent) obj).internalObject(), output, outname, tname);
            break;

        case IdlType.e_exception :
            output.println(tab2 + "" + outname + ".write_string(id());");
            obj.reset();

            while (obj.end() != true)
            {
                obj.current().reset();
                translate_marshalling_member(obj.current().current(), output, outname, tname + "." + obj.current().name(), tab2 + "");
                obj.next();
            }

            break;

        case IdlType.e_native :
            output.println(tab2 + "throw new org.omg.CORBA.MARSHAL();");
            break;

        case IdlType.e_interface :

        case IdlType.e_forward_interface :

            if (((IdlInterface) obj).local_interface())
            {
                output.println(tab2 + "throw new org.omg.CORBA.MARSHAL();");
                break;
            }

            if (((IdlInterface) obj).abstract_interface())
                output.println(tab2 + "((org.omg.CORBA_2_3.portable.OutputStream)" + outname + ").write_abstract_interface(" + tname + ");");
            else
                output.println(tab2 + "" + outname + ".write_Object((org.omg.CORBA.portable.ObjectImpl)" + tname + ");");

            break;

        case IdlType.e_value_box :
            obj.reset();

            if (((IdlValueBox) obj).simple())
            {
                output.println(tab2 + "" + obj.name() + " _box = (" + obj.name() + ")" + tname + ";");
                translate_marshalling_member(obj.current(), output, outname, "_box.value", tab2 + "");
            }
            else
            {
                output.print(tab2 + "");
                translate_type(obj.current(), output);
                output.print(" _box = (");
                translate_type(obj.current(), output);
                output.println(")" + tname + ";");
                translate_marshalling_member(obj.current(), output, outname, "_box", tab2 + "");
            }

            break;

        case IdlType.e_value :

            if (((IdlValue) obj).abstract_value())
                output.println(tab2 + "((org.omg.CORBA_2_3.portable.OutputStream)" + outname + ").write_value(" + tname + ", _id);");
            else
                output.println(tab2 + "((org.omg.CORBA_2_3.portable.OutputStream)" + outname + ").write_value(" + tname + ", _id);");

            break;
        }
    }

    /**
     * Return the typed member for an union member
     */
    private IdlObject getAsNext(IdlObject obj)
    {
        int p = obj.pos();

        while (obj.end() != true)
        {
            IdlUnionMember member = (IdlUnionMember) obj.current();

            if (member.isAsNext() == false)
            {
                obj.pos(p);
                return member;
            }

            obj.next();
        }

        obj.pos(p);
        return null;
    }

    /**
     * Decode a data type
     *
     * @param obj the data type to decode
     * @param output write access
     * @param inname inputstream name
     */
    public void translate_unmarshalling (IdlObject obj, java.io.PrintWriter output, String inname)
    {
        int i;
        int idx;

        switch (obj.kind())
        {

        case IdlType.e_simple :

            if (((IdlSimple) obj).internal() == Token.t_typecode)
                output.println(tab2 + "return " + inname + ".read_TypeCode();");

            break;

        case IdlType.e_enum :
            output.println(tab2 + "return " + obj.name() + ".from_int(" + inname + ".read_ulong());");

            break;

        case IdlType.e_struct :
            obj.reset();

            output.println(tab2 + "" + fullname(obj) + " new_one = new " + fullname(obj) + "();");

            output.println("");

            while (obj.end() != true)
            {
                obj.current().reset();
                translate_unmarshalling_member(obj.current().current(), output, inname, "new_one." + obj.current().name(), tab2 + "");
                obj.next();
            }

            output.println("");
            output.println(tab2 + "return new_one;");
            break;

        case IdlType.e_union :
            idx = ((IdlUnion) obj).index();
            obj.reset();
            output.println(tab2 + "" + fullname(obj) + " new_one = new " + fullname(obj) + "();");
            output.println("");

            obj.current().reset();
            boolean enumeration = false;

            if (final_kind(obj.current().current()) == IdlType.e_enum)
                enumeration = true;

            IdlObject d = obj.current().current();

            translate_unmarshalling_member(obj.current().current(), output, inname, "new_one._" + obj.current().name(), tab2 + "");

            obj.next();

            String discrim = null;

            if (((IdlUnionMember) obj.current()).getExpression().equals("true ") ||
                    ((IdlUnionMember) obj.current()).getExpression().equals("false "))
            {
                discrim = "new_one.toInt()";
            }
            else
            {
                if (enumeration)
                    discrim = "new_one.__d.value()";
                else
                    discrim = "new_one.__d";
            }

            i = 0;

            while (obj.end() != true)
            {
                if (i != idx)
                {
                    output.print(tab2 + "if (" + discrim + " == ");

                    if (((IdlUnionMember) obj.current()).getExpression().equals("true "))
                        output.println("1)");
                    else
                        if (((IdlUnionMember) obj.current()).getExpression().equals("false "))
                            output.println("0)");
                        else
                        {
                            if (!enumeration)
                            {
                                output.print("(");
                                translate_type(d, output);
                                output.print(")");
                            }

                            output.println(translate_to_java_expression(((IdlUnionMember) obj.current()).getExpression(), false, ((IdlUnionMember) obj.current())) + ")");
                        }

                    output.println(tab2 + "{");

                    if (((IdlUnionMember) obj.current()).isAsNext() == false)
                    {
                        obj.current().reset();
                        translate_unmarshalling_member(obj.current().current(), output, inname, "new_one._" + obj.current().name(), tab3 + "");
                    }
                    else
                    {
                        IdlObject next = getAsNext(obj);

                        next.reset();
                        translate_unmarshalling_member(next.current(), output, inname, "new_one._" + obj.current().name(), tab3 + "");
                    }

                    output.println(tab2 + "}");

                }

                obj.next();

                if ((obj.end() != true) && ((i + 1) != idx))
                    output.println(tab2 + "else");

                i++;
            }

            i = 0;
            obj.reset();
            obj.next();

            while (obj.end() != true)
            {
                if (i == idx)
                {
                    if (obj.length() != 2)
                        output.println(tab2 + "else");

                    output.println(tab2 + "{");

                    obj.current().reset();

                    translate_unmarshalling_member(obj.current().current(), output, inname, "new_one._" + obj.current().name(), tab3 + "");

                    output.println(tab2 + "}");

                }

                obj.next();

                i++;
            }

            output.println("");
            output.println(tab2 + "return new_one;");
            break;

        case IdlType.e_typedef :
            obj.reset();

            switch (obj.current().kind())
            {

            case IdlType.e_string :

            case IdlType.e_wstring :

            case IdlType.e_simple :

            case IdlType.e_sequence :

            case IdlType.e_fixed :

            case IdlType.e_array :
                output.print(tab2 + "");
                translate_type(obj.current(), output);
                output.println(" new_one;");
                translate_unmarshalling_member(obj.current(), output, inname, "new_one", tab2 + "");
                output.println("");
                output.println(tab2 + "return new_one;");
                break;

            default :
                translate_unmarshalling(obj.current(), output, inname);
            }

            break;

        case IdlType.e_ident :
            translate_unmarshalling(((IdlIdent) obj).internalObject(), output, inname);
            break;

        case IdlType.e_exception :
            obj.reset();
            output.println(tab2 + "" + fullname(obj) + " new_one = new " + fullname(obj) + "();");
            output.println("");
            output.println(tab2 + "if (!" + inname + ".read_string().equals(id()))");
            output.println(tab2 + " throw new org.omg.CORBA.MARSHAL();");

            while (obj.end() != true)
            {
                obj.current().reset();
                translate_unmarshalling_member(obj.current().current(), output, inname, "new_one." + obj.current().name(), tab2 + "");
                obj.next();
            }

            output.println("");
            output.println(tab2 + "return new_one;");
            break;

        case IdlType.e_native :
            output.println(tab2 + "throw new org.omg.CORBA.MARSHAL();");
            break;

        case IdlType.e_interface :

        case IdlType.e_forward_interface :

            if (((IdlInterface) obj).local_interface())
            {
                output.println(tab2 + "throw new org.omg.CORBA.MARSHAL();");
                break;
            }

            if (((IdlInterface) obj).abstract_interface())
            {
                output.print(tab2 + "Object new_one = ((org.omg.CORBA_2_3.portable.InputStream)" + inname + ").read_abstract_interface(");

                String stubname = fullname(obj);

                if (stubname.lastIndexOf(".") != -1)
                    stubname = stubname.substring(0, stubname.lastIndexOf(".") + 1);
                else
                    stubname = "";

                stubname = stubname + "_" + obj.name() + "Stub";

                output.println(stubname + ".class);");

                output.println(tab2 + "return (" + fullname(obj) + ") new_one;");
            }
            else
            {
                String stubname = fullname(obj);

                if (stubname.lastIndexOf(".") != -1)
                    stubname = stubname.substring(0, stubname.lastIndexOf(".") + 1);
                else
                    stubname = "";

                stubname = stubname + "_" + obj.name() + "Stub";

                output.println(tab2 + "return(" + fullname(obj) + ")" + inname + ".read_Object(" + stubname + ".class);");
            }

            break;

        case IdlType.e_value :

            if (((IdlValue) obj).abstract_value())
                output.println(tab2 + "return (" + obj.name() + ") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value(_id);");
            else
                output.println(tab2 + "return (" + obj.name() + ") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value(_id);");

            break;

        case IdlType.e_value_box :
            obj.reset();

            if (((IdlValueBox) obj).simple())
            {
                if (is_boolean(obj.current()))
                    output.println(tab2 + "" + fullname(obj) + " _box = new " + fullname(obj) + "(false);");
                else
                {
                    output.print( tab2 + "" + fullname( obj ) + " _box = new " + fullname( obj ) + "((" );
                    translate_type(obj.current(), output);
                    output.println( ")0);" );
                }

                translate_unmarshalling_member(obj.current(), output, inname, "_box.value", tab2 + "");
            }
            else
            {
                output.print(tab2 + "");
                translate_type(obj.current(), output);
                output.println(" _box = null;");

                translate_unmarshalling_member(obj.current(), output, inname, "_box", tab2 + "");
            }

            output.println(tab2 + "return _box;");
            break;
        }
    }

    private boolean is_boolean(IdlObject obj)
    {
        switch (final_kind(obj))
        {

        case IdlType.e_simple :

            if (((IdlSimple) obj).internal() == Token.t_boolean)
                return true;

        default :
            return false;
        }
    }

    private boolean queryUseLongLiteral(final IdlObject obj) {
        switch (obj.kind()) {
            case IdlType.e_simple :
                switch (((IdlSimple)obj).internal()) {
                    case Token.t_float :
                    case Token.t_double :
                        return false;
                    default :
                        return true;
                }
            case IdlType.e_ident :
                return queryUseLongLiteral(((IdlIdent)obj).internalObject());
            case IdlType.e_typedef :
                obj.reset();
                return queryUseLongLiteral(obj.current());
            default :
                return false;
        }
    }

    /**
     * Translate a constant
     *
     * @param obj the constant to translate
     * @param writeInto the directory where the constant must be defined
     */
    public void translate_constant(IdlObject obj, java.io.File writeInto, java.io.PrintWriter print)
    {
        IdlConst const_obj = (IdlConst) obj;

        java.io.PrintWriter output = null;

        final boolean fixed = IdlType.e_fixed == final_kind(obj.current());
        final boolean useLongLiteral = queryUseLongLiteral(obj.current());

        if ((const_obj.into(IdlType.e_interface) == true) || (const_obj.into(IdlType.e_value)))
        {

            print.println(tab + "/**");
            print.println(tab + " * Constant value");
            print.println(tab + " */");
            print.print(tab + "public static final ");

            translate_type(obj.current(), print);

            print.print(" " + obj.name() + " = ");

            if (!fixed)
            {
                print.print("(");
                obj.reset();
                translate_type(obj.current(), print);
                print.print(") ");
            }

            print.print("(");
            print.print(translateToJavaExpression(const_obj.expression(), fixed,
                    useLongLiteral, const_obj));
            print.println(");");
            print.println("");

        }
        else
        {
            output = newFile(writeInto, obj.name());

            addDescriptiveHeader(output, obj);

            output.println("public interface " + obj.name());
            output.println("{");
            output.println(tab + "/**");
            output.println(tab + " * Constant value");
            output.println(tab + " */");
            output.print(tab + " public static final ");

            translate_type(obj.current(), output);

            output.print(" value = ");

            if (!fixed)
            {
                output.print(" (");
                obj.reset();
                translate_type(obj.current(), output);
                output.print(") ");
            }

            output.print("(");
            output.print(translateToJavaExpression(const_obj.expression(), fixed,
                    useLongLiteral, const_obj));
            output.println(");");

            output.println("}");

            output.close();
        }
    }

    private String translateToJavaExpression(final String expr, final boolean fixed,
            final boolean useLongLiteral, final IdlObject obj) {

        String newExpr = idlScopeToJavaScope(expr, true, fixed, useLongLiteral, obj);

        newExpr = IdlEspaceCharToJavaEscapeChar(newExpr);

        if (fixed)
            newExpr = "new java.math.BigDecimal(\"" + newExpr + "\")";

        return newExpr;

    }

    /**
     * This function is used to test if an interface has some inherits from bases abstracts
     * interfaces
     */
    public boolean isAbstractBaseInterface(IdlObject obj)
    {
        IdlInterface itf = (IdlInterface) obj;

        List base = itf.getInheritance();

        for (int i = 0; i < base.size(); i++)
            if (((IdlInterface) (base.get(i))).abstract_interface())
                return true;

        for (int i = 0; i < base.size(); i++)
            if (isAbstractBaseInterface((IdlInterface) (base.get(i))))
                return true;

        return false;
    }

    /**
     * Add a Helper for a data type
     *
     * @param obj the object to translate
     * @param writeInto the directory where the object must be defined
     */
    public void write_helper(IdlObject obj, java.io.File writeInto)
    {
        java.io.PrintWriter output = newFile(writeInto, obj.name() + "Helper");
        boolean real_corba_object = false;
        boolean abstract_object = false;

        if (current_pkg != null)
        {
            if (current_pkg.equals("generated"))
            {
                if (m_cp.getM_use_package() == true)
                {
                    output.println("package " + current_pkg + ";");
                    output.println("");
                }
            }
            else
                if (!current_pkg.equals(""))
                {
                    output.println("package " + current_pkg + ";");
                    output.println("");
                }
        }

        output.println("/** ");
        output.println(" * Helper class for : " + obj.name());
        output.println(" *  ");
        output.println(" * @author OpenORB Compiler");
        output.println(" */ ");

        // Define the Helper class
        if (obj.kind() == IdlType.e_value_box)
            output.println("public class " + obj.name() + "Helper implements org.omg.CORBA.portable.BoxedValueHelper");
        else
            output.println("public class " + obj.name() + "Helper");

        output.println("{");

        switch (final_kind(obj))
        {

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_exception :

        case IdlType.e_sequence :

        case IdlType.e_array :

        case IdlType.e_fixed :
            // Test for presence of OpenORB any class.
            if (!m_cp.getM_portableHelper())
            {
                output.println(tab + "private static final boolean HAS_OPENORB;");
                output.println(tab + "static");
                output.println(tab + "{");
                output.println(tab2 + "boolean hasOpenORB = false;");
                output.println(tab2 + "try");
                output.println(tab2 + "{");
                output.println(tab3 + "Thread.currentThread().getContextClassLoader().loadClass( \"org.openorb.orb.core.Any\" );");
                output.println(tab3 + "hasOpenORB = true;");
                output.println(tab2 + "}");
                output.println(tab2 + "catch ( ClassNotFoundException ex )");
                output.println(tab2 + "{");
                output.println(tab3 + "// do nothing");
                output.println(tab2 + "}");
                output.println(tab2 + "HAS_OPENORB = hasOpenORB;");
                output.println(tab + "}");
            }
            else
            {
                output.println(tab + "/** extract_X methods found for the current ORBs Any type. */" );
                output.println(tab + "private static java.lang.Object [] _extractMethods;" );
                output.println(tab + "" );
//                output.println(tab + "/** Flag whether we are running on JDK 1.4. */" );
//                output.println(tab + "public static final boolean V1_4;" );
//                output.println(tab + "" );
                output.println(tab + "static" );
                output.println(tab + "{" );
/*
                output.println(tab2 + "final Class[] parameterTypes = { Throwable.class };" );
                output.println(tab2 + "boolean is1_4 = false;" );
                output.println(tab2 + "try" );
                output.println(tab2 + "{" );
                output.println(tab3 + "Throwable.class.getMethod( \"initCause\", parameterTypes );" );
                output.println(tab3 + "is1_4 = true;" );
                output.println(tab2 + "}" );
                output.println(tab2 + "catch ( final NoSuchMethodException e )" );
                output.println(tab2 + "{" );
                output.println(tab3 + "// now we know that this is a JDK older than 1.4" );
                output.println(tab2 + "}" );
                output.println(tab2 + "V1_4 = is1_4;" );
                output.println(tab2 + "" );
*/
                output.println(tab2 + "try");
                output.println(tab2 + "{");
                output.println(tab3 + "Class clz = Thread.currentThread().getContextClassLoader().loadClass( \"org.openorb.orb.core.Any\" );");
                output.println(tab3 + "java.lang.reflect.Method meth = clz.getMethod( \"extract_Streamable\", null );");
                output.println(tab3 + "_extractMethods = new java.lang.Object[] { clz, meth };");
                output.println(tab2 + "}");
                output.println(tab2 + "catch ( Exception ex )");
                output.println(tab2 + "{");
                output.println(tab3 + "// do nothing");
                output.println(tab2 + "}");
                output.println(tab2 + "");
                output.println(tab2 + "if ( _extractMethods == null )");
                output.println(tab2 + "{");
                output.println(tab3 + "_extractMethods = new java.lang.Object[ 0 ];");
                output.println(tab2 + "}");
                output.println(tab + "}");
                output.println();
                output.println(tab + "private static java.lang.reflect.Method getExtract( Class clz )");
                output.println(tab + "{");
                output.println(tab2 + "int len = _extractMethods.length;");
                output.println(tab2 + "for ( int i = 0; i < len; i += 2 )");
                output.println(tab2 + "{");
                output.println(tab3 + "if ( clz.equals( _extractMethods[ i ] ) )");
                output.println(tab3 + "{");
                output.println(tab4 + "return ( java.lang.reflect.Method ) _extractMethods[ i + 1 ];");
                output.println(tab3 + "}");
                output.println(tab2 + "}");
                output.println(tab2 + "");
                output.println(tab2 + "// unknown class, look for method.");
                output.println(tab2 + "synchronized ( org.omg.CORBA.Any.class )");
                output.println(tab2 + "{");
                output.println(tab3 + "for ( int i = len; i < _extractMethods.length; i += 2 )");
                output.println(tab3 + "{");
                output.println(tab4 + "if ( clz.equals( _extractMethods[ i ] ) )");
                output.println(tab4 + "{");
                output.println(tab5 + "return ( java.lang.reflect.Method ) _extractMethods[ i + 1 ];");
                output.println(tab4 + "}");
                output.println(tab3 + "}");
                output.println(tab3 + "");
                output.println(tab3 + "java.lang.Object [] tmp = new java.lang.Object[ _extractMethods.length + 2 ];");
                output.println(tab3 + "System.arraycopy( _extractMethods, 0, tmp, 0, _extractMethods.length );");
                output.println(tab3 + "tmp[ _extractMethods.length ] = clz;");
                output.println(tab3 + "try");
                output.println(tab3 + "{");
                output.println(tab4 + "tmp[ _extractMethods.length + 1 ] = clz.getMethod( \"extract_Streamable\", null );");
                output.println(tab3 + "}");
                output.println(tab3 + "catch ( Exception ex )");
                output.println(tab3 + "{");
                output.println(tab4 + "// do nothing");
                output.println(tab3 + "}");
                output.println(tab3 + "_extractMethods = tmp;");
                output.println(tab3 + "return ( java.lang.reflect.Method )_extractMethods[ _extractMethods.length - 1 ];");
                output.println(tab2 + "}");
                output.println(tab + "}");
            }
        }

        // The method  insert
        output.println(tab + "/**");

        output.println(tab + " * Insert " + obj.name() + " into an any");

        output.println(tab + " * @param a an any");

        output.println(tab + " * @param t " + obj.name() + " value");

        output.println(tab + " */");

        output.print(tab + "public static void insert(org.omg.CORBA.Any a, ");

        translate_type(obj, output);

        output.println(" t)");

        output.println(tab + "{");

        switch (final_kind(obj))
        {

        case IdlType.e_interface :

            if (((IdlInterface) final_type(obj)).abstract_interface())
            {
                output.println(tab2 + "if ( t instanceof org.omg.CORBA.Object )");
                output.println(tab3 + "a.insert_Object( ( org.omg.CORBA.Object ) t , type() );");
                output.println(tab2 + "else if(t instanceof java.io.Serializable)");
                output.println(tab3 + "a.insert_Value((java.io.Serializable)t, type());");
                output.println(tab2 + "else");
                output.println(tab3 + "throw new org.omg.CORBA.BAD_PARAM();");
            }
            else
                output.println(tab2 + "a.insert_Object(t , type());");

            break;

        case IdlType.e_value:

        case IdlType.e_value_box:
            output.println(tab2 + "a.insert_Value(t, type());");

            break;

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_exception :
            output.print(tab2 + "a.insert_Streamable(new ");

            translate_type(obj, output);

            output.println("Holder(t));");

            break;

        case IdlType.e_sequence :

        case IdlType.e_array :

        case IdlType.e_fixed :
            output.println(tab2 + "a.insert_Streamable(new " + fullname(obj) + "Holder(t));");

            break;

        default:
            output.println(tab2 + "a.type(type());");

            output.println(tab2 + "write(a.create_output_stream(),t);");
        }

        output.println(tab + "}");
        output.println("");

        // The method  extract
        output.println(tab + "/**");
        output.println(tab + " * Extract " + obj.name() + " from an any");
        output.println(tab + " *");
        output.println(tab + " * @param a an any");
        output.println(tab + " * @return the extracted " + obj.name() + " value");
        output.println(tab + " */");
        output.print(tab + "public static ");

        translate_type(obj, output);

        output.println(" extract( org.omg.CORBA.Any a )");
        output.println(tab + "{");
        output.println(tab2 + "if ( !a.type().equivalent( type() ) )");
        output.println(tab2 + "{");
        output.println(tab3 + "throw new org.omg.CORBA.MARSHAL();");
        output.println(tab2 + "}");

        switch (final_kind(obj))
        {

        case IdlType.e_interface :
            output.println(tab2 + "try");
            output.println(tab2 + "{");
            output.print(tab3 + "return ");
            translate_type(obj, output);
            output.println("Helper.narrow( a.extract_Object() );");
            output.println(tab2 + "}");
            output.println(tab2 + "catch ( final org.omg.CORBA.BAD_PARAM e )");
            output.println(tab2 + "{");

            writeThrowException(output, tab3, "org.omg.CORBA.MARSHAL", "e.getMessage()", "e");

            output.println(tab2 + "}");

            if (!((IdlInterface) final_type(obj)).abstract_interface())
                break;

            output.println(tab2 + "catch ( org.omg.CORBA.BAD_OPERATION ex )");
            output.println(tab2 + "{");
            output.println(tab3 + "// do nothing");
            output.println(tab2 + "}");

            // fallthrough

        case IdlType.e_value:

        case IdlType.e_value_box:
            output.println(tab2 + "try");
            output.println(tab2 + "{");

            output.print(tab3 + "return (");

            translate_type(obj, output);

            output.println(") a.extract_Value();");

            output.println(tab2 + "}");

            output.println(tab2 + "catch ( final ClassCastException e )");
            output.println(tab2 + "{");


            writeThrowException(output, tab3, "org.omg.CORBA.MARSHAL", "e.getMessage()", "e");

            output.println(tab2 + "}");

            break;

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_exception :
            if (!m_cp.getM_portableHelper())
            {
                output.println(tab2 + "if (HAS_OPENORB && a instanceof org.openorb.orb.core.Any) {");
                output.println(tab3 + "// streamable extraction. The jdk stubs incorrectly define the Any stub");
                output.println(tab3 + "org.openorb.orb.core.Any any = (org.openorb.orb.core.Any)a;");
                output.println(tab3 + "try {");
                output.println(tab4 + "org.omg.CORBA.portable.Streamable s = any.extract_Streamable();");
            }
            else
            {
/*
                output.println(tab3 + "if ( V1_4 )");
                output.println(tab3 + "{");
                output.println(tab4 + "return read( a.create_input_stream() );");
                output.println(tab3 + "}");
*/
                output.println(tab3 + "// streamable extraction. The jdk stubs incorrectly define the Any stub");
                output.println(tab2 + "java.lang.reflect.Method meth = getExtract( a.getClass() );");
                output.println(tab2 + "if ( meth != null )");
                output.println(tab2 + "{");
                output.println(tab3 + "try");
                output.println(tab3 + "{");
                output.println(tab4 + "org.omg.CORBA.portable.Streamable s =");
                output.println(tab5 + "( org.omg.CORBA.portable.Streamable ) meth.invoke( a, null );");
            }

            output.print( tab4 + "if ( s instanceof " );
            translate_type( obj, output );
            output.println( "Holder )" );
            output.print( tab5 + "return ( ( " );
            translate_type( obj, output );
            output.println( "Holder ) s ).value;" );

            if (!m_cp.getM_portableHelper())
            {
                output.println(tab3 + "}");
                output.println(tab3 + "catch ( org.omg.CORBA.BAD_INV_ORDER ex )");
                output.println(tab3 + "{");
            }
            else
            {
                output.println(tab3 + "}");
                output.println(tab3 + "catch ( final IllegalAccessException e )");
                output.println(tab3 + "{");

                writeThrowException(output, tab4, "org.omg.CORBA.INTERNAL", "e.toString()", "e");

                output.println(tab3 + "}");
                output.println(tab3 + "catch ( final IllegalArgumentException e )");
                output.println(tab3 + "{");

                writeThrowException(output, tab4, "org.omg.CORBA.INTERNAL", "e.toString()", "e");

                output.println(tab3 + "}");
                output.println(tab3 + "catch ( final java.lang.reflect.InvocationTargetException e )");
                output.println(tab3 + "{");
                output.println(tab4 + "Throwable rex = e.getTargetException();");
                output.println(tab4 + "if ( rex instanceof org.omg.CORBA.BAD_INV_ORDER )");
                output.println(tab4 + "{");
                output.println(tab5 + "// do nothing");
                output.println(tab4 + "}");
                output.println(tab4 + "else if ( rex instanceof Error )");
                output.println(tab4 + "{");
                output.println(tab5 + "throw ( Error ) rex;");
                output.println(tab4 + "}");
                output.println(tab4 + "else if ( rex instanceof RuntimeException )");
                output.println(tab4 + "{");
                output.println(tab5 + "throw ( RuntimeException ) rex;");
                output.println(tab4 + "}");
                output.println(tab4 + "else");
                output.println(tab4 + "{");

//                writeThrowException(output, tab5, "org.omg.CORBA.INTERNAL", "rex.toString()", "rex");

                output.println(tab4 + "}");
            }
            output.println(tab3 + "}");

            output.print( tab3 + "" );
            translate_type( obj, output );
            output.print( "Holder h = new " );
            translate_type( obj, output );
            output.println("Holder( read( a.create_input_stream() ) );");
            output.println(tab3 + "a.insert_Streamable( h );");
            output.println(tab3 + "return h.value;");
            output.println(tab2 + "}");
            output.println(tab2 + "return read( a.create_input_stream() );");
            break;

        case IdlType.e_sequence :

        case IdlType.e_array :

        case IdlType.e_fixed :

            if (!m_cp.getM_portableHelper())
            {
                output.println(tab2 + "if ( HAS_OPENORB && a instanceof org.openorb.orb.core.Any )");
                output.println(tab2 + "{");
                output.println(tab3 + "// streamable extraction. The jdk stubs incorrectly define the Any stub");
                output.println(tab3 + "org.openorb.orb.core.Any any = ( org.openorb.orb.core.Any ) a;");
                output.println(tab3 + "try");
                output.println(tab3 + "{");
                output.println(tab4 + "org.omg.CORBA.portable.Streamable s = any.extract_Streamable();");
            }
            else
            {
/*
                output.println(tab2 + "if ( V1_4 )");
                output.println(tab2 + "{");
                output.println(tab3 + "return read( a.create_input_stream() );");
                output.println(tab2 + "}");
*/
                output.println(tab2 + "// streamable extraction. The jdk stubs incorrectly define the Any stub");
                output.println(tab2 + "java.lang.reflect.Method meth = getExtract( a.getClass() );");
                output.println(tab2 + "if ( meth != null )");
                output.println(tab2 + "{");
                output.println(tab3 + "try");
                output.println(tab3 + "{");
                output.println(tab4 + "org.omg.CORBA.portable.Streamable s ");
                output.println(tab5 + "= ( org.omg.CORBA.portable.Streamable ) meth.invoke( a, null );");
            }

            output.println(tab4 + "if ( s instanceof " + fullname(obj) + "Holder )");
            output.println(tab4 + "{");
            output.println(tab5 + "return ( ( " + fullname(obj) + "Holder ) s ).value;");
            output.println(tab4 + "}");

            if (!m_cp.getM_portableHelper())
            {
                output.println(tab3 + "}");
                output.println(tab3 + "catch ( org.omg.CORBA.BAD_INV_ORDER ex )");
                output.println(tab3 + "{");
            }
            else
            {
                output.println(tab3 + "}");
                output.println(tab3 + "catch ( final IllegalAccessException e )");
                output.println(tab3 + "{");

                writeThrowException(output, tab4, "org.omg.CORBA.INTERNAL", "e.toString()", "e");

                output.println(tab3 + "}");
                output.println(tab3 + "catch ( final IllegalArgumentException e )");
                output.println(tab3 + "{");

                writeThrowException(output, tab4, "org.omg.CORBA.INTERNAL", "e.toString()", "e");

                output.println(tab3 + "}");
                output.println(tab3 + "catch ( final java.lang.reflect.InvocationTargetException e )");
                output.println(tab3 + "{");
                output.println(tab4 + "final Throwable rex = e.getTargetException();");
                output.println(tab4 + "if ( rex instanceof org.omg.CORBA.BAD_INV_ORDER )");
                output.println(tab4 + "{");
                output.println(tab5 + "// do nothing");
                output.println(tab4 + "}");
                output.println(tab4 + "else if ( rex instanceof Error )");
                output.println(tab4 + "{");
                output.println(tab5 + "throw ( Error ) rex;");
                output.println(tab4 + "}");
                output.println(tab4 + "else if ( rex instanceof RuntimeException )");
                output.println(tab4 + "{");
                output.println(tab5 + "throw ( RuntimeException ) rex;");
                output.println(tab4 + "}");

//                writeThrowException(output, tab4, "org.omg.CORBA.INTERNAL", "rex.toString()", "rex");
            }
            output.println(tab3 + "}");
            output.println(tab3 + "" + fullname(obj) + "Holder h = new " + fullname(obj) + "Holder( read( a.create_input_stream() ) );");
            output.println(tab3 + "a.insert_Streamable( h );");
            output.println(tab3 + "return h.value;");
            output.println(tab2 + "}");
            output.println(tab2 + "return read( a.create_input_stream() );");
            break;

        default:
            output.println(tab2 + "return read( a.create_input_stream() );");
        }

        output.println(tab + "}");
        output.println("");

        // The method static _tc
        output.println(tab + "//");
        output.println(tab + "// Internal TypeCode value");
        output.println(tab + "//");
        output.println(tab + "private static org.omg.CORBA.TypeCode _tc = null;");

        switch (final_kind(obj))
        {

        case IdlType.e_value:

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_exception :
            output.println(tab + "private static boolean _working = false;");
        }

        output.println("");

        // The method type
        output.println(tab + "/**");
        output.println(tab + " * Return the " + obj.name() + " TypeCode");
        output.println(tab + " * @return a TypeCode");
        output.println(tab + " */");
        output.println(tab + "public static org.omg.CORBA.TypeCode type()");
        output.println(tab + "{");
        output.println(tab2 + "if (_tc == null) {");

        switch (final_kind(obj))
        {

        case IdlType.e_value:

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_exception :
            output.println(tab3 + "synchronized(org.omg.CORBA.TypeCode.class) {");
            output.println(tab4 + "if (_tc != null)");
            output.println(tab5 + "return _tc;");
            output.println(tab4 + "if (_working)");
            output.println(tab5 + "return org.omg.CORBA.ORB.init().create_recursive_tc(id());");
            output.println(tab4 + "_working = true;");
        }

        translate_new_typecode(obj, output);

        switch (final_kind(obj))
        {

        case IdlType.e_value:

        case IdlType.e_struct :

        case IdlType.e_union :

        case IdlType.e_exception :
            output.println(tab4 + "_working = false;");
            output.println(tab3 + "}");
        }

        output.println(tab2 + "}");
        output.println(tab2 + "return _tc;");
        output.println(tab + "}");
        output.println("");

        // The method id
        output.println(tab + "/**");
        output.println(tab + " * Return the " + obj.name() + " IDL ID");
        output.println(tab + " * @return an ID");
        output.println(tab + " */");
        output.println(tab + "public static String id()");
        output.println(tab + "{");
        output.println(tab2 + "return _id;");
        output.println(tab + "}");
        output.println("");
        output.println(tab + "private final static String _id = \"" + obj.getId() + "\";");
        output.println("");

        // The method read
        output.println(tab + "/**");
        output.println(tab + " * Read " + obj.name() + " from a marshalled stream");
        output.println(tab + " * @param istream the input stream");
        output.println(tab + " * @return the readed " + obj.name() + " value");
        output.println(tab + " */");
        output.print(tab + "public static ");

        translate_type(obj, output);

        output.println(" read(org.omg.CORBA.portable.InputStream istream)");
        output.println(tab + "{");

        if (final_kind(obj) == IdlType.e_value_box)
        {
            output.print(tab2 + "return (");

            translate_type(final_type(obj), output);

            output.println(") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value(new " + fullname(final_type(obj)) + "Helper());");
        }
        else
            if (final_kind(obj) == IdlType.e_forward_interface)
            {
                if ((((IdlInterface) final_type(obj)).getInterface().local_interface()) || (m_cp.getM_pidl()))
                    output.println(tab2 + "throw new org.omg.CORBA.MARSHAL();");
                else
                    translate_unmarshalling(obj, output, "istream");
            }
            else
                if (final_kind(obj) == IdlType.e_interface)
                {
                    if ((((IdlInterface) final_type(obj)).local_interface()) || (m_cp.getM_pidl()))
                        output.println(tab2 + "throw new org.omg.CORBA.MARSHAL();");
                    else
                        translate_unmarshalling(obj, output, "istream");
                }
                else
                    if (final_kind(obj) == IdlType.e_fixed)
                    {
                        output.println(tab2 + "java.math.BigDecimal _f = istream.read_fixed();");

                        if (((IdlFixed) final_type(obj)).scale() != 0)
                            output.println(tab2 + "return _f.movePointLeft(" + ((IdlFixed) final_type(obj)).scale() + ");");
                        else
                            output.println(tab2 + "return _f;");
                    }
                    else
                        if ((obj.kind() == IdlType.e_typedef) &&
                                (final_kind(obj) != IdlType.e_sequence) &&
                                (final_kind(obj) != IdlType.e_array) &&
                                (final_kind(obj) != IdlType.e_string) &&
                                (final_kind(obj) != IdlType.e_wstring) &&
                                (final_kind(obj) != IdlType.e_simple))
                        {
                            output.print(tab2 + "return ");

                            translate_type(final_type(obj), output);

                            output.println("Helper.read(istream);");
                        }
                        else
                            translate_unmarshalling(obj, output, "istream");

        output.println(tab + "}");

        output.println("");

        // La fonction write
        output.println(tab + "/**");

        output.println(tab + " * Write " + obj.name() + " into a marshalled stream");

        output.println(tab + " * @param ostream the output stream");

        output.println(tab + " * @param value " + obj.name() + " value");

        output.println(tab + " */");

        output.print(tab + "public static void write(org.omg.CORBA.portable.OutputStream ostream, ");

        translate_type(obj, output);

        output.println(" value)");

        output.println(tab + "{");

        if (final_kind(obj) == IdlType.e_value_box)
        {
            output.println(tab2 + "((org.omg.CORBA_2_3.portable.OutputStream)ostream).write_value(value, new " + fullname(final_type(obj)) + "Helper());");
        }
        else
            if (final_kind(obj) == IdlType.e_interface)
            {
                if ((((IdlInterface) final_type(obj)).local_interface()) || (m_cp.getM_pidl()))
                    output.println(tab2 + "throw new org.omg.CORBA.MARSHAL();");
                else
                    translate_marshalling(obj, output, "ostream", "value");
            }
            else
                if (final_kind(obj) == IdlType.e_fixed)
                {
                    output.println(tab2 + "try");
                    output.println(tab2 + "{");
                    output.println(tab3 + "value = value.setScale(" + ((IdlFixed) final_type(obj)).scale() + ");");
                    output.println(tab2 + "}");
                    output.println(tab2 + "catch (java.lang.ArithmeticException e)");
                    output.println(tab2 + "{");
                    output.println(tab3 + "throw new org.omg.CORBA.DATA_CONVERSION();");
                    output.println(tab2 + "}");

                    // the following precision check is only available on Java5
                    // disabled for now, since we currently target Java 1.4
                    final boolean targetJava5 = false;
                    if (targetJava5)
                    {
                        output.println(tab2);
                        output.println(tab2 + "if (value.precision() > " + ((IdlFixed) final_type(obj)).digits() + ")");
                        output.println(tab2 + "{");
                        output.println(tab3 + "throw new org.omg.CORBA.DATA_CONVERSION();");
                        output.println(tab2 + "}");
                    }

                    output.println(tab2 + "ostream.write_fixed(value);");
                }
                else
                    if ((obj.kind() == IdlType.e_typedef) &&
                            (final_kind(obj) != IdlType.e_sequence) &&
                            (final_kind(obj) != IdlType.e_array) &&
                            (final_kind(obj) != IdlType.e_string) &&
                            (final_kind(obj) != IdlType.e_wstring) &&
                            (final_kind(obj) != IdlType.e_simple))
                    {
                        output.print(tab2 + "");

                        translate_type(final_type(obj), output);

                        output.println("Helper.write(ostream, value);");
                    }
                    else
                        translate_marshalling(obj, output, "ostream", "value");

        output.println(tab + "}");

        output.println("");

        // The narrow function
        if (obj.kind() == IdlType.e_interface)
        {

            if (((IdlInterface) obj).abstract_interface())
                abstract_object = true;
            else
                real_corba_object = true;

            if (isAbstractBaseInterface(obj))
                abstract_object = true;

            if (abstract_object)
            {
                output.println(tab + "/**");
                output.println(tab + " * Narrow CORBA::Object to " + obj.name());
                output.println(tab + " * @param obj the abstract Object");
                output.println(tab + " * @return " + obj.name() + " Object");
                output.println(tab + " */");
                output.println(tab + "public static " + obj.name() + " narrow(Object obj)");
                output.println(tab + "{");
                output.println(tab2 + "if (obj == null)");
                output.println(tab3 + "return null;");
                output.println(tab2 + "if (obj instanceof " + obj.name() + ")");
                output.println(tab3 + "return (" + obj.name() + ")obj;");

                if (!m_cp.getM_pidl() && !((IdlInterface) obj).local_interface())
                {
                    output.println();
                    output.println(tab2 + "if (obj instanceof org.omg.CORBA.portable.ObjectImpl) {");
                    output.println(tab3 + "org.omg.CORBA.portable.ObjectImpl objimpl = (org.omg.CORBA.portable.ObjectImpl)obj;");
                    output.println(tab3 + "if (objimpl._is_a(id())) {");
                    output.println(tab4 + "_" + obj.name() + "Stub stub = new _" + obj.name() + "Stub();");
                    output.println(tab4 + "stub._set_delegate(objimpl._get_delegate());");
                    output.println(tab4 + "return stub;");
                    output.println(tab3 + "}");
                    output.println(tab2 + "}");
                    output.println();
                }

                output.println("");
                output.println(tab2 + "throw new org.omg.CORBA.BAD_PARAM();");
                output.println(tab + "}");
                output.println("");

                // Unchecked narrow
                output.println(tab + "/**");
                output.println(tab + " * Unchecked Narrow CORBA::Object to " + obj.name());
                output.println(tab + " * @param obj the abstract Object");
                output.println(tab + " * @return " + obj.name() + " Object");
                output.println(tab + " */");
                output.println(tab + "public static " + obj.name() + " unchecked_narrow(Object obj)");
                output.println(tab + "{");
                output.println(tab2 + "if (obj == null)");
                output.println(tab3 + "return null;");
                output.println(tab2 + "if (obj instanceof " + obj.name() + ")");
                output.println(tab3 + "return (" + obj.name() + ")obj;");

                if (!m_cp.getM_pidl() && !((IdlInterface) obj).local_interface())
                {
                    output.println();
                    output.println(tab2 + "if (obj instanceof org.omg.CORBA.portable.ObjectImpl) {");
                    output.println(tab3 + "org.omg.CORBA.portable.ObjectImpl objimpl = (org.omg.CORBA.portable.ObjectImpl)obj;");
                    output.println(tab3 + "_" + obj.name() + "Stub stub = new _" + obj.name() + "Stub();");
                    output.println(tab3 + "stub._set_delegate(objimpl._get_delegate());");
                    output.println(tab3 + "return stub;");
                    output.println(tab2 + "}");
                    output.println();
                }

                output.println("");
                output.println(tab2 + "throw new org.omg.CORBA.BAD_PARAM();");
                output.println(tab + "}");
                output.println("");
            }

            if (real_corba_object)
            {
                output.println(tab + "/**");
                output.println(tab + " * Narrow CORBA::Object to " + obj.name());
                output.println(tab + " * @param obj the CORBA Object");
                output.println(tab + " * @return " + obj.name() + " Object");
                output.println(tab + " */");

                if (!m_cp.getM_pidl())
                    output.println(tab + "public static " + obj.name() + " narrow(org.omg.CORBA.Object obj)");
                else
                    output.println(tab + "public static " + obj.name() + " narrow(Object obj)");

                output.println(tab + "{");

                output.println(tab2 + "if (obj == null)");

                output.println(tab3 + "return null;");

                output.println(tab2 + "if (obj instanceof " + obj.name() + ")");

                output.println(tab3 + "return (" + obj.name() + ")obj;");

                output.println("");

                if (!m_cp.getM_pidl() && !((IdlInterface) obj).local_interface())
                {
                    output.println(tab2 + "if (obj._is_a(id()))");
                    output.println(tab2 + "{");
                    output.println(tab3 + "_" + obj.name() + "Stub stub = new _" + obj.name() + "Stub();");
                    output.println(tab3 + "stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());");
                    output.println(tab3 + "return stub;");
                    output.println(tab2 + "}");
                    output.println("");
                }

                output.println(tab2 + "throw new org.omg.CORBA.BAD_PARAM();");
                output.println(tab + "}");
                output.println("");

                // Unchecked narrow
                output.println(tab + "/**");
                output.println(tab + " * Unchecked Narrow CORBA::Object to " + obj.name());
                output.println(tab + " * @param obj the CORBA Object");
                output.println(tab + " * @return " + obj.name() + " Object");
                output.println(tab + " */");

                if (m_cp.getM_pidl() == false)
                    output.println(tab + "public static " + obj.name() + " unchecked_narrow(org.omg.CORBA.Object obj)");
                else
                    output.println(tab + "public static " + obj.name() + " unchecked_narrow(Object obj)");

                output.println(tab + "{");

                output.println(tab2 + "if (obj == null)");

                output.println(tab3 + "return null;");

                output.println(tab2 + "if (obj instanceof " + obj.name() + ")");

                output.println(tab3 + "return (" + obj.name() + ")obj;");

                output.println("");

                if (!m_cp.getM_pidl() && !((IdlInterface) obj).local_interface())
                {
                    output.println(tab2 + "_" + obj.name() + "Stub stub = new _" + obj.name() + "Stub();");
                    output.println(tab2 + "stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());");
                    output.println(tab2 + "return stub;");
                    output.println();
                }
                else
                {
                    output.println(tab2 + "throw new org.omg.CORBA.BAD_PARAM();");
                }

                output.println(tab + "}");
                output.println("");
            }
        }

        // Special for value type
        if (obj.kind() == IdlType.e_value)
        {
            if (((IdlValue) obj).abstract_value() == false)
            {
                obj.reset();

                while (obj.end() != true)
                {
                    if (obj.current().kind() == IdlType.e_factory)
                    {
                        output.println(tab + "/**");
                        output.println(tab + " * Create a value type (using factory method)");
                        output.println(tab + " */");
                        output.print(tab + "public static " + obj.name() + " " + obj.current().name() + "(");

                        output.print("org.omg.CORBA.ORB orb");

                        obj.current().reset();

                        while (obj.current().end() != true)
                        {
                            output.print(", ");

                            IdlFactoryMember member = (IdlFactoryMember) obj.current().current();

                            member.reset();
                            translate_type(member.current(), output);
                            output.print(" " + member.name());

                            obj.current().next();
                        }

                        output.println(")");
                        output.println(tab + "{");
                        output.println(tab2 + "org.omg.CORBA.portable.ValueFactory _factory = ((org.omg.CORBA_2_3.ORB)orb).lookup_value_factory(id());");
                        output.println(tab2 + "if ( _factory == null )");
                        output.println(tab3 + "throw new org.omg.CORBA.BAD_INV_ORDER();");
                        output.print(tab2 + "return ( ( " + fullname( obj ) + "ValueFactory ) ( _factory ) )." + obj.current().name() + "(");

                        obj.current().reset();

                        while (obj.current().end() != true)
                        {
                            IdlFactoryMember member = (IdlFactoryMember) obj.current().current();

                            member.reset();
                            output.print(" " + member.name());

                            obj.current().next();

                            if (obj.current().end() != true)
                                output.print(", ");
                        }

                        output.println(");");
                        output.println(tab + "}");
                        output.println("");
                    }

                    obj.next();
                }
            }
        }

        // Special for value box
        if (obj.kind() == IdlType.e_value_box)
        {
            output.println(tab + "/**");
            output.println(tab + " * Read a value from an input stream");
            output.println(tab + " */");
            output.println(tab + "public java.io.Serializable read_value(org.omg.CORBA.portable.InputStream is)");
            output.println(tab + "{");

            translate_unmarshalling(obj, output, "is");

            output.println(tab + "}");
            output.println("");

            output.println(tab + "/**");
            output.println(tab + " * Write a value into an output stream");
            output.println(tab + " */");
            output.println(tab + "public void write_value(org.omg.CORBA.portable.OutputStream os, java.io.Serializable value)");
            output.println(tab + "{");

            translate_marshalling(obj, output, "os", "value");

            output.println(tab + "}");
            output.println("");

            output.println(tab + "/**");
            output.println(tab + " * Return the value id");
            output.println(tab + " */");
            output.println(tab + "public String get_id()");
            output.println(tab + "{");
            output.println(tab2 + "return id();");
            output.println(tab + "}");
            output.println("");

        }

        output.println("}");
        output.close();
    }

    /**
     * Add an holder for a data type
     *
     * @param obj the object to translate
     * @param writeInto the directory where the object must be defined
     */
    public void write_holder(IdlObject obj, java.io.File writeInto)
    {
        java.io.PrintWriter output = newFile(writeInto, obj.name() + "Holder");

        if (current_pkg != null)
        {
            if (current_pkg.equals("generated"))
            {
                if (m_cp.getM_use_package() == true)
                {
                    output.println("package " + current_pkg + ";");
                    output.println("");
                }
            }
            else
                if (!current_pkg.equals(""))
                {
                    output.println("package " + current_pkg + ";");
                    output.println("");
                }
        }

        output.println("/**");
        output.println(" * Holder class for : " + obj.name());
        output.println(" * ");
        output.println(" * @author OpenORB Compiler");
        output.println(" */");

        output.println("final public class " + obj.name() + "Holder");
        output.println(tab2 + "implements org.omg.CORBA.portable.Streamable");
        output.println("{");

        // The internal value
        output.println(tab + "/**");
        output.println(tab + " * Internal " + obj.name() + " value");
        output.println(tab + " */");

        output.print(tab + "public ");

        if (obj.kind() == IdlType.e_value_box)
        {
            if (((IdlValueBox) obj).simple())
            {
                obj.reset();
                translate_type(obj.current(), output);
            }
            else
                translate_type(obj, output);
        }
        else
            translate_type(obj, output);

        output.println(" value;");

        output.println("");

        // Default constructor
        output.println(tab + "/**");

        output.println(tab + " * Default constructor");

        output.println(tab + " */");

        output.println(tab + "public " + obj.name() + "Holder()");

        output.println(tab + "{ }");

        output.println("");

        // Constructor with init
        output.println(tab + "/**");

        output.println(tab + " * Constructor with value initialisation");

        output.println(tab + " * @param initial the initial value");

        output.println(tab + " */");

        output.print(tab + "public " + obj.name() + "Holder(");

        translate_type(obj, output);

        output.println(" initial)");

        output.println(tab + "{");

        if (obj.kind() == IdlType.e_value_box)
        {
            if (((IdlValueBox) obj).simple())
                output.println(tab2 + "value = initial.value;");
            else
                output.println(tab2 + "value = initial;");
        }
        else
            output.println(tab2 + "value = initial;");

        output.println(tab + "}");

        output.println("");

        // The method _read
        output.println(tab + "/**");

        output.println(tab + " * Read " + obj.name() + " from a marshalled stream");

        output.println(tab + " * @param istream the input stream");

        output.println(tab + " */");

        output.println(tab + "public void _read(org.omg.CORBA.portable.InputStream istream)");

        output.println(tab + "{");

        if (obj.kind() == IdlType.e_value_box)
        {
            if (((IdlValueBox) obj).simple())
                output.println(tab2 + "value = (" + obj.name() + "Helper.read(istream)).value;");
            else
                output.println(tab2 + "value = " + obj.name() + "Helper.read(istream);");
        }
        else
            if (obj.kind() == IdlType.e_interface)
            {
                if (((IdlInterface) obj).local_interface())
                    output.println(tab2 + "throw new org.omg.CORBA.NO_IMPLEMENT();");
                else
                    output.println(tab2 + "value = " + obj.name() + "Helper.read(istream);");
            }
            else
                output.println(tab2 + "value = " + obj.name() + "Helper.read(istream);");

        output.println(tab + "}");

        output.println("");

        // The method _write
        output.println(tab + "/**");

        output.println(tab + " * Write " + obj.name() + " into a marshalled stream");

        output.println(tab + " * @param ostream the output stream");

        output.println(tab + " */");

        output.println(tab + "public void _write(org.omg.CORBA.portable.OutputStream ostream)");

        output.println(tab + "{");

        if (obj.kind() == IdlType.e_value_box)
        {
            if (((IdlValueBox) obj).simple())
                output.println(tab2 + "" + obj.name() + "Helper.write(ostream, new " + obj.name() + "(value));");
            else
                output.println(tab2 + "" + obj.name() + "Helper.write(ostream,value);");
        }
        else
            if (obj.kind() == IdlType.e_interface)
            {
                if (((IdlInterface) obj).local_interface())
                    output.println(tab2 + "throw new org.omg.CORBA.NO_IMPLEMENT();");
                else
                    output.println(tab2 + "" + obj.name() + "Helper.write(ostream,value);");
            }
            else
                output.println(tab2 + "" + obj.name() + "Helper.write(ostream,value);");

        output.println(tab + "}");

        output.println("");

        // The method _type
        output.println(tab + "/**");

        output.println(tab + " * Return the " + obj.name() + " TypeCode");

        output.println(tab + " * @return a TypeCode");

        output.println(tab + " */");

        output.println(tab + "public org.omg.CORBA.TypeCode _type()");

        output.println(tab + "{");

        if (obj.kind() == IdlType.e_interface)
        {
            if (((IdlInterface) obj).local_interface())
                output.println(tab2 + "throw new org.omg.CORBA.NO_IMPLEMENT();");
            else
                output.println(tab2 + "return " + obj.name() + "Helper.type();");
        }
        else
            output.println(tab2 + "return " + obj.name() + "Helper.type();");

        output.println(tab + "}");

        output.println("");

        output.println("}");

        output.close();
    }

    /**
     * Translate an enumeration
     *
     * @param obj the enum to be translated
     * @param writeInto the directory where the enum must be defined
     */
    public void translate_enum(IdlObject obj, java.io.File writeInto)
    {
        // Deprecated
        // java.io.PrintStream output = ...
        java.io.PrintWriter output = newFile(writeInto, obj.name());
        IdlEnumMember member_obj;

        addDescriptiveHeader(output, obj);


        // Define the class
        output.println("public final class " + obj.name() + " implements org.omg.CORBA.portable.IDLEntity");
        output.println("{");

        // Value of each member
        obj.reset();

        while ( !obj.end() )
        {
            member_obj = (IdlEnumMember) obj.current();

            output.println(tab + "/**");
            output.println(tab + " * Enum member " + member_obj.name() + " value ");
            output.println(tab + " */");
            output.print(tab + "public static final int _" + member_obj.name());
            output.println(" = " + member_obj.getValue() + ";");
            output.println();

            output.println(tab + "/**");
            output.println(tab + " * Enum member " + member_obj.name());
            output.println(tab + " */");
            output.print(tab + "public static final " + obj.name() + " " + member_obj.name());
            output.println(" = new " + obj.name() + "(_" + member_obj.name() + ");");
            output.println();

            obj.next();
        }

        // The internal member
        output.println( tab + "/**" );
        output.println( tab + " * Internal member value " );
        output.println( tab + " */" );
        output.println( tab + "private final int _" + obj.name() + "_value;" );
        output.println();

        // The constructor
        output.println( tab1 + "/**" );
        output.println( tab1 + " * Private constructor" );
        output.println( tab1 + " * @param  the enum value for this new member" );
        output.println( tab1 + " */" );
        output.println( tab1 + "private " + obj.name() + "( final int value )" );
        output.println( tab1 + "{" );
        output.println( tab2 + "_" + obj.name() + "_value = value;" );
        output.println( tab1 + "}" );
        output.println();

        // readResolve to maintain singleton property.
        // Issue 4271: IDL/Java issue, Mapping for IDL enum
        output.println( tab1 + "/**" );
        output.println( tab1 + " * Maintains singleton property for serialized enums." );
        output.println( tab1 + " * Issue 4271: IDL/Java issue, Mapping for IDL enum." );
        output.println( tab1 + " */" );
        output.println( tab1 + "public java.lang.Object readResolve() throws java.io.ObjectStreamException" );
        output.println( tab1 + "{" );
        output.println( tab2 + "return from_int( value() );" );
        output.println( tab1 + "}" );
        output.println();

        // the method value
        output.println( tab1 + "/**" );
        output.println( tab1 + " * Return the internal member value" );
        output.println( tab1 + " * @return the member value" );
        output.println( tab1 + " */" );
        output.println( tab1 + "public int value()");
        output.println( tab1 + "{" );
        output.println( tab2 + "return _" + obj.name() + "_value;" );
        output.println( tab1 + "}" );
        output.println();

        // the method from_int
        output.println( tab1 + "/**" );
        output.println( tab1 + " * Return a enum member from its value." );
        output.println( tab1 + " * @param value An enum value" );
        output.println( tab1 + " * @return An enum member" );
        output.println( tab2 + " */" );

        output.println( tab1 + "public static " + obj.name() + " from_int( int value )" );
        output.println( tab1 + "{" );
        output.println( tab2 + "switch ( value )" );
        output.println( tab2 + "{" );

        obj.reset();

        while (obj.end() != true)
        {
            member_obj = (IdlEnumMember) obj.current();

            output.println(tab2 + "case " + member_obj.getValue() + ":");
            output.println(tab3 + "return " + member_obj.name() + ";");

            obj.next();
        }

        output.println(tab2 + "}");
        output.println(tab2 + "throw new org.omg.CORBA.BAD_OPERATION();");
        output.println(tab + "}");
        output.println("");

        // the method toString()
        output.println(tab + "/**");
        output.println(tab + " * Return a string representation");
        output.println(tab + " * @return a string representation of the enumeration");
        output.println(tab + " */");
        output.println(tab + "public java.lang.String toString()");
        output.println(tab + "{");
        output.println(tab2 + "switch ( _" + obj.name() + "_value )");
        output.println(tab2 + "{");

        obj.reset();

        while (obj.end() != true)
        {
            member_obj = (IdlEnumMember) obj.current();

            output.println(tab2 + "case " + member_obj.getValue() + ":");
            output.println(tab3 + "return \"" + member_obj.name() + "\";");

            obj.next();
        }

        output.println(tab2 + "}");
        output.println(tab2 + "throw new org.omg.CORBA.BAD_OPERATION();");
        output.println(tab + "}");
        output.println("");

        output.println("}");

        output.close();

        write_helper(obj, writeInto);
        write_holder(obj, writeInto);
    }

    /**
     * Translate the structure
     *
     * @param obj le module to be translated
     * @param writeInto the directory where the structure must be defined
     */
    public void translate_struct(IdlObject obj, java.io.File writeInto)
    {
        // Deprecated
        // java.io.PrintStream output = ...
        java.io.PrintWriter output = newFile(writeInto, obj.name());
        java.io.File sub = writeInto;
        String old_pkg = current_pkg;

        IdlStructMember member_obj;

        addDescriptiveHeader(output, obj);

        if (isEmpty(obj) == false)
        {
            sub = createDirectory(obj.name() + "Package" , writeInto);
        }

        addToPkg(obj, obj.name() + "Package");

        // Define the sub-types
        obj.reset();

        while (obj.end() != true)
        {
            obj.current().reset();

            switch (obj.current().current().kind())
            {

            case IdlType.e_union :
                translate_union(obj.current().current(), sub);
                break;

            case IdlType.e_struct :
                translate_struct(obj.current().current(), sub);
                break;

            case IdlType.e_enum :
                translate_enum(obj.current().current(), sub);
                break;
            }

            obj.next();
        }

        current_pkg = old_pkg;

        // Define the class
        output.println("public final class " + obj.name() + " implements org.omg.CORBA.portable.IDLEntity");
        output.println("{");

        // Declare each member of the structure
        obj.reset();

        while (obj.end() != true)
        {
            member_obj = (IdlStructMember) obj.current();

            output.println(tab + "/**");
            output.println(tab + " * Struct member " + member_obj.name());
            output.println(tab + " */");
            output.print(tab + "public ");

            member_obj.reset();
            translate_type(member_obj.current(), output);

            output.println(" " + member_obj.name() + ";");
            output.println("");

            obj.next();
        }

        // Default constructor
        output.println(tab + "/**");

        output.println(tab + " * Default constructor");

        output.println(tab + " */");

        output.println(tab + "public " + obj.name() + "()");

        output.println(tab + "{ }");

        output.println("");


        output.println(tab + "/**");

        output.println(tab + " * Constructor with fields initialization");

        obj.reset();

        while (obj.end() != true)
        {
            member_obj = (IdlStructMember) obj.current();
            output.println(tab + " * @param " + member_obj.name() + " " + member_obj.name() + " struct member");
            obj.next();
        }

        output.println(tab + " */");
        output.print(tab + "public " + obj.name() + "(");

        obj.reset();

        while (obj.end() != true)
        {
            member_obj = (IdlStructMember) obj.current();

            translate_type(member_obj.current(), output);
            output.print(" " + member_obj.name());
            obj.next();

            if (obj.end() != true)
                output.print(", ");
        }

        output.println(")");
        output.println(tab + "{");

        obj.reset();

        while (obj.end() != true)
        {
            member_obj = (IdlStructMember) obj.current();

            output.println(tab2 + "this." + member_obj.name() + " = " + member_obj.name() + ";");

            obj.next();
        }

        output.println(tab + "}");
        output.println("");

        output.println("}");

        output.close();

        write_helper(obj, writeInto);
        write_holder(obj, writeInto);
    }

    /**
     * Search the default value for an union
     *
     * @param obj the union
     * @return default value
     */
    public String find_default_value(IdlObject obj)
    {
        IdlUnionMember disc;
        IdlUnionMember member_obj;
        IdlObject en;
        int idx = ((IdlUnion) obj).index();
        int i;
        int l;
        int p = obj.pos();
        String s;
        String ts;

        obj.reset();
        disc = (IdlUnionMember) obj.current();

        disc.reset();

        switch (final_kind(disc.current()))
        {

        case IdlType.e_enum :
            en = final_type(disc.current());
            obj.next();
            l = 0;
            i = 0;
            en.reset();
            s = fullname(en.current());
            ts = s + "@ ";

            while (obj.end() != true)
            {
                if (i != idx)
                {
                    member_obj = (IdlUnionMember) obj.current();

                    if (ts.equals(member_obj.getExpression()))
                    {
                        l++;
                        i = -1;
                        en.next();
                        s = fullname(en.current());
                        ts = s + "@ ";
                        obj.reset();
                    }
                }

                i++;
                obj.next();
            }

            obj.pos(p);
            return s;

        default :
            l = 0;
            i = 0;
            obj.next();

            while (obj.end() != true)
            {
                if (i != idx)
                {
                    member_obj = (IdlUnionMember) obj.current();

                    if (l == member_obj.getValue())
                    {
                        l++;
                        i = -1;
                        obj.reset();
                    }
                }

                i++;
                obj.next();
            }

            if (final_kind(disc.current()) == IdlType.e_simple)
            {}

            break;
        }

        obj.pos(p);
        return "" + l;
    }

    /**
     * Translate an union
     *
     * @param obj the union to translate
     * @param writeInto the directory where the union must be defined
     */
    public void translate_union(IdlObject obj, java.io.File writeInto)
    {
        java.io.PrintWriter output = newFile(writeInto, obj.name());
        java.io.File sub = writeInto;
        String old_pkg = current_pkg;

        IdlUnionMember member_obj;
        IdlObject disc;
        int i;
        int idx = ((IdlUnion) obj).index();
        boolean doMap = false;

        addDescriptiveHeader(output, obj);

        if (isEmpty(obj) == false)
            sub = createDirectory(obj.name() + "Package", writeInto);

        addToPkg(obj, obj.name() + "Package");

        // Define the sub-types
        obj.reset();

        while (obj.end() != true)
        {
            obj.current().reset();

            switch (obj.current().current().kind())
            {

            case IdlType.e_union :
                translate_union(obj.current().current(), sub);
                break;

            case IdlType.e_struct :
                translate_struct(obj.current().current(), sub);
                break;

            case IdlType.e_enum :
                translate_enum(obj.current().current(), sub);
                break;
            }

            obj.next();
        }

        current_pkg = old_pkg;

        // Define the class
        output.println("public final class " + obj.name() + " implements org.omg.CORBA.portable.IDLEntity");
        output.println("{");

        // Declare union members
        obj.reset();

        while (obj.end() != true)
        {
            member_obj = (IdlUnionMember) obj.current();

            if (member_obj.isAsNext() == false)
            {
                output.println(tab + "/**");
                output.println(tab + " * Union member " + member_obj.name());
                output.println(tab + " */");
                output.print(tab + "protected ");

                member_obj.reset();
                translate_type(member_obj.current(), output);

                output.println(" _" + member_obj.name() + ";");
                output.println("");

            }

            obj.next();
        }

        // Default constructor
        output.println(tab + "/**");

        output.println(tab + " * Default constructor");

        output.println(tab + " */");

        output.println(tab + "public " + obj.name() + "()");

        output.println(tab + "{");

        if (idx != -1)
        {
            output.println(tab2 + "__d = " + find_default_value(obj) + ";");
        }

        output.println(tab + "}");
        output.println("");

        obj.reset();

        output.println(tab + "/**");
        output.println(tab + " * Get discriminator value");
        output.println(tab + " */");
        output.print(tab + "public ");

        member_obj = (IdlUnionMember) obj.current();

        IdlObject discri = member_obj;
        member_obj.reset();
        disc = member_obj.current();
        translate_type(member_obj.current(), output);

        output.println(" discriminator()");
        output.println(tab + "{");
        output.println(tab2 + "return __d;");
        output.println(tab + "}");
        output.println("");


        obj.next();
        i = 0;

        while (obj.end() != true)
        {
            member_obj = (IdlUnionMember) obj.current();

            if (member_obj.isAsNext() == false)
            {
                doMap = false;
                output.println(tab + "/**");
                output.println(tab + " * Set " + member_obj.name() + " value");
                output.println(tab + " */");
                output.print(tab + "public void " + member_obj.name() + "(");

                member_obj.reset();
                translate_type(member_obj.current(), output);

                output.println(" value)");
                output.println(tab + "{");

                if (i != idx)
                    output.println(tab2 + "__d = " + translate_to_union_case_expression((IdlUnionMember) discri, member_obj.getExpression()) + ";");
                else
                    output.println(tab2 + "__d = " + find_default_value(obj) + ";");

                output.println(tab2 + "_" + member_obj.name() + " = value;");

                output.println(tab + "}");

                output.println("");

                if (i == idx)
                {
                    output.println(tab + "/**");
                    output.println(tab + " * Set " + member_obj.name() + " value");
                    output.println(tab + " */");
                    output.print(tab + "public void " + member_obj.name() + "(");

                    translate_type(disc, output);

                    output.print(" dvalue, ");

                    member_obj.reset();
                    translate_type(member_obj.current(), output);

                    output.println(" value)");
                    output.println(tab + "{");
                    output.println(tab2 + "__d = dvalue;");

                    output.println(tab2 + "_" + member_obj.name() + " = value;");
                    output.println(tab + "}");
                    output.println("");

                }

                output.println(tab + "/**");
                output.println(tab + " * Get " + member_obj.name() + " value");
                output.println(tab + " */");
                output.print(tab + "public ");

                member_obj.reset();
                translate_type(member_obj.current(), output);

                output.println(" " + member_obj.name() + "()");
                output.println(tab + "{");
                output.println(tab2 + "return _" + member_obj.name() + ";");
                output.println(tab + "}");
                output.println("");
            }
            else
            {
                if (doMap == false)
                {
                    output.println(tab + "/**");
                    output.println(tab + " * Set " + member_obj.name() + " value");
                    output.println(tab + " */");
                    output.print(tab + "public void " + member_obj.name() + "(");

                    translate_type(disc, output);

                    output.print(" dvalue, ");

                    member_obj.reset();
                    translate_type(member_obj.current(), output);

                    output.println(" value)");
                    output.println(tab + "{");
                    output.println(tab2 + "__d = dvalue;");

                    output.println(tab2 + "_" + member_obj.name() + " = value;");
                    output.println(tab + "}");
                    output.println("");

                    doMap = true;
                }

            }

            obj.next();

            i++;
        }

        /*
         // TODO: Bugtracker #515917
         Two default modifier methods, both named __default(), are
         generated if there is no explicit default case label, and the
         set of case labels does not completely cover the possible
         values of the discriminant. The simple method taking no
         arguments and returning void sets the discriminant to the
         first available default value starting from a 0 index of the
         discriminant type. The second method takes a discriminator as
         parameter and returns void. Both of these of methods shall
         leave the union with a discriminator value set, and the value
         member uninitialized.
        */
        if (idx == -1)
        {
            output.println(tab + "/**");
            output.println(tab + " * default access");
            output.println(tab + " */");
            output.println(tab + "public void __default()");
            output.println(tab + "{");
            output.println(tab + "}");

            output.println(tab + "/**");
            output.println(tab + " * default access");
            output.println(tab + " */");
            output.print(tab + "public void __default(");
            translate_type(disc, output);
            output.println(" _discriminator)");
            output.println(tab + "{");
            output.println(tab + "}");
        }

        obj.reset();
        obj.next();

        if (((IdlUnionMember) obj.current()).getExpression().equals("true ") ||
                ((IdlUnionMember) obj.current()).getExpression().equals("false "))
        {
            output.println(tab + "/**");
            output.println(tab + " * Return an int value for discriminator");
            output.println(tab + " */");
            output.println(tab + "public int toInt()");
            output.println(tab + "{");
            output.println(tab + "if (__d == true)");
            output.println(tab + " return 1;");
            output.println(tab + "return 0;");
            output.println(tab + "}");
        }

        output.println("}");

        output.close();

        write_helper(obj, writeInto);
        write_holder(obj, writeInto);
    }

    /**
     * Translate a typedef
     *
     * @param obj the typedef to translate
     * @param writeInto the directory where the typedef must be defined
     */
    public void translate_typedef(IdlObject obj, java.io.File writeInto)
    {
        obj.reset();

        switch (obj.current().kind())
        {

        case IdlType.e_string :

        case IdlType.e_wstring :

        case IdlType.e_simple :
            write_helper(obj, writeInto);
            break;

        case IdlType.e_union :

        case IdlType.e_struct :

        case IdlType.e_enum :
            write_helper(obj, writeInto);
            break;

        case IdlType.e_fixed :

        case IdlType.e_sequence :

        case IdlType.e_array :
            write_helper(obj, writeInto);
            write_holder(obj, writeInto);
            break;

        case IdlType.e_ident :
            write_helper(obj, writeInto);

            if ((final_type(obj.current()).kind() == IdlType.e_sequence) ||
                    (final_type(obj.current()).kind() == IdlType.e_array))
                write_holder(obj, writeInto);

            break;

        case IdlType.e_typedef :
            write_helper(obj, writeInto);

            break;
        }
    }

    private void writeThrowException(final java.io.PrintWriter output,
            final String indent, final String exceptionName, final String args,
            final String causeName)
    {
        output.print(indent);
        output.print("throw ");

        if (m_cp.getM_jdk1_4())
        {
            output.print("(");
            output.print(exceptionName);
            output.print(")");
        }

        output.print("new ");
        output.print(exceptionName);
        output.print("(");
        output.print(args);
        output.print(")");

        if (m_cp.getM_jdk1_4())
        {
            output.print(".initCause(");
            output.print(causeName);
            output.print(")");
        }
        output.println(";");
    }

    /**
     * Translate an exception
     *
     * @param obj exception to translate
     * @param writeInto the directory where the exception must be defined
     */
    public void translate_exception(IdlObject obj, java.io.File writeInto)
    {
        // Deprecated
        // java.io.PrintStream output = ...
        java.io.PrintWriter output = newFile(writeInto, obj.name());
        java.io.File sub = writeInto;
        String old_pkg = current_pkg;

        IdlStructMember member_obj;

        addDescriptiveHeader(output, obj);

        if (isEmpty(obj) == false)
            sub = createDirectory(obj.name() + "Package" , writeInto);

        addToPkg(obj, obj.name() + "Package");

        // Define sub-types
        obj.reset();

        while (obj.end() != true)
        {
            obj.current().reset();

            switch (obj.current().current().kind())
            {

            case IdlType.e_union :
                translate_union(obj.current().current(), sub);
                break;

            case IdlType.e_struct :
                translate_struct(obj.current().current(), sub);
                break;

            case IdlType.e_enum :
                translate_enum(obj.current().current(), sub);
                break;
            }

            obj.next();
        }

        current_pkg = old_pkg;

        // Define the class
        output.println("public final class " + obj.name() + " extends org.omg.CORBA.UserException");
        output.println("{");

        // Declare exception members
        obj.reset();

        while (obj.end() != true)
        {
            member_obj = (IdlStructMember) obj.current();

            output.println(tab + "/**");
            output.println(tab + " * Exception member " + member_obj.name());
            output.println(tab + " */");
            output.print(tab + "public ");

            member_obj.reset();
            translate_type(member_obj.current(), output);

            output.println(" " + member_obj.name() + ";");
            output.println("");

            obj.next();
        }

        // Default constructor
        output.println(tab + "/**");

        output.println(tab + " * Default constructor");

        output.println(tab + " */");

        output.println(tab + "public " + obj.name() + "()");

        output.println(tab + "{");

        output.println(tab2 + "super(" + obj.name() + "Helper.id());");

        output.println(tab + "}");

        output.println("");

        if (obj.length() != 0)
        {
            output.println(tab + "/**");
            output.println(tab + " * Constructor with fields initialization");

            obj.reset();

            while (obj.end() != true)
            {
                member_obj = (IdlStructMember) obj.current();
                output.println(tab + " * @param " + member_obj.name() + " " + member_obj.name() + " exception member");
                obj.next();
            }

            output.println(tab + " */");
            output.print(tab + "public " + obj.name() + "(");

            obj.reset();

            while (obj.end() != true)
            {
                member_obj = (IdlStructMember) obj.current();

                translate_type(member_obj.current(), output);
                output.print(" " + member_obj.name());
                obj.next();

                if (obj.end() != true)
                    output.print(", ");
            }

            output.println(")");
            output.println(tab + "{");
            output.println(tab2 + "super(" + obj.name() + "Helper.id());");

            obj.reset();

            while (obj.end() != true)
            {
                member_obj = (IdlStructMember) obj.current();

                output.println(tab2 + "this." + member_obj.name() + " = " + member_obj.name() + ";");

                obj.next();
            }

            output.println(tab + "}");
            output.println("");
        }

        // Le constructeur le plus complet avec initialisation de chaque membre
        output.println(tab + "/**");

        output.println(tab + " * Full constructor with fields initialization");

        obj.reset();

        while (obj.end() != true)
        {
            member_obj = (IdlStructMember) obj.current();
            output.println(tab + " * @param " + member_obj.name() + " " + member_obj.name() + " exception member");
            obj.next();
        }

        output.println(tab + " */");
        output.print(tab + "public " + obj.name() + "(String orb_reason");

        obj.reset();

        while (obj.end() != true)
        {
            output.print(", ");

            member_obj = (IdlStructMember) obj.current();

            translate_type(member_obj.current(), output);
            output.print(" " + member_obj.name());
            obj.next();
        }

        output.println(")");
        output.println(tab + "{");

        obj.reset();
        output.println(tab2 + "super(" + obj.name() + "Helper.id() +\" \" +  orb_reason);");

        while (obj.end() != true)
        {
            member_obj = (IdlStructMember) obj.current();

            output.println(tab2 + "this." + member_obj.name() + " = " + member_obj.name() + ";");

            obj.next();
        }

        output.println(tab + "}");
        output.println("");

        output.println("}");

        output.close();

        write_helper(obj, writeInto);
        write_holder(obj, writeInto);
    }

    /**
     * Translate an attribute
     *
     * @param obj attribute to translate
     * @param output write access
     */
    public void translate_attribute (IdlObject obj, java.io.PrintWriter output)
    {
        if (obj.hasComment())
            javadoc(output, obj);
        else
        {

            output.println(tab + "/**");
            output.println(tab + " * Read accessor for " + obj.name() + " attribute");
            output.println(tab + " * @return the attribute value");
            output.println(tab + " */");
        }

        output.print(tab + "public ");

        if (obj.upper().kind() == IdlType.e_value)
            output.print("abstract ");

        obj.reset();

        translate_type(obj.current(), output);

        output.println(" " + obj.name() + "();");

        output.println("");

        if (((IdlAttribute) obj).readOnly() == false)
        {
            if (obj.hasComment())
                javadoc(output, obj);
            else
            {
                output.println(tab + "/**");
                output.println(tab + " * Write accessor for " + obj.name() + " attribute");
                output.println(tab + " * @param value the attribute value");
                output.println(tab + " */");
            }

            output.print(tab + "public ");

            if (obj.upper().kind() == IdlType.e_value)
                output.print("abstract ");

            output.print("void " + obj.name() + "(");

            translate_type(obj.current(), output);

            output.println(" value);");

            output.println("");
        }
    }

    /**
     * Translate an attribute for the user code
     *
     * @param obj attribute to translate
     * @param output write access
     */
    public void translate_user_attribute (IdlObject obj, java.io.PrintWriter output)
    {
        output.println(tab + "/**");
        output.println(tab + " * " + obj.name() + " read attribute");
        output.println(tab + " */");
        output.print(tab + "public ");

        obj.reset();
        translate_type(obj.current(), output);

        output.println(" " + obj.name() + "()");
        output.println(tab + "{");
        output.println(tab2 + "//TODO: put your code here");
        output.println(tab + "}");
        output.println("");

        if (((IdlAttribute) obj).readOnly() == false)
        {
            output.println(tab + "//");
            output.println(tab + "// " + obj.name() + " write attribute");
            output.println(tab + "//");
            output.print(tab + "public void " + obj.name() + "(");

            translate_type(obj.current(), output);

            output.println(" value)");
            output.println(tab + "{");
            output.println(tab2 + "//TODO: put your code here");
            output.println(tab + "}");
            output.println("");
        }
    }

    /**
     * Translate an attribute for a TIE approach
     *
     * @param obj attribute to translate
     * @param output write access
     */
    public void translate_attribute_tie (IdlObject obj, java.io.PrintWriter output)
    {
        output.println(tab + "/**");
        output.println(tab + " * Read accessor for " + obj.name() + " attribute");
        output.println(tab + " */");
        output.print(tab + "public ");

        obj.reset();
        translate_type(obj.current(), output);

        output.println(" " + obj.name() + "()");
        output.println(tab + "{");
        output.println(tab2 + "return _tie." + obj.name() + "();");
        output.println(tab + "}");
        output.println("");

        if (((IdlAttribute) obj).readOnly() == false)
        {
            output.println(tab + "/**");
            output.println(tab + " * Write accessor for " + obj.name() + " attribute");
            output.println(tab + " */");
            output.print(tab + "public void " + obj.name() + "(");

            translate_type(obj.current(), output);

            output.println(" value)");
            output.println(tab + "{");
            output.println(tab2 + "_tie." + obj.name() + "(value);");
            output.println(tab + "}");
            output.println("");
        }
    }

    /**
     * Translate an attribute
     *
     * @param obj attribute to translate
     * @param output write access
     */
    public void translate_attribute_stub (IdlObject obj, java.io.PrintWriter output)
    {
        output.println(tab + "/**");
        output.println(tab + " * Read accessor for " + obj.name() + " attribute");
        output.println(tab + " * @return the attribute value");
        output.println(tab + " */");
        output.print(tab + "public ");

        obj.reset();
        translate_type(obj.current(), output);

        output.println(" " + obj.name() + "()");
        output.println(tab + "{");
        output.println(tab2 + "org.omg.CORBA.Request _arg_request = _request(\"_get_" + obj.name() + "\");");
        output.println(tab3 + "org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");
        output.println("");
        output.print(tab2 + "_arg_request.set_return_type(");

        obj.reset();
        translate_typecode(obj.current(), output);

        output.println(");");
        output.println("");

        output.println(tab2 + "_arg_request.invoke();");
        output.println("");

        output.println(tab2 + "Exception _except = _arg_request.env().exception();");
        output.println(tab2 + "if (_except != null)");
        output.println(tab2 + " throw (org.omg.CORBA.SystemException)_except;");
        output.println("");

        output.println(tab2 + "org.omg.CORBA.Any _arg_result = _arg_request.return_value();");
        output.print(tab2 + "return ");

        translate_unmarshalling_data(obj.current(), output, "_arg_result.create_input_stream()");

        output.println(tab + "}");
        output.println("");

        if (((IdlAttribute) obj).readOnly() == false)
        {
            output.println(tab + "/**");
            output.println(tab + " * Write accessor for " + obj.name() + " attribute");
            output.println(tab + " * @param value the attribute value");
            output.println(tab + " */");
            output.print(tab + "public void " + obj.name() + "(");

            translate_type(obj.current(), output);

            output.println(" value)");
            output.println(tab + "{");
            output.println(tab2 + "org.omg.CORBA.Request _arg_request = _request(\"_set_" + obj.name() + "\");");
            output.println(tab3 + "org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");
            output.println("");
            output.println(tab2 + "org.omg.CORBA.Any _arg = _arg_request.add_in_arg();");

            output.print(tab2 + "");
            translate_any_insert(obj.current(), output, "_arg", "value");
            output.println(";");

            output.println(tab2 + "_arg_request.invoke();");
            output.println("");

            output.println(tab2 + "Exception _except = _arg_request.env().exception();");
            output.println(tab2 + "if (_except != null)");
            output.println(tab2 + " throw (org.omg.CORBA.SystemException)_except;");
            output.println("");

            output.println(tab + "}");
            output.println("");
        }
    }

    /**
     * Translate an attribute with Stream mode
     *
     * @param obj attribute to translate
     * @param output write access
     */
    public void translate_attribute_stub_stream (IdlObject obj, java.io.PrintWriter output)
    {
        output.println(tab + "/**");
        output.println(tab + " * Read accessor for " + obj.name() + " attribute");
        output.println(tab + " * @return the attribute value");
        output.println(tab + " */");
        output.print(tab + "public ");

        obj.reset();
        translate_type(obj.current(), output);

        output.println(" " + obj.name() + "()");
        output.println(tab + "{");

        output.println(tab2 + "while(true)");
        output.println(tab2 + "{");

        if (m_cp.getM_local_stub())
        {
            output.println(tab3 + "if (!this._is_local())");
            output.println(tab3 + "{");
        }

        output.println(tab4 + "org.omg.CORBA.portable.InputStream _input = null;");
        output.println(tab4 + "try {");
        output.println(tab5 + "org.omg.CORBA.portable.OutputStream _output = this._request(\"_get_" + obj.name() + "\",true);");

        output.println(tab5 + "_input = this._invoke(_output);");

        output.print(tab5 + "return ");

        translate_unmarshalling_data(obj.current(), output, "_input");

        output.println(tab4 + "} catch (final org.omg.CORBA.portable.RemarshalException _exception) {");
        output.println(tab5 + "continue;");
        output.println(tab4 + "} catch (final org.omg.CORBA.portable.ApplicationException _exception) {");
        output.println(tab5 + "final String _exception_id = _exception.getId();");

        writeThrowException(output, tab5, "org.omg.CORBA.UNKNOWN",
            "\"Unexpected User Exception: \"+ _exception_id", "_exception");

        output.println(tab4 + "} finally {");
        output.println(tab5 + "this._releaseReply(_input);");
        output.println(tab4 + "}");

        boolean isAbstract = obj.upper().kind() == IdlType.e_interface &&
                             ((IdlInterface) (obj.upper())).abstract_interface();

        if (m_cp.getM_local_stub())
        {
            output.println(tab3 + "}");
            output.println(tab3 + "else");
            output.println(tab3 + "{");
            output.println(tab4 + "org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke(\"_get_" + obj.name() + "\",_opsClass);");
            output.println(tab4 + "if (_so == null)");
            output.println(tab4 + "   continue;");

            if (isAbstract)
                output.println(tab4 + "" + fullname(obj.upper()) + " _self = (" + fullname(obj.upper()) + ") _so.servant;");
            else
                output.println(tab4 + "" + fullname(obj.upper()) + "Operations _self = (" + fullname(obj.upper()) + "Operations) _so.servant;");

            output.println(tab4 + "try");

            output.println(tab4 + "{");

            output.println(tab5 + "return _self." + obj.name() + "();");

            output.println(tab4 + "}");

            output.println(tab4 + "finally");

            output.println(tab4 + "{");

            output.println(tab5 + "_servant_postinvoke(_so);");

            output.println(tab4 + "}");

            output.println(tab3 + "}");
        }

        output.println(tab2 + "}");
        output.println(tab + "}");
        output.println("");

        if (((IdlAttribute) obj).readOnly() == false)
        {
            output.println(tab + "/**");
            output.println(tab + " * Write accessor for " + obj.name() + " attribute");
            output.println(tab + " * @param value the attribute value");
            output.println(tab + " */");
            output.print(tab + "public void " + obj.name() + "(");

            translate_type(obj.current(), output);

            output.println(" value)");
            output.println(tab + "{");

            output.println(tab2 + "while(true)");
            output.println(tab2 + "{");

            if (m_cp.getM_local_stub())
            {
                output.println(tab3 + "if (!this._is_local())");
                output.println(tab3 + "{");
            }

            output.println(tab5 + "org.omg.CORBA.portable.InputStream _input = null;");
            output.println(tab4 + "try {");
            output.println(tab5 + "org.omg.CORBA.portable.OutputStream _output = this._request(\"_set_" + obj.name() + "\",true);");

            output.print(tab5 + "");
            translate_marshalling_data(obj.current(), output, "_output", "value");

            output.println(tab5 + "_input = this._invoke(_output);");

            output.println(tab5 + "return;");

            output.println(tab4 + "} catch (final org.omg.CORBA.portable.RemarshalException _exception) {");
            output.println(tab5 + "continue;");
            output.println(tab4 + "} catch (final org.omg.CORBA.portable.ApplicationException _exception) {");
            output.println(tab5 + "String _exception_id = _exception.getId();");

            writeThrowException(output, tab5, "org.omg.CORBA.UNKNOWN",
                "\"Unexpected User Exception: \"+ _exception_id", "_exception");

            output.println(tab4 + "} finally {");
            output.println(tab5 + "this._releaseReply(_input);");
            output.println(tab4 + "}");

            if (m_cp.getM_local_stub())
            {
                output.println(tab3 + "}");

                output.println(tab3 + "else");
                output.println(tab3 + "{");
                output.println(tab4 + "org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke(\"_set_" + obj.name() + "\",_opsClass);");
                output.println(tab4 + "if (_so == null)");
                output.println(tab4 + "   continue;");

                if (isAbstract)
                    output.println(tab4 + "" + fullname(obj.upper()) + " _self = (" + fullname(obj.upper()) + ") _so.servant;");
                else
                    output.println(tab4 + "" + fullname(obj.upper()) + "Operations _self = (" + fullname(obj.upper()) + "Operations) _so.servant;");

                output.println(tab4 + "try");

                output.println(tab4 + "{");

                output.println(tab5 + "_self." + obj.name() + "(value);");

                output.println(tab5 + "return;");

                output.println(tab4 + "}");

                output.println(tab4 + "finally");

                output.println(tab4 + "{");

                output.println(tab5 + "_servant_postinvoke(_so);");

                output.println(tab4 + "}");

                output.println(tab3 + "}");
            }

            output.println(tab2 + "}");
            output.println(tab + "}");
            output.println("");
        }
    }

    /**
     * Translate a read attribute for a skeleton
     *
     * @param obj attribute to translate
     * @param output write access
     */
    public void translate_read_attribute_skel (IdlObject obj, java.io.PrintWriter output)
    {
        obj.reset();

        output.print(tab2 + "");

        translate_type(obj.current(), output);

        output.println(" arg = " + obj.name() + "();");

        output.println("");
        output.println(tab2 + "org.omg.CORBA.NVList argList = orb.create_list(0);");
        output.println(tab2 + "org.omg.CORBA.Any result = orb.create_any();");
        output.println(tab2 + "request.arguments(argList);");
        output.print(tab2 + "");

        translate_any_insert(obj.current(), output, "result", "arg");
        output.println(";");
        output.println(tab2 + "request.set_result(result);");
    }

    /**
     * Translate a read attribute for a Stream mode skeleton
     *
     * @param obj attribute to translate
     * @param output write access
     */
    public void translate_read_attribute_skel_stream (IdlObject obj, java.io.PrintWriter output)
    {
        obj.reset();

        output.print(tab2 + "");

        translate_type(obj.current(), output);

        output.println(" arg = " + obj.name() + "();");

        output.println(tab2 + "_output = handler.createReply();");

        output.print(tab2 + "");
        translate_marshalling_data(obj.current(), output, "_output", "arg");

        output.println(tab2 + "return _output;");
    }

    /**
     * Translate a write attribute for a skeleton
     *
     * @param obj attribute to translate
     * @param output write access
     */
    public void translate_write_attribute_skel (IdlObject obj, java.io.PrintWriter output)
    {
        obj.reset();

        output.println(tab2 + "org.omg.CORBA.NVList argList = orb.create_list(0);");
        output.println(tab2 + "org.omg.CORBA.Any arg = orb.create_any();");

        output.print(tab2 + "arg.type(");

        translate_typecode(obj.current(), output);

        output.println(");");

        output.println(tab2 + "argList.add_value(\"\", arg, org.omg.CORBA.ARG_IN.value);");
        output.println(tab2 + "request.arguments(argList);");
        output.println("");

        output.print(tab2 + "");
        translate_type(obj.current(), output);

        output.print(" result = ");

        translate_unmarshalling_data(obj.current(), output, "arg.create_input_stream()");

        output.println("");
        output.println(tab2 + "" + obj.name() + "(result);");
    }

    /**
     * Translate a write attribute for a Stream mode skeleton
     *
     * @param obj attribute to translate
     * @param output write access
     */
    public void translate_write_attribute_skel_stream (IdlObject obj, java.io.PrintWriter output)
    {
        obj.reset();

        output.print(tab2 + "");
        translate_type(obj.current(), output);

        output.print(" result = ");

        translate_unmarshalling_data(obj.current(), output, "_is");
        output.println();

        output.println(tab2 + "" + obj.name() + "(result);");

        output.println(tab2 + "_output = handler.createReply();");
        output.println(tab2 + "return _output;");
    }

    /**
     * Return the context associated with an operation
     *
     * @param obj the operation
     * @return associated context object
     */
    public IdlContext getContext(IdlObject obj)
    {
        int p = obj.pos();
        IdlObject find = null;

        obj.reset();

        while (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_context)
            {
                find = obj.current();
                break;
            }

            obj.next();
        }

        obj.pos(p);

        return (IdlContext) find;
    }

    /**
     * Translate an operation
     *
     * @param obj operation to translate
     * @param output write access
     */
    public void translate_operation(IdlObject obj, java.io.PrintWriter output)
    {
        IdlRaises r;
        IdlContext c;
        boolean someParams = false;

        if (obj.hasComment())
            javadoc(output, obj);
        else
        {
            output.println(tab + "/**");
            output.println(tab + " * Operation " + obj.name());
            output.println(tab + " */");
        }

        output.print(tab + "public ");

        if (obj.upper().kind() == IdlType.e_value)
            output.print("abstract ");

        obj.reset();

        translate_type(obj.current(), output);

        output.print(" " + obj.name() + "(");

        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
            {
                someParams = true;

                while (obj.end() != true)
                {

                    obj.current().reset();
                    translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());

                    output.print(" " + obj.current().name());

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() == IdlType.e_param)
                            output.print(", ");
                        else
                            break;
                    }
                }
            }
        }

        c = getContext(obj);

        if (c != null)
        {
            if (someParams == true)
                output.print(", ");

            output.print("org.omg.CORBA.Context ctx");
        }

        output.print(")");

        if (obj.end() != true)
            if (obj.current().kind() == IdlType.e_raises)
            {
                output.println("");
                output.print(tab2 + "throws ");
                r = (IdlRaises) obj.current();

                r.reset();

                while (r.end() != true)
                {

                    output.print(fullname(r.current()));

                    r.next();

                    if (r.end() != true)

                        output.print(", ");

                }
            }

        output.println(";");
        output.println("");
    }

    /**
     * Translate an operation for the user code
     *
     * @param obj operation to translate
     * @param output write access
     */
    public void translate_user_operation(IdlObject obj, java.io.PrintWriter output)
    {
        IdlRaises r;
        IdlContext c;
        boolean someParams = false;

        output.println(tab + "/**");
        output.println(tab + " * Operation " + obj.name());
        output.println(tab + " */");
        output.print(tab + "public ");

        obj.reset();
        translate_type(obj.current(), output);

        output.print(" " + obj.name() + "(");

        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
            {
                someParams = true;

                while (obj.end() != true)
                {

                    obj.current().reset();
                    translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());

                    output.print(" " + obj.current().name());

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() == IdlType.e_param)
                            output.print(", ");
                        else
                            break;
                    }
                }
            }
        }

        c = getContext(obj);

        if (c != null)
        {
            if (someParams == true)
                output.print(", ");

            output.print("org.omg.CORBA.Context ctx");
        }

        output.print(")");

        if (obj.end() != true)
            if (obj.current().kind() == IdlType.e_raises)
            {
                output.println("");
                output.print(tab2 + "throws ");
                r = (IdlRaises) obj.current();

                r.reset();

                while (r.end() != true)
                {

                    output.print(fullname(r.current()));

                    r.next();

                    if (r.end() != true)

                        output.print(", ");

                }
            }

        output.println("");
        output.println(tab + "{");
        output.println(tab2 + "//TODO: put your code here");
        output.println(tab + "}");
        output.println("");
    }

    /**
     * Translate an operation for the TIE approach
     *
     * @param obj operation to translate
     * @param output write access
     */
    public void translate_operation_tie(IdlObject obj, java.io.PrintWriter output)
    {
        IdlRaises r;
        IdlContext c;
        boolean someParams = false;

        output.println(tab + "/**");
        output.println(tab + " * Operation " + obj.name());
        output.println(tab + " */");
        output.print(tab + "public ");

        obj.reset();
        translate_type(obj.current(), output);

        output.print(" " + obj.name() + "(");

        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
            {
                someParams = true;

                while (obj.end() != true)
                {

                    obj.current().reset();
                    translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());

                    output.print(" " + obj.current().name());

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() == IdlType.e_param)
                            output.print(", ");
                        else
                            break;
                    }
                }
            }
        }

        c = getContext(obj);

        if (c != null)
        {
            if (someParams == true)
                output.print(", ");

            output.print("org.omg.CORBA.Context ctx");
        }

        output.print(")");

        if (obj.end() != true)
            if (obj.current().kind() == IdlType.e_raises)
            {
                output.println("");
                output.print(tab2 + "throws ");
                r = (IdlRaises) obj.current();

                r.reset();

                while (r.end() != true)
                {

                    output.print(fullname(r.current()));

                    r.next();

                    if (r.end() != true)

                        output.print(", ");

                }
            }

        output.println("");
        output.println(tab + "{");

        obj.reset();

        output.print(tab2 + "");

        if (obj.current().kind() == IdlType.e_simple)
        {
            if (((IdlSimple) obj.current()).internal() != Token.t_void)
                output.print("return ");
        }
        else
            output.print("return ");

        obj.next();

        output.print("_tie." + obj.name() + "(");

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
                while (obj.end() != true)
                {
                    output.print(" " + obj.current().name());

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() == IdlType.e_param)
                            output.print(", ");
                        else
                            break;
                    }
                }
        }

        c = getContext(obj);

        if (c != null)
        {
            if (someParams == true)
                output.print(", ");

            output.print("ctx");
        }

        output.println(");");

        output.println(tab + "}");
        output.println("");
    }

    /**
     * Translate an operation for a skeleton
     *
     * @param obj operation to translate
     * @param output write access
     */
    public void translate_operation_skel(IdlObject obj, java.io.PrintWriter output)
    {
        IdlRaises r;
        IdlContext c;
        int i = 0;
        boolean raises = false;
        boolean someParams = false;

        output.println(tab + "org.omg.CORBA.NVList argList = orb.create_list(0);");

        obj.reset();
        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
                while (obj.end() != true)
                {
                    output.println(tab2 + "org.omg.CORBA.Any arg" + i + " = orb.create_any();");
                    obj.current().reset();

                    switch (((IdlParam) obj.current()).param_attr())
                    {

                    case 0 :
                        output.print(tab2 + "arg" + i + ".type(");
                        translate_typecode(obj.current().current(), output);
                        output.println(");");
                        output.println(tab2 + "argList.add_value(\"\",arg" + i + ",org.omg.CORBA.ARG_IN.value);");
                        break;

                    case 1 :
                        output.println(tab2 + "argList.add_value(\"\",arg" + i + ",org.omg.CORBA.ARG_OUT.value);");
                        break;

                    case 2 :
                        output.print(tab2 + "arg" + i + ".type(");
                        translate_typecode(obj.current().current(), output);
                        output.println(");");
                        output.println(tab2 + "argList.add_value(\"\",arg" + i + ",org.omg.CORBA.ARG_INOUT.value);");
                        break;

                    }

                    i++;

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() != IdlType.e_param)
                            break;
                    }
                }
        }

        i = 0;
        output.println("");
        output.println(tab2 + "request.arguments(argList);");
        output.println("");

        c = getContext(obj);

        if (c != null)
        {
            output.println(tab2 + "org.omg.CORBA.Context arg_ctx = request.ctx();");
            output.println("");
        }

        obj.reset();
        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
                while (obj.end() != true)
                {
                    obj.current().reset();

                    switch (((IdlParam) obj.current()).param_attr())
                    {

                    case 0 :
                        output.print(tab2 + "");
                        translate_type(obj.current().current(), output);
                        output.print(" arg" + i + "_in = ");
                        translate_unmarshalling_data(obj.current().current(), output, "arg" + i + ".create_input_stream()");
                        break;

                    case 1 :
                        output.print(tab2 + "");
                        translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());
                        output.print(" arg" + i + "_out = new ");
                        translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());
                        output.println("();");
                        break;

                    case 2 :
                        output.print(tab2 + "");
                        translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());
                        output.print(" arg" + i + "_inout = new ");
                        translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());
                        output.println("();");
                        output.print(tab2 + "arg" + i + "_inout.value = ");
                        translate_unmarshalling_data(obj.current().current(), output, "arg" + i + ".create_input_stream()");
                        break;
                    }

                    i++;

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() != IdlType.e_param)
                            break;
                    }
                }
        }

        i = 0;
        output.println("");

        if (obj.end() != true)
            if (obj.current().kind() == IdlType.e_raises)
            {
                output.println(tab2 + "try");
                output.println(tab2 + "{");
                raises = true;
            }

        obj.reset();

        if (obj.current().kind() == IdlType.e_simple)
        {
            if (((IdlSimple) obj.current()).internal() != Token.t_void)
            {
                if (raises == true)
                    output.print(tab + "");

                output.print(tab2 + "");

                translate_type(obj.current(), output);

                output.print(" _arg_result = ");
            }
            else
            {
                if (raises == true)
                    output.print(tab + "");

                output.print(tab2 + "");
            }
        }
        else
        {
            if (raises == true)
                output.print(tab + "");

            output.print(tab2 + "");

            translate_type(obj.current(), output);

            output.print(" _arg_result = ");
        }



        output.print(obj.name() + "(");

        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
            {
                someParams = true;

                while (obj.end() != true)
                {
                    obj.current().reset();

                    switch (((IdlParam) obj.current()).param_attr())
                    {

                    case 0 :
                        output.print("arg" + i + "_in");
                        break;

                    case 1 :
                        output.print("arg" + i + "_out");
                        break;

                    case 2 :
                        output.print("arg" + i + "_inout");
                        break;
                    }

                    i++;

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() != IdlType.e_param)
                            break;
                        else
                            output.print(", ");
                    }
                }
            }
        }

        i = 0;

        if (c != null)
        {
            if (someParams == true)
                output.print(", ");

            output.print("arg_ctx");
        }

        output.println(");");
        output.println("");

        obj.reset();

        if (obj.current().kind() == IdlType.e_simple)
        {
            if (((IdlSimple) obj.current()).internal() != Token.t_void)
            {
                if (raises == true)
                    output.print(tab + "");

                output.println(tab2 + "org.omg.CORBA.Any any_result = orb.create_any();");

                if (raises == true)
                    output.print(tab + "");

                output.print(tab2 + "");

                translate_any_insert(obj.current(), output, "any_result", "_arg_result");

                output.println(";");

                if (raises == true)
                    output.print(tab + "");

                output.println(tab2 + "request.set_result(any_result);");
            }
        }
        else
        {
            if (raises == true)
                output.print(tab + "");

            output.println(tab2 + "org.omg.CORBA.Any any_result = orb.create_any();");

            if (raises == true)
                output.print(tab + "");

            output.print(tab2 + "");

            translate_any_insert(obj.current(), output, "any_result", "_arg_result");

            output.println(";");

            if (raises == true)
                output.print(tab + "");

            output.println(tab2 + "request.set_result(any_result);");
        }

        output.println("");

        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
                while (obj.end() != true)
                {
                    obj.current().reset();

                    switch (((IdlParam) obj.current()).param_attr())
                    {

                    case 0 :
                        break;

                    case 1 :

                        if (raises == true)
                            output.print(tab + "");

                        output.print(tab2 + "");

                        translate_any_insert(obj.current().current(), output, "arg" + i, "arg" + i + "_out.value");

                        output.println(";");

                        break;

                    case 2 :
                        if (raises == true)
                            output.print(tab + "");

                        output.print(tab2 + "");

                        translate_any_insert(obj.current().current(), output, "arg" + i, "arg" + i + "_inout.value");

                        output.println(";");

                        break;
                    }

                    i++;

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() != IdlType.e_param)
                            break;
                    }
                }
        }

        if (obj.end() != true)
            if (obj.current().kind() == IdlType.e_raises)
            {

                r = (IdlRaises) obj.current();

                r.reset();

                output.println(tab2 + "}");

                while (r.end() != true)
                {

                    output.println(tab2 + "catch (" + fullname(r.current()) + " ex)");
                    output.println(tab2 + "{");
                    output.println(tab3 + "org.omg.CORBA.Any any_ex = orb.create_any();");
                    output.println(tab3 + "" + fullname(r.current()) + "Helper.insert(any_ex,ex);");
                    output.println(tab3 + "request.set_exception(any_ex);");
                    output.println(tab2 + "}");

                    r.next();

                }

            }
    }

    /**
     * Translate an operation for a skeleton with Stream mode
     *
     * @param obj operation to translate
     * @param output write access
     */
    public void translate_operation_skel_stream(IdlObject obj, java.io.PrintWriter output)
    {
        IdlRaises r;
        IdlContext c;
        int i = 0;
        boolean raises = false;
        boolean someParams = false;

        // Extract the parameters
        obj.reset();
        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
                while (obj.end() != true)
                {
                    obj.current().reset();

                    switch (((IdlParam) obj.current()).param_attr())
                    {

                    case 0 :
                        output.print(tab2 + "");
                        translate_type(obj.current().current(), output);
                        output.print(" arg" + i + "_in = ");
                        translate_unmarshalling_data(obj.current().current(), output, "_is");
                        break;

                    case 1 :
                        output.print(tab2 + "");
                        translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());
                        output.print(" arg" + i + "_out = new ");
                        translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());
                        output.println("();");
                        break;

                    case 2 :
                        output.print(tab2 + "");
                        translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());
                        output.print(" arg" + i + "_inout = new ");
                        translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());
                        output.println("();");
                        output.print(tab2 + "arg" + i + "_inout.value = ");
                        translate_unmarshalling_data(obj.current().current(), output, "_is");
                        break;
                    }

                    i++;

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() != IdlType.e_param)
                            break;
                    }
                }
        }

        i = 0;
        output.println("");

        c = getContext(obj);

        if (c != null)
        {
            output.println(tab2 + "org.omg.CORBA.Context arg_ctx = _is.read_Context();");
            output.println("");
        }

        // Check if the exceptions can be managed
        if (obj.end() != true)
            if (obj.current().kind() == IdlType.e_raises)
            {
                output.println(tab2 + "try");
                output.println(tab2 + "{");
                raises = true;
            }

        obj.reset();

        // Do the invoke
        if (obj.current().kind() == IdlType.e_simple)
        {
            if (((IdlSimple) obj.current()).internal() != Token.t_void)
            {
                if (raises == true)
                    output.print(tab + "");

                output.print(tab2 + "");

                translate_type(obj.current(), output);

                output.print(" _arg_result = ");
            }
            else
            {
                if (raises == true)
                    output.print(tab + "");

                output.print(tab2 + "");
            }
        }
        else
        {
            if (raises == true)
                output.print(tab + "");

            output.print(tab2 + "");

            translate_type(obj.current(), output);

            output.print(" _arg_result = ");
        }



        output.print(obj.name() + "(");

        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
            {
                someParams = true;

                while (obj.end() != true)
                {
                    obj.current().reset();

                    switch (((IdlParam) obj.current()).param_attr())
                    {

                    case 0 :
                        output.print("arg" + i + "_in");
                        break;

                    case 1 :
                        output.print("arg" + i + "_out");
                        break;

                    case 2 :
                        output.print("arg" + i + "_inout");
                        break;
                    }

                    i++;

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() != IdlType.e_param)
                            break;
                        else
                            output.print(", ");
                    }
                }
            }
        }

        i = 0;

        if (c != null)
        {
            if (someParams == true)
                output.print(", ");

            output.print("arg_ctx");
        }

        output.println(");");
        output.println("");

        if (raises == true)
            output.print(tab + "");

        output.println(tab2 + "_output = handler.createReply();");

        // Encode return parameters
        obj.reset();

        if (obj.current().kind() == IdlType.e_simple)
        {
            if (((IdlSimple) obj.current()).internal() != Token.t_void)
            {
                if (raises == true)
                    output.print(tab + "");

                output.print(tab2 + "");

                translate_marshalling_data(obj.current(), output, "_output", "_arg_result");
            }
        }
        else
        {
            if (raises == true)
                output.print(tab + "");

            output.print(tab2 + "");

            translate_marshalling_data(obj.current(), output, "_output", "_arg_result");
        }

        output.println("");

        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
                while (obj.end() != true)
                {
                    obj.current().reset();

                    switch (((IdlParam) obj.current()).param_attr())
                    {

                    case 0 :
                        break;

                    case 1 :

                        if (raises == true)
                            output.print(tab + "");

                        output.print(tab2 + "");

                        translate_marshalling_data(obj.current().current(), output, "_output", "arg" + i + "_out.value");

                        break;

                    case 2 :
                        if (raises == true)
                            output.print(tab + "");

                        output.print(tab2 + "");

                        translate_marshalling_data(obj.current().current(), output, "_output", "arg" + i + "_inout.value");

                        break;
                    }

                    i++;

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() != IdlType.e_param)
                            break;
                    }
                }
        }

        // Catch th exceptions
        if (obj.end() != true)
            if (obj.current().kind() == IdlType.e_raises)
            {

                r = (IdlRaises) obj.current();

                r.reset();

                output.println(tab2 + "}");

                while (r.end() != true)
                {

                    output.println(tab2 + "catch (" + fullname(r.current()) + " _exception)");
                    output.println(tab2 + "{");
                    output.println(tab3 + "_output = handler.createExceptionReply();");
                    output.println(tab3 + "" + fullname(r.current()) + "Helper.write(_output,_exception);");
                    output.println(tab2 + "}");

                    r.next();
                }
            }

        // Retourne le output
        output.println(tab2 + "return _output;");
    }

    /**
     * Translate an operation
     *
     * @param obj operation to translate
     * @param output write access
     */
    public void translate_operation_stub(IdlObject obj, java.io.PrintWriter output)
    {
        IdlRaises r;
        IdlContext c;
        boolean someParams = false;
        int i = 0;

        output.println(tab + "/**");
        output.println(tab + " * Operation " + obj.name());
        output.println(tab + " */");
        output.print(tab + "public ");

        obj.reset();
        translate_type(obj.current(), output);

        output.print(" " + obj.name() + "(");

        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
            {
                someParams = true;

                while (obj.end() != true)
                {

                    obj.current().reset();
                    translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());

                    output.print(" " + obj.current().name());

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() == IdlType.e_param)
                            output.print(", ");
                        else
                            break;
                    }
                }
            }
        }

        c = getContext(obj);

        if (c != null)
        {
            if (someParams == true)
                output.print(", ");

            output.print("org.omg.CORBA.Context ctx");
        }

        output.print(")");

        if (obj.end() != true)
            if (obj.current().kind() == IdlType.e_raises)
            {
                output.println("");
                output.print(tab2 + "throws ");
                r = (IdlRaises) obj.current();

                r.reset();

                while (r.end() != true)
                {

                    output.print(fullname(r.current()));

                    r.next();

                    if (r.end() != true)

                        output.print(", ");

                }
            }

        output.println("");
        output.println(tab + "{");
        output.println(tab2 + "org.omg.CORBA.Request request = _request(\"" + initialName(obj.name()) + "\");");
        output.println(tab3 + "org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");
        output.println("");

        if (c != null)
        {
            List ctx = c.getValues();

            for (int j = 0; j < ctx.size(); j++)
            {
                output.print(tab2 + "request.contexts().add(\"");
                output.print((String) ctx.get(j));
                output.println("\");");
            }

            output.println(tab2 + "request.ctx(ctx);");
            output.println("");
        }


        obj.reset();

        if (obj.current().kind() == IdlType.e_simple)
        {
            if (((IdlSimple) obj.current()).internal() != Token.t_void)
            {
                output.print(tab2 + "request.set_return_type(");
                translate_typecode(obj.current(), output);
                output.println(");");
                output.println("");
            }
        }
        else
        {
            output.print(tab2 + "request.set_return_type(");
            translate_typecode(obj.current(), output);
            output.println(");");
            output.println("");
        }

        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
                while (obj.end() != true)
                {

                    obj.current().reset();

                    switch (((IdlParam) obj.current()).param_attr())
                    {

                    case 0 :
                        output.println(tab2 + "org.omg.CORBA.Any arg" + i + " = request.add_in_arg();");
                        output.print(tab2 + "");
                        obj.current().reset();
                        translate_any_insert(obj.current().current(), output, "arg" + i, obj.current().name());
                        output.println(";");
                        break;

                    case 1 :
                        output.println(tab2 + "org.omg.CORBA.Any arg" + i + " = request.add_out_arg();");
                        output.print(tab2 + "arg" + i + ".type(");
                        translate_typecode(obj.current().current(), output);
                        output.println(");");
                        break;

                    case 2 :
                        output.println(tab2 + "org.omg.CORBA.Any arg" + i + " = request.add_inout_arg();");
                        output.print(tab2 + "");
                        obj.current().reset();
                        translate_any_insert(obj.current().current(), output, "arg" + i, obj.current().name() + ".value");
                        output.println(";");
                        break;
                    }

                    i++;

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() != IdlType.e_param)
                        {
                            break;
                        }
                    }
                }
        }

        if (obj.end() != true)
            if (obj.current().kind() == IdlType.e_raises)
            {

                output.println("");

                r = (IdlRaises) obj.current();

                r.reset();

                while (r.end() != true)
                {

                    output.print(tab2 + "request.exceptions().add(");
                    output.println(fullname(r.current()) + "Helper.type());");

                    r.next();
                }

            }

        output.println("");

        if (((IdlOp) obj).oneway() == true)
            output.println(tab2 + "request.send_oneway();");
        else
            output.println(tab2 + "request.invoke();");

        output.println("");

        if (obj.end() != true)
            if (obj.current().kind() == IdlType.e_raises)
            {
                output.println(tab2 + "Exception except = request.env().exception();");
                output.println(tab2 + "if (except != null)");
                output.println(tab2 + "{");
                output.println(tab2 + " org.omg.CORBA.UnknownUserException unk_except;");
                output.println(tab2 + " try");
                output.println(tab2 + " {");
                output.println(tab2 + "  unk_except = (org.omg.CORBA.UnknownUserException)except;");
                output.println(tab2 + " }");
                output.println(tab2 + " catch (ClassCastException ex)");
                output.println(tab2 + " {");
                output.println(tab2 + "  throw (org.omg.CORBA.SystemException)except;");
                output.println(tab2 + " }");
                output.println("");

                r = (IdlRaises) obj.current();

                r.reset();

                while (r.end() != true)
                {

                    output.println(tab2 + " try {");
                    output.println(tab2 + "  throw " + fullname(r.current()) + "Helper.extract(unk_except.except);");
                    output.println(tab2 + " } catch (final org.omg.CORBA.MARSHAL e) {");
                    output.println(tab2 + " }");
                    output.println("");

                    r.next();
                }

                output.println(tab2 + "throw new org.omg.CORBA.UNKNOWN();");
                output.println(tab2 + "}");
                output.println("");
            }


        obj.reset();
        obj.next();

        i = 0;

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
                while (obj.end() != true)
                {

                    obj.current().reset();

                    switch (((IdlParam) obj.current()).param_attr())
                    {

                    case 0 :

                        break;

                    case 1 :
                        output.print(tab2 + "" + obj.current().name() + ".value = ");
                        obj.current().reset();
                        translate_unmarshalling_data(obj.current().current(), output, "arg" + i + ".create_input_stream()");
                        break;

                    case 2 :
                        output.print(tab2 + "" + obj.current().name() + ".value = ");
                        obj.current().reset();
                        translate_unmarshalling_data(obj.current().current(), output, "arg" + i + ".create_input_stream()");
                        break;
                    }

                    i++;

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() != IdlType.e_param)
                            break;
                    }
                }
        }

        output.println("");
        obj.reset();

        if (obj.current().kind() == IdlType.e_simple)
        {
            if (((IdlSimple) obj.current()).internal() != Token.t_void)
            {
                output.println(tab2 + "org.omg.CORBA.Any _arg_result = request.return_value();");
                output.print(tab2 + "return ");

                translate_unmarshalling_data(obj.current(), output, "_arg_result.create_input_stream()");
            }
        }
        else
        {
            output.println(tab2 + "org.omg.CORBA.Any _arg_result = request.return_value();");
            output.print(tab2 + "return ");

            translate_unmarshalling_data(obj.current(), output, "_arg_result.create_input_stream()");
        }

        output.println(tab + "}");
        output.println("");
    }

    /**
     * Translate an operation in Stream mode
     *
     * @param obj operation to translate
     * @param output write access
     */
    public void translate_operation_stub_stream(IdlObject obj, java.io.PrintWriter output)
    {
        IdlRaises r;
        IdlContext c;
        boolean someParams = false;
        boolean noReturn = false;
        int p;

        output.println(tab + "/**");
        output.println(tab + " * Operation " + obj.name());
        output.println(tab + " */");
        output.print(tab + "public ");

        obj.reset();
        translate_type(obj.current(), output);

        output.print(" " + obj.name() + "(");

        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
            {
                someParams = true;

                while (obj.end() != true)
                {

                    obj.current().reset();
                    translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());

                    output.print(" " + obj.current().name());

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() == IdlType.e_param)
                            output.print(", ");
                        else
                            break;
                    }
                }
            }
        }

        c = getContext(obj);

        if (c != null)
        {
            if (someParams == true)
                output.print(", ");

            output.print("org.omg.CORBA.Context ctx");
        }

        output.print(")");

        if (obj.end() != true)
            if (obj.current().kind() == IdlType.e_raises)
            {
                output.println("");
                output.print(tab2 + "throws ");
                r = (IdlRaises) obj.current();

                r.reset();

                while (r.end() != true)
                {

                    output.print(fullname(r.current()));

                    r.next();

                    if (r.end() != true)

                        output.print(", ");

                }
            }

        output.println("");
        output.println(tab + "{");

        output.println(tab2 + "while(true)");
        output.println(tab2 + "{");

        if (m_cp.getM_local_stub())
        {
            output.println(tab3 + "if (!this._is_local())");
            output.println(tab3 + "{");
        }

        if (c != null)
        {
            output.println(tab4 + "org.omg.CORBA.ContextList ctxList = org.omg.CORBA.ORB.init().create_context_list();");
        }

        output.println(tab4 + "org.omg.CORBA.portable.InputStream _input = null;");
        output.println(tab4 + "try");
        output.println(tab4 + "{");

        if (((IdlOp) obj).oneway() == true)
            output.println(tab5 + "org.omg.CORBA.portable.OutputStream _output = this._request(\"" + initialName(obj.name()) + "\",false);");
        else
            output.println(tab5 + "org.omg.CORBA.portable.OutputStream _output = this._request(\"" + initialName(obj.name()) + "\",true);");

        // Encode the parameters

        obj.reset();

        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
                while (obj.end() != true)
                {

                    obj.current().reset();

                    switch (((IdlParam) obj.current()).param_attr())
                    {

                    case 0 :
                        output.print(tab5 + "");
                        obj.current().reset();
                        translate_marshalling_data(obj.current().current(), output, "_output", obj.current().name());
                        break;

                    case 2 :
                        output.print(tab5 + "");
                        obj.current().reset();
                        translate_marshalling_data(obj.current().current(), output, "_output", obj.current().name() + ".value");
                        break;
                    }

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() != IdlType.e_param)
                        {
                            break;
                        }
                    }
                }
        }

        // Encode the contexts
        if (c != null)
        {
            List ctx = c.getValues();

            for (int j = 0; j < ctx.size(); j++)
            {
                output.println(tab5 + "ctxList.add(\"" + (String) ctx.get(j) + "\");");
            }

            output.println(tab5 + "_output.write_Context(ctx,ctxList);");

            output.println("");
        }

        // Do the invoke

        output.println(tab5 + "_input = this._invoke(_output);");

        // Decode the params

        obj.reset();

        if (obj.current().kind() == IdlType.e_simple)
        {
            if (((IdlSimple) obj.current()).internal() != Token.t_void)
            {
                output.print(tab5 + "");
                translate_type(obj.current(), output);
                output.print(" _arg_ret = ");
                translate_unmarshalling_data(obj.current(), output, "_input");
            }
        }
        else
        {
            output.print(tab5 + "");
            translate_type(obj.current(), output);
            output.print(" _arg_ret = ");
            translate_unmarshalling_data(obj.current(), output, "_input");
        }

        obj.next();

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_param)
                while (obj.end() != true)
                {

                    obj.current().reset();

                    switch (((IdlParam) obj.current()).param_attr())
                    {

                    case 1 :

                    case 2 :
                        output.print(tab5 + "" + obj.current().name() + ".value = ");
                        obj.current().reset();
                        translate_unmarshalling_data(obj.current().current(), output, "_input");
                        break;
                    }

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() != IdlType.e_param)
                            break;
                    }
                }
        }

        p = obj.pos();

        // Return the result
        obj.reset();

        if (obj.current().kind() == IdlType.e_simple)
        {
            if (((IdlSimple) obj.current()).internal() != Token.t_void)
            {
                output.println(tab5 + "return _arg_ret;");
            }
            else
            {
                output.println(tab5 + "return;");
                noReturn = true;
            }
        }
        else
        {
            output.println(tab5 + "return _arg_ret;");
        }

        output.println(tab4 + "}");
        output.println(tab4 + "catch(org.omg.CORBA.portable.RemarshalException _exception)");
        output.println(tab4 + "{");
        output.println(tab5 + "continue;");
        output.println(tab4 + "}");

        // Get the user exceptions
        output.println(tab4 + "catch(org.omg.CORBA.portable.ApplicationException _exception)");
        output.println(tab4 + "{");
        output.println(tab5 + "String _exception_id = _exception.getId();");

        obj.pos(p);

        if (obj.end() != true)
            if (obj.current().kind() == IdlType.e_raises)
            {
                r = (IdlRaises) obj.current();

                r.reset();

                while (r.end() != true)
                {

                    output.println(tab5 + "if (_exception_id.equals(" + fullname(r.current()) + "Helper.id()))");
                    output.println(tab5 + "{");
                    output.println(tab6 + "throw " + fullname(r.current()) + "Helper.read(_exception.getInputStream());");
                    output.println(tab5 + "}");
                    output.println("");

                    r.next();
                }

            }

        output.println(tab5 + "throw new org.omg.CORBA.UNKNOWN(\"Unexpected User Exception: \"+ _exception_id);");
        output.println(tab4 + "}");

        output.println(tab4 + "finally");
        output.println(tab4 + "{");
        output.println(tab5 + "this._releaseReply(_input);");
        output.println(tab4 + "}");

        boolean isAbstract = obj.upper().kind() == IdlType.e_interface &&
                             ((IdlInterface) (obj.upper())).abstract_interface();

        if (m_cp.getM_local_stub())
        {
            output.println(tab3 + "}");
            output.println(tab3 + "else");
            output.println(tab3 + "{");
            output.println(tab4 + "org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke(\"" + obj.name() + "\",_opsClass);");
            output.println(tab4 + "if (_so == null)");
            output.println(tab4 + "   continue;");

            if (isAbstract)
                output.println(tab4 + "" + fullname(obj.upper()) + " _self = (" + fullname(obj.upper()) + ") _so.servant;");
            else
                output.println(tab4 + "" + fullname(obj.upper()) + "Operations _self = (" + fullname(obj.upper()) + "Operations) _so.servant;");

            output.println(tab4 + "try");

            output.println(tab4 + "{");

            if (noReturn)
                output.print(tab5 + "_self." + obj.name() + "(");
            else
                output.print(tab5 + "return _self." + obj.name() + "(");

            obj.reset();

            obj.next();

            if (obj.end() != true)
            {
                if (obj.current().kind() == IdlType.e_param)
                {
                    someParams = true;

                    while (obj.end() != true)
                    {

                        obj.current().reset();
                        output.print(" " + obj.current().name());

                        obj.next();

                        if (obj.end() != true)
                        {
                            if (obj.current().kind() == IdlType.e_param)
                                output.print(", ");
                            else
                                break;
                        }
                    }
                }
            }

            c = getContext(obj);

            if (c != null)
            {
                if (someParams)
                    output.print(", ");

                output.print("ctx");
            }

            output.println(");");

            if (noReturn)
                output.println(tab5 + "return;");

            output.println(tab4 + "}");

            output.println(tab4 + "finally");

            output.println(tab4 + "{");

            output.println(tab5 + "_servant_postinvoke(_so);");

            output.println(tab4 + "}");

            output.println(tab3 + "}");
        }

        output.println(tab2 + "}");

        output.println(tab + "}");
        output.println("");
    }

    /**
     * Translate the operations declaration of an interface
     *
     * @param obj  interface from which operations must be translated
     * @param writeInto the directory where the interface must be defined
     */
    public void translate_interface_operations(IdlObject obj, java.io.File writeInto)
    {
        java.io.PrintWriter output;

        if (!((IdlInterface) obj).abstract_interface())
            output = newFile(writeInto, obj.name() + "Operations");
        else
            output = newFile(writeInto, obj.name());

        List list = ((IdlInterface) obj).getInheritance();

        addDescriptiveHeader(output, obj);

        // Interface header
        if (!((IdlInterface) obj).abstract_interface())
        {
            output.print("public interface " + obj.name() + "Operations");
            if (list.size() != 0)
            {
                output.print(" extends ");
            }
        }
        else
        {
            output.print("public interface " + obj.name() + " extends org.omg.CORBA.portable.IDLEntity");

            if (list.size() != 0)
            {
                output.print(", ");
            }
        }

        if (list.size() != 0)
        {
            for (int i = 0; i < list.size(); i++)
            {
                if (!((IdlInterface) list.get(i)).abstract_interface())
                    output.print(fullname(((IdlObject) list.get(i))) + "Operations");
                else
                    output.print(fullname(((IdlObject) list.get(i))));

                if (i != (list.size() - 1))
                    output.print(", ");
            }

            output.println("");
        }
        else
            output.println("");

        output.println("{");

        obj.reset();

        while (obj.end() != true)
        {
            switch (obj.current().kind())
            {

            case IdlType.e_operation :
                translate_operation(obj.current(), output);
                break;

            case IdlType.e_attribute :
                translate_attribute(obj.current(), output);
                break;

            case IdlType.e_const:
                // Modification by Jojakim Stahl
                if (((IdlInterface) obj).abstract_interface())
                {
                    translate_constant(obj.current(), null, output);
                }

                break;
            }

            obj.next();
        }

        output.println("}");
        output.close();
    }


    /**
     * This method an interface is empty in terms of
     * type typedef, union, exception...
     */
    public boolean isEmptyInterface(IdlObject obj)
    {
        obj.reset();

        while (obj.end() != true)
        {
            switch (obj.current().kind())
            {

            case IdlType.e_enum :
                return false;

            case IdlType.e_struct :
                return false;

            case IdlType.e_union :
                return false;

            case IdlType.e_typedef :
                return false;

            case IdlType.e_exception :
                return false;

            case IdlType.e_native :
                return false;
            }

            obj.next();
        }

        return true;
    }

    /**
     * This method check a value type is empty in terms of
     * type typedef, union, exception...
     */
    public boolean isEmptyValue(IdlObject obj)
    {
        obj.reset();

        while (obj.end() != true)
        {
            switch (obj.current().kind())
            {

            case IdlType.e_enum :
                return false;

            case IdlType.e_struct :
                return false;

            case IdlType.e_union :
                return false;

            case IdlType.e_typedef :
                return false;

            case IdlType.e_exception :
                return false;

            case IdlType.e_native :
                return false;
            }

            obj.next();
        }

        return true;
    }

    /**
     * Check a type is empty in terms of
     * type union, struct or enum
     */
    public boolean isEmpty(IdlObject obj)
    {
        obj.reset();

        while (obj.end() != true)
        {
            obj.current().reset ();

            switch (obj.current().current().kind())
            {

            case IdlType.e_enum :
                return false;

            case IdlType.e_struct :
                return false;

            case IdlType.e_union :
                return false;
            }

            obj.next();
        }

        return true;
    }

    /**
     * Translate an interface
     *
     * @param obj interface to translate
     * @param writeInto the directory where the interface must be defined
     */
    public void translate_interface(IdlObject obj, java.io.File writeInto)
    {
        java.io.PrintWriter output = null;

        if (!((IdlInterface) obj).abstract_interface())
        {
            output = newFile(writeInto, obj.name());
            addDescriptiveHeader(output, obj);
        }

        String old_pkg;
        List list = ((IdlInterface) obj).getInheritance();

        // Declare the operations interface
        translate_interface_operations(obj, writeInto);

        java.io.File intoMe = null;

        if (isEmptyInterface(obj) == false)
            intoMe = createDirectory(obj.name() + "Package", writeInto);
        else
            intoMe = writeInto;

        if (!((IdlInterface) obj).abstract_interface())
        {
            // Interface header
            output.print("public interface " + obj.name() + " extends " + obj.name() + "Operations");

            if (list.size() != 0)
            {
                for (int i = 0; i < list.size(); i++)
                {
                    output.print(", " + fullname(((IdlObject) list.get(i))));
                }

                //output.println("");
            }

            output.println(", org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity");

            output.println("{");
        }

        // Translate the internal definitions
        old_pkg = current_pkg;

        addToPkg(obj, obj.name() + "Package");

        obj.reset();

        while (obj.end() != true)
        {
            switch (obj.current().kind())
            {

            case IdlType.e_enum :
                translate_enum(obj.current(), intoMe);
                break;

            case IdlType.e_struct :
                translate_struct(obj.current(), intoMe);
                break;

            case IdlType.e_union :
                translate_union(obj.current(), intoMe);
                break;

            case IdlType.e_typedef :
                translate_typedef(obj.current(), intoMe);
                break;

            case IdlType.e_exception :
                translate_exception(obj.current(), intoMe);
                break;

            case IdlType.e_native :
                translate_native(obj.current(), intoMe);
                break;

            case IdlType.e_const :
                // Modification by Jojakim Stahl
                if (!((IdlInterface) obj).abstract_interface())
                {
                    translate_constant(obj.current(), null, output);
                }

                break;
            }

            obj.next();
        }

        if (!((IdlInterface) obj).abstract_interface())
        {
            output.println("}");
            output.close();
        }

        current_pkg = old_pkg;

        write_helper(obj, writeInto);
        write_holder(obj, writeInto);
    }

    /**
     * Translate an interface for the user
     *
     * @param obj interface to translate
     * @param writeInto the directory where the interface must be defined
     */
    public void translate_user_interface(IdlObject obj, java.io.File writeInto)
    {
        // Deprecated
        // java.io.PrintStream output = ...
        java.io.PrintWriter output = newFile(initial, obj.name() + "Impl");

        String old_pkg;

        output.println("//");
        output.println("// Interface implementation definition : " + obj.name());
        output.println("//");
        output.println("// !!!! THIS CODE MUST BE COMPLETED TO BE USED !!!");
        output.println("//");

        if (m_cp.getM_pidl() == false)
        {
            if (m_cp.getM_map_poa() == true)
            {
                if (obj.upper().upper() != null)
                    output.println("public class " + obj.name() + "Impl extends " + fullname(obj.upper()) + "." + obj.name() + "POA");
                else
                    output.println("public class " + obj.name() + "Impl extends " + obj.name() + "POA");
            }
            else
            {
                if (obj.upper().upper() != null)
                    output.println("public class " + obj.name() + "Impl extends " + fullname(obj.upper()) + "._" + obj.name() + "ImplBase");
                else
                    output.println("public class " + obj.name() + "Impl extends _" + obj.name() + "ImplBase");
            }
        }
        else
            output.println("public class " + obj.name() + "Impl implements " + fullname(obj) + "Operations");

        output.println("{");

        // Translate the internal definitions
        old_pkg = current_pkg;

        addToPkg(obj, obj.name() + "Package");

        obj.reset();

        while (obj.end() != true)
        {
            switch (obj.current().kind())
            {

            case IdlType.e_operation :
                translate_user_operation(obj.current(), output);
                break;

            case IdlType.e_attribute :
                translate_user_attribute(obj.current(), output);
                break;
            }

            obj.next();
        }

        output.println("}");
        output.close();

        current_pkg = old_pkg;
    }

    /**
     * Check if the operation or the attribute is already in the list
     *
     * @param opList operations and attributes list
     * @param obj the operation or attribute
     * @return  true if the operation or attribute is included in the list
     */
    public boolean isInto(List opList, IdlObject obj)
    {
        for (int i = 0; i < opList.size(); i++)
        {
            IdlObject elem = (IdlObject) opList.get(i);

            if (obj.name().equals(elem.name()) &&
                    obj.kind() == elem.kind())
            {
                // we would end up with more than one getXYZ()/setXYZ() method
                if (obj.kind() == IdlType.e_attribute)
                    return true;

                // check the signature
                if (obj.kind() == IdlType.e_operation)
                {
                    IdlOp op = (IdlOp) obj;
                    org.openorb.compiler.idl.reflect.idlParameter [] op_params = op.parameters();
                    IdlOp ops = (IdlOp) elem;
                    org.openorb.compiler.idl.reflect.idlParameter [] ops_params = ops.parameters();

                    // different number of parameters
                    if (op_params.length == ops_params.length)
                    {
                        boolean identical = true;

                        // compare the parameters, return true when equal, continue otherwise
                        for (int j = 0; j < op_params.length; j++)
                            if (((IdlObject) op_params[ j ]).kind() != ((IdlObject) op_params[ j ]).kind())
                                identical = false;

                        if (identical)
                            return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Return the list of operations and attributes to implement
     * (incl. inheritance).
     *
     * @param obj interface object
     * @param opList already found operations and attributes list
     * @return operations and attributes list
     */
    protected List getInheritanceOpList(IdlObject obj, List opList)
    {
        List list = ((IdlInterface) obj).getInheritance();

        obj.reset();

        while (obj.end() != true)
        {
            switch (obj.current().kind())
            {

            case IdlType.e_attribute :

            case IdlType.e_operation :

                if (!isInto(opList, obj.current()))
                    opList.add(obj.current());

                break;
            }

            obj.next();
        }

        for ( int i = 0; i < list.size(); i++ )
        {
            if ( list.get(i) instanceof IdlInterface )
            {
                IdlInterface itf = ( IdlInterface ) list.get(i);

                if ( itf.kind() == IdlType.e_forward_interface )
                    itf = itf.getInterface();

                opList = getInheritanceOpList( itf, opList );
            }
        }

        return opList;
    }

    /**
     * Return the list of operations and attributes to implement
     * (incl. the inheritance).
     *
     * @param obj the interface object
     * @param inList already found operations and attributes list
     * @return operations and attributes list
     */
    protected List getInheritanceList(IdlObject obj, List inList)
    {
        if ( ! ( obj instanceof IdlInterface ) )
            return inList;

        IdlInterface itf = (IdlInterface) obj;

        if (itf.isForward())
            itf = itf.getInterface();

        List list = itf.getInheritance();

        boolean found = false;

        for (int i = 0; i < inList.size(); i++)
        {
            if ((inList.get(i)).equals(obj.getId()))
            {
                found = true;
                break;
            }
        }

        if (found == false)
        {
            inList.add(obj.getId());
        }


        for (int i = 0; i < list.size(); i++)
            inList = getInheritanceList((IdlObject) list.get(i), inList);

        return inList;
    }

    /**
     * Return the list of classes to import explicitly.
     * Normally the classes/interfaces are fully qualified,
     * so that no import statement is necessary. If a class
     * or an interface does not have a package statement, i.e.
     * it is located in the global scope, then the class needs
     * to be imported explicitly so that the compilation of
     * stubs and ties can be completed successfully.
     *
     * @param root the idl tree root object
     * @return classes and interfaces to import
     */
    protected List getImportList(IdlObject root)
    {
        if (root == null || root._list == null)
            return null;

        List impList = new ArrayList();

        for (int i = 0; i < root._list.size(); i++)
        {
            if (((IdlObject) root._list.get(i)) instanceof IdlInclude)
            {
                IdlInclude inc = (IdlInclude) (root._list.get(i));

                if (!inc.file_name().equals("") && inc.file_name().indexOf(java.io.File.separatorChar) <= 0)
                {
                    String n = inc.file_name();
                    // The IDL name of exceptions have an "Ex" appended
                    // because we are creating a Java import list here we
                    // need to revert what the RMIoverIIOP's
                    // MappingAPI.process_exception_suffix()
                    // did to the name of the exception
                    if (n.endsWith("Ex"))
                        n += "ception";

                    impList.add(n);
                }
            }
        }

        return impList;
    }

    /**
     * Return the initial name.
     */
    private String initialName(String name)
    {
        if (name.startsWith("_"))
            return name.substring(1, name.length());

        return name;
    }

    /**
     * Translate a syub for an interface
     *
     * @param obj the interface to translate
     * @param writeInto the directory where the interface must be defined
     */
    public void translate_interface_stub(IdlObject obj, java.io.File writeInto)
    {
        java.io.PrintWriter output = newFile(writeInto, "_" + obj.name() + "Stub");

        String old_pkg;
        List list = new ArrayList();

        list = getInheritanceList(obj, list);

        addDescriptiveHeader(output, obj);

        // Interface header
        output.println("public class _" + obj.name() + "Stub extends org.omg.CORBA.portable.ObjectImpl");
        output.print(tab2 + "implements " + obj.name());
        output.println("");
        output.println("{");

        List intoList = getInheritanceOpList(obj, new ArrayList());

        // Construct the _ids list
        output.println(tab + "static final String[] _ids_list =");
        output.println(tab + "{");

        if (list.size() != 0)
        {
            for (int i = 0; i < list.size(); i++)
            {
                output.print(tab2 + "\"" + (String) list.get(i) + "\"");

                if (i + 1 < list.size())
                    output.println(", ");
            }
        }

        output.println("");
        output.println(tab + "};");
        output.println("");

        // Translate the operation _id
        output.println(tab + "public String[] _ids()");
        output.println(tab + "{");
        output.println(tab + " return _ids_list;");
        output.println(tab + "}");
        output.println("");

        // Local invocations
        if (!m_cp.getM_dynamic() && m_cp.getM_local_stub())
        {
            if (!((IdlInterface) obj).abstract_interface())
                output.println(tab + "private final static Class _opsClass = " + fullname(obj) + "Operations.class;");
            else
                output.println(tab + "private final static Class _opsClass = " + fullname(obj) + ".class;");

            output.println("");
        }

        // Translate the internal definitions
        old_pkg = current_pkg;

        addToPkg(obj, obj.name() + "Package");

        for (int i = 0; i < intoList.size(); i++)
        {
            switch (((IdlObject) intoList.get(i)).kind())
            {

            case IdlType.e_operation :

                if (m_cp.getM_dynamic())
                    translate_operation_stub(((IdlObject) intoList.get(i)), output);
                else
                    translate_operation_stub_stream(((IdlObject) intoList.get(i)), output);

                break;

            case IdlType.e_attribute :
                if (m_cp.getM_dynamic())
                    translate_attribute_stub(((IdlObject) intoList.get(i)), output);
                else
                    translate_attribute_stub_stream(((IdlObject) intoList.get(i)), output);

                break;
            }
        }

        output.println("}");
        output.close();

        current_pkg = old_pkg;
    }

    /**
     * Translate a skeleton for an interface
     *
     * @param obj the interface to translate
     * @param writeInto the directory where the interface must be defined
     */
    public void translate_interface_skel(IdlObject obj, java.io.File writeInto)
    {
        java.io.PrintWriter output = null;

        final String baseName;

        if (m_cp.getM_map_poa() == false)
        {
            baseName = "_" + obj.name() + "ImplBase";
        }
        else
        {
            baseName = obj.name() + "POA";
        }
        output = newFile(writeInto, baseName);

        List list = new ArrayList();

        list = getInheritanceList(obj, list);

        addDescriptiveHeader(output, obj);

        // Creation du package correspond au definitions internes de l'interface
        //java.io.File intoMe = getDirectory(obj.name()+"Package",writeInto);


        // En-tete de l'interface
        if (m_cp.getM_map_poa() == false)
        {
            if (m_cp.getM_dynamic())
                output.println("public abstract class " + baseName + " extends org.omg.CORBA.DynamicImplementation");
            else
                output.println("public abstract class " + baseName + " extends org.omg.CORBA.portable.ObjectImpl");

            output.print(tab2 + "implements " + obj.name());

            if (!m_cp.getM_dynamic())
                output.print(", org.omg.CORBA.portable.InvokeHandler");

            output.println("");
        }
        else
        {
            if (m_cp.getM_dynamic())
                output.println("public abstract class " + baseName + " extends org.omg.PortableServer.DynamicImplementation");
            else
                output.println("public abstract class " + baseName + " extends org.omg.PortableServer.Servant");

            output.print(tab2 + "implements " + obj.name() + "Operations");

            if (!m_cp.getM_dynamic())
                output.print(", org.omg.CORBA.portable.InvokeHandler");

            output.println("");
        }

        output.println("{");

        List intoList = getInheritanceOpList(obj, new ArrayList());

        if (m_cp.getM_map_poa() == false)
        {
            // Construct the _ids list
            output.println(tab + "static final String[] _ids_list = ");
            output.println(tab + "{");

            if (list.size() != 0)
            {
                for (int i = 0; i < list.size(); i++)
                {
                    output.print(tab2 + "\"" + (String) list.get(i) + "\"");

                    if (i + 1 < list.size())
                        output.println(", ");
                }
            }

            output.println("");
            output.println(tab + "};");
            output.println("");

            // Translate the operation _id
            output.println(tab + "public String[] _ids()");
            output.println(tab + "{");
            output.println(tab + " return _ids_list;");
            output.println(tab + "}");
            output.println("");
        }
        else
        {
            // Operation _this
            output.println(tab + "public " + obj.name() + " _this()");
            output.println(tab + "{");
            output.println(tab2 + "return " + obj.name() + "Helper.narrow(_this_object());");
            output.println(tab + "}");
            output.println("");

            // Operation _this(...)
            output.println(tab + "public " + obj.name() + " _this(org.omg.CORBA.ORB orb)");
            output.println(tab + "{");
            output.println(tab2 + "return " + obj.name() + "Helper.narrow(_this_object(orb));");
            output.println(tab + "}");
            output.println("");

            // Operation _all_interfaces

            output.println(tab + "private static String [] _ids_list =");
            output.println(tab + "{");

            if (list.size() != 0)
            {
                for (int i = 0; i < list.size(); i++)
                {
                    output.print(tab2 + "\"" + (String) list.get(i) + "\"");

                    if (i + 1 < list.size())
                        output.println(", ");
                }
            }

            output.println("");
            output.println(tab + "};");
            output.println("");

            output.println(tab + "public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte [] objectId)");
            output.println(tab + "{");
            output.println(tab2 + "return _ids_list;");
            output.println(tab + "}");
            output.println("");

        }

        boolean bUseHash = m_cp.getM_minTableSize() <= intoList.size();

        if (m_cp.getM_useReflection())
        {
            output.println(tab + "private static final Class[] operationTypes = {");

            if (m_cp.getM_dynamic())
            {
                output.println(tab3 + "org.omg.CORBA.ServerRequest.class};");
            }
            else
            {
                output.println(tab3 + "org.omg.CORBA.portable.InputStream.class,");
                output.println(tab3 + "org.omg.CORBA.portable.ResponseHandler.class};");
            }
            output.println();
        }

        final String[] operationNames = sortIntoArray(intoList);

        // generate ops hashtable
        if (bUseHash)
        {

            if (m_cp.getM_useReflection())
            {
                output.println(tab + "private static final java.util.Map operationMap = new java.util.HashMap();");
                output.println();
                output.println(tab + "static {");
                output.println(tab2 + "try {");
                output.println(tab3 + "final Class clazz = " + baseName + ".class;");

                for (int i = 0; i < operationNames.length; i++)
                {
                    final String name = operationNames[i];
                    output.println(tab3 + "operationMap.put(\"" + name + "\",");
                    output.println(tab5 + "clazz.getDeclaredMethod(\"_invoke_" + name + "\", operationTypes));");
                }

                output.println(tab2 + "} catch (final NoSuchMethodException e) {");
                writeThrowException(output, tab3, "Error", "\"Error constructing operation table\"", "e");
                output.println(tab2 + "}");
            }
            else
            if (m_cp.getM_useClasses())
            {
                output.println(tab + "private static final java.util.Map operationMap = new java.util.HashMap();");
                output.println();
                output.println(tab + "static {");
                for (int i = 0; i < operationNames.length; i++)
                {
                    final String name = operationNames[i];
                    output.println(tab3 + "operationMap.put(\"" + name + "\",");
                    output.println(tab5 + "new Operation_" + name + "());");
                }
            }
            else
            if (m_cp.getM_useSwitch())
            {
                output.println(tab + "private static final String[] operationNames = new String[" +
                        operationNames.length + "];");
                output.println();

                output.println(tab + "static {");
                for (int i = 0; i < operationNames.length; i++)
                {
                    output.println(tab2 + "operationNames[" + i + "] = \"" +
                            operationNames[i] + "\";");
                }
            }
            else
            {
                throw new Error("IllegalCondition");
            }


            output.println(tab + "}");
            output.println("");
        }

        // Translate the invoke operation
        if (m_cp.getM_dynamic())
        {
            output.println(tab1 + "public final void invoke (final org.omg.CORBA.ServerRequest request)");
            output.println(tab1 + "{");
            output.println(tab2 + "final String opName = request.operation();");
        }
        else
        {
            output.println(tab1 + "public final org.omg.CORBA.portable.OutputStream _invoke(final String opName,");
            output.println(tab3 + "final org.omg.CORBA.portable.InputStream _is,");
            output.println(tab3 + "final org.omg.CORBA.portable.ResponseHandler handler)");
            output.println(tab1 + "{");
        }

        output.println();

        if (bUseHash)
        {
            if (m_cp.getM_useReflection())
            {
                output.println(tab2 + "final java.lang.reflect.Method operation = (java.lang.reflect.Method)operationMap.get(opName);");
                output.println();

                output.println(tab2 + "if (null == operation) {");
                output.println(tab3 + "throw new org.omg.CORBA.BAD_OPERATION(opName);");
                output.println(tab2 + "}");
                output.println();
                output.println(tab2 + "try {");
                if (m_cp.getM_dynamic())
                {
                    output.println(tab3 + "operation.invoke(this, new Object[] {(Object)request});");
                    output.println(tab3 + "return;");
                }
                else
                {
                    output.println(tab3 + "return (org.omg.CORBA.portable.OutputStream)operation.invoke(this, new Object[] {(Object)_is, (Object)handler});");
                }
                output.println(tab2 + "} catch (final IllegalAccessException e) {");

                writeThrowException(output, tab3, "Error", "e.getMessage()", "e");

                output.println(tab2 + "} catch (final java.lang.reflect.InvocationTargetException e) {");
                output.println(tab3 + "if (e.getTargetException() instanceof RuntimeException) {");
                output.println(tab4 + "throw (RuntimeException)e.getTargetException();");
                output.println(tab3 + "}");
                output.println(tab3 + "if (e.getTargetException() instanceof Error) {");
                output.println(tab4 + "throw (Error)e.getTargetException();");
                output.println(tab3 + "}");

                writeThrowException(output, tab3, "Error", "e.getMessage()", "e");

                output.println(tab2 + "}");
            }
            else
            if (m_cp.getM_useClasses())
            {
                output.println(tab2 + "final AbstractOperation operation = (AbstractOperation)operationMap.get(opName);");
                output.println();

                output.println(tab2 + "if (null == operation) {");
                output.println(tab3 + "throw new org.omg.CORBA.BAD_OPERATION(opName);");
                output.println(tab2 + "}");
                output.println();
                if (m_cp.getM_dynamic())
                {
                    output.println(tab2 + "operation.invoke(this, request);");
                }
                else
                {
                    output.println(tab2 + "return operation.invoke(this, _is, handler);");
                }
            }
            else
            if (m_cp.getM_useSwitch())
            {
                output.println(tab2 + "final int index = java.util.Arrays.binarySearch(operationNames, opName);");

                output.println(tab2 + "if (index < 0) {");
                output.println(tab3 + "throw new org.omg.CORBA.BAD_OPERATION(opName);");
                output.println(tab2 + "}");

                output.println(tab2 + "switch (index) {");

                for (int i = 0; i < operationNames.length; i++)
                {
                    output.println(tab3 + "case " + i + " :");
                    if (m_cp.getM_dynamic())
                    {
                        output.println(tab4 + "_invoke_" + operationNames[i] + "(request);");
                        output.println(tab4 + "return;");
                    }
                    else
                    {
                        output.println(tab4 + "return _invoke_" + operationNames[i] + "(_is, handler);");
                    }
                }

                output.println(tab2 + "}");
                output.println();

                output.println(tab2 + "throw new Error(\"unreachable code\");");
            }
            else
            {
                throw new Error("IllegalCondition");
            }
        }
        else
        {
            output.print(tab2);
            for (int i = 0; i < operationNames.length; i++)
            {
                final String name = operationNames[i];

                output.println("if (opName.equals(\"" + name + "\")) {");
                if (m_cp.getM_dynamic())
                {
                    output.println(tab4 + "_invoke_" + name + "(request);");
                    output.println(tab4 + "return;");
                }
                else
                {
                    output.println(tab4 + "return _invoke_" + name + "(_is, handler);");
                }
                output.print(tab2 + "} else ");
            }

            output.println("{");
            output.println(tab3 + "throw new org.omg.CORBA.BAD_OPERATION(opName);");
            output.println(tab2 + "}");
        }

        output.println(tab + "}");
        output.println();

        // make helper methods
        output.println(tab1 + "// helper methods");
        for (int i = 0; i < intoList.size(); i++)
        {
            writeOperationHelperMethod((IdlObject)intoList.get(i), output, m_cp.getM_dynamic());
        }
        if (bUseHash && m_cp.getM_useClasses())
        {
            //
            output.println(tab1 + "// operation classes");
            output.println(tab1 + "private abstract static class AbstractOperation {");

            if (m_cp.getM_dynamic())
            {
                output.println(tab2 + "protected abstract void invoke(" + baseName + " target,");
                output.println(tab4 + "org.omg.CORBA.ServerRequest request);");
            }
            else
            {
                output.println(tab2 + "protected abstract org.omg.CORBA.portable.OutputStream invoke(");
                output.println(tab4 + baseName + " target,");
                output.println(tab4 + "org.omg.CORBA.portable.InputStream _is,");
                output.println(tab4 + "org.omg.CORBA.portable.ResponseHandler handler);");
            }

            output.println(tab1 + "}");
            output.println();
            for (int i = 0; i < intoList.size(); i++)
            {
                writeOperationHelperClass((IdlObject)intoList.get(i), output, m_cp.getM_dynamic(), baseName);
            }
        }

        output.println("}");

        output.close();
    }

    private String[] sortIntoArray(final List list) {
        final String[] output;

        {
            int count = 0;

            for (int i = 0; i < list.size(); i++) {
                final IdlObject obj = ((IdlObject)list.get(i));
                switch (obj.kind())
                {
                    case IdlType.e_operation :
                        count++;
                        break;

                    case IdlType.e_attribute :
                        count += ((!((IdlAttribute)obj).readOnly()) ? 2 : 1);
                        break;
                }
            }

            output = new String[count];
        }

        for (int i = 0, count = 0; i < list.size(); i++) {
            final IdlObject obj = ((IdlObject)list.get(i));
            final String name = initialName(obj.name());
            switch (obj.kind())
            {
                case IdlType.e_operation :
                    output[count++] = name;
                    break;

                case IdlType.e_attribute :
                    output[count++] = "_get_" + name;

                    if (!((IdlAttribute)obj).readOnly())
                    {
                        output[count++] = "_set_" + name;
                    }

                    break;
            }
        }

        Arrays.sort(output);

        return output;
    }

    private void writeOperationHelperMethod(final IdlObject obj, final java.io.PrintWriter output, final boolean dynamic)
    {
        switch (obj.kind())
        {
            case IdlType.e_operation :
                writeOperationHelperMethodHeader(obj, output, dynamic, "");

                if (dynamic)
                {
                    translate_operation_skel(obj, output);
                }
                else
                {
                    translate_operation_skel_stream(obj, output);
                }
                break;

            case IdlType.e_attribute :
                writeOperationHelperMethodHeader(obj, output, dynamic, "_get_");

                if (dynamic)
                {
                    translate_read_attribute_skel(obj, output);
                }
                else
                {
                    translate_read_attribute_skel_stream(obj, output);
                }

                if (!((IdlAttribute)obj).readOnly())
                {
                    output.println(tab1 + "}");
                    output.println();
                    writeOperationHelperMethodHeader(obj, output, dynamic, "_set_");

                    if (dynamic)
                    {
                        translate_write_attribute_skel(obj, output);
                    }
                    else
                    {
                        translate_write_attribute_skel_stream(obj, output);
                    }
                }
                break;
            default :
                throw new Error("Illegal condition");
        }
        output.println(tab1 + "}");
        output.println();
    }

    private void writeOperationHelperMethodHeader(final IdlObject obj,
            final java.io.PrintWriter output, final boolean dynamic,
            final String operationPrefix)
    {
        final String name = initialName(obj.name());
        if (dynamic)
        {
            output.println(tab1 + "private void _invoke_" + operationPrefix + name + "(");
            output.println(tab3 + "final org.omg.CORBA.ServerRequest request) {");
            output.println(tab2 + "final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();");
        }
        else
        {
            output.println(tab1 + "private org.omg.CORBA.portable.OutputStream _invoke_" + operationPrefix + name + "(");
            output.println(tab3 + "final org.omg.CORBA.portable.InputStream _is,");
            output.println(tab3 + "final org.omg.CORBA.portable.ResponseHandler handler) {");
            output.println(tab2 + "org.omg.CORBA.portable.OutputStream _output;");
        }
    }

    private void writeOperationHelperClass(final IdlObject obj,
            final java.io.PrintWriter output, final boolean dynamic,
            final String baseName)
    {
        final String name = initialName(obj.name());

        switch (obj.kind())
        {
            case IdlType.e_operation :
                writeOperationHelperClass(output, dynamic, baseName, name, "");
                break;

            case IdlType.e_attribute :
                writeOperationHelperClass(output, dynamic, baseName, name, "_get_");

                if (!((IdlAttribute)obj).readOnly())
                {
                    writeOperationHelperClass(output, dynamic, baseName, name, "_set_");
                }
                break;
        }

    }
    private void writeOperationHelperClass(final java.io.PrintWriter output,
            final boolean dynamic, final String baseName, final String name,
            final String operationPrefix)
    {
        output.println(tab1 + "private static final class " + "Operation_" + operationPrefix + name + " extends AbstractOperation");
        output.println(tab1 + "{");

        if (dynamic) {
            output.println(tab2 + "protected void invoke(");
            output.println(tab4 + "final " + baseName + " target,");
            output.println(tab4 + "final org.omg.CORBA.ServerRequest request) {");
            output.println(tab3 + "_invoke_" + operationPrefix + name + "(request);");
        }
        else
        {
            output.println(tab2 + "protected org.omg.CORBA.portable.OutputStream invoke(");
            output.println(tab4 + "final " + baseName + " target,");
            output.println(tab4 + "final org.omg.CORBA.portable.InputStream _is,");
            output.println(tab4 + "final org.omg.CORBA.portable.ResponseHandler handler) {");
            output.println(tab3 + "return target._invoke_" + operationPrefix + name + "(_is, handler);");
        }
        output.println(tab2 + "}");
        output.println(tab1 + "}");
        output.println();
    }

    /**
     * Translate a TIE approach for an interface
     *
     * @param obj the interface to translate
     * @param writeInto the directory where the interface must be defined
     */
    public void translate_interface_tie(IdlObject obj, java.io.File writeInto)
    {
        // Deprecated
        // java.io.PrintStream output = ...
        java.io.PrintWriter output = null;

        if (m_cp.getM_map_poa() == false)
            output = newFile(writeInto, obj.name() + "Tie");
        else
            output = newFile(writeInto, obj.name() + "POATie");

        addDescriptiveHeader(output, obj);

        // Creating package
        //java.io.File intoMe = getDirectory(obj.name()+"Package",writeInto);

        // Interface header
        if (m_cp.getM_map_poa() == false)
            output.println("public class " + obj.name() + "Tie extends _" + obj.name() + "ImplBase");
        else
            output.println("public class " + obj.name() + "POATie extends " + obj.name() + "POA");

        //output.println(tab2 + "implements "+obj.name());

        output.println("{");

        output.println("");


        // Private member for delegation
        output.println(tab + "//");

        output.println(tab + "// Private reference to implementation object");

        output.println(tab + "//");

        output.println(tab + "private " + obj.name() + "Operations _tie;");

        output.println("");

        if (m_cp.getM_map_poa() == true)
        {
            // Private member for POA
            output.println(tab + "//");
            output.println(tab + "// Private reference to POA");
            output.println(tab + "//");
            output.println(tab + "private org.omg.PortableServer.POA _poa;");
            output.println("");
        }

        // Constructor
        output.println(tab + "/**");

        output.println(tab + " * Constructor");

        output.println(tab + " */");

        if (m_cp.getM_map_poa() == false)
            output.println(tab + "public " + obj.name() + "Tie(" + obj.name() + "Operations tieObject)");
        else
            output.println(tab + "public " + obj.name() + "POATie(" + obj.name() + "Operations tieObject)");

        output.println(tab + "{");

        output.println(tab2 + "_tie = tieObject;");

        output.println(tab + "}");

        output.println("");

        if (m_cp.getM_map_poa() == true)
        {
            output.println(tab + "/**");
            output.println(tab + " * Constructor");
            output.println(tab + " */");
            output.println(tab + "public " + obj.name() + "POATie(" + obj.name() + "Operations tieObject, org.omg.PortableServer.POA poa)");
            output.println(tab + "{");
            output.println(tab2 + "_tie = tieObject;");
            output.println(tab2 + "_poa = poa;");
            output.println(tab + "}");
            output.println("");
        }

        output.println(tab + "/**");
        output.println(tab + " * Get the delegate");
        output.println(tab + " */");
        output.println(tab + "public " + obj.name() + "Operations _delegate()");
        output.println(tab + "{");
        output.println(tab2 + "return _tie;");
        output.println(tab + "}");
        output.println("");

        output.println(tab + "/**");
        output.println(tab + " * Set the delegate");
        output.println(tab + " */");
        output.println(tab + "public void _delegate(" + obj.name() + "Operations delegate_)");
        output.println(tab + "{");
        output.println(tab2 + "_tie = delegate_;");
        output.println(tab + "}");
        output.println("");

        if (m_cp.getM_map_poa() == true)
        {
            output.println(tab + "/**");
            output.println(tab + " * _default_POA method");
            output.println(tab + " */");
            output.println(tab + "public org.omg.PortableServer.POA _default_POA()");
            output.println(tab + "{");
            output.println(tab2 + "if (_poa != null)");
            output.println(tab2 + "    return _poa;");
            output.println(tab2 + "else");
            output.println(tab2 + "    return super._default_POA();");
            output.println(tab + "}");
            output.println("");
        }

        List intoList = getInheritanceOpList(obj, new ArrayList());

        for (int i = 0; i < intoList.size(); i++)
        {
            switch (((IdlObject) intoList.get(i)).kind())
            {

            case IdlType.e_operation :
                translate_operation_tie(((IdlObject) intoList.get(i)), output);
                break;

            case IdlType.e_attribute :
                translate_attribute_tie(((IdlObject) intoList.get(i)), output);
                break;
            }
        }

        output.println("}");
        output.close();
    }

    /**
     * Translate a module
     *
     * @param obj the module to translate
     * @param writeInto the directory where the module must be defined
     */
    public void translate_module(IdlObject obj, java.io.File writeInto, int translateType)
    {
        final String name = obj.name();

        java.io.File intoModule;
        if (translateType == 0)
        {
            intoModule = createDirectory(name, writeInto);
        }
        else
        {
            intoModule = getDirectory(name, writeInto);
        }

        String old_pkg = current_pkg;

        addToPkg(obj, name);

        translate_object(obj, intoModule, translateType);

        current_pkg = old_pkg;
    }

    /**
     * Translate a user module
     *
     * @param obj the module to translate
     * @param writeInto the directory where the module must be defined
     */
    public void translate_user_module(IdlObject obj, java.io.File writeInto)
    {
        String old_pkg;

        java.io.File intoModule;

        if (obj.getPrefix() != null)
        {
            writeInto = getPrefixDirectories(obj.getPrefix(), writeInto);
        }

        intoModule = getDirectory(obj.name(), writeInto);

        old_pkg = current_pkg;

        if (obj.getPrefix() != null)
        {
            if (m_cp.getM_reversePrefix())
                addToPkg(obj, inversedPrefix(obj.getPrefix()) + "." + obj.name());
            else
                addToPkg(obj, obj.getPrefix() + "." + obj.name());
        }
        else
            addToPkg(obj, obj.name());

        translate_user_object(obj, intoModule);

        current_pkg = old_pkg;
    }


    /**
     * Translate a value type state
     */
    public void translate_state_member(IdlObject obj, java.io.PrintWriter output)
    {
        IdlStateMember member = (IdlStateMember) obj;

        if (obj.hasComment())
            javadoc(output, obj);
        else
        {
            output.println(tab + "/**");

            if (member.public_member())
                output.println(tab + " *  Public member : " + obj.name());
            else
                output.println(tab + " * Private member : " + obj.name());

            output.println(tab + " */");
        }

        if (member.public_member())
            output.print(tab + " public ");
        else
            output.print(tab + " protected ");

        obj.reset();

        translate_type(obj.current(), output);

        output.println(" " + obj.name() + ";");

        output.println("");
    }

    /**
     * Return the list of the inherited members
     *
     */
    private List getInheritedStateMember(IdlObject obj)
    {
        List list = new ArrayList();
        List sub = null;

        IdlValue [] inherited = ((IdlValue) obj).getInheritance();

        if (inherited.length != 0)
        {
            sub = getInheritedStateMember(inherited[ 0 ]);
        }

        if (sub != null)
        {
            for (int i = 0; i < sub.size(); i++)
                list.add(sub.get(i));
        }

        obj.reset();

        while (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_state_member)
                list.add(obj.current());

            obj.next();
        }

        return list;
    }

    /**
     * Translate a Value Type
     *
     * @param obj la value type to translate
     * @param writeInto the directory where the module must be defined
     */
    public void translate_value_type(IdlObject obj, java.io.File writeInto)
    {
        IdlValue value = (IdlValue) obj;
        boolean base_custom = false;
        String old_pkg;

        java.io.PrintWriter output = newFile(writeInto, obj.name());

        addDescriptiveHeader(output, obj);

        if (value.abstract_value())
        {
            output.print("public interface " + obj.name() + " extends org.omg.CORBA.portable.ValueBase");

            IdlValue [] abs_inheritance = value.getInheritance();

            if (abs_inheritance.length != 0)
                output.print(", ");

            for (int i = 0; i < abs_inheritance.length; i++)
            {
                output.print(fullname(abs_inheritance[ i ]));

                if (i + 1 < abs_inheritance.length)
                    output.print(", ");
            }

            output.println();

            if (value.supports().size() != 0)
            {
                output.print(tab2 + ", ");

                List list = value.supports();

                for (int i = 0; i < list.size(); i++)
                {
                    IdlInterface itf = (IdlInterface) list.get(i);

                    output.print(fullname(itf));

                    if (!itf.abstract_interface())
                        output.print("Operations");

                    if (i + 1 < list.size())
                        output.print(", ");
                }
            }

            output.println("{");

            // Functions from IDL description

            java.io.File intoMe = null;

            if (isEmptyValue(obj) == false)
                intoMe = createDirectory(obj.name() + "Package", writeInto);
            else
                intoMe = writeInto;

            // Traduit les definitions internes
            old_pkg = current_pkg;

            addToPkg(obj, obj.name() + "Package");

            obj.reset();

            while (obj.end() != true)
            {
                switch (obj.current().kind())
                {

                case IdlType.e_enum :
                    translate_enum(obj.current(), intoMe);
                    break;

                case IdlType.e_struct :
                    translate_struct(obj.current(), intoMe);
                    break;

                case IdlType.e_union :
                    translate_union(obj.current(), intoMe);
                    break;

                case IdlType.e_typedef :
                    translate_typedef(obj.current(), intoMe);
                    break;

                case IdlType.e_exception :
                    translate_exception(obj.current(), intoMe);
                    break;

                case IdlType.e_native :
                    translate_native(obj.current(), intoMe);
                    break;

                case IdlType.e_const :
                    translate_constant(obj.current(), null, output);
                    break;

                case IdlType.e_operation :
                    translate_operation(obj.current(), output);
                    break;

                case IdlType.e_attribute :
                    translate_attribute(obj.current(), output);
                    break;
                }

                obj.next();
            }

            current_pkg = old_pkg;

            output.println("}");
            output.println("");
            output.close();
        }
        else
        {
            // -----------------------------------------
            // The class implementing the concrete value
            // -----------------------------------------

            output.print("public abstract class " + obj.name());

            // Case : Values types that do not inherit from other values or interfaces
            if ((value.supports().size() == 0) && (value.getInheritance().length == 0))
            {
                if (value.custom_value())
                {
                    output.println(" implements org.omg.CORBA.portable.CustomValue");
                }
                else
                {
                    output.println(" implements org.omg.CORBA.portable.StreamableValue");
                }
            }
            else
            {

                IdlValue [] inheritance = value.getInheritance();

                boolean extends_value = false;
                boolean streamNeed = false;

                if (inheritance.length == 0)
                    streamNeed = true;

                if (inheritance.length != 0)
                    extends_value = true;

                for (int i = 0; i < inheritance.length; i++)
                {
                    if (inheritance[ i ].custom_value())
                        base_custom = true;
                }

                if ((extends_value == false) && (value.supports().size() == 0))
                {
                    output.print(" implements org.omg.CORBA.portable.StreamableValue");
                }

                boolean extend_concrete = false;
                boolean extend_abstract = false;

                for (int i = 0; i < inheritance.length; i++)
                {
                    if (! inheritance[ i ].abstract_value())
                        extend_concrete = true;
                    else
                        extend_abstract = true;
                }

                // case : Inheritance from other stateful values
                if (extends_value)
                {
                    if (extend_concrete)
                    {
                        output.print(" extends ");
                        int j = 0;

                        for (int i = 0; i < inheritance.length; i++)
                        {
                            if (inheritance[ i ].abstract_value() == false)
                            {
                                j++;

                                if (j > 1)
                                    output.print(", ");

                                output.print(fullname(inheritance[ i ]));
                            }
                        }
                    }

                    if (extend_abstract)
                    {
                        output.print(" implements ");
                        int j = 0;

                        for (int i = 0; i < inheritance.length; i++)
                        {
                            if (inheritance[ i ].abstract_value())
                            {
                                j++;

                                if (j > 1)
                                    output.print(", ");

                                output.print(fullname(inheritance[ i ]));
                            }
                        }

                        if (!extend_concrete)
                            output.print(", org.omg.CORBA.portable.StreamableValue");
                    }

                }

                if (!(extend_abstract && (value.supports().size() != 0)))
                    output.println("");

                // case : Inheritance from abstract values
                boolean implement_map = false;

                if (value.custom_value() && !base_custom)
                {
                    output.print(tab2 + "implements org.omg.CORBA.portable.CustomValue");
                    implement_map = true;
                }

                // case : Supported interfaces
                if (value.supports().size() != 0)
                {
                    if (implement_map)
                        output.print(", ");
                    else
                    {
                        if (!extend_abstract)
                            output.print(tab2 + "implements ");
                        else
                            output.print(", ");

                        if (streamNeed)
                            output.print("org.omg.CORBA.portable.StreamableValue, ");
                    }

                    List list = value.supports();

                    for (int i = 0; i < list.size(); i++)
                    {
                        IdlInterface itf = (IdlInterface) list.get(i);

                        output.print(fullname(itf));

                        if (!itf.abstract_interface())
                            output.print("Operations");

                        if (i + 1 < list.size())
                            output.print(", ");
                    }
                }

                output.println("");
            }

            output.println("{");

            // Functions from IDL description

            java.io.File intoMe = null;

            if (isEmptyValue(obj) == false)
                intoMe = createDirectory(obj.name() + "Package", writeInto);
            else
                intoMe = writeInto;

            // Translate the internal definitions
            old_pkg = current_pkg;

            addToPkg(obj, obj.name() + "Package");

            obj.reset();

            while (obj.end() != true)
            {
                switch (obj.current().kind())
                {

                case IdlType.e_enum :
                    translate_enum(obj.current(), intoMe);
                    break;

                case IdlType.e_struct :
                    translate_struct(obj.current(), intoMe);
                    break;

                case IdlType.e_union :
                    translate_union(obj.current(), intoMe);
                    break;

                case IdlType.e_typedef :
                    translate_typedef(obj.current(), intoMe);
                    break;

                case IdlType.e_exception :
                    translate_exception(obj.current(), intoMe);
                    break;

                case IdlType.e_native :
                    translate_native(obj.current(), intoMe);
                    break;

                case IdlType.e_const :
                    translate_constant(obj.current(), null, output);
                    break;

                case IdlType.e_operation :
                    translate_operation(obj.current(), output);
                    break;

                case IdlType.e_attribute :
                    translate_attribute(obj.current(), output);
                    break;

                case IdlType.e_state_member :
                    translate_state_member(obj.current(), output);
                    break;
                }

                obj.next();
            }

            current_pkg = old_pkg;

            // Functions from ValueBase

            output.println(tab + "/**");
            output.println(tab + " * Return the truncatable ids");
            output.println(tab + " */");

            output.println(tab + "static final String[] _ids_list =");
            output.println(tab + "{");

            String [] list = value.truncatableList();
            int max = list.length;

            if (!value.isTruncatable())
                max = 1;

            for (int i = 0; i < max; i++)
            {
                output.print(tab2 + "\"" + list[ i ] + "\"");

                if (i + 1 < list.length)
                    output.print(",");

                output.println("");
            }

            output.println(tab + "};");
            output.println("");

            output.println(tab + "public String [] _truncatable_ids()");
            output.println(tab + "{");
            output.println(tab2 + "return _ids_list;");
            output.println(tab + "}");
            output.println("");

            // Functions from Streamable
            if (!value.custom_value() && !base_custom)
            {
                output.println(tab + "/**");
                output.println(tab + " * Unmarshal the value into an InputStream");
                output.println(tab + " */");
                output.println(tab + "public void _read(org.omg.CORBA.portable.InputStream is)");
                output.println(tab + "{");

                List stateList = getInheritedStateMember(obj);

                for (int i = 0; i < stateList.size(); i++)
                {
                    IdlStateMember state = (IdlStateMember) stateList.get(i);
                    state.reset();
                    translate_unmarshalling_member(state.current(), output, "is", state.name(), tab2 + "");
                }

                output.println(tab + "}");
                output.println("");

                output.println(tab + "/**");
                output.println(tab + " * Marshal the value into an OutputStream");
                output.println(tab + " */");
                output.println(tab + " public void _write(org.omg.CORBA.portable.OutputStream os)");
                output.println(tab + " {");

                for (int i = 0; i < stateList.size(); i++)
                {
                    IdlStateMember state = (IdlStateMember) stateList.get(i);
                    state.reset();
                    translate_marshalling_member(state.current(), output, "os", state.name(), tab2 + "");
                }

                output.println(tab + "}");
                output.println("");

                output.println(tab + "/**");
                output.println(tab + " * Return the value TypeCode");
                output.println(tab + " */");
                output.println(tab + " public org.omg.CORBA.TypeCode _type()");
                output.println(tab + " {");
                output.println(tab2 + "return " + fullname(obj) + "Helper.type();");
                output.println(tab + " }");
                output.println("");
            }

            output.println("}");
            output.println("");
            output.close();

            // ----------------------------------
            // The class implementing the factory
            // ----------------------------------
            boolean factory = false;
            obj.reset();

            while (obj.end() != true)
            {
                if (obj.current().kind() == IdlType.e_factory)
                {
                    factory = true;
                    break;
                }

                obj.next();
            }

            if (factory)
            {
                output = newFile(writeInto, obj.name() + "ValueFactory");

                addDescriptiveHeader(output, obj.current());

                output.println("public interface " + obj.name() + "ValueFactory extends org.omg.CORBA.portable.ValueFactory");
                output.println("{");

                obj.reset();

                while (obj.end() != true)
                {
                    if (obj.current().kind() == IdlType.e_factory)
                    {
                        output.println(tab + "/**");
                        output.println(tab + " * Return the value type");
                        output.println(tab + " */");
                        output.print(tab + " public abstract " + fullname(obj) + " " + obj.current().name() + "(");

                        obj.current().reset();

                        while (obj.current().end() != true)
                        {
                            IdlFactoryMember member = (IdlFactoryMember) obj.current().current();

                            member.reset();
                            translate_type(member.current(), output);
                            output.print(" " + member.name());

                            obj.current().next();

                            if (obj.current().end() != true)
                                output.print(", ");
                        }

                        output.println(");");
                        output.println("");
                    }

                    obj.next();
                }

                output.println("}");
                output.println("");
                output.close();
            }
        }

        write_holder(obj, writeInto);
        write_helper(obj, writeInto);

        writeDefaultValueFactory((IdlValue)obj, writeInto);
        writeDefaultValueImpl((IdlValue)obj, writeInto);
    }


    public void writeDefaultValueFactory(final IdlValue obj, final File writeInto)
    {
        if( obj.isAbstract() || obj.isCustom() ||
                ( null == m_cp.getM_generateValueFactory()) )
        {
            return;
        }

        final String className = obj.name() + "DefaultFactory";
        final String implName = obj.name() + m_cp.getM_generateValueFactory();
        final PrintWriter output = newFile(writeInto, className);

        addPackageName(output);
        output.println("// " + fullname(obj));

        output.println("public class " + className + " implements org.omg.CORBA.portable.ValueFactory {");

        output.println(tab1 + "public " + className + "() {}");
        output.println();

        output.println(tab1 + "public java.io.Serializable read_value(");
        output.println(tab3 + "final org.omg.CORBA_2_3.portable.InputStream is) {");
        output.println(tab2 + "return is.read_value(new " + implName + "());");
        output.println(tab1 + "}");
        output.println();

        output.println(tab1 + "}");
        output.println();
        output.flush();
        output.close();
    }

    public void writeDefaultValueImpl(final IdlValue obj, final File writeInto)
    {
        if( obj.isAbstract() || ( null == m_cp.getM_generateValueImpl() ) )
        {
            return;
        }
        final String className = obj.name() + m_cp.getM_generateValueImpl();
        final PrintWriter output = newFile(writeInto, className);

        addPackageName(output);

        output.println("public class " + className + " extends " + obj.name() + " {");

        output.println(tab1 + "public " + className + "() {}");
        output.println();

        writeDefaultMethods(output, obj, new HashSet());

        output.println("}");
        output.println();
        output.flush();
        output.close();
    }

    public void writeDefaultMethods(final PrintWriter output, final IdlObject obj, final Set ids)
    {
        if (!ids.add(obj.getId())) {
            return;
        }

        for (obj.reset(); !obj.end(); obj.next())
        {
            switch (obj.current().kind())
            {
                case IdlType.e_operation :
                    writeDefaultOperation(output, (IdlOp)obj.current());
                    break;

                case IdlType.e_attribute :
                    writeDefaultAttribute(output, (IdlAttribute)obj.current());
                    break;
            }
        }

        switch (obj.kind())
        {
            case IdlType.e_interface:
                writeDefaultMethodsForInterfaces(output, ((IdlInterface)obj).getInheritance() ,ids);
                break;

            case IdlType.e_value:
                writeDefaultMethodsForValueTypes(output, ((IdlValue)obj).getInheritanceList(),ids);
                writeDefaultMethodsForInterfaces(output, ((IdlValue)obj).supports(),ids);
                break;
        }
    }

    public void writeDefaultMethodsForValueTypes(final PrintWriter output, final List list, final Set ids)
    {
        for (int i = 0; i < list.size(); i++)
        {
            writeDefaultMethods(output, ((IdlValueInheritance)list.get(i)).getValue(), ids);
        }
    }

    public void writeDefaultMethodsForInterfaces(final PrintWriter output, final List list, final Set ids)
    {
        for (int i = 0; i < list.size(); i++)
        {
            writeDefaultMethods(output, (IdlInterface)list.get(i), ids);
        }
    }

    public void writeDefaultAttribute(final PrintWriter output, final IdlAttribute obj)
    {
        obj.reset();
        final IdlObject attributeType = obj.current();

        output.print(tab + "public ");
        translate_type(attributeType, output);
        output.println(" " + obj.name() + "() {");

        output.println(tab2 + "throw new Error(\"Unimplemented operation\");");

        output.println(tab1 + "}");
        output.println();

        if (!obj.readOnly())
        {
            output.print(tab + "public ");
            output.print("void " + obj.name() + "(");
            translate_type(attributeType, output);
            output.println(") {");
            output.println(tab2 + "throw new Error(\"Unimplemented operation\");");
            output.println(tab1 + "}");
            output.println();
        }
    }

    public void writeDefaultOperation(final PrintWriter output, final IdlOp obj)
    {
        IdlRaises r;
        IdlContext c;
        boolean someParams = false;

        output.print(tab + "public ");

        obj.reset();

        final IdlObject returnType = obj.current();

        translate_type(returnType, output);

        output.print(" " + obj.name() + "(");

        obj.next();

        if (!obj.end())
        {
            if (obj.current().kind() == IdlType.e_param)
            {
                someParams = true;

                while (!obj.end())
                {

                    obj.current().reset();
                    translate_parameter(obj.current().current(), output, ((IdlParam) obj.current()).param_attr());

                    output.print(" " + obj.current().name());

                    obj.next();

                    if (obj.end() != true)
                    {
                        if (obj.current().kind() == IdlType.e_param)
                            output.print(", ");
                        else
                            break;
                    }
                }
            }
        }

        c = getContext(obj);

        if (c != null)
        {
            if (someParams == true)
                output.print(", ");

            output.print("org.omg.CORBA.Context ctx");
        }

        output.print(")");

        if (obj.end() != true)
        {
            if (obj.current().kind() == IdlType.e_raises)
            {
                output.println("");
                output.print(tab2 + "throws ");
                r = (IdlRaises) obj.current();

                r.reset();

                while (!r.end())
                {

                    output.print(fullname(r.current()));

                    r.next();

                    if (!r.end())

                        output.print(", ");

                }
            }
        }

        output.println("{");
        output.println(tab2 + "throw new Error(\"Unimplemented operation\");");
        output.println(tab1 + "}");
        output.println();
    }


    /**
     * Translate a Value Box
     *
     * @param obj the value box to translate
     * @param writeInto the directory where the module must be defined
     */
    public void translate_value_box(IdlObject obj, java.io.File writeInto)
    {
        IdlValueBox value = (IdlValueBox) obj;

        if (value.simple())
        {
            java.io.PrintWriter output = newFile(writeInto, obj.name());

            addDescriptiveHeader(output, obj);

            output.println("public class " + obj.name() + " implements org.omg.CORBA.portable.ValueBase");
            output.println("{");

            output.println(tab + "/**");
            output.println(tab + " * Reference to the boxed value");
            output.println(tab + " */");
            output.print(tab + "public ");

            obj.reset();
            translate_type(obj.current(), output);
            output.println(" value;");
            output.println("");

            output.println(tab + "/**");
            output.println(tab + " * Constructor");
            output.println(tab + " * ");
            output.println(tab + " * @param initial the initial boxed value");
            output.println(tab + " */");
            output.print(tab + "public " + obj.name() + "(");
            translate_type(obj.current(), output);
            output.println(" initial)");
            output.println(tab + "{");
            output.println(tab2 + "value = initial;");
            output.println(tab + "}");
            output.println("");

            output.println(tab + "//");
            output.println(tab + "// Return value box id");
            output.println(tab + "//");
            output.println(tab + "private static String[] _ids = { " + obj.name() + "Helper.id() };");
            output.println("");

            output.println(tab + "/**");
            output.println(tab + " * Return truncatable ids");
            output.println(tab + " */");
            output.println(tab + " public String[] _truncatable_ids()");
            output.println(tab + " {");
            output.println(tab2 + "return _ids;");
            output.println(tab + " }");
            output.println("");

            output.println("}");

            output.close();

            write_holder(obj, writeInto);
            write_helper(obj, writeInto);
        }
        else
        {
            // Definition of the sub-types
            obj.reset();

            while (obj.end() != true)
            {
                switch (obj.current().kind())
                {

                case IdlType.e_union :
                    translate_union(obj.current(), writeInto);
                    break;

                case IdlType.e_struct :
                    translate_struct(obj.current(), writeInto);
                    break;

                case IdlType.e_enum :
                    translate_enum(obj.current(), writeInto);
                    break;
                }

                obj.next();
            }

            obj.reset();

            switch (obj.current().kind())
            {

            case IdlType.e_string :

            case IdlType.e_wstring :

            case IdlType.e_simple :

            case IdlType.e_fixed :

            case IdlType.e_union :

            case IdlType.e_struct :

            case IdlType.e_enum :

            case IdlType.e_sequence :

            case IdlType.e_array :

            case IdlType.e_typedef :

            case IdlType.e_ident :
                write_helper(obj, writeInto);
                write_holder(obj, writeInto);
                break;
            }
        }

    }

    /**
     * Translate a native type
     *
     * @param obj the module to translate
     * @param writeInto the directory where the module must be defined
     */
    public void translate_native(IdlObject obj, java.io.File writeInto)
    {
        write_holder(obj, writeInto);
        write_helper(obj, writeInto);
    }

    /**
     * Translate an object content
     */
    public void translate_object_content(IdlObject obj, java.io.File writeInto, int translateType)
    {
        final IdlObject current = obj.current();
        switch (current.kind())
        {

        case IdlType.e_module :
            translate_module(current, writeInto, translateType);
            break;

        case IdlType.e_const :

            if (translateType == 0)
                translate_constant(current, writeInto, null);

            break;

        case IdlType.e_enum :
            if (translateType == 0)
                translate_enum(current, writeInto);

            break;

        case IdlType.e_struct :
            if (translateType == 0)
                translate_struct(current, writeInto);

            break;

        case IdlType.e_union :
            if (translateType == 0)
                translate_union(current, writeInto);

            break;

        case IdlType.e_typedef :
            if (translateType == 0)
                translate_typedef(current, writeInto);

            break;

        case IdlType.e_exception :
            if (translateType == 0)
                translate_exception(current, writeInto);

            break;

        case IdlType.e_native :
            if (translateType == 0)
                translate_native(current, writeInto);

            break;

        case IdlType.e_value_box :
            if (translateType == 0)
                translate_value_box(current, writeInto);

            break;

        case IdlType.e_value :
            if (translateType == 0)
                translate_value_type(current, writeInto);

            break;

        case IdlType.e_interface :
            if (translateType == 0)
                translate_interface(current, writeInto);
            else
                if (translateType == 1)
                {
                    if (((IdlInterface) current).local_interface() == false)
                        translate_interface_stub(current, writeInto);
                }
                else
                    if (translateType == 2)
                    {
                        if (((IdlInterface) current).abstract_interface() == false)
                        {
                            if (((IdlInterface) current).local_interface() == false)
                                translate_interface_skel(current, writeInto);
                        }
                    }
                    else
                    {
                        if (translateType == 3)
                        {
                            if (((IdlInterface) current).abstract_interface() == false)
                            {
                                if (((IdlInterface) current).local_interface() == false)
                                    translate_interface_tie(current, writeInto);
                            }
                        }
                    }

            break;
        }

    }

    /**
     * Translate the data from a container object (Module, Interface, Root)
     *
     * @param obj the object to translate
     * @param writeInto the write access
     * @param translateType the translation type (0=data, 1=stub, 2=skeleton)
     */
    public void translate_object(IdlObject obj, java.io.File writeInto, int translateType)
    {
        obj.reset();

        while (obj.end() != true)
        {
            java.io.File tmpInto = writeInto;

            final IdlObject current = obj.current();
            if (current.included() == false)
            {
                String old_pkg = current_pkg;

                if (m_cp.getM_usePrefix())
                {
                    final String prefix = current.getPrefix();
                    if ((prefix != null) && (obj.kind() == IdlType.e_root))
                    {
                        if (translateType == 0)
                        {
                            tmpInto = createPrefixDirectories(prefix, writeInto);
                        }
                        else
                        {
                            tmpInto = getPrefixDirectories(prefix, writeInto);
                        }

                        if (m_cp.getM_reversePrefix())
                            addToPkg(obj, inversedPrefix(prefix));
                        else
                            addToPkg(obj, prefix);
                    }
                }

                translate_object_content(obj, tmpInto, translateType);

                current_pkg = old_pkg;
            }

            obj.next();
        }
    }

    /**
     * Translate the data from a container object (Module, Interface, Root)
     * for the user code
     *
     * @param obj the object to translate
     * @param writeInto the write access
     */
    public void translate_user_object(IdlObject obj, java.io.File writeInto)
    {
        obj.reset();

        while (obj.end() != true)
        {
            if (obj.included() == false)
                switch (obj.current().kind())
                {

                case IdlType.e_module :
                    translate_user_module(obj.current(), writeInto);
                    break;

                case IdlType.e_interface :
                    translate_user_interface(obj.current(), writeInto);
                    break;
                }

            obj.next();
        }
    }

    /**
     * Translate the data from IDL to Java
     *
     * @param obj The compilation graph root
     * @param packageName The directory where the definitions are added
     */
    public void translateData(IdlObject obj, String packageName)
    {
        _root = obj;

        java.io.File writeInto = createDirectory(packageName, m_cp.getM_destdir());

        translate_object(obj, writeInto, 0);
    }

    /**
     * Create the class for the delegation
     *
     * @param obj The compilation graph root
     * @param packageName The directory where the definitions are added
     */
    public void translateTIE(IdlObject obj, String packageName)
    {
        _root = obj;

        if (m_cp.getM_pidl())
            return;

        java.io.File writeInto = getDirectory(packageName, m_cp.getM_destdir());

        translate_object(obj, writeInto, 3);
    }

    /**
     * Generate the user code (implementation class)
     *
     * @param obj The compilation graph root
     * @param packageName The directory where the definitions are added
     */
    public void translateUser(IdlObject obj, String packageName)
    {
        _root = obj;

        java.io.File writeInto = getDirectory(packageName, m_cp.getM_destdir());

        initial = writeInto;

        translate_user_object(obj, writeInto);
    }

    /**
     * Creates the stub
     *
     * @param obj The compilation graph root
     * @param packageName The directory where the definitions are added
     */
    public void translateStub(IdlObject obj, String packageName)
    {
        _root = obj;

        java.io.File writeInto = getDirectory(packageName, m_cp.getM_destdir());

        translate_object(obj, writeInto, 1);
    }

    /**
     * Creates the skeleton
     *
     * @param obj The compilation graph root
     * @param packageName The directory where the definitions are added
     */
    public void translateSkeleton(IdlObject obj, String packageName)
    {
        _root = obj;

        java.io.File writeInto = getDirectory(packageName, m_cp.getM_destdir());

        translate_object(obj, writeInto, 2);
    }



}
