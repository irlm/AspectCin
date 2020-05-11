package edu.wustl.doc.facet.feature_eventtype;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_eventheader.EventHeaderFeature;

public interface EventTypeFeature extends EventHeaderFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerFeature(EventTypeFeature.class);
        }
    }
}
