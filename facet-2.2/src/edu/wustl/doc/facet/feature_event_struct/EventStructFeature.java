package edu.wustl.doc.facet.feature_event_struct;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_mutex_eventtype.EventTypeMutexFeature;

public interface EventStructFeature extends EventTypeMutexFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerAbstractFeature(EventStructFeature.class);
        }
    }
}
