package edu.wustl.doc.facet.feature_ttl;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;

import junit.framework.Assert;
import junit.framework.TestSuite;

interface Upgrade extends Upgradeable, TtlFeature {};


public class TestEventTtl extends EventChannelTestCaseBase
        implements Upgrade {

        
	public TestEventTtl(String s)
	{
		super(s);
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer
                implements Upgrade {

		public void createAndConnect (EventChannel ec, Object poa, Object orb) throws Throwable
		{
			ProxyPushSupplier pps = ec.for_consumers().obtain_push_supplier();

			TestEventTtlConsumer consumerImpl = new TestEventTtlConsumer (pps, orb);

			PushConsumer pc = CorbaGetPushConsumerRef (poa, consumerImpl);

			pps.connect_push_consumer (pc);
		}
	}

	public class TestEventTtlConsumer extends PushConsumerBase implements Upgrade {
		private ProxyPushSupplier pps_ = null;
		private Object orb_ = null;
		private int eventCount_ = 0;

		/**
		 * Create a new TestEventTtlConsumer instance.
		 *
		 * @param pps The proxy push supplier for the test event channel.
		 * @param orb The orb that we're using.
		 * @param sk  The reference to the server kill on the event channel's
		 *            orb.
		 */
		public TestEventTtlConsumer (ProxyPushSupplier pps, Object orb)
		{
			pps_ = pps;
			orb_ = orb;
		}

		/**
		 * Called by the event channel if we're disconnected.
		 */
		public void disconnect_push_consumer ()
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
			// Check that the TTL is properly decremented.
			Assert.assertEquals (eventCount_, data.getHeader().getTtl ());

			eventCount_++;

			if (eventCount_ == 4) {
				pps_.disconnect_push_supplier ();

				CorbaShutdownORB (orb_, false);
			}
		}
	}

	/**
	 * Test event channel supplier
	 */
	public class SupplierStrategy implements EventChannelTestSupplier, Upgrade {

		public void runTest (EventChannel ec, Object orb) throws Throwable
		{
			ProxyPushConsumer ppc = ec.for_suppliers().obtain_push_consumer ();

			for (int i = 0; i < 5; i++) {
				Event data = new Event();
				data.setHeader (new EventHeader());
				
				data.getHeader().setTtl (i);
				ppc.push (data);
			}

			ppc.disconnect_push_consumer ();
		}
	}

	/**
	 * The actual jUnit test.  Create the threads and run them.
	 */
	public void testEventTtl () {
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread ();
		tct[1] = new ConsumerThread (new ConsumerStrategy());
		tct[2] = new SupplierThread (new SupplierStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {
		protected void addTestSuites(TestSuite suite) {
			suite.addTestSuite(TestEventTtl.class);
		}
	}
}
