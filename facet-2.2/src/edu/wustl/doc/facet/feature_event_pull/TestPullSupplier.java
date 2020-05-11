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
public class TestPullSupplier extends EventChannelTestCaseBase {

	/**
	 * Create a new TestPullSupplier instance.  Instances are
	 * usually created by the jUnit test framework.
	 *
	 * @param s The name of the test case
	 */
	public TestPullSupplier(String s)
	{
		super (s);
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer {

		public void createAndConnect (EventChannel ec,
					      Object poa,
					      Object orb) throws Throwable
		{
			ProxyPushSupplier pps = ec.for_consumers().obtain_push_supplier ();
			TestEventConsumer consumerImpl = new TestEventConsumer (pps, orb);
			PushConsumer pc = CorbaGetPushConsumerRef (poa, consumerImpl);

			pps.connect_push_consumer (pc);
		}
	}

	public class TestEventConsumer extends PushConsumerBase {
		private ProxyPushSupplier pps_ = null;
		private Object orb_ = null;
		private int eventCount_ = 0;

		/**
		 * Create a new TestEventConsumer instance.
		 *
		 * @param pps The proxy push supplier for the test event channel.
		 * @param orb The orb that we're using.
		 * @param sk  The reference to the server kill on the event channel's
		 *            orb.
		 */
		public TestEventConsumer (ProxyPushSupplier pps, Object orb)
		{
			pps_ = pps;
			orb_ = orb;
		}

		/**
		 * Called by the event channel if we're disconnected.
		 */
		public void disconnect_push_consumer()
		{
			Assert.fail ("Why was I disconnected????");
		}

		/**
		 * Called by the event channel whenever an event is received.
		 *
		 * @param data The event.
		 * @throws Disconnected If we have disconnected
		 *         from the event channel.
		 */
		public synchronized void push (Event data) 
		{
			eventCount_++;

			if (eventCount_ >= 100) {
				pps_.disconnect_push_supplier();

				// Kill ourselves.
				CorbaShutdownORB (orb_, false);
			}
		}
	}


	/**
	 * Strategy to create and connect the test event channel supplier
	 */
	public class SupplierStrategy implements EventChannelTestSupplier {

		public void runTest (EventChannel ec, Object orb) throws Throwable
		{
			ProxyPullConsumer ppc = ec.for_suppliers().obtain_pull_consumer ();

			Object poa = CorbaGetAndActivatePOA (orb);

			TestEventSupplier supplierImpl = new TestEventSupplier (ppc, orb);

			PullSupplier ps = CorbaGetPullSupplierRef (poa, supplierImpl);

			ppc.connect_pull_supplier (ps);
			CorbaRunORB (orb);
		}
	}

	public class TestEventSupplier extends PullSupplierBase {
		private ProxyPullConsumer ppc_ = null;
		private Object orb_ = null;
		private int eventCount_ = 0;

		/**
		 * Create a new TestEventSupplier instance.
		 *
		 * @param ppc The proxy pull consumer for the test event channel.
		 * @param orb The orb that we're using.
		 */
		public TestEventSupplier(ProxyPullConsumer ppc, Object orb)
		{
			ppc_ = ppc;
			orb_ = orb;
		}

		/**
		 * Called by the event channel if we're disconnected.
		 */
		public void disconnect_pull_supplier()
		{
			Assert.fail("Why was I disconnected????");
		}

		/**
		 * Called by the event channel whenever an event is requested
		 *
		 * @param data The event.
		 * @throws Disconnected If we have disconnected
		 *         from the event channel.
		 */
		public synchronized Event pull ()
		{
			eventCount_++;

			if (eventCount_ >= 100) {
				ppc_.disconnect_pull_consumer();

				// Kill ourselves.
				CorbaShutdownORB (orb_, false);
			}

 			return new Event ();
		}

		public synchronized Event try_pull (BooleanHolder has_event)
		{
			Assert.fail ("Why is try_pull getting called?");
			has_event.value = false;
			return new Event();
		}
	}

	/**
	 * The actual jUnit test.  Create the threads and run them.
	 */
	public void testPullSupplier () {
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread();
		tct[1] = new ConsumerThread(new ConsumerStrategy());
		tct[2] = new SupplierThread(new SupplierStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {

		protected void addTestSuites (junit.framework.TestSuite suite) {
			suite.addTestSuite (TestPullSupplier.class);
		}

		declare parents:
			((edu.wustl.doc.facet.feature_event_pull.TestPullSupplier ||
			  edu.wustl.doc.facet.feature_event_pull.TestPullSupplier.*) &&
			 !AddTests)
			implements Upgradeable,
			edu.wustl.doc.facet.feature_event_struct.EventStructFeature;

	}
}
