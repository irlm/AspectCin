/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;


import glassbox.util.concurrent.ConcurrentFactory;
import glassbox.util.concurrent.IConcurrentMap;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;


public class StatisticsRegistryImpl implements StatisticsRegistry {

    // n.b. this map holds on to the stats object by key without allowing GC
    // to scale properly, we will need to summarize old stats & clear the maps periodically
    // it's important to use small keys like Strings NOT the actual objects, since the
    // keys aren't GC'd
    protected Map/*<Object, Stats>*/ data[] = initHashMap();

    private long startTime = System.currentTimeMillis();

    private long slowThreshold;
    
    /** 
     * If this registry is a delegate for another implementor of the interface, track the
     * root implementor
     */
    private StatisticsRegistry rootRegistry;
    
    public static final Serializable NULL_KEY = new NullKey();
    
    // default constructor: not a delegated registry
    public StatisticsRegistryImpl() {
        this.rootRegistry = this;
    }
    
    public StatisticsRegistryImpl(StatisticsRegistry rootRegistry) {
        this.rootRegistry = rootRegistry;
    }
    
    public StatisticsRegistry cloneRegistry() {
        StatisticsRegistryImpl copy = new StatisticsRegistryImpl();
        for (int i=0; i<data.length; i++) {
            for (Iterator it=data[i].entrySet().iterator(); it.hasNext();) {
                Entry entry = (Entry)it.next();
                PerfStats stats = (PerfStats)entry.getValue();
                copy.data[i].put(entry.getKey(), stats.klone());
            }
        }
        return copy;
//        try {
//            ByteArrayOutputStream bas = new ByteArrayOutputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(bas);
//            oos.writeObject(original);
//            oos.close();
//            
//            ByteArrayInputStream bis = new ByteArrayInputStream(bas.toByteArray());
//            ObjectInputStream ois = new ObjectInputStream(bis);
//            return (StatisticsRegistry)ois.readObject();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return null;
    }
    
    public PerfStats getPerfStats(int idx, Serializable key) {
        return getPerfStats(StatisticsTypeImpl.getStatsType(idx), key);
    }
    
    // random object whose name is null that isn't equal to the string "null"
    public final static class NullKey implements Serializable {
        public String toString() {
            return "(null)";
        }
        private NullKey() {}
        public boolean equals(Object obj) {
            return obj instanceof NullKey;
        }        
        public int hashCode() {
            return NullKey.class.hashCode() * 2;
        }
        private static final long serialVersionUID = 1L;
    };
    
    public PerfStats getPerfStats(StatisticsType type, Serializable key) {
        PerfStats stats;
        IConcurrentMap typeMap = (IConcurrentMap)data[type.getIndex()];
        if (key==null) {
            if (type.getIndex() != StatisticsType.DatabaseConnectionIdx) {
                RuntimeException rte = new IllegalArgumentException("should pass null key ONLY for database connection stats, statistics type is "+type.getIndex());
                logError("improper stats use", rte);
            }
            
            key = NULL_KEY;
        }
        stats = (PerfStats)typeMap.get(key);
        if (stats == null) {
            stats = createPerfStats(typeMap, type, key);
        }
        return stats;
    }
    
    public PerfStats removePerfStats(StatisticsType type, Serializable key) {
        IConcurrentMap typeMap = (IConcurrentMap)data[type.getIndex()];
        if (key==null) {
            key = NULL_KEY;
        }
        return (PerfStats)typeMap.remove(key);
    }
        
