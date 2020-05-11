/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.util;

import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * A zip handle contains information to access a zip file
 * ( for input and output ).
 *
 * @author Jerome Daniel
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:28:45 $
 */
public class ZipHandle
{
    /** The input zip file. */
    private ZipFile m_in;

    /** The zip output stream */
    private ZipOutputStream m_out;

    /**
     * Constructor.
     *
     * @param in The input zip file.
     * @param out The output zip stream.
     */
    public ZipHandle( ZipFile in, ZipOutputStream out )
    {
        m_in = in;
        m_out = out;
    }

    /**
     * Return the input zip file.
     *
     * @return The input zip file instance.
     */
    public ZipFile getIn()
    {
        return m_in;
    }

    /**
     * Return the output zip file stream.
     *
     * @return The output zip file stream instance.
     */
    public ZipOutputStream getOut()
    {
        return m_out;
    }
}

