package edu.wustl.doc.facet.feature_eventheader;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_event_struct.EventStructFeature;

public interface EventHeaderFeature extends EventStructFeature {
    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerAbstractFeature(EventHeaderFeature.class);
        }
    }
}
