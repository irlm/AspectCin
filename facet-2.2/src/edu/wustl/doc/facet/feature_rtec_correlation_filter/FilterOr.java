/*
 * $Id: FilterOr.java,v 1.1 2003/06/25 18:30:52 ravip Exp $
 */

package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.*;

/**
 * One or all subtrees need to fire before this node fires.
 *
 * Note: Semantics will need to be improved.  This one is mostly
 *       proof of concept right now.
 *
 * @author <a href="mailto:pm2@cs.wustl.edu">Pavan Mandalkar</a>
 * @version 1.0
 */
public class FilterOr implements FilterNode {

    private FilterNode [] children_;
    private int countOfChildren_;

    public FilterOr (int count, FilterNode [] children) {
        countOfChildren_ = count;
        children_ = children;
    }

    public int pushEvent(EventCarrier ec, int eventnum) {

        int childRetVal = 0;
        for ( int i = 0; i != countOfChildren_; i++ ) 
            childRetVal |= children_ [i].pushEvent (ec, eventnum);
        
        return childRetVal;
    }
}
