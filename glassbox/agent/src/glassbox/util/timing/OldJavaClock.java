/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 24, 2005
 */
package glassbox.util.timing;

import edu.emory.mathcs.backport.java.util.concurrent.helpers.Utils;
import glassbox.track.api.UsageTrackingInfo;
import glassbox.util.timing.api.NoCpuUsageTrackingInfoImpl;
import glassbox.util.timing.api.TimeConversion;


/**
 * 
 * Updated to use the JSR 166 backport
 * 
 * @author Ron Bodkin
 */
public class OldJavaClock implements Clock {
    
    public static final long SCALING_FACTOR = TimeConversion.NANOSECONDS_PER_MILLISECOND;
    public long EPOCH_DELTA = System.currentTimeMillis() * SCALING_FACTOR - Utils.nanoTime();

    public long getTime() {
        return Utils.nanoTime() + EPOCH_DELTA;
    }

    public UsageTrackingInfo getUsage() {
        return new NoCpuUsageTrackingInfoImpl();
    }
    
    public UsageTrackingInfo getZeroUsage() {
        return new NoCpuUsageTrackingInfoImpl(0L);
    }
    
    public long getTimeQuickly() {
        return System.currentTimeMillis()*SCALING_FACTOR;
    }
    
    private static final long serialVersionUID = 1L;
}
