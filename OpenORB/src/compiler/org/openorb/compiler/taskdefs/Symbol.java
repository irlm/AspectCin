/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.compiler.taskdefs;

import org.apache.tools.ant.Task;

/**
 * @author Erik Putrycz
 * @version $Revision: 1.2 $ $Date: 2004/02/10 21:02:42 $
 *
 * Nested tag of Idl2Java for a Symbol
 */
public class Symbol extends Task
{
    private String m_name, m_value;

    public void setName( String name )
    {
        m_name = name;
    }

    public String getName()
    {
        return m_name;
    }

    public void setValue( String value )
    {
        m_value = value;
    }

    public String getValue()
    {
        return m_value;
    }
}
