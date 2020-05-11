/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved. This program along with all
 * accompanying source code and applicable materials are made available under the 
 * terms of the Lesser
 * Gnu Public License v2.1, which accompanies this distribution and is available at http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.common;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

// should extend DelegatedDependencyInjectionSpringContextTests 
public abstract class BaseTestCase extends TestCase {

    protected ConfigurableApplicationContext context = null;

    public BaseTestCase(String arg0) {
        super(arg0);
    }

    public void setUp() throws Exception {
        File file = new File("glassbox.db");
        if (file.exists())
        	if (!file.delete())
        		throw new IllegalStateException("Couldn't delete old database file");
        
        System.setProperty("glassbox.config.ds", "jdbc:hsqldb:file:glassbox.db;shutdown=true");
    }

    protected final void tearDown() throws Exception {
        super.tearDown();
        if (context != null) {
            System.err.println("Stop db");
            context.close();
        }
        doTearDown();
    }
    
    protected void doTearDown() throws Exception {}

    protected ApplicationContext getContext() {
    	System.out.println("context first check: " + (context == null ? "null" : "not null"));
        if (context != null) {
            return context;
        }
        System.err.println("Start db");
        try {
            context = new ClassPathXmlApplicationContext(new String[] { "springapp-servlet.xml", "applicationContext.xml"  });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    	System.out.println("context second check: " + (context == null ? "null" : "not null"));
        return context;
    }

}
