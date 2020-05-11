/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 29, 2005
 */
package glassbox.track;

import glassbox.track.api.OperationDescription;

import java.util.Set;

import glassbox.analysis.api.OperationAnalysis;

/**
 * 
 * @author Ron Bodkin
 */
public interface OperationTracker {
    /**
     * 
     * @return all operations that have executed in the last monitoring period (e.g., hour)
     * that this service is monitoring. We return an instance of OperationDescriptor.
     */
    Set getOperations();
    Set getFailingOperations();
    Set getProblemOperations();

    // we need to get all operations, then filter to support plugin extensions...
    public pointcut gettingOperations() : execution(public Set OperationTracker.getOperations());
    
    OperationAnalysis analyze(OperationDescription key);
    
    /**
     * Globally clear all statistics to zero.
     */
    void clear();
    
    long getStartTime();
        
    Set/*<OperationAnalysis>*/ analyzeAll();
}
