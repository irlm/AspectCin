package edu.wustl.doc.facet.feature_eventbody_octetseq;

import edu.wustl.doc.facet.feature.*;
import edu.wustl.doc.facet.feature_event_struct.EventStructFeature;
import edu.wustl.doc.facet.feature_mutex_payloadtype.PayloadTypeMutexFeature;

public interface EventBodyOctetSeqFeature extends EventStructFeature, PayloadTypeMutexFeature {
	
	static aspect Register extends AutoRegisterAspect {
		protected void register(FeatureRegistry fr) {
			fr.registerFeature(EventBodyOctetSeqFeature.class);
		}
	}
}
