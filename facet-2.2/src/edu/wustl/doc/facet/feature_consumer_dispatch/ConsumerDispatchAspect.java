package edu.wustl.doc.facet.feature_consumer_dispatch;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;
import edu.wustl.doc.facet.EventChannelAdmin.*;

import edu.wustl.doc.facet.feature_event_struct.EventStructAspect;

/** 
 * Since multiple features wrap around the same join point, we use
 * aspect precedence to specify the order in which they are applied
 */
public aspect ConsumerDispatchAspect {

        declare precedence: ConsumerDispatchAspect, EventStructAspect;

        /**
         * Filter by wrapping around ProxyPushSupplierImpl.push (EventCarrier)
         */
	void around (ProxyPushSupplierImpl pps, EventCarrier eventCarrier) :
		execution (void ProxyPushSupplierImpl.push (EventCarrier)) &&
		target (pps) &&
		args (eventCarrier)
	{
		ConsumerQOS qos = pps.getConsumerQOS ();

		//
                // If qos is null or empty, then there's no filtering
                //
		if (qos == null) {
                        proceed (pps, eventCarrier);
                        return;
                }

                int length = qos.getDependenciesLength ();

                if (length == 0) {
                        proceed (pps, eventCarrier);
		} else {
			// Filter
			long type = eventCarrier.getEvent ().getHeader ().getType ();
			for (int i = 0; i < length; i++) {
                                long cons_type = qos.getDependency (i).getHeader ().getType ();

				if (cons_type == type || cons_type == FilterOpTypes.TYPE_ANY) {
					proceed (pps, eventCarrier);
                                        return;
                                }
                        }
		}

		return;
	}
}
