package edu.wustl.doc.facet.feature_event_object;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_mutex_eventtype.EventTypeMutexFeature;
import edu.wustl.doc.facet.feature_disable_corba.DisableCorbaFeature;

public interface EventObjectFeature extends EventTypeMutexFeature, DisableCorbaFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerFeature(EventObjectFeature.class);
        }
    }
}
