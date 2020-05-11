/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import glassbox.client.persistence.jdbc.ConfigurationDAO;
import glassbox.client.persistence.jdbc.ConnectionDAO;
import glassbox.client.pojo.ConnectionData;
import glassbox.client.remote.DistributedAgentManager;
import glassbox.client.util.StringUtil;
import glassbox.client.web.session.SessionData;
import glassbox.installer.GlassboxInstallerFactory;

import java.util.*;

import javax.servlet.http.HttpSession;

import org.springframework.dao.DataIntegrityViolationException;

public class ConnectionHelper extends BaseHelper  {

    protected static ConnectionHelper instance = new ConnectionHelper();
    protected DistributedAgentManager agentManager = null;
    
    protected ConnectionDAO connectionDAO = null;
    
    protected ConcurrentHashMap connections = new ConcurrentHashMap();
    protected ConfigurationDAO configuration = null;
    
    
    public ConnectionHelper(ConnectionDAO connectionDAO, ConfigurationDAO configuration) {
		super();
		this.configuration = configuration;
		this.connectionDAO = connectionDAO;
        
        //Need to check the database...
        if (configuration.configure()) {
    		//Add the connections from db.        
    		Iterator databaseConnectionsIt = connectionDAO.iterator();
            while(databaseConnectionsIt.hasNext()) {
            	ConnectionData connectionData = (ConnectionData)databaseConnectionsIt.next();
            	connections.put(connectionData.getName(), connectionData);            
            }        
        }
	}

	public static ConnectionHelper instance() {
        return instance;
    }
    
    protected ConnectionHelper() {
        
    }
    
    public Collection getConnections(HttpSession session) {
        synchronized(session) {
            // make a copy for thread safety
            Collection result = new ArrayList(getConnectionMap(session).values());
//            System.err.println("getConnections: "+session+" for "+this);
//            for (Iterator it=result.iterator(); it.hasNext();) {
//                System.err.print(" "+it.next());
//            }
//            System.err.println();
            return result;
        }
    }
    
    public Map getConnectionMap(HttpSession session) {
        synchronized(session) {
            SessionData sessionData = getSessionData(session);
            
            // synchronize: remove deleted ones
            Iterator sessionsIt = sessionData.getConnections().keySet().iterator();
            while(sessionsIt.hasNext()) {
                String name = (String)sessionsIt.next();
                if (getConnectionByName(name) == null) {
                    sessionsIt.remove();
//                    System.err.println("getConnection: "+session+" for "+this+" removed session copy of "+name);
                }
            }
            
            Iterator connectionsIt = connections.values().iterator();
            while(connectionsIt.hasNext()) {
                ConnectionData data = (ConnectionData)connectionsIt.next();
                data.checkConnectivity();
                if(!sessionData.getConnections().containsKey(data.getName())) {
                    try {
                        sessionData.getConnections().put(data.getName(), data.copy());
//                        System.err.println("getConnection: "+session+" for "+this+" added session copy of "+data);
                    } catch (Exception e) {                    
    //                    e.printStackTrace();
                    }                
                } else {
                    //Sort of a hack to synchronize all sessions. Should be using a listener...
                    ConnectionData sessionCopy = (ConnectionData)sessionData.getConnections().get(data.getName());
                    sessionCopy.setConnected(data.isConnected());
                }
            }
            return sessionData.getConnections();
        }
    }
    
    public ConnectionData getConnection(HttpSession session, String name) {
        synchronized(session) {
            SessionData sessionData = getSessionData(session);
            Map sessionConnections = sessionData.getConnections();
            ConnectionData data = (ConnectionData) sessionConnections.get(name);
            if (data != null) {
                return data;
            }
            return (ConnectionData)getConnectionMap(session).get(name);
        }
    }

        
    public void addConnection(String name, String url) {
        ConnectionData data = null;
        if((data = getConnectionByName(name)) == null) {
            data = new ConnectionData();
            connections.put(name, data);
            connectionDAO.add(data);
        }
        data.setName(name);
        data.setUrl(url);
        data.setViewed(true);
        agentManager.addAgentClientManager(data);
    }
    
    public void reset(String name) {
       agentManager.getAgentClientManager(name).resetStatistics();
    }
    
    public String deleteConnection(String name) {
        ConnectionData data = deleteConnectionByName(name);
        if (data == null) {
            return "unknown.connection";            
        }
        return "ok";
    }
        
