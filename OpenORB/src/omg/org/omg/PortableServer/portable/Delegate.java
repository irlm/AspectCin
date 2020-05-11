/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.PortableServer.portable;

import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POA;

/**
 * The Delegate interface provides the ORB vendor specific implemenation of
 * PortableServer::Servant
 */
public interface Delegate
{
    org.omg.CORBA.ORB orb( Servant self );

    org.omg.CORBA.Object this_object( Servant self );

    POA poa( Servant self );

    byte [] object_id( Servant self );

    POA default_POA( Servant self );

    boolean is_a( Servant self, String repository_id );

    boolean non_existent( Servant self );

    org.omg.CORBA.Object get_interface_def( Servant self );
}
