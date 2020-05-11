/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.remote;

import glassbox.analysis.api.ConfigurationSummary;
import glassbox.analysis.api.OperationAnalysis;
import glassbox.track.api.OperationDescription;

import java.util.Set;

public interface AgentClientProvider {

    /**
     * Requests the analysis engine to produce a list of all client request
     * operations
     */
    public abstract Set selectOperations();
    
    /**
     * Requests the analysis engine to produce a Configurations object.
     * 
     */
    public abstract ConfigurationSummary selectConfiguration();

    /**
     * Requests the analysis engine to clear all statistics gathered so far
     */
    public abstract void resetStatistics();

    /**
     * Requests this monitoring agent to enable or disable all monitoring globally.
     * In future, we will allow setting a threshold of overhead and/or control over specific agents.
     */
    public abstract void setActive(boolean active);

    /**
     * Requests the analysis engine to produce an analysis of the provided
     * operation
     */
    public abstract OperationAnalysis findOperationAnalysis(final OperationDescription operation);
    
    public abstract String getAgentInstanceName();
    
    public abstract String getAgentInstanceDescription();

    /** either no last request or last request succeeded, and no pending requests */ 
    public static final int NO_REQUEST = 0;
    /** last request failed to connect */ 
    public static final int CONNECTION_FAILURE = 1;
    /** configuration error: will never be able to connect (e.g., bad URL) */ 
    public static final int CONFIGURATION_ERROR = 2;
    /** either no last request or last request succeeded, and there is a pending request */ 
    public static final int PENDING_REQUEST = 3;
    public abstract int getState();
    public abstract boolean getLastFailed();
    
    /** warning: blocking remote call. Better to use getState() or getLastFailed() */
    public abstract boolean isActive();
    
    public String getConnectionURL();
}
