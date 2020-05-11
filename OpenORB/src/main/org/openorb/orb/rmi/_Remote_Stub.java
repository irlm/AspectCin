/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.rmi;

/**
 * This is a default implementation for a Remote interface
 *
 * @author Jerome Daniel
 */
public class _Remote_Stub
    extends javax.rmi.CORBA.Stub
    implements java.rmi.Remote
{
    /**
     * Returns the array of repository IDs for the object.
     *
     * @return Array of repository IDs.
     */
    public String[] _ids()
    {
        String [] id = { "IDL:omg.org/CORBA/Object:1.0" };
        return id;
    }
}

