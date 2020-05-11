package edu.wustl.doc.facet.feature_consumer_qos;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

/**
 * Ensures that other unit tests are appropriately updated to call the
 * right version of the connect_push_consumer call.  Remember that IDL
 * doesn't allow method call overloading, so we can either introduce a
 * connect_push_consumer method that follows the non-dependency way of
 * connecting or modify all of the calls to use the appropriate
 * method.  The former is slightly easier, so that's what we do for
 * now...
 */
public aspect ConsumerQosUpgrader implements Upgradeable, ConsumerQosFeature {

	/**
	 * In order to allow other aspects to upgrade this
	 * code, we need to put it in a method in this aspect
	 * rather than putting it in an introduced method.
	 */
	public void connect_push_consumer (ProxyPushSupplier pps, PushConsumer push_consumer)
	{
		ConsumerQOS qos = new ConsumerQOS();
		
		try {
			pps.connect_push_consumer(push_consumer, qos);
		} catch (Throwable e) {
			// Mask since the old prototype doesn't support these.
			e.printStackTrace();
		}
	}

	public void ProxyPushSupplier.connect_push_consumer (PushConsumer push_consumer)
	{
		ConsumerQosUpgrader.aspectOf().connect_push_consumer (this, push_consumer);
	}
}
