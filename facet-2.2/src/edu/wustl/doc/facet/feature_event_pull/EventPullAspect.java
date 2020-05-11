package edu.wustl.doc.facet.feature_event_pull;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

privileged aspect EventPullAspect {

	//
	// Introduce factory methods to return ProxyPullSuppliers and
	// ProxyPullConsumers
	//

	//
	// Obtain a proxy pull supplier to be able to connect a
	// pull consumer to an event channel.
	//
	// @return ProxyPullSupplier The push supplier.
	//
	public ProxyPullSupplier ConsumerAdminImpl.obtain_pull_supplier ()
	{
		ProxyPullSupplierImpl ppsImpl = new ProxyPullSupplierImpl (eventChannel_);

		try {
			ProxyPullSupplier pps = CorbaGetProxyPullSupplierRef (poa_, ppsImpl);
			return pps;

		} catch (Throwable se) {
			se.printStackTrace();
		}

		return null;
	}

	//
	// Obtain a proxy pull consumer to be able to connect a
	// pull supplier to an event channel.
	//
	// @return ProxyPullConsumer The pull consumer.
	//
	public ProxyPullConsumer SupplierAdminImpl.obtain_pull_consumer()
	{
		ProxyPullConsumerImpl ppcImpl = new ProxyPullConsumerImpl(eventChannel_);

		try {
			ProxyPullConsumer ppc = CorbaGetProxyPullConsumerRef (poa_, ppcImpl);
			return ppc;
			
		} catch (Throwable se) {
			se.printStackTrace ();
		}

		return null;
	}


}
