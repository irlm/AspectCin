package edu.wustl.doc.facet.feature_consumer_qos;

import edu.wustl.doc.facet.feature.*;

public interface ConsumerQosFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerAbstractFeature(ConsumerQosFeature.class);
        }
    }
}
