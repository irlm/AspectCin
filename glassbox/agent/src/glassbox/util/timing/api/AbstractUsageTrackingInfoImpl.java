/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.timing.api;

import glassbox.track.api.UsageTrackingInfo;

public abstract class AbstractUsageTrackingInfoImpl implements UsageTrackingInfo {

    public int compareTo(Object other) {
        UsageTrackingInfo otherUsage = (UsageTrackingInfo)other;
        long delta = getEventTime() - otherUsage.getEventTime();
        return delta<0 ? -1 : (delta>0 ? 1 : 0);
    }

    public UsageTrackingInfo klone() {
        try {
            return (UsageTrackingInfo)clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("clone failure?", e);
        }
    }
    
    static final private long serialVersionUID = 1;
}
