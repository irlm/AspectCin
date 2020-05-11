/*
 * $Id: FilterBuilder.java,v 1.3 2003/04/18 20:47:44 ravip Exp $
 */

package edu.wustl.doc.facet.feature_correlation_filter;

import edu.wustl.doc.facet.EventChannelAdmin.*;

/**
 * Interface for everything that knows how to build a filter tree.
 *
 * @author <a href="mailto:fhunleth@cs.wustl.edu">Frank Hunleth</a>
 * @version 1.0
 */
public interface FilterBuilder {

        public FilterNode buildTree (Dependency dependency,
                                     FilterTreeBuilder builder)
                throws EventChannelException;

}
