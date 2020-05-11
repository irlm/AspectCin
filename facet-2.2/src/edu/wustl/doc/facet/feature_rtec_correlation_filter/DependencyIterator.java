package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.EventChannelAdmin.*;

/**
 * Simple iterator for the Dependency array.  Doesn't quite follow the
 * semantics of normal Java iterators, but is more convenient.
 */
class DependencyIterator {

    Dependency[] dependencies_;
    int i;

    DependencyIterator(Dependency[] dependencies) {
        dependencies_ = dependencies;
        i = 0;
    }

    public boolean hasNext() {
        return i < dependencies_.length;
    }

    public Dependency next () throws EventChannelException {
        if (hasNext())
            return dependencies_[i++];
        else
            throw new EventChannelException ();
    }
    
}
