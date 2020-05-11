/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.summary;

import glassbox.response.Response;
import glassbox.response.ResponseFactory;
import glassbox.track.ThreadStats;
import glassbox.track.api.*;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.InvocationMatcher;

public class StatsSummarizerTest extends MockObjectTestCase {

    private StatsSummarizer summarizer;
    private Mock mockRegistry;
    private Mock mockResponseFactory;
    private MockOperation initial;

    private class MockOperation {
        private Mock mockResponse;
        private Response response;
        private Mock mockOperation;
        private OperationDescription operation;
        private Mock mockStats;
        private OperationPerfStats stats;
        private Response parentResponse;

        public MockOperation(String role, MockOperation parent, String layer, StatisticsType type) {
            mockOperation = mock(OperationDescription.class, role + " operation");
            operation = (OperationDescription) mockOperation.proxy();
            mockResponse = mock(Response.class, role + " response");
            response = (Response) mockResponse.proxy();
            mockStats = mock(OperationPerfStats.class, role + " stats");
            stats = (OperationPerfStats) mockStats.proxy();

            parentResponse = parent == null ? null : parent.response;
            mockResponse.stubs().method("getParent").will(returnValue(parentResponse));
            if (parent != null) {
                mockOperation.expects(atLeastOnce()).method("setParent").with(eq(parent.operation)).isVoid();
            }
            mockResponse.stubs().method("getKey").will(returnValue(operation));
            mockResponse.stubs().method("getFactory").will(returnValue(mockResponseFactory.proxy()));
            mockResponse.stubs().method("getLayer").will(returnValue(Response.UI_CONTROLLER));
            mockStats.stubs().method("isOperationThisRequest").will(returnValue(false));

            Mock mockRep = (parent==null ? mockRegistry : parent.mockStats); 
            mockRep.stubs().method("getPerfStats").with(eq(type), eq(operation)).will(returnValue(stats));
        }

        public void expectInvocation(long startTm) {
            expectInvocation(startTm, "ensure");
        }
        
        public void expectInvocation(long startTm, String idVal) {
            mockResponse.stubs().method("getStart").will(returnValue(startTm));

            mockStats.expects(once()).method("recordStart").with(eq(startTm));
            setupEnsureValues(mockResponse, idVal);
            mockStats.expects(once()).method("recordEnd").with(eq(response)).after("recordStart").after(mockResponse,
                    idVal).will(returnValue(0L));
        }
    };

    public void setUp() {
        summarizer = new StatsSummarizer();
        ThreadStats threadStats = new ThreadStats();
        summarizer.setThreadStats(threadStats);

        mockResponseFactory = mock(ResponseFactory.class);
        mockResponseFactory.stubs().method("getLastResponse").withNoArguments().will(returnValue(null));
        mockResponseFactory.expects(atLeastOnce()).method("setApplication").with(eq(null)).isVoid();

        mockRegistry = mock(StatisticsRegistry.class);
        threadStats.setRegistry((StatisticsRegistry) mockRegistry.proxy());
        initial = new MockOperation("initial", null, Response.UI_CONTROLLER, StatisticsTypeImpl.UiRequest);
    }

    public void testSingleOperation() {
        initial.expectInvocation(100000L);

        // main expectations
        initial.mockStats.expects(once()).method("recordAsOperation").isVoid();
        initial.mockStats.expects(once()).method("summarizeOperation").with(eq(initial.response), eq(initial.stats))
                .isVoid();

        // drive the test...
        summarizer.startedResponse(initial.response);
        summarizer.finishedResponse(initial.response);
    }

    public void testOneNestedDefaults() {
        MockOperation opA = setupOneNested(null, null);
        
        expectTopOperation(opA);

        runOneNested(opA);
    }

    public void testOneNestedTopGt() {
        MockOperation opA = setupOneNested(new Integer(10),  new Integer(1));
        
        expectTopOperation(opA);

        runOneNested(opA);
    }
    
    public void testOneNestedTopEqual() {
        MockOperation opA = setupOneNested(new Integer(10),  new Integer(10));
        
        expectTopOperation(opA);

        runOneNested(opA);
    }
    
    public void testOneNestedTopLower() {
        MockOperation opA = setupOneNested(new Integer(10),  new Integer(11));

        expectChildKey(opA);

        runOneNested(opA);
    }
    
