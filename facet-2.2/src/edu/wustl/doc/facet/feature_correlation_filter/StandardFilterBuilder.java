/*
 * $Id: StandardFilterBuilder.java,v 1.9 2003/07/09 19:56:28 ravip Exp $
 */

package edu.wustl.doc.facet.feature_correlation_filter;

import edu.wustl.doc.facet.EventChannelAdmin.*;

public class StandardFilterBuilder implements FilterBuilder {
	
	public FilterNode buildTree (Dependency dependency, FilterTreeBuilder builder)
		throws EventChannelException
	{
		switch (dependency.getFilterOp ()) {

		case FilterOpTypes.CORRELATE_MATCHOR:
			return buildMatchOr (dependency, builder);

		case FilterOpTypes.CORRELATE_MATCH:
			return buildMatch (dependency, builder);

		case FilterOpTypes.CORRELATE_AND:
			return buildAnd (dependency, builder);

		case FilterOpTypes.CORRELATE_OR:
			return buildOr (dependency, builder);

		default:
			// This is really really bad if we get here.
			// throw new InvalidFilter();
			return null;
		}
	}

	public FilterNode buildAnd (Dependency dependency, FilterTreeBuilder builder)
		throws EventChannelException
	{
		return new FilterAnd (builder.buildNext(), builder.buildNext());
	}
	
	public FilterNode buildOr (Dependency dependency, FilterTreeBuilder builder)
		throws EventChannelException
	{
		return new FilterOr (builder.buildNext(), builder.buildNext());
	}

	public FilterNode buildMatch (Dependency dependency, FilterTreeBuilder builder)
		throws EventChannelException
	{
		
		return new FilterMatchByTypeAndSource (dependency.getHeader ().getType (), dependency.getHeader ().getSource ());
	}

	public FilterNode buildMatchOr (Dependency dependency, FilterTreeBuilder builder)
		throws EventChannelException
	{
		if (builder.canBuildNext ())
			return new FilterOr (buildMatch (dependency, builder), builder.buildNext ());
	        else
			return new FilterMatchByTypeAndSource (dependency.getHeader ().getType (), dependency.getHeader ().getSource ());
	}
}
