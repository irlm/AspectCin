package edu.wustl.doc.facet.feature_eventvector;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;
import edu.wustl.doc.facet.EventComm.*;

public aspect EventVectorAspect {

	/* Provide the correct implementation for ProxyPushConsumerImpl */
	public void ProxyPushConsumerImpl.push_vec (Event[] data)
	{
		for (int i = 0; i < data.length; i++) {
			EventCarrier ec = new EventCarrier (data[i]);
			this.getEventChannel ().pushEvent (ec);
		}
	}
}
