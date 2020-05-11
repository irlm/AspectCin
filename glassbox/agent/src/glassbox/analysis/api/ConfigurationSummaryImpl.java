/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;

import glassbox.track.api.OperationDescriptionImpl;
import glassbox.track.api.PerfStats;
import glassbox.version.InstanceID;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.List;


public class ConfigurationSummaryImpl implements ConfigurationSummary {

    long maxVMMemory = 0;
    InstanceID instanceID = null;

    public ConfigurationSummaryImpl() {
        try {
            Java5ConfigurationSummary summary = new Java5ConfigurationSummary();
            maxVMMemory = summary.getMaxVmMemory();
        } catch (NoClassDefFoundError _) {
        }
    }
    
    public long maxVMMemory() {
        return maxVMMemory;
    }
    
    public void setInstanceID(InstanceID instanceID) {
        this.instanceID = instanceID;
    }
    
    public InstanceID getInstanceID() {
        return instanceID;
    }
    

    static final private long serialVersionUID = 1L;
}
