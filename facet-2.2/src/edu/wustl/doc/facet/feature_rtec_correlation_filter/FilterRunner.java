package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.*;

/**
 * Run through a filter tree and save state between events.
 *
 * @author <a href="mailto:pm2@cs.wustl.edu">Pavan Mandalkar</a>
 * @version 1.0
 */
public class FilterRunner {
    private FilterNode filterRoot_;
    private int currentEventNum_ = FilterNode.FIRST_EVENT_NUM;

    public FilterRunner(FilterNode root) {
        filterRoot_ = root;
    }

    public boolean pushEvent(EventCarrier ec,
                             EventCarrierList savedEvents) {
        int retval = filterRoot_.pushEvent(ec, currentEventNum_);

        // Check if we should save this event away.
        if ((retval & FilterNode.SAVE) != 0) {
            savedEvents.add(ec);
        }

        // Check if the event was fired by the filter tree.
        if ((retval & FilterNode.FIRE) != 0) {
            currentEventNum_++;
            if (currentEventNum_ == FilterNode.INVALID_EVENT_NUM) {
                currentEventNum_ = FilterNode.FIRST_EVENT_NUM;
            }
            return true;
        }

        return false;
    }
}
