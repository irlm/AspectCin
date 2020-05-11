/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.timing;

import glassbox.track.api.UsageTrackingInfo;
import glassbox.util.timing.api.CpuUsageTrackingInfoImpl;
import glassbox.util.timing.api.TimeConversion;

public class Java5Clock implements Clock {
    
    public static final long SCALING_FACTOR = TimeConversion.NANOSECONDS_PER_MILLISECOND;
    public static final long EPOCH_DELTA = System.currentTimeMillis() * SCALING_FACTOR - System.nanoTime();

    public UsageTrackingInfo getUsage() {
        return new CpuUsageTrackingInfoImpl();
    }

    public UsageTrackingInfo getZeroUsage() {
        return new CpuUsageTrackingInfoImpl(0L, 0L, 0L);
    }
    
    public long getTime() {
        return System.nanoTime() + EPOCH_DELTA;
    }
    
    public long getTimeQuickly() {
        return System.currentTimeMillis()*SCALING_FACTOR;
    }
    
    private static final long serialVersionUID = 1L;

}
