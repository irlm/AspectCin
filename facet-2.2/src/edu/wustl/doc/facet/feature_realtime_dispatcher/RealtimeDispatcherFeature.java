package edu.wustl.doc.facet.feature_realtime_dispatcher;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;

import edu.wustl.doc.facet.feature_consumer_dispatch.ConsumerDispatchFeature;
import edu.wustl.doc.facet.feature_mutex_dispatchertype.DispatcherTypeMutexFeature;
import edu.wustl.doc.facet.feature_source_filter.SourceFilterFeature;

public interface RealtimeDispatcherFeature extends ConsumerDispatchFeature,
						   DispatcherTypeMutexFeature,
						   SourceFilterFeature {
	
	static aspect Register extends AutoRegisterAspect {

		protected void register (FeatureRegistry fr) {
			fr.registerFeature (RealtimeDispatcherFeature.class);
		}
	}
}