    public void testKeyTwice() {
        MockOperation opA = setupOneNested(new Integer(10),  new Integer(11));

        opA.expectInvocation(110000L, "i2");        
        expectChildKey(opA, 2);

        runNested(new MockOperation[] { opA, opA });
    }
    
    public void testTwoSiblingsTop() {
        initial.expectInvocation(100000L);
        initial.mockResponse.stubs().method("get").with(eq(Response.OPERATION_PRIORITY)).will(returnValue(null));

        MockOperation[] ops = setupSiblings(initial, null, null);
        
        expectTopOperation(ops);

        runNested(ops);
    }
    
    public void testTwoSiblingsB() {
        initial.expectInvocation(100000L);
        initial.mockResponse.stubs().method("get").with(eq(Response.OPERATION_PRIORITY)).will(returnValue(new Integer(20)));

        MockOperation[] ops = setupSiblings(initial, 20, 50);
        
        expectNeverOp(ops[0]);        
        expectChildKey(ops[1]);

        runNested(ops);
    }
    
    public void testThreeSiblingsB() {
        initial.expectInvocation(100000L);
        initial.mockResponse.stubs().method("get").with(eq(Response.OPERATION_PRIORITY)).will(returnValue(new Integer(20)));

        MockOperation[] ops = setupSiblings(initial, new int[] { 20, 50, 60 });
        
        expectNeverOp(ops[0]);
        
        expectChildKey(ops[1]);
        expectAlsoKey(ops[2]);

        runNested(ops);
    }
    
    public void testDeepChangingOperation() {
        // parent(5)
        //     5        6          4  4  6 6
        //         (6, 7, 8, 5)    6  5  3 7
        initial.expectInvocation(100000L);
        initial.mockResponse.stubs().method("get").with(eq(Response.OPERATION_PRIORITY)).will(returnValue(new Integer(5)));
        MockOperation[] ops = setupSiblings(initial, new int[] { 5, 6, 4, 4, 6, 6 });
        MockOperation[][] grandKids = new MockOperation[ops.length][];
        expectNeverOp(ops[0]);
        expectParentNonKey(ops[1]);
        expectNeverOp(ops[2]);
        expectNeverOp(ops[3]);
        expectAlsoKey(ops[4]);
        expectParentNonKey(ops[5]);
        
        grandKids[1] = setupSiblings(ops[1], new int[] { 6, 7, 8, 5 });
        expectNeverOp(grandKids[1][0]);
        expectNeverOp(grandKids[1][3]);
        Mock master = grandKids[1][1].mockStats; 
        expectChildKey(grandKids[1][1]);
        
        // grandKids[1][4] - this will go as dispatch for now, needs to change
        expectAlsoKey(grandKids[1][2]); // notice the higher priority operation isn't the key...
        grandKids[2] = setupSiblings(ops[2], new int[] { 6 });
        expectAlsoKey(grandKids[2][0]);
        grandKids[3] = setupSiblings(ops[3], new int[] { 5 });
        expectNeverOp(grandKids[3][0]); // equal to parentmost operation
        grandKids[4] = setupSiblings(ops[4], new int[] { 3 });
        expectNeverOp(grandKids[4][0]);
        grandKids[5] = setupSiblings(ops[5], new int[] { 7 });
        expectAlsoKey(grandKids[5][0]);

        master.expects(once()).method("recordDispatch").with(eq(ops[1].response)).after("recordEnd").isVoid();
        // odd case: these priority 4 responses aren't considered dispatch ?!
//        master.expects(once()).method("recordDispatch").with(eq(ops[2].response)).after("recordEnd").isVoid();
//        master.expects(once()).method("recordDispatch").with(eq(ops[3].response)).after("recordEnd").isVoid();
        // counts as dispatch because it has a CHILD operation...
        master.expects(once()).method("recordDispatch").with(eq(ops[5].response)).after("recordEnd").isVoid(); 

        summarizer.startedResponse(initial.response);
        for (int i=0; i<ops.length; i++) {
            summarizer.startedResponse(ops[i].response);
            for (int j=0; (grandKids[i] != null && j<grandKids[i].length); j++) {
                summarizer.startedResponse(grandKids[i][j].response);
                summarizer.finishedResponse(grandKids[i][j].response);
            }
            summarizer.finishedResponse(ops[i].response);
        }
        summarizer.finishedResponse(initial.response);        
    }
    
