/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import glassbox.analysis.api.OperationSummary;

import org.jmock.Mock;

public class OperationValuesSortTest extends AbstractTestOperationSorter {
    protected Mock mockLowValuesFailing;
    protected Mock mockLowValues;
    protected Mock mockLowValuesAlt;
    protected Mock mockHighValues;
    
    public void setUp() {
        super.setUp();
        mockLowValuesFailing = mockData(OperationSummary.StatusFailing, "low values failing");
        mockLowValues = mockData(OperationSummary.StatusOK, "low values");
        mockLowValuesAlt = mockData(OperationSummary.StatusOK, "low values alt");
        mockHighValues = mockData(OperationSummary.StatusOK, "high values");
    }
    
    public void testExecutions() {
        setupValues("getExecutions", new Integer(6), new Integer(5));
        useSorter(ColumnHelper.EXECUTIONS);
        
        assertSelfComparisons();
        assertLess(mockLowValues, mockHighValues);
        assertLess(mockLowValuesFailing, mockLowValues);
        assertEquivalent(mockLowValues, mockLowValuesAlt);
    }
    
    public void testAverageTime() {
        setupValues("getAverageExecutionTimeAsDouble", new Double(0.03), new Double(0.));
        useSorter(ColumnHelper.AVERAGE_TIME);
        
        assertSelfComparisons();
        assertLess(mockLowValues, mockHighValues);
        assertEquivalent(mockLowValuesFailing, mockLowValues);
        assertEquivalent(mockLowValues, mockLowValuesAlt);
    }    

    protected void setupValues(String method, Object low, Object high) {
        mockLowValuesFailing.expects(atLeastOnce()).method(method).will(returnValue(low));
        mockLowValues.expects(atLeastOnce()).method(method).will(returnValue(low));
        mockLowValuesAlt.expects(atLeastOnce()).method(method).will(returnValue(low));
        mockHighValues.expects(atLeastOnce()).method(method).will(returnValue(high));
    }        
}
