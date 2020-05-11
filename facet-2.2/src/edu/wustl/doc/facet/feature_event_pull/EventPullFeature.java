package edu.wustl.doc.facet.feature_event_pull;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_event_struct.EventStructFeature;

public interface EventPullFeature extends EventStructFeature {
	
	static aspect Register extends AutoRegisterAspect {

		protected void register (FeatureRegistry ar) {
			ar.registerFeature(EventPullFeature.class);
			
			// Mark that we have a "Uses" relationship with
			// the event struct feature.  This is subtle, but
			// important since adding the pull interface does
			// not concretize the event.
			ar.markContainsRelationship (EventPullFeature.class,
						     EventStructFeature.class);
		}
	}
}
