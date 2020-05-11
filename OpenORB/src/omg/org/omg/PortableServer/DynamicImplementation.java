/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.omg.PortableServer;

/**
 * This interface must be implemented to provide a dynamic skeleton.
 */
public abstract class DynamicImplementation
    extends Servant
{
    public abstract void invoke( org.omg.CORBA.ServerRequest request );
}
