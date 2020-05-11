/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.adapter.poa;

import org.openorb.orb.adapter.ObjectAdapter;
import org.openorb.orb.adapter.AdapterDestroyedException;
import org.openorb.util.ExceptionTool;

/**
 * This is the implementation of the POA DomainManager.
 * It provides a method to access the policies assigned to the
 * the object adapter that has been used on creation of the instance.
 * @author Unknown
 */
class POADomainManagerImpl
    extends org.omg.CORBA.DomainManagerPOA
{
    private ObjectAdapter m_root;

    public POADomainManagerImpl( ObjectAdapter root )
    {
        m_root = root;
    }

    public org.omg.CORBA.Policy get_domain_policy( int policy_type )
    {
        byte [] object_id = _object_id();
        ObjectAdapter target;
        try
        {
            target = m_root.find_adapter( object_id );
        }
        catch ( final AdapterDestroyedException ex )
        {
            throw ExceptionTool.initCause( new org.omg.CORBA.OBJ_ADAPTER(), ex );
        }
        int [] ts = new int[ 1 ];
        ts[ 0 ] = policy_type;
        org.omg.CORBA.Policy [] policies = target.get_server_policies( ts );
        if ( policies.length == 0 || policies[ 0 ] instanceof org.omg.CORBA.LocalObject )
        {
            throw new org.omg.CORBA.INV_POLICY( 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES );
        }
        return policies[ 0 ];
    }
}

