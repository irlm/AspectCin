/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test;

import glassbox.config.GlassboxInitializer;
import glassbox.test.ajmock.VirtualMockObjectTestCase;
import glassbox.test.spring.DelegatedDependencyInjectionSpringContextTests;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class GlassboxIntegrationTest extends VirtualMockObjectTestCase { //AbstractDependencyInjectionSpringContextTests {
    DelegatedDependencyInjectionSpringContextTests contextTestState;

    public GlassboxIntegrationTest() {
        super();
        contextTestState = DelegatedDependencyInjectionSpringContextTests.makeTestCase(this);
        contextTestState.setConfigLocations(getConfigLocations());        
    }

    public void setUp() {
        // before spring constructs itself, shutdown any existing system initialization...
        GlassboxInitializer.stop();
        
        super.setUp();
        //System.setProperty("glassbox.agent.location", "../nowhere/bin");
        contextTestState.invokeSetUp();
    }
    
	public void tearDown() {
        super.tearDown();
        contextTestState.invokeTearDown();
		ConfigurableApplicationContext cac = (ConfigurableApplicationContext)getApplicationContext();
		cac.close();
        contextTestState.setDirty();
	}
    
    public ApplicationContext getApplicationContext() {
        return contextTestState.getApplicationContext();
    }
    
    protected String[] getConfigLocations() {
        String config[] = { "beans.xml" };
        return config;
    }
    
    /**
     * This method lets us determine whether we want to run test cases that are known to fail. Lets us keep such tests in the proper location in the source tree. 
     */
    public static boolean runFailingTests() {
        return Boolean.getBoolean("run.failing.tests");
    }
}
