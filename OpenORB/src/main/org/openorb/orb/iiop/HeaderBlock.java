/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

import org.openorb.orb.io.MarshalBuffer;

/**
 * This class is used to create GIOP headers.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:49 $
 */
final class HeaderBlock
    implements MarshalBuffer.HeaderGenerator
{
    private static final byte [][] GIOP_HEADERS = new byte[][]
    {
        { ( byte ) 'G', ( byte ) 'I', ( byte ) 'O', ( byte ) 'P',
          ( byte ) 1, ( byte ) 0 },
        { ( byte ) 'G', ( byte ) 'I', ( byte ) 'O', ( byte ) 'P',
          ( byte ) 1, ( byte ) 1 },
        { ( byte ) 'G', ( byte ) 'I', ( byte ) 'O', ( byte ) 'P',
          ( byte ) 1, ( byte ) 2 }
    };

    private byte m_msgType;

    private int m_reqID;

    private byte [] m_reqIDBytes;

    private org.omg.GIOP.Version m_version;

    HeaderBlock( byte msgType, int reqID, CDROutputStream os )
    {
        m_msgType = msgType;
        m_reqID = reqID;
        m_version = os.version();

        os.write_octet_array( GIOP_HEADERS[ m_version.minor ], 0, 6 );
        os.addHeader( this, 6, m_version.minor >= 2, null );
    }

    /**
     * Called when message is about to get sent. Modifications can be made
     * to the bytes allocated at the addHeader stage.
     *
     * @param buf buffer containing the reserved bytes. Not all of the buffer
     *           is considered to be read-write.
     * @param pos offset into buf of first modifiable byte.
     * @param len length of modifiable bytes.
     * @param fragment true if this is called as a response to the fragment
     *           operation.
     * @param length length in bytes between the position that addHeader was
     *           called and the end of the message.
     * @param cookie the cookie passed to the addHeader operation.
     */
    public void endMessage( byte[] buf, int pos, int len, boolean fragment,
                            int length, Object cookie )
    {
        // don't include the header itself in the fragment size.
        length -= 6;

        buf[ pos ] = ( byte ) ( fragment ? 2 : 0 );
        buf[ pos + 1 ] = m_msgType;
        buf[ pos + 2 ] = ( byte ) ( length >>> 24 );
        buf[ pos + 3 ] = ( byte ) ( length >>> 16 );
        buf[ pos + 4 ] = ( byte ) ( length >>> 8 );
        buf[ pos + 5 ] = ( byte ) length;

        m_msgType = ( byte ) org.omg.GIOP.MsgType_1_1._Fragment;
    }

    /**
     * Called to begin a new fragment. Writes may be made to the marshal buffer,
     * including adding a new header.
     *
     * @param buffer the buffer getting marshaled to.
     * @param cookie the cookie passed to the addHeader operation.
     */
    public void beginMessage( MarshalBuffer buffer, Object cookie )
    {
        // This does not write through the stream since fragment headers
        // do not contribute to message lengths.
        buffer.append( GIOP_HEADERS[ m_version.minor ], 0, 6 );
        buffer.addHeader( this, 6, m_version.minor >= 2, null );

        if ( m_version.minor >= 2 )
        {
            if ( m_reqIDBytes == null )
            {
                m_reqIDBytes = new byte[]
                    {
                        ( byte ) ( m_reqID >>> 24 ), ( byte ) ( m_reqID >>> 16 ),
                        ( byte ) ( m_reqID >>> 8 ),  ( byte ) m_reqID
                    };
            }
            buffer.append( m_reqIDBytes, 0, 4 );
        }
    }
}