    public String buildAndAddConnection(String name, String oldName, String hostname, String protocol, String port) {
        return buildAndAddConnectionHelper(name, oldName, hostname, protocol, port, false);
    }
    
    public String buildAndAddTempConnection(String name, String oldName, String hostname, String protocol, String port) {
        return buildAndAddConnectionHelper(name, oldName, hostname, protocol, port, true);
    }
    /**
     * @return validation code or null if ok
     */
    public String buildAndAddConnectionHelper(String name, String oldName, String hostname, String protocol, String port, boolean temp) {
        String connectionURL;
        //System.err.println("buildAndAdd: "+name);
        connectionURL = getConnectionUrl(hostname, protocol, port);

        if ("".equals(name)) {
            // just a sanity check - will be blocked in JavaScript
            return "invalid.name";
        }
        
        ConnectionData data = getConnectionByName(name);
        if (!name.equals(oldName)) {
            if (data != null) {
                return "duplicate.name";                
            }
        }
        //TODO check duplicate.endpoint
            
        data = deleteConnectionByName(oldName);   
        if (data==null) {
            data = new ConnectionData();
        } else {
            OperationHelper.instance().removedAgent(oldName);
        }

        data.setName(name);
        data.setHostName(hostname);
        data.setPort(port);
        data.setProtocol(protocol);
        data.setUrl(connectionURL);
        data.setViewed(true);

        agentManager.addAgentClientManager(data);
        connections.put(name, data);
        if (!temp) {
            try {
                connectionDAO.add(data);    
            } catch (DataIntegrityViolationException ve) {
                return "duplicate.name"; // should never happen
            }
        }
        return "ok";
    }

    public boolean removeLocalConnectionsTransiently() {
        boolean deleted = false;
        for (Iterator it=connections.values().iterator(); it.hasNext();) {
            ConnectionData data = (ConnectionData)it.next();            
            if (ConnectionData.LOCAL_PROTOCOL.equals(data.getProtocol())) {
                deleteConnection(data);
                deleted = true;
            }
        }
        return deleted;
    }
    
    protected ConnectionData deleteConnectionByName(String name) {
        ConnectionData data = (ConnectionData)connections.remove(name);
        if (data != null) {
            deleteConnection(data);
            connectionDAO.delete(data);
        }
        return data;
    }
    
    protected void deleteConnection(ConnectionData data) {
        deleteFromAgentManager(data.getName());
        OperationHelper.instance().removedAgent(data.getName());
    }        
    
    private String getConnectionUrl(String hostname, String protocol, String port) {
        String connectionURL;
        if (ConnectionData.JMX_RMI_PROTOCOL.equals(protocol)) {
            connectionURL = ConnectionData.JMX_RMI_CONNECTION_URL;
        } else if (ConnectionData.RMI_PROTOCOL.equals(protocol)) {
            connectionURL = ConnectionData.RMI_CONNECTION_URL;
        } else {
            connectionURL = ConnectionData.LOCAL_CONNECTION_URL;
        }
        connectionURL = StringUtil.replace(connectionURL, ConnectionData.PORT, port);
        connectionURL = StringUtil.replace(connectionURL, ConnectionData.HOSTNAME, hostname);
        return connectionURL;
    }   
    
    public void setVisibility(HttpSession session, String name, boolean visibility) {
    	SessionData sessionData = getSessionData(session);
        ConnectionData data = null;
        if ((data = (ConnectionData) sessionData.getConnections().get(name)) != null) {
            data.setViewed(visibility);
        }          
    }
    
    protected ConnectionData getConnectionByName(String name) {
        return (ConnectionData)connections.get(name);
    }

    public DistributedAgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(DistributedAgentManager agentManager) {
    	this.agentManager = agentManager;        
        if(connections != null) {
            Iterator connectionIt = connections.values().iterator();
            while(connectionIt.hasNext()) {
                ConnectionData data = (ConnectionData)connectionIt.next();              
                agentManager.addAgentClientManager(data);
            }
        }                
    }

    protected void deleteFromAgentManager(String name) {
        agentManager.removeAgentClient(name);
    }

	public Collection getAllConnections() {
		return connections.values();
	}
	
	public ConnectionDAO getConnectionDAO() {
		return connectionDAO;
	}
	
	public void setConnectionDAO(ConnectionDAO connections) {
		this.connectionDAO = connections;
	}

	public ConfigurationDAO getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ConfigurationDAO configuration) {
		this.configuration = configuration;
	}
}
