/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.io;

import org.openorb.util.HexPrintStream;

/**
 * Small scrap of data.
 * This is a simple struct (no behaviour) for a small piece of
 * data inside a marshalling stream.
 *
 * <pre>
 *                    m_head                                  m_tail
 *                  m_fPosition                             m_fPosition
 *     |-----------------|---------------------------------------|
 *                       |         ____fNext___                  |
 *                       |         |          |                  |
 * +----------------------------------+    +----------------------------------+
 * |                     |            |    |                     |            |
 * |   |-----fLength-----|            |    |   |-----fLength-----|            |
 * |   +--------------------------+   |    |   +--------------------------+   |
 * |   |        m_fBuffer         |   |    |   |        m_fBuffer         |   |
 * |   +--------------------------+   |    |   +--------------------------+   |
 * |   |-----m_fBuffer.length-----|   |    |   |-----m_fBuffer.length-----|   |
 * |                                  |    |                                  |
 * +----------------------------------+    +----------------------------------+
 *
 * </pre>
 *
 * @author Chris Wood
 * @version $Revision: 1.6 $ $Date: 2004/11/20 12:53:02 $
 */
class Scrap
{
    /** Default size of a scrap's buffer: 2 kBytes. */
    public static final int SCRAP_SIZE_DEFAULT = 2048;

    /** Normal internal scrap. */
    public static final int SCRAP_MODE_NORMAL = 0x00;
    /** A scrap that shares buffer space with a buffer further down the list. */
    public static final int SCRAP_MODE_SHARED = 0x01;
    /** The scrap's buffer space is readonly. Clone it to make it read-write. */
    public static final int SCRAP_MODE_READONLY = 0x03;

    /** A counter that identifies this instance. */
    private static int s_global_index = 0;

    /** This scrap's byte buffer. */
    public byte [] m_fBuffer;

    /** ??? */
    public int m_fOffset;

    /** The number of valid bytes inside the buffer. */
    public int m_fLength;

    /** The overall position of this scrap inside the top level container (array). */
    public int m_fPosition;

    /** The mode of this scrap. See SCRAP_MODE_* constants in this class. */
    public int m_fMode = SCRAP_MODE_NORMAL;

    /** Next scrap instance in the list. */
    public Scrap m_fNext = null;

    /** The unique index of this Scrap instance. */
    private int m_index;

    /**
     * Constructor.
     */
    public Scrap()
    {
       m_index = ++s_global_index;
    }

    /**
     * Return a string representation of this instance.
     *
     * @return A string describing this instance.
     */
    public String toString()
    {
       StringBuffer sb = new StringBuffer();
       sb.append( "Scrap [" );
       sb.append( "idx=" );
       sb.append( m_index );
       sb.append( ", buf[8]='" );
       sb.append( HexPrintStream.toHex( m_fBuffer[0] ) );
       sb.append( HexPrintStream.toHex( m_fBuffer[1] ) );
       sb.append( HexPrintStream.toHex( m_fBuffer[2] ) );
       sb.append( HexPrintStream.toHex( m_fBuffer[3] ) );
       sb.append( " " );
       sb.append( HexPrintStream.toHex( m_fBuffer[4] ) );
       sb.append( HexPrintStream.toHex( m_fBuffer[5] ) );
       sb.append( HexPrintStream.toHex( m_fBuffer[6] ) );
       sb.append( HexPrintStream.toHex( m_fBuffer[7] ) );
       sb.append( "' , buf_size=" );
       sb.append( ( ( m_fBuffer != null ) ? ( "" + m_fBuffer.length ) : "null" ) );
       sb.append( ", off=" );
       sb.append( m_fOffset );
       sb.append( ", len=" );
       sb.append( m_fLength );
       sb.append( ", pos=" );
       sb.append( m_fPosition );
       sb.append( ", mode=" );
       sb.append( m_fMode );
       sb.append( "]" );
       return sb.toString();
    }
}
