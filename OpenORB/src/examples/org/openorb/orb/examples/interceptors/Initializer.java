/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.examples.interceptors;

/**
 * This class initializes the client and server side interceptors.
 */
public class Initializer
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ORBInitializer
{
    public void pre_init( org.omg.PortableInterceptor.ORBInitInfo info )
    {
        ClientInterceptor clientInterceptor = new ClientInterceptor();
        ServerInterceptor serverInterceptor = new ServerInterceptor();

        try
        {
            System.out.println( "Installing client interceptor..." );
            info.add_client_request_interceptor( clientInterceptor );
            System.out.println( "Installing server interceptor..." );
            info.add_server_request_interceptor( serverInterceptor );
            System.out.println( "Interceptors successfully installed!" );
        }
        catch ( org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName dn )
        {
            System.out.println( "DuplicateName" );
        }
    }

    public void post_init( org.omg.PortableInterceptor.ORBInitInfo info )
    {
    }
}
