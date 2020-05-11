/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test.spring;

import junit.framework.TestCase;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * work-around JUnit single-inheritance
 * abstract with factory to avoid JUnit warnings
 *
 */
public abstract class DelegatedDependencyInjectionSpringContextTests extends AbstractDependencyInjectionSpringContextTests {
    public static DelegatedDependencyInjectionSpringContextTests makeTestCase(TestCase parent) {
        DelegatedDependencyInjectionSpringContextTests ret = new DelegatedDependencyInjectionSpringContextTests() {};
        ret.parent = parent;
        return ret;
    }
    
    private String[] configLocations;
    private TestCase parent;

    private DelegatedDependencyInjectionSpringContextTests() {
    }
    
    /**
     * Must call this setter before running tests.
     */
    public void setConfigLocations(String[] configLocations) {
        this.configLocations = configLocations;
    }
    
    public String[] getConfigLocations() {
        return configLocations;
    }
    
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void invokeSetUp() throws Exception {
        setUp();
        
        if (!isPopulateProtectedVariables()) {
            this.applicationContext.getBeanFactory().autowireBeanProperties(
                    parent, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
        }
        
    }

    public void invokeTearDown() {
        tearDown();
    }
}
