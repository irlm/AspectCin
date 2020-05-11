package edu.wustl.doc.facet.feature_rtec_correlation_filter;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.Event;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Holder for all of the events that may be buffered before sending
 * them to a consumer.
 *
 * @author <a href="mailto:pm2@cs.wustl.edu">Pavan Mandalkar</a>
 * @version 1.0
 */
class EventCarrierList {
    private ArrayList events_ = new ArrayList();

    public EventCarrierList() {
    }

    public void add(EventCarrier ec) {
        events_.add(ec);
    }

    public void clear() {
        events_.clear();
    }

    public Event[] createEventArray() {
        Event[] events = new Event[events_.size()];
        Iterator i = events_.iterator();
        int ix = 0;

        while (i.hasNext()) {
            events[ix] = ((EventCarrier) i.next()).getEvent();
            ix++;
        }

        return events;
    }
}
