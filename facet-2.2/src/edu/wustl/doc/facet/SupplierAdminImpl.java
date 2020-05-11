/*
 * $Id: SupplierAdminImpl.java,v 1.4 2003/08/20 20:50:39 ravip Exp $
 */

package edu.wustl.doc.facet;

import edu.wustl.doc.facet.EventChannelAdmin.*;

/**
 * Implementation of the SupplierAdmin interface.
 *
 * @author Frank Hunleth, Ravi Pratap
 * @version $Revision: 1.4 $
 */
public class SupplierAdminImpl extends SupplierAdminBase {

	private Object poa_;
	private EventChannelImpl eventChannel_;

	/**
	 *  Constructor for the SupplierAdminImpl object.
         *
         * @param ec    Reference to the EventChannelImpl
         * @param poa   The POA
	 */
	protected SupplierAdminImpl (EventChannelImpl ec, Object poa)
	{
		eventChannel_ = ec;
		poa_ = poa;
	}

	/**
	 * Obtain a proxy push consumer to be able to connect a
	 * push supplier to an event channel.
	 *
	 * @return ProxyPushConsumer The push consumer.
	 */
	public ProxyPushConsumer obtain_push_consumer ()
	{
		ProxyPushConsumerImpl ppcImpl = new ProxyPushConsumerImpl (eventChannel_);
		ProxyPushConsumer ret = null;
		
		try {
			ret = CorbaGetProxyPushConsumerRef (poa_, ppcImpl);

		} catch (Throwable se) {
			System.err.println (se);
		}

		return ret;
	}

        /**
         * Return a reference to the ProxyPushConsumer doing the right thing
         * in the CORBA and no-CORBA case
         *
         * @param poa   The POA
         * @param impl  Reference to the ProxyPushSupplierImpl object
         */
	public ProxyPushConsumer CorbaGetProxyPushConsumerRef (Object poa,
                                                               ProxyPushConsumerBase impl)
                throws Throwable
	{
		// Empty - filled in by aspects
		return null;
	}

        /**
         * Return a reference to the ProxyPullConsumer doing the right thing
         * in the CORBA and no-CORBA case
         *
         * @param poa   The POA
         * @param impl  Reference to the ProxyPullSupplierImpl object
         */
	public ProxyPullConsumer CorbaGetProxyPullConsumerRef (Object poa,
                                                               ProxyPullConsumerBase impl)
                throws Throwable
	{
		// Empty - filled in by aspects
		return null;
	}
}
