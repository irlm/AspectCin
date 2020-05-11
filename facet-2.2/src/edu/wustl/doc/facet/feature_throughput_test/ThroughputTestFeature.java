package edu.wustl.doc.facet.feature_throughput_test;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;

public interface ThroughputTestFeature {

	//
	// This feature is just a test.  It is registered so that it can
	// reuse all of the infrastructure to manage features, but it is
	// not considered as a real FACET configuration.  The intent is
	// to have it specified when throughput measurements are taken,
	// since it takes a long time.
	//
	
	static aspect Register extends AutoRegisterAspect {
		protected void register(FeatureRegistry fr) {
			fr.registerFeature(ThroughputTestFeature.class, false);
		}
	}
}
