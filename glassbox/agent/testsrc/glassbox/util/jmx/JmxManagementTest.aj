/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.util.jmx;

import javax.management.*;

import junit.framework.TestCase;
import glassbox.util.jmx.JmxManagement.ManagedBean;

public class JmxManagementTest extends TestCase {

    private DefaultJmxServerManager jmxServerManager;
    private MBeanServer mbeanServer;
    
    public void setUp() {
        mbeanServer = MBeanServerFactory.createMBeanServer();
        jmxServerManager = new DefaultJmxServerManager();
        jmxServerManager.setAssembler(new SpringAssembler());
    }
    
    public void tearDown() {
        jmxServerManager.destroy();
        jmxServerManager.setMBeanServer(null);
        JmxManagement.aspectOf().setEnabled(true);
    }
        
    public void testCleanUp() {
        ManagedBean two = new TestManagedBean();
        ManagedBean three = new TestManagedBean();
        
        JmxManagement.aspectOf().register(three);
        
        jmxServerManager.setMBeanServer(mbeanServer);
        jmxServerManager.afterPropertiesSet();
        
        JmxManagement.aspectOf().setJmxServerManager(jmxServerManager);
        JmxManagement.aspectOf().register(two);
        
        assertTrue(mbeanServer.isRegistered(jmxServerManager.getObjectName(two)));
        assertTrue(mbeanServer.isRegistered(jmxServerManager.getObjectName(three)));
        
        jmxServerManager.destroy();
        assertFalse(mbeanServer.isRegistered(jmxServerManager.getObjectName(two)));        
        assertFalse(mbeanServer.isRegistered(jmxServerManager.getObjectName(three)));
    }
    
    public void testHandleBadQuotedNames() {
        testHandleBadNames(true);
    }
    
    public void testHandleBadUnquotedNames() {
        // tests JMX 1.1 behavior...
        testHandleBadNames(false);
    }
    
    public void testHandleBadNames(boolean quote) {
        jmxServerManager.setMBeanServer(mbeanServer);
        jmxServerManager.afterPropertiesSet();
        
        StringBuffer allChars = new StringBuffer();
        for (char ch=0x00; ch<=0xff; ch++) {
            allChars.append((char)ch);
        }
        
        String names[] = { "simple", "", "'", "'x'=\"name, big, overall?much=12&url=3\n", allChars.toString() };
        for (int i=0; i<names.length; i++) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("key=");
            if (quote) {
                buffer.append("\"");
            }
            int pos=buffer.length();
            buffer.append(names[i]);
            DefaultJmxServerManager.jmxEncode(buffer, pos);
            if (quote) {
                buffer.append("\"");
            }
            ManagedBean bean = new TestManagedBean(buffer.toString());
            ObjectName oName = jmxServerManager.getObjectName(bean);
            try {                
                System.err.println(oName);
                jmxServerManager.register(bean);
            } catch (RuntimeException exc) {
                System.err.println("Can't register "+oName);
                throw exc;
            }
            assertTrue(mbeanServer.isRegistered(oName));            
        }
    }
    
    // this test does NOT work - the modelMbean pins the pojo
    public void testClassGc() {
//        ManagedBean one = new TestManagedBean();
//        MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();
//        
//        JmxManagement jmxManagement = JmxManagement.aspectOf();
//        jmxManagement.setMBeanServer(mbeanServer);
//        jmxManagement.afterPropertiesSet();
//        jmxManagement.register(one);        
//        
//        assertTrue(mbeanServer.isRegistered(one.getObjectName()));
//        
//        ObjectName oneName = one.getObjectName();
//        
//        one = null;
//        System.gc();
//        jmxManagement.clearReferenceQueue();
//        System.gc();
//        jmxManagement.clearReferenceQueue();
//        
//        assertFalse(mbeanServer.isRegistered(oneName));
//
//        jmxManagement.destroy();
    }
    
    public class TestManagedBean implements ManagedBean, Runnable {
        private String operationName;
        
        public TestManagedBean() {
            this.operationName = "control=test,instance="+this;
        }
        
        public TestManagedBean(String operationName) {
            this.operationName = operationName;
        }
        
        public String getOperationName() { return operationName; }
        
        public String getTopic() { return "test"; }
        /** Get the interface type used as the management interface */
        public Class getManagementInterface() { return Runnable.class; }
        
        public void run() {}
    }
}
