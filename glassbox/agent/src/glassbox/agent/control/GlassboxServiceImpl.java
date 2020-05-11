/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 22, 2005
 */
package glassbox.agent.control;

import glassbox.agent.control.api.GlassboxService;
import glassbox.analysis.api.ConfigurationSummary;
import glassbox.analysis.api.OperationAnalysis;
import glassbox.config.AspectConfigurationException;
import glassbox.monitor.AbstractMonitor;
import glassbox.track.OperationTracker;
import glassbox.track.api.OperationDescription;

import java.util.Set;

/**
 * The listener exposes an API for remote clients. 
 * 
 * @author Ron Bodkin
 */
//RBTODO: address proper validation when Spring framework code is constructing the context
// could adapt validator to use InitializingBean & delegate validate to call afterPropertiesSet()

public class GlassboxServiceImpl implements GlassboxService/*, ValidateConfiguration*/ {
    private long startupTime;
    private OperationTracker operationTracker;
    private ConfigurationSummary configurationSummary;
    
    public boolean isActive() {
        return !AbstractMonitor.getAllDisabled();
    }
    
    public void reset() {
        operationTracker.clear();
    }
    
    public void setActive(boolean active) {
        // this will change to interact with the monitor aspects, or else the monitor aspects
        // will watch a global enabled flag...
		//logInfo("Glassbox service now "+(active ? "" : "not ")+"active.");
        AbstractMonitor.setAllDisabled(!active);
        startupTime = glassbox.util.timing.ClockManager.getTime();
    }
    
    public void setOperationTracker(OperationTracker operationsMonitor) {
        this.operationTracker = operationsMonitor;
    }
    
    public void setConfigurationSummary(ConfigurationSummary configurationSummary) {
        this.configurationSummary = configurationSummary;
    }
    
    public long getUptime() {
        long now = glassbox.util.timing.ClockManager.getTime();
        long upTime = now - startupTime;
        return upTime;
    }

    /* (non-Javadoc)
     * @see glassbox.agent.control.GlassboxServiceImpl#getOperations()
     */
    public Set listOperations() {
        return operationTracker.getOperations();
    }

    /* (non-Javadoc)
     * @see glassbox.agent.control.GlassboxServiceImpl#getOperations()
     */
    public Set listProblemOperations() {
        return operationTracker.getProblemOperations();
    }

    /* (non-Javadoc)
     * @see glassbox.config.ValidateConfiguration#validate()
     */
    public void validate() throws AspectConfigurationException {
        if (operationTracker == null) {
            throw new AspectConfigurationException("No operations tracker defined");
        }
    }

    public OperationAnalysis analyze(OperationDescription operation) {
        return operationTracker.analyze(operation);
    }
    
    public ConfigurationSummary configuration() {
    	return configurationSummary; 	
    }
    
    static final private long serialVersionUID = 1L;
}

