/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler;

import java.io.File;

/**
 * @author Erik Putrycz
 *
 * Contains an entry for the compiler framework
 * Entry = 1 src path + 1 file
 */
public class CompileListEntry
{
    private File m_src_path;
    private String m_file_name;

    /**
     * Constructor for CompileListEntry.
     */
    public CompileListEntry( String filename )
    {
        m_file_name = filename;
        m_src_path = null;
    }

    /**
     * Constructor for CompileListEntry.
     */
    public CompileListEntry( File srcdir, String filename )
    {
        m_file_name = filename;
        m_src_path = srcdir;
    }


    /**
     * Returns the m_src_path.
     * @return File
     */
    public File getSrcPath()
    {
        return m_src_path;
    }

    /**
     * Returns the m_file_name.
     * @return String
     */
    public String getFileName()
    {
        return m_file_name;
    }

    public boolean equals( Object o )
    {
        if ( o instanceof CompileListEntry )
        {
            CompileListEntry new_elem = ( CompileListEntry ) o;
            return ( new_elem.m_file_name.equals( this.m_file_name )
             && new_elem.m_src_path.equals( this.m_src_path ) );
        }
        return false;

    }
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return m_src_path.hashCode() + m_file_name.hashCode();
    }

}
