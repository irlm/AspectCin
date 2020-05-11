/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.test;

import glassbox.config.AutoInitialization;
import glassbox.config.GlassboxInitializer;
import glassbox.test.spring.DelegatedDependencyInjectionSpringContextTests;
import glassbox.util.jmx.*;
import glassbox.util.logging.api.LogManagement;
import glassbox.util.org.sl4j.ILoggerFactory;
import glassbox.util.org.sl4j.Logger;
import junit.framework.TestCase;

/**
 * The new means of initializing the system makes all test cases that drive monitored objects set up a context.
 * We force this to be done early on, so that the tests can override mock values.
 * True unit tests in the monitor can avoid all this by just being defined as monitor unit tests...
 */
public aspect ForceEarlyInit {
    
    private static interface ExplicitlyInitializedTestCase {}
    private static interface InitializedTestCase {}
    
    declare parents: 
        (GlassboxIntegrationTest || DelegatedDependencyInjectionSpringContextTests || JmxManagementTest)+ implements 
        ExplicitlyInitializedTestCase;
    declare parents: (TestCase+ && glassbox.monitor..* ) implements InitializedTestCase;

    pointcut newTestCase() : execution(TestCase+.new(..)) && within(glassbox..*);
    
    before() : newTestCase() {
        LogManagement.setLoggerFactory(new ILoggerFactory() {
            public Logger getLogger(String name) {           
                return LogManagement.getStubLogger();
            }
        });
    }
    
    pointcut execTestMethod() : 
        newTestCase() && this(InitializedTestCase) && !this(ExplicitlyInitializedTestCase);
    
    before() :  execTestMethod() {
        enableJmx(false); // jmx registration can be a bit slow... disable it by default
        GlassboxInitializer.start(false);
    }
    
    before() :  newTestCase() && !this(InitializedTestCase) {
        AutoInitialization.setEnabled(false);
        enableJmx(false);
    }
    
    before() :  newTestCase() && this(ExplicitlyInitializedTestCase) {
        enableJmx(true);
        AutoInitialization.setEnabled(false);
    }
    
    after() : execution(* TestCase+.tearDown(..)) && this(InitializedTestCase) && !this(ExplicitlyInitializedTestCase) {
        GlassboxInitializer.stop();
    }
    
    private void enableJmx(boolean flag) {
        JmxManagement.aspectOf().setEnabled(flag);
    }
}