    protected PerfStats createPerfStats(IConcurrentMap typeMap, StatisticsType type, Serializable key) {
        PerfStats stats = type.makePerfStats(key);
        
        stats.setSlowThreshold(getSlowThreshold());

        PerfStats inMap = (PerfStats)typeMap.putIfAbsent(key, stats);
        if (inMap == null) {
            // no previous value
            ((PerfStatsImpl)stats).setOwner(this);
            
            if (isDebugEnabled()) {
                typeCount[stats.getType().getIndex()]++;
                depthCount[getDepth(stats)]++;
            }
            
            if (typeMap.size() > 200 && (typeMap.size() % 100) == 0) {
                logInfo("type map for "+(rootRegistry==null ? "root" : rootRegistry.toString())+" has grown to size "+typeMap.size()+", "+type);
                if (isDebugEnabled()) {
                    dumpCounts();
                }
            }
            return stats;
        }
        return inMap;
    }
    
    public static int getDepth(PerfStats stats) {
        int depth = 0;
        while (stats.getParent() != null) {
            depth++;
            stats = stats.getParent();
        }
        return depth;
    }

    private void dumpCounts() {
        logDebug("type counts:");
        dump(typeCount);
        logDebug("depth counts:");
        dump(depthCount);
    }
    
    private void dump(int counts[]) {
        int end=counts.length-1;
        for (; counts[end]==0 && end>=0; end--) {
        }
        for (int i=0; i<=end; i++) {
            logDebug(i+": "+counts[i]);
        }
    }
    
    private static int typeCount[] = new int[StatisticsTypeImpl.getAllTypes().size()];
    private static int depthCount[] = new int[100];
    
	public void clear() {
		data = initHashMap();
		startTime = System.currentTimeMillis();
        glassbox.track.api.OperationPerfStatsImpl.setInitialSkipCount(0); // don't keep skipping... might cause problems in tests
	}

	public long getStartTime() {
		return startTime;
	}

    public int getDirectCountForType(StatisticsType type) {
        return (data[type.getIndex()]).entrySet().size();
    }
    
	public Iterator getDirectEntriesForType(StatisticsType type) {
        return data[type.getIndex()].entrySet().iterator();
	}
    
    public Iterator getEntries() {
        return new ArrayOfMapIterator(data);
    }
    
    public int size() {
        int sz = 0;
        for (int i=0; i<data.length; i++) {
            sz += data[i].size();
        }
        return sz;
    }
    
    public long getSlowThreshold() {
        return slowThreshold;
    }
    
    public void setSlowThreshold(long slowThreshold) {
        this.slowThreshold = slowThreshold;
    }
    
    private static Map[] initHashMap() {
        List allTypes = StatisticsTypeImpl.getAllTypes();
        Map[] typeMaps = new Map[allTypes.size()];
        for (Iterator it=allTypes.iterator(); it.hasNext();) {
            StatisticsType type = (StatisticsType)it.next();
            typeMaps[type.getIndex()] = makeMap(0);
        }
        return typeMaps;
    }
    
    protected static Map makeMap(int sz) {
        // reduce concurrency in favor of smaller memory use...
        return ConcurrentFactory.makeConcurrentMap(sz, 0.75f, 4);
    }
    
    public StringBuffer dump(StringBuffer buffer, int depth) {
        String spaces = "                                                                                     ".substring(0, 2*depth);
        for (int i=0; i<data.length; i++) {
            buffer.append(spaces+StatisticsTypeImpl.getStatsType(i)+":\n");
            for (Iterator cit = data[i].entrySet().iterator(); cit.hasNext();) {
                Entry centry = (Entry)cit.next();
                buffer.append(spaces+"  "+centry.getKey()+":");
                PerfStats stats = (PerfStats)centry.getValue();
                stats.dump(buffer, depth+1);
            }
        }
        return buffer;
    }
    
    public StatisticsRegistry getContainer() {
        return rootRegistry;
    }
    
    public int getCountForType(final StatisticsType type) {
        // inefficient, but not used
        Iterator it = getEntriesForType(type);
        int count = 0;
        while (it.hasNext()) {
            count++;
        }
        return count;
    }
    
    public Iterator getEntriesForType(StatisticsType type) {
        return new IteratorOfStatsEntryIterator(type, getEntries());
    }
    
    private static final long serialVersionUID = 1;
}
