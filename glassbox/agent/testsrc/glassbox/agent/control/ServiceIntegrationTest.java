/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt 
 * 
 * Created on Mar 24, 2005
 */
package glassbox.agent.control;

import java.util.Collection;

import glassbox.agent.control.api.GlassboxService;
import glassbox.test.GlassboxIntegrationTest;
import glassbox.test.DelayingRunnable;
import glassbox.test.TimingTestHelper;

/**
 * 
 * @author Ron Bodkin
 */
public class ServiceIntegrationTest extends GlassboxIntegrationTest {
    private GlassboxServiceImpl service;

    public void setUp() {
        super.setUp();
        //service = (GlassboxServiceImpl)getApplicationContext().getBean("glassboxService");
    }
    
    public void setGlassboxService(GlassboxServiceImpl service) {
        this.service = service;
    }
    
    public String[] getConfigLocations() {
        return new String[] { "beans.xml", "glassbox/agent/control/testServiceIntegration.xml" };
    }
    
    public void testLocally() {
        service.setActive(true);
        assertTrue(service.isActive());
        DelayingRunnable.sleep(TimingTestHelper.TICK_TIME);      
        assertTrue(service.getUptime() > 0);
    }

    public void testRemotely() {
        TestClient testClient = (TestClient)getApplicationContext().getBean("testClient");
        testClient.assertWorks();
    }
    
    public static class TestClient {
        private GlassboxService service;
        public void setGlassboxService(GlassboxService glassboxService) {
          this.service = glassboxService;
        }
        public void assertWorks() {
            service.setActive(true);
            assertTrue(service.isActive());            
        }
    }

}
