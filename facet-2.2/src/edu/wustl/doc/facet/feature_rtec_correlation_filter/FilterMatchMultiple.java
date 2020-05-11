package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.*;

/**
 * Take one filter and force it to match a specified number of times
 * before firing.  This filter has the advantage of just needing one
 * filter instead of many.
 *
 * @author <a href="mailto:pm2@cs.wustl.edu">Pavan Mandalkar</a>
 * @version 1.0
 */
public class FilterMatchMultiple implements FilterNode {
    /* Flyweight for multiple filters */
    private FilterNode filter_;

    private int maxTimes_;
    private int numTimes_;

    private int internalEventNum_ = FIRST_EVENT_NUM;
    private int lastFired_ = INVALID_EVENT_NUM;

    public FilterMatchMultiple(FilterNode filter, int times) {
        filter_ = filter;
        maxTimes_ = times;
        numTimes_ = 0;
    }

    public int pushEvent(EventCarrier ec, int eventnum) {
        int retval = NOMATCH;

        if (eventnum != lastFired_) {
            retval = filter_.pushEvent( ec, internalEventNum_);
            if ((retval & FIRE) != 0) {

                // Update the number of times that we matched.
                numTimes_++;

                // Update our internal event number that we give to
                // our subfilter.
                internalEventNum_++;
                if (internalEventNum_ == INVALID_EVENT_NUM) {
                    internalEventNum_ = FIRST_EVENT_NUM;
                }

                // Check if we should fire.
                if (numTimes_ == maxTimes_) {
                    lastFired_ = eventnum;
                } else {
                    // Turn off the FIRED bit, since we didn't fire.
                    // Propogate any other bit settings up though.
                    retval = retval & ~FIRE;
                }
            }
        }

        return retval;
    }
}
