package edu.wustl.doc.facet.feature_supplier_dispatch;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

import edu.wustl.doc.facet.utils.concurrent.CopyOnWriteArrayList;
import edu.wustl.doc.facet.utils.concurrent.ConcurrentReaderHashMap;
import java.util.Iterator;

/**
 * Implementation of the Type dispatcher
 *
 */
public class EventTypeDispatcher extends Dispatcher {

	private ConcurrentReaderHashMap dispatchTable_ =
		new ConcurrentReaderHashMap ();

	private CopyOnWriteArrayList defaultConsumers_ =
		new CopyOnWriteArrayList ();

	/**
	 * Register a consumer.
	 *
	 * @param pps The push consumer's proxy push supplier
	 */
	protected void registerConsumer(EventCarrierPushConsumer pps)
	{
		ConsumerQOS qos = pps.getConsumerQOS ();

		if (qos == null || qos.getDependenciesLength () == 0) {
			// Consumer wants to be notified for all events.
			defaultConsumers_.add (pps);
		} else {
			for (int i = 0; i < qos.getDependenciesLength(); i++) {
				Integer type = new Integer (qos.getDependency (i).getHeader().getType());

				CopyOnWriteArrayList consumerList =
					(CopyOnWriteArrayList) dispatchTable_.get (type);

				if (consumerList == null) {
					consumerList = new CopyOnWriteArrayList ();
					dispatchTable_.put (type, consumerList);
				}

				consumerList.add (pps);
			}
		}
	}

	/**
	 * Remove a consumer from the dispatcher.
	 *
	 * @param consumer The push consumer
	 */
	protected void removeConsumer (EventCarrierPushConsumer pps)
	{
		ConsumerQOS qos = pps.getConsumerQOS ();

		if (qos == null || qos.getDependenciesLength () == 0) {
			// Consumer wants to be notified for all events.
			defaultConsumers_.remove (pps);
		} else {
			for (int i = 0; i < qos.getDependenciesLength (); i++) {
				Integer type = new Integer (qos.getDependency (i).getHeader().getType());

				CopyOnWriteArrayList consumerList =
					(CopyOnWriteArrayList) dispatchTable_.get(type);

				// assert(consumerList != null);
				consumerList.remove (pps);
			}
		}
	}

	/**
	 * Internal method used to push an event to all of the consumers.
	 *
	 * @param event The event
	 */
	protected void pushEvent (EventCarrier eventCarrier)
	{
		Integer type = new Integer(eventCarrier.getEvent().getHeader().getType ());

		// Dispatch to those who registered for particular events.
		CopyOnWriteArrayList consumerList =
			(CopyOnWriteArrayList) dispatchTable_.get(type);

		if (consumerList != null) {
			iterateDispatchList(consumerList, eventCarrier);
		}

		// Dispatch to consumers that are listening to every
		// kind of event.
		iterateDispatchList(defaultConsumers_, eventCarrier);
	}

	/**
	 * Helper method to pushEvent.
	 *
	 * @param l the list to iterate over.
	 */
	protected void iterateDispatchList(CopyOnWriteArrayList l, EventCarrier eventCarrier)
	{
		Iterator it = l.iterator();

		while (it.hasNext()) {
			EventCarrierPushConsumer pps = (EventCarrierPushConsumer)it.next();
			pps.push(eventCarrier);
		}
	}
}
