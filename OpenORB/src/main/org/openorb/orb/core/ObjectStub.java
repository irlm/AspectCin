/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.core;

/**
 * This class is the stub for org.omg.CORBA.Object.
 *
 * @author Chris Wood
 * @version $Revision: 1.3 $ $Date: 2004/02/10 21:02:47 $
 */
public class ObjectStub
    extends org.omg.CORBA_2_3.portable.ObjectImpl
    implements org.omg.CORBA.portable.IDLEntity
{
    /**
     * Default constructor. Leave delegate unset.
     */
    public ObjectStub()
    {
    }

    /**
     * Create new delegate with specified orb / IOR
     */
    public ObjectStub( org.omg.CORBA.ORB orb, org.omg.IOP.IOR ior )
    {
        _set_delegate( new org.openorb.orb.core.Delegate( orb, ior ) );
    }

    private static final String[] ID_LIST = new String[ 0 ];

    /**
     * Return list of object ids.
     */
    public String[] _ids()
    {
        return ID_LIST;
    }
}

