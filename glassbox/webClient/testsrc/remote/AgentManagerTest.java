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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AgentManagerTest extends BaseTestCase {

    protected static final String CONNECTION_URL = "service:jmx:rmi://127.0.0.1:7232/jndi/rmi://127.0.0.1:7232/GlassboxTroubleshooter";
    //protected static final String CONNECTION_URL = "service:jmx:rmi://localhost:7232/jndi/rmi://localhost:7232/GlassboxTroubleshooter";
    public AgentManagerTest(String arg0) {
        super(arg0);        
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AgentManagerTest.class);
        return suite;
    }
    
    public void testAgentManager() {                
        AgentClientProvider provider = AgentClientManager.getClient(new ConnectionData("test", "testD", CONNECTION_URL));
        assertNotNull(provider);            
        try {
            Set operations = provider.selectOperations();
            assertNotNull(operations);            
            assertTrue(operations.size() > 0);  
            System.out.println("Number of operations: " + operations.size());
            assertTrue(isOpen(CONNECTION_URL));
        } catch (AgentConnectionException e) {            
            System.err.println("warning: test not valid: can't connect to server");
        }        
    }
    
    protected boolean isOpen(String URL) {             
        int hostIndex = URL.lastIndexOf("//") + 2;
        int portIndex = URL.lastIndexOf(":");
        int endPortIndex = URL.lastIndexOf("/");
        Socket skt = new Socket();
        if(hostIndex > 0 && portIndex > hostIndex && endPortIndex > portIndex) {
            String hostName = URL.substring(hostIndex, portIndex);
            int port = Integer.parseInt(URL.substring(portIndex + 1, endPortIndex));
            try {                    
            skt.connect(new InetSocketAddress(hostName, port), 2000);               
            return true;
            } catch(Exception e) {
            System.out.println("Error in connecting: " + e.getMessage());	
            return false;
            } finally {           
            try {
                skt.close();
                        } catch (IOException e) {                      
                            e.printStackTrace();
                        }
            }
        }
        return true;
    }
    
    
}
