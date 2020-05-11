/*
 * $Id: FilterOr.java,v 1.1 2002/09/28 19:58:29 ravip Exp $
 */

package edu.wustl.doc.facet.feature_correlation_filter;

import edu.wustl.doc.facet.*;

/**
 * Either or both subtrees need to fire before this node fires.
 *
 * Note: Semantics will need to be improved.  This one is mostly
 *       proof of concept right now.
 *
 * @author <a href="mailto:fhunleth@cs.wustl.edu">Frank Hunleth</a>
 * @version 1.0
 */
public class FilterOr implements FilterNode {
    private FilterNode left_;
    private FilterNode right_;

    public FilterOr(FilterNode left, FilterNode right) {
        left_ = left;
        right_ = right;
    }

    public int pushEvent(EventCarrier ec, int eventnum) {
        return (left_.pushEvent(ec, eventnum) |
                right_.pushEvent(ec, eventnum));
    }
}
