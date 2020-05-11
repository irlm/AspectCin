/********************************************************************
 * Copyright (c) 2005 Glassbox Corporation, Contributors.
 * All rights reserved. 
 * This program along with all accompanying source code and applicable materials are made available 
 * under the terms of the Lesser Gnu Public License v2.1, 
 * which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 *  
 * Contributors: 
 *     Ron Bodkin     initial implementation 
 *******************************************************************/
package glassbox.util.jmx;

import glassbox.util.jmx.JmxManagement.EagerlyRegisteredManagedBean;
import glassbox.monitor.AbstractMonitor;

import org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler;
import org.springframework.jmx.export.assembler.MBeanInfoAssembler;

/** 
 * Applies JMX management to monitors. 
 */
public aspect MonitorJmxManagement {
    /** Management interface for request monitors: allow enabling and disabling. */
    public interface RequestMonitorManagementInterface {
        public boolean isEnabled();
        public void setEnabled(boolean enabled);
    }
    
    public interface RequestMonitorMBean extends EagerlyRegisteredManagedBean, RequestMonitorManagementInterface {}
    
    /** Make the @link AbstractMonitor aspect implement @link RequestMonitorMBean, so all instances can be managed */
    declare parents: AbstractMonitor implements RequestMonitorMBean;

    public String RequestMonitorMBean.getOperationName() {
        return "control=monitor,type="+getClass().getName();
    }
    
    public Class RequestMonitorMBean.getManagementInterface() {
        return RequestMonitorManagementInterface.class;
    }    
    
    public String RequestMonitorMBean.getTopic() {
        return "monitor";
    }
}
