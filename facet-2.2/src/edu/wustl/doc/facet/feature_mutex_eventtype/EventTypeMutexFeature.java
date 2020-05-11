package edu.wustl.doc.facet.feature_mutex_eventtype;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;

public interface EventTypeMutexFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerMutexFeature(EventTypeMutexFeature.class);
        }
    }
}
