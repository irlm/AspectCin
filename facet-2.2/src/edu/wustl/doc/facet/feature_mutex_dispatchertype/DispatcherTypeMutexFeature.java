package edu.wustl.doc.facet.feature_mutex_dispatchertype;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;

public interface DispatcherTypeMutexFeature {

	static aspect Register extends AutoRegisterAspect {

		protected void register (FeatureRegistry fr) {
			fr.registerMutexFeature (DispatcherTypeMutexFeature.class);
		}
	}
}
