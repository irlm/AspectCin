/*
 * $Id: ConsumerAdminImpl.java,v 1.5 2003/08/20 20:50:39 ravip Exp $
 */

package edu.wustl.doc.facet;

import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

/**
 * Implementation of the ConsumerAdmin interface to
 * the event channel.
 *
 * @author Frank Hunleth, Ravi Pratap
 * @version $Revision: 1.5 $
 */
public class ConsumerAdminImpl extends ConsumerAdminBase {

	private Object poa_;
	private EventChannelImpl eventChannel_;

	/**
	 *  Constructor for the ConsumerAdminImpl object.
         *
         * @param ec    Reference to the {@link EventChannelImpl}
         * @param poa   The Object
	 */
	protected ConsumerAdminImpl (EventChannelImpl ec, Object poa)
	{
		eventChannel_ = ec;
		poa_ = poa;
	}

	/**
	 * Obtain a proxy push supplier to be able to connect a
	 * push consumer to an event channel.
	 * (From the spec)
         *
	 * @return ProxyPushSupplier The push supplier.
	 */
	public ProxyPushSupplier obtain_push_supplier ()
	{
		ProxyPushSupplierImpl ppsImpl = new ProxyPushSupplierImpl (eventChannel_);
		ProxyPushSupplier ref = null;
		
		try {
			ref = CorbaGetProxyPushSupplierRef (poa_, ppsImpl);
			
		} catch (Throwable se) {
			se.printStackTrace();
		}

		return ref;
	}

        /**
         * Return a reference to the ProxyPushSupplier doing the right thing
         * in the CORBA and no-CORBA case
         *
         * @param poa   The Object
         * @param impl  Reference to the ProxyPushSupplierImpl object
         */
	public ProxyPushSupplier CorbaGetProxyPushSupplierRef (Object poa,
                                                               ProxyPushSupplierBase impl)
                throws Throwable
	{
		// Filled in by aspect
		return null;
	}

        /**
         * Return a reference to the ProxyPullSupplier doing the right thing
         * in the CORBA and no-CORBA case
         *
         * @param poa   The Object
         * @param impl  Reference to the ProxyPullSupplierImpl object
         */
	public ProxyPullSupplier CorbaGetProxyPullSupplierRef (Object poa,
                                                               ProxyPullSupplierBase impl)
                throws Throwable
	{
		// Filled in by an aspect
		return null;
	}
}
