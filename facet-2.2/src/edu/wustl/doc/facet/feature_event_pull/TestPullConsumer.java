package edu.wustl.doc.facet.feature_event_pull;


import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;

import junit.framework.Assert;
import junit.framework.TestSuite;

/**
 * Test a simple CORBA event channel application.  This test
 * uses one event channel, one consumer, and one supplier.
 *
 */
public class TestPullConsumer extends EventChannelTestCaseBase {

	/**
	 * Create a new TestPullConsumer instance.  Instances are
	 * usually created by the jUnit test framework.
	 *
	 * @param s The name of the test case
	 */
	public TestPullConsumer(String s)
	{
		super(s);
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer {
		private ProxyPullSupplier pps_;
		private Object orb_;

		public void createAndConnect (EventChannel ec,
					      Object poa,
					      Object orb) throws Throwable
		{
			pps_ = ec.for_consumers ().obtain_pull_supplier ();

			// Connect so that events start queue, but there is no actual
			// PullConsumer instance here.
			pps_.connect_pull_consumer (null);
			orb_ = orb;
		}

		public void preRun()
		{
			// Pull 100 events...
			for (int eventCount = 0; eventCount < 100; eventCount++) {
				Event e = pps_.pull ();
			}

			// Make sure that we didn't get too many.
			BooleanHolder hasEvent = new BooleanHolder ();
			Event e = pps_.try_pull (hasEvent);

			if (hasEvent.value) 
				Assert.fail("Got an extra event - strange?");
		

			// Disconnect and shutdown the ORB...
			pps_.disconnect_pull_supplier();
			CorbaShutdownORB (orb_, false);
		}
	}


	/**
	 * Test event channel supplier
	 */
	public class SupplierStrategy implements EventChannelTestSupplier {

		public void runTest (EventChannel ec, Object orb) throws Throwable
		{
			ProxyPushConsumer ppc = ec.for_suppliers().obtain_push_consumer();

			for (int i = 0; i < 100; i++) { 
				Event data = new Event ();
				ppc.push (data);
			}

			ppc.disconnect_push_consumer ();
		}
	}


	/**
	 * The actual jUnit test.  Create the threads and run them.
	 */
	public void testPullConsumer () {
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread();
		tct[1] = new ConsumerThread(new ConsumerStrategy());
		tct[2] = new SupplierThread(new SupplierStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {
		protected void addTestSuites (junit.framework.TestSuite suite) {
			suite.addTestSuite (TestPullConsumer.class);
		}

		declare parents:
			((edu.wustl.doc.facet.feature_event_pull.TestPullConsumer ||
			  edu.wustl.doc.facet.feature_event_pull.TestPullConsumer.*) &&
			 !AddTests)
			implements Upgradeable,
			edu.wustl.doc.facet.feature_event_struct.EventStructFeature;

	}
}
