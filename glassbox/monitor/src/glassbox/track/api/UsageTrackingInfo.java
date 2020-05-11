/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.track.api;

public interface UsageTrackingInfo extends Comparable, Cloneable {
	public long getEventCPUTime();
	public long getEventTime();
	public long getEventUserCPUTime();
    
	public void setEventCPUTime(long eventCPUTime);
	public void setEventTime(long eventTime);
	public void setEventUserCPUTime(long eventUserCPUTime);
    
    /**
     * Mutates the structure: subtracts the passed argument value from this time tracking info object.
     */
    public void subtract(UsageTrackingInfo arg); 
    
    /**
     * Mutates the structure: addsthe passed argument value from this time tracking info object.
     */
    public void add(UsageTrackingInfo arg); 
    
    public void reset();
    
    public UsageTrackingInfo klone();    
}
