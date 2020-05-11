package edu.wustl.doc.facet.feature_eventvector;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_event_struct.EventStructFeature;

public interface EventVectorFeature extends EventStructFeature {

	static aspect Register extends AutoRegisterAspect {

		protected void register(FeatureRegistry fr) {
			fr.registerFeature(EventVectorFeature.class);

			// Mark that we have a "Uses" relationship with
			// the event struct feature.  This is subtle, but
			// important since creating vectors of events does
			// not concretize the event.
			fr.markContainsRelationship(EventVectorFeature.class,
						    EventStructFeature.class);
		}
	}
}
