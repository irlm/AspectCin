package edu.wustl.doc.facet.feature_eventbody_any;

import edu.wustl.doc.facet.feature.*;
import edu.wustl.doc.facet.feature_event_struct.EventStructFeature;
import edu.wustl.doc.facet.feature_enable_corba.EnableCorbaFeature;
import edu.wustl.doc.facet.feature_mutex_payloadtype.PayloadTypeMutexFeature;

public interface EventBodyAnyFeature
        extends EventStructFeature, PayloadTypeMutexFeature, EnableCorbaFeature {

	static aspect Register extends AutoRegisterAspect {
		protected void register(FeatureRegistry fr) {
			fr.registerFeature (EventBodyAnyFeature.class);
		}
	}
}
