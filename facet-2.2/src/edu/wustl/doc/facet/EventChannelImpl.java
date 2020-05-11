/*
 * $Id: EventChannelImpl.java,v 1.5 2003/08/20 20:50:39 ravip Exp $
 */

package edu.wustl.doc.facet;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

/**
 * Implementation of the EventChannel.
 *
 * @author Frank Hunleth, Ravi Pratap
 * @version $Revision: 1.5 $
 */
public class EventChannelImpl extends EventChannelBase {

	private ConsumerAdmin consumerAdmin_;
	private SupplierAdmin supplierAdmin_;
	private Dispatcher dispatcher_;

        /**
         * Constructor for the EventChannelImpl object (CORBA enabled)
         *
         * @param orb   Reference to the CORBA ORB.
         * @param poa   Reference to the POA for the EventChannel object
         */
	public EventChannelImpl (Object orb, Object poa)
	{
		ConsumerAdminImpl consumerAdminImpl = new ConsumerAdminImpl (this, poa);
		SupplierAdminImpl supplierAdminImpl = new SupplierAdminImpl (this, poa);

		try {
			consumerAdmin_ = CorbaGetConsumerAdminRef (poa, consumerAdminImpl);
			supplierAdmin_ = CorbaGetSupplierAdminRef (poa, supplierAdminImpl);

		} catch (Throwable e) {
			e.printStackTrace ();
		}

		//
		// Create a dispatcher instance.  This may be overriden by
		// aspects to support more and more complicated dispatchers.
		//
		dispatcher_ = new Dispatcher ();
	}

        /**
         * Constructor for the EventChannelImpl object (CORBA disabled)
         */
	public EventChannelImpl ()
	{
		this (null, null);
	}

        /**
         * Returns the reference to the ConsumerAdmin object doing the right thing
         * for the CORBA and non-CORBA cases.
         *
         * @param poa   The POA
         * @param impl  Reference to the ConsumerAdminImpl object 
         */
	public ConsumerAdmin CorbaGetConsumerAdminRef (Object poa,
                                                       ConsumerAdminBase impl) throws Throwable 
	{
		// Empty - filled in by aspects
		return null;
	}

        /**
         * Returns the reference to the SupplierAdmin object doing the right thing
         * for the CORBA and non-CORBA cases.
         *
         * @param poa   The POA
         * @param impl  Reference to the SupplierAdminImpl object
         */
	public SupplierAdmin CorbaGetSupplierAdminRef (Object poa,
                                                       SupplierAdminBase impl) throws Throwable
	{
		// Empty
		return null;
	}

	/**
	 * Forcibly destroy the event channel.
         * (From the spec)
	 */
	public void destroy () { }

	/**
	 * Return the ConsumerAdmin instance associated with this
	 * event channel. (From the spec)
	 *
	 * @return ConsumerAdmin
	 */
	public ConsumerAdmin for_consumers ()
	{
		return consumerAdmin_;
	}

	/**
	 * Return the SupplierAdmin instance associated with this
	 * event channel. (From the spec)
	 *
	 * @return SupplierAdmin
	 */
	public SupplierAdmin for_suppliers ()
	{
		return supplierAdmin_;
	}

	/**
	 * Attach a push consumer to the event channel. (Internal)
         * 
	 * @param consumer The push consumer
	 */
	public void addPushConsumer (EventCarrierPushConsumer pps)
	{
		dispatcher_.registerConsumer (pps);
	}

	/**
	 * Remove a push consumer from the event channel. (Internal)
	 * 
	 * @param consumer The push consumer
	 */
	public void removePushConsumer (EventCarrierPushConsumer pps)
	{
		dispatcher_.removeConsumer (pps);
	}

	/**
	 * Push an event to all of the consumers. (Internal)
	 *
	 * @param eventCarrier The event wrapped in an {@link EventCarrier}
	 */
	public void pushEvent (EventCarrier eventCarrier)
	{
		dispatcher_.pushEvent (eventCarrier);
	}
}
