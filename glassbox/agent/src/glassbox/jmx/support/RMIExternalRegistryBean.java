/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on December 3, 2005
 */
package glassbox.jmx.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.rmi.Naming;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.net.ConnectException;

import java.rmi.RemoteException;

/**
 * Starts the RMI Registry in a Process so that it might be shut down. Not used by default in Glassbox,
 * but available to be configured if necessary.
 * 
 * Based on Spring Auto-start Bean, and the original RMIFactoryBean. Uses the same XML summary ad the
 * RMIFactoryBean. Ex.:
 * 
 * <bean id="rmiRegistry" class="glassbox.jmx.support.RMIRegistryBean"> <property name="port" value="7232"/> </bean>
 * 
 * @author Joseph Shoop
 */
public class RMIExternalRegistryBean implements InitializingBean, DisposableBean {

    protected Process rmiProcess = null;

    private RMIClientSocketFactory clientSocketFactory;

    protected final String JAVA_HOME = "java.home";

    protected final String registryPath = File.separator + "bin" + File.separator + "rmiregistry";

    protected int testSocketTimeout = 100;

    protected String hostname = "localhost";

    protected String port = "1099";

    protected boolean daemon = true;

    protected boolean running = false;

    protected boolean existing = false;

    public boolean isSingleton() {
        return true;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return this.port;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setTestSocketTimeout(int timeout) {
        this.testSocketTimeout = timeout;
    }

    public int getTestSocketTimeout() {
        return this.testSocketTimeout;
    }

    public boolean getDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isExisting() {
        return existing;
    }

    public void setClientSocketFactory(RMIClientSocketFactory clientSocketFactory) {
        this.clientSocketFactory = clientSocketFactory;
    }

    public void afterPropertiesSet() throws Exception {
        initialize();
    }

    protected String buildCommandString() {
        String javaHomePath = null;
        String registryCommand = null;
        if ((javaHomePath = System.getProperties().getProperty(JAVA_HOME)) != null) {
            registryCommand = javaHomePath + registryPath;
        } else {
            registryCommand = "rmiregistry";
        }

        return registryCommand;
    }

    protected void startRMIServer() {
        try {
            if (!isRegistryRunning()) {
                rmiProcess = Runtime.getRuntime().exec(new String[] { buildCommandString(), getPort() });
                try {
                    while(!isRegistryRunning()) {
                        Thread.sleep(10);
                    }
                } catch(Exception we) {}
                running = true;
            } else {
                existing = true;
                running = true;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    protected boolean isRegistryRunning() {
        try {
            Registry registry = LocateRegistry.getRegistry(getHostname(), new Integer(getPort()).intValue(),
                    new RMIClientSocketFactory() {
                        public Socket createSocket(String host, int port) throws IOException {
                            try {
                                Socket socket = new Socket(getHostname(), new Integer(getPort()).intValue());
                                socket.setSoTimeout(testSocketTimeout);
                                return socket;
                            } catch (Exception e) {
                                throw new IOException();
                            }
                        }
                    });
            registry.list();
            return true;
        } catch (IOException ext) {
            return false;
        } catch (Exception ext) {
            return false;
        }
    }

    protected void testRegistry(Registry registry) throws RemoteException {
        registry.list();
    }

    public void addShutdownHook() {
        try {
            // Add a shutdown hook if not daemon
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    if (rmiProcess != null) {
                        rmiProcess.destroy();
                        rmiProcess = null;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        startRMIServer();
        if (!daemon) {
            addShutdownHook();
        }
    }

    public void destroy() throws Exception {
        if (!daemon) {
            if (rmiProcess != null) {
                rmiProcess.destroy();
                rmiProcess = null;
            }
        }
    }

    public void stop() throws Exception {
        destroy();
    }

    public void start() {
        startRMIServer();
    }

    private static final long serialVersionUID = 1L;
}
