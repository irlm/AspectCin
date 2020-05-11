/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.pojo;

import glassbox.client.remote.AgentClientProvider;
import glassbox.client.remote.DistributedAgentManager;

import java.io.Serializable;

public class ConnectionData implements Serializable, Cloneable {
    public static final String JMX_RMI_PROTOCOL = "jmx/rmi";
    public static final String RMI_PROTOCOL = "rmi";
    public static final String LOCAL_PROTOCOL = "local";
    
    // for the *client*, we only need to know how to get to the jndi lookup
    public static final String JMX_RMI_CONNECTION_URL = "service:jmx:rmi:///jndi/rmi://@HOSTNAME@:@PORT@/GlassboxTroubleshooter";
    
    public static final String RMI_CONNECTION_URL = "rmi://@HOSTNAME@:@PORT@/GlassboxRmi";
    public static final String LOCAL_CONNECTION_URL = "local:glassbox";
    
	public static final String PORT = "@PORT@";
	public static final String HOSTNAME = "@HOSTNAME@";
	public Integer id = null;
	public String name = null;
    public String description = null;
    public String url = null;
    public String hostName = null;
    public String port = null;
    public String protocol = null;
    public boolean viewed = true;
    public boolean connected = false;

    private static final long serialVersionUID = 2L;
    
    public ConnectionData() {
    }
    
    public ConnectionData(String name, String description, String url) {
        this.name = name;
        this.description = description;
        this.url = url;
    }
    
    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConnected() {
        return connected;
    }
    
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    public boolean checkConnectivity() {
        AgentClientProvider agent = DistributedAgentManager.instance().getAgentClientManager(name);
        if (agent == null) {
            // can happen if a client has a stale snapshot...
            return connected=false;
        }
        // don't block, use last connection status instead!
        return connected=!agent.getLastFailed();
    }

    public ConnectionData copy() throws Exception {
       return (ConnectionData)this.clone();
    }

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

    /**
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

    public String getDescription() {
        if (description == null) {
            return name;
        }
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return super.toString()+" name="+name+", url="+url+", viewed = "+viewed+", connected = "+connected+", description = "+description;
    }
}
