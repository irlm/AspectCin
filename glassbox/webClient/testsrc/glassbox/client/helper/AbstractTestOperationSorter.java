/*
 * Copyright (c) 2005-2006 Glassbox Corporation, Contributors. All rights reserved.
 * This program along with all accompanying source code and applicable materials are made available under the 
 * terms of the Lesser Gnu Public License v2.1, which accompanies this distribution and is available at 
 * http://www.gnu.org/licenses/lgpl.txt
 */
package glassbox.client.helper;

import glassbox.analysis.api.OperationSummary;
import glassbox.client.pojo.OperationData;

import java.util.*;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public abstract class AbstractTestOperationSorter extends MockObjectTestCase {

    protected List dataList;
    protected Comparator sorter;
    protected Comparator reverse;

    // we should probably just make OperationData an interface 
    protected static class TestOperationData extends OperationData {
        public TestOperationData() {
            super(null, null);
        }
    }

    public void setUp() {
        dataList = new ArrayList(20);
    }

    protected void assertEquivalent(Mock mockA, Mock mockB) {
        Object a=mockA.proxy();
        Object b=mockB.proxy();
        int delta = sorter.compare(a, b);
        assertTrue("Expected "+a+" == "+b+": "+delta, delta==0);        
    }

    protected void assertLess(Mock mockSmaller, Mock mockBigger) {
        Object smaller=mockSmaller.proxy();
        Object bigger=mockBigger.proxy();
        int delta = sorter.compare(smaller, bigger);
        assertTrue("Expected "+smaller+" < "+bigger+": "+delta, delta<0);
        delta = sorter.compare(bigger, smaller);
        assertTrue("Expected "+bigger+" > "+smaller+": "+delta, delta>0);
        
        delta = reverse.compare(smaller, bigger);
        assertTrue("Expected "+smaller+" r> "+bigger+": "+delta, delta>0);
        delta = reverse.compare(bigger, smaller);
        assertTrue("Expected "+bigger+" r< "+smaller+": "+delta, delta<0);        
    }

    protected Mock mockData(String role) {
        Mock m = mock(TestOperationData.class, role);
        dataList.add(m.proxy());
        return m;
    }

    protected Mock mockData(int status, String role) {
        Mock m = mockData(role);
        m.stubs().method("isOk").will(returnValue(status==OperationSummary.StatusOK));
        m.stubs().method("isSlow").will(returnValue(status==OperationSummary.StatusSlow));
        m.stubs().method("isFailing").will(returnValue(status==OperationSummary.StatusFailing));
        return m;
    }
    
    protected void useSorter(String name) {
        sorter = ColumnHelper.getSorter(name);
        reverse = ColumnHelper.getReverseSorter(name);
    }

    protected void assertSelfComparisons() {
        for (Iterator it=dataList.iterator(); it.hasNext();) {
            Object val = it.next();
            assertEquals(0, sorter.compare(val, val));
            assertEquals(0, reverse.compare(val, val));
        }
    }

    public AbstractTestOperationSorter() {
        super();
    }

}