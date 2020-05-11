package edu.wustl.doc.facet.feature_mutex_filtertype;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;

public interface FilterTypeMutexFeature {

        static aspect Register extends AutoRegisterAspect {

                protected void register (FeatureRegistry fr) {
                        fr.registerMutexFeature (FilterTypeMutexFeature.class);
                }
        }
}
