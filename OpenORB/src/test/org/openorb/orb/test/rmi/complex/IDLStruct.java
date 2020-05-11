/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi.complex;

/**
 * This class is used to test the mapping for IDLEntities other
 * than org.omg.CORBA.Any and org.omg.CORBA.TypeCode.
 *
 * @author Michael Rumpf
 */
public final class IDLStruct
    implements org.omg.CORBA.portable.IDLEntity
{
    private short m_x;

    public IDLStruct()
    {
        m_x = 0;
    }

    public IDLStruct( short x )
    {
        m_x = x;
    }

    public short getValue()
    {
        return m_x;
    }

    public void setValue( short x )
    {
        m_x = x;
    }
}
