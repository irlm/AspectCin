/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.timing.api;

import glassbox.track.api.UsageTrackingInfo;
import glassbox.util.timing.Clock;
import glassbox.util.timing.ClockManager;

public class NoCpuUsageTrackingInfoImpl extends AbstractUsageTrackingInfoImpl {

    protected long eventTime = Clock.UNDEFINED_TIME;
    
    public NoCpuUsageTrackingInfoImpl() {
        eventTime = ClockManager.getTime();
    }
 
    public NoCpuUsageTrackingInfoImpl(long clockTime) {
        eventTime = clockTime;
    }
    
    public long getEventCPUTime() {
        return Clock.UNDEFINED_TIME;
    }
    public long getEventTime() {
        return eventTime;
    }
    public long getEventUserCPUTime() {
        return Clock.UNDEFINED_TIME;
    }
    public void setEventCPUTime(long eventCPUTime) {
        //throw new OperationNotSupportedException();
    }
    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }
    public void setEventUserCPUTime(long eventUserCPUTime) {
        //throw new OperationNotSupportedException();
    }

    public void subtract(UsageTrackingInfo arg) {
        eventTime -= arg.getEventTime();
    }

    public void add(UsageTrackingInfo arg) {
        eventTime += arg.getEventTime();
    }
    
    public void set(UsageTrackingInfo arg) {
        eventTime = arg.getEventTime();
    }
    
    public void reset() {
        eventTime = 0;
    }
        
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if (!(obj instanceof NoCpuUsageTrackingInfoImpl)) {
            return false;
        }
        NoCpuUsageTrackingInfoImpl other = (NoCpuUsageTrackingInfoImpl) obj;
        return (this.eventTime == other.getEventTime());
    }
    
    public int hashCode() {
        // not a great hashing function
        return (int)(eventTime*253 + 0x7fcd);
    }
    
    public String toString() {
        return TimeConversion.formatTime(eventTime);
    }
    
    static final private long serialVersionUID = 2;
}
