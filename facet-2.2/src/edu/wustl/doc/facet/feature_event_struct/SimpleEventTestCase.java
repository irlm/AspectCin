package edu.wustl.doc.facet.feature_event_struct;

import edu.wustl.doc.utils.*;
import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

import junit.framework.Assert;
import junit.framework.TestSuite;

/**
 * Template class for simple CORBA event channel application.  Subclasses
 * that use this will have one event channel, one consumer, and one supplier.
 *
 * This class requires that the Event struct feature has been enabled
 * on the event channel.
 *
 */
public abstract class SimpleEventTestCase extends EventChannelTestCaseBase {

	public SimpleEventTestCase (String s)
	{
		super(s);
	}


	/**
	 * Handle an event that was received by the consumer thread.
	 * Return true if this is the last event and the test is over or
	 * false if more events should be expected.
	 *
	 * @param data an <code>Event</code>
	 * @return true if this is the last event
	 */
	public abstract boolean handleEvent (Event data);


	/**
	 * Called by the test case whenever it is ready to send another
	 * event to the consumer.  It should either return an Event or
	 * null to indicate that no more events should be sent.
	 *
	 * @return an <code>Event</code>
	 */
	public abstract Event getNextEventToSend (Object orb);


	/**
	 * Override this method if it is necessary to specify parameters
	 * to the connect_push_consumer call.
	 *
	 * @param pps the <code>ProxyPushSupplier</code>
	 * @param pc the <code>PushConsumer</code>
	 * @exception Throwable if an error occurs
	 */
	public void connectPushConsumer (ProxyPushSupplier pps,	PushConsumer pc) throws Throwable
	{
		pps.connect_push_consumer(pc);
	}

	/**
	 * Override if creating a differet kind of event consumer.
	 *
	 * @param pps a <code>ProxyPushSupplier</code> value
	 * @param orb an <code>ORB</code> value
	 * @return a <code>PushConsumerPOA</code> value
	 */
	public PushConsumerBase createSimpleEventConsumer (ProxyPushSupplier pps, Object orb)
	{
		return new SimpleEventConsumer (pps, orb);
	}

	/**
	 * Strategy to create and connect the test event channel consumer
	 */
	public class ConsumerStrategy extends EventChannelTestConsumer {

		public void createAndConnect (EventChannel ec, Object poa, Object orb) throws Throwable
		{
			ProxyPushSupplier pps = ec.for_consumers().obtain_push_supplier();

			PushConsumerBase consumerImpl = SimpleEventTestCase.this.createSimpleEventConsumer (pps, orb);

			PushConsumer pc = CorbaGetPushConsumerRef (poa, consumerImpl);

			connectPushConsumer (pps, pc);
		}
	}

	public class SimpleEventConsumer extends PushConsumerBase implements Upgradeable {
		
		protected ProxyPushSupplier pps_ = null;
		protected Object orb_ = null;
		private int eventCount_ = 0;

		/**
		 * Create a new SimpleEventConsumer instance.
		 *
		 * @param pps The proxy push supplier for the test event channel.
		 * @param orb The orb that we're using.
		 */
		public SimpleEventConsumer (ProxyPushSupplier pps, Object orb)
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
			if (SimpleEventTestCase.this.handleEvent (data)) {
				pps_.disconnect_push_supplier();

				// Kill ourselves.
				SimpleEventTestCase.this.CorbaShutdownORB (orb_, false);
			}
		}
	}

	/**
	 * Test event channel supplier
	 */
	public class SupplierStrategy implements EventChannelTestSupplier {

		public void runTest (EventChannel ec, Object orb) throws Throwable
		{
			ProxyPushConsumer ppc = ec.for_suppliers().obtain_push_consumer();

			while (true) {
				Event data = SimpleEventTestCase.this.getNextEventToSend (orb);

				if (data == null) 
					break;
				
				ppc.push (data);
			}
			
			ppc.disconnect_push_consumer();
		}
	}


}
