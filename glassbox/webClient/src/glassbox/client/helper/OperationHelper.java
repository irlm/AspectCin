/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper; 


import edu.emory.mathcs.backport.java.util.Collections;
import edu.emory.mathcs.backport.java.util.concurrent.*;
import glassbox.analysis.api.OperationSummary;
import glassbox.client.pojo.*;
import glassbox.client.remote.AgentClientProvider;
import glassbox.client.remote.DistributedAgentManager;
import glassbox.client.web.session.SessionData;

import java.util.*;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;

public class OperationHelper extends BaseHelper {
    
    protected static OperationHelper instance = new OperationHelper();
    protected DistributedAgentManager agentManager = null;    
    protected Collection testOperations = null;
    protected boolean monitoring = true;
    protected long monitoringFrequency = 5000L;
    protected ConcurrentHashMap operationMap = new ConcurrentHashMap();
    protected ConnectionHelper connectionHelper; 
    protected UpdateResponse updateResponse;
    private static final Log log = LogFactory.getLog(OperationHelper.class);
    private static final long serialVersionUID = 3L;
   
    public static OperationHelper instance() {
        return instance;
    }
    
    protected OperationHelper() {
        //Start the monitor.
        Thread monitoringThread = new OperationBuilder();
        monitoringThread.start();
    }
    
    public OperationHelper(boolean thread) {        
    }
  
    public UpdateResponse updateCheck() {
        return updateResponse;
    }
    
    public Collection getOperations(HttpSession session) {        
        ArrayList userList = new ArrayList();
        ArrayList list = new ArrayList(operationMap.values());
        Iterator listIt = list.iterator();
        SessionData sessionData = getSessionData(session);
        synchronized(session) {
            Map sessionConnections = connectionHelper.getConnectionMap(session);
            if (sessionConnections != null) {
                while(listIt.hasNext()) {
                    OperationData data = (OperationData)listIt.next();
                    if (data != null) {
                        String agentName = data.getAgentName();
                        ConnectionData sessionConnection = (ConnectionData)sessionConnections.get(agentName);
                        if(sessionConnection != null && sessionConnection.isViewed()) {
                            userList.add(data);	            
                        }
                    }
                }           
                Collections.sort(userList, sessionData.getColumnSorter());
            }
        }
        return userList;
    }
    
    public OperationData getOperation(String key) {
        OperationData data = (OperationData)operationMap.get(key);      
        return data;        
    }
    
    
    public OperationAnalysisData getOperationAnalysis(OperationData operation) {                
        return new OperationAnalysisData(agentManager.getAgentClientManager(operation.getAgentName()).findOperationAnalysis(operation.getOperationKey()));
    }

    // TODO: accept a list of names to reset
    public void reset() {
        Iterator it = DistributedAgentManager.instance().getAgentClientManagerKeys().iterator();
        while(it.hasNext()) {
            String name = (String)it.next();
            AgentClientProvider  agentClient = DistributedAgentManager.instance().getAgentClientManager(name); 
            agentClient.resetStatistics();
        }                    
    }    
    
    public ConfigurationData getConfigurationData() {
        return new ConfigurationData();
    }
    
    public void setConfigurationData(ConfigurationData data) {        
    }

    public DistributedAgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(DistributedAgentManager agentManager) {
        this.agentManager = agentManager;
    }

    
    public class OperationBuilder extends Thread {
        // 1.4 dependency: use backported thread pool
        protected BlockingQueue queue = new LinkedBlockingQueue();
        protected ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 15, 25L, TimeUnit.SECONDS, queue);
        
        public void run() {
            // this logic refreshes every monitoringFrequency ms even when exceptions occur
            // it'd be cleaner to use a TimerTask
            long nextRefresh = System.currentTimeMillis();
            while(monitoring) {
                try {
                    for (;;) {
                        long sleepTime = nextRefresh - System.currentTimeMillis();
                        if (sleepTime <= 0) {
                            break;
                        }
                        sleep(sleepTime);
                    };
                    while (!(queue.isEmpty())) {
                        log.warn("Backlog of remote agent operations encountered: this typically happens when the server can't "+
                                "establish connections to other servers or there's a configuration problem");
                    }
                    
                    updateListOfOperations();
                    
                    nextRefresh = System.currentTimeMillis() + monitoringFrequency;
                } catch (Throwable t) {
                    log.error("Problem while monitoring operations", t);
                }
            }
            threadPoolExecutor.shutdown();
        }

