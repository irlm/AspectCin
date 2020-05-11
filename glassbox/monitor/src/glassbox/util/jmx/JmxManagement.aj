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

import java.util.*;


/** 
 * Reusable aspect that automatically registers beans for management.
 */
public aspect JmxManagement {
    
    boolean ManagedBean.hasRegistered;
      
    private JmxServerManager manager;
    
    private boolean enabled = !Boolean.getBoolean("glassbox.util.jmx.registration.disabled"); // enabled by default

    private Map deferredRegistration = new WeakHashMap();

    /** Defines classes to be managed and defines basic management operation */
    public interface ManagedBean {
        /** Define a JMX operation name for this bean. Not to be confused with a Web request operation. */
//        Map getAttributes();
        String getOperationName();
        
        String getTopic();
        /** Returns the underlying JMX MBean that provides management information for this bean (POJO).. */
        Object getMBean(JmxServerManager serverManager);
        /** Get the interface type used as the management interface */
        Class getManagementInterface();
    }
    public interface EagerlyRegisteredManagedBean extends ManagedBean {}
    
    private pointcut eagerManagedBeanConstruction(ManagedBean bean) : 
        execution(ManagedBean+.new(..)) && this(bean) && this(EagerlyRegisteredManagedBean); 
    
    private pointcut topEagerLevelManagedBeanConstruction(ManagedBean bean) : 
        eagerManagedBeanConstruction(bean) && if(thisJoinPointStaticPart!=null && thisJoinPointStaticPart.getSignature().getDeclaringType() == bean.getClass() && !bean.hasRegistered); 
        
    /** After constructing an instance of <code>ManagedBean</code>, register it */
    // advise top-level executions of constructors; this lets us advise construction of classes and of aspects too, exactly once
    after(ManagedBean bean) returning: topEagerLevelManagedBeanConstruction(bean) {
        register(bean);
    }
    
    public void register(ManagedBean bean) {
        if (enabled) {
            if (manager != null) {
                manager.register(bean);
            } else {
                // JMX isn't ready yet: enqueue
                deferredRegistration.put(bean, null);
            }                
        }
    }
    
    public void unregister(ManagedBean bean) {
        if (enabled && manager != null) {
            manager.unregister(bean);
        }
    }
    
    /** Creates a JMX MBean to represent this instance. */ 
    public Object ManagedBean.getMBean(JmxServerManager serverManager) {
        return serverManager.getDefaultMBean(this);
    }
    
    public JmxServerManager getJmxServerManager() {
        return manager;
    }
    
    public void setJmxServerManager(JmxServerManager manager) {
        this.manager = manager;
        if (!deferredRegistration.isEmpty() && manager != null) {
            for (Iterator it = deferredRegistration.keySet().iterator(); it.hasNext();) {
                ManagedBean bean = (ManagedBean)it.next();
                manager.register(bean);
            }
            deferredRegistration.clear();
        }        
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
        
}
