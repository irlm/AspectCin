package edu.wustl.doc.facet.feature_event_any;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;
import junit.framework.Assert;

/**
 * Test a simple CORBA event channel application.  This test
 * uses one event channel, one consumer, and one supplier.
 *
 */
public class TestEventChannel extends EventChannelTestCaseBase {

	/**
	 * Create a new TestEventChannel instance.  Instances are
	 * usually created by the jUnit test framework.
	 *
	 * @param s The name of the test case
	 */
	public TestEventChannel(String s)
	{
		super(s);
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer {

		public void createAndConnect(EventChannel ec, Object poa, Object orb) throws Throwable
		{
			ProxyPushSupplier pps = ec.for_consumers().obtain_push_supplier();

			TestEventConsumer consumerImpl = new TestEventConsumer (pps, orb);

			PushConsumer pc = CorbaGetPushConsumerRef (poa, consumerImpl);
			
			pps.connect_push_consumer(pc);
		}
	}

	public class TestEventConsumer extends PushConsumerBase {
		
		private ProxyPushSupplier pps_ = null;
		private Object orb_ = null;
		private int eventCount_ = 0;

		public TestEventConsumer(ProxyPushSupplier pps, Object orb)
		{
			pps_ = pps;
			orb_ = orb;
		}

		/**
		 * Called by the event channel if we're disconnected.
		 */
		public void disconnect_push_consumer()
		{
			Assert.fail("Why was I disconnected????");
		}

		/**
		 * Called by the event channel whenever an event is received.
		 *
		 */
		public synchronized void push (java.lang.Object data)
		{
			int eventNum = ((Integer) data).intValue ();

			Assert.assertEquals (eventCount_, eventNum);

			eventCount_++;

			if (eventCount_ >= 100) {
				pps_.disconnect_push_supplier();
			}
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
				java.lang.Object data = new Integer (i);
				ppc.push (data);
			}

			ppc.disconnect_push_consumer();
		}
	}


	/**
	 * The actual jUnit test.  Create the threads and run them.
	 */
	public void testPushEventObject () {
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread();
		tct[1] = new ConsumerThread(new ConsumerStrategy());
		tct[2] = new SupplierThread(new SupplierStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {
		protected void addTestSuites(junit.framework.TestSuite suite) {
			suite.addTestSuite(TestEventChannel.class);
		}

		declare parents:
			((edu.wustl.doc.facet.event_any.TestEventChannel ||
			  edu.wustl.doc.facet.event_any.TestEventChannel.*) &&
			 !AddTests)
			implements Upgradeable;
	}
}
