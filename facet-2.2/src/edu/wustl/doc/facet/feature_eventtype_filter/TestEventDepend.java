package edu.wustl.doc.facet.feature_eventtype_filter;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;

import junit.framework.Assert;
import junit.framework.TestSuite;

interface Upgrade extends Upgradeable, EventTypeFilterFeature {};

/**
 * Test a simple CORBA event channel application.  This test
 * uses one event channel, one consumer, and one supplier.
 *
 */
public class TestEventDepend extends EventChannelTestCaseBase
        implements Upgrade {

	public TestEventDepend(String s)
	{
		super(s);
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer implements Upgrade {

		public void createAndConnect (EventChannel ec, Object poa, Object orb) throws Throwable
		{
			ProxyPushSupplier pps = ec.for_consumers().obtain_push_supplier();

			TestEventDependConsumer consumerImpl = new TestEventDependConsumer(pps, orb);

			PushConsumer pc = CorbaGetPushConsumerRef (poa, consumerImpl);

			ConsumerQOS qos = new ConsumerQOS();
			qos.setDependencies (new Dependency[1]);
			qos.setDependency (0, new Dependency());
			qos.getDependency (0).setHeader (new EventHeader());
			qos.getDependency (0).getHeader().setType (1);  /* only accept type 1 */

			pps.connect_push_consumer (pc, qos);
		}
	}

	public class TestEventDependConsumer extends PushConsumerBase implements Upgrade {
		
		private ProxyPushSupplier pps_ = null;
		private Object orb_ = null;
		private int eventCount_ = 1;

		public TestEventDependConsumer (ProxyPushSupplier pps, Object orb)
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
		public synchronized void push(Event data)
		{
			// Check that we only receive events that we expect.
			Assert.assertEquals (1, data.getHeader().getType());

			eventCount_++;

			if (eventCount_ >= 5) {
				pps_.disconnect_push_supplier();

				// Kill ourselves.
				CorbaShutdownORB (orb_, false);
			}
		}
	}

	/**
	 * Test event channel supplier
	 */
	public class SupplierStrategy implements EventChannelTestSupplier, Upgrade  {

		public void runTest(EventChannel ec, Object orb) throws Throwable
		{
			ProxyPushConsumer ppc = ec.for_suppliers().obtain_push_consumer();
			
			for (int i=0; i < 10; i++) {
				
				Event data = new Event();
				data.setHeader (new EventHeader());
				
				data.getHeader().setType (i % 2);
				ppc.push (data);
			}

			ppc.disconnect_push_consumer();
		}
	}

	/**
	 * The actual jUnit test.  Create the threads and run them.
	 */
	public void testEventDependOne ()
	{
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread();
		tct[1] = new ConsumerThread(new ConsumerStrategy());
		tct[2] = new SupplierThread(new SupplierStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {
		protected void addTestSuites(TestSuite suite) {
			suite.addTestSuite(TestEventDepend.class);
		}
	}
}
