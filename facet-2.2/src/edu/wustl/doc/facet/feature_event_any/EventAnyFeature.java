package edu.wustl.doc.facet.feature_event_any;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_mutex_eventtype.EventTypeMutexFeature;
import edu.wustl.doc.facet.feature_enable_corba.EnableCorbaFeature;

public interface EventAnyFeature extends EnableCorbaFeature, EventTypeMutexFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerFeature(EventAnyFeature.class);
        }
    }
}
