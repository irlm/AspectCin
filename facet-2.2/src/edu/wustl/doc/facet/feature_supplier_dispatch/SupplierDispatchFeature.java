package edu.wustl.doc.facet.feature_supplier_dispatch;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_eventtype_filter.EventTypeFilterFeature;
import edu.wustl.doc.facet.feature_mutex_dispatchertype.DispatcherTypeMutexFeature;

public interface SupplierDispatchFeature extends DispatcherTypeMutexFeature, EventTypeFilterFeature {

    static aspect Register extends AutoRegisterAspect {
        protected void register(FeatureRegistry fr) {
            fr.registerFeature(SupplierDispatchFeature.class);
        }
    }
}
