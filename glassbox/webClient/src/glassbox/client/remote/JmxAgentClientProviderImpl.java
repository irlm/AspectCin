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

import java.io.IOException;

import javax.management.*;
import javax.management.remote.*;

public class JmxAgentClientProviderImpl extends AbstractAgentClientProvider implements AgentClientProvider, ConnectByURL {
    
    private JMXConnector jmxc;

    private MBeanServerConnection mbsc;

    public JmxAgentClientProviderImpl(ConnectionData connectionData) {
        super(connectionData);
    }

   protected synchronized void connect() {
        try {
            close();
        } catch (Exception e) {
            ; // do nothing
        }

        SavedContextLoader savedContextLoader = new SavedContextLoader(); 
        try {
            // set the context classloder to use our plugin's classloader, to
            // pick up code in our plugin, not just the system classpath

            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            jmxc = JMXConnectorFactory.connect(new JMXServiceURL(URL));
            mbsc = jmxc.getMBeanServerConnection();

            // Get the Agent MX bean
            ObjectName dynMBeanName = new ObjectName("Glassbox:type=service,name=default");

            Object proxy = MBeanServerInvocationHandler.newProxyInstance(mbsc, dynMBeanName, GlassboxService.class,
                    true);
            serviceBean = (GlassboxService) proxy;

        } catch (Exception e) {
            serviceBean = null;
            throw new AgentConnectionException("connection exception occurred-- is the server started?", e, URL);
       } catch (NoClassDefFoundError err) {
           serviceBean = null;
           throw new AgentConnectionException("connection exception -- glassbox monitor not on classpath", err, URL);
       } finally {
           savedContextLoader.restore();
       }
    }

    protected void close() {

        try {
            if (jmxc != null) {
                jmxc.close();
            }
            jmxc = null;
            serviceBean = null;
        } catch (IOException e) {
            if (onceClose) {
                System.out.println("Error closing service connection");
                e.printStackTrace();
                onceClose=false;
            }
        }
    }
}
