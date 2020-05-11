package edu.wustl.doc.facet.feature_eventvector;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;

import junit.framework.Assert;
import junit.framework.TestSuite;

interface Upgrade extends Upgradeable, EventVectorFeature {};

/**
 * Test a simple CORBA event channel application.  This test
 * uses one event channel, one consumer, and one supplier.
 *
 */
public class TestEventVec extends EventChannelTestCaseBase
        implements Upgrade {

	public TestEventVec (String s)
	{
		super(s);
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer implements Upgrade {

		public void createAndConnect(EventChannel ec, Object poa, Object orb) throws Throwable
		{
			ProxyPushSupplier pps = ec.for_consumers().obtain_push_supplier();

			TestEventVecConsumer consumerImpl = new TestEventVecConsumer(pps, orb);
			PushConsumer pc = CorbaGetPushConsumerRef (poa, consumerImpl);

			pps.connect_push_consumer (pc);
		}
	}

	public class TestEventVecConsumer extends PushConsumerBase implements Upgrade {
		
		private ProxyPushSupplier pps_ = null;
		private Object orb_ = null;
		private int eventCount_ = 0;

		public TestEventVecConsumer(ProxyPushSupplier pps, Object orb)
		{
			pps_ = pps;
			orb_ = orb;
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
			eventCount_++;
			if (eventCount_ >= 10) {
				pps_.disconnect_push_supplier();

				// Kill ourselves.
				CorbaShutdownORB (orb_, false);
			}
		}

		public synchronized void push_vec (Event[] data)
		{
			Assert.fail("Vector push should never be called on the consumer");
                }
        }

	/**
	 * Test event channel supplier
	 */
	public class SupplierStrategy implements EventChannelTestSupplier, Upgrade {

		public void runTest(EventChannel ec, Object orb) throws Throwable
		{
			ProxyPushConsumer ppc = ec.for_suppliers().obtain_push_consumer();

			Event[] events = new Event[10];

			for (int i = 0; i < 10; i++) {
				events[i] = new Event();

				//
				// Header and type field may not be present, so can't count
				// on these.
				// events[i].header = new EventHeader();
				// events[i].header.type = i;
				//
			}
			
			ppc.push_vec (events);

			ppc.disconnect_push_consumer ();
		}
	}

	/**
	 * The actual jUnit test.  Create the threads and run them.
	 */
	public void testEventVec ()
	{
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread();
		tct[1] = new ConsumerThread(new ConsumerStrategy());
		tct[2] = new SupplierThread(new SupplierStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {

		protected void addTestSuites(TestSuite suite) {
			suite.addTestSuite(TestEventVec.class);
		}

		declare parents:
			((edu.wustl.doc.facet.feature_eventvector.TestEventVec ||
			  edu.wustl.doc.facet.feature_eventvector.TestEventVec.*) &&
			 !AddTests)
			implements Upgradeable,
			edu.wustl.doc.facet.feature_event_struct.EventStructFeature;
	}
}