    public void testDeepChangingOperationLowPriMidLevel() {
        // parent(5)
        //   5   3   4 4 6 7 6 
        //       6   5 6 4   7
        initial.expectInvocation(100000L);
        initial.mockResponse.stubs().method("get").with(eq(Response.OPERATION_PRIORITY)).will(returnValue(new Integer(5)));
        MockOperation[] ops = setupSiblings(initial, new int[] { 5, 3, 4, 4, 6, 7, 6 });
        MockOperation[][] grandKids = new MockOperation[ops.length][];
        expectNeverOp(ops[0]);
        expectNeverOp(ops[1]);
        expectNeverOp(ops[2]);
        expectNeverOp(ops[3]);
        expectAlsoKey(ops[4]);
        expectAlsoKey(ops[5]);
        expectParentNonKey(ops[6]);
        
        grandKids[1] = setupSiblings(ops[1], new int[] { 6 });
        Mock master = grandKids[1][0].mockStats; 
        expectChildKey(grandKids[1][0]);
        
        grandKids[2] = setupSiblings(ops[2], new int[] { 5 });
        expectNeverOp(grandKids[2][0]); // equal to parentmost operation
        grandKids[3] = setupSiblings(ops[3], new int[] { 6 });
        expectAlsoKey(grandKids[3][0]);
        grandKids[4] = setupSiblings(ops[4], new int[] { 5 });
        expectNeverOp(grandKids[4][0]);
        grandKids[6] = setupSiblings(ops[6], new int[] { 7 });
        expectAlsoKey(grandKids[6][0]);

        // this isn't counted as dispatch because it could never be an operation... 
//        master.expects(once()).method("recordDispatch").with(eq(ops[1].response)).after("recordEnd").isVoid();
        
        // same story here: these priority 4 responses aren't considered dispatch ?!
//        master.expects(once()).method("recordDispatch").with(eq(ops[2].response)).after("recordEnd").isVoid();
//        master.expects(once()).method("recordDispatch").with(eq(ops[3].response)).after("recordEnd").isVoid();

        // and this one isn't considered dispatch: they are operations!
//        master.expects(once()).method("recordDispatch").with(eq(ops[4].response)).after("recordEnd").isVoid();
        // this is also a real operation
//        master.expects(once()).method("recordDispatch").with(eq(ops[5].response)).after("recordEnd").isVoid();
        master.expects(once()).method("recordDispatch").with(eq(ops[6].response)).after("recordEnd").isVoid();

        summarizer.startedResponse(initial.response);
        for (int i=0; i<ops.length; i++) {
            summarizer.startedResponse(ops[i].response);
            for (int j=0; (grandKids[i] != null && j<grandKids[i].length); j++) {
                summarizer.startedResponse(grandKids[i][j].response);
                summarizer.finishedResponse(grandKids[i][j].response);
            }
            summarizer.finishedResponse(ops[i].response);
        }
        summarizer.finishedResponse(initial.response);        
    }
        
//    public void testNonOperationSibling() {
//    }
//    
//    public void testNonOperationRoot() {
//    }
//    public void testMismatched() {
//        mockResponseFactory.expects().method("getLastResponse").withNoArguments().will(returnValue(opA));
//    }
//    public void testResourceKeys() {
//    }
//    public void testSkipDupKeyChild() {
//    }
//    public void testSkipDupKeyMidLevel() {
//    }

    
    private MockOperation setupOneNested(Object priInit, Object priA) { 
        MockOperation opA = new MockOperation("nestedA", initial, Response.UI_CONTROLLER, StatisticsTypeImpl.UiRequest);

        // we don't assert the before/after ordering here!
        initial.expectInvocation(100000L);
        opA.expectInvocation(110000L);        

        initial.mockResponse.stubs().method("get").with(eq(Response.OPERATION_PRIORITY)).will(returnValue(priInit));
        opA.mockResponse.stubs().method("get").with(eq(Response.OPERATION_PRIORITY)).will(returnValue(priA));
        return opA;
    }
    
    private MockOperation[] setupSiblings(MockOperation parent, Object priA, Object priB) {
        return setupSiblings(parent, new Object[] { priA, priB });
    }
    
    private MockOperation[] setupSiblings(MockOperation parent, int priA, int priB) {
        return setupSiblings(parent, new int[] { priA, priB });
    }
    
