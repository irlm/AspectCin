package edu.wustl.doc.facet.feature_rtec_correlation_filter;

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
		RtecFilterBuilder rfb = new RtecFilterBuilder();
                //
                // MATCHOR is redundant wrt to Rtec, but it is still
                // included to be in synch with FACET's original
                // correlation filter
                //
		filterBuilders_.put (new Integer (FilterOpTypes.CORRELATE_MATCHOR), rfb);
		filterBuilders_.put (new Integer (FilterOpTypes.CORRELATE_MATCH), rfb);
		filterBuilders_.put (new Integer (FilterOpTypes.CORRELATE_AND), rfb);
		filterBuilders_.put (new Integer (FilterOpTypes.CORRELATE_OR), rfb);
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
