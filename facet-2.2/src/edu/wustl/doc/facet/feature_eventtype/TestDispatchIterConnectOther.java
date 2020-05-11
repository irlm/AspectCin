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
 * properly when consumers connect other consumers when
 * processing an event.
 *
 */
public class TestDispatchIterConnectOther extends EventChannelTestCaseBase {

	private static final int TOTAL_ITERATIONS = 4;

	private CountedWait eventWaiter_;
	private Counter consumerCounter_;


	public TestDispatchIterConnectOther(String s)
	{
		super(s);
	}

	public static class CountedWait {
		private int num_;

		synchronized public void setCountToWaitFor(int num)
                {
			num_ = num;
		}

		synchronized public void doWait() throws InterruptedException
                {
			while (num_ > 0) {
				wait();
			}
		}

		synchronized public void doNotify()
                {
			num_--;
			if (num_ <= 0) {
				notify();
			}
		}
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer {

		public void createAndConnect(EventChannel ec, Object poa, Object orb) throws Throwable
		{
			TestDispatchIterConnectOther.this.consumerCounter_ = new Counter();

			ProxyPushSupplier pps = ec.for_consumers().obtain_push_supplier();

			TestDispatchIterConnectOtherConsumer consumerImpl =
				new TestDispatchIterConnectOtherConsumer (ec,
                                                                          pps,
                                                                          orb,
                                                                          poa,
                                                                          TestDispatchIterConnectOther.this.consumerCounter_,
                                                                          0);

			pps.connect_push_consumer (CorbaGetPushConsumerRef (poa, consumerImpl));
		}
	}

	public class TestDispatchIterConnectOtherConsumer extends PushConsumerBase
                implements Upgradeable {
		
		private EventChannel ec_;
		private ProxyPushSupplier pps_;
		private Object orb_;
		private Object poa_;
		private int eventCount_ = 1;
		private int consumerNumber_;
		private Counter counter_;

		public TestDispatchIterConnectOtherConsumer(EventChannel ec,
							    ProxyPushSupplier pps,
							    Object orb,
							    Object poa,
							    Counter counter,
							    int consumerNumber)
		{
			ec_ = ec;
			pps_ = pps;
			orb_ = orb;
			poa_ = poa;
			counter_ = counter;
			consumerNumber_ = consumerNumber;

			counter_.incrCount();
		}

		/**
		 * Called by the event channel if we're disconnected.
		 */
		public void disconnect_push_consumer ()
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

			if (consumerNumber_ == data.getHeader().getType()) {
				/* Connect more consumers */

				/* Figure out the index of the consumer to connect. */
				int index = 1;
				for (int i = 1; i <= consumerNumber_; i++)
					index = index + i;

				try {
					/* Create and connect the required number of consumers */
					for (int i = 0; i < consumerNumber_ + 1; i++) {
						ProxyPushSupplier newPps = ec_.for_consumers().obtain_push_supplier();
						
						TestDispatchIterConnectOtherConsumer consumerImpl =
							new TestDispatchIterConnectOtherConsumer (ec_, newPps, orb_,
                                                                                                  poa_, counter_, index);
                                                
						PushConsumer pc = null;
						try {
							pc = CorbaGetPushConsumerRef (poa_, consumerImpl);
						} catch (Throwable e) { }
							
						
						newPps.connect_push_consumer(pc);

						index = index + 1;
					}
					
				} catch (Exception e) {

					e.printStackTrace();
					Assert.fail ("Got unexpected exception!");
				}

			} else if (data.getHeader().getType() == -1) {

				pps_.disconnect_push_supplier();

				if (counter_.decrCount() == 0) {
					// We're the last ones, so kill the ORB.
					CorbaShutdownORB (orb_, false);
				}
			}
			
			TestDispatchIterConnectOther.this.eventWaiter_.doNotify();
		}
	}

	/**
	 * Test event channel supplier
	 */
	public class SupplierStrategy implements EventChannelTestSupplier {

		public void runTest(EventChannel ec, Object orb) throws Throwable
		{
			TestDispatchIterConnectOther.this.eventWaiter_ = new CountedWait();

			ProxyPushConsumer ppc = ec.for_suppliers().obtain_push_consumer();

			Counter counter = TestDispatchIterConnectOther.this.consumerCounter_;
			CountedWait eventWaiter = TestDispatchIterConnectOther.this.eventWaiter_;

			for (int i = 0; i < TOTAL_ITERATIONS; i++) {

				Event data = new Event();
				data.setHeader (new EventHeader());

				eventWaiter.setCountToWaitFor(counter.getCount());
				
				data.getHeader().setType (i);
				ppc.push (data); 

				/* Wait for all events to be received by consumers,
				 * in the case that they get sent asynchronously.
				 * If we don't wait, then the test may not exercise
				 * the desired conditions.
				 */
				eventWaiter.doWait();

			}

			/* Send the kill event */
			
			Event data = new Event();
			data.setHeader (new EventHeader());
			
			data.getHeader().setType (-1);
			ppc.push (data);

			ppc.disconnect_push_consumer ();
		}
	}

	/**
	 * The actual jUnit test.  Create the threads and run them.
	 */
	public void testDispatchIterConnectOther() {
		TestCaseRunnable tct[] = new TestCaseRunnable[3];
		tct[0] = new EventChannelThread();
		tct[1] = new SupplierThread(new SupplierStrategy());
		tct[2] = new ConsumerThread(new ConsumerStrategy());

		runTestCaseRunnables(tct);
	}

	static aspect AddTests extends TestSuiteAdder {
		protected void addTestSuites(TestSuite suite) {
			suite.addTestSuite(TestDispatchIterConnectOther.class);
		}

		declare parents:
			((edu.wustl.doc.facet.feature_eventtype.TestDispatchIterConnectOther ||
			  edu.wustl.doc.facet.feature_eventtype.TestDispatchIterConnectOther.*) &&
			 !AddTests)
			implements Upgradeable,
			edu.wustl.doc.facet.feature_eventheader.EventHeaderFeature;
	}
}
