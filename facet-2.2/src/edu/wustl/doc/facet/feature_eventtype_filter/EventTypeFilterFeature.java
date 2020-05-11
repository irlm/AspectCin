package edu.wustl.doc.facet.feature_eventtype_filter;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_eventtype.EventTypeFeature;
import edu.wustl.doc.facet.feature_dependency.DependencyFeature;

public interface EventTypeFilterFeature extends EventTypeFeature, DependencyFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerAbstractFeature(EventTypeFilterFeature.class);
        }
    }
}
