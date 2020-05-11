/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.CORBA_2_3.portable;

/**
 * Copyright (c) 1999 Object Management Group. Unlimited rights to
 * duplicate and use this code are hereby granted provided that this
 * copyright notice is included.
 */
public abstract class ObjectImpl
    extends org.omg.CORBA.portable.ObjectImpl
{
    public String _get_codebase()
    {
        org.omg.CORBA.portable.Delegate deleg = _get_delegate();
        if ( deleg instanceof org.omg.CORBA_2_3.portable.Delegate )
        {
            return ( ( org.omg.CORBA_2_3.portable.Delegate )
                     deleg ).get_codebase( this );
        }
        return null;
    }
}
