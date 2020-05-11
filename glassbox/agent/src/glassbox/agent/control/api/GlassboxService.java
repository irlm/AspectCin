/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 22, 2005
 */
package glassbox.agent.control.api;

import glassbox.analysis.api.ConfigurationSummary;
import glassbox.analysis.api.OperationAnalysis;
import glassbox.track.api.OperationDescription;

import java.util.Set;

/**
 * Defines the service interface for interacting with a Glassbox agent.
 * 
 * @author Ron Bodkin
 */
public interface GlassboxService {
    
    boolean isActive();   
    
    void setActive(boolean active);
    
    /**
     * Globally reset all statistics to zero.
     */
    void reset();
    
    /**
     * 
     * @return Set<OperationSummary>: all operations that have executed in the last monitoring period (e.g., hour)
     * that this service is monitoring. 
     */
    Set/*<OperationSummary>*/ listOperations();
    
    /**
     * 
     * @return Set<OperationSummary>: all <i>slow</i> operations that have executed in the last monitoring period (e.g., hour)
     * that this service is monitoring. I.e., those that took more than the threshold time (2s) more
     * than 10% of the time.
     */
    Set/*<OperationSummary>*/ listProblemOperations();
    
    OperationAnalysis analyze(OperationDescription operation);

    ConfigurationSummary configuration();
    
}
