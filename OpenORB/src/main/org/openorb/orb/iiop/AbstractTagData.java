/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.iiop;

/**
 * Base class for TagData objects. IIOPAddress creates for each tag found in a profile
 * a class of this type. The Delegate class takes care of dumping the info contained
 * in ech class.
 *
 * @author Chris Wood
 * @version $Revision: 1.4 $ $Date: 2004/02/10 21:02:48 $
 */
public class AbstractTagData
{
    private static final char[] HEX_DIGITS =
        {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        };
    private static final int DIGIT_MASK = 0xF;

    private final int m_componentId;
    private String m_msg;

    protected AbstractTagData( final int componentId )
    {
        m_componentId = componentId;
    }

    protected StringBuffer createMessage()
    {
        final StringBuffer buf = new StringBuffer();
        buf.append( "    ComponentID = " );
        buf.append( m_componentId );
        return buf;
    }

   /**
    * Appends the hex representation of an int to a StringBuffer.
    * The hex representation is always 8 characters long,
    * padded with leading '0' characters if necessary.
    */
    protected final void appendIntAsHex( final StringBuffer buf, final int value )
    {
        buf.ensureCapacity( buf.length() + 10 );
        buf.append( "0x" );

        for ( int i = 28; i >= 0; i -= 4 )
        {
            buf.append( HEX_DIGITS[ ( value >> i ) & DIGIT_MASK ] );
        }
    }

    public synchronized String toString()
    {
        if ( null == m_msg )
        {
            // create a new string to avoid wasting memory
            // see sourceforge bug 738579
            m_msg = new String( createMessage().toString() );
        }
        return m_msg;
    }
}

