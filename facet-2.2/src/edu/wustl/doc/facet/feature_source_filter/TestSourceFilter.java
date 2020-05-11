/*
 * $Id: TestSourceFilter.java,v 1.9 2003/08/20 20:50:39 ravip Exp $
 */

package edu.wustl.doc.facet.feature_source_filter;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;

import junit.framework.Assert;
import junit.framework.TestSuite;

interface Upgrade extends Upgradeable, SourceFilterFeature {};

/**
 * Test a simple CORBA event channel application.  This test
 * uses one event channel, one consumer, and one supplier.
 *
 */
public class TestSourceFilter extends EventChannelTestCaseBase
        implements Upgrade {

	public TestSourceFilter (String s)
	{
		super(s);
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer implements Upgrade  {

		public void createAndConnect (EventChannel ec, Object poa, Object orb) throws Throwable
		{
			ProxyPushSupplier pps = ec.for_consumers().obtain_push_supplier();

			TestSourceFilterConsumer consumerImpl = new TestSourceFilterConsumer (pps, orb);
			PushConsumer pc = CorbaGetPushConsumerRef (poa, consumerImpl);

			ConsumerQOS qos = new ConsumerQOS ();
			qos.setDependencies (new Dependency [1]);
			qos.setDependency (0, new Dependency ());
			qos.getDependency (0).setHeader (new EventHeader ());
                        // we only accept events from supplier whose id is 1 
			qos.getDependency (0).getHeader ().setSource (1);
			
			pps.connect_push_consumer (pc, qos);
		}
	}

	public class TestSourceFilterConsumer extends PushConsumerBase implements Upgrade {
		private ProxyPushSupplier pps_ = null;
		private Object orb_ = null;
		private int eventCount_ = 1;

		/**
		 * Create a new TestEventDependConsumer instance.
		 *
		 * @param pps The proxy push supplier for the test event channel.
		 * @param sk  The reference to the server kill on the event channel's
		 *            orb.
		 */
		public TestSourceFilterConsumer (ProxyPushSupplier pps, Object orb)
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
			// Check that we only receive events that we expect.
			Assert.assertEquals (1, data.getHeader().getSource());

			eventCount_++;

			// This number here is connected with how many events we expect to get
			if (eventCount_ >= 3) {
				pps_.disconnect_push_supplier();
				CorbaShutdownORB (orb_, false);
			}
		}
	}

	/**
	 * Test event channel supplier
	 */
	public class SupplierStrategy implements EventChannelTestSupplier, Upgrade  {

		public void runTest (EventChannel ec, Object orb) throws Throwable {
			ProxyPushConsumer ppc = ec.for_suppliers().obtain_push_consumer();
			
			Event data = new Event();
			data.setHeader (new EventHeader());

			for (int i = 0; i < 10; i++) {
				data.getHeader().setSource (i % 3);
				ppc.push (data);
			}

			ppc.disconnect_push_consumer();
		}
	}

	/**
	 * The actual jUnit test.  Create the threads and run them.
	 */
	public void testSourceFilterOne () {
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread ();
		tct[1] = new ConsumerThread(new ConsumerStrategy());
		tct[2] = new SupplierThread(new SupplierStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {
		
		protected void addTestSuites (TestSuite suite) {
			suite.addTestSuite (TestSourceFilter.class);
		}
	}
}
