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

import java.lang.ref.*;
import java.util.*;

import javax.management.*;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;

import glassbox.util.jmx.JmxManagement.ManagedBean;

/** 
 * Server manager that accepts beans to be managed and ensures proper queueing on startup and clean up on shutdown.
 * Limitation: prevents GC of types that are registered, since the MBeanServer will hold a hard reference to them.
 * Glassbox keeps mbeans associated with names of types that get reused when apps are redeployed.
 * This needs to be fixed in future by registering weak references instead. 
 * If you need to allow GC and clean up of the objects being auto-registered, then you should use proxy with a weak reference...  
 */
// ensures that we can deregister the monitored objects from JMX, e.g., when undeploying an app   
public class DefaultJmxServerManager implements JmxServerManager {
    
    /** Defines the MBeanServer with which beans are auto-registered */
    private MBeanServer server;
    
    // map from ObjectName to Reference ...
    private Map hasRegistered = new HashMap();
    private ReferenceQueue queue = new ReferenceQueue();
    private Timer timer;
    private Assembler assembler;
    
    public void unregister(ManagedBean bean) {
        ObjectName objectName ;
        try {
            objectName = getObjectName(bean);
            if (isDebugEnabled()) {
                logDebug("unregistering MBean: "+objectName);
            }
            doUnregister(objectName);
        } catch (MalformedObjectNameException e) {
            getLogger().error("Can't register bean for "+bean+": bad object name", e);
            return;
        }
    }
    
    private void doUnregister(ObjectName objectName) {
        try {
            server.unregisterMBean(objectName);
        } catch (Exception e) {
            ; // ignore: must have failed on registration
        }
    }        
    
