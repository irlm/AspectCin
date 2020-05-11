package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.*;

/**
 * Match any event.
 *
 * @author <a href="mailto:pm2@cs.wustl.edu">Pavan Mandalkar</a>
 * @version 1.0
 */
public class FilterMatchAnyEvent implements FilterNode {
    private int lastFired_ = INVALID_EVENT_NUM;

    public int pushEvent(EventCarrier ec, int eventnum) {
        int retval = FIRE;

        if (lastFired_ != eventnum) {
            /* Save away that we've matched for this particular
             * event so that if we're asked again, we can respond
             * appropriately.
             */
            lastFired_ = eventnum;
            retval |= SAVE;
        }
        return retval;
    }
}
