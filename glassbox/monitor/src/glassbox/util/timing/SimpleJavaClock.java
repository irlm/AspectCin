/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.timing;

import glassbox.track.api.UsageTrackingInfo;
import glassbox.util.timing.api.TimeConversion;

public class SimpleJavaClock implements Clock {

    public long getTime() {
        return getTimeQuickly(); 
    }

    public long getTimeQuickly() {
        return System.currentTimeMillis() * TimeConversion.NANOSECONDS_PER_MILLISECOND; 
    }

    // is this safe?
    public UsageTrackingInfo getUsage() {
        return null;
    }
    
    public UsageTrackingInfo getZeroUsage() {
        return null;
    }

    private static final long serialVersionUID = 1L;

}
