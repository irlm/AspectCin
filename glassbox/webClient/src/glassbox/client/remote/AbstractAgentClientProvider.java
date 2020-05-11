/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.remote;

import glassbox.agent.control.api.GlassboxService;
import glassbox.analysis.api.ConfigurationSummary;
import glassbox.analysis.api.OperationAnalysis;
import glassbox.client.pojo.ConnectionData;
import glassbox.track.api.OperationDescription;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.rmi.UnmarshalException;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractAgentClientProvider implements AgentClientProvider {

    protected String URL = "";

    protected abstract void close();

    protected abstract void connect();

    private String agentInstanceName;
    private String agentInstanceDescription;

    protected GlassboxService serviceBean;
    protected boolean onceClose = false;

    protected int state;
    protected static final long LAST_SUCCEEDED = -1L;
    protected long nextTryToCommunicate = LAST_SUCCEEDED;
    protected long retryDelay = 30000; // only retry connections every 15s

    
    private static final Log log = LogFactory.getLog(AbstractAgentClientProvider.class);

    protected AbstractAgentClientProvider(ConnectionData connectionData) {
        this.agentInstanceName = connectionData.getName();
        this.agentInstanceDescription = connectionData.getDescription();
        setConnectionURL(connectionData.getUrl());
    }
    
    public String getAgentInstanceName() {
    	return agentInstanceName;
    }
    
    public void setAgentInstanceName(String name) {
        this.agentInstanceName = name;
    }

    /**
     * Command interface for executing remote operations
     */
    protected abstract class RemoteCommand {

        // Currently connects and closes connection for every operation
        public Object execute() {

            if (getLastFailed()) {
                synchronized(this) {
                    // don't permanently disable: maybe they fixed the configuration ...
                    if (nextTryToCommunicate>System.currentTimeMillis()) {
                        throw new AgentConnectionException("Agent disabled due to error communicating with remote server.");
                    }
                }
            }
            
            state = PENDING_REQUEST;
            try {
                if (isConnected()) {
                    Object result = doCommand();
                    state = NO_REQUEST;
                    return result;
                }
            } catch (UndeclaredThrowableException e) {
                handleRmiError(e);
            } catch (Exception e) {
                // assume that it's from socket time out, etc.
            }
            
            try {
                connect();
                
                Object result = doCommand();
                state = NO_REQUEST;
                return result;
            } catch (UndeclaredThrowableException e) {
                handleRmiError(e);
                throw connectionError("Can't communicate with remote server", e);
            } catch (AgentConnectionException e) {
                serviceBean  = null;
                failedConnection();
                throw e;
            } catch (Exception e) {
                serviceBean  = null;
                throw connectionError("Can't communicate with remote server", e);
            }
        }

        private AgentConnectionException connectionError(String message, Throwable cause) {
            failedConnection();
            return new AgentConnectionException(message, cause);
        }
        
        /**
         * Override to implement the command and return the result
         */
        protected abstract Object doCommand();
    }
    
    protected synchronized void failedConnection() {
        state = CONNECTION_FAILURE;
        nextTryToCommunicate = System.currentTimeMillis() + retryDelay;
    }

    /**
     * @see glassbox.client.remote.AgentClientManager#selectOperations()
     */
    public ConfigurationSummary selectConfiguration() {
        return (ConfigurationSummary)doRemoteCommand(new RemoteCommand() {
            public Object doCommand() {
                return serviceBean.configuration();
            }
        });
    }

    /**
     * @see glassbox.client.remote.AgentClientManager#selectOperations()
     */
    public Set selectOperations() {
        return (Set)doRemoteCommand(new RemoteCommand() {
            public Object doCommand() {
                return serviceBean.listOperations();
            }
        });
    }

    /**
     * @see glassbox.client.remote.AgentClientManager#reset()
     */
    public void resetStatistics() {
        doRemoteCommand(new RemoteCommand() {
            // there has to be a better way than this ugly Void return
            public Object doCommand() {
                serviceBean.reset();
                return null;
            }
        });
    }

    public int getState() {
        return state;
    }
    
    public boolean getLastFailed() {
        return state==CONNECTION_FAILURE || state==CONFIGURATION_ERROR;
    }
    
    public boolean isActive() {
    //        if(!isValidConnection()) {
    //            return false;
    //        }
        try {
            return ((Boolean)doRemoteCommand(new RemoteCommand() {
                public Object doCommand() {
                    return new Boolean(serviceBean.isActive());
                }
            })).booleanValue();        
        } catch (AgentConnectionException ae) {
            return false;
        }
    }

    public void setActive(final boolean active) {
    //       if(!isValidConnection()) { 
            doRemoteCommand(new RemoteCommand() {
                public Object doCommand() {
                    serviceBean.setActive(active);
                    return null;
                }
            });
    //       }
        }

    /**
     * @see glassbox.client.remote.AgentClientManager#findOperationAnalysis(OperationDescription)
     */
    public OperationAnalysis findOperationAnalysis(final OperationDescription operation) {
        return (OperationAnalysis)doRemoteCommand(new RemoteCommand() {
            public Object doCommand() {
                return serviceBean.analyze(operation);
            }
        });
    }

    /**
     * Execute the remote command
     */
    private Object doRemoteCommand(RemoteCommand command) {
        
        return command.execute();
        
    }

    public boolean isConnected() {
        return serviceBean!=null;
    }

    public synchronized void setConnectionURL(String URL) {
    	this.URL = URL;
    }
    
    public String getConnectionURL() {
        return URL;
    }

    public boolean isValidConnection() {
        return serviceBean!=null;
    }

    protected void handleRmiError(Exception e) {
        if (isRmiUrlError(e)) {
            log.error("Unable to connect to remote server. This is normally caused by connecting to a remote server where there are spaces in the classpath, "+
                    "due to an RMI bug. To use this connection to "+agentInstanceName+", please add the following Java option to your startup script:\n"+
                    "-Djava.rmi.server.useCodebaseOnly=true");
            log.debug("Root cause", e);
            state = CONFIGURATION_ERROR;
            serviceBean = null;
            throw new AgentConnectionException("Can't connect to remote server", e);        
        }           
    }
    
    protected boolean isRmiUrlError(Exception wrapper) {
        try {
            return testForRmiUrlError(wrapper);
        } catch (NoSuchMethodError jdk13) {
            // our best guess
            return true;
        }
    }
    
    protected boolean testForRmiUrlError(Exception wrapper) {
        Throwable c1 = wrapper.getCause();
        if (c1 instanceof UnmarshalException) {
            Throwable c2 = c1.getCause();
            return c2 instanceof MalformedURLException;
        }
        return false;
    }            

    public String toString() {
        return super.toString()+" named "+agentInstanceName+" for URL "+URL;
    }

    public String getAgentInstanceDescription() {
        return agentInstanceDescription;
    }

    public void setAgentInstanceDescription(String agentInstanceDescription) {
        this.agentInstanceDescription = agentInstanceDescription;
    }
        
}