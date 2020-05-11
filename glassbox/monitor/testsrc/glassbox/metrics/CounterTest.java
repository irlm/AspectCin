/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.metrics;

import junit.framework.TestCase;

public class CounterTest extends TestCase {
    private MetricFactory factory;
    public void setUp() {
        factory = new MetricFactory();
    }    
    
    public void testIncrementIntCounter() {
        String name;
        IntMetric intMetric = factory.createIntCounter(name="simpleCounter");
        assertEquals(0, intMetric.getValue());
        assertEquals(name, intMetric.getName());
        assertEquals(Integer.TYPE, intMetric.getType());
        assertEquals(Metric.COUNT, intMetric.getDimensions());
        assertEquals(1, intMetric.increment());
        //assertEquals(1, factory.getMetric(name).getValue());
    }
    
    public void testIncrementLongCounter() {
        LongMetric longMetric = factory.createLongCounter("");
        assertEquals(0, longMetric.getValue());
        assertEquals("", longMetric.getName());
        assertEquals(Long.TYPE, longMetric.getType());
        assertEquals(Metric.COUNT, longMetric.getDimensions());
        
        assertEquals(1, longMetric.increment());
    }
    
    public void testUpdateLongCounter() {
        LongMetric longMetric = factory.createLongCounter("foo", Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, longMetric.getValue());
        assertEquals(1L+(long)Integer.MAX_VALUE, longMetric.increment());
        assertEquals(1L+2L*(long)Integer.MAX_VALUE, longMetric.incrementBy(Integer.MAX_VALUE));
        assertEquals("foo", longMetric.getName());
    }

    //goal: prove thread safety ... not just statistical evidence?
    //extend past AOP approach to force all interleavings
    //on a single CPU machine, this test almost never sees an error if the method is totally unsynchronized
    //splitting an add into a long is of course rare
    public void testAtomicIncrementLongCounter() throws Exception {
        final LongMetric counter = factory.createLongCounter("foo");
        final int nThreads = 5;
        final int nLoops = 1000;

        Thread thread[] = new Thread[nThreads];
        for (int i=0; i<nThreads; i++) {
            thread[i] = new Thread() {
                public void run() {
                    for (int i=0; i<nLoops; i++) {
                        counter.incrementBy(Integer.MAX_VALUE);                                                
                    }
                }
            };
        }        
        for (int i=0; i<nThreads; i++) {
            thread[i].run();
        }
        
        for (int i=0; i<nThreads; i++) {
            thread[i].join();
        }
        assertEquals((long)(nThreads * nLoops) * (long)Integer.MAX_VALUE, counter.getValue());
    } 
    
}
