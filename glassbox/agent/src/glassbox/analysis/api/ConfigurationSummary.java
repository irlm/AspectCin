/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.analysis.api;


import glassbox.agent.api.ApiType;
import glassbox.track.api.OperationDescriptionImpl;
import glassbox.track.api.PerfStats;
import glassbox.version.InstanceID;

import java.util.List;


/**
 * 
 * Description of data about the Configuration of the JVM.
 *  
 * We don't use JavaBeans-style getters for these operations, so that they are portable
 * to JMX's that require a matching setter...
 * 
 * @author Joseph Shoop
 *
 */
public interface ConfigurationSummary {
    
	   public long maxVMMemory();
       public InstanceID getInstanceID();
	   
}
