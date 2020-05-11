/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.agent.control;

import junit.framework.TestCase;

// this test tested auto-initialization, which we don't do now

public class InitializationTest extends TestCase {//extends MockObjectTestCase {
    public void testNothing() {}
    
//    private Mock initializerMock;
//    
//    public void setUp() {
//        super.setUp();
//        initializerMock = mock(ApplicationLifecycleAware.class);
//        GlassboxTestInitializer.aspectOf().initializer = (ApplicationLifecycleAware)initializerMock.proxy();
//    }
//
//    public void tearDown() {
//        super.tearDown();
//    }
//
//    public static class TestBean implements Bean {
//        public void run() {
//        }
//    }
//
//    public void testInitialization() {
//        initializerMock.expects(atLeastOnce()).method("startUp");
//        initializerMock.expects(atLeastOnce()).method("shutDown");
//        TestBean bean = new TestBean(); 
//        bean.run();
//    }
//    
//
//    public void testInitializationError() {
//        Throwable throwable = new RuntimeException("boom");
//        initializerMock.expects(atLeastOnce()).method("startUp").will(throwException(throwable));
//
//        try {
//            TestBean bean = new TestBean(); 
//            bean.run();
//            //should be swallowed, not kill the system!
//        } catch (Throwable t) {
//            fail("error containment should catch this exception!");
//        }
//    }
//    
////    protected String[] getConfigLocations() {
////        return new String[] { "beans.xml", "com/glassbox/agent/control/testInitialization.xml" };
////    }
//    private static aspect GlassboxTestInitializer extends GlassboxInitializer {
//        public pointcut startRunning() : execution(* InitializationTest.TestBean.run());
//        
//        // shut down any time we exit a main method in a test case
//        //public pointcut stopRunning() 
//        protected ApplicationLifecycleAware getInitialization() {
//            return initializer;
//        }
//        
//        public ApplicationLifecycleAware initializer;
//    }
//
//    
}

