/*
 * $Id: SourceFilterFeature.java,v 1.2 2003/02/28 19:47:36 ravip Exp $
 */

package edu.wustl.doc.facet.feature_source_filter;

import edu.wustl.doc.facet.feature.AutoRegisterAspect;
import edu.wustl.doc.facet.feature.FeatureRegistry;
import edu.wustl.doc.facet.feature_eventsource.EventSourceFeature;
import edu.wustl.doc.facet.feature_dependency.DependencyFeature;

public interface SourceFilterFeature extends EventSourceFeature, DependencyFeature {

        static aspect Register extends AutoRegisterAspect {
		
                protected void register (FeatureRegistry fr) {
                        fr.registerFeature (SourceFilterFeature.class);
                }
        }
}
