package edu.wustl.doc.facet.utils;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;

/**
 * Test a simple Event channel application.  This test
 * uses one event channel, one consumer, and one supplier.
 *
 * @author     Ravi Pratap
 * @version    $Revision: 1.4 $
 */
public class EventChannelTestCase extends FacetMTTestCase {

	/**
	 * Create a new EventChannelTestCase instance.  Instances are
	 * usually created by the jUnit test framework.
	 *
	 * @param s The name of the test case
	 */
	public EventChannelTestCase (String s)
	{
		super (s);
	}

	/**
	 * Simple syncronized counter utility class.  This comes in handy
	 * any many tests.
	 */
	static public class Counter {
		private int count_ = 0;

		public synchronized int incrCount()
		{
			count_ = count_ + 1;
			return count_;
		}

		public synchronized int decrCount()
		{
			count_ = count_ - 1;
			return count_;
		}

		public synchronized int getCount()
		{
			return count_;
		}
	}

	/**
	 * The test event channel.
	 */
	public class EventChannelThread extends TestCaseRunnable {

		/**
		 * Initialize and run the event channel.
		 */
		public void runTestCase() throws Throwable {
			
			EventChannel evImpl = new EventChannelImpl ();

			EventChannelTestCase.this.addJavaObjectRef ("EventChannel", evImpl);
			EventChannelTestCase.this.notifyInitializationBarrier ("Channel");

			assertNotNull (evImpl);
		}
	}

	/**
	 * The test event consumer.
	 */
	protected class ConsumerThread extends TestCaseRunnable {
		
		private EventChannelTestConsumer consumer_;

		public ConsumerThread (EventChannelTestConsumer consumer)
		{
			consumer_ = consumer;
		}

		/**
		 * Run the event consumer.
		 */
		public void runTestCase() throws Throwable
		{
			// Wait for the event channel to be initialized.
			EventChannelTestCase.this.waitOnInitializationBarrier("Channel");

			EventChannel ec =
                                (EventChannel) EventChannelTestCase.this.getJavaObjectRef ("EventChannel");
			assertNotNull(ec);

			consumer_.createAndConnect (ec, null, null);
			EventChannelTestCase.this.notifyInitializationBarrier ("Consumer");
			consumer_.preRun ();
		}
	}

	/**
	 * The test event supplier.
	 */
	protected class SupplierThread extends TestCaseRunnable {
		
		private EventChannelTestSupplier supplier_;

		public SupplierThread(EventChannelTestSupplier supplier)
		{
			supplier_ = supplier;
		}

		public void runTestCase () throws Throwable
		{
			// Wait until the channels and the consumers have initialized.
			EventChannelTestCase.this.waitOnInitializationBarrier("Channel");
			EventChannelTestCase.this.waitOnInitializationBarrier("Consumer");

			EventChannel ec = (EventChannel) EventChannelTestCase.this.getJavaObjectRef ("EventChannel");
			assertNotNull(ec);

			// Let the test case's supplier send its events.
			supplier_.runTest (ec, null);
		}
	}

	/*
	 * Intercept the call to runTestCaseRunnables and initialize
	 * the barriers depending on the number of threads.
	 */
	protected void runTestCaseRunnables (final TestCaseRunnable[] runnables) {
		int numEventChannels = 0;
		int numConsumers = 0;
		int numSuppliers = 0;

		for (int i = 0; i < runnables.length; i++) {

			if (runnables[i] instanceof EventChannelThread)
				numEventChannels++;
			else if (runnables[i] instanceof ConsumerThread) 
				numConsumers++;
			else if (runnables[i] instanceof SupplierThread) 
				numSuppliers++;
		}

		createInitializationBarrier("Channel", numEventChannels);
		createInitializationBarrier("Consumer", numConsumers);

		super.runTestCaseRunnables(runnables);
	}
}
