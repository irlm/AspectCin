/*
 * $Id: FilterBuilderRegistry.java,v 1.3 2003/07/09 19:56:28 ravip Exp $
 */

package edu.wustl.doc.facet.feature_correlation_filter;

import edu.wustl.doc.facet.EventChannelAdmin.*;
import java.util.HashMap;

/**
 * Registry for all of the different types of FilterBuilders
 *
 */
class FilterBuilderRegistry {

	private HashMap filterBuilders_ = new HashMap();

	/**
	 * Creates a new <code>FilterBuilderRegistry</code> instance and
	 * registers the basic filters.
	 */
	public FilterBuilderRegistry() {
		StandardFilterBuilder sfb = new StandardFilterBuilder();

		filterBuilders_.put (new Integer (FilterOpTypes.CORRELATE_MATCHOR), sfb);
		filterBuilders_.put (new Integer (FilterOpTypes.CORRELATE_MATCH), sfb);
		filterBuilders_.put (new Integer (FilterOpTypes.CORRELATE_AND), sfb);
		filterBuilders_.put (new Integer (FilterOpTypes.CORRELATE_OR), sfb);
	}

	public FilterBuilder findBuilder(int filter_op) throws EventChannelException
	{
		Integer op = new Integer(filter_op);
		
		if (filterBuilders_.containsKey(op)) {
			return (FilterBuilder) filterBuilders_.get(op);
		} else {
			throw new EventChannelException ();
		}
	}
}
