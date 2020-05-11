/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.complex;

import java.io.Serializable;

/**
 * This class uses the ObjectOutputStream.PutField mechanism for
 * serializing the internal members.
 */
public class SerialPersistentFieldsTest
    implements Serializable
{
    private char m_c;
    private boolean m_b;
    private byte m_y;
    private short m_s;
    private int m_i;
    private long m_l;
    private float m_f;
    private double m_d;
    private String m_str;
    private String m_another_str;

    private static final java.io.ObjectStreamField[] SERIAL_PERSISTENT_FIELDS =
        new java.io.ObjectStreamField[]
        {
            new java.io.ObjectStreamField( "m_c", Character.TYPE ),
            new java.io.ObjectStreamField( "m_b", Boolean.TYPE ),
            new java.io.ObjectStreamField( "m_y", Byte.TYPE ),
            new java.io.ObjectStreamField( "m_s", Short.TYPE ),
            new java.io.ObjectStreamField( "m_i", Integer.TYPE ),
            new java.io.ObjectStreamField( "m_l", Long.TYPE ),
            new java.io.ObjectStreamField( "m_f", Float.TYPE ),
            new java.io.ObjectStreamField( "m_d", Double.TYPE ),
            new java.io.ObjectStreamField( "m_str", String.class )
        };

    public SerialPersistentFieldsTest()
    {
        m_c = 'c';
        m_b = true;
        m_y = 127;
        m_s = 1000;
        m_i = 1000000;
        m_l = 100000000000L;
        m_f = 10.3f;
        m_d = 10E+200;
        m_str = new String( "Halloween" );
        m_str = new String( "Another Halloween" );
    }

    public boolean equals( Object obj )
    {
        SerialPersistentFieldsTest spft = ( SerialPersistentFieldsTest ) obj;
        return  m_c == spft.m_c
             && m_b == spft.m_b
             && m_y == spft.m_y
             && m_s == spft.m_s
             && m_i == spft.m_i
             && m_l == spft.m_l
             && m_f == spft.m_f
             && m_d == spft.m_d
             && m_str.equals( spft.m_str );
    }
}

