/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import glassbox.analysis.api.OperationSummary;

import org.jmock.Mock;

public class StatusSorterTest extends AbstractTestOperationSorter {
    
    protected Mock mockOkFast;
    protected Mock mockOkSlow;
    protected Mock mockOkAlsoSlow;
    protected Mock mockFail;
    protected Mock mockSlow;
    
    public void setUp() {
        super.setUp();
        mockOkFast = mockData(OperationSummary.StatusOK, "okFast");
        mockOkSlow = mockData(OperationSummary.StatusOK, "okSlow");
        mockOkAlsoSlow = mockData(OperationSummary.StatusOK, "okAlsoSlow");
        mockFail = mockData(OperationSummary.StatusFailing, "failing");
        mockFail.stubs().method("getAverageExecutionTimeAsDouble").will(returnValue(new Double(0.)));
        mockSlow = mockData(OperationSummary.StatusSlow, "slow");
        mockSlow.stubs().method("getAverageExecutionTimeAsDouble").will(returnValue(new Double(0.)));
        mockOkFast.stubs().method("getAverageExecutionTimeAsDouble").will(returnValue(new Double(10.)));
        mockOkSlow.stubs().method("getAverageExecutionTimeAsDouble").will(returnValue(new Double(1000.)));
        mockOkAlsoSlow.stubs().method("getAverageExecutionTimeAsDouble").will(returnValue(new Double(1000.)));
        useSorter(ColumnHelper.STATUS);
    }
    
    public void testSortStatusSelfComparison() {        
        assertSelfComparisons();
    }

    public void testSortStatusSlowOk() {
        assertLess(mockSlow, mockOkFast);
    }
    
    public void testSortStatusFailOk() {
        assertLess(mockFail, mockOkFast);
    }
    
    public void testSortStatusFailSlow() {
        assertLess(mockFail, mockSlow);
    }
    
    public void testSortStatusDifferentOk() {
        assertLess(mockOkSlow, mockOkFast);
    }

    public void testSortStatusEquivOk() {
        assertEquivalent(mockOkSlow, mockOkAlsoSlow);
    }
    
}

