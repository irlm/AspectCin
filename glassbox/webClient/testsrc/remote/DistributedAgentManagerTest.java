/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package remote;

import glassbox.client.pojo.ConnectionData;
import glassbox.client.remote.*;
import glassbox.common.BaseTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class DistributedAgentManagerTest extends BaseTestCase {

    protected static final String CONNECTION_URL = "service:jmx:rmi://localhost:7232/jndi/rmi://localhost:7232/GlassboxTroubleshooter";
    
    public DistributedAgentManagerTest(String arg0) {
        super(arg0);
        
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DistributedAgentManagerTest.class);
        return suite;
    }
    
    public void testDistributedAgentManager() {
        
        DistributedAgentManager manager = DistributedAgentManager.instance();
        assertNotNull(manager);
        
        manager.addAgentClientManager(new ConnectionData("testName", "testD", CONNECTION_URL));
        assertTrue(manager.getAgentClientManagers().size() > 0);
        
        try {
            AgentClientProvider client = manager.getAgentClientManager("testName");
            assertNotNull(client);
            assertEquals(CONNECTION_URL, client.getConnectionURL());
            
            assertTrue(client.selectOperations().size() > 0);                        
        } catch (AgentConnectionException e) {            
            System.err.println("warning: test not valid: can't connect to server");
        }
               
        
    }
    
}
