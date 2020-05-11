/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.remote;


import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import glassbox.client.pojo.ConnectionData;

import java.util.*;

public class DistributedAgentManager   {

    private static DistributedAgentManager instance = new DistributedAgentManager();

    protected ConcurrentHashMap agentClients = new ConcurrentHashMap();
 
    private static boolean active = false;
    
    private DistributedAgentManager() {
        
    }

    public static DistributedAgentManager instance() {
        return instance;
    }

    public AgentClientProvider getDefaultAgentClientManager() {
        if(agentClients.values() != null && agentClients.values().size() > 0)
            return (AgentClientProvider )(new ArrayList(agentClients.values()).get(0));
        
        return null;
    }
    
    public AgentClientProvider getAgentClientManager(String name) {
        return (AgentClientProvider )agentClients.get(name);
    }
    
    public void addAgentClientManager(final ConnectionData connectionData) {
         AgentClientProvider  agentClient = AgentClientManager.getClient(connectionData);
         addAgentClient(connectionData.getName(), agentClient); 
     }
    
    public void addAgentClient(String name, AgentClientProvider agentClient) {
       agentClients.put(name, agentClient);   
    }
    
    public void removeAgentClient(String name) {
        agentClients.remove(name);
    }
    
    public Collection getAgentClientManagerKeys() {
        return agentClients.keySet();
    }
    
    public Collection getAgentClientManagers() {
        return agentClients.values();
    }
    
    public void deleteAllClientManagers() {
        agentClients.clear();
    }
    
    /**
     * @see glassbox.client.remote.AgentClientProvider#selectOperations()
     */
    public Set selectOperations() {
        Set set = new HashSet();
        for(Iterator it = agentClients.values().iterator(); it.hasNext();) {
            AgentClientProvider  agentClient = (AgentClientProvider )it.next();
            if(agentClient.isActive()) {
                set.addAll(agentClient.selectOperations());
            }
        }
        return set;
    }
    
    
    public void resetStatistics() {
        for(Iterator it = agentClients.values().iterator(); it.hasNext();) {
            AgentClientProvider  agentClient = (AgentClientProvider )it.next();
            agentClient.resetStatistics();
        }
    }
    
    public Collection getActiveClientManagers() {
        ArrayList list = new ArrayList();
        for(Iterator it = agentClients.values().iterator(); it.hasNext();) {
            AgentClientProvider  agentClient = (AgentClientProvider )it.next();
            if(!agentClient.getLastFailed()) {
               list.add(agentClient); 
            }
        }
        return list;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        for(Iterator it = agentClients.values().iterator(); it.hasNext();) {
            AgentClientProvider  agentClient = (AgentClientProvider )it.next();
            agentClient.setActive(active);
        }
    }
    
    
    public boolean isActive() {
        return active;
    }
}
    
