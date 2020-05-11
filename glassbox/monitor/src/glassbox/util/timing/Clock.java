/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 22, 2005
 */
package glassbox.util.timing;

import glassbox.agent.api.ApiType;
import glassbox.track.api.UsageTrackingInfo;

/**
 * 
 * @author Ron Bodkin
 */
public interface Clock extends ApiType {
    /**
     * Normalize clock time to nanosecond accuracy. Uses the most precise available timer...
     * 
     * @return time in nanoseconds 
     */
    long getTime();
    
    /**
     * Normalize clock time to millisecond accuracy. Uses the lowest overhead available timer...
     * 
     * @return time in milliseconds 
     */
    long getTimeQuickly();
    
    /**
     * 
     * @return usage tracking for "now"
     */
    UsageTrackingInfo getUsage();

    /**
     * 
     * @return usage tracking for zero accumulated time
     */
    UsageTrackingInfo getZeroUsage();
    
	static final long UNDEFINED_TIME = -1L;	
    
}
