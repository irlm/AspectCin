package edu.wustl.doc.facet.feature_realtime_dispatcher;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

import edu.wustl.doc.facet.utils.concurrent.CopyOnWriteArrayList;

import java.util.Stack;
import java.util.Iterator;

import javax.realtime.BoundAsyncEventHandler;
import javax.realtime.AsyncEvent;
import javax.realtime.PriorityParameters;

public class RealtimeDispatcher extends Dispatcher {

	private final int NUM_OF_PRIORITIES = 20;

	private EventQueue [] event_queues;
	private CopyOnWriteArrayList non_realtime_consumers;

	public RealtimeDispatcher ()
	{
		event_queues = new EventQueue [NUM_OF_PRIORITIES];
		
		for (int i = 0; i < NUM_OF_PRIORITIES; ++i) {
			event_queues [i] = new EventQueue ();
			event_queues [i].setPriority (i);
		}

		non_realtime_consumers = new CopyOnWriteArrayList ();
	}

	protected void registerConsumer (EventCarrierPushConsumer pps)
	{
		ConsumerQOS qos = pps.getConsumerQOS ();

		if (qos == null || qos.dependencies.length == 0) {
			non_realtime_consumers.add (pps);
		} else {

			//
			// Okay so to determine which priority we register the
			// consumer on, we examine only the first dependency's priority value
			//
			// This really is just my solution to the problem ;-)
			//
			
			int priority = qos.getDependency (0).getHeader().getPriority();

			if (priority == 0)
				non_realtime_consumers.add (pps);
			else
				event_queues [priority].registerConsumer (pps);
		}
	}

	protected void removeConsumer (EventCarrierPushConsumer pps)
	{
		ConsumerQOS qos = pps.getConsumerQOS ();

		if (qos == null || qos.dependencies.length == 0)
			non_realtime_consumers.remove (pps);
		else {
			int priority = qos.getDependency (0).getHeader().getPriority();

			if (priority == 0)
				non_realtime_consumers.remove (pps);
			else
				event_queues [priority].removeConsumer (pps);
		}
	}

	protected synchronized void pushEvent (EventCarrier eventCarrier)
	{
		int priority = eventCarrier.getEvent ().getHeader ().getPriority ();

		if (priority == 0) {
			//
			// Non-realtime events have the priority set to 0 
			//
			Iterator it = non_realtime_consumers.iterator ();
			
			while (it.hasNext ()) {
				EventCarrierPushConsumer pps = (EventCarrierPushConsumer) it.next ();
				pps.push (eventCarrier);
			}
			
		} else {
			//
			// Push it on all priority queues
			//
			for (int i = 1; i < NUM_OF_PRIORITIES; ++i)
				event_queues [i].pushEvent (eventCarrier);
		}
	}
}

public class EventQueue extends BoundAsyncEventHandler {
	
	private final int BASE_PRIORITY = 20;
	
	private CopyOnWriteArrayList consumers;
	private CopyOnWriteArrayList consumer_data;
	private AsyncEvent async_event;

	private boolean in_handler;
	
	public EventQueue ()
	{
		async_event = new AsyncEvent ();
		async_event.addHandler (this);

		consumers = new CopyOnWriteArrayList ();
		consumer_data = new CopyOnWriteArrayList ();
	}

	public void registerConsumer (EventCarrierPushConsumer pps)
	{
		consumers.add (pps);
	}

	public void removeConsumer (EventCarrierPushConsumer pps)
	{
		consumers.remove (pps);
	}

	public void handleAsyncEvent ()
	{
		getAndClearPendingFireCount ();

		for (int i = 0; i < consumer_data.size (); ++i) {
			EventCarrier carrier = (EventCarrier) consumer_data.get (i);

			Iterator it = consumers.listIterator ();
			
			while (it.hasNext ()) {
				EventCarrierPushConsumer pps = (EventCarrierPushConsumer) it.next ();
				pps.push (carrier);
			}
		}
		
		consumer_data.clear ();
	}

	protected synchronized void pushEvent (EventCarrier carrier)
	{
		consumer_data.add (carrier);
		async_event.fire ();
	}

	public synchronized void setPriority (int priority)
	{
		PriorityParameters priorityParameters = new PriorityParameters (BASE_PRIORITY + priority);
		this.setSchedulingParameters (priorityParameters);
	}

}
