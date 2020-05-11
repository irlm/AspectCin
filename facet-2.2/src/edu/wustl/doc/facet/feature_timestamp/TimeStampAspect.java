package edu.wustl.doc.facet.feature_timestamp;

import edu.wustl.doc.facet.*;
import edu.wustl.doc.facet.EventComm.*;

aspect TimestampAspect {

	//
        // Mark a timestamp on the event as soon as it arrives
        //
	before (EventCarrier ec) :
		execution (void EventChannelImpl.pushEvent(EventCarrier))
		&& args(ec)
        {
		Event event = ec.getEvent();
		event.getHeader ().setTimestamp (System.currentTimeMillis ());
	}

        //
        // Add appropriate accessors
        //

        public long EventHeader.getTimestamp ()
        {
                return this.timestamp;
        }

        public void EventHeader.setTimestamp (long tstamp)
        {
                this.timestamp = tstamp;
        }
}
