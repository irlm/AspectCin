/*
 * $Id: FilterTreeBuilder.java,v 1.1 2003/06/25 18:30:52 ravip Exp $
 */

package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.EventChannelAdmin.*;

/**
 * Interface for everything that knows how to build a filter tree.
 *
 * @author <a href="mailto:pm2@cs.wustl.edu">Pavan Mandalkar</a>
 * @version 1.0
 */
public class FilterTreeBuilder {

    private FilterBuilderRegistry builderRegistry_;
    private DependencyIterator dependencyIterator_;


    /**
     * Creates a new <code>FilterTreeBuilder</code> instance.
     *
     */
    public FilterTreeBuilder (FilterBuilderRegistry builderRegistry,
                              Dependency[] dependencies)
    {
        builderRegistry_ = builderRegistry;
        dependencyIterator_ = new DependencyIterator (dependencies);
    }

    /**
     * This method is just like buildNext except that it should be
     * called first to get everything going.  It also handles the
     * special null filter case.
     *
     * @return a <code>FilterNode</code> value
     * @exception EventChannelException if an error occurs
     */
    public FilterNode buildTree() throws EventChannelException
    {
        if (dependencyIterator_.hasNext ()) {
            return buildNext ();
        } else {
            return new FilterMatchAnyEvent ();
        }
    }

    /**
     * Build the next filter subtree and return it.
     *
     * @return a <code>FilterNode</code> value
     * @exception EventChannelException if an error occurs
     */
    public FilterNode buildNext() throws EventChannelException
    {
        Dependency depend = dependencyIterator_.next ();

        FilterBuilder fb = builderRegistry_.findBuilder (depend.getFilterOp ());

        return fb.buildTree (depend, this);
    }

    public boolean canBuildNext()
    {
        return dependencyIterator_.hasNext ();
    }

    public boolean checkDone()
    {
        return !dependencyIterator_.hasNext ();
    }

}
