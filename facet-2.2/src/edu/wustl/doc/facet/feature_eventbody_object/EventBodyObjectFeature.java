package edu.wustl.doc.facet.feature_eventbody_object;

import edu.wustl.doc.facet.feature.*;
import edu.wustl.doc.facet.feature_event_struct.EventStructFeature;
import edu.wustl.doc.facet.feature_disable_corba.DisableCorbaFeature;
import edu.wustl.doc.facet.feature_mutex_payloadtype.PayloadTypeMutexFeature;

public interface EventBodyObjectFeature
        extends EventStructFeature, DisableCorbaFeature, PayloadTypeMutexFeature {

	static aspect Register extends AutoRegisterAspect {
		
		protected void register(FeatureRegistry fr) {
			fr.registerFeature(EventBodyObjectFeature.class);
		}
	}
}
