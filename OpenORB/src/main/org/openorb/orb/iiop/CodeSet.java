/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

/**
 * This class represents an entry in the codeset database.
 * The primary key ist the codeset id. Attached to this key
 * are information like name, canonical name, description, array of,
 * charset ids number of bytes, maximum number of bytes, and a flag
 * whether this is a server side or a client side entry.
 *
 * @author <a href="michael@rumpfonline.de">Michael Rumpf</a>
 */

public final class CodeSet
    extends java.lang.Object
{
    private final int      m_id;
    private final String   m_canonical;
    private final String   m_name;
    private final String   m_description;
    private final short [] m_charsets;
    private final int      m_maxSize;
    private final int      m_alignment;
    private final boolean  m_server;

    /**
     * Constructor.
     * This constructor populates all the fields of this class.
     */
    public CodeSet( String description, int id, short [] charsets,
                    int maxSize, String canonical, String name, int alignment, boolean server )
    {
        m_description = description;
        m_id          = id;
        m_charsets    = charsets;
        m_maxSize     = maxSize;
        m_canonical   = canonical;
        m_name        = name;
        m_alignment   = alignment;
        m_server      = server;
    }

    /**
     * Return the id of this codeset.
     * The id is an unsigned 32bit integer from the OSF charset and codeset registry.
     */
    public int getId()
    {
        return m_id;
    }

    /**
     * Returns the canonical Java name for the codeset.
     */
    public String getCanonicalName()
    {
        return m_canonical;
    }

    /**
     * Return the name of this codeset.
     * This is not the canonical Java name of the codeset. This name has been
     * entered during the first/last run of the CodeSetDatabase.main() method
     * and has not been changed since then. This name is canonicalized before
     * it is used in the Java environment. The reason why this name isn't
     * changed to the canonical name per default is that there are several
     * codesets mapped on to the same Java name. For example the several
     * UCS Level 1-3 and the UTF-16BE codesets. In Java all of them are mapped
     * to UnicodeBigUnmarked. When object references are printed to the command
     * line we want the names as defined in the OSF codeset registry and not
     * the Java canonical name, because it will be no longer comparable to
     * object references from e.g. C/C++ environments where the Java canonical
     * name isn't known.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the description of this codeset.
     * The description from the OSF charset and codeset registry.
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Return the array of charsets covered by this codeset.
     * A charset registry entry is represented by an unsigned 16bit short value
     * as defined in the OSF charset and codeset registry.
     */
    public short[] getCharsets()
    {
        return m_charsets;
    }

    /**
     * Return the maximum number of bytes used by this codeset.
     * There are two main types of codesets: byte-oriented and non-byte-oriented.
     * The byte-oriented have another two sub-categories: single-byte and multi-byte.
     * This makes three different types regarding to the length in bytes:
     * single-byte, variable-length multi-byte, and fixed-length multi-byte.
     */
    public int getMaxSize()
    {
        return m_maxSize;
    }

    /**
     * Return the alignment for this codeset.
     * This value has been supplied by the first run of the CodeSetDatabaseInitializer.main()
     * method and is reused each time the CodeSetDatabase class is regenerated.
     * The reason why the CodeSetDatabase.getAlignmentFromId returns another value
     * than this method is currently unknown.
     *
     * @return The aligment for the specified codeset
     */
    public int getAlignment()
    {
        return m_alignment;
    }

    /**
     * Return whether the codeset is to be used in server profiles or not.
     * This flag can be specified in the interactive mode when creating the
     * CodeSetDatabase file. The reason why some codesets are marked for
     * client use only is currently unknown.
     */
    public boolean forServer()
    {
        return m_server;
    }

    /**
     * This method is used to create a stringified representation of
     * an entry.
     */
    public String toString()
    {
        String ret = "\"" + m_description + "\", 0x"
              + org.openorb.util.HexPrintStream.toHex( m_id ) + ", new short [] { ";

        for ( int i = 0; i < m_charsets.length; i++ )
        {
            ret += "0x" + org.openorb.util.HexPrintStream.toHex( m_charsets[ i ] );

            if ( i < m_charsets.length - 1 )
            {
                ret += ", ";
            }
        }

        ret += " } ," + m_maxSize + ", \"" + m_canonical + "\", \""
              + m_name + "\", " + m_alignment + ", " + m_server;
        return ret;
    }

    /**
     * Compare this codeset to the specified one.
     *
     * @param to_comp The codest to compare this one to.
     * @return True if the codesets are compatible, false otherwise.
     */
    public boolean isCompatibleTo( CodeSet to_comp )
    {
        return CodeSet.compatible( this, to_comp );
    }

    /**
     * Returns true if two codesets are compatible.
     *
     * @param csA codeset id for codeset A.
     * @param csB codeset id for codeset B.
     * @return True when codeset B is compatible to codeset A. False otherwise.
     */
    public static boolean compatible( int csA, int csB )
    {
        CodeSet entryA = CodeSetDatabase.getCodeSetFromId( csA );
        CodeSet entryB = CodeSetDatabase.getCodeSetFromId( csB );
        if ( entryA == null || entryB == null )
        {
            return false;
        }
        else
        {
            return compatible( entryA, entryB );
        }
    }

    /**
     * Returns true if two codesets are compatible.
     * For example, when codeset A maps 3 charsets and codeset B only 2
     * then they are not compatible, because codeset A can represent more
     * charsets than codeset B and a comparison of the character sets doesn't
     * matter. When codeset A maps 3 charsets and codeset B
     * 3 or more, then the charsets are compatible when all one charset from
     * codeset A and one charset from codeset B match.
     *
     * CORBA 2.5 chapter 13.10.5.1:
     * Compatibility  is determined with respect to two code sets by examining
     * their entries in the registry, paying special attention to the character
     * sets encoded by each code set. For each of the two code sets, an attempt
     * is made to see if there is at least one (fuzzy-defined) character set in
     * common, and if such a character set is found, then the assumption is made
     * that these code sets are  compatible.  Obviously, applications which
     * exploit parts of a character set not properly encoded in this scheme will
     * suffer information loss when communicating with another application in
     * this  fuzzy  scheme.
     *
     * @param entryA Codeset A.
     * @param entryB Codeset B.
     * @return True when codeset B is compatible to codeset A. False otherwise.
     */
    public static boolean compatible( CodeSet entryA, CodeSet entryB )
    {
        if ( entryA.getCharsets().length >= 1 )
        {
            return entryB.getCharsets().length == 1
                  && entryA.getCharsets()[ 0 ] == entryB.getCharsets()[ 0 ];
        }
        if ( entryB.getCharsets().length == 1 )
        {
            return false;
        }
        boolean found = false;

        for ( int i = 0, j = 0; i < entryA.getCharsets().length
              && j < entryB.getCharsets().length; )
        {
            if ( entryA.getCharsets()[ i ] == entryB.getCharsets()[ j ] )
            {
                if ( found )
                {
                    return true;
                }
                found = true;

                i++;

                j++;
            }
            else
            {
                if ( entryA.getCharsets()[ i ] < entryB.getCharsets()[ j ] )
                {
                    i++;
                }
                else
                {
                    j++;
                }
            }
        }
        return false;
    }
}

