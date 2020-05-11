/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 24, 2005
 */
package glassbox.util.timing;

import glassbox.track.api.UsageTrackingInfo;


/**
 * 
 * @author Ron Bodkin
 */
public class ClockManager {
	private static Clock instance = null;
	
	public static Clock getInstance() {
		return instance;
	}
    
    public static void setInstance(Clock theInstance) {
        instance = theInstance;
    }
	
	public static long getTime() {
        if (instance==null) return 0;
		return instance.getTime();
	}
    
    public static UsageTrackingInfo getUsage() {
        if (instance==null) return null;
        return instance.getUsage();
    }
    
    public static UsageTrackingInfo getZeroUsage() {
        if (instance==null) return null;
        return instance.getZeroUsage();
    }

}
