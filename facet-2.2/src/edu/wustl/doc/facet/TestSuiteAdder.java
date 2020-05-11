/*
 * $Id: TestSuiteAdder.java,v 1.1 2002/09/28 19:58:28 ravip Exp $
 */

package edu.wustl.doc.facet;

import junit.framework.TestSuite;

public abstract aspect TestSuiteAdder {
    abstract protected void addTestSuites(TestSuite suite);

    private pointcut addTestSuitesCut(TestSuite suite) :
        execution(void AllTests.addTestSuites(TestSuite)) && args(suite);

    before (TestSuite suite) : addTestSuitesCut(suite) {
        this.addTestSuites(suite);
    }
}
