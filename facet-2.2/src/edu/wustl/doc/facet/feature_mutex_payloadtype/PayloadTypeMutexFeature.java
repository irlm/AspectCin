package edu.wustl.doc.facet.feature_mutex_payloadtype;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;

public interface PayloadTypeMutexFeature {

        static aspect Register extends AutoRegisterAspect {

                protected void register (FeatureRegistry fr) {
                        fr.registerMutexFeature (PayloadTypeMutexFeature.class);
                }
        }
}
