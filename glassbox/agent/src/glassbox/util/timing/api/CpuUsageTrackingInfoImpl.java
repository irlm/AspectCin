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
import glassbox.util.timing.api.TimeConversion;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;


public class CpuUsageTrackingInfoImpl extends AbstractUsageTrackingInfoImpl {
    protected long eventCPUTime = Clock.UNDEFINED_TIME;
    protected long eventTime = Clock.UNDEFINED_TIME;
    protected long eventUserCPUTime = Clock.UNDEFINED_TIME;
    
    protected static final ThreadMXBean thMxbean = ManagementFactory.getThreadMXBean();

    public CpuUsageTrackingInfoImpl() {
    	this(ClockManager.getTime());
	}
 
    public CpuUsageTrackingInfoImpl(long clockTime) {
		eventTime = clockTime;
        if (thMxbean != null) {
            // in bizarre cases we get a null ThreadMXBean. Perhaps this occurs when there's a permission error? 
    	eventCPUTime = thMxbean.getCurrentThreadCpuTime();
    	eventUserCPUTime = thMxbean.getCurrentThreadUserTime();
        }
	}
    
    public CpuUsageTrackingInfoImpl(long clockTime, long cpuTime, long userCpuTime) {
        eventTime = clockTime;
        eventCPUTime = cpuTime;
        eventUserCPUTime = userCpuTime;
    }

	public long getEventCPUTime() {
		return eventCPUTime;
	}
	public long getEventTime() {
		return eventTime;
	}
	public long getEventUserCPUTime() {
		return eventUserCPUTime;
	}
	public void setEventCPUTime(long eventCPUTime) {
		this.eventCPUTime = eventCPUTime;
	}
	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}
	public void setEventUserCPUTime(long eventUserCPUTime) {
		this.eventUserCPUTime = eventUserCPUTime;
	}

	public void subtract(UsageTrackingInfo arg) {
	    eventCPUTime -= arg.getEventCPUTime();
        eventTime -= arg.getEventTime();
        eventUserCPUTime -= arg.getEventUserCPUTime();
    }

    public void add(UsageTrackingInfo arg) {
        eventCPUTime += arg.getEventCPUTime();
        eventTime += arg.getEventTime();
        eventUserCPUTime += arg.getEventUserCPUTime();
    }
    
    public void set(UsageTrackingInfo arg) {
        eventCPUTime = arg.getEventCPUTime();
        eventTime = arg.getEventTime();
        eventUserCPUTime = arg.getEventUserCPUTime();
    }
    
    public void reset() {
        eventTime = 0;
        eventCPUTime = 0;
        eventUserCPUTime = 0;
    }
        
    public boolean equals(Object obj) {
        if(this == obj) return true;
		if (!(obj instanceof CpuUsageTrackingInfoImpl)) {
            return false;
        }
		CpuUsageTrackingInfoImpl other = (CpuUsageTrackingInfoImpl) obj;
		if(this.eventTime != other.getEventTime()) return false;
		if(this.eventCPUTime != other.getEventCPUTime()) return false;
		if(this.eventUserCPUTime != other.getEventUserCPUTime()) return false;
		return true;
	}
    
    public int hashCode() {
        return (int)(eventTime*76 ^ eventCPUTime*225 ^ eventUserCPUTime);
    }
	
    public String toString() {
        return "Elapsed = "+TimeConversion.formatTime(eventTime)+", CPU = " + 
            TimeConversion.formatTime(eventCPUTime)+", User Mode CPU = " +
            TimeConversion.formatTime(eventUserCPUTime);
    }
    
    static final private long serialVersionUID = 2;
}
