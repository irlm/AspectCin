/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.remote;

import glassbox.client.pojo.ConnectionData;



public class AgentClientManager {

    private static AgentClientManager instance = new AgentClientManager();

    private AgentClientManager() {
    }

    public static AgentClientManager getNewInstance() {
        return new AgentClientManager();
    }

    public static AgentClientProvider getClient(ConnectionData connectionData) {
        String protocol = protocolPrefix(connectionData.getUrl());
        if (protocol.equals(protocolPrefix(ConnectionData.LOCAL_CONNECTION_URL))) {
            return new LocalAgentClient(connectionData);
        } else if (protocol.equals(protocolPrefix(ConnectionData.JMX_RMI_CONNECTION_URL))) {
            return new JmxAgentClientProviderImpl(connectionData);
        } else if (protocol.equals(protocolPrefix(ConnectionData.RMI_CONNECTION_URL))) {
            return new RmiAgentClientProviderImpl(connectionData);
        }
        
        throw new IllegalArgumentException("Unrecognized protocol "+connectionData.getUrl());
    }

    public static String protocolPrefix(String url) {
        int protocolPos = url.indexOf(':');
        return url.substring(0, protocolPos).toLowerCase();
    }        
}