    private MockOperation[] setupSiblings(MockOperation parent, int[] pris) {
        Object[] opris = new Object[pris.length];
        for (int i=0; i<pris.length; i++) {
            opris[i] = new Integer(pris[i]);
        }
        return setupSiblings(parent, opris);
    }
    
    private MockOperation[] setupSiblings(MockOperation parent, Object[] pris) { 
        
        MockOperation[] ops = new MockOperation[pris.length];
        for (int i=0; i<ops.length; i++) {
            ops[i] = new MockOperation("child "+i+" of "+parent.mockOperation, parent, Response.UI_CONTROLLER, StatisticsTypeImpl.UiRequest);
            ops[i].expectInvocation((i+3)*50000L);        

            ops[i].mockResponse.stubs().method("get").with(eq(Response.OPERATION_PRIORITY)).will(returnValue(pris[i]));
        }

        return ops;
    }
    
    private void expectTopOperation(MockOperation opA) {
        expectTopOperation(new MockOperation[] { opA });
    }
    
    private void expectTopOperation(MockOperation[] ops) {
        initial.mockStats.expects(once()).method("recordAsOperation").isVoid();
        initial.mockStats.stubs().method("isOperationThisRequest").after("recordAsOperation").will(returnValue(true));
        initial.mockStats.expects(once()).method("summarizeOperation").with(eq(initial.response), eq(initial.stats))
                .isVoid();
        for (int i=0; i<ops.length; i++) {
            expectNeverOp(ops[i]);
        }
    }
    
    private void expectChildKey(MockOperation op) {
        expectChildKey(op, 1);
    }
    
    private void expectChildKey(MockOperation op, int count) {
        op.mockStats.stubs().method("isOperationKey").will(returnValue(true));
        op.mockStats.expects(exactly(count)).method("recordAsOperation").isVoid();
        op.mockStats.stubs().method("isOperationThisRequest").after("recordAsOperation").will(returnValue(true));
        op.mockStats.expects(once()).method("summarizeOperation").with(eq(initial.response), eq(initial.stats))
                .isVoid();
        op.mockStats.expects(once()).method("recordDispatch").with(eq(initial.response)).after("recordEnd").isVoid();
    }        
    
    private void expectParentNonKey(MockOperation op) {
        op.mockStats.stubs().method("isOperationKey").will(returnValue(true));
    }
    
    private void expectAlsoKey(MockOperation op) {
        op.mockStats.stubs().method("isOperationKey").will(returnValue(true));
        op.mockStats.expects(once()).method("recordAsOperation").isVoid();
        op.mockStats.stubs().method("isOperationThisRequest").after("recordAsOperation").will(returnValue(true));
    }        
    
    private void expectNeverOp(MockOperation op) {
        op.mockStats.expects(once()).method("setOperationKey").with(eq(false)).isVoid();
        op.mockStats.stubs().method("isOperationKey").will(returnValue(false));
    }        
    
    private void runOneNested(MockOperation opA) {
        runNested(new MockOperation[] { opA });
    }
    
    private void runNested(MockOperation[] ops) {
        summarizer.startedResponse(initial.response);
        runSiblings(ops);
        summarizer.finishedResponse(initial.response);
    }

    private void runSiblings(MockOperation[] ops) {
        for (int i = 0; i < ops.length; i++) {
            summarizer.startedResponse(ops[i].response);
            summarizer.finishedResponse(ops[i].response);            
        }
    }

    private void setupEnsureValues(Mock mockResponse) {
        setupEnsureValues(mockResponse, "ensure");
    }
    
    private void setupEnsureValues(Mock mockResponse, String setRequest) {
        // ensure values...
        mockResponse.stubs().method("get").with(eq(Response.RESOURCE_KEY)).will(returnValue(null));
        mockResponse.expects(once()).method("set").with(eq(PerfStatsImpl.RECORDED), eq(new Integer(0))).isVoid();
        mockResponse.stubs().method("get").with(eq(Response.FAILURE_DATA)).will(returnValue(null));
        mockResponse.expects(once()).method("set").with(eq(Response.FAILURE_DATA), eq(null)).isVoid();
        mockResponse.stubs().method("get").with(eq(Response.REQUEST)).will(returnValue(null));
        mockResponse.expects(once()).method("set").with(eq(Response.REQUEST), eq(null)).id(setRequest);
    }
}
