/*
 * Copyright (c) 2005-2007 Glassbox Corporation, Contributors. All rights reserved. This program along with all
 * accompanying source code and applicable materials are made available under the terms of the Lesser Gnu Public License
 * v2.1, which accompanies this distribution and is available at http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.pojo;

import glassbox.analysis.api.OperationSummary;
import glassbox.track.api.OperationDescription;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class OperationDataTest extends MockObjectTestCase {
    private OperationData one;
    private Mock mockSummaryOne;
    private OperationSummary summaryOne;
    private Mock mockOpOne;    
    private OperationDescription opOne;
    private static final String OP_ONE_STR = "operation.OneKey";
    private static final String OP_ONE_NAME = "OneKeyNm";
    private static final String OP_ONE_APP = "operationOneApp";
    private static final String OP_ONE_URL = "rmi://192.1.1.23/service";
    
    public void setUp() {
        mockSummaryOne = mock(OperationSummary.class);
        summaryOne = (OperationSummary)mockSummaryOne.proxy();
        one = new OperationData("agentX", "x lives remotely", OP_ONE_URL, summaryOne);
        mockOpOne = mock(OperationDescription.class);
        opOne= (OperationDescription)mockOpOne.proxy();
        mockSummaryOne.stubs().method("getOperation").will(returnValue(opOne));
        mockOpOne.stubs().method("toString").will(returnValue(OP_ONE_STR));
        mockOpOne.stubs().method("getContextName").will(returnValue(OP_ONE_APP));
    }

    public void testGetOperationSummary() {
        assertEquals(summaryOne, one.getOperationSummary());
    }

    public void testGetKey() {
        String key = (String)one.getKey();
        assertTrue(key.indexOf(OP_ONE_STR)>=0);
        assertTrue(key.indexOf(OP_ONE_APP)>=0);
        assertTrue(key.indexOf(OP_ONE_URL)>=0);
    }

    public void testGetOperationName() {
        mockOpOne.expects(atLeastOnce()).method("getOperationName").will(returnValue(OP_ONE_NAME));
        assertTrue(one.getOperationName().indexOf(OP_ONE_NAME)>=0);
    }

    public void testGetOperationShortName() {
        mockOpOne.expects(atLeastOnce()).method("getShortName").will(returnValue("short"));
        assertTrue(one.getOperationShortName().indexOf("short")>=0);
    }

    public void testGetAgentUrl() {
        String test = "local:glassbox";
        assertEquals(OP_ONE_URL, one.getAgentUrl());
        one.setUrl(test);
        assertEquals(test, one.getAgentUrl());
    }

    public void testGetAverageExecutionTime() {
        mockSummaryOne.expects(atLeastOnce()).method("getAvgExecutionTime").will(returnValue(0.2));
        assertEquals("200 ms", one.getAverageExecutionTime());
    }

    //need to refactor message helper or use virtual mocks to test analysis functions
}
