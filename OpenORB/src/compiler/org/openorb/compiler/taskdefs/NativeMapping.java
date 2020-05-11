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
 *  Nested tag of Idl2Java for a NativeMapping
 */
public class NativeMapping extends Task
{

    private String m_name, m_mapping;

    public void setName( String name )
    {
        m_name = name;
    }

    public String getName()
    {
        return m_name;
    }

    public void setMapping( String mapping )
    {
        m_mapping = mapping;
    }

    public String getMapping()
    {
        return m_mapping;
    }
}
