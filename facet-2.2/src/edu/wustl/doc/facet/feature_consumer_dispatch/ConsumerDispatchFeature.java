package edu.wustl.doc.facet.feature_consumer_dispatch;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_eventtype_filter.EventTypeFilterFeature;

public interface ConsumerDispatchFeature extends EventTypeFilterFeature {

	static aspect Register extends AutoRegisterAspect {
		protected void register(FeatureRegistry fr) {
			fr.registerFeature(ConsumerDispatchFeature.class);
		}
	}
}
