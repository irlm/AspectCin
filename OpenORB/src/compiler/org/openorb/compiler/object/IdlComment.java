/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.object;

/**
 * This class contains all comments for an IDL definition. It manage comment extension as for
 * example @exception and @return
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:38 $
 */

public class IdlComment implements java.io.Serializable
{
    /**
     * Section list
     */
    private IdlCommentSection [] _sections;

    /**
     * Comment
     */
    private String _comment;

    /**
     * Constructor
     */
    public IdlComment()
    {
        _sections = new IdlCommentSection[ 0 ];
    }

    /**
     * Add a new section to the section list
     */
    public void add_section( IdlCommentSection section )
    {
        IdlCommentSection [] tmp = new IdlCommentSection[ _sections.length + 1 ];

        System.arraycopy( _sections, 0, tmp, 0, _sections.length );

        tmp[ _sections.length ] = section;

        _sections = tmp;
    }

    /**
     * Add a comment description
     */
    public void add_description( String description )
    {
        _comment = description;
    }

    /**
     * Return a comment description
     */
    public String get_description()
    {
        return _comment;
    }

    /**
     * Return all comment sections
     */
    public IdlCommentSection [] get_sections()
    {
        return _sections;
    }
}
