package edu.wustl.doc.facet.feature_throughput_test;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;

import junit.framework.Assert;
import junit.framework.TestSuite;

/**
 * Simple test that tries to measure the throughput of the event
 * channel.  Note that it takes a long time so is not included
 * with the main Junit test case like.
 *
 */
public class ThroughputTestCase extends EventChannelTestCaseBase {

	public static final int WARMUP_EVENTS = 100;
	public static final int PERFORMANCE_EVENTS = 10000;

	public ThroughputTestCase(String s)
	{
		super(s);
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer {

		public void createAndConnect (EventChannel ec, Object poa, Object orb)
                        throws Throwable
		{
			ProxyPushSupplier pps = ec.for_consumers().obtain_push_supplier();
			PushConsumerBase consumerImpl = new ThroughputConsumer (pps, orb);

			PushConsumer pc = CorbaGetPushConsumerRef (poa, consumerImpl);
			pps.connect_push_consumer(pc);
		}
	}

	/**
	 * Test event channel supplier
	 */
	public class SupplierStrategy implements EventChannelTestSupplier {

		public void runTest(EventChannel ec, Object orb) throws Throwable
		{
			ProxyPushConsumer ppc = ec.for_suppliers().obtain_push_consumer();

			for (int i = 0; i < WARMUP_EVENTS; i++)
				ppc.push();
			
			for (int i = 0; i < PERFORMANCE_EVENTS; i++)
				ppc.push();
				
			// Done.
			ppc.disconnect_push_consumer();
		}
	}


	public class ThroughputConsumer extends PushConsumerBase {

		protected ProxyPushSupplier pps_ = null;
		protected Object orb_ = null;

		private long startTime_;
		private int eventCounter_ = 0;

		/**
		 * Create a new ThroughputConsumer instance.
		 *
		 * @param pps The proxy push supplier for the test event channel.
		 * @param orb The orb that we're using.
		 * @param sk  The reference to the server kill on the event channel's
		 *            orb.
		 */
		public ThroughputConsumer (ProxyPushSupplier pps, Object orb)
		{
			pps_ = pps;
			orb_ = orb;
		}

		/**
		 * Called by the event channel if we're disconnected.
		 */
		public void disconnect_push_consumer()
		{
			Assert.fail("Why was I disconnected ????");
		}

		public synchronized void push (org.omg.CORBA.Any a)
		{
			eventCounter_++;
			
			if (eventCounter_ == WARMUP_EVENTS) {
				// Start timing.
				startTime_ = System.currentTimeMillis();
			} else if (eventCounter_ == WARMUP_EVENTS + PERFORMANCE_EVENTS) {
				// Stop timing.
				
				long stopTime = System.currentTimeMillis();
				long diffTime = stopTime - startTime_;
				double throughput =
					(double) PERFORMANCE_EVENTS / ((double)diffTime / 1000.0);

				System.out.println ("\nTotal events transferred: " + PERFORMANCE_EVENTS);
				System.out.println ("Time: " + diffTime + " ms");
				System.out.println ("Throughput: " + throughput + " events/sec");
				// Shutdown...
				pps_.disconnect_push_supplier ();
				CorbaShutdownORB (orb_, false);
			}
		}

		public synchronized void push (Event e)
		{
			eventCounter_++;
			
			if (eventCounter_ == WARMUP_EVENTS) {
				// Start timing.
				startTime_ = System.currentTimeMillis();
			} else if (eventCounter_ == WARMUP_EVENTS + PERFORMANCE_EVENTS) {
				// Stop timing.
				
				long stopTime = System.currentTimeMillis();
				long diffTime = stopTime - startTime_;
				double throughput =
					(double) PERFORMANCE_EVENTS / ((double)diffTime / 1000.0);

				System.out.println ("\nTotal events transferred: " + PERFORMANCE_EVENTS);
				System.out.println ("Time: " + diffTime + " ms");
				System.out.println ("Throughput: " + throughput + " events/sec");
				// Shutdown...
				pps_.disconnect_push_supplier ();
				CorbaShutdownORB (orb_, false);
			}
		}
		
		public synchronized void push (java.lang.Object o)
		{
			eventCounter_++;
			
			if (eventCounter_ == WARMUP_EVENTS) {
				// Start timing.
				startTime_ = System.currentTimeMillis();
			} else if (eventCounter_ == WARMUP_EVENTS + PERFORMANCE_EVENTS) {
				// Stop timing.
				
				long stopTime = System.currentTimeMillis();
				long diffTime = stopTime - startTime_;
				double throughput =
					(double) PERFORMANCE_EVENTS / ((double)diffTime / 1000.0);

				System.out.println ("\nTotal events transferred: " + PERFORMANCE_EVENTS);
				System.out.println ("Time: " + diffTime + " ms");
				System.out.println ("Throughput: " + throughput + " events/sec");
				// Shutdown...
				pps_.disconnect_push_supplier ();
				CorbaShutdownORB (orb_, false);
			}
		}
				
		/**
		 * Called by the event channel whenever an event is received.
		 *
		 * @param data The event.
		 * @throws Disconnected If we have disconnected
		 *         from the event channel.
		 */
		public synchronized void push ()
		{
			eventCounter_++;

			if (eventCounter_ == WARMUP_EVENTS) {
				// Start timing.
				startTime_ = System.currentTimeMillis();
			} else if (eventCounter_ == WARMUP_EVENTS + PERFORMANCE_EVENTS) {
				// Stop timing.
				
				long stopTime = System.currentTimeMillis();
				long diffTime = stopTime - startTime_;
				double throughput =
					(double) PERFORMANCE_EVENTS / ((double)diffTime / 1000.0);

				System.out.println ("\nTotal events transferred: " + PERFORMANCE_EVENTS);
				System.out.println ("Time: " + diffTime + " ms");
				System.out.println ("Throughput: " + throughput + " events/sec");
				// Shutdown...
				pps_.disconnect_push_supplier ();
				CorbaShutdownORB (orb_, false);
			}
		}
	}

	/**
	 * The actual jUnit test.  Create the threads and run them.
	 */
	public void testThroughput()
	{
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread ();
		tct[1] = new ConsumerThread (new ConsumerStrategy());
		tct[2] = new SupplierThread (new SupplierStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {

		protected void addTestSuites(junit.framework.TestSuite suite) {
			suite.addTestSuite (ThroughputTestCase.class);
		}

		declare parents:
			((edu.wustl.doc.facet.feature_throughput_test.ThroughputTestCase ||
			  edu.wustl.doc.facet.feature_throughput_test.ThroughputTestCase.*) &&
			 !AddTests)
			implements Upgradeable;
	}

}
