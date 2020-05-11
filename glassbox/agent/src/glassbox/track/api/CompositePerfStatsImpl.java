/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

import glassbox.response.Response;
import glassbox.util.timing.Clock;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public class CompositePerfStatsImpl extends PerfStatsImpl implements CompositePerfStats {

    protected StatisticsRegistry registry = new StatisticsRegistryImpl(this);
    
    protected PerfStatsImpl simpleStatsHeld[];

    private static final long serialVersionUID = 2;

    public CompositePerfStatsImpl() {
        this(false);
    }

    public CompositePerfStatsImpl(boolean virtuallyContained) {
        super(virtuallyContained);
    }

    public CompositePerfStatsImpl(PerfStats stats) {
        super(stats);
    }

    protected Collection getUsedStats() {
        return getUsedStatsMap().keySet();
    }

    protected Map getUsedStatsMap() {
        return ((PerOperationStruct)(perOperationData.get())).usedStats;
    }
    
    protected synchronized PerOperationStruct updateSummaries(PerOperationStruct totals, Response response) {
        for (Iterator iter = getUsedStats().iterator(); iter.hasNext();) {
            PerfStatsImpl stats = (PerfStatsImpl) iter.next();
            // time stats don't really belong here: they're an orthogonal view, not a mutually exclusive category
            if (!(stats instanceof TreeTimeStats)) {
                PerOperationStruct childTotals = stats.updateSummaries(response);
                totals.add(childTotals);
            }
        }
        if (simpleStatsHeld != null) {
            updateSimpleSummaries(totals, response);
        }
        return updateThisNodeSummaries(totals, response);
    }
    
    protected void updateSimpleSummaries(PerOperationStruct totals, Response response) {
        for (int i=0; i<simpleStatsHeld.length; i++) {
            PerfStatsImpl simpleStats = (PerfStatsImpl)(simpleStatsHeld[i]);
            simpleStats.updateSummaries(simpleStats.perOperationData.get(), response);
        }            
    }    
       
    protected void resetOperationData() {
        // we have to reset the children before calling super.reset, which clears the set of used stats  
        for (Iterator iter = getUsedStats().iterator(); iter.hasNext();) {
            PerfStatsImpl stats = (PerfStatsImpl) iter.next();
            stats.resetOperationData();
        }
        if (simpleStatsHeld != null) {
            for (int i=0; i<simpleStatsHeld.length; i++) {
                simpleStatsHeld[i].resetOperationData();
            }            
        }
        super.resetOperationData();
    }
    
    public void setSlowThreshold(long slowThreshold) {
        super.setSlowThreshold(slowThreshold);
        if (registry != null) {
            // special case logic: we don't keep a registry for leaf nodes...
            registry.setSlowThreshold(slowThreshold);
        }
        if (simpleStatsHeld != null) {
            for(int i=0; i<simpleStatsHeld.length; i++) {
                simpleStatsHeld[i].setSlowThreshold(slowThreshold);            
            }
        }
    }

    public boolean hasUofWFailure() {
        if (super.hasUofWFailure()) {
            return true;
        }
        for (Iterator iter = getUsedStats().iterator(); iter.hasNext();) {
            PerfStatsImpl stats = (PerfStatsImpl) iter.next();
            if (stats.hasUofWFailure()) {
                return true;
            }
        }
        return false;
    }

    public PerfStats getPerfStats(int idx, Serializable key) {
        PerfStats stats = registry.getPerfStats(idx, key);
        
        // this is ugly: we have to handle the null case for when the client code calls getPerfStats
        if (perOperationData != null) {
            getUsedStatsMap().put(stats, StatisticsTypeImpl.getStatsType(idx));
        }
        return stats;
    }
    
    public PerfStats getPerfStats(StatisticsType type, Serializable key) {
        //System.out.println("  Looking up "+key+" in "+this+"/ "+System.identityHashCode(this));
        //return registry.getPerfStats(type, key);
        PerfStats ret = registry.getPerfStats(type, key);
        
        // this is ugly: we have to handle the null case for when the client code calls getPerfStats
        if (perOperationData != null) {
            getUsedStatsMap().put(ret, type);
        }
        //System.out.println("  Returning "+ret+"/ "+System.identityHashCode(ret));        
        return ret;
    }
    
    public PerfStats removePerfStats(StatisticsType type, Serializable key) {
        return registry.removePerfStats(type, key);
    }
    
    public void clear() {
        if (registry==null) return;
        registry.clear();
    }

    public long getStartTime() {
        if (registry==null) return Clock.UNDEFINED_TIME;
        return registry.getStartTime();
    }

    public int getDirectCountForType(StatisticsType type) {
        if (registry == null) return 0;
        return registry.getDirectCountForType(type);
    }
    
    public Iterator getDirectEntriesForType(StatisticsType type) {
        if (registry == null) return getChildren().iterator();
        return registry.getDirectEntriesForType(type);
    }
    
    public int getCountForType(StatisticsType type) {
        if (registry == null) return 0;
        return registry.getCountForType(type);
    }
    
    public Iterator getEntriesForType(StatisticsType type) {
        if (registry == null) return getChildren().iterator();
        return registry.getEntriesForType(type);
    }
    
    public Collection getChildren() {
        Collection result = new ArrayList();
        
        if (registry == null) return result;
        
        for (Iterator it = registry.getEntries(); it.hasNext();) {            
            Entry entry = (Entry)it.next();
            result.add(entry.getValue());
        }
        return result;
    }
    
    public Iterator getEntries() {
        if (registry == null) return getChildren().iterator();
        return registry.getEntries();
    }

    public int size() {
        return registry.size();
    }
    
    /**
     * Helper method for subclasses that create special summary perf stats that aren't part of the standard tree
     * structure that's rolled up
     */
    protected PerfStatsImpl makeSimplePerfStats() {        
        PerfStatsImpl childStats = (PerfStatsImpl)PrivateStatisticsType.instance.makePerfStats(null);
        childStats.setSlowThreshold(getSlowThreshold());
        
        addChildStats(childStats);
        
        return childStats;
    }
    
    protected void addChildStats(PerfStatsImpl childStats) {
        if (simpleStatsHeld==null) {
            simpleStatsHeld = new PerfStatsImpl[1];
        } else {
            PerfStatsImpl[] old = simpleStatsHeld;
            simpleStatsHeld = new PerfStatsImpl[old.length+1];
            for (int i=0; i<old.length; i++) {
                simpleStatsHeld[i] = old[i];
            }
        }
        simpleStatsHeld[simpleStatsHeld.length-1] = childStats;
    }

    public long getFirstEventTime() {
        long firstTime = super.getFirstEventTime();
        
        if (isVirtuallyContained()) {
            for (Iterator it = getEntries(); it.hasNext();) {
                Entry entry = (Entry)it.next();
                long entryTime = ((PerfStats)entry.getValue()).getFirstEventTime();
                if (entryTime != Clock.UNDEFINED_TIME) {
                    if (firstTime == Clock.UNDEFINED_TIME) {
                        firstTime = entryTime;
                    } else {
                        firstTime = Math.min(firstTime, entryTime);
                    }
                }                
            }
        }
        return firstTime;
    }

    public long getLastEventTime() {
        long lastTime = super.getLastEventTime();
        
        if (isVirtuallyContained()) {
            for (Iterator it = getEntries(); it.hasNext();) {
                Entry entry = (Entry)it.next();
                // hack: undefined time is less than anything...
                lastTime = Math.max(lastTime, ((PerfStats)entry.getValue()).getLastEventTime());
            }
        }
        return lastTime;
    }

    public StringBuffer dump(StringBuffer buffer, int depth) {
        super.dump(buffer, depth);
        buffer.append('\n');      
        return registry.dump(buffer, depth+1);
    }    

    public StatisticsRegistry cloneRegistry() {
        return (StatisticsRegistry)klone();
    }
    
    public PerfStats klone() {
        CompositePerfStatsImpl copy = new CompositePerfStatsImpl(this);
        return cloneInto(copy);
    }
    
    protected PerfStats cloneInto(CompositePerfStatsImpl copy) {
        copy.registry = copy.registry.cloneRegistry();
        if (simpleStatsHeld != null) {
            copy.simpleStatsHeld = new PerfStatsImpl[simpleStatsHeld.length];
            for (int i=0; i<simpleStatsHeld.length; i++) {
                copy.simpleStatsHeld[i] = (PerfStatsImpl)simpleStatsHeld[i].klone();
            }
        }

        return copy;
    }
}
