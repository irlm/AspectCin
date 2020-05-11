/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 */
package glassbox.test;

import glassbox.monitor.AbstractMonitor;
import glassbox.response.Response;
import glassbox.summary.StatsSummarizer;
import glassbox.track.ThreadStats;
import glassbox.track.api.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Testing utility to hook up a performance statistics instance for this object.
 * Just as easy as hooking up a mock object... 
 */
public aspect TestMonitor extends AbstractMonitor {
    /** Set up statistics to allow testing a new operation that creates a unit of work, i.e., a fully empty stack */ 
    public static ThreadStats setUpStatsForUofW(AbstractMonitor uofwMonitor) {
        StatisticsRegistry registry = new StatisticsRegistryImpl();
        registry.setSlowThreshold(TimingTestHelper.SLOW_THRESHOLD);
        ThreadStats threadStats = new ThreadStats();
        threadStats.setRegistry(registry);
        glassbox.track.api.OperationPerfStatsImpl.setInitialSkipCount(0);
        StatsSummarizer summarizer = new StatsSummarizer(); 
        summarizer.setThreadStats(threadStats);
        List listeners = new ArrayList();
        listeners.add(summarizer);
        responseFactory.setListeners(listeners);        
        return threadStats;
    }
    
    /** Set up statistics to allow testing nested statistics, creating a dummy operation to allow holding contained statistics*/ 
	public static ThreadStats setUpStats() {
        ThreadStats threadStats = setUpStatsForUofW(null);
        OperationPerfStatsImpl scenarioStats = new OperationPerfStatsImpl();
        //scenarioStats.decrementSkipCount();
        threadStats.push(scenarioStats);
		return threadStats;
	}

    public String getLayer() {
        return Response.UI_CONTROLLER;
    }
        
	declare warning: call(* TestMonitor.*(..)) && !within(TestCase+) && !within(TestMonitor):
		"make sure to only call TestMonitor from testing code!";
}
 