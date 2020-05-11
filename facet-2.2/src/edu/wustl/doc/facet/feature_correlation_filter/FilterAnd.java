/*
 * $Id: FilterAnd.java,v 1.1 2002/09/28 19:58:29 ravip Exp $
 */

package edu.wustl.doc.facet.feature_correlation_filter;

import edu.wustl.doc.facet.*;

/**
 * Both subtrees need to fire before this node fires.
 *
 * Note: Semantics will need to be improved.  This one is mostly
 *       proof of concept right now.
 *
 * @author <a href="mailto:fhunleth@cs.wustl.edu">Frank Hunleth</a>
 * @version 1.0
 */
public class FilterAnd implements FilterNode {
    private FilterNode left_;
    private FilterNode right_;

    public FilterAnd(FilterNode left, FilterNode right) {
        left_ = left;
        right_ = right;
    }

    public int pushEvent(EventCarrier ec, int eventnum) {
        int lretval = left_.pushEvent(ec, eventnum);
        int rretval = right_.pushEvent(ec, eventnum);
        int retval = lretval | rretval;

        if ((lretval & rretval & FIRE) == 0) {
            retval = retval & ~FIRE;
        }

        return retval;
    }

}
