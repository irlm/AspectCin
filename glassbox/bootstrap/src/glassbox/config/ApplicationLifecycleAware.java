/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.config;

import java.io.File;

public interface ApplicationLifecycleAware {
    /**
     * 
     * @param homeDirectory for Glassbox
     * @param monitorVmShutdown is used if detecting system shutdown is the only way to ensure Glassbox shuts down (i.e., prevent hanging because of RMI objects)   
     * @return the glassbox service object
     */
	Object startUp(File homeDirectory, boolean monitorVmShutdown);
	void shutDown();
    Object getService();
}
