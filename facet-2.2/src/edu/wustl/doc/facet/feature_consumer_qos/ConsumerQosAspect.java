package edu.wustl.doc.facet.feature_consumer_qos;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

/**
 * ConsumerQOS is used by consumers to specify information like types
 * and sources they are interested in
 */
aspect ConsumerQosAspect {

        /**
         * Introduce the field and its accessor on EventCarrierPushConsumer
         */
	public ConsumerQOS EventCarrierPushConsumer.qos_;
    
	public ConsumerQOS EventCarrierPushConsumer.getConsumerQOS ()
	{
		return this.qos_;
	}

	public void ProxyPushSupplierImpl.connect_push_consumer (PushConsumer push_consumer, ConsumerQOS qos)
	{
		this.qos_ = qos;
		this.connect_push_consumer (push_consumer);
	}
}
