package edu.wustl.doc.facet.feature_event_pull;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.*;

import org.omg.CORBA.BooleanHolder;

import edu.wustl.doc.facet.feature_event_struct.EventStructFeature;
import edu.wustl.doc.facet.utils.concurrent.LinkedQueue;

/**
 * Implementation of the ProxyPullSupplier interface.
 *
 * NOTE: This class is Upgradeable since it may have to
 *  create bogus events for the try_pull method.
 */
public class ProxyPullSupplierImpl extends ProxyPullSupplierBase
        implements Upgradeable, EventStructFeature {

	private EventChannelImpl eventChannel_;
	private PullConsumer pullConsumer_;
	private boolean connected_ = false;
	private EventHandler eventHandler_;
	private LinkedQueue eventQueue_;

	private static class EventHandler implements EventCarrierPushConsumer {
		private LinkedQueue eventQueue_;

		EventHandler(LinkedQueue eventq)
		{
			eventQueue_ = eventq;
		}

		public void push(EventCarrier eventCarrier)
		{
			try {
				// Enqueue event.
				eventQueue_.put (eventCarrier);
			} catch (InterruptedException ie) {
				// Ignore - We can't do anything about this except keep it
				// from propogating.
			}
		}
	}


	/**
	 *  Constructor for the ProxyPushSupplierImpl object.
	 */
	protected ProxyPullSupplierImpl(EventChannelImpl ec)
	{
		eventChannel_ = ec;
	}

	/**
	 * Connect a user's pull consumer to the event channel and
	 * enable queuing of events.
	 * pull_consumer can be null if there is no pull_consumer.
	 *
	 * @param  pull_consumer  Reference to a PushConsumer
	 */
	public synchronized void connect_pull_consumer (PullConsumer pull_consumer) 
	{
		if (!connected_) {
			eventQueue_ = new LinkedQueue();
			eventHandler_ = new EventHandler(eventQueue_);
			eventChannel_.addPushConsumer(eventHandler_);
			pullConsumer_ = pull_consumer;
			connected_ = true;
		}
	}

	/**
	 * Disconnect from the event channel.
	 */
	public synchronized void disconnect_pull_supplier()
	{
		if (connected_) {
			eventChannel_.removePushConsumer(eventHandler_);
			pullConsumer_ = null;
			connected_ = false;
			eventHandler_ = null;

			// reset out the queue
			eventQueue_ = null;
		}
	}

	/**
	 * Block until an event has been received and then
	 * return it to the caller.
	 *
	 * @return an <code>Event</code>
	 */
	public Event pull ()
	{
		if (eventQueue_ == null)
			return null;

		try {
			EventCarrier ec = (EventCarrier) eventQueue_.take();
			return ec.getEvent();

		} catch (InterruptedException ie) {
			return null;
		}
	}

	/**
	 * Check if an event is available.  If it is, set has_event
	 * and return the Event to the caller.  If no Event, reset
	 * has_event and return an empty event.
	 *
	 * @param has_event set to true if we're returning an Event
	 * @return an <code>Event</code>
	 */
	public Event try_pull (BooleanHolder has_event) 
	{
		if (eventQueue_ == null)
			return null;

		try {
			EventCarrier ec = (EventCarrier) eventQueue_.poll (0);
			if (ec != null) {
				has_event.value = true;
				return ec.getEvent();
			} else {
				has_event.value = false;
				return new Event();
			}

		} catch (InterruptedException ie) {
			return null;
		}
	}
}
