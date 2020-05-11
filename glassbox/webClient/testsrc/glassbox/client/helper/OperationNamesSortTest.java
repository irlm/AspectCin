/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import glassbox.analysis.api.OperationSummary;

import org.jmock.Mock;

public class OperationNamesSortTest extends AbstractTestOperationSorter {
    protected Mock mockEarlyNamesFailing;
    protected Mock mockEarlyNames;
    protected Mock mockEarlyNamesAlt;
    protected Mock mockLateNames;
    
    public void setUp() {
        super.setUp();
        mockEarlyNamesFailing = mockData(OperationSummary.StatusFailing, "early failing");
        mockEarlyNames = mockData(OperationSummary.StatusOK, "early");
        mockEarlyNamesAlt = mockData(OperationSummary.StatusOK, "early alt");
        mockLateNames = mockData(OperationSummary.StatusOK, "late");
    }
    
    public void testAnalysis() {
        setUpNames("getAnalysis");        
        useSorter(ColumnHelper.ANALYSIS);
        assertOrdering();
    }
    
    public void testOperation() {
        setUpNames("getOperationShortName");        
        useSorter(ColumnHelper.OPERATION);
        assertOrdering();
    }

    public void testServer() {
        setUpNames("getAgentName");        
        useSorter(ColumnHelper.SERVER);
        assertOrdering();
    }
    
    protected void assertOrdering() {
        assertSelfComparisons();
        assertLess(mockEarlyNames, mockLateNames);
        assertLess(mockEarlyNamesFailing, mockEarlyNames);
        assertEquivalent(mockEarlyNames, mockEarlyNamesAlt);
    }
    
    protected void setUpNames(String method) {
        mockEarlyNames.expects(atLeastOnce()).method(method).will(returnValue("An early name"));
        mockEarlyNamesAlt.expects(atLeastOnce()).method(method).will(returnValue("An early name"));
        mockEarlyNamesFailing.expects(atLeastOnce()).method(method).will(returnValue("An early name"));
        mockLateNames.expects(atLeastOnce()).method(method).will(returnValue("an early name... really a later name"));
    }

}
