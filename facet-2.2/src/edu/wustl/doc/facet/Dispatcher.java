/*
 * $Id: Dispatcher.java,v 1.5 2003/08/21 16:40:12 ravip Exp $
 */

package edu.wustl.doc.facet;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

import edu.wustl.doc.facet.utils.concurrent.CopyOnWriteArrayList;
import java.util.Iterator;

/**
 * Implementation of the Event Channel's dispatcher.
 *
 * @author Frank Hunleth
 * @version $Revision: 1.5 $
 */
public class Dispatcher {

	private CopyOnWriteArrayList proxyPushSuppliers_ =
		new CopyOnWriteArrayList ();

	/**
	 * Register a consumer.
	 *
	 * @param pps An event channel local object that can receive events.
	 */
	protected void registerConsumer (EventCarrierPushConsumer pc)
	{
		proxyPushSuppliers_.add(pc);
	}

	/**
	 * Remove a consumer from the dispatcher.
	 *
	 * @param consumer The push consumer
	 */
	protected void removeConsumer (EventCarrierPushConsumer pps)
	{
		proxyPushSuppliers_.remove(pps);
	}

	/**
	 * Internal method used to push an event to all of the consumers.
	 *
	 * @param event The event
	 */
	protected void pushEvent (EventCarrier eventCarrier)
	{
		Iterator it = proxyPushSuppliers_.iterator();

		while (it.hasNext()) {
			EventCarrierPushConsumer pc = (EventCarrierPushConsumer) it.next();
 			pc.push (eventCarrier);
		}
	}
}
