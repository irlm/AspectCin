/*
* Copyright (C) The Community OpenORB Project. All rights reserved.
*
* This software is published under the terms of The OpenORB Community Software
* License version 1.0, a copy of which has been included with this distribution
* in the LICENSE.txt file.
*/

package org.openorb.orb.test.rmi;

import java.util.Properties;

import junit.framework.TestCase;

import org.openorb.orb.rmi.DefaultORB;

/**
 * Skeleton orb test case to use when testing an orb component. The pre_init and
 * post init functions deal with creating and destroying orb instances for the
 * client and server ends.
 *
 * @author Chris Wood
 */
public abstract class RMITestCase
    extends TestCase
{
    /**
     * Constructor.
     * @param name Name of the test case.
     */
    public RMITestCase( String name )
    {
        super( name );
    }

    /**
     * This method is called prior to calling run and basically starts
     * up a server and client orb, and spawns a thread for the server orb
     * to run with.
     */
    protected void setUp()
    {
        Properties orbProps = new Properties();
        try
        {
            DefaultORB.setInitParams( null, orbProps );
        }
        catch ( IllegalStateException ex )
        {
            // orb allready set up, ignore
            // we could check to ensure the no local invoke attribute is set
            // but don't worry about it for now.
        }
    }

    /**
     * This method is called after calling run. It shuts down the local name
     * service.
     */
    protected void tearDown()
    {
    }
}

