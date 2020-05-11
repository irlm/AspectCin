/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.metrics;

public interface RateMetric extends Metric {

    /** count (individuals) */
    long getCount();

    /** in nanoseconds */
    long getDuration();
    
    /** mean overall rate since the start of time: count/(duration * 10^9) */
    double getMeanRate();

    void recordInterval(long count, long duration);    
}
