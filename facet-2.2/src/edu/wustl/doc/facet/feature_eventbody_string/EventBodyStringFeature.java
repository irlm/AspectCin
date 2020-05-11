package edu.wustl.doc.facet.feature_eventbody_string;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_event_struct.EventStructFeature;
import edu.wustl.doc.facet.feature_mutex_payloadtype.PayloadTypeMutexFeature;

public interface EventBodyStringFeature extends EventStructFeature, PayloadTypeMutexFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerFeature(EventBodyStringFeature.class);
        }
    }
}
