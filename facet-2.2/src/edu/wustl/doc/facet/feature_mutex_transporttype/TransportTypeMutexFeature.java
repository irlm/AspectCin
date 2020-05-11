package edu.wustl.doc.facet.feature_mutex_transporttype;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;

public interface TransportTypeMutexFeature {

        static aspect Register extends AutoRegisterAspect {

                protected void register (FeatureRegistry fr) {
                        fr.registerMutexFeature (TransportTypeMutexFeature.class);
                }
        }
}
