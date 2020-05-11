/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track;

import glassbox.track.api.PerfStats;
import glassbox.track.api.StatisticsRegistry;
import glassbox.track.api.StatisticsType;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import junit.framework.TestCase;

import glassbox.track.api.StatisticsRegistryImpl;
import glassbox.track.api.StatisticsTypeImpl;

public class StatisticsRegistryTest extends TestCase {
    protected StatisticsRegistry registry;
    
    public void setUp() {
        registry = createRegistry();
    }
    
    protected StatisticsRegistry createRegistry() {
        return new StatisticsRegistryImpl();
    }
    
    public void testEmpty() {
        assertEmpty();
    }

    protected void assertEmpty() {
        assertFalse(registry.getEntries().hasNext());
        assertFalse(registry.getEntriesForType(StatisticsTypeImpl.UiRequest).hasNext());
        try {
            registry.getEntries().next();
            fail("should throw exception");
        } catch (NoSuchElementException ex) {
            // success
        }
    }
    
    public void testInsertOne() {
        PerfStats stats = registry.getPerfStats(StatisticsTypeImpl.UiRequest, "test");
        
        Iterator it = registry.getEntries(); 
        assertTrue(it.hasNext());
        assertEquals(stats, getNextValue(it));
        assertFalse(it.hasNext());
        
        it = registry.getEntriesForType(StatisticsTypeImpl.UiRequest); 
        assertEquals(stats, getNextValue(it));
        assertFalse(it.hasNext());
        
        it= registry.getEntriesForType(StatisticsTypeImpl.Database);
        assertFalse(it.hasNext());
        assertEquals(stats, registry.getPerfStats(StatisticsTypeImpl.UiRequest, "test"));
        assertNotSame(stats, registry.getPerfStats(StatisticsTypeImpl.UiRequest, "baz"));        
    }
    
    public void testClear() {
        testInsertOne();
        registry.clear();
        assertEmpty();
    }
    
    public void testThreshold() {
        long thresh = 200L;
        registry.setSlowThreshold(thresh);
        PerfStats stats = registry.getPerfStats(StatisticsTypeImpl.UiRequest, "test");
        
        assertEquals(thresh, registry.getSlowThreshold());
        assertEquals(thresh, stats.getSlowThreshold());
    }
    
    public void testInsertMixed() {
        PerfStats uStats = registry.getPerfStats(StatisticsTypeImpl.UiRequest, "test");
        PerfStats dStats = registry.getPerfStats(StatisticsTypeImpl.Database, "db");

        Iterator it = registry.getEntries(); 
        PerfStats one = getNextValue(it);
        PerfStats two = getNextValue(it);
        assertFalse(it.hasNext());
        assertTrue (uStats.equals(one) && dStats.equals(two) || uStats.equals(two) && dStats.equals(one));
        
        it = registry.getEntriesForType(StatisticsTypeImpl.UiRequest); 
        assertEquals(uStats, getNextValue(it));
        assertFalse(it.hasNext());
        
        it= registry.getEntriesForType(StatisticsTypeImpl.Database);
        assertEquals(dStats, getNextValue(it));
        assertFalse(it.hasNext());

        it= registry.getEntriesForType(StatisticsTypeImpl.DatabaseConnection);
        assertFalse(it.hasNext());
        
        assertEquals(uStats, registry.getPerfStats(StatisticsTypeImpl.UiRequest, "test"));
        assertNotSame(uStats, registry.getPerfStats(StatisticsTypeImpl.UiRequest, "baz"));        
    }

    private PerfStats getNextValue(Iterator it) {
        Entry entry = (Entry)it.next();
        return (PerfStats)entry.getValue();
    }
}
