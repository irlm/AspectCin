/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.metrics;

import glassbox.util.timing.api.TimeConversion;
import junit.framework.TestCase;

public class MetricsTest extends TestCase {
    private MetricFactory factory;
    public void setUp() {
        factory = new MetricFactory();
    }
    
    public void testZeroRateMetric() {
        String name = "requests per second";
        RateMetric metric = factory.createRateMetric(name);
        assertEquals(0, metric.getCount());
        assertEquals(0., metric.getMeanRate(), 0.);
        assertEquals(0, metric.getDuration());
        
        assertEquals(name, metric.getName());
    }
    
    // hmmm.... I can't see how it is sensible to mix summary & detailed point data
    // nor how you can sensibly have multiple threads updating a summary...
    public void testSummaryRateMetric() {
        String name = "requests per second";
        RateMetric metric = factory.createRateMetric(name);
        long seconds = 279;
        long nanos = TimeConversion.convertSecondsToNanos(seconds);
        metric.recordInterval(23, nanos);
        assertEquals(23./(double)seconds, metric.getMeanRate(), 1e-3);
        assertEquals(23, metric.getCount());
        assertEquals(nanos, metric.getDuration());
    }
}
