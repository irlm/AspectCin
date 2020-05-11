package edu.wustl.doc.facet.feature_eventtype_filter;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;
import junit.framework.Assert;
import junit.framework.TestSuite;

/**
 * Test a simple CORBA event channel application.  This test
 * uses one event channel, many consumers, and one supplier.
 *
 */
public class TestEventDependMulti extends EventChannelTestCaseBase
        implements Upgrade {

	private static final int TOTAL_CONSUMERS = 10;

	public TestEventDependMulti(String s)
	{
		super(s);
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer implements Upgrade {

		public void createAndConnect(EventChannel ec, Object poa, Object orb) throws Throwable
		{
			Counter counter = new Counter ();

			for (int i = 1; i <= TOTAL_CONSUMERS; i++) {
				ProxyPushSupplier pps = ec.for_consumers ().obtain_push_supplier ();

				TestEventDependMultiConsumer consumerImpl =
					new TestEventDependMultiConsumer (pps, orb, counter, i);

				PushConsumer pc = CorbaGetPushConsumerRef (poa, consumerImpl);

				ConsumerQOS qos = new ConsumerQOS();
				qos.setDependencies (new Dependency[i]);

				/* Accept all events with types < consumerNumber_ */
				for (int j = 0; j < i; j++) {
                                        qos.setDependency (j, new Dependency());
					qos.getDependency (j).setHeader (new EventHeader());
					qos.getDependency (j).getHeader().setType (j);
				}

				pps.connect_push_consumer (pc, qos);
			}
		}
	}

	public class TestEventDependMultiConsumer extends PushConsumerBase implements Upgrade {
		
		private ProxyPushSupplier pps_ = null;
		private Object orb_ = null;
		private int eventCount_ = 1;
		private Counter counter_;
		private int consumerNumber_;


		public TestEventDependMultiConsumer (ProxyPushSupplier pps,
                                                     Object orb,
                                                     Counter counter,
                                                     int consumerNumber)
		{
			pps_ = pps;
			orb_ = orb;
			counter_ = counter;
			consumerNumber_ = consumerNumber;

			counter_.incrCount ();
		}

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
			// Check that we only receive events that we expect.

                        Assert.assertTrue (data.getHeader().getType() < consumerNumber_);
			
			if (data.getHeader().getType() == 0) {
				pps_.disconnect_push_supplier();

				if (counter_.decrCount() == 0) {
					// Kill ourselves.
					CorbaShutdownORB (orb_, false);
				}
			}
		}
	}

	/**
	 * Test event channel supplier
	 */
	public class SupplierStrategy implements EventChannelTestSupplier, Upgrade  {

		public void runTest (EventChannel ec, Object orb) throws Throwable
		{
			ProxyPushConsumer ppc = ec.for_suppliers ().obtain_push_consumer ();

			for (int i = 1; i < 50; i++) {

				Event data = new Event();
				data.setHeader (new EventHeader());
				
				data.getHeader().setType (i);
				ppc.push (data);
			}

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
	public void testEventDependMulti ()
	{
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread();
		tct[1] = new SupplierThread(new SupplierStrategy());
		tct[2] = new ConsumerThread(new ConsumerStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {
		protected void addTestSuites(TestSuite suite) {
			suite.addTestSuite(TestEventDependMulti.class);
		}
	}
}
