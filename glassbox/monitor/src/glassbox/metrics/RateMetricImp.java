/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.metrics;

import glassbox.util.timing.api.TimeConversion;

public class RateMetricImp extends AbstractMetric implements RateMetric {
    
    private long count;
    private long duration;
    
    public RateMetricImp(String name) {
        super(name);
    }

    public Object getDimensions() {
        return "count/time"; //XXX?
    }

    public Object getType() {
        return Long.TYPE;
    }

    public long getCount() {
        return count;
    }

    public long getDuration() {
        return duration;
    }

    public double getMeanRate() {
        if (count == 0) {
            return 0.;
        }
        return (double)count / (double)TimeConversion.convertNanosToSeconds(duration);
    }

    public synchronized void recordInterval(long count, long duration) {
        this.count += count;
        this.duration += duration;
    }

}