    public ObjectName getObjectName(ManagedBean bean) throws MalformedObjectNameException {
        String prefix = "Glassbox:topic="+bean.getTopic()+","; 
        String name = prefix+bean.getOperationName();
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException badNameOne) {
            logDebug("Detected older JMX implementation that won't accept name:"+name);
            try {
                name = prefix+strip10Chars(bean.getOperationName());
                return new ObjectName(name);
            } catch (MalformedObjectNameException badName) {
                logError("Bad JMX name "+name);
                throw badName;
            }
        }
    }

    public static String strip10Chars(String name) {
        StringBuffer buf = new StringBuffer();
        boolean inBody = false;
        for (int i=0; i<name.length(); i++) {
            char ch = name.charAt(i);
            if (ch=='(' || ch==')' || ch==':' || ch=='*') {
                continue;
            } else if (ch=='\\') {
                continue;
            } else if (ch=='?') {
                ch='$';
            } else if (ch==',') {
                inBody=false;
            } else if (ch=='=') {
                if (inBody) {
                    continue; // don't include = in values
                }
                inBody=true;
            }
            buf.append(ch);
        }
        return buf.toString();
    }
    
    public void register(ManagedBean bean) {
        synchronized(bean) {
            if (bean.hasRegistered) {
                return;
            }
            bean.hasRegistered = true;
        }
        
        ObjectName objectName ;
        try {
            objectName = getObjectName(bean);
            logDebug("registering MBean: "+objectName);     
        } catch (MalformedObjectNameException e) {
            logError("Can't register bean for "+bean+": bad object name", e);
            return;
        }

        Object mBean = bean.getMBean(this);
        if (mBean != null) {
            try {
                try {
                    server.registerMBean(mBean, objectName);
                } catch (InstanceAlreadyExistsException e) {
                    // can be caused by clearing stats 
                    // might also be caused when redeploying a managed application... we simply unregister & retry once more
                    // test: the Spring beans should be unregistering all the beans with a listener when a servlet context   
                    // in any event only a single bean with the given name can exist at one time, so the synchronization
                    // above is sufficient
                    try {
                        server.unregisterMBean(objectName);
                    } catch (InstanceNotFoundException notFound) {
                        ;//ok - try again
                        // why does this happen frequently for Alec? Is there a race?
                    }
                    server.registerMBean(mBean, objectName);
                }
                hasRegistered.put(new WeakReference(bean, queue), objectName);
                logDebug("registered: "+objectName);
            } catch (Throwable t) {
                logWarn("Can't register bean "+objectName, t);
            }
        } else {
            logError("Null MBean!");
        }
    }

    // Spring won't allow a property where the setter signature differs from the return value on the getter
    
    public void setMBeanServer(Object server) {
        clearEntries();
        
        this.server = (MBeanServer)server;
    }

    public Object getMBeanServer() {
    	return server;
    }
    
    public void clearEntries() {
        for (Iterator iter = hasRegistered.values().iterator(); iter.hasNext();) {           
            ObjectName objectName = (ObjectName) iter.next();
            doUnregister(objectName);
        }
    }    
    
    public void afterPropertiesSet() {
        TimerTask task = new TimerTask() {

            /* (non-Javadoc)
             * @see java.util.TimerTask#run()
             */
            public void run() {
                clearReferenceQueue();
            }            
        };
        // Timer constructor doesn't take a string in JDK 1.4
        timer = new Timer(/*"jmxCleanup",*/ true);
        timer.schedule(task, 10000, 10000);
    }
    
    public void destroy() {
        if (timer != null) {
            timer.cancel();
        }
        clearEntries();
    }
    
    void clearReferenceQueue() {
        for(;;) {
            try {
                Reference ref = (Reference)queue.remove(1);
                if (ref == null) {
                    break;
                }
                ObjectName oName = (ObjectName)hasRegistered.remove(ref);
                server.unregisterMBean(oName);
            } catch (Exception e) {
                ; // ignore unregistration errors: must have failed on registration
                // ignore interrupted: try again
            }
        }
    }            
    
    public Object getDefaultMBean(ManagedBean bean) {
        try {
            String operationName = bean.getOperationName();
            if (operationName == null) {
                getLogger().warn("Unexpected null bean for operation: " + operationName);
                return null;
            }
            
            RequiredModelMBean mBean = new RequiredModelMBean();
            ModelMBeanInfo mbeanInfo = getMBeanInfo(bean, operationName);
            mBean.setModelMBeanInfo(mbeanInfo);
            if (isDebugEnabled()) logDebug("mBean: "+mbeanInfo);            
            mBean.setManagedResource(bean, "ObjectReference");
            return mBean;

            // the use of a StandardMBean would be easier but would break backward
            // compatibility with Weblogic 8.1
            //return new StandardMBean(this, getManagementInterface());
        } catch (Throwable t) {
            /* This is safe because @link glassbox.inspector.error.ErrorHandling will resolve it as described later! */
            throw new RegistrationFailureException("can't register bean ", t);
        }
    }
    
    public ModelMBeanInfo getMBeanInfo(ManagedBean bean, String operationName) throws JMException {
        return assembler.assemble(bean.getManagementInterface(), bean, operationName);        
    }

    /** 
     * Utility method to encode a JMX key name, escaping illegal characters.
     * Similar goal to JMX 1.2's ObjectName.quote
     * @param jmxName unescaped string buffer of form JMX keyname=key 
     * @param attrPos position of key in String
     */ 
    public static StringBuffer jmxEncode(StringBuffer jmxName, int attrPos) {
        if (jmxName.length() == attrPos) {
            jmxName.append(' ');
            return jmxName;
        }
        
        // translate illegal JMX characters
        for (int i=attrPos; i<jmxName.length(); i++) {
            if (jmxName.charAt(i)==',' ) {
                jmxName.setCharAt(i, ';');
            } else if (jmxName.charAt(i)=='?' || jmxName.charAt(i)=='*' || jmxName.charAt(i)=='\\' ) {
                jmxName.insert(i, '\\');
                i++;
            } else if (jmxName.charAt(i)=='"') {
                jmxName.deleteCharAt(i);
                i--;
            } else if (jmxName.charAt(i)=='\n') {
                jmxName.insert(i, '\\');
                i++;
                jmxName.setCharAt(i, 'n');
            }
        }
        return jmxName;
    }
    
    public void setAssembler(Assembler assembler) {
        this.assembler = assembler;
    }

    public Assembler getAssembler() {
        return assembler;
    }
    
    
}
