package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.EventChannelAdmin.*;

/**
 * Interface for everything that knows how to build a filter tree.
 *
 * @author <a href="mailto:pm2@cs.wustl.edu">Pavan Mandalkar</a>
 * @version 1.0
 */
public interface FilterBuilder {

        public FilterNode buildTree (Dependency dependency,
                                     FilterTreeBuilder builder)
                throws EventChannelException;

}
