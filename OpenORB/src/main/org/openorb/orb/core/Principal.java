/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core;

/**
 * @author Jerome Daniel
 */
public class Principal
    extends org.omg.CORBA.Principal
{
    private byte[] m_name;

    /**
     * Constructor with fields initialization.
     *
     * @param nam name struct member.
     */
    public Principal( byte[] nam )
    {
        m_name = nam;
    }

    public void name( byte [] nam )
    {
        m_name = nam;
    }

    public byte [] name()
    {
        return m_name;
    }

    public boolean equals( Object obj )
    {
        Principal o2 = ( Principal ) obj;
        if ( m_name.length != o2.m_name.length )
        {
            return false;
        }
        for ( int i = 0; i < m_name.length; ++i )
        {
            if ( m_name[ i ] != o2.m_name[ i ] )
            {
                return false;
            }
        }
        return true;
    }

    public int hashCode()
    {
        return m_name == null ? 0 : 31 * m_name.length;
    }
}

