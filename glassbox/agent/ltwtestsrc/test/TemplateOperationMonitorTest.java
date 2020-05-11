/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package test;

import glassbox.config.GlassboxInitializer;
import glassbox.monitor.OperationFactory;
import glassbox.util.jmx.JmxManagement;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import junit.framework.TestCase;

public class TemplateOperationMonitorTest extends TestCase {
    private ObjectName soloName;
    private ObjectName parentName;
    private ObjectName childName;
    private String childKey;

    public void setUp() throws Exception {
        String appStr = "Glassbox:topic=stats,application=\""+OperationFactory.UNDEFINED_APPLICATION+"\"";
        String opStr = ",ui0=\"operation(type test.TestMonitor monitor; name test.DummyMonitoredItem.";
        String parentStr = appStr+opStr+"nested)\"";
        childKey = "operation(type test.TestMonitor monitor; name test.DummyMonitoredItem.monitored)";
        String childStr = parentStr+",ui1=\""+childKey+"\"";
        
        soloName = new ObjectName(appStr+opStr+"monitored)\"");
        parentName = new ObjectName(parentStr);
        childName = new ObjectName(childStr);
        GlassboxInitializer.start(true);
    }
    
    public void tearDown() throws Exception {
        GlassboxInitializer.stop();
    }
    
    public void testTemplateMonitor() throws Exception {
        new DummyMonitoredItem().monitored();

        Object count = getAttribute(soloName, "count");
        assertEquals(new Integer(1), count);
    }

    public void testOneChildStat() throws Exception {        
        new DummyMonitoredItem().nested();

        TabularData table = (TabularData)getAttribute(parentName, "children");
        assertTrue("bad table size "+table.size(), table.size()>=1 && table.size()<=2);

        for (int i=0; i<table.size(); i++) {
            CompositeData childStats = (CompositeData)table.get(new Object[] {new Integer(i)});
            String key = childStats.get("key").toString();
            if ("time".equals(key)) {
                continue;
            }
            assertEquals(childKey, key);
            assertEquals(new Integer(1), childStats.get("count"));
        }
    }
    
    public void testParentStats() throws Exception {
        new DummyMonitoredItem().nested();

        ObjectName result = (ObjectName) getAttribute(childName, "parent");
        assertEquals(parentName, result);
    }

    public void testOrdering()  throws Exception {
        new DummyMonitoredItem().init();
    }
    
    private Object getAttribute(ObjectName oName, String attr) throws Exception {
        MBeanServer server = getServer();
        return server.getAttribute(oName, attr);
    }

    private MBeanServer getServer() {
        return (MBeanServer)JmxManagement.aspectOf().getJmxServerManager().getMBeanServer();
    }
}