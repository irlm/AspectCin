package edu.wustl.doc.facet.feature_realtime_dispatcher;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;

import junit.framework.Assert;
import junit.framework.TestSuite;

interface Upgrade extends Upgradeable, RealtimeDispatcherFeature {};

/**
 * Test a simple CORBA event channel application.  This test
 * uses one event channel, one consumer, and one supplier.
 */
public class TestRealtimeDispatcher extends EventChannelTestCaseBase
        implements Upgrade {

	private CountedWait event_waiter;

	/**
	 * Create a new TestEventDepend instance.  Instances are
	 * usually created by the jUnit test framework.
	 *
	 * @param s The name of the test case
	 */
	public TestRealtimeDispatcher (String s)
	{
		super(s);
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer implements Upgrade  {

		public void createAndConnect (EventChannel ec, Object poa, Object orb) throws Throwable {

			ProxyPushSupplier pps = ec.for_consumers().obtain_push_supplier();

			TestRealtimeDispatcherConsumer consumerImpl
				= new TestRealtimeDispatcherConsumer (pps, orb);

			PushConsumer pc = CorbaGetPushConsumerRef (poa, consumerImpl);

			ConsumerQOS qos = new ConsumerQOS();
			qos.setDependencies (new Dependency [1]);
			qos.setDependency (0, new Dependency ());
			qos.getDependency (0).setHeader (new EventHeader ());
                        // only accept type 1 
			qos.getDependency (0).getHeader().setType(1);
                        // Let me live on the queue with priority 1 
			qos.getDependency (0).getHeader().setPriority(1);
                        // source filter : accept only id 2 
			qos.getDependency (0).getHeader().setSource (2);

			pps.connect_push_consumer (pc, qos);
		}
	}

	public static class CountedWait {
		private int num_;

		synchronized public void setCountToWaitFor (int num)
                {
			num_ = num;
		}

		synchronized public void doWait () throws InterruptedException
                {
			while (num_ > 0) {
				wait ();
			}
		}

		synchronized public void doNotify ()
                {
			num_--;
			if (num_ <= 0) {
				notify ();
			}
		}
	}

	public class TestRealtimeDispatcherConsumer extends PushConsumerBase implements Upgrade {

		private ProxyPushSupplier pps_ = null;
		private Object orb_ = null;
		private int eventCount_ = 1;

		/**
		 * Create a new TestEventDependConsumer instance.
		 *
		 * @param pps The proxy push supplier for the test event channel.
		 */
		public TestRealtimeDispatcherConsumer (ProxyPushSupplier pps, Object orb)
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
		 * @param data The event.
		 * @throws Disconnected If we have disconnected
		 *         from the event channel.
		 */
		public synchronized void push (Event data)
		{
			// Check that we only receive events that we expect.
			Assert.assertEquals (1, data.getHeader().getType ());
			Assert.assertEquals (2, data.getHeader().getSource ());
			
			eventCount_++;

			if (eventCount_ >= 5) {
				pps_.disconnect_push_supplier ();
				CorbaShutdownORB (orb_, false);
			}
		}
	}

	/**
	 * Test event channel supplier
	 */
	public class SupplierStrategy implements EventChannelTestSupplier, Upgrade  {

		public void runTest (EventChannel ec, Object orb) throws Throwable
		{
			TestRealtimeDispatcher.this.event_waiter = new CountedWait ();
			ProxyPushConsumer ppc = ec.for_suppliers().obtain_push_consumer();
			
			for (int i = 0; i < 10; i++) {

				Event data = new Event ();
				data.setHeader (new EventHeader ());
				data.getHeader().setType (i % 2);
				data.getHeader().setPriority ((i % 2) + 1);
				data.getHeader().setSource  (i % 3);
				
				ppc.push (data);
			}

			ppc.disconnect_push_consumer ();
		}
	}

	/**
	 * The actual jUnit test.  Create the threads and run them.
	 */
	public void testRealtimeDispatcherOne () {

		TestCaseRunnable tct [] = new TestCaseRunnable [3];
		tct[0] = new EventChannelThread ();
		tct[1] = new ConsumerThread (new ConsumerStrategy ());
		tct[2] = new SupplierThread (new SupplierStrategy ());

		runTestCaseRunnables (tct);
	}

	static aspect AddTests extends TestSuiteAdder {
		
		protected void addTestSuites (TestSuite suite) {
			suite.addTestSuite (TestRealtimeDispatcher.class);
		}
	}
}
