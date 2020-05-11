/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.object;

/**
 * An IDL comment section is a comment extension field as for example
 * 'exception' or 'author'
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public class IdlCommentSection implements java.io.Serializable
{
    /**
     * Section type
     */
    private IdlCommentField _kind;

    /**
     * Section title ( for unknown section )
     */
    private String _title;

    /**
     * Section comment
     */
    private String _comment;

    /**
     * Constructor
     */
    public IdlCommentSection( IdlCommentField SectionKind )
    {
        _kind = SectionKind;
    }

    /**
     * Add the section comment
     */
    public void add_description( String description )
    {
        _comment = description;
    }

    /**
     * Return the section type
     */
    public IdlCommentField kind()
    {
        return _kind;
    }

    /**
     * Return the section description
     */
    public String get_description()
    {
        return _comment;
    }

    /**
     * Return the section title
     */
    public String get_title()
    {
        return _title;
    }

    /**
     * Set the section title
     */
    public void set_title( String title )
    {
        _title = title;
    }
}
