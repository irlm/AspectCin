package edu.wustl.doc.facet.feature_correlation_filter;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_mutex_filtertype.FilterTypeMutexFeature;
import edu.wustl.doc.facet.feature_eventsource.EventSourceFeature;
import edu.wustl.doc.facet.feature_eventtype_filter.EventTypeFilterFeature;
import edu.wustl.doc.facet.feature_eventvector.EventVectorFeature;


public interface CorrelationFilterFeature extends FilterTypeMutexFeature,
                          EventTypeFilterFeature, EventVectorFeature, EventSourceFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerFeature(CorrelationFilterFeature.class);
        }
    }
}
