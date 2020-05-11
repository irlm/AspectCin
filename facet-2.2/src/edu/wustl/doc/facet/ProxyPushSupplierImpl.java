/*
 * $Id: ProxyPushSupplierImpl.java,v 1.6 2003/08/20 20:50:39 ravip Exp $
 */

package edu.wustl.doc.facet;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

/**
 * Implementation of the ProxyPushSupplier interface.
 *
 * @author Frank Hunleth, Ravi Pratap
 * @version $Revision: 1.6 $
 */
public class ProxyPushSupplierImpl extends ProxyPushSupplierBase
        implements EventCarrierPushConsumer {

	private EventChannelImpl eventChannel_;
	private PushConsumer pushConsumer_;

	/**
	 * Constructor for the ProxyPushSupplierImpl object.
         *
         * @param ec    Reference to the EventChannelImpl
	 */
	protected ProxyPushSupplierImpl (EventChannelImpl ec)
	{
		eventChannel_ = ec;
	}

	/**
	 * Connect a user's push consumer to the event channel.
	 * (From the spec)
         * 
	 * @param  push_consumer  Reference to a PushConsumer
	 */
	public void connect_push_consumer (PushConsumer push_consumer)
	{
		eventChannel_.addPushConsumer(this);
		pushConsumer_ = push_consumer;
	}

	/**
	 * Disconnect from the event channel.
         * (From the spec)
	 */
	public void disconnect_push_supplier ()
	{
		if (pushConsumer_ != null) {
			eventChannel_.removePushConsumer(this);
			pushConsumer_ = null;
		}
	}

        /**
         * Accessor method to get at the PushConsumer associated
         * with this ProxyPushSupplier
         */
	public PushConsumer getPushConsumer ()
	{
		return pushConsumer_;
	}


	/**
	 * Internal method used to push an event to the consumers.
	 * (Public so that features can access this method.)
         *
         * @param eventCarrier  The event wrapped in an EventCarrier
	 */
	public void push (EventCarrier eventCarrier) 
	{
                pushConsumer_.push ();
	}
}
