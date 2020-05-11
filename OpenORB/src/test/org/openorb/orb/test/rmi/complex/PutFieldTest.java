/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.complex;

import java.io.Serializable;
import java.io.IOException;

/**
 * This class uses the ObjectOutputStream.PutField mechanism for
 * serializing the internal members.
 *
 * @author Michael Rumpf
 */
public class PutFieldTest
    implements Serializable
{
    private char    m_c;
    private boolean m_b;
    private byte    m_y;
    private short   m_s;
    private int     m_i;
    private long    m_l;
    private float   m_f;
    private double  m_d;
    private String  m_str;

    /**
     * Default constructor.
     */
    public PutFieldTest()
    {
        m_c   = 'c';
        m_b   = true;
        m_y   = 127;
        m_s   = 1000;
        m_i   = 1000000;
        m_l   = 100000000000L;
        m_f   = 10.3f;
        m_d   = 10E+200;
        m_str = new String( "Halloween" );
    }

    private void writeObject( java.io.ObjectOutputStream out )
        throws IOException
    {
        // ATTENTION: It does not work when the field names written to the stream
        // differ from the name sof the declared fields
        java.io.ObjectOutputStream.PutField pf = out.putFields();
        pf.put( "m_c",   m_c );
        pf.put( "m_b",   m_b );
        pf.put( "m_y",   m_y );
        pf.put( "m_s",   m_s );
        pf.put( "m_i",   m_i );
        pf.put( "m_l",   m_l );
        pf.put( "m_f",   m_f );
        pf.put( "m_d",   m_d );
        pf.put( "m_str", m_str );
        out.writeFields();
    }

    private void readObject( java.io.ObjectInputStream in )
        throws IOException, ClassNotFoundException
    {
        // ATTENTION: It does not work when the field names written to the stream
        // differ from the name sof the declared fields
        java.io.ObjectInputStream.GetField gf = in.readFields();
        m_c   = gf.get( "m_c", 'd' );
        m_b   = gf.get( "m_b", false );
        m_y   = gf.get( "m_y", ( byte ) 126 );
        m_s   = gf.get( "m_s", ( short ) 1001 );
        m_i   = gf.get( "m_i", 1000001 );
        m_l   = gf.get( "m_l", 100000000001L );
        m_f   = gf.get( "m_f", 10.4f );
        m_d   = gf.get( "m_d", 20E+200 );
        m_str = ( String ) gf.get( "m_str", new String( "Hello" ) );
    }

    /**
     * A method for testing equality.
     *
     * @param obj The object to test against.
     * @return True when the instances are equal, false otherwise.
     */
    public boolean equals( Object obj )
    {
        PutFieldTest pft = ( PutFieldTest ) obj;
        return  m_c == pft.m_c
             && m_b == pft.m_b
             && m_y == pft.m_y
             && m_s == pft.m_s
             && m_i == pft.m_i
             && m_l == pft.m_l
             && m_f == pft.m_f
             && m_d == pft.m_d
             && m_str.compareTo( pft.m_str ) == 0;
    }
}

