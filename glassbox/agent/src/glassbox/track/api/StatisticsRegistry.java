/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 30, 2005
 */
package glassbox.track.api;

import java.io.Serializable;
import java.util.Iterator;


/**
 * 
 * At this point, we only guarantee membership checking by the equals operator. However, for efficiency and to avoid
 * issues with, e.g., application objects having state checks, we might change to use an IdentityMap instead. The
 * API for the registry should remain the same regardless.
 * 
 * @author Ron Bodkin
 */
public interface StatisticsRegistry {
    /**
     * Return a performance statistics object held within this registry. 
     * Creates a new one if none exists.
     * The type of statistics object (leaf or composite) is determiend by the type parameter. 
     * 
     * @param type The type of statistics object.
     * @param key The key for the specific object entry.
     * @return The performance statistics object. Never null.
     */
    PerfStats getPerfStats(StatisticsType type, Serializable key);
    
    /**
     * Returns a performance statistics object based on index, rather than type.
     * @see #getPerfStats(StatisticsType, Serializable)
     */
    PerfStats getPerfStats(int typeIndex, Serializable key);

    /**
     * 
     * @return previous value
     */
    PerfStats removePerfStats(StatisticsType type, Serializable key);

    /**
     * Clear all the stored performance statistics (whether composite or not) in this registry.
     */
    void clear();

    /**
     * Get time since creation or last clear(), whichever is more recent
     */
    long getStartTime();

    /**
     * Get the number of direct entries of the given type of statistic held as immediate children of this statistics registry.
     */
    public int getCountForType(StatisticsType type);
        
    public int getDirectCountForType(StatisticsType type);

    /**
     * Get all entries (of type Map.Entry) of the given type of statistic held as immediate children of this statistics registry.
     * The map key depends on the statistics type. The value is a PerfStats instance.
     * 
     * @param type the type of statistic.
     * @return iterator over the entries
     */
    Iterator getEntriesForType(StatisticsType type);
    
    /** 
     * Returns only immediate children of the given type. 
     * getEntriesForType also returns indirect children. 
     */
    public Iterator getDirectEntriesForType(StatisticsType type);
 
    /**
     * Get all entries (of type Map.Entry) held as immediate children of this of this statistics registry.
     * The map key depends on the statistics type. The value is a PerfStats instance.
     * 
     * @return iterator over the entries
     */
    Iterator getEntries();
    
    /**
     * 
     * @return size of the registry (useful for management)
     */
    int size();
    
    long getSlowThreshold();

    /**
     * Sets the threshold for slow operations. This affects new performance stats created within the registry, but does not change the threshold for existing
     * 
     * @param slowThreshold
     */
    void setSlowThreshold(long slowThreshold);

    /** debug method */
    StringBuffer dump(StringBuffer buffer, int depth);    
    
    StatisticsRegistry cloneRegistry();
}
