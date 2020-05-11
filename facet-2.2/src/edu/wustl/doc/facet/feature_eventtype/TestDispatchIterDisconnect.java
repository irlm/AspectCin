package edu.wustl.doc.facet.feature_eventtype;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;

import junit.framework.Assert;
import junit.framework.TestSuite;

/**
 * This unittest verifies that the dispatch iterators work
 * properly when consumers disconnect themselves
 *
 */
public class TestDispatchIterDisconnect extends EventChannelTestCaseBase {

	private static final int TOTAL_CONSUMERS = 10;


	public TestDispatchIterDisconnect(String s)
	{
		super(s);
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer {

		public void createAndConnect(EventChannel ec, Object poa, Object orb) throws Throwable
		{
			Counter counter = new Counter();
			for (int i = 0; i < TOTAL_CONSUMERS; i++) {
				ProxyPushSupplier pps = ec.for_consumers().obtain_push_supplier();

				TestDispatchIterDisconnectConsumer consumerImpl =
					new TestDispatchIterDisconnectConsumer (pps, orb, counter, i);

				PushConsumer pc = CorbaGetPushConsumerRef (poa, consumerImpl);

				pps.connect_push_consumer (pc);
			}
		}
	}

	public class TestDispatchIterDisconnectConsumer extends PushConsumerBase {
		
		private ProxyPushSupplier pps_ = null;
		private Object orb_ = null;
		private int eventCount_ = 1;
		private int consumerNumber_;
		private Counter counter_;

		public TestDispatchIterDisconnectConsumer(ProxyPushSupplier pps,
							  Object orb,
							  Counter counter,
							  int consumerNumber)
		{
			pps_ = pps;
			orb_ = orb;
			counter_ = counter;
			consumerNumber_ = consumerNumber;

			counter_.incrCount();
		}

		public void disconnect_push_consumer()
		{
			Assert.fail("Why was I disconnected????");
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
			pps_.disconnect_push_supplier ();

			if (counter_.decrCount() == 0) {
				// Kill ourselves.
				CorbaShutdownORB (orb_, false);
			}
		}
	}

	/**
	 * Test event channel supplier
	 */
	public class SupplierStrategy implements EventChannelTestSupplier {

		public void runTest(EventChannel ec, Object orb) throws Throwable
		{
			ProxyPushConsumer ppc = ec.for_suppliers().obtain_push_consumer();

			/* Send the kill event */
			Event data = new Event();
			data.setHeader (new EventHeader());
			data.getHeader().setType(0);
			ppc.push (data);

			ppc.disconnect_push_consumer();
		}
	}

	/**
	 * The actual jUnit test.  Create the threads and run them.
	 */
	public void testDispatchIterDisconnect() {
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread();
		tct[1] = new SupplierThread(new SupplierStrategy());
		tct[2] = new ConsumerThread(new ConsumerStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {
		protected void addTestSuites(TestSuite suite) {
			suite.addTestSuite(TestDispatchIterDisconnect.class);
		}

		declare parents:
			((edu.wustl.doc.facet.feature_eventtype.TestDispatchIterDisconnect ||
			  edu.wustl.doc.facet.feature_eventtype.TestDispatchIterDisconnect.*) &&
			 !AddTests)
			implements Upgradeable,
			edu.wustl.doc.facet.feature_eventheader.EventHeaderFeature;
	}
}
