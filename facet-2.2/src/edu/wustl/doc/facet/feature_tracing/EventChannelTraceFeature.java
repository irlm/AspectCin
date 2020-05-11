package edu.wustl.doc.facet.feature_tracing;

import edu.wustl.doc.facet.feature.*;

public interface EventChannelTraceFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerFeature(EventChannelTraceFeature.class);
        }
    }
}
