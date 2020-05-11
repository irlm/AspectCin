/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

/*
 * Copyright (c) 1999 Object Management Group. Unlimited rights to
 * duplicate and use this code are hereby granted provided that this
 * copyright notice is included.
 */

package org.omg.CORBA.portable;

public class ApplicationException
    extends Exception
{
    private String m_id;

    private org.omg.CORBA.portable.InputStream m_is;

    public ApplicationException( String id, org.omg.CORBA.portable.InputStream is )
    {
        m_id = id;
        m_is = is;
    }

    public String getId()
    {
        return m_id;
    }

    public InputStream getInputStream()
    {
        return m_is;
    }
}
