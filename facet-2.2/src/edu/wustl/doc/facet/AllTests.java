/*
 * $Id: AllTests.java,v 1.2 2003/07/11 19:38:38 ravip Exp $
 */

package edu.wustl.doc.facet;

import junit.framework.TestSuite;
import junit.framework.Test;
import junit.framework.AssertionFailedError;

/**
 * jUnit TestSuite to run all of the Event Channel tests.
 *
 * @author     Frank Hunleth
 * @version    $Revision: 1.2 $
 */
public class AllTests {
	
	public static class VerboseTestRunner extends junit.textui.TestRunner {

		public synchronized void addError (Test test, Throwable t)
		{
			super.addError(test,t);
			t.printStackTrace();
		}
		public synchronized void addFailure (Test test, AssertionFailedError t)
		{
			super.addFailure(test,t);
			t.printStackTrace();
		}
	}

	public static void main (String[] args)
	{
		System.out.println("Running all FACET test cases...");
		new VerboseTestRunner().doRun (suite(), false);
	}

	/**
         * Acts as a hook for all of the aspect * added test suites
	 */
	protected static void addTestSuites (TestSuite suite) { }

	public static Test suite()
	{
		TestSuite suite= new TestSuite("FACET Tests");

		/* Add in the tests that correspond to various aspects */
		addTestSuites (suite);

		return suite;
	}
}
