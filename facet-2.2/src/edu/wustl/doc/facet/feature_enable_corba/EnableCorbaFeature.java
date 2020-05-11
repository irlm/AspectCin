package edu.wustl.doc.facet.feature_enable_corba;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_mutex_transporttype.TransportTypeMutexFeature;

public interface EnableCorbaFeature extends TransportTypeMutexFeature {

	static aspect Register extends AutoRegisterAspect {

		protected void register (FeatureRegistry fr) {
			fr.registerFeature (EnableCorbaFeature.class);
		}
	}
}
