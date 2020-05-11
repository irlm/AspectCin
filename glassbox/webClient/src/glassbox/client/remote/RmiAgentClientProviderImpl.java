/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.remote;

import glassbox.agent.control.api.GlassboxService;
import glassbox.client.pojo.ConnectionData;
import glassbox.thread.context.SavedContextLoader;

import org.springframework.remoting.rmi.RmiProxyFactoryBean;

public class RmiAgentClientProviderImpl extends AbstractAgentClientProvider {

    public RmiAgentClientProviderImpl(ConnectionData connectionData) {
        super(connectionData);
    }

    protected void connect() {
        SavedContextLoader savedContextLoader = new SavedContextLoader(); 
        try {
            // set the context classloder to use our plugin's classloader, to
            // pick up code in our plugin, not just the system classpath
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            RmiProxyFactoryBean springBean = new RmiProxyFactoryBean();
            springBean.setServiceUrl(URL.toString());
            springBean.setServiceInterface(GlassboxService.class);
            springBean.afterPropertiesSet();
            serviceBean = (GlassboxService)springBean.getObject();                        
        } catch (Exception e) {
            serviceBean = null;
            throw new AgentConnectionException("connection exception occurred-- is the server started?" , e, URL);
        } finally {
            savedContextLoader.restore();
        }
    }
    
    protected void close() {
        // sadly, a no-op in RMI?!
    }

}
