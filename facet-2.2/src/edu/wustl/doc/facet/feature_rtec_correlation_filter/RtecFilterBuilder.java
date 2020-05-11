package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.EventChannelAdmin.*;

public class RtecFilterBuilder implements FilterBuilder {
	
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
      
        int count = (dependency.getHeader ()).getSource ();
        FilterNode [] children = new FilterNode [count];

        for (int i = 0; i != count; i ++)
            children [i]  = builder.buildNext ();
            
        return new FilterAnd (count, children); 
    }
	
    public FilterNode buildOr (Dependency dependency, FilterTreeBuilder builder)
        throws EventChannelException
    {
        int count = (dependency.getHeader ()).getSource ();
        FilterNode [] children = new FilterNode [count];

        for (int i = 0; i != count; i ++)
            children [i]  = builder.buildNext ();
            
        return new FilterOr (count, children); 
            
    }

    public FilterNode buildMatch (Dependency dependency, FilterTreeBuilder builder)
        throws EventChannelException
    {
        return new FilterMatchByTypeAndSource (dependency.getHeader ().getType (), dependency.getHeader ().getSource ());
    }


    public FilterNode buildMatchOr (Dependency dependency, FilterTreeBuilder builder)
        throws EventChannelException
    {
        if (builder.canBuildNext ()) {
            int count = 2;
            FilterNode [] children = new FilterNode [count];
            children [0] = buildMatch (dependency, builder);
            children [1] = builder.buildNext ();
            return new FilterOr (count, children);
        }
        else
            return new FilterMatchByTypeAndSource (dependency.getHeader ().getType (), dependency.getHeader ().getSource ());
    }
}
