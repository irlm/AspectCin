/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.metrics;

public class MetricFactory {
        
    public IntMetric createIntCounter(String name) {
        return new IntCounter(name);
    }

    public LongMetric createLongCounter(String name) {
        return new LongCounter(name);
    }

    public LongMetric createLongCounter(String name, int initValue) {
        LongCounter counter = new LongCounter(name);
        counter.incrementBy(initValue);
        return counter;
    }

    public RateMetric createRateMetric(String name) {
        return new RateMetricImp(name);
    }

}
