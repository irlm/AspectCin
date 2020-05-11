/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.summary;

import glassbox.monitor.AbstractMonitor;
import glassbox.monitor.MonitorResponseTestCase;

import java.io.Serializable;

public class ContextManagementTest extends MonitorResponseTestCase {

    // not finished rewriting test after context management changed... really an integration test of stats summarizer
    public void testIncomplete() {
    }
    
//    public void setUp() {
//        super.setUp();
//        StatsSummarizer summarizer = new StatsSummarizer(); 
//        responseFactory.addListener(summarizer);
//    }
//    
//    public void testSimpleContext() {
//        ctxSimple(1);
//    }
//    public void testNestedContext() {
//        ctxSimple(2);
//    }
//    public void testNesting() {
//        ctxValid();
//    }
//    public void testRecovery() {
//        ctxInvalid();
//        ctxValid();//ensure it was corrected!
//    }
//    
//    private void ctxSimple(int depth) {
//        if (depth>0) {
//            ctxSimple(depth-1);
//        }
//    }
//    private void ctxValid() {
//        valid();
//    }
//    private void ctxInvalid() {
//        invalid();
//    }
//    private void valid() {}
//    private void invalid() {}
//
//    private static aspect ContextCreator extends AbstractMonitor {
//        protected pointcut contextEntry() : within(ContextManagementTest) && execution(* ctx*(..));
//        before() : contextEntry() {
//            responseFactory.setApplication("test");
//            begin(operationFactory.makeOperation("test", "test"));
//        }
//        protected pointcut monitorEnd() : contextEntry();
//        
//        after() returning: contextEntry() && cflowbelow(contextEntry()) {
//            assertEquals("test", responseFactory.getApplication());
//        }
//        after() returning: contextEntry() && !cflowbelow(contextEntry()) {
//            assertNull(responseFactory.getApplication());
//        }
//    }
//    private static aspect Valid extends AbstractMonitor {
//        protected pointcut monitorPoint(Object id) :  within(ContextManagementTest) && execution(* valid()) && this(id);
//        protected Serializable getKey(Object identifier) { return operationFactory.makeOperation("test", "test"); }
//        
//    }
//    private static aspect Invalid extends AbstractMonitor {
//        protected pointcut monitorBegin(Object id)  :  within(ContextManagementTest) && execution(* invalid()) && this(id);
//        protected Serializable getKey(Object identifier) { return operationFactory.makeOperation("testbad", "bad"); }
//    }
}
