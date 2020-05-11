/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import glassbox.client.helper.OperationHelper.OperationBuilder;
import glassbox.client.pojo.ConnectionData;
import glassbox.client.pojo.OperationData;
import glassbox.client.remote.DistributedAgentManager;
import glassbox.common.BaseTestCase;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.springframework.context.ApplicationContext;

public class OperationHelperTest extends BaseTestCase {

    protected static final String CONNECTION_URL = "service:jmx:rmi://localhost:7232/jndi/rmi://localhost:7232/GlassboxTroubleshooter";
    
    
    public OperationHelperTest(String arg0) {
        super(arg0);

    }

    public static Test suite() {
        TestSuite suite = new TestSuite(OperationHelperTest.class);
        return suite;
    }
    
    public void testOperationHelper() {
        
        ApplicationContext context = getContext();
        assertNotNull(context);
        
        OperationHelper helper = (OperationHelper)context.getBean("operationHelper");
        assertNotNull(helper);
        
        DistributedAgentManager agentManager = helper.getAgentManager();
        assertNotNull(agentManager);
        
        OperationBuilder builder = helper.new OperationBuilder();
        builder.updateListOfOperations();
        //assertTrue(helper.getOperations(null).size() == 0);
        
        //Add a connection
        agentManager.addAgentClientManager(new ConnectionData("testName", "testD", CONNECTION_URL));
        assertTrue(agentManager.getAgentClientManagers().size() > 0);
        
        //Recheck the operations
        try {
            builder.updateListOfOperations();
            assertTrue(helper.getOperations(null).size() > 0);
            
            Iterator it = helper.getOperations(null).iterator();
            while(it.hasNext()) {
                OperationData data = (OperationData)it.next();
                assertNotNull(data);
                System.out.println("Operation: " + data.getOperationShortName() + " " + data.getKey());                
                assertNotNull(helper.getOperationAnalysis(data));
                
            }
        } catch (Exception e) {
            //don't let the test fail until we have a server available for automated regression tests
            System.err.println("Test failed: is the server running?");
            e.printStackTrace();
        }                
    }
    
    
}
