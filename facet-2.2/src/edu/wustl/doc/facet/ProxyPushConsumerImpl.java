/*
 * $Id: ProxyPushConsumerImpl.java,v 1.4 2003/08/25 19:08:13 ravip Exp $
 */

package edu.wustl.doc.facet;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

/**
 * Implementation of the ProxyPushConsumer interface.
 *
 * @author Frank Hunleth, Ravi Pratap
 * @version $Revision: 1.4 $
 */
public class ProxyPushConsumerImpl extends ProxyPushConsumerBase {
	
	protected EventChannelImpl eventChannel_;

	/**
	 * Constructor for the ProxyPushConsumerImpl object.
         *
         * @param ec    Reference to the {@link EventChannelImpl}
	 */
	protected ProxyPushConsumerImpl (EventChannelImpl ec)
        {
		eventChannel_ = ec;
	}

        public EventChannelImpl getEventChannel ()
        {
                return eventChannel_;
        }

	/**
	 * Connect a user's push supplier to the event channel.
	 * (From the spec)
         *
	 * @param  push_supplier  Reference to a PushSupplier
	 */
	public void connect_push_supplier (PushSupplier push_supplier)
        {

        }

	/**
	 * Disconnect from the event channel.
         * (From the spec)
	 */
	public void disconnect_push_consumer ()
        {

        }

	/**
	 * Push an event to the event channel.
	 * (From the spec)
	 */
	public void push ()
	{
		EventCarrier ec = new EventCarrier();
		eventChannel_.pushEvent (ec);
	}
}
