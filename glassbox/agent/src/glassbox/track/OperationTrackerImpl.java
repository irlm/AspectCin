/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 24, 2005
 */
package glassbox.track;


import glassbox.agent.api.NotSerializable;
import glassbox.analysis.OperationAnalyzer;
import glassbox.analysis.api.OperationAnalysis;
import glassbox.analysis.api.OperationSummary;
import glassbox.track.api.*;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * 
 * @author Ron Bodkin
 */
public class OperationTrackerImpl implements OperationTracker, StatisticsRegistry {
    private StatisticsRegistry registry;
    private OperationAnalyzer analyzer;
    
    /*
     * @see OperationTracker#getOperations() 
     */
    public Set getOperations() {
        Set result = new HashSet();
        getOperations(registry, result);
        return result;
    }
    
    public Set getFailingOperations() {
        return getOperations(failingOperationsFilter);
    }
    
    public Set getProblemOperations() {
        return getOperations(problemOperationsFilter);
    }
    
    public OperationAnalysis analyze(OperationDescription key) {
        OperationPerfStats stats = getUofwStats(key, registry);
            
        return analyzer.analyze(key, stats, registry.getStartTime());
    }
    
    public Set analyzeAll() {
        Set result = new HashSet();
        Set operations = getOperations(); 
        for (Iterator it=operations.iterator(); it.hasNext();) {
            OperationSummary summary = (OperationSummary)it.next();
            result.add(analyze(summary.getOperation()));
        }
        return result;
    }
    
    private OperationPerfStats getUofwStats(OperationDescription key, StatisticsRegistry registry) {        
        if (key.getParent() != null) {
            // look up using parent stats as the registry...
            registry = getUofwStats(key.getParent(), registry);
        }
        StatisticsType type = StatisticsTypeImpl.UiRequest;
        if (key.callType() == CallDescription.REMOTE_CALL) {
            type = StatisticsTypeImpl.RemoteCall;
        }
        return (OperationPerfStats)registry.getPerfStats(type, key);
    }

    protected Set getOperations(OperationsFilter filter) {
        Set result = getOperations();
        for (Iterator it=result.iterator(); it.hasNext();) {
            if (!filter.include((OperationSummary)it.next())) { 
                it.remove();
            }
        }
        return result;
    }

    protected boolean getOperations(StatisticsRegistry registry, Set result) {
        boolean added = false;
		for (Iterator it=registry.getEntries(); it.hasNext();) {
            Entry entry = (Map.Entry)it.next();
            if (entry.getValue() instanceof OperationPerfStats) {
                OperationPerfStats  stats = (OperationPerfStats )entry.getValue();
                if (getOperations(stats, result)) {
                    added = true;
                } 
                
                if (stats.getOperationCount()>0) {
                    OperationDescriptionImpl key = (OperationDescriptionImpl)entry.getKey();
                    result.add(makeSummary(key, stats));
                    added = true;
                } 
            }
		}
        return added;
    }
    
    private OperationSummary makeSummary(OperationDescriptionImpl key, OperationPerfStats stats) {
        return analyzer.summarize(key, stats);
	}

	public long getStartTime() {
		return (registry != null ? registry.getStartTime() :  0L);
	}

	public void setRegistry(StatisticsRegistry registry) {
		this.registry = registry;
        if (analyzer != null) {
            registry.setSlowThreshold(analyzer.getSlowThresholdNanos());
        }
	}
    
    public int size() {
        return registry.size();
    }
	
	public StatisticsRegistry getRegistry() {
		return registry;
	}

    public PerfStats getPerfStats(int idx, Serializable key) {
        return registry.getPerfStats(idx, key);
    }
    
	public PerfStats getPerfStats(StatisticsType type, Serializable key) {
		return registry.getPerfStats(type, key);
	}

    public PerfStats removePerfStats(StatisticsType type, Serializable key) {
        return registry.removePerfStats(type, key);
    }

	public Iterator getEntries() {
        return registry.getEntries();
    }

    public int getCountForType(StatisticsType type) {
        return registry.getCountForType(type);
    }

    public int getDirectCountForType(StatisticsType type) {
        return registry.getCountForType(type);
    }

    public Iterator getEntriesForType(StatisticsType type) {
		return registry.getEntriesForType(type);
	}

    public Iterator getDirectEntriesForType(StatisticsType type) {
        return registry.getEntriesForType(type);
    }
    
	public void clear() {
		registry.clear();
	}
    
    public OperationAnalyzer getOperationAnalyzer() {
        return analyzer;
    }
    
    public long getSlowThreshold() {
        return analyzer.getSlowThresholdNanos();
    }

    public void setSlowThreshold(long slowThreshold) {
        analyzer.setSlowThresholdNanos(slowThreshold);
        registry.setSlowThreshold(slowThreshold);
        
    }

    public void setOperationAnalyzer(OperationAnalyzer analyzer) {
        this.analyzer = analyzer;
        if (registry != null) {
            registry.setSlowThreshold(analyzer.getSlowThresholdNanos());
        }
    }
    
    protected interface OperationsFilter extends NotSerializable {
        boolean include(OperationSummary summary);
    }
    protected final transient OperationsFilter allOperationsFilter = new OperationsFilter(){
        public boolean include(OperationSummary summary) {
            return true;
        }
    };
    protected final transient OperationsFilter problemOperationsFilter = new OperationsFilter(){
        public boolean include(OperationSummary summary) {
            return summary.isProblem();
        }
    };
    protected final transient OperationsFilter failingOperationsFilter = new OperationsFilter(){
        public boolean include(OperationSummary summary) {
            return summary.isFailing();
        }
    };
    
    public StringBuffer dump(StringBuffer buffer, int depth) {
        return registry.dump(buffer, depth);
    }

    public StatisticsRegistry cloneRegistry() {
        throw new UnsupportedOperationException();
    }
    
    private static final long serialVersionUID = 1;
}
