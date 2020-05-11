package edu.wustl.doc.facet.feature_eventsource;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_eventheader.EventHeaderFeature;

public interface EventSourceFeature extends EventHeaderFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerAbstractFeature(EventSourceFeature.class);
        }
    }
}
