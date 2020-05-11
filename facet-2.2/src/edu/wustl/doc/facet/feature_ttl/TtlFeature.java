package edu.wustl.doc.facet.feature_ttl;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_eventheader.EventHeaderFeature;

public interface TtlFeature extends EventHeaderFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerFeature(TtlFeature.class);
        }
    }
}
