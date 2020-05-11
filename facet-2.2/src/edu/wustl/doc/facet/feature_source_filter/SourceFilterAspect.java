package edu.wustl.doc.facet.feature_source_filter;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

import edu.wustl.doc.facet.feature_event_struct.EventStructAspect;

public aspect SourceFilterAspect {

        declare precedence: SourceFilterAspect, EventStructAspect;

        declare precedence:
                edu.wustl.doc.facet.feauture_consumer_dispatch.ConsumerDispatchAspect,
                SourceFilterAspect;
        
	void around (ProxyPushSupplierImpl pps, EventCarrier eventCarrier) :
		execution (void ProxyPushSupplierImpl.push (EventCarrier)) &&
		target (pps) &&
		args (eventCarrier)
	{
		ConsumerQOS qos = pps.getConsumerQOS ();

		// If qos is null or empty, then there's no filtering
		if (qos == null) {
                        proceed (pps, eventCarrier);
                        return;
                }
                
                int length = qos.getDependenciesLength ();
                
                if (length == 0) 
			proceed (pps, eventCarrier);
		else {
			// Filter on the source
			int source_id = eventCarrier.getEvent ().getHeader().getSource ();
			
			for (int i = 0; i < length; i++) {
				if (qos.getDependency (i).getHeader ().getSource () == source_id ||
                                    qos.getDependency (i).getHeader ().getSource () == FilterOpTypes.SOURCE_ANY) {
					proceed (pps, eventCarrier);
                                        return;
                                }
			}
		}
	}
}
