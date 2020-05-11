/*
 * $Id: FilterMatchAnyEvent.java,v 1.1 2002/09/28 19:58:29 ravip Exp $
 */

package edu.wustl.doc.facet.feature_correlation_filter;

import edu.wustl.doc.facet.*;

/**
 * Match any event.
 *
 * @author <a href="mailto:fhunleth@cs.wustl.edu">Frank Hunleth</a>
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
