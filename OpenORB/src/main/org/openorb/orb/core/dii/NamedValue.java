/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core.dii;

/**
 * This class implements the OMG class : NamedValue.
 *
 * @author Jerome Daniel
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:47 $
 */
public class NamedValue
    extends org.omg.CORBA.NamedValue
{
    /**
     * Value name
     */
    private String m_name;

    /**
     * Value
     */
    private org.omg.CORBA.Any m_value;

    /**
     * Value flag
     */
    private int m_flags;

    /**
     * Constructor
     */
    public NamedValue( String name, org.omg.CORBA.Any value, int flags )
    {
        m_name = name;
        m_value = value;
        m_flags = flags;
    }

    /**
     * Return the value name
     */
    public String name()
    {
        return m_name;
    }

    /**
     * Return the value
     */
    public org.omg.CORBA.Any value()
    {
        return m_value;
    }

    /**
     * Return the value flag
     */
    public int flags()
    {
        return m_flags;
    }

    /**
     * Set a new value
     */
    public void setNewValue( org.omg.CORBA.Any new_value )
    {
        m_value = new_value;
    }
}