        /**
         * Update the list of client operations and return the display bean
         */
        public void updateListOfOperations() {
            if (!monitoring) return; // in case the application ended after we slept or before we got the lock

            Iterator it = DistributedAgentManager.instance().getAgentClientManagerKeys().iterator();
            while(it.hasNext()) {
                final String name = (String)it.next();
                final AgentClientProvider  agentClient = DistributedAgentManager.instance().getAgentClientManager(name);
                
                final int state=agentClient.getState();
                if(state==AgentClientProvider.NO_REQUEST || state==AgentClientProvider.CONNECTION_FAILURE) {
                    threadPoolExecutor.execute(new Runnable() { 
                        public void run() {
                            try {
                                updateListOfOperations(agentClient);
                            } catch (RuntimeException rte) {
                                removedAgent(name);
                                if (state==AgentClientProvider.NO_REQUEST) {
                                    // log ONCE when first not accessible
                                    log.warn("Can't connect to previously accessible agent "+name, rte);                                    
                                } else if (log.isDebugEnabled()) { 
                                    log.debug("Can't connect to agent "+name, rte);
                                }
                            }
                        }
                    });
                } else {
                    removedAgent(name); // we didn't remove it, but we don't have any operations available either!
                }
            }                    
        }

        protected void updateListOfOperations(AgentClientProvider agentClient) {
            Set allOperations = agentClient.selectOperations();
            String instanceName = agentClient.getAgentInstanceName();
            String instanceDescription = agentClient.getAgentInstanceDescription();
            String url = agentClient.getConnectionURL();
            
            Map clientOperationMap = new HashMap();
            if (allOperations != null) {
                Iterator iter = allOperations.iterator();
                while (iter.hasNext()) {
                    OperationData data = new OperationData(instanceName, instanceDescription, url, (OperationSummary)iter.next()); 
                    clientOperationMap.put(data.getKey(), data);
                }
            }
            
            operationMap.putAll(clientOperationMap);
            
            // iterate over all and delete any not added with the corresponding URL 
            for (Iterator it=operationMap.entrySet().iterator(); it.hasNext();) {
                Entry entry = (Entry)it.next();
                OperationData mainEntry = (OperationData)entry.getValue();
                if (url.equals(mainEntry.getAgentUrl())) {
                    if (!clientOperationMap.containsKey(mainEntry.getKey())) {
                        it.remove();
                    }
                }
            }
        }
        
    }

    public boolean isMonitoring() {
        return monitoring;
    }

    public void setMonitoring(boolean monitoring) {
        this.monitoring = monitoring;
    }

    protected void updateListOfOperations(AgentClientProvider agentClient) {
        Set allOperations = agentClient.selectOperations();
        String instanceName = agentClient.getAgentInstanceName();
        String instanceDescription = agentClient.getAgentInstanceDescription();
        String url = agentClient.getConnectionURL();
        
        Map clientOperationMap = new HashMap();
        if (allOperations != null) {
            Iterator iter = allOperations.iterator();
            while (iter.hasNext()) {
                OperationData data = new OperationData(instanceName, instanceDescription, url, (OperationSummary)iter.next()); 
                clientOperationMap.put(data.getKey(), data);
            }
        }
        
        operationMap.putAll(clientOperationMap);
        
        // iterate over all and delete any not added with the corresponding URL 
        for (Iterator it=operationMap.entrySet().iterator(); it.hasNext();) {
            Entry entry = (Entry)it.next();
            OperationData mainEntry = (OperationData)entry.getValue();
            if (url.equals(mainEntry.getAgentUrl())) {
                if (!clientOperationMap.containsKey(mainEntry.getKey())) {
                    it.remove();
                }
            }
        }
    }
    
    public synchronized void destroy() {
        monitoring = false;
    }

    public void removedAgent(String agentName) {
        for (Iterator it=operationMap.entrySet().iterator(); it.hasNext();) {
            Entry entry = (Entry)it.next();
            OperationData mainEntry = (OperationData)entry.getValue();
            if (agentName.equals(mainEntry.getAgentName())) {
                it.remove();
            }
        }
    }

	public ConnectionHelper getConnectionHelper() {
		return connectionHelper;
	}

	public void setConnectionHelper(ConnectionHelper connectionHelper) {
		this.connectionHelper = connectionHelper;
	}
    
    public UpdateResponse getUpdateResponse() {
        return updateResponse;
    }

    public void setUpdateResponse(UpdateResponse updateResponse) {
        this.updateResponse = updateResponse;
    }

}
