package edu.wustl.doc.facet.feature_consumer_qos;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

/**
 * Introductions which should show up in IDL must be
 * done in aspects whose names end with `IntroAspect'
 */
aspect ConsumerQosIntroAspect {

	public void ProxyPushSupplier.connect_push_consumer (PushConsumer push_consumer,
                                                             ConsumerQOS qos)
        {
        }
}
