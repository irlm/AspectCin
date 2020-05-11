package edu.wustl.doc.facet.feature_dependency;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_eventheader.EventHeaderFeature;
import edu.wustl.doc.facet.feature_consumer_qos.ConsumerQosFeature;

public interface DependencyFeature extends ConsumerQosFeature, EventHeaderFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerAbstractFeature(DependencyFeature.class);
        }
    }
}
