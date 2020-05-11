package edu.wustl.doc.facet.feature_eventvector;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.utils.*;
import edu.wustl.doc.facet.feature_event_struct.*;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.utils.*;
import junit.framework.Assert;
import junit.framework.TestSuite;

/**
 * Template class for simple CORBA event channel application.  Subclasses
 * that use this will have one event channel, one consumer, and one supplier.
 *
 * This class requires that the EventStructFeature has been enabled
 * on the event channel.
 */
abstract public class SimpleEventVecTestCase extends SimpleEventTestCase
        implements Upgradeable {


	public SimpleEventVecTestCase(String s)
	{
		super(s);
	}


	/**
	 * Handle an event that was received by the consumer thread.
	 * Return true if this is the last event and the test is over or
	 * false if more events should be expected.
	 *
	 * @param data  an <code>Event</code> array
	 * @return true if this is the last event
	 */
	public abstract boolean handleEvents (Event[] data);

	public PushConsumerBase createSimpleEventConsumer (ProxyPushSupplier pps, Object orb)
	{
		return new SimpleEventVecConsumer (pps, orb);
	}

	public class SimpleEventVecConsumer extends SimpleEventConsumer
                implements Upgradeable {

		public SimpleEventVecConsumer (ProxyPushSupplier pps, Object orb)
		{
			super (pps, orb);
		}

		/**
		 * Called by the event channel whenever an event is received.
		 *
		 * @param data The events.
		 * @throws Disconnected If we have disconnected
		 *         from the event channel.
		 */
		public void push_vec (Event[] data)
		{
			if (SimpleEventVecTestCase.this.handleEvents(data)) {
				pps_.disconnect_push_supplier ();

				// Kill ourselves.
				SimpleEventVecTestCase.this.CorbaShutdownORB (orb_, false);
			}
		}

		public void push (Event data)
		{
			Assert.fail ("This is not supposed to be called in this configuration !");
		}
	}
}
